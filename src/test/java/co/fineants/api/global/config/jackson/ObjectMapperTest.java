package co.fineants.api.global.config.jackson;

import java.util.Set;

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

	@DisplayName("리스폰스 객체를 직렬화한다")
	@Test
	void givenResponse_whenSerialize_thenReturnStringOfResponse() throws JsonProcessingException {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		Stock samsung = createSamsungStock();
		PortfolioHolding holding = createPortfolioHolding(portfolio, samsung);
		PortfolioStockCreateResponse response = PortfolioStockCreateResponse.from(holding);
		// when
		String actual = objectMapper.writeValueAsString(response);
		// then
		Assertions.assertThat(actual).contains("portfolioHoldingId");
	}

	// TODO: 캐시 설정, 레디스 설정, ObjectMapper 설정간에 에러가 없는지 테스트 추가
	@DisplayName("캐시를 위한 직렬화기가 직렬화할때는 클래스 이름이 포함되어 있다")
	@Test
	void givenCacheObjectMapper_whenSerialization_thenContainsClassName() throws JsonProcessingException {
		// given
		ObjectMapper cacheObjectMapper = new ObjectMapper();
		cacheObjectMapper.setConfig(objectMapper.getSerializationConfig());
		cacheObjectMapper.activateDefaultTyping(
			cacheObjectMapper.getPolymorphicTypeValidator(),
			ObjectMapper.DefaultTyping.EVERYTHING
		);
		Set<String> tickerSymbols = Set.of("005930", "123456");
		// when
		String actual = cacheObjectMapper.writeValueAsString(tickerSymbols);
		// then
		Set expected = cacheObjectMapper.readValue(actual, Set.class);
		Assertions.assertThat(tickerSymbols).isEqualTo(expected);
	}
}
