package _Project.Mita.controller.api;

import java.util.List;
import java.util.NoSuchElementException;

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
import _Project.Mita.exception.ForbiddenOperationException;
import _Project.Mita.exception.NotAuthenticatedException;
import _Project.Mita.exception.ReservationNotAllowedException;
import _Project.Mita.form.ReservationRequest;
import _Project.Mita.response.AdminReservationResponse;
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

    /**
     * 新規予約を作成するAPI
     * 
     * @param request 予約したい書籍のIDを持つリクエスト
     * @param servletRequest セッション情報をもつリクエスト
     * @return 登録された予約情報
     * @throws NotAuthenticatedException ログイン情報が確認できなかったとき
     * @throws NoSuchElementException 書籍が見つからなかったとき
     * @throws ReservationNotAllowedException 予約ができる状態じゃなかった（在庫有か、予約済だった）とき
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(@Valid @RequestBody ReservationRequest request,
            HttpServletRequest servletRequest) {
        User user = sessionUserService.requireCurrentUser(servletRequest);
        return ReservationResponse.from(reservationService.create(user, request));
    }

    /**
     * 一般ユーザーが自身の予約をキャンセルするAPI
     * 
     * @param id キャンセル対象の予約ID
     * @param servletRequest セッション情報をもつリクエスト
     * @return キャンセル処理が行われた予約情報
     * @throws NotAuthenticatedException ログイン情報が確認できなかったとき
     * @throws ForbiddenOperationException 操作ユーザーと予約情報のユーザーが異なるとき
     * @throws NoSuchElementException 予約が見つからなかったとき
     */
    @PostMapping("/{id}/cancel")
    public ReservationResponse cancel(@PathVariable("id") Long id, HttpServletRequest servletRequest) {
        User user = sessionUserService.requireCurrentUser(servletRequest);
        return ReservationResponse.from(reservationService.cancel(user, id));
    }

    /**
     * ログイン中のユーザー自身の予約一覧を取得するAPI
     * 
     * @param servletRequest セッション情報をもつリクエスト
     * @return ログインユーザーの予約情報レスポンスのリスト
     * @throws NotAuthenticatedException ログイン情報が確認できなかったとき
     */
    @GetMapping("/me")
    public List<ReservationResponse> myReservations(HttpServletRequest servletRequest) {
        User user = sessionUserService.requireCurrentUser(servletRequest);
        return reservationService.findMyReservations(user).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    /**
     * 管理者向けにすべての予約一覧を取得するAPI
     * 
     * @return システム内の全予約情報を含んだ管理者用レスポンスのリスト
     */
    @GetMapping
    public List<AdminReservationResponse> list() {
        return reservationService.findAllForAdmin().stream()
                .map(AdminReservationResponse::from)
                .toList();
    }
    
    /**
     * 管理者権限で指定された予約を強制キャンセルするAPI
     * 
     * @param id キャンセル対象の予約ID
     * @return キャンセル処理が行われた予約情報
     * @throws NoSuchElementException 予約が見つからなかったとき
     */
    @PostMapping("/{id}/admin-cancel")
    public ReservationResponse adminCancel(@PathVariable("id") Long id) {
        return ReservationResponse.from(reservationService.adminCancel(id));
    }
}
