package _Project.Mita.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import _Project.Mita.entity.Book;
import _Project.Mita.entity.Reservation;
import _Project.Mita.entity.User;
import _Project.Mita.entity.enums.ReservationStatus;
import _Project.Mita.exception.ForbiddenOperationException;
import _Project.Mita.exception.ReservationNotAllowedException;
import _Project.Mita.form.ReservationRequest;
import _Project.Mita.repository.BookRepository;
import _Project.Mita.repository.ReservationRepository;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private ReservationService reservationService;

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
        book.setAvailableCopies(0);
    }

    @Test
    void create_在庫0の書籍を予約できる() {
        when(bookRepository.findByIdForUpdate(book.getBookId())).thenReturn(Optional.of(book));
        when(reservationRepository.findByBook_BookIdAndUser_UserIdAndStatusIn(book.getBookId(), user.getUserId(),
                List.of(ReservationStatus.WAITING, ReservationStatus.AVAILABLE)))
                        .thenReturn(List.of());
        when(reservationRepository.save(org.mockito.ArgumentMatchers.any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Reservation reservation = reservationService.create(user, new ReservationRequest(book.getBookId()));

        assertThat(reservation.getBook()).isEqualTo(book);
        assertThat(reservation.getUser()).isEqualTo(user);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.WAITING);
    }

    @Test
    void create_在庫がある書籍を予約しようとするとReservationNotAllowedException() {
        book.setAvailableCopies(1);
        when(bookRepository.findByIdForUpdate(book.getBookId())).thenReturn(Optional.of(book));

        assertThrows(ReservationNotAllowedException.class,
                () -> reservationService.create(user, new ReservationRequest(book.getBookId())));
    }

    @Test
    void create_同じ本を重複して予約しようとするとReservationNotAllowedException() {
        when(bookRepository.findByIdForUpdate(book.getBookId())).thenReturn(Optional.of(book));
        Reservation existing = new Reservation();
        existing.setBook(book);
        existing.setUser(user);
        existing.setStatus(ReservationStatus.WAITING);
        when(reservationRepository.findByBook_BookIdAndUser_UserIdAndStatusIn(book.getBookId(), user.getUserId(),
                List.of(ReservationStatus.WAITING, ReservationStatus.AVAILABLE)))
                        .thenReturn(List.of(existing));

        assertThrows(ReservationNotAllowedException.class,
                () -> reservationService.create(user, new ReservationRequest(book.getBookId())));
    }

    @Test
    void cancel_他人の予約をキャンセルしようとするとForbiddenOperationException() {
        User otherUser = new User();
        otherUser.setUserId(2L);

        Reservation reservation = new Reservation();
        reservation.setReservationId(200L);
        reservation.setBook(book);
        reservation.setUser(otherUser);
        reservation.setStatus(ReservationStatus.WAITING);
        when(reservationRepository.findById(reservation.getReservationId())).thenReturn(Optional.of(reservation));

        assertThrows(ForbiddenOperationException.class,
                () -> reservationService.cancel(user, reservation.getReservationId()));
    }
}
