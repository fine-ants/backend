package co.fineants.api.domain.member.domain.dto.request;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import co.fineants.api.global.security.ajax.token.AjaxAuthenticationToken;

public class LoginRequest {
	@JsonProperty
	private final String email;
	@JsonProperty
	private final String password;

	@JsonCreator
	public LoginRequest(@JsonProperty("email") String email, @JsonProperty("password") String password) {
		this.email = email;
		this.password = password;
	}

	public static LoginRequest create(String email, String password) {
		return new LoginRequest(email, password);
	}

	public AbstractAuthenticationToken toUnauthenticatedAjaxToken() {
		return AjaxAuthenticationToken.unauthenticated(email, password);
	}

	public boolean hasEmail() {
		return StringUtils.hasText(email);
	}

	public boolean hasPassword() {
		return StringUtils.hasText(password);
	}

	@Override
	public String toString() {
		return String.format("LoginRequest(email=%s, password=%s)", email, password);
	}
}
