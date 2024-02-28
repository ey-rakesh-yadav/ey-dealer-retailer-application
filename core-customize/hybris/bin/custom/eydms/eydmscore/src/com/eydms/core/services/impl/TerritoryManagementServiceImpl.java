package com.eydms.core.services.impl;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.dao.TerritoryManagementDao;
import com.eydms.core.enums.CounterType;
import com.eydms.core.enums.EyDmsUserType;
import com.eydms.core.jalo.EyDmsCustomer;
import com.eydms.core.jalo.EyDmsUser;
import com.eydms.core.model.*;
import com.eydms.core.region.dao.DistrictMasterDao;
import com.eydms.core.region.dao.RegionMasterDao;
import com.eydms.core.services.TerritoryManagementService;
import com.eydms.facades.data.FilterDistrictData;
import com.eydms.facades.data.FilterRegionData;
import com.eydms.facades.data.FilterTalukaData;
import com.eydms.facades.data.RequestCustomerData;

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

public class TerritoryManagementServiceImpl implements TerritoryManagementService {

	private static final Logger LOG = Logger.getLogger(TerritoryManagementServiceImpl.class);

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
	public List<EyDmsCustomerModel> getDealersForSubArea() {
//		List<SubAreaMasterModel> list = new ArrayList<SubAreaMasterModel>();
//		UserModel user = userService.getCurrentUser();
//		if(user instanceof EyDmsUserModel) {
//			list = getTerritoriesForSO();
//		}
//		else if(user instanceof EyDmsCustomerModel){
//			list = getTerritoriesForCustomer((EyDmsCustomerModel)user);
//		}
//		List<EyDmsCustomerModel> dealerList = getAllCustomerForSubArea(list).stream().filter(d -> (d.getGroups()).contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList());
//		
		RequestCustomerData requestCustomerData = new RequestCustomerData();
		requestCustomerData.setCounterType(List.of(CounterType.DEALER.getCode()));
		List<EyDmsCustomerModel> dealerList = getCustomerforUser(requestCustomerData);
		return dealerList!=null && !dealerList.isEmpty() ? dealerList :Collections.emptyList() ;

	}

	//New Territory Change
	@Override
	public List<EyDmsCustomerModel> getEYDMSAndNonEYDMSDealersRetailersForSubArea() {
		UserGroupModel dealerGroup = userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID);
		UserGroupModel retailerGroup = userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID);

		List<EyDmsCustomerModel> dealerRetailerList = territoryManagementDao.getEYDMSAndNonEYDMSCustomerForSubArea(getTerritoriesForSO()).stream().filter(d->d!=null && d.getGroups()!=null && ((d.getGroups()).contains(dealerGroup) || (d.getGroups()).contains(retailerGroup))).collect(Collectors.toList());
		return dealerRetailerList;
	}
	@Override
	public List<EyDmsCustomerModel> getEYDMSAndNonEYDMSDealersRetailersForSubArea(String LeadType) {
		List<EyDmsCustomerModel> dealerRetailerList =null;
		if(LeadType.equalsIgnoreCase("Dealer")) {
			UserGroupModel dealerGroup = userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID);
			dealerRetailerList = territoryManagementDao.getEYDMSAndNonEYDMSCustomerForSubArea(getTerritoriesForSO()).stream().filter(d -> (d.getGroups()).contains(dealerGroup)).collect(Collectors.toList());

		}
		else if(LeadType.equalsIgnoreCase("Retailer")) {
			UserGroupModel retailerGroup = userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID);
			dealerRetailerList = territoryManagementDao.getEYDMSAndNonEYDMSCustomerForSubArea(getTerritoriesForSO()).stream().filter(d->(d.getGroups()).contains(retailerGroup)).collect(Collectors.toList());

		}
		return dealerRetailerList;
	}

	//New Territory Change
	@Override
	public List<EyDmsCustomerModel> getEYDMSAndNonEYDMSAllForSO() {
		List<EyDmsCustomerModel> dealerRetailerList = getEYDMSAndNonEYDMSAllForSubArea(getTerritoriesForSO());
		LOG.info(String.format("dealerRetailerList :: %s ",dealerRetailerList.size()));
		return dealerRetailerList;
	}

	//New Territory Change
	@Override
	public List<EyDmsCustomerModel> getEYDMSAndNonEYDMSAllForSubArea(List<SubAreaMasterModel> subSreas) {
		List<EyDmsCustomerModel> dealerRetailerList = territoryManagementDao.getEYDMSAndNonEYDMSCustomerForSubArea(subSreas);
		return dealerRetailerList;
	}

	//New Territory Change
	@Override
	public List<EyDmsCustomerModel> getDealerRetailerInfluencerForSubArea() {
		RequestCustomerData requestCustomerData = new RequestCustomerData();
		requestCustomerData.setCounterType(List.of(CounterType.DEALER.getCode(),CounterType.RETAILER.getCode(),CounterType.INFLUENCER.getCode()));
		List<EyDmsCustomerModel> resultList = getCustomerforUser(requestCustomerData);
		return resultList!=null && !resultList.isEmpty() ? resultList :Collections.emptyList() ;
//		List<EyDmsCustomerModel> resultList = getAllCustomerForSubArea(getTerritoriesForSO()).stream().filter(d->!(d.getGroups()).contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.SITE_USER_GROUP_UID))).collect(Collectors.toList());
//		return resultList;
	}

	//New Territory Change
	@Override
	public List<EyDmsCustomerModel> getRetailersForSubArea() {
		RequestCustomerData requestCustomerData = new RequestCustomerData();
		requestCustomerData.setCounterType(List.of(CounterType.RETAILER.getCode()));
		List<EyDmsCustomerModel> retailerList = getCustomerforUser(requestCustomerData);
		return retailerList!=null && !retailerList.isEmpty() ? retailerList :Collections.emptyList() ;
//		List<EyDmsCustomerModel> resultList = getAllCustomerForSubArea(getTerritoriesForSO()).stream().filter(r->(r.getGroups()).contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))).collect(Collectors.toList());
//		return resultList;
	}

	@Override
	public List<EyDmsCustomerModel> getDealersForSubArea(String subAreaMaster) {
		RequestCustomerData requestCustomerData = new RequestCustomerData();
		requestCustomerData.setCounterType(List.of(CounterType.DEALER.getCode()));
		requestCustomerData.setSubAreaMasterPk(subAreaMaster);
		List<EyDmsCustomerModel> dealerList = getCustomerforUser(requestCustomerData);
		return dealerList!=null && !dealerList.isEmpty() ? dealerList :Collections.emptyList() ;
//		return getAllCustomerForSubArea(List.of(getTerritoryById(subAreaMaster))).stream().filter(d->(d.getGroups()).contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList());
	}


	@Override
	public List<EyDmsCustomerModel> getRetailersForSubArea(String subAreaMaster) {
		RequestCustomerData requestCustomerData = new RequestCustomerData();
		requestCustomerData.setCounterType(List.of(CounterType.RETAILER.getCode()));
		requestCustomerData.setSubAreaMasterPk(subAreaMaster);
		List<EyDmsCustomerModel> retailerList = getCustomerforUser(requestCustomerData);
		return retailerList!=null && !retailerList.isEmpty() ? retailerList :Collections.emptyList() ;
//		return getAllCustomerForSubArea(List.of(getTerritoryById(subAreaMaster))).stream().filter(r->(r.getGroups()).contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))).collect(Collectors.toList());
	}

	@Override
	public List<EyDmsCustomerModel> getInfluencersForSubArea(String subAreaMaster) {
		RequestCustomerData requestCustomerData = new RequestCustomerData();
		requestCustomerData.setCounterType(List.of(CounterType.INFLUENCER.getCode()));
		requestCustomerData.setSubAreaMasterPk(subAreaMaster);
		List<EyDmsCustomerModel> influencerList = getCustomerforUser(requestCustomerData);
		return influencerList!=null && !influencerList.isEmpty() ? influencerList :Collections.emptyList() ;
		//return getAllCustomerForSubArea(List.of(getTerritoryById(subAreaMaster))).stream().filter(r->(r.getGroups()).contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID))).collect(Collectors.toList());
	}

	//New Territory Change
	@Override
	public List<EyDmsCustomerModel> getInfluencersForSubArea() {
		RequestCustomerData requestCustomerData = new RequestCustomerData();
		requestCustomerData.setCounterType(List.of(CounterType.INFLUENCER.getCode()));
		List<EyDmsCustomerModel> influencerList = getCustomerforUser(requestCustomerData);
		return influencerList!=null && !influencerList.isEmpty() ? influencerList :Collections.emptyList() ;
//		List<EyDmsCustomerModel> resultList = getAllCustomerForSubArea(getTerritoriesForSO()).stream().filter(i->(i.getGroups()).contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID))).collect(Collectors.toList());
//		return resultList;
	}


	//New Territory Change
	@Override
	public List<EyDmsCustomerModel> getSitesForSubArea() {
		RequestCustomerData requestCustomerData = new RequestCustomerData();
		requestCustomerData.setCounterType(List.of(CounterType.SITE.getCode()));
		List<EyDmsCustomerModel> siteList = getCustomerforUser(requestCustomerData);
		return siteList!=null && !siteList.isEmpty() ? siteList :Collections.emptyList() ;
//		List<EyDmsCustomerModel> resultList = getAllCustomerForSubArea(getTerritoriesForSO()).stream().filter(s->(s.getGroups()).contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.SITE_USER_GROUP_UID))).collect(Collectors.toList());
//		return resultList;
	}

	@Override
	public List<EyDmsCustomerModel> getSitesForSubAreaSO() {
		RequestCustomerData requestCustomerData = new RequestCustomerData();
		requestCustomerData.setCounterType(List.of(CounterType.SITE.getCode()));
		List<EyDmsCustomerModel> siteList = getCustomerforUser(requestCustomerData);
		return siteList!=null && !siteList.isEmpty() ? siteList :Collections.emptyList() ;
//		List<EyDmsCustomerModel> resultList = getAllCustomerForSubAreaSO(getTerritoriesForSO()).stream().filter(s->(s.getGroups()).contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.SITE_USER_GROUP_UID))).collect(Collectors.toList());
//		return resultList;
	}

	@Override
	public List<EyDmsCustomerModel> getSalesPromotersForSubArea() {
		return	getAllCustomerForSubArea(getTerritoriesForSO()).stream().filter(s->(s.getGroups()).contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.SALES_PROMOTER_USER_GROUP_UID))).collect(Collectors.toList());
	}


	@Override
	public 	List<EyDmsCustomerModel> getAllRetailersForSubAreaTOP(String subArea, String dealerCode){
		BaseSiteModel site = baseSiteService.getCurrentBaseSite();
		B2BCustomerModel currentUser=(B2BCustomerModel) userService.getCurrentUser();
		List<SubAreaMasterModel> subAreaMasterList  = new ArrayList<SubAreaMasterModel>();
		if(StringUtils.isBlank(subArea)) {
			if(currentUser instanceof EyDmsUserModel) {
				FilterTalukaData filterTalukaData = new FilterTalukaData();
				subAreaMasterList  =  getTaulkaForUser(filterTalukaData);
			}
			else if(currentUser instanceof EyDmsCustomerModel){
				EyDmsUserModel eydmsUserModel = getSOforCustomer((EyDmsCustomerModel) currentUser);
				subAreaMasterList  =  getTerritoriesForSO(eydmsUserModel.getUid());
			}
		}
		else {
			subAreaMasterList.add(getTerritoryById(subArea));
		}

		List<EyDmsCustomerModel> resultList = territoryManagementDao.getAllRetailersForSubAreaTOP(subAreaMasterList, site,dealerCode).stream().filter(r->(r.getGroups()).contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))).collect(Collectors.toList());
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
	public List<EyDmsUserModel> getAllSalesOfficersByState(String state) {
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
	public List<SubAreaMasterModel> getTerritoriesForSO(String uid) {
		return territoryManagementDao.getTerritoriesForSO((EyDmsUserModel) userService.getUserForUID(uid));
	}

	@Override
	public List<SubAreaMasterModel> getTerritoriesForCustomer(String customerId) {
		return getTerritoriesForCustomer((EyDmsCustomerModel)userService.getUserForUID(customerId));
	}

	@Override
	public List<SubAreaMasterModel> getTerritoriesForCustomer(EyDmsCustomerModel customer) {
		return territoryManagementDao.getTerritoriesForCustomer(customer);
	}



	@Override
	public List<SubAreaMasterModel> getTerritoryForCustWithAllBrands(EyDmsCustomerModel customer) {
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
	public List<SubAreaMasterModel> getTerritoriesForPromoter(EyDmsCustomerModel customer) {
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
	public List<EyDmsCustomerModel> getAllCustomerForSO() {
		List<EyDmsCustomerModel> customerList = new ArrayList<EyDmsCustomerModel>();
		var subareaMaster=getTerritoriesForSO();
		if(CollectionUtils.isNotEmpty(subareaMaster)) {
			RequestCustomerData requestCustomerData = new RequestCustomerData();
			customerList = territoryManagementDao.getCustomerForUser(requestCustomerData, subareaMaster);
		}
		return customerList;
	}

	@Override
	public List<EyDmsCustomerModel> getAllCustomerForSO(String uid) {
		List<EyDmsCustomerModel> customerList = new ArrayList<>();
		var subareaMaster=getTerritoriesForSO(uid);
		if(CollectionUtils.isNotEmpty(subareaMaster)) {
			RequestCustomerData requestCustomerData = new RequestCustomerData();
			customerList = territoryManagementDao.getCustomerForUser(requestCustomerData, subareaMaster);
		}
		return customerList;
	}

	@Override
	public List<EyDmsCustomerModel> getAllCustomerForSubArea(List<SubAreaMasterModel> subAreas) {
		List<EyDmsCustomerModel> customerList = new ArrayList<EyDmsCustomerModel>();
		if(subAreas!=null && !subAreas.isEmpty()) {
			RequestCustomerData requestCustomerData = new RequestCustomerData();
			customerList = territoryManagementDao.getCustomerForUser(requestCustomerData, subAreas);
		}
		return customerList;
	}

	@Override
	public List<EyDmsCustomerModel> getAllCustomerForSubAreaSO(List<SubAreaMasterModel> subAreas) {
		List<EyDmsCustomerModel> customerList = new ArrayList<EyDmsCustomerModel>();
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
			if( userService.getCurrentUser() instanceof EyDmsUserModel) {
				list.addAll(territoryManagementDao.getTerritoriesForSOInLocalView());
			}
			else if(userService.getCurrentUser() instanceof EyDmsCustomerModel) {
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
	public EyDmsUserModel getSOforCustomer(EyDmsCustomerModel customer) {
		return territoryManagementDao.getSOForSubArea(customer);
	}

	@Override
	public List<EyDmsUserModel> getUsersForSubAreas(List<SubAreaMasterModel> subAreaMasters) {
		return territoryManagementDao.getEyDmsUsersForSubArea(subAreaMasters);
	}

	@Override
	public CustDepotMasterModel getCustDepotForCustomer(EyDmsCustomerModel customer) {
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
	public List<EyDmsCustomerModel> getRetailerListForDealer() {
		EyDmsCustomerModel eydmsCustomerModel=(EyDmsCustomerModel) userService.getCurrentUser();
		BaseSiteModel site = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao.getRetailerListForDealer(eydmsCustomerModel,site);
	}

	@Override
	public List<EyDmsCustomerModel> getInfluencerListForDealer() {
		EyDmsCustomerModel eydmsCustomerModel=(EyDmsCustomerModel) userService.getCurrentUser();
		BaseSiteModel site = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao.getInfluencerListForDealer(eydmsCustomerModel,site);
	}

	@Override
	public List<EyDmsCustomerModel> getInfluencerListForRetailer() {
		EyDmsCustomerModel eydmsCustomerModel=(EyDmsCustomerModel) userService.getCurrentUser();
		BaseSiteModel site = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao.getInfluencerListForRetailer(eydmsCustomerModel,site);
	}

	@Override
	public SearchPageData<EyDmsCustomerModel> getRetailerListForDealerPagination(SearchPageData searchPageData, String networkType, boolean isNew, String filter, Boolean isTop) {
		EyDmsCustomerModel eydmsCustomerModel=(EyDmsCustomerModel) userService.getCurrentUser();
		BaseSiteModel site = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao.getRetailerListForDealerPagination(searchPageData, eydmsCustomerModel, site, networkType, isNew, filter, isTop);
	}
	@Override
	public SearchPageData<EyDmsCustomerModel> getInfluencerListForDealerPagination(SearchPageData searchPageData, String networkType, boolean isNew, String filter,
																				 String influencerType, String dealerCategory) {
		EyDmsCustomerModel eydmsCustomerModel=(EyDmsCustomerModel) userService.getCurrentUser();
		BaseSiteModel site = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao.getInfluencerListForDealerPagination(searchPageData, eydmsCustomerModel, site, networkType, isNew, filter, influencerType, dealerCategory);
	}
	@Override
	public SearchPageData<EyDmsCustomerModel> getInfluencerListForRetailerPagination(SearchPageData searchPageData, String networkType, boolean isNew, String filter,
																				   String influencerType, String dealerCategory) {
		EyDmsCustomerModel eydmsCustomerModel=(EyDmsCustomerModel) userService.getCurrentUser();
		BaseSiteModel site = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao.getInfluencerListForRetailerPagination(searchPageData, eydmsCustomerModel, site, networkType, isNew, filter, influencerType, dealerCategory);
	}

	@Override
	public Integer getDealerCountForRetailer() {
		EyDmsCustomerModel currentUser = (EyDmsCustomerModel) userService.getCurrentUser();
		BaseSiteModel currentSite = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao.getDealerCountForRetailer(currentUser,currentSite);
	}

	@Override
	public Integer getRetailerCountForDealer() {
		EyDmsCustomerModel currentUser = (EyDmsCustomerModel) userService.getCurrentUser();
		BaseSiteModel currentSite = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao.getRetailerCountForDealer(currentUser,currentSite);
	}

	@Override
	public Integer getInfluencerCountForDealer() {
		EyDmsCustomerModel currentUser = (EyDmsCustomerModel) userService.getCurrentUser();
		BaseSiteModel currentSite = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao.getInfluencerCountForDealer(currentUser,currentSite);
	}

	@Override
	public SearchPageData<EyDmsCustomerModel> getDealerListForRetailerPagination(SearchPageData searchPageData, String filter) {
		EyDmsCustomerModel currentUser=(EyDmsCustomerModel) userService.getCurrentUser();
		BaseSiteModel currentSite = baseSiteService.getCurrentBaseSite();
		return territoryManagementDao.getDealerListForRetailerPagination(searchPageData,currentUser,currentSite,filter);
	}


	public List<SubAreaMasterModel> getTaulkaForUser(FilterTalukaData filterTalukaData) {
		B2BCustomerModel currentUser=(B2BCustomerModel) userService.getCurrentUser();
		if(currentUser instanceof EyDmsUserModel) {
			return territoryManagementDao.getTalukaForUser(filterTalukaData);
		} else if ((((EyDmsCustomerModel) currentUser).getCounterType()!=null) &&
				(((EyDmsCustomerModel) currentUser).getCounterType().equals(CounterType.SP))) {
			return territoryManagementDao.getTalukaForSP(filterTalukaData);

		} else {
			return getTerritoriesForCustomer((EyDmsCustomerModel)currentUser);
		}
	}

	@Override
	public List<DistrictMasterModel> getDistrictForUser(FilterDistrictData filterDistrictData) {
		return territoryManagementDao.getDistrictForUser(filterDistrictData);
	}

	@Override
	public List<EyDmsCustomerModel> getCustomerforUser(RequestCustomerData requestCustomerData){
		B2BCustomerModel currentUser=(B2BCustomerModel) userService.getCurrentUser();
		List<EyDmsCustomerModel> customerList = null;
		if(currentUser instanceof EyDmsUserModel ||
				(((EyDmsCustomerModel) currentUser).getCounterType()==null) ||
				(( !((EyDmsCustomerModel) currentUser).getCounterType().equals(CounterType.SP)))){
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
	public SearchPageData<EyDmsCustomerModel> getCustomerForUserPagination(SearchPageData searchPageData,
																		 RequestCustomerData customerData) {
		B2BCustomerModel currentUser=(B2BCustomerModel) userService.getCurrentUser();
		if(currentUser instanceof EyDmsUserModel ||
				(((EyDmsCustomerModel) currentUser).getCounterType()==null) ||
				((!((EyDmsCustomerModel) currentUser).getCounterType().equals(CounterType.SP)))){
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
		if(currentUser instanceof EyDmsUserModel && currentUser.getUserType()!=null){
			if(currentUser.getUserType().equals(EyDmsUserType.RH)){
				regionMasterList = territoryManagementDao.getRegionsForRH(filterRegionData);
			}
			else if(currentUser.getUserType().equals(EyDmsUserType.TSM)){
				regionMasterList = territoryManagementDao.getRegionsForTSM(filterRegionData);
			}
			else if(currentUser.getUserType().equals(EyDmsUserType.SO)){
				regionMasterList = territoryManagementDao.getRegionsForSO(filterRegionData);
			}
			return regionMasterList;
		}
		return Collections.emptyList();
	}


	@Override
	public Integer getInfluencerCountForRetailer() {
		EyDmsCustomerModel currentUser = (EyDmsCustomerModel) userService.getCurrentUser();
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
			if(userService.getCurrentUser() instanceof EyDmsUserModel) {
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
			if(userService.getCurrentUser() instanceof EyDmsUserModel) {
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
	public List<EyDmsUserModel> getTSMForUser(FilterDistrictData filterDistrictData) {
		List<EyDmsUserModel> eydmsUserList = new ArrayList<>();
		List<TsmDistrictMappingModel> tsmMappingList = territoryManagementDao.getTSMDistrcitMappingForUser(filterDistrictData);
		if(tsmMappingList!=null && !tsmMappingList.isEmpty()) {
			eydmsUserList = tsmMappingList.stream().map(each->each.getTsmUser()).distinct().collect(Collectors.toList());
		}
		return eydmsUserList;
	}

	@Override
	public List<EyDmsUserModel> getSOForUser(FilterTalukaData filterTalukaData) {
		List<EyDmsUserModel> eydmsUserList = new ArrayList<>();
		B2BCustomerModel currentUser=(B2BCustomerModel) userService.getCurrentUser();
		//for TSO Linked SO List
		if (currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.TSO_GROUP_ID))){
			List<SubAreaMasterModel> taulkaForUser = getTaulkaForUser(filterTalukaData);
			if(taulkaForUser!=null && !taulkaForUser.isEmpty()) {
				eydmsUserList = getUsersForSubAreas(taulkaForUser);
			}
		}
		else if(currentUser instanceof EyDmsUserModel){
			List<UserSubAreaMappingModel> soMappingList = territoryManagementDao.getSOSubAreaMappingForUser(filterTalukaData);
			if(soMappingList!=null && !soMappingList.isEmpty()) {
				eydmsUserList = soMappingList.stream().map(each -> each.getEyDmsUser()).distinct().collect(Collectors.toList());
			}
		}
		else if ((((EyDmsCustomerModel) currentUser).getCounterType()!=null) &&
				(((EyDmsCustomerModel) currentUser).getCounterType().equals(CounterType.SP))) {
			eydmsUserList = territoryManagementDao.getSOForSP(filterTalukaData);
		}

		return eydmsUserList;
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
	public EyDmsUserModel getSalesOfficerforTaluka(String taluka, BaseSiteModel brand) {
		return territoryManagementDao.getSalesOfficerforTaluka(taluka,brand);
	}


	@Override
	public List<DistrictMasterModel> getDistrictForSP(FilterDistrictData filterDistrictData) {
		return territoryManagementDao.getDistrictForSP(filterDistrictData);
	}

	@Override
	public CounterVisitMasterModel findCounterVisitForUnFlaggedDealer(EyDmsCustomerModel eydmsCustomer, boolean isUnFlagRequsetRaised, Date unFlagtime, String remarkForUnFlag) {
		return territoryManagementDao.findCounterVisitForUnFlaggedDealer(eydmsCustomer,isUnFlagRequsetRaised,unFlagtime,remarkForUnFlag);
	}

	@Override
	public EyDmsCustomerModel getSpForCustomer(EyDmsCustomerModel dealer) {
		return territoryManagementDao.getSPForCustomer(dealer);
	}

	@Override
	public EyDmsCustomerModel getSpForCustomerAndBrand(EyDmsCustomerModel dealer, BaseSiteModel brand) {
		return territoryManagementDao.getSPForCustomerAndBrand(dealer,brand);
	}

	@Override
	public List<EyDmsUserModel> getTSMforDistrict(DistrictMasterModel district, BaseSiteModel brand) {
		return territoryManagementDao.getTSMforDistrict(district,brand);
	}

	@Override
	public List<EyDmsUserModel> getRHforRegion(RegionMasterModel region, BaseSiteModel brand) {
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
	public List<EyDmsUserModel> getTSOforSubArea(SubAreaMasterModel subArea) {
		return territoryManagementDao.getTSOforSubArea(subArea);
	}

	@Override
	public List<EyDmsUserModel> getTSOforSubAreas(List<SubAreaMasterModel> subArea) {
		return territoryManagementDao.getTSOforSubAreas(subArea);
	}
}