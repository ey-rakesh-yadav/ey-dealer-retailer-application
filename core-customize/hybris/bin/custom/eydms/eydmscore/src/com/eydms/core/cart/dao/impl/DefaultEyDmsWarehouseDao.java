package com.eydms.core.cart.dao.impl;

import java.util.Collections;
import java.util.List;

import com.eydms.core.cart.dao.EyDmsWarehouseDao;

import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

public class DefaultEyDmsWarehouseDao extends DefaultGenericDao<WarehouseModel> implements EyDmsWarehouseDao {

	DefaultEyDmsWarehouseDao(){
        super(WarehouseModel._TYPECODE);
    }

    /**
     * Dao method to fetch lead by lead ID
     * @param leadID
     * @return
     */
    @Override
    public WarehouseModel findWarehouseByCode(String warehouseCode) {
        validateParameterNotNullStandardMessage("warehouseCode", warehouseCode);
        final List<WarehouseModel> warehouseList = this.find(Collections.singletonMap(WarehouseModel.CODE, warehouseCode));
        if (warehouseList.size() > 1)
        {
            throw new AmbiguousIdentifierException(
                    String.format("Found %d warehouses with the warehouseCode value: '%s', which should be unique", warehouseList.size(),
                    		warehouseCode));
        }
        else
        {
            return warehouseList.isEmpty() ? null : warehouseList.get(0);
        }
    }

    @Override
    public WarehouseModel findWarehouseByOrgCode(String organisationCode) {
        validateParameterNotNullStandardMessage("organisationCode", organisationCode);
        final List<WarehouseModel> warehouseList = this.find(Collections.singletonMap(WarehouseModel.ORGANISATIONID, organisationCode));
        if (warehouseList.size() > 1)
        {
            throw new AmbiguousIdentifierException(
                    String.format("Found %d warehouses with the organisationCode value: '%s', which should be unique", warehouseList.size(),
                    		organisationCode));
        }
        else
        {
            return warehouseList.isEmpty() ? null : warehouseList.get(0);
        }
    }
}
