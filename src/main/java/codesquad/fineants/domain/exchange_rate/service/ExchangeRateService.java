package codesquad.fineants.domain.exchange_rate.service;

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
		if (exchangeRateRepository.findByCode(code).isPresent()) {
			throw new FineAntsException(ExchangeRateErrorCode.DUPLICATE_EXCHANGE_RATE);
		}

		Double rate = webClient.fetchRateBy(code);
		ExchangeRate exchangeRate = ExchangeRate.of(code, rate);
		exchangeRateRepository.save(exchangeRate);
	}

	@Transactional(readOnly = true)
	public ExchangeRateListResponse readExchangeRates() {
		List<ExchangeRate> rates = exchangeRateRepository.findAll();
		List<ExchangeRateItem> items = rates.stream()
			.map(ExchangeRateItem::from)
			.collect(Collectors.toList());
		return ExchangeRateListResponse.from(items);
	}

	@Transactional
	public void updateExchangeRates() {
		Map<String, Double> rateMap = webClient.fetchRates();
		List<ExchangeRate> originalRates = exchangeRateRepository.findAll();
		originalRates.stream()
			.filter(rate -> rateMap.containsKey(rate.getCode()))
			.forEach(rate -> rate.changeRate(rateMap.get(rate.getCode())));
	}

	@Transactional
	public void deleteExchangeRates(List<String> codes) {
		exchangeRateRepository.deleteByCodeIn(codes);
	}
}
