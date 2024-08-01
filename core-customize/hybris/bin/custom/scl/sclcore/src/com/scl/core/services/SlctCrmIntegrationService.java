package com.scl.core.services;

//import java.util.Date;
import java.text.ParseException;
import java.util.List;

import com.scl.core.enums.CustomerCategory;
import com.scl.core.enums.OrderType;
import com.scl.core.model.*;
import com.scl.facades.data.*;
import com.scl.facades.order.data.IntegrationOrderData;

import com.scl.facades.order.data.IntegrationOrderEntryData;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.core.model.product.ProductModel;

public interface SlctCrmIntegrationService {

	List<MarketMappingDetailsModel> getAllMarketMappingDetails();

	List<VisitMasterModel> getAllVisit();

	List<CounterVisitMasterModel> getAllCounterVisit();

	List<SclCustomerModel> getAllInfluencerDetails();

	List<List<Object>> getAllRetailerSalesInfo();

	public List<DestinationSourceMasterModel> getLpSourceMasterList(BaseSiteModel brand,
																	DeliveryModeModel deliveryMode, String productCode, String incoTerm, String destCityId);

	OrderType getTheOrderType(String order);

	CustomerCategory getTheCustType(String customer);

	BaseSiteModel getTheBrand(String brand);

	DeliveryModeModel getTheDeliveryMode(String delivery);

	public CreditAndOutstandingModel getCrmOutstandingDetails(String custCode);

	public boolean updateRunMasterDetails(SlctRunMasterListData slctRunMasterListData);

	public boolean updateCounterScoreDetails(SlctCounterScoreListData slctCounterScoreListData);

	public boolean updateRouteScoreDetails(SlctRouteScoreListData slctRouteScoreListData);

	public boolean insertNcrDetails(SlctNcrListData slctNcrListData);

	public boolean insertMitraTransactionDetails(SlctMitraTransactionListData slctMitraTransactionListData);
	public boolean insertUpdateMitraMasterDetails(SlctMitraMasterListData slctMitraMasterListData);

	public List<OrderEntryModel> getOrderLineScheduleDetails();
	boolean updateOrderFromSlct(IntegrationOrderData orderData);

	public boolean updateDealerCategorization(DealerCategorizationListData dealerCategorizationListData);

	public List<List<Object>> getSlctCrmCustomerDetails();

	public boolean insertProspectiveNetworksList(SlctProspectiveNetworksListData prospectiveNetworksListData);

	public boolean insertDealerLedgerDetails(DealerLedgerListData dealerLedgerListData) throws ParseException;

	boolean insertUpdateDealerLedgerDetails(DealerLedgerListData dealerLedgerListData) throws ParseException;

	public boolean insertIsoMasterDetails(SlctISOMasterListData slctISOMasterListData);

	public boolean deleteOldIsoMasterData();

	OrderModel createOrderFromErp(IntegrationOrderData orderData);

	List<FreightAndIncoTermsMasterModel> findFreightAndIncoTerms(String state, String district, BaseSiteModel brand,
																 String orgType);


	void populateDeliverySlotAndDate(String routeId, OrderModel order, OrderEntryModel entryModel);

	boolean insertEtaDetails(SlctEtaListData slctEtaListData);

	boolean insertUpdateCustomerMasterDetails(SlctCrmIntegrationCustomerMasterListData slctCrmIntegrationCustomerMasterListData);

	List<MonthlySalesModel> getMonthlySaleModel(String monthYear);

	List<DealerPlannedMonthlySalesModel> getDealerPlannedMonthlySalesDetails(List<SubAreaMasterModel> subAreaMasterList, SclUserModel salesOfficer, String monthName, String monthYear);

	boolean insertUpdateGeographicalMasterDetails(SlctGeographicalMasterListData slctGeographicalMasterListData);

	boolean insertUpdateFreightAndIncoTermsMasterDetails(SlctFreightAndIncoTermsListData slctFreightAndIncoTermsListData);

	boolean insertUpdateProductMasterDetails(SlctProductMasterListData slctProductMasterListData);

	boolean insertUpdateSalesOrderDeliverySlaDetails(SlctOrderDeliverySlaListData slctOrderDeliverySlaListData);

	boolean insertWarehouseDetails(SlctWarehouseListData slctWarehouseListData);

	List<DeliveryItemModel> getOrderLinesForEpod();

	List<SLCTruckReachedDateData> updateTruckReachData(SLCTruckReachedDateListData slctTruckReachedDateListData);

	boolean removeExistingDestinationSourceMasterData();

	boolean insertDestinationSourceData(LPSourceMasterListData lpSourceMasterListData);

	boolean removeExistingLpSourceMasterDataThroughSqlQuery();

	boolean removeExistingDepotSubAreaMappingData();

	boolean insertUpdateDepotSubAreaMappingDetails(SlctDepotSubAreaMappingListData slctDepotSubAreaMappingListData);

	public SclCustomerModel findCustomerByCustomerNo(String customerNo);

	List<String> updateOrderEntryFromErp(List<IntegrationOrderEntryData> orderEntryData);

	SlctCrmIntegrationDOMasterListData insertUpdateDoMasterDetails(SlctCrmIntegrationDOMasterListData slctCrmIntegrationDOMasterListData);

	SlctDOSubAreaMappingListData insertUpdateDoSubAreaMapping(SlctDOSubAreaMappingListData slctDOSubAreaMappingListData);

	List<List<Object>> getLeadCount(Integer year, Integer month);

	boolean insertRoutesForSoDeliverySla(SlctRouteSLAListData slctRouteSLAListData);

	List<List<Object>> getProductSaleDetails(SclUserModel so, String monthName, String monthYear, List<SubAreaMasterModel> subAreaMasterModelList, DistrictMasterModel districtMaster, CMSSiteModel brand);

	SalesPlanningTopDownIntegrationListData getTopDownIntegrationDetails(SalesPlanningTopDownIntegrationListData salesPlanningTopDownIntegrationListData);

	List<AnnualSalesModel> getAnnualSalesBottomUpData(String financialYear);

	SlctBrandMasterListData insertUpdateBrandMaster(SlctBrandMasterListData slctBrandMasterListData);

	//boolean insertUpdateCompetitorProductMaster(SlctCompetitorProductMasterListData slctCompetitorProductMasterListData);

    boolean insertUpdateSalesNcrData(SalesNcrIntegrationListData salesNcrIntegrationListData);

    String getAliasNameFromProduct(ProductModel productModel, String state, String district);
	SlctCompetitorProductMasterListData insertUpdateCompetitorProductMaster(SlctCompetitorProductMasterListData slctCompetitorProductMasterListData);

	List<OrderModel> getModifiedOrders();

	boolean insertUpdateCustomerAddress(ERPCustomerAddressListData erpCustomerAddressListData);

	List<OrderEntryModel> getModifiedOrderEntries();

	List<InfluencerVisitMasterModel> getInfluencerVisitDetails();
	List<EndCustomerComplaintModel> getEndCustomerComplaintsForSLCT(String startDate, String endDate);


	List<MeetingScheduleModel> getinfluencerMeetCompletion();

	List<PointRequisitionModel> getPointRequisitonDetails();

	List<EndCustomerComplaintModel> getEndCustomerComplaintClosureRequest();

	List<SiteVisitMasterModel> getSiteVisitForms();

	List<SclSiteMasterModel> getSiteMasterDetailsForSLCT();

	boolean insertUpdateProductPointMaster(SlctProductPointMasterListData slctProductPointMasterListData);

	boolean insertUpdateGiftData(SlctGiftSchemesListData slctGiftSchemesListData);

	boolean schemesDefinitionFromSlct(SlctSchemesListData slctSchemesListData);
	List<List<Object>> getGiftRedemptionForSLCT();
	List<OrderEntryModel> getGiftRedemptionsForSLCT();

	SlctCrmTSOMasterListData insertUpdateTSOMasterDetails(SlctCrmTSOMasterListData slctCrmTSOMasterListData);

	SlctTSOSubAreaMappingListData insertUpdateTSOSubAreaMapping(SlctTSOSubAreaMappingListData slctTSOSubAreaMappingListData);

    boolean updateProductEquiCode(SlctProductEquivalenceDataListData slctProductPointMasterListData);

	boolean syncMarketMappingIds(MarketMappingInboundSyncData marketMappingInboundSyncData);

	boolean insertUpdateStockAllocationData(DealerInvoiceSummaryData dealerInvoiceSummaryData);

	List<SclSiteMasterModel> getSiteTransactionDetailsForSLCT();


	/**
	 * Fetches the list of non-synced SiteTransaction Models to be sent to SLCT
	 * @return the list of non-synced SiteTransaction Models for SLCT integration
	 */
    List<SiteTransactionModel> getSiteConversionDetailsForSLCT();

	/**
	 * Returns true if all the SiteTransaction Models are synced for the given IDs
	 * @param siteConversionInboundSyncData
	 * @return
	 */
	boolean syncSiteTransactionData(SiteConversionInboundSyncData siteConversionInboundSyncData);

	boolean isCustomerBlocked(SclCustomerModel customerModel);


	public PartnerCustomerModel findPartnerCustomerByCustomerNo(String customerNo);
}