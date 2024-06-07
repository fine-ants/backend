package codesquad.fineants.docs;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import codesquad.fineants.domain.common.count.Count;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.dividend.domain.entity.StockDividend;
import codesquad.fineants.domain.holding.domain.entity.PortfolioHolding;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.notification.domain.dto.response.NotifyMessage;
import codesquad.fineants.domain.notification.domain.dto.response.PortfolioNotifyMessage;
import codesquad.fineants.domain.notification.domain.dto.response.StockNotifyMessage;
import codesquad.fineants.domain.notification.domain.entity.Notification;
import codesquad.fineants.domain.notification.domain.entity.PortfolioNotification;
import codesquad.fineants.domain.notification.domain.entity.StockTargetPriceNotification;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.portfolio_gain_history.domain.entity.PortfolioGainHistory;
import codesquad.fineants.domain.purchasehistory.domain.entity.PurchaseHistory;
import codesquad.fineants.domain.stock.domain.entity.Market;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.domain.stock_target_price.domain.entity.StockTargetPrice;
import codesquad.fineants.domain.stock_target_price.domain.entity.TargetPriceNotification;
import codesquad.fineants.domain.watchlist.domain.entity.WatchList;
import codesquad.fineants.global.config.JacksonConfig;
import codesquad.fineants.global.security.oauth.dto.MemberAuthentication;
import codesquad.fineants.global.security.oauth.resolver.MemberAuthenticationArgumentResolver;

@ExtendWith(RestDocumentationExtension.class)
public abstract class RestDocsSupport {
	protected MockMvc mockMvc;

	protected MemberAuthenticationArgumentResolver memberAuthenticationArgumentResolver;

	@BeforeEach
	void setUp(RestDocumentationContextProvider provider) throws Exception {
		this.memberAuthenticationArgumentResolver = Mockito.mock(MemberAuthenticationArgumentResolver.class);
		this.mockMvc = MockMvcBuilders.standaloneSetup(initController())
			.apply(MockMvcRestDocumentation.documentationConfiguration(provider))
			.setCustomArgumentResolvers(memberAuthenticationArgumentResolver)
			.setMessageConverters(new MappingJackson2HttpMessageConverter(new JacksonConfig().objectMapper()))
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

	protected Member createMember() {
		return Member.localMember(
			1L,
			"kim1234@gmail.com",
			"일개미1234",
			"kim1234@",
			"profileUrl"
		);
	}

	protected Stock createSamsungStock() {
		return Stock.of("005930", "삼성전자보통주", "SamsungElectronics", "KR7005930003", "전기전자", Market.KOSPI);
	}

	protected Portfolio createPortfolio(Member member) {
		return Portfolio.active(
			1L,
			"내꿈은 워렌버핏",
			"토스증권",
			Money.won(1000000L),
			Money.won(1500000L),
			Money.won(900000L),
			member
		);
	}

	protected PortfolioHolding createPortfolioHolding(Portfolio portfolio, Stock stock) {
		return PortfolioHolding.of(1L, portfolio, stock, Money.won(60000L));
	}

	protected PurchaseHistory createPurchaseHistory(PortfolioHolding portfolioHolding, LocalDateTime purchaseDate) {
		return PurchaseHistory.create(1L, purchaseDate, Count.from(3L), Money.won(50000), "첫구매", portfolioHolding);
	}

	protected PortfolioGainHistory createEmptyPortfolioGainHistory(Portfolio portfolio) {
		return PortfolioGainHistory.empty(portfolio);
	}

	protected List<StockDividend> createStockDividendWith(Stock stock) {
		return List.of(
			createStockDividend(
				LocalDate.of(2022, 3, 31), LocalDate.of(2022, 3, 30),
				LocalDate.of(2022, 5, 17),
				stock
			),
			createStockDividend(
				LocalDate.of(2022, 6, 30), LocalDate.of(2022, 6, 29),
				LocalDate.of(2022, 8, 16),
				stock
			),
			createStockDividend(
				LocalDate.of(2022, 9, 30), LocalDate.of(2022, 9, 29),
				LocalDate.of(2022, 11, 15),
				stock
			),
			createStockDividend(
				LocalDate.of(2022, 12, 31), LocalDate.of(2022, 12, 30),
				LocalDate.of(2023, 4, 14),
				stock),
			createStockDividend(
				LocalDate.of(2023, 3, 31), LocalDate.of(2023, 3, 30),
				LocalDate.of(2023, 5, 17),
				stock),
			createStockDividend(
				LocalDate.of(2023, 6, 30), LocalDate.of(2023, 6, 29),
				LocalDate.of(2023, 8, 16),
				stock),
			createStockDividend(
				LocalDate.of(2023, 9, 30), LocalDate.of(2023, 9, 27),
				LocalDate.of(2023, 11, 20),
				stock),
			createStockDividend(
				LocalDate.of(2024, 3, 31), LocalDate.of(2024, 3, 30),
				LocalDate.of(2024, 5, 17),
				stock)
		);
	}

	protected StockDividend createStockDividend(LocalDate recordDate, LocalDate exDividendDate, LocalDate paymentDate,
		Stock stock) {
		return StockDividend.create(Money.won(361), recordDate, exDividendDate, paymentDate, stock);
	}

	protected Notification createPortfolioNotification(PortfolioNotifyMessage message, Member member) {
		return PortfolioNotification.newNotification(1L, message.getTitle(), message.getType(),
			message.getReferenceId(), message.getLink(), message.getName(), member);
	}

	protected Notification createStockNotification(StockNotifyMessage message, Member member) {
		return StockTargetPriceNotification.newNotification(
			1L,
			message.getStockName(),
			message.getTargetPrice(),
			message.getTitle(),
			message.getReferenceId(),
			message.getLink(),
			message.getTargetPriceNotificationId(),
			member);
	}

	protected StockTargetPrice createStockTargetPrice(Member member, Stock stock) {
		return StockTargetPrice.builder()
			.id(1L)
			.isActive(true)
			.member(member)
			.stock(stock)
			.build();
	}

	protected TargetPriceNotification createTargetPriceNotification(StockTargetPrice stockTargetPrice) {
		return TargetPriceNotification.builder()
			.id(1L)
			.targetPrice(Money.won(60000L))
			.stockTargetPrice(stockTargetPrice)
			.build();
	}

	protected PortfolioNotification createPortfolioTargetGainNotification(Portfolio portfolio, Member member) {
		NotifyMessage message = portfolio.getTargetGainMessage("token");
		return PortfolioNotification.newNotification(1L, message.getTitle(), message.getType(),
			message.getReferenceId(), message.getLink(), portfolio.getName(), member);
	}

	protected StockTargetPriceNotification createStockTargetPriceNotification(
		TargetPriceNotification targetPriceNotification, Member member) {
		StockNotifyMessage message = (StockNotifyMessage)targetPriceNotification.getTargetPriceMessage("token");
		return StockTargetPriceNotification.newNotification(
			1L,
			message.getStockName(),
			message.getTargetPrice(),
			message.getTitle(),
			message.getReferenceId(),
			message.getLink(),
			message.getTargetPriceNotificationId(),
			member
		);
	}

	protected WatchList createWatchList(Member member) {
		return WatchList.newWatchList(1L, "my watchlist 1", member);
	}

	protected MultipartFile createMockMultipartFile() {
		ClassPathResource classPathResource = new ClassPathResource("profile.jpeg");
		try {
			Path path = Paths.get(classPathResource.getURI());
			byte[] profile = Files.readAllBytes(path);
			return new MockMultipartFile("profileImageFile", "profile.jpeg", "image/jpeg",
				profile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected abstract Object initController();
}
