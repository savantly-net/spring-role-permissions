package net.savantly.authorization.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
	
	@RequestMapping("/")
	public String home() {
		return "home";
	}
	
	@RequestMapping("/secure")
	public String secure() {
		return "secure";
	}

	@PreAuthorize("hasAuthority('CREATE')")
	@RequestMapping("/authority/create")
	public String authCreate() {
		return "create";
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping("/role/create")
	public String roleCreate() {
		return "create";
	}

}
