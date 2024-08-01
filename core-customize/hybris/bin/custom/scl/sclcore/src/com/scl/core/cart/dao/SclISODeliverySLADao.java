package com.scl.core.cart.dao;

import com.scl.core.model.ISODeliverySLAModel;

public interface SclISODeliverySLADao {

	ISODeliverySLAModel findByRoute(String routeId);

}