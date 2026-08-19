package _Project.Mita.controller.api;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import _Project.Mita.entity.User;
import _Project.Mita.form.ReservationRequest;
import _Project.Mita.response.ReservationResponse;
import _Project.Mita.service.ReservationService;
import _Project.Mita.service.SessionUserService;

@RestController
@RequestMapping("/api/reservations")
public class ReservationApiController {

    private final ReservationService reservationService;
    private final SessionUserService sessionUserService;

    public ReservationApiController(ReservationService reservationService, SessionUserService sessionUserService) {
        this.reservationService = reservationService;
        this.sessionUserService = sessionUserService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(@Valid @RequestBody ReservationRequest request,
            HttpServletRequest servletRequest) {
        User user = sessionUserService.requireCurrentUser(servletRequest);
        return ReservationResponse.from(reservationService.create(user, request));
    }

    @PostMapping("/{id}/cancel")
    public ReservationResponse cancel(@PathVariable("id") Long id, HttpServletRequest servletRequest) {
        User user = sessionUserService.requireCurrentUser(servletRequest);
        return ReservationResponse.from(reservationService.cancel(user, id));
    }

    @GetMapping("/me")
    public List<ReservationResponse> myReservations(HttpServletRequest servletRequest) {
        User user = sessionUserService.requireCurrentUser(servletRequest);
        return reservationService.findMyReservations(user).stream()
                .map(ReservationResponse::from)
                .toList();
    }
}
