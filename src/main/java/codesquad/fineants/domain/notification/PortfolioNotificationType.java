package codesquad.fineants.domain.notification;

import lombok.Getter;

@Getter
public enum PortfolioNotificationType {
	TARGET_GAIN("목표 수익률"), MAX_LOSS("최대 손실율");

	private final String name;

	PortfolioNotificationType(String name) {
		this.name = name;
	}

	public static PortfolioNotificationType from(String target) {
		for (PortfolioNotificationType type : values()) {
			if (type.name.equals(target)) {
				return type;
			}
		}
		throw new IllegalArgumentException("잘못된 매개변수입니다. target=" + target);
	}
}
