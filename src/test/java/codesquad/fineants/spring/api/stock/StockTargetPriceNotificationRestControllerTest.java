package codesquad.fineants.spring.api.stock;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.anyLong;
import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.oauth.support.AuthPrincipalArgumentResolver;
import codesquad.fineants.spring.api.errors.handler.GlobalExceptionHandler;
import codesquad.fineants.spring.api.stock.request.TargetPriceNotificationCreateRequest;
import codesquad.fineants.spring.api.stock.response.TargetPriceNotificationCreateResponse;
import codesquad.fineants.spring.config.JpaAuditingConfiguration;
import codesquad.fineants.spring.util.ObjectMapperUtil;

@ActiveProfiles("test")
@WebMvcTest(controllers = StockTargetPriceNotificationRestController.class)
@MockBean(JpaAuditingConfiguration.class)
class StockTargetPriceNotificationRestControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private StockTargetPriceNotificationRestController stockTargetPriceNotificationRestController;

	@Autowired
	private GlobalExceptionHandler globalExceptionHandler;

	@MockBean
	private AuthPrincipalArgumentResolver authPrincipalArgumentResolver;

	@MockBean
	private StockTargetPriceNotificationService service;

	@BeforeEach
	void setup() {
		mockMvc = MockMvcBuilders.standaloneSetup(stockTargetPriceNotificationRestController)
			.setControllerAdvice(globalExceptionHandler)
			.setCustomArgumentResolvers(authPrincipalArgumentResolver)
			.alwaysDo(print())
			.build();

		given(authPrincipalArgumentResolver.supportsParameter(ArgumentMatchers.any(MethodParameter.class)))
			.willReturn(true);
		given(authPrincipalArgumentResolver.resolveArgument(any(), any(), any(), any()))
			.willReturn(AuthMember.from(createMember()));
	}

	@DisplayName("사용자는 종목 지정가 알림을 추가합니다")
	@Test
	void createStockTargetPriceNotification() throws Exception {
		// given
		given(service.createStockTargetPriceNotification(
			anyString(),
			any(TargetPriceNotificationCreateRequest.class),
			anyLong()))
			.willReturn(TargetPriceNotificationCreateResponse.builder()
				.targetPriceNotificationId(1L)
				.tickerSymbol("005930")
				.targetPrice(60000L)
				.build());

		String tickerSymbol = "005930";
		Map<String, Long> body = Map.of("targetPrice", 60000L);

		// when & then
		mockMvc.perform(post("/api/stocks/{tickerSymbol}/target-price/notifications", tickerSymbol)
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("code").value(equalTo(201)))
			.andExpect(jsonPath("status").value(equalTo("Created")))
			.andExpect(jsonPath("message").value(equalTo("해당 종목 지정가 알림을 추가했습니다")))
			.andExpect(jsonPath("data.targetPriceNotificationId").value(equalTo(1)))
			.andExpect(jsonPath("data.tickerSymbol").value(equalTo("005930")))
			.andExpect(jsonPath("data.targetPrice").value(equalTo(60000)));
	}

	@DisplayName("사용자는 유효하지 않은 입력으로 종목 지정가 알림을 추가할 수 없습니다")
	@MethodSource(value = "invalidTargetPrice")
	@ParameterizedTest
	void createStockTargetPriceNotification_whenInvalidTargetPrice_thenResponse400Error(Long targetPrice) throws
		Exception {
		// given
		String tickerSymbol = "005930";
		Map<String, Long> body = new HashMap<>();
		body.put("targetPrice", targetPrice);

		// when & then
		mockMvc.perform(post("/api/stocks/{tickerSymbol}/target-price/notifications", tickerSymbol)
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")))
			.andExpect(jsonPath("data").isArray());
	}

	private Member createMember() {
		return Member.builder()
			.id(1L)
			.nickname("일개미1234")
			.email("kim1234@gmail.com")
			.password("kim1234@")
			.provider("local")
			.build();
	}

	public static Stream<Arguments> invalidTargetPrice() {
		return Stream.of(
			Arguments.of(-1L),
			Arguments.of((Object)null)
		);
	}
}
