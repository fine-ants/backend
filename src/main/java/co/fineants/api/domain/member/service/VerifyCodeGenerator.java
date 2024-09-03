package co.fineants.api.domain.member.service;

import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class VerifyCodeGenerator {
	private final int len;
	private final int limit;
	private final Random random;

	public VerifyCodeGenerator(
		@Value("${member.verifyCode.len}") int len,
		@Value("${member.verifyCode.limit}") int limit) {
		this.len = len;
		this.limit = limit;
		this.random = new Random();
	}

	public String generate() {
		int code = random.nextInt(limit); // Generates a number between 0 and 999999
		String format = "%0" + len + "d";
		return String.format(format, code);
	}
}
