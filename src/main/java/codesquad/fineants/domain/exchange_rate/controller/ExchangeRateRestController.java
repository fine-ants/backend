package codesquad.fineants.domain.exchange_rate.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.domain.exchange_rate.domain.dto.response.ExchangeRateListResponse;
import codesquad.fineants.domain.exchange_rate.service.ExchangeRateService;
import codesquad.fineants.global.api.ApiResponse;
import codesquad.fineants.global.success.ExchangeRateSuccessCode;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/exchange-rates")
@RequiredArgsConstructor
public class ExchangeRateRestController {

	private final ExchangeRateService service;

	@PostMapping
	public ApiResponse<Void> createExchangeRate(@RequestBody String code) {
		service.createExchangeRate(code);
		return ApiResponse.success(ExchangeRateSuccessCode.CREATE_EXCHANGE_RATE);
	}

	@GetMapping
	public ApiResponse<ExchangeRateListResponse> readExchangeRates() {
		ExchangeRateListResponse response = service.readExchangeRates();
		return ApiResponse.success(ExchangeRateSuccessCode.READ_EXCHANGE_RATE, response);
	}

	@PutMapping
	public ApiResponse<Void> updateExchangeRates() {
		service.updateExchangeRates();
		return ApiResponse.success(ExchangeRateSuccessCode.UPDATE_EXCHANGE_RATE);
	}

	@DeleteMapping
	public ApiResponse<Void> deleteExchangeRates(@RequestBody List<String> codes) {
		service.deleteExchangeRates(codes);
		return ApiResponse.success(ExchangeRateSuccessCode.DELETE_EXCHANGE_RATE);
	}
}
