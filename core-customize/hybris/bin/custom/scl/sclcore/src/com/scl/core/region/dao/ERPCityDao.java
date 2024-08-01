package com.scl.core.region.dao;

import java.util.List;

import com.scl.core.model.ERPCityModel;



public interface ERPCityDao {
	public List<ERPCityModel> findERPCityByCode(String erpCity);
}
