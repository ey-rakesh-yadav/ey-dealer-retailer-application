package com.eydms.core.region.service;

import com.eydms.core.model.GeographicalMasterModel;

import java.util.List;

public interface GeographicalRegionService {
	public List<String> findAllState();
	public List<String> findAllDistrict(String state);
	public List<String> findAllTaluka(String state, String district);
	public List<String> findAllErpCity(String state, String district, String taluka);
	List<String> findAllErpCity(String state, String district);
	public List<GeographicalMasterModel> getGeographyByPincode(String pincode);
	public List<String> getBusinessState(String state, String district, String taluka, String erpCity);
	public List<String> getGeographicalStateByGoogleMapState(String googleMapState);
	String getErpStateForGstState(String gstState);
	List<String> findAllLpSourceErpCity(String state, String district, String taluka);
	List<String> findAllLpSourceTaluka(String state, String district);
	List<String> findAllLpSourceDistrict(String state);
	List<String> findAllLpSourceState();
	public List<String> findUserState(String customerCode);
}
