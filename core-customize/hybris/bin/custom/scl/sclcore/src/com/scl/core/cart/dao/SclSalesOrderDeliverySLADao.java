package com.scl.core.cart.dao;

import com.scl.core.model.DestinationSourceMasterModel;
import com.scl.core.model.ERPCityModel;

import com.scl.core.model.SalesOrderDeliverySLAModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;

public interface SclSalesOrderDeliverySLADao {

//    SalesOrderDeliverySLAModel findSalesOrderDeliverySLA(BaseSiteModel brand, WarehouseModel source, ERPCityModel destination,
//                                                         ProductModel product);

	SalesOrderDeliverySLAModel findByRoute(String routeId);

	DestinationSourceMasterModel getDeliverySlaHour(String routeId);

}