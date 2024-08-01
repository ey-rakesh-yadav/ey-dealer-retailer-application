package com.scl.core.orderhistory.service;

import java.util.List;
import java.util.Map;

import com.scl.core.model.DeliveryItemModel;
import com.scl.facades.orderhistory.data.DispatchDetailsData;

import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;


public interface OrderHistoryService
{
	Map<String,Object> getDispatchDetails(String sourceType, String date);
	SearchPageData<DeliveryItemModel> getTradeOrderListing(SearchPageData paginationData, String sourceType, String filter, int month, int year, String productName , String orderType, String status);
}
