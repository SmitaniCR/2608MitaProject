package _Project.Mita.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import _Project.Mita.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByIsDeletedFalse();

    @Query("SELECT b FROM Book b WHERE b.isDeleted = false "
            + "AND (:keyword IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
            + "AND (:categoryId IS NULL OR b.category.categoryId = :categoryId)")
    Page<Book> search(@Param("keyword") String keyword, @Param("categoryId") Long categoryId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Book b WHERE b.bookId = :bookId")
    Optional<Book> findByIdForUpdate(@Param("bookId") Long bookId);
}
