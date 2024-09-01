package co.fineants.api.domain.exchangerate.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import co.fineants.api.AbstractContainerBaseTest;
import co.fineants.api.domain.common.money.Percentage;
import co.fineants.api.domain.exchangerate.client.ExchangeRateWebClient;
import co.fineants.api.domain.exchangerate.domain.entity.ExchangeRate;
import co.fineants.api.domain.exchangerate.repository.ExchangeRateRepository;
import co.fineants.api.global.errors.errorcode.ExchangeRateErrorCode;
import co.fineants.api.global.errors.exception.FineAntsException;

class ExchangeRateUpdateServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private ExchangeRateRepository repository;

	@MockBean
	private ExchangeRateWebClient webClient;

	@Autowired
	private ExchangeRateUpdateService service;

	@DisplayName("환율을 최신화한다")
	@Test
	void updateExchangeRates() {
		// given
		String krw = "KRW";
		String usd = "USD";
		double rate = 0.1;
		repository.save(ExchangeRate.base(krw));
		repository.save(ExchangeRate.of(usd, rate, false));

		double usdRate = 0.2;
		given(webClient.fetchRates(krw)).willReturn(Map.of(usd, usdRate));
		// when
		service.updateExchangeRates();
		// then
		ExchangeRate exchangeRate = repository.findByCode(usd).orElseThrow();
		Percentage expected = Percentage.from(usdRate);
		assertThat(exchangeRate)
			.extracting("rate")
			.usingComparatorForType(Percentage::compareTo, Percentage.class)
			.isEqualTo(expected);
	}

	@DisplayName("관리자는 기준 통화가 없는 상태에서 환율 업데이트를 할 수 없다")
	@Test
	void updateExchangeRates_whenNoBase_thenError() {
		// given
		String baseCode = "KRW";
		given(webClient.fetchRates(baseCode)).willReturn(Map.of(baseCode, 1.0));
		// when
		Throwable throwable = catchThrowable(() -> service.updateExchangeRates());
		// then
		assertThat(throwable)
			.isInstanceOf(FineAntsException.class)
			.hasMessage(ExchangeRateErrorCode.UNAVAILABLE_UPDATE_EXCHANGE_RATE.getMessage());
	}
}
