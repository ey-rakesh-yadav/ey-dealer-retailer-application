package com.scl.core.orderhistory.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.scl.core.enums.OrderType;
import com.scl.core.model.DeliveryItemModel;
import com.scl.facades.orderhistory.data.DispatchDetailsData;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

public interface OrderHistoryDao
{
	Map<String,Object> getDispatchDetails(String sourceType, String date, UserModel user);
	SearchPageData<DeliveryItemModel> getTradeOrderListing(SearchPageData searchPageData, String sourceType, UserModel user, Date startDate, Date endDate, BaseSiteModel site, String filter, String productName, OrderType orderType, String status);
}
