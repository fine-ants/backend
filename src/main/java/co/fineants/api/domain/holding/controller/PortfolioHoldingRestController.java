package co.fineants.api.domain.holding.controller;

import java.time.LocalDate;

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

import co.fineants.api.domain.holding.domain.dto.request.PortfolioHoldingCreateRequest;
import co.fineants.api.domain.holding.domain.dto.request.PortfolioStocksDeleteRequest;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioChartResponse;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingsResponse;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioStockCreateResponse;
import co.fineants.api.domain.holding.service.PortfolioHoldingService;
import co.fineants.api.domain.holding.service.PortfolioObservableService;
import co.fineants.api.global.api.ApiResponse;
import co.fineants.api.global.security.oauth.dto.MemberAuthentication;
import co.fineants.api.global.security.oauth.resolver.MemberAuthenticationPrincipal;
import co.fineants.api.global.success.PortfolioStockSuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/api/portfolio/{portfolioId}")
@RequiredArgsConstructor
@RestController
public class PortfolioHoldingRestController {

	private final PortfolioHoldingService portfolioHoldingService;
	private final PortfolioObservableService portfolioObservableService;

	// 포트폴리오 종목 생성
	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping("/holdings")
	public ApiResponse<PortfolioStockCreateResponse> createPortfolioHolding(@PathVariable Long portfolioId,
		@Valid @RequestBody PortfolioHoldingCreateRequest request) {
		PortfolioStockCreateResponse response = portfolioHoldingService.createPortfolioHolding(portfolioId, request);
		return ApiResponse.success(PortfolioStockSuccessCode.CREATED_ADD_PORTFOLIO_STOCK, response);
	}

	// 포트폴리오 종목 조회
	@GetMapping("/holdings")
	public ApiResponse<PortfolioHoldingsResponse> readPortfolioHoldings(@PathVariable Long portfolioId) {
		return ApiResponse.success(PortfolioStockSuccessCode.OK_READ_PORTFOLIO_STOCKS,
			portfolioHoldingService.readPortfolioHoldings(portfolioId));
	}

	// 포트폴리오 종목 실시간 조회
	@GetMapping(value = "/holdings/realtime", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter observePortfolioHoldings(@PathVariable Long portfolioId) {
		// TODO: 포트폴리오에 담겨있는 종목들을 RealTimeStockRepository에 저장한다
		return portfolioObservableService.observePortfolioHoldings(portfolioId);
	}

	// 포트폴리오 차트 조회
	@GetMapping("/charts")
	public ApiResponse<PortfolioChartResponse> readPortfolioCharts(@PathVariable Long portfolioId) {
		PortfolioChartResponse response = portfolioHoldingService.readPortfolioCharts(portfolioId, LocalDate.now());
		return ApiResponse.success(PortfolioStockSuccessCode.OK_READ_PORTFOLIO_CHARTS, response);
	}

	// 포트폴리오 종목 단일 삭제
	@DeleteMapping("/holdings/{portfolioHoldingId}")
	public ApiResponse<Void> deletePortfolioHolding(@PathVariable Long portfolioId,
		@PathVariable Long portfolioHoldingId) {
		portfolioHoldingService.deletePortfolioStock(portfolioHoldingId);
		return ApiResponse.success(PortfolioStockSuccessCode.OK_DELETE_PORTFOLIO_STOCK);
	}

	// 포트폴리오 종목 다수 삭제
	@DeleteMapping("/holdings")
	public ApiResponse<Void> deletePortfolioHoldings(@PathVariable Long portfolioId,
		@MemberAuthenticationPrincipal MemberAuthentication authentication,
		@Valid @RequestBody PortfolioStocksDeleteRequest request) {
		portfolioHoldingService.deletePortfolioHoldings(portfolioId, authentication.getId(),
			request.getPortfolioHoldingIds());
		return ApiResponse.success(PortfolioStockSuccessCode.OK_DELETE_PORTFOLIO_STOCKS);
	}
}
