package co.fineants.api.domain.kis.domain.dto.response;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

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
	private final String stockCode;            // 표준 상품 번호
	@JsonProperty("tickerSymbol")
	private final String tickerSymbol;         // 상품 번호
	@JsonProperty("companyName")
	private final String companyName;          // 상품명
	@JsonProperty("companyEngName")
	private final String companyEngName;       // 상품 영문명
	@JsonProperty("marketIdCode")
	private final String marketIdCode;         // 시장 ID 코드
	@JsonProperty("majorSector")
	private final String majorSector;          // 지수 업종 대분류 코드명
	@JsonProperty("midSector")
	private final String midSector;               // 지수 업종 중분류 코드명
	@JsonProperty("subSector")
	private final String subSector;            // 지수 업종 소분류 코드명
	@JsonProperty("delistedDate")
	private final LocalDate delistedDate;      // 상장 폐지 일자

	@Builder(access = AccessLevel.PRIVATE)
	private KisSearchStockInfo(String stockCode, String tickerSymbol, String companyName, String companyEngName,
		String marketIdCode, String majorSector, String midSector, String subSector, LocalDate delistedDate) {
		this.stockCode = stockCode;
		this.tickerSymbol = tickerSymbol;
		this.companyName = companyName;
		this.companyEngName = companyEngName;
		this.marketIdCode = marketIdCode;
		this.majorSector = majorSector;
		this.midSector = midSector;
		this.subSector = subSector;
		this.delistedDate = delistedDate;
	}

	// 상장된 종목
	public static KisSearchStockInfo listedStock(String stockCode, String tickerSymbol, String companyName,
		String companyEngName, String marketIdCode, String majorSector, String midSector, String subSector) {
		return KisSearchStockInfo.builder()
			.stockCode(stockCode)
			.tickerSymbol(tickerSymbol)
			.companyName(companyName)
			.companyEngName(companyEngName)
			.marketIdCode(marketIdCode)
			.majorSector(majorSector)
			.midSector(midSector)
			.subSector(subSector)
			.delistedDate(null)
			.build();
	}

	// 상장 폐지된 종목
	public static KisSearchStockInfo delistedStock(String stockCode, String tickerSymbol, String companyName,
		String companyEngName, String marketIdCode, String majorSector, String midSector, String subSector,
		LocalDate delistedDate) {
		return KisSearchStockInfo.builder()
			.stockCode(stockCode)
			.tickerSymbol(tickerSymbol)
			.companyName(companyName)
			.companyEngName(companyEngName)
			.marketIdCode(marketIdCode)
			.majorSector(majorSector)
			.midSector(midSector)
			.subSector(subSector)
			.delistedDate(delistedDate)
			.build();
	}

	public Stock toEntity() {
		Market market = Market.valueBy(marketIdCode);
		String sector = Optional.ofNullable(subSector)
			.filter(Strings::isNotBlank)
			.orElse("기타");
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
			"stockCode=%s, tickerSymbol=%s, companyName=%s, companyEngName=%s, marketIdCode=%s, "
				+ "majorSector=%s, midSector=%s, subSector=%s, delistedDate=%s",
			stockCode, tickerSymbol, companyName, companyEngName, marketIdCode, majorSector, midSector, subSector,
			delistedDate);
	}

	static class KisSearchStockInfoDeserializer extends JsonDeserializer<KisSearchStockInfo> {
		@Override
		public KisSearchStockInfo deserialize(JsonParser parser, DeserializationContext context) throws
			IOException {
			TreeNode rootNode = parser.readValueAsTree();
			TreeNode treeNode = rootNode.get("output");
			JsonNode outputNode = (JsonNode)treeNode;

			return KisSearchStockInfo.builder()
				.stockCode(outputNode.get("std_pdno").asText()) // 표준 상품 번호
				.tickerSymbol(outputNode.get("pdno")
					.asText()
					.substring(outputNode.get("pdno").asText().length() - 6)) // 상품 번호, 마지막 6자리 추출
				.companyName(outputNode.get("prdt_name").asText())
				.companyEngName(outputNode.get("prdt_eng_name").asText()) // 상품 영문명
				.marketIdCode(outputNode.get("mket_id_cd").asText()) // 시장 ID 코드
				.majorSector(outputNode.get("idx_bztp_lcls_cd_name").asText()) // 지수 업종 대분류 코드명
				.midSector(outputNode.get("idx_bztp_mcls_cd_name").asText()) // 지수 업종 중분류 코드명
				.subSector(outputNode.get("idx_bztp_scls_cd_name").asText()) // 지수 업종 소분류 코드명
				.delistedDate(parseDelistedDate(outputNode).orElse(null)) // 상장 폐지 일자
				.build();
		}

		@NotNull
		private Optional<LocalDate> parseDelistedDate(JsonNode outputNode) {
			String text = outputNode.get("lstg_abol_dt").asText();
			if (Strings.isBlank(text)) {
				return Optional.empty();
			}
			return Optional.of(LocalDate.parse(text, DateTimeFormatter.BASIC_ISO_DATE));
		}
	}
}
