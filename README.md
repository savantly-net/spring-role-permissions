# spring-role-permissions

A Spring Boot library for easy Role to Permission relationships.  


### Quickstart  

Add the `@EnableRolePermissions` annotation on a Spring Configuaration class to automatically create `JdbcPermissionProvider` and `RolePermissionsRepository` beans.  
 
There are 2 built-in integration points for Spring Security.  
You may also implement a custom `PermissionProvider` to provide a mapping of roles to permissions to Spring Security. 


#### Example OAuth/JWT Configuration -  
The `PermissionAwareJwtAuthenticationConverter` can be used for OAuth/JWT integration.  

```java
@Bean
public PermissionAwareJwtAuthenticationConverter jwtAuthenticationConverter(PermissionProvider permissionProvider) {
	return new PermissionAwareJwtAuthenticationConverter(permissionProvider);
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
					})
 			);
		}
	};
}
```

#### Example UserDetailsService Wrapper  
The `PermissionAwareUserDetailsService` can be used as a wrapper around your existing `UserDetailsService` -   

```java
@Bean
public WebSecurityConfigurerAdapter webSecurity(JdbcPermissionProvider permissionProvider) {
	
	return new WebSecurityConfigurerAdapter() {
		
		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.authorizeRequests().antMatchers("/secure/**").authenticated()
			.and().httpBasic();
		}
		
		@Autowired
		public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
			PermissionAwareUserDetailsService permissionAwareUserDetailsService = 
					new PermissionAwareUserDetailsService(myUserDetailsService(), permissionProvider);
			auth.userDetailsService(permissionAwareUserDetailsService)
				.passwordEncoder(NoOpPasswordEncoder.getInstance());
		}
	};
}

```