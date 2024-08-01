package com.scl.facades.cart;

import com.scl.facades.order.data.SclOrderHistoryData;

import de.hybris.platform.commercefacades.order.data.CommerceSaveCartParameterData;
import de.hybris.platform.commercefacades.order.data.CommerceSaveCartResultData;
import de.hybris.platform.commerceservices.order.CommerceSaveCartException;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

public interface SclB2BCartFacade {

	public SearchPageData<SclOrderHistoryData> getSavedCartsBySavedBy(SearchPageData searchPageData, String filter, int month, int year,String productName,String orderType);

	boolean saveCart(CommerceSaveCartParameterData parameters, String employeeCode) throws CommerceSaveCartException;
}
