package codesquad.fineants.spring.api.portfolio_stock;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
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
import codesquad.fineants.spring.api.portfolio_stock.request.PortfolioStockCreateRequest;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioChartResponse;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioHoldingsResponse;
import codesquad.fineants.spring.api.response.ApiResponse;
import codesquad.fineants.spring.api.success.code.PortfolioStockSuccessCode;
import codesquad.fineants.spring.auth.HasAuthorization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/api/portfolio/{portfolioId}")
@RequiredArgsConstructor
@RestController
public class PortfolioStockRestController {

	private static final ScheduledExecutorService sseExecutor = Executors.newScheduledThreadPool(100);

	private final PortfolioStockService portfolioStockService;
	private final StockMarketChecker stockMarketChecker;

	@HasAuthorization
	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping("/holdings")
	public ApiResponse<Void> addPortfolioStock(@PathVariable Long portfolioId,
		@AuthPrincipalMember AuthMember authMember,
		@Valid @RequestBody PortfolioStockCreateRequest request) {
		portfolioStockService.addPortfolioStock(portfolioId, request, authMember);
		return ApiResponse.success(PortfolioStockSuccessCode.CREATED_ADD_PORTFOLIO_STOCK);
	}

	@HasAuthorization
	@DeleteMapping("/holdings/{portfolioHoldingId}")
	public ApiResponse<Void> deletePortfolioStock(@PathVariable Long portfolioId,
		@AuthPrincipalMember AuthMember authMember,
		@PathVariable Long portfolioHoldingId) {
		portfolioStockService.deletePortfolioStock(portfolioHoldingId, portfolioId, authMember);
		return ApiResponse.success(PortfolioStockSuccessCode.OK_DELETE_PORTFOLIO_STOCK);
	}

	@HasAuthorization
	@GetMapping("/holdings")
	public ApiResponse<PortfolioHoldingsResponse> readMyPortfolioStocks(@PathVariable Long portfolioId,
		@AuthPrincipalMember AuthMember authMember) {
		return ApiResponse.success(PortfolioStockSuccessCode.OK_READ_PORTFOLIO_STOCKS,
			portfolioStockService.readMyPortfolioStocks(portfolioId));
	}

	@HasAuthorization
	@GetMapping("/holdings/realtime")
	public SseEmitter readMyPortfolioStocksInRealTime(@PathVariable Long portfolioId,
		@AuthPrincipalMember AuthMember authMember) {
		SseEmitter emitter = new SseEmitter(Duration.ofHours(10).toMillis());
		emitter.onTimeout(emitter::complete);

		// 장시간 동안에는 스케줄러를 이용하여 지속적 응답
		if (stockMarketChecker.isMarketOpen(LocalDateTime.now())) {
			scheduleSseEventTask(portfolioId, emitter, false);
		} else {
			scheduleSseEventTask(portfolioId, emitter, true);
		}
		return emitter;
	}

	private void scheduleSseEventTask(Long portfolioId, SseEmitter emitter, boolean isComplete) {
		Runnable task = () -> {
			try {
				emitter.send(SseEmitter.event()
					.data(
						portfolioStockService.readMyPortfolioStocksInRealTime(portfolioId))
					.name("sse event - myPortfolioStocks"));
				log.info("send message");
				if (isComplete) {
					Thread.sleep(2000L); // sse event - myPortfolioStocks 메시지와 전송 간격
					emitter.send(SseEmitter.event()
						.data("sse complete")
						.name("complete"));
					emitter.complete();
					log.info("emitter complete");
				}
			} catch (Exception e) {
				log.error(e.getMessage());
				emitter.completeWithError(e);
			}
		};
		if (isComplete) {
			sseExecutor.schedule(task, 0, TimeUnit.SECONDS);
		} else {
			sseExecutor.scheduleAtFixedRate(task, 0, 5L, TimeUnit.SECONDS);
		}
	}

	@HasAuthorization
	@GetMapping("/charts")
	public ApiResponse<PortfolioChartResponse> readMyPortfolioCharts(@PathVariable Long portfolioId,
		@AuthPrincipalMember AuthMember authMember) {
		PortfolioChartResponse response = portfolioStockService.readMyPortfolioCharts(portfolioId);
		return ApiResponse.success(PortfolioStockSuccessCode.OK_READ_PORTFOLIO_CHARTS, response);
	}
}
