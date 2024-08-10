package codesquad.fineants.global.init;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@ConfigurationProperties(prefix = "role")
public class RoleProperties {

	private final Role admin;
	private final Role manager;
	private final Role user;

	@ConstructorBinding
	public RoleProperties(Role admin, Role manager, Role user) {
		this.admin = admin;
		this.manager = manager;
		this.user = user;
	}

	@Getter
	@RequiredArgsConstructor
	public static class Role {
		private String roleName;
		private String roleDesc;

		@ConstructorBinding
		public Role(String roleName, String roleDesc) {
			this.roleName = roleName;
			this.roleDesc = roleDesc;
		}
	}

	public String getAdminRoleName() {
		return getAdmin().getRoleName();
	}

	public String getAdminRoleDesc() {
		return getAdmin().getRoleDesc();
	}

	public String getManagerRoleName() {
		return getManager().getRoleName();
	}

	public String getManagerRoleDesc() {
		return getManager().getRoleDesc();
	}

	public String getUserRoleName() {
		return getUser().getRoleName();
	}

	public String getUserRoleDesc() {
		return getUser().getRoleDesc();
	}
}
