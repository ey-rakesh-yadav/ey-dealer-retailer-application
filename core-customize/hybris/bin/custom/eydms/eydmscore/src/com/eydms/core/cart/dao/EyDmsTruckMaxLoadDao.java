package com.eydms.core.cart.dao;

import com.eydms.core.model.TerritoryUnitModel;

import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.internal.dao.Dao;

public interface EyDmsTruckMaxLoadDao extends Dao{
	
	Integer findTruckMaxLoadSize(WarehouseModel source, TerritoryUnitModel city);
}
