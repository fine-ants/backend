package codesquad.fineants.domain.stock.converter;

import javax.persistence.AttributeConverter;

import codesquad.fineants.domain.stock.Market;

public class MarketConverter implements AttributeConverter<Market, String> {

	@Override
	public String convertToDatabaseColumn(Market market) {
		return market.name().replace("_", " ");
	}

	@Override
	public Market convertToEntityAttribute(String dbData) {
		return Market.ofMarket(dbData.replace(" ", "_"));
	}
}
