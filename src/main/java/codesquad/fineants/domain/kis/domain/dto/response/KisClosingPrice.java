package codesquad.fineants.domain.kis.domain.dto.response;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
@JsonDeserialize(using = KisClosingPrice.KisClosingPriceDeserializer.class)
public class KisClosingPrice {
	private String tickerSymbol;
	private Long price;

	public static KisClosingPrice create(String tickerSymbol, Long closingPrice) {
		return new KisClosingPrice(tickerSymbol, closingPrice);
	}

	public static KisClosingPrice empty(String tickerSymbol) {
		return KisClosingPrice.builder()
			.tickerSymbol(tickerSymbol)
			.price(0L)
			.build();
	}

	static class KisClosingPriceDeserializer extends JsonDeserializer<KisClosingPrice> {
		@Override
		public KisClosingPrice deserialize(JsonParser parser, DeserializationContext context) throws
			IOException {
			TreeNode rootNode = parser.readValueAsTree();
			KisClosingPrice kisClosingPrice = new KisClosingPrice();

			JsonNode outputNode = (JsonNode)rootNode.get("output1");
			JsonNode stckPrdyClpr = outputNode.get("stck_prdy_clpr"); // 종목 종가
			if (stckPrdyClpr != null) {
				kisClosingPrice.price = stckPrdyClpr.asLong();
			}

			JsonNode stckShrnIscd = outputNode.get("stck_shrn_iscd"); // 종목 코드
			if (stckShrnIscd != null) {
				kisClosingPrice.tickerSymbol = stckShrnIscd.asText();
			}
			return kisClosingPrice;
		}
	}
}
