package codesquad.fineants.spring.api.portfolio_stock;

import java.io.IOException;
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
		SseEmitter emitter = new SseEmitter();
		sseExecutor.scheduleAtFixedRate(generateSseEventTask(portfolioId, emitter), 0, 5L, TimeUnit.SECONDS);
		emitter.onCompletion(sseExecutor::shutdown);
		return emitter;
	}

	private Runnable generateSseEventTask(Long portfolioId, SseEmitter emitter) {
		return () -> {
			try {
				SseEmitter.SseEventBuilder event = SseEmitter.event()
					.data(portfolioStockService.readMyPortfolioStocks(portfolioId, lastDayClosingPriceManager))
					.name("sse event - myPortfolioStocks");
				emitter.send(event);
			} catch (IOException | FineAntsException e) {
				log.error(e.getMessage(), e);
				emitter.completeWithError(e);
			}
		};
	}
}
