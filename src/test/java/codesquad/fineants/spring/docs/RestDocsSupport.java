package codesquad.fineants.spring.docs;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.notification.Notification;
import codesquad.fineants.domain.notification.PortfolioNotification;
import codesquad.fineants.domain.notification.StockTargetPriceNotification;
import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.oauth.support.AuthPrincipalArgumentResolver;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistory;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import codesquad.fineants.domain.purchase_history.PurchaseHistory;
import codesquad.fineants.domain.stock.Market;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.stock_dividend.StockDividend;
import codesquad.fineants.domain.stock_target_price.StockTargetPrice;
import codesquad.fineants.domain.target_price_notification.TargetPriceNotification;
import codesquad.fineants.domain.watch_list.WatchList;
import codesquad.fineants.spring.api.notification.response.NotifyMessage;
import codesquad.fineants.spring.api.notification.response.PortfolioNotifyMessage;
import codesquad.fineants.spring.api.notification.response.StockNotifyMessage;
import codesquad.fineants.spring.config.JacksonConfig;
import codesquad.fineants.spring.intercetpor.LogoutInterceptor;

@ExtendWith(RestDocumentationExtension.class)
public abstract class RestDocsSupport {
	protected MockMvc mockMvc;

	protected AuthPrincipalArgumentResolver authPrincipalArgumentResolver;

	@BeforeEach
	void setUp(RestDocumentationContextProvider provider) {
		this.authPrincipalArgumentResolver = Mockito.mock(AuthPrincipalArgumentResolver.class);
		this.mockMvc = MockMvcBuilders.standaloneSetup(initController())
			.apply(MockMvcRestDocumentation.documentationConfiguration(provider))
			.setCustomArgumentResolvers(authPrincipalArgumentResolver)
			.setMessageConverters(new MappingJackson2HttpMessageConverter(new JacksonConfig().objectMapper()))
			.addMappedInterceptors(new String[] {"/api/auth/logout"}, new LogoutInterceptor())
			.build();

		given(authPrincipalArgumentResolver.supportsParameter(ArgumentMatchers.any(MethodParameter.class)))
			.willReturn(true);
		given(authPrincipalArgumentResolver.resolveArgument(any(), any(), any(), any()))
			.willReturn(AuthMember.from(createMember()));
	}

	protected Member createMember() {
		return Member.builder()
			.id(1L)
			.nickname("일개미1234")
			.email("kim1234@gmail.com")
			.password("kim1234@")
			.provider("local")
			.profileUrl("profileUrl")
			.build();
	}

	protected Stock createStock() {
		return Stock.builder()
			.companyName("삼성전자보통주")
			.tickerSymbol("005930")
			.companyNameEng("SamsungElectronics")
			.stockCode("KR7005930003")
			.sector("전기전자")
			.market(Market.KOSPI)
			.build();
	}

	protected Portfolio createPortfolio(Member member) {
		return Portfolio.builder()
			.id(1L)
			.name("내꿈은 워렌버핏")
			.securitiesFirm("토스")
			.budget(1000000L)
			.targetGain(1500000L)
			.maximumLoss(900000L)
			.targetGainIsActive(true)
			.maximumLossIsActive(true)
			.member(member)
			.build();
	}

	protected PortfolioHolding createPortfolioHolding(Portfolio portfolio, Stock stock) {
		return PortfolioHolding.builder()
			.id(1L)
			.portfolio(portfolio)
			.stock(stock)
			.currentPrice(60000L)
			.build();
	}

	protected PurchaseHistory createPurchaseHistory(PortfolioHolding portfolioHolding, LocalDateTime purchaseDate) {
		return PurchaseHistory.builder()
			.id(1L)
			.purchaseDate(purchaseDate)
			.numShares(3L)
			.purchasePricePerShare(50000.0)
			.memo("첫구매")
			.portfolioHolding(portfolioHolding)
			.build();
	}

	protected PortfolioGainHistory createEmptyPortfolioGainHistory() {
		return PortfolioGainHistory.empty();
	}

	protected StockDividend createStockDividend(LocalDate exDividendDate, LocalDate recordDate, LocalDate paymentDate,
		Stock stock) {
		return StockDividend.builder()
			.dividend(361L)
			.exDividendDate(exDividendDate)
			.recordDate(recordDate)
			.paymentDate(paymentDate)
			.stock(stock)
			.build();
	}

	protected List<StockDividend> createStockDividendWith(Stock stock) {
		return List.of(
			createStockDividend(
				LocalDate.of(2022, 3, 30),
				LocalDate.of(2022, 3, 31),
				LocalDate.of(2022, 5, 17),
				stock
			),
			createStockDividend(
				LocalDate.of(2022, 6, 29),
				LocalDate.of(2022, 6, 30),
				LocalDate.of(2022, 8, 16),
				stock
			),
			createStockDividend(
				LocalDate.of(2022, 9, 29),
				LocalDate.of(2022, 9, 30),
				LocalDate.of(2022, 11, 15),
				stock
			),
			createStockDividend(
				LocalDate.of(2022, 12, 30),
				LocalDate.of(2022, 12, 31),
				LocalDate.of(2023, 4, 14),
				stock),
			createStockDividend(
				LocalDate.of(2023, 3, 30),
				LocalDate.of(2023, 3, 31),
				LocalDate.of(2023, 5, 17),
				stock),
			createStockDividend(
				LocalDate.of(2023, 6, 29),
				LocalDate.of(2023, 6, 30),
				LocalDate.of(2023, 8, 16),
				stock),
			createStockDividend(
				LocalDate.of(2023, 9, 27),
				LocalDate.of(2023, 9, 30),
				LocalDate.of(2023, 11, 20),
				stock),
			createStockDividend(
				LocalDate.of(2024, 3, 30),
				LocalDate.of(2024, 3, 31),
				LocalDate.of(2024, 5, 17),
				stock)
		);
	}

	protected Notification createPortfolioNotification(PortfolioNotifyMessage message, Member member) {
		return PortfolioNotification.builder()
			.id(1L)
			.isRead(false)
			.type(message.getType())
			.title(message.getTitle())
			.referenceId(message.getReferenceId())
			.link(message.getLink())
			.name(message.getName())
			.member(member)
			.build();
	}

	protected Notification createStockNotification(StockNotifyMessage message, Member member) {
		return StockTargetPriceNotification.builder()
			.id(1L)
			.isRead(false)
			.type(message.getType())
			.title(message.getTitle())
			.referenceId(message.getReferenceId())
			.link(message.getLink())
			.stockName(message.getStockName())
			.targetPrice(message.getTargetPrice())
			.targetPriceNotificationId(message.getTargetPriceNotificationId())
			.member(member)
			.build();
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
			.targetPrice(60000L)
			.stockTargetPrice(stockTargetPrice)
			.build();
	}

	protected PortfolioNotification createPortfolioTargetGainNotification(Portfolio portfolio, Member member) {
		NotifyMessage message = portfolio.getTargetGainMessage("token");
		return PortfolioNotification.builder()
			.id(1L)
			.name(portfolio.getName())
			.title(message.getTitle())
			.isRead(false)
			.type(message.getType())
			.referenceId(message.getReferenceId())
			.link(message.getLink())
			.createAt(LocalDateTime.of(2024, 1, 24, 10, 10, 10))
			.member(member)
			.build();
	}

	protected StockTargetPriceNotification createStockTargetPriceNotification(
		TargetPriceNotification targetPriceNotification, Member member) {
		StockNotifyMessage message = (StockNotifyMessage)targetPriceNotification.getTargetPriceMessage("token");
		return StockTargetPriceNotification.builder()
			.id(1L)
			.stockName(message.getStockName())
			.targetPrice(message.getTargetPrice())
			.targetPriceNotificationId(message.getTargetPriceNotificationId())
			.title(message.getTitle())
			.isRead(false)
			.type(message.getType())
			.referenceId(message.getReferenceId())
			.link(message.getLink())
			.createAt(LocalDateTime.of(2024, 1, 24, 10, 10, 10))
			.member(member)
			.build();
	}

	protected WatchList createWatchList(Member member) {
		return WatchList.builder()
			.id(1L)
			.name("my watchlist 1")
			.member(member)
			.createAt(LocalDateTime.now())
			.member(member)
			.build();
	}

	protected MultipartFile createMockMultipartFile() {
		ClassPathResource classPathResource = new ClassPathResource("profile.jpeg");
		Path path = null;
		try {
			path = Paths.get(classPathResource.getURI());
			byte[] profile = Files.readAllBytes(path);
			return new MockMultipartFile("profileImageFile", "profile.jpeg", "image/jpeg",
				profile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected MultipartFile createEmptyMockMultipartFile() {
		return new MockMultipartFile("profileImageFile", new byte[] {});
	}

	protected abstract Object initController();
}
