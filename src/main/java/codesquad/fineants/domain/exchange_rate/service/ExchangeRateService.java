package codesquad.fineants.domain.exchange_rate.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.exchange_rate.client.ExchangeRateWebClient;
import codesquad.fineants.domain.exchange_rate.domain.dto.response.ExchangeRateItem;
import codesquad.fineants.domain.exchange_rate.domain.dto.response.ExchangeRateListResponse;
import codesquad.fineants.domain.exchange_rate.domain.entity.ExchangeRate;
import codesquad.fineants.domain.exchange_rate.repository.ExchangeRateRepository;
import codesquad.fineants.global.errors.errorcode.ExchangeRateErrorCode;
import codesquad.fineants.global.errors.exception.FineAntsException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {

	private final ExchangeRateRepository exchangeRateRepository;
	private final ExchangeRateWebClient webClient;

	@Transactional
	public void createExchangeRate(String code) {
		List<ExchangeRate> rates = exchangeRateRepository.findAll();
		validateDuplicateExchangeRate(rates, code);

		ExchangeRate base = findBaseExchangeRate(code);

		Double rate = webClient.fetchRateBy(code, base.getCode());
		ExchangeRate exchangeRate = ExchangeRate.of(code, rate, base.equalCode(code));
		exchangeRateRepository.save(exchangeRate);
	}

	private void validateDuplicateExchangeRate(List<ExchangeRate> rates, String code) {
		boolean match = rates.stream()
			.map(ExchangeRate::getCode)
			.anyMatch(c -> c.equals(code));
		if (match) {
			throw new FineAntsException(ExchangeRateErrorCode.DUPLICATE_EXCHANGE_RATE);
		}
	}

	private ExchangeRate findBaseExchangeRate(String defaultCode) {
		return exchangeRateRepository.findBase()
			.orElseGet(() -> ExchangeRate.base(defaultCode));
	}

	@Transactional(readOnly = true)
	public ExchangeRateListResponse readExchangeRates() {
		List<ExchangeRateItem> items = exchangeRateRepository.findAll().stream()
			.map(ExchangeRateItem::from)
			.collect(Collectors.toList());
		return ExchangeRateListResponse.from(items);
	}

	@Transactional
	public void updateExchangeRates() {
		List<ExchangeRate> originalRates = exchangeRateRepository.findAll();
		validateExistBase(originalRates);
		Map<String, Double> rateMap = webClient.fetchRates();

		originalRates.stream()
			.filter(rate -> rateMap.containsKey(rate.getCode()))
			.forEach(rate -> rate.changeRate(rateMap.get(rate.getCode())));
	}

	private void validateExistBase(List<ExchangeRate> rates) {
		if (rates.stream()
			.noneMatch(ExchangeRate::isBase)) {
			throw new FineAntsException(ExchangeRateErrorCode.UNAVAILABLE_EXCHANGE_RATE);
		}
	}

	@Transactional
	public void patchBase(String code) {
		// 기존 기준 통화의 base 값을 false로 변경
		exchangeRateRepository.findBase()
			.orElseThrow(() -> new FineAntsException(ExchangeRateErrorCode.NOT_EXIST_BASE))
			.changeBase(false);
		// code의 base 값을 true로 변경
		exchangeRateRepository.findByCode(code)
			.orElseThrow(() -> new FineAntsException(ExchangeRateErrorCode.NOT_EXIST_EXCHANGE_RATE))
			.changeBase(true);
	}

	@Transactional
	public void deleteExchangeRates(List<String> codes) {
		exchangeRateRepository.deleteByCodeIn(codes);
		ExchangeRate nextBaseExchangeRate = exchangeRateRepository.findBase().orElseGet(() ->
			exchangeRateRepository.findAll().stream()
				.min(Comparator.comparing(ExchangeRate::getCreateAt))
				.orElse(null)
		);
		if (nextBaseExchangeRate != null) {
			nextBaseExchangeRate.changeBase(true);
		}
	}
}
