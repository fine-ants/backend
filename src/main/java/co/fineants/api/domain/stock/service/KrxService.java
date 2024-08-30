package co.fineants.api.domain.stock.service;

import static org.springframework.http.HttpHeaders.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import co.fineants.api.domain.member.service.WebClientWrapper;
import co.fineants.api.domain.stock.domain.dto.response.StockSectorResponse;
import co.fineants.api.global.util.ObjectMapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class KrxService {

	private final WebClientWrapper webClient;
	private final StockCsvReader stockCsvReader;

	// KOSPI & KOSDAQ 섹터 정보 조회
	public Map<String, StockSectorResponse.SectorInfo> fetchSectorInfo() {
		Map<String, StockSectorResponse.SectorInfo> kospiMap = fetchKospiSector();
		Map<String, StockSectorResponse.SectorInfo> result = new HashMap<>(kospiMap);
		result.putAll(fetchKosdqSector());
		return result;
	}

	// KOSPI 섹터 정보 조회
	private Map<String, StockSectorResponse.SectorInfo> fetchKospiSector() {
		String requestUri = "http://data.krx.co.kr/comm/bldAttendant/getJsonData.cmd";
		try {
			String responseText = webClient.post(requestUri, createHeader(), createKospiSectorBody(), String.class);
			Map<String, StockSectorResponse.SectorInfo> kospiSectorMap = ObjectMapperUtil.deserialize(responseText,
					StockSectorResponse.class)
				.getSectorInfos().parallelStream()
				.collect(Collectors.toMap(StockSectorResponse.SectorInfo::getTickerSymbol, sectorInfo -> sectorInfo));
			log.debug("sectorInfos count {}", kospiSectorMap.size());
			return kospiSectorMap;
		} catch (Exception e) {
			log.error("fetchKospiSector error: {}", e.getMessage());
			return stockCsvReader.readKospiCsv();
		}
	}

	// KOSDAQ 섹터 정보 조회
	private Map<String, StockSectorResponse.SectorInfo> fetchKosdqSector() {
		String requestUri = "http://data.krx.co.kr/comm/bldAttendant/getJsonData.cmd";
		try {
			String responseText = webClient.post(requestUri, createHeader(), createKosdaqSectorBody(), String.class);
			Map<String, StockSectorResponse.SectorInfo> kosdaqSectorMap = ObjectMapperUtil.deserialize(responseText,
					StockSectorResponse.class)
				.getSectorInfos().parallelStream()
				.collect(Collectors.toMap(StockSectorResponse.SectorInfo::getTickerSymbol, sectorInfo -> sectorInfo));
			log.debug("sectorInfos count {}", kosdaqSectorMap.size());
			return kosdaqSectorMap;
		} catch (Exception e) {
			log.error("fetchKosdaqSector error: {}", e.getMessage());
			return stockCsvReader.readKosdaqCsv();
		}
	}

	private MultiValueMap<String, String> createHeader() {
		MultiValueMap<String, String> result = new LinkedMultiValueMap<>();
		result.add(CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
		result.add(ACCEPT, MediaType.TEXT_HTML_VALUE);
		result.add(ACCEPT_CHARSET, StandardCharsets.UTF_8.name());
		return result;
	}
	
	private MultiValueMap<String, String> createKospiSectorBody() {
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("bld", "dbms/MDC/STAT/standard/MDCSTAT03901");
		formData.add("locale", "ko_KR");
		formData.add("mktId", "STK");
		formData.add("trdDd", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
		formData.add("money", "1");
		formData.add("csvxls_isNo", "false");
		return formData;
	}

	private MultiValueMap<String, String> createKosdaqSectorBody() {
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("bld", "dbms/MDC/STAT/standard/MDCSTAT03901");
		formData.add("locale", "ko_KR");
		formData.add("mktId", "KSQ");
		formData.add("segTpCd", "ALL");
		formData.add("trdDd", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
		formData.add("money", "1");
		formData.add("csvxls_isNo", "false");
		return formData;
	}
}
