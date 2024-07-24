package codesquad.fineants.domain.kis.domain.dto.response;

import java.io.IOException;

import org.apache.logging.log4j.util.Strings;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@JsonDeserialize(using = KisIpo.KisIpoDeserializer.class)
public class KisIpo {
	private String listDt; // 상장/등록일
	private String shtCd; // 종목 코드 (tickerSymbol)
	private String isinName; // 종목명

	public boolean isEmpty() {
		return Strings.isEmpty(shtCd);
	}

	static class KisIpoDeserializer extends JsonDeserializer<KisIpo> {
		@Override
		public KisIpo deserialize(JsonParser parser, DeserializationContext context) throws
			IOException {
			TreeNode rootNode = parser.readValueAsTree();
			KisIpo kisIpo = new KisIpo();

			JsonNode outputNode = (JsonNode)rootNode;
			JsonNode listDt = outputNode.get("list_dt"); // 상장 등록일
			if (listDt != null) {
				kisIpo.listDt = listDt.asText();
			}

			JsonNode shtCd = outputNode.get("sht_cd"); // 종목코드(ticker)
			if (shtCd != null) {
				kisIpo.shtCd = shtCd.asText();
			}

			JsonNode isinName = outputNode.get("isin_name"); // 종목명
			if (isinName != null) {
				kisIpo.isinName = isinName.asText();
			}
			return kisIpo;
		}
	}
}
