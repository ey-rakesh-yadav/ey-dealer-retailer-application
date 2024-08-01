package com.scl.core.services;

import com.scl.core.model.SclCustomerModel;

public interface MarketMappingService {

	void saveCounter(SclCustomerModel sclCustomer, String type, String routeId,String brand);
}
