package com.scl.facades.orderhistory.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.scl.core.model.DeliveryItemModel;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Objects;
import com.scl.core.orderhistory.service.OrderHistoryService;
import com.scl.facades.order.data.SclOrderHistoryData;
import com.scl.facades.orderhistory.OrderHistoryFacade;
import com.scl.facades.orderhistory.data.DispatchDetailsData;

import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.dto.converter.Converter;

public class OrderHistoryFacadeImpl implements OrderHistoryFacade{

	private OrderHistoryService sclOrderHistoryService;
	
	@Autowired
	Converter<OrderEntryModel, SclOrderHistoryData> sclOrderEntryHistoryCardConverter;

	@Autowired
	Converter<DeliveryItemModel, SclOrderHistoryData> sclDeliveryItemHistoryCardConverter;

	@Override
	public Map<String, Object> getDispatchDetails(String sourceType, String date) {
		
		return getSclOrderHistoryService().getDispatchDetails(sourceType,date);
	}
	
	@Override
	public SearchPageData<SclOrderHistoryData> getTradeOrderListing(SearchPageData paginationData, String sourceType,  String filter, int month, int year,String productName,String orderType, String status)
	{
		SearchPageData<DeliveryItemModel> entries = getSclOrderHistoryService().getTradeOrderListing(paginationData, sourceType, filter, month, year,productName,orderType, status);
		final SearchPageData<SclOrderHistoryData> result = new SearchPageData<>();
		result.setPagination(entries.getPagination());
		result.setSorts(entries.getSorts());
		List<SclOrderHistoryData> sclOrderHistoryData = sclDeliveryItemHistoryCardConverter.convertAll(entries.getResults());
		result.setResults(sclOrderHistoryData);
		return result;
	}
	
	public OrderHistoryService getSclOrderHistoryService() {
		return sclOrderHistoryService;
	}

	public void setSclOrderHistoryService(OrderHistoryService sclOrderHistoryService) {
		this.sclOrderHistoryService = sclOrderHistoryService;
	}
	
}
