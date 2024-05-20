package codesquad.fineants.global.init;

import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.kis.service.KisService;
import codesquad.fineants.domain.member.domain.entity.Role;
import codesquad.fineants.domain.member.repository.RoleRepository;
import codesquad.fineants.domain.stock.service.StockService;
import codesquad.fineants.domain.stock_dividend.service.StockDividendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Profile(value = {"local", "dev"})
@Component
@RequiredArgsConstructor
@Slf4j
public class StartupApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

	private final KisService kisService;
	private final StockService stockService;
	private final StockDividendService stockDividendService;
	private final RoleRepository roleRepository;

	@Override
	@Transactional
	public void onApplicationEvent(ContextRefreshedEvent event) {
		log.info("애플리케이션 시작시 종목 현재가 및 종가 초기화 시작");
		kisService.scheduleRefreshingAllStockCurrentPrice();
		kisService.scheduleRefreshingAllLastDayClosingPrice();
		log.info("애플리케이션 시작시 종목 현재가 및 종가 초기화 종료");
		stockService.refreshStocks();
		stockDividendService.initializeStockDividend();

		setupSecurityResources();
	}

	@Transactional
	public void setupSecurityResources() {
		Role adminRole = createRoleIfNotFound("ROLE_ADMIN", "관리자");
		Role managerRole = createRoleIfNotFound("ROLE_MANAGER", "매니저");
		Role userRole = createRoleIfNotFound("ROLE_USER", "회원");
		log.info("create the adminRole : {}", adminRole);
		log.info("create the managerRole : {}", managerRole);
		log.info("create the userRole : {}", userRole);
	}

	@Transactional
	public Role createRoleIfNotFound(String roleName, String roleDesc) {
		Role role = roleRepository.findRoleByRoleName(roleName)
			.orElseGet(() -> Role.create(roleName, roleDesc));
		return roleRepository.save(role);
	}
}
