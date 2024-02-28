package com.eydms.core.cart.dao;

import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.internal.dao.Dao;

public interface EyDmsWarehouseDao {
	
	/**
	 * Dao method to fetch Warehouse by Warehouse Code
	 * @param warehouseCode
	 * @return
	 */
	WarehouseModel findWarehouseByCode(final String warehouseCode);

	WarehouseModel findWarehouseByOrgCode(String organisationCode);

}
