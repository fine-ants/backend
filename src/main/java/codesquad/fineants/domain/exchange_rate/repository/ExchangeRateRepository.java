package codesquad.fineants.domain.exchange_rate.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import codesquad.fineants.domain.exchange_rate.domain.entity.ExchangeRate;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, String> {

	@Query("select e from ExchangeRate e where e.code = :code")
	Optional<ExchangeRate> findByCode(@Param("code") String code);
}
