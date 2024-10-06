package co.fineants.api.domain.portfolio.domain.entity;

import java.util.regex.Pattern;

import co.fineants.api.domain.portfolio.properties.PortfolioProperties;
import co.fineants.api.global.errors.errorcode.PortfolioErrorCode;
import co.fineants.api.global.errors.exception.portfolio.IllegalPortfolioNameArgumentException;
import co.fineants.api.global.errors.exception.portfolio.IllegalPortfolioSecuritiesFirmArgumentException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = {"name", "securitiesFirm"})
@Getter
public class PortfolioDetail {
	// 한글 또는 영문자로 시작하고 최대 100글자
	public static final String NAME_REGEXP = "^[가-힣a-zA-Z0-9][가-힣a-zA-Z0-9 ]{0,99}$";
	private static final Pattern NAME_PATTERN = Pattern.compile(NAME_REGEXP);
	// 포트폴리오 이름
	@Column(name = "name", nullable = false)
	private String name;
	// 증권사 이름
	@Column(name = "securities_firm", nullable = false)
	private String securitiesFirm;

	private PortfolioDetail(String name, String securitiesFirm, PortfolioProperties properties) {
		if (name == null || !NAME_PATTERN.matcher(name).matches()) {
			throw new IllegalPortfolioNameArgumentException(name, PortfolioErrorCode.INVALID_PORTFOLIO_NAME);
		}
		if (!properties.contains(securitiesFirm)) {
			throw new IllegalPortfolioSecuritiesFirmArgumentException(securitiesFirm,
				PortfolioErrorCode.UNLISTED_SECURITIES_FIRM);
		}
		this.name = name;
		this.securitiesFirm = securitiesFirm;
	}

	/**
	 * PortfolioDetail 객체를 생성하여 반환.
	 *
	 * @param name 포트폴리오 이름
	 * @param securitiesFirm 증권사 이름
	 * @param properties 증권사 목록이 담긴 포트폴리오 프로퍼티
	 * @return 포트폴리오 상세 정보 객체
	 * @throws IllegalPortfolioNameArgumentException 포트폴리오 이름의 형식이 유효하지 않으면 예외 발생
	 * @throws IllegalPortfolioSecuritiesFirmArgumentException 증권사 이름이 목록(properties)에 없으면 예외 발생
	 */
	public static PortfolioDetail of(String name, String securitiesFirm, PortfolioProperties properties) {
		return new PortfolioDetail(name, securitiesFirm, properties);
	}

	/**
	 * PortfolioDetail 객체의 name, securitiesFirm 필드를 변경한다.
	 *
	 * @param detail 변경하고자 하는 포트폴리오 상세 정보 객체
	 */
	public void change(PortfolioDetail detail) {
		this.name = detail.name;
		this.securitiesFirm = detail.securitiesFirm;
	}

	/**
	 * PortfolioDetail 객체의 name 필드가 같은지 비교한다.
	 *
	 * @param detail 비교하고자 하는 PortfolioDetail 객체
	 * @return name 필드가 같으면 true, 다르면 false
	 */
	public boolean equalName(PortfolioDetail detail) {
		return this.name.equals(detail.name);
	}

	/**
	 * 포트폴리오의 목표 수익율 달성 메시지를 생성하여 반환한다.
	 *
	 * @return 포트폴리오 목표 수익율 달성 메시지 (ex, 'portfolio1의 목표 수익률을 달성했습니다')
	 */
	public String getTargetGainReachMessage() {
		return String.format("%s의 목표 수익률을 달성했습니다", name);
	}

	/**
	 * 포트폴리오의 최대 손실율 도달 메시지를 생성하여 반환한다.
	 *
	 * @return 포트폴리오 최대 손실율 도달 메시지 (ex, 'portfolio1의 최대 손실율에 도달했습니다')
	 */
	public String getMaximumLossReachMessage() {
		return String.format("%s이(가) 최대 손실율에 도달했습니다", name);
	}

	@Override
	public String toString() {
		return String.format("(name=%s, securitiesFirm=%s)", name, securitiesFirm);
	}
}
