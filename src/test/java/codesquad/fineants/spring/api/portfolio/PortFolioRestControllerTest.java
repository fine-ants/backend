package codesquad.fineants.spring.api.portfolio;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.oauth.support.AuthPrincipalArgumentResolver;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio.PortfolioRepository;
import codesquad.fineants.spring.api.errors.handler.GlobalExceptionHandler;
import codesquad.fineants.spring.api.portfolio.request.PortfolioCreateRequest;
import codesquad.fineants.spring.api.portfolio.request.PortfolioModifyRequest;
import codesquad.fineants.spring.api.portfolio.response.PortFolioCreateResponse;
import codesquad.fineants.spring.api.portfolio.response.PortfolioModifyResponse;
import codesquad.fineants.spring.auth.HasPortfolioAuthorizationAspect;
import codesquad.fineants.spring.config.JpaAuditingConfiguration;
import codesquad.fineants.spring.config.SpringConfig;
import codesquad.fineants.spring.util.ObjectMapperUtil;

@ActiveProfiles("test")
@WebMvcTest(controllers = PortFolioRestController.class)
@Import(value = {SpringConfig.class, HasPortfolioAuthorizationAspect.class})
@MockBean(JpaAuditingConfiguration.class)
class PortFolioRestControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private PortFolioRestController portFolioRestController;

	@Autowired
	private GlobalExceptionHandler globalExceptionHandler;

	@MockBean
	private AuthPrincipalArgumentResolver authPrincipalArgumentResolver;

	@MockBean
	private PortFolioService portFolioService;

	@MockBean
	private PortfolioRepository portfolioRepository;

	private Member member;

	@BeforeEach
	void setup() {
		mockMvc = MockMvcBuilders.standaloneSetup(portFolioRestController)
			.setControllerAdvice(globalExceptionHandler)
			.setCustomArgumentResolvers(authPrincipalArgumentResolver)
			.alwaysDo(print())
			.build();

		given(authPrincipalArgumentResolver.supportsParameter(any())).willReturn(true);

		member = createMember();
		AuthMember authMember = AuthMember.from(member);
		given(authPrincipalArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(authMember);
		given(portFolioService.hasAuthorizationBy(anyLong(), anyLong())).willReturn(true);
	}

	@DisplayName("사용자는 포트폴리오 추가를 요청한다")
	@CsvSource(value = {"1000000,1500000,900000", "0,0,0"})
	@ParameterizedTest
	void addPortfolio(Long budget, Long targetGain, Long maximumLoss) throws Exception {
		// given
		PortFolioCreateResponse response = PortFolioCreateResponse.from(createPortfolio(Portfolio.builder()
			.id(1L), budget, targetGain, maximumLoss));
		given(portFolioService.addPortFolio(any(PortfolioCreateRequest.class), any(AuthMember.class)))
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

	@DisplayName("사용자는 포트폴리오 수정을 요청한다")
	@Test
	void modifyPortfolio() throws Exception {
		// given
		Portfolio portfolio = createPortfolio(Portfolio.builder(), 1000000L, 1500000L, 900000L);
		PortfolioModifyResponse response = PortfolioModifyResponse.from(portfolio);

		given(portFolioService.modifyPortfolio(any(PortfolioModifyRequest.class), anyLong(), any(AuthMember.class)))
			.willReturn(response);
		given(portfolioRepository.findById(anyLong())).willReturn(Optional.of(portfolio));

		Map<String, Object> requestBodyMap = new HashMap<>();
		requestBodyMap.put("name", "내꿈은 찰리몽거");
		requestBodyMap.put("securitiesFirm", "토스");
		requestBodyMap.put("budget", 1000000L);
		requestBodyMap.put("targetGain", 1500000L);
		requestBodyMap.put("maximumLoss", 900000L);

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
	@Test
	void modifyPortfolioWithInvalidInput() throws Exception {
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
		Portfolio portfolio = createPortfolio(Portfolio.builder(), 1000000L, 1500000L, 900000L);
		given(portfolioRepository.findById(anyLong())).willReturn(Optional.of(portfolio));

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

	private Member createMember() {
		return Member.builder()
			.id(1L)
			.nickname("일개미1234")
			.email("kim1234@gmail.com")
			.provider("local")
			.password("kim1234@")
			.profileUrl("profileValue")
			.build();
	}

	private Portfolio createPortfolio(Portfolio.PortfolioBuilder id, Long budget, Long targetGain, Long maximumLoss) {
		return id
			.name("내꿈은 워렌버핏")
			.securitiesFirm("토스")
			.budget(budget)
			.targetGain(targetGain)
			.maximumLoss(maximumLoss)
			.member(member)
			.build();
	}
}
