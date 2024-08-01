package com.scl.core.cart.dao;

import com.scl.core.model.TerritoryUnitModel;

import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.internal.dao.Dao;

public interface SclTruckMaxLoadDao extends Dao{
	
	Integer findTruckMaxLoadSize(WarehouseModel source, TerritoryUnitModel city);
}
