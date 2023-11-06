package codesquad.fineants.spring.api.stock;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import codesquad.fineants.domain.stock.StockRepository;
import codesquad.fineants.spring.api.stock.request.StockSearchRequest;
import codesquad.fineants.spring.api.stock.response.StockSearchItem;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class StockService {
	private final StockRepository stockRepository;

	public List<StockSearchItem> search(StockSearchRequest request) {
		return stockRepository.search(request.getSearchTerm()).stream()
			.map(StockSearchItem::from)
			.collect(Collectors.toList());
	}
}
