package codesquad.fineants.spring.api.stock;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.stock.Market;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.stock.StockRepository;
import codesquad.fineants.domain.stock_target_price.StockTargetPrice;
import codesquad.fineants.domain.stock_target_price.StockTargetPriceRepository;
import codesquad.fineants.domain.target_price_notification.TargetPriceNotification;
import codesquad.fineants.domain.target_price_notification.TargetPriceNotificationRepository;
import codesquad.fineants.spring.AbstractContainerBaseTest;
import codesquad.fineants.spring.api.errors.errorcode.StockErrorCode;
import codesquad.fineants.spring.api.errors.exception.BadRequestException;
import codesquad.fineants.spring.api.errors.exception.ForBiddenException;
import codesquad.fineants.spring.api.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.kis.manager.LastDayClosingPriceManager;
import codesquad.fineants.spring.api.stock.request.TargetPriceNotificationCreateRequest;
import codesquad.fineants.spring.api.stock.request.TargetPriceNotificationUpdateRequest;
import codesquad.fineants.spring.api.stock.response.TargetPriceNotificationCreateResponse;
import codesquad.fineants.spring.api.stock.response.TargetPriceNotificationDeleteResponse;
import codesquad.fineants.spring.api.stock.response.TargetPriceNotificationSearchResponse;
import codesquad.fineants.spring.api.stock.response.TargetPriceNotificationSpecifiedSearchResponse;
import codesquad.fineants.spring.api.stock.response.TargetPriceNotificationUpdateResponse;

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

	@MockBean
	private LastDayClosingPriceManager manager;

	@AfterEach
	void tearDown() {
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
			.willReturn(50000L);

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
			.willReturn(50000L);

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
}
