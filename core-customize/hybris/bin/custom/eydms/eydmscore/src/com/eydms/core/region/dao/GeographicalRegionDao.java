package com.eydms.core.region.dao;

import com.eydms.core.model.GeographicalMasterModel;

import java.util.List;


public interface GeographicalRegionDao {

	public List<String> findAllState();

	public List<String> findAllDistrict(String state);

	public List<String> findAllTaluka(String state, String district);

	public List<String> findAllErpCity(String state, String district, String taluka);

	List<String> findAllErpCity(String state, String district);
	
	public List<String> findAllErpCityByDistrictCode(String district);

	public List<GeographicalMasterModel> getGeographyByPincode(String pincode);

	public List<String> getBusinessState(String state, String district, String taluka, String erpCity);

	List<String> getGeographicalStateByGoogleMapState(String googleMapState);

	List<String> getStateByGSTState(String gstState);
	
	public List<String> findAllLpSourceErpCity(String state, String district, String taluka);

	List<String> findAllLpSourceTaluka(String state, String district);

	List<String> findAllLpSourceDistrict(String state);

	List<String> findAllLpSourceState();

	public List<String> findUserState(String customerCode);
}
