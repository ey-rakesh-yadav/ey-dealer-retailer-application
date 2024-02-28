package com.eydms.core.services;

import com.eydms.core.model.EyDmsCustomerModel;

public interface MarketMappingService {

	void saveCounter(EyDmsCustomerModel eydmsCustomer, String type, String routeId,String brand);
}
