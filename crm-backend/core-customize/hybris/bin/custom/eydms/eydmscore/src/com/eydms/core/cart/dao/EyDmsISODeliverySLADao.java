package com.eydms.core.cart.dao;

import com.eydms.core.model.ISODeliverySLAModel;

public interface EyDmsISODeliverySLADao {

	ISODeliverySLAModel findByRoute(String routeId);

}