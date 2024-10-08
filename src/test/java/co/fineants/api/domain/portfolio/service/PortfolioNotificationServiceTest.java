package co.fineants.api.domain.portfolio.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.holding.repository.PortfolioHoldingRepository;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.portfolio.domain.dto.request.PortfolioNotificationUpdateRequest;
import co.fineants.api.domain.portfolio.domain.dto.response.PortfolioNotificationUpdateResponse;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.purchasehistory.repository.PurchaseHistoryRepository;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.global.errors.errorcode.MemberErrorCode;
import co.fineants.api.global.errors.errorcode.PortfolioErrorCode;
import co.fineants.api.global.errors.exception.FineAntsException;

class PortfolioNotificationServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private PortfolioNotificationService service;

	@Autowired
	private PortfolioHoldingRepository portFolioHoldingRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private PurchaseHistoryRepository purchaseHistoryRepository;

	@DisplayName("사용자는 포트폴리오 목표수익금액 알림을 활성화한다")
	@Test
	void modifyPortfolioTargetGainNotification() throws JsonProcessingException {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());
		PortfolioHolding portfolioHolding = portFolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(3);
		Money purchasePerShare = Money.won(50000);
		String memo = "첫구매";
		purchaseHistoryRepository.save(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePerShare, memo, portfolioHolding));

		Map<String, Boolean> requestBodyMap = new HashMap<>();
		requestBodyMap.put("isActive", true);
		PortfolioNotificationUpdateRequest request = objectMapper.readValue(
			objectMapper.writeValueAsString(requestBodyMap), PortfolioNotificationUpdateRequest.class);

		setAuthentication(member);
		// when
		PortfolioNotificationUpdateResponse response = service.updateNotificationTargetGain(request.getIsActive(),
			portfolio.getId());

		// then
		assertAll(
			() -> assertThat(response).extracting("portfolioId", "isActive")
				.containsExactlyInAnyOrder(portfolio.getId(), true),
			() -> assertThat(
				portfolioRepository.findById(portfolio.getId()).orElseThrow().getTargetGainIsActive()).isTrue()
		);
	}

	@DisplayName("사용자는 포트폴리오의 목표수익금액이 0원인 경우 알림을 수정할 수 없다")
	@Test
	void updateNotificationTargetGain_whenTargetGainIsZero_thenNotUpdate() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(
			createPortfolio(member, "내꿈은 워렌버핏", Money.won(1000000L), Money.zero(), Money.zero()));

		setAuthentication(member);
		PortfolioNotificationUpdateRequest request = PortfolioNotificationUpdateRequest.active();
		// when
		Throwable throwable = catchThrowable(
			() -> service.updateNotificationTargetGain(request.getIsActive(), portfolio.getId()));

		// then
		assertThat(throwable)
			.isInstanceOf(FineAntsException.class)
			.hasMessage(PortfolioErrorCode.TARGET_GAIN_IS_ZERO_WITH_NOTIFY_UPDATE.getMessage());
	}

	@DisplayName("회원은 다른 회원 포트폴리오의 목표 수익률 알림 상태를 변경할 수 없다")
	@Test
	void modifyPortfolioTargetGainNotification_whenOtherMemberModify_thenThrowException() {
		// given
		Member member = memberRepository.save(createMember());
		Member hacker = memberRepository.save(createMember("hacker"));
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		PortfolioNotificationUpdateRequest request = PortfolioNotificationUpdateRequest.active();

		setAuthentication(hacker);
		// when
		Throwable throwable = catchThrowable(
			() -> service.updateNotificationTargetGain(request.getIsActive(), portfolio.getId()));

		// then
		assertThat(throwable)
			.isInstanceOf(FineAntsException.class)
			.hasMessage(MemberErrorCode.FORBIDDEN_MEMBER.getMessage());
	}

	@DisplayName("사용자는 포트폴리오 최대손실금액 알림을 활성화한다")
	@Test
	void modifyPortfolioMaximumLossNotification() throws JsonProcessingException {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());
		PortfolioHolding portfolioHolding = portFolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(3);
		Money purchasePerShare = Money.won(50000);
		String memo = "첫구매";
		purchaseHistoryRepository.save(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePerShare, memo, portfolioHolding));

		long portfolioId = portfolio.getId();
		Map<String, Boolean> requestBodyMap = new HashMap<>();
		requestBodyMap.put("isActive", true);
		PortfolioNotificationUpdateRequest request = objectMapper.readValue(
			objectMapper.writeValueAsString(requestBodyMap), PortfolioNotificationUpdateRequest.class);

		setAuthentication(member);
		// when
		PortfolioNotificationUpdateResponse response = service.updateNotificationMaximumLoss(request.getIsActive(),
			portfolioId);

		// then
		assertAll(
			() -> assertThat(response).extracting("portfolioId", "isActive")
				.containsExactlyInAnyOrder(portfolioId, true),
			() -> assertThat(portfolioRepository.findById(portfolioId).orElseThrow().getMaximumLossIsActive()).isTrue()
		);
	}

	@DisplayName("사용자는 포트폴리오의 최대손실금액이 0원이어서 알림을 수정할 수 없다")
	@Test
	void updateNotificationMaximumLoss() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(
			createPortfolio(member, "내 꿈은 워렌버핏", Money.won(1000000L), Money.won(1500000L), Money.zero()));

		setAuthentication(member);
		PortfolioNotificationUpdateRequest request = PortfolioNotificationUpdateRequest.active();
		// when
		Throwable throwable = catchThrowable(
			() -> service.updateNotificationMaximumLoss(request.getIsActive(), portfolio.getId()));

		// then
		assertThat(throwable)
			.isInstanceOf(FineAntsException.class)
			.hasMessage(PortfolioErrorCode.MAX_LOSS_IS_ZERO_WITH_NOTIFY_UPDATE.getMessage());
	}

	@DisplayName("회원은 다른 회원 포트포리오의 최대손실금액 알림 상태를 수정할 수 없다")
	@Test
	void updateNotificationMaximumLoss_whenOtherMemberModify_thenThrowException() {
		// given
		Member member = memberRepository.save(createMember());
		Member hacker = memberRepository.save(createMember("hacker"));
		Portfolio portfolio = portfolioRepository.save(
			createPortfolio(member, "내 꿈은 워렌버핏", Money.won(1000000L), Money.won(1500000L), Money.won(900000L)));

		setAuthentication(hacker);
		PortfolioNotificationUpdateRequest request = PortfolioNotificationUpdateRequest.active();
		// when
		Throwable throwable = catchThrowable(
			() -> service.updateNotificationMaximumLoss(request.getIsActive(), portfolio.getId()));

		// then
		assertThat(throwable)
			.isInstanceOf(FineAntsException.class)
			.hasMessage(MemberErrorCode.FORBIDDEN_MEMBER.getMessage());
	}
}
