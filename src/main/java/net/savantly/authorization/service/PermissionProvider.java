package net.savantly.authorization.service;

import java.util.List;

public interface PermissionProvider {
	
	List<String> getEffectivePermissions(String role);

}
