package _Project.Mita.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import _Project.Mita.entity.Book;
import _Project.Mita.entity.Loan;
import _Project.Mita.entity.Reservation;
import _Project.Mita.entity.ReservationStatus;
import _Project.Mita.entity.User;
import _Project.Mita.exception.AlreadyReturnedException;
import _Project.Mita.exception.BookNotAvailableException;
import _Project.Mita.exception.ConcurrentUpdateException;
import _Project.Mita.exception.DuplicateLoanException;
import _Project.Mita.exception.ForbiddenOperationException;
import _Project.Mita.exception.ReservationHeldException;
import _Project.Mita.form.LoanRequest;
import _Project.Mita.repository.BookRepository;
import _Project.Mita.repository.LoanRepository;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private LoanService loanService;

    private User user;
    private Book book;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);
        user.setName("利用者A");

        book = new Book();
        book.setBookId(10L);
        book.setTitle("テスト書籍");
        book.setTotalCopies(1);
        book.setAvailableCopies(1);
    }

    @Test
    void create_在庫がある書籍を貸出できる() {
        when(bookRepository.findById(book.getBookId())).thenReturn(Optional.of(book));
        when(loanRepository.findByBook_BookIdAndUser_UserIdAndReturnDateIsNull(book.getBookId(), user.getUserId()))
                .thenReturn(Optional.empty());

        Loan loan = loanService.create(user, new LoanRequest(book.getBookId()));

        assertThat(book.getAvailableCopies()).isEqualTo(0);
        assertThat(loan.getBook()).isEqualTo(book);
        assertThat(loan.getUser()).isEqualTo(user);
        assertThat(loan.getDueDate()).isEqualTo(loan.getLoanDate().plusDays(14));
    }

    @Test
    void create_在庫0の書籍を貸出しようとするとBookNotAvailableException() {
        book.setAvailableCopies(0);
        when(bookRepository.findById(book.getBookId())).thenReturn(Optional.of(book));

        assertThrows(BookNotAvailableException.class,
                () -> loanService.create(user, new LoanRequest(book.getBookId())));
    }

    @Test
    void create_同じ本を重複して貸出しようとするとDuplicateLoanException() {
        when(bookRepository.findById(book.getBookId())).thenReturn(Optional.of(book));
        when(loanRepository.findByBook_BookIdAndUser_UserIdAndReturnDateIsNull(book.getBookId(), user.getUserId()))
                .thenReturn(Optional.of(new Loan()));

        assertThrows(DuplicateLoanException.class,
                () -> loanService.create(user, new LoanRequest(book.getBookId())));
    }

    @Test
    void create_楽観的ロック競合が発生するとConcurrentUpdateException() {
        when(bookRepository.findById(book.getBookId())).thenReturn(Optional.of(book));
        when(loanRepository.findByBook_BookIdAndUser_UserIdAndReturnDateIsNull(book.getBookId(), user.getUserId()))
                .thenReturn(Optional.empty());
        when(bookRepository.saveAndFlush(book)).thenThrow(new OptimisticLockingFailureException("conflict"));

        assertThrows(ConcurrentUpdateException.class,
                () -> loanService.create(user, new LoanRequest(book.getBookId())));
    }

    @Test
    void returnBook_返却するとavailableCopiesが1増えreturnDateが設定される() {
        book.setAvailableCopies(0);
        Loan loan = new Loan();
        loan.setLoanId(100L);
        loan.setBook(book);
        loan.setUser(user);
        loan.setLoanDate(LocalDate.now().minusDays(1));
        loan.setDueDate(LocalDate.now().plusDays(13));
        when(loanRepository.findById(loan.getLoanId())).thenReturn(Optional.of(loan));

        Loan result = loanService.returnBook(user, loan.getLoanId());

        assertThat(result.getReturnDate()).isEqualTo(LocalDate.now());
        assertThat(book.getAvailableCopies()).isEqualTo(1);
    }

    @Test
    void returnBook_他人の貸出を返却しようとするとForbiddenOperationException() {
        User otherUser = new User();
        otherUser.setUserId(2L);

        Loan loan = new Loan();
        loan.setLoanId(100L);
        loan.setBook(book);
        loan.setUser(otherUser);
        when(loanRepository.findById(loan.getLoanId())).thenReturn(Optional.of(loan));

        assertThrows(ForbiddenOperationException.class, () -> loanService.returnBook(user, loan.getLoanId()));
    }

    @Test
    void returnBook_返却時にWAITING予約の昇格処理が呼ばれる() {
        Loan loan = new Loan();
        loan.setLoanId(100L);
        loan.setBook(book);
        loan.setUser(user);
        loan.setLoanDate(LocalDate.now().minusDays(1));
        loan.setDueDate(LocalDate.now().plusDays(13));
        when(loanRepository.findById(loan.getLoanId())).thenReturn(Optional.of(loan));

        loanService.returnBook(user, loan.getLoanId());

        verify(reservationService).promoteNextWaitingReservation(book.getBookId());
    }

    @Test
    void create_AVAILABLE予約が他ユーザーのものだとReservationHeldException() {
        User otherUser = new User();
        otherUser.setUserId(99L);

        Reservation availableReservation = new Reservation();
        availableReservation.setReservationId(500L);
        availableReservation.setBook(book);
        availableReservation.setUser(otherUser);
        availableReservation.setStatus(ReservationStatus.AVAILABLE);

        when(bookRepository.findById(book.getBookId())).thenReturn(Optional.of(book));
        when(reservationService.findAvailableReservation(book.getBookId()))
                .thenReturn(Optional.of(availableReservation));

        assertThrows(ReservationHeldException.class,
                () -> loanService.create(user, new LoanRequest(book.getBookId())));
    }

    @Test
    void create_AVAILABLE予約の本人が借りると貸出成功し予約がCOMPLETEDになる() {
        Reservation availableReservation = new Reservation();
        availableReservation.setReservationId(500L);
        availableReservation.setBook(book);
        availableReservation.setUser(user);
        availableReservation.setStatus(ReservationStatus.AVAILABLE);

        when(bookRepository.findById(book.getBookId())).thenReturn(Optional.of(book));
        when(reservationService.findAvailableReservation(book.getBookId()))
                .thenReturn(Optional.of(availableReservation));
        when(loanRepository.findByBook_BookIdAndUser_UserIdAndReturnDateIsNull(book.getBookId(), user.getUserId()))
                .thenReturn(Optional.empty());

        Loan loan = loanService.create(user, new LoanRequest(book.getBookId()));

        assertThat(loan.getUser()).isEqualTo(user);
        assertThat(book.getAvailableCopies()).isEqualTo(0);
        verify(reservationService).completeReservation(availableReservation.getReservationId());
    }

    @Test
    void returnBook_既に返却済みの貸出を返却しようとするとAlreadyReturnedException() {
        Loan loan = new Loan();
        loan.setLoanId(100L);
        loan.setBook(book);
        loan.setUser(user);
        loan.setLoanDate(LocalDate.now().minusDays(5));
        loan.setDueDate(LocalDate.now().plusDays(9));
        loan.setReturnDate(LocalDate.now());
        when(loanRepository.findById(loan.getLoanId())).thenReturn(Optional.of(loan));

        assertThrows(AlreadyReturnedException.class, () -> loanService.returnBook(user, loan.getLoanId()));
    }
}
