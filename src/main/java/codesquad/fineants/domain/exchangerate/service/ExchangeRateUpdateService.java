package codesquad.fineants.domain.exchangerate.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.exchangerate.client.ExchangeRateWebClient;
import codesquad.fineants.domain.exchangerate.domain.entity.ExchangeRate;
import codesquad.fineants.domain.exchangerate.repository.ExchangeRateRepository;
import codesquad.fineants.global.errors.errorcode.ExchangeRateErrorCode;
import codesquad.fineants.global.errors.exception.FineAntsException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExchangeRateUpdateService {

	private final ExchangeRateRepository exchangeRateRepository;
	private final ExchangeRateWebClient webClient;

	@Transactional
	public void updateExchangeRates() {
		List<ExchangeRate> originalRates = exchangeRateRepository.findAll();
		validateExistBase(originalRates);
		ExchangeRate baseRate = findBaseExchangeRate(originalRates);
		Map<String, Double> rateMap = webClient.fetchRates(baseRate.getCode());

		originalRates.stream()
			.filter(rate -> rateMap.containsKey(rate.getCode()))
			.forEach(rate -> rate.changeRate(rateMap.get(rate.getCode())));
	}

	private void validateExistBase(List<ExchangeRate> rates) {
		if (rates.stream()
			.noneMatch(ExchangeRate::isBase)) {
			throw new FineAntsException(ExchangeRateErrorCode.UNAVAILABLE_UPDATE_EXCHANGE_RATE);
		}
	}

	private ExchangeRate findBaseExchangeRate(List<ExchangeRate> rates) {
		return rates.stream()
			.filter(ExchangeRate::isBase)
			.findFirst()
			.orElseThrow(() -> new FineAntsException(ExchangeRateErrorCode.NOT_EXIST_BASE));
	}
}
