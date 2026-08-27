package _Project.Mita.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import _Project.Mita.entity.Book;
import _Project.Mita.entity.Category;
import _Project.Mita.form.BookRequest;
import _Project.Mita.repository.BookRepository;
import _Project.Mita.repository.CategoryRepository;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private BookService bookService;

    private Category category;
    private Book book;
    private BookRequest bookRequest;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setCategoryId(100L);
        category.setCategoryName("児童本");

        book = new Book();
        book.setBookId(1L);
        book.setTitle("テステ");
        book.setAuthor("トステ");
        book.setIsbn("1111111111111");
        book.setTotalCopies(5);
        book.setAvailableCopies(5);
        book.setDescription("テストステ");
        book.setPublishedDate(LocalDate.of(2026, 1, 1));
        book.setCategory(category);
        book.setDeleted(false);
        
        bookRequest = new BookRequest("新テスト","テス太","2222222222222",100L,10,10,"新テテテ",LocalDate.of(2026, 8, 27));
    }

    @Test
    void 指定された条件で書籍を検索一覧取得できる() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> expectedPage = new PageImpl<>(List.of(book));
        when(bookRepository.search("テスト", 100L, pageable)).thenReturn(expectedPage);

        Page<Book> result = bookService.search("テスト", 100L, pageable);

        assertThat(result).isEqualTo(expectedPage);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findAllForExport_削除されていないすべての書籍を取得できる() {
        List<Book> expectedList = List.of(book);
        when(bookRepository.findByIsDeletedFalse()).thenReturn(expectedList);

        List<Book> result = bookService.findAllForExport();

        assertThat(result).isEqualTo(expectedList);
    }

    @Test
    void findById_存在するIDを指定したとき書籍エンティティを取得できる() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        Book result = bookService.findById(1L);

        assertThat(result).isEqualTo(book);
    }

    @Test
    void findById_存在しないIDを指定したときNoSuchElementException() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> bookService.findById(999L));
        assertThat(exception.getMessage()).contains("書籍が見つかりません: id=999");
    }

    @Test
    void create_カテゴリIDが存在するときカテゴリを紐付けて登録できる() {
        when(categoryRepository.findById(100L)).thenReturn(Optional.of(category));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Book result = bookService.create(bookRequest);

        assertThat(result.getTitle()).isEqualTo("新テスト");
        assertThat(result.getAuthor()).isEqualTo("テス太");
        assertThat(result.getCategory()).isEqualTo(category);
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void create_カテゴリIDがnullのときカテゴリなしで登録できる() {
    	BookRequest noCategoryRequest = new BookRequest("タイトル", "著者", "ISBN", null, 5, 5, "説明", LocalDate.now());
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Book result = bookService.create(noCategoryRequest);

        assertThat(result.getCategory()).isNull();
        verify(categoryRepository, never()).findById(anyLong());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void create_指定されたカテゴリIDが存在しないときNoSuchElementException() {
        when(categoryRepository.findById(100L)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> bookService.create(bookRequest));
        assertThat(exception.getMessage()).contains("カテゴリが見つかりません: id=100");
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void update_既存の書籍情報をリクエスト情報で更新できる() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(categoryRepository.findById(100L)).thenReturn(Optional.of(category));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Book result = bookService.update(1L, bookRequest);

        assertThat(result.getTitle()).isEqualTo("新テスト");
        assertThat(result.getAuthor()).isEqualTo("テス太");
        assertThat(result.getIsbn()).isEqualTo("2222222222222");
        assertThat(result.getTotalCopies()).isEqualTo(10);
        assertThat(result.getAvailableCopies()).isEqualTo(10);
        assertThat(result.getDescription()).isEqualTo("新テテテ");
        assertThat(result.getPublishedDate()).isEqualTo(LocalDate.of(2026, 8, 27));
        assertThat(result.getCategory()).isEqualTo(category);
        verify(bookRepository).save(book);
    }

    @Test
    void update_存在しない書籍IDを指定したときNoSuchElementException() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> bookService.update(999L, bookRequest));
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void delete_指定された書籍の削除フラグを有効にできる() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        
        bookService.delete(1L);

        // 引数に渡されたBookエンティティの状態を検証
        ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(bookCaptor.capture());
        
        Book savedBook = bookCaptor.getValue();
        assertThat(savedBook.isDeleted()).isTrue();
    }
}
