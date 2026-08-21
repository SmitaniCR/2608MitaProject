package _Project.Mita.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import _Project.Mita.entity.Category;
import _Project.Mita.form.CategoryForm;
import _Project.Mita.service.CategoryService;

@Controller
@RequestMapping("/admin/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list() {
        return "admin/categories/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("categoryForm", new CategoryForm());
        return "admin/categories/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Category category = categoryService.findById(id);
        CategoryForm form = new CategoryForm();
        form.setCategoryId(category.getCategoryId());
        form.setCategoryName(category.getCategoryName());
        model.addAttribute("categoryForm", form);
        return "admin/categories/form";
    }
}
