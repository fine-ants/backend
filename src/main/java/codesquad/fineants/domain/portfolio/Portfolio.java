package codesquad.fineants.domain.portfolio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistory;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"member", "portfolioHoldings"})
@Entity
public class Portfolio {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String securitiesFirm;
	private Long budget;
	private Long targetGain;
	private Long maximumLoss;
	private Boolean targetGainIsActive;
	private Boolean maximumIsActive;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@OneToMany(mappedBy = "portfolio")
	private final List<PortfolioHolding> portfolioHoldings = new ArrayList<>();

	@Builder
	public Portfolio(Long id, String name, String securitiesFirm, Long budget, Long targetGain, Long maximumLoss,
		Boolean targetGainIsActive, Boolean maximumIsActive, Member member) {
		this.id = id;
		this.name = name;
		this.securitiesFirm = securitiesFirm;
		this.budget = budget;
		this.targetGain = targetGain;
		this.maximumLoss = maximumLoss;
		this.targetGainIsActive = targetGainIsActive;
		this.maximumIsActive = maximumIsActive;
		this.member = member;
	}

	//== 연관관계 메소드 ==//
	public void addPortfolioStock(PortfolioHolding portFolioHolding) {
		if (!portfolioHoldings.contains(portFolioHolding)) {
			portfolioHoldings.add(portFolioHolding);
		}
	}

	public void change(Portfolio changePortfolio) {
		this.name = changePortfolio.name;
		this.securitiesFirm = changePortfolio.securitiesFirm;
		this.budget = changePortfolio.budget;
		this.targetGain = changePortfolio.targetGain;
		this.maximumLoss = changePortfolio.maximumLoss;
	}

	public boolean hasAuthorization(Long memberId) {
		return member.getId().equals(memberId);
	}

	// 포트폴리오 총 손익 = 모든 종목 총 손익의 합계
	// 종목 총 손익 = (종목 현재가 - 종목 평균 매입가) * 개수
	// 종목 평균 매입가 = 종목의 총 투자 금액 / 총 주식 개수
	public long calculateTotalGain() {
		return portfolioHoldings.stream()
			.mapToLong(PortfolioHolding::calculateTotalGain)
			.sum();
	}

	// 포트폴리오 총 손익율 = (포트폴리오 총 손익 / 포트폴리오 총 투자 금액) * 100%
	public Integer calculateTotalGainRate() {
		long totalInvestmentAmount = calculateTotalInvestmentAmount();
		if (totalInvestmentAmount == 0) {
			return 0;
		}
		long totalGain = calculateTotalGain();
		log.info("totalGain : {}", totalGain);
		log.info("totalInvestmentAmount : {}", totalInvestmentAmount);
		log.info("result : {}", (int)(((double)calculateTotalGain() / (double)totalInvestmentAmount) * 100));
		return (int)(((double)calculateTotalGain() / (double)totalInvestmentAmount) * 100);
	}

	// 포트폴리오 총 투자 금액 = 각 종목들의 구입가들의 합계
	public Long calculateTotalInvestmentAmount() {
		return portfolioHoldings.stream()
			.mapToLong(PortfolioHolding::calculateTotalInvestmentAmount)
			.sum();
	}

	// 포트폴리오 당일 손익 = 모든 종목들의 평가 금액 합계 - 이전일 포트폴리오의 모든 종목들의 평가 금액 합계
	// 단, 이전일의 포트포릴오의 모든 종목들의 평가금액이 없는 경우 총 투자금액으로 뺀다
	public Long calculateDailyGain(PortfolioGainHistory previousHistory) {
		Long previousCurrentValuation = previousHistory.getCurrentValuation();
		if (previousCurrentValuation == 0) {
			return calculateTotalCurrentValuation() - calculateTotalInvestmentAmount();
		}
		return calculateTotalCurrentValuation() - previousCurrentValuation;
	}

	// 포트폴리오 평가 금액(현재 가치) = 모든 종목들의 평가금액 합계
	private Long calculateTotalCurrentValuation() {
		return portfolioHoldings.stream()
			.mapToLong(PortfolioHolding::calculateCurrentValuation)
			.sum();
	}

	// 포트폴리오 당일 손익율 = (당일 포트폴리오 가치 총합 - 이전 포트폴리오 가치 총합) / 이전 포트폴리오 가치 총합
	// 단, 이전 포트폴리오가 없는 경우 ((당일 포트폴리오 가치 총합 - 당일 포트폴리오 총 투자 금액) / 당일 포트폴리오 총 투자 금액) * 100%
	public Integer calculateDailyGainRate(PortfolioGainHistory prevHistory) {
		double prevCurrentValuation = prevHistory.getCurrentValuation();
		if (prevCurrentValuation == 0) {
			double currentValuation = calculateTotalCurrentValuation();
			double totalInvestmentAmount = calculateTotalInvestmentAmount();
			return (int)(((currentValuation - totalInvestmentAmount) / totalInvestmentAmount) * 100);
		}
		double currentValuation = calculateTotalCurrentValuation();
		return (int)(((currentValuation - prevCurrentValuation) / prevCurrentValuation) * 100);
	}

	// 포트폴리오 당월 예상 배당금 = 각 종목들에 해당월의 배당금 합계
	public Long calculateExpectedMonthlyDividend(LocalDateTime monthDateTime) {
		return portfolioHoldings.stream()
			.filter(portFolioStock -> portFolioStock.hasMonthlyDividend(monthDateTime))
			.mapToLong(portFolioStock -> portFolioStock.readDividend(monthDateTime))
			.findAny().orElse(0L);
	}

	public Integer getNumberOfShares() {
		return portfolioHoldings.size();
	}

	// 잔고 = 예산 - 총 투자 금액
	public Long calculateBalance() {
		return budget - calculateTotalInvestmentAmount();
	}

	// 총 연간 배당금 = 각 종목들의 연배당금의 합계
	public Long calculateTotalAnnualDividend() {
		return portfolioHoldings.stream()
			.mapToLong(PortfolioHolding::calculateAnnualDividend)
			.sum();
	}

	// 총 연간배당율 = 모든 종목들의 연 배당금 합계 / 모든 종목들의 총 가치의 합계) * 100
	public Integer calculateTotalAnnualDividendYield() {
		double currentValuation = calculateTotalCurrentValuation();
		if (currentValuation == 0) {
			return 0;
		}
		double totalAnnualDividend = calculateTotalAnnualDividend();
		return (int)((totalAnnualDividend / currentValuation) * 100);
	}

	// 최대손실율 = ((예산 - 최대손실금액) / 예산) * 100
	public Integer calculateMaximumLossRate() {
		return (int)(((double)(budget - maximumLoss) / (double)budget) * 100);
	}

	// 투자대비 연간 배당율 = 포트폴리오 총 연배당금 / 포트폴리오 투자금액 * 100
	public Integer calculateAnnualInvestmentDividendYield() {
		double totalInvestmentAmount = calculateTotalInvestmentAmount();
		if (totalInvestmentAmount == 0) {
			return 0;
		}
		double totalAnnualDividend = calculateTotalAnnualDividend();
		return (int)((totalAnnualDividend / totalInvestmentAmount) * 100);
	}

	public PortfolioGainHistory createPortfolioGainHistory(PortfolioGainHistory history) {
		Long totalGain = calculateTotalGain();
		Long dailyGain = calculateDailyGain(history);
		Long currentValuation = calculateTotalCurrentValuation();
		return PortfolioGainHistory.builder()
			.totalGain(totalGain)
			.dailyGain(dailyGain)
			.currentValuation(currentValuation)
			.portfolio(this)
			.build();
	}

	public List<PortfolioHolding> changeCurrentPriceFromHoldings(CurrentPriceManager manager) {
		List<PortfolioHolding> result = new ArrayList<>();
		for (PortfolioHolding portfolioHolding : portfolioHoldings) {
			String tickerSymbol = portfolioHolding.getStock().getTickerSymbol();
			if (manager.hasCurrentPrice(tickerSymbol)) {
				portfolioHolding.changeCurrentPrice(manager.getCurrentPrice(tickerSymbol));
				result.add(portfolioHolding);
			}
		}
		return result;
	}

	// 목표 수익률 = ((목표 수익 금액 - 예산) / 예산) * 100
	public Integer calculateTargetReturnRate() {
		return (int)(((double)(targetGain - budget) / (double)budget) * 100);
	}

	public void changeTargetGainNotification(Boolean isActive) {
		this.targetGainIsActive = isActive;
	}

	public void changeMaximumLossNotification(Boolean isActive) {
		this.maximumIsActive = isActive;
	}

	public boolean reachedTargetGain() {
		return budget + calculateTotalGain() >= targetGain;
	}

	public boolean reachedMaximumLoss() {
		return budget + calculateTotalGain() <= maximumLoss;
	}
}
