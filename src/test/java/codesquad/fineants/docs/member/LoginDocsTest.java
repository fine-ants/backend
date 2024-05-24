package codesquad.fineants.docs.member;

import static org.hamcrest.Matchers.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.config.BeanIds;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;

import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.domain.entity.MemberRole;
import codesquad.fineants.domain.member.domain.entity.Role;
import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.domain.member.repository.MemberRoleRepository;
import codesquad.fineants.domain.member.repository.RoleRepository;
import codesquad.fineants.global.util.ObjectMapperUtil;
import jakarta.servlet.ServletException;

@ExtendWith(RestDocumentationExtension.class)
public class LoginDocsTest extends AbstractContainerBaseTest {
	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private MemberRoleRepository memberRoleRepository;

	protected MockMvc mockMvc;

	@BeforeEach
	void setUp(RestDocumentationContextProvider provider) throws ServletException {
		DelegatingFilterProxy delegatingFilterProxy = new DelegatingFilterProxy();
		delegatingFilterProxy.init(new MockFilterConfig(webApplicationContext.getServletContext(),
			BeanIds.SPRING_SECURITY_FILTER_CHAIN));
		this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
			.apply(MockMvcRestDocumentation.documentationConfiguration(provider))
			.addFilter(delegatingFilterProxy)
			.alwaysDo(print())
			.build();
	}

	@AfterEach
	void tearDown() {
		memberRoleRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
		roleRepository.deleteAllInBatch();
	}

	@DisplayName("사용자가 일반 로그인한다")
	@Test
	void login() throws Exception {
		// given
		Role userRole = roleRepository.save(Role.create("ROLE_USER", "유저"));
		Member member = memberRepository.save(createMember());
		MemberRole memberRole = MemberRole.create(member, userRole);
		member.addMemberRole(memberRole);
		memberRepository.save(member);

		Map<String, Object> body = Map.of(
			"email", "dragonbead95@naver.com",
			"password", "nemo1234@"
		);
		// when & then
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(body)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("로그인에 성공하였습니다.")))
			.andExpect(jsonPath("data.jwt.accessToken").isNotEmpty())
			.andExpect(jsonPath("data.jwt.refreshToken").isNotEmpty())
			.andDo(
				document(
					"member-login",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					requestFields(
						fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
						fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
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
						fieldWithPath("data.jwt").type(JsonFieldType.OBJECT)
							.description("Json Web Token"),
						fieldWithPath("data.jwt.accessToken").type(JsonFieldType.STRING)
							.description("액세스 토큰"),
						fieldWithPath("data.jwt.refreshToken").type(JsonFieldType.STRING)
							.description("리프레시 토큰")
					)
				)
			);
	}
}
