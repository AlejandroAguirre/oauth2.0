package com.poc.oauth.resourceserver.oauth;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.poc.oauth.resourceserver.error.JwtAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	@Value("${spring.cors.allowed-origin}")
    private String allowedOrigin;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationEntryPoint authenticationEntryPoint)
			throws Exception {
		http.cors(Customizer.withDefaults()).authorizeHttpRequests(authorize -> authorize
				.requestMatchers(HttpMethod.GET, "/resources/**").hasAnyAuthority("SCOPE_read", "SCOPE_write")
				.requestMatchers(HttpMethod.POST, "/resources/**").hasAuthority("SCOPE_write")
				.anyRequest().authenticated())
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults())
						.authenticationEntryPoint(authenticationEntryPoint));
		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of(allowedOrigin));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		configuration.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
