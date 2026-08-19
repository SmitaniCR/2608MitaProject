package _Project.Mita.service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import _Project.Mita.entity.Book;
import _Project.Mita.entity.Loan;
import _Project.Mita.entity.Reservation;
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

@Service
@Transactional
public class LoanService {

    private static final int LOAN_PERIOD_DAYS = 14;

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final ReservationService reservationService;

    public LoanService(LoanRepository loanRepository, BookRepository bookRepository,
            ReservationService reservationService) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.reservationService = reservationService;
    }

    @Transactional(readOnly = true)
    public List<Loan> findMyLoans(User user) {
        return loanRepository.findByUser_UserIdOrderByLoanDateDesc(user.getUserId());
    }

    public Loan create(User user, LoanRequest request) {
        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new NoSuchElementException("書籍が見つかりません: id=" + request.bookId()));

        Optional<Reservation> availableReservation = reservationService.findAvailableReservation(book.getBookId());

        if (availableReservation.isPresent()) {
            if (!availableReservation.get().getUser().getUserId().equals(user.getUserId())) {
                throw new ReservationHeldException("この書籍は他のユーザーの予約のために確保されています");
            }
        } else if (book.getAvailableCopies() <= 0) {
            throw new BookNotAvailableException("この書籍は現在貸出可能な在庫がありません");
        }

        loanRepository.findByBook_BookIdAndUser_UserIdAndReturnDateIsNull(book.getBookId(), user.getUserId())
                .ifPresent(existing -> {
                    throw new DuplicateLoanException("この書籍は既に貸出中です");
                });

        LocalDate today = LocalDate.now();

        Loan loan = new Loan();
        loan.setBook(book);
        loan.setUser(user);
        loan.setLoanDate(today);
        loan.setDueDate(today.plusDays(LOAN_PERIOD_DAYS));
        loanRepository.save(loan);

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        try {
            bookRepository.saveAndFlush(book);
        } catch (OptimisticLockingFailureException e) {
            throw new ConcurrentUpdateException("他の操作と競合しました。もう一度お試しください");
        }

        availableReservation.ifPresent(
                reservation -> reservationService.completeReservation(reservation.getReservationId()));

        return loan;
    }

    @Transactional(readOnly = true)
    public List<Loan> findAllForAdmin(boolean overdueOnly) {
        if (overdueOnly) {
            return loanRepository.findByReturnDateIsNullAndDueDateBeforeOrderByDueDateAsc(LocalDate.now());
        }
        return loanRepository.findAllByOrderByLoanDateDesc();
    }

    public Loan returnBook(User user, Long loanId) {
        Loan loan = getLoanOrThrow(loanId);

        if (!loan.getUser().getUserId().equals(user.getUserId())) {
            throw new ForbiddenOperationException("他のユーザーの貸出は返却できません");
        }

        return doReturn(loan);
    }

    public Loan adminReturnBook(Long loanId) {
        Loan loan = getLoanOrThrow(loanId);
        return doReturn(loan);
    }

    private Loan getLoanOrThrow(Long loanId) {
        return loanRepository.findById(loanId)
                .orElseThrow(() -> new NoSuchElementException("貸出情報が見つかりません: id=" + loanId));
    }

    private Loan doReturn(Loan loan) {
        if (loan.getReturnDate() != null) {
            throw new AlreadyReturnedException("この貸出は既に返却済みです");
        }

        loan.setReturnDate(LocalDate.now());
        loanRepository.save(loan);

        Book book = loan.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        reservationService.promoteNextWaitingReservation(book.getBookId());

        return loan;
    }
}
