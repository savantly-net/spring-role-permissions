package net.savantly.authorization.jdbc;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;

@Entity
public class RolePermissions {

	@Id
	private String role;
	
	@ElementCollection(fetch = FetchType.EAGER)
	private List<String> permissions = new ArrayList<>();
	
	public RolePermissions() {}
	
	public RolePermissions(String role, List<String> permissions) {
		this.role = role;
		this.permissions = permissions;
	}
	
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	
	public List<String> getPermissions() {
		return permissions;
	}
	public void setPermissions(List<String> permissions) {
		this.permissions = permissions;
	}
}
