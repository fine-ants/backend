package co.fineants.api.domain.portfolio.properties;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import co.fineants.AbstractContainerBaseTest;

class PortfolioPropertiesTest extends AbstractContainerBaseTest {

	@Value("${portfolio.securities-firm}")
	private String[] securitiesFirm;

	@DisplayName("스프링 컨텍스트가 초기화되면 증권사 데이터들이 주입된다")
	@Test
	void whenContextIsInitialized_thenInjectedSecuritiesFirmContainsExpectedValues() {
		String[] securities = {
			"BNK투자증권", "부국증권", "케이프투자증권", "대신증권", "다올투자증권",
			"DB금융투자", "이베스트투자증권", "유진투자증권", "하나증권", "한화투자증권",
			"하이투자증권", "현대차증권", "IBK투자증권", "카카오페이증권", "KB증권",
			"키움증권", "한국투자증권", "한국포스증권", "교보증권", "메리츠증권",
			"미래에셋증권", "나무증권", "삼성증권", "상상인증권", "신한투자증권",
			"신영증권", "SK증권", "토스증권", "유안타증권", "FineAnts"
		};
		Assertions.assertThat(securitiesFirm).containsExactlyInAnyOrder(securities);
	}
}
