package com.scl.core.cart.dao.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

import java.util.Collections;
import java.util.List;

import com.scl.core.cart.dao.SclERPCityDao;
import com.scl.core.model.ERPCityModel;

import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;

public class DefaultSclERPCityDao extends DefaultGenericDao<ERPCityModel> implements SclERPCityDao{

    public DefaultSclERPCityDao() {
        super(ERPCityModel._TYPECODE);
    }

    @Override
    public List<ERPCityModel> findERPCityByISOCode(String isocode) {
        validateParameterNotNullStandardMessage("isocode", isocode);
        final List<ERPCityModel> erpCityModelList = this.find(Collections.singletonMap(ERPCityModel.ISOCODE, isocode));
        return erpCityModelList;
    }

}