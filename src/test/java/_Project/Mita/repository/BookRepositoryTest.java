package _Project.Mita.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import _Project.Mita.entity.Book;
import _Project.Mita.entity.Category;

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
	    @DisplayName("削除されていない書籍のみが取得できること")
	    void findByIsDeletedFalse_shouldReturnOnlyActiveBooks() {
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
	    @DisplayName("キーワードとカテゴリの両方で絞り込めること")
	    void search_shouldFilterByKeywordAndCategory() {
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
	    @DisplayName("search：キーワードがnullの場合はカテゴリのみで絞り込めること")
	    void search_shouldFilterByCategoryOnly_whenKeywordIsNull() {
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
	    @DisplayName("著者名のキーワード部分一致でも取得できること")
	    void search_shouldFilterByAuthorKeyword() {
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
	    @DisplayName("絞り込みなしで全件取得できること")
	    void search_shouldReturnAll_whenKeyIsAllNull() {
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
	    @DisplayName("指定したIDの書籍が悲観的ロック付きで正しく取得できること")
	    void findByIdForUpdate_shouldReturnBook() {
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

}
