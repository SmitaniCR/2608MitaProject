package _Project.Mita.response;

import _Project.Mita.entity.Category;

public record CategoryResponse(Long categoryId, String categoryName) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getCategoryId(), category.getCategoryName());
    }
}
