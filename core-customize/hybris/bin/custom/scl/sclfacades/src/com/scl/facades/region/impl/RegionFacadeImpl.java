package com.scl.facades.region.impl;

import java.util.ArrayList;
import java.util.List;

import com.scl.core.model.CityModel;
import com.scl.core.model.DistrictModel;
import com.scl.core.model.StateModel;
import com.scl.core.model.TalukaModel;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.region.service.RegionService;
import com.scl.facades.data.CityData;
import com.scl.facades.data.DistrictData;
import com.scl.facades.data.StateData;
import com.scl.facades.data.TalukaData;
import com.scl.facades.region.RegionFacade;

import de.hybris.platform.servicelayer.i18n.I18NService;

public class RegionFacadeImpl implements RegionFacade{

	@Autowired
	RegionService regionService;
	
	@Autowired
	private I18NService i18NService;


	@Override
	public List<CityData> findCityByTaluka(String talukaCode) {
		List<CityModel> cityModelList =  regionService.findCityByTaluka(talukaCode);
		List<CityData> cityDataList = new ArrayList<CityData>();
		cityModelList.stream().forEach(city-> 
		{
			CityData data = new CityData();
			data.setIsocode(city.getIsocode());
			data.setName(city.getName(i18NService.getCurrentLocale()));
			cityDataList.add(data);
		});
		return cityDataList;
	}

	@Override
	public List<TalukaData> findTalukaByDistrict(String districtCode) {
		List<TalukaModel> modelList =  regionService.findTalukaByDistrict(districtCode);
		List<TalukaData> dataList = new ArrayList<TalukaData>();
		modelList.stream().forEach(city-> 
		{
			TalukaData data = new TalukaData();
			data.setIsocode(city.getIsocode());
			data.setName(city.getName(i18NService.getCurrentLocale()));
			dataList.add(data);
		});
		return dataList;
	}

	@Override
	public List<DistrictData> findDistrictByState(String stateCode) {
		List<DistrictModel> modelList =  regionService.findDistrictByState(stateCode);
		List<DistrictData> dataList = new ArrayList<DistrictData>();
		modelList.stream().forEach(city-> 
		{
			DistrictData data = new DistrictData();
			data.setIsocode(city.getIsocode());
			data.setName(city.getName(i18NService.getCurrentLocale()));
			dataList.add(data);
		});
		return dataList;
	}

	@Override
	public List<StateData> findAllState() {
		List<StateModel> modelList =  regionService.findAllState();
		List<StateData> dataList = new ArrayList<StateData>();
		modelList.stream().forEach(city-> 
		{
			StateData data = new StateData();
			data.setIsocode(city.getIsocode());
			data.setName(city.getName(i18NService.getCurrentLocale()));
			dataList.add(data);
		});
		return dataList;
	}

}
