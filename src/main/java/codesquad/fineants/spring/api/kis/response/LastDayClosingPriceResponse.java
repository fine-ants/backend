package codesquad.fineants.spring.api.kis.response;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
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
@JsonDeserialize(using = LastDayClosingPriceResponse.LastDayClosingPriceDeserializer.class)
public class LastDayClosingPriceResponse {
	private String tickerSymbol;
	private Long closingPrice;

	public static LastDayClosingPriceResponse create(String tickerSymbol, Long closingPrice) {
		return new LastDayClosingPriceResponse(tickerSymbol, closingPrice);
	}

	public static LastDayClosingPriceResponse empty(String tickerSymbol) {
		return LastDayClosingPriceResponse.builder()
			.tickerSymbol(tickerSymbol)
			.closingPrice(0L)
			.build();
	}

	static class LastDayClosingPriceDeserializer extends JsonDeserializer<LastDayClosingPriceResponse> {
		@Override
		public LastDayClosingPriceResponse deserialize(JsonParser p, DeserializationContext ctxt) throws
			IOException,
			JacksonException {
			TreeNode rootNode = p.readValueAsTree();
			LastDayClosingPriceResponse lastDayClosingPriceResponse = new LastDayClosingPriceResponse();

			JsonNode outputNode = (JsonNode)rootNode.get("output1");
			JsonNode stckPrdyClpr = outputNode.get("stck_prdy_clpr"); // 종목 종가
			if (stckPrdyClpr != null) {
				lastDayClosingPriceResponse.closingPrice = stckPrdyClpr.asLong();
			}

			JsonNode stckShrnIscd = outputNode.get("stck_shrn_iscd");// 종목 코드
			if (stckShrnIscd != null) {
				lastDayClosingPriceResponse.tickerSymbol = stckShrnIscd.asText();
			}
			return lastDayClosingPriceResponse;
		}
	}
}
