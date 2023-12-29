package matgo.global.filesystem.s3;

import static matgo.global.exception.ErrorCode.FILE_DELETE_ERROR;
import static matgo.global.exception.ErrorCode.FILE_UPLOAD_ERROR;
import static matgo.global.exception.ErrorCode.NOT_IMAGE_EXTENSION;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matgo.global.filesystem.FileException;
import matgo.global.filesystem.s3.exception.S3Exception;
import matgo.global.type.S3Directory;
import matgo.global.util.FileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
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
    @Value("${images.default-profile-image}")
    private String defaultProfileImage;

    @Override
    public String upload(MultipartFile multipartFile, String directoryName, String saveFileName,
      String originFileName) {
        try {
            FileUtil.checkFileSize(multipartFile);
            String extension = FileUtil.getExtension(multipartFile);
            if (FileUtil.isImageExtension(extension)) {
                RequestBody requestBody = RequestBody.fromInputStream(multipartFile.getInputStream(),
                  multipartFile.getSize());
                MediaType mediaType = MediaType.parseMediaType(Files.probeContentType(Paths.get(originFileName)));
                return uploadFile(requestBody, directoryName, saveFileName, mediaType);
            } else {
                throw new FileException(NOT_IMAGE_EXTENSION);
            }
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

        try {
            s3Client.putObject(putObjectRequest, requestBody);
            // uploaded url return
            return s3Client.utilities().getUrl(builder -> builder.bucket(bucket).key(uploadPath)).toExternalForm();
        } catch (AwsServiceException | SdkClientException e) {
            log.error("S3 파일 업로드 에러", e);
            throw new S3Exception(FILE_UPLOAD_ERROR);
        }
    }


    @Override
    public void delete(String imageUrl) throws S3Exception {
        String key = extractKeyFromUrl(imageUrl);

        try {
            log.info("S3 삭제 요청 : {}", key);
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
        } catch (AwsServiceException | SdkClientException e) {
            log.error("S3 삭제 에러", e);
            throw new S3Exception(FILE_DELETE_ERROR);
        }
    }

    @Override
    public String uploadAndGetImageURL(MultipartFile file, S3Directory directory) {
        if (file == null || file.isEmpty()) {
            if (S3Directory.MEMBER.equals(directory)) {
                return defaultProfileImage;
            }
            return null;
        }
        return this.upload(file, directory.getDirectory(), String.valueOf(UUID.randomUUID()),
          file.getOriginalFilename());
    }

    private String extractKeyFromUrl(String imageUrl) {
        String key = imageUrl.substring(imageUrl.indexOf(bucket) + bucket.length() + 1);
        return key.substring(key.indexOf("/") + 1);
    }
}
