package com.eydms.core.cart.dao;

import java.util.Date;

import com.eydms.core.enums.OrderType;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

public interface EyDmsB2BCartDao {
	
	public SearchPageData<CartModel> getSavedCartsBySavedBy(UserModel user, SearchPageData searchPageData, String filter, Date startDate, Date endDate, String productCode, OrderType orderType);

}
