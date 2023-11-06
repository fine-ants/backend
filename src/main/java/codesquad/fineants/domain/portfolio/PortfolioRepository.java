package codesquad.fineants.domain.portfolio;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import codesquad.fineants.domain.member.Member;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

	boolean existsByNameAndMember(String name, Member member);

	Page<Portfolio> findAllByMemberIdAndIdLessThanOrderByIdDesc(Long memberId, Long id, Pageable pageable);

	List<Portfolio> findAllByMemberId(Long memberId);
}
