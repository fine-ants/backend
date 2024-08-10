package codesquad.fineants.global.common.authorized;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import codesquad.fineants.global.common.authorized.service.AuthorizedService;
import codesquad.fineants.global.common.resource.ResourceIdParser;
import codesquad.fineants.global.errors.errorcode.MemberErrorCode;
import codesquad.fineants.global.errors.exception.FineAntsException;
import codesquad.fineants.global.errors.exception.ForBiddenException;
import codesquad.fineants.global.security.oauth.dto.MemberAuthentication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthorizationAspect {

	private final ApplicationContext applicationContext;

	private final ResourceIdParser resourceIdParser;

	@Before(value = "@annotation(authorized) && args(..)")
	public void validatePortfolioAuthorization(JoinPoint joinPoint, Authorized authorized) {
		AuthorizedService<?> service = (AuthorizedService<?>)applicationContext.getBean(authorized.serviceClass());

		List<Long> resourceIds = getResourceId((ProceedingJoinPoint)joinPoint);
		List<?> resources = service.findResourceAllBy(resourceIds);
		Long memberId = getLoggedInMemberId();

		resources.stream()
			.filter(resource -> !service.isAuthorized(resource, memberId))
			.forEach(resource -> {
				log.error("User with memberId {} have invalid authorization for resourceIds {}", memberId, resourceIds);
				throw new ForBiddenException(MemberErrorCode.FORBIDDEN_MEMBER);
			});
		log.info("User with memberId {} has valid authorization for resourceIds {}.", memberId, resourceIds);
	}

	private List<Long> getResourceId(ProceedingJoinPoint joinPoint) {
		try {
			return resourceIdParser.getResourceList(joinPoint);
		} catch (Throwable e) {
			log.error(e.getMessage());
			return Collections.emptyList();
		}
	}

	private Long getLoggedInMemberId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Object principal = authentication.getPrincipal();
		MemberAuthentication memberAuthentication = (MemberAuthentication)principal;
		return Optional.ofNullable(memberAuthentication).map(MemberAuthentication::getId)
			.orElseThrow(() -> new FineAntsException(MemberErrorCode.UNAUTHORIZED_MEMBER));
	}
}
