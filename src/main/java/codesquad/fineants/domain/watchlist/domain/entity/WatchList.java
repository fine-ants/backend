package codesquad.fineants.domain.watchlist.domain.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import codesquad.fineants.domain.BaseEntity;
import codesquad.fineants.domain.member.domain.entity.Member;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
	public WatchList(LocalDateTime createAt, LocalDateTime modifiedAt, Long id, String name,
		Member member, List<WatchStock> watchStocks) {
		super(createAt, modifiedAt);
		this.id = id;
		this.name = name;
		this.member = member;
		this.watchStocks = watchStocks;
	}

	public void change(String name) {
		this.name = name;
	}
}
