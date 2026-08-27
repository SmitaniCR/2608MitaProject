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

    /**
     * 指定されたユーザー自身の予約一覧を、予約日時の降順で取得する
     * 
     * @param user 取得対象のユーザーエンティティ
     * @return 該当ユーザーの予約エンティティリスト
     */
    @Transactional(readOnly = true)
    public List<Reservation> findMyReservations(User user) {
        return reservationRepository.findByUser_UserIdOrderByReservedAtDesc(user.getUserId());
    }

    /**
     * 新規に書籍の予約を登録する
     * 
     * @param user 予約を行うユーザーエンティティ
     * @param request 予約対象の書籍IDを含むリクエスト
     * @return 予約エンティティ
     * @throws NoSuchElementException 指定された書籍IDが存在しないとき
     * @throws ReservationNotAllowedException 書籍の在庫があるとき、または既に同じ書籍を予約中のとき
     */
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

    /**
     * 管理者用に、システム内のすべての予約を予約日時の降順で取得する
     * 
     * @return すべての予約エンティティリスト
     */
    @Transactional(readOnly = true)
    public List<Reservation> findAllForAdmin() {
        return reservationRepository.findAllByOrderByReservedAtDesc();
    }

    /**
     * 一般ユーザーが自身の予約をキャンセルする
     * 
     * @param user キャンセルを実行するユーザー
     * @param reservationId キャンセル対象の予約ID
     * @return キャンセル状態に更新された予約
     * @throws NoSuchElementException 指定された予約IDが存在しないとき
     * @throws ForbiddenOperationException 他のユーザーの予約をキャンセルしようとしたとき
     */
    public Reservation cancel(User user, Long reservationId) {
        Reservation reservation = getReservationOrThrow(reservationId);

        if (!reservation.getUser().getUserId().equals(user.getUserId())) {
            throw new ForbiddenOperationException("他のユーザーの予約は操作できません");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservationRepository.save(reservation);
    }

    /**
     * 管理者が指定された予約を強制キャンセルする
     * 
     * @param reservationId キャンセル対象の予約ID
     * @return キャンセル状態に更新された予約エンティティ
     * @throws NoSuchElementException 指定された予約IDが存在しないとき
     */
    public Reservation adminCancel(Long reservationId) {
        Reservation reservation = getReservationOrThrow(reservationId);
        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservationRepository.save(reservation);
    }

    /**
     * 予約IDを条件に予約情報を取得する
     * 
     * @param reservationId 検索対象の予約ID
     * @return 予約エンティティ
     * @throws NoSuchElementException 指定された予約IDが存在しないとき
     */
    private Reservation getReservationOrThrow(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NoSuchElementException("予約情報が見つかりません: id=" + reservationId));
    }

    /**
     * 返却等に伴い、指定された書籍の先頭予約を繰り上げる
     * 
     * @param bookId 繰り上げ対象の書籍ID
     */
    public void promoteNextWaitingReservation(Long bookId) {
        reservationRepository.findFirstByBook_BookIdAndStatusOrderByReservedAtAsc(bookId, ReservationStatus.WAITING)
                .ifPresent(reservation -> {
                    reservation.setStatus(ReservationStatus.AVAILABLE);
                    reservationRepository.save(reservation);
                });
    }

    /**
     * 指定された書籍で、既にAVAILABLEとなっている最古の予約情報を取得する
     * 
     * @param bookId 検索対象の書籍ID
     * @return 引渡可能な予約情報（存在しない場合はOptional.empty()）
     */
    @Transactional(readOnly = true)
    public Optional<Reservation> findAvailableReservation(Long bookId) {
        return reservationRepository.findFirstByBook_BookIdAndStatusOrderByReservedAtAsc(
                bookId, ReservationStatus.AVAILABLE);
    }

    /**
     * 書籍の引渡が完了した予約を完了状態にする
     * 
     * @param reservationId 完了状態にする予約ID
     * @throws NoSuchElementException 指定された予約IDが存在しないとき
     */
    public void completeReservation(Long reservationId) {
        Reservation reservation = getReservationOrThrow(reservationId);
        reservation.setStatus(ReservationStatus.COMPLETED);
        reservationRepository.save(reservation);
    }
}
