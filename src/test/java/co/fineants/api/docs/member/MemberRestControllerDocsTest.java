package co.fineants.api.docs.member;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;

import co.fineants.api.docs.RestDocsSupport;
import co.fineants.api.domain.member.controller.MemberRestController;
import co.fineants.api.domain.member.domain.dto.request.ProfileChangeServiceRequest;
import co.fineants.api.domain.member.domain.dto.response.ProfileChangeResponse;
import co.fineants.api.domain.member.domain.dto.response.ProfileResponse;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.service.MemberService;
import co.fineants.api.global.util.ObjectMapperUtil;

class MemberRestControllerDocsTest extends RestDocsSupport {

	private final MemberService memberService = Mockito.mock(MemberService.class);

	@Override
	protected Object initController() {
		return new MemberRestController(memberService);
	}

	@DisplayName("회원 프로필 조회 API")
	@Test
	void readProfile() throws Exception {
		// given
		given(memberService.readProfile(anyLong()))
			.willReturn(
				ProfileResponse.builder()
					.user(ProfileResponse.MemberProfile.builder()
						.id(1L)
						.nickname("일개미1234")
						.email("dragonbead95@naver.com")
						.profileUrl("profileUrl")
						.provider("local")
						.notificationPreferences(ProfileResponse.NotificationPreference.builder()
							.browserNotify(false)
							.targetGainNotify(true)
							.maxLossNotify(true)
							.targetPriceNotify(true)
							.build())
						.build())
					.build()
			);

		// when & then
		mockMvc.perform(get("/api/profile")
				.cookie(createTokenCookies()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("프로필 정보 조회에 성공하였습니다")))
			.andExpect(jsonPath("data.user.id").value(equalTo(1)))
			.andExpect(jsonPath("data.user.nickname").value(equalTo("일개미1234")))
			.andExpect(jsonPath("data.user.email").value(equalTo("dragonbead95@naver.com")))
			.andExpect(jsonPath("data.user.profileUrl").value(equalTo("profileUrl")))
			.andExpect(jsonPath("data.user.provider").value(equalTo("local")))
			.andExpect(jsonPath("data.user.notificationPreferences.browserNotify").value(equalTo(false)))
			.andExpect(jsonPath("data.user.notificationPreferences.targetGainNotify").value(equalTo(true)))
			.andExpect(jsonPath("data.user.notificationPreferences.maxLossNotify").value(equalTo(true)))
			.andExpect(jsonPath("data.user.notificationPreferences.targetPriceNotify").value(equalTo(true)))
			.andDo(
				document(
					"member-search",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(JsonFieldType.OBJECT)
							.description("응답 데이터"),
						fieldWithPath("data.user").type(JsonFieldType.OBJECT)
							.description("회원 정보"),
						fieldWithPath("data.user.id").type(JsonFieldType.NUMBER)
							.description("회원 등록번호"),
						fieldWithPath("data.user.nickname").type(JsonFieldType.STRING)
							.description("회원 닉네임"),
						fieldWithPath("data.user.email").type(JsonFieldType.STRING)
							.description("회원 이메일"),
						fieldWithPath("data.user.profileUrl").type(JsonFieldType.STRING)
							.description("회원 프로필 URL (NULL 허용)"),
						fieldWithPath("data.user.provider").type(JsonFieldType.STRING)
							.description("회원 가입 플랫폼"),
						fieldWithPath("data.user.notificationPreferences").type(JsonFieldType.OBJECT)
							.description("알림 설정"),
						fieldWithPath("data.user.notificationPreferences.browserNotify").type(JsonFieldType.BOOLEAN)
							.description("브라우저 알림 설정"),
						fieldWithPath("data.user.notificationPreferences.targetGainNotify").type(JsonFieldType.BOOLEAN)
							.description("목표 수익률 알림 설정"),
						fieldWithPath("data.user.notificationPreferences.maxLossNotify").type(JsonFieldType.BOOLEAN)
							.description("최대 손실율 알림 설정"),
						fieldWithPath("data.user.notificationPreferences.targetPriceNotify").type(JsonFieldType.BOOLEAN)
							.description("지정가 알림 설정")
					)
				)
			);

	}

	@DisplayName("회원 프로필 수정 API")
	@Test
	void changeProfile() throws Exception {
		// given
		Member member = createMember();

		Map<String, Object> profileInformationMap = Map.of("nickname", "일개미12345");
		MockMultipartFile profileInformation = new MockMultipartFile(
			"profileInformation",
			"profileInformation",
			MediaType.APPLICATION_JSON_VALUE,
			ObjectMapperUtil.serialize(profileInformationMap)
				.getBytes(StandardCharsets.UTF_8));

		member.changeNickname("일개미12345");
		given(memberService.changeProfile(ArgumentMatchers.any(ProfileChangeServiceRequest.class)))
			.willReturn(ProfileChangeResponse.from(member));

		// when & then
		mockMvc.perform(multipart(POST, "/api/profile")
				.file((MockMultipartFile)createMockMultipartFile())
				.file(profileInformation)
				.cookie(createTokenCookies()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("프로필이 수정되었습니다")))
			.andExpect(jsonPath("data.user.id").value(equalTo(1)))
			.andExpect(jsonPath("data.user.nickname").value(equalTo("일개미12345")))
			.andExpect(jsonPath("data.user.email").value(equalTo("kim1234@gmail.com")))
			.andExpect(jsonPath("data.user.profileUrl").value(equalTo("profileUrl")))
			.andExpect(jsonPath("data.user.provider").value(equalTo("local")))
			.andDo(
				document(
					"member-update",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestParts(
						partWithName("profileImageFile")
							.optional()
							.description("프로필 파일"),
						partWithName("profileInformation")
							.optional()
							.description("회원 수정 정보")
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
						fieldWithPath("data.user").type(JsonFieldType.OBJECT)
							.description("회원 정보"),
						fieldWithPath("data.user.id").type(JsonFieldType.NUMBER)
							.description("회원 등록번호"),
						fieldWithPath("data.user.nickname").type(JsonFieldType.STRING)
							.description("회원 닉네임"),
						fieldWithPath("data.user.email").type(JsonFieldType.STRING)
							.description("회원 이메일"),
						fieldWithPath("data.user.profileUrl").type(JsonFieldType.STRING)
							.description("회원 프로필 URL (NULL 허용)"),
						fieldWithPath("data.user.provider").type(JsonFieldType.STRING)
							.description("회원 가입 플랫폼")
					)
				)
			);
	}

	@DisplayName("비밀번호 변경 API")
	@Test
	void changePassword() throws Exception {
		// given
		Map<String, Object> body = Map.of(
			"currentPassword", "currentPassword",
			"newPassword", "newPassword",
			"newPasswordConfirm", "newPasswordConfirm"
		);

		// when & then
		mockMvc.perform(RestDocumentationRequestBuilders.put("/api/account/password")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body))
				.cookie(createTokenCookies()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("비밀번호를 성공적으로 변경했습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)))
			.andDo(
				document(
					"member_password-update",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestFields(
						fieldWithPath("currentPassword").type(JsonFieldType.STRING).description("현재 비밀번호"),
						fieldWithPath("newPassword").type(JsonFieldType.STRING).description("새 비밀번호"),
						fieldWithPath("newPasswordConfirm").type(JsonFieldType.STRING).description("새 비밀번호 확인")
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

	@DisplayName("회원 계정 삭제 API")
	@Test
	void deleteAccount() throws Exception {
		// given

		// when & then
		mockMvc.perform(delete("/api/account")
				.cookie(createTokenCookies()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("계정이 삭제되었습니다")))
			.andDo(
				document(
					"member-delete",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
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
