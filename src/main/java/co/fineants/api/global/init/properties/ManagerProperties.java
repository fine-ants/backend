package co.fineants.api.global.init.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import lombok.Getter;

@Getter
@ConfigurationProperties(value = "member.manager")
public class ManagerProperties {
	private final String email;
	private final String nickname;
	private final String password;

	@ConstructorBinding
	public ManagerProperties(String email, String nickname, String password) {
		this.email = email;
		this.nickname = nickname;
		this.password = password;
	}
}
