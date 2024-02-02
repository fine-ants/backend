package codesquad.fineants.spring.api.fcm.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.fcm_token.FcmRepository;
import codesquad.fineants.domain.fcm_token.FcmToken;
import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.spring.api.errors.errorcode.MemberErrorCode;
import codesquad.fineants.spring.api.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.fcm.request.FcmRegisterRequest;
import codesquad.fineants.spring.api.fcm.response.FcmRegisterResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FcmService {

	private final FcmRepository fcmRepository;
	private final MemberRepository memberRepository;

	@Transactional
	public FcmRegisterResponse registerToken(FcmRegisterRequest request, AuthMember authMember) {
		Member member = findMember(authMember);
		FcmToken saveFcmToken = fcmRepository.save(request.toEntity(member));
		return FcmRegisterResponse.from(saveFcmToken);
	}

	private Member findMember(AuthMember authMember) {
		return memberRepository.findById(authMember.getMemberId())
			.orElseThrow(() -> new NotFoundResourceException(MemberErrorCode.NOT_FOUND_MEMBER));
	}
}
