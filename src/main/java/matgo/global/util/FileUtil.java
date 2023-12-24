package matgo.global.util;

import static matgo.global.exception.ErrorCode.FILE_SIZE_EXCEEDED;
import static matgo.global.exception.ErrorCode.NOT_FOUND_IMAGE_EXTENSION;

import java.util.Objects;
import matgo.global.filesystem.AllowedFileExtension;
import matgo.global.filesystem.FileException;
import org.springframework.web.multipart.MultipartFile;

public class FileUtil {

    private static final long MAX_FILE_SIZE = 1024 * 1024 * 5; // 5MB

    public static String getExtension(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        if (Objects.nonNull(originalFileName)) {
            int lastIndexOf = originalFileName.lastIndexOf(".");
            if (lastIndexOf != -1) {
                return originalFileName.substring(lastIndexOf + 1);
            }
        }

        throw new FileException(NOT_FOUND_IMAGE_EXTENSION);
    }

    public static boolean isImageExtension(String extension) {
        return AllowedFileExtension.isImageExtension(extension);
    }

    public static void checkFileSize(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileException(FILE_SIZE_EXCEEDED);
        }
    }

}
