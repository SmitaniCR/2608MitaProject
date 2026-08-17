package _Project.Mita.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import _Project.Mita.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByIsDeletedFalse();
}
