package _Project.Mita.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import _Project.Mita.entity.Reservation;
import _Project.Mita.entity.enu.ReservationStatus;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUser_UserIdOrderByReservedAtDesc(Long userId);

    List<Reservation> findByBook_BookIdAndUser_UserIdAndStatusIn(
            Long bookId, Long userId, List<ReservationStatus> statuses);

    Optional<Reservation> findFirstByBook_BookIdAndStatusOrderByReservedAtAsc(Long bookId, ReservationStatus status);

    List<Reservation> findAllByOrderByReservedAtDesc();
}
