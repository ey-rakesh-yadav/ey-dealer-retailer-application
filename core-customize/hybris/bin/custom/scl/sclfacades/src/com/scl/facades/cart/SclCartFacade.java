package com.scl.facades.cart;

import com.scl.facades.data.*;



import de.hybris.platform.commercewebservicescommons.dto.order.CartWsDTO;

public interface SclCartFacade {
    boolean setCartDetails(CartWsDTO cartDetails);

	/**
	 * Fetch Destination Source
	 * @param orderType
	 * @param deliveryMode
	 * @param productCode
	 * @param transportationZone
	 * @param incoTerm
	 * @param orderQty
	 * @return
	 */
    DestinationSourceListData fetchDestinationSource(String orderType, String deliveryMode, String productCode, String transportationZone, String incoTerm, double orderQty);

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

	SCLIncoTermMasterListData fetchIncoTerms(String orderType, String deliveryMode, String productCode, String transportationZone);
	/**
	 *
	 * @param orderType
	 * @param productCode
	 * @param transportationZone
	 * @return
	 */
	DeliveryModeListData fetchDeliveryMode(String orderType, String productCode, String transportationZone);
}
