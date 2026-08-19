package _Project.Mita.response;

import java.time.LocalDate;

import _Project.Mita.entity.Book;

public record BookResponse(
        Long bookId,
        String title,
        String author,
        String isbn,
        Long categoryId,
        String categoryName,
        Integer totalCopies,
        Integer availableCopies,
        String description,
        LocalDate publishedDate) {

    public static BookResponse from(Book book) {
        return new BookResponse(
                book.getBookId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getCategory() != null ? book.getCategory().getCategoryId() : null,
                book.getCategory() != null ? book.getCategory().getCategoryName() : null,
                book.getTotalCopies(),
                book.getAvailableCopies(),
                book.getDescription(),
                book.getPublishedDate());
    }
}
