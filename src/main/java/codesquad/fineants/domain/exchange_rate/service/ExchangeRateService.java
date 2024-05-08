package codesquad.fineants.domain.exchange_rate.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.exchange_rate.client.ExchangeRateWebClient;
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

}
