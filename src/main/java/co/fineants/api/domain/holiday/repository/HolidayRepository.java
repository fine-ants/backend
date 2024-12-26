package co.fineants.api.domain.holiday.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.fineants.api.domain.holiday.domain.entity.Holiday;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {
}
