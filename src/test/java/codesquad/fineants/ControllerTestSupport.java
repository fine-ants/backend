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

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.holding.domain.entity.PortfolioHolding;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.stock.domain.entity.Stock;
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

	protected Portfolio createPortfolio(Member member) {
		return createPortfolio(
			member,
			Money.won(1000000)
		);
	}

	protected Portfolio createPortfolio(Member member, Money budget) {
		return createPortfolio(
			member,
			"내꿈은 워렌버핏",
			budget,
			Money.won(1500000L),
			Money.won(900000L)
		);
	}

	protected Portfolio createPortfolio(Member member, String name, Money budget, Money targetGain, Money maximumLoss) {
		return createPortfolio(1L, member, name, budget, targetGain, maximumLoss);
	}

	protected Portfolio createPortfolio(Long id, Member member, String name, Money budget, Money targetGain,
		Money maximumLoss) {
		return Portfolio.active(
			id,
			name,
			"토스증권",
			budget,
			targetGain,
			maximumLoss,
			member
		);
	}

	protected PortfolioHolding createPortfolioHolding(Portfolio portfolio, Stock stock) {
		return PortfolioHolding.of(1L, portfolio, stock, null);
	}

	protected PortfolioHolding createPortfolioHolding(Portfolio portfolio, Stock stock, Money currentPrice) {
		return PortfolioHolding.of(1L, portfolio, stock, currentPrice);
	}

	protected abstract Object initController();
}
