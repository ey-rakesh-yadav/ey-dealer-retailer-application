package com.scl.facades;

import java.util.List;

import com.scl.facades.data.*;
import com.scl.occ.dto.LiftingBlockWsDTO;
import com.scl.occ.dto.PartnerCustomerWsDTO;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import com.scl.occ.dto.OrderBlockWsDTO;

public interface DealerFacade {

	SclCustomerData getCustomerProfile(String uid);
	
	List<MonthlySalesData> getLastSixMonthSalesForDealer(String customerId, String Filter, String customerType,String retailerId);

	CreditLimitData getHighPriorityActions(String uid);

    ProductStockAllocationListData getProductListForStockAllocation(String dealerId, String retailerId, String selectedLiftingDate) throws Exception;

	LiftingBlockWsDTO getCustomerLiftingBlock(String dealerUid, String retailerUid, String influencerUid);

    SCLDealerSalesAllocationData getStockAllocationForDealer(String productCode);
	
	SCLDealerSalesAllocationData getStockAllocationForRetailer(String productCode);
	
	List<CreditLimitData> getHighPriorityActionsForDealer(String uid);

	LiftingDateRangeData getLiftingDateRange(String customerId) throws Exception;

	boolean isValidSelectedDate(String selectedLiftingDate, String customerId) throws Exception;

	InvoiceListData getInvoiceListForProduct(SearchPageData searchPageData, String dealerUid, String retailerUid, String selectedLiftingDate, String productCode, String productAlias, Double quantity, String filter) throws Exception;

	/**
	 *
	 * @param uid
	 * @param retailerUid
	 * @return
	 */
    OrderBlockWsDTO getDealerOrderBlock(String uid, String retailerUid);

    PartnerCustomerListData getPartnerCustomers(String dealerUid, boolean isManagePartnerWidget);

	PartnerCustomerData saveExtendedPartnerInfo(PartnerCustomerData partnerCustomerData);

	PartnerCustomerData updatePartnerCustomerInfo(PartnerCustomerData partnerCustomerData, String operationType);
}
