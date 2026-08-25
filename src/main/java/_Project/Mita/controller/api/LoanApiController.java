package _Project.Mita.controller.api;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import _Project.Mita.entity.Loan;
import _Project.Mita.entity.User;
import _Project.Mita.exception.AlreadyReturnedException;
import _Project.Mita.exception.BookNotAvailableException;
import _Project.Mita.exception.ConcurrentUpdateException;
import _Project.Mita.exception.DuplicateLoanException;
import _Project.Mita.exception.ForbiddenOperationException;
import _Project.Mita.exception.NotAuthenticatedException;
import _Project.Mita.exception.ReservationHeldException;
import _Project.Mita.form.LoanRequest;
import _Project.Mita.response.AdminLoanResponse;
import _Project.Mita.response.LoanResponse;
import _Project.Mita.service.LoanService;
import _Project.Mita.service.SessionUserService;

@RestController
@RequestMapping("/api/loans")
public class LoanApiController {

    private final LoanService loanService;
    private final SessionUserService sessionUserService;

    public LoanApiController(LoanService loanService, SessionUserService sessionUserService) {
        this.loanService = loanService;
        this.sessionUserService = sessionUserService;
    }

    /**
     * 貸出情報を作成するAPI
     * 
     * @param request 借りたい書籍のID（NotNull）をもつリクエスト
     * @param servletRequest セッション情報をもつリクエスト
     * @return 登録された貸出情報
     * @throws NotAuthenticatedException ログインされていなかったとき
     * @throws NoSuchElementException Bookから貸出予定の本情報が取得できなかったとき
     * @throws ReservationHeldException 確保予約が他人名義で存在し、貸出情報が作成できないとき
     * @throws BookNotAvailableException 貸出できる状態の本がない（在庫がない）とき
     * @throws DuplicateLoanException すでにその本が貸し出されていたとき
     * @throws ConcurrentUpdateException 書き換えた情報の競合を検知し、貸出情報作成が中断したとき
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LoanResponse create(@Valid @RequestBody LoanRequest request, HttpServletRequest servletRequest) {
        User user = sessionUserService.requireCurrentUser(servletRequest);
        return LoanResponse.from(loanService.create(user, request.bookId()));
    }

    /**
     * 返却情報を登録するAPI
     * 
     * @param id 返却したい貸出情報ID
     * @param servletRequest セッション情報をもつリクエスト
     * @return 登録された返却情報
     * @throws NotAuthenticatedException ログインされていなかったとき
     * @throws NoSuchElementException 指定された貸出記録が存在しないとき
     * @throws ForbiddenOperationException 操作ユーザーと貸出情報のユーザーが異なるとき
     * @throws AlreadyReturnedException すでに返却情報が登録されていたとき
     * @throws ConcurrentUpdateException 書き換えた情報の競合を検知し、貸出情報作成が中断したとき
     */
    @PostMapping("/{id}/return")
    public LoanResponse returnBook(@PathVariable("id") Long id, HttpServletRequest servletRequest) {
        User user = sessionUserService.requireCurrentUser(servletRequest);
        return LoanResponse.from(loanService.returnBook(user, id));
    }

    /**
     * ユーザーの貸出情報の一覧を取得するAPI
     * 
     * @param servletRequest セッション情報をもつリクエスト
     * @return ユーザーの貸出情報一覧
     * @throws NotAuthenticatedException ログインされていなかったとき
     */
    @GetMapping("/me")
    public List<LoanResponse> myLoans(HttpServletRequest servletRequest) {
        User user = sessionUserService.requireCurrentUser(servletRequest);
        return loanService.findMyLoans(user).stream()
                .map(LoanResponse::from)
                .toList();
    }

    /**
     * 管理者用の貸出情報一覧取得API(ADMIN権限が必要)
     * 
     * @param overdueOnly 期限切れを探すかどうかのスイッチ。trueなら延滞中の貸出のみに絞り込み、falseなら全件を貸出日降順で返す。
     * @return 条件に沿った貸出情報一覧
     */
    @GetMapping
    public List<AdminLoanResponse> list(
            @RequestParam(value = "overdueOnly", required = false, defaultValue = "false") boolean overdueOnly) {
        return loanService.findAllForAdmin(overdueOnly).stream()
                .map(AdminLoanResponse::from)
                .toList();
    }

    /**
     * 管理者による返却登録API(ADMIN権限が必要)
     * 
     * @param id 貸出情報のID
     * @return 登録された返却情報
     * @throws NoSuchElementException 指定された貸出記録が存在しないとき
     * @throws AlreadyReturnedException すでに返却情報が登録されていたとき
     * @throws ConcurrentUpdateException 書き換えた情報の競合を検知し、貸出情報作成が中断したとき
     */
    @PostMapping("/{id}/admin-return")
    public LoanResponse adminReturn(@PathVariable("id") Long id) {
        return LoanResponse.from(loanService.adminReturnBook(id));
    }

    /**
     * 管理者用に貸出情報をCSVファイルとしてエクスポートするAPI(ADMIN権限が必要)
     * 
     * @return 貸出情報の一覧CSV
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> export() {
        StringBuilder csv = new StringBuilder("貸出ID,書籍タイトル,利用者名,貸出日,返却期限,返却日,延滞フラグ\r\n");
        LocalDate today = LocalDate.now();
        for (Loan loan : loanService.findAllForAdmin(false)) {
            boolean overdue = loan.getReturnDate() == null && loan.getDueDate().isBefore(today);
            csv.append(CsvUtils.escape(loan.getLoanId())).append(',')
                    .append(CsvUtils.escape(loan.getBook().getTitle())).append(',')
                    .append(CsvUtils.escape(loan.getUser().getName())).append(',')
                    .append(CsvUtils.escape(loan.getLoanDate())).append(',')
                    .append(CsvUtils.escape(loan.getDueDate())).append(',')
                    .append(CsvUtils.escape(loan.getReturnDate())).append(',')
                    .append(CsvUtils.escape(overdue ? "延滞" : "")).append("\r\n");
        }
        return CsvUtils.buildResponse("loans.csv", csv.toString());
    }
}
