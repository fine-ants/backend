package co.fineants.api.domain.stock.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.Percentage;
import co.fineants.api.domain.stock.domain.dto.response.StockResponse;
import co.fineants.api.domain.stock.domain.entity.Market;
import co.fineants.api.domain.stock.service.StockService;
import co.fineants.support.controller.ControllerTestSupport;

@WebMvcTest(controllers = StockRestController.class)
class StockRestControllerTest extends ControllerTestSupport {

	@MockBean
	private StockService stockService;

	@Override
	protected Object initController() {
		return new StockRestController(stockService);
	}

	@DisplayName("주식 종목을 조회한다.")
	@Test
	void getStock() throws Exception {
		// given
		String tickerSymbol = "006660";
		StockResponse response = StockResponse.builder()
			.stockCode("KR7006660005")
			.tickerSymbol("006660")
			.companyName("삼성공조보통주")
			.companyNameEng("SamsungClimateControlCo.,Ltd")
			.market(Market.KOSPI)
			.currentPrice(Money.won(68000L))
			.dailyChange(Money.won(12000L))
			.dailyChangeRate(Percentage.from(0.2045))
			.sector("전기전자")
			.annualDividend(Money.won(6000L))
			.annualDividendYield(Percentage.from(0.1))
			.dividendMonths(List.of(1, 4))
			.build();
		given(stockService.getDetailedStock(anyString())).willReturn(response);

		// when & then
		mockMvc.perform(get("/api/stocks/{tickerSymbol}", tickerSymbol)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("종목 상세정보 조회가 완료되었습니다")))
			.andExpect(jsonPath("data.stockCode").value(equalTo("KR7006660005")))
			.andExpect(jsonPath("data.tickerSymbol").value(equalTo("006660")))
			.andExpect(jsonPath("data.companyName").value(equalTo("삼성공조보통주")))
			.andExpect(jsonPath("data.companyNameEng").value(equalTo("SamsungClimateControlCo.,Ltd")))
			.andExpect(jsonPath("data.market").value(equalTo("KOSPI")))
			.andExpect(jsonPath("data.currentPrice").value(equalTo(68000)))
			.andExpect(jsonPath("data.dailyChange").value(equalTo(12000)))
			.andExpect(jsonPath("data.dailyChangeRate").value(equalTo(20.45)))
			.andExpect(jsonPath("data.sector").value(equalTo("전기전자")))
			.andExpect(jsonPath("data.annualDividend").value(equalTo(6000)))
			.andExpect(jsonPath("data.annualDividendYield").value(equalTo(10.0)))
			.andExpect(jsonPath("data.dividendMonths[0]").value(equalTo(1)))
			.andExpect(jsonPath("data.dividendMonths[1]").value(equalTo(4)));
	}
}
