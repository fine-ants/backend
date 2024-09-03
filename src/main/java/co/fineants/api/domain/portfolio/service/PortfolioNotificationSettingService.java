package co.fineants.api.domain.portfolio.service;

import java.util.List;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.portfolio.domain.dto.response.PortfolioNotificationSettingSearchItem;
import co.fineants.api.domain.portfolio.domain.dto.response.PortfolioNotificationSettingSearchResponse;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class PortfolioNotificationSettingService {

	private final PortfolioRepository repository;

	@Secured("ROLE_USER")
	public PortfolioNotificationSettingSearchResponse searchPortfolioNotificationSetting(Long memberId) {
		List<PortfolioNotificationSettingSearchItem> portfolios = repository.findAllByMemberId(memberId).stream()
			.map(PortfolioNotificationSettingSearchItem::from)
			.toList();
		return PortfolioNotificationSettingSearchResponse.from(portfolios);
	}
}
