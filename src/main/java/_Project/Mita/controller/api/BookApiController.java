package _Project.Mita.controller.api;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import _Project.Mita.entity.Book;
import _Project.Mita.form.BookRequest;
import _Project.Mita.response.BookResponse;
import _Project.Mita.response.BookSuggestionResponse;
import _Project.Mita.response.PageResponse;
import _Project.Mita.service.BookService;

@RestController
@RequestMapping("/api/books")
public class BookApiController {

    private static final int SUGGEST_LIMIT = 8;

    private final BookService bookService;

    public BookApiController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public PageResponse<BookResponse> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(value = "sortBy", required = false, defaultValue = "title") String sortBy,
            @RequestParam(value = "sortDir", required = false, defaultValue = "asc") String sortDir) {
        Pageable pageable = PageRequest.of(page, size, resolveSort(sortBy, sortDir));
        Page<Book> result = bookService.search(keyword, categoryId, pageable);
        return PageResponse.from(result, BookResponse::from);
    }

    @GetMapping("/suggest")
    public List<BookSuggestionResponse> suggest(@RequestParam("keyword") String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        Page<Book> result = bookService.search(keyword, null, PageRequest.of(0, SUGGEST_LIMIT));
        return result.getContent().stream()
                .map(BookSuggestionResponse::from)
                .toList();
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export() {
        StringBuilder csv = new StringBuilder("書籍ID,タイトル,著者,ISBN,カテゴリ,総冊数,貸出可能数,出版日\r\n");
        for (Book book : bookService.findAllForExport()) {
            csv.append(CsvUtils.escape(book.getBookId())).append(',')
                    .append(CsvUtils.escape(book.getTitle())).append(',')
                    .append(CsvUtils.escape(book.getAuthor())).append(',')
                    .append(CsvUtils.escape(book.getIsbn())).append(',')
                    .append(CsvUtils.escape(book.getCategory() != null ? book.getCategory().getCategoryName() : null))
                    .append(',')
                    .append(CsvUtils.escape(book.getTotalCopies())).append(',')
                    .append(CsvUtils.escape(book.getAvailableCopies())).append(',')
                    .append(CsvUtils.escape(book.getPublishedDate())).append("\r\n");
        }
        return CsvUtils.buildResponse("books.csv", csv.toString());
    }

    private Sort resolveSort(String sortBy, String sortDir) {
        String property = switch (sortBy) {
            case "author" -> "author";
            case "publishedDate" -> "publishedDate";
            default -> "title";
        };
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, property);
    }

    @GetMapping("/{id}")
    public BookResponse detail(@PathVariable("id") Long id) {
        return BookResponse.from(bookService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookResponse create(@Valid @RequestBody BookRequest request) {
        return BookResponse.from(bookService.create(request));
    }

    @PutMapping("/{id}")
    public BookResponse update(@PathVariable("id") Long id, @Valid @RequestBody BookRequest request) {
        return BookResponse.from(bookService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        bookService.delete(id);
    }
}
