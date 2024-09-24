package co.fineants.price.domain.stockprice.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import co.fineants.api.domain.kis.repository.WebSocketApprovalKeyRedisRepository;
import co.fineants.api.domain.kis.service.KisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RequiredWebSocketApprovalKeyAspect {

	private final WebSocketApprovalKeyRedisRepository repository;
	private final KisService kisService;

	@Before(value = "@annotation(requiredWebSocketApprovalKey) && args(..)")
	public void validatePortfolioAuthorization(RequiredWebSocketApprovalKey requiredWebSocketApprovalKey) {
		repository.fetchApprovalKey().ifPresentOrElse(
			approvalKey -> log.info("Approval key already exists: {}", approvalKey),
			() -> {
				kisService.fetchApprovalKey().ifPresent(repository::saveApprovalKey);
				log.info("Approval key fetched and saved");
			}
		);
	}
}
