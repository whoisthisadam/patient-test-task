package com.kasperovich.patients.security;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import static lombok.AccessLevel.PRIVATE;

@Configuration
@ConfigurationProperties(prefix = "keycloak")
@Getter
@Setter
@FieldDefaults(level = PRIVATE)
public class KeycloakProperties {

    String serverUrl;
    String realm;
    String username;
    String password;
    String clientId;
    String targetClientId;
}
