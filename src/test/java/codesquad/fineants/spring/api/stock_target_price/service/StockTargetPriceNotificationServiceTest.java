package codesquad.fineants.spring.api.stock_target_price.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.google.firebase.messaging.Message;

import codesquad.fineants.domain.fcm_token.FcmRepository;
import codesquad.fineants.domain.fcm_token.FcmToken;
import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.notification.NotificationRepository;
import codesquad.fineants.domain.notification.type.NotificationType;
import codesquad.fineants.domain.notification_preference.NotificationPreference;
import codesquad.fineants.domain.notification_preference.NotificationPreferenceRepository;
import codesquad.fineants.domain.stock.Market;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.stock.StockRepository;
import codesquad.fineants.domain.stock_target_price.StockTargetPrice;
import codesquad.fineants.domain.stock_target_price.StockTargetPriceRepository;
import codesquad.fineants.domain.target_price_notification.TargetPriceNotification;
import codesquad.fineants.domain.target_price_notification.TargetPriceNotificationRepository;
import codesquad.fineants.spring.AbstractContainerBaseTest;
import codesquad.fineants.spring.api.common.errors.errorcode.StockErrorCode;
import codesquad.fineants.spring.api.common.errors.exception.BadRequestException;
import codesquad.fineants.spring.api.common.errors.exception.ForBiddenException;
import codesquad.fineants.spring.api.common.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.firebase.service.FirebaseMessagingService;
import codesquad.fineants.spring.api.kis.client.KisCurrentPrice;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.kis.manager.LastDayClosingPriceManager;
import codesquad.fineants.spring.api.kis.service.KisService;
import codesquad.fineants.spring.api.stock_target_price.manager.TargetPriceNotificationSentManager;
import codesquad.fineants.spring.api.stock_target_price.request.TargetPriceNotificationCreateRequest;
import codesquad.fineants.spring.api.stock_target_price.request.TargetPriceNotificationUpdateRequest;
import codesquad.fineants.spring.api.stock_target_price.response.TargetPriceNotificationCreateResponse;
import codesquad.fineants.spring.api.stock_target_price.response.TargetPriceNotificationDeleteResponse;
import codesquad.fineants.spring.api.stock_target_price.response.TargetPriceNotificationSearchResponse;
import codesquad.fineants.spring.api.stock_target_price.response.TargetPriceNotificationSendResponse;
import codesquad.fineants.spring.api.stock_target_price.response.TargetPriceNotificationSpecifiedSearchResponse;
import codesquad.fineants.spring.api.stock_target_price.response.TargetPriceNotificationUpdateResponse;
import reactor.core.publisher.Mono;

class StockTargetPriceNotificationServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private StockTargetPriceNotificationService service;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private StockTargetPriceRepository repository;

	@Autowired
	private TargetPriceNotificationRepository targetPriceNotificationRepository;

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private FcmRepository fcmRepository;

	@Autowired
	private NotificationPreferenceRepository notificationPreferenceRepository;

	@MockBean
	private LastDayClosingPriceManager manager;

	@MockBean
	private CurrentPriceManager currentPriceManager;

	@MockBean
	private KisService kisService;

	@MockBean
	private FirebaseMessagingService firebaseMessagingService;

	@MockBean
	private TargetPriceNotificationSentManager targetPriceNotificationSentManager;

	@AfterEach
	void tearDown() {
		notificationPreferenceRepository.deleteAllInBatch();
		fcmRepository.deleteAllInBatch();
		notificationRepository.deleteAllInBatch();
		targetPriceNotificationRepository.deleteAllInBatch();
		repository.deleteAllInBatch();
		stockRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
	}

	@DisplayName("사용자는 종목 지정가 알림을 추가합니다")
	@Test
	void createStockTargetPriceNotification() {
		// given
		Member member = memberRepository.save(createMember());
		Stock stock = stockRepository.save(createStock());
		TargetPriceNotificationCreateRequest request = TargetPriceNotificationCreateRequest.builder()
			.tickerSymbol(stock.getTickerSymbol())
			.targetPrice(60000L)
			.build();

		// when
		TargetPriceNotificationCreateResponse response = service.createStockTargetPriceNotification(request,
			member.getId());

		// then
		TargetPriceNotification targetPriceNotification = targetPriceNotificationRepository.findById(
			response.getTargetPriceNotificationId()).orElseThrow();

		assertThat(response)
			.extracting("targetPriceNotificationId", "tickerSymbol", "targetPrice")
			.containsExactlyInAnyOrder(targetPriceNotification.getId(),
				stock.getTickerSymbol(),
				60000L);
	}

	@DisplayName("사용자는 한 종목의 지정가 알림 개수를 5개를 초과할 수 없다")
	@Test
	void createStockTargetPriceNotification_whenTargetPriceNotificationLimit_thenThrow400Error() {
		// given
		Member member = memberRepository.save(createMember());
		Stock stock = stockRepository.save(createStock());
		TargetPriceNotificationCreateRequest request = TargetPriceNotificationCreateRequest.builder()
			.tickerSymbol(stock.getTickerSymbol())
			.targetPrice(60000L)
			.build();
		StockTargetPrice stockTargetPrice = repository.save(createStockTargetPrice(member, stock));
		targetPriceNotificationRepository.saveAll(createTargetPriceNotification(
			stockTargetPrice,
			List.of(10000L, 20000L, 30000L, 40000L, 50000L)));

		// when
		Throwable throwable = catchThrowable(() ->
			service.createStockTargetPriceNotification(request, member.getId()));

		// then
		assertThat(throwable)
			.isInstanceOf(BadRequestException.class)
			.hasMessage(StockErrorCode.BAD_REQUEST_TARGET_PRICE_NOTIFICATION_LIMIT.getMessage());
	}

	@DisplayName("사용자는 한 종목의 지정가가 이미 존재하는 경우 추가할 수 없다")
	@Test
	void createStockTargetPriceNotification_whenExistTargetPrice_thenThrow400Error() {
		// given
		Member member = memberRepository.save(createMember());
		Stock stock = stockRepository.save(createStock());
		TargetPriceNotificationCreateRequest request = TargetPriceNotificationCreateRequest.builder()
			.tickerSymbol(stock.getTickerSymbol())
			.targetPrice(60000L)
			.build();
		StockTargetPrice stockTargetPrice = repository.save(createStockTargetPrice(member, stock));
		targetPriceNotificationRepository.saveAll(createTargetPriceNotification(stockTargetPrice, List.of(60000L)));

		// when
		Throwable throwable = catchThrowable(() ->
			service.createStockTargetPriceNotification(request, member.getId()));

		// then
		assertThat(throwable)
			.isInstanceOf(BadRequestException.class)
			.hasMessage(StockErrorCode.BAD_REQUEST_TARGET_PRICE_NOTIFICATION_EXIST.getMessage());
	}

	@DisplayName("모든 회원들을 대상으로 특정 티커 심볼에 대한 종목 지정가 알림을 발송한다")
	@Test
	void sendAllStockTargetPriceNotification() {
		// given
		Member member = memberRepository.save(createMember("일개미1234", "kim1234@naver.com"));
		Member member2 = memberRepository.save(createMember("네모네모", "dragonbead95@naver.com"));

		notificationPreferenceRepository.save(createNotificationPreference(member));
		notificationPreferenceRepository.save(createNotificationPreference(member2));

		fcmRepository.save(createFcmToken(member, "token1"));
		fcmRepository.save(createFcmToken(member2, "token2"));

		Stock stock = stockRepository.save(createStock());
		Stock stock2 = stockRepository.save(createStock2());

		StockTargetPrice stockTargetPrice1 = repository.save(createStockTargetPrice(member, stock));
		StockTargetPrice stockTargetPrice2 = repository.save(createStockTargetPrice(member, stock2));
		targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(stockTargetPrice1, List.of(60000L, 70000L)));
		targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(stockTargetPrice2, List.of(10000L, 20000L)));

		StockTargetPrice stockTargetPrice3 = repository.save(createStockTargetPrice(member2, stock));
		StockTargetPrice stockTargetPrice4 = repository.save(createStockTargetPrice(member2, stock2));
		targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(stockTargetPrice3, List.of(60000L, 70000L)));
		targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(stockTargetPrice4, List.of(10000L, 20000L)));

		given(currentPriceManager.getCurrentPrice(stock.getTickerSymbol()))
			.willReturn(Optional.of(60000L));
		given(currentPriceManager.getCurrentPrice(stock2.getTickerSymbol()))
			.willReturn(Optional.empty());
		given(targetPriceNotificationSentManager.hasNotificationSent(anyLong()))
			.willReturn(false);
		given(kisService.fetchCurrentPrice(stock2.getTickerSymbol()))
			.willReturn(Mono.just(KisCurrentPrice.create(stock2.getTickerSymbol(), 10000L)));
		given(firebaseMessagingService.sendNotification(any(Message.class)))
			.willReturn(Optional.of("messageId1"))
			.willReturn(Optional.of("messageId2"))
			.willReturn(Optional.of("messageId3"))
			.willReturn(Optional.of("messageId4"));

		List<String> tickerSymbols = Stream.of(stock, stock2)
			.map(Stock::getTickerSymbol)
			.collect(Collectors.toList());

		// when
		TargetPriceNotificationSendResponse response = service.sendAllStockTargetPriceNotification(tickerSymbols);

		// then
		assertAll(
			() -> assertThat(response.getNotifications())
				.asList()
				.hasSize(4),
			() -> assertThat(notificationRepository.findAllByMemberIds(List.of(member.getId(), member2.getId())))
				.asList()
				.hasSize(4)
		);
	}

	@DisplayName("사용자는 사용자가 지정한 종목 지정가에 대한 푸시 알림을 받는다")
	@Test
	void sendStockTargetPriceNotification() {
		// given
		Member member = memberRepository.save(createMember());
		notificationPreferenceRepository.save(NotificationPreference.builder()
			.browserNotify(true)
			.targetGainNotify(true)
			.maxLossNotify(true)
			.targetPriceNotify(true)
			.member(member)
			.build());
		fcmRepository.save(createFcmToken(member, "token"));
		Stock stock = stockRepository.save(createStock());
		Stock stock2 = stockRepository.save(createStock2());
		StockTargetPrice stockTargetPrice = repository.save(createStockTargetPrice(member, stock));
		StockTargetPrice stockTargetPrice2 = repository.save(createStockTargetPrice(member, stock2));
		targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(stockTargetPrice, List.of(60000L, 70000L)));
		targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(stockTargetPrice2, List.of(10000L, 20000L)));

		given(currentPriceManager.getCurrentPrice(stock.getTickerSymbol()))
			.willReturn(Optional.of(60000L));
		given(currentPriceManager.getCurrentPrice(stock2.getTickerSymbol()))
			.willReturn(Optional.empty());
		given(targetPriceNotificationSentManager.hasNotificationSent(anyLong()))
			.willReturn(false);
		given(kisService.fetchCurrentPrice(stock2.getTickerSymbol()))
			.willReturn(Mono.just(KisCurrentPrice.create(stock2.getTickerSymbol(), 10000L)));
		given(firebaseMessagingService.sendNotification(any(Message.class)))
			.willReturn(Optional.of("messageId1"))
			.willReturn(Optional.of("messageId2"));
		// when
		TargetPriceNotificationSendResponse response = service.sendStockTargetPriceNotification(
			member.getId());

		// then
		NotificationType type = NotificationType.STOCK_TARGET_PRICE;
		assertThat(response.getNotifications())
			.asList()
			.hasSize(2)
			.extracting("title", "type", "referenceId", "messageId")
			.containsExactlyInAnyOrder(
				Tuple.tuple(type.getName(), type, "005930", "messageId1"),
				Tuple.tuple(type.getName(), type, "000020", "messageId2"));
		assertThat(notificationRepository.findAllByMemberId(member.getId()))
			.asList()
			.hasSize(2);
	}

	@DisplayName("티커 심볼을 기준으로 종목 지정가 알림을 발송한다")
	@Test
	void sendAllStockTargetPriceNotification_whenMultipleMember_thenSendNotification() {
		// given
		Member member = memberRepository.save(createMember());
		notificationPreferenceRepository.save(NotificationPreference.builder()
			.browserNotify(true)
			.targetGainNotify(true)
			.maxLossNotify(true)
			.targetPriceNotify(true)
			.member(member)
			.build());
		fcmRepository.save(createFcmToken(member, "token"));
		Stock stock = stockRepository.save(createStock());
		Stock stock2 = stockRepository.save(createStock2());
		StockTargetPrice stockTargetPrice = repository.save(createStockTargetPrice(member, stock));
		StockTargetPrice stockTargetPrice2 = repository.save(createStockTargetPrice(member, stock2));
		targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(stockTargetPrice, List.of(60000L, 70000L)));
		targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(stockTargetPrice2, List.of(10000L, 20000L)));

		given(currentPriceManager.getCurrentPrice(stock.getTickerSymbol()))
			.willReturn(Optional.empty());
		given(currentPriceManager.getCurrentPrice(stock2.getTickerSymbol()))
			.willReturn(Optional.empty());
		given(targetPriceNotificationSentManager.hasNotificationSent(anyLong()))
			.willReturn(false);
		given(kisService.fetchCurrentPrice(stock.getTickerSymbol()))
			.willAnswer(invocation -> {
				Thread.sleep(1000L);
				return Mono.just(KisCurrentPrice.create(stock.getTickerSymbol(), 60000L));
			});
		given(kisService.fetchCurrentPrice(stock2.getTickerSymbol()))
			.willAnswer(invocation -> {
				Thread.sleep(1000L);
				return Mono.just(KisCurrentPrice.create(stock2.getTickerSymbol(), 10000L));
			});
		given(firebaseMessagingService.sendNotification(any(Message.class)))
			.willReturn(Optional.of("messageId"))
			.willReturn(Optional.of("messageId"));
		// when
		TargetPriceNotificationSendResponse response = service.sendAllStockTargetPriceNotification(
			List.of(stock.getTickerSymbol(), stock2.getTickerSymbol()));

		// then
		NotificationType type = NotificationType.STOCK_TARGET_PRICE;
		assertThat(response.getNotifications())
			.asList()
			.hasSize(2)
			.extracting("title", "type", "referenceId", "messageId")
			.containsExactlyInAnyOrder(
				Tuple.tuple(type.getName(), type, "005930", "messageId"),
				Tuple.tuple(type.getName(), type, "000020", "messageId"));
		assertThat(notificationRepository.findAllByMemberId(member.getId()))
			.asList()
			.hasSize(2);
	}

	@DisplayName("사용자는 종목 지정가 알림 목록을 조회합니다")
	@Test
	void searchStockTargetPriceNotification() {
		// given
		Member member = memberRepository.save(createMember());
		Stock stock = stockRepository.save(createStock());
		Stock stock2 = stockRepository.save(createStock2());

		StockTargetPrice stockTargetPrice = repository.save(createStockTargetPrice(member, stock));
		List<TargetPriceNotification> targetPriceNotifications = targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(stockTargetPrice, List.of(60000L, 70000L)));

		StockTargetPrice stockTargetPrice2 = repository.save(createStockTargetPrice(member, stock2));
		List<TargetPriceNotification> targetPriceNotifications2 = targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(stockTargetPrice2, List.of(60000L, 70000L)));

		given(manager.getPrice(anyString()))
			.willReturn(Optional.of(50000L));

		// when
		TargetPriceNotificationSearchResponse response = service.searchStockTargetPriceNotification(member.getId());

		// then
		assertAll(
			() -> assertThat(response)
				.extracting("stocks")
				.asList()
				.hasSize(2)
				.extracting("companyName", "tickerSymbol", "lastPrice", "isActive")
				.containsExactly(
					Tuple.tuple(stock.getCompanyName(), stock.getTickerSymbol(), 50000L, true),
					Tuple.tuple(stock2.getCompanyName(), stock2.getTickerSymbol(), 50000L, true)
				),
			() -> assertThat(response.getStocks().get(0))
				.extracting("targetPrices")
				.asList()
				.hasSize(2)
				.extracting("notificationId", "targetPrice")
				.containsExactly(
					Tuple.tuple(targetPriceNotifications.get(0).getId(), 60000L),
					Tuple.tuple(targetPriceNotifications.get(1).getId(), 70000L)),
			() -> assertThat(response.getStocks().get(1))
				.extracting("targetPrices")
				.asList()
				.hasSize(2)
				.extracting("notificationId", "targetPrice")
				.containsExactly(
					Tuple.tuple(targetPriceNotifications2.get(0).getId(), 60000L),
					Tuple.tuple(targetPriceNotifications2.get(1).getId(), 70000L))
		);
	}

	@DisplayName("사용자는 특정 종목 지정가 알림들을 조회합니다")
	@Test
	void searchTargetPriceNotifications() {
		// given
		Member member = memberRepository.save(createMember());
		Stock stock = stockRepository.save(createStock());
		StockTargetPrice stockTargetPrice = repository.save(createStockTargetPrice(member, stock));
		List<TargetPriceNotification> targetPriceNotifications = targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(stockTargetPrice, List.of(60000L, 70000L)));

		given(manager.getPrice(anyString()))
			.willReturn(Optional.of(50000L));

		// when
		TargetPriceNotificationSpecifiedSearchResponse response = service.searchTargetPriceNotifications(
			stock.getTickerSymbol(), member.getId());

		// then
		assertThat(response)
			.extracting("targetPrices")
			.asList()
			.hasSize(2)
			.extracting("notificationId", "targetPrice")
			.containsExactlyInAnyOrder(
				Tuple.tuple(targetPriceNotifications.get(0).getId(), 60000L),
				Tuple.tuple(targetPriceNotifications.get(1).getId(), 70000L));
	}

	@DisplayName("사용자가 없는 종목을 대상으로 지정가 알림 목록 조회시 빈 리스트를 반환받는다")
	@Test
	void searchTargetPriceNotifications_whenNotExistStock_thenResponseEmptyList() {
		// given
		Member member = memberRepository.save(createMember());
		Stock stock = stockRepository.save(createStock());

		// when
		TargetPriceNotificationSpecifiedSearchResponse response = service.searchTargetPriceNotifications(
			stock.getTickerSymbol(), member.getId());

		// then
		assertThat(response)
			.extracting("targetPrices")
			.asList()
			.isEmpty();
	}

	@DisplayName("사용자는 종목 지정가 알림을 수정한다")
	@Test
	void updateStockTargetPriceNotification() {
		// given
		Member member = memberRepository.save(createMember());
		Stock stock = stockRepository.save(createStock());
		StockTargetPrice stockTargetPrice = repository.save(createStockTargetPrice(member, stock));
		targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(
				stockTargetPrice,
				List.of(10000L, 20000L)
			)
		);
		TargetPriceNotificationUpdateRequest request = TargetPriceNotificationUpdateRequest.builder()
			.tickerSymbol(stock.getTickerSymbol())
			.isActive(false)
			.build();

		// when
		TargetPriceNotificationUpdateResponse response = service.updateStockTargetPriceNotification(
			request, member.getId());

		// then
		StockTargetPrice findStockTargetPrice = repository.findByTickerSymbolAndMemberId(stock.getTickerSymbol(),
			member.getId()).orElseThrow();
		assertAll(
			() -> assertThat(findStockTargetPrice.getIsActive()).isFalse(),
			() -> assertThat(response)
				.extracting("isActive")
				.isEqualTo(false)
		);
	}

	@DisplayName("사용자는 자신이 지정하지 않은 종목 지정가를 수정할 수 없다")
	@Test
	void updateStockTargetPriceNotification_whenNotExistTickerSymbol_thenThrow404Error() {
		// given
		Member member = memberRepository.save(createMember());
		Stock stock = stockRepository.save(createStock());
		StockTargetPrice stockTargetPrice = repository.save(createStockTargetPrice(member, stock));
		targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(
				stockTargetPrice,
				List.of(10000L, 20000L)
			)
		);
		TargetPriceNotificationUpdateRequest request = TargetPriceNotificationUpdateRequest.builder()
			.tickerSymbol("999999")
			.isActive(false)
			.build();

		// when
		Throwable throwable = catchThrowable(() -> service.updateStockTargetPriceNotification(request, member.getId()));

		// then
		assertThat(throwable)
			.isInstanceOf(NotFoundResourceException.class)
			.hasMessage(StockErrorCode.NOT_FOUND_STOCK_TARGET_PRICE.getMessage());
	}

	@DisplayName("사용자는 종목 지정가 알림을 제거합니다")
	@Test
	void deleteStockTargetPriceNotification() {
		// given
		Member member = memberRepository.save(createMember());
		Stock stock = stockRepository.save(createStock());
		StockTargetPrice stockTargetPrice = repository.save(createStockTargetPrice(member, stock));
		TargetPriceNotification targetPriceNotification = targetPriceNotificationRepository.save(
			createTargetPriceNotification(stockTargetPrice, 60000L));

		Long id = targetPriceNotification.getId();
		// when
		TargetPriceNotificationDeleteResponse response = service.deleteStockTargetPriceNotification(
			id,
			member.getId()
		);

		// then
		assertAll(
			() -> assertThat(response)
				.extracting("deletedIds")
				.asList()
				.hasSize(1),
			() -> assertThat(targetPriceNotificationRepository.findById(id).isEmpty()).isTrue(),
			() -> assertThat(repository.findById(stockTargetPrice.getId()).isEmpty()).isTrue()
		);
	}

	@DisplayName("사용자는 존재하지 않는 지정가를 삭제할 수 없습니다")
	@Test
	void deleteStockTargetPriceNotification_whenNotExistTargetPriceNotificationIds_thenThrow404Error() {
		// given
		Member member = memberRepository.save(createMember());
		Stock stock = stockRepository.save(createStock());
		StockTargetPrice stockTargetPrice = repository.save(createStockTargetPrice(member, stock));
		targetPriceNotificationRepository.save(createTargetPriceNotification(stockTargetPrice, 60000L));

		Long id = 9999L;

		// when
		Throwable throwable = catchThrowable(() -> service.deleteStockTargetPriceNotification(
				id,
				member.getId()
			)
		);

		// then
		assertThat(throwable)
			.isInstanceOf(NotFoundResourceException.class)
			.hasMessage(StockErrorCode.NOT_FOUND_TARGET_PRICE.getMessage());
	}

	@DisplayName("사용자는 다른 사용자의 지정가 알림을 삭제할 수 없습니다.")
	@Test
	void deleteStockTargetPriceNotification_whenForbiddenTargetPriceNotificationIds_thenThrow403Error() {
		// given
		Member member = memberRepository.save(createMember());
		Stock stock = stockRepository.save(createStock());
		StockTargetPrice stockTargetPrice = repository.save(createStockTargetPrice(member, stock));
		TargetPriceNotification targetPriceNotification = targetPriceNotificationRepository.save(
			createTargetPriceNotification(stockTargetPrice, 60000L));

		Member hacker = memberRepository.save(createMember("일개미4567", "kim2@gmail.com"));

		// when
		Throwable throwable = catchThrowable(
			() -> service.deleteStockTargetPriceNotification(targetPriceNotification.getId(), hacker.getId()));

		// then
		assertThat(throwable)
			.isInstanceOf(ForBiddenException.class)
			.hasMessage(StockErrorCode.FORBIDDEN_DELETE_TARGET_PRICE_NOTIFICATION.getMessage());
	}

	@DisplayName("사용자는 종목 지정가 알림 전체를 삭제합니다")
	@Test
	void deleteAllStockTargetPriceNotification() {
		// given
		Member member = memberRepository.save(createMember());
		Stock stock = stockRepository.save(createStock());
		StockTargetPrice stockTargetPrice = repository.save(createStockTargetPrice(member, stock));
		List<TargetPriceNotification> targetPriceNotifications = targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(stockTargetPrice, List.of(60000L, 70000L)));

		List<Long> ids = targetPriceNotifications.stream()
			.map(TargetPriceNotification::getId)
			.collect(Collectors.toList());
		// when
		TargetPriceNotificationDeleteResponse response = service.deleteAllStockTargetPriceNotification(
			ids,
			stock.getTickerSymbol(),
			member.getId()
		);

		// then
		assertAll(
			() -> assertThat(response)
				.extracting("deletedIds")
				.asList()
				.hasSize(2),
			() -> assertThat(targetPriceNotificationRepository.findAllById(ids).isEmpty()).isTrue(),
			() -> assertThat(repository.findById(stockTargetPrice.getId()).isEmpty()).isTrue()
		);
	}

	@DisplayName("사용자는 종목 지정가 알림을 전체 삭제할 때, 존재하지 않는 지정가 알림을 제거할 수 없습니다.")
	@Test
	void deleteAllStockTargetPriceNotification_whenNotExistTargetPriceNotificationIds_thenThrow404Error() {
		// given
		Member member = memberRepository.save(createMember());
		Stock stock = stockRepository.save(createStock());
		StockTargetPrice stockTargetPrice = repository.save(createStockTargetPrice(member, stock));
		List<TargetPriceNotification> targetPriceNotifications = targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(stockTargetPrice, List.of(60000L, 70000L)));

		List<Long> ids = targetPriceNotifications.stream()
			.map(TargetPriceNotification::getId)
			.collect(Collectors.toList());
		ids.add(9999L);

		// when
		Throwable throwable = catchThrowable(() -> service.deleteAllStockTargetPriceNotification(
			ids,
			stock.getTickerSymbol(),
			member.getId()
		));

		// then
		assertThat(throwable)
			.isInstanceOf(NotFoundResourceException.class)
			.hasMessage(StockErrorCode.NOT_FOUND_TARGET_PRICE.getMessage());
	}

	@DisplayName("사용자는 종목 지정가 알림을 전체 삭제할 때, 존재하지 않는 종목에 대해서 제거할 수 없습니다.")
	@Test
	void deleteAllStockTargetPriceNotification_whenNotExistStock_thenThrow404Error() {
		// given
		Member member = memberRepository.save(createMember());
		Stock stock = stockRepository.save(createStock());
		StockTargetPrice stockTargetPrice = repository.save(createStockTargetPrice(member, stock));
		List<TargetPriceNotification> targetPriceNotifications = targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(stockTargetPrice, List.of(60000L, 70000L)));

		List<Long> ids = targetPriceNotifications.stream()
			.map(TargetPriceNotification::getId)
			.collect(Collectors.toList());

		// when
		Throwable throwable = catchThrowable(() -> service.deleteAllStockTargetPriceNotification(
			ids,
			"999999",
			member.getId()
		));

		// then
		assertThat(throwable)
			.isInstanceOf(NotFoundResourceException.class)
			.hasMessage(StockErrorCode.NOT_FOUND_STOCK.getMessage());
	}

	@DisplayName("사용자는 종목 지정가 알림을 전체 삭제할 때, 다른 사용자의 지정가 알림을 삭제할 수 없습니다")
	@Test
	void deleteAllStockTargetPriceNotification_whenForbiddenTargetPriceNotifications_thenThrow403Error() {
		// given
		Member member = memberRepository.save(createMember());
		Stock stock = stockRepository.save(createStock());
		StockTargetPrice stockTargetPrice = repository.save(createStockTargetPrice(member, stock));
		List<TargetPriceNotification> targetPriceNotifications = targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(stockTargetPrice, List.of(60000L, 70000L)));

		List<Long> ids = targetPriceNotifications.stream()
			.map(TargetPriceNotification::getId)
			.collect(Collectors.toList());

		Member hacker = createMember("일개미4567", "kim2@gmail.com");

		// when
		Throwable throwable = catchThrowable(() -> service.deleteAllStockTargetPriceNotification(
			ids,
			stock.getTickerSymbol(),
			hacker.getId()
		));

		// then
		assertThat(throwable)
			.isInstanceOf(ForBiddenException.class)
			.hasMessage(StockErrorCode.FORBIDDEN_DELETE_TARGET_PRICE_NOTIFICATION.getMessage());
	}

	private StockTargetPrice createStockTargetPrice(Member member, Stock stock) {
		return StockTargetPrice.builder()
			.member(member)
			.stock(stock)
			.isActive(true)
			.build();
	}

	private StockTargetPrice createStockTargetPrice(Member member, Stock stock, LocalDateTime createAt) {
		return StockTargetPrice.builder()
			.member(member)
			.stock(stock)
			.isActive(true)
			.createAt(createAt)
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

	private Member createMember() {
		return createMember("일개미1234", "kim1234@gmail.com");
	}

	private Member createMember(String nickname, String email) {
		return Member.builder()
			.nickname(nickname)
			.email(email)
			.password("kim1234@")
			.provider("local")
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

	private Stock createStock2() {
		return Stock.builder()
			.companyName("동화약품보통주")
			.tickerSymbol("000020")
			.companyNameEng("DongwhaPharm")
			.stockCode("KR7000020008")
			.sector("의약품")
			.market(Market.KOSPI)
			.build();
	}

	private FcmToken createFcmToken(Member member, String token) {
		return FcmToken.builder()
			.token(token)
			.latestActivationTime(LocalDateTime.now())
			.member(member)
			.build();
	}

	private NotificationPreference createNotificationPreference(Member member) {
		return NotificationPreference.builder()
			.browserNotify(true)
			.targetGainNotify(true)
			.maxLossNotify(true)
			.targetPriceNotify(true)
			.member(member)
			.build();
	}
}
