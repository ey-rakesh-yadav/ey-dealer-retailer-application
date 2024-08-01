package com.scl.facades.impl;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.customer.services.SclCustomerService;
import com.scl.core.dao.DJPVisitDao;
import com.scl.core.dao.DataConstraintDao;
import com.scl.core.dao.DjpRouteScoreDao;
import com.scl.core.dao.SalesPerformanceDao;
import com.scl.core.enums.ApprovalStatus;
import com.scl.core.enums.CounterType;
import com.scl.core.lead.services.SclLeadService;
import com.scl.core.model.*;
import com.scl.core.services.DJPVisitService;
import com.scl.core.services.TerritoryMasterService;
import com.scl.facades.DJPVisitFacade;
import com.scl.facades.SalesPerformanceFacade;
import com.scl.facades.data.*;
import com.scl.facades.data.marketvisit.MarketVisitDetailsData;
import com.scl.facades.djp.data.*;
import com.scl.facades.djp.data.marketvisit.VisitSummaryData;
import com.scl.facades.marketvisit.scheme.SchemeDetailsData;
import com.scl.facades.visit.data.DealerSummaryData;
import com.scl.facades.visit.data.InfluencerSummaryData;
import com.scl.facades.visit.data.RetailerSummaryData;
import com.scl.facades.visit.data.SiteSummaryData;
import com.scl.occ.dto.djp.CounterVisitAnalyticsWsDTO;
import com.scl.occ.dto.djp.DJPFinalizedPlanWsDTO;
import com.scl.occ.dto.djp.UpdateCountersWsDTO;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.c2l.BrandModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.webservicescommons.dto.error.ErrorListWsDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
	SalesPerformanceDao salesPerformanceDao;

	@Autowired
	SclLeadService sclLeadService;
	
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
	Converter<B2BCustomerModel,SclSiteData> sclSiteConverter;

	@Autowired
	Converter<CompetitorProductModel,CompetitorProductData> competitorProductConverter;

	@Autowired
	TerritoryMasterService territoryMasterService;

	@Autowired
	DataConstraintDao dataConstraintDao;
	
	@Resource
	private UserService userService;

	@Resource
	private Populator<SchemeDetailsData,SchemeDetailsModel> schemeDetailsReversePopulator;

	@Resource
	private ModelService modelService;
	
	@Resource
	SalesPerformanceFacade salesPerformanceFacade;
	
	@Resource
	SclCustomerService sclCustomerService;

	@Resource
	BaseSiteService baseSiteService;
	
    @Autowired
    DjpRouteScoreDao djpRouteScoreDao;
    
    @Autowired
    DJPVisitDao djpVisitDao;

	@Autowired
	I18NService i18NService;

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
		if(CollectionUtils.isNotEmpty(dataList)) {
			listData.setCompetitorProductList(dataList.stream().filter(competitorProductData -> Objects.nonNull(competitorProductData.getCode())).sorted(Comparator.comparing(CompetitorProductData::getCode)).distinct().collect(Collectors.toList()));
			return  listData;
		}else {
			return listData;
		}
	}
	
	@Override
	public List<TruckModelData> getAllTrucks() {
		List<TruckModelMasterModel> modelList =  djpVisitService.findAllTrucks();
		List<TruckModelData> dataList = new ArrayList<TruckModelData>();
		List<TruckModelData> finalDataList = dataList;
		modelList.stream().forEach(truck->
		{
			TruckModelData data = new TruckModelData();
			data.setTruckModel(truck.getTruckModel());
			data.setCapacity(truck.getCapacity());
			data.setVehicleMake(truck.getVehicleMake());
			data.setVehicleType(String.valueOf(truck.getVehicleType()));
			//data.setCount(truck.getCount());
			finalDataList.add(data);
		});
		if(CollectionUtils.isNotEmpty(dataList)){
			dataList=finalDataList.stream().filter(truckModelData -> Objects.nonNull(truckModelData.getVehicleMake())).sorted(Comparator.comparing(TruckModelData::getVehicleMake)).collect(Collectors.toList());
			return  dataList;
		}else {
			return finalDataList;
		}
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
		LOG.info(String.format("visit details list::%s",list));
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
			List<DJPCounterScoreMasterModel> filteredModelList=filterScoreMasterByDOTerritoryCode(modelList);
			filteredModelList.forEach(model -> {
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
						DateFormat dateFormat = new SimpleDateFormat(SclCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);  
						String strDate = dateFormat.format(model.getCustomer().getLastVisitTime());
						data.setLastVisitDate(strDate);
					}
					data.setObjectiveId(objectiveId);
					data.setRouteScoreId(routeScoreId);
                	data.setCustomerNo(model.getCustomer().getCustomerNo());

					if(djpVisitService.isNonSclCounter(model.getCustomer())){
						data.setIsNonSclCounter(Boolean.TRUE);
						if(CollectionUtils.isNotEmpty(model.getCustomer().getAddresses())){
							AddressModel addressModel=model.getCustomer().getAddresses().iterator().next();
							data.setLine1(addressModel.getLine1());
							data.setLine2(addressModel.getLine2());
						}
					}else{
						data.setIsNonSclCounter(Boolean.FALSE);
						AddressModel addressModel=djpVisitService.getCustomerOwnAddress(model.getCustomer());
						if(Objects.nonNull(addressModel)) {
							data.setLine1(addressModel.getLine1());
							data.setLine2(addressModel.getLine2());
						}
					}
					counterScoreList.add(data);
				}
			});
		}
		return counterScoreList;
	}

	@Override
	public List<CounterDetailsData> getExistingCounters(final DJPFinalizedPlanWsDTO plannedData){
		List<SclCustomerModel> filteredCounters = djpVisitService.getFilteredCounters(plannedData);
		return  populateCounterData(filteredCounters);
		//return populateCounterData(djpVisitService.filterCustomerByDOTerritoryCode(filteredCounters));
	}



	/**
	 *
	 * @param scoreMasterModels
	 * @return
	 */
	public List<DJPCounterScoreMasterModel> filterScoreMasterByDOTerritoryCode(List<DJPCounterScoreMasterModel> scoreMasterModels)
	{
		Collection<TerritoryMasterModel> territoryMasterModels=territoryMasterService.getCurrentTerritory();
		LOG.info(String.format("territoryMasterModels:: %s",territoryMasterModels));


			List<DJPCounterScoreMasterModel> filterdList=new ArrayList<>();
			if (CollectionUtils.isNotEmpty(territoryMasterModels)) {
				List<TerritoryMasterModel> territoryMasterModelList=territoryMasterModels.stream().distinct().collect(Collectors.toList());
				scoreMasterModels.forEach(masterModel -> {
					if(masterModel.getCustomer()!=null) {
						if(Objects.nonNull(masterModel.getCustomer().getDefaultB2BUnit()) && masterModel.getCustomer().getDefaultB2BUnit().getUid().equalsIgnoreCase(SclCoreConstants.B2B_UNIT.SCL_SHREE_UNIT_UID))
						{
							//dealer
							if (masterModel.getCustomer().getCounterType().equals(CounterType.DEALER) && Objects.nonNull(masterModel.getCustomer().getTerritoryCode())) {

								LOG.info(String.format("Inside dealer check ::%s and territoryCode::%s",masterModel.getCustomer(),masterModel.getCustomer().getTerritoryCode()));
									if (territoryMasterModelList.contains(masterModel.getCustomer().getTerritoryCode())) {
										LOG.info(String.format("territoryModels dealer check ::%s ",territoryMasterModelList.contains(masterModel.getCustomer().getTerritoryCode())));
										filterdList.add(masterModel);
									}

								//retailer
							} else if (masterModel.getCustomer().getCounterType().equals(CounterType.RETAILER)) {
								Integer retailerCount = djpVisitService.getRetailerCountByTerritory(masterModel.getCustomer(), territoryMasterModels);
								LOG.info(String.format("Inside retailer check ::%s and territoryCode::%s and retailerCount::%s",masterModel.getCustomer(),territoryMasterModels,retailerCount));
								if (retailerCount > 0) {
									filterdList.add(masterModel);
								}
							}
						}else {
							//non-scl user
							LOG.info(String.format("non scl user ::%s ",masterModel.getCustomer()));
							filterdList.add(masterModel);
						}
					}
				});
                 return filterdList.stream().distinct().collect(Collectors.toList());
			}
			return filterdList;

	}
	private List<CounterDetailsData> populateCounterData(Collection<SclCustomerModel> filteredCounters) {
		List<CounterDetailsData> counterDetailsDataList = new ArrayList<>();
		if(CollectionUtils.isNotEmpty(filteredCounters)){
			for(SclCustomerModel sclCustomerModel : filteredCounters){
				if(sclCustomerModel!=null) {
					CounterDetailsData  counterDetailsData = new CounterDetailsData();

					counterDetailsData.setCustomerCode(sclCustomerModel.getUid());
					counterDetailsData.setCustomerName(sclCustomerModel.getName());
					counterDetailsData.setCounterPotential(sclCustomerModel.getCounterPotential());

					if(null!= sclCustomerModel.getLastVisitTime()){
						DateFormat dateFormat = new SimpleDateFormat(SclCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
						counterDetailsData.setLastVisitDate(dateFormat.format(sclCustomerModel.getLastVisitTime()));
					}
					counterDetailsData.setCustomerType(djpVisitService.getCustomerType(sclCustomerModel));

					counterDetailsData.setCustomerNo(sclCustomerModel.getCustomerNo()!=null ? sclCustomerModel.getCustomerNo() : "");

					if(djpVisitService.isNonSclCounter(sclCustomerModel)){
						counterDetailsData.setIsNonSclCounter(Boolean.TRUE);
						if(CollectionUtils.isNotEmpty(sclCustomerModel.getAddresses())){
							AddressModel addressModel=sclCustomerModel.getAddresses().iterator().next();
							counterDetailsData.setLine1(addressModel.getLine1());
							counterDetailsData.setLine2(addressModel.getLine2());
						}
					}else{
						counterDetailsData.setIsNonSclCounter(Boolean.FALSE);
						AddressModel addressModel=djpVisitService.getCustomerOwnAddress(sclCustomerModel);
						if(Objects.nonNull(addressModel)) {
							counterDetailsData.setLine1(addressModel.getLine1());
							counterDetailsData.setLine2(addressModel.getLine2());
						}
					}

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
		return  dataList;
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
		SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
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
		SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
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
		//Collection<SclCustomerModel> filteredCountersByTerritory = new ArrayList<>();
		Collection<SclCustomerModel> filteredCounters=new ArrayList<>();
		try {

			 filteredCounters = djpVisitService.getAdHocExistingCounters(plannedData);
			filteredCounters=filteredCounters.stream().sorted(Comparator.comparing(SclCustomerModel::getCreationtime).reversed()).collect(Collectors.toList());
			LOG.info("FilteredCounters:" + filteredCounters);

			/*if (CollectionUtils.isNotEmpty(filteredCounters)) {
				filteredCountersByTerritory = djpVisitService.filterCustomerByDOTerritoryCode(filteredCounters.stream().collect(Collectors.toList()));
			}*/
		}
		catch (Exception ex) {
			LOG.error(String.format("getAdHocExistingCounters giving exception: %s, cause: %s", ex.getMessage(), ex.getCause()));
		}
		return populateCounterData(filteredCounters);
	}
	
	@Override
	public void createAndSaveSiteDetails(final AddNewSiteData siteData){
		djpVisitService.createAndSaveSite(siteData);
	}

	@Override
	public List<RouteData> getRoutesForSalesofficer(){
		final SclUserModel sclUserModel = djpVisitService.getCurrentSalesOfficer();
		if(null!= sclUserModel){
			List<RouteData> routeData = new ArrayList<>();
			Set<RouteMasterModel> routeMasterModels = djpVisitService.findAllRoutesForSO(sclUserModel);
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
		Collection<SclCustomerModel> filteredCounters = djpVisitService.getcounterNotVisitedList(month, year);
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
		SclUserModel sclUserModel = djpVisitService.getCurrentSalesOfficer();

		if(null!= sclUserModel){
			return djpVisitService.getCompletedVisitStatisticsDataForSO(sclUserModel);
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

		SclCustomerModel sclCustomer = djpVisitService.getProductMixForDealerSummary(counterVisitId);

		DealerSummaryData dealerSummary = djpVisitService.getDealerSummary(counterVisitId);

		if(Objects.nonNull(dealerSummary)) {
			ProductMixVolumeAndRatioListData productData = new ProductMixVolumeAndRatioListData();
			//dealerSummary.setProductMixMTD(salesPerformanceFacade.getProductMixVolumeAndRatioListDataForMTD((SclUserModel) userService.getCurrentUser(), baseSiteService.getCurrentBaseSite(), productData, sclCustomer,null));
			//dealerSummary.setProductMixYTD(salesPerformanceFacade.getProductMixVolumeAndRatioListDataForYTD((SclUserModel) userService.getCurrentUser(), baseSiteService.getCurrentBaseSite(), productData, sclCustomer,null,null,null));

			dealerSummary.setProductMixMTD(salesPerformanceFacade.getProductwiseSalesPercentRatioAndVolumeRatioForCustomer(sclCustomer,null, null,null,null));
			dealerSummary.setProductMixYTD(salesPerformanceFacade.getProductMixVolumeAndRatioListDataForYTD(null, baseSiteService.getCurrentBaseSite(), productData, sclCustomer,  null,null,null));

		}
		/*ProductMixVolumeAndRatioListData productData = salesPerformanceFacade.getProductwiseSalesPercentRatioAndVolumeRatioForCustomer(sclCustomerService.getSclCustomerForUid(sclCustomer.getUid()), "DEALER");
		dealerSummary.setProductData(productData);*/
		return dealerSummary;
	}

	@Override
	public RetailerSummaryData getRetailerSummary(String code,List<String> subAreaList,List<String> districtList) {
		RetailerSummaryData data = djpVisitService.getRetailerSummary(code,subAreaList,districtList);
		
		ProductMixVolumeAndRatioListData productData = salesPerformanceFacade.getProductwiseSalesPercentRatioAndVolumeRatioForCustomer(sclCustomerService.getSclCustomerForUid(code), "RETAILER",null,subAreaList,districtList);
		data.setProductData(productData);

		if(Objects.nonNull(data)){
			ProductMixVolumeAndRatioListData productDataMixMTD = new ProductMixVolumeAndRatioListData();
			ProductMixVolumeAndRatioListData productDataMixYTD = new ProductMixVolumeAndRatioListData();
			//changes done
			data.setProductMixMTD(salesPerformanceFacade.getProductMixVolumeAndRatioListDataMTDForCustomer(null, baseSiteService.getCurrentBaseSite(), productDataMixMTD, districtList ,subAreaList, sclCustomerService.getSclCustomerForUid(code)));
			data.setProductMixYTD(salesPerformanceFacade.getProductMixVolumeAndRatioListDataForYTD(null, baseSiteService.getCurrentBaseSite(), productDataMixYTD, sclCustomerService.getSclCustomerForUid(code),null,subAreaList,districtList));
		}
		
		return data;
	}
	
	@Override
	public InfluencerSummaryData getInfluencerSummary(String counterVisitId,List<String> subAreaList,List<String> districtList) {
		return djpVisitService.getInfluencerSummary(counterVisitId,subAreaList,districtList);
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

		SclUserModel sclUserModel = djpVisitService.getCurrentSalesOfficer();
		if(null!= sclUserModel){
			LocalDate now = LocalDate.now();
			LocalDate planEndDate = now;
			LocalDate planStartDate = now.with(TemporalAdjusters.firstDayOfMonth());

			Date startDate = Date.from(planStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
			Date endDate = Date.from(planEndDate.atTime(23,59,59).atZone(ZoneId.systemDefault()).toInstant());

			return djpVisitService.calculatePlanComplianceForSODJP(sclUserModel,startDate,endDate);

		}
		else{
			throw new ModelNotFoundException("Could not find current logged in sales officer");
		}
	}
	
	@Override
	public SclSiteListData getInfluencerDetails(String counterVisitId) {
		List<B2BCustomerModel> list = djpVisitService.getInfluencerDetails(counterVisitId);
		List<SclSiteData> dataList = sclSiteConverter.convertAll(list);
		SclSiteListData result = new SclSiteListData();
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
	public SalesHistoryData getSalesHistoryDataFor360(String counterVisitId,List<String> subAreaList,List<String> districtList) {
		return djpVisitService.getSalesHistoryDataFor360(counterVisitId,subAreaList,districtList);
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
				if(counterVisit.getSclCustomer()!=null) {
					counter.setCustomerNo(counterVisit.getSclCustomer().getCustomerNo());
					counter.setCrmCustomerCode(counterVisit.getSclCustomer().getUid());
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

	/**
	 * Get Brands & Competitor Products
	 * @param brandIds
	 * @param uid
	 * @return
	 */
	@Override
	public BrandProductListData getBrandsCompetitorProducts(List<String> brandIds,String uid){
		BrandProductListData brandProductListData = new BrandProductListData();
		List<BrandData> allbrandData = new ArrayList<>();
		List<BrandModel> selectedBrand = new ArrayList<>();
		List<BrandModel> allbrandModels = new ArrayList<>();
		Map<List<BrandModel>,List<CompetitorProductModel>> allbrandProductsMap = djpVisitService.getBrandsCompetitorProducts(brandIds);
		Map<List<BrandModel>, List<CompetitorProductModel>> selectedbrandProductsMap = new HashMap();
		List<MarketMappingDetailsModel> selectedMarketMappingDetails = new ArrayList<>();
		if(StringUtils.isNotEmpty(uid)) {
			selectedbrandProductsMap = djpVisitService.getBrandsCompetitorProductsByUid(uid);
			selectedMarketMappingDetails = djpVisitService.getMarketMappingDetails(uid);
		}
		for(Map.Entry<List<BrandModel>, List<CompetitorProductModel>> brandProdMap : allbrandProductsMap.entrySet())
		{
			allbrandModels = brandProdMap.getKey();
			List<CompetitorProductModel> listModel = brandProdMap.getValue();
			List<CompetitorProductData> competitorProductDataList = listModel.stream().map(a->competitorProductConverter.convert(a)).collect(Collectors.toList());
			List<CompetitorProductData> competitorProductDataLists = competitorProductDataList.stream().sorted(Comparator.comparing(CompetitorProductData::getCode)).collect(Collectors.toList());
			for(Map.Entry<List<BrandModel>, List<CompetitorProductModel>> selectedbrandProductMap :selectedbrandProductsMap.entrySet()) {
				selectedBrand= selectedbrandProductMap.getKey();
				List<CompetitorProductModel> selectedCompetitorProductList = selectedbrandProductMap.getValue();
				List<String> collect = selectedCompetitorProductList.stream().map(a -> a.getCode()).collect(Collectors.toList());
				Optional<CompetitorProductData> competitorProductData = competitorProductDataLists.stream().filter(s -> collect.contains(s.getCode())).findFirst();
				if (competitorProductData.isPresent()) {
					competitorProductData.get().setSelectedProductIndicator(true);
				}
				if(CollectionUtils.isNotEmpty(selectedMarketMappingDetails)) {
				for (MarketMappingDetailsModel marketMappingDetailsModel : selectedMarketMappingDetails) {
					if (competitorProductData.isPresent() && marketMappingDetailsModel.getProduct().getCode().equals(competitorProductData.get().getCode())) {
						if(null != marketMappingDetailsModel.getWholeSales()) {
							competitorProductData.get().setWholeSale(marketMappingDetailsModel.getWholeSales().intValue());
						}
						if(null != marketMappingDetailsModel.getRetailSales()) {
							competitorProductData.get().setRetailSale(marketMappingDetailsModel.getRetailSales().intValue());
						}
					}
				}
			   }
			}
			brandProductListData.setProducts(competitorProductDataLists);
			allbrandData = getBrandDataList(allbrandModels);
			List<String> collect = selectedBrand.stream().map(a -> a.getIsocode()).collect(Collectors.toList());
			for(BrandData brand : allbrandData) {
				if (collect.contains(brand.getIsocode())) {
					brand.setSelectedBrandIndicator(true);
				}
			}
			brandProductListData.setBrands(allbrandData);
		}
		return brandProductListData;
	}

	private List<BrandData> getBrandDataList(List<BrandModel> modelList) {
		List<BrandData> dataList = new ArrayList<>();

		for (BrandModel brand : modelList) {
			BrandData data = new BrandData();
			data.setIsocode(brand.getIsocode());
			data.setName(brand.getName(i18NService.getCurrentLocale()));
			dataList.add(data);
		}
		dataList = dataList.stream().sorted(Comparator.comparing(BrandData::getIsocode)).collect(Collectors.toList());
		return dataList;
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
		List<ObjectiveData> finalDataList = dataList;
		Collection<ObjectiveModel> modelList = djpVisitService.getAllObjective();
		if(modelList!=null) {
			modelList.forEach(model -> {
				ObjectiveData data = new ObjectiveData();
				data.setId(model.getObjectiveId());
				data.setName(model.getObjectiveName());
				finalDataList.add(data);
			});
		}
		if(CollectionUtils.isNotEmpty(dataList)){
			dataList=finalDataList.stream().filter(obj->Objects.nonNull(obj.getName())).sorted(Comparator.comparing(ObjectiveData::getName)).collect(Collectors.toList());
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
