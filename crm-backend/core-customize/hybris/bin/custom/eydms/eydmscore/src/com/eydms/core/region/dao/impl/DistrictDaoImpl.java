package com.eydms.core.region.dao.impl;

import java.util.Collections;
import java.util.List;

import com.eydms.core.model.DistrictModel;
import com.eydms.core.model.StateModel;
import com.eydms.core.region.dao.DistrictDao;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;

public class DistrictDaoImpl extends DefaultGenericDao<DistrictModel> implements DistrictDao {

	public DistrictDaoImpl() {
		super(DistrictModel._TYPECODE);
	}

	public List<DistrictModel> findDistrictByState(StateModel state){
		return this.find(Collections.singletonMap(DistrictModel.STATE, state));
	}
	
	public List<DistrictModel> findDistrictByCode(String district){
		return this.find(Collections.singletonMap(DistrictModel.ISOCODE, district));
	}
}
