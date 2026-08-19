package _Project.Mita.controller.api;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
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

import _Project.Mita.form.BookRequest;
import _Project.Mita.response.BookResponse;
import _Project.Mita.service.BookService;

@RestController
@RequestMapping("/api/books")
public class BookApiController {

    private final BookService bookService;

    public BookApiController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public List<BookResponse> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Long categoryId) {
        return bookService.search(keyword, categoryId).stream()
                .map(BookResponse::from)
                .toList();
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
