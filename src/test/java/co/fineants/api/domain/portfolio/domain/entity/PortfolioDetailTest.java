package co.fineants.api.domain.portfolio.domain.entity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PortfolioDetailTest {

	@DisplayName("포트폴리오 상세 정보를 생성한다")
	@Test
	void of() {
		// given
		String name = "portfolio1";
		String securitiesFirm = "토스증권";
		// when
		PortfolioDetail detail = PortfolioDetail.of(name, securitiesFirm);
		// then
		String expected = String.format("(name=%s, securitiesFirm=%s)", name, securitiesFirm);
		Assertions.assertThat(detail.toString()).isEqualTo(expected);
	}
}
