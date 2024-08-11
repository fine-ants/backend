package codesquad.fineants.domain.member.domain.entity;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "ROLE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = {"roleName", "roleDescription"})
@ToString
@Getter
public class Role {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "role_id")
	private Long id;

	@Column(name = "role_name", nullable = false)
	private String roleName;

	@Column(name = "role_description")
	private String roleDescription;

	public static Role create(String roleName, String roleDesc) {
		return new Role(null, roleName, roleDesc);
	}

	public SimpleGrantedAuthority toSimpleGrantedAuthority() {
		return new SimpleGrantedAuthority(roleName);
	}
}
