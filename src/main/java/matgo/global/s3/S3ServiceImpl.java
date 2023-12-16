package matgo.global.s3;

import static matgo.global.exception.ErrorCode.FILE_UPLOAD_ERROR;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matgo.global.s3.exception.S3Exception;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;
    @Value("${aws.s3.bucket}")
    private String bucket;
    @Value("${aws.s3.directory}")
    private String rootDirectory;

    @Override
    public String upload(MultipartFile multipartFile, String directoryName, String saveFileName,
      String originFileName) {
        try {
            RequestBody requestBody = RequestBody.fromInputStream(multipartFile.getInputStream(),
              multipartFile.getSize());
            MediaType mediaType = MediaType.parseMediaType(Files.probeContentType(Paths.get(originFileName)));
            return uploadFile(requestBody, directoryName, saveFileName, mediaType);
        } catch (IOException e) {
            log.error("S3 업로드 에러", e);
            throw new S3Exception(FILE_UPLOAD_ERROR);
        }
    }

    private String uploadFile(RequestBody requestBody, String directoryName, String filename, MediaType mediaType) {
        String uploadPath = String.format("%s/%s/%s", rootDirectory, directoryName, filename);
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                                                            .bucket(bucket)
                                                            .key(uploadPath)
                                                            .contentType(mediaType.toString())
                                                            .build();

        s3Client.putObject(putObjectRequest, requestBody);
        // uploaded url return
        return s3Client.utilities().getUrl(builder -> builder.bucket(bucket).key(uploadPath)).toExternalForm();
    }


    @Override
    public void delete(String imageUrl) throws S3Exception {

    }
}
