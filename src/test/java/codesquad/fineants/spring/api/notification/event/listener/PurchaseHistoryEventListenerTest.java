package codesquad.fineants.spring.api.notification.event.listener;

import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.notification_preference.NotificationPreference;
import codesquad.fineants.domain.notification_preference.NotificationPreferenceRepository;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio.PortfolioRepository;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import codesquad.fineants.domain.portfolio_holding.PortfolioHoldingRepository;
import codesquad.fineants.domain.purchase_history.PurchaseHistory;
import codesquad.fineants.domain.purchase_history.PurchaseHistoryRepository;
import codesquad.fineants.domain.stock.Market;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.stock.StockRepository;
import codesquad.fineants.domain.stock_target_price.StockTargetPrice;
import codesquad.fineants.domain.target_price_notification.TargetPriceNotification;
import codesquad.fineants.spring.AbstractContainerBaseTest;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.purchase_history.event.PurchaseHistoryEventListener;
import codesquad.fineants.spring.api.purchase_history.event.PurchaseHistoryEventSendableParameter;
import codesquad.fineants.spring.api.purchase_history.event.PushNotificationEvent;

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
	private NotificationPreferenceRepository notificationPreferenceRepository;

	@MockBean
	private FirebaseMessaging firebaseMessaging;

	@MockBean
	private CurrentPriceManager currentPriceManager;

	@AfterEach
	void tearDown() {
		purchaseHistoryRepository.deleteAllInBatch();
		portfolioHoldingRepository.deleteAllInBatch();
		portfolioRepository.deleteAllInBatch();
		notificationPreferenceRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
		stockRepository.deleteAllInBatch();
	}

	@DisplayName("매입 이력 이벤트 발생시 목표 수익률에 달성하여 푸시 알림을 한다")
	@Test
	void listenPurchaseHistory() throws FirebaseMessagingException {
		// given
		given(firebaseMessaging.send(any(Message.class)))
			.willReturn("projects/fineants-404407/messages/4754d355-5d5d-4f14-a642-75fecdb91fa5");
		given(currentPriceManager.getCurrentPrice(anyString()))
			.willReturn(Optional.of(50000L));

		Member member = memberRepository.save(createMember());
		notificationPreferenceRepository.save(NotificationPreference.builder()
			.browserNotify(true)
			.targetGainNotify(true)
			.maxLossNotify(true)
			.targetPriceNotify(true)
			.member(member)
			.build()
		);
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createStock());
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
		purchaseHistoryRepository.save(createPurchaseHistory(portfolioHolding, 100L, 10000.0));

		PushNotificationEvent event = new PushNotificationEvent(
			PurchaseHistoryEventSendableParameter.create(portfolio.getId(), member.getId()));
		// when
		purchaseHistoryEventListener.notifyTargetGainBy(event);

		// then

	}

	private Member createMember() {
		return Member.builder()
			.nickname("일개미1234")
			.email("dragonbead95@naver.com")
			.password("kim1234@")
			.provider("local")
			.build();
	}

	private Portfolio createPortfolio(Member member) {
		return Portfolio.builder()
			.name("내꿈은 워렌버핏")
			.securitiesFirm("토스")
			.budget(Money.from(1000000L))
			.targetGain(1500000L)
			.maximumLoss(900000L)
			.member(member)
			.targetGainIsActive(false)
			.maximumLossIsActive(false)
			.build();
	}

	private Stock createStock() {
		return Stock.builder()
			.companyName("삼성전자보통주")
			.tickerSymbol("005930")
			.companyNameEng("SamsungElectronics")
			.stockCode("KR7005930003")
			.sector("전기전자")
			.market(Market.KOSPI)
			.build();
	}

	private StockTargetPrice createStockTargetPrice(Member member, Stock stock) {
		return StockTargetPrice.builder()
			.member(member)
			.stock(stock)
			.isActive(true)
			.build();
	}

	private TargetPriceNotification createTargetPriceNotification(StockTargetPrice stockTargetPrice, Long targetPrice) {
		return TargetPriceNotification.builder()
			.targetPrice(targetPrice)
			.stockTargetPrice(stockTargetPrice)
			.build();
	}

	private PortfolioHolding createPortfolioHolding(Portfolio portfolio, Stock stock) {
		return PortfolioHolding.builder()
			.portfolio(portfolio)
			.stock(stock)
			.build();
	}

	private PurchaseHistory createPurchaseHistory(PortfolioHolding portfolioHolding, Long numShares,
		Double purchasePricePerShare) {
		return PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.of(2023, 9, 26, 9, 30, 0))
			.numShares(numShares)
			.purchasePricePerShare(purchasePricePerShare)
			.memo("첫구매")
			.portfolioHolding(portfolioHolding)
			.build();
	}
}
