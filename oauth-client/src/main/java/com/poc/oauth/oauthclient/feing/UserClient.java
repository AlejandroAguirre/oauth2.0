package com.poc.oauth.oauthclient.feing;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.poc.oauth.oauthclient.controllers.UserController.UserResponse;

@FeignClient(name = "userClient")
public interface UserClient {

	@GetMapping("/resources/user")
	UserResponse getUser();
}