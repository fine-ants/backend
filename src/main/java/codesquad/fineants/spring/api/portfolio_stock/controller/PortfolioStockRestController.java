package codesquad.fineants.spring.api.portfolio_stock.controller;

import java.time.Duration;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.oauth.support.AuthPrincipalMember;
import codesquad.fineants.spring.api.portfolio_stock.manager.SseEmitterKey;
import codesquad.fineants.spring.api.portfolio_stock.manager.SseEmitterManager;
import codesquad.fineants.spring.api.portfolio_stock.request.PortfolioStockCreateRequest;
import codesquad.fineants.spring.api.portfolio_stock.request.PortfolioStocksDeleteRequest;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioChartResponse;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioHoldingsResponse;
import codesquad.fineants.spring.api.portfolio_stock.service.PortfolioStockService;
import codesquad.fineants.spring.api.response.ApiResponse;
import codesquad.fineants.spring.api.success.code.PortfolioStockSuccessCode;
import codesquad.fineants.spring.auth.HasPortfolioAuthorization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/api/portfolio/{portfolioId}")
@RequiredArgsConstructor
@RestController
public class PortfolioStockRestController {

	private final PortfolioStockService portfolioStockService;
	private final SseEmitterManager manager;

	@HasPortfolioAuthorization
	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping("/holdings")
	public ApiResponse<Void> addPortfolioStock(@PathVariable Long portfolioId,
		@AuthPrincipalMember AuthMember authMember,
		@Valid @RequestBody PortfolioStockCreateRequest request) {
		portfolioStockService.addPortfolioStock(portfolioId, request, authMember);
		return ApiResponse.success(PortfolioStockSuccessCode.CREATED_ADD_PORTFOLIO_STOCK);
	}

	@HasPortfolioAuthorization
	@DeleteMapping("/holdings/{portfolioHoldingId}")
	public ApiResponse<Void> deletePortfolioStock(@PathVariable Long portfolioId,
		@AuthPrincipalMember AuthMember authMember,
		@PathVariable Long portfolioHoldingId) {
		portfolioStockService.deletePortfolioStock(portfolioHoldingId, portfolioId, authMember);
		return ApiResponse.success(PortfolioStockSuccessCode.OK_DELETE_PORTFOLIO_STOCK);
	}

	@HasPortfolioAuthorization
	@DeleteMapping("/holdings")
	public ApiResponse<Void> deletePortfolioStocks(@PathVariable Long portfolioId,
		@AuthPrincipalMember AuthMember authMember,
		@Valid @RequestBody PortfolioStocksDeleteRequest request) {
		portfolioStockService.deletePortfolioStocks(portfolioId, authMember, request);
		return ApiResponse.success(PortfolioStockSuccessCode.OK_DELETE_PORTFOLIO_STOCKS);
	}

	@HasPortfolioAuthorization
	@GetMapping("/holdings")
	public ApiResponse<PortfolioHoldingsResponse> readMyPortfolioStocks(@PathVariable Long portfolioId,
		@AuthPrincipalMember AuthMember authMember) {
		return ApiResponse.success(PortfolioStockSuccessCode.OK_READ_PORTFOLIO_STOCKS,
			portfolioStockService.readMyPortfolioStocks(portfolioId));
	}

	@HasPortfolioAuthorization
	@GetMapping(value = "/holdings/realtime", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter readMyPortfolioStocksInRealTime(@PathVariable Long portfolioId,
		@AuthPrincipalMember AuthMember authMember) {
		SseEmitter emitter = new SseEmitter(Duration.ofSeconds(30).toMillis());
		SseEmitterKey key = SseEmitterKey.create(portfolioId);
		emitter.onTimeout(() -> {
			log.info("emitter{} timeout으로 인한 제거", portfolioId);
			emitter.complete();
		});
		emitter.onCompletion(() -> {
			log.info("emitter{} completion으로 인한 제거", portfolioId);
			manager.remove(key);
		});
		emitter.onError(throwable -> {
			log.error(throwable.getMessage(), throwable);
			emitter.complete();
		});
		manager.add(key, emitter);
		return emitter;
	}

	@HasPortfolioAuthorization
	@GetMapping("/charts")
	public ApiResponse<PortfolioChartResponse> readMyPortfolioCharts(@PathVariable Long portfolioId,
		@AuthPrincipalMember AuthMember authMember) {
		PortfolioChartResponse response = portfolioStockService.readMyPortfolioCharts(portfolioId);
		return ApiResponse.success(PortfolioStockSuccessCode.OK_READ_PORTFOLIO_CHARTS, response);
	}
}
