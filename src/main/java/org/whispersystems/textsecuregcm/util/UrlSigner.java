/**
 * Copyright (C) 2013 Open WhisperSystems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.whispersystems.textsecuregcm.util;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import org.whispersystems.textsecuregcm.configuration.S3Configuration;
import io.minio.MinioClient;

import java.net.URL;
import java.util.Date;

public class UrlSigner {

  private static final long   DURATION = 60 * 60 * 1000;
  private static final int    DURATION_M = 60 * 60;

  private final AWSCredentials credentials;
  private final String bucket;
  //
  private final String accessKey;
  private final String accessSecret;
  private final String providerUrl;

  public UrlSigner(S3Configuration config) {
    this.credentials = new BasicAWSCredentials(config.getAccessKey(), config.getAccessSecret());
    this.bucket      = config.getAttachmentsBucket();
    //
    this.accessKey    = config.getAccessKey();
    this.accessSecret = config.getAccessSecret();
    this.providerUrl  = config.getProviderUrl();
  }

  public URL getPreSignedUrl(long attachmentId, HttpMethod method)
    throws Exception
  {
    if(this.providerUrl == null || this.providerUrl.isEmpty()) {
      AmazonS3                    client  = new AmazonS3Client(credentials);
      GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, String.valueOf(attachmentId), method);

      request.setExpiration(new Date(System.currentTimeMillis() + DURATION));
      request.setContentType("application/octet-stream");

      return client.generatePresignedUrl(request);
    } else {
      return getPreSignedUrlMinio(attachmentId, method);
    }
  }

  private URL getPreSignedUrlMinio(long attachmentId, HttpMethod method)
    throws Exception
  {
    MinioClient client = new MinioClient(providerUrl, accessKey, accessSecret);
    String request;
    if (method == HttpMethod.GET) {
      request = client.presignedGetObject(
        bucket, String.valueOf(attachmentId), DURATION_M
      );
    } else if (method == HttpMethod.PUT) {
      request = client.presignedPutObject(
        bucket, String.valueOf(attachmentId), DURATION_M
      );
    } else {
      throw new Exception();
    }
    return new URL(request);
  }
}
