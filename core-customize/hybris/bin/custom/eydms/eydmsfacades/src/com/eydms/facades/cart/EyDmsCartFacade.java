package com.eydms.facades.cart;

import com.eydms.facades.data.DestinationSourceListData;
import com.eydms.facades.data.DropdownListData;

import de.hybris.platform.commercewebservicescommons.dto.order.CartWsDTO;

public interface EyDmsCartFacade {
    boolean setCartDetails(CartWsDTO cartDetails);

    DestinationSourceListData fetchDestinationSource(String city, String orderType, String deliveryMode, String productCode, String district, String state, String taluka);

	/**
	 * @param orderQty
	 * @param cityUid
	 * @param warehouseCode
	 * @return
	 */
	Integer getCountOfDI(Integer orderQty, String cityUid, String warehouseCode);
	
	/**
	 * @param districtIsoCode
	 * @return
	 */
	DropdownListData getListOfERPCityByDistrictCode(String districtIsoCode);

	boolean setOrderRequistionOnOrder(CartWsDTO cartDetails);
}
