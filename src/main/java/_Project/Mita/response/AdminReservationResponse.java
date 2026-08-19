package _Project.Mita.response;

import java.time.LocalDateTime;

import _Project.Mita.entity.Reservation;

public record AdminReservationResponse(
        Long reservationId,
        Long bookId,
        String bookTitle,
        Long userId,
        String userName,
        LocalDateTime reservedAt,
        String status) {

    public static AdminReservationResponse from(Reservation reservation) {
        return new AdminReservationResponse(
                reservation.getReservationId(),
                reservation.getBook().getBookId(),
                reservation.getBook().getTitle(),
                reservation.getUser().getUserId(),
                reservation.getUser().getName(),
                reservation.getReservedAt(),
                reservation.getStatus().name());
    }
}
