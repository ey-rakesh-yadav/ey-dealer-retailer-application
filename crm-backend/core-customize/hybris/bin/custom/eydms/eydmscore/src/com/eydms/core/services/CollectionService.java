package com.eydms.core.services;

import java.util.List;
import java.util.Map;

import com.eydms.core.model.CreditAndOutstandingModel;
import com.eydms.core.model.LedgerDetailsModel;
import com.eydms.core.model.EyDmsUserModel;
import com.eydms.core.model.NetworkAssistanceModel;
import com.eydms.core.model.SPInvoiceModel;
import com.eydms.facades.data.*;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import de.hybris.platform.core.servicelayer.data.SearchPageData;

public interface CollectionService {
	
	public CollectionOutstandingData getOutstandingData();
	
	public CollectionDealerOutstandingDetailsListData getDealerOutstandingDetails();


	
	public CollectionDealerDetailsData getDealerDetails(String dealerCode);
	
	public CollectionCreditDetailsListData getDealerCreditDetails(String dealerCode);
	
	public List<LedgerDetailsModel> getLedgerDetails(String dealerCode, Boolean isDebit, Boolean isCredit, String startDate, String endDate);

	public CollectionCDDetailsListData getCashDiscountDetails(Boolean isMTD, Boolean isYTD);

	public TopDealerOutstandingListData topDealerOutstanding();

//	public void sendLedgerReportMail(String dealerCode, Boolean isDebit, Boolean isCredit, String startDate, String endDate);

	CollectionOutstandingData getOutstandingDataForSP();
	
	SearchPageData<SPInvoiceModel> getSPInvoiceList(SearchPageData searchPageData, String startDate,
			String endDate, String userId, String sortKey, String sort);

	SearchPageData<CreditAndOutstandingModel> getDealerOutstandingDetailsForTSMRH(SearchPageData searchPageData, List<EyDmsUserModel> soList, List<EyDmsUserModel> tsmList);

	List<List<Object>> getOutstandingDataForTSMRH();

}
