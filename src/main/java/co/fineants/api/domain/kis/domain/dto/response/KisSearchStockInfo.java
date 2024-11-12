package co.fineants.api.domain.kis.domain.dto.response;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import co.fineants.api.domain.stock.domain.entity.Market;
import co.fineants.api.domain.stock.domain.entity.Stock;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;

@JsonDeserialize(using = KisSearchStockInfo.KisSearchStockInfoDeserializer.class)
@EqualsAndHashCode
public class KisSearchStockInfo {
	@JsonProperty("stockCode")
	private String stockCode;            // 표준 상품 번호
	@JsonProperty("tickerSymbol")
	private String tickerSymbol;         // 상품 번호
	@JsonProperty("companyName")
	private String companyName;          // 상품명
	@JsonProperty("companyEngName")
	private String companyEngName;       // 상품 영문명
	@JsonProperty("marketIdCode")
	private String marketIdCode;         // 시장 ID 코드
	@JsonProperty("sector")
	private String sector;               // 지수 업종 소분류 코드명
	@JsonProperty("delistedDate")
	private LocalDate delistedDate;      // 상장 폐지 일자

	private KisSearchStockInfo() {

	}

	@Builder(access = AccessLevel.PRIVATE)
	private KisSearchStockInfo(String stockCode, String tickerSymbol, String companyName, String companyEngName,
		String marketIdCode, String sector, LocalDate delistedDate) {
		this.stockCode = stockCode;
		this.tickerSymbol = tickerSymbol;
		this.companyName = companyName;
		this.companyEngName = companyEngName;
		this.marketIdCode = marketIdCode;
		this.sector = sector;
		this.delistedDate = delistedDate;
	}

	// 상장된 종목
	public static KisSearchStockInfo listedStock(String stockCode, String tickerSymbol, String companyName,
		String companyEngName, String marketIdCode, String sector) {
		return new KisSearchStockInfo(stockCode, tickerSymbol, companyName, companyEngName, marketIdCode, sector, null);
	}

	// 상장 폐지된 종목
	public static KisSearchStockInfo delistedStock(String stockCode, String tickerSymbol, String companyName,
		String companyEngName, String marketIdCode, String sector, LocalDate delistedDate) {
		return new KisSearchStockInfo(stockCode, tickerSymbol, companyName, companyEngName, marketIdCode, sector,
			delistedDate);
	}

	public Stock toEntity() {
		Market market = Market.valueBy(marketIdCode);
		return Stock.of(
			tickerSymbol,
			companyName,
			companyEngName,
			stockCode,
			sector,
			market
		);
	}

	/**
	 * 상장 폐지 여부
	 *
	 * @return true=폐지, false=상장
	 */
	public boolean isDelisted() {
		return delistedDate != null;
	}

	@Override
	public String toString() {
		return String.format(
			"stockCode=%s, tickerSymbol=%s, companyName=%s, companyEngName=%s, marketIdCode=%s, sector=%s, delistedDate=%s",
			stockCode, tickerSymbol, companyName, companyEngName, marketIdCode, sector, delistedDate);
	}

	static class KisSearchStockInfoDeserializer extends JsonDeserializer<KisSearchStockInfo> {
		@Override
		public KisSearchStockInfo deserialize(JsonParser parser, DeserializationContext context) throws
			IOException {
			TreeNode rootNode = parser.readValueAsTree();
			TreeNode treeNode = rootNode.get("output");
			KisSearchStockInfo kisSearchStockInfo = new KisSearchStockInfo();

			JsonNode outputNode = (JsonNode)treeNode;
			JsonNode stdPdno = outputNode.get("std_pdno"); // 표준 상품 번호
			if (stdPdno != null) {
				kisSearchStockInfo.stockCode = stdPdno.asText();
			}

			JsonNode pdno = outputNode.get("pdno"); // 상품 번호
			if (pdno != null) {
				// 상품번호의 마지막 6자리 추출
				kisSearchStockInfo.tickerSymbol = pdno.asText().substring(pdno.asText().length() - 6);
			}

			JsonNode prdtName = outputNode.get("prdt_name"); // 상품명
			if (prdtName != null) {
				kisSearchStockInfo.companyName = prdtName.asText();
			}

			JsonNode prdtEngName = outputNode.get("prdt_eng_name"); // 상품 영문명
			if (prdtEngName != null) {
				kisSearchStockInfo.companyEngName = prdtEngName.asText();
			}

			JsonNode mketIdCd = outputNode.get("mket_id_cd"); // 시장 ID 코드
			if (mketIdCd != null) {
				kisSearchStockInfo.marketIdCode = mketIdCd.asText();
			}

			JsonNode idxBztpSclsCdName = outputNode.get("idx_bztp_scls_cd_name"); // 지수 업종 소분류 코드명
			if (idxBztpSclsCdName != null) {
				kisSearchStockInfo.sector = idxBztpSclsCdName.asText();
			}

			JsonNode lstgAbolDt = outputNode.get("lstg_abol_dt"); // 상장 폐지 일자
			if (lstgAbolDt != null && !lstgAbolDt.asText().isBlank()) {
				kisSearchStockInfo.delistedDate = LocalDate.parse(lstgAbolDt.asText(),
					DateTimeFormatter.BASIC_ISO_DATE);
			}
			return kisSearchStockInfo;
		}
	}
}
