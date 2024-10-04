package co.fineants.api.domain.portfolio.domain.entity;

import co.fineants.api.domain.portfolio.properties.PortfolioProperties;
import jakarta.persistence.Column;

public class PortfolioDetail {
	@Column(name = "name", nullable = false)
	private String name;
	@Column(name = "securities_firm", nullable = false)
	private String securitiesFirm;

	private PortfolioDetail(String name, String securitiesFirm, PortfolioProperties properties) {
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
