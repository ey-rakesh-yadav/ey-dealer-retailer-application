package com.scl.facades.region;

import com.scl.facades.data.GeographicalMasterData;
import com.scl.facades.data.PincodeData;

import java.util.Collection;
import java.util.List;

public interface GeographicalRegionFacade {

	public List<String> findAllState();
	public List<String> findAllDistrict(String state);
	public List<String> findAllTaluka(String state, String district);
	public List<String> findAllErpCity(String state, String district, String taluka);
	List<String> findAllErpCity(String state, String district);
	public List<GeographicalMasterData> getGeographyByPincode(String pincode);
	public List<String> getBusinessState(String state, String district, String taluka, String erpCity);
	public List<String> getGeographicalStateByGoogleMapState(String googleMapState);
	String getErpStateForGstState(String gstState);
	List<String> findAllLpSourceErpCity(String dealerId,String retailerUid,String state, String district, String taluka);
	List<String> findAllLpSourceTaluka(String state, String district,String dealerId,String retailerUid);
	List<String> findAllLpSourceDistrict(String dealerId,String retailerUid, String state);
	List<String> findAllLpSourceState();
	public List<String> findUserState(String customerCode);
	public List<PincodeData> findAllLpSourcePincode(String dealerId,String retailerUid,String state, String district, String taluka, String city);

	List<String> findAllLpSourceState(String dealerId,String retailerId);

	List<PincodeData> findPincode(String state, String district, String taluka, String erpCity);
}
