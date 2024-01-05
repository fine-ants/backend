package codesquad.fineants.spring.api.stock;

import static org.springframework.http.HttpHeaders.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import codesquad.fineants.domain.stock.StockRepository;
import codesquad.fineants.spring.api.member.service.WebClientWrapper;
import codesquad.fineants.spring.api.stock.request.StockSearchRequest;
import codesquad.fineants.spring.api.stock.response.StockDataResponse;
import codesquad.fineants.spring.api.stock.response.StockSearchItem;
import codesquad.fineants.spring.util.FileMaker;
import codesquad.fineants.spring.util.ObjectMapperUtil;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class StockService {
	private final StockRepository stockRepository;
	private final WebClientWrapper webClient;

	public List<StockSearchItem> search(StockSearchRequest request) {
		return stockRepository.search(request.getSearchTerm())
			.stream()
			.map(StockSearchItem::from)
			.collect(Collectors.toList());
	}

	@Scheduled(fixedRate = 100000L, initialDelay = 5000L)
	public void refreshStockFile() {
		String requestUri = "http://data.krx.co.kr/comm/bldAttendant/getJsonData.cmd";
		String responseText = webClient.post(requestUri, createHeader(), createBody(), String.class);
		StockDataResponse response = ObjectMapperUtil.deserialize(responseText, StockDataResponse.class);
		FileMaker.convertToTsv(response.getStockInfos());
		System.out.println(0);
	}

	private MultiValueMap<String, String> createBody() {
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("bld", "dbms/MDC/STAT/standard/MDCSTAT01901");
		formData.add("locale", "ko_KR");
		formData.add("mktId", "ALL");
		formData.add("share", "1");
		formData.add("csvxls_isNo", "false");
		return formData;
	}

	private MultiValueMap<String, String> createHeader() {
		MultiValueMap<String, String> result = new LinkedMultiValueMap<>();
		result.add(CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
		result.add(ACCEPT, MediaType.TEXT_HTML_VALUE);
		result.add(ACCEPT_CHARSET, StandardCharsets.UTF_8.name());
		return result;
	}
}
