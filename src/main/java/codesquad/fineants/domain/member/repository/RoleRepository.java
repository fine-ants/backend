package codesquad.fineants.domain.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import codesquad.fineants.domain.member.domain.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {

	@Query("select r from Role r where r.roleName = :roleName")
	Optional<Role> findRoleByRoleName(@Param("roleName") String roleName);
}
