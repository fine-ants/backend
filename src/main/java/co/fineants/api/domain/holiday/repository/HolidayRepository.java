package co.fineants.api.domain.holiday.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.fineants.api.domain.holiday.domain.entity.Holiday;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {

	@Query("select h from Holiday h where h.baseDate = :baseDate")
	Optional<Holiday> findByBaseDate(@Param("baseDate") LocalDate baseDate);

	@Modifying
	@Query("delete from Holiday h where h.baseDate in :baseDates")
	int deleteAllByBaseDate(@Param("baseDates") List<LocalDate> baseDates);
}
