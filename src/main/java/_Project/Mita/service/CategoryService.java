package _Project.Mita.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import _Project.Mita.entity.Category;
import _Project.Mita.form.CategoryRequest;
import _Project.Mita.repository.CategoryRepository;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findByIsDeletedFalse();
    }

    @Transactional(readOnly = true)
    public Category findById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NoSuchElementException("カテゴリが見つかりません: id=" + categoryId));
    }

    public Category create(CategoryRequest request) {
        Category category = new Category();
        category.setCategoryName(request.categoryName());
        return categoryRepository.save(category);
    }

    public Category update(Long categoryId, CategoryRequest request) {
        Category category = findById(categoryId);
        category.setCategoryName(request.categoryName());
        return categoryRepository.save(category);
    }

    public void delete(Long categoryId) {
        Category category = findById(categoryId);
        category.setDeleted(true);
        categoryRepository.save(category);
    }
}
