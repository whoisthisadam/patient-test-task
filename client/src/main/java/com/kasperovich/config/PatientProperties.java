package com.kasperovich.config;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import static lombok.AccessLevel.PRIVATE;

@Configuration
@ConfigurationProperties(prefix = "patients")
@FieldDefaults(level = PRIVATE)
@Getter
@Setter
public class PatientProperties {

    String baseUrl;

    String username;

    String password;

    int recordsNumber;

}
