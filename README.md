# spring-role-permissions

A Spring Boot library for easy Role to Permission relationships.  


### Quickstart  

Include the dependency  

Gradle

```
implementation 'net.savantly.security:spring-role-permissions:0.0.1.RELEASE'
```

Maven

```
<dependency>
  <groupId>net.savantly.security</groupId>
  <artifactId>spring-role-permissions</artifactId>
  <version>0.0.1.RELEASE</version>
</dependency>
```

Add the `@EnableRolePermissions` annotation on a Spring Configuaration class to automatically create `JdbcPermissionProvider` and `RolePermissionsRepository` beans.  

Example - 

```java
@SpringBootApplication
@EnableRolePermissions
@EnableGlobalMethodSecurity(
  prePostEnabled = true, 
  securedEnabled = true, 
  jsr250Enabled = true)
public class MyApplication {
	
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
}
```

Add Role to Permission mappings using the `RolePermissionsRepository`  

```java
@Autowired 
RolePermissionsRepository repository;

public void setup(){
  String roleName = "ROLE_ADMIN";
  List<String> permissions = new ArrayList<String>();
  permissions.add("CREATE");
  permissions.add("READ");
  permissions.add("UPDATE");
  permissions.add("DELETE");
  repository.save(new RolePermissions(roleName, permissions));
}
```

When an `Authentication` object is injected in the current security session, it will have the original roles/granted authorities.  
This library provides 2 built-in integration points for Spring Security.  
When the integration point is reached, the `PermissionProvider` is called to get the effective permissions for each role the user is a member of.  The distinct list of permissions are added as `GrantedAuthority` items in the `Authentication` object.  

You may also implement a custom `PermissionProvider` to provide a mapping of roles to permissions to Spring Security. For example, if you'd like to store your permissions in memory, or a configuration file, you just need to implement the [PermissionProvider](./src/main/java/net/savantly/authorization/service/PermissionProvider.java) interface, and setup one of the integration methods.


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

Standard Spring Security annotations can be used the same as before - but the user's granted authorities now also contain the permissions that have been mapped.  

#### Example using the security annotations

```java
@PreAuthorize("hasAuthority('CREATE')")
  @RequestMapping("/create")
  public String create() {
  ...
}
```