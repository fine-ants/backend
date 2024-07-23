package codesquad.fineants.domain.portfolio.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import codesquad.fineants.ControllerTestSupport;
import codesquad.fineants.domain.common.count.Count;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.Percentage;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.portfolio.domain.dto.request.PortfolioCreateRequest;
import codesquad.fineants.domain.portfolio.domain.dto.request.PortfolioModifyRequest;
import codesquad.fineants.domain.portfolio.domain.dto.response.PortFolioCreateResponse;
import codesquad.fineants.domain.portfolio.domain.dto.response.PortFolioItem;
import codesquad.fineants.domain.portfolio.domain.dto.response.PortfolioModifyResponse;
import codesquad.fineants.domain.portfolio.domain.dto.response.PortfoliosResponse;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.portfolio.service.PortFolioService;
import codesquad.fineants.global.util.ObjectMapperUtil;

@WebMvcTest(controllers = PortFolioRestController.class)
class PortFolioRestControllerTest extends ControllerTestSupport {

	@MockBean
	private PortFolioService portFolioService;

	@Override
	protected Object initController() {
		return new PortFolioRestController(portFolioService);
	}

	@DisplayName("사용자는 포트폴리오 추가를 요청한다")
	@CsvSource(value = {"1000000,1500000,900000", "0,0,0", "0,1500000,900000"})
	@ParameterizedTest
	void createPortfolio_whenAddPortfolio_thenSavePortfolio(Long budget, Long targetGain, Long maximumLoss) throws
		Exception {
		// given
		Member member = createMember();
		PortFolioCreateResponse response = PortFolioCreateResponse.from(
			createPortfolio(
				member,
				"내꿈은 워렙버핏",
				Money.won(budget),
				Money.won(targetGain),
				Money.won(maximumLoss)
			)
		);
		BDDMockito.given(portFolioService.createPortfolio(any(PortfolioCreateRequest.class),
				ArgumentMatchers.anyLong()))
			.willReturn(response);

		Map<String, Object> requestBodyMap = new HashMap<>();
		requestBodyMap.put("name", "내꿈은 워렌버핏");
		requestBodyMap.put("securitiesFirm", "토스");
		requestBodyMap.put("budget", budget);
		requestBodyMap.put("targetGain", targetGain);
		requestBodyMap.put("maximumLoss", maximumLoss);

		String body = ObjectMapperUtil.serialize(requestBodyMap);
		// when & then
		mockMvc.perform(post("/api/portfolios")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("code").value(equalTo(201)))
			.andExpect(jsonPath("status").value(equalTo("Created")))
			.andExpect(jsonPath("message").value(equalTo("포트폴리오가 추가되었습니다")))
			.andExpect(jsonPath("data.portfolioId").value(equalTo(1)));
	}

	@DisplayName("사용자는 포트폴리오 추가시 유효하지 않은 입력 정보로 추가할 수 없다")
	@MethodSource(value = "invalidPortfolioInput")
	@ParameterizedTest
	void addPortfolioWithInvalidInput(String name, String securitiesFirm, Long budget, Long targetGain,
		Long maximumLoss) throws Exception {
		// given
		Map<String, Object> requestBodyMap = new HashMap<>();
		requestBodyMap.put("name", name);
		requestBodyMap.put("securitiesFirm", securitiesFirm);
		requestBodyMap.put("budget", budget);
		requestBodyMap.put("targetGain", targetGain);
		requestBodyMap.put("maximumLoss", maximumLoss);

		String body = ObjectMapperUtil.serialize(requestBodyMap);
		// when & then
		mockMvc.perform(post("/api/portfolios")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")))
			.andExpect(jsonPath("data").isArray());
	}

	@DisplayName("사용자는 자신의 포트폴리오 목록을 조회한다")
	@Test
	void searchMyAllPortfolios() throws Exception {
		// given
		PortFolioItem portFolioItem = PortFolioItem.builder()
			.id(1L)
			.securitiesFirm("토스증권")
			.name("내꿈은 워렌버핏")
			.budget(Money.won(1000000L))
			.totalGain(Money.won(100000L))
			.totalGainRate(Percentage.from(0.1))
			.dailyGain(Money.won(100000L))
			.dailyGainRate(Percentage.from(0.1))
			.currentValuation(Money.won(100000L))
			.expectedMonthlyDividend(Money.won(20000L))
			.numShares(Count.from(0))
			.dateCreated(LocalDateTime.now())
			.build();
		BDDMockito.given(portFolioService.readMyAllPortfolio(ArgumentMatchers.anyLong()))
			.willReturn(PortfoliosResponse.builder()
				.portfolios(List.of(portFolioItem))
				.build());

		// when & then
		mockMvc.perform(get("/api/portfolios"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("포트폴리오 목록 조회가 완료되었습니다")))
			.andExpect(jsonPath("data.portfolios[0].id").value(equalTo(1)))
			.andExpect(jsonPath("data.portfolios[0].securitiesFirm").value(equalTo("토스증권")))
			.andExpect(jsonPath("data.portfolios[0].name").value(equalTo("내꿈은 워렌버핏")))
			.andExpect(jsonPath("data.portfolios[0].budget").value(equalTo(1000000)))
			.andExpect(jsonPath("data.portfolios[0].totalGain").value(equalTo(100000)))
			.andExpect(jsonPath("data.portfolios[0].totalGainRate").value(equalTo(10.0)))
			.andExpect(jsonPath("data.portfolios[0].dailyGain").value(equalTo(100000)))
			.andExpect(jsonPath("data.portfolios[0].dailyGainRate").value(equalTo(10.0)))
			.andExpect(jsonPath("data.portfolios[0].currentValuation")
				.value(equalTo(100000)))
			.andExpect(jsonPath("data.portfolios[0].expectedMonthlyDividend")
				.value(equalTo(20000)))
			.andExpect(jsonPath("data.portfolios[0].numShares")
				.value(equalTo(0)))
			.andExpect(jsonPath("data.portfolios[0].dateCreated").isNotEmpty());
	}

	@DisplayName("사용자는 포트폴리오 수정을 요청한다")
	@CsvSource(value = {"1000000,1500000,900000", "0,0,0", "0,1500000,900000"})
	@ParameterizedTest
	void updatePortfolio(Long budget, Long targetGain, Long maximumLoss) throws Exception {
		// given
		Member member = createMember();
		Portfolio portfolio = createPortfolio(
			member,
			"내꿈은 워렌버핏",
			Money.won(1000000L),
			Money.won(1500000L),
			Money.won(900000L)
		);
		PortfolioModifyResponse response = PortfolioModifyResponse.from(portfolio);

		BDDMockito.given(portFolioService.updatePortfolio(any(PortfolioModifyRequest.class), ArgumentMatchers.anyLong(),
				ArgumentMatchers.anyLong()))
			.willReturn(response);

		Map<String, Object> requestBodyMap = new HashMap<>();
		requestBodyMap.put("name", "내꿈은 찰리몽거");
		requestBodyMap.put("securitiesFirm", "토스");
		requestBodyMap.put("budget", budget);
		requestBodyMap.put("targetGain", targetGain);
		requestBodyMap.put("maximumLoss", maximumLoss);

		String body = ObjectMapperUtil.serialize(requestBodyMap);
		// when & then
		mockMvc.perform(put("/api/portfolios/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("포트폴리오가 수정되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)));
	}

	@DisplayName("사용자는 포트폴리오 수정시 유효하지 않은 입력 정보로 추가할 수 없다")
	@MethodSource("invalidPortfolioInput")
	@ParameterizedTest
	void updatePortfolioWithInvalidInput() throws Exception {
		// given
		Map<String, Object> requestBodyMap = new HashMap<>();
		requestBodyMap.put("name", "");
		requestBodyMap.put("securitiesFirm", "");
		requestBodyMap.put("budget", 0);
		requestBodyMap.put("targetGain", null);
		requestBodyMap.put("maximumLoss", -1);

		String body = ObjectMapperUtil.serialize(requestBodyMap);
		// when & then
		mockMvc.perform(put("/api/portfolios/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")))
			.andExpect(jsonPath("data").isArray());
	}

	@DisplayName("사용자는 포트폴리오 삭제를 요청한다")
	@Test
	void deletePortfolio() throws Exception {
		// given

		// when & then
		mockMvc.perform(delete("/api/portfolios/1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("포트폴리오 삭제가 완료되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)));
	}

	public static Stream<Arguments> invalidPortfolioInput() {
		return Stream.of(
			Arguments.of("", "", 0L, null, -1L)
		);
	}
}
