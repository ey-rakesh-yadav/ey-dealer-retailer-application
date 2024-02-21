package com.eydms.facades.cart;

import com.eydms.facades.order.data.EyDmsOrderHistoryData;

import de.hybris.platform.commercefacades.order.data.CommerceSaveCartParameterData;
import de.hybris.platform.commercefacades.order.data.CommerceSaveCartResultData;
import de.hybris.platform.commerceservices.order.CommerceSaveCartException;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

public interface EyDmsB2BCartFacade {

	public SearchPageData<EyDmsOrderHistoryData> getSavedCartsBySavedBy(SearchPageData searchPageData, String filter, int month, int year,String productName,String orderType);

	boolean saveCart(CommerceSaveCartParameterData parameters, String employeeCode) throws CommerceSaveCartException;
}
