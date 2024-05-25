package codesquad.fineants.domain.exchangerate.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.exchangerate.client.ExchangeRateWebClient;
import codesquad.fineants.domain.exchangerate.domain.dto.response.ExchangeRateItem;
import codesquad.fineants.domain.exchangerate.domain.dto.response.ExchangeRateListResponse;
import codesquad.fineants.domain.exchangerate.domain.entity.ExchangeRate;
import codesquad.fineants.domain.exchangerate.repository.ExchangeRateRepository;
import codesquad.fineants.global.errors.errorcode.ExchangeRateErrorCode;
import codesquad.fineants.global.errors.exception.FineAntsException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {

	private final ExchangeRateRepository exchangeRateRepository;
	private final ExchangeRateWebClient webClient;

	@Transactional
	@Secured("ROLE_ADMIN")
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
	@Secured("ROLE_ADMIN")
	public ExchangeRateListResponse readExchangeRates() {
		List<ExchangeRateItem> items = exchangeRateRepository.findAll().stream()
			.map(ExchangeRateItem::from)
			.collect(Collectors.toList());
		return ExchangeRateListResponse.from(items);
	}

	@Scheduled(cron = "0 0 * * * *") // 매일 자정에 한번씩 수행
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

	@Transactional
	@Secured("ROLE_ADMIN")
	public void patchBase(String code) {
		// 기존 기준 통화의 base 값을 false로 변경
		findBaseExchangeRate().changeBase(false);
		// code의 base 값을 true로 변경
		findExchangeRateBy(code).changeBase(true);
		// TODO: rate 변경 반영이 안됨
		updateExchangeRates();
	}

	private ExchangeRate findExchangeRateBy(String code) {
		return exchangeRateRepository.findByCode(code)
			.orElseThrow(() -> new FineAntsException(ExchangeRateErrorCode.NOT_EXIST_EXCHANGE_RATE));
	}

	@Transactional
	@Secured("ROLE_ADMIN")
	public void deleteExchangeRates(List<String> codes) {
		validateContainsBaseExchangeRateForDelete(codes);
		exchangeRateRepository.deleteByCodeIn(codes);
	}

	/**
	 * 입력으로 받은 통화 코드들중 기준 통화가 있는지 확인합니다.
	 * @param codes 삭제하고자 하는 통화 코드 리스트
	 */
	private void validateContainsBaseExchangeRateForDelete(List<String> codes) {
		ExchangeRate base = findBaseExchangeRate();
		boolean match = codes.stream()
			.anyMatch(base::equalCode);
		if (match) {
			throw new FineAntsException(ExchangeRateErrorCode.UNAVAILABLE_DELETE_BASE_EXCHANGE_RATE);
		}
	}

	private ExchangeRate findBaseExchangeRate() {
		return exchangeRateRepository.findBase()
			.orElseThrow(() -> new FineAntsException(ExchangeRateErrorCode.NOT_EXIST_BASE));
	}
}
