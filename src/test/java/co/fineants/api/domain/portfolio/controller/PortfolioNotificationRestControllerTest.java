package co.fineants.api.domain.portfolio.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import co.fineants.api.domain.portfolio.domain.dto.response.PortfolioNotificationUpdateResponse;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.portfolio.service.PortfolioNotificationService;
import co.fineants.support.controller.ControllerTestSupport;

@WebMvcTest(controllers = PortfolioNotificationRestController.class)
class PortfolioNotificationRestControllerTest extends ControllerTestSupport {

	@MockBean
	private PortfolioNotificationService service;

	@MockBean
	private PortfolioRepository portfolioRepository;

	@Override
	protected Object initController() {
		return new PortfolioNotificationRestController(service);
	}

	@DisplayName("사용자는 포트폴리오의 목표수익금액 알람을 활성화합니다.")
	@Test
	void modifyNotificationTargetGain() throws Exception {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		long portfolioId = portfolio.getId();
		Map<String, String> requestBodyMap = new HashMap<>();
		requestBodyMap.put("isActive", "true");

		Map<String, Object> responseBodyMap = new HashMap<>();
		responseBodyMap.put("portfolioId", portfolioId);
		responseBodyMap.put("isActive", true);

		PortfolioNotificationUpdateResponse response = objectMapper.readValue(
			objectMapper.writeValueAsString(responseBodyMap), PortfolioNotificationUpdateResponse.class);
		given(portfolioRepository.findById(anyLong())).willReturn(Optional.of(portfolio));
		given(service.updateNotificationTargetGain(
			anyBoolean(),
			anyLong()
		)).willReturn(response);

		// when & then
		mockMvc.perform(put(String.format("/api/portfolio/%d/notification/targetGain", portfolioId))
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8)
				.content(objectMapper.writeValueAsString(requestBodyMap)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("목표 수익률 알림이 활성화되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)));
	}

	@DisplayName("사용자는 포트폴리오의 목표수익금액 알람을 비활성화합니다.")
	@Test
	void modifyNotificationTargetGainWithInActive() throws Exception {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		long portfolioId = portfolio.getId();
		Map<String, String> requestBodyMap = new HashMap<>();
		requestBodyMap.put("isActive", "false");

		Map<String, Object> responseBodyMap = new HashMap<>();
		responseBodyMap.put("portfolioId", portfolioId);
		responseBodyMap.put("isActive", false);

		PortfolioNotificationUpdateResponse response = objectMapper.readValue(
			objectMapper.writeValueAsString(responseBodyMap), PortfolioNotificationUpdateResponse.class);
		given(portfolioRepository.findById(anyLong())).willReturn(Optional.of(portfolio));
		given(service.updateNotificationTargetGain(
			anyBoolean(),
			anyLong()
		)).willReturn(response);

		// when & then
		mockMvc.perform(put(String.format("/api/portfolio/%d/notification/targetGain", portfolioId))
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8)
				.content(objectMapper.writeValueAsString(requestBodyMap)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("목표 수익률 알림이 비 활성화되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)));
	}

	@DisplayName("사용자는 포트폴리오의 최대손실금액 알람을 활성화합니다.")
	@Test
	void modifyNotificationMaximumLoss() throws Exception {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		long portfolioId = portfolio.getId();
		Map<String, String> requestBodyMap = new HashMap<>();
		requestBodyMap.put("isActive", "true");

		Map<String, Object> responseBodyMap = new HashMap<>();
		responseBodyMap.put("portfolioId", portfolioId);
		responseBodyMap.put("isActive", true);

		PortfolioNotificationUpdateResponse response = objectMapper.readValue(
			objectMapper.writeValueAsString(responseBodyMap), PortfolioNotificationUpdateResponse.class);
		given(portfolioRepository.findById(anyLong())).willReturn(Optional.of(portfolio));
		given(service.updateNotificationMaximumLoss(
			anyBoolean(),
			anyLong()
		)).willReturn(response);

		// when & then
		mockMvc.perform(put(String.format("/api/portfolio/%d/notification/maxLoss", portfolioId))
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8)
				.content(objectMapper.writeValueAsString(requestBodyMap)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("최대 손실율 알림이 활성화되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)));
	}

	@DisplayName("사용자는 포트폴리오의 최대손실금액 알람을 비활성화합니다.")
	@Test
	void modifyNotificationMaximumLossWithInActive() throws Exception {
		// given
		Portfolio portfolio = createPortfolio(createMember());
		long portfolioId = portfolio.getId();
		Map<String, String> requestBodyMap = new HashMap<>();
		requestBodyMap.put("isActive", "false");

		Map<String, Object> responseBodyMap = new HashMap<>();
		responseBodyMap.put("portfolioId", portfolioId);
		responseBodyMap.put("isActive", false);

		PortfolioNotificationUpdateResponse response = objectMapper.readValue(
			objectMapper.writeValueAsString(responseBodyMap), PortfolioNotificationUpdateResponse.class);
		given(portfolioRepository.findById(anyLong())).willReturn(Optional.of(portfolio));
		given(service.updateNotificationMaximumLoss(
			anyBoolean(),
			anyLong()
		)).willReturn(response);

		// when & then
		mockMvc.perform(put(String.format("/api/portfolio/%d/notification/maxLoss", portfolioId))
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8)
				.content(objectMapper.writeValueAsString(requestBodyMap)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("최대 손실율 알림이 비 활성화되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)));
	}
}
