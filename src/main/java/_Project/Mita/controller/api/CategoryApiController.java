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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import _Project.Mita.form.CategoryRequest;
import _Project.Mita.response.CategoryResponse;
import _Project.Mita.service.CategoryService;

@RestController
@RequestMapping("/api/categories")
public class CategoryApiController {

    private final CategoryService categoryService;

    public CategoryApiController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryResponse> list() {
        return categoryService.findAll().stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        return CategoryResponse.from(categoryService.create(request));
    }

    @PutMapping("/{id}")
    public CategoryResponse update(@PathVariable("id") Long id, @Valid @RequestBody CategoryRequest request) {
        return CategoryResponse.from(categoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        categoryService.delete(id);
    }
}
