package codesquad.fineants.spring.api.portfolio;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.anyLong;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.MethodParameter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.oauth.support.AuthPrincipalArgumentResolver;
import codesquad.fineants.spring.api.errors.handler.GlobalExceptionHandler;
import codesquad.fineants.spring.api.portfolio.response.PortfolioNotificationSettingSearchItem;
import codesquad.fineants.spring.api.portfolio.response.PortfolioNotificationSettingSearchResponse;
import codesquad.fineants.spring.config.JpaAuditingConfiguration;

@ActiveProfiles("test")
@WebMvcTest(controllers = PortfolioNotificationSettingRestController.class)
@MockBean(JpaAuditingConfiguration.class)
class PortfolioNotificationSettingRestControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private PortfolioNotificationSettingRestController portfolioNotificationSettingRestController;

	@Autowired
	private GlobalExceptionHandler globalExceptionHandler;

	@MockBean
	private AuthPrincipalArgumentResolver authPrincipalArgumentResolver;

	@MockBean
	private PortfolioNotificationSettingService service;

	@BeforeEach
	void setup() {
		mockMvc = MockMvcBuilders.standaloneSetup(portfolioNotificationSettingRestController)
			.setControllerAdvice(globalExceptionHandler)
			.setCustomArgumentResolvers(authPrincipalArgumentResolver)
			.alwaysDo(print())
			.build();

		given(authPrincipalArgumentResolver.supportsParameter(ArgumentMatchers.any(MethodParameter.class)))
			.willReturn(true);
		given(authPrincipalArgumentResolver.resolveArgument(any(), any(), any(), any()))
			.willReturn(AuthMember.from(createMember()));
	}

	@DisplayName("사용자는 포트폴리오 활성 알림 목록을 조회합니다")
	@Test
	void searchPortfolioNotificationSetting() throws Exception {
		// given
		LocalDateTime now = LocalDateTime.now();
		given(service.searchPortfolioNotificationSetting(anyLong()))
			.willReturn(PortfolioNotificationSettingSearchResponse.builder()
				.portfolios(List.of(PortfolioNotificationSettingSearchItem.builder()
						.portfolioId(1L)
						.securitiesFirm("토스증권")
						.name("포트폴리오 1")
						.targetGainNotify(true)
						.maxLossNotify(false)
						.lastUpdated(now)
						.build(),
					PortfolioNotificationSettingSearchItem.builder()
						.portfolioId(2L)
						.securitiesFirm("토스증권")
						.name("포트폴리오 2")
						.targetGainNotify(true)
						.maxLossNotify(false)
						.lastUpdated(now)
						.build()))
				.build());

		// when & then
		mockMvc.perform(get("/api/portfolios/notification/settings"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("모든 알림 조회를 성공했습니다")))
			.andExpect(jsonPath("data.portfolios[0].portfolioId").value(equalTo(1)))
			.andExpect(jsonPath("data.portfolios[0].securitiesFirm").value(equalTo("토스증권")))
			.andExpect(jsonPath("data.portfolios[0].name").value(equalTo("포트폴리오 1")))
			.andExpect(jsonPath("data.portfolios[0].targetGainNotify").value(equalTo(true)))
			.andExpect(jsonPath("data.portfolios[0].maxLossNotify").value(equalTo(false)))
			.andExpect(jsonPath("data.portfolios[0].lastUpdated").isNotEmpty())
			.andExpect(jsonPath("data.portfolios[1].portfolioId").value(equalTo(2)))
			.andExpect(jsonPath("data.portfolios[1].securitiesFirm").value(equalTo("토스증권")))
			.andExpect(jsonPath("data.portfolios[1].name").value(equalTo("포트폴리오 2")))
			.andExpect(jsonPath("data.portfolios[1].targetGainNotify").value(equalTo(true)))
			.andExpect(jsonPath("data.portfolios[1].maxLossNotify").value(equalTo(false)))
			.andExpect(jsonPath("data.portfolios[1].lastUpdated").isNotEmpty());
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
}
