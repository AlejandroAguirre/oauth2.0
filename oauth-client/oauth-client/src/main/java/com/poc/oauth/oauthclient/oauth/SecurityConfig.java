package com.poc.oauth.oauthclient.oauth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.web.SecurityFilterChain;

import feign.RequestInterceptor;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http.authorizeHttpRequests(
				auth -> auth.requestMatchers("/", "/css/**", "/js/**").permitAll().anyRequest().authenticated())
				.oauth2Login(oauth2 -> oauth2.defaultSuccessUrl("/user", true)).oauth2Client(Customizer.withDefaults())
				.logout(logout -> logout.logoutSuccessUrl("/").invalidateHttpSession(true).clearAuthentication(true)
						.deleteCookies("JSESSIONID"));

		return http.build();
	}

	@Bean
	public RequestInterceptor oauth2Interceptor(OAuth2AuthorizedClientManager authorizedClientManager) {
		return requestTemplate -> {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId("oauth-client")
					.principal(authentication).build();
			OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);
			String token = authorizedClient.getAccessToken().getTokenValue();
			System.out.println("================================");
			System.out.println("ACCESS TOKEN = " + token);
			System.out.println("================================");
			requestTemplate.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
		};
	}
}