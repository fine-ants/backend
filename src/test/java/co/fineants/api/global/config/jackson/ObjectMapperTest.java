package co.fineants.api.global.config.jackson;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioStockCreateResponse;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.stock.domain.entity.Stock;

class ObjectMapperTest extends AbstractContainerBaseTest {

	@Autowired
	private ObjectMapper objectMapper;

	@DisplayName("포트폴리오 종목 생성 리스폰스를 직렬화/역직렬화한다")
	@Test
	void givenPortfolioStockCreateResponse_whenSerializationAndDeserialization_thenReturnJsonAndResponse() throws
		JsonProcessingException {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock samsung = createSamsungStock();
		PortfolioHolding holding = createPortfolioHolding(portfolio, samsung);
		PortfolioStockCreateResponse response = PortfolioStockCreateResponse.from(holding);
		String json = objectMapper.writeValueAsString(response);
		// when
		PortfolioStockCreateResponse actual = objectMapper.readValue(json, PortfolioStockCreateResponse.class);
		// then
		PortfolioStockCreateResponse expected = PortfolioStockCreateResponse.from(holding);
		Assertions.assertThat(actual).isEqualTo(expected);
	}
}
