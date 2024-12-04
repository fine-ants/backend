package co.fineants.api.domain.notification.service.provider;

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

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.notification.Notifiable;
import co.fineants.api.domain.common.notification.PortfolioMaximumLossNotifiable;
import co.fineants.api.domain.common.notification.PortfolioTargetGainNotifiable;
import co.fineants.api.domain.fcm.repository.FcmRepository;
import co.fineants.api.domain.fcm.service.FirebaseMessagingService;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.holding.repository.PortfolioHoldingRepository;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.notification.domain.dto.response.SentNotifyMessage;
import co.fineants.api.domain.notification.domain.entity.policy.MaxLossNotificationPolicy;
import co.fineants.api.domain.notification.domain.entity.policy.TargetGainNotificationPolicy;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import co.fineants.api.domain.purchasehistory.repository.PurchaseHistoryRepository;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockRepository;

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

	@DisplayName("포트폴리오의 목표수익률 달성 알림을 FCM 방식으로 푸시한다")
	@Test
	void sendNotification_whenPolicyIsTargetGain_thenNotifyMessage() {
		// given
		Member member = memberRepository.save(createMember());
		Stock samsung = stockRepository.save(createSamsungStock());

		currentPriceRedisRepository.savePrice(KisCurrentPrice.create(samsung.getTickerSymbol(), 50000L));
		Portfolio portfolio = createPortfolioSample(member, samsung);
		Notifiable notifiable = PortfolioTargetGainNotifiable.from(portfolio, true);

		given(firebaseMessagingService.send(ArgumentMatchers.any(Message.class)))
			.willReturn(Optional.of("messageId"));

		// when
		List<SentNotifyMessage> messages = provider.sendNotification(List.of(notifiable), targetGainNotificationPolicy);

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

		currentPriceRedisRepository.savePrice(KisCurrentPrice.create(samsung.getTickerSymbol(), 50000L));
		PurchaseHistory purchaseHistory = createPurchaseHistory(null, LocalDateTime.now(), Count.from(30),
			Money.won(100000), "첫구매", null);
		Portfolio portfolio = createPortfolioSample(member, samsung, purchaseHistory);
		PortfolioMaximumLossNotifiable notifiable = PortfolioMaximumLossNotifiable.from(portfolio, true);

		given(firebaseMessagingService.send(ArgumentMatchers.any(Message.class)))
			.willReturn(Optional.of("messageId"));

		// when
		List<SentNotifyMessage> messages = provider.sendNotification(List.of(notifiable), maxLossNotificationPolicy);

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
		portfolio.addHolding(holding);
		fcmRepository.save(createFcmToken("token", member));
		return portfolio;
	}

}
