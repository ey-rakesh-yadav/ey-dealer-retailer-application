package com.scl.core.region.service;

import com.scl.core.model.GeographicalMasterModel;


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
	List<String> findAllLpSourceErpCity(String dealerId,String retailerUid,String state, String district, String taluka);
	List<String> findAllLpSourceTaluka(String state, String district,String dealerId,String retailerUid);
	List<String> findAllLpSourceDistrict(String dealerId,String retailerUid, String state);
	List<String> findAllLpSourceState();
	public List<String> findUserState(String customerCode);

	List<String> findAllLpSourceState(String dealerId,String retailerId);

	GeographicalMasterModel getGeographicalMaster(String state,String district,String taluka,String erpCity);
	GeographicalMasterModel getGeographicalMaster(String transportationZone);
	public List<List<Object>> findAllLpSourcePincode(String dealerId,String retailerUid,String state, String district, String taluka, String city);

	List<List<Object>> findPincode(String state, String district, String taluka, String erpCity);
}
