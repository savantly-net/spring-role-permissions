package com.example.app;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import net.savantly.authorization.configuration.EnableRolePermissions;
import net.savantly.authorization.service.PermissionProvider;

class ConfigurationTests {

	@Test
	void permissionProviderCreated() {
		final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
				.withUserConfiguration(TestConfig.class);
		contextRunner.run((context) -> {
			assertThat(context).getBean(PermissionProvider.class).isNotNull();
		});
	}
	
	@SpringBootApplication
	@EnableRolePermissions
	public static class TestConfig {
		
	}

}
