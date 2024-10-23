package co.fineants.api.global.config.jackson;

import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioStockCreateResponse;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.portfolio.domain.dto.response.DashboardLineChartResponse;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.stock.domain.entity.Stock;

class ObjectMapperTest extends AbstractContainerBaseTest {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	@Qualifier("cacheObjectMapper")
	private ObjectMapper cacheObjectMapper;

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

	@DisplayName("캐시를 위한 직렬화기가 직렬화할때는 클래스 이름이 포함되어 있다")
	@Test
	void givenCacheObjectMapper_whenSerialization_thenContainsClassName() throws JsonProcessingException {
		// given
		Set<String> tickerSymbols = Set.of("005930", "123456");
		// when
		String actual = cacheObjectMapper.writeValueAsString(tickerSymbols);
		// then
		Set expected = cacheObjectMapper.readValue(actual, Set.class);
		Assertions.assertThat(tickerSymbols).isEqualTo(expected);
	}

	@DisplayName("json 데이터를 역직렬화해서 Money 객체로 반환한다")
	@Test
	void givenMoney_whenDeserialize_thenReturnMoneyInstance() throws JsonProcessingException {
		// given
		Money money = Money.won(10000);
		String json = cacheObjectMapper.writeValueAsString(money);
		// when
		Money actual = cacheObjectMapper.readValue(json, Money.class);
		// then
		Money expected = Money.won(10000);
		Assertions.assertThat(actual).isEqualByComparingTo(expected);
	}

	@DisplayName("dto json 데이터를 역직렬화해서 DTO 객체로 반환한다")
	@Test
	void givenObjectMapper_whenDeserialize_thenReturnDtoOfJson() throws JsonProcessingException {
		// given
		DashboardLineChartResponse response = DashboardLineChartResponse.of("2024-10-22",
			Money.won(10000));
		String json = cacheObjectMapper.writeValueAsString(response);
		// when
		DashboardLineChartResponse actual = cacheObjectMapper.readValue(json, DashboardLineChartResponse.class);
		// then
		DashboardLineChartResponse expected = DashboardLineChartResponse.of("2024-10-22", Money.won(10000));
		Assertions.assertThat(actual).isEqualTo(expected);
	}
}
