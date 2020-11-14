package net.savantly.authorization.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

/**
 * 
 * @author jeremy branham
 *
 */
public class PermissionAwareJwtAuthenticationConverter extends JwtAuthenticationConverter {
	
	private static final Logger log = LoggerFactory.getLogger(PermissionAwareJwtAuthenticationConverter.class);
	private final PermissionProvider permissionProvider;
	private final String groupsClaim;

	public PermissionAwareJwtAuthenticationConverter(PermissionProvider permissionProvider) {
		this(permissionProvider, "groups");
	}
	public PermissionAwareJwtAuthenticationConverter(PermissionProvider permissionProvider, String groupsClaim) {
		this.permissionProvider = permissionProvider;
		this.groupsClaim = groupsClaim;
	}

	@Override
	protected Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
		Collection<String> usersGroups = getRolesFromClaims(jwt.getClaims());
		Collection<GrantedAuthority> allAuthorities = usersGroups.stream().map(SimpleGrantedAuthority::new)
				.collect(Collectors.toList());
		List<SimpleGrantedAuthority> roleAuthorities = getPermissionsFromRoles(usersGroups)
				.stream()
				.map(SimpleGrantedAuthority::new)
				.collect(Collectors.toList());
		allAuthorities.addAll(roleAuthorities);
		return allAuthorities;
	}
	
	private List<String> getPermissionsFromRoles(Collection<String> usersRoles) {
		List<String> effectiveAuthorities = new ArrayList<>();
		usersRoles.forEach(r -> {
			effectiveAuthorities.addAll(permissionProvider.getEffectivePermissions(r));
		});
		if(log.isDebugEnabled()) {
			log.debug("role: {} effective permissions: {}", effectiveAuthorities);
		}
		return effectiveAuthorities;
	}

	@SuppressWarnings("unchecked")
    private Collection<String> getRolesFromClaims(Map<String, Object> claims) {
		if (groupsClaim.contains(".")) {
			String[] pathParts = groupsClaim.split("\\.");
			try {
			return getNestedValue(claims, pathParts);
			} catch (Exception e) {
				throw new RuntimeException("failed to extract roles using groupsClaim value: " + groupsClaim, e);
			}
		} else {
			return (Collection<String>) claims.getOrDefault(groupsClaim, new ArrayList<>());
		}
    }
	
	private static <T> T getNestedValue(Map map, String... keys) {
	    Object value = map;

	    for (String key : keys) {
	        value = ((Map) value).get(key);
	    }

	    return (T) value;
	}
}