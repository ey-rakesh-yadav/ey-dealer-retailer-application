package com.eydms.core.services;

import com.eydms.core.model.*;
import com.eydms.facades.data.*;
import com.eydms.facades.data.marketvisit.MarketVisitDetailsData;
import com.eydms.facades.data.marketvisit.MarketVisitDeviationData;
import com.eydms.facades.djp.data.AddNewSiteData;
import com.eydms.facades.djp.data.marketvisit.VisitSummaryData;
import com.eydms.facades.visit.data.DealerSummaryData;
import com.eydms.facades.visit.data.InfluencerSummaryData;
import com.eydms.facades.visit.data.RetailerSummaryData;
import com.eydms.facades.visit.data.SiteSummaryData;
import com.eydms.occ.dto.djp.CounterVisitAnalyticsWsDTO;
import com.eydms.occ.dto.djp.DJPCounterWsDTO;
import com.eydms.occ.dto.djp.DJPFinalizedPlanWsDTO;
import com.eydms.occ.dto.djp.UpdateCountersWsDTO;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
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

	List<EyDmsCustomerModel> getFilteredCounters(final DJPFinalizedPlanWsDTO plannedData);

	String getCustomerType(final EyDmsCustomerModel eydmsCustomerModel);

	public Long getCountOfCounterNotVisited();

	public Long getCountOfTotalJouneyPlanned();

	public Map<String, Double> getAvgTimeSpent();

	Collection<EyDmsCustomerModel> getAdHocExistingCounters(DJPFinalizedPlanWsDTO plannedData);

	void createAndSaveSite(final AddNewSiteData newSiteData);

	Set<RouteMasterModel> findAllRoutesForSO(EyDmsUserModel eydmsUserModel);

	boolean submitSiteVisitForm(String counterVisitId, SiteVisitFormData siteVisitFormData);

	public Collection<EyDmsCustomerModel> getcounterNotVisitedList(int month, int year);

	public Map<String, String> getLastSixCounterVisitDates(String customerId);

	public Map<String, Object> counterVisitedForSelectedRoutes(String routeScoreId);

	public boolean saveOrderRequisitionForTaggedSites(OrderRequistionMasterData orderRequisitionData, String counterVisitId,
													  String siteCode);

	public String getLastVisitDate(String counterVisitId);

	public Integer getVisitCountMTD(String counterVisitId);

	ErrorListWsDTO updateAdhocCounters(final UpdateCountersWsDTO adHocCountersWsDTO);

	ErrorListWsDTO updateCounters(final UpdateCountersWsDTO updateCountersWsDTO);

	public Collection<CounterVisitMasterModel> getSelectedCounterList(String id);

	CounterVisitAnalyticsWsDTO getCompletedVisitStatisticsDataForSO(final EyDmsUserModel eydmsUserModel);


	public SiteSummaryData getSiteSummary(String counterVisitId);

	public DealerSummaryData getDealerSummary(String counterVisitId);

	public EyDmsCustomerModel getProductMixForDealerSummary(String counterVisitId);

	public RetailerSummaryData getRetailerSummary(String code);

	public CounterVisitMasterModel getCounterVisitMasterForLastVisitDate(String counterVisitId);

	public List<DealersFleetDetailsModel> getDealerFleetDetails(String counterVisitId);

	String calculatePlanComplianceForSODJP(EyDmsUserModel eydmsUserModel, final Date planStartDate, final Date planEndDate);

	EyDmsUserModel getCurrentSalesOfficer();

	public InfluencerSummaryData getInfluencerSummary(String counterVisitId);

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

	void getMonthlySalesForDealer(List<MonthlySalesData> dataList, EyDmsCustomerModel customer);

	MarketVisitDetailsData getMarketVisitDetailsData(final String visitId);

	void saveVisitSummary(final VisitSummaryData visitSummaryData);

	MarketVisitDetailsData getJounreyDetailsData(String visitId);

	MarketVisitDeviationData getCounterDeviationData(VisitMasterModel visitMasterModel, boolean journeyDetails);

	MarketVisitDeviationData getRouteDeviationData(VisitMasterModel visitMasterModel, boolean journeyDetails);

	MarketVisitDeviationData getObjectiveDeviationData(VisitMasterModel visitMasterModel, boolean journeyDetails);

	void createCounterRouteMapping(String state, String district, String taluka, BaseSiteModel brand, String counterCode, EyDmsUserModel employee, String routeId, String routeName);

	SalesHistoryData getSalesHistoryDataFor360(String counterVisitId);

	List<MonthlySalesData> getLastSixMonthSalesForRetailer(String counterVisitId);

	Collection<ObjectiveModel> getDJPObjective(String routeScoreId, List<ObjectiveModel> recommendedObj);

	Collection<DJPRouteScoreMasterModel> getDJPRouteScores(String plannedDate,
														   List<DJPRouteScoreMasterModel> recommendedRoute, String district, String taluka);

	CustomerSubAreaMappingModel createCustomerSubAreaMapping(String state, String district, String taluka, EyDmsCustomerModel eydmsCustomer, CMSSiteModel site);

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

	void getLastSixMonthSalesForRetailer(List<MonthlySalesData> dataList, EyDmsCustomerModel customer);

	public Map<String, Object> counterVisitedForRoutes(String route);

	public VisitMasterModel createVisitMasterData(final DJPRouteScoreMasterModel djpRouteScoreMaster , final String objectiveId,final EyDmsUserModel eydmsUserModel, String subAreaMasterId, String planDate);
}
