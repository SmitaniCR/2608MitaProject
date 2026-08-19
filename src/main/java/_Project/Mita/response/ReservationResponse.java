package _Project.Mita.response;

import java.time.LocalDateTime;

import _Project.Mita.entity.Reservation;

public record ReservationResponse(
        Long reservationId,
        Long bookId,
        String bookTitle,
        LocalDateTime reservedAt,
        String status) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getReservationId(),
                reservation.getBook().getBookId(),
                reservation.getBook().getTitle(),
                reservation.getReservedAt(),
                reservation.getStatus().name());
    }
}
