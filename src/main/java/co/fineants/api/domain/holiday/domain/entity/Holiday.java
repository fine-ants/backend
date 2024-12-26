package co.fineants.api.domain.holiday.domain.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Holiday {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "base_date", nullable = false)
	private LocalDate baseDate;

	@Column(name = "is_open", nullable = false)
	private Boolean isOpen;

	@Builder(access = AccessLevel.PRIVATE, toBuilder = true)
	private Holiday(Long id, LocalDate baseDate, Boolean isOpen) {
		this.id = id;
		this.baseDate = baseDate;
		this.isOpen = isOpen;
	}

	public static Holiday close(LocalDate baseDate) {
		return Holiday.builder()
			.baseDate(baseDate)
			.isOpen(false)
			.build();
	}
}
