package com.scl.core.region.dao;

import com.scl.core.model.*;

import java.util.List;


public interface GeographicalRegionDao {

	public List<String> findAllState();

	public List<String> findAllDistrict(String state);

	public List<String> findAllTaluka(String state, String district);

	public List<String> findAllErpCity(String state, String district, String taluka);

	List<String> findAllErpCity(String state, String district);
	
	public List<String> findAllErpCityByDistrictCode(String district);

	public List<GeographicalMasterModel> getGeographyByPincode(String pincode);

	List<GeographicalMasterModel> getAllGeographyMasters();

	public List<String> getBusinessState(String state, String district, String taluka, String erpCity);

	List<String> getGeographicalStateByGoogleMapState(String googleMapState);

	List<String> getStateByGSTState(String gstState);
	
	public List<String> findAllLpSourceErpCity(String dealerId,String retailerUid,String state, String district, String taluka);

	List<String> findAllLpSourceTaluka(String state, String district,String dealerId,String retailerUid);

	List<String> findAllLpSourceDistrict(String dealerId,String retailerUid, String state);

    GeographicalMasterModel getGeographyMasterForTransZone(String transportationZone);

	List<String> findAllLpSourceState();

	public List<String> findUserState(String customerCode);

	GeographicalMasterModel fetchGeographicalMaster(String state, String district, String taluka,String erpCity);


	GeographicalMasterModel fetchGeographicalMaster(String transportationZone);

	public List<List<Object>> findAllLpSourcePincode(String dealerId,String retailerUid,String state, String district, String taluka, String city);

	List<String> findAllLpSourceState(String dealerId,String retailer);

	List<List<Object>> fetchPincode(String state, String district, String taluka, String erpCity);
	List<UserSubAreaMappingModel> getUserSubAreaMappingForUser(SclUserModel sclUser);

	List<DestinationSourceMasterModel> validateAddressFields(String state, String district, String taluka, String erpCity, String pincode, String productCode, SclCustomerModel dealer);

	List<TsoTalukaMappingModel> getTsoTalukaMappingForTso(SclUserModel tsoUser);
}
