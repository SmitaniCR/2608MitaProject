package _Project.Mita.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import _Project.Mita.exception.FileStorageException;

public class FileStorageUtils {
	
	private FileStorageUtils() {//鍵
    }
	
	public static String saveImage(MultipartFile file, String uploadDir) {
		
		String originalFilename = file.getOriginalFilename();
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
        
        file.transferTo(targetPath);
        
        return "/images/" + newFilename;
        
        } catch (IOException e) {
            throw new FileStorageException("アップロード失敗: " , e);
        }
	}
}
