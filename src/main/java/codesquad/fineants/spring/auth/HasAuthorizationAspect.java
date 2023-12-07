package codesquad.fineants.spring.auth;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.oauth.support.AuthPrincipalMember;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio.PortfolioRepository;
import codesquad.fineants.spring.api.errors.errorcode.MemberErrorCode;
import codesquad.fineants.spring.api.errors.errorcode.PortfolioErrorCode;
import codesquad.fineants.spring.api.errors.exception.ForBiddenException;
import codesquad.fineants.spring.api.errors.exception.NotFoundResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Aspect
@Component
public class HasAuthorizationAspect {

	private final PortfolioRepository portfolioRepository;

	@Before(value = "within(@org.springframework.web.bind.annotation.RestController *) && @annotation(hasAuthorization) && args(portfolioId, authMember, ..)", argNames = "hasAuthorization,portfolioId,authMember")
	public void hasAuthorization(final HasAuthorization hasAuthorization,
		@PathVariable final Long portfolioId,
		@AuthPrincipalMember final AuthMember authMember) {
		log.info("call hasAuthorization");
		Portfolio portfolio = portfolioRepository.findById(portfolioId)
			.orElseThrow(() -> new NotFoundResourceException(PortfolioErrorCode.NOT_FOUND_PORTFOLIO));
		if (!portfolio.hasAuthorization(authMember.getMemberId())) {
			throw new ForBiddenException(MemberErrorCode.FORBIDDEN_MEMBER);
		}
	}
}

