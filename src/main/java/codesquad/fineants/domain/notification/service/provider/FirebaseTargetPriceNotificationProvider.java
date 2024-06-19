package codesquad.fineants.domain.notification.service.provider;

import java.util.List;

import org.springframework.stereotype.Component;

import codesquad.fineants.domain.fcm.service.FcmService;
import codesquad.fineants.domain.fcm.service.FirebaseMessagingService;
import codesquad.fineants.domain.stock_target_price.domain.entity.TargetPriceNotification;

@Component
public class FirebaseTargetPriceNotificationProvider extends FirebaseNotificationProvider<TargetPriceNotification> {

	public FirebaseTargetPriceNotificationProvider(FcmService fcmService,
		FirebaseMessagingService firebaseMessagingService) {
		super(fcmService, firebaseMessagingService);
	}

	@Override
	public List<String> findTokens(TargetPriceNotification target) {
		return fcmService.findTokens(target.getStockTargetPrice().getMember().getId());
	}
}
