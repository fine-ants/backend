package codesquad.fineants.domain.watch_list;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import codesquad.fineants.domain.member.Member;

public interface WatchListRepository extends JpaRepository<WatchList, Long> {
	List<WatchList> findByMember(Member member);
}
