package _Project.Mita.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import _Project.Mita.entity.Book;
import _Project.Mita.entity.Category;
import _Project.Mita.form.BookRequest;
import _Project.Mita.repository.BookRepository;
import _Project.Mita.repository.CategoryRepository;

@Service
@Transactional
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    public BookService(BookRepository bookRepository, CategoryRepository categoryRepository) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public Page<Book> search(String keyword, Long categoryId, Pageable pageable) {
        return bookRepository.search(keyword, categoryId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Book> findAllForExport() {
        return bookRepository.findByIsDeletedFalse();
    }

    @Transactional(readOnly = true)
    public Book findById(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new NoSuchElementException("書籍が見つかりません: id=" + bookId));
    }

    public Book create(BookRequest request) {
        Book book = new Book();
        applyRequest(book, request);
        return bookRepository.save(book);
    }

    public Book update(Long bookId, BookRequest request) {
        Book book = findById(bookId);
        applyRequest(book, request);
        return bookRepository.save(book);
    }

    public void delete(Long bookId) {
        Book book = findById(bookId);
        book.setDeleted(true);
        bookRepository.save(book);
    }

    private void applyRequest(Book book, BookRequest request) {
        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setIsbn(request.isbn());
        book.setTotalCopies(request.totalCopies());
        book.setAvailableCopies(request.availableCopies());
        book.setDescription(request.description());
        book.setPublishedDate(request.publishedDate());

        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new NoSuchElementException("カテゴリが見つかりません: id=" + request.categoryId()));
            book.setCategory(category);
        } else {
            book.setCategory(null);
        }
    }
}
