package co.fineants.api.global.common.authorized.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.stock_target_price.domain.entity.TargetPriceNotification;
import co.fineants.api.domain.stock_target_price.repository.TargetPriceNotificationRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TargetPriceNotificationAuthorizedService implements AuthorizedService<TargetPriceNotification> {

	private final TargetPriceNotificationRepository repository;

	@Override
	public List<TargetPriceNotification> findResourceAllBy(List<Long> ids) {
		return repository.findAllById(ids);
	}

	@Override
	public boolean isAuthorized(Object resource, Long memberId) {
		return ((TargetPriceNotification)resource).hasAuthorization(memberId);
	}
}
