package co.fineants.api.domain.stock.domain.entity;

import static co.fineants.api.domain.stock.service.StockCsvReader.*;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import co.fineants.api.domain.BaseEntity;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.RateDivision;
import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.kis.repository.ClosingPriceRepository;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.kis.repository.PriceRepository;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import co.fineants.api.domain.stock.converter.MarketConverter;
import co.fineants.api.global.common.time.LocalDateTimeService;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString(exclude = "stockDividends")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "stockCode", callSuper = false)
@Entity
public class Stock extends BaseEntity {

	@Id
	private String tickerSymbol;
	private String companyName;
	private String companyNameEng;
	private String stockCode;
	private String sector;
	@Convert(converter = MarketConverter.class)
	private Market market;
	private boolean isDeleted;

	@OneToMany(mappedBy = "stock", fetch = FetchType.LAZY)
	private final List<StockDividend> stockDividends = new ArrayList<>();

	public static final String TICKER_PREFIX = "TS";

	private Stock(String tickerSymbol, String companyName, String companyNameEng, String stockCode, String sector,
		Market market) {
		this.tickerSymbol = tickerSymbol;
		this.companyName = companyName;
		this.companyNameEng = companyNameEng;
		this.stockCode = stockCode;
		this.sector = sector;
		this.market = market;
		this.isDeleted = false;
	}

	public static Stock of(String tickerSymbol, String companyName, String companyNameEng, String stockCode,
		String sector, Market market) {
		return new Stock(tickerSymbol, companyName, companyNameEng, stockCode, sector, market);
	}

	public void addStockDividend(StockDividend stockDividend) {
		if (!stockDividends.contains(stockDividend)) {
			stockDividends.add(stockDividend);
		}
	}

	public List<StockDividend> getCurrentMonthDividends(LocalDateTimeService localDateTimeService) {
		LocalDate today = localDateTimeService.getLocalDateWithNow();
		return stockDividends.stream()
			.filter(dividend -> dividend.isCurrentMonthPaymentDate(today))
			.toList();
	}

	public List<StockDividend> getCurrentYearDividends(LocalDateTimeService localDateTimeService) {
		LocalDate today = localDateTimeService.getLocalDateWithNow();
		return stockDividends.stream()
			.filter(dividend -> dividend.isCurrentYearPaymentDate(today))
			.toList();
	}

	public Map<Month, Expression> createMonthlyDividends(List<PurchaseHistory> purchaseHistories,
		LocalDate currentLocalDate) {
		Map<Month, Expression> result = initMonthlyDividendMap();
		List<StockDividend> currentYearStockDividends = getStockDividendsWithCurrentYearRecordDateBy(currentLocalDate);

		for (StockDividend stockDividend : currentYearStockDividends) {
			for (PurchaseHistory purchaseHistory : purchaseHistories) {
				if (stockDividend.canReceiveDividendOn(purchaseHistory)) {
					Month paymentMonth = stockDividend.getMonthByPaymentDate();
					Expression dividendSum = stockDividend.calculateDividendSum(purchaseHistory.getNumShares());
					Expression sum = result.getOrDefault(paymentMonth, Money.zero()).plus(dividendSum);
					result.put(paymentMonth, sum);
				}
			}
		}
		return result;
	}

	@NotNull
	private List<StockDividend> getStockDividendsWithCurrentYearRecordDateBy(LocalDate currentLocalDate) {
		return stockDividends.stream()
			.filter(stockDividend -> stockDividend.isCurrentYearRecordDate(currentLocalDate))
			.toList();
	}

	public Map<Month, Expression> createMonthlyExpectedDividends(List<PurchaseHistory> purchaseHistories,
		LocalDate currentLocalDate) {
		Map<Month, Expression> result = initMonthlyDividendMap();
		// 0. 현재년도에 해당하는 배당금 정보를 필터링하여 별도 저장합니다.
		List<StockDividend> currentYearStockDividends = getStockDividendsWithCurrentYearRecordDateBy(currentLocalDate);

		// 1. 배당금 데이터 중에서 현금지급일자가 작년도에 해당하는 배당금 정보를 필터링합니다.
		// 2. 1단계에서 필터링한 배당금 데이터들중 0단계에서 별도 저장한 현재년도의 분기 배당금과 중복되는 배당금 정보를 필터링합니다.
		LocalDate lastYearLocalDate = currentLocalDate.minusYears(1L);
		stockDividends.stream()
			.filter(stockDividend -> stockDividend.isLastYearPaymentDate(lastYearLocalDate))
			.filter(stockDividend -> !stockDividend.isDuplicatedRecordDate(currentYearStockDividends))
			.forEach(stockDividend -> {
				// 3. 필터링한 배당금 정보들을 이용하여 배당금을 계산합니다.
				for (PurchaseHistory purchaseHistory : purchaseHistories) {
					Month paymentMonth = stockDividend.getMonthByPaymentDate();
					Expression dividendSum = stockDividend.calculateDividendSum(purchaseHistory.getNumShares());
					result.put(paymentMonth, result.getOrDefault(paymentMonth, Money.zero()).plus(dividendSum));
				}
			});
		return result;
	}

	@NotNull
	private static Map<Month, Expression> initMonthlyDividendMap() {
		Map<Month, Expression> result = new EnumMap<>(Month.class);
		for (Month month : Month.values()) {
			result.put(month, Money.zero());
		}
		return result;
	}

	public Expression getAnnualDividend(LocalDateTimeService localDateTimeService) {
		return stockDividends.stream()
			.filter(dividend -> dividend.isCurrentYearPaymentDate(localDateTimeService.getLocalDateWithNow()))
			.map(StockDividend::getDividend)
			.map(Expression.class::cast)
			.reduce(Money.zero(), Expression::plus);
	}

	public RateDivision getAnnualDividendYield(CurrentPriceRedisRepository manager,
		LocalDateTimeService localDateTimeService) {
		Expression dividends = stockDividends.stream()
			.filter(dividend -> dividend.isPaymentInCurrentYear(localDateTimeService.getLocalDateWithNow()))
			.map(StockDividend::getDividend)
			.map(Expression.class::cast)
			.reduce(Money.zero(), Expression::plus);
		return dividends.divide(getCurrentPrice(manager));
	}

	public Expression getDailyChange(CurrentPriceRedisRepository currentPriceRedisRepository,
		ClosingPriceRepository closingPriceRepository) {
		Expression currentPrice = getCurrentPrice(currentPriceRedisRepository);
		Expression closingPrice = getClosingPrice(closingPriceRepository);
		return currentPrice.minus(closingPrice);
	}

	public RateDivision getDailyChangeRate(CurrentPriceRedisRepository currentPriceRedisRepository,
		ClosingPriceRepository closingPriceRepository) {
		Money currentPrice = currentPriceRedisRepository.fetchPriceBy(tickerSymbol).orElse(null);
		Money lastDayClosingPrice = closingPriceRepository.fetchPrice(tickerSymbol).orElse(null);
		if (currentPrice == null || lastDayClosingPrice == null) {
			return null;
		}
		return currentPrice.minus(lastDayClosingPrice).divide(lastDayClosingPrice);
	}

	public Expression getCurrentPrice(PriceRepository manager) {
		return manager.fetchPriceBy(tickerSymbol).orElseGet(Money::zero);
	}

	public Expression getClosingPrice(ClosingPriceRepository manager) {
		return manager.fetchPrice(tickerSymbol).orElseGet(Money::zero);
	}

	public List<Month> getDividendMonths(LocalDateTimeService localDateTimeService) {
		return stockDividends.stream()
			.filter(dividend -> dividend.isCurrentYearPaymentDate(localDateTimeService.getLocalDateWithNow()))
			.map(StockDividend::getMonthByPaymentDate)
			.toList();
	}
	// ticker 및 recordDate 기준으로 KisDividend가 매치되어 있는지 확인

	public boolean matchByTickerSymbolAndRecordDate(String tickerSymbol, LocalDate recordDate) {
		if (!this.tickerSymbol.equals(tickerSymbol)) {
			return false;
		}
		return stockDividends.stream()
			.anyMatch(s -> s.equalRecordDate(recordDate));
	}

	public Optional<StockDividend> getStockDividendBy(String tickerSymbol, LocalDate recordDate) {
		if (!this.tickerSymbol.equals(tickerSymbol)) {
			return Optional.empty();
		}
		return stockDividends.stream()
			.filter(s -> s.equalRecordDate(recordDate))
			.findAny();
	}

	public List<StockDividend> getStockDividendNotInRange(LocalDate from, LocalDate to) {
		return stockDividends.stream()
			.filter(stockDividend -> !stockDividend.hasInRangeForRecordDate(from, to))
			.toList();
	}

	public String toCsvLineString() {
		String ticker = String.format("%s%s", TICKER_PREFIX, tickerSymbol);
		return String.join(CSV_DELIMITER,
			stockCode,
			ticker,
			companyName,
			companyNameEng,
			sector,
			market.name());
	}

	public List<StockDividend> getStockDividends() {
		return Collections.unmodifiableList(stockDividends);
	}

	public Optional<Money> fetchPrice(PriceRepository repository) {
		return repository.fetchPriceBy(tickerSymbol);
	}

	public void savePrice(PriceRepository repository, long price) {
		repository.savePrice(tickerSymbol, price);
	}
}
