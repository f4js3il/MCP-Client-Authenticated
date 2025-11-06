package com.example.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.client.endpoint.*;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Adds an audience parameter to the client_credentials token request when configured.
 */
public class AudienceAwareClientCredentialsTokenResponseClient implements OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> {

    private static final Logger log = LoggerFactory.getLogger(AudienceAwareClientCredentialsTokenResponseClient.class);

    private final OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> delegate;
    private final String audience; // null or trimmed value

    AudienceAwareClientCredentialsTokenResponseClient(String audience) {
        this.audience = (audience != null && !audience.trim().isEmpty()) ? audience.trim() : null;
        RestClientClientCredentialsTokenResponseClient client =
                new RestClientClientCredentialsTokenResponseClient();
        Converter<OAuth2ClientCredentialsGrantRequest, MultiValueMap<String, String>> paramsConverter =
                grantRequest -> {
                    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                    // default parameters
                    params.add("grant_type", "client_credentials");
                    params.add("client_id", grantRequest.getClientRegistration().getClientId());
                    params.add("client_secret", grantRequest.getClientRegistration().getClientSecret());

                    // custom audience parameter when present
                    if (this.audience != null) {
                        params.add("audience", this.audience);
                    }

                    return params;
                };
        client.setParametersConverter(paramsConverter);
        this.delegate = client;
    }

    @Override
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2ClientCredentialsGrantRequest grantRequest) {
        log.debug("Requesting new client_credentials access token for registrationId={} (audience set? {} )",
                grantRequest.getClientRegistration().getRegistrationId(), this.audience != null);
        return this.delegate.getTokenResponse(grantRequest);
    }
}