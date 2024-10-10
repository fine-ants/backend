package co.fineants.api.domain.portfolio.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.gainhistory.domain.entity.PortfolioGainHistory;
import co.fineants.api.domain.gainhistory.repository.PortfolioGainHistoryRepository;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.holding.repository.PortfolioHoldingRepository;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.domain.portfolio.domain.dto.request.PortfolioCreateRequest;
import co.fineants.api.domain.portfolio.domain.dto.request.PortfolioModifyRequest;
import co.fineants.api.domain.portfolio.domain.dto.response.PortFolioCreateResponse;
import co.fineants.api.domain.portfolio.domain.dto.response.PortfolioModifyResponse;
import co.fineants.api.domain.portfolio.domain.dto.response.PortfoliosResponse;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.properties.PortfolioProperties;
import co.fineants.api.domain.portfolio.repository.PortfolioPropertiesRepository;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.purchasehistory.repository.PurchaseHistoryRepository;
import co.fineants.api.global.common.authorized.Authorized;
import co.fineants.api.global.common.authorized.service.PortfolioAuthorizedService;
import co.fineants.api.global.common.resource.ResourceId;
import co.fineants.api.global.common.resource.ResourceIds;
import co.fineants.api.global.errors.errorcode.MemberErrorCode;
import co.fineants.api.global.errors.errorcode.PortfolioErrorCode;
import co.fineants.api.global.errors.exception.BadRequestException;
import co.fineants.api.global.errors.exception.ConflictException;
import co.fineants.api.global.errors.exception.NotFoundResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PortFolioService {

	private final PortfolioRepository portfolioRepository;
	private final MemberRepository memberRepository;
	private final PortfolioHoldingRepository portfolioHoldingRepository;
	private final PurchaseHistoryRepository purchaseHistoryRepository;
	private final PortfolioGainHistoryRepository portfolioGainHistoryRepository;
	private final CurrentPriceRedisRepository currentPriceRedisRepository;
	private final PortfolioPropertiesRepository portfolioPropertiesRepository;
	private final PortfolioProperties properties;
	private final PortfolioCalculator calculator;

	@Transactional
	@Secured("ROLE_USER")
	public PortFolioCreateResponse createPortfolio(PortfolioCreateRequest request, Long memberId) {
		validateSecuritiesFirm(request.getSecuritiesFirm());

		Member member = findMember(memberId);

		validateUniquePortfolioName(request.getName(), member);
		Portfolio portfolio = request.toEntity(member, properties);
		return PortFolioCreateResponse.from(portfolioRepository.save(portfolio));
	}

	private Member findMember(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new NotFoundResourceException(MemberErrorCode.NOT_FOUND_MEMBER));
	}

	private void validateSecuritiesFirm(String securitiesFirm) {
		if (!portfolioPropertiesRepository.contains(securitiesFirm)) {
			throw new BadRequestException(PortfolioErrorCode.SECURITIES_FIRM_IS_NOT_CONTAINS);
		}
	}

	private void validateUniquePortfolioName(String name, Member member) {
		if (portfolioRepository.findByNameAndMember(name, member).isPresent()) {
			throw new ConflictException(PortfolioErrorCode.DUPLICATE_NAME);
		}
	}

	@Transactional
	@Authorized(serviceClass = PortfolioAuthorizedService.class)
	@Secured("ROLE_USER")
	public PortfolioModifyResponse updatePortfolio(PortfolioModifyRequest request, @ResourceId Long portfolioId,
		Long memberId) {
		log.info("포트폴리오 수정 서비스 요청 : request={}, portfolioId={}, memberId={}", request, portfolioId, memberId);
		Member member = findMember(memberId);
		Portfolio originalPortfolio = findPortfolio(portfolioId);
		Portfolio changePortfolio = request.toEntity(member, properties);

		if (!originalPortfolio.equalName(changePortfolio)) {
			validateUniquePortfolioName(changePortfolio.name(), member);
		}
		originalPortfolio.change(changePortfolio);

		log.info("변경된 포트폴리오 결과 : {}", originalPortfolio);
		return PortfolioModifyResponse.from(changePortfolio);
	}

	@Transactional
	@Authorized(serviceClass = PortfolioAuthorizedService.class)
	@Secured("ROLE_USER")
	public void deletePortfolio(@ResourceId Long portfolioId, Long memberId) {
		log.info("포트폴리오 삭제 서비스 요청 : portfolioId={}, memberId={}", portfolioId, memberId);

		Portfolio findPortfolio = findPortfolio(portfolioId);
		List<Long> portfolioHoldingIds = portfolioHoldingRepository.findAllByPortfolio(findPortfolio).stream()
			.map(PortfolioHolding::getId)
			.toList();

		int delPortfolioGainHistoryCnt = portfolioGainHistoryRepository.deleteAllByPortfolioId(portfolioId);
		log.info("포트폴리오 손익 내역 삭제 개수 : {}", delPortfolioGainHistoryCnt);

		int delTradeHistoryCnt = purchaseHistoryRepository.deleteAllByPortfolioHoldingIdIn(portfolioHoldingIds);
		log.info("매매이력 삭제 개수 : {}", delTradeHistoryCnt);

		int delPortfolioCnt = portfolioHoldingRepository.deleteAllByPortfolioId(findPortfolio.getId());
		log.info("포트폴리오 종목 삭제 개수 : {}", delPortfolioCnt);

		portfolioRepository.deleteById(findPortfolio.getId());
		log.info("포트폴리오 삭제 : delPortfolio={}", findPortfolio);
	}

	@Transactional
	@Authorized(serviceClass = PortfolioAuthorizedService.class)
	@Secured("ROLE_USER")
	public void deletePortfolios(@ResourceIds List<Long> portfolioIds) {
		for (Long portfolioId : portfolioIds) {
			Portfolio portfolio = findPortfolio(portfolioId);
			List<Long> portfolioStockIds = portfolioHoldingRepository.findAllByPortfolio(portfolio).stream()
				.map(PortfolioHolding::getId)
				.toList();
			purchaseHistoryRepository.deleteAllByPortfolioHoldingIdIn(portfolioStockIds);
			portfolioHoldingRepository.deleteAllByPortfolioId(portfolio.getId());
			portfolioGainHistoryRepository.deleteAllByPortfolioId(portfolioId);
			portfolioRepository.deleteById(portfolio.getId());
		}
	}

	@Secured("ROLE_USER")
	@Authorized(serviceClass = PortfolioAuthorizedService.class)
	public Portfolio findPortfolio(@ResourceId Long portfolioId) {
		return portfolioRepository.findById(portfolioId)
			.orElseThrow(() -> new NotFoundResourceException(PortfolioErrorCode.NOT_FOUND_PORTFOLIO));
	}

	@Secured("ROLE_USER")
	public PortfoliosResponse readMyAllPortfolio(Long memberId) {
		List<Portfolio> portfolios = portfolioRepository.findAllByMemberIdOrderByIdDesc(memberId);
		Map<Portfolio, PortfolioGainHistory> portfolioGainHistoryMap = portfolios.stream()
			.collect(Collectors.toMap(
				portfolio -> portfolio,
				portfolio ->
					portfolioGainHistoryRepository.findFirstByPortfolioAndCreateAtIsLessThanEqualOrderByCreateAtDesc(
							portfolio.getId(), LocalDateTime.now())
						.stream()
						.findFirst()
						.orElseGet(() -> PortfolioGainHistory.empty(portfolio))
			));

		return PortfoliosResponse.of(portfolios, portfolioGainHistoryMap, currentPriceRedisRepository, calculator);
	}
}
