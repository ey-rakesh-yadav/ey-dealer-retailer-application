package com.scl.core.region.dao;

import com.scl.core.model.CityModel;
import com.scl.core.model.TalukaModel;

import java.util.List;


public interface CityDao {

	public List<CityModel> findCityByTaluka(TalukaModel taluka);
	
	public List<CityModel> findCityByCode(String city);
}
