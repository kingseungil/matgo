package matgo.global.filesystem.s3;

import matgo.global.filesystem.s3.exception.S3Exception;
import matgo.global.type.S3Directory;
import org.springframework.web.multipart.MultipartFile;

public interface S3Service {

    String upload(MultipartFile multipartFile, String directoryName, String saveFileName, String originFileName)
      throws S3Exception;

    void delete(String imageUrl) throws S3Exception;

    String uploadAndGetImageURL(MultipartFile file, S3Directory directory);

}
