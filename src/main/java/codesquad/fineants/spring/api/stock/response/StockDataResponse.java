package codesquad.fineants.spring.api.stock.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Setter
public class StockDataResponse {
	@JsonProperty("OutBlock_1")
	private List<StockInfo> stockInfos;
	@JsonProperty("CURRENT_DATETIME")
	private String dateTime;

	@Getter
	@NoArgsConstructor
	@Setter
	public static class StockInfo {
		@JsonProperty("ISU_CD")
		private String stockCode;

		@JsonProperty("ISU_SRT_CD")
		private String tickerSymbol;

		@JsonProperty("ISU_NM")
		private String companyName;

		@JsonProperty("ISU_ABBRV")
		private String isuAbbrv;

		@JsonProperty("ISU_ENG_NM")
		private String companyNameEng;

		@JsonProperty("LIST_DD")
		private String listDd;

		@JsonProperty("MKT_TP_NM")
		private String market;

		@JsonProperty("SECUGRP_NM")
		private String secugrpNm;

		@JsonProperty("SECT_TP_NM")
		private String sectTpNm;

		@JsonProperty("KIND_STKCERT_TP_NM")
		private String kindStkcertTpNm;

		@JsonProperty("PARVAL")
		private String parval;

		@JsonProperty("LIST_SHRS")
		private String listShrs;
	}
}
