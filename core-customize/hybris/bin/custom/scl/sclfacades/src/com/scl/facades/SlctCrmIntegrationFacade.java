package com.scl.facades;

import com.scl.facades.data.*;
import com.scl.facades.order.data.IntegrationOrderData;
import com.scl.facades.order.data.IntegrationOrderEntryData;

import java.text.ParseException;
import java.util.List;

public interface SlctCrmIntegrationFacade {

	public MarketMappingListData getAllMarketMappingDetails();

	public CRMVisitListData getAllVisit();

	public CRMCounterVisitListData getAllCounterVisit();

	public SlctCrmInfluencerListData getAllInfluencerDetails();

	public RetailerSalesInfoListData getAllRetailerSalesInfo();

	public boolean deleteLPSourceMasterData(LPSourceMasterListData lpSourceMasterListData);

	public boolean insertUpdateOutstandingDetails(SlctCrmOutstandingDetailsListData slctCrmOutstandingDetailsListData);

	public boolean updateRunMasterDetails(SlctRunMasterListData slctRunMasterListData);

	public boolean updateCounterScoreDetails(SlctCounterScoreListData slctCounterScoreListData);

	public boolean updateRouteScoreDetails(SlctRouteScoreListData slctRouteScoreListData);

	public boolean insertNcrDetails(SlctNcrListData slctNcrListData);

	public void insertOutstandingHistoryDetails(SlctCrmOutstandingDetailsData slctCrmOutstandingDetailsData);

	public boolean insertMitraTransactionDetails(SlctMitraTransactionListData slctMitraTransactionListData);

	public boolean insertUpdateMitraMasterDetails(SlctMitraMasterListData slctMitraMasterListData);

	public slctOrderLineSchedulerDetailsListData getOrderLineSchedulerDetails();

	boolean updateOrderFromErp(IntegrationOrderData order);

	public boolean updateDealerCategorization(DealerCategorizationListData dealerCategorizationListData);

	public SlctCrmCustomerListData getAllSlctCrmCustomerDetails();

	public boolean insertProspectiveNetworksList(SlctProspectiveNetworksListData prospectiveNetworksListData);

	public boolean insertDealerLedgerDetails(DealerLedgerListData dealerLedgerListData) throws ParseException;

	boolean insertUpdateDealerLedgerDetails(DealerLedgerListData dealerLedgerListData) throws ParseException;

	public boolean insertIsoMasterDetails(SlctISOMasterListData slctISOMasterListData) throws ParseException;

	public boolean deleteOldIsoMasterData();

	public NewOrderReponseData createOrderFromErp(IntegrationOrderData integrationOrderData);

	boolean insertEtaDetails(SlctEtaListData slctEtaListData);

	boolean insertUpdateCustomerMasterDetails(SlctCrmIntegrationCustomerMasterListData slctCrmIntegrationCustomerMasterListData);

	SlctIntegrationSalesPlanningListData getSalesPlanningListData();

	boolean insertUpdateGeographicalMasterDetails(SlctGeographicalMasterListData slctGeographicalMasterListData);

	boolean insertUpdateFreightAndIncoTermsMasterDetails(SlctFreightAndIncoTermsListData slctFreightAndIncoTermsListData);

	boolean insertUpdateProductMasterDetails(SlctProductMasterListData slctProductMasterListData);

	boolean insertUpdateSalesOrderDeliverySlaDetails(SlctOrderDeliverySlaListData slctOrderDeliverySlaListData);

	boolean insertWarehouseDetails(SlctWarehouseListData slctWarehouseListData);

	SlctEpodOutboundListData getOrderLineForEpod();

	SLCTruckReachedDateListData updateTruckReachData(SLCTruckReachedDateListData slctTruckReachedDateListData);

	boolean removeExistingDestinationSourceMasterData();

	boolean insertDestinationSourceData(LPSourceMasterListData lpSourceMasterListData);

	boolean removeExistingLpSourceMasterDataThroughSqlQuery();

	boolean removeExistingDepotSubAreaMappingData();

	boolean insertUpdateDepotSubAreaMappingDetails(SlctDepotSubAreaMappingListData slctDepotSubAreaMappingListData);

	List<String> updateOrderEntryFromErp(List<IntegrationOrderEntryData> orderEntryData);

	SlctCrmIntegrationDOMasterListData insertUpdateDoMasterDetails(SlctCrmIntegrationDOMasterListData slctCrmIntegrationDOMasterListData);

    SlctCrmTSOMasterListData insertUpdateTsoMasterDetails(SlctCrmTSOMasterListData slctCrmTSOMasterListData);

    SlctDOSubAreaMappingListData insertUpdateDoSubAreaMapping(SlctDOSubAreaMappingListData slctDOSubAreaMappingListData);

	LeadGeneratedDetailsListData getLeadCount(Integer year, Integer month);

	boolean insertRoutesForSoDeliverySla(SlctRouteSLAListData slctRouteSLAListData);

	SalesPlanningBottomUpIntegrationListData getBottomUpSalesPlanningData();

	SalesPlanningTopDownIntegrationListData getTopDownIntegrationDetails(SalesPlanningTopDownIntegrationListData salesPlanningTopDownIntegrationListData);

	AnnualSalesBottomUpIntegrationListData getAnnualSalesBottomUpData();

	SlctBrandMasterListData insertUpdateBrandMaster(SlctBrandMasterListData slctBrandMasterListData);

	//boolean insertUpdateCompetitorProductMaster(SlctCompetitorProductMasterListData slctCompetitorProductMasterListData);

    boolean insertUpdateSalesNcrData(SalesNcrIntegrationListData salesNcrIntegrationListData);

    SlctCompetitorProductMasterListData insertUpdateCompetitorProductMaster(SlctCompetitorProductMasterListData slctCompetitorProductMasterListData);

	SlctCrmOrderListData getModifiedOrders();

	SalesPlanningDealerReviewListData getSalesPlanningReviewedTargets();

	boolean insertUpdateCustomerAddress(ERPCustomerAddressListData erpCustomerAddressListData);

	SlctCrmOrderEntryListData getModifiedOrderEntries();


	InfluencerVisitListData getInfluencerVisitDetails();
	EndCustomerComplaintDataList getEndCustomerComplaintsForSLCT(String startDate, String endDate);

	public ScheduledMeetListData getinfluencerMeetCompletion();

	PointRequisitionSalesListData getPointRequisitionDetails();

	EndCustomerComplaintDataList getEndCustomerComplaintClosureRequest();
	SlctCrmSiteMasterListData getSiteMasterDetailsForSLCT();

	SlctCrmSiteMasterListData getSiteVisitForms();

    boolean insertUpdateProductPointMaster(SlctProductPointMasterListData slctProductPointMasterListData);

	boolean schemesDefinitionFromSlct(SlctSchemesListData slctSchemesListData);

	SlctGiftRedemptionListData getGiftRedemptionForSLCT();

    boolean insertUpdateGiftData(SlctGiftSchemesListData slctGiftSchemesListData);

	SlctTSOSubAreaMappingListData insertUpdateTsoSubAreaMapping(SlctTSOSubAreaMappingListData slctTSOSubAreaMappingListData);

    boolean updateProductEquiCode(SlctProductEquivalenceDataListData slctProductPointMasterListData);

    boolean syncMarketMappingIds(MarketMappingInboundSyncData marketMappingInboundSyncData);

  	SlctCrmSiteTransactionListData getSiteTransactionDetailsForSLCT();

	/**
	 * Fetches the List of SiteTransactionModel and populates in the SiteConversionListData object
	 * @return
	 */
	SiteConversionListData getSiteConversionDetailsForSLCT();


	/**
	 * Returns true if all the SiteTransaction Models are synced for the given IDs
	 * @param siteConversionInboundSyncData
	 * @return
	 */
	boolean syncSiteTransactionData(SiteConversionInboundSyncData siteConversionInboundSyncData);

	boolean insertUpdateStockAllocationData(DealerInvoiceSummaryData dealerInvoiceSummaryData);



}