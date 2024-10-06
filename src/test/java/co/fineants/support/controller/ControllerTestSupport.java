package co.fineants.support.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.gainhistory.domain.entity.PortfolioGainHistory;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.domain.entity.MemberProfile;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.domain.entity.PortfolioDetail;
import co.fineants.api.domain.portfolio.domain.entity.PortfolioFinancial;
import co.fineants.api.domain.portfolio.properties.PortfolioProperties;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import co.fineants.api.domain.stock.domain.entity.Market;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.global.config.JacksonConfig;
import co.fineants.api.global.config.JpaAuditingConfiguration;
import co.fineants.api.global.config.SpringConfig;
import co.fineants.api.global.errors.handler.GlobalExceptionHandler;
import co.fineants.api.global.security.oauth.dto.MemberAuthentication;
import co.fineants.api.global.security.oauth.resolver.MemberAuthenticationArgumentResolver;

@ActiveProfiles("test")
@Import(value = {SpringConfig.class, JacksonConfig.class})
@MockBean(JpaAuditingConfiguration.class)
public abstract class ControllerTestSupport {

	protected MockMvc mockMvc;

	@Autowired
	private GlobalExceptionHandler globalExceptionHandler;

	@Autowired
	protected ObjectMapper objectMapper;

	@Autowired
	private PortfolioProperties properties;

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
		MemberProfile profile = MemberProfile.localMemberProfile("dragonbead95@naver.com", "nemo1234", "nemo1234@",
			"profileUrl");
		return Member.localMember(1L, profile);
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
		PortfolioDetail detail = PortfolioDetail.of(name, "토스증권", properties);
		PortfolioFinancial financial = PortfolioFinancial.of(budget, targetGain, maximumLoss);
		return Portfolio.active(id, detail, financial, member);
	}

	protected PortfolioHolding createPortfolioHolding(Portfolio portfolio, Stock stock) {
		return PortfolioHolding.of(1L, portfolio, stock, null);
	}

	protected PortfolioHolding createPortfolioHolding(Portfolio portfolio, Stock stock, Money currentPrice) {
		return PortfolioHolding.of(1L, portfolio, stock, currentPrice);
	}

	protected Stock createSamsungStock() {
		return Stock.of("005930", "삼성전자보통주", "SamsungElectronics", "KR7005930003", "전기전자", Market.KOSPI);
	}

	protected StockDividend createStockDividend(LocalDate recordDate, LocalDate paymentDate,
		Stock stock) {
		return StockDividend.create(Money.won(361), recordDate, paymentDate, stock);
	}

	protected PurchaseHistory createPurchaseHistory(Long id, LocalDateTime purchaseDate, Count count,
		Money purchasePricePerShare, String memo, PortfolioHolding portfolioHolding) {
		return PurchaseHistory.create(id, purchaseDate, count, purchasePricePerShare, memo, portfolioHolding);
	}

	protected PortfolioGainHistory createEmptyPortfolioGainHistory(Portfolio portfolio) {
		return PortfolioGainHistory.empty(portfolio);
	}

	protected abstract Object initController();
}
