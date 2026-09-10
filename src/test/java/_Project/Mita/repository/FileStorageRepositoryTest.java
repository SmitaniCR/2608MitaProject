package _Project.Mita.repository;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileStorageRepositoryTest {

    private final FileStorageRepository fileStorageRepository = new FileStorageRepository();

    @TempDir
    Path tempDir;

    @Test
    void 画像ファイルが正常に保存され保存した内容を取得できること() throws IOException {

        byte[] content = new byte[]{1, 2, 3, 4, 5};
        String originalFilename = "test.jpg";

        String result = fileStorageRepository.saveImage(
                content,
                originalFilename,
                tempDir.toString()
        );

        assertThat(result)
                .startsWith("/images/")
                .endsWith(".jpg");

        try (var files = Files.list(tempDir)) {
            assertThat(files.count()).isEqualTo(1);
        }

        String savedFilename = result.substring("/images/".length());

        Path savedFile = tempDir.resolve(savedFilename);

        assertThat(savedFile).exists();

        byte[] savedContent = Files.readAllBytes(savedFile);

        assertThat(savedContent).isEqualTo(content);
    }

    @Test
    void 元のファイル名の拡張子が新しいファイル名に引き継がれること() throws IOException {

        byte[] content = new byte[]{10, 20, 30};
        String originalFilename = "sample.png";

        String result = fileStorageRepository.saveImage(
                content,
                originalFilename,
                tempDir.toString()
        );

        assertThat(result)
                .startsWith("/images/")
                .endsWith(".png");

        String savedFilename = result.substring("/images/".length());

        Path savedFile = tempDir.resolve(savedFilename);

        assertThat(savedFile).exists();
    }

    @Test
    void 元のファイル名がnullの場合拡張子なしで保存されること() throws IOException {

        byte[] content = new byte[]{1, 2, 3};

        String result = fileStorageRepository.saveImage(
                content,
                null,
                tempDir.toString()
        );

        assertThat(result).startsWith("/images/");
        assertThat(result).doesNotContain(".");

        String savedFilename = result.substring("/images/".length());

        Path savedFile = tempDir.resolve(savedFilename);

        assertThat(savedFile).exists();
        assertThat(Files.readAllBytes(savedFile)).isEqualTo(content);
    }

    @Test
    void 元のファイル名に拡張子がない場合拡張子なしで保存されること()
            throws IOException {

        byte[] content = new byte[]{4, 5, 6};
        String originalFilename = "sample";

        String result = fileStorageRepository.saveImage(
                content,
                originalFilename,
                tempDir.toString()
        );

        assertThat(result).startsWith("/images/");
        assertThat(result).doesNotContain(".");

        String savedFilename = result.substring("/images/".length());

        Path savedFile = tempDir.resolve(savedFilename);

        assertThat(savedFile).exists();
        assertThat(Files.readAllBytes(savedFile)).isEqualTo(content);
    }

    @Test
    void 存在しない保存先ディレクトリが自動的に作成されること() throws IOException {

        byte[] content = new byte[]{7, 8, 9};
        String originalFilename = "test.jpg";

        Path newDirectory = tempDir.resolve("images");

        assertThat(newDirectory).doesNotExist();

        String result = fileStorageRepository.saveImage(
                content,
                originalFilename,
                newDirectory.toString()
        );

        assertThat(newDirectory).exists().isDirectory();

        String savedFilename = result.substring("/images/".length());

        Path savedFile = newDirectory.resolve(savedFilename);

        assertThat(savedFile).exists();
        assertThat(Files.readAllBytes(savedFile)).isEqualTo(content);
    }

    @Test
    void 保存するファイルごとに異なるUUIDのファイル名が生成されること() throws IOException {

        byte[] content = new byte[]{1, 2, 3};
        String originalFilename = "test.jpg";

        String result1 = fileStorageRepository.saveImage(
                content,
                originalFilename,
                tempDir.toString()
        );

        String result2 = fileStorageRepository.saveImage(
                content,
                originalFilename,
                tempDir.toString()
        );

        assertThat(result1).isNotEqualTo(result2);

        assertThat(result1).endsWith(".jpg");
        assertThat(result2).endsWith(".jpg");

        try (var files = Files.list(tempDir)) {
            assertThat(files.count()).isEqualTo(2);
        }
    }
}//失敗時の分岐は保留（IOExceptionを意図的に発生させるのは、OS依存で不安定）
