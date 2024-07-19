package codesquad.fineants.domain.portfolio.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.portfolio.repository.PortfolioRepository;
import codesquad.fineants.global.common.authorized.AuthorizeService;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PortfolioAuthorizeService implements AuthorizeService<Portfolio> {
	private final PortfolioRepository repository;

	@Override
	public List<Portfolio> findResourceAllBy(List<Long> ids) {
		return repository.findAllById(ids);
	}

	@Override
	public boolean isAuthorized(Object resource, Long memberId) {
		return ((Portfolio)resource).hasAuthorization(memberId);
	}
}
