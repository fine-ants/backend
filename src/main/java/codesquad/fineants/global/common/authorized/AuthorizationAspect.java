package codesquad.fineants.global.common.authorized;

import java.util.Optional;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import codesquad.fineants.global.common.resource.ResourceIdAspect;
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

	private final ResourceIdAspect resourceIdAspect;

	@Before(value = "@annotation(Authorized) && args(..)")
	public void validatePortfolioAuthorization(JoinPoint joinPoint) {
		Object target = joinPoint.getTarget();
		AuthorizeService<?> service = (AuthorizeService<?>)applicationContext.getBean(target.getClass());
		Long resourceId = getResourceId((ProceedingJoinPoint)joinPoint);
		Optional<?> resource = service.findResourceById(resourceId);
		Long memberId = getLoggedInMemberId();
		Class<?> resourceClass = resource.filter(r -> service.isAuthorized(r, memberId))
			.map(Object::getClass)
			.orElseThrow(() -> {
				log.error("User with memberId {} have invalid authorization for resourceId {}", memberId, resourceId);
				return new ForBiddenException(MemberErrorCode.FORBIDDEN_MEMBER);
			});
		log.info("User with memberId {} has valid authorization for resourceId {} of type {}.", memberId, resourceId,
			resourceClass.getSimpleName());
	}

	private Long getResourceId(ProceedingJoinPoint joinPoint) {
		try {
			return resourceIdAspect.around(joinPoint);
		} catch (Throwable e) {
			log.error(e.getMessage());
			return 0L;
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
