package codesquad.fineants.domain.stock.domain.dto.response;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import codesquad.fineants.domain.stock.domain.entity.Market;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StockDataResponse {
	@JsonProperty("OutBlock_1")
	private Set<StockInfo> stockInfos;
	@JsonProperty("CURRENT_DATETIME")
	private String dateTime;

	@Getter
	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	@JsonIgnoreProperties(value = {"SECUGRP_NM", "ISU_ABBRV", "LIST_DD", "SECT_TP_NM",
		"KIND_STKCERT_TP_NM", "PARVAL", "LIST_SHRS"})
	@EqualsAndHashCode
	public static class StockInfo {
		@JsonProperty("ISU_CD")
		private String stockCode;
		@JsonProperty("ISU_SRT_CD")
		private String tickerSymbol;
		@JsonProperty("ISU_NM")
		private String companyName;
		@JsonProperty("ISU_ENG_NM")
		private String companyNameEng;
		@JsonProperty("MKT_TP_NM")
		private String market;

		@Builder(access = AccessLevel.PRIVATE)
		public StockInfo(String stockCode, String tickerSymbol, String companyName, String companyNameEng,
			String market) {
			this.stockCode = stockCode;
			this.tickerSymbol = tickerSymbol;
			this.companyName = companyName;
			this.companyNameEng = companyNameEng;
			this.market = market;
		}

		public static StockInfo of(String stockCode, String tickerSymbol, String companyName, String companyNameEng,
			String market) {
			return StockInfo.builder()
				.stockCode(stockCode)
				.tickerSymbol(tickerSymbol)
				.companyName(companyName)
				.companyNameEng(companyNameEng)
				.market(market)
				.build();
		}

		public static StockInfo from(Stock stock) {
			return StockInfo.builder()
				.stockCode(stock.getStockCode())
				.tickerSymbol(stock.getTickerSymbol())
				.companyName(stock.getCompanyName())
				.companyNameEng(stock.getCompanyNameEng())
				.market(stock.getMarket().name())
				.build();
		}

		public Stock toEntity() {
			return Stock.of(
				tickerSymbol,
				companyName,
				companyNameEng,
				stockCode,
				null,
				Market.ofMarket(market)
			);
		}
	}

	@Getter
	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	@Builder(access = AccessLevel.PRIVATE)
	@EqualsAndHashCode(of = "tickerSymbol")
	@ToString
	public static class StockIntegrationInfo {
		private String tickerSymbol;
		private String companyName;
		private String companyNameEng;
		private String stockCode;
		private String sector;
		private Market market;

		public static StockIntegrationInfo from(StockInfo stockInfo, String sector) {
			return StockIntegrationInfo.builder()
				.tickerSymbol(stockInfo.getTickerSymbol())
				.companyName(stockInfo.getCompanyName())
				.companyNameEng(stockInfo.getCompanyNameEng())
				.stockCode(stockInfo.getStockCode())
				.sector(sector)
				.market(Market.ofMarket(stockInfo.getMarket()))
				.build();
		}

		public static StockIntegrationInfo from(Stock stock) {
			return StockIntegrationInfo.builder()
				.tickerSymbol(stock.getTickerSymbol())
				.companyName(stock.getCompanyName())
				.companyNameEng(stock.getCompanyNameEng())
				.stockCode(stock.getStockCode())
				.sector(stock.getSector())
				.market(stock.getMarket())
				.build();
		}

		public static StockIntegrationInfo create(String tickerSymbol, String companyName, String companyNameEng,
			String stockCode, String sector, Market market) {
			return new StockIntegrationInfo(tickerSymbol, companyName, companyNameEng, stockCode, sector, market);
		}

		public Stock toEntity() {
			return Stock.of(tickerSymbol, companyName, companyNameEng, stockCode, sector, market);
		}
	}
}
