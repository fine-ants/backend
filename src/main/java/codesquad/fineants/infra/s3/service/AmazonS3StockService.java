package codesquad.fineants.infra.s3.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import codesquad.fineants.domain.stock.domain.entity.Stock;

@Service
public class AmazonS3StockService {

	public List<Stock> fetchStocks() {
		return Collections.emptyList();
	}
}
