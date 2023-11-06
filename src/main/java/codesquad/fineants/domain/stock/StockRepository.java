package codesquad.fineants.domain.stock;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockRepository extends JpaRepository<Stock, Long> {

	Optional<Stock> findByTickerSymbol(String tickerSymbol);

	@Query("select s from Stock s where s.stockCode like %:keyword% or s.tickerSymbol like %:keyword% or s.companyName like %:keyword% or s.companyNameEng like %:keyword%")
	List<Stock> search(@Param("keyword") String keyword);
}
