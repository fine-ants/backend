package codesquad.fineants.spring.api.stock_dividend.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.stock.Market;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.stock.StockRepository;
import codesquad.fineants.domain.stock_dividend.StockDividend;
import codesquad.fineants.domain.stock_dividend.StockDividendRepository;
import codesquad.fineants.spring.AbstractContainerBaseTest;
import codesquad.fineants.spring.api.S3.dto.Dividend;
import codesquad.fineants.spring.api.S3.service.AmazonS3DividendService;
import codesquad.fineants.spring.api.kis.manager.KisAccessTokenManager;
import codesquad.fineants.spring.api.kis.response.KisDividend;
import codesquad.fineants.spring.api.kis.service.KisService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class StockDividendServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private StockDividendService stockDividendService;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private StockDividendRepository stockDividendRepository;

	@Autowired
	private AmazonS3DividendService amazonS3DividendService;

	@MockBean
	private KisService kisService;

	@MockBean
	private KisAccessTokenManager kisAccessTokenManager;

	@AfterEach
	void tearDown() {
		stockDividendRepository.deleteAllInBatch();
		stockRepository.deleteAllInBatch();
	}

	/**
	 * 해당 테스트 수행시 localStack에 저장된 dividends.csv 파일을 이용하여 배당 일정을 초기화합니다.
	 */
	@DisplayName("배당일정을 초기화한다")
	@Test
	void initializeStockDividend() {
		// given
		stockRepository.save(createStock());
		// when
		stockDividendService.initializeStockDividend();
		// then
		assertThat(stockDividendRepository.count()).isEqualTo(5);
	}

	@DisplayName("배당 일정을 최신화한다")
	@Test
	void refreshStockDividend() {
		// given
		Stock samsung = createSamsungStock();
		Stock kakao = createKakaoStock();
		stockRepository.saveAll(List.of(samsung, kakao));
		stockDividendRepository.saveAll(createSamsungDividends(samsung));
		stockDividendRepository.saveAll(createKakaoDividends(kakao));

		// 새로운 배정 기준일이 생김
		// 기존 데이터에 현금 배당 지급일이 새로 할당됨
		String samsungTickerSymbol = "005930";
		int samsungDividend = 361;

		String kakaoTickerSymbol = "035720";
		int kakaoDividend = 61;

		given(kisAccessTokenManager.createAuthorization()).willReturn(createAuthorization());
		given(kisService.fetchDividendAll(
			ArgumentMatchers.any(LocalDate.class),
			ArgumentMatchers.any(LocalDate.class)
		)).willReturn(List.of(
			KisDividend.create(
				samsungTickerSymbol,
				Money.from(samsungDividend),
				LocalDate.of(2023, 3, 31),
				LocalDate.of(2023, 5, 17)
			),
			KisDividend.create(
				samsungTickerSymbol,
				Money.from(samsungDividend),
				LocalDate.of(2023, 6, 30),
				LocalDate.of(2023, 8, 16)
			),
			KisDividend.create(
				samsungTickerSymbol,
				Money.from(samsungDividend),
				LocalDate.of(2023, 9, 30),
				LocalDate.of(2023, 11, 20)
			),
			KisDividend.create(
				samsungTickerSymbol,
				Money.from(samsungDividend),
				LocalDate.of(2023, 12, 31),
				LocalDate.of(2024, 4, 19)
			),
			KisDividend.create(
				samsungTickerSymbol,
				Money.from(samsungDividend),
				LocalDate.of(2024, 3, 31),
				LocalDate.of(2024, 5, 17) // 기존 데이터에서 새로운 현금 배당 지급일이 할당된 경우
			),
			KisDividend.create(
				samsungTickerSymbol,
				Money.from(samsungDividend),
				LocalDate.of(2024, 6, 30), // 새로운 배당 기준일이 생긴 경우
				null
			),
			KisDividend.create(
				kakaoTickerSymbol,
				Money.from(kakaoDividend),
				LocalDate.of(2024, 2, 29),
				null
			)
		));
		// when
		stockDividendService.refreshStockDividend(LocalDate.of(2024, 4, 17));

		// then
		List<StockDividend> stockDividends = stockDividendRepository.findAllStockDividends();
		log.debug("stockDividends: {}", stockDividends);
		assertThat(stockDividends)
			.hasSize(7)
			.map(StockDividend::parse)
			.containsExactlyInAnyOrder(
				"005930:361:2023-03-31:2023-03-30:2023-05-17",
				"005930:361:2023-06-30:2023-06-29:2023-08-16",
				"005930:361:2023-09-30:2023-09-27:2023-11-20",
				"005930:361:2023-12-31:2023-12-28:2024-04-19",
				"005930:361:2024-03-31:2024-03-29:2024-05-17",
				"005930:361:2024-06-30:2024-06-28:null",
				"035720:61:2024-02-29:2024-02-28:null"
			);
	}

	@DisplayName("S3 저장소에 배당 일정 csv 파일을 작성한다")
	@Test
	void writeDividendCsvToS3() {
		// given
		stockRepository.save(createSamsungStock());
		stockRepository.save(createKakaoStock());
		stockDividendService.initializeStockDividend();
		// when
		stockDividendService.writeDividendCsvToS3();
		// then
		List<Dividend> dividends = amazonS3DividendService.fetchDividend();
		assertThat(dividends)
			.hasSize(6);
	}

	private Stock createSamsungStock() {
		return createStock(
			"삼성전자보통주",
			"005930",
			"SamsungElectronics",
			"KR7005930003",
			"전기전자",
			Market.KOSPI
		);
	}

	private Stock createKakaoStock() {
		return createStock(
			"카카오보통주",
			"035720",
			"Kakao",
			"KR7035720002",
			"서비스업",
			Market.KOSPI
		);
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

	private Stock createStock(String companyName, String tickerSymbol, String companyNameEng, String stockCode,
		String sector, Market market) {
		return Stock.builder()
			.companyName(companyName)
			.tickerSymbol(tickerSymbol)
			.companyNameEng(companyNameEng)
			.stockCode(stockCode)
			.sector(sector)
			.market(market)
			.build();
	}

	private List<StockDividend> createSamsungDividends(Stock stock) {
		return List.of(
			createStockDividend(
				Money.from(361L),
				LocalDate.of(2022, 3, 31),
				LocalDate.of(2022, 3, 30),
				LocalDate.of(2022, 5, 17),
				stock
			),
			createStockDividend(
				Money.from(361L),
				LocalDate.of(2022, 6, 30),
				LocalDate.of(2022, 6, 29),
				LocalDate.of(2022, 8, 16),
				stock
			),
			createStockDividend(
				Money.from(361L),
				LocalDate.of(2022, 9, 30),
				LocalDate.of(2022, 9, 29),
				LocalDate.of(2022, 11, 15),
				stock
			),
			createStockDividend(
				Money.from(361L),
				LocalDate.of(2022, 12, 31),
				LocalDate.of(2022, 12, 30),
				LocalDate.of(2023, 4, 14),
				stock),
			createStockDividend(
				Money.from(361L),
				LocalDate.of(2023, 3, 31),
				LocalDate.of(2023, 3, 30),
				LocalDate.of(2023, 5, 17),
				stock),
			createStockDividend(
				Money.from(361L),
				LocalDate.of(2023, 6, 30),
				LocalDate.of(2023, 6, 29),
				LocalDate.of(2023, 8, 16),
				stock),
			createStockDividend(
				Money.from(361L),
				LocalDate.of(2023, 9, 30),
				LocalDate.of(2023, 9, 27),
				LocalDate.of(2023, 11, 20),
				stock),
			createStockDividend(
				Money.from(361L),
				LocalDate.of(2023, 12, 31),
				LocalDate.of(2023, 12, 28),
				LocalDate.of(2024, 4, 19),
				stock),
			createStockDividend(
				Money.from(361L),
				LocalDate.of(2024, 3, 31),
				LocalDate.of(2024, 3, 29),
				null,
				stock)
		);
	}

	private List<StockDividend> createKakaoDividends(Stock stock) {
		return List.of(
			createStockDividend(
				Money.from(61L),
				LocalDate.of(2022, 12, 31),
				LocalDate.of(2022, 12, 30),
				LocalDate.of(2023, 4, 25),
				stock
			),
			createStockDividend(
				Money.from(61L),
				LocalDate.of(2024, 2, 29),
				LocalDate.of(2024, 2, 28),
				null,
				stock
			)
		);
	}

	private StockDividend createStockDividend(Money dividend, LocalDate recordDate, LocalDate exDividendDate,
		LocalDate paymentDate,
		Stock stock) {
		return StockDividend.builder()
			.dividend(dividend)
			.exDividendDate(exDividendDate)
			.recordDate(recordDate)
			.paymentDate(paymentDate)
			.stock(stock)
			.build();
	}

	private String createAuthorization() {
		return "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0b2tlbiIsImF1ZCI6Ijg5MjBlNjM2LTNkYmItNGU5MS04ZGJmLWJmZDU5ZmI2YjAwYiIsImlzcyI6InVub2d3IiwiZXhwIjoxNzAzNTcwOTA0LCJpYXQiOjE3MDM0ODQ1MDQsImp0aSI6IlBTRGc4WlVJd041eVl5ZkR6bnA0TDM2Z2xhRUpic2RJNGd6biJ9.z8dh9rlOyPq_ukm9KeCz0tkKI2QaHEe07LhXTcKQBrcP1-uiW3dwAwdknpAojJZ7aUWLUaQQn0HmjTCttjSJaA";
	}
}
