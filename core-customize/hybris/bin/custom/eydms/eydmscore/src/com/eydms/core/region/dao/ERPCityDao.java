package com.eydms.core.region.dao;

import java.util.List;

import com.eydms.core.model.ERPCityModel;



public interface ERPCityDao {
	public List<ERPCityModel> findERPCityByCode(String erpCity);
}
