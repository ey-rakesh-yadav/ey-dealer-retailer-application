package com.scl.facades.user;

import com.scl.facades.data.SOCockpitData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.List;

public interface SCLUserFacade {
	
	SOCockpitData getOutstandingAmountAndBucketsForSO(String uid);
	
	Integer getDealersCountForDSOGreaterThanThirty(String userId);

	List<AddressData> getSclAddressBook();

	List<AddressData> getSclAddressForUser(SearchPageData searchPageData, String retailerUid, String filter, String transportationZone);
}
