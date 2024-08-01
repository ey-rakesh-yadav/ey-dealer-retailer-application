package com.scl.core.source.dao;

import com.scl.core.enums.CustomerCategory;
import com.scl.core.enums.FreightType;
import com.scl.core.enums.OrderType;
import com.scl.core.enums.SpecialProcessIndicator;
import com.scl.core.model.DestinationSourceMasterModel;
import com.scl.core.model.FreightSPIMappingModel;
import com.scl.core.model.GeographicalMasterModel;
import com.scl.core.model.SclIncoTermMasterModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;

import java.util.List;

public interface DestinationSourceMasterDao {

	List<DestinationSourceMasterModel> findDestinationSourceByCode(String city, DeliveryModeModel deliveryMode, OrderType orderType, CustomerCategory customerCategory,String productCode, String district, String state, BaseSiteModel brand, String taluka);

	List<DestinationSourceMasterModel> findDestinationSource(DeliveryModeModel deliveryMode, OrderType orderType, CustomerCategory customerCategory, String productCode, String transportationZone, BaseSiteModel brand, String incoTerm);

	List<DestinationSourceMasterModel> findL1Source(String city, DeliveryModeModel deliveryMode, OrderType orderType,
													CustomerCategory customerCategory, String grade, String packaging, String district, String state, BaseSiteModel brand, String taluka);

	DestinationSourceMasterModel getDestinationSourceBySource(OrderType orderType, CustomerCategory customerCategory, WarehouseModel source, DeliveryModeModel deliveryMode, String city, String district, String state, BaseSiteModel brand, String grade, String packaging, String taluka);


	DestinationSourceMasterModel getDestinationSourceBySourceAndSapProductCode(OrderType orderType,
			CustomerCategory customerCategory, WarehouseModel source, DeliveryModeModel deliveryMode,
			String transportationZone, String sapProductCode, BaseSiteModel brand,
			SclIncoTermMasterModel incoTerm);

	SclIncoTermMasterModel findIncoTermByCode(String incoTerm);

	List<SclIncoTermMasterModel> findIncoTerms(DeliveryModeModel deliveryModeModel, OrderType orderType, CustomerCategory custCategory, String productCode, String transportationZone, BaseSiteModel currentBaseSite);
	/**
	 *
	 * @param orderType
	 * @param custCategory
	 * @param productCode
	 * @param transportationZone
	 * @param brand
	 * @return
	 */
	List<DeliveryModeModel> findDeliveryMode(OrderType orderType, CustomerCategory custCategory, String productCode, String transportationZone, BaseSiteModel brand);

	/**
	 * Gets the SPI from freight type.
	 *
	 * @param freightType the freight type
	 * @return the SPI from freight type
	 */
	FreightSPIMappingModel getSPIFromFreightType(FreightType freightType);
	
	/**
	 * Gets the freight type from SPI.
	 *
	 * @param specialProcessIndicator the special process indicator
	 * @return the freight type from SPI
	 */

	FreightSPIMappingModel getFreightTypeFromSPI(SpecialProcessIndicator specialProcessIndicator);

}
