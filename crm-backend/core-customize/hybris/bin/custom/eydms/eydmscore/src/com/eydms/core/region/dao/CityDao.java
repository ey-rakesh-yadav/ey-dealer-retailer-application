package com.eydms.core.region.dao;

import com.eydms.core.model.CityModel;
import com.eydms.core.model.TalukaModel;

import java.util.List;


public interface CityDao {

	public List<CityModel> findCityByTaluka(TalukaModel taluka);
	
	public List<CityModel> findCityByCode(String city);
}
