package co.fineants.api.docs.holiday;

import static org.hamcrest.Matchers.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.payload.JsonFieldType;

import co.fineants.api.docs.RestDocsSupport;
import co.fineants.api.domain.holiday.controller.HolidayRestController;
import co.fineants.api.domain.holiday.domain.entity.Holiday;
import co.fineants.api.domain.holiday.service.HolidayService;
import co.fineants.api.global.success.HolidaySuccessCode;

class HolidayRestControllerDocsTest extends RestDocsSupport {

	private HolidayService service;

	@Override
	protected Object initController() {
		service = Mockito.mock(HolidayService.class);
		return new HolidayRestController(service);
	}

	@DisplayName("국내 휴장 일정 업데이트 API")
	@Test
	void updateHoliday() throws Exception {
		// given
		LocalDate baseDate = LocalDate.of(2024, 12, 26);

		List<Holiday> holidays = List.of(
			Holiday.close(LocalDate.of(2025, 1, 1)),
			Holiday.close(LocalDate.of(2025, 1, 28)),
			Holiday.close(LocalDate.of(2025, 1, 29)),
			Holiday.close(LocalDate.of(2025, 1, 30))
		);
		BDDMockito.given(service.updateHoliday(baseDate))
			.willReturn(holidays);
		// when & then
		mockMvc.perform(put("/api/holidays")
				.param("baseDate", baseDate.toString())
				.cookie(createTokenCookies()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.OK.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.OK.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo(HolidaySuccessCode.UPDATE_HOLIDAYS.getMessage())))
			.andExpect(jsonPath("data").isArray())
			.andExpect(jsonPath("data[0].baseDate").value(equalTo("2025-01-01")))
			.andExpect(jsonPath("data[0].dayOfWeek").value(equalTo("WEDNESDAY")))
			.andExpect(jsonPath("data[0].isOpen").value(equalTo(false)))
			.andExpect(jsonPath("data[1].baseDate").value(equalTo("2025-01-28")))
			.andExpect(jsonPath("data[1].dayOfWeek").value(equalTo("TUESDAY")))
			.andExpect(jsonPath("data[1].isOpen").value(equalTo(false)))
			.andExpect(jsonPath("data[2].baseDate").value(equalTo("2025-01-29")))
			.andExpect(jsonPath("data[2].dayOfWeek").value(equalTo("WEDNESDAY")))
			.andExpect(jsonPath("data[2].isOpen").value(equalTo(false)))
			.andExpect(jsonPath("data[3].baseDate").value(equalTo("2025-01-30")))
			.andExpect(jsonPath("data[3].dayOfWeek").value(equalTo("THURSDAY")))
			.andExpect(jsonPath("data[3].isOpen").value(equalTo(false)))
			.andDo(
				document(
					"holiday",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					queryParameters(
						parameterWithName("baseDate").description("기준일자(YYYY-MM-DD), 값이 없는 경우 당일로 선정").optional()
					),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(JsonFieldType.ARRAY)
							.description("응답 데이터"),
						fieldWithPath("data[].baseDate").type(JsonFieldType.STRING)
							.description("기준 일자"),
						fieldWithPath("data[].dayOfWeek").type(JsonFieldType.STRING)
							.description("요일명"),
						fieldWithPath("data[].isOpen").type(JsonFieldType.BOOLEAN)
							.description("개장 여부")
					)
				)
			);
	}
}
