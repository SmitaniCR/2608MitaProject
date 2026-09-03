package _Project.Mita.repository;
import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import _Project.Mita.entity.TestEntity;



@DataJpaTest 
class TestRepositoryTest {
	
	@Autowired
    private TestRepository testRepository;
	
	@Autowired
	private TestEntityManager entityManager;

    @Test
    @DisplayName("@Lobアノテーションを付与した大容量テキストとバイナリが正しく保存・取得できること")
    void saveAndFindArticleWithLobFields() {
        
    	TestEntity testEntity = new TestEntity();
    	testEntity.setId(1L);
    	testEntity.setContent("これは非常に長いテキストデータです。@LobによってCLOBとして扱われます。".repeat(100));
        
        byte[] mockImageBytes = new byte[]{1, 2, 3, 4, 5}; // 擬似的なバイナリデータ。今は仮で適当なダミーの数。今後実際の画像ファイルをFiles.readAllBytes(...)で読み込む予定。
        testEntity.setImageBytes(mockImageBytes);
       
        TestEntity savedArticle = testRepository.save(testEntity);
        
        entityManager.flush();
        entityManager.clear();

        Optional<TestEntity> foundArticleOpt = testRepository.findById(savedArticle.getId());
        
        assertThat(foundArticleOpt).isPresent(); 
        
        TestEntity foundArticle = foundArticleOpt.get();
        assertThat(foundArticle.getContent()).isEqualTo(testEntity.getContent()); 
        assertThat(foundArticle.getImageBytes()).isEqualTo(testEntity.getImageBytes()); 
    }

}
