package co.fineants.api.domain.portfolio.domain.entity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.portfolio.properties.PortfolioProperties;

class PortfolioDetailTest extends AbstractContainerBaseTest {

	@Autowired
	private PortfolioProperties properties;

	@DisplayName("포트폴리오 상세 정보를 생성한다")
	@Test
	void of() {
		// given
		String name = "portfolio1";
		String securitiesFirm = "토스증권";
		// when
		PortfolioDetail detail = PortfolioDetail.of(name, securitiesFirm, properties);
		// then
		String expected = String.format("(name=%s, securitiesFirm=%s)", name, securitiesFirm);
		Assertions.assertThat(detail.toString()).hasToString(expected);
	}

	@DisplayName("목록에 없는 증권사 이름가 주어지고 포트폴리오 상세 정보 인스턴스 생성시 예외가 발생한다")
	@Test
	void of_givenUnlistedSecuritiesFirm_whenCreatingPortfolioDetail_thenThrowException() {
		// given
		String name = "portfolio1";
		String securitiesFirm = "없는 증권사";
		// when
		Throwable throwable = Assertions.catchThrowable(() -> PortfolioDetail.of(name, securitiesFirm, properties));
		// then
		Assertions.assertThat(throwable)
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Unlisted securitiesFirm: " + securitiesFirm);
	}
}
