package codesquad.fineants.domain.notification.service.provider;

import java.util.List;

import org.springframework.stereotype.Component;

import codesquad.fineants.domain.fcm.service.FcmService;
import codesquad.fineants.domain.fcm.service.FirebaseMessagingService;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;

@Component
public class FirebasePortfolioNotificationProvider extends FirebaseNotificationProvider<Portfolio> {

	public FirebasePortfolioNotificationProvider(FcmService fcmService,
		FirebaseMessagingService firebaseMessagingService) {
		super(fcmService, firebaseMessagingService);
	}

	@Override
	public List<String> findTokens(Portfolio target) {
		return fcmService.findTokens(target.getMember().getId());
	}
}
