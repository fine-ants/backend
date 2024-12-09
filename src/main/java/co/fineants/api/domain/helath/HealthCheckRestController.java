package co.fineants.api.domain.helath;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.security.PermitAll;

@RestController
public class HealthCheckRestController {
	@GetMapping("/health-check")
	@PermitAll
	public ResponseEntity<Void> healthCheck() {
		return ResponseEntity.ok().build();
	}
}
