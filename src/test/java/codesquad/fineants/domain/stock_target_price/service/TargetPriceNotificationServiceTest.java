package codesquad.fineants.domain.stock_target_price.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.domain.stock.repository.StockRepository;
import codesquad.fineants.domain.stock_target_price.domain.dto.response.TargetPriceNotificationDeleteResponse;
import codesquad.fineants.domain.stock_target_price.domain.entity.StockTargetPrice;
import codesquad.fineants.domain.stock_target_price.domain.entity.TargetPriceNotification;
import codesquad.fineants.domain.stock_target_price.repository.StockTargetPriceRepository;
import codesquad.fineants.domain.stock_target_price.repository.TargetPriceNotificationRepository;
import codesquad.fineants.global.errors.errorcode.MemberErrorCode;
import codesquad.fineants.global.errors.errorcode.StockErrorCode;
import codesquad.fineants.global.errors.exception.FineAntsException;
import codesquad.fineants.global.errors.exception.NotFoundResourceException;

class TargetPriceNotificationServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private TargetPriceNotificationService service;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private StockTargetPriceRepository repository;

	@Autowired
	private TargetPriceNotificationRepository targetPriceNotificationRepository;

	@DisplayName("사용자는 종목 지정가 알림을 제거합니다")
	@Test
	void deleteStockTargetPriceNotification() {
		// given
		Member member = memberRepository.save(createMember());
		Stock stock = stockRepository.save(createSamsungStock());
		StockTargetPrice stockTargetPrice = repository.save(createStockTargetPrice(member, stock));
		TargetPriceNotification targetPriceNotification = targetPriceNotificationRepository.save(
			createTargetPriceNotification(stockTargetPrice));

		Long id = targetPriceNotification.getId();

		setAuthentication(member);
		// when
		TargetPriceNotificationDeleteResponse response = service.deleteStockTargetPriceNotification(id);

		// then
		assertAll(
			() -> assertThat(response)
				.extracting("deletedIds")
				.asList()
				.hasSize(1),
			() -> assertThat(targetPriceNotificationRepository.findById(id).isEmpty()).isTrue(),
			() -> assertThat(repository.findById(stockTargetPrice.getId()).isEmpty()).isTrue()
		);
	}

	@DisplayName("사용자는 존재하지 않는 지정가를 삭제할 수 없습니다")
	@Test
	void deleteStockTargetPriceNotification_whenNotExistTargetPriceNotificationIds_thenThrow404Error() {
		// given
		Member member = memberRepository.save(createMember());
		Stock stock = stockRepository.save(createSamsungStock());
		StockTargetPrice stockTargetPrice = repository.save(createStockTargetPrice(member, stock));
		targetPriceNotificationRepository.save(createTargetPriceNotification(stockTargetPrice));

		Long id = 9999L;

		setAuthentication(member);
		// when
		Throwable throwable = catchThrowable(() -> service.deleteStockTargetPriceNotification(id));

		// then
		assertThat(throwable)
			.isInstanceOf(NotFoundResourceException.class)
			.hasMessage(StockErrorCode.NOT_FOUND_TARGET_PRICE.getMessage());
	}

	@DisplayName("사용자는 다른 사용자의 지정가 알림을 삭제할 수 없습니다.")
	@Test
	void deleteStockTargetPriceNotification_whenForbiddenTargetPriceNotificationIds_thenThrow403Error() {
		// given
		Member member = memberRepository.save(createMember());
		Stock stock = stockRepository.save(createSamsungStock());
		StockTargetPrice stockTargetPrice = repository.save(createStockTargetPrice(member, stock));
		TargetPriceNotification targetPriceNotification = targetPriceNotificationRepository.save(
			createTargetPriceNotification(stockTargetPrice));

		Member hacker = memberRepository.save(createMember("일개미4567", "kim2@gmail.com"));

		setAuthentication(hacker);
		// when
		Throwable throwable = catchThrowable(
			() -> service.deleteStockTargetPriceNotification(targetPriceNotification.getId()));

		// then
		assertThat(throwable)
			.isInstanceOf(FineAntsException.class)
			.hasMessage(MemberErrorCode.FORBIDDEN_MEMBER.getMessage());
	}

	@DisplayName("사용자는 종목 지정가 알림 전체를 삭제합니다")
	@Test
	void deleteAllStockTargetPriceNotification() {
		// given
		Member member = memberRepository.save(createMember());
		Stock stock = stockRepository.save(createSamsungStock());
		StockTargetPrice stockTargetPrice = repository.save(createStockTargetPrice(member, stock));
		List<TargetPriceNotification> targetPriceNotifications = targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(stockTargetPrice, List.of(60000L, 70000L)));

		List<Long> ids = targetPriceNotifications.stream()
			.map(TargetPriceNotification::getId)
			.collect(Collectors.toList());

		setAuthentication(member);
		// when
		TargetPriceNotificationDeleteResponse response = service.deleteAllStockTargetPriceNotification(
			ids,
			stock.getTickerSymbol(),
			member.getId()
		);

		// then
		assertAll(
			() -> assertThat(response)
				.extracting("deletedIds")
				.asList()
				.hasSize(2),
			() -> assertThat(targetPriceNotificationRepository.findAllById(ids).isEmpty()).isTrue(),
			() -> assertThat(repository.findById(stockTargetPrice.getId()).isEmpty()).isTrue()
		);
	}

	@DisplayName("사용자는 종목 지정가 알림을 전체 삭제할 때, 존재하지 않는 지정가 알림을 제거할 수 없습니다.")
	@Test
	void deleteAllStockTargetPriceNotification_whenNotExistTargetPriceNotificationIds_thenThrow404Error() {
		// given
		Member member = memberRepository.save(createMember());
		Stock stock = stockRepository.save(createSamsungStock());
		StockTargetPrice stockTargetPrice = repository.save(createStockTargetPrice(member, stock));
		List<TargetPriceNotification> targetPriceNotifications = targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(stockTargetPrice, List.of(60000L, 70000L)));

		List<Long> ids = targetPriceNotifications.stream()
			.map(TargetPriceNotification::getId)
			.collect(Collectors.toList());
		ids.add(9999L);

		setAuthentication(member);
		// when
		Throwable throwable = catchThrowable(() -> service.deleteAllStockTargetPriceNotification(
			ids,
			stock.getTickerSymbol(),
			member.getId()
		));

		// then
		assertThat(throwable)
			.isInstanceOf(NotFoundResourceException.class)
			.hasMessage(StockErrorCode.NOT_FOUND_TARGET_PRICE.getMessage());
	}

	@DisplayName("사용자는 종목 지정가 알림을 전체 삭제할 때, 존재하지 않는 종목에 대해서 제거할 수 없습니다.")
	@Test
	void deleteAllStockTargetPriceNotification_whenNotExistStock_thenThrow404Error() {
		// given
		Member member = memberRepository.save(createMember());
		Stock stock = stockRepository.save(createSamsungStock());
		StockTargetPrice stockTargetPrice = repository.save(createStockTargetPrice(member, stock));
		List<TargetPriceNotification> targetPriceNotifications = targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(stockTargetPrice, List.of(60000L, 70000L)));

		List<Long> ids = targetPriceNotifications.stream()
			.map(TargetPriceNotification::getId)
			.collect(Collectors.toList());

		setAuthentication(member);
		// when
		Throwable throwable = catchThrowable(() -> service.deleteAllStockTargetPriceNotification(
			ids,
			"999999",
			member.getId()
		));

		// then
		assertThat(throwable)
			.isInstanceOf(NotFoundResourceException.class)
			.hasMessage(StockErrorCode.NOT_FOUND_STOCK.getMessage());
	}

	@DisplayName("사용자는 종목 지정가 알림을 전체 삭제할 때, 다른 사용자의 지정가 알림을 삭제할 수 없습니다")
	@Test
	void deleteAllStockTargetPriceNotification_whenForbiddenTargetPriceNotifications_thenThrow403Error() {
		// given
		Member member = memberRepository.save(createMember());
		Member hacker = memberRepository.save(createMember("hacker"));
		Stock stock = stockRepository.save(createSamsungStock());
		StockTargetPrice stockTargetPrice = repository.save(createStockTargetPrice(member, stock));
		List<TargetPriceNotification> targetPriceNotifications = targetPriceNotificationRepository.saveAll(
			createTargetPriceNotification(stockTargetPrice, List.of(60000L, 70000L)));

		List<Long> ids = targetPriceNotifications.stream()
			.map(TargetPriceNotification::getId)
			.collect(Collectors.toList());

		setAuthentication(hacker);
		// when
		Throwable throwable = catchThrowable(() -> service.deleteAllStockTargetPriceNotification(
			ids,
			stock.getTickerSymbol(),
			hacker.getId()
		));

		// then
		assertThat(throwable)
			.isInstanceOf(FineAntsException.class)
			.hasMessage(MemberErrorCode.FORBIDDEN_MEMBER.getMessage());
	}

}
