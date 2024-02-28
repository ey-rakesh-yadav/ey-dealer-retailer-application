package com.eydms.core.dao;

import com.eydms.core.enums.OrderType;
import com.eydms.core.model.*;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;

import java.util.List;

public interface EYDMSMastersDao {

    DestinationSourceMasterModel findDestinationSourceMasterForSourceAndBrand(final WarehouseModel source , final BaseSiteModel brand, ERPCityModel erpCity, OrderType orderType, DeliveryModeModel deliveryMode);

}