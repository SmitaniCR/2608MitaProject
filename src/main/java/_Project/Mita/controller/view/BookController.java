package _Project.Mita.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import _Project.Mita.entity.Book;
import _Project.Mita.form.BookForm;
import _Project.Mita.service.BookService;
import _Project.Mita.service.CategoryService;

@Controller
@RequestMapping("/admin/books")
public class BookController {

    private final BookService bookService;
    private final CategoryService categoryService;

    public BookController(BookService bookService, CategoryService categoryService) {
        this.bookService = bookService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list() {
        return "admin/books/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("bookForm", new BookForm());
        model.addAttribute("categories", categoryService.findAll());
        return "admin/books/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Book book = bookService.findById(id);
        BookForm form = new BookForm();
        form.setBookId(book.getBookId());
        form.setTitle(book.getTitle());
        form.setAuthor(book.getAuthor());
        form.setIsbn(book.getIsbn());
        form.setCategoryId(book.getCategory() != null ? book.getCategory().getCategoryId() : null);
        form.setTotalCopies(book.getTotalCopies());
        form.setAvailableCopies(book.getAvailableCopies());
        form.setDescription(book.getDescription());
        form.setPublishedDate(book.getPublishedDate());
        model.addAttribute("bookForm", form);
        model.addAttribute("categories", categoryService.findAll());
        return "admin/books/form";
    }
}
