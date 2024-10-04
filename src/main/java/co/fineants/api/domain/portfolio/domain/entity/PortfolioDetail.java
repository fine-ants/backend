package co.fineants.api.domain.portfolio.domain.entity;

import java.util.regex.Pattern;

import co.fineants.api.domain.portfolio.properties.PortfolioProperties;
import jakarta.persistence.Column;

public class PortfolioDetail {
	// 한글 또는 영문자로 시작하고 최대 100글자
	public static final String NAME_REGEXP = "^[가-힣a-zA-Z0-9][가-힣a-zA-Z0-9 ]{0,99}$";
	private static final Pattern NAME_PATTERN = Pattern.compile(NAME_REGEXP);
	@Column(name = "name", nullable = false)
	private String name;
	@Column(name = "securities_firm", nullable = false)
	private String securitiesFirm;

	private PortfolioDetail(String name, String securitiesFirm, PortfolioProperties properties) {
		if (name == null || !NAME_PATTERN.matcher(name).matches()) {
			throw new IllegalArgumentException("Invalid Portfolio name: " + name);
		}
		if (!properties.contains(securitiesFirm)) {
			throw new IllegalArgumentException("Unlisted securitiesFirm: " + securitiesFirm);
		}
		this.name = name;
		this.securitiesFirm = securitiesFirm;
	}

	public static PortfolioDetail of(String name, String securitiesFirm, PortfolioProperties properties) {
		return new PortfolioDetail(name, securitiesFirm, properties);
	}

	@Override
	public String toString() {
		return String.format("(name=%s, securitiesFirm=%s)", name, securitiesFirm);
	}
}
