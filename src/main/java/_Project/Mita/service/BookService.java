package _Project.Mita.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import _Project.Mita.entity.Book;
import _Project.Mita.entity.Category;
import _Project.Mita.form.BookForm;
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
    public List<Book> findAll() {
        return bookRepository.findByIsDeletedFalse();
    }

    @Transactional(readOnly = true)
    public Book findById(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new NoSuchElementException("書籍が見つかりません: id=" + bookId));
    }

    public Book create(BookForm form) {
        Book book = new Book();
        applyForm(book, form);
        return bookRepository.save(book);
    }

    public Book update(Long bookId, BookForm form) {
        Book book = findById(bookId);
        applyForm(book, form);
        return bookRepository.save(book);
    }

    public void delete(Long bookId) {
        Book book = findById(bookId);
        book.setDeleted(true);
        bookRepository.save(book);
    }

    private void applyForm(Book book, BookForm form) {
        book.setTitle(form.getTitle());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());
        book.setTotalCopies(form.getTotalCopies());
        book.setAvailableCopies(form.getAvailableCopies());
        book.setDescription(form.getDescription());
        book.setPublishedDate(form.getPublishedDate());

        if (form.getCategoryId() != null) {
            Category category = categoryRepository.findById(form.getCategoryId())
                    .orElseThrow(() -> new NoSuchElementException("カテゴリが見つかりません: id=" + form.getCategoryId()));
            book.setCategory(category);
        } else {
            book.setCategory(null);
        }
    }
}
