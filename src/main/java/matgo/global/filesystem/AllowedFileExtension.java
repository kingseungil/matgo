package matgo.global.filesystem;

import java.util.Arrays;

public enum AllowedFileExtension {
    JPG, JPEG, PNG, SVG, GIF;

    public static boolean isImageExtension(String extension) {
        String upperCaseExtension = extension.toUpperCase();

        return Arrays.stream(AllowedFileExtension.values())
                     .anyMatch(allowedFileExtension -> allowedFileExtension.name().equals(upperCaseExtension));
    }
}