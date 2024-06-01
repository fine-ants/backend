package codesquad.fineants;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.global.config.JacksonConfig;
import codesquad.fineants.global.config.JpaAuditingConfiguration;
import codesquad.fineants.global.config.SpringConfig;
import codesquad.fineants.global.errors.handler.GlobalExceptionHandler;
import codesquad.fineants.global.security.oauth.dto.MemberAuthentication;
import codesquad.fineants.global.security.oauth.resolver.MemberAuthenticationArgumentResolver;

@ActiveProfiles("test")
@Import(value = {SpringConfig.class, JacksonConfig.class})
@MockBean(JpaAuditingConfiguration.class)
public abstract class ControllerTestSupport {

	protected MockMvc mockMvc;

	@Autowired
	private GlobalExceptionHandler globalExceptionHandler;

	@Autowired
	protected ObjectMapper objectMapper;

	@MockBean
	protected MemberAuthenticationArgumentResolver memberAuthenticationArgumentResolver;

	@BeforeEach
	void setup() throws Exception {
		mockMvc = MockMvcBuilders.standaloneSetup(initController())
			.setControllerAdvice(globalExceptionHandler)
			.setCustomArgumentResolvers(memberAuthenticationArgumentResolver)
			.setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
			.alwaysDo(print())
			.build();

		given(memberAuthenticationArgumentResolver.supportsParameter(ArgumentMatchers.any(MethodParameter.class)))
			.willReturn(true);
		given(memberAuthenticationArgumentResolver.resolveArgument(any(), any(), any(), any()))
			.willReturn(createMemberAuthentication());
	}

	private MemberAuthentication createMemberAuthentication() {
		return MemberAuthentication.create(
			1L,
			"dragonbead95@naver.com",
			"일개미1234",
			"local",
			"profileUrl",
			Set.of("ROLE_USER")
		);
	}

	protected static Member createMember() {
		return Member.localMember(
			1L,
			"dragonbead95@naver.com",
			"nemo1234",
			"nemo1234@",
			"profileUrl"
		);
	}

	protected abstract Object initController();
}
