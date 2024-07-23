package codesquad.fineants.global.security.ajax.provider;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import codesquad.fineants.global.security.ajax.token.AjaxAuthenticationToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class AjaxAuthenticationProvider implements AuthenticationProvider {

	private final UserDetailsService userDetailsService;
	private final PasswordEncoder passwordEncoder;

	@Override
	public Authentication authenticate(Authentication authentication) {
		String email = authentication.getName();
		String password = (String)authentication.getCredentials();
		MemberContext memberContext = (MemberContext)userDetailsService.loadUserByUsername(email);

		log.debug("email : {}", email);
		log.debug("password : {}", password);
		log.debug("memberContext : {}", memberContext);
		if (!passwordEncoder.matches(password, memberContext.getMember().getPassword())) {
			throw new BadCredentialsException("Invalid email or password");
		}
		return AjaxAuthenticationToken.authenticated(memberContext.getMember(), null, memberContext.getAuthorities());
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(AjaxAuthenticationToken.class);
	}
}
