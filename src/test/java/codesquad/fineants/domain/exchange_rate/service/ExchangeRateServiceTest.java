package codesquad.fineants.domain.exchange_rate.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.domain.common.money.Currency;
import codesquad.fineants.domain.common.money.Percentage;
import codesquad.fineants.domain.exchange_rate.client.ExchangeRateWebClient;
import codesquad.fineants.domain.exchange_rate.domain.dto.response.ExchangeRateListResponse;
import codesquad.fineants.domain.exchange_rate.domain.entity.ExchangeRate;
import codesquad.fineants.domain.exchange_rate.repository.ExchangeRateRepository;
import codesquad.fineants.global.errors.errorcode.ExchangeRateErrorCode;
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

	@WithMockUser(roles = {"ADMIN"})
	@DisplayName("관리자는 환율을 저장한다")
	@CsvSource(value = {
		"KRW, 1.0, true",
		"USD, 0.0007322, false",
		"JPY, 0.0097, false",
		"EUR, 0.0088, false",
		"CNY, 0.0122, false",
		"GBP, 0.0077, false",
		"AUD, 0.0088, false",
		"CAD, 0.0088, false",
		"CHF, 0.0088, false"
	})
	@ParameterizedTest
	void createExchangeRate(String code, double rate, boolean base) {
		// given
		given(webClient.fetchRateBy(code, code)).willReturn(rate);

		// when
		service.createExchangeRate(code);

		// then
		ExchangeRate exchangeRate = repository.findByCode(code).orElseThrow();
		ExchangeRate expected = ExchangeRate.of(code, rate, base);
		assertThat(exchangeRate).isEqualTo(expected);

		DecimalFormat decimalFormat = new DecimalFormat("0.##########");
		assertThat(exchangeRate.parse()).isEqualTo(String.format("%s:%s", code, decimalFormat.format(rate)));
	}

	@WithMockUser(roles = {"ADMIN"})
	@DisplayName("환율 추가 시나리오")
	@TestFactory
	Collection<DynamicTest> createExchangeRateDynamicTest() {

		return List.of(
			DynamicTest.dynamicTest("기준 통화가 없는 상태에서 통화를 추가시 기준 통화가 된다", () -> {
				// given
				String krw = Currency.KRW.name();

				given(webClient.fetchRateBy(krw, krw))
					.willReturn(1.0);
				// when
				service.createExchangeRate(krw);

				// then
				ExchangeRate exchangeRate = repository.findByCode(krw).orElseThrow();
				assertThat(exchangeRate)
					.extracting("code", "rate", "base")
					.usingComparatorForType(Percentage::compareTo, Percentage.class)
					.containsExactly(krw, Percentage.from(1.0), true);
			}),
			DynamicTest.dynamicTest("기준 통화가 있는 상태에서 다른 통화를 추가할 수 있다", () -> {
				// given
				String base = "KRW";
				String usd = Currency.USD.name();
				double rate = 0.0007322;
				given(webClient.fetchRateBy(usd, base))
					.willReturn(rate);
				// when
				service.createExchangeRate(usd);

				// then
				ExchangeRate exchangeRate = repository.findByCode(usd).orElseThrow();
				assertThat(exchangeRate)
					.extracting("code", "rate", "base")
					.usingComparatorForType(Percentage::compareTo, Percentage.class)
					.containsExactly(usd, Percentage.from(rate), false);
			})
		);
	}

	@WithMockUser(roles = {"ADMIN"})
	@DisplayName("관리자는 존재하지 않는 통화를 추가할 수 없다")
	@Test
	void createExchangeRate_whenNotExistCode_thenError() {
		// given
		String usd = "AAA";
		given(webClient.fetchRateBy(usd, usd))
			.willThrow(new FineAntsException(ExchangeRateErrorCode.NOT_EXIST_EXCHANGE_RATE));

		// when
		Throwable throwable = catchThrowable(() -> service.createExchangeRate(usd));

		// then
		assertThat(throwable)
			.isInstanceOf(FineAntsException.class)
			.hasMessage(ExchangeRateErrorCode.NOT_EXIST_EXCHANGE_RATE.getMessage());
	}

	@WithMockUser(roles = {"ADMIN"})
	@DisplayName("관리자는 이미 존재하는 통화를 저장할 수 없다")
	@Test
	void createExchangeRate_whenExistRate_thenThrowError() {
		// given
		String usd = "USD";
		repository.save(ExchangeRate.zero(usd, false));

		// when
		Throwable throwable = catchThrowable(() -> service.createExchangeRate(usd));

		// then
		assertThat(throwable)
			.isInstanceOf(FineAntsException.class)
			.hasMessage("이미 존재하는 통화입니다");
	}

	@WithMockUser(roles = {"ADMIN"})
	@DisplayName("관리자는 환율을 조회한다")
	@Test
	void readExchangeRates() {
		// given
		repository.save(ExchangeRate.of("KRW", 1.0, true));
		repository.save(ExchangeRate.of("USD", 0.1, false));

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
				tuple("KRW", Percentage.from(1.0)),
				tuple("USD", Percentage.from(0.1))
			);
	}

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

	@WithMockUser(roles = {"ADMIN"})
	@DisplayName("기준 통화를 변경한다")
	@Test
	void patchBase() {
		// given
		repository.save(ExchangeRate.base(Currency.KRW.name()));
		repository.save(ExchangeRate.noneBase(Currency.USD.name(), 0.1));

		given(webClient.fetchRates(Currency.USD.name()))
			.willReturn(Map.of("USD", 1.0, "KRW", 1300.0));
		// when
		service.patchBase("USD");

		// then
		List<ExchangeRate> rates = repository.findAll();
		assertThat(rates)
			.hasSize(2)
			.extracting("code", "rate", "base")
			.usingComparatorForType(Percentage::compareTo, Percentage.class)
			.containsExactlyInAnyOrder(
				tuple(Currency.KRW.name(), Percentage.from(1300.0), false),
				tuple(Currency.USD.name(), Percentage.from(1.0), true)
			);
	}

	@WithMockUser(roles = {"ADMIN"})
	@DisplayName("관리자가 환율을 삭제한다")
	@Test
	void deleteExchangeRates() {
		// given
		String krw = "KRW";
		String usd = "USD";
		repository.save(ExchangeRate.base(krw));
		repository.save(ExchangeRate.zero(usd, false));

		// when
		service.deleteExchangeRates(List.of(usd));

		// then
		boolean actual = repository.findByCode(usd).isEmpty();
		assertThat(actual).isTrue();
	}

	@WithMockUser(roles = {"ADMIN"})
	@DisplayName("관리자는 기준 통화를 제거할 수 없다")
	@Test
	void deleteExchangeRates_whenDeletedBaseCode_thenChangeBase() {
		// given
		repository.save(ExchangeRate.base(Currency.KRW.name()));
		repository.save(ExchangeRate.noneBase(Currency.USD.name(), 0.1));
		repository.save(ExchangeRate.noneBase(Currency.CHF.name(), 0.2));
		// when
		Throwable throwable = catchThrowable(() -> service.deleteExchangeRates(List.of(Currency.KRW.name())));
		// then
		assertThat(throwable)
			.isInstanceOf(FineAntsException.class)
			.hasMessage(ExchangeRateErrorCode.UNAVAILABLE_DELETE_BASE_EXCHANGE_RATE.getMessage());
	}

	@WithMockUser(roles = {"ADMIN"})
	@DisplayName("관리자가 기준 통화를 제외한 모든 통화를 제거한다")
	@Test
	void deleteExchangeRates_whenAllDeleted() {
		// given
		repository.save(ExchangeRate.base(Currency.KRW.name()));
		repository.save(ExchangeRate.noneBase(Currency.USD.name(), 0.1));
		repository.save(ExchangeRate.noneBase(Currency.CHF.name(), 0.2));
		// when
		service.deleteExchangeRates(List.of(Currency.USD.name(), Currency.CHF.name()));
		// then
		List<ExchangeRate> rates = repository.findAll();
		assertThat(rates).hasSize(1);
	}
}
