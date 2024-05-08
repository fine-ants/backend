package codesquad.fineants.domain.exchange_rate.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.domain.exchange_rate.client.ExchangeRateWebClient;
import codesquad.fineants.domain.exchange_rate.domain.entity.ExchangeRate;
import codesquad.fineants.domain.exchange_rate.repository.ExchangeRateRepository;
import codesquad.fineants.global.errors.exception.FineAntsException;

class ExchangeRateServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private ExchangeRateService service;

	@Autowired
	private ExchangeRateRepository repository;

	@MockBean
	private ExchangeRateWebClient webClient;

	@AfterEach
	void tearDown() {
		repository.deleteAllInBatch();
	}

	@DisplayName("관리자는 환율을 저장한다")
	@Test
	void createExchangeRate() {
		// given
		String code = "USD";
		double rate = 0.0007322;
		given(webClient.fetchRateBy(code)).willReturn(rate);
		// when
		service.createExchangeRate(code);

		// then
		ExchangeRate exchangeRate = repository.findByCode(code).orElseThrow();
		String expectedExchangeRate = String.valueOf(rate);
		ExchangeRate expected = ExchangeRate.of(code, Double.parseDouble(expectedExchangeRate));
		assertThat(exchangeRate).isEqualTo(expected);
		assertThat(exchangeRate.parse()).isEqualTo("USD:" + expectedExchangeRate);
	}

	@DisplayName("관리자는 이미 존재하는 환율을 저장하려다 실패한다")
	@Test
	void createExchangeRate_whenExistRate_thenThrowError() {
		// given
		String code = "USD";
		repository.save(ExchangeRate.zero(code));

		// when
		Throwable throwable = catchThrowable(() -> service.createExchangeRate(code));

		// then
		assertThat(throwable)
			.isInstanceOf(FineAntsException.class)
			.hasMessage("이미 존재하는 환율입니다");
	}
}
