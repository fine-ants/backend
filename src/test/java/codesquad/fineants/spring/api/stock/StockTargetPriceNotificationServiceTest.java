package codesquad.fineants.spring.api.stock;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.stock.Market;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.stock.StockRepository;
import codesquad.fineants.domain.stock_target_price.StockTargetPrice;
import codesquad.fineants.domain.stock_target_price.StockTargetPriceRepository;
import codesquad.fineants.spring.api.errors.errorcode.StockErrorCode;
import codesquad.fineants.spring.api.errors.exception.BadRequestException;
import codesquad.fineants.spring.api.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.stock.request.TargetPriceNotificationCreateRequest;
import codesquad.fineants.spring.api.stock.response.TargetPriceNotificationCreateResponse;
import codesquad.fineants.spring.api.stock.response.TargetPriceNotificationDeleteResponse;

@ActiveProfiles("test")
@SpringBootTest
class StockTargetPriceNotificationServiceTest {

	@Autowired
	private StockTargetPriceNotificationService service;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private StockTargetPriceRepository repository;

	@AfterEach
	void tearDown() {
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
		StockTargetPrice stockTargetPrice = repository.findById(response.getTargetPriceNotificationId()).orElseThrow();

		assertThat(response)
			.extracting("targetPriceNotificationId", "tickerSymbol", "targetPrice")
			.containsExactlyInAnyOrder(stockTargetPrice.getId(), stockTargetPrice.getStock().getTickerSymbol(),
				stockTargetPrice.getTargetPrice());
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
		repository.saveAll(
			List.of(
				createStockTargetPrice(member, stock, 10000L),
				createStockTargetPrice(member, stock, 20000L),
				createStockTargetPrice(member, stock, 30000L),
				createStockTargetPrice(member, stock, 40000L),
				createStockTargetPrice(member, stock, 50000L)
			)
		);

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
		repository.save(createStockTargetPrice(member, stock, 60000L));

		// when
		Throwable throwable = catchThrowable(() ->
			service.createStockTargetPriceNotification(request, member.getId()));

		// then
		assertThat(throwable)
			.isInstanceOf(BadRequestException.class)
			.hasMessage(StockErrorCode.BAD_REQUEST_TARGET_PRICE_NOTIFICATION_EXIST.getMessage());
	}

	@DisplayName("사용자는 종목 지정가 알림을 제거합니다")
	@Test
	void deleteStockTargetPriceNotification() {
		// given
		Member member = memberRepository.save(createMember());
		Stock stock = stockRepository.save(createStock());
		List<StockTargetPrice> stockTargetPrices = repository.saveAll(List.of(
			createStockTargetPrice(member, stock, 60000L),
			createStockTargetPrice(member, stock, 70000L)
		));
		List<Long> ids = stockTargetPrices.stream()
			.map(StockTargetPrice::getId)
			.collect(Collectors.toList());

		// when
		TargetPriceNotificationDeleteResponse response = service.deleteStockTargetPriceNotification(
			ids, stock.getTickerSymbol(), member.getId());

		// then
		assertAll(
			() -> assertThat(response)
				.extracting("deletedIds")
				.asList()
				.hasSize(2),
			() -> assertThat(repository.findAllById(ids).size()).isZero()
		);
	}

	@DisplayName("사용자는 존재하지 않는 지정가를 삭제할 수 없습니다")
	@Test
	void deleteStockTargetPriceNotification_whenNotExistTargetPriceNotificationIds_thenThrow404Error() {
		// given
		Member member = memberRepository.save(createMember());
		Stock stock = stockRepository.save(createStock());
		List<StockTargetPrice> stockTargetPrices = repository.saveAll(List.of(
			createStockTargetPrice(member, stock, 60000L),
			createStockTargetPrice(member, stock, 70000L)
		));
		List<Long> ids = stockTargetPrices.stream()
			.map(StockTargetPrice::getId)
			.collect(Collectors.toList());
		ids.add(9999L);

		// when
		Throwable throwable = catchThrowable(() -> service.deleteStockTargetPriceNotification(
			ids, stock.getTickerSymbol(), member.getId()));

		// then
		assertThat(throwable)
			.isInstanceOf(NotFoundResourceException.class)
			.hasMessage(StockErrorCode.NOT_FOUND_TARGET_PRICE.getMessage());
	}

	private StockTargetPrice createStockTargetPrice(Member member, Stock stock, Long targetPrice) {
		return StockTargetPrice.builder()
			.member(member)
			.stock(stock)
			.targetPrice(targetPrice)
			.build();
	}

	private Member createMember() {
		return Member.builder()
			.id(1L)
			.nickname("일개미1234")
			.email("kim1234@gmail.com")
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
}
