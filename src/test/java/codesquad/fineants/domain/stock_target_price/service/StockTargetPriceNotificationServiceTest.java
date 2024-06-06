package codesquad.fineants.domain.stock_target_price.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.fcm.repository.FcmRepository;
import codesquad.fineants.domain.kis.domain.dto.response.KisClosingPrice;
import codesquad.fineants.domain.kis.repository.ClosingPriceRepository;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.domain.notification.repository.NotificationRepository;
import codesquad.fineants.domain.notificationpreference.repository.NotificationPreferenceRepository;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.domain.stock.repository.StockRepository;
import codesquad.fineants.domain.stock_target_price.domain.dto.request.TargetPriceNotificationCreateRequest;
import codesquad.fineants.domain.stock_target_price.domain.dto.request.TargetPriceNotificationUpdateRequest;
import codesquad.fineants.domain.stock_target_price.domain.dto.response.TargetPriceNotificationCreateResponse;
import codesquad.fineants.domain.stock_target_price.domain.dto.response.TargetPriceNotificationDeleteResponse;
import codesquad.fineants.domain.stock_target_price.domain.dto.response.TargetPriceNotificationSearchResponse;
import codesquad.fineants.domain.stock_target_price.domain.dto.response.TargetPriceNotificationSpecifiedSearchResponse;
import codesquad.fineants.domain.stock_target_price.domain.dto.response.TargetPriceNotificationUpdateResponse;
import codesquad.fineants.domain.stock_target_price.domain.entity.StockTargetPrice;
import codesquad.fineants.domain.stock_target_price.domain.entity.TargetPriceNotification;
import codesquad.fineants.domain.stock_target_price.repository.StockTargetPriceRepository;
import codesquad.fineants.domain.stock_target_price.repository.TargetPriceNotificationRepository;
import codesquad.fineants.global.errors.errorcode.StockErrorCode;
import codesquad.fineants.global.errors.exception.BadRequestException;
import codesquad.fineants.global.errors.exception.ForBiddenException;
import codesquad.fineants.global.errors.exception.NotFoundResourceException;

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

	@Autowired
	private ClosingPriceRepository manager;

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
		Stock stock = stockRepository.save(createSamsungStock());
		TargetPriceNotificationCreateRequest request = TargetPriceNotificationCreateRequest.builder()
			.tickerSymbol(stock.getTickerSymbol())
			.targetPrice(Money.won(60000L))
			.build();

		// when
		TargetPriceNotificationCreateResponse response = service.createStockTargetPriceNotification(request,
			member.getId());

		// then
		TargetPriceNotification targetPriceNotification = targetPriceNotificationRepository.findById(
			response.getTargetPriceNotificationId()).orElseThrow();

		assertThat(response)
			.extracting("targetPriceNotificationId", "tickerSymbol", "targetPrice")
			.usingComparatorForType(Money::compareTo, Money.class)
			.containsExactlyInAnyOrder(targetPriceNotification.getId(),
				stock.getTickerSymbol(),
				Money.won(60000L));
	}

	@DisplayName("사용자는 한 종목의 지정가 알림 개수를 5개를 초과할 수 없다")
	@Test
	void createStockTargetPriceNotification_whenTargetPriceNotificationLimit_thenThrow400Error() {
		// given
		Member member = memberRepository.save(createMember());
		Stock stock = stockRepository.save(createSamsungStock());
		TargetPriceNotificationCreateRequest request = TargetPriceNotificationCreateRequest.builder()
			.tickerSymbol(stock.getTickerSymbol())
			.targetPrice(Money.won(60000L))
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
		Stock stock = stockRepository.save(createSamsungStock());
		TargetPriceNotificationCreateRequest request = TargetPriceNotificationCreateRequest.builder()
			.tickerSymbol(stock.getTickerSymbol())
			.targetPrice(Money.won(60000L))
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

	@DisplayName("사용자는 종목 지정가 알림 목록을 조회합니다")
	@Test
	void searchStockTargetPriceNotification() {
		// given
		Member member = memberRepository.save(createMember());
		notificationPreferenceRepository.save(createAllActiveNotificationPreference(member));
		Stock stock = stockRepository.save(createSamsungStock());
		Stock stock2 = stockRepository.save(createDongwhaPharmStock());

		StockTargetPrice stockTargetPrice = repository.save(createStockTargetPrice(member, stock));
		List<TargetPriceNotification> targetPriceNotifications = targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(stockTargetPrice, List.of(60000L, 70000L)));

		StockTargetPrice stockTargetPrice2 = repository.save(createStockTargetPrice(member, stock2));
		List<TargetPriceNotification> targetPriceNotifications2 = targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(stockTargetPrice2, List.of(60000L, 70000L)));

		manager.addPrice(KisClosingPrice.create(stock.getTickerSymbol(), 50000L));
		manager.addPrice(KisClosingPrice.create(stock2.getTickerSymbol(), 50000L));
		// when
		TargetPriceNotificationSearchResponse response = service.searchStockTargetPriceNotification(member.getId());

		// then
		assertAll(
			() -> assertThat(response.getStocks())
				.asList()
				.hasSize(2)
				.extracting("companyName", "tickerSymbol", "lastPrice", "isActive")
				.usingComparatorForType(Money::compareTo, Money.class)
				.containsExactly(
					Tuple.tuple(stock.getCompanyName(), stock.getTickerSymbol(), Money.won(50000L), true),
					Tuple.tuple(stock2.getCompanyName(), stock2.getTickerSymbol(), Money.won(50000L), true)
				),
			() -> assertThat(response.getStocks().get(0))
				.extracting("targetPrices")
				.asList()
				.hasSize(2)
				.extracting("notificationId", "targetPrice")
				.containsExactly(
					Tuple.tuple(targetPriceNotifications.get(0).getId(), Money.won(60000L)),
					Tuple.tuple(targetPriceNotifications.get(1).getId(), Money.won(70000L))),
			() -> assertThat(response.getStocks().get(1))
				.extracting("targetPrices")
				.asList()
				.hasSize(2)
				.extracting("notificationId", "targetPrice")
				.usingComparatorForType(Money::compareTo, Money.class)
				.containsExactly(
					Tuple.tuple(targetPriceNotifications2.get(0).getId(), Money.won(60000L)),
					Tuple.tuple(targetPriceNotifications2.get(1).getId(), Money.won(70000L)))
		);
	}

	@DisplayName("사용자는 특정 종목 지정가 알림들을 조회합니다")
	@Test
	void searchTargetPriceNotifications() {
		// given
		Member member = memberRepository.save(createMember());
		Stock stock = stockRepository.save(createSamsungStock());
		StockTargetPrice stockTargetPrice = repository.save(createStockTargetPrice(member, stock));
		List<TargetPriceNotification> targetPriceNotifications = targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(stockTargetPrice, List.of(60000L, 70000L)));

		manager.addPrice(KisClosingPrice.create(stock.getTickerSymbol(), 50000L));

		// when
		TargetPriceNotificationSpecifiedSearchResponse response = service.searchTargetPriceNotifications(
			stock.getTickerSymbol(), member.getId());

		// then
		assertThat(response)
			.extracting("targetPrices")
			.asList()
			.hasSize(2)
			.extracting("notificationId", "targetPrice")
			.usingComparatorForType(Money::compareTo, Money.class)
			.containsExactlyInAnyOrder(
				Tuple.tuple(targetPriceNotifications.get(0).getId(), Money.won(60000L)),
				Tuple.tuple(targetPriceNotifications.get(1).getId(), Money.won(70000L)));
	}

	@DisplayName("사용자가 없는 종목을 대상으로 지정가 알림 목록 조회시 빈 리스트를 반환받는다")
	@Test
	void searchTargetPriceNotifications_whenNotExistStock_thenResponseEmptyList() {
		// given
		Member member = memberRepository.save(createMember());
		Stock stock = stockRepository.save(createSamsungStock());

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
		Stock stock = stockRepository.save(createSamsungStock());
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
		Stock stock = stockRepository.save(createSamsungStock());
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
		Stock stock = stockRepository.save(createSamsungStock());
		StockTargetPrice stockTargetPrice = repository.save(createStockTargetPrice(member, stock));
		TargetPriceNotification targetPriceNotification = targetPriceNotificationRepository.save(
			createTargetPriceNotification(stockTargetPrice));

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
		Stock stock = stockRepository.save(createSamsungStock());
		StockTargetPrice stockTargetPrice = repository.save(createStockTargetPrice(member, stock));
		targetPriceNotificationRepository.save(createTargetPriceNotification(stockTargetPrice));

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
		Stock stock = stockRepository.save(createSamsungStock());
		StockTargetPrice stockTargetPrice = repository.save(createStockTargetPrice(member, stock));
		TargetPriceNotification targetPriceNotification = targetPriceNotificationRepository.save(
			createTargetPriceNotification(stockTargetPrice));

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
		Stock stock = stockRepository.save(createSamsungStock());
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
		Stock stock = stockRepository.save(createSamsungStock());
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
		Stock stock = stockRepository.save(createSamsungStock());
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
		Stock stock = stockRepository.save(createSamsungStock());
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

	private TargetPriceNotification createTargetPriceNotification(StockTargetPrice stockTargetPrice) {
		return TargetPriceNotification.builder()
			.targetPrice(Money.won(60000L))
			.stockTargetPrice(stockTargetPrice)
			.build();
	}

	private List<TargetPriceNotification> createTargetPriceNotification(StockTargetPrice stockTargetPrice,
		List<Long> targetPrices) {
		return targetPrices.stream()
			.map(targetPrice -> TargetPriceNotification.builder()
				.targetPrice(Money.won(targetPrice))
				.stockTargetPrice(stockTargetPrice)
				.build())
			.collect(Collectors.toList());
	}
}
