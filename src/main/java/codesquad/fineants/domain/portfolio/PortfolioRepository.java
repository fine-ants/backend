package codesquad.fineants.domain.portfolio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import codesquad.fineants.domain.member.Member;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

	boolean existsByNameAndMember(String name, Member member);

	List<Portfolio> findAllByMemberIdOrderByIdDesc(Long memberId);

	List<Portfolio> findAllByMemberId(Long memberId);
}
