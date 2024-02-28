package com.eydms.core.region.service.impl;

import com.eydms.core.enums.CounterType;
import com.eydms.core.enums.EyDmsUserType;
import com.eydms.core.model.GeographicalMasterModel;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.model.EyDmsUserModel;
import com.eydms.core.model.SubAreaMasterModel;
import com.eydms.core.region.dao.GeographicalRegionDao;
import com.eydms.core.region.service.GeographicalRegionService;
import com.eydms.core.services.TerritoryManagementService;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.servicelayer.user.UserService;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;
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
			if(customer.getState()!=null) {
				List<String> result = new ArrayList<String>();	        
				result.add(customer.getState());
				return result;
			}
		}
		return geographicalRegionDao.findAllState();
	}

	@Override
	public List<String> findAllDistrict(String state) {
		if(userService.getCurrentUser()!=null && userService.getCurrentUser() instanceof B2BCustomerModel) {
			B2BCustomerModel user =  (B2BCustomerModel) userService.getCurrentUser();
			List<SubAreaMasterModel> subAreaList = null;
			if(user instanceof EyDmsUserModel && user.getUserType()!=null && user.getUserType().equals(EyDmsUserType.SO)) {
				subAreaList =  territoryManagementService.getTerritoriesForSO();
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
			if(user instanceof EyDmsUserModel && user.getUserType()!=null && user.getUserType().equals(EyDmsUserType.SO)) {
				subAreaList =  territoryManagementService.getTerritoriesForSO();
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
	public List<String> findAllLpSourceErpCity(String state, String district, String taluka) {
		return geographicalRegionDao.findAllLpSourceErpCity(state, district, taluka);
	}
	
	@Override
	public List<String> findAllLpSourceTaluka(String state, String district) {
		return geographicalRegionDao.findAllLpSourceTaluka(state, district);
	}
	
	@Override
	public List<String> findAllLpSourceDistrict(String state) {
		return geographicalRegionDao.findAllLpSourceDistrict(state);
	}
	
	@Override
	public List<String> findAllLpSourceState() {
		return geographicalRegionDao.findAllLpSourceState();
	}

	@Override
	public List<String> findUserState(String customerCode) {
		return geographicalRegionDao.findUserState(customerCode);
	}	
}
