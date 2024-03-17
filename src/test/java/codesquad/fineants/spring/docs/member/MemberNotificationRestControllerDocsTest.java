package codesquad.fineants.spring.docs.member;

import org.mockito.Mockito;

import codesquad.fineants.spring.api.member.controller.MemberNotificationRestController;
import codesquad.fineants.spring.api.member.service.MemberNotificationPreferenceService;
import codesquad.fineants.spring.api.member.service.MemberNotificationService;
import codesquad.fineants.spring.docs.RestDocsSupport;

public class MemberNotificationRestControllerDocsTest extends RestDocsSupport {

	private final MemberNotificationService service = Mockito.mock(MemberNotificationService.class);
	private final MemberNotificationPreferenceService preferenceService = Mockito.mock(
		MemberNotificationPreferenceService.class);

	@Override
	protected Object initController() {
		return new MemberNotificationRestController(service, preferenceService);
	}
}
