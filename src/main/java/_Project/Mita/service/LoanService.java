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
	
	/**
	 * ユーザー用に自分の貸出情報（履歴含む）を取得するメソッド
	 * 
	 * @param user ユーザ情報
	 * @return ユーザーの貸出情報の一覧
	 */
	@Transactional(readOnly = true)
	public List<Loan> findMyLoans(User user) {
		return loanRepository.findByUser_UserIdOrderByLoanDateDesc(user.getUserId());
	}

	/**
	 * 貸出情報を作成しDBに登録させるメソッド。
	 * 
	 * User情報と書籍情報、予約情報も取得して貸出可能かを確認している。
	 * 
	 * @param user 貸出するユーザー
	 * @param request 借りたい書籍のID（NotNull）をもつリクエスト
	 * @return 作成された貸出情報を表すLoanエンティティ
	 * @throws NoSuchElementException Bookから貸出予定の本情報が取得できなかったとき
	 * @throws ReservationHeldException 確保予約が他人名義で存在し、貸出情報が作成できないとき
	 * @throws BookNotAvailableException 貸出できる状態の本がない（在庫がない）とき
	 * @throws DuplicateLoanException すでにその本が貸し出されていたとき
	 * @throws ConcurrentUpdateException 書き換えた情報の競合を検知し、貸出情報作成が中断したとき
	 */
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

	/**
	 * 管理者用に貸出情報を取得するメソッド
	 * 
	 * @param overdueOnly 期限切れを探すかどうかのスイッチ。trueなら延滞中の貸出のみに絞り込み、falseなら全件を貸出日降順で返す。
	 * @return 条件に合った貸出情報の一覧
	 */
	@Transactional(readOnly = true)
	public List<Loan> findAllForAdmin(boolean overdueOnly) {
		if (overdueOnly) {
			return loanRepository.findByReturnDateIsNullAndDueDateBeforeOrderByDueDateAsc(LocalDate.now());
		}
		return loanRepository.findAllByOrderByLoanDateDesc();
	}
	
	/**
	 * 貸出情報を探し、返却情報を追加するメソッド
	 * 
	 * @param user 返却するユーザー情報
	 * @param loanId 返却手続きをする貸出情報
	 * @return 返却済みにしたLoanエンティティ
	 * @throws NoSuchElementException 指定された貸出記録が存在しないとき
	 * @throws ForbiddenOperationException 操作ユーザーと貸出情報のユーザーが異なるとき
	 * @throws AlreadyReturnedException すでに返却情報が登録されていたとき
	 * @throws ConcurrentUpdateException 書き換えた情報の競合を検知し、返却情報作成が中断したとき
	 */
	public Loan returnBook(User user, Long loanId) {
		Loan loan = getLoanOrThrow(loanId);

		if (!loan.getUser().getUserId().equals(user.getUserId())) {
			throw new ForbiddenOperationException("他のユーザーの貸出は返却できません");
		}

		return doReturn(loan);
	}

	/**
	 * 管理者用の返却状態を登録するメソッド
	 * 
	 * @param loanId 貸出情報のID
	 * @return 返却済みにしたLoanエンティティ
	 * @throws NoSuchElementException 指定された貸出記録が存在しないとき
	 * @throws AlreadyReturnedException すでに返却情報が登録されていたとき
	 * @throws ConcurrentUpdateException 書き換えた情報の競合を検知し、返却情報作成が中断したとき
	 */
	public Loan adminReturnBook(Long loanId) {
		Loan loan = getLoanOrThrow(loanId);
		return doReturn(loan);
	}

	/**
	 * 受け取ったIDから貸出情報を探し、なかったら例外をスローする。
	 */
	private Loan getLoanOrThrow(Long loanId) {
		return loanRepository.findById(loanId)
				.orElseThrow(() -> new NoSuchElementException("貸出情報が見つかりません: id=" + loanId));
	}

	/**
	 * 例外かどうかを検査し返却情報を登録する。その後在庫数を増やし待機中の予約を繰り上げる。
	 * 
	 * @param loan 返却対象としている貸出情報
	 * @return 正常な返却情報
	 * @throws AlreadyReturnedException すでに返却情報が登録されていたとき
	 * @throws ConcurrentUpdateException 書き換えた情報の競合を検知し、中断したとき
	 */
	private Loan doReturn(Loan loan) {
		if (loan.getReturnDate() != null) {
			throw new AlreadyReturnedException("この貸出は既に返却済みです");
		}

		loan.setReturnDate(LocalDate.now());
		loanRepository.save(loan);

		Book book = loan.getBook();
		book.setAvailableCopies(book.getAvailableCopies() + 1);
		try {
			bookRepository.saveAndFlush(book);
		} catch (OptimisticLockingFailureException e) {
			throw new ConcurrentUpdateException("他の操作と競合しました。もう一度お試しください");
		}

		reservationService.promoteNextWaitingReservation(book.getBookId());

		return loan;
	}
}
