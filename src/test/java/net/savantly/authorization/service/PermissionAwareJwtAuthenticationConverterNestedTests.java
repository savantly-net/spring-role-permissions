package net.savantly.authorization.service;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.test.web.servlet.MockMvc;

import net.savantly.authorization.configuration.EnableRolePermissions;
import net.savantly.authorization.controller.TestController;
import net.savantly.authorization.jdbc.RolePermissions;
import net.savantly.authorization.jdbc.RolePermissionsRepository;
import net.savantly.authorization.jwt.MockJwt;
import net.savantly.authorization.jwt.MockJwtDecoder;

@SpringBootTest
@AutoConfigureMockMvc
public class PermissionAwareJwtAuthenticationConverterNestedTests {
	
	protected static final String USER_BASIC = "user";
	protected static final String USER_ADMIN = "admin";
	protected static final String ROLE_BASIC = "ROLE_USER";
	protected static final String ROLE_ADMIN = "ROLE_ADMIN";
	protected static final String TEST_PASSWORD = "TEST_PASSWORD";
	
	@Autowired
	private RolePermissionsRepository repository;
	@Autowired
    private MockMvc mvc;
	
	@BeforeEach
	public void beforeEach() {
		List<String> adminPermissions = new ArrayList<String>();
		adminPermissions.add("CREATE");
		adminPermissions.add("READ");
		adminPermissions.add("UPDATE");
		adminPermissions.add("DELETE");
		repository.save(new RolePermissions(ROLE_ADMIN, adminPermissions));

		List<String> basicPermissions = new ArrayList<String>();
		basicPermissions.add("READ");
		repository.save(new RolePermissions(ROLE_BASIC, basicPermissions));
	}
	
	@AfterEach
	public void afterEach() {
		repository.deleteAll();
	}
	
	@Test
	public void testAnonymousOk() throws Exception {
		mvc.perform(get("/"))
				.andExpect(status().isOk());
	}
	
	@Test
	public void testAnonymousUnauthorized() throws Exception {
		mvc.perform(get("/secure/"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	public void testSecureOk() throws Exception {
		mvc.perform(get("/secure/")
				.header("Authorization", getAuthorizationHeaderValue(USER_BASIC, ROLE_BASIC)))
				.andExpect(status().isOk());
	}
	
	@Test
	public void testCreateByAuthorityOk() throws Exception {
		mvc.perform(get("/authority/create")
				.header("Authorization", getAuthorizationHeaderValue(USER_ADMIN, ROLE_ADMIN)))
				.andExpect(status().isOk());
	}
	
	@Test
	public void testCreateByRoleOk() throws Exception {
		mvc.perform(get("/role/create")
				.header("Authorization", getAuthorizationHeaderValue(USER_ADMIN, ROLE_ADMIN)))
				.andExpect(status().isOk());
	}
	
	@Test
	public void testCreateByAuthorityDenied() throws Exception {
		mvc.perform(get("/authority/create")
				.header("Authorization", getAuthorizationHeaderValue(USER_BASIC, ROLE_BASIC)))
				.andExpect(status().isForbidden());
	}
	
	private String getAuthorizationHeaderValue(String username, String group) {
		return String.format("Bearer %s", new MockJwt().createJWT(username, Collections.singletonList(group)));
	}
	
	@SpringBootApplication
	@EnableRolePermissions
	@EnableGlobalMethodSecurity(
			  prePostEnabled = true, 
			  securedEnabled = true, 
			  jsr250Enabled = true)
	public static class testConfig {
		
		@Bean
		public TestController testController() {
			return new TestController();
		}
		
		@Bean
		public PermissionAwareJwtAuthenticationConverter jwtAuthenticationConverter(PermissionProvider permissionProvider) {
			return new PermissionAwareJwtAuthenticationConverter(permissionProvider, "nested.groups");
		}

		@Bean
		public WebSecurityConfigurerAdapter webSecurity(PermissionAwareJwtAuthenticationConverter jwtAuthenticationConverter) {
			
			return new WebSecurityConfigurerAdapter() {
				
				@Override
				protected void configure(HttpSecurity http) throws Exception {
					http.authorizeRequests().antMatchers("/secure/**").authenticated()
					.and().oauth2ResourceServer(oauth2ResourceServer ->
						oauth2ResourceServer
							.jwt(jwt -> {
								jwt.jwtAuthenticationConverter(jwtAuthenticationConverter);
								jwt.decoder(new MockJwtDecoder());
							})
		 			);
				}
			};
		}
	}
}
