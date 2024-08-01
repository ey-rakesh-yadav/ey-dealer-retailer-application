package com.scl.core.user;

import com.scl.facades.data.SOCockpitData;

public interface SCLUserService {
	
	SOCockpitData getOutstandingAmountAndBucketsForSO(String uid);
	
	Integer getDealersCountForDSOGreaterThanThirty(String userId);
}
