package com.eydms.core.dao;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eydms.core.enums.QuarterEndOverdueStatus;
import com.eydms.core.model.CashDiscountAvailedModel;
import com.eydms.core.model.CashDiscountSlabsModel;
import com.eydms.core.model.InvoiceMasterModel;
import com.eydms.core.model.LedgerDetailsModel;
import com.eydms.core.model.NetworkAssistanceModel;
import com.eydms.core.model.SPInvoiceModel;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.model.SubAreaMasterModel;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;

public interface CollectionDao {

	Double getSalesForDealer(List<EyDmsCustomerModel> dealerList, Date startDate, Date endDate);
	
	Double getSecurityDepositForDealer(String dealer);
	
	List<OrderModel> getCreditBreachedOrders(EyDmsCustomerModel dealer);
	
	List<LedgerDetailsModel> getLedgerForDealer(String customerNo, Boolean isDebit, Boolean isCredit, Date startDate, Date endDate);
	
	Double getTotalCDAvailedForDealer(String customerNo, Date startDate, Date endDate);
	
	Double getTotalCDLostForDealer(String customerNo, Date startDate, Date endDate);
	
	List<List<Double>> getTotalEligibleCDForDealer(String customerNo);
	
	Double getDealerNetOutstandingAmount(String customer);
	
	QuarterEndOverdueStatus getDealerQuarterEndOverdueStatus(String customerNo);
	
	List<CashDiscountSlabsModel> getAllCashDiscountSlabs();
	
	List<InvoiceMasterModel> getNonReconciledInvoices(String customerNo, Date startDate, Date endDate);
	
	Double getNextSlabDiscount(String customerNo, double currentDiscount);
	
	List<InvoiceMasterModel> getReconciledInvoices();
	
	CashDiscountAvailedModel getCashDiscountAvailedModel(String customerNo, String invoiceNo);
	
	Double getDailyAverageSalesForDealer(String customerNo); 		// dso = totalOutstanding/dailyAverageSales
	
	Double getDailyAverageSalesForListOfDealers(List<String> customerNo); 
	
	SearchPageData<SPInvoiceModel> getSPInvoiceList(EyDmsCustomerModel sp,
			SearchPageData searchPageData, Date startDate, Date endDate, String sortKey, String sort);

	Date getLastUpdateDateForOutstanding(List<String> customerNo);
}
