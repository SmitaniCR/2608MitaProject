package _Project.Mita.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import _Project.Mita.exception.FileStorageException;

@Repository
public class FileStorageRepository {
	
	//鍵は削除済み
	
	public String saveImage(byte[] content, String originalFilename, String uploadDir) {
		
		String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        String newFilename = UUID.randomUUID().toString() + extension;
        
        Path targetPath = Paths.get(uploadDir , newFilename);
        
        try {
        if (Files.notExists(targetPath.getParent())) {
            Files.createDirectories(targetPath.getParent());
        }
        
        Files.write(targetPath, content);
        
        return "/images/" + newFilename;
        
        } catch (IOException e) {
            throw new FileStorageException("アップロード失敗: " , e);
        }
	}
}
