package codesquad.fineants.domain.stock.domain.dto.response;

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
public class StockSectorResponse {
	@JsonProperty("block1")
	private Set<SectorInfo> sectorInfos;
	@JsonProperty("CURRENT_DATETIME")
	private String dateTime;

	@Getter
	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	@Builder(access = AccessLevel.PRIVATE)
	@EqualsAndHashCode
	@JsonIgnoreProperties(value = {"ISU_ABBRV", "TDD_CLSPRC", "CMPPREVDD_PRC", "FLUC_RT", "MKTCAP", "FLUC_TP_CD"})
	public static class SectorInfo {
		@JsonProperty("ISU_SRT_CD")
		private String tickerSymbol;
		@JsonProperty("IDX_IND_NM")
		private String sector;
		@JsonProperty("MKT_TP_NM")
		private String market;

		public static SectorInfo of(String tickerSymbol, String sector, String market) {
			return SectorInfo.builder()
				.tickerSymbol(tickerSymbol)
				.sector(sector)
				.market(market)
				.build();
		}

		public static SectorInfo empty() {
			return SectorInfo.builder().build();
		}
	}
}
