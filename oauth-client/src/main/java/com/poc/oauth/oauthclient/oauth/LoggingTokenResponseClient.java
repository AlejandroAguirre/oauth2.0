package com.poc.oauth.oauthclient.oauth;

import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.stereotype.Component;

@Component
public class LoggingTokenResponseClient
		implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {

	private final RestClientAuthorizationCodeTokenResponseClient delegate = new RestClientAuthorizationCodeTokenResponseClient();

	@Override
	public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest request) {

		OAuth2AccessTokenResponse response = delegate.getTokenResponse(request);

		System.out.println();
		System.out.println("==============================================");
		System.out.println("       RESPUESTA DE /oauth2/token");
		System.out.println("==============================================");

		System.out.println("ACCESS TOKEN:");
		System.out.println(response.getAccessToken().getTokenValue());

		System.out.println();
		System.out.println("REFRESH TOKEN:");
		System.out.println(response.getRefreshToken() != null ? response.getRefreshToken().getTokenValue()
				: "NO SE RECIBIO REFRESH TOKEN");

		System.out.println();
		System.out.println("TOKEN TYPE:");
		System.out.println(response.getAccessToken().getTokenType());

		System.out.println();
		System.out.println("SCOPES:");
		System.out.println(response.getAccessToken().getScopes());

		System.out.println();
		System.out.println("EXPIRES AT:");
		System.out.println(response.getAccessToken().getExpiresAt());

		System.out.println();
		System.out.println("PARAMETROS ADICIONALES:");
		System.out.println(response.getAdditionalParameters());

		System.out.println("==============================================");
		System.out.println();

		return response;
	}
}