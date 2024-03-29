package codesquad.fineants.spring.api.krx.service;

import static org.springframework.http.HttpHeaders.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import codesquad.fineants.spring.api.member.service.WebClientWrapper;
import codesquad.fineants.spring.api.stock.response.StockDataResponse;
import codesquad.fineants.spring.api.stock.response.StockSectorResponse;
import codesquad.fineants.spring.util.ObjectMapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class KRXService {

	private final WebClientWrapper webClient;

	// 전 종목 기본 정보 조회
	public Set<StockDataResponse.StockInfo> fetchStockInfo() {
		String requestUri = "http://data.krx.co.kr/comm/bldAttendant/getJsonData.cmd";
		String responseText = webClient.post(requestUri, createHeader(), createStockInfoBody(), String.class);
		Set<StockDataResponse.StockInfo> latestStocks = ObjectMapperUtil.deserialize(responseText,
				StockDataResponse.class)
			.getStockInfos();
		log.debug("latestStocks count {}", latestStocks.size());
		return latestStocks;
	}

	// KOSPI & KOSDAQ 섹터 정보 조회
	public Map<String, StockSectorResponse.SectorInfo> fetchSectorInfo() {
		Map<String, StockSectorResponse.SectorInfo> kospiMap = fetchKOSPISectorInfo();
		Map<String, StockSectorResponse.SectorInfo> result = new HashMap<>(kospiMap);
		result.putAll(fetchKOSDAQSectorInfo());
		return result;
	}

	// KOSPI 섹터 정보 조회
	private Map<String, StockSectorResponse.SectorInfo> fetchKOSPISectorInfo() {
		String requestUri = "http://data.krx.co.kr/comm/bldAttendant/getJsonData.cmd";
		String responseText = webClient.post(requestUri, createHeader(), createKOSPISectorBody(), String.class);
		Map<String, StockSectorResponse.SectorInfo> kospiSectorMap = ObjectMapperUtil.deserialize(responseText,
				StockSectorResponse.class)
			.getSectorInfos().parallelStream()
			.collect(Collectors.toMap(StockSectorResponse.SectorInfo::getTickerSymbol, sectorInfo -> sectorInfo));
		log.debug("sectorInfos count {}", kospiSectorMap.size());
		return kospiSectorMap;
	}

	// KOSDAQ 섹터 정보 조회
	private Map<String, StockSectorResponse.SectorInfo> fetchKOSDAQSectorInfo() {
		String requestUri = "http://data.krx.co.kr/comm/bldAttendant/getJsonData.cmd";
		String responseText = webClient.post(requestUri, createHeader(), createKOSDAQSectorBody(), String.class);
		Map<String, StockSectorResponse.SectorInfo> kosdaqSectorMap = ObjectMapperUtil.deserialize(responseText,
				StockSectorResponse.class)
			.getSectorInfos().parallelStream()
			.collect(Collectors.toMap(StockSectorResponse.SectorInfo::getTickerSymbol, sectorInfo -> sectorInfo));
		log.debug("sectorInfos count {}", kosdaqSectorMap.size());
		return kosdaqSectorMap;
	}

	private MultiValueMap<String, String> createHeader() {
		MultiValueMap<String, String> result = new LinkedMultiValueMap<>();
		result.add(CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
		result.add(ACCEPT, MediaType.TEXT_HTML_VALUE);
		result.add(ACCEPT_CHARSET, StandardCharsets.UTF_8.name());
		return result;
	}

	private MultiValueMap<String, String> createStockInfoBody() {
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("bld", "dbms/MDC/STAT/standard/MDCSTAT01901");
		formData.add("locale", "ko_KR");
		formData.add("mktId", "ALL");
		formData.add("share", "1");
		formData.add("csvxls_isNo", "false");
		return formData;
	}

	private MultiValueMap<String, String> createKOSPISectorBody() {
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("bld", "dbms/MDC/STAT/standard/MDCSTAT03901");
		formData.add("locale", "ko_KR");
		formData.add("mktId", "STK");
		formData.add("trdDd", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
		formData.add("money", "1");
		formData.add("csvxls_isNo", "false");
		return formData;
	}

	private MultiValueMap<String, String> createKOSDAQSectorBody() {
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
