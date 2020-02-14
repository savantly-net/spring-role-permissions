package net.savantly.authorization.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import net.savantly.authorization.configuration.EnableRolePermissions;
import net.savantly.authorization.jdbc.RolePermissions;
import net.savantly.authorization.jdbc.RolePermissionsRepository;

@DataJpaTest
public class PermissionProviderImplTests {
	
	String roleName = "TEST_ROLE";
	List<String> permissions = new ArrayList<String>();
	{
		permissions.add("CREATE");
		permissions.add("READ");
		permissions.add("UPDATE");
		permissions.add("DELETE");
	}
	
	@Autowired
	RolePermissionsRepository repository;
	
	@BeforeEach
	public void beforeEach() {
		repository.save(new RolePermissions(roleName, permissions));
	}
	
	@AfterEach
	public void afterEach() {
		repository.deleteAll();
	}

	@Test
	public void testEffectivePermissions() {
		JdbcPermissionProvider provider = new JdbcPermissionProvider(repository);
		List<String> effectivePermissions = provider.getEffectivePermissions(roleName);
		assertTrue(effectivePermissions.contains("CREATE"), "CREATE permission should be present");
		assertTrue(effectivePermissions.contains("READ"), "READ permission should be present");
		assertTrue(effectivePermissions.contains("UPDATE"), "UPDATE permission should be present");
		assertTrue(effectivePermissions.contains("DELETE"), "DELETE permission should be present");
	}
	
	@SpringBootApplication
	@EnableRolePermissions
	public static class TestConfig {
		
	}
	
}
