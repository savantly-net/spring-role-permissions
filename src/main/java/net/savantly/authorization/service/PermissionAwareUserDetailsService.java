package net.savantly.authorization.service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Wraps a UserDetailsService to inject mapped permissions as granted authorities
 * @author Jeremy Branham
 *
 */
public class PermissionAwareUserDetailsService implements UserDetailsService {
	
	/**
	 * internal UserDetailsService
	 */
	private UserDetailsService userDetailsService;
	
	private PermissionProvider permissionProvider;
	
	public PermissionAwareUserDetailsService(UserDetailsService userDetailsService, PermissionProvider permissionProvider) {
		this.userDetailsService = userDetailsService;
		this.permissionProvider = permissionProvider;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
		List<GrantedAuthority> permissions = extractPermissions(userDetails);
		return new UserDetailsWrapper(userDetails, permissions);
	}

	private List<GrantedAuthority> extractPermissions(UserDetails userDetails) {
		List<GrantedAuthority> permissions = userDetails.getAuthorities().stream()
			.flatMap(a -> permissionProvider.getEffectivePermissions(a.getAuthority()).stream()).distinct()
			.map(p -> new SimpleGrantedAuthority(p))
			.collect(Collectors.toList());
		// add the original authorities back to the list
		permissions.addAll(userDetails.getAuthorities());
		return permissions;
	}
	
	protected class UserDetailsWrapper implements UserDetails {
		
		private static final long serialVersionUID = 1L;
		UserDetails userDetails;
		private List<GrantedAuthority> permissions;

		public UserDetailsWrapper(UserDetails userDetails, List<GrantedAuthority> permissions) {
			this.userDetails = userDetails;
			this.permissions = permissions;
		}

		@Override
		public Collection<? extends GrantedAuthority> getAuthorities() {
			return permissions;
		}

		@Override
		public String getPassword() {
			return this.userDetails.getPassword();
		}

		@Override
		public String getUsername() {
			return this.userDetails.getUsername();
		}

		@Override
		public boolean isAccountNonExpired() {
			return this.userDetails.isAccountNonExpired();
		}

		@Override
		public boolean isAccountNonLocked() {
			return this.userDetails.isAccountNonLocked();
		}

		@Override
		public boolean isCredentialsNonExpired() {
			return this.userDetails.isCredentialsNonExpired();
		}

		@Override
		public boolean isEnabled() {
			return this.userDetails.isEnabled();
		}
		
	}

}
