package codesquad.fineants.spring.api.portfolio_stock;

import java.io.IOException;
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
import codesquad.fineants.spring.api.errors.exception.FineAntsException;
import codesquad.fineants.spring.api.kis.manager.LastDayClosingPriceManager;
import codesquad.fineants.spring.api.portfolio_stock.request.PortfolioStockCreateRequest;
import codesquad.fineants.spring.api.response.ApiResponse;
import codesquad.fineants.spring.api.success.code.PortfolioStockSuccessCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/api/portfolio/{portfolioId}/holdings")
@RequiredArgsConstructor
@RestController
public class PortfolioStockRestController {

	private static final ScheduledExecutorService sseExecutor = Executors.newScheduledThreadPool(100);

	private final PortfolioStockService portfolioStockService;
	private final LastDayClosingPriceManager lastDayClosingPriceManager;
	private final StockMarketChecker stockMarketChecker;

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping
	public ApiResponse<Void> addPortfolioStock(@PathVariable Long portfolioId,
		@Valid @RequestBody PortfolioStockCreateRequest request,
		@AuthPrincipalMember AuthMember authMember) {
		portfolioStockService.addPortfolioStock(portfolioId, request, authMember);
		return ApiResponse.success(PortfolioStockSuccessCode.CREATED_ADD_PORTFOLIO_STOCK);
	}

	@DeleteMapping("/{portfolioHoldingId}")
	public ApiResponse<Void> deletePortfolioStock(@PathVariable Long portfolioId,
		@PathVariable Long portfolioHoldingId,
		@AuthPrincipalMember AuthMember authMember) {
		portfolioStockService.deletePortfolioStock(portfolioHoldingId, portfolioId, authMember);
		return ApiResponse.success(PortfolioStockSuccessCode.OK_DELETE_PORTFOLIO_STOCK);
	}

	@GetMapping
	public SseEmitter readMyPortfolioStocks(@PathVariable Long portfolioId) {
		SseEmitter emitter = new SseEmitter(Duration.ofHours(10L).toMillis());
		emitter.onTimeout(emitter::complete);

		// 장시간 동안에는 스케줄러를 이용하여 지속적 응답
		LocalDateTime now = LocalDateTime.now();
		log.info("now : {}", now);
		if (stockMarketChecker.isMarketOpen(now)) {
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
					.data(portfolioStockService.readMyPortfolioStocks(portfolioId, lastDayClosingPriceManager))
					.name("sse event - myPortfolioStocks"));
				log.info("send message");
				if (isComplete) {
					emitter.send(SseEmitter.event()
						.data("sse complete")
						.name("complete"));
					emitter.complete();
				}
			} catch (IOException | FineAntsException e) {
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
}
