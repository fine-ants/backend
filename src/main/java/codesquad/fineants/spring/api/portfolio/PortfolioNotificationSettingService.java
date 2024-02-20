package codesquad.fineants.spring.api.portfolio;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.portfolio.PortfolioRepository;
import codesquad.fineants.spring.api.portfolio.response.PortfolioNotificationSettingSearchItem;
import codesquad.fineants.spring.api.portfolio.response.PortfolioNotificationSettingSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class PortfolioNotificationSettingService {

	private final PortfolioRepository repository;

	public PortfolioNotificationSettingSearchResponse searchPortfolioNotificationSetting(Long memberId) {
		List<PortfolioNotificationSettingSearchItem> portfolios = repository.findAllByMemberId(memberId).stream()
			.map(PortfolioNotificationSettingSearchItem::from)
			.collect(Collectors.toList());
		return PortfolioNotificationSettingSearchResponse.from(portfolios);
	}
}
