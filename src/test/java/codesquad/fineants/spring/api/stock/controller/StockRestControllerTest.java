package codesquad.fineants.spring.api.stock.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.Percentage;
import codesquad.fineants.domain.oauth.support.AuthPrincipalArgumentResolver;
import codesquad.fineants.domain.stock.Market;
import codesquad.fineants.spring.api.common.errors.handler.GlobalExceptionHandler;
import codesquad.fineants.spring.api.stock.response.StockResponse;
import codesquad.fineants.spring.api.stock.service.StockService;
import codesquad.fineants.spring.config.JacksonConfig;
import codesquad.fineants.spring.config.JpaAuditingConfiguration;
import codesquad.fineants.spring.config.SpringConfig;

@ActiveProfiles("test")
@WebMvcTest(controllers = StockRestController.class)
@Import(value = {SpringConfig.class, JacksonConfig.class})
@MockBean(JpaAuditingConfiguration.class)
public class StockRestControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private StockRestController stockRestController;

	@MockBean
	private StockService stockService;

	@Autowired
	private GlobalExceptionHandler globalExceptionHandler;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private AuthPrincipalArgumentResolver authPrincipalArgumentResolver;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(stockRestController)
			.setControllerAdvice(globalExceptionHandler)
			.setCustomArgumentResolvers(authPrincipalArgumentResolver)
			.setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
			.alwaysDo(print())
			.build();
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
		given(stockService.getStock(anyString())).willReturn(response);

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
