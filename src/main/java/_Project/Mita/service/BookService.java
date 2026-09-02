package _Project.Mita.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import _Project.Mita.entity.Book;
import _Project.Mita.entity.Category;
import _Project.Mita.form.BookRequest;
import _Project.Mita.repository.BookRepository;
import _Project.Mita.repository.CategoryRepository;
import _Project.Mita.utils.FileStorageUtils;

@Service
@Transactional
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    public BookService(BookRepository bookRepository, CategoryRepository categoryRepository) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
    }
    
    @Value("${file.upload-dir}")
	private String uploadDir;

    /**
     * 指定された条件に合致する書籍を検索・一覧取得する
     * 
     * @param keyword 検索キーワード
     * @param categoryId カテゴリID
     * @param pageable ページ情報
     * @return 書籍エンティティの一覧
     */
    @Transactional(readOnly = true)
    public Page<Book> search(String keyword, Long categoryId, Pageable pageable) {
        return bookRepository.search(keyword, categoryId, pageable);
    }

    /**
     * CSVエクスポート用にすべての書籍を取得する
     * 
     * @return 削除フラグがfalseの書籍エンティティリスト
     */
    @Transactional(readOnly = true)
    public List<Book> findAllForExport() {
        return bookRepository.findByIsDeletedFalse();
    }

    /**
     * 指定された書籍IDに該当する書籍を取得する
     * 
     * @param bookId 取得対象の書籍ID
     * @return 書籍エンティティ
     * @throws NoSuchElementException IDの書籍が見つからないとき
     */
    @Transactional(readOnly = true)
    public Book findById(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new NoSuchElementException("書籍が見つかりません: id=" + bookId));
    }

    /**
     * リクエスト情報を基に、新しい書籍を登録する
     * 
     * @param request 登録する書籍の情報をもつリクエスト
     * @return 永続化された書籍エンティティ
     * @throws NoSuchElementException IDのカテゴリが見つからないとき
     */
    public Book create(BookRequest request) {
        Book book = new Book();
        applyRequest(book, request);
        return bookRepository.save(book);
    }

    /**
     * 既存の書籍情報をリクエスト情報で更新する
     * 
     * @param bookId 更新対象の書籍ID
     * @param request 更新する書籍の情報をもつリクエスト
     * @return 更新し永続化された書籍エンティティ
     * @throws NoSuchElementException IDの書籍/カテゴリが見つからないとき
     */
    public Book update(Long bookId, BookRequest request) {
        Book book = findById(bookId);
        applyRequest(book, request);
        return bookRepository.save(book);
    }

    /**
     * 指定された書籍の削除フラグを有効（論理削除）にする
     * 
     * @param bookId 削除対象の書籍ID
     * @throws NoSuchElementException IDの書籍が見つからないとき
     */
    public void delete(Long bookId) {
        Book book = findById(bookId);
        book.setDeleted(true);
        bookRepository.save(book);
    }

    /**
     * リクエストの値を書籍エンティティに詰め替える。カテゴリIDが存在する場合はカテゴリの紐付けを行う
     * 
     * @param book 詰め替え対象の書籍エンティティ
     * @param request 詰め替え元の書籍リクエスト
     * @throws NoSuchElementException IDのカテゴリが見つからないとき
     */
    private void applyRequest(Book book, BookRequest request) {
        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setIsbn(request.isbn());
        book.setTotalCopies(request.totalCopies());
        book.setAvailableCopies(request.availableCopies());
        book.setDescription(request.description());
        book.setPublishedDate(request.publishedDate());
        book.setCoverImagePath(request.coverImagePath());

        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new NoSuchElementException("カテゴリが見つかりません: id=" + request.categoryId()));
            book.setCategory(category);
        } else {
            book.setCategory(null);
        }
    }
    
    public String upImage(MultipartFile file) {
    	return FileStorageUtils.saveImage(file, uploadDir);
    }
}
