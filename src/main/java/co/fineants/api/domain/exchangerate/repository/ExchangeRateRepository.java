package co.fineants.api.domain.exchangerate.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.fineants.api.domain.exchangerate.domain.entity.ExchangeRate;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, String> {

	@Query("select e from ExchangeRate e where e.code = :code")
	Optional<ExchangeRate> findByCode(@Param("code") String code);

	@Query("delete from ExchangeRate e where e.code in (:codes)")
	@Modifying
	void deleteByCodeIn(@Param("codes") List<String> codes);

	@Query("select e from ExchangeRate e where e.base = true")
	Optional<ExchangeRate> findBase();
}
