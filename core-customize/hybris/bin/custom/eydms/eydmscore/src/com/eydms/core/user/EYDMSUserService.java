package com.eydms.core.user;

import com.eydms.facades.data.SOCockpitData;

public interface EYDMSUserService {
	
	SOCockpitData getOutstandingAmountAndBucketsForSO(String uid);
	
	Integer getDealersCountForDSOGreaterThanThirty(String userId);
}
