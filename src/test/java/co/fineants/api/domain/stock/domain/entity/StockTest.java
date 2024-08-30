package co.fineants.api.domain.stock.domain.entity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StockTest {

	@DisplayName("csv에 저장하기 위해서 쉼표(,)로 구분한 한줄의 문자열로 변환한다")
	@Test
	void toCsvLineString() {
		// given
		Stock stock = Stock.of("000370", "한화손해보험보통주", "\"Hanwha General Insurance Co.,Ltd.\"", "KR7000370007", "보험",
			Market.KOSPI);
		// when
		String result = stock.toCsvLineString();
		// then
		String expected = "KR7000370007,000370,한화손해보험보통주,\"Hanwha General Insurance Co.,Ltd.\",KOSPI,보험";
		Assertions.assertThat(result).isEqualTo(expected);
	}
}
