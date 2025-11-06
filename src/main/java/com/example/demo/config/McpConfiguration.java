package com.example.demo.config;

import io.modelcontextprotocol.client.transport.customizer.McpSyncHttpClientRequestCustomizer;
import org.springaicommunity.mcp.security.client.sync.AuthenticationMcpTransportContextProvider;
import org.springaicommunity.mcp.security.client.sync.oauth2.http.client.OAuth2ClientCredentialsSyncHttpRequestCustomizer;
import org.springframework.ai.mcp.customizer.McpSyncClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import java.time.Duration;


@Configuration
class McpConfiguration {




    @Bean
    public OAuth2AuthorizedClientService authorizedClientService(
            ClientRegistrationRepository clientRegistrationRepository) {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    }

    /**
     * Configure AuthorizedClientServiceOAuth2AuthorizedClientManager for client credentials flow
     */
    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> audienceAwareClientCredentialsTokenClient(
            @org.springframework.beans.factory.annotation.Value("${spring.client-credentials.audience:}") String audience) {
        return new AudienceAwareClientCredentialsTokenResponseClient(audience);
    }

    @Bean
    public AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService,
            OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> audienceAwareClient) {

        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials(cc -> cc
                                .accessTokenResponseClient(audienceAwareClient)
                                .clockSkew(Duration.ofSeconds(60))
                        )
                        .authorizationCode()
                        .refreshToken()
                        .build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository,
                        authorizedClientService);

        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

    /**
     * Option 1: Client Credentials Only
     * Use this when you only need machine-to-machine authentication
     * without user-level permissions
     */
    @Bean
    public McpSyncHttpClientRequestCustomizer clientCredentialsCustomizer(
            AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager) {
        return new OAuth2ClientCredentialsSyncHttpRequestCustomizer(
                authorizedClientManager,
                "mcp-client-credentials"  // Registration ID from application.yml
        );
    }

    /**
     * Add transport context provider to make authentication data available
     */
    @Bean
    public McpSyncClientCustomizer syncClientCustomizer() {
        return (name, syncSpec) -> syncSpec.transportContextProvider(
                new AuthenticationMcpTransportContextProvider()
        );
    }
}