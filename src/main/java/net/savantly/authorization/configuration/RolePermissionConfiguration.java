package net.savantly.authorization.configuration;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import net.savantly.authorization.jdbc.RolePermissionsRepository;
import net.savantly.authorization.service.JdbcPermissionProvider;
import net.savantly.authorization.service.PermissionProvider;

/**
 * 
 * @author jeremy branham
 *
 */
@Configuration
@EnableJpaRepositories(basePackages = "net.savantly.authorization.jdbc")
@EntityScan(basePackages = "net.savantly.authorization.jdbc")
public class RolePermissionConfiguration {

	@Bean
	public PermissionProvider jdbcPermissionProvider(RolePermissionsRepository rolePermissionRepository) {
		return new JdbcPermissionProvider(rolePermissionRepository);
	}
}
