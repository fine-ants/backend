package codesquad.fineants.domain.exchange_rate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.domain.exchange_rate.domain.dto.request.ExchangeRateCreateRequest;
import codesquad.fineants.domain.exchange_rate.domain.dto.response.ExchangeRateDeleteRequest;
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

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping
	public ApiResponse<Void> createExchangeRate(@RequestBody ExchangeRateCreateRequest request) {
		service.createExchangeRate(request.getCode());
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
	public ApiResponse<Void> deleteExchangeRates(@RequestBody ExchangeRateDeleteRequest codes) {
		service.deleteExchangeRates(codes.getCodes());
		return ApiResponse.success(ExchangeRateSuccessCode.DELETE_EXCHANGE_RATE);
	}
}
