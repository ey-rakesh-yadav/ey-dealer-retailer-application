package com.scl.core.region.service.impl;

import static org.mockito.Matchers.anyObject;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.scl.core.model.CityModel;
import com.scl.core.model.DistrictModel;
import com.scl.core.model.ERPCityModel;
import com.scl.core.model.StateModel;
import com.scl.core.model.TalukaModel;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.region.dao.CityDao;
import com.scl.core.region.dao.DistrictDao;
import com.scl.core.region.dao.ERPCityDao;
import com.scl.core.region.dao.StateDao;
import com.scl.core.region.dao.TalukaDao;
import com.scl.core.region.service.RegionService;


public class RegionServiceImpl implements RegionService{

	@Autowired
	StateDao stateDao;
	
	@Autowired
	DistrictDao districtDao;
	
	@Autowired
	TalukaDao talukaDao;
	
	@Autowired
	CityDao cityDao;
	
	@Autowired
	ERPCityDao erpCityDao;
	
	@Override
	public List<CityModel> findCityByTaluka(String talukaCode){
		List<TalukaModel> taluka = talukaDao.findTalukaByCode(talukaCode);
		if(Objects.nonNull(taluka) && !taluka.isEmpty())
			return cityDao.findCityByTaluka(taluka.get(0));
		else 
			return Collections.emptyList();
	}

	@Override
	public List<TalukaModel> findTalukaByDistrict(String districtCode){
		List<DistrictModel> district = districtDao.findDistrictByCode(districtCode);
		if(Objects.nonNull(district) && !district.isEmpty())
			return talukaDao.findTalukaByDistrict(district.get(0));
		else 
			return Collections.emptyList();
	}
	
	@Override
	public List<DistrictModel> findDistrictByState(String stateCode){
		List<StateModel> state = stateDao.findStateByCode(stateCode);
		if(Objects.nonNull(state) && !state.isEmpty())
			return districtDao.findDistrictByState(state.get(0));
		else 
			return Collections.emptyList();
	}
	
	@Override
	public List<StateModel> findAllState(){
		List<StateModel> stateList = stateDao.findAllState();
			return Objects.nonNull(stateList) ? stateList : Collections.emptyList();
	}

	@Override
	public List<StateModel> findStateByCode(String stateCode) {
		List<StateModel> state = stateDao.findStateByCode(stateCode);
		if(Objects.nonNull(state) && !state.isEmpty())
			return state;
		else 
			return Collections.emptyList();
	}

	@Override
	public List<CityModel> findCityByCode(String cityCode) {
		List<CityModel> city = cityDao.findCityByCode(cityCode);
		if(Objects.nonNull(city) && !city.isEmpty())
			return city;
		else 
			return Collections.emptyList();
	}

	@Override
	public List<TalukaModel> findTalukaByCode(String talukaCode) {
		List<TalukaModel> taluka = talukaDao.findTalukaByCode(talukaCode);
		if(Objects.nonNull(taluka) && !taluka.isEmpty())
			return taluka;
		else 
			return Collections.emptyList();
	}

	@Override
	public List<DistrictModel> findDistrictByCode(String districtCode) {
		List<DistrictModel> district = districtDao.findDistrictByCode(districtCode);
		if(Objects.nonNull(district) && !district.isEmpty())
			return district;
		else 
			return Collections.emptyList();
	}

	@Override
	public List<ERPCityModel> findERPCityByCode(String erpCityCode) {
		List<ERPCityModel> erpCity = erpCityDao.findERPCityByCode(erpCityCode);
		if(Objects.nonNull(erpCity) && !erpCity.isEmpty())
			return erpCity;
		else 
			return Collections.emptyList();
	}
}
