package com.scl.core.services;

import com.scl.core.model.DataConstraintModel;
import com.scl.facades.data.SSOLoginData;
import com.scl.occ.dto.DropdownListWsDTO;

import java.util.List;

public interface SSOLoginService {

	SSOLoginData verifyUserAndGetBrand(String uid);

	String getApplicationVersionByName(String appName);

	Boolean getStatusByBuildAndVersionNumber(String buildNumber, String versionNumber);

	Boolean getApplicationVersionByNumber(int buildNumber);

	Boolean getStatusByBuildAndVersionNo(String buildNumber, String versionNumber);

	Boolean getApplicationVersionByNo(int buildNumber);

	SSOLoginData verifyCustomerAndGetBrand(String customerNo);

	List<DataConstraintModel> getAppSettings();
}
