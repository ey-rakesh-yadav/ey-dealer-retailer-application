package com.eydms.core.source.dao;

import com.eydms.core.enums.CustomerCategory;
import com.eydms.core.enums.OrderType;
import com.eydms.core.model.DestinationSourceMasterModel;
import com.eydms.core.model.ERPCityModel;
import com.eydms.core.model.TerritoryUnitModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;

import java.util.List;

public interface DestinationSourceMasterDao {

    List<DestinationSourceMasterModel> findDestinationSourceByCode(String city, DeliveryModeModel deliveryMode, OrderType orderType, CustomerCategory customerCategory, String grade, String packaging, String district, String state, BaseSiteModel brand, String taluka);

	List<DestinationSourceMasterModel> findL1Source(String city, DeliveryModeModel deliveryMode, OrderType orderType,
													CustomerCategory customerCategory, String grade, String packaging, String district, String state, BaseSiteModel brand, String taluka);

	DestinationSourceMasterModel getDestinationSourceBySource(OrderType orderType, CustomerCategory customerCategory, WarehouseModel source, DeliveryModeModel deliveryMode, String city, String district, String state, BaseSiteModel brand, String grade, String packaging, String taluka);
}
