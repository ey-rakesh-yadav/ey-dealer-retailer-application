package com.scl.core.cart.service;

import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

public interface SclB2BCartService {
	
	public SearchPageData<CartModel> getSavedCartsBySavedBy(UserModel user, SearchPageData searchPageData, String filter, int month, int year,String productName , String orderType);

}
