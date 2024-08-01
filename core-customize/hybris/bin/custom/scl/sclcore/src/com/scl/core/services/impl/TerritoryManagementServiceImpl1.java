package com.scl.core.services.impl;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.TerritoryManagementDao1;
import com.scl.core.enums.CounterType;
import com.scl.core.enums.SclUserType;
import com.scl.core.jalo.SclCustomer;
import com.scl.core.jalo.SclUser;
import com.scl.core.model.*;
import com.scl.core.region.dao.DistrictMasterDao;
import com.scl.core.region.dao.RegionMasterDao;
import com.scl.core.services.TerritoryManagementService1;
import com.scl.facades.data.FilterDistrictData;
import com.scl.facades.data.FilterRegionData;
import com.scl.facades.data.FilterTalukaData;
import com.scl.facades.data.RequestCustomerData;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.core.model.user.UserModel;
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

public class TerritoryManagementServiceImpl1 implements TerritoryManagementService1 {

	private static final Logger LOG = Logger.getLogger(TerritoryManagementServiceImpl1.class);
	 
	@Autowired
	TerritoryManagementDao1 territoryManagementDao1;
	
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
	
	//To be Checked
	@Override
	public List<String> getAllSubAreaForSO(String userId) {
		BaseSiteModel site = baseSiteService.getCurrentBaseSite();		
		return territoryManagementDao1.getAllSubAreaForSO(userService.getUserForUID(userId),site);
	}

	//To be Checked
	@Override
	public List<String> getAllSubAreaForCustomer(String customerId) {
		BaseSiteModel site = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao1.getAllSubAreaForCustomer(userService.getUserForUID(customerId),site);
	}

	//Not to be used
	@Override
	public List<SclCustomerModel> getAllCustomerForSubArea(String subArea) {
		BaseSiteModel site = baseSiteService.getCurrentBaseSite();
		List<String> subAreaList = null;
		if(StringUtils.isNotBlank(subArea)) {
			subAreaList = Arrays.asList(subArea.split(","));
		}
		List<SclCustomerModel> customerList = territoryManagementDao1.getAllCustomerForSubArea(subAreaList,site);
		return customerList;
	}
	
	//New Territory Change
	@Override
	public List<SclCustomerModel> getDealersForSubArea() {
		List<SubAreaMasterModel> list = new ArrayList<SubAreaMasterModel>();
		UserModel user = userService.getCurrentUser();
		if(user instanceof SclUserModel) {
			list = getTerritoriesForSO();
		}
		else if(user instanceof SclCustomerModel){
			list = getTerritoriesForCustomer((SclCustomerModel)user);
		}
		List<SclCustomerModel> dealerList = getAllCustomerForSubArea(list).stream().filter(d -> (d.getGroups()).contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList());
		return dealerList!=null && !dealerList.isEmpty() ? dealerList :Collections.emptyList() ;
	}
	
	//New Territory Change
	@Override
	public List<SclCustomerModel> getSCLAndNonSCLDealersRetailersForSubArea() {
		UserGroupModel dealerGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID);
		UserGroupModel retailerGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID);
		List<SclCustomerModel> dealerRetailerList = territoryManagementDao1.getSCLAndNonSCLCustomerForSubArea(getTerritoriesForSO()).stream().filter(d->(d.getGroups()).contains(dealerGroup) || (d.getGroups()).contains(retailerGroup)).collect(Collectors.toList());
		return dealerRetailerList;
	}
	@Override
	public List<SclCustomerModel> getSCLAndNonSCLDealersRetailersForSubArea(String LeadType) {
		List<SclCustomerModel> dealerRetailerList =null;
		if(LeadType.equalsIgnoreCase("Dealer")) {
			UserGroupModel dealerGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID);
		 dealerRetailerList = territoryManagementDao1.getSCLAndNonSCLCustomerForSubArea(getTerritoriesForSO()).stream().filter(d -> (d.getGroups()).contains(dealerGroup)).collect(Collectors.toList());

		}
		else if(LeadType.equalsIgnoreCase("Retailer")) {
			UserGroupModel retailerGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID);
		dealerRetailerList = territoryManagementDao1.getSCLAndNonSCLCustomerForSubArea(getTerritoriesForSO()).stream().filter(d->(d.getGroups()).contains(retailerGroup)).collect(Collectors.toList());

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
		List<SclCustomerModel> dealerRetailerList = territoryManagementDao1.getSCLAndNonSCLCustomerForSubArea(subSreas);
		return dealerRetailerList;
	}	
	
	//New Territory Change
	@Override
	public List<SclCustomerModel> getDealerRetailerInfluencerForSubArea() {
		List<SclCustomerModel> resultList = getAllCustomerForSubArea(getTerritoriesForSO()).stream().filter(d->!(d.getGroups()).contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SITE_USER_GROUP_UID))).collect(Collectors.toList());
		return resultList;
	}
	
	//New Territory Change
	@Override
	public List<SclCustomerModel> getRetailersForSubArea() {
		List<SclCustomerModel> resultList = getAllCustomerForSubArea(getTerritoriesForSO()).stream().filter(r->(r.getGroups()).contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))).collect(Collectors.toList());
		return resultList;
	}

	@Override
	public List<SclCustomerModel> getDealersForSubArea(String subAreaMaster) {
		return getAllCustomerForSubArea(List.of(getTerritoryById(subAreaMaster))).stream().filter(d->(d.getGroups()).contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList());
	}

	@Override
	public List<SclCustomerModel> getAllSclAndNonSclCustomerForTerritories(String subAreaMaster) {
		return getAllSclAndNonSclCustomerForTerritory(List.of(getTerritoryById(subAreaMaster))).stream().filter(d->(d.getGroups()).contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList());
	}

	@Override
	public List<SclCustomerModel> getRetailersForSubArea(String subAreaMaster) {
		return getAllCustomerForSubArea(List.of(getTerritoryById(subAreaMaster))).stream().filter(r->(r.getGroups()).contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))).collect(Collectors.toList());
	}

	@Override
	public List<SclCustomerModel> getInfluencersForSubArea(String subAreaMaster) {
		return getAllCustomerForSubArea(List.of(getTerritoryById(subAreaMaster))).stream().filter(r->(r.getGroups()).contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID))).collect(Collectors.toList());
	}

	//New Territory Change
	@Override
	public List<SclCustomerModel> getInfluencersForSubArea() {
		List<SclCustomerModel> resultList = getAllCustomerForSubArea(getTerritoriesForSO()).stream().filter(i->(i.getGroups()).contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID))).collect(Collectors.toList());
		return resultList;
	}

	@Override
	public SearchPageData<SclCustomerModel> getPaginatedInfluencersForSubArea(SearchPageData searchPageData) {
		List<SubAreaMasterModel> territoriesForSO = getTerritoriesForSO();
		SearchPageData<SclCustomerModel> resultList = territoryManagementDao1.getAllPaginatedCustomerForTerritories(territoriesForSO,searchPageData);
	   // List<SclCustomerModel> collect = resultList.getResults().stream().filter(i -> (i.getGroups()).contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID))).collect(Collectors.toList());
		//resultList.setResults(collect);
		return resultList;
	}

	//New Territory Change
	@Override
	public List<SclCustomerModel> getSitesForSubArea() {
		List<SclCustomerModel> resultList = getAllCustomerForSubArea(getTerritoriesForSO()).stream().filter(s->(s.getGroups()).contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SITE_USER_GROUP_UID))).collect(Collectors.toList());
		return resultList;
	}

	@Override
	public List<SclCustomerModel> getSitesForSubAreaSO() {
		List<SclCustomerModel> resultList = getAllCustomerForSubAreaSO(getTerritoriesForSO()).stream().filter(s->(s.getGroups()).contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SITE_USER_GROUP_UID))).collect(Collectors.toList());
		return resultList;
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

		List<SclCustomerModel> resultList = territoryManagementDao1.getAllRetailersForSubAreaTOP(subAreaMasterList, site,dealerCode).stream().filter(r->(r.getGroups()).contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))).collect(Collectors.toList());
		return resultList;
	}

	//To be Checked
	@Override
	public List<List<Object>> getAllTerritoryForSO() {
		BaseSiteModel site = baseSiteService.getCurrentBaseSite();
		List<List<Object>> list = territoryManagementDao1.getAllStateDistrictSubAreaForSO(site);
		return list;
	}


	@Override
	public List<SclUserModel> getAllSalesOfficersByState(String state) {
		BaseSiteModel site = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao1.getAllSalesOfficersByState(state,site) ;
	}

	@Override
	public List<SclCustomerModel> getAllSalesOfficersByDistrict(String district) {
		BaseSiteModel site = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao1.getAllSalesOfficersByDistrict(district,site) ;
	}

	@Override
	public List<String> getAllStatesForSO() {
		B2BCustomerModel customer=(B2BCustomerModel) userService.getCurrentUser();
		BaseSiteModel site = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao1.getAllStatesForSO(customer,site);
	}

	@Override
	public List<String> getAllDistrictForSO(UserModel sclUser) {
		return territoryManagementDao1.getAllDistrictForSO(sclUser,getBaseSite());
	}



	@Override
	public List<SclUserModel> getAllUserForDistrict(List<String> districts) {
		return territoryManagementDao1.getAllUserForDistrict(districts,getBaseSite());
	}

	private BaseSiteModel getBaseSite() {
		return baseSiteService.getCurrentBaseSite();
	}
	
	@Override
	public List<SubAreaMasterModel> getTerritoriesForSO() {
		return territoryManagementDao1.getTerritoriesForSO();
	}

	@Override
	public List<SubAreaMasterModel> getTerritoriesForSO(String uid) {
		return territoryManagementDao1.getTerritoriesForSO((SclUserModel) userService.getUserForUID(uid));
	}

	@Override
	public List<SubAreaMasterModel> getTerritoriesForCustomer(String customerId) {
		return getTerritoriesForCustomer((SclCustomerModel)userService.getUserForUID(customerId));
	}
	
	@Override
	public List<SubAreaMasterModel> getTerritoriesForCustomer(SclCustomerModel customer) {
		return territoryManagementDao1.getTerritoriesForCustomer(customer);
	}

	@Override
	public List<SubAreaMasterModel> getTerritoriesForPromoter(SclCustomerModel customer) {
		return territoryManagementDao1.getTerritoriesForPromoter(customer);
	}

	@Override
	public SubAreaMasterModel getTerritoryById(String territoryId) {
		return territoryManagementDao1.getTerritoryById(territoryId);
	}
	
	@Override
	public SubAreaMasterModel getTerritoryByDistrictAndTaluka(String district, String taluka) {
		return territoryManagementDao1.getTerritoryByDistrictAndTaluka(district, taluka);
	}

	@Override
	public List<SclCustomerModel> getAllCustomerForSO() {
		List<SclCustomerModel> customerList = new ArrayList<SclCustomerModel>();
		var subareaMaster=getTerritoriesForSO();
		if(CollectionUtils.isNotEmpty(subareaMaster)) {
			customerList = territoryManagementDao1.getAllCustomerForTerritories(subareaMaster);
		}
		return customerList;
	}

	@Override
	public List<SclCustomerModel> getAllCustomerForSO(String uid) {
		List<SclCustomerModel> customerList = new ArrayList<>();
		var subareaMaster=getTerritoriesForSO(uid);
		if(CollectionUtils.isNotEmpty(subareaMaster)) {
			customerList = territoryManagementDao1.getAllCustomerForTerritories(subareaMaster);
		}
		return customerList;
	}

	@Override
	public List<SclCustomerModel> getAllCustomerForSubArea(List<SubAreaMasterModel> subAreas) {
		List<SclCustomerModel> customerList = new ArrayList<SclCustomerModel>();
		if(subAreas!=null && !subAreas.isEmpty()) {
			customerList = territoryManagementDao1.getAllCustomerForTerritories(subAreas);
		}
		return customerList;
	}

	@Override
	public List<SclCustomerModel> getAllCustomerForSubAreaSO(List<SubAreaMasterModel> subAreas) {
		List<SclCustomerModel> customerList = new ArrayList<SclCustomerModel>();
		if(subAreas!=null && !subAreas.isEmpty()) {
			customerList = territoryManagementDao1.getAllCustomerForTerritoriesSO(subAreas);
		}
		return customerList;
	}
	//getAllSclandNonSclCustomerForTerritories
	@Override
	public List<SclCustomerModel> getAllSclAndNonSclCustomerForTerritory(List<SubAreaMasterModel> subAreas) {
		List<SclCustomerModel> customerList = new ArrayList<SclCustomerModel>();
		if(subAreas!=null && !subAreas.isEmpty()) {
			customerList = territoryManagementDao1.getAllSclAndNonSclCustomerForTerritory(subAreas);
		}
		LOG.info(String.format("customer List %s ::",customerList.size()));
		return customerList;
	}
	
	@Override
	public SearchPageData<SclCustomerModel> getCustomerByTerritoriesAndCounterType(SearchPageData searchPageData, List<SubAreaMasterModel> subAreaMaster, String counterType, String networkType, boolean isNew, String filter, String influencerType, String dealerCategory) {
		return territoryManagementDao1.getCustomerByTerritoriesAndCounterType(searchPageData, subAreaMaster, counterType, networkType, isNew, filter, influencerType, dealerCategory);
	}

	@Override
	public List<SclCustomerModel> getCustomerByTerritoriesAndCounterTypeWithoutPagination(String counterType, String networkType, String filter, String influencerType, String dealerCategory,boolean isNew) {
		return territoryManagementDao1.getCustomerByTerritoriesAndCounterTypeWithoutPagination( counterType, getTerritoriesForSO(),networkType,  filter, influencerType, dealerCategory,isNew);
	}

	@Override
	public SearchPageData<SclCustomerModel> getCustomerByTerritoriesAndCounterType(SearchPageData searchPageData, String counterType, String networkType, boolean isNew, String filter, String influencerType, String dealerCategory) {
		return getCustomerByTerritoriesAndCounterType(searchPageData, getTerritoriesForSO(), counterType, networkType, isNew, filter, influencerType, dealerCategory);
	}
	
	public SearchPageData<SclCustomerModel> getAllPaginatedCustomerForSubArea(List<SubAreaMasterModel> subAreas,SearchPageData searchPageData) {
		return territoryManagementDao1.getAllPaginatedCustomerForTerritories(subAreas,searchPageData);
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
				list.addAll(territoryManagementDao1.getTerritoriesForSOInLocalView());
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
	public SubAreaMasterModel getTerritoryByTaluka(String taluka) {
		return territoryManagementDao1.getTerritoryByTaluka(taluka);
	}
	
	@Override
	public SclUserModel getSOforCustomer(SclCustomerModel customer) {
		return territoryManagementDao1.getSOForSubArea(customer);
	}

	@Override
	public List<SubAreaMasterModel> getSubAreaForDealers(List<SclCustomerModel> dealers) {
		return territoryManagementDao1.getSubAreaForDealers(dealers);
	}

	@Override
	public List<SclUserModel> getUsersForSubAreas(List<SubAreaMasterModel> subAreaMasters) {
		return territoryManagementDao1.getSclUsersForSubArea(subAreaMasters);
	}

	@Override
	public CustDepotMasterModel getCustDepotForCustomer(SclCustomerModel customer) {
		return territoryManagementDao1.getCustDepotForCustomer(customer);
	}

	@Override
	public List<CustDepotMasterModel> getCustDepotForSP(B2BCustomerModel sp) {
		return territoryManagementDao1.getCustDepotForSP(sp);
	}
	
	@Override
	public CustDepotMasterModel getCustDepotForCode(String code) {
		return territoryManagementDao1.getCustDepotForCode(code);
	}
	
	@Override
	public List<SclCustomerModel> getRetailerListForDealer() {
		SclCustomerModel sclCustomerModel=(SclCustomerModel) userService.getCurrentUser();
		BaseSiteModel site = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao1.getRetailerListForDealer(sclCustomerModel,site);
	}

	@Override
	public List<SclCustomerModel> getInfluencerListForDealer() {
		SclCustomerModel sclCustomerModel=(SclCustomerModel) userService.getCurrentUser();
		BaseSiteModel site = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao1.getInfluencerListForDealer(sclCustomerModel,site);
	}

	@Override
	public List<SclCustomerModel> getInfluencerListForRetailer() {
		SclCustomerModel sclCustomerModel=(SclCustomerModel) userService.getCurrentUser();
		BaseSiteModel site = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao1.getInfluencerListForRetailer(sclCustomerModel,site);
	}

	@Override
	public SearchPageData<SclCustomerModel> getRetailerListForDealerPagination(SearchPageData searchPageData, String networkType, boolean isNew, String filter, Boolean isTop) {
		SclCustomerModel sclCustomerModel=(SclCustomerModel) userService.getCurrentUser();
		BaseSiteModel site = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao1.getRetailerListForDealerPagination(searchPageData, sclCustomerModel, site, networkType, isNew, filter, isTop);
	}
	@Override
	public SearchPageData<SclCustomerModel> getInfluencerListForDealerPagination(SearchPageData searchPageData, String networkType, boolean isNew, String filter,
			String influencerType, String dealerCategory) {
		SclCustomerModel sclCustomerModel=(SclCustomerModel) userService.getCurrentUser();
		BaseSiteModel site = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao1.getInfluencerListForDealerPagination(searchPageData, sclCustomerModel, site, networkType, isNew, filter, influencerType, dealerCategory);
	}
	@Override
	public SearchPageData<SclCustomerModel> getInfluencerListForRetailerPagination(SearchPageData searchPageData, String networkType, boolean isNew, String filter,
			String influencerType, String dealerCategory) {
		SclCustomerModel sclCustomerModel=(SclCustomerModel) userService.getCurrentUser();
		BaseSiteModel site = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao1.getInfluencerListForRetailerPagination(searchPageData, sclCustomerModel, site, networkType, isNew, filter, influencerType, dealerCategory);
	}

	@Override
	public Integer getDealerCountForRetailer() {
		SclCustomerModel currentUser = (SclCustomerModel) userService.getCurrentUser();
		BaseSiteModel currentSite = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao1.getDealerCountForRetailer(currentUser,currentSite);
	}

	@Override
	public Integer getRetailerCountForDealer() {
		SclCustomerModel currentUser = (SclCustomerModel) userService.getCurrentUser();
		BaseSiteModel currentSite = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao1.getRetailerCountForDealer(currentUser,currentSite);
	}

	@Override
	public Integer getInfluencerCountForDealer() {
		SclCustomerModel currentUser = (SclCustomerModel) userService.getCurrentUser();
		BaseSiteModel currentSite = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao1.getInfluencerCountForDealer(currentUser,currentSite);
	}

	@Override
	public SearchPageData<SclCustomerModel> getDealerListForRetailerPagination(SearchPageData searchPageData, String filter) {
		SclCustomerModel currentUser=(SclCustomerModel) userService.getCurrentUser();
		BaseSiteModel currentSite = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao1.getDealerListForRetailerPagination(searchPageData,currentUser,currentSite,filter);
	}

	@Override
	public List<SclCustomerModel> getAllSalesOfficersByTaluka(String taluka) {
		BaseSiteModel site = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao1.getAllSalesOfficersByTaluka(taluka,site) ;
	}

	public List<SubAreaMasterModel> getTaulkaForUser(FilterTalukaData filterTalukaData) {
		B2BCustomerModel currentUser=(B2BCustomerModel) userService.getCurrentUser();
		if(currentUser instanceof SclUserModel) {
			return territoryManagementDao1.getTalukaForUser(filterTalukaData);
		} else if ((((SclCustomerModel) currentUser).getCounterType()!=null) &&
				(((SclCustomerModel) currentUser).getCounterType().equals(CounterType.SP))) {
			return territoryManagementDao1.getTalukaForSP(filterTalukaData);
			
		} else {
			return getTerritoriesForCustomer((SclCustomerModel)currentUser);
		}
	}
	
	@Override
	public List<DistrictMasterModel> getDistrictForUser(FilterDistrictData filterDistrictData) {
		return territoryManagementDao1.getDistrictForUser(filterDistrictData);
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
			customerList = territoryManagementDao1.getCustomerForUser(requestCustomerData, subAreaMasterList);
			return customerList;
		}
		}
		else{
			customerList=territoryManagementDao1.getDealersForSP(requestCustomerData);
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
			return territoryManagementDao1.getPaginatedCustomerForUser(searchPageData, customerData, subAreaMasterList);
		}
		}
		else{
			return territoryManagementDao1.getPaginatedDealersForSP(searchPageData,customerData);
		}
		return null;
	}


	@Override
	public List<RegionMasterModel> getRegionsForUser(FilterRegionData filterRegionData){
		B2BCustomerModel currentUser=(B2BCustomerModel) userService.getCurrentUser();
		List<RegionMasterModel> regionMasterList = null;
		if(currentUser instanceof SclUserModel && currentUser.getUserType()!=null){
			if(currentUser.getUserType().equals(SclUserType.RH)){
				regionMasterList = territoryManagementDao1.getRegionsForRH(filterRegionData);
			}
			else if(currentUser.getUserType().equals(SclUserType.TSM)){
				regionMasterList = territoryManagementDao1.getRegionsForTSM(filterRegionData);
			}
			else if(currentUser.getUserType().equals(SclUserType.SO)){
				regionMasterList = territoryManagementDao1.getRegionsForSO(filterRegionData);
			}
			return regionMasterList;
		}
		return Collections.emptyList();
	}

	
	@Override
	public Integer getInfluencerCountForRetailer() {
		SclCustomerModel currentUser = (SclCustomerModel) userService.getCurrentUser();
		BaseSiteModel currentSite = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao1.getInfluencerCountForRetailer(currentUser,currentSite);
	}

	@Override
	public List<UserSubAreaMappingModel> getSOSubAreaMappingForUser(FilterTalukaData filterTalukaData) {
		return territoryManagementDao1.getSOSubAreaMappingForUser(filterTalukaData);
	}

	@Override
	public List<TsmDistrictMappingModel> getTSMDistrcitMappingForUser(FilterDistrictData filterDistrictData) {
		return territoryManagementDao1.getTSMDistrcitMappingForUser(filterDistrictData);
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
		List<TsmDistrictMappingModel> tsmMappingList = territoryManagementDao1.getTSMDistrcitMappingForUser(filterDistrictData);
		if(tsmMappingList!=null && !tsmMappingList.isEmpty()) {
			sclUserList = tsmMappingList.stream().map(each->each.getTsmUser()).distinct().collect(Collectors.toList());
		}
		return sclUserList;
	}

	@Override
	public List<SclUserModel> getSOForUser(FilterTalukaData filterTalukaData) {
		List<SclUserModel> sclUserList = new ArrayList<>();
		B2BCustomerModel currentUser=(B2BCustomerModel) userService.getCurrentUser();
		if(currentUser instanceof SclUserModel){
			List<UserSubAreaMappingModel> soMappingList = territoryManagementDao1.getSOSubAreaMappingForUser(filterTalukaData);
			if(soMappingList!=null && !soMappingList.isEmpty()) {
				sclUserList = soMappingList.stream().map(each -> each.getSclUser()).distinct().collect(Collectors.toList());
			}
		}
		else if ((((SclCustomerModel) currentUser).getCounterType()!=null) &&
				(((SclCustomerModel) currentUser).getCounterType().equals(CounterType.SP))) {
			sclUserList = territoryManagementDao1.getSOForSP(filterTalukaData);
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
		return territoryManagementDao1.getSalesOfficerforTaluka(taluka,brand);
	}


	@Override
    public List<DistrictMasterModel> getDistrictForSP(FilterDistrictData filterDistrictData) {
		return territoryManagementDao1.getDistrictForSP(filterDistrictData);
    }

	@Override
	public CounterVisitMasterModel findCounterVisitForUnFlaggedDealer(SclCustomerModel sclCustomer, boolean isUnFlagRequsetRaised, Date unFlagtime, String remarkForUnFlag) {
		return territoryManagementDao1.findCounterVisitForUnFlaggedDealer(sclCustomer,isUnFlagRequsetRaised,unFlagtime,remarkForUnFlag);
	}

    @Override
    public SclCustomerModel getSpForCustomer(SclCustomerModel dealer) {
		return territoryManagementDao1.getSPForCustomer(dealer);
		}

	@Override
	public SclCustomerModel getSpForDealer(SclCustomerModel dealer, BaseSiteModel currentBaseSite) {
		return territoryManagementDao1.getSpForDealer(dealer,currentBaseSite);
	}

	@Override
	public SclCustomerModel getSpForCustomerAndBrand(SclCustomerModel dealer, BaseSiteModel brand) {
		return territoryManagementDao1.getSPForCustomerAndBrand(dealer,brand);
	}

	@Override
	public List<SclUserModel> getTSMforDistrict(DistrictMasterModel district, BaseSiteModel brand) {
		return territoryManagementDao1.getTSMforDistrict(district,brand);
	}

	@Override
	public List<SclUserModel> getRHforRegion(RegionMasterModel region, BaseSiteModel brand) {
		return territoryManagementDao1.getRHforRegion(region,brand);
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

}