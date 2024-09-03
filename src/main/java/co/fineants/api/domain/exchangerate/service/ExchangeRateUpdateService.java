package co.fineants.api.domain.exchangerate.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.exchangerate.client.ExchangeRateWebClient;
import co.fineants.api.domain.exchangerate.domain.entity.ExchangeRate;
import co.fineants.api.domain.exchangerate.repository.ExchangeRateRepository;
import co.fineants.api.global.errors.errorcode.ExchangeRateErrorCode;
import co.fineants.api.global.errors.exception.FineAntsException;
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
