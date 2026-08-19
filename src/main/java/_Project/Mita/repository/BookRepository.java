package _Project.Mita.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import _Project.Mita.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByIsDeletedFalse();

    @Query("SELECT b FROM Book b WHERE b.isDeleted = false "
            + "AND (:keyword IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
            + "AND (:categoryId IS NULL OR b.category.categoryId = :categoryId)")
    List<Book> search(@Param("keyword") String keyword, @Param("categoryId") Long categoryId);
}
