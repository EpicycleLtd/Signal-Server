package org.whispersystems.textsecuregcm.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class ProfilesConfiguration {
  @NotEmpty
  @JsonProperty
  private String accessKey;

  @NotEmpty
  @JsonProperty
  private String accessSecret;

  @NotEmpty
  @JsonProperty
  private String bucket;

  @NotEmpty
  @JsonProperty
  private String region;

  @JsonProperty
  private String providerUrl = null;

  public String getAccessKey() {
    return accessKey;
  }

  public String getAccessSecret() {
    return accessSecret;
  }

  public String getBucket() {
    return bucket;
  }

  public String getRegion() {
    return region;
  }

  public String getProviderUrl() {
    return providerUrl;
  }
}
