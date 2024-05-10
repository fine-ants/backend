package codesquad.fineants.domain.notification.event.listener;

import codesquad.fineants.AbstractContainerBaseTest;

// TODO: MemberServiceTest 데드락 문제로 인해서 해당 테스트 클래스를 주석 처리함
class PurchaseHistoryEventListenerTest extends AbstractContainerBaseTest {
	//
	// @Autowired
	// private PurchaseHistoryEventListener purchaseHistoryEventListener;
	//
	// @Autowired
	// private MemberRepository memberRepository;
	//
	// @Autowired
	// private PortfolioRepository portfolioRepository;
	//
	// @Autowired
	// private StockRepository stockRepository;
	//
	// @Autowired
	// private PortfolioHoldingRepository portfolioHoldingRepository;
	//
	// @Autowired
	// private PurchaseHistoryRepository purchaseHistoryRepository;
	//
	// @Autowired
	// private NotificationPreferenceRepository notificationPreferenceRepository;
	//
	// @MockBean
	// private FirebaseMessaging firebaseMessaging;
	//
	// @MockBean
	// private CurrentPriceRepository currentPriceRepository;
	//
	// @AfterEach
	// void tearDown() {
	// 	purchaseHistoryRepository.deleteAllInBatch();
	// 	portfolioHoldingRepository.deleteAllInBatch();
	// 	portfolioRepository.deleteAllInBatch();
	// 	notificationPreferenceRepository.deleteAllInBatch();
	// 	memberRepository.deleteAllInBatch();
	// 	stockRepository.deleteAllInBatch();
	// }
	//
	// @DisplayName("매입 이력 이벤트 발생시 목표 수익률에 달성하여 푸시 알림을 한다")
	// @Test
	// void listenPurchaseHistory() throws
	// 	FirebaseMessagingException,
	// 	ExecutionException,
	// 	InterruptedException,
	// 	TimeoutException {
	// 	// given
	// 	given(firebaseMessaging.send(any(Message.class)))
	// 		.willReturn("projects/fineants-404407/messages/4754d355-5d5d-4f14-a642-75fecdb91fa5");
	// 	given(currentPriceRepository.getCurrentPrice(anyString()))
	// 		.willReturn(Optional.of(Money.won(50000L)));
	//
	// 	Member member = memberRepository.save(createMember());
	// 	notificationPreferenceRepository.save(NotificationPreference.builder()
	// 		.browserNotify(true)
	// 		.targetGainNotify(true)
	// 		.maxLossNotify(true)
	// 		.targetPriceNotify(true)
	// 		.member(member)
	// 		.build()
	// 	);
	// 	Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
	// 	Stock stock = stockRepository.save(createStock());
	// 	PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
	// 	purchaseHistoryRepository.save(createPurchaseHistory(portfolioHolding, 100L, 10000.0));
	//
	// 	PushNotificationEvent event = new PushNotificationEvent(
	// 		PurchaseHistoryEventSendableParameter.create(portfolio.getId(), member.getId()));
	// 	// when
	// 	CompletableFuture<Void> future = CompletableFuture.runAsync(
	// 		() -> purchaseHistoryEventListener.notifyTargetGainBy(event));
	// 	// then
	// 	future.get(5L, TimeUnit.SECONDS);
	// }
	//
	// private Member createMember() {
	// 	return Member.builder()
	// 		.nickname("일개미1234")
	// 		.email("dragonbead95@naver.com")
	// 		.password("kim1234@")
	// 		.provider("local")
	// 		.build();
	// }
	//
	// private Portfolio createPortfolio(Member member) {
	// 	return Portfolio.builder()
	// 		.name("내꿈은 워렌버핏")
	// 		.securitiesFirm("토스")
	// 		.budget(Money.won(1000000L))
	// 		.targetGain(Money.won(1500000L))
	// 		.maximumLoss(Money.won(900000L))
	// 		.member(member)
	// 		.targetGainIsActive(false)
	// 		.maximumLossIsActive(false)
	// 		.build();
	// }
	//
	// private Stock createStock() {
	// 	return Stock.builder()
	// 		.companyName("삼성전자보통주")
	// 		.tickerSymbol("005930")
	// 		.companyNameEng("SamsungElectronics")
	// 		.stockCode("KR7005930003")
	// 		.sector("전기전자")
	// 		.market(Market.KOSPI)
	// 		.build();
	// }
	//
	// private StockTargetPrice createStockTargetPrice(Member member, Stock stock) {
	// 	return StockTargetPrice.builder()
	// 		.member(member)
	// 		.stock(stock)
	// 		.isActive(true)
	// 		.build();
	// }
	//
	// private PortfolioHolding createPortfolioHolding(Portfolio portfolio, Stock stock) {
	// 	return PortfolioHolding.builder()
	// 		.portfolio(portfolio)
	// 		.stock(stock)
	// 		.build();
	// }
	//
	// private PurchaseHistory createPurchaseHistory(PortfolioHolding portfolioHolding, Long numShares,
	// 	Double purchasePricePerShare) {
	// 	return PurchaseHistory.builder()
	// 		.purchaseDate(LocalDateTime.of(2023, 9, 26, 9, 30, 0))
	// 		.numShares(Count.from(numShares))
	// 		.purchasePricePerShare(Money.won(purchasePricePerShare))
	// 		.memo("첫구매")
	// 		.portfolioHolding(portfolioHolding)
	// 		.build();
	// }
}
