package co.fineants.api.global.config.jackson;

import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.portfolio.domain.dto.response.DashboardLineChartResponse;
import co.fineants.api.global.config.CacheConfig;

class CacheObjectMapperTest {

	private final ObjectMapper cacheObjectMapper = new CacheConfig().cacheObjectMapper(
		new JacksonConfig().objectMapper());

	@DisplayName("티커 심볼이 담긴 Set 컬렉션을 직렬화하면 클래스 이름 정보가 포함되어 있다")
	@Test
	void givenSet_whenSerialization_thenReturnJson() throws JsonProcessingException {
		// given
		Set<String> tickerSymbols = Set.of("005930", "123456");
		// when
		String json = cacheObjectMapper.writeValueAsString(tickerSymbols);
		// then
		Set expected = cacheObjectMapper.readValue(json, Set.class);
		Assertions.assertThat(tickerSymbols).isEqualTo(expected);
	}

	@DisplayName("Money 객체를 직렬화/역직렬화를 수행한다")
	@Test
	void givenMoney_whenSerializationAndDeserialization_thenReturnJsonAndMoney() throws JsonProcessingException {
		// given
		Money money = Money.won(10000);
		String json = cacheObjectMapper.writeValueAsString(money);
		// when
		Money actual = cacheObjectMapper.readValue(json, Money.class);
		// then
		Money expected = Money.won(10000);
		Assertions.assertThat(actual).isEqualByComparingTo(expected);
	}

	@DisplayName("DashboardLineChartResponse 객체를 직렬화/역직렬화를 수행한다")
	@Test
	void givenDashboardLineChartResponse_whenSerializationAndDeserialization_thenReturnJsonAndMoney() throws
		JsonProcessingException {
		// given
		DashboardLineChartResponse response = DashboardLineChartResponse.of("2024-10-22", Money.won(10000));
		String json = cacheObjectMapper.writeValueAsString(response);
		// when
		DashboardLineChartResponse actual = cacheObjectMapper.readValue(json, DashboardLineChartResponse.class);
		// then
		DashboardLineChartResponse expected = DashboardLineChartResponse.of("2024-10-22", Money.won(10000));
		Assertions.assertThat(actual).isEqualTo(expected);
	}
}
