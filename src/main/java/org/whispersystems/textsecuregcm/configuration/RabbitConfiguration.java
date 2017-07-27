package org.whispersystems.textsecuregcm.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;


public class RabbitConfiguration {

    @NotEmpty
    @JsonProperty private String host;

    @NotEmpty
    @JsonProperty private String queue;

    @NotEmpty
    @JsonProperty private String username;

    @NotEmpty
    @JsonProperty private String password;

    @JsonProperty private int port;

    @NotEmpty
    @JsonProperty private String vhost;

    public String getQueue() {
        return queue;
    }

    public String getHost() {
        return host;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }

    public String getVhost() {
        return vhost;
    }
}
