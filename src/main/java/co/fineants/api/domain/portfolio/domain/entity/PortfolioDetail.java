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

	/**
	 * Return a PortfolioDetail instance.
	 *
	 * @param name the name 포트폴리오 이름
	 * @param securitiesFirm the securities firm 증권사 이름
	 * @param properties the properties 증권사 목록이 담긴 포트폴리오 프로퍼티
	 * @return the portfolio detail 포트폴리오 상세 정보 객체
	 * @throws IllegalArgumentException 포트폴리오 이름이 형식에 유효하지 않거나 증권사 이름이 properties 목록에 포함되지 않으면 예외 발생한다
	 */
	public static PortfolioDetail of(String name, String securitiesFirm, PortfolioProperties properties) {
		return new PortfolioDetail(name, securitiesFirm, properties);
	}

	@Override
	public String toString() {
		return String.format("(name=%s, securitiesFirm=%s)", name, securitiesFirm);
	}
}
