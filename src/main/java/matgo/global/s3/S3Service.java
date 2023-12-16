package matgo.global.s3;

import matgo.global.s3.exception.S3Exception;
import org.springframework.web.multipart.MultipartFile;

public interface S3Service {

    String upload(MultipartFile multipartFile, String directoryName, String saveFileName, String originFileName)
      throws S3Exception;

    void delete(String imageUrl) throws S3Exception;

}
