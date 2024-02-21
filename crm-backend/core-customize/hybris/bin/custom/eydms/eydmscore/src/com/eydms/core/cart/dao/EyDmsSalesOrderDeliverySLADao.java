package com.eydms.core.cart.dao;

import com.eydms.core.model.ERPCityModel;

import com.eydms.core.model.SalesOrderDeliverySLAModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;

public interface EyDmsSalesOrderDeliverySLADao {

//    SalesOrderDeliverySLAModel findSalesOrderDeliverySLA(BaseSiteModel brand, WarehouseModel source, ERPCityModel destination,
//                                                         ProductModel product);

	SalesOrderDeliverySLAModel findByRoute(String routeId);

}