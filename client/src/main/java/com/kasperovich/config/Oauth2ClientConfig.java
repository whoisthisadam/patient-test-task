package com.kasperovich.config;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

import static lombok.AccessLevel.PRIVATE;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class Oauth2ClientConfig {

    PatientProperties patientProperties;

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService oAuth2AuthorizedClientService) {

        OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
                .refreshToken()
                .password()
                .build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, oAuth2AuthorizedClientService);

        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        authorizedClientManager.setContextAttributesMapper(authorizeRequest -> {
            Map<String, Object> contextAttributes = new HashMap<>();

            contextAttributes.put(OAuth2AuthorizationContext.USERNAME_ATTRIBUTE_NAME, patientProperties.getUsername());
            contextAttributes.put(OAuth2AuthorizationContext.PASSWORD_ATTRIBUTE_NAME, patientProperties.getPassword());

            return contextAttributes;
        });

        return authorizedClientManager;
    }

    @Bean
    WebClient webClient(OAuth2AuthorizedClientManager authorizedClientManager) {

        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Client =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);

        oauth2Client.setDefaultClientRegistrationId("patients");

        return WebClient.builder()
                .baseUrl(patientProperties.getBaseUrl())
                .apply(oauth2Client.oauth2Configuration())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
