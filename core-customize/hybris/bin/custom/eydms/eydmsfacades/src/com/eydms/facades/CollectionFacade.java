package com.eydms.facades;

import java.util.Map;

import com.eydms.facades.data.*;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.List;

import de.hybris.platform.core.servicelayer.data.SearchPageData;

public interface CollectionFacade {

	public CollectionOutstandingData getOutstandingData();
	
	public CollectionDealerOutstandingDetailsListData getDealerOutstandingDetails();

	public TopDealerOutstandingListData topDealerOutstanding();
	
	public CollectionDealerDetailsData getDealerDetails(String dealerCode);
	
	public CollectionCreditDetailsListData getDealerCreditDetails(String dealerCode);
	
	public CollectionLedgerListData getLedgerDetails(String dealerCode, Boolean isDebit, Boolean isCredit, String startDate, String endDate);
	
	public CollectionCDDetailsListData getCashDiscountDetails( Boolean isMTD, Boolean isYTD);
	
	public void sendLedgerReportMail(String dealerCode, Boolean isDebit, Boolean isCredit, String startDate, String endDate);

	CollectionOutstandingData getOutstandingDataForSP();
  
	Boolean uploadSPInvoice(SPInvoiceData data);
	
	SearchPageData<SPInvoiceData> getSPInvoiceList(SearchPageData searchPageData, String startDate,
			String endDate, String userId, String sortKey, String sort);

	CollectionDealerOutstandingDetailsListData getDealerOutstandingDetailsForTSMRH(SearchPageData searchPageData, List<String> so,List<String> tsm);

	CollectionOutstandingData getOutstandingDataForTSMRH();


}
