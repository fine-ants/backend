package codesquad.fineants.global.common.authorized.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.fcm.domain.entity.FcmToken;
import codesquad.fineants.domain.fcm.repository.FcmRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FcmAuthorizedService implements AuthorizedService<FcmToken> {

	private final FcmRepository repository;

	@Override
	public List<FcmToken> findResourceAllBy(List<Long> ids) {
		return repository.findAllById(ids);
	}

	@Override
	public boolean isAuthorized(Object resource, Long memberId) {
		return ((FcmToken)resource).hasAuthorization(memberId);
	}
}
