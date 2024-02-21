package com.eydms.facades.impl;

import com.eydms.core.model.DataConstraintModel;
import com.eydms.facades.data.DropdownData;
import com.eydms.facades.data.DropdownListData;
import com.eydms.occ.dto.DropdownListWsDTO;
import org.springframework.beans.factory.annotation.Autowired;

import com.eydms.facades.SSOLoginFacade;
import com.eydms.facades.data.SSOLoginData;

import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;

import com.eydms.core.services.SSOLoginService;

import java.util.ArrayList;
import java.util.List;

public class SSOLoginFacadeImpl implements SSOLoginFacade {

	@Autowired
	SSOLoginService sSOLoginService;
	
	@Override
	public SSOLoginData verifyUserAndGetBrand(String uid) throws UnknownIdentifierException{
		
		return sSOLoginService.verifyUserAndGetBrand(uid);
	}

	@Override
	public String getApplicationVersionByName(String appName) {
		return sSOLoginService.getApplicationVersionByName(appName);
	}

	@Override
	public Boolean getStatusByBuildAndVersionNumber(String buildNumber, String versionNumber) {
		return sSOLoginService.getStatusByBuildAndVersionNumber(buildNumber, versionNumber);
	}

	@Override
	public Boolean getApplicationVersionByNumber(int buildNumber) {
		return sSOLoginService.getApplicationVersionByNumber(buildNumber);
	}

	@Override
	public Boolean getStatusByBuildAndVersionNo(String buildNumber, String versionNumber) {
		return sSOLoginService.getStatusByBuildAndVersionNo(buildNumber, versionNumber);
	}

	@Override
	public Boolean getApplicationVersionByNo(int buildNumber) {
		return sSOLoginService.getApplicationVersionByNo(buildNumber);
	}
	
	@Override
	public SSOLoginData verifyCustomerAndGetBrand(String customerNo) throws UnknownIdentifierException{
		
		return sSOLoginService.verifyCustomerAndGetBrand(customerNo);
	}

	@Override
	public DropdownListData getAppSettings() {
		DropdownListData dropdownListData = new DropdownListData();
		List<DataConstraintModel> list = sSOLoginService.getAppSettings();
		List<DropdownData> dropdownDataList = new ArrayList<>();
		for(DataConstraintModel model: list){
			DropdownData dropdownData = new DropdownData();
			dropdownData.setCode(model.getConstraintName());
			if(model.getDay()!=null){

			}
			dropdownData.setName(model.getDay()!=null?String.valueOf(model.getDay()):"0");
			dropdownDataList.add(dropdownData);

		}
		dropdownListData.setDropdown(dropdownDataList);
		return dropdownListData;
	}


}
