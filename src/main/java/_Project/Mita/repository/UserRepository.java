package _Project.Mita.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import _Project.Mita.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByIsDeletedFalse();

    Optional<User> findByEmailAndIsDeletedFalse(String email);

    boolean existsByEmailAndIsDeletedFalse(String email);
}
