package codesquad.fineants.global.init;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.domain.dividend.repository.StockDividendRepository;
import codesquad.fineants.domain.exchangerate.domain.entity.ExchangeRate;
import codesquad.fineants.domain.exchangerate.repository.ExchangeRateRepository;
import codesquad.fineants.domain.exchangerate.service.ExchangeRateService;
import codesquad.fineants.domain.kis.service.KisService;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.domain.entity.Role;
import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.domain.member.repository.RoleRepository;
import codesquad.fineants.domain.stock.domain.dto.response.StockDataResponse;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.domain.stock.repository.StockRepository;
import codesquad.fineants.domain.stock.service.StockCsvReader;
import codesquad.fineants.global.security.oauth.dto.MemberAuthentication;
import codesquad.fineants.infra.s3.service.AmazonS3DividendService;
import codesquad.fineants.infra.s3.service.AmazonS3StockService;

class SetupDataLoaderTest extends AbstractContainerBaseTest {

	@Autowired
	private SetupDataLoader setupDataLoader;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private AdminProperties adminProperties;

	@Autowired
	private ManagerProperties managerProperties;

	@Autowired
	private ExchangeRateRepository exchangeRateRepository;

	@Autowired
	private AmazonS3StockService amazonS3StockService;

	@Autowired
	private StockCsvReader stockCsvReader;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private AmazonS3DividendService amazonS3DividendService;

	@Autowired
	private StockDividendRepository stockDividendRepository;

	@MockBean
	private ExchangeRateService exchangeRateService;

	@MockBean
	private KisService kisService;

	@Transactional
	@DisplayName("서버는 권한 및 역할 등의 리소스들을 생성한다")
	@Test
	void setupResources() {
		// given
		List<Stock> stocks = writeStocks();

		doNothing().when(exchangeRateService).updateExchangeRates();
		doNothing().when(kisService).refreshCurrentPrice();
		doNothing().when(kisService).refreshClosingPrice();
		// when
		setupDataLoader.setupResources();
		// then
		assertThat(roleRepository.findAll())
			.hasSize(3)
			.containsExactlyInAnyOrder(
				Role.create("ROLE_ADMIN", "관리자"),
				Role.create("ROLE_MANAGER", "매니저"),
				Role.create("ROLE_USER", "회원")
			);
		assertThat(memberRepository.findAll())
			.hasSize(3)
			.containsExactlyInAnyOrder(
				Member.localMember(adminProperties.getEmail(), adminProperties.getNickname(),
					adminProperties.getPassword()),
				Member.localMember(managerProperties.getEmail(), managerProperties.getNickname(),
					managerProperties.getPassword()),
				Member.localMember("dragonbead95@naver.com", "일개미1111", "nemo1234@")
			);
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		MemberAuthentication memberAuthentication = MemberAuthentication.from(
			memberRepository.findMemberByEmailAndProvider(adminProperties.getEmail(), "local").orElseThrow()
		);
		assertThat(authentication)
			.extracting(Authentication::getPrincipal, Authentication::getCredentials)
			.containsExactly(memberAuthentication, Strings.EMPTY);
		assertThat(authentication.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.toUnmodifiableSet()))
			.containsExactlyElementsOf(memberAuthentication.getRoleSet());
		assertThat(exchangeRateRepository.findAll())
			.hasSize(2)
			.containsExactly(ExchangeRate.base("KRW"), ExchangeRate.noneBase("USD", 0.0007316));
		assertThat(stockRepository.findAll())
			.containsExactlyInAnyOrderElementsOf(stocks);
	}

	private List<Stock> writeStocks() {
		List<Stock> stocks = stockCsvReader.readStockCsv().stream()
			.map(StockDataResponse.StockInfo::toEntity)
			.toList();
		amazonS3StockService.writeStocks(stocks);
		return stocks;
	}
}
