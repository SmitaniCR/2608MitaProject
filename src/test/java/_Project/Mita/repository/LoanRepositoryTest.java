package _Project.Mita.repository;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import _Project.Mita.entity.Book;
import _Project.Mita.entity.Category;
import _Project.Mita.entity.Loan;
import _Project.Mita.entity.User;
import _Project.Mita.response.MonthlyLoanCountView;

@DataJpaTest
class LoanRepositoryTest {

	@Autowired
	private LoanRepository loanRepository;

	@Autowired
	private TestEntityManager entityManager;// @DataJpaTest専用の「テストデータを仕込むための道具」

	private User createUser(String name, String email) {
		User user = new User();
		user.setName(name);
		user.setEmail(email);
		user.setPassword("password");
		return entityManager.persist(user);
	}

	private Category createCategory(String name) {
		Category category = new Category();
		category.setCategoryName(name);
		return entityManager.persist(category);
	}

	private Book createBook(String title, Category category) {
		Book book = new Book();
		book.setTitle(title);
		book.setCategory(category);
		return entityManager.persist(book);
	}

	private Loan createLoan(Book book, User user, LocalDate loanDate, LocalDate dueDate, LocalDate returnDate) {
		Loan loan = new Loan();
		loan.setBook(book);
		loan.setUser(user);
		loan.setLoanDate(loanDate);
		loan.setDueDate(dueDate);
		loan.setReturnDate(returnDate);
		return entityManager.persist(loan);
	}

	@Test
	void 未返却かつ期日超過のデータのみが期日の昇順で取得できること() {

		// 1. 準備：親データ（マスターデータ）の作成

		// プライベートメソッドを使って各1行で作成・保存
		Category category = createCategory("プログラミング");
		Book book = createBook("Java入門", category);
		User user = createUser("テスト太郎", "a@a"); // メソッド内で自動でpersist

		LocalDate targetDate = LocalDate.of(2026, 8, 25);

		// Loanデータの準備（1データ1行なので、データの条件比較がしやすい）
		createLoan(book, user, targetDate, targetDate.minusDays(1), null); // 期待するデータ①
		createLoan(book, user, targetDate, targetDate.minusDays(2), null); // 期待するデータ②
		createLoan(book, user, targetDate, targetDate.minusDays(1), targetDate.minusDays(1)); // 除外データ①
		createLoan(book, user, targetDate, targetDate, null); // 除外データ②
		createLoan(book, user, targetDate, targetDate.plusDays(1), targetDate.plusDays(1)); // 除外データ③

		// キャッシュをクリアして確実にDBから検証データを取得する
		entityManager.flush();
		entityManager.clear();

		// 2. 実行：リポジトリメソッドの呼び出し

		List<Loan> result = loanRepository.findByReturnDateIsNullAndDueDateBeforeOrderByDueDateAsc(targetDate);

		// 3. 検証：結果の確認

		// 取得された件数が2件であることを検証
		assertThat(result).hasSize(2);

		// OrderByDueDateAscの通り、より日付の古いloan2が先頭に来ていることを検証
		assertThat(result.get(0).getDueDate()).isEqualTo(targetDate.minusDays(2));
		assertThat(result.get(1).getDueDate()).isEqualTo(targetDate.minusDays(1));

		// 除外データが含まれていないことを、IDなどを元に厳密にチェック
		assertThat(result).extracting(Loan::getReturnDate).containsOnlyNulls();
	}

	@Test
	void 指定したユーザーの貸出履歴のみが貸出日の降順で取得できること() {

		// 1. 準備

		Category category = createCategory("プログラミング");
		Book book = createBook("Java入門", category);

		// テスト対象のユーザーA と 除外対象のユーザーB
		User userA = createUser("ユーザーA", "a@a");
		User userB = createUser("ユーザーB", "b@b");

		LocalDate baseDate = LocalDate.of(2026, 8, 25);

		// 期待するデータ①
		createLoan(book, userA, baseDate, baseDate.plusDays(7), null);

		// 期待するデータ②
		createLoan(book, userA, baseDate.minusDays(1), baseDate.plusDays(6), null);

		// 除外データ
		createLoan(book, userB, baseDate, baseDate.plusDays(7), null);

		entityManager.flush();
		entityManager.clear();

		// 2. 実行

		List<Loan> result = loanRepository.findByUser_UserIdOrderByLoanDateDesc(userA.getUserId());

		// 3. 検証

		assertThat(result).hasSize(2);

		// 貸出日の降順の検証
		assertThat(result.get(0).getLoanDate()).isEqualTo(baseDate);
		assertThat(result.get(1).getLoanDate()).isEqualTo(baseDate.minusDays(1));

		// すべてユーザーAのデータであることの検証
		assertThat(result).extracting(l -> l.getUser().getUserId()).containsOnly(userA.getUserId());
	}

	@Test
	void 指定した本とユーザーの未返却データが取得できること() {

		// 1. 準備

		Category category = createCategory("プログラミング");

		// テスト対象の本A、除外対象の本B
		Book bookA = createBook("Java入門", category);
		Book bookB = createBook("Spring入門", category);

		// テスト対象のユーザーA、除外対象のユーザーB
		User userA = createUser("太郎", "a@a");
		User userB = createUser("次郎", "b@b");

		LocalDate baseDate = LocalDate.of(2026, 8, 25);

		// 期待するデータ
		Loan loan1 = createLoan(bookA, userA, baseDate, baseDate.plusDays(7), null);

		// 除外データ
		createLoan(bookA, userA, baseDate, baseDate.plusDays(7), baseDate.plusDays(1));

		// 除外データ
		createLoan(bookB, userA, baseDate, baseDate.plusDays(7), null);

		// 除外データ
		createLoan(bookA, userB, baseDate, baseDate.plusDays(7), null);

		entityManager.flush();
		entityManager.clear();

		// 2. 実行

		Optional<Loan> result = loanRepository.findByBook_BookIdAndUser_UserIdAndReturnDateIsNull(bookA.getBookId(),
				userA.getUserId());

		// 3. 検証

		assertThat(result).isPresent();
		assertThat(result.get().getLoanId()).isEqualTo(loan1.getLoanId());
	}

	@Test
	void すべての貸出データが貸出日の降順で取得できること() {

		// 1. 準備

		Category category = createCategory("プログラミング");
		Book book = createBook("Java入門", category);
		User user = createUser("テスト太郎", "a@a");

		LocalDate baseDate = LocalDate.of(2026, 8, 25);

		// 貸出日：昨日
		createLoan(book, user, baseDate.minusDays(1), baseDate.plusDays(6), null);

		// 貸出日：今日
		createLoan(book, user, baseDate, baseDate.plusDays(7), null);

		// 貸出日：一昨日
		createLoan(book, user, baseDate.minusDays(2), baseDate.plusDays(5), null);

		entityManager.flush();
		entityManager.clear();

		// 2. 実行

		List<Loan> result = loanRepository.findAllByOrderByLoanDateDesc();

		// 3. 検証

		assertThat(result).hasSize(3);

		// 降順（新しい順）に並んでいること
		assertThat(result.get(0).getLoanDate()).isEqualTo(baseDate); // 今日
		assertThat(result.get(1).getLoanDate()).isEqualTo(baseDate.minusDays(1)); // 昨日
		assertThat(result.get(2).getLoanDate()).isEqualTo(baseDate.minusDays(2)); // 一昨日
	}

	@Test
	void 月ごとの貸出件数が集計され月の昇順で取得できること() {

		// 1. 準備：親データ（マスターデータ）の作成
		Category category = createCategory("test");
		Book book = createBook("test", category);
		User user = createUser("テスト", "a@a");

		// 異なる月の貸出データを仕込む
		// 2026年6月: 1件
		createLoan(book, user, LocalDate.of(2026, 6, 15), LocalDate.of(2026, 7, 15), null);

		// 2026, 8月: 2件 (順番をバラつかせるため、先に8月を登録)
		createLoan(book, user, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 9, 1), null);
		createLoan(book, user, LocalDate.of(2026, 8, 20), LocalDate.of(2026, 9, 20), null);

		// 2026年7月: 3件
		createLoan(book, user, LocalDate.of(2026, 7, 10), LocalDate.of(2026, 8, 10), null);
		createLoan(book, user, LocalDate.of(2026, 7, 20), LocalDate.of(2026, 8, 20), null);
		createLoan(book, user, LocalDate.of(2026, 7, 25), LocalDate.of(2026, 8, 25), null);

		// キャッシュをクリアして確実にDBから検証データを取得する
		entityManager.flush();
		entityManager.clear();

		// 2. 実行：リポジトリメソッドの呼び出し
		List<MonthlyLoanCountView> result = loanRepository.findMonthlyLoanCounts();

		// 3. 検証：結果の確認
		// 集計された月の種類が 3つ であることを検証
		assertThat(result).hasSize(3);

		// 件数の集計検証
		// 1番目: 2026年06月 -> 1件
		assertThat(result.get(0).getLoanYear()).isEqualTo(2026);
		assertThat(result.get(0).getLoanMonth()).isEqualTo(6);
		assertThat(result.get(0).getLoanCount()).isEqualTo(1);

		// 2番目: 2026年07月 -> 3件
		assertThat(result.get(1).getLoanYear()).isEqualTo(2026);
		assertThat(result.get(1).getLoanMonth()).isEqualTo(7);
		assertThat(result.get(1).getLoanCount()).isEqualTo(3);

		// 3番目: 2026年08月 -> 2件
		assertThat(result.get(2).getLoanYear()).isEqualTo(2026);
		assertThat(result.get(2).getLoanMonth()).isEqualTo(8);
		assertThat(result.get(2).getLoanCount()).isEqualTo(2);
	}

}
