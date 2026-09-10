package _Project.Mita.external;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Optional;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import _Project.Mita.external.stub.TestApiController;

@Disabled //実行時、Security絡みで302が返るため失敗、テスト専用のSecurityFilterChainが必要.本日は保留
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestApiController.class)
public class ExternalBookInfoRepositoryUtTest {//結合テストの領域 テストControllerを必要とする

	private static final int PORT = findAvailablePort();//OSに空ポート番号を問い合わせPORTとして確保
	
    @Autowired
    private ExternalBookInfoRepository repository;
    
    @DynamicPropertySource 
    static void registerDynamicProperties(
            DynamicPropertyRegistry registry) {

        registry.add("server.port", () -> PORT);//確保したPORTでSpringを起動

        registry.add(
            "app.api.base-url",
            () -> "http://localhost:" + PORT //外部APIではなくPORTに問い合わせさせる
        );
    }
    
    private static int findAvailablePort() { //ポート番号に0を指定「何番でもいいから、空いている番号を割り当てて」とOSに依頼する意味
        try (ServerSocket socket = new ServerSocket(0)) {//番号取得し、すぐにソケット自体は閉じる
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException(
                "空きポートを取得できませんでした", e);
        }
    }

    @Test
    void ISBNを指定して外部書籍情報を取得できる() {//実行時、Security絡みで302が返るため失敗、テスト専用のSecurityFilterChainが必要.本日は保留

        Optional<ExternalBookInfoResponse> result =
                repository.fetchBookInfo("978-1234567890");

        assertThat(result).isPresent();
        assertThat(result.get().summary().isbn())
                .isEqualTo("978-1234567890");
    }

}
