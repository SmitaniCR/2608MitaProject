package _Project.Mita.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import _Project.Mita.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByIsDeletedFalse();
}
