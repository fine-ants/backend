package codesquad.fineants.domain.holding.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.holding.domain.entity.PortfolioHolding;
import codesquad.fineants.domain.holding.repository.PortfolioHoldingRepository;
import codesquad.fineants.global.common.authorized.AuthorizeService;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PortfolioHoldingAuthorizeService implements AuthorizeService<PortfolioHolding> {

	private final PortfolioHoldingRepository repository;

	@Override
	public List<PortfolioHolding> findResourceAllBy(List<Long> ids) {
		return repository.findAllById(ids);
	}

	@Override
	public boolean isAuthorized(Object resource, Long memberId) {
		return ((PortfolioHolding)resource).hasAuthorization(memberId);
	}
}
