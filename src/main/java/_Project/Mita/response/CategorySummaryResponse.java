package _Project.Mita.response;

public record CategorySummaryResponse(
        Long categoryId,
        String categoryName,
        long bookCount,
        long totalCopies) {
}
