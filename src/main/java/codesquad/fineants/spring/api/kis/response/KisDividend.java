package codesquad.fineants.spring.api.kis.response;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.stock_dividend.StockDividend;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@JsonDeserialize(using = KisDividend.KissDividendDeserializer.class)
public class KisDividend implements Comparable<KisDividend> {
	private String tickerSymbol;
	private Money dividend;
	private LocalDate recordDate;
	private LocalDate paymentDate;

	public static KisDividend create(String tickerSymbol, Money dividend, LocalDate recordDate, LocalDate paymentDate) {
		return new KisDividend(tickerSymbol, dividend, recordDate, paymentDate);
	}

	public StockDividend toEntity(Stock stock) {
		return StockDividend.create(dividend, recordDate, paymentDate, stock);
	}

	public boolean containsFrom(Map<String, Stock> stockMap) {
		return stockMap.containsKey(tickerSymbol);
	}

	public boolean matchTickerSymbolAndRecordDateFrom(Map<String, Stock> stockMap) {
		if (!stockMap.containsKey(tickerSymbol)) {
			return false;
		}
		return stockMap.get(tickerSymbol).matchByTickerSymbolAndRecordDate(tickerSymbol, recordDate);
	}

	public Optional<StockDividend> getStockDividendByTickerSymbolAndRecordDateFrom(Map<String, Stock> stockMap) {
		if (!stockMap.containsKey(tickerSymbol)) {
			return Optional.empty();
		}
		return stockMap.get(tickerSymbol).getStockDividendBy(tickerSymbol, recordDate);
	}

	/**
	 * 1차 정렬 : tickerSymbol asc
	 * 2차 정렬 : recordDate asc
	 * @param k the object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
	 */
	@Override
	public int compareTo(KisDividend k) {
		return this.tickerSymbol.compareTo(k.tickerSymbol) == 0 ?
			this.recordDate.compareTo(k.recordDate) : this.tickerSymbol.compareTo(k.tickerSymbol);
	}

	public Stock getStockBy(Map<String, Stock> stockMap) {
		if (!stockMap.containsKey(tickerSymbol)) {
			return null;
		}
		return stockMap.get(tickerSymbol);
	}

	static class KissDividendDeserializer extends JsonDeserializer<KisDividend> {

		private static final DateTimeFormatter RECORD_DATE_DTF = DateTimeFormatter.BASIC_ISO_DATE;
		private static final DateTimeFormatter OTHER_DATE_DTF = DateTimeFormatter.ofPattern("yyyy/MM/dd");

		@Override
		public KisDividend deserialize(JsonParser p, DeserializationContext ctxt) throws
			IOException,
			JacksonException {
			TreeNode rootNode = p.readValueAsTree();
			KisDividend kisDividend = new KisDividend();

			JsonNode outputNode = (JsonNode)rootNode;
			JsonNode tickerSymbol = outputNode.get("sht_cd"); // 티커 심볼
			if (tickerSymbol != null) {
				kisDividend.tickerSymbol = tickerSymbol.asText();
			}

			JsonNode dividend = outputNode.get("per_sto_divi_amt");// 배당금
			if (dividend != null) {
				kisDividend.dividend = Money.from(dividend.asLong());
			}

			JsonNode recordDate = outputNode.get("record_date"); // 배정기준일
			if (recordDate != null) {
				kisDividend.recordDate = LocalDate.parse(recordDate.asText(), RECORD_DATE_DTF);
			}

			JsonNode paymentDate = outputNode.get("divi_pay_dt"); // 현금 지급일
			if (paymentDate != null && !paymentDate.asText().isBlank()) {
				kisDividend.paymentDate = LocalDate.parse(paymentDate.asText(), OTHER_DATE_DTF);
			}
			return kisDividend;
		}
	}
}
