package codesquad.fineants.spring.docs.portfolio;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.payload.JsonFieldType;

import codesquad.fineants.spring.api.portfolio_notification_setting.controller.PortfolioNotificationSettingRestController;
import codesquad.fineants.spring.api.portfolio_notification_setting.response.PortfolioNotificationSettingSearchItem;
import codesquad.fineants.spring.api.portfolio_notification_setting.response.PortfolioNotificationSettingSearchResponse;
import codesquad.fineants.spring.api.portfolio_notification_setting.service.PortfolioNotificationSettingService;
import codesquad.fineants.spring.docs.RestDocsSupport;

public class PortfolioNotificationSettingRestControllerDocsTest extends RestDocsSupport {

	private final PortfolioNotificationSettingService service = Mockito.mock(PortfolioNotificationSettingService.class);

	@Override
	protected Object initController() {
		return new PortfolioNotificationSettingRestController(service);
	}

	@DisplayName("포트폴리오 활성 알림 목록 조회 API")
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
						.isTargetGainSet(true)
						.isMaxLossSet(true)
						.createdAt(now)
						.build(),
					PortfolioNotificationSettingSearchItem.builder()
						.portfolioId(2L)
						.securitiesFirm("토스증권")
						.name("포트폴리오 2")
						.targetGainNotify(true)
						.maxLossNotify(false)
						.isTargetGainSet(true)
						.isMaxLossSet(true)
						.createdAt(now)
						.build()))
				.build());

		// when & then
		mockMvc.perform(get("/api/portfolios/notification/settings")
				.header(HttpHeaders.AUTHORIZATION, "Bearer accessToken"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("모든 알림 조회를 성공했습니다")))
			.andExpect(jsonPath("data.portfolios[0].portfolioId").value(equalTo(1)))
			.andExpect(jsonPath("data.portfolios[0].securitiesFirm").value(equalTo("토스증권")))
			.andExpect(jsonPath("data.portfolios[0].name").value(equalTo("포트폴리오 1")))
			.andExpect(jsonPath("data.portfolios[0].targetGainNotify").value(equalTo(true)))
			.andExpect(jsonPath("data.portfolios[0].maxLossNotify").value(equalTo(false)))
			.andExpect(jsonPath("data.portfolios[0].isTargetGainSet").value(equalTo(true)))
			.andExpect(jsonPath("data.portfolios[0].isMaxLossSet").value(equalTo(true)))
			.andExpect(jsonPath("data.portfolios[0].createdAt").isNotEmpty())
			.andExpect(jsonPath("data.portfolios[1].portfolioId").value(equalTo(2)))
			.andExpect(jsonPath("data.portfolios[1].securitiesFirm").value(equalTo("토스증권")))
			.andExpect(jsonPath("data.portfolios[1].name").value(equalTo("포트폴리오 2")))
			.andExpect(jsonPath("data.portfolios[1].targetGainNotify").value(equalTo(true)))
			.andExpect(jsonPath("data.portfolios[1].maxLossNotify").value(equalTo(false)))
			.andExpect(jsonPath("data.portfolios[1].isTargetGainSet").value(equalTo(true)))
			.andExpect(jsonPath("data.portfolios[1].isMaxLossSet").value(equalTo(true)))
			.andExpect(jsonPath("data.portfolios[1].createdAt").isNotEmpty())
			.andDo(
				document(
					"portfolio_notification_settings-search",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestHeaders(
						headerWithName(HttpHeaders.AUTHORIZATION).description("액세스 토큰")
					),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(JsonFieldType.OBJECT)
							.description("응답 데이터"),
						fieldWithPath("data.portfolios[].portfolioId").type(JsonFieldType.NUMBER)
							.description("포트폴리오 등록번호"),
						fieldWithPath("data.portfolios[].securitiesFirm").type(JsonFieldType.STRING)
							.description("증권사"),
						fieldWithPath("data.portfolios[].name").type(JsonFieldType.STRING)
							.description("포트폴리오 이름"),
						fieldWithPath("data.portfolios[].targetGainNotify").type(JsonFieldType.BOOLEAN)
							.description("목표 수익률 알림 여부"),
						fieldWithPath("data.portfolios[].maxLossNotify").type(JsonFieldType.BOOLEAN)
							.description("최대 손실율 알림 여부"),
						fieldWithPath("data.portfolios[].isTargetGainSet").type(JsonFieldType.BOOLEAN)
							.description("목표 수익률 알림 설정 가능 여부"),
						fieldWithPath("data.portfolios[].isMaxLossSet").type(JsonFieldType.BOOLEAN)
							.description("최대 손실율 알림 설정 가능 여부"),
						fieldWithPath("data.portfolios[].createdAt").type(JsonFieldType.STRING)
							.description("생성 일자")
					)
				)
			);
	}
}
