package codesquad.fineants.domain.notification.event.listener;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;

import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.domain.common.count.Count;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.fcm.repository.FcmRepository;
import codesquad.fineants.domain.holding.domain.entity.PortfolioHolding;
import codesquad.fineants.domain.holding.repository.PortfolioHoldingRepository;
import codesquad.fineants.domain.kis.client.KisCurrentPrice;
import codesquad.fineants.domain.kis.repository.CurrentPriceRedisRepository;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.domain.notification.domain.dto.response.PortfolioNotifyMessagesResponse;
import codesquad.fineants.domain.notification.repository.NotificationRepository;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.portfolio.repository.PortfolioRepository;
import codesquad.fineants.domain.purchasehistory.event.aop.PurchaseHistoryEventSendableParameter;
import codesquad.fineants.domain.purchasehistory.event.domain.PushNotificationEvent;
import codesquad.fineants.domain.purchasehistory.event.listener.PurchaseHistoryEventListener;
import codesquad.fineants.domain.purchasehistory.repository.PurchaseHistoryRepository;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.domain.stock.repository.StockRepository;

class PurchaseHistoryEventListenerTest extends AbstractContainerBaseTest {

	@Autowired
	private PurchaseHistoryEventListener purchaseHistoryEventListener;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private PortfolioHoldingRepository portfolioHoldingRepository;

	@Autowired
	private PurchaseHistoryRepository purchaseHistoryRepository;

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private CurrentPriceRedisRepository currentPriceRedisRepository;

	@Autowired
	private FcmRepository fcmRepository;

	@MockBean
	private FirebaseMessaging firebaseMessaging;

	@DisplayName("매입 이력 이벤트 발생시 목표 수익률에 달성하여 푸시 알림을 한다")
	@Test
	void listenPurchaseHistory() throws FirebaseMessagingException {
		// given
		given(firebaseMessaging.send(any(Message.class)))
			.willReturn("projects/fineants-404407/messages/4754d355-5d5d-4f14-a642-75fecdb91fa5");

		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
		purchaseHistoryRepository.save(
			createPurchaseHistory(null, LocalDateTime.now(), Count.from(100), Money.won(10000), "memo",
				portfolioHolding));
		fcmRepository.save(createFcmToken("token", member));
		currentPriceRedisRepository.savePrice(KisCurrentPrice.create(stock.getTickerSymbol(), 50000L));

		PushNotificationEvent event = new PushNotificationEvent(
			PurchaseHistoryEventSendableParameter.create(portfolio.getId(), member.getId()));
		// when
		CompletableFuture<PortfolioNotifyMessagesResponse> future = purchaseHistoryEventListener.notifyTargetGainBy(
			event);
		// then
		PortfolioNotifyMessagesResponse response = future.join();
		assertThat(response.getNotifications()).hasSize(1);
		assertThat(notificationRepository.findAllByMemberId(member.getId())).hasSize(1);
	}

	@DisplayName("사용자는 매입 이벤트 발생 시 최대 손실율에 달성하여 푸시 알림을 받는다")
	@Test
	void addPurchaseHistory_whenAchieveMaxLoss_thenSaveNotification() throws FirebaseMessagingException {
		// given
		given(firebaseMessaging.send(any(Message.class)))
			.willReturn("projects/fineants-404407/messages/4754d355-5d5d-4f14-a642-75fecdb91fa5");

		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
		purchaseHistoryRepository.save(
			createPurchaseHistory(null, LocalDateTime.now(), Count.from(1), Money.won(1000000), "memo",
				portfolioHolding));
		fcmRepository.save(createFcmToken("token", member));
		currentPriceRedisRepository.savePrice(KisCurrentPrice.create(stock.getTickerSymbol(), 50000L));

		PushNotificationEvent event = new PushNotificationEvent(
			PurchaseHistoryEventSendableParameter.create(portfolio.getId(), member.getId()));
		// when
		CompletableFuture<PortfolioNotifyMessagesResponse> future = purchaseHistoryEventListener.notifyMaxLoss(event);
		// then
		PortfolioNotifyMessagesResponse response = future.join();
		assertThat(response.getNotifications()).hasSize(1);
		assertThat(notificationRepository.findAllByMemberId(member.getId())).hasSize(1);
	}
}
