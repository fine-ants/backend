package codesquad.fineants.docs.portfolio;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.snippet.Attributes;

import codesquad.fineants.docs.RestDocsSupport;
import codesquad.fineants.domain.portfolio.controller.PortfolioNotificationRestController;
import codesquad.fineants.domain.portfolio.domain.dto.request.PortfolioNotificationUpdateRequest;
import codesquad.fineants.domain.portfolio.domain.dto.response.PortfolioNotificationUpdateResponse;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.portfolio.service.PortfolioNotificationService;
import codesquad.fineants.global.util.ObjectMapperUtil;

public class PortfolioNotificationRestControllerDocsTest extends RestDocsSupport {

	private final PortfolioNotificationService service = mock(PortfolioNotificationService.class);

	@Override
	protected Object initController() {
		return new PortfolioNotificationRestController(service);
	}

	@DisplayName("포트폴리오 목표수익률 알림 활성화 수정 API")
	@Test
	void updateNotificationTargetGain() throws Exception {
		// given
		Portfolio portfolio = createPortfolio(createMember());

		given(service.updateNotificationTargetGain(
			ArgumentMatchers.any(PortfolioNotificationUpdateRequest.class),
			anyLong()))
			.willReturn(PortfolioNotificationUpdateResponse.targetGainIsActive(portfolio));

		Map<String, Object> body = Map.of(
			"isActive", true
		);

		// when
		mockMvc.perform(put("/api/portfolio/{portfolioId}/notification/targetGain", portfolio.getId())
				.cookie(createTokenCookies())
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("목표 수익률 알림이 활성화되었습니다")))
			.andDo(
				document(
					"portfolio_notification_target_gain_active-update",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					pathParameters(
						parameterWithName("portfolioId").description("포트폴리오 등록번호")
					),
					requestFields(
						fieldWithPath("isActive").type(JsonFieldType.BOOLEAN).description("활성화 여부")
							.attributes(
								Attributes.key("constraints").value(
									String.join(",",
										"true", "false"
									)
								))
					),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(JsonFieldType.NULL)
							.description("응답 데이터")
					)
				)
			);
	}

	@DisplayName("포트폴리오 최대손실율 알림 활성화 수정 API")
	@Test
	void updateNotificationMaximumLoss() throws Exception {
		// given
		Portfolio portfolio = createPortfolio(createMember());

		given(service.updateNotificationMaximumLoss(
			ArgumentMatchers.any(PortfolioNotificationUpdateRequest.class),
			anyLong()))
			.willReturn(PortfolioNotificationUpdateResponse.maximumLossIsActive(portfolio));

		Map<String, Object> body = Map.of(
			"isActive", true
		);

		// when
		mockMvc.perform(put("/api/portfolio/{portfolioId}/notification/maxLoss", portfolio.getId())
				.cookie(createTokenCookies())
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("최대 손실율 알림이 활성화되었습니다")))
			.andDo(
				document(
					"portfolio_notification_maximum_loss_active-update",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					pathParameters(
						parameterWithName("portfolioId").description("포트폴리오 등록번호")
					),
					requestFields(
						fieldWithPath("isActive").type(JsonFieldType.BOOLEAN).description("활성화 여부")
							.attributes(
								Attributes.key("constraints").value(
									String.join(",",
										"true", "false"
									)
								))
					),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(JsonFieldType.NULL)
							.description("응답 데이터")
					)
				)
			);
	}
}
