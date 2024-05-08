package codesquad.fineants.domain.exchange_rate.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.domain.common.money.Percentage;
import codesquad.fineants.domain.exchange_rate.client.ExchangeRateWebClient;
import codesquad.fineants.domain.exchange_rate.domain.dto.response.ExchangeRateListResponse;
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
		String usd = "USD";
		double rate = 0.0007322;
		given(webClient.fetchRateBy(usd)).willReturn(rate);
		// when
		service.createExchangeRate(usd);

		// then
		ExchangeRate exchangeRate = repository.findByCode(usd).orElseThrow();
		String expectedExchangeRate = String.valueOf(rate);
		ExchangeRate expected = ExchangeRate.of(usd, Double.parseDouble(expectedExchangeRate));
		assertThat(exchangeRate).isEqualTo(expected);
		assertThat(exchangeRate.parse()).isEqualTo("USD:" + expectedExchangeRate);
	}

	@DisplayName("관리자는 이미 존재하는 환율을 저장하려다 실패한다")
	@Test
	void createExchangeRate_whenExistRate_thenThrowError() {
		// given
		String usd = "USD";
		repository.save(ExchangeRate.zero(usd));

		// when
		Throwable throwable = catchThrowable(() -> service.createExchangeRate(usd));

		// then
		assertThat(throwable)
			.isInstanceOf(FineAntsException.class)
			.hasMessage("이미 존재하는 환율입니다");
	}

	@DisplayName("관리자는 환율을 조회한다")
	@Test
	void readExchangeRates() {
		// given
		repository.save(ExchangeRate.of("KRW", 1.0));
		repository.save(ExchangeRate.of("USD", 0.1));

		// when
		ExchangeRateListResponse response = service.readExchangeRates();
		// then
		assertThat(response)
			.extracting("rates")
			.asList()
			.hasSize(2)
			.extracting("code", "rate")
			.usingComparatorForType(Percentage::compareTo, Percentage.class)
			.containsExactlyInAnyOrder(
				tuple("KRW", Percentage.from(1)),
				tuple("USD", Percentage.from(0.1))
			);
	}

	@DisplayName("환율을 최신화한다")
	@Test
	void updateExchangeRates() {
		// given
		String usd = "USD";
		double rate = 0.1;
		repository.save(ExchangeRate.of(usd, rate));

		double usdRate = 0.2;
		given(webClient.fetchRates()).willReturn(Map.of(usd, usdRate));
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

	@DisplayName("관리자가 환율을 삭제한다")
	@Test
	void deleteExchangeRates() {
		// given
		String usd = "USD";
		repository.save(ExchangeRate.zero(usd));

		// when
		service.deleteExchangeRates(List.of(usd));

		// then
		boolean actual = repository.findByCode(usd).isEmpty();
		assertThat(actual).isTrue();
	}
}
