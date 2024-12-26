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
import org.springframework.restdocs.payload.JsonFieldType;

import co.fineants.api.docs.RestDocsSupport;
import co.fineants.api.domain.holiday.controller.HolidayRestController;
import co.fineants.api.domain.holiday.domain.dto.response.HolidayUpdateDto;

class HolidayRestControllerDocsTest extends RestDocsSupport {
	@Override
	protected Object initController() {
		return new HolidayRestController();
	}

	@DisplayName("국내 휴장 일정 업데이트 API")
	@Test
	void updateHoliday() throws Exception {
		// given
		String baseDate = "20241226";
		List<HolidayUpdateDto> expected = List.of(
			HolidayUpdateDto.open(LocalDate.of(2025, 1, 1)),
			HolidayUpdateDto.open(LocalDate.of(2025, 1, 28)),
			HolidayUpdateDto.open(LocalDate.of(2025, 1, 29)),
			HolidayUpdateDto.open(LocalDate.of(2025, 1, 30))
		);
		// when & then
		mockMvc.perform(put("/api/holidays")
				.param("baseDateTime", baseDate)
				.cookie(createTokenCookies()))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("국내 휴장 일정을 업데이트하였습니다")))
			.andExpect(jsonPath("data").isArray())
			.andExpect(jsonPath("data").value(equalTo(expected)))
			.andDo(
				document(
					"holiday",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					queryParameters(
						parameterWithName("baseDate").description("기준일자, 값이 없는 경우 당일로 선정").optional()
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
