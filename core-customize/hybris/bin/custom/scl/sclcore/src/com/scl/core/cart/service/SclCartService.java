package com.scl.core.cart.service;

import com.scl.core.model.DestinationSourceMasterModel;
import com.scl.core.model.ERPCityModel;
import com.scl.core.model.SalesOrderDeliverySLAModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import com.scl.core.model.SclIncoTermMasterModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;

import de.hybris.platform.ordersplitting.model.WarehouseModel;

import java.util.Collection;
import java.util.List;

public interface SclCartService {

    List<DestinationSourceMasterModel> fetchDestinationSource(String orderType, String deliveryMode, String productCode, String transportationZone, String incoTerm);
    
    /**
	 * Service to get max truck load size
	 * @param cityUid
	 * @param warehouseCode
	 * @return
     * @throws Exception 
	 */
	Integer getMaxTruckLoadSize(String cityUid, String warehouseCode) throws Exception;
	
	/**
	 * @param districtIsoCode
	 * @return
	 */
	Collection<ERPCityModel> getListOfERPCityByDistrictCode(String districtIsoCode);

	SalesOrderDeliverySLAModel getSalesOrderDeliverySLA(BaseSiteModel brand, String productCode, String erpCityCode,
			WarehouseModel source);

	List<String> getListOfERPCityByDistrict(String districtIsoCode);

    List<SclIncoTermMasterModel> fetchIncoTerms(String orderType, String deliveryMode, String productCode, String transportationZone);


	/**
	 *
	 * @param orderType
	 * @param productCode
	 * @param transportationZone
	 * @return
	 */
	List<DeliveryModeModel> fetchDeliveryMode(String orderType, String productCode, String transportationZone);

}
