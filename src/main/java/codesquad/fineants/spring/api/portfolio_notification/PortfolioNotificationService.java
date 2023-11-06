package codesquad.fineants.spring.api.portfolio_notification;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio.PortfolioRepository;
import codesquad.fineants.spring.api.errors.errorcode.PortfolioErrorCode;
import codesquad.fineants.spring.api.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.kis.KisService;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.portfolio_notification.request.PortfolioNotificationModifyRequest;
import codesquad.fineants.spring.api.portfolio_notification.response.PortfolioNotificationModifyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PortfolioNotificationService {

	private final PortfolioRepository portfolioRepository;
	private final MailService mailService;
	private final KisService kisService;
	private final RedisTemplate<String, String> redisTemplate;
	private final CurrentPriceManager currentPriceManager;

	@Transactional
	public PortfolioNotificationModifyResponse modifyPortfolioTargetGainNotification(
		PortfolioNotificationModifyRequest request,
		Long portfolioId) {
		log.info("포트폴리오 목표수익금액 알림 수정 서비스, request={}, portfolioId={}", request, portfolioId);
		Portfolio portfolio = findPortfolio(portfolioId);
		portfolio.changeTargetGainNotification(request.getIsActive());
		log.info("포트폴리오 목표수익금액 알림 수정 서비스 결과 : portfolio={}", portfolio);
		return PortfolioNotificationModifyResponse.targetGainIsActive(portfolio);
	}

	private Portfolio findPortfolio(Long portfolioId) {
		return portfolioRepository.findById(portfolioId)
			.orElseThrow(() -> new NotFoundResourceException(PortfolioErrorCode.NOT_FOUND_PORTFOLIO));
	}

	@Transactional
	public PortfolioNotificationModifyResponse modifyPortfolioMaximumLossNotification(
		PortfolioNotificationModifyRequest request,
		Long portfolioId) {
		log.info("포트폴리오 최대손실금액 알림 수정 서비스, request={}, portfolioId={}", request, portfolioId);
		Portfolio portfolio = findPortfolio(portfolioId);
		portfolio.changeMaximumLossNotification(request.getIsActive());
		log.info("포트폴리오 최대손실금액 알림 수정 서비스 결과 : portfolio={}", portfolio);
		return PortfolioNotificationModifyResponse.maximumLossIsActive(portfolio);
	}

	// 주기적으로 포트폴리오를 확인하여 목표수익금액에 도달하면 메일을 보낸다
	@Scheduled(fixedRate = 120000L, initialDelay = 30000L)
	public void notifyTargetGain() {
		log.info("notifyTargetGain");
		List<Portfolio> portfolios = portfolioRepository.findAll();
		portfolios.forEach(portfolio -> portfolio.changeCurrentPriceFromHoldings(currentPriceManager));

		List<Portfolio> reachedTargetGainPortfolios = portfolios.stream()
			.filter(portfolio -> !hasMailSentHistoryForTargetGain(portfolio))
			.filter(Portfolio::getTargetGainIsActive)
			.filter(Portfolio::reachedTargetGain)
			.collect(Collectors.toList());
		log.info("포트폴리오 목표수익금액 도달 포트폴리오 : {}개", reachedTargetGainPortfolios.size());

		reachedTargetGainPortfolios.forEach(portfolio -> {
			String to = portfolio.getMember().getEmail();
			String subject = String.format("%s 포트폴리오 목표수익금액 도달 안내 메일입니다", portfolio.getName());
			String content = createMailContent(portfolio);
			mailService.sendEmail(to, subject, content);
			setMailSentHistoryForTargetGain(portfolio);
		});
	}

	private boolean hasMailSentHistoryForTargetGain(Portfolio portfolio) {
		String value = redisTemplate.opsForValue().get(createMailSentHistoryKeyForTargetGain(portfolio));
		return value != null;
	}

	private String createMailSentHistoryKeyForTargetGain(Portfolio portfolio) {
		return String.format("targetGainMail:%d", portfolio.getId());
	}

	private boolean hasMailSentHistoryForMaximumLoss(Portfolio portfolio) {
		String value = redisTemplate.opsForValue().get(createMailSentHistoryKeyForMaximumLoss(portfolio));
		log.info("hasMailSentHistoryForMaximumLoss value={}", value);
		return value != null;
	}

	private String createMailSentHistoryKeyForMaximumLoss(Portfolio portfolio) {
		return String.format("MaximumLossMail:%d", portfolio.getId());
	}

	private String createMailContent(Portfolio portfolio) {
		String title = String.format("%s 포트폴리오 목표 수익 금액 도달 안내", portfolio.getName());
		String targetGain = String.format("목표 수익금액: %d원", portfolio.getTargetGain());
		String maximumLoss = String.format("최대 손실금액: %d원", portfolio.getMaximumLoss());
		String totalGain = String.format("현재 총손익: %d원", portfolio.calculateTotalGain());
		return String.join("\n", title, targetGain, maximumLoss, totalGain);
	}

	private void setMailSentHistoryForTargetGain(Portfolio portfolio) {
		redisTemplate.opsForValue().set(
			createMailSentHistoryKeyForTargetGain(portfolio),
			Boolean.TRUE.toString(),
			Duration.ofDays(1));
	}

	private void setMailSentHistoryMaximumLoss(Portfolio portfolio) {
		redisTemplate.opsForValue().set(
			createMailSentHistoryKeyForMaximumLoss(portfolio),
			Boolean.TRUE.toString(),
			Duration.ofDays(1));
	}

	// 1분마다 포트폴리오를 확인하여 최대손실금액에 도달하면 메일을 보낸다
	@Scheduled(fixedRate = 120000L, initialDelay = 30000L)
	public void notifyMaximumLoss() {
		log.info("notifyMaximumLoss");
		List<Portfolio> portfolios = portfolioRepository.findAll();
		portfolios.forEach(portfolio -> portfolio.changeCurrentPriceFromHoldings(currentPriceManager));

		List<Portfolio> reachedMaximumLossPortfolios = portfolios.stream()
			.filter(portfolio -> !hasMailSentHistoryForMaximumLoss(portfolio))
			.filter(Portfolio::getMaximumIsActive)
			.filter(Portfolio::reachedMaximumLoss)
			.collect(Collectors.toList());
		log.info("포트폴리오 최대손실금액 도달 포트폴리오 : {}개", reachedMaximumLossPortfolios.size());

		reachedMaximumLossPortfolios.forEach(portfolio -> {
			String to = portfolio.getMember().getEmail();
			String subject = String.format("%s 포트폴리오 최대손실금액 도달 안내 메일입니다", portfolio.getName());
			String content = createMailContent(portfolio);
			mailService.sendEmail(to, subject, content);
			setMailSentHistoryMaximumLoss(portfolio);
		});
	}
}
