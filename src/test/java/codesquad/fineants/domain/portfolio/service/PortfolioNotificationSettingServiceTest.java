package codesquad.fineants.domain.portfolio.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.domain.portfolio.domain.dto.response.PortfolioNotificationSettingSearchResponse;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.portfolio.repository.PortfolioRepository;

class PortfolioNotificationSettingServiceTest extends AbstractContainerBaseTest {
	@Autowired
	private PortfolioNotificationSettingService service;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@AfterEach
	void tearDown() {
		portfolioRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
	}

	@DisplayName("사용자는 포트폴리오의 활성 알림 목록을 조회합니다")
	@Test
	void searchPortfolioNotificationSetting() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio1 = portfolioRepository.save(createPortfolio(member, "내 꿈은 워렌버핏"));
		Portfolio portfolio2 = portfolioRepository.save(createPortfolio(member, "내 꿈은 찰리몽거"));

		// when
		PortfolioNotificationSettingSearchResponse response = service.searchPortfolioNotificationSetting(
			member.getId());

		// then
		assertAll(
			() -> assertThat(response)
				.extracting("portfolios")
				.asList()
				.hasSize(2)
				.extracting("portfolioId", "name", "targetGainNotify", "maxLossNotify")
				.containsExactlyInAnyOrder(
					Tuple.tuple(portfolio1.getId(), "내 꿈은 워렌버핏", false, false),
					Tuple.tuple(portfolio2.getId(), "내 꿈은 찰리몽거", false, false))
		);
	}

	private Portfolio createPortfolio(Member member, String name) {
		return Portfolio.builder()
			.name(name)
			.securitiesFirm("토스")
			.budget(Money.won(1000000L))
			.targetGain(Money.won(1500000L))
			.maximumLoss(Money.won(900000L))
			.member(member)
			.targetGainIsActive(false)
			.maximumLossIsActive(false)
			.build();
	}

}
