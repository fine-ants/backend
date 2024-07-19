package codesquad.fineants.global.common.authorized;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.portfolio.service.PortFolioService;
import codesquad.fineants.global.errors.errorcode.PortfolioErrorCode;
import codesquad.fineants.global.errors.exception.ForBiddenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthorizationAspect {

	private final PortFolioService portfolioService;

	@Before(value = "@annotation(Authorized) && args(request, portfolioId, memberId,..)", argNames = "joinPoint,request,portfolioId,memberId")
	public void validatePortfolioAuthorization(JoinPoint joinPoint, Object request, Long portfolioId, Long memberId) {
		Portfolio portfolio = portfolioService.findPortfolio(portfolioId);
		if (!portfolio.hasAuthorization(memberId)) {
			log.error("memberId {} not have authorized on portfolioId {}", memberId, portfolioId);
			throw new ForBiddenException(PortfolioErrorCode.NOT_HAVE_AUTHORIZATION);
		}
	}
}
