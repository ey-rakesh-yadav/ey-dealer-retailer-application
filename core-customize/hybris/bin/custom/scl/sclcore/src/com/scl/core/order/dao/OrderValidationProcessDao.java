package com.scl.core.order.dao;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.scl.core.model.SclCustomerModel;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;

public interface OrderValidationProcessDao
{
	List<String> getOptimalSource(OrderModel order);
	Double getMaxQuantity(OrderModel order);
	Double getPendingOrderAmount(String userId);
	Double getMaxQuantityThreshold(BaseSiteModel site, SclCustomerModel customer, ProductModel product);
	Double getQtyForExpectedDeliveredDate(BaseSiteModel site, SclCustomerModel customer, ProductModel product,
			Date expectedDeliveryDate);

    double getRemainingQtyAndPrice(String userId);
	double getDiQtyAndPrice(String userId);
}
