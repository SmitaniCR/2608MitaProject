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
		createLoan(book, user, targetDate, targetDate.minusDays(1), null); // 期待するデータ①：未返却（Null）かつ 期日超過（前日）
		createLoan(book, user, targetDate, targetDate.minusDays(2), null); // 期待するデータ②：未返却（Null）かつ 期日超過（2日前） -> loan1より古いため、こちらが1番目に取得されるべき
		createLoan(book, user, targetDate, targetDate.minusDays(1), targetDate.minusDays(1)); // 除外データ①：返却済（NotNull）かつ 期日超過
		createLoan(book, user, targetDate, targetDate, null); // 除外データ②：未返却（Null）かつ 期日当日（Beforeに含まれない）
		createLoan(book, user, targetDate, targetDate.plusDays(1), targetDate.plusDays(1)); // 除外データ③：返却済（NotNull）かつ 期日前

		// キャッシュをクリアして確実にDBから検証データを取得する
		entityManager.flush();
		entityManager.clear();

		// 2. 実行：リポジトリメソッドの呼び出し

		List<Loan> result = loanRepository.findByReturnDateIsNullAndDueDateBeforeOrderByDueDateAsc(targetDate);

		// 3. 検証：結果の確認

		// 取得された件数が2件であることを検証
		assertThat(result).hasSize(2);

		// OrderByDueDateAsc（期日の昇順）の通り、より日付の古いloan2が先頭に来ていることを検証
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

		// 期待するデータ①：ユーザーA、貸出日（新しい：当日） -> 降順なので先頭にくるべき
		createLoan(book, userA, baseDate, baseDate.plusDays(7), null);

		// 期待するデータ②：ユーザーA、貸出日（古い：前日）
		createLoan(book, userA, baseDate.minusDays(1), baseDate.plusDays(6), null);

		// 除外データ：ユーザーBの貸出データ
		createLoan(book, userB, baseDate, baseDate.plusDays(7), null);

		entityManager.flush();
		entityManager.clear();

		// 2. 実行

		List<Loan> result = loanRepository.findByUser_UserIdOrderByLoanDateDesc(userA.getUserId());

		// 3. 検証

		assertThat(result).hasSize(2);

		// 貸出日の降順（新しい順）の検証
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

		// 期待するデータ：本A × ユーザーA × 未返却(null)
		Loan loan1 = createLoan(bookA, userA, baseDate, baseDate.plusDays(7), null);

		// 除外データ①：本A × ユーザーA × 返却済
		createLoan(bookA, userA, baseDate, baseDate.plusDays(7), baseDate.plusDays(1));

		// 除外データ②：本B(違う本) × ユーザーA × 未返却
		createLoan(bookB, userA, baseDate, baseDate.plusDays(7), null);

		// 除外データ③：本A × ユーザーB(違う人) × 未返却
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

		// 貸出日：昨日 (降順で2番目)
		createLoan(book, user, baseDate.minusDays(1), baseDate.plusDays(6), null);

		// 貸出日：今日（一番新しい ➔ 降順で1番目）
		createLoan(book, user, baseDate, baseDate.plusDays(7), null);

		// 貸出日：一昨日（一番古い ➔ 降順で3番目）
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

}
