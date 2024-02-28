package com.eydms.core.region.dao.impl;

import java.util.Collections;
import java.util.List;

import com.eydms.core.model.CityModel;
import com.eydms.core.model.TalukaModel;
import com.eydms.core.region.dao.CityDao;

import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;

public class CityDaoImpl extends DefaultGenericDao<CityModel> implements CityDao {

	public CityDaoImpl() {
		super(CityModel._TYPECODE);
	}

	public List<CityModel> findCityByTaluka(TalukaModel taluka){
		return this.find(Collections.singletonMap(CityModel.TALUKA, taluka));
	}
	
	public List<CityModel> findCityByCode(String city){
		return this.find(Collections.singletonMap(CityModel.ISOCODE, city));
	}
}
