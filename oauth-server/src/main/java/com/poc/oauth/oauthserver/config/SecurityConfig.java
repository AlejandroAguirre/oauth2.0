package com.poc.oauth.oauthserver.config;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	@Value("${oauth.clients.oauth-client.redirect-uri}")
	private String oauthClientRedirectUri;

	@Value("${oauth.clients.oauth-client.post-logout-redirect-uri}")
	private String oauthClientPostLogoutRedirectUri;

	@Value("${oauth.clients.angular-client.redirect-uri}")
	private String angularClientRedirectUri;

	@Value("${oauth.clients.angular-client.post-logout-redirect-uri}")
	private String angularClientPostLogoutRedirectUri;

	@Value("${cors.allowed-origin}")
	private String corsAllowedOrigin;

	@Bean
	@Order(1)
	public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {

		OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = OAuth2AuthorizationServerConfigurer
				.authorizationServer();

		http.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
				.with(authorizationServerConfigurer,
						authorizationServer -> authorizationServer.oidc(Customizer.withDefaults()))
				.authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
				.exceptionHandling(exceptions -> exceptions.defaultAuthenticationEntryPointFor(
						new LoginUrlAuthenticationEntryPoint("/login"),
						new MediaTypeRequestMatcher(MediaType.TEXT_HTML)))
				.cors(Customizer.withDefaults());
		return http.build();
	}

	@Bean
	@Order(2)
	public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {

		http.authorizeHttpRequests(authorize -> authorize.requestMatchers("/login", "/css/**", "/js/**",
				"/h2-console/**", "/error", "/.well-known/appspecific/**").permitAll().anyRequest().authenticated())
				.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**", "/oauth2/token"))
				.headers(headers -> headers.frameOptions(frame -> frame.disable()))
				.formLogin(form -> form.loginPage("/login").permitAll()).cors(Customizer.withDefaults());

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public RegisteredClientRepository registeredClientRepository() {

		RegisteredClient oidcClient = RegisteredClient.withId(UUID.randomUUID().toString()).clientId("oidc-client")
				.clientSecret("{noop}123456789")
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
				.redirectUri("https://oauthdebugger.com/debug").scope(OidcScopes.OPENID).scope(OidcScopes.PROFILE)
				.scope("read").scope("write").build();

		RegisteredClient oauthClient = RegisteredClient.withId(UUID.randomUUID().toString()).clientId("oauth-client")
				.clientSecret(new BCryptPasswordEncoder().encode("12345678910"))
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
				.redirectUri(oauthClientRedirectUri)//http://127.0.0.1:8080/login/oauth2/code/oauth-client
				.postLogoutRedirectUri(oauthClientPostLogoutRedirectUri).scope(OidcScopes.OPENID)//http://127.0.0.1:8080/logout
				.scope(OidcScopes.PROFILE).scope("read").scope("write")
				.clientSettings(ClientSettings.builder().requireAuthorizationConsent(false).build()).build();

		RegisteredClient angularClient = RegisteredClient.withId(UUID.randomUUID().toString())
				.clientId("angular-client").clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.redirectUri(angularClientRedirectUri)//http://127.0.0.1:4200/login-callback
				.postLogoutRedirectUri(angularClientPostLogoutRedirectUri).scope(OidcScopes.OPENID)//http://127.0.0.1:4200/logout
				.scope(OidcScopes.PROFILE).scope("read").scope("write")
				.clientSettings(
						ClientSettings.builder().requireProofKey(true).requireAuthorizationConsent(false).build())
				.build();

		return new InMemoryRegisteredClientRepository(oidcClient, oauthClient, angularClient);
	}

	@Bean
	public JWKSource<SecurityContext> jwkSource() {
		KeyPair keyPair = generateRsaKey();
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		RSAKey rsaKey = new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(UUID.randomUUID().toString())
				.build();
		JWKSet jwkSet = new JWKSet(rsaKey);
		return new ImmutableJWKSet<>(jwkSet);
	}

	@Bean
	public CorsFilter corsFilter() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of(corsAllowedOrigin));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return new CorsFilter(source);
	}

	private static KeyPair generateRsaKey() {

		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(2048);
			return keyPairGenerator.generateKeyPair();
		} catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

	@Bean
	public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
		return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
	}

	@Bean
	public AuthorizationServerSettings authorizationServerSettings() {
		return AuthorizationServerSettings.builder().build();
	}
}
