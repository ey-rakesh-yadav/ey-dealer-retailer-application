package com.eydms.facades.region.impl;

import com.eydms.core.model.GeographicalMasterModel;
import com.eydms.core.region.service.GeographicalRegionService;
import com.eydms.facades.data.GeographicalMasterData;
import com.eydms.facades.region.GeographicalRegionFacade;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

public class GeographicalRegionFacadeImpl implements GeographicalRegionFacade{

	@Resource
	GeographicalRegionService geographicalRegionService;
	
	@Override
	public List<String> findAllState() {
		return geographicalRegionService.findAllState();
	}

	@Override
	public List<String> findAllDistrict(String state) {
		return geographicalRegionService.findAllDistrict(state);
	}

	@Override
	public List<String> findAllTaluka(String state, String district) {
		return geographicalRegionService.findAllTaluka(state, district);
	}

	@Override
	public List<String> findAllErpCity(String state, String district, String taluka) {
		return geographicalRegionService.findAllErpCity(state, district, taluka);
	}

	@Override
	public List<String> findAllErpCity(String state, String district) {
		return geographicalRegionService.findAllErpCity(state, district);
	}
	
	@Override
	public List<GeographicalMasterData> getGeographyByPincode(String pincode) {
		List<GeographicalMasterData> dataList = new ArrayList<GeographicalMasterData>();
		List<GeographicalMasterModel> modelList = geographicalRegionService.getGeographyByPincode(pincode);
		if(modelList!=null) {
			modelList.stream().forEach(model -> {
				GeographicalMasterData data = new GeographicalMasterData();
				data.setZone(model.getZone());
				data.setState(model.getState());
				data.setGeographicalSate(model.getGeographicalState());
				data.setGoogleMapState(model.getGoogleMapState());
				data.setDistrict(model.getDistrict());
				data.setTaluka(model.getTaluka());
				data.setErpCity(model.getErpCity());
				data.setPincode(model.getPincode());
				dataList.add(data);
			});
		}
		return dataList;
	}

	@Override
	public List<String> getBusinessState(String state, String district, String taluka, String erpCity){
		return geographicalRegionService.getBusinessState(state, district, taluka, erpCity);
	}
	
	@Override
	public List<String> getGeographicalStateByGoogleMapState(String googleMapState){
		return geographicalRegionService.getGeographicalStateByGoogleMapState(googleMapState);
	}

	@Override
	public String getErpStateForGstState(String gstState) {
		return geographicalRegionService.getErpStateForGstState(gstState);
	}
	
	@Override
	public List<String> findAllLpSourceErpCity(String state, String district, String taluka) {
		return geographicalRegionService.findAllLpSourceErpCity(state, district, taluka);
	}
	
	@Override
	public List<String> findAllLpSourceTaluka(String state, String district) {
		return geographicalRegionService.findAllLpSourceTaluka(state, district);
	}
	
	@Override
	public List<String> findAllLpSourceDistrict(String state) {
		return geographicalRegionService.findAllLpSourceDistrict(state);
	}
	
	@Override
	public List<String> findAllLpSourceState() {
		return geographicalRegionService.findAllLpSourceState();
	}

	@Override
	public List<String> findUserState(String customerCode) {
		return geographicalRegionService.findUserState(customerCode);
	}
}
