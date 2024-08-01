package com.scl.core.services.impl;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.TerritoryManagementDao;
import com.scl.core.enums.CounterType;
import com.scl.core.enums.SclUserType;
import com.scl.core.model.*;
import com.scl.core.region.dao.DistrictMasterDao;
import com.scl.core.region.dao.RegionMasterDao;
import com.scl.core.services.SalesPerformanceService;
import com.scl.core.services.TerritoryManagementService;
import com.scl.facades.data.FilterDistrictData;
import com.scl.facades.data.FilterRegionData;
import com.scl.facades.data.FilterTalukaData;
import com.scl.facades.data.RequestCustomerData;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.jalo.JaloObjectNoLongerValidException;
import de.hybris.platform.search.restriction.SearchRestrictionService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

public class TerritoryManagementServiceImpl implements TerritoryManagementService {

	private static final Logger LOG = Logger.getLogger(TerritoryManagementServiceImpl.class);
	private static final String SITE = "SITE";

	@Autowired
	TerritoryManagementDao territoryManagementDao;

	@Autowired
	UserService userService;

	@Autowired
	BaseSiteService baseSiteService;

	@Autowired
	SessionService sessionService;

	@Autowired
	DistrictMasterDao districtMasterDao;

	@Autowired
	RegionMasterDao regionMasterDao;

	@Autowired
	private SearchRestrictionService searchRestrictionService;

	@Autowired
	private SalesPerformanceService salesPerformanceService;

	//To be Checked
	@Override
	public List<String> getAllSubAreaForSO(String userId) {
//		BaseSiteModel site = baseSiteService.getCurrentBaseSite();		
//		return territoryManagementDao.getAllSubAreaForSO(userService.getUserForUID(userId),site);
		List<SubAreaMasterModel> subareas = getTerritoriesForSO(userId);
		if(subareas!=null) {
			return subareas.stream().map(subArea -> subArea.getTaluka()).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	//To be Checked
	@Override
	public List<String> getAllSubAreaForCustomer(String customerId) {
//		BaseSiteModel site = baseSiteService.getCurrentBaseSite();
//		return territoryManagementDao.getAllSubAreaForCustomer(userService.getUserForUID(customerId),site);
		List<SubAreaMasterModel> subareas = getTerritoriesForCustomer(customerId);
		if(subareas!=null) {
			return subareas.stream().map(subArea -> subArea.getTaluka()).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	//New Territory Change
	@Override
	public List<SclCustomerModel> getDealersForSubArea() {
//		List<SubAreaMasterModel> list = new ArrayList<SubAreaMasterModel>();
//		UserModel user = userService.getCurrentUser();
//		if(user instanceof SclUserModel) {
//			list = getTerritoriesForSO();
//		}
//		else if(user instanceof SclCustomerModel){
//			list = getTerritoriesForCustomer((SclCustomerModel)user);
//		}
//		List<SclCustomerModel> dealerList = getAllCustomerForSubArea(list).stream().filter(d -> (d.getGroups()).contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList());
//		
		RequestCustomerData requestCustomerData = new RequestCustomerData();
		requestCustomerData.setCounterType(List.of(CounterType.DEALER.getCode()));
		List<SclCustomerModel> dealerList = getCustomerforUser(requestCustomerData);
		return dealerList!=null && !dealerList.isEmpty() ? dealerList :Collections.emptyList() ;

	}

	//New Territory Change
	@Override
	public List<SclCustomerModel> getSCLAndNonSCLDealersRetailersForSubArea() {
		UserGroupModel dealerGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID);
		UserGroupModel retailerGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID);
		List<SclCustomerModel> filteredDealerRetailerList = new ArrayList<>();
		List<SclCustomerModel> dealerRetailerList = territoryManagementDao.getSCLAndNonSCLCustomerForSubArea(getTerritoriesForSO());
		if(CollectionUtils.isNotEmpty(dealerRetailerList)) {
			filteredDealerRetailerList = dealerRetailerList.stream().filter(d -> (Objects.nonNull(d) && CollectionUtils.isNotEmpty(d.getGroups())) && ((d.getGroups()).contains(dealerGroup) || (d.getGroups()).contains(retailerGroup)))
					.peek(d->System.out.println(String.format("scl and non-scl customer uid::%s and name::%s",d.getUid(),d.getName())))
					.collect(Collectors.toList());
		}
		return filteredDealerRetailerList;
	}
	@Override
	public List<SclCustomerModel> getSCLAndNonSCLDealersRetailersForSubArea(SubAreaMasterModel subAreaMasterModel,DistrictMasterModel districtMasterModel) {
		UserGroupModel dealerGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID);
		UserGroupModel retailerGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID);

		FilterTalukaData filterTalukaData=new FilterTalukaData();
		if(Objects.nonNull(subAreaMasterModel) && subAreaMasterModel.getTaluka()!=null)
			filterTalukaData.setTalukaName(subAreaMasterModel.getTaluka());
		if(Objects.nonNull(districtMasterModel) && districtMasterModel.getCode()!=null)
			filterTalukaData.setDistrictCode(districtMasterModel.getCode());
		List<SubAreaMasterModel> talukaForUser = territoryManagementDao.getTalukaForUser(filterTalukaData);

		LOG.info(String.format("getTalukaForUser ::%s",talukaForUser));
		List<SclCustomerModel> dealerRetailerList = territoryManagementDao.getSCLAndNonSCLCustomerForSubArea(talukaForUser).stream().filter(d->d!=null && d.getGroups()!=null && ((d.getGroups()).contains(dealerGroup) || (d.getGroups()).contains(retailerGroup))).collect(Collectors.toList());
		LOG.info(String.format("dealerRetailerList ::%s",dealerRetailerList));
		return dealerRetailerList;
	}
	@Override
	public List<SclCustomerModel> getSCLAndNonSCLDealersRetailersForSubArea(String LeadType) {
		List<SclCustomerModel> dealerRetailerList =null;
		if(LeadType.equalsIgnoreCase("Dealer")) {
			UserGroupModel dealerGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID);
			dealerRetailerList = territoryManagementDao.getSCLAndNonSCLCustomerForSubArea(getTerritoriesForSO()).stream().filter(d -> (d.getGroups()).contains(dealerGroup)).collect(Collectors.toList());

		}
		else if(LeadType.equalsIgnoreCase("Retailer")) {
			UserGroupModel retailerGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID);
			dealerRetailerList = territoryManagementDao.getSCLAndNonSCLCustomerForSubArea(getTerritoriesForSO()).stream().filter(d->(d.getGroups()).contains(retailerGroup)).collect(Collectors.toList());

		}
		return dealerRetailerList;
	}

	//New Territory Change
	@Override
	public List<SclCustomerModel> getSCLAndNonSCLAllForSO() {
		List<SclCustomerModel> dealerRetailerList = getSCLAndNonSCLAllForSubArea(getTerritoriesForSO());
		LOG.info(String.format("dealerRetailerList :: %s ",dealerRetailerList.size()));
		return dealerRetailerList;
	}

	//New Territory Change
	@Override
	public List<SclCustomerModel> getSCLAndNonSCLAllForSubArea(List<SubAreaMasterModel> subSreas) {
		List<SclCustomerModel> dealerRetailerList = territoryManagementDao.getSCLAndNonSCLCustomerForSubArea(subSreas);
		return dealerRetailerList;
	}

	//New Territory Change
	@Override
	public List<SclCustomerModel> getDealerRetailerInfluencerForSubArea() {
		RequestCustomerData requestCustomerData = new RequestCustomerData();
		requestCustomerData.setCounterType(List.of(CounterType.DEALER.getCode(),CounterType.RETAILER.getCode(),CounterType.INFLUENCER.getCode()));
		List<SclCustomerModel> resultList = getCustomerforUser(requestCustomerData);
		return resultList!=null && !resultList.isEmpty() ? resultList :Collections.emptyList() ;
//		List<SclCustomerModel> resultList = getAllCustomerForSubArea(getTerritoriesForSO()).stream().filter(d->!(d.getGroups()).contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SITE_USER_GROUP_UID))).collect(Collectors.toList());
//		return resultList;
	}

	//New Territory Change
	@Override
	public List<SclCustomerModel> getRetailersForSubArea() {
		RequestCustomerData requestCustomerData = new RequestCustomerData();
		requestCustomerData.setCounterType(List.of(CounterType.RETAILER.getCode()));
		List<SclCustomerModel> retailerList = getCustomerforUser(requestCustomerData);
		return retailerList!=null && !retailerList.isEmpty() ? retailerList :Collections.emptyList() ;
//		List<SclCustomerModel> resultList = getAllCustomerForSubArea(getTerritoriesForSO()).stream().filter(r->(r.getGroups()).contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))).collect(Collectors.toList());
//		return resultList;
	}

	@Override
	public List<SclCustomerModel> getDealersForSubArea(String subAreaMaster) {
		RequestCustomerData requestCustomerData = new RequestCustomerData();
		requestCustomerData.setCounterType(List.of(CounterType.DEALER.getCode()));
		requestCustomerData.setSubAreaMasterPk(subAreaMaster);
		List<SclCustomerModel> dealerList = getCustomerforUser(requestCustomerData);
		return dealerList!=null && !dealerList.isEmpty() ? dealerList :Collections.emptyList() ;
//		return getAllCustomerForSubArea(List.of(getTerritoryById(subAreaMaster))).stream().filter(d->(d.getGroups()).contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList());
	}


	@Override
	public List<SclCustomerModel> getRetailersForSubArea(String subAreaMaster) {
		RequestCustomerData requestCustomerData = new RequestCustomerData();
		requestCustomerData.setCounterType(List.of(CounterType.RETAILER.getCode()));
		requestCustomerData.setSubAreaMasterPk(subAreaMaster);
		List<SclCustomerModel> retailerList = getCustomerforUser(requestCustomerData);
		return retailerList!=null && !retailerList.isEmpty() ? retailerList :Collections.emptyList() ;
//		return getAllCustomerForSubArea(List.of(getTerritoryById(subAreaMaster))).stream().filter(r->(r.getGroups()).contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))).collect(Collectors.toList());
	}

	@Override
	public List<SclCustomerModel> getInfluencersForSubArea(String subAreaMaster) {
		RequestCustomerData requestCustomerData = new RequestCustomerData();
		requestCustomerData.setCounterType(List.of(CounterType.INFLUENCER.getCode()));
		requestCustomerData.setSubAreaMasterPk(subAreaMaster);
		List<SclCustomerModel> influencerList = getCustomerforUser(requestCustomerData);
		return influencerList!=null && !influencerList.isEmpty() ? influencerList :Collections.emptyList() ;
		//return getAllCustomerForSubArea(List.of(getTerritoryById(subAreaMaster))).stream().filter(r->(r.getGroups()).contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID))).collect(Collectors.toList());
	}

	//New Territory Change
	@Override
	public List<SclCustomerModel> getInfluencersForSubArea() {
		RequestCustomerData requestCustomerData = new RequestCustomerData();
		requestCustomerData.setCounterType(List.of(CounterType.INFLUENCER.getCode()));
		List<SclCustomerModel> influencerList = getCustomerforUser(requestCustomerData);
		return influencerList!=null && !influencerList.isEmpty() ? influencerList :Collections.emptyList() ;
//		List<SclCustomerModel> resultList = getAllCustomerForSubArea(getTerritoriesForSO()).stream().filter(i->(i.getGroups()).contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID))).collect(Collectors.toList());
//		return resultList;
	}


	//New Territory Change
	@Override
	public List<SclCustomerModel> getSitesForSubArea() {
		RequestCustomerData requestCustomerData = new RequestCustomerData();
		requestCustomerData.setCounterType(List.of(CounterType.SITE.getCode()));
		List<SclCustomerModel> siteList = getCustomerforUser(requestCustomerData);
		return siteList!=null && !siteList.isEmpty() ? siteList :Collections.emptyList() ;
//		List<SclCustomerModel> resultList = getAllCustomerForSubArea(getTerritoriesForSO()).stream().filter(s->(s.getGroups()).contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SITE_USER_GROUP_UID))).collect(Collectors.toList());
//		return resultList;
	}

	@Override
	public List<SclCustomerModel> getSitesForSubAreaSO(List<String> districtList, List<String> subAreaList) {
		RequestCustomerData requestCustomerData = new RequestCustomerData();
		requestCustomerData.setCounterType(List.of(CounterType.SITE.getCode()));
		//List<SclCustomerModel> siteList = getCustomerforUser(requestCustomerData);
		List<SclCustomerModel> siteList = salesPerformanceService.getCustomersByLeadType(SITE,null,subAreaList,districtList);
		return siteList!=null && !siteList.isEmpty() ? siteList :Collections.emptyList() ;
//		List<SclCustomerModel> resultList = getAllCustomerForSubAreaSO(getTerritoriesForSO()).stream().filter(s->(s.getGroups()).contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SITE_USER_GROUP_UID))).collect(Collectors.toList());
//		return resultList;
	}

	@Override
	public List<SclCustomerModel> getSalesPromotersForSubArea() {
		return	getAllCustomerForSubArea(getTerritoriesForSO()).stream().filter(s->(s.getGroups()).contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SALES_PROMOTER_USER_GROUP_UID))).collect(Collectors.toList());
	}


	@Override
	public 	List<SclCustomerModel> getAllRetailersForSubAreaTOP(String subArea, String dealerCode){
		BaseSiteModel site = baseSiteService.getCurrentBaseSite();
		B2BCustomerModel currentUser=(B2BCustomerModel) userService.getCurrentUser();
		List<SubAreaMasterModel> subAreaMasterList  = new ArrayList<SubAreaMasterModel>();
		if(StringUtils.isBlank(subArea)) {
			if(currentUser instanceof SclUserModel) {
				FilterTalukaData filterTalukaData = new FilterTalukaData();
				subAreaMasterList  =  getTaulkaForUser(filterTalukaData);
			}
			else if(currentUser instanceof SclCustomerModel){
				SclUserModel sclUserModel = getSOforCustomer((SclCustomerModel) currentUser);
				subAreaMasterList  =  getTerritoriesForSO(sclUserModel.getUid());
			}
		}
		else {
			subAreaMasterList.add(getTerritoryById(subArea));
		}

		List<SclCustomerModel> resultList = territoryManagementDao.getAllRetailersForSubAreaTOP(subAreaMasterList, site,dealerCode).stream().filter(r->(r.getGroups()).contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))).collect(Collectors.toList());
		return resultList;
	}

	//To be Checked
	@Override
	public List<List<Object>> getAllTerritoryForSO() {
		BaseSiteModel site = baseSiteService.getCurrentBaseSite();
		List<List<Object>> list = territoryManagementDao.getAllStateDistrictSubAreaForSO(site);
		return list;
	}


	@Override
	public List<SclUserModel> getAllSalesOfficersByState(String state) {
		BaseSiteModel site = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao.getAllSalesOfficersByState(state,site) ;
	}


	@Override
	public List<String> getAllStatesForSO() {
		B2BCustomerModel customer=(B2BCustomerModel) userService.getCurrentUser();
		BaseSiteModel site = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao.getAllStatesForSO(customer,site);
	}

	private BaseSiteModel getBaseSite() {
		return baseSiteService.getCurrentBaseSite();
	}

	@Override
	public List<SubAreaMasterModel> getTerritoriesForSO() {
		FilterTalukaData filterTalukaData = new FilterTalukaData();
		return getTaulkaForUser(filterTalukaData);
	}

	@Override
	public List<SubAreaMasterModel> getDistrictsForSO(String state) {
		return territoryManagementDao.getTalukasForUser(state);
	}


	@Override
	public List<SubAreaMasterModel> getTerritoriesForSO(String uid) {
		return territoryManagementDao.getTerritoriesForSO((SclUserModel) userService.getUserForUID(uid));
	}

	@Override
	public List<SubAreaMasterModel> getTerritoriesForCustomer(String customerId) {
		return getTerritoriesForCustomer((SclCustomerModel)userService.getUserForUID(customerId));
	}

	@Override
	public List<SubAreaMasterModel> getTerritoriesForCustomer(SclCustomerModel customer) {
		return territoryManagementDao.getTerritoriesForCustomer(customer);
	}



	@Override
	public List<SubAreaMasterModel> getTerritoryForCustWithAllBrands(SclCustomerModel customer) {
		return (List<SubAreaMasterModel>) sessionService.executeInLocalView(new SessionExecutionBody()
		{
			@Override
			public List<SubAreaMasterModel> execute()
			{
				try {
					searchRestrictionService.disableSearchRestrictions();
					return territoryManagementDao.getTerritoryForCustWithAllBrands(customer);
				}
		finally {
						searchRestrictionService.enableSearchRestrictions();
					}
				}
			});
	}

	@Override
	public List<SubAreaMasterModel> getTerritoriesForPromoter(SclCustomerModel customer) {
		return territoryManagementDao.getTerritoriesForPromoter(customer);
	}

	@Override
	public SubAreaMasterModel getTerritoryById(String territoryId) {
		return territoryManagementDao.getTerritoryById(territoryId);
	}

	@Override
	public SubAreaMasterModel getTerritoryByDistrictAndTaluka(String district, String taluka) {
		return territoryManagementDao.getTerritoryByDistrictAndTaluka(district, taluka);
	}

	@Override
	public List<SclCustomerModel> getAllCustomerForSO() {
		List<SclCustomerModel> customerList = new ArrayList<SclCustomerModel>();
		var subareaMaster=getTerritoriesForSO();
		if(CollectionUtils.isNotEmpty(subareaMaster)) {
			RequestCustomerData requestCustomerData = new RequestCustomerData();
			customerList = territoryManagementDao.getCustomerForUser(requestCustomerData, subareaMaster);
		}
		return customerList;
	}

	@Override
	public List<SclCustomerModel> getAllCustomerForSO(String uid) {
		List<SclCustomerModel> customerList = new ArrayList<>();
		var subareaMaster=getTerritoriesForSO(uid);
		if(CollectionUtils.isNotEmpty(subareaMaster)) {
			RequestCustomerData requestCustomerData = new RequestCustomerData();
			customerList = territoryManagementDao.getCustomerForUser(requestCustomerData, subareaMaster);
		}
		return customerList;
	}

	@Override
	public List<SclCustomerModel> getAllCustomerForSubArea(List<SubAreaMasterModel> subAreas) {
		List<SclCustomerModel> customerList = new ArrayList<SclCustomerModel>();
		if(subAreas!=null && !subAreas.isEmpty()) {
			RequestCustomerData requestCustomerData = new RequestCustomerData();
			customerList = territoryManagementDao.getCustomerForUser(requestCustomerData, subAreas);
		}
		return customerList;
	}



	@Override
	public List<SclCustomerModel> getAllCustomerForSubAreaSO(List<SubAreaMasterModel> subAreas) {
		List<SclCustomerModel> customerList = new ArrayList<SclCustomerModel>();
		if(subAreas!=null && !subAreas.isEmpty()) {
			RequestCustomerData requestCustomerData = new RequestCustomerData();
			customerList = territoryManagementDao.getCustomerForUser(requestCustomerData, subAreas);
		}
		return customerList;
	}


	@Override
	public Collection<SubAreaMasterModel> getCurrentTerritory() {
		try {
			return sessionService.getAttribute("currentTerritory");
		} catch (JaloObjectNoLongerValidException var2) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Session Territory no longer valid. Removing from session. getCurrentTerritory will return empty list. {}", var2);
			}
			sessionService.setAttribute("currentTerritory", (Object)null);
			return Collections.emptyList();
		}
	}

	@Override
	public void setCurrentTerritory(Collection<SubAreaMasterModel> subAreas) {
		Collection<SubAreaMasterModel> list = new ArrayList<SubAreaMasterModel>();
		if(subAreas==null || subAreas.isEmpty()) {
			if( userService.getCurrentUser() instanceof SclUserModel) {
				list.addAll(territoryManagementDao.getTerritoriesForSOInLocalView());
			}
			else if(userService.getCurrentUser() instanceof SclCustomerModel) {
				//TODO
			}
		}
		else {
			LOG.error("Territory Set from UI");
			list.addAll(subAreas);
		}
		sessionService.setAttribute("currentTerritory", list);
	}

	@Override
	public SclUserModel getSOforCustomer(SclCustomerModel customer) {
		return territoryManagementDao.getSOForSubArea(customer);
	}

	@Override
	public List<SclUserModel> getUsersForSubAreas(List<SubAreaMasterModel> subAreaMasters) {
		return territoryManagementDao.getSclUsersForSubArea(subAreaMasters);
	}

	@Override
	public CustDepotMasterModel getCustDepotForCustomer(SclCustomerModel customer) {
		return territoryManagementDao.getCustDepotForCustomer(customer);
	}

	@Override
	public List<CustDepotMasterModel> getCustDepotForSP(B2BCustomerModel sp) {
		return territoryManagementDao.getCustDepotForSP(sp);
	}

	@Override
	public CustDepotMasterModel getCustDepotForCode(String code) {
		return territoryManagementDao.getCustDepotForCode(code);
	}

	@Override
	public List<SclCustomerModel> getRetailerListForDealer() {
		SclCustomerModel sclCustomerModel=(SclCustomerModel) userService.getCurrentUser();
		return territoryManagementDao.getRetailerListForDealer(sclCustomerModel);
	}

	@Override
	public List<SclCustomerModel> getInfluencerListForDealer() {
		SclCustomerModel sclCustomerModel=(SclCustomerModel) userService.getCurrentUser();
		BaseSiteModel site = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao.getInfluencerListForDealer(sclCustomerModel,site);
	}

	@Override
	public List<SclCustomerModel> getInfluencerListForRetailer() {
		SclCustomerModel sclCustomerModel=(SclCustomerModel) userService.getCurrentUser();
		BaseSiteModel site = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao.getInfluencerListForRetailer(sclCustomerModel,site);
	}

	@Override
	public SearchPageData<SclCustomerModel> getRetailerListForDealerPagination(SearchPageData searchPageData, String networkType, boolean isNew, String filter, Boolean isTop) {
		SclCustomerModel sclCustomerModel=(SclCustomerModel) userService.getCurrentUser();
		BaseSiteModel site = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao.getRetailerListForDealerPagination(searchPageData, sclCustomerModel, site, networkType, isNew, filter, isTop);
	}
	@Override
	public SearchPageData<SclCustomerModel> getInfluencerListForDealerPagination(SearchPageData searchPageData, String networkType, boolean isNew, String filter,
																				 String influencerType, String dealerCategory) {
		SclCustomerModel sclCustomerModel=(SclCustomerModel) userService.getCurrentUser();
		BaseSiteModel site = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao.getInfluencerListForDealerPagination(searchPageData, sclCustomerModel, site, networkType, isNew, filter, influencerType, dealerCategory);
	}
	@Override
	public SearchPageData<SclCustomerModel> getInfluencerListForRetailerPagination(SearchPageData searchPageData, String networkType, boolean isNew, String filter,
																				   String influencerType, String dealerCategory) {
		SclCustomerModel sclCustomerModel=(SclCustomerModel) userService.getCurrentUser();
		BaseSiteModel site = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao.getInfluencerListForRetailerPagination(searchPageData, sclCustomerModel, site, networkType, isNew, filter, influencerType, dealerCategory);
	}

	@Override
	public Integer getDealerCountForRetailer() {
		SclCustomerModel currentUser = (SclCustomerModel) userService.getCurrentUser();
		BaseSiteModel currentSite = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao.getDealerCountForRetailer(currentUser,currentSite);
	}

	@Override
	public Integer getRetailerCountForDealer() {
		SclCustomerModel currentUser = (SclCustomerModel) userService.getCurrentUser();
		BaseSiteModel currentSite = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao.getRetailerCountForDealer(currentUser,currentSite);
	}

	@Override
	public Integer getInfluencerCountForDealer() {
		SclCustomerModel currentUser = (SclCustomerModel) userService.getCurrentUser();
		BaseSiteModel currentSite = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao.getInfluencerCountForDealer(currentUser,currentSite);
	}

	@Override
	public SearchPageData<SclCustomerModel> getDealerListForRetailerPagination(SearchPageData searchPageData, String filter) {
		SclCustomerModel currentUser=(SclCustomerModel) userService.getCurrentUser();
		BaseSiteModel currentSite = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao.getDealerListForRetailerPagination(searchPageData,currentUser,currentSite,filter);
	}

	@Override
	public List<SclCustomerModel> getDealerListForRetailer(){
		SclCustomerModel currentUser = (SclCustomerModel) userService.getCurrentUser();
		BaseSiteModel currentSite = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao.getDealerListForRetailer(currentUser,currentSite);
	}


	public List<SubAreaMasterModel>  getTaulkaForUser(FilterTalukaData filterTalukaData) {
		B2BCustomerModel currentUser=(B2BCustomerModel) userService.getCurrentUser();
		if(currentUser instanceof SclUserModel) {
			return territoryManagementDao.getTalukaForUser(filterTalukaData);
		} else if ((((SclCustomerModel) currentUser).getCounterType()!=null) &&
				(((SclCustomerModel) currentUser).getCounterType().equals(CounterType.SP))) {
			return territoryManagementDao.getTalukaForSP(filterTalukaData);

		} else {
			return getTerritoriesForCustomer((SclCustomerModel)currentUser);
		}
	}

	@Override
	public List<DistrictMasterModel> getDistrictForUser(FilterDistrictData filterDistrictData) {
		return territoryManagementDao.getDistrictForUser(filterDistrictData);
	}

	@Override
	public List<SclCustomerModel> getCustomerforUser(RequestCustomerData requestCustomerData){
		B2BCustomerModel currentUser=(B2BCustomerModel) userService.getCurrentUser();
		List<SclCustomerModel> customerList = null;
		if(currentUser instanceof SclUserModel ||
				(((SclCustomerModel) currentUser).getCounterType()==null) ||
				(( !((SclCustomerModel) currentUser).getCounterType().equals(CounterType.SP)))){
			List<SubAreaMasterModel> subAreaMasterList = new ArrayList<SubAreaMasterModel>();
			if(StringUtils.isNotBlank(requestCustomerData.getSubAreaMasterPk())) {
				subAreaMasterList.add(getTerritoryById(requestCustomerData.getSubAreaMasterPk()));
			}
			else{
				FilterTalukaData filterTalukaData = new FilterTalukaData();
				subAreaMasterList = getTaulkaForUser(filterTalukaData);
			}
			if(subAreaMasterList!=null && !subAreaMasterList.isEmpty()) {
				customerList = territoryManagementDao.getCustomerForUser(requestCustomerData, subAreaMasterList);
				return customerList;
			}
		}
		else{
			customerList=territoryManagementDao.getDealersForSP(requestCustomerData);
			return customerList;
		}
		return Collections.emptyList();
	}


	@Override
	public SearchPageData<SclCustomerModel> getCustomerForUserPagination(SearchPageData searchPageData,
																		 RequestCustomerData customerData) {
		B2BCustomerModel currentUser=(B2BCustomerModel) userService.getCurrentUser();
		if(currentUser instanceof SclUserModel ||
				(((SclCustomerModel) currentUser).getCounterType()==null) ||
				((!((SclCustomerModel) currentUser).getCounterType().equals(CounterType.SP)))){
			List<SubAreaMasterModel> subAreaMasterList = new ArrayList<SubAreaMasterModel>();
			if(StringUtils.isNotBlank(customerData.getSubAreaMasterPk())) {
				subAreaMasterList.add(getTerritoryById(customerData.getSubAreaMasterPk()));
			}
			else{
				FilterTalukaData filterTalukaData = new FilterTalukaData();
				subAreaMasterList = getTaulkaForUser(filterTalukaData);
			}
			if(subAreaMasterList!=null && !subAreaMasterList.isEmpty()) {
				return territoryManagementDao.getPaginatedCustomerForUser(searchPageData, customerData, subAreaMasterList);
			}
		}
		else{
			return territoryManagementDao.getPaginatedDealersForSP(searchPageData,customerData);
		}
		return null;
	}


	@Override
	public List<RegionMasterModel> getRegionsForUser(FilterRegionData filterRegionData){
		B2BCustomerModel currentUser=(B2BCustomerModel) userService.getCurrentUser();
		List<RegionMasterModel> regionMasterList = null;
		if(currentUser instanceof SclUserModel && currentUser.getUserType()!=null){
			if(currentUser.getUserType().equals(SclUserType.RH)){
				regionMasterList = territoryManagementDao.getRegionsForRH(filterRegionData);
			}
			else if(currentUser.getUserType().equals(SclUserType.TSM)){
				regionMasterList = territoryManagementDao.getRegionsForTSM(filterRegionData);
			}
			else if(currentUser.getUserType().equals(SclUserType.SO)){
				regionMasterList = territoryManagementDao.getRegionsForSO(filterRegionData);
			}
			return regionMasterList;
		}
		return Collections.emptyList();
	}


	@Override
	public Integer getInfluencerCountForRetailer() {
		SclCustomerModel currentUser = (SclCustomerModel) userService.getCurrentUser();
		BaseSiteModel currentSite = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao.getInfluencerCountForRetailer(currentUser,currentSite);
	}

	@Override
	public List<UserSubAreaMappingModel> getSOSubAreaMappingForUser(FilterTalukaData filterTalukaData) {
		return territoryManagementDao.getSOSubAreaMappingForUser(filterTalukaData);
	}

	@Override
	public List<TsmDistrictMappingModel> getTSMDistrcitMappingForUser(FilterDistrictData filterDistrictData) {
		return territoryManagementDao.getTSMDistrcitMappingForUser(filterDistrictData);
	}

	public Collection<DistrictMasterModel> getCurrentDistrict() {
		try {
			return sessionService.getAttribute("currentDistrict");
		} catch (JaloObjectNoLongerValidException var2) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Session District no longer valid. Removing from session. getCurrentDistrict will return empty list. {}", var2);
			}
			sessionService.setAttribute("currentDistrict", (Object)null);
			return Collections.emptyList();
		}
	}

	@Override
	public void setCurrentDistrict(Collection<DistrictMasterModel> districts) {
		Collection<DistrictMasterModel> list = new ArrayList<>();
		if(districts == null || districts.isEmpty()) {
			if(userService.getCurrentUser() instanceof SclUserModel) {
				list.addAll(districtMasterDao.getDistrictsForTsmInLocalView());
			}
		}
		else {
			LOG.error("District Set from UI");
			list.addAll(districts);
		}
		sessionService.setAttribute("currentDistrict", list);
	}

	@Override
	public Collection<RegionMasterModel> getCurrentRegion() {
		try {
			return sessionService.getAttribute("currentRegion");
		} catch (JaloObjectNoLongerValidException var2) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Session Region no longer valid. Removing from session. getCurrentRegion will return empty list. {}", var2);
			}
			sessionService.setAttribute("currentRegion", (Object)null);
			return Collections.emptyList();
		}
	}

	@Override
	public void setCurrentRegion(Collection<RegionMasterModel> regions) {
		Collection<RegionMasterModel> list = new ArrayList<>();
		if(regions == null || regions.isEmpty()) {
			if(userService.getCurrentUser() instanceof SclUserModel) {
				list.addAll(regionMasterDao.getRegionsForRhInLocalView());
			}
		}
		else {
			LOG.error("Region Set from UI");
			list.addAll(regions);
		}
		sessionService.setAttribute("currentRegion", list);
	}

	@Override
	public List<SclUserModel> getTSMForUser(FilterDistrictData filterDistrictData) {
		List<SclUserModel> sclUserList = new ArrayList<>();
		List<TsmDistrictMappingModel> tsmMappingList = territoryManagementDao.getTSMDistrcitMappingForUser(filterDistrictData);
		if(tsmMappingList!=null && !tsmMappingList.isEmpty()) {
			sclUserList = tsmMappingList.stream().map(each->each.getTsmUser()).distinct().collect(Collectors.toList());
		}
		return sclUserList;
	}

	@Override
	public List<SclUserModel> getSOForUser(FilterTalukaData filterTalukaData) {
		List<SclUserModel> sclUserList = new ArrayList<>();
		B2BCustomerModel currentUser=(B2BCustomerModel) userService.getCurrentUser();
		//for TSO Linked SO List
		if (currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.TSO_GROUP_ID))){
			List<SubAreaMasterModel> taulkaForUser = getTaulkaForUser(filterTalukaData);
			if(taulkaForUser!=null && !taulkaForUser.isEmpty()) {
				sclUserList = getUsersForSubAreas(taulkaForUser);
			}
		}
		else if(currentUser instanceof SclUserModel){
			List<UserSubAreaMappingModel> soMappingList = territoryManagementDao.getSOSubAreaMappingForUser(filterTalukaData);
			if(soMappingList!=null && !soMappingList.isEmpty()) {
				sclUserList = soMappingList.stream().map(each -> each.getSclUser()).distinct().collect(Collectors.toList());
			}
		}
		else if ((((SclCustomerModel) currentUser).getCounterType()!=null) &&
				(((SclCustomerModel) currentUser).getCounterType().equals(CounterType.SP))) {
			sclUserList = territoryManagementDao.getSOForSP(filterTalukaData);
		}

		return sclUserList;
	}

	@Override
	public void setCurrentCustDepot(Collection<CustDepotMasterModel> custDepotModelList) {
		Collection<CustDepotMasterModel> list = new ArrayList<>();
		if(custDepotModelList == null || custDepotModelList.isEmpty()) {
			list.addAll(getCustDepotForSPInLocalView((B2BCustomerModel) userService.getCurrentUser()));
		}
		else {
			list.addAll(custDepotModelList);
		}
		sessionService.setAttribute("currentCustDepot", list);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CustDepotMasterModel> getCustDepotForSPInLocalView(B2BCustomerModel sp) {
		return (List<CustDepotMasterModel>) sessionService.executeInLocalView(new SessionExecutionBody()
		{
			@Override
			public List<CustDepotMasterModel> execute()
			{
				try {
					searchRestrictionService.disableSearchRestrictions();
					return getCustDepotForSP(sp);
				}
				finally {
					searchRestrictionService.enableSearchRestrictions();
				}
			}
		});
	}

	@Override
	public SclUserModel getSalesOfficerforTaluka(String taluka, BaseSiteModel brand) {
		return territoryManagementDao.getSalesOfficerforTaluka(taluka,brand);
	}


	@Override
	public List<DistrictMasterModel> getDistrictForSP(FilterDistrictData filterDistrictData) {
		return territoryManagementDao.getDistrictForSP(filterDistrictData);
	}

	@Override
	public CounterVisitMasterModel findCounterVisitForUnFlaggedDealer(SclCustomerModel sclCustomer, boolean isUnFlagRequsetRaised, Date unFlagtime, String remarkForUnFlag) {
		return territoryManagementDao.findCounterVisitForUnFlaggedDealer(sclCustomer,isUnFlagRequsetRaised,unFlagtime,remarkForUnFlag);
	}

	@Override
	public SclCustomerModel getSpForCustomer(SclCustomerModel dealer) {
		return territoryManagementDao.getSPForCustomer(dealer);
	}

	@Override
	public SclCustomerModel getSpForCustomerAndBrand(SclCustomerModel dealer, BaseSiteModel brand) {
		return territoryManagementDao.getSPForCustomerAndBrand(dealer,brand);
	}

	@Override
	public List<SclUserModel> getTSMforDistrict(DistrictMasterModel district, BaseSiteModel brand) {
		return territoryManagementDao.getTSMforDistrict(district,brand);
	}

	@Override
	public List<SclUserModel> getRHforRegion(RegionMasterModel region, BaseSiteModel brand) {
		return territoryManagementDao.getRHforRegion(region,brand);
	}


	@Override
	public Collection<CustDepotMasterModel> getCurrentCustDepot() {
		try {
			return sessionService.getAttribute("currentCustDepot");
		} catch (JaloObjectNoLongerValidException var2) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Session Cust Depot no longer valid. Removing from session. getCurrentCustDepot will return empty list. {}", var2);
			}
			sessionService.setAttribute("currentCustDepot", (Object)null);
			return Collections.emptyList();
		}
	}

	@Override
	public List<SclUserModel> getTSOforSubArea(SubAreaMasterModel subArea) {
		return territoryManagementDao.getTSOforSubArea(subArea);
	}

	@Override
	public SearchPageData<SclCustomerModel> fetchRetailersForDealer(SearchPageData searchPageData, String searchKey) {
		SclCustomerModel sclCustomerModel=(SclCustomerModel) userService.getCurrentUser();
		BaseSiteModel site = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao.fetchRetailersForDealer(searchPageData, sclCustomerModel, site,  searchKey);
	}
	public List<SclUserModel> getTSOforSubAreas(List<SubAreaMasterModel> subArea) {
		return territoryManagementDao.getTSOforSubAreas(subArea);
	}

	@Override
	public List<SclUserModel> getSOforSite(TerritoryMasterModel territoryMaster) {
		return territoryManagementDao.getSOforSite(territoryMaster);
	}

}