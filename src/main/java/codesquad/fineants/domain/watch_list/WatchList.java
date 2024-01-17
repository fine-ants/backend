package codesquad.fineants.domain.watch_list;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import codesquad.fineants.domain.BaseEntity;
import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.watch_stock.WatchStock;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class WatchList extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	private String name;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@OneToMany(mappedBy = "watchList", cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<WatchStock> watchStocks = new ArrayList<>();

	@Builder
	public WatchList(String name, Member member){
		this.name = name;
		this.member = member;
	}
}
