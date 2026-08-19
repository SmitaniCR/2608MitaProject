package _Project.Mita.response;

import _Project.Mita.entity.Book;

public record BookSuggestionResponse(Long bookId, String title, String author) {

    public static BookSuggestionResponse from(Book book) {
        return new BookSuggestionResponse(book.getBookId(), book.getTitle(), book.getAuthor());
    }
}
