package com.scl.core.region.service.impl;

import com.scl.core.enums.CounterType;
import com.scl.core.enums.SclUserType;
import com.scl.core.model.*;
import com.scl.core.region.dao.GeographicalRegionDao;
import com.scl.core.region.service.GeographicalRegionService;
import com.scl.core.services.TerritoryManagementService;


import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.servicelayer.user.UserService;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GeographicalRegionServiceImpl implements GeographicalRegionService {

	@Resource
	GeographicalRegionDao geographicalRegionDao;
	
	@Autowired
	UserService userService;
	
	@Autowired
	TerritoryManagementService territoryManagementService;
	
	@Override
	public List<String> findAllState() {
		if(userService.getCurrentUser()!=null && userService.getCurrentUser() instanceof B2BCustomerModel) {
			B2BCustomerModel customer = (B2BCustomerModel) userService.getCurrentUser();
			List<UserSubAreaMappingModel> userSubAreaList = null;
			List<TsoTalukaMappingModel> tsoTalukasList = null;
			if(customer instanceof SclUserModel && customer.getUserType()!=null) {
				SclUserModel sclUser=(SclUserModel)userService.getCurrentUser();
				if(customer.getUserType().equals(SclUserType.SO) || customer.getUserType().equals(SclUserType.TSM) || customer.getUserType().equals(SclUserType.RH)) {
					userSubAreaList =  geographicalRegionDao.getUserSubAreaMappingForUser(sclUser);
				} else if (customer.getUserType().equals(SclUserType.TSO)) {
					tsoTalukasList = geographicalRegionDao.getTsoTalukaMappingForTso(sclUser);
				}

			}
			if(userSubAreaList!=null) {
				return userSubAreaList.stream().map(data -> data.getState()).distinct().collect(Collectors.toList());
			} else if (tsoTalukasList!=null && !tsoTalukasList.isEmpty()) {
				return tsoTalukasList.stream().map(data -> data.getState()).distinct().collect(Collectors.toList());
			}
		}
		return geographicalRegionDao.findAllState();
	}

	@Override
	public List<String> findAllDistrict(String state) {
		if(userService.getCurrentUser()!=null && userService.getCurrentUser() instanceof B2BCustomerModel) {
			B2BCustomerModel user =  (B2BCustomerModel) userService.getCurrentUser();
			List<SubAreaMasterModel> subAreaList = null;
			if(user instanceof SclUserModel && user.getUserType()!=null && (user.getUserType().equals(SclUserType.SO) || user.getUserType().equals(SclUserType.TSM) || user.getUserType().equals(SclUserType.RH))) {
				subAreaList =  territoryManagementService.getDistrictsForSO(state);
			}
			if(subAreaList!=null) {
				return subAreaList.stream().map(data -> data.getDistrict()).distinct().collect(Collectors.toList());
			}
		}
		return geographicalRegionDao.findAllDistrict(state);
	}

	@Override
	public List<String> findAllTaluka(String state, String district) {
		if(userService.getCurrentUser()!=null && userService.getCurrentUser() instanceof B2BCustomerModel) {
			B2BCustomerModel user =  (B2BCustomerModel) userService.getCurrentUser();
			List<SubAreaMasterModel> subAreaList = null;
			if(user instanceof SclUserModel && user.getUserType()!=null && (user.getUserType().equals(SclUserType.SO) || user.getUserType().equals(SclUserType.TSM) || user.getUserType().equals(SclUserType.RH))) {
				subAreaList =  territoryManagementService.getDistrictsForSO(state);
			}
			if(subAreaList!=null) {
				return subAreaList.stream().filter(data -> data.getDistrict()!=null && data.getDistrict().equals(district)).map(data -> data.getTaluka()).distinct().collect(Collectors.toList());
			}
		}
		return geographicalRegionDao.findAllTaluka(state, district);
	}

	@Override
	public List<String> findAllErpCity(String state, String district, String taluka) {
		return geographicalRegionDao.findAllErpCity(state, district, taluka);
	}

	@Override
	public List<String> findAllErpCity(String state, String district) {
		return geographicalRegionDao.findAllErpCity(state, district);
	}
	
	@Override
	public List<GeographicalMasterModel> getGeographyByPincode(String pincode) {
		return geographicalRegionDao.getGeographyByPincode(pincode);
	}

	@Override
	public List<String> getBusinessState(String state, String district, String taluka, String erpCity) {
		return geographicalRegionDao.getBusinessState(state, district, taluka, erpCity);
	}
	
	@Override
	public List<String> getGeographicalStateByGoogleMapState(String googleMapState){
		return geographicalRegionDao.getGeographicalStateByGoogleMapState(googleMapState);
	}

	@Override
	public String getErpStateForGstState(String gstState) {
		var stateList=geographicalRegionDao.getStateByGSTState(gstState);
		if(CollectionUtils.isNotEmpty(stateList)) {
			return stateList.get(0);
		}
		return "";
	}

	@Override
	public List<String> findAllLpSourceErpCity(String dealerId,String retailerUid,String state, String district, String taluka) {
		return geographicalRegionDao.findAllLpSourceErpCity(dealerId,retailerUid,state, district, taluka);
	}
	
	@Override
	public List<String> findAllLpSourceTaluka(String state, String district,String dealerId,String retailerUid) {
		return geographicalRegionDao.findAllLpSourceTaluka(state, district,dealerId,retailerUid);
	}
	
	@Override
	public List<String> findAllLpSourceDistrict(String dealerId,String retailerUid, String state) {
		return geographicalRegionDao.findAllLpSourceDistrict(dealerId,retailerUid, state);
	}
	
	@Override
	public List<String> findAllLpSourceState() {
		return geographicalRegionDao.findAllLpSourceState();
	}

	@Override
	public List<String> findUserState(String customerCode) {
		return geographicalRegionDao.findUserState(customerCode);
	}

	/**
	 * @param retailerId
	 * @param dealerId
	 * @return
	 */
	@Override
	public List<String> findAllLpSourceState(String dealerId,String retailerId) {
		return geographicalRegionDao.findAllLpSourceState(dealerId,retailerId);
	}

	/**
	 * @param state
	 * @param district
	 * @param taluka
	 * @param erpCity
	 * @return
	 */
	@Override
	public GeographicalMasterModel getGeographicalMaster(String state, String district, String taluka, String erpCity) {
		return geographicalRegionDao.fetchGeographicalMaster(state,district,taluka,erpCity);
	}

	/**
	 * @param transportationZone
	 * @return
	 */
	@Override
	public GeographicalMasterModel getGeographicalMaster(String transportationZone) {
		return geographicalRegionDao.fetchGeographicalMaster(transportationZone);
	}

	@Override
	public List<List<Object>> findAllLpSourcePincode(String dealerId,String retailerUid,String state, String district, String taluka, String city) {
		return geographicalRegionDao.findAllLpSourcePincode(dealerId,retailerUid,state,district,taluka,city);
	}


	/**
	 * @param state
	 * @param district
	 * @param taluka
	 * @param erpCity
	 * @return
	 */
	@Override
	public List<List<Object>> findPincode(String state, String district, String taluka, String erpCity) {
		return geographicalRegionDao.fetchPincode(state,district,taluka,erpCity);
	}
}
