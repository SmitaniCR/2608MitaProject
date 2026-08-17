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
    public String list(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "admin/categories/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("categoryForm", new CategoryForm());
        return "admin/categories/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("categoryForm") CategoryForm form, BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/categories/form";
        }
        categoryService.create(form);
        redirectAttributes.addFlashAttribute("message", "カテゴリを登録しました");
        return "redirect:/admin/categories";
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

    @PostMapping("/{id}/edit")
    public String update(@PathVariable("id") Long id, @Valid @ModelAttribute("categoryForm") CategoryForm form,
            BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/categories/form";
        }
        categoryService.update(id, form);
        redirectAttributes.addFlashAttribute("message", "カテゴリを更新しました");
        return "redirect:/admin/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        categoryService.delete(id);
        redirectAttributes.addFlashAttribute("message", "カテゴリを削除しました");
        return "redirect:/admin/categories";
    }
}
