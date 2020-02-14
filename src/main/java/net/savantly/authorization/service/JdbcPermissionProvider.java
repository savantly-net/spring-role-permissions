package net.savantly.authorization.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.savantly.authorization.jdbc.RolePermissions;
import net.savantly.authorization.jdbc.RolePermissionsRepository;

/**
 * 
 * @author jeremy branham
 *
 */
public class JdbcPermissionProvider implements PermissionProvider {
	
	private static final Logger log = LoggerFactory.getLogger(JdbcPermissionProvider.class);
	
	private RolePermissionsRepository rolePermissionsRepository;
	
	public JdbcPermissionProvider(RolePermissionsRepository rolePermissionsRepository) {
		this.rolePermissionsRepository = rolePermissionsRepository;
	}

	@Override
	public List<String> getEffectivePermissions(String role) {
		Optional<RolePermissions> roleMap = this.rolePermissionsRepository.findById(role);
		if (roleMap.isPresent()) {
			if (log.isDebugEnabled()) {
				log.debug("role: {} effective permissions: {}", role, roleMap.get().getPermissions());
			}
			return roleMap.get().getPermissions();
		} else {
			return Collections.emptyList();
		}
	}

}
