package codesquad.fineants.spring.api.portfolio_stock;

import java.util.Random;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RandomColorGenerator {
	private static final Random random = new Random();

	public String generateRandomColor() {
		// 랜덤 RGB 값 생성
		int red = random.nextInt(256);
		int green = random.nextInt(256);
		int blue = random.nextInt(256);

		// RGB 값을 16진수 문자열로 변환
		String hexRed = Integer.toHexString(red).toUpperCase();
		String hexGreen = Integer.toHexString(green).toUpperCase();
		String hexBlue = Integer.toHexString(blue).toUpperCase();

		// 16진수 문자열을 2자리로 만들기
		hexRed = padZero(hexRed);
		hexGreen = padZero(hexGreen);
		hexBlue = padZero(hexBlue);

		// 최종 색상 문자열 생성
		return "#" + hexRed + hexGreen + hexBlue;
	}

	private String padZero(String hex) {
		return hex.length() == 1 ? "0" + hex : hex;
	}
}
