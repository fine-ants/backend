package codesquad.fineants.domain.kis.domain.dto.response;

import java.io.IOException;

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
	private String stdPdno;              // 표준 상품 번호
	private String pdno;                 // 상품 번호
	private String prdtName;             // 상품명
	private String prdtEngName;          // 상품 영문명
	private String mketIdCd;             // 시장 ID 코드
	private String kospi200ItemYn;       // 코스피200종목 여부
	private String idxBztpSclsCdName;    // 지수 업종 소분류 코드명

	public static KisSearchStockInfo create(String stdPdno, String pdno, String prdtName, String prdtEngName,
		String mketIdCd, String kospi200ItemYn, String idxBztpSclsCdName) {
		return new KisSearchStockInfo(stdPdno, pdno, prdtName, prdtEngName, mketIdCd, kospi200ItemYn,
			idxBztpSclsCdName);
	}

	public Stock toEntity() {
		Market market = Market.valueOf(kospi200ItemYn, mketIdCd);
		return Stock.of(
			pdno,
			prdtName,
			prdtEngName,
			stdPdno,
			idxBztpSclsCdName,
			market
		);
	}

	static class KisSearchStockInfoSerializer extends JsonSerializer<KisSearchStockInfo> {
		@Override
		public void serialize(KisSearchStockInfo value, JsonGenerator gen, SerializerProvider serializers) throws
			IOException {
			gen.writeStartObject();
			gen.writeStringField("std_pdno", value.stdPdno);
			gen.writeStringField("pdno", value.pdno);
			gen.writeStringField("prdt_name", value.prdtName);
			gen.writeStringField("prdt_eng_name", value.prdtEngName);
			gen.writeStringField("mket_id_cd", value.mketIdCd);
			gen.writeStringField("kospi200_item_yn", value.kospi200ItemYn);
			gen.writeStringField("idx_bztp_scls_cd_name", value.idxBztpSclsCdName);
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
				kisSearchStockInfo.stdPdno = stdPdno.asText();
			}

			JsonNode pdno = outputNode.get("pdno"); // 상품 번호
			if (pdno != null) {
				// 상품번호의 마지막 6자리 추출
				kisSearchStockInfo.pdno = pdno.asText().substring(pdno.asText().length() - 6);
			}

			JsonNode prdtName = outputNode.get("prdt_name"); // 상품명
			if (prdtName != null) {
				kisSearchStockInfo.prdtName = prdtName.asText();
			}

			JsonNode prdtEngName = outputNode.get("prdt_eng_name"); // 상품 영문명
			if (prdtEngName != null) {
				kisSearchStockInfo.prdtEngName = prdtEngName.asText();
			}

			JsonNode mketIdCd = outputNode.get("mket_id_cd"); // 시장 ID 코드
			if (mketIdCd != null) {
				kisSearchStockInfo.mketIdCd = mketIdCd.asText();
			}

			JsonNode kospi200ItemYn = outputNode.get("kospi200_item_yn"); // 코스피200종목 여부
			if (kospi200ItemYn != null) {
				kisSearchStockInfo.kospi200ItemYn = kospi200ItemYn.asText();
			}

			JsonNode idxBztpSclsCdName = outputNode.get("idx_bztp_scls_cd_name"); // 지수 업종 소분류 코드명
			if (idxBztpSclsCdName != null) {
				kisSearchStockInfo.idxBztpSclsCdName = idxBztpSclsCdName.asText();
			}
			return kisSearchStockInfo;
		}
	}
}
