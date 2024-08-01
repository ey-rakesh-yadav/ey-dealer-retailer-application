package com.scl.core.services;

import com.scl.core.model.*;
import com.scl.facades.data.*;
import com.scl.facades.data.marketvisit.MarketVisitDetailsData;
import com.scl.facades.data.marketvisit.MarketVisitDeviationData;
import com.scl.facades.djp.data.AddNewSiteData;
import com.scl.facades.djp.data.marketvisit.VisitSummaryData;
import com.scl.facades.visit.data.DealerSummaryData;
import com.scl.facades.visit.data.InfluencerSummaryData;
import com.scl.facades.visit.data.RetailerSummaryData;
import com.scl.facades.visit.data.SiteSummaryData;
import com.scl.occ.dto.djp.CounterVisitAnalyticsWsDTO;
import com.scl.occ.dto.djp.DJPCounterWsDTO;
import com.scl.occ.dto.djp.DJPFinalizedPlanWsDTO;
import com.scl.occ.dto.djp.UpdateCountersWsDTO;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.core.model.c2l.BrandModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.webservicescommons.dto.error.ErrorListWsDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

public interface DJPVisitService {

	public boolean submitMarketMappingDetails(CounterVisitMasterData data);

	public boolean submitBrandInsightDetails(String counterVisitId, BrandingInsightData brandingInsightData);

	public boolean submitLeadGeneration(LeadMasterData leadMasterData, String counterVisitId);

	public boolean submitFlagDealer(String counterVisitId, boolean isFlagged, String remarkForFlag);

	public boolean submitFeedbackAndComplaints(String counterVisitId, FeedbackAndComplaintsListData data);

	DropdownListData getEnumTypes(String type);

	public Collection<CompetitorProductModel> getCompetitorProducts(String brandId);

	SearchPageData<VisitMasterModel> getMarketVisitDetails(SearchPageData searchPageData);

	VisitMasterModel getCounterList(String id);

	public List<TruckModelMasterModel> findAllTrucks();

	public boolean submitTruckFleetDetails(DealerFleetListData dealerFleetListData, String counterVisitId);

	CounterAggregationData getCounterAggregationData(String counterVisitId);

	void createAndSaveFinalizedCounterVisitPlan(final DJPFinalizedPlanWsDTO finalizedPlan);

	List<DJPCounterScoreMasterModel> getDJPCounterScores(String routeScoreId, String objectiveId);

	boolean startDjpVisit(String visitId);

	boolean completeDjpVisit(String visitId);

	boolean startCounterVisit(String counterVisitId);

	long completeCounterVisit(String counterVisitId);

	SearchPageData<VisitMasterModel> getReviewLogs(SearchPageData searchPageData, String startDate, String endDate, String searchKey, boolean isDjpApprovalWidget);

	List<SclCustomerModel> getFilteredCounters(final DJPFinalizedPlanWsDTO plannedData);

	String getCustomerType(final SclCustomerModel sclCustomerModel);

	public Long getCountOfCounterNotVisited();

	public Long getCountOfTotalJouneyPlanned();

	public Map<String, Double> getAvgTimeSpent();

	Collection<SclCustomerModel> getAdHocExistingCounters(DJPFinalizedPlanWsDTO plannedData);

	void createAndSaveSite(final AddNewSiteData newSiteData);

	Set<RouteMasterModel> findAllRoutesForSO(SclUserModel sclUserModel);

	boolean submitSiteVisitForm(String counterVisitId, SiteVisitFormData siteVisitFormData);

	public Collection<SclCustomerModel> getcounterNotVisitedList(int month, int year);

	public Map<String, String> getLastSixCounterVisitDates(String customerId);

	public Map<String, Object> counterVisitedForSelectedRoutes(String routeScoreId);

	public boolean saveOrderRequisitionForTaggedSites(OrderRequistionMasterData orderRequisitionData, String counterVisitId,
													  String siteCode);

	public String getLastVisitDate(String counterVisitId);

	public Integer getVisitCountMTD(String counterVisitId);

	ErrorListWsDTO updateAdhocCounters(final UpdateCountersWsDTO adHocCountersWsDTO);

	ErrorListWsDTO updateCounters(final UpdateCountersWsDTO updateCountersWsDTO);

	public Collection<CounterVisitMasterModel> getSelectedCounterList(String id);

	CounterVisitAnalyticsWsDTO getCompletedVisitStatisticsDataForSO(final SclUserModel sclUserModel);


	public SiteSummaryData getSiteSummary(String counterVisitId);

	public DealerSummaryData getDealerSummary(String counterVisitId);

	public SclCustomerModel getProductMixForDealerSummary(String counterVisitId);

	public RetailerSummaryData getRetailerSummary(String code,List<String> subAreaList,List<String> districtList);

	public CounterVisitMasterModel getCounterVisitMasterForLastVisitDate(String counterVisitId);

	public List<DealersFleetDetailsModel> getDealerFleetDetails(String counterVisitId);

	String calculatePlanComplianceForSODJP(SclUserModel sclUserModel, final Date planStartDate, final Date planEndDate);

	SclUserModel getCurrentSalesOfficer();

	public InfluencerSummaryData getInfluencerSummary(String counterVisitId,List<String> subAreaList,List<String> districtList);

	public List<B2BCustomerModel> getInfluencerDetails(String counterVisitId);

	boolean submitUnflagDealer(String counterVisitId, boolean isUnFlagged, String remarkForUnflag);

	public Collection<CounterVisitMasterModel> getTodaysPlan();

	public String getRouteForId(String id);

	public DropdownListData getListOfRoutes(List<String> subAreas);

	Double getTotalOrderGenerated(String siteCode, String counterVisitId);

	String submitSchemeDetails(String counterVisitId, SchemeDetailsModel schemeDetailsModel);

	void submitSchemeDocuments(final String schemeID, MultipartFile[] files);

	public SalesHistoryData getSalesHistoryForDealer(String counterVisitId);

	List<MonthlySalesData> getLastSixMonthSalesForDealer(String counterVisitId);

	List<MonthlySalesData> getMonthlySalesForDealer(List<MonthlySalesData> dataList, SclCustomerModel customer);

	MarketVisitDetailsData getMarketVisitDetailsData(final String visitId);

	void saveVisitSummary(final VisitSummaryData visitSummaryData);

	MarketVisitDetailsData getJounreyDetailsData(String visitId);

	MarketVisitDeviationData getCounterDeviationData(VisitMasterModel visitMasterModel, boolean journeyDetails);

	MarketVisitDeviationData getRouteDeviationData(VisitMasterModel visitMasterModel, boolean journeyDetails);

	MarketVisitDeviationData getObjectiveDeviationData(VisitMasterModel visitMasterModel, boolean journeyDetails);

	void createCounterRouteMapping(String state, String district, String taluka, BaseSiteModel brand, String counterCode, SclUserModel employee, String routeId, String routeName);

	SalesHistoryData getSalesHistoryDataFor360(String counterVisitId,List<String> subAreaList,List<String> districtList);

	List<MonthlySalesData> getLastSixMonthSalesForRetailer(String counterVisitId);

	Collection<ObjectiveModel> getDJPObjective(String routeScoreId, List<ObjectiveModel> recommendedObj);

	Collection<DJPRouteScoreMasterModel> getDJPRouteScores(String plannedDate,
														   List<DJPRouteScoreMasterModel> recommendedRoute, String district, String taluka);

	CustomerSubAreaMappingModel createCustomerSubAreaMapping(String state, String district, String taluka, SclCustomerModel sclCustomer, CMSSiteModel site);

	public List<VisitMasterModel> getAllVisit(String startDate, String endDate);

	Boolean saveCustomerCoordinates(String customerId, Double latitude, Double longitude);

	List<ObjectiveModel> getAllObjective();

	List<RouteMasterModel> getRouteMasterList(String plannedDate, String subAreaMasterPk, List<RouteMasterModel> recommendedRoute);

	Integer flaggedDealerCount();

	Integer unFlaggedDealerRequestCount();

	boolean updateUnFlagRequestApprovalByTSM(UnFlagRequestApprovalData unFlagRequestApprovalData);


	VisitMasterModel updateStatusForApprovalByTsm(String visitId);

	VisitMasterModel updateStatusForRejectedByTsm(String visitId);

	
	Integer getPendingApprovalVisitsCountForTsmorRh();

	Double calculateDJPCompliance();

    DropdownListData getPartnerType();

	public Map<String, Object> counterVisitedForRoutes(String route);

	public VisitMasterModel createVisitMasterData(final DJPRouteScoreMasterModel djpRouteScoreMaster , final String objectiveId,final SclUserModel sclUserModel, String subAreaMasterId, String planDate);

    Integer getRetailerCountByTerritory(SclCustomerModel sclCustomerModel,Collection<TerritoryMasterModel> soTerritory);

	List<SclCustomerModel> filterCustomerByDOTerritoryCode(List<SclCustomerModel> sclCustomerModels);

	/**
	 * Get List of All Brands & Competitor Products
	 * @param brandIds
	 * @return
	 */

	public Map<List<BrandModel>,List<CompetitorProductModel>> getBrandsCompetitorProducts(List<String> brandIds);

	/**
	 * Get Selected Brands and Competitor Products with Retailer/Dealer Uid
	 * @param uid
	 * @return
	 */
	public Map<List<BrandModel>,List<CompetitorProductModel>> getBrandsCompetitorProductsByUid(String uid);

	/**
	 * Get Marking Mapping Details with Latest Counter Visit & Uid
	 * @param uid
	 * @return
	 */
	List<MarketMappingDetailsModel> getMarketMappingDetails(String uid);

	List<MonthlySalesData>  getLastSixMonthSalesForRetailer(List<MonthlySalesData> dataList, SclCustomerModel sclretailer,List<SclCustomerModel> sclDealer);

	Boolean isNonSclCounter(final SclCustomerModel sclCustomerModel);
	AddressModel getCustomerOwnAddress(final SclCustomerModel sclCustomerModel);
}
