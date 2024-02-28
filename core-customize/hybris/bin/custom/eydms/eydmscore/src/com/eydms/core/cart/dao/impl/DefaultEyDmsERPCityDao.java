package com.eydms.core.cart.dao.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

import java.util.Collections;
import java.util.List;

import com.eydms.core.cart.dao.EyDmsERPCityDao;
import com.eydms.core.model.ERPCityModel;

import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;

public class DefaultEyDmsERPCityDao extends DefaultGenericDao<ERPCityModel> implements EyDmsERPCityDao{

    public DefaultEyDmsERPCityDao() {
        super(ERPCityModel._TYPECODE);
    }

    @Override
    public List<ERPCityModel> findERPCityByISOCode(String isocode) {
        validateParameterNotNullStandardMessage("isocode", isocode);
        final List<ERPCityModel> erpCityModelList = this.find(Collections.singletonMap(ERPCityModel.ISOCODE, isocode));
        return erpCityModelList;
    }

}