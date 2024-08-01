package com.scl.core.services;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import com.scl.core.model.SclCustomerModel;
import com.scl.facades.CreditLimitData;
import com.scl.facades.data.MonthlySalesData;
import com.scl.facades.data.PartnerCustomerData;
import com.scl.facades.data.SCLDealerSalesAllocationData;
import com.scl.occ.dto.PartnerCustomerWsDTO;


public interface DealerService {

	List<MonthlySalesData> getLastSixMonthSalesForDealer(String customerId, String Filter, String customerType,String retailerId);

	CreditLimitData getHighPriorityActions(String dealerCode);
	int getStockAvailForRetailer(int receipt,int saleToRetailer,int saleToInfluencer);
	int getStockAvailForInfluencer(int receipt,int saleToRetailer,int saleToInfluencer);
	SCLDealerSalesAllocationData getStockAllocationForDealer(String productCode);
	SCLDealerSalesAllocationData getStockAllocationForRetailer(String productCode);
	List<CreditLimitData> getHighPriorityActionsForDealer(String uid);

	PartnerCustomerData saveExtendedPartnerInfo(PartnerCustomerData partnerCustomerData);

	PartnerCustomerData editPartnerCustomerInfo(PartnerCustomerData partnerCustomerData, SclCustomerModel dealer, String operationType);

	PartnerCustomerData deletePartnerCustomer(PartnerCustomerData partnerCustomerData, SclCustomerModel dealer, String operationType);

	/**
	 * Returns the validityExpiredDate based on the ValidityInMonths
	 * @param validityInMonths
	 * @param currentDate
	 * @return
	 */
	Date getValidityExpiredDate(Integer validityInMonths, LocalDate currentDate);
}
