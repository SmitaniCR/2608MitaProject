package _Project.Mita.response;
import _Project.Mita.entity.Book;

public record CoverImageUploadResponse(String coverImagePath) {
	
	public static CoverImageUploadResponse from(Book book) {
		return new CoverImageUploadResponse(
				book.getCoverImagePath());
	}
}
