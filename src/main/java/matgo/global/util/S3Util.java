package matgo.global.util;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import matgo.global.filesystem.s3.S3Service;
import matgo.global.type.S3Directory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class S3Util {

    private final S3Service s3Service;
    @Value("${images.default-profile-image}")
    private String defaultProfileImage;

    public String uploadAndGetImageURL(MultipartFile file, S3Directory directory) {
        if (file == null || file.isEmpty()) {
            if (S3Directory.MEMBER.equals(directory)) {
                return defaultProfileImage;
            }
            return null;
        }
        return s3Service.upload(file, directory.getDirectory(), String.valueOf(UUID.randomUUID()),
          file.getOriginalFilename());
    }

}
