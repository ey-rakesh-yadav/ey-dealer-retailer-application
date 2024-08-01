package com.scl.core.services.impl;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.scl.core.dao.AppBuildMasterDao;
import com.scl.core.dao.CustomerAppBuildMasterDao;
import com.scl.core.dao.DataConstraintDao;
import com.scl.core.model.AppBuildMasterModel;
import com.scl.core.model.CustomerAppBuildMasterModel;
import com.scl.core.model.DataConstraintModel;
import com.scl.occ.dto.DropdownListWsDTO;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.model.SclUserModel;
import com.scl.core.services.SSOLoginService;
import com.scl.core.services.SlctCrmIntegrationService;
import com.scl.facades.data.SSOLoginData;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.user.UserService;

public class SSOLoginServiceImpl implements SSOLoginService {

	@Autowired
	UserService userService;

  @Autowired
	DataConstraintDao dataConstraintDao;
	private static final Logger LOGGER = Logger.getLogger(SSOLoginServiceImpl.class);

	@Autowired
	AppBuildMasterDao appBuildMasterDao;

	@Autowired
	CustomerAppBuildMasterDao customerAppBuildMasterDao;
	
	@Autowired
	SlctCrmIntegrationService slctCrmIntegrationService;

	@Override
	public SSOLoginData verifyUserAndGetBrand(String uid) {
		
		SSOLoginData data = new SSOLoginData();
		
		try {
			UserModel user =  userService.getUserForUID(uid);
			
			if(!Objects.isNull(user))
			{
				String brand = ((B2BCustomerModel) user).getDefaultB2BUnit().getUid();
				
				switch (brand) {
				
					case SclCoreConstants.B2B_UNIT.SCL_SHREE_UNIT_UID:
						data.setBrandId(SclCoreConstants.SITE.SCL_SITE);
						break;

					case SclCoreConstants.B2B_UNIT.SCL_BANGUR_UNIT_UID:
						data.setBrandId(SclCoreConstants.SITE.BANGUR_SITE);
						break;
					case SclCoreConstants.B2B_UNIT.SCL_ROCKSTRONG_UNIT_UID:
						data.setBrandId(SclCoreConstants.SITE.ROCKSTRONG_SITE);
						break;
				}
				
				data.setIsUserPresent(Boolean.TRUE);
				
				 Set<PrincipalGroupModel> ugSet = user.getGroups();
				 
				 if(ugSet.contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID)))
				 {
					 data.setUserGroup(SclCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID);
				 }
				 else if(ugSet.contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))
				 {
					 data.setUserGroup(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID);
				 }
				 else if(ugSet.contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))
				 {
					 data.setUserGroup(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID);
				 }
				 else if(ugSet.contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SALES_OFFICER_GROUP_ID)))
				 {
					 data.setUserGroup(SclCoreConstants.CUSTOMER.SALES_OFFICER_GROUP_ID);
				 }
				 else if(ugSet.contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.TSM_GROUP_ID)))
				 {
					 data.setUserGroup(SclCoreConstants.CUSTOMER.TSM_GROUP_ID);
				 }
				 else if(ugSet.contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RH_GROUP_ID)))
				 {
					 data.setUserGroup(SclCoreConstants.CUSTOMER.RH_GROUP_ID);
				 }
				 else if(ugSet.contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SP_GROUP_ID)))
				 {
					 data.setUserGroup(SclCoreConstants.CUSTOMER.SP_GROUP_ID);
				 }				 
				 else if(ugSet.contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.INFLUENCER_ONBOARDING_USER_GROUP_UID)))
				 {
					 data.setUserGroup(SclCoreConstants.CUSTOMER.INFLUENCER_ONBOARDING_USER_GROUP_UID);
				 }
				 else if(ugSet.contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_ONBOARDING_USER_GROUP_UID)))
				 {
					 data.setUserGroup(SclCoreConstants.CUSTOMER.RETAILER_ONBOARDING_USER_GROUP_UID);
				 }
				 else if(ugSet.contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_ONBOARDING_USER_GROUP_UID)))
				 {
					 data.setUserGroup(SclCoreConstants.CUSTOMER.DEALER_ONBOARDING_USER_GROUP_UID);
				 }
				 else if(ugSet.contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.TSO_GROUP_ID)))
				 {
					 data.setUserGroup(SclCoreConstants.CUSTOMER.TSO_GROUP_ID);
				 }
			}
			else
			{
				throw new UnknownIdentifierException("User Not Found!");
			}
			
		}catch(Exception e)
		{
			LOGGER.debug(e.getMessage(), e);
			data.setIsUserPresent(Boolean.FALSE);
			return data;
		}
		
		return data;
	}

	
	@Override
	public SSOLoginData verifyCustomerAndGetBrand(String customerNo) {
		
		SSOLoginData data = new SSOLoginData();
		
		try {
			UserModel user =  slctCrmIntegrationService.findCustomerByCustomerNo(customerNo) ;
			
			if(!Objects.isNull(user))
			{
				String brand = ((B2BCustomerModel) user).getDefaultB2BUnit().getUid();
				
				switch (brand) {
				
					case SclCoreConstants.B2B_UNIT.SCL_SHREE_UNIT_UID:
						data.setBrandId(SclCoreConstants.SITE.SCL_SITE);
						break;
					
					case SclCoreConstants.B2B_UNIT.SCL_BANGUR_UNIT_UID:
						data.setBrandId(SclCoreConstants.SITE.BANGUR_SITE);
						break;
					case SclCoreConstants.B2B_UNIT.SCL_ROCKSTRONG_UNIT_UID:
						data.setBrandId(SclCoreConstants.SITE.ROCKSTRONG_SITE);
						break;
				}
				
				data.setIsUserPresent(Boolean.TRUE);
				
				 Set<PrincipalGroupModel> ugSet = user.getGroups();
				 
				 if(ugSet.contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID)))
				 {
					 data.setUserGroup(SclCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID);
				 }
				 else if(ugSet.contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))
				 {
					 data.setUserGroup(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID);
				 }
				 else if(ugSet.contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))
				 {
					 data.setUserGroup(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID);
				 }
				 else if(ugSet.contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SALES_OFFICER_GROUP_ID)))
				 {
					 data.setUserGroup(SclCoreConstants.CUSTOMER.SALES_OFFICER_GROUP_ID);
				 }
				 else if(ugSet.contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.TSM_GROUP_ID)))
				 {
					 data.setUserGroup(SclCoreConstants.CUSTOMER.TSM_GROUP_ID);
				 }
				 else if(ugSet.contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RH_GROUP_ID)))
				 {
					 data.setUserGroup(SclCoreConstants.CUSTOMER.RH_GROUP_ID);
				 }
				 else if(ugSet.contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SP_GROUP_ID)))
				 {
					 data.setUserGroup(SclCoreConstants.CUSTOMER.SP_GROUP_ID);
				 }				 
				 else if(ugSet.contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.INFLUENCER_ONBOARDING_USER_GROUP_UID)))
				 {
					 data.setUserGroup(SclCoreConstants.CUSTOMER.INFLUENCER_ONBOARDING_USER_GROUP_UID);
				 }
				 else if(ugSet.contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_ONBOARDING_USER_GROUP_UID)))
				 {
					 data.setUserGroup(SclCoreConstants.CUSTOMER.RETAILER_ONBOARDING_USER_GROUP_UID);
				 }
				 else if(ugSet.contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_ONBOARDING_USER_GROUP_UID)))
				 {
					 data.setUserGroup(SclCoreConstants.CUSTOMER.DEALER_ONBOARDING_USER_GROUP_UID);
				 }
			}
			else
			{
				throw new UnknownIdentifierException("User Not Found!");
			}
			
		}catch(Exception e)
		{
			LOGGER.debug(e.getMessage(), e);
			data.setIsUserPresent(Boolean.FALSE);
			return data;
		}
		
		return data;
	}

	@Override
	public String getApplicationVersionByName(String appName) {
		String version = dataConstraintDao.findVersionByConstraintName(appName);
		if(version!=null){
			return version;
		}
		return "";
	}

	@Override
	public Boolean getStatusByBuildAndVersionNumber(String buildNumber, String versionNumber) {
		return appBuildMasterDao.findStatusByBuildAndVersionNumber(buildNumber, versionNumber).getStatus();
	}

	@Override
	public Boolean getApplicationVersionByNumber(int buildNumber) {
		List<AppBuildMasterModel> list = appBuildMasterDao.findApplicationVersionByNumber(buildNumber);
		if(list!=null && !list.isEmpty()){
			return Boolean.TRUE;
		}
		else{
			return Boolean.FALSE;
		}
	}

	@Override
	public Boolean getStatusByBuildAndVersionNo(String buildNumber, String versionNumber) {
		return customerAppBuildMasterDao.findStatusByBuildAndVersionNo(buildNumber, versionNumber).getStatus();
	}

	public Boolean getApplicationVersionByNo(int buildNumber) {
		List<CustomerAppBuildMasterModel> list = customerAppBuildMasterDao.findApplicationVersionByNo(buildNumber);
		if(list!=null && !list.isEmpty()){
			return Boolean.TRUE;
		}
		else{
			return Boolean.FALSE;
		}
	}

	@Override
	public List<DataConstraintModel> getAppSettings() {
		return dataConstraintDao.findAll();
	}

}
