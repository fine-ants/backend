package co.fineants.api.global.common.authorized.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.holding.repository.PortfolioHoldingRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PortfolioHoldingAuthorizedService implements AuthorizedService<PortfolioHolding> {

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
