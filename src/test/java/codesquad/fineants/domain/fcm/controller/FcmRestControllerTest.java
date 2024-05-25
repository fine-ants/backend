package codesquad.fineants.domain.fcm.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import codesquad.fineants.ControllerTestSupport;
import codesquad.fineants.domain.fcm.domain.dto.request.FcmRegisterRequest;
import codesquad.fineants.domain.fcm.domain.dto.response.FcmDeleteResponse;
import codesquad.fineants.domain.fcm.domain.dto.response.FcmRegisterResponse;
import codesquad.fineants.domain.fcm.service.FcmService;
import codesquad.fineants.global.util.ObjectMapperUtil;

@WebMvcTest(controllers = FcmRestController.class)
class FcmRestControllerTest extends ControllerTestSupport {

	@MockBean
	private FcmService fcmService;

	@Override
	protected Object initController() {
		return new FcmRestController(fcmService);
	}

	@DisplayName("사용자는 FCM 토큰을 등록한다")
	@Test
	void createToken() throws Exception {
		// given
		given(fcmService.createToken(any(FcmRegisterRequest.class), ArgumentMatchers.anyLong()))
			.willReturn(FcmRegisterResponse.builder()
				.fcmTokenId(1L)
				.build());

		Map<String, String> body = Map.of("fcmToken", "fcmToken");

		// when & then
		mockMvc.perform(post("/api/fcm/tokens")
				.content(ObjectMapperUtil.serialize(body))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("code").value(equalTo(201)))
			.andExpect(jsonPath("status").value(equalTo("Created")))
			.andExpect(jsonPath("message").value(equalTo("FCM 토큰을 성공적으로 등록하였습니다")))
			.andExpect(jsonPath("data.fcmTokenId").value(equalTo(1)));
	}

	@DisplayName("사용자는 유효하지 않은 형식의 토큰을 전달하여 등록할 수 없다")
	@Test
	void registerToken_whenInvalidFcmToken_thenResponse400Error() throws Exception {
		// given
		Map<String, String> body = new HashMap<>();
		body.put("fcmToken", null);

		// when
		mockMvc.perform(post("/api/fcm/tokens")
				.content(ObjectMapperUtil.serialize(body))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")));
	}

	@DisplayName("사용자는 FCM 토큰을 삭제한다")
	@Test
	void deleteToken() throws Exception {
		// given
		Long fcmTokenId = 1L;
		given(fcmService.deleteToken(ArgumentMatchers.anyLong(), ArgumentMatchers.anyLong()))
			.willReturn(FcmDeleteResponse.builder()
				.fcmTokenId(fcmTokenId)
				.build());

		// when & then
		mockMvc.perform(delete("/api/fcm/tokens/{fcmTokenId}", fcmTokenId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("FCM 토큰을 성공적으로 삭제하였습니다")));
	}
}
