package com.scl.core.customer.dao;

import java.util.Date;
import java.util.List;

import com.scl.core.model.SclCustomerModel;

public interface SclCustomerDao {
	
	List<List<Object>> getDealerListWithLastInvoiceDate(Date startDate, Date endDate);
	List<List<Object>> getRetailerAndInfluencerListWithLastInvoiceDate(Date startDate, Date endDate);
	Date getNetworkTypeUpdateDate();
	Date getLiftingDateForRetailerAndInfluencer(SclCustomerModel customer);
	Date getLiftingDateForDealer(SclCustomerModel customer);

	List<SclCustomerModel> getRetailerInfluencerList();

	List<SclCustomerModel> getCustomerList();
	List<SclCustomerModel> getDealersList();
	List<SclCustomerModel> getRetailerList();
	List<SclCustomerModel> getInfluencersList();
}
