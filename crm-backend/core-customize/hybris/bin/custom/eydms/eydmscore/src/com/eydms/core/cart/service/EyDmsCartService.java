package com.eydms.core.cart.service;

import com.eydms.core.model.DestinationSourceMasterModel;
import com.eydms.core.model.ERPCityModel;
import com.eydms.core.model.SalesOrderDeliverySLAModel;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;

import java.util.Collection;
import java.util.List;

public interface EyDmsCartService {

    List<DestinationSourceMasterModel> fetchDestinationSourceByCity(String city, String orderType, String deliveryMode, String productCode,String district, String state, String taluka);
    
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
}
