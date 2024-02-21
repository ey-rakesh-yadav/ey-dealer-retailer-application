package com.eydms.core.order.services;

import de.hybris.platform.core.model.order.OrderModel;

public interface OrderValidationProcessService {
	
	boolean validateOrder(OrderModel order);

}
