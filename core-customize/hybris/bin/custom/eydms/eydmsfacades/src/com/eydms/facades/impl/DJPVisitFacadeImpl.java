package com.eydms.facades.impl;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.customer.services.EyDmsCustomerService;
import com.eydms.core.dao.DJPVisitDao;
import com.eydms.core.dao.DjpRouteScoreDao;
import com.eydms.core.enums.ApprovalStatus;
import com.eydms.core.lead.services.EyDmsLeadService;
import com.eydms.core.model.*;
import com.eydms.core.services.DJPVisitService;
import com.eydms.facades.DJPVisitFacade;
import com.eydms.facades.SalesPerformanceFacade;
import com.eydms.facades.data.*;
import com.eydms.facades.data.marketvisit.MarketVisitDetailsData;
import com.eydms.facades.djp.data.*;
import com.eydms.facades.djp.data.marketvisit.VisitSummaryData;
import com.eydms.facades.marketvisit.scheme.SchemeDetailsData;
import com.eydms.facades.visit.data.DealerSummaryData;
import com.eydms.facades.visit.data.InfluencerSummaryData;
import com.eydms.facades.visit.data.RetailerSummaryData;
import com.eydms.facades.visit.data.SiteSummaryData;
import com.eydms.occ.dto.djp.CounterVisitAnalyticsWsDTO;
import com.eydms.occ.dto.djp.DJPFinalizedPlanWsDTO;
import com.eydms.occ.dto.djp.UpdateCountersWsDTO;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.webservicescommons.dto.error.ErrorListWsDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;


public class DJPVisitFacadeImpl implements DJPVisitFacade{

	private static final Logger LOG = Logger.getLogger(DJPVisitFacadeImpl.class);

	@Autowired
	DJPVisitService djpVisitService;

	@Autowired
	EyDmsLeadService eydmsLeadService;
	
	@Autowired
	Converter<VisitMasterModel,VisitMasterData> visitDataConverter;
	
	@Autowired
	Converter<CounterVisitMasterModel,CounterVisitData> counterVisitDataConverter;
	
	@Autowired
	Converter<CounterVisitMasterModel,SiteVisitFormData> siteVisitDataConverter;
	
	@Autowired
	Converter<VisitMasterModel,VisitMasterData> reviewLogsVisitDataConverter;
	
	@Autowired
	Populator<CounterVisitMasterModel,CounterVisitMasterData> counterVisitMasterDataPopulator;

	@Autowired
	Converter<DealersFleetDetailsModel,DealerFleetData> dealerFleetDetailsConverter;
	
	@Autowired
	Converter<B2BCustomerModel,EyDmsSiteData> eydmsSiteConverter;
	
	@Resource
	private UserService userService;

	@Resource
	private Populator<SchemeDetailsData,SchemeDetailsModel> schemeDetailsReversePopulator;

	@Resource
	private ModelService modelService;
	
	@Resource
	SalesPerformanceFacade salesPerformanceFacade;
	
	@Resource
	EyDmsCustomerService eydmsCustomerService;

	@Resource
	BaseSiteService baseSiteService;
	
    @Autowired
    DjpRouteScoreDao djpRouteScoreDao;
    
    @Autowired
    DJPVisitDao djpVisitDao;

	@Override
	public boolean submitMarketMappingDetails(CounterVisitMasterData data) {
		return djpVisitService.submitMarketMappingDetails(data);
	}

	@Override
	public boolean submitBrandInsightDetails(String counterVisitId, BrandingInsightData brandingInsightData) {
		return djpVisitService.submitBrandInsightDetails(counterVisitId,brandingInsightData);
	}
	@Override
	public String submitSchemeDetails(String counterVisitId, SchemeDetailsData schemeDetailsData) {
		/*if(null!= files && files.length>0){
			schemeDetailsData.setDocuments(Arrays.asList(files));
		}*/
		SchemeDetailsModel schemeDetailsModel = modelService.create(SchemeDetailsModel.class);
		schemeDetailsReversePopulator.populate(schemeDetailsData,schemeDetailsModel);
		return  djpVisitService.submitSchemeDetails(counterVisitId,schemeDetailsModel);
	}

	@Override
	public void submitSchemeDocuments(String schemeID, MultipartFile [] files) {

		djpVisitService.submitSchemeDocuments(schemeID,files);
	}

	@Override
	public void submitVisitSummaryDetails(final VisitSummaryData visitSummaryData) {
		djpVisitService.saveVisitSummary(visitSummaryData);
	}
	@Override
	public boolean submitLeadGeneration(LeadMasterData leadMasterData, String counterVisitId) {
		return djpVisitService.submitLeadGeneration(leadMasterData, counterVisitId);
	}

	@Override
	public boolean submitFlagDealer(String counterVisitId, boolean isFlagged, String remarkForFlag) {
		return djpVisitService.submitFlagDealer(counterVisitId, isFlagged, remarkForFlag);
	}
	
	@Override
	public boolean submitUnflagDealer(String counterVisitId, boolean isUnFlagged, String remarkForUnflag) {
		return djpVisitService.submitUnflagDealer(counterVisitId, isUnFlagged, remarkForUnflag);
	}

	@Override
	public boolean submitFeedbackAndComplaints(String counterVisitId, FeedbackAndComplaintsListData data) {
		return djpVisitService.submitFeedbackAndComplaints(counterVisitId,data);
	}

	@Override
	public DropdownListData getEnumTypes(String type) {
		return djpVisitService.getEnumTypes(type);
	}
	
	@Override
	public CompetitorProductListData getCompetitorProducts(String brandId) {
		CompetitorProductListData listData = new CompetitorProductListData();
		Collection<CompetitorProductModel> listModel = djpVisitService.getCompetitorProducts(brandId);
		List<CompetitorProductData> dataList = listModel.stream().map(this::populateCompetitorProductData).collect(Collectors.toList());
		listData.setCompetitorProductList(dataList);
		return listData;
	}
	
	@Override
	public List<TruckModelData> getAllTrucks() {
		List<TruckModelMasterModel> modelList =  djpVisitService.findAllTrucks();
		List<TruckModelData> dataList = new ArrayList<TruckModelData>();
		modelList.stream().forEach(truck-> 
		{
			TruckModelData data = new TruckModelData();
			data.setTruckModel(truck.getTruckModel());
			data.setCapacity(truck.getCapacity());
			data.setVehicleMake(truck.getVehicleMake());
			data.setVehicleType(String.valueOf(truck.getVehicleType()));
			//data.setCount(truck.getCount());
			dataList.add(data);
		});
		return dataList;
	}
	
	@Override
	public boolean submitTruckFleetDetails(DealerFleetListData dealerFleetListData, String counterVisitId) {
		return djpVisitService.submitTruckFleetDetails(dealerFleetListData, counterVisitId);
	}

	private CompetitorProductData populateCompetitorProductData(CompetitorProductModel model) {
		CompetitorProductData data = new CompetitorProductData();
		data.setCode(model.getCode());
		data.setName(model.getName());
		data.setGrade(model.getGrade());
		data.setPackaging(model.getPackaging());
		data.setState(model.getState());
		return data;
	}

	@Override
	public SearchPageData<VisitMasterData> getMarketVisitDetails(SearchPageData searchPageData) {
		SearchPageData<VisitMasterModel> visitDetails = djpVisitService.getMarketVisitDetails(searchPageData);
		SearchPageData<VisitMasterData> result = new SearchPageData();
		result.setPagination(visitDetails.getPagination());
		result.setSorts(visitDetails.getSorts());
		List<VisitMasterData> list = visitDataConverter.convertAll(visitDetails.getResults());
		result.setResults(list);
		return result;
	}	

	public CounterAggregationData getCounterAggregationData( String counterVisitId) {
		return djpVisitService.getCounterAggregationData(counterVisitId);
	}

	@Override

	public List<CounterVisitData> getCounterList(String id) {
		List<CounterVisitMasterModel> counterList = (List<CounterVisitMasterModel>) djpVisitService.getCounterList(id).getCounterVisits();
		List<CounterVisitData> result = counterVisitDataConverter.convertAll(counterList);
		return result;
	}

  @Override
	public void finalizeCounterVisitPlan(final DJPFinalizedPlanWsDTO finalizedPlan){
		djpVisitService.createAndSaveFinalizedCounterVisitPlan(finalizedPlan);
	}

	@Override
	public List<DJPCounterScoreData> getDJPCounterScores(String routeScoreId, String objectiveId){
		List<DJPCounterScoreData> counterScoreList = new ArrayList<DJPCounterScoreData>();
		List<DJPCounterScoreMasterModel> modelList = djpVisitService.getDJPCounterScores(routeScoreId, objectiveId);

		if(modelList!=null) {
			modelList.forEach(model -> {
				if(model.getCustomer()!=null) {
					DJPCounterScoreData data = new DJPCounterScoreData();
					data.setId(model.getId());
					data.setCustomerCode(model.getCustomer().getUid());
					data.setCustomerName(model.getCustomer().getName());
					data.setCustomerType(djpVisitService.getCustomerType(model.getCustomer()));
					data.setCounterId(model.getCounterID());
					data.setCounterPotential(model.getCustomer().getCounterPotential()!=null?model.getCustomer().getCounterPotential():0);
					data.setCounterScore(model.getCounterScore());
					data.setVisitSequence(model.getVisitSequence());
					if(model.getCustomer().getLastVisitTime()!=null) {
						DateFormat dateFormat = new SimpleDateFormat(EyDmsCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);  
						String strDate = dateFormat.format(model.getCustomer().getLastVisitTime());
						data.setLastVisitDate(strDate);
					}
					data.setObjectiveId(objectiveId);
					data.setRouteScoreId(routeScoreId);
                	data.setCustomerNo(model.getCustomer().getCustomerNo());
					counterScoreList.add(data);
				}
			});
		}
		return counterScoreList;
	}

	@Override
	public List<CounterDetailsData> getExistingCounters(final DJPFinalizedPlanWsDTO plannedData){
		List<EyDmsCustomerModel> filteredCounters = djpVisitService.getFilteredCounters(plannedData);
		return populateCounterData(filteredCounters);
	}

	private List<CounterDetailsData> populateCounterData(Collection<EyDmsCustomerModel> filteredCounters) {
		List<CounterDetailsData> counterDetailsDataList = new ArrayList<>();
		if(CollectionUtils.isNotEmpty(filteredCounters)){
			for(EyDmsCustomerModel eydmsCustomerModel : filteredCounters){
				if(eydmsCustomerModel!=null) {
					CounterDetailsData  counterDetailsData = new CounterDetailsData();

					counterDetailsData.setCustomerCode(eydmsCustomerModel.getUid());
					counterDetailsData.setCustomerName(eydmsCustomerModel.getName());
					counterDetailsData.setCounterPotential(eydmsCustomerModel.getCounterPotential());

					if(null!= eydmsCustomerModel.getLastVisitTime()){
						DateFormat dateFormat = new SimpleDateFormat(EyDmsCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
						counterDetailsData.setLastVisitDate(dateFormat.format(eydmsCustomerModel.getLastVisitTime()));
					}
					counterDetailsData.setCustomerType(djpVisitService.getCustomerType(eydmsCustomerModel));

					counterDetailsData.setCustomerNo(eydmsCustomerModel.getCustomerNo()!=null ? eydmsCustomerModel.getCustomerNo() : "");
					LOG.info("CounterDetails Data adhoc " + counterDetailsData);
					counterDetailsDataList.add(counterDetailsData);
				}
			}
		}
		return counterDetailsDataList;
	}



	@Override
	public boolean startDjpVisit(String visitId) {
		return djpVisitService.startDjpVisit(visitId);
	}

	@Override
	public boolean completeDjpVisit(String visitId) {
		return djpVisitService.completeDjpVisit(visitId);
	}

	@Override
	public boolean startCounterVisit(String counterVisitId) {
		return djpVisitService.startCounterVisit(counterVisitId);
	}

	@Override
	public long completeCounterVisit(String counterVisitId) {
		return djpVisitService.completeCounterVisit(counterVisitId);
	}

	@Override
	public List<DJPRouteScoreData> getDJPRouteScores(String plannedDate, String district, String taluka) {
		List<DJPRouteScoreData> dataList = new ArrayList<DJPRouteScoreData>();
		List<DJPRouteScoreMasterModel> reccomendedRoutes = new ArrayList<DJPRouteScoreMasterModel>();
		Collection<DJPRouteScoreMasterModel> modelList = djpVisitService.getDJPRouteScores(plannedDate, reccomendedRoutes, district, taluka);
		if(modelList!=null) {
			modelList.forEach(model -> {
				if(model.getRoute()!=null) {
					DJPRouteScoreData data = new DJPRouteScoreData();
					data.setId(model.getId());
					data.setRoute(model.getRoute().getRouteId());
					data.setRouteScore(model.getRoutesScore());
					data.setRouteName(model.getRoute().getRouteName()!=null ? model.getRoute().getRouteName() : model.getRoute().getRouteId());
					Boolean recommended = Boolean.FALSE;
					if(reccomendedRoutes.contains(model)) {
						recommended = true;
					}
					data.setRecommended(recommended);
					dataList.add(data);
				}
			});
		}
		return dataList;
	}
	
	@Override
	public List<DJPRouteScoreData> getDJPRouteScores(String plannedDate, String subAreaMasterPk) {
		List<DJPRouteScoreData> dataList = new ArrayList<DJPRouteScoreData>();
		List<DJPRouteScoreMasterModel> reccomendedRoutes = new ArrayList<DJPRouteScoreMasterModel>();
		List<RouteMasterModel> recommendedRoute = new ArrayList<>();
		List<RouteMasterModel> routeMasterList =  djpVisitService.getRouteMasterList(plannedDate, subAreaMasterPk, recommendedRoute);
		if(routeMasterList!=null && !routeMasterList.isEmpty()) {
			Collection<DJPRouteScoreMasterModel> modelList = djpVisitService.getDJPRouteScores(plannedDate, reccomendedRoutes, routeMasterList.get(0).getSubAreaMaster().getDistrict(), routeMasterList.get(0).getSubAreaMaster().getTaluka());
			routeMasterList.forEach(model -> {
				DJPRouteScoreData data = new DJPRouteScoreData();
				Optional<DJPRouteScoreMasterModel> djpRouteModelopt = modelList.stream()
						.filter(djpRoute-> djpRoute.getRoute()!=null && djpRoute.getRoute().equals(model)).findAny();
				if(djpRouteModelopt.isPresent()) {
					DJPRouteScoreMasterModel djpRoutesModel = djpRouteModelopt.get();
					data.setId(djpRoutesModel.getId());
					data.setRouteScore(djpRoutesModel.getRoutesScore());			
				}

				data.setRoute(model.getRouteId());
				data.setRouteName(model.getRouteName()!=null ? model.getRouteName() : model.getRouteId());
				Boolean recommended = Boolean.FALSE;
				if(recommendedRoute.contains(model)) {
					recommended = true;
				}
				data.setRecommended(recommended);		
				dataList.add(data);
			});
		}
		return dataList;
	}

	@Override
	public List<ObjectiveData> getDJPObjective(String routeId, String routeScoreId) {
		List<ObjectiveData> dataList = new ArrayList<ObjectiveData>();
		Collection<ObjectiveModel> modelList = djpVisitService.getAllObjective();

		if(routeScoreId!=null) {
			DJPRouteScoreMasterModel djpRouteScore = djpRouteScoreDao.findByRouteScoreId(routeScoreId);
			if(djpRouteScore!=null) {
				ObjectiveModel recommendedObj1 = djpVisitDao.findOjectiveById(djpRouteScore.getRecommendedObj1());
				if(recommendedObj1!=null) {
					populateObjectiveDate(recommendedObj1, true, dataList);
					modelList = modelList.stream().filter(obj->!obj.equals(recommendedObj1)).collect(Collectors.toList());
				}
				ObjectiveModel recommendedObj2 = djpVisitDao.findOjectiveById(djpRouteScore.getRecommendedObj2());
				if(recommendedObj2!=null) {
					populateObjectiveDate(recommendedObj2, true, dataList);
					modelList = modelList.stream().filter(obj->!obj.equals(recommendedObj2)).collect(Collectors.toList());
				}
			}
		}
		if(modelList!=null) {
			modelList.forEach(model -> {
				populateObjectiveDate(model, false, dataList);
			});
		}
		return dataList;
	}

	@Override
	public Integer flaggedDealerCount() {
		return djpVisitService.flaggedDealerCount();
	}

	@Override
	public Integer unFlaggedDealerRequestCount() {
		return djpVisitService.unFlaggedDealerRequestCount();
	}

	@Override
	public boolean updateUnFlagRequestApprovalByTSM(UnFlagRequestApprovalData unFlagRequestApprovalData) {
		return djpVisitService.updateUnFlagRequestApprovalByTSM(unFlagRequestApprovalData);
	}

	@Override
	public boolean updateStatusForApproval(String visitId) {
		EyDmsUserModel currentUser = (EyDmsUserModel) userService.getCurrentUser();
		BaseSiteModel baseSite = (BaseSiteModel) baseSiteService.getCurrentBaseSite();
		if(visitId!=null) {
			LOG.info("VisitID");
			VisitMasterModel visit = djpVisitService.updateStatusForApprovalByTsm(visitId);
			if (visit != null) {
				LOG.info("Visit is DI approved");
				visit.setApprovalStatus(ApprovalStatus.DI_APPROVED);
				visit.setApprovalDate(new Date());
				visit.setApprovedBy(currentUser);
				modelService.save(visit);
				return true;
			}
			else {
				LOG.info("No visit ID");
				return false;
			}
		}
		else {
			LOG.info("No visit ID is present");
				return false;
			}

	}

	@Override
	public boolean updateStatusForRejectedByTsm(String visitId) {
		EyDmsUserModel currentUser = (EyDmsUserModel) userService.getCurrentUser();
		BaseSiteModel baseSite = (BaseSiteModel) baseSiteService.getCurrentBaseSite();
		if(visitId!=null) {
			LOG.info("VisitID");
			VisitMasterModel visit = djpVisitService.updateStatusForRejectedByTsm(visitId);
			if (visit != null) {
				LOG.info("Visit is DI rejected");
				visit.setApprovalStatus(ApprovalStatus.DI_REJECTED);
				visit.setApprovalDate(new Date());
				visit.setApprovedBy(currentUser);
				modelService.save(visit);
				return true;
			}
			else {
				LOG.info("No visit IDs");
				return false;
			}
		}
		else {
			LOG.info("No visit ID is present");
			return false;
		}
	}

	private void populateObjectiveDate(ObjectiveModel model, Boolean recommended, List<ObjectiveData> dataList) {
		ObjectiveData data = new ObjectiveData();
		data.setId(model.getObjectiveId());
		data.setName(model.getObjectiveName());
		data.setRecommended(recommended);
		dataList.add(data);	
	}
	
	@Override
	public List<ObjectiveData> getDJPObjective(String routeScoreId) {
		List<ObjectiveData> dataList = new ArrayList<ObjectiveData>();
		List<ObjectiveModel> reccomendedObjs = new ArrayList<ObjectiveModel>();
		Collection<ObjectiveModel> modelList = djpVisitService.getDJPObjective(routeScoreId, reccomendedObjs);
		if(modelList!=null) {
			modelList.forEach(model -> {
				ObjectiveData data = new ObjectiveData();
				data.setId(model.getObjectiveId());
				data.setName(model.getObjectiveName());
				Boolean recommended = Boolean.FALSE;
				if(reccomendedObjs.contains(model)) {
					recommended = true;
				}
				data.setRecommended(recommended);
				dataList.add(data);
			});
		}
		return dataList;
	}

	@Override
	public SearchPageData<VisitMasterData> getReviewLogs(SearchPageData searchPageData, String startDate, String endDate, String searchKey, boolean isDjpApprovalWidget) {
		
		SearchPageData<VisitMasterModel> visitDetails = djpVisitService.getReviewLogs(searchPageData,startDate,endDate,searchKey,isDjpApprovalWidget);
		SearchPageData<VisitMasterData> result = new SearchPageData();
		result.setPagination(visitDetails.getPagination());
		result.setSorts(visitDetails.getSorts());
		List<VisitMasterData> list = reviewLogsVisitDataConverter.convertAll(visitDetails.getResults());
		result.setResults(list);
		return result;
	}

	@Override
	public Long getCountOfCounterNotVisited() {
		return djpVisitService.getCountOfCounterNotVisited();
	}

	@Override
	public Long getCountOfTotalJouneyPlanned() {
		return djpVisitService.getCountOfTotalJouneyPlanned();
	}

	@Override
	public Map<String, Double> getAvgTimeSpent() {
		return djpVisitService.getAvgTimeSpent();
	}

	@Override
	public List<CounterDetailsData> getAdHocExistingCounters(final DJPFinalizedPlanWsDTO plannedData){
		Collection<EyDmsCustomerModel> filteredCounters = djpVisitService.getAdHocExistingCounters(plannedData);
		LOG.info("FilteredCounters:"+filteredCounters);
		return populateCounterData(filteredCounters);
	}
	
	@Override
	public void createAndSaveSiteDetails(final AddNewSiteData siteData){
		djpVisitService.createAndSaveSite(siteData);
	}

	@Override
	public List<RouteData> getRoutesForSalesofficer(){
		final EyDmsUserModel eydmsUserModel = djpVisitService.getCurrentSalesOfficer();
		if(null!= eydmsUserModel){
			List<RouteData> routeData = new ArrayList<>();
			Set<RouteMasterModel> routeMasterModels = djpVisitService.findAllRoutesForSO(eydmsUserModel);
			if(CollectionUtils.isNotEmpty(routeMasterModels) && routeMasterModels.size()>0){
				routeData = populateRouteData(routeMasterModels);
			}
			return routeData;
		}
		else{
			throw new ModelNotFoundException("Could not find current logged in sales officer");
		}
	}

	List<RouteData> populateRouteData(final Set<RouteMasterModel> routeMasterModels){
		List<RouteData> routeDataList = new ArrayList<>();
		for(RouteMasterModel routeMaster: routeMasterModels){
			RouteData routeData = new RouteData();
			routeData.setId(routeMaster.getRouteId());
			routeDataList.add(routeData);
		}
		return routeDataList;
	}

	@Override
	public boolean submitSiteVisitForm(String counterVisitId, SiteVisitFormData siteVisitFormData) {
		return djpVisitService.submitSiteVisitForm(counterVisitId,siteVisitFormData);
	}
	
	@Override
	public List<CounterDetailsData> getcounterNotVisitedList(int month, int year) {
		Collection<EyDmsCustomerModel> filteredCounters = djpVisitService.getcounterNotVisitedList(month, year);
		return populateCounterData(filteredCounters);
	}

	@Override
	public Map<String, String> getLastSixCounterVisitDates(String customerId) {
		return djpVisitService.getLastSixCounterVisitDates(customerId);
	}

	@Override
	public Map<String, Object> counterVisitedForSelectedRoutes(String routeScoreId) {
		return djpVisitService.counterVisitedForSelectedRoutes(routeScoreId);
	}		
	@Override
	public boolean saveOrderRequisitionForTaggedSites(OrderRequistionMasterData orderRequisitionData, String counterVisitId, String siteCode) {
		return djpVisitService.saveOrderRequisitionForTaggedSites(orderRequisitionData, counterVisitId, siteCode);
	}

	@Override
	public ErrorListWsDTO updateAdHocCounters(UpdateCountersWsDTO updateCountersWsDTO) {
		return djpVisitService.updateAdhocCounters(updateCountersWsDTO);
	}

	@Override
	public ErrorListWsDTO updateCounters(UpdateCountersWsDTO updateCountersWsDTO) {
		return djpVisitService.updateCounters(updateCountersWsDTO);
	}
	
	@Override
	public List<CounterVisitData> getSelectedCounterList(String id) {
		Collection<CounterVisitMasterModel> counterList = djpVisitService.getSelectedCounterList(id);
		List<CounterVisitData> result = new ArrayList<CounterVisitData>();
		if(counterList!=null) {
			result = counterVisitDataConverter.convertAll(counterList);
		}
		return result;
	}

	@Override
	public CounterVisitAnalyticsWsDTO getCompletedVisitStatisticsData(){
		EyDmsUserModel eydmsUserModel = djpVisitService.getCurrentSalesOfficer();

		if(null!= eydmsUserModel){
			return djpVisitService.getCompletedVisitStatisticsDataForSO(eydmsUserModel);
		}
		else{
			throw new ModelNotFoundException("Could not find current logged in sales officer");
		}

	}
	public SiteSummaryData getSiteSummary(String counterVisitId) {
		return djpVisitService.getSiteSummary(counterVisitId);
	}

	@Override
	public String getLastVisitDate(String counterVisitId) {
		return djpVisitService.getLastVisitDate(counterVisitId);
	}
	
	@Override
	public Integer getVisitCountMTD(String counterVisitId) {
		return djpVisitService.getVisitCountMTD(counterVisitId);
	}

	@Override
	public DealerSummaryData getDealerSummary(String counterVisitId) {

		EyDmsCustomerModel eydmsCustomer = djpVisitService.getProductMixForDealerSummary(counterVisitId);

		DealerSummaryData dealerSummary = djpVisitService.getDealerSummary(counterVisitId);

		if(Objects.nonNull(dealerSummary)) {
			ProductMixVolumeAndRatioListData productData = new ProductMixVolumeAndRatioListData();
			dealerSummary.setProductMixMTD(salesPerformanceFacade.getProductMixVolumeAndRatioListDataForMTD((EyDmsUserModel) userService.getCurrentUser(), baseSiteService.getCurrentBaseSite(), productData, eydmsCustomer,null,null));
			dealerSummary.setProductMixYTD(salesPerformanceFacade.getProductMixVolumeAndRatioListDataForYTD((EyDmsUserModel) userService.getCurrentUser(), baseSiteService.getCurrentBaseSite(), productData, eydmsCustomer,null,null));
		}
		/*ProductMixVolumeAndRatioListData productData = salesPerformanceFacade.getProductwiseSalesPercentRatioAndVolumeRatioForCustomer(eydmsCustomerService.getEyDmsCustomerForUid(eydmsCustomer.getUid()), "DEALER");
		dealerSummary.setProductData(productData);*/
		return dealerSummary;
	}

	@Override
	public RetailerSummaryData getRetailerSummary(String code) {
		RetailerSummaryData data = djpVisitService.getRetailerSummary(code);
		
		ProductMixVolumeAndRatioListData productData = salesPerformanceFacade.getProductwiseSalesPercentRatioAndVolumeRatioForCustomer(eydmsCustomerService.getEyDmsCustomerForUid(code), "RETAILER",null,null);
		data.setProductData(productData);

		if(Objects.nonNull(data)){
			ProductMixVolumeAndRatioListData productDataMixMTD = new ProductMixVolumeAndRatioListData();
			ProductMixVolumeAndRatioListData productDataMixYTD = new ProductMixVolumeAndRatioListData();
			data.setProductMixMTD(salesPerformanceFacade.getProductMixVolumeAndRatioListDataMTDForCustomer((EyDmsUserModel) userService.getCurrentUser(), baseSiteService.getCurrentBaseSite(), productDataMixMTD, null ,null, eydmsCustomerService.getEyDmsCustomerForUid(code)));
			data.setProductMixYTD(salesPerformanceFacade.getProductMixVolumeAndRatioListDataForYTD((EyDmsUserModel) userService.getCurrentUser(), baseSiteService.getCurrentBaseSite(), productDataMixYTD, eydmsCustomerService.getEyDmsCustomerForUid(code),null,null));
		}
		
		return data;
	}
	
	@Override
	public InfluencerSummaryData getInfluencerSummary(String counterVisitId) {
		return djpVisitService.getInfluencerSummary(counterVisitId);
	}

	@Override
	public CounterVisitMasterData getCounterVisitFormDetails(String counterVisitId) {
		CounterVisitMasterModel visitMaster = djpVisitService.getCounterVisitMasterForLastVisitDate(counterVisitId);
		
		CounterVisitMasterData visitMasterData = new CounterVisitMasterData();
		try {
			counterVisitMasterDataPopulator.populate(visitMaster, visitMasterData);
		}catch(NullPointerException e)
		{
			LOG.info("CounterVisitMasterModel not found");
		}
		return visitMasterData;
	}

	@Override
	public DealerFleetListData getDealerFleetDetails(String counterVisitId) {
		List<DealersFleetDetailsModel> list = djpVisitService.getDealerFleetDetails(counterVisitId);
		List<DealerFleetData> dataList = dealerFleetDetailsConverter.convertAll(list);
		DealerFleetListData result = new DealerFleetListData();
		int count = 0;
		double capacity = 0.0;
		if(dataList!=null)
		{
		 count = dataList.stream().mapToInt(fleet->fleet.getCount()).sum();
		 capacity = dataList.stream().mapToDouble(fleet->(fleet.getCapacity()*fleet.getCount())).sum();
		}
		result.setTotalFleetCount(count);
		result.setTotalFleetCapacity(capacity);
		result.setDealerFleetList(dataList);
		
		return result;
	}

	@Override
	public String getDJPPlanComplianceForSO(){

		EyDmsUserModel eydmsUserModel = djpVisitService.getCurrentSalesOfficer();
		if(null!= eydmsUserModel){
			LocalDate now = LocalDate.now();
			LocalDate planEndDate = now;
			LocalDate planStartDate = now.with(TemporalAdjusters.firstDayOfMonth());

			Date startDate = Date.from(planStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
			Date endDate = Date.from(planEndDate.atTime(23,59,59).atZone(ZoneId.systemDefault()).toInstant());

			return djpVisitService.calculatePlanComplianceForSODJP(eydmsUserModel,startDate,endDate);

		}
		else{
			throw new ModelNotFoundException("Could not find current logged in sales officer");
		}
	}
	
	@Override
	public EyDmsSiteListData getInfluencerDetails(String counterVisitId) {
		List<B2BCustomerModel> list = djpVisitService.getInfluencerDetails(counterVisitId);
		List<EyDmsSiteData> dataList = eydmsSiteConverter.convertAll(list);
		EyDmsSiteListData result = new EyDmsSiteListData();
		result.setSites(dataList);
		return result;
	}
	
	@Override
	public SiteVisitFormData getLastSiteVisitFormData(String counterVisitId) {
		CounterVisitMasterModel counterVisit = djpVisitService.getCounterVisitMasterForLastVisitDate(counterVisitId);
		SiteVisitFormData result = new SiteVisitFormData();
		if(counterVisit!=null) {
			result = siteVisitDataConverter.convert(counterVisit);
		}
		return result;
	}

	@Override
	public List<CounterVisitData> getTodaysPlan() {
		Collection<CounterVisitMasterModel> counterList = djpVisitService.getTodaysPlan();
		List<CounterVisitData> result = new ArrayList<CounterVisitData>();
		if(counterList!=null) {
			result = counterVisitDataConverter.convertAll(counterList);
		}
		return result;
	}

	@Override
	public String getRouteForId(String id) {
		return djpVisitService.getRouteForId(id);
	}

	@Override
	public DropdownListData getListOfRoutes(List<String> subAreas) {
		return djpVisitService.getListOfRoutes(subAreas);
	}
	
	@Override
    public Double getTotalOrderGenerated(String siteCode,String counterVisitId) {
        return djpVisitService.getTotalOrderGenerated(siteCode, counterVisitId);
    }

	@Override
	public SalesHistoryData getSalesHistoryForDealer(String counterVisitId) {
		return djpVisitService.getSalesHistoryForDealer(counterVisitId);
	}

	@Override
	public MarketVisitDetailsData fetchMarketVisitDetailsData(final String visitId){
		return djpVisitService.getMarketVisitDetailsData(visitId);
	}

	@Override
	public List<MonthlySalesData> getLastSixMonthSalesForDealer(String counterVisitId) {
		return djpVisitService.getLastSixMonthSalesForDealer(counterVisitId);
	}

	@Override
	public MarketVisitDetailsData getVisitJourneyDetails(String visitId) {
		return djpVisitService.getJounreyDetailsData(visitId);
	}

	@Override
	public SalesHistoryData getSalesHistoryDataFor360(String counterVisitId) {
		return djpVisitService.getSalesHistoryDataFor360(counterVisitId);
	}

	@Override
	public List<MonthlySalesData> getLastSixMonthSalesForRetailer(String counterVisitId) {
		return djpVisitService.getLastSixMonthSalesForRetailer(counterVisitId);
	}
	
	
	@Override
	public CRMVisitListData getAllVisit(String startDate, String endDate) {
		CRMVisitListData resultOutput = new CRMVisitListData();
		List<VisitMasterModel> list = djpVisitService.getAllVisit(startDate, endDate);
		List<CRMVisitData> result = new ArrayList<CRMVisitData>();
		for(VisitMasterModel visit: list) {
			CRMVisitData data = new CRMVisitData();
			data.setId(visit.getPk().toString());
			if(visit.getRoute()!=null)
				data.setRoute(visit.getRoute().getRouteId());
			if(visit.getObjective()!=null) {
				data.setObjectiveId(visit.getObjective().getObjectiveId());
				data.setObjectiveName(visit.getObjective().getObjectiveName());
			}
			
			DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");  
			if(visit.getStartVisitTime()!=null)
				data.setStartVisitTime(dateFormat.format(visit.getStartVisitTime()));
			
			if(visit.getEndVisitTime()!=null)
				data.setEndVisitTime(dateFormat.format(visit.getEndVisitTime()));

			DateFormat dateFormat1 = new SimpleDateFormat("dd-MM-yyyy"); 
			if(visit.getVisitPlannedDate()!=null) {
				data.setVisitPlannedDate(dateFormat1.format(visit.getVisitPlannedDate()));
			}
			if(visit.getStatus()!=null) {
				data.setStatus(visit.getStatus().getCode());
			}
			//New Territory Change
			if(visit.getSubAreaMaster()!=null) {
				data.setSubArea(visit.getSubAreaMaster().getTaluka());
				data.setDistrict(visit.getSubAreaMaster().getDistrict());				
			}
			if(visit.getUser()!=null) {
				data.setSoEmail(visit.getUser().getUid());
				data.setSoEmployeeCode(visit.getUser().getEmployeeCode());
			}
			data.setRouteDeviationComment(visit.getRouteDeviationComment());
			data.setRouteDeviationReason(visit.getRouteDeviationReason());
			data.setObjectiveDeviationComment(visit.getObjectiveDeviationComment());
			data.setObjectiveDeviationReason(visit.getObjectiveDeviationReason());
			
			List<CRMCounterVisitData> counters = new ArrayList<CRMCounterVisitData>();
			for(CounterVisitMasterModel counterVisit : visit.getCounterVisits()) {
				CRMCounterVisitData counter = new CRMCounterVisitData();
				counter.setId(counterVisit.getPk().toString());
				if(counterVisit.getEyDmsCustomer()!=null) {
					counter.setCustomerNo(counterVisit.getEyDmsCustomer().getCustomerNo());
					counter.setCrmCustomerCode(counterVisit.getEyDmsCustomer().getUid());
				}
				if(counterVisit.getCounterType()!=null) {
					counter.setCounterType(counterVisit.getCounterType().getCode());
				}
				counter.setIsAdoc(counterVisit.getIsAdHoc());
				counter.setDeviationComment(counterVisit.getDeviationComment());
				counter.setDeviationReason(counterVisit.getDeviationReason());
				counter.setSystemRecommended(counterVisit.getCounterScore()!=null?true:false);
				
				if(counterVisit.getStartVisitTime()!=null)
					counter.setStartVisitTime(dateFormat.format(counterVisit.getStartVisitTime()));
				
				if(counterVisit.getEndVisitTime()!=null)
					counter.setEndVisitTime(dateFormat.format(counterVisit.getEndVisitTime()));
				
				counters.add(counter);
				
			}
			data.setCounterVisitList(counters);
			result.add(data);
		}
		resultOutput.setVisitList(result);
		return resultOutput;
	}

	@Override
	public Boolean saveCustomerCoordinates(String customerId, Double latitude, Double longitude) {
		return djpVisitService.saveCustomerCoordinates(customerId, latitude, longitude);
	}

	public BaseSiteService getBaseSiteService() {
		return baseSiteService;
	}

	public void setBaseSiteService(BaseSiteService baseSiteService) {
		this.baseSiteService = baseSiteService;
	}
	
	@Override
	public List<ObjectiveData> getAllObjective() {
		List<ObjectiveData> dataList = new ArrayList<ObjectiveData>();
		List<ObjectiveModel> reccomendedObjs = new ArrayList<ObjectiveModel>();
		Collection<ObjectiveModel> modelList = djpVisitService.getAllObjective();
		if(modelList!=null) {
			modelList.forEach(model -> {
				ObjectiveData data = new ObjectiveData();
				data.setId(model.getObjectiveId());
				data.setName(model.getObjectiveName());
				dataList.add(data);
			});
		}
		return dataList;
	}

	@Override
	public Integer getPendingApprovalVisitsCountForTsmorRh() {
		return djpVisitService.getPendingApprovalVisitsCountForTsmorRh();
	}

	@Override
	public Double getDJPCompliance(){
		return djpVisitService.calculateDJPCompliance();
	}

	@Override
	public DropdownListData getPartnerType() {
		return djpVisitService.getPartnerType();
	}
	
	@Override
	public Map<String, Object> counterVisitedForRoutes(String route) {
		return djpVisitService.counterVisitedForRoutes(route);
	}	
}
