package co.fineants.api.domain.portfolio.domain.entity;

import jakarta.persistence.Column;

public class PortfolioDetail {
	@Column(name = "name", nullable = false)
	private String name;
	@Column(name = "securities_firm", nullable = false)
	private String securitiesFirm;

	private PortfolioDetail(String name, String securitiesFirm) {
		this.name = name;
		this.securitiesFirm = securitiesFirm;
	}

	public static PortfolioDetail of(String name, String securitiesFirm) {
		return new PortfolioDetail(name, securitiesFirm);
	}

	@Override
	public String toString() {
		return String.format("(name=%s, securitiesFirm=%s)", name, securitiesFirm);
	}
}
