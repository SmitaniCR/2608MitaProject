package _Project.Mita.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import _Project.Mita.entity.Book;
import _Project.Mita.response.BookLoanRankingResponse;
import _Project.Mita.response.CategorySummaryResponse;

public interface BookRepository extends JpaRepository<Book, Long> {

	List<Book> findByIsDeletedFalse();

	@Query("SELECT b FROM Book b WHERE b.isDeleted = false "
			+ "AND (:keyword IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
			+ "AND (:categoryId IS NULL OR b.category.categoryId = :categoryId)")
	Page<Book> search(@Param("keyword") String keyword, @Param("categoryId") Long categoryId, Pageable pageable);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT b FROM Book b WHERE b.bookId = :bookId")
	Optional<Book> findByIdForUpdate(@Param("bookId") Long bookId);

	@Query("SELECT new _Project.Mita.response.CategorySummaryResponse(" + "c.categoryId," + "c.categoryName,"
			+ "COUNT(b)," + "COALESCE(SUM(b.totalCopies),0L))"
			+ " FROM Category c LEFT JOIN Book b ON b.category = c AND b.isDeleted = false"
			+ " WHERE c.isDeleted = false"
			+ " GROUP BY c.categoryId, c.categoryName")
	List<CategorySummaryResponse> summarizeByCategory();

	@Query("SELECT new _Project.Mita.response.BookLoanRankingResponse(b.bookId, b.title, COUNT(l)) "
			+ "FROM Book b LEFT JOIN Loan l ON l.book = b WHERE b.isDeleted = false "
			+ "GROUP BY b.bookId, b.title ORDER BY COUNT(l) DESC, b.title ASC")
	List<BookLoanRankingResponse> findBookLoanRanking(Pageable pageable);
}
