package com.scl.core.region.dao.impl;

import java.util.Collections;
import java.util.List;

import com.scl.core.model.ERPCityModel;
import com.scl.core.region.dao.ERPCityDao;

import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;

public class ERPCityDaoImpl extends DefaultGenericDao<ERPCityModel> implements  ERPCityDao {

	public ERPCityDaoImpl() {
		super(ERPCityModel._TYPECODE);
	}

	@Override
	public List<ERPCityModel> findERPCityByCode(String erpCity) {
		return this.find(Collections.singletonMap(ERPCityModel.ISOCODE, erpCity));
	}

}
