package _Project.Mita.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

@Entity
public class TestEntity {

    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public byte[] getImageBytes() {
		return imageBytes;
	}

	public void setImageBytes(byte[] imageBytes) {
		this.imageBytes = imageBytes;
	}

	@Id
    private Long id;

    // 大容量のテキストとしてDBに保存させる
    @Lob
    private String content;

    // 画像やファイルなどのバイナリとしてDBに保存させる
    @Lob
    private byte[] imageBytes;
}
