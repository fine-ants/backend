package co.fineants.api.global.common.authorized.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.stock_target_price.domain.entity.StockTargetPrice;
import co.fineants.api.domain.stock_target_price.repository.StockTargetPriceRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StockTargetPriceAuthorizedService implements AuthorizedService<StockTargetPrice> {
	private final StockTargetPriceRepository repository;

	@Override
	public List<StockTargetPrice> findResourceAllBy(List<Long> ids) {
		return repository.findAllById(ids);
	}

	@Override
	public boolean isAuthorized(Object resource, Long memberId) {
		return ((StockTargetPrice)resource).hasAuthorization(memberId);
	}
}
