package _Project.Mita.controller;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String list(Model model) {
        model.addAttribute("books", bookService.findAll());
        return "admin/books/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("bookForm", new BookForm());
        model.addAttribute("categories", categoryService.findAll());
        return "admin/books/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("bookForm") BookForm form, BindingResult bindingResult,
            Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            return "admin/books/form";
        }
        bookService.create(form);
        redirectAttributes.addFlashAttribute("message", "書籍を登録しました");
        return "redirect:/admin/books";
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

    @PostMapping("/{id}/edit")
    public String update(@PathVariable("id") Long id, @Valid @ModelAttribute("bookForm") BookForm form,
            BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            return "admin/books/form";
        }
        bookService.update(id, form);
        redirectAttributes.addFlashAttribute("message", "書籍を更新しました");
        return "redirect:/admin/books";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        bookService.delete(id);
        redirectAttributes.addFlashAttribute("message", "書籍を削除しました");
        return "redirect:/admin/books";
    }
}
