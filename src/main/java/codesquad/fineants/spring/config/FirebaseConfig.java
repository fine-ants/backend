package codesquad.fineants.spring.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

@Configuration
public class FirebaseConfig {
	@Bean
	public FirebaseMessaging firebaseMessaging() throws IOException {
		InputStream refreshToken = new ClassPathResource("secret/firebase/fineants-firebase-adminsdk.json")
			.getInputStream();
		FirebaseApp firebaseApp = null;
		List<FirebaseApp> firebaseApps = FirebaseApp.getApps();

		if (firebaseApps != null && !firebaseApps.isEmpty()) {
			for (FirebaseApp app : firebaseApps) {
				if (app.getName().equals(FirebaseApp.DEFAULT_APP_NAME)) {
					firebaseApp = app;
				}
			}
		} else {
			FirebaseOptions options = FirebaseOptions.builder()
				.setCredentials(GoogleCredentials.fromStream(refreshToken))
				.build();
			firebaseApp = FirebaseApp.initializeApp(options);
		}
		return FirebaseMessaging.getInstance(Objects.requireNonNull(firebaseApp));
	}
}
