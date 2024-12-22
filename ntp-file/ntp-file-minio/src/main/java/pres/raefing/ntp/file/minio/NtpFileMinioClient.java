package pres.raefing.ntp.file.minio;

import io.minio.*;

public class NtpFileMinioClient {

    public static void main(String[] args) {
        MinioClient client = MinioClient.builder()
                .endpoint("http://127.0.0.1:9000")
                .credentials("y5xXkPf6peX8i9ipOQhV","k0IlzhiSgeFonGe6VhRYnYovpErOyjQgNnPGcad5")
                .build();
        try {
            boolean isExists = client.bucketExists(BucketExistsArgs.builder().bucket("test.bucket").build());
            if (!isExists) {
                throw new RuntimeException();
            }
            SelectResponseStream stream = client.selectObjectContent(SelectObjectContentArgs.builder()
                    .bucket("test.bucket")
                    .object("差旅变更-截图.jpg")
                            .sqlExpression("")
                    .build());
            System.err.println(stream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
