package com.poc.oauth.oauthserver.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ErrorController {

	@GetMapping("/error")
	@ResponseBody
	public String error(HttpServletRequest request) {

		return "ERROR - status=" + request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE) + ", exception="
				+ request.getAttribute(RequestDispatcher.ERROR_EXCEPTION) + ", message="
				+ request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
	}
}