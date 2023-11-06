package codesquad.fineants.domain.oauth.support;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import lombok.Getter;

@Getter
@Component
@RequestScope
public class AuthenticationContext {

	private AuthMember authMember;

	public void setAuthMember(AuthMember authMember) {
		this.authMember = authMember;
	}

	@Override
	public String toString() {
		return String.format("%s, %s(%s)", "인증 컨텍스트", this.getClass().getSimpleName(), authMember);
	}
}
