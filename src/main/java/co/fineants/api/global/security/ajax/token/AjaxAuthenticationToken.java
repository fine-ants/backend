package co.fineants.api.global.security.ajax.token;

import java.io.Serial;
import java.util.Collection;
import java.util.Objects;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.util.Assert;

public class AjaxAuthenticationToken extends AbstractAuthenticationToken {
	@Serial
	private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

	private final transient Object principal;

	private transient Object credentials;

	private AjaxAuthenticationToken(Object principal, Object credentials) {
		super(null);
		this.principal = principal;
		this.credentials = credentials;
		setAuthenticated(false);
	}

	private AjaxAuthenticationToken(Object principal, Object credentials,
		Collection<? extends GrantedAuthority> authorities) {
		super(authorities);
		this.principal = principal;
		this.credentials = credentials;
		super.setAuthenticated(true); // must use super, as we override
	}

	public static AjaxAuthenticationToken unauthenticated(Object principal, Object credentials) {
		return new AjaxAuthenticationToken(principal, credentials);
	}

	public static AjaxAuthenticationToken authenticated(Object principal, Object credentials,
		Collection<? extends GrantedAuthority> authorities) {
		return new AjaxAuthenticationToken(principal, credentials, authorities);
	}

	@Override
	public Object getCredentials() {
		return this.credentials;
	}

	@Override
	public Object getPrincipal() {
		return this.principal;
	}

	@Override
	public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
		Assert.isTrue(!isAuthenticated,
			"Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
		super.setAuthenticated(false);
	}

	@Override
	public void eraseCredentials() {
		super.eraseCredentials();
		this.credentials = null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		if (!super.equals(o))
			return false;
		AjaxAuthenticationToken that = (AjaxAuthenticationToken)o;
		return Objects.equals(principal, that.principal) && Objects.equals(credentials,
			that.credentials);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), principal, credentials);
	}
}
