package matgo.global.type;

import lombok.Getter;

@Getter
public enum S3Directory {
    MEMBER("member"),
    REVIEW("review"),
    POST("post");

    private final String directory;

    S3Directory(String directory) {
        this.directory = directory;
    }

}
