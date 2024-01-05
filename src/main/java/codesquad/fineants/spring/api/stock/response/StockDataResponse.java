package codesquad.fineants.spring.api.stock.response;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
	}
}
