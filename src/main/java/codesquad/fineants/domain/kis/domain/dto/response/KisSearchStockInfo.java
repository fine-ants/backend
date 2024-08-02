package codesquad.fineants.domain.kis.domain.dto.response;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import codesquad.fineants.domain.stock.domain.entity.Market;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@JsonSerialize(using = KisSearchStockInfo.KisSearchStockInfoSerializer.class)
@JsonDeserialize(using = KisSearchStockInfo.KisSearchStockInfoDeserializer.class)
public class KisSearchStockInfo {
	private String stockCode;            // 표준 상품 번호
	private String tickerSymbol;         // 상품 번호
	private String companyName;          // 상품명
	private String companyEngName;       // 상품 영문명
	private String marketIdCode;         // 시장 ID 코드
	private String sector;               // 지수 업종 소분류 코드명
	private LocalDate delistedDate;      // 상장 폐지 일자

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
		Market market = Market.valueByMarketIdCode(marketIdCode);
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
	 * @return true=폐지, false=상장
	 */
	public boolean isDelisted() {
		return delistedDate != null;
	}

	public boolean isListed() {
		return delistedDate == null;
	}

	static class KisSearchStockInfoSerializer extends JsonSerializer<KisSearchStockInfo> {
		@Override
		public void serialize(KisSearchStockInfo value, JsonGenerator gen, SerializerProvider serializers) throws
			IOException {
			gen.writeStartObject();
			gen.writeStringField("std_pdno", value.stockCode);
			gen.writeStringField("pdno", value.tickerSymbol);
			gen.writeStringField("prdt_name", value.companyName);
			gen.writeStringField("prdt_eng_name", value.companyEngName);
			gen.writeStringField("mket_id_cd", value.marketIdCode);
			gen.writeStringField("idx_bztp_scls_cd_name", value.sector);
			gen.writeEndObject();
		}
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
