package com.eydms.facades.user;

import com.eydms.facades.data.SOCockpitData;
import de.hybris.platform.commercefacades.user.data.AddressData;

import java.util.List;

public interface EYDMSUserFacade {
	
	SOCockpitData getOutstandingAmountAndBucketsForSO(String uid);
	
	Integer getDealersCountForDSOGreaterThanThirty(String userId);

	List<AddressData> getEyDmsAddressBook();
}
