package com.poc.oauth.resourceserver.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poc.oauth.resourceserver.dto.EmpleadoDTO;
import com.poc.oauth.resourceserver.dto.UserResponseDTO;
import com.poc.oauth.resourceserver.service.EmpleadoService;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

@RestController
@RequestMapping("/resources")
public class ResourceController {

	private final EmpleadoService empleadoService;

	public ResourceController(EmpleadoService empleadoService) {
		this.empleadoService = empleadoService;
	}

	@GetMapping("/user")
	@RateLimiter(name = "userEndpoint", fallbackMethod = "rateLimitFallback")

	public ResponseEntity<UserResponseDTO> readUser(Authentication authentication) {
		List<EmpleadoDTO> empleados = empleadoService.obtenerEmpleados();
		UserResponseDTO response = new UserResponseDTO("Usuario tiene permisos ", authentication.getName(),
				authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList(), empleados);
		return ResponseEntity.ok(response);
	}
	
	@PostMapping("/user")
	public ResponseEntity<UserResponse> writeUser(Authentication authentication) {

		UserResponse response = new UserResponse("Usuario tiene permisos ", authentication.getName(),
				authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());

		return ResponseEntity.ok(response);
	}
	
	public ResponseEntity<UserResponseDTO> rateLimitFallback(Authentication authentication, RequestNotPermitted ex) {
		return ResponseEntity.status(429).build();
	}
	
	public record UserResponse(String message, String username, List<String> authorities) {
	}
}