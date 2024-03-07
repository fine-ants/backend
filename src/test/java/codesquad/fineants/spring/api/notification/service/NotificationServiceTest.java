package codesquad.fineants.spring.api.notification.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;

import codesquad.fineants.domain.fcm_token.FcmRepository;
import codesquad.fineants.domain.fcm_token.FcmToken;
import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.notification.NotificationRepository;
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
import codesquad.fineants.domain.stock_target_price.StockTargetPriceRepository;
import codesquad.fineants.domain.target_price_notification.TargetPriceNotification;
import codesquad.fineants.domain.target_price_notification.TargetPriceNotificationRepository;
import codesquad.fineants.spring.AbstractContainerBaseTest;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.notification.response.NotifyMessageItem;
import codesquad.fineants.spring.api.notification.response.NotifyPortfolioMessagesResponse;

class NotificationServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private NotificationService service;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private FcmRepository fcmRepository;

	@Autowired
	private StockTargetPriceRepository stockTargetPriceRepository;

	@Autowired
	private TargetPriceNotificationRepository targetPriceNotificationRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private PortfolioHoldingRepository portfolioHoldingRepository;

	@Autowired
	private PurchaseHistoryRepository purchaseHistoryRepository;

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private NotificationPreferenceRepository notificationPreferenceRepository;

	@MockBean
	private FirebaseMessaging firebaseMessaging;

	@MockBean
	private CurrentPriceManager manager;

	@AfterEach
	void tearDown() {
		notificationRepository.deleteAllInBatch();
		fcmRepository.deleteAllInBatch();
		targetPriceNotificationRepository.deleteAllInBatch();
		stockTargetPriceRepository.deleteAllInBatch();
		purchaseHistoryRepository.deleteAllInBatch();
		portfolioHoldingRepository.deleteAllInBatch();
		portfolioRepository.deleteAllInBatch();
		notificationPreferenceRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
		stockRepository.deleteAllInBatch();
	}

	@DisplayName("포트폴리오 목표 수익률 달성 알림 메시지들을 푸시합니다")
	@Test
	void notifyPortfolioTargetGainMessages() throws FirebaseMessagingException {
		// given
		Member member = memberRepository.save(createMember());
		notificationPreferenceRepository.save(NotificationPreference.builder()
			.browserNotify(true)
			.targetGainNotify(true)
			.maxLossNotify(true)
			.targetPriceNotify(true)
			.member(member)
			.build());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createStock());
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
		purchaseHistoryRepository.save(createPurchaseHistory(portfolioHolding, 100L, 10000.0));

		fcmRepository.save(FcmToken.builder()
			.latestActivationTime(LocalDateTime.now())
			.token(
				"fahY76rRwq8HGy0m1lwckx:APA91bEovbLJyqdSRq8MWDbsIN8sbk90JiNHbIBs6rDoiOKeC-aa5P1QydiRa6okGrIZELrxx_cYieWUN44iX-AD6jma-cYRUR7e3bTMXwkqZFLRZh5s7-bcksGniB7Y2DkoONHtSjos")
			.member(member)
			.build());

		given(firebaseMessaging.send(any(Message.class)))
			.willReturn("projects/fineants-404407/messages/4754d355-5d5d-4f14-a642-75fecdb91fa5");
		given(manager.getCurrentPrice(anyString()))
			.willReturn(50000L);

		// when
		NotifyPortfolioMessagesResponse response = service.notifyPortfolioTargetGainMessages(
			portfolio.getId(), member.getId());

		// then
		assertAll(
			() -> assertThat(response.getNotifications()).hasSize(1),
			() -> assertThat(notificationRepository.findAllByMemberId(member.getId())).hasSize(1)
		);
	}

	@DisplayName("토큰이 유효하지 않아서 목표 수익률 알림을 보낼수 없다")
	@Test
	void notifyPortfolioTargetGainMessages_whenInvalidFcmToken_thenDeleteFcmToken() throws FirebaseMessagingException {
		// given
		Member member = memberRepository.save(createMember());
		notificationPreferenceRepository.save(NotificationPreference.builder()
			.browserNotify(true)
			.targetGainNotify(true)
			.maxLossNotify(true)
			.targetPriceNotify(true)
			.member(member)
			.build());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createStock());
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
		purchaseHistoryRepository.save(createPurchaseHistory(portfolioHolding, 100L, 10000.0));

		FcmToken fcmToken = fcmRepository.save(FcmToken.builder()
			.latestActivationTime(LocalDateTime.now())
			.token(
				"fahY76rRwq8HGy0m1lwckx:APA91bEovbLJyqdSRq8MWDbsIN8sbk90JiNHbIBs6rDoiOKeC-aa5P1QydiRa6okGrIZELrxx_cYieWUN44iX-AD6jma-cYRUR7e3bTMXwkqZFLRZh5s7-bcksGniB7Y2DkoONHtSjos")
			.member(member)
			.build());

		given(firebaseMessaging.send(any(Message.class)))
			.willThrow(FirebaseMessagingException.class);
		given(manager.getCurrentPrice(anyString()))
			.willReturn(50000L);

		// when
		NotifyPortfolioMessagesResponse response = service.notifyPortfolioTargetGainMessages(portfolio.getId(),
			member.getId());

		// then
		assertAll(
			() -> assertThat(response.getNotifications()).isEmpty(),
			() -> assertThat(fcmRepository.findById(fcmToken.getId())).isEmpty()
		);
	}

	@DisplayName("브라우저 알림 설정이 비활성화되어 목표 수익률 알림을 보낼수 없다")
	@CsvSource(value = {"false,true", "true,false", "false, false"})
	@ParameterizedTest
	void notifyPortfolioTargetGainMessages_whenBrowserNotifyIsInActive_thenResponseEmptyList(boolean browserNotify,
		boolean targetGainNotify) {
		// given
		Member member = memberRepository.save(createMember());
		notificationPreferenceRepository.save(NotificationPreference.builder()
			.browserNotify(browserNotify)
			.targetGainNotify(targetGainNotify)
			.maxLossNotify(true)
			.targetPriceNotify(true)
			.member(member)
			.build());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createStock());
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
		purchaseHistoryRepository.save(createPurchaseHistory(portfolioHolding, 100L, 10000.0));

		fcmRepository.save(FcmToken.builder()
			.latestActivationTime(LocalDateTime.now())
			.token(
				"fahY76rRwq8HGy0m1lwckx:APA91bEovbLJyqdSRq8MWDbsIN8sbk90JiNHbIBs6rDoiOKeC-aa5P1QydiRa6okGrIZELrxx_cYieWUN44iX-AD6jma-cYRUR7e3bTMXwkqZFLRZh5s7-bcksGniB7Y2DkoONHtSjos")
			.member(member)
			.build());
		// when
		NotifyPortfolioMessagesResponse response = service.notifyPortfolioTargetGainMessages(
			portfolio.getId(), member.getId());

		// then
		assertAll(
			() -> assertThat(response.getNotifications()).isEmpty()
		);
	}

	@DisplayName("포트폴리오의 최대 손실율에 도달하여 사용자에게 알림을 푸시합니다")
	@Test
	void notifyPortfolioMaxLossMessages() throws FirebaseMessagingException {
		// given
		Member member = memberRepository.save(createMember());
		notificationPreferenceRepository.save(NotificationPreference.builder()
			.browserNotify(true)
			.targetGainNotify(true)
			.maxLossNotify(true)
			.targetPriceNotify(true)
			.member(member)
			.build());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createStock());
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
		purchaseHistoryRepository.save(createPurchaseHistory(portfolioHolding, 50L, 60000.0));

		fcmRepository.save(FcmToken.builder()
			.latestActivationTime(LocalDateTime.now())
			.token(
				"dcEZXm1dxCV31t-Mt3yikc:APA91bHJv4tQHRaL9P985sCvGOw3b0qr0maz0BXb7_eKOKBZPFM51HytTJMbiUv9L37utFpPNPE5Uxr_VbdUIvmBahOftmVuaNiuOJ35Jk50yKlC-Cj2sQHMwruUZ_O6BjSuGMbrRCi3")
			.member(member)
			.build());

		given(firebaseMessaging.send(any(Message.class)))
			.willReturn("projects/fineants-404407/messages/4754d355-5d5d-4f14-a642-75fecdb91fa5");
		given(manager.getCurrentPrice(anyString()))
			.willReturn(100L);

		// when
		NotifyPortfolioMessagesResponse response = service.notifyPortfolioMaxLossMessages(portfolio.getId(),
			member.getId());

		// then
		assertAll(
			() -> assertThat(response.getNotifications()).hasSize(1),
			() -> assertThat(notificationRepository.findAllByMemberId(member.getId())).hasSize(1)
		);
	}

	@DisplayName("알림 설정이 비활성화 되어 있어서 포트폴리오의 최대 손실율에 도달하여 사용자에게 알림을 푸시할 수 없습니다")
	@CsvSource(value = {"false,true", "true,false", "false, false"})
	@ParameterizedTest
	void notifyPortfolioMaxLossMessages_whenNotifySettingIsInActive_thenResponseEmptyList(boolean browserNotify,
		boolean maxLossNotify) throws FirebaseMessagingException {
		// given
		Member member = memberRepository.save(createMember());
		notificationPreferenceRepository.save(NotificationPreference.builder()
			.browserNotify(browserNotify)
			.targetGainNotify(true)
			.maxLossNotify(maxLossNotify)
			.targetPriceNotify(true)
			.member(member)
			.build());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createStock());
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
		purchaseHistoryRepository.save(createPurchaseHistory(portfolioHolding, 50L, 60000.0));

		fcmRepository.save(FcmToken.builder()
			.latestActivationTime(LocalDateTime.now())
			.token(
				"dcEZXm1dxCV31t-Mt3yikc:APA91bHJv4tQHRaL9P985sCvGOw3b0qr0maz0BXb7_eKOKBZPFM51HytTJMbiUv9L37utFpPNPE5Uxr_VbdUIvmBahOftmVuaNiuOJ35Jk50yKlC-Cj2sQHMwruUZ_O6BjSuGMbrRCi3")
			.member(member)
			.build());

		// when
		NotifyPortfolioMessagesResponse response = service.notifyPortfolioMaxLossMessages(portfolio.getId(),
			member.getId());

		// then
		assertAll(
			() -> assertThat(response.getNotifications()).isEmpty()
		);
	}

	@DisplayName("토큰이 유효하지 않아서 최대 손실율 달성 알림을 보낼수 없다")
	@Test
	void notifyPortfolioMaxLossMessages_whenInvalidFcmToken_thenDeleteFcmToken() throws FirebaseMessagingException {
		// given
		Member member = memberRepository.save(createMember());
		notificationPreferenceRepository.save(NotificationPreference.builder()
			.browserNotify(true)
			.targetGainNotify(true)
			.maxLossNotify(true)
			.targetPriceNotify(true)
			.member(member)
			.build());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createStock());
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
		purchaseHistoryRepository.save(createPurchaseHistory(portfolioHolding, 50L, 60000.0));

		FcmToken fcmToken = fcmRepository.save(FcmToken.builder()
			.latestActivationTime(LocalDateTime.now())
			.token(
				"dcEZXm1dxCV31t-Mt3yikc:APA91bHJv4tQHRaL9P985sCvGOw3b0qr0maz0BXb7_eKOKBZPFM51HytTJMbiUv9L37utFpPNPE5Uxr_VbdUIvmBahOftmVuaNiuOJ35Jk50yKlC-Cj2sQHMwruUZ_O6BjSuGMbrRCi3")
			.member(member)
			.build());

		given(firebaseMessaging.send(any(Message.class)))
			.willThrow(FirebaseMessagingException.class);
		given(manager.getCurrentPrice(anyString()))
			.willReturn(50000L);

		// when
		NotifyPortfolioMessagesResponse response = service.notifyPortfolioMaxLossMessages(portfolio.getId(),
			member.getId());

		// then
		assertAll(
			() -> assertThat(response.getNotifications()).isEmpty(),
			() -> assertThat(fcmRepository.findById(fcmToken.getId())).isEmpty()
		);
	}

	@DisplayName("종목 지정가가 도달하여 사용자에게 알림을 푸시합니다")
	@Test
	void notifyStockAchievedTargetPrice() throws FirebaseMessagingException {
		// given
		Member member = memberRepository.save(createMember());
		notificationPreferenceRepository.save(NotificationPreference.builder()
			.browserNotify(true)
			.targetGainNotify(true)
			.maxLossNotify(true)
			.targetPriceNotify(true)
			.member(member)
			.build());
		portfolioRepository.save(createPortfolio(member));
		FcmToken fcmToken = fcmRepository.save(FcmToken.builder()
			.latestActivationTime(LocalDateTime.now())
			.token(
				"fahY76rRwq8HGy0m1lwckx:APA91bEovbLJyqdSRq8MWDbsIN8sbk90JiNHbIBs6rDoiOKeC-aa5P1QydiRa6okGrIZELrxx_cYieWUN44iX-AD6jma-cYRUR7e3bTMXwkqZFLRZh5s7-bcksGniB7Y2DkoONHtSjos")
			.member(member)
			.build());
		Stock stock = stockRepository.save(createStock());
		StockTargetPrice stockTargetPrice = stockTargetPriceRepository.save(createStockTargetPrice(member, stock));
		TargetPriceNotification targetPriceNotification = targetPriceNotificationRepository.save(
			createTargetPriceNotification(stockTargetPrice, 60000L));

		given(firebaseMessaging.send(any(Message.class)))
			.willReturn("projects/fineants-404407/messages/4754d355-5d5d-4f14-a642-75fecdb91fa5");

		// when
		Optional<NotifyMessageItem> optionalNotifyMessageItem = service.notifyStockAchievedTargetPrice(
			fcmToken.getToken(),
			targetPriceNotification);

		// then
		assertAll(
			() -> assertThat(optionalNotifyMessageItem.isPresent()).isTrue(),
			() -> assertThat(fcmRepository.findByTokenAndMemberId(fcmToken.getToken(), member.getId()).isPresent())
				.isTrue()
		);
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
			.budget(1000000L)
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

	private List<TargetPriceNotification> createTargetPriceNotification(StockTargetPrice stockTargetPrice,
		List<Long> targetPrices) {
		return targetPrices.stream()
			.map(targetPrice -> TargetPriceNotification.builder()
				.targetPrice(targetPrice)
				.stockTargetPrice(stockTargetPrice)
				.build())
			.collect(Collectors.toList());
	}

}
