package com.eydms.facades.orderhistory.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Objects;
import com.eydms.core.orderhistory.service.OrderHistoryService;
import com.eydms.facades.order.data.EyDmsOrderHistoryData;
import com.eydms.facades.orderhistory.OrderHistoryFacade;
import com.eydms.facades.orderhistory.data.DispatchDetailsData;

import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.dto.converter.Converter;

public class OrderHistoryFacadeImpl implements OrderHistoryFacade{

	private OrderHistoryService eydmsOrderHistoryService;
	
	@Autowired
	Converter<OrderEntryModel, EyDmsOrderHistoryData> eydmsOrderEntryHistoryCardConverter;

	@Override
	public Map<String, Object> getDispatchDetails(String sourceType, String date) {
		
		return getEyDmsOrderHistoryService().getDispatchDetails(sourceType,date);
	}
	
	@Override
	public SearchPageData<EyDmsOrderHistoryData> getTradeOrderListing(SearchPageData paginationData, String sourceType,  String filter, int month, int year,String productName,String orderType, String status)
	{
		SearchPageData<OrderEntryModel> entries = getEyDmsOrderHistoryService().getTradeOrderListing(paginationData, sourceType, filter, month, year,productName,orderType, status);
		final SearchPageData<EyDmsOrderHistoryData> result = new SearchPageData<>();
		result.setPagination(entries.getPagination());
		result.setSorts(entries.getSorts());
		List<EyDmsOrderHistoryData> eydmsOrderHistoryData = eydmsOrderEntryHistoryCardConverter.convertAll(entries.getResults());
		result.setResults(eydmsOrderHistoryData);
		return result;
	}
	
	public OrderHistoryService getEyDmsOrderHistoryService() {
		return eydmsOrderHistoryService;
	}

	public void setEyDmsOrderHistoryService(OrderHistoryService eydmsOrderHistoryService) {
		this.eydmsOrderHistoryService = eydmsOrderHistoryService;
	}
	
}
