package codesquad.fineants.spring.api.portfolio_stock.controller;

import java.time.LocalDate;

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
import codesquad.fineants.spring.api.common.response.ApiResponse;
import codesquad.fineants.spring.api.common.success.PortfolioStockSuccessCode;
import codesquad.fineants.spring.api.portfolio_stock.request.PortfolioHoldingCreateRequest;
import codesquad.fineants.spring.api.portfolio_stock.request.PortfolioStocksDeleteRequest;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioChartResponse;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioHoldingsResponse;
import codesquad.fineants.spring.api.portfolio_stock.service.PortfolioHoldingService;
import codesquad.fineants.spring.api.portfolio_stock.service.PortfolioObservableService;
import codesquad.fineants.spring.auth.HasPortfolioAuthorization;
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
	@HasPortfolioAuthorization
	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping("/holdings")
	public ApiResponse<Void> createPortfolioHolding(@PathVariable Long portfolioId,
		@AuthPrincipalMember AuthMember authMember,
		@Valid @RequestBody PortfolioHoldingCreateRequest request) {
		portfolioHoldingService.createPortfolioHolding(portfolioId, request, authMember);
		return ApiResponse.success(PortfolioStockSuccessCode.CREATED_ADD_PORTFOLIO_STOCK);
	}

	// 포트폴리오 종목 조회
	@HasPortfolioAuthorization
	@GetMapping("/holdings")
	public ApiResponse<PortfolioHoldingsResponse> readPortfolioHoldings(@PathVariable Long portfolioId,
		@AuthPrincipalMember AuthMember authMember) {
		return ApiResponse.success(PortfolioStockSuccessCode.OK_READ_PORTFOLIO_STOCKS,
			portfolioHoldingService.readPortfolioHoldings(portfolioId));
	}

	// 포트폴리오 종목 실시간 조회
	@HasPortfolioAuthorization
	@GetMapping(value = "/holdings/realtime", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter observePortfolioHoldings(
		@PathVariable Long portfolioId,
		@AuthPrincipalMember AuthMember authMember
	) {
		return portfolioObservableService.observePortfolioHoldings(portfolioId);
	}

	// 포트폴리오 차트 조회
	@HasPortfolioAuthorization
	@GetMapping("/charts")
	public ApiResponse<PortfolioChartResponse> readPortfolioCharts(@PathVariable Long portfolioId,
		@AuthPrincipalMember AuthMember authMember) {
		PortfolioChartResponse response = portfolioHoldingService.readPortfolioCharts(portfolioId, LocalDate.now());
		return ApiResponse.success(PortfolioStockSuccessCode.OK_READ_PORTFOLIO_CHARTS, response);
	}

	// 포트폴리오 종목 단일 삭제
	@HasPortfolioAuthorization
	@DeleteMapping("/holdings/{portfolioHoldingId}")
	public ApiResponse<Void> deletePortfolioHolding(@PathVariable Long portfolioId,
		@AuthPrincipalMember AuthMember authMember,
		@PathVariable Long portfolioHoldingId) {
		portfolioHoldingService.deletePortfolioStock(portfolioHoldingId, authMember);
		return ApiResponse.success(PortfolioStockSuccessCode.OK_DELETE_PORTFOLIO_STOCK);
	}

	// 포트폴리오 종목 다수 삭제
	@HasPortfolioAuthorization
	@DeleteMapping("/holdings")
	public ApiResponse<Void> deletePortfolioHoldings(@PathVariable Long portfolioId,
		@AuthPrincipalMember AuthMember authMember,
		@Valid @RequestBody PortfolioStocksDeleteRequest request) {
		portfolioHoldingService.deletePortfolioHoldings(portfolioId, authMember, request);
		return ApiResponse.success(PortfolioStockSuccessCode.OK_DELETE_PORTFOLIO_STOCKS);
	}
}
