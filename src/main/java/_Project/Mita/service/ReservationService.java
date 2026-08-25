package _Project.Mita.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import _Project.Mita.entity.Book;
import _Project.Mita.entity.Reservation;
import _Project.Mita.entity.User;
import _Project.Mita.entity.enums.ReservationStatus;
import _Project.Mita.exception.ForbiddenOperationException;
import _Project.Mita.exception.ReservationNotAllowedException;
import _Project.Mita.form.ReservationRequest;
import _Project.Mita.repository.BookRepository;
import _Project.Mita.repository.ReservationRepository;

@Service
@Transactional
public class ReservationService {

    private static final List<ReservationStatus> ACTIVE_STATUSES =
            List.of(ReservationStatus.WAITING, ReservationStatus.AVAILABLE);

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;

    public ReservationService(ReservationRepository reservationRepository, BookRepository bookRepository) {
        this.reservationRepository = reservationRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    public List<Reservation> findMyReservations(User user) {
        return reservationRepository.findByUser_UserIdOrderByReservedAtDesc(user.getUserId());
    }

    public Reservation create(User user, ReservationRequest request) {
        Book book = bookRepository.findByIdForUpdate(request.bookId())
                .orElseThrow(() -> new NoSuchElementException("書籍が見つかりません: id=" + request.bookId()));

        if (book.getAvailableCopies() >= 1) {
            throw new ReservationNotAllowedException("在庫があるため貸出をご利用ください");
        }

        boolean alreadyReserved = !reservationRepository
                .findByBook_BookIdAndUser_UserIdAndStatusIn(book.getBookId(), user.getUserId(), ACTIVE_STATUSES)
                .isEmpty();
        if (alreadyReserved) {
            throw new ReservationNotAllowedException("この書籍は既に予約済みです");
        }

        Reservation reservation = new Reservation();
        reservation.setBook(book);
        reservation.setUser(user);
        reservation.setReservedAt(LocalDateTime.now());
        reservation.setStatus(ReservationStatus.WAITING);
        return reservationRepository.save(reservation);
    }

    @Transactional(readOnly = true)
    public List<Reservation> findAllForAdmin() {
        return reservationRepository.findAllByOrderByReservedAtDesc();
    }

    public Reservation cancel(User user, Long reservationId) {
        Reservation reservation = getReservationOrThrow(reservationId);

        if (!reservation.getUser().getUserId().equals(user.getUserId())) {
            throw new ForbiddenOperationException("他のユーザーの予約は操作できません");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservationRepository.save(reservation);
    }

    public Reservation adminCancel(Long reservationId) {
        Reservation reservation = getReservationOrThrow(reservationId);
        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservationRepository.save(reservation);
    }

    private Reservation getReservationOrThrow(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NoSuchElementException("予約情報が見つかりません: id=" + reservationId));
    }

    public void promoteNextWaitingReservation(Long bookId) {
        reservationRepository.findFirstByBook_BookIdAndStatusOrderByReservedAtAsc(bookId, ReservationStatus.WAITING)
                .ifPresent(reservation -> {
                    reservation.setStatus(ReservationStatus.AVAILABLE);
                    reservationRepository.save(reservation);
                });
    }

    @Transactional(readOnly = true)
    public Optional<Reservation> findAvailableReservation(Long bookId) {
        return reservationRepository.findFirstByBook_BookIdAndStatusOrderByReservedAtAsc(
                bookId, ReservationStatus.AVAILABLE);
    }

    public void completeReservation(Long reservationId) {
        Reservation reservation = getReservationOrThrow(reservationId);
        reservation.setStatus(ReservationStatus.COMPLETED);
        reservationRepository.save(reservation);
    }
}
