package codesquad.fineants.spring.api.fcm.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.anyLong;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import codesquad.fineants.spring.api.fcm.request.FcmRegisterRequest;
import codesquad.fineants.spring.api.fcm.response.FcmDeleteResponse;
import codesquad.fineants.spring.api.fcm.response.FcmRegisterResponse;
import codesquad.fineants.spring.api.fcm.service.FcmService;
import codesquad.fineants.spring.config.JpaAuditingConfiguration;
import codesquad.fineants.spring.util.ObjectMapperUtil;

@ActiveProfiles("test")
@WebMvcTest(controllers = FcmRestController.class)
@MockBean(JpaAuditingConfiguration.class)
class FcmRestControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private FcmRestController fcmRestController;

	@Autowired
	private GlobalExceptionHandler globalExceptionHandler;

	@MockBean
	private AuthPrincipalArgumentResolver authPrincipalArgumentResolver;

	@MockBean
	private FcmService fcmService;

	@BeforeEach
	void setup() {
		mockMvc = MockMvcBuilders.standaloneSetup(fcmRestController)
			.setControllerAdvice(globalExceptionHandler)
			.setCustomArgumentResolvers(authPrincipalArgumentResolver)
			.alwaysDo(print())
			.build();

		given(authPrincipalArgumentResolver.supportsParameter(ArgumentMatchers.any(MethodParameter.class)))
			.willReturn(true);
		given(authPrincipalArgumentResolver.resolveArgument(any(), any(), any(), any()))
			.willReturn(AuthMember.from(createMember()));
	}

	@DisplayName("사용자는 FCM 토큰을 등록한다")
	@Test
	void createToken() throws Exception {
		// given
		given(fcmService.createToken(
			any(FcmRegisterRequest.class),
			any(AuthMember.class)))
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
		given(fcmService.deleteToken(anyLong(), anyLong()))
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

	private Member createMember() {
		return Member.builder()
			.nickname("nemo1234")
			.email("dragonbead95@naver.com")
			.password("nemo1234")
			.provider("local")
			.build();
	}
}
