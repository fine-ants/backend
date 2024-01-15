package codesquad.fineants.spring.auth;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.oauth.support.AuthPrincipalMember;
import codesquad.fineants.spring.api.errors.errorcode.MemberErrorCode;
import codesquad.fineants.spring.api.errors.exception.ForBiddenException;
import codesquad.fineants.spring.api.portfolio.PortFolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Aspect
@Component
public class HasPortfolioAuthorizationAspect {

	private final PortFolioService portFolioService;

	@Before(value = "within(@org.springframework.web.bind.annotation.RestController *) && @annotation(hasPortfolioAuthorization) && args(portfolioId, authMember, ..)", argNames = "hasPortfolioAuthorization,portfolioId,authMember")
	public void hasAuthorization(final HasPortfolioAuthorization hasPortfolioAuthorization,
		@PathVariable final Long portfolioId,
		@AuthPrincipalMember final AuthMember authMember) {
		if (!portFolioService.hasAuthorizationBy(portfolioId, authMember.getMemberId())) {
			throw new ForBiddenException(MemberErrorCode.FORBIDDEN_MEMBER);
		}
	}
}

