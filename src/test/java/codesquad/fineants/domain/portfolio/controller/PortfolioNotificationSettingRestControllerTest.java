package codesquad.fineants.domain.portfolio.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import codesquad.fineants.ControllerTestSupport;
import codesquad.fineants.domain.portfolio.domain.dto.response.PortfolioNotificationSettingSearchItem;
import codesquad.fineants.domain.portfolio.domain.dto.response.PortfolioNotificationSettingSearchResponse;
import codesquad.fineants.domain.portfolio.service.PortfolioNotificationSettingService;

@WebMvcTest(controllers = PortfolioNotificationSettingRestController.class)
class PortfolioNotificationSettingRestControllerTest extends ControllerTestSupport {

	@MockBean
	private PortfolioNotificationSettingService service;

	@Override
	protected Object initController() {
		return new PortfolioNotificationSettingRestController(service);
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
						.createdAt(now)
						.build(),
					PortfolioNotificationSettingSearchItem.builder()
						.portfolioId(2L)
						.securitiesFirm("토스증권")
						.name("포트폴리오 2")
						.targetGainNotify(true)
						.maxLossNotify(false)
						.createdAt(now)
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
			.andExpect(jsonPath("data.portfolios[0].createdAt").isNotEmpty())
			.andExpect(jsonPath("data.portfolios[1].portfolioId").value(equalTo(2)))
			.andExpect(jsonPath("data.portfolios[1].securitiesFirm").value(equalTo("토스증권")))
			.andExpect(jsonPath("data.portfolios[1].name").value(equalTo("포트폴리오 2")))
			.andExpect(jsonPath("data.portfolios[1].targetGainNotify").value(equalTo(true)))
			.andExpect(jsonPath("data.portfolios[1].maxLossNotify").value(equalTo(false)))
			.andExpect(jsonPath("data.portfolios[1].createdAt").isNotEmpty());
	}
}
