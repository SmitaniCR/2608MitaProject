package _Project.Mita.repository;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import _Project.Mita.entity.Book;
import _Project.Mita.entity.Category;
import _Project.Mita.response.CategorySummaryResponse;

@DataJpaTest
public class BookRepositoryTest {
	
	 @Autowired
	    private BookRepository bookRepository;

	    @Autowired
	    private TestEntityManager entityManager;

	    private Category createCategory(String name) {
	        Category category = new Category();
	        category.setCategoryName(name);
	        return entityManager.persist(category);
	    }
	    
	    private Category createCategory(String name, boolean isDeleted) {
	        Category category = new Category();
	        category.setCategoryName(name);
	        category.setDeleted(isDeleted); 
	        return entityManager.persist(category);
	    }

	    private Book createBook(String title, String author, Category category, boolean isDeleted) {
	        Book book = new Book();
	        book.setTitle(title);
	        book.setAuthor(author);
	        book.setCategory(category);
	        book.setTotalCopies(10);
	        book.setAvailableCopies(10);
	        book.setDeleted(isDeleted);
	        return entityManager.persist(book);
	    }

	    // テストケース 

	    @Test
	    void 削除されていない書籍のみが取得できること() {
	        // 1. 準備
	        Category category = createCategory("プログラミング");
	        createBook("Java入門", "著者A", category, false); // 取得対象
	        createBook("Spring解説", "著者B", category, true); // 除外対象

	        entityManager.flush();
	        entityManager.clear();

	        // 2. 実行
	        List<Book> result = bookRepository.findByIsDeletedFalse();

	        // 3. 検証
	        assertThat(result).hasSize(1);
	        assertThat(result.get(0).getTitle()).isEqualTo("Java入門");
	    }

	    @Test
	    void キーワードとカテゴリの両方で絞り込めること() {
	        // 1. 準備
	        Category javaCategory = createCategory("Java");
	        Category phpCategory = createCategory("PHP");

	        // 検索対象になるデータ
	        createBook("BEGINNER JAVA", "Author A", javaCategory, false); 
	        
	        // 除外されるデータ
	        createBook("BEGINNER JAVA", "Author A", phpCategory, false);  // カテゴリ不一致
	        createBook("Python入門", "Author A", javaCategory, false);     // キーワード不一致
	        createBook("BEGINNER JAVA", "Author A", javaCategory, true);   // 削除済みのため除外

	        entityManager.flush();
	        entityManager.clear();

	        Pageable pageable = PageRequest.of(0, 10);

	        // 2. 実行：大文字混じりの「java」で検索
	        Page<Book> result = bookRepository.search("java", javaCategory.getCategoryId(), pageable);

	        // 3. 検証
	        assertThat(result.getContent()).hasSize(1);
	        assertThat(result.getContent().get(0).getTitle()).isEqualTo("BEGINNER JAVA");
	    }

	    @Test
	    void キーワードがnullの場合はカテゴリのみで絞り込めること() {
	        // 1. 準備
	        Category category = createCategory("デザイン");
	        createBook("UI設計", "著者X", category, false);
	        createBook("UXデザイン", "著者Y", category, false);
	        createBook("Java入門", "著者Z", null, false);

	        entityManager.flush();
	        entityManager.clear();

	        Pageable pageable = PageRequest.of(0, 10);

	        // 2. 実行
	        Page<Book> result = bookRepository.search(null, category.getCategoryId(), pageable);

	        // 3. 検証
	        assertThat(result.getContent()).hasSize(2);
	        assertThat(result.getContent()).extracting(Book::getTitle).containsExactlyInAnyOrder("UI設計", "UXデザイン");
	    }

	    @Test
	    void 著者名のキーワード部分一致でも取得できること() {
	        // 1. 準備
	        createBook("書籍A", "山田太郎", null, false); // 取得対象
	        createBook("書籍B", "佐藤次郎", null, false); // 除外対象

	        entityManager.flush();
	        entityManager.clear();

	        Pageable pageable = PageRequest.of(0, 10);

	        // 2. 実行
	        Page<Book> result = bookRepository.search("山田", null, pageable);

	        // 3. 検証
	        assertThat(result.getContent()).hasSize(1);
	        assertThat(result.getContent().get(0).getAuthor()).isEqualTo("山田太郎");
	    }
	    
	    @Test
	    void 絞り込みなしで全件取得できること() {
	        // 1. 準備
	        createBook("書籍B", "山田太郎", null, false); // 取得対象
	        createBook("書籍A", "佐藤次郎", null, false); // 取得対象
	        createBook("書籍C", "鈴木三郎", null, false); // 取得対象
	        createBook("書籍G", "加藤四郎", null, false); // 取得対象
	        createBook("書籍E", "高橋五郎", null, false); // 取得対象
	        createBook("書籍F", "伊藤六郎", null, false); // 取得対象
	        createBook("書籍D", "山田太郎", null, false); // 取得対象
	        createBook("書籍H", "佐藤次郎", null, false); // 取得対象
	        createBook("書籍I", "鈴木三郎", null, false); // 取得対象
	        createBook("書籍J", "加藤四郎", null, false); // 取得対象
	        createBook("書籍K", "高橋五郎", null, false); // 取得対象
	        createBook("書籍L", "伊藤六郎", null, false); // 取得対象
	        createBook("書籍M", "鈴木三郎", null, false); // 取得対象
	        createBook("書籍N", "加藤四郎", null, false); // 取得対象
	        createBook("書籍O", "高橋五郎", null, false); // 取得対象
	        createBook("書籍P", "伊藤六郎", null, false); // 取得対象

	        entityManager.flush();
	        entityManager.clear();

	        Pageable pageable = PageRequest.of(0, 10, Sort.by("title"));
	        Pageable pageable2 = PageRequest.of(1, 10);

	        // 2. 実行
	        Page<Book> result = bookRepository.search(null, null, pageable);
	        Page<Book> result2 = bookRepository.search(null, null, pageable2);

	        // 3. 検証
	        assertThat(result.getContent()).hasSize(10);
	        assertThat(result.getTotalElements()).isEqualTo(16);
	        assertThat(result.getTotalPages()).isEqualTo(2);
	        assertThat(result.getNumber()).isEqualTo(0);
	        assertThat(result.getContent()).extracting(Book::getTitle).containsExactlyInAnyOrder("書籍A", "書籍B", "書籍C", "書籍D", "書籍E", "書籍F", "書籍G", "書籍H", "書籍I", "書籍J");
	        assertThat(result2.getContent()).hasSize(6);
	    }
	    
	    

	    @Test
	    void 指定したIDの書籍が悲観的ロック付きで正しく取得できること() {
	        // 1. 準備
	        Book book = createBook("排他制御テスト", "著者", null, false);

	        entityManager.flush();
	        entityManager.clear();

	        // 2. 実行
	        Optional<Book> result = bookRepository.findByIdForUpdate(book.getBookId());

	        // 3. 検証
	        assertThat(result).isPresent();
	        assertThat(result.get().getTitle()).isEqualTo("排他制御テスト");
	    }

	    @Test
	    void 古いバージョンのまま更新しようとするとOptimisticLockingFailureExceptionが発生する() {
	        // 1. 準備
	        Book book = createBook("排他制御テスト", null, null, false);
	        entityManager.flush();
	        Long bookId = book.getBookId();

	        entityManager.clear(); // キャッシュを空に

	        //古いバージョンのまま持ち続けるインスタンス
	        Book bookForUserA = bookRepository.findById(bookId).orElseThrow();

	        entityManager.clear(); //再度キャッシュを空に

	        //先に更新・保存してversionを進めてしまう
	        Book bookForUserB = bookRepository.findById(bookId).orElseThrow();
	        bookForUserB.setAvailableCopies(bookForUserB.getAvailableCopies() - 1);
	        bookRepository.saveAndFlush(bookForUserB); // ここでDB上のversionが1つ進む

	        // 2. 実行
	        bookForUserA.setAvailableCopies(bookForUserA.getAvailableCopies() - 1);

	        // 3. 検証
	        assertThrows(OptimisticLockingFailureException.class,
	                () -> bookRepository.saveAndFlush(bookForUserA));
	    }
	    
	    @Test
	    void カテゴリ別に書籍数と在庫数が正しく集計されること() {
	        // 1. 準備
	        // カテゴリA: 通常データ
	        Category catNormal = createCategory("教本", false);
	        createBook("Java入門", "太郎", catNormal, false);
	        createBook("Spring解説", "太郎", catNormal, false);

	        // カテゴリB: 論理削除された書籍が含まれる
	        Category catDeleted = createCategory("漫画", false);
	        createBook("ワンピ", "次郎", catDeleted, false);
	        createBook("なると", "次郎", catDeleted, true); 

	        // カテゴリC: 書籍が1冊もない
	        Category catNoBook = createCategory("新聞", false);

	        // カテゴリD: カテゴリ自体が論理削除されている
	        Category catNoCategory = createCategory("小説", true);
	        createBook("ガレリオ", "三郎", catNoCategory, false);

	        entityManager.flush();
	        entityManager.clear();

	        // 2. 実行
	        List<CategorySummaryResponse> result = bookRepository.summarizeByCategory();

	        // 3. 検証

	        assertThat(result).hasSize(3);

	 

	        CategorySummaryResponse resA = result.stream()
	                .filter(r -> r.categoryId().equals(catNormal.getCategoryId())).findFirst().orElseThrow();
	        assertThat(resA.categoryName()).isEqualTo("教本");
	        assertThat(resA.bookCount()).isEqualTo(2);      
	        assertThat(resA.totalCopies()).isEqualTo(20);    

	
	        CategorySummaryResponse resB = result.stream()
	                .filter(r -> r.categoryId().equals(catDeleted.getCategoryId())).findFirst().orElseThrow();
	        assertThat(resB.categoryName()).isEqualTo("漫画");
	        assertThat(resB.bookCount()).isEqualTo(1);      
	        assertThat(resB.totalCopies()).isEqualTo(10);     


	        CategorySummaryResponse resC = result.stream()
	                .filter(r -> r.categoryId().equals(catNoBook.getCategoryId())).findFirst().orElseThrow();
	        assertThat(resC.categoryName()).isEqualTo("新聞");
	        assertThat(resC.bookCount()).isEqualTo(0);      
	        assertThat(resC.totalCopies()).isEqualTo(0);    

	
	        boolean hasDeletedCategory = result.stream()
	                .anyMatch(r -> r.categoryId().equals(catNoCategory.getCategoryId()));
	        assertThat(hasDeletedCategory).isFalse();
	    }
}
