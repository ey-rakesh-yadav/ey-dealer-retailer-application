package com.scl.facades.orderhistory;

import java.util.List;
import java.util.Map;

import com.scl.facades.order.data.SclOrderHistoryData;
import com.scl.facades.orderhistory.data.DispatchDetailsData;

import de.hybris.platform.core.servicelayer.data.SearchPageData;



public interface OrderHistoryFacade 
{
	Map<String,Object> getDispatchDetails(String sourceType, String date);
	SearchPageData<SclOrderHistoryData> getTradeOrderListing(SearchPageData paginationData, String sourceType,  String filter, int month, int year,String productName,String orderType, String status);

}
