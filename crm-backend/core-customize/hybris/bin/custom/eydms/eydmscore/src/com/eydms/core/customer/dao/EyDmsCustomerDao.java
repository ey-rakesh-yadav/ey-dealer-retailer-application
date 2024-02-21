package com.eydms.core.customer.dao;

import java.util.Date;
import java.util.List;

import com.eydms.core.model.EyDmsCustomerModel;

public interface EyDmsCustomerDao {
	
	List<List<Object>> getDealerListWithLastInvoiceDate(Date startDate, Date endDate);
	List<List<Object>> getRetailerAndInfluencerListWithLastInvoiceDate(Date startDate, Date endDate);
	Date getNetworkTypeUpdateDate();
	Date getLiftingDateForRetailerAndInfluencer(EyDmsCustomerModel customer);
	Date getLiftingDateForDealer(EyDmsCustomerModel customer);

	List<EyDmsCustomerModel> getRetailerInfluencerList();

	List<EyDmsCustomerModel> getCustomerList();
}
