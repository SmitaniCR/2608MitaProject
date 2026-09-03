package _Project.Mita.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import _Project.Mita.entity.TestEntity;

@Repository
public interface TestRepository extends JpaRepository<TestEntity, Long> {
    // 基本的なCRUD操作（保存、検索、削除など）はJpaRepositoryが自動で提供
}