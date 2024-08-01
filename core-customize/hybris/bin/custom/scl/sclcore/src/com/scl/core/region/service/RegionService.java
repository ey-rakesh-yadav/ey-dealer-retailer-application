package com.scl.core.region.service;

import com.scl.core.model.CityModel;
import com.scl.core.model.DistrictModel;
import com.scl.core.model.ERPCityModel;
import com.scl.core.model.StateModel;
import com.scl.core.model.TalukaModel;

import java.util.List;

public interface RegionService {

	List<CityModel> findCityByTaluka(String talukaCode);

	List<TalukaModel> findTalukaByDistrict(String districtCode);

	List<DistrictModel> findDistrictByState(String stateCode);

	List<StateModel> findAllState();
	
	List<StateModel> findStateByCode(String stateCode);
	
	List<CityModel> findCityByCode(String cityCode);
	
	List<TalukaModel> findTalukaByCode(String talukaCode);
	
	List<DistrictModel> findDistrictByCode(String districtCode);
	
	List<ERPCityModel> findERPCityByCode(String erpCity);

}
