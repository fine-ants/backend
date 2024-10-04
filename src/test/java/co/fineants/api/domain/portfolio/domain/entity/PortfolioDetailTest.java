package co.fineants.api.domain.portfolio.domain.entity;

import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
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

	@DisplayName("포트폴리오 이름 형식을 지키지 않으면 예외가 발생한다")
	@MethodSource("invalidPortfolioNames")
	@NullAndEmptySource
	@ParameterizedTest(name = "{index}: {0} 입력시 예외 발생")
	void of_givenInvalidName_whenCreatingPortfolioDetail_thenThrowException(String name) {
		// given
		String securitiesFirm = "토스증권";
		// when
		Throwable throwable = Assertions.catchThrowable(() -> PortfolioDetail.of(name, securitiesFirm, properties));
		// then
		Assertions.assertThat(throwable)
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Invalid Portfolio name: " + name);
	}

	private static Stream<Arguments> invalidPortfolioNames() {
		return Stream.of(
			Arguments.of(
				"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
				"이름이 100글자를 초과"),
			Arguments.of(" portfolio1", "공백으로 시작함"),
			Arguments.of("#!@$!@#!@#4!@4", "특수문자가 포함됨")
		);
	}

}
