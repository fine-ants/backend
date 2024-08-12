package codesquad.fineants.domain.notification.service.provider;

import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.google.firebase.messaging.Message;

import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.domain.common.count.Count;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.fcm.repository.FcmRepository;
import codesquad.fineants.domain.fcm.service.FirebaseMessagingService;
import codesquad.fineants.domain.holding.domain.entity.PortfolioHolding;
import codesquad.fineants.domain.holding.repository.PortfolioHoldingRepository;
import codesquad.fineants.domain.kis.aop.AccessTokenAspect;
import codesquad.fineants.domain.kis.client.KisCurrentPrice;
import codesquad.fineants.domain.kis.repository.CurrentPriceRedisRepository;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.domain.notification.domain.dto.response.SentNotifyMessage;
import codesquad.fineants.domain.notification.domain.entity.policy.maxloss.MaxLossNotificationPolicy;
import codesquad.fineants.domain.notification.domain.entity.policy.target_gain.TargetGainNotificationPolicy;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.portfolio.repository.PortfolioRepository;
import codesquad.fineants.domain.purchasehistory.domain.entity.PurchaseHistory;
import codesquad.fineants.domain.purchasehistory.repository.PurchaseHistoryRepository;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.domain.stock.repository.StockRepository;

class FirebaseNotificationProviderTest extends AbstractContainerBaseTest {

	@Autowired
	private FirebaseNotificationProvider provider;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private FcmRepository fcmRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private PortfolioHoldingRepository holdingRepository;

	@Autowired
	private PurchaseHistoryRepository purchaseHistoryRepository;

	@Autowired
	private CurrentPriceRedisRepository currentPriceRedisRepository;

	@Autowired
	private TargetGainNotificationPolicy targetGainNotificationPolicy;

	@Autowired
	private MaxLossNotificationPolicy maxLossNotificationPolicy;

	@MockBean
	private FirebaseMessagingService firebaseMessagingService;

	@MockBean
	private AccessTokenAspect accessTokenAspect;

	@DisplayName("포트폴리오의 목표수익률 달성 알림을 FCM 방식으로 푸시한다")
	@Test
	void sendNotification_whenPolicyIsTargetGain_thenNotifyMessage() {
		// given
		Member member = memberRepository.save(createMember());
		Stock samsung = stockRepository.save(createSamsungStock());

		currentPriceRedisRepository.addCurrentPrice(KisCurrentPrice.create(samsung.getTickerSymbol(), 50000L));
		willDoNothing().given(accessTokenAspect).checkAccessTokenExpiration();
		Portfolio portfolio = createPortfolioSample(member, samsung);

		given(firebaseMessagingService.send(ArgumentMatchers.any(Message.class)))
			.willReturn(Optional.of("messageId"));

		// when
		List<SentNotifyMessage> messages = provider.sendNotification(List.of(portfolio), targetGainNotificationPolicy);

		// then
		Assertions.assertThat(messages)
			.hasSize(1)
			.extracting(SentNotifyMessage::getMessageId)
			.containsExactly("messageId");
	}

	@DisplayName("포트폴리오의 최대 손실율 달성 알림을 FCM 방식으로 푸시한다")
	@Test
	void sendNotification_whenPolicyIsMaximumLoss_thenNotifyMessage() {
		// given
		Member member = memberRepository.save(createMember());
		Stock samsung = stockRepository.save(createSamsungStock());

		currentPriceRedisRepository.addCurrentPrice(KisCurrentPrice.create(samsung.getTickerSymbol(), 50000L));
		willDoNothing().given(accessTokenAspect).checkAccessTokenExpiration();
		PurchaseHistory purchaseHistory = createPurchaseHistory(null, LocalDateTime.now(), Count.from(30),
			Money.won(100000), "첫구매", null);
		Portfolio portfolio = createPortfolioSample(member, samsung, purchaseHistory);

		given(firebaseMessagingService.send(ArgumentMatchers.any(Message.class)))
			.willReturn(Optional.of("messageId"));

		// when
		List<SentNotifyMessage> messages = provider.sendNotification(List.of(portfolio), maxLossNotificationPolicy);

		// then
		Assertions.assertThat(messages)
			.hasSize(1)
			.extracting(SentNotifyMessage::getMessageId)
			.containsExactly("messageId");
	}

	private Portfolio createPortfolioSample(Member member, Stock stock) {
		PurchaseHistory purchaseHistory = createPurchaseHistory(null, LocalDateTime.now(), Count.from(30),
			Money.won(100), "첫구매", null);
		return createPortfolioSample(member, stock, purchaseHistory);
	}

	private Portfolio createPortfolioSample(Member member, Stock stock, PurchaseHistory purchaseHistory) {
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		PortfolioHolding holding = holdingRepository.save(createPortfolioHolding(portfolio, stock));
		PurchaseHistory history = purchaseHistoryRepository.save(purchaseHistory);
		holding.addPurchaseHistory(history);
		holding.applyCurrentPrice(currentPriceRedisRepository);
		portfolio.addHolding(holding);
		fcmRepository.save(createFcmToken("token", member));
		return portfolio;
	}

}
