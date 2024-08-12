package codesquad.fineants.domain.kis.client;

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
@Builder(access = AccessLevel.PRIVATE)
@ToString
@JsonDeserialize(using = KisCurrentPrice.KisCurrentPriceDeserializer.class)
public class KisCurrentPrice {
	private String tickerSymbol;
	private Long price;

	public static KisCurrentPrice empty(String tickerSymbol) {
		return new KisCurrentPrice(tickerSymbol, 0L);
	}

	public static KisCurrentPrice create(String tickerSymbol, Long price) {
		return new KisCurrentPrice(tickerSymbol, price);
	}

	public String toRedisKey(String format) {
		return String.format(format, tickerSymbol);
	}

	public String toRedisValue() {
		return String.valueOf(price);
	}

	static class KisCurrentPriceDeserializer extends JsonDeserializer<KisCurrentPrice> {
		@Override
		public KisCurrentPrice deserialize(JsonParser parser, DeserializationContext context) throws
			IOException {
			TreeNode rootNode = parser.readValueAsTree();
			KisCurrentPrice kisCurrentPrice = new KisCurrentPrice();

			JsonNode outputNode = (JsonNode)rootNode.get("output");
			JsonNode stckPrprNode = outputNode.get("stck_prpr"); // 주식 현재가
			if (stckPrprNode != null) {
				kisCurrentPrice.price = stckPrprNode.asLong();
			}

			JsonNode stckShrnIscd = outputNode.get("stck_shrn_iscd"); // 주식 종목 단축 코드
			if (stckShrnIscd != null) {
				kisCurrentPrice.tickerSymbol = stckShrnIscd.asText();
			}
			return kisCurrentPrice;
		}
	}
}
