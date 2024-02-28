package com.eydms.core.services;

import com.eydms.core.model.DataConstraintModel;
import com.eydms.facades.data.SSOLoginData;
import com.eydms.occ.dto.DropdownListWsDTO;

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
