package org.whispersystems.textsecuregcm.minio;


import com.amazonaws.HttpMethod;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;


public class MinioClient {

  private static final String TAG = MinioClient.class.getSimpleName();
  private final Logger logger;
  private io.minio.MinioClient minioClient;

  public MinioClient(String endpoint, String accessKey, String secretKey) {
    logger = LoggerFactory.getLogger(MinioClient.class);
    try {
      minioClient = new io.minio.MinioClient(endpoint, accessKey, secretKey);
    } catch (InvalidEndpointException | InvalidPortException e) {
      logger.error("Minio init Error:", e);
      minioClient = null;
    }
  }

  public void deleteObject(String bucketName, String objectName) {
    if (minioClient != null) {
      try {
        minioClient.removeObject(bucketName, objectName);
      } catch (Exception e) {
        logger.error(TAG, "deleteObject error", e);
      }
    }
  }

  public URL getPreSignedUrlMinio(long attachmentId, HttpMethod method, String bucket, int duration)
          throws IOException {
    try {
      String request;
      if (method == HttpMethod.GET) {
        request = minioClient.presignedGetObject(bucket, String.valueOf(attachmentId), duration);
      } else if (method == HttpMethod.PUT) {
        request = minioClient.presignedPutObject(bucket, String.valueOf(attachmentId), duration);
      } else {
        throw new Exception("Unknown method");
      }
      return new URL(request);
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

}
