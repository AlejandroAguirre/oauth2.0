package com.poc.oauth.oauthclient.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.poc.oauth.oauthclient.dto.EmpleadoDTO;
import com.poc.oauth.oauthclient.feing.UserClient;

@Controller
public class UserController {

	private final UserClient userClient;

	public UserController(UserClient userClient) {
		this.userClient = userClient;
	}

	@GetMapping("/user")
	public String user(Model model) {
		UserResponse response = userClient.getUser();
		model.addAttribute("user", response);
		return "user";
	}

	public record UserResponse(String mensaje, String usuario, List<String> authorities, List<EmpleadoDTO> empleados) {
	}

}