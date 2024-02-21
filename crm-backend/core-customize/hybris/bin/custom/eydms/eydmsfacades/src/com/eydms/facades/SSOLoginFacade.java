package com.eydms.facades;

import com.eydms.facades.data.DropdownListData;
import com.eydms.facades.data.SSOLoginData;

import com.eydms.occ.dto.DropdownListWsDTO;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;

public interface SSOLoginFacade {

	SSOLoginData verifyUserAndGetBrand(String uid) throws UnknownIdentifierException;

	String getApplicationVersionByName(String appName);

	Boolean getStatusByBuildAndVersionNumber(String buildNumber, String versionNumber);

	Boolean getApplicationVersionByNumber(int buildNumber);

	Boolean getStatusByBuildAndVersionNo(String buildNumber, String versionNumber);

	Boolean getApplicationVersionByNo(int buildNumber);

	SSOLoginData verifyCustomerAndGetBrand(String customerNo) throws UnknownIdentifierException;

	DropdownListData getAppSettings();
}
