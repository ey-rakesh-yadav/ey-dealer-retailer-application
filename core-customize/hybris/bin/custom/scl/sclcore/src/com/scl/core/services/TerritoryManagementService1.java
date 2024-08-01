package com.scl.core.services;

import com.scl.core.model.*;
import com.scl.facades.data.FilterDistrictData;
import com.scl.facades.data.FilterRegionData;
import com.scl.facades.data.FilterTalukaData;
import com.scl.facades.data.RequestCustomerData;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface TerritoryManagementService1 {

	List<String> getAllSubAreaForSO(String userId);
	List<String> getAllSubAreaForCustomer(String customerId);
	List<SclCustomerModel> getAllCustomerForSubArea(String subArea);
	List<SclCustomerModel> getDealersForSubArea();
	List<SclCustomerModel> getRetailersForSubArea();
	List<SclCustomerModel> getDealersForSubArea(String subAreaMaster);

	List<SclCustomerModel> getAllSclAndNonSclCustomerForTerritories(String subAreaMaster);

	List<SclCustomerModel> getAllSclAndNonSclCustomerForTerritory(List<SubAreaMasterModel> subAreas);
	List<SclCustomerModel> getRetailersForSubArea(String subAreaMaster);
	List<SclCustomerModel> getInfluencersForSubArea();

	List<SclCustomerModel> getInfluencersForSubArea(String subAreaMaster);

	SearchPageData<SclCustomerModel> getPaginatedInfluencersForSubArea(SearchPageData searchPageData);
	List<SclCustomerModel> getSitesForSubArea();

	List<SclCustomerModel> getSitesForSubAreaSO();
	List<SclCustomerModel> getSalesPromotersForSubArea();
	List<SclCustomerModel> getAllRetailersForSubAreaTOP(String subArea, String dealerCode);
	List<SclCustomerModel> getDealerRetailerInfluencerForSubArea();
	List<SclCustomerModel> getSCLAndNonSCLDealersRetailersForSubArea();
	 List<SclCustomerModel> getSCLAndNonSCLDealersRetailersForSubArea(String LeadType);
	List<List<Object>> getAllTerritoryForSO();
	List<String> getAllDistrictForSO(UserModel sclUser);
	List<SclUserModel> getAllUserForDistrict(List<String> districts);

    List<SclUserModel> getAllSalesOfficersByState(String state);
	List<SclCustomerModel> getAllSalesOfficersByDistrict(String district);

    List<String> getAllStatesForSO();
    
	List<SubAreaMasterModel> getTerritoriesForSO();
	List<SubAreaMasterModel> getTerritoriesForSO(String uid);
	List<SubAreaMasterModel> getTerritoriesForCustomer(String customerId);
	List<SubAreaMasterModel> getTerritoriesForCustomer(SclCustomerModel customer);
	List<SubAreaMasterModel> getTerritoriesForPromoter(SclCustomerModel customer);
	List<SclCustomerModel> getAllCustomerForSubArea(List<SubAreaMasterModel> subAreas);

	List<SclCustomerModel> getAllCustomerForSubAreaSO(List<SubAreaMasterModel> subAreas);
	SubAreaMasterModel getTerritoryById(String territoryId);
	List<SclCustomerModel> getAllCustomerForSO();
	List<SclCustomerModel> getAllCustomerForSO(String uid);
	List<SclCustomerModel> getSCLAndNonSCLAllForSO();
	List<SclCustomerModel> getSCLAndNonSCLAllForSubArea(List<SubAreaMasterModel> subSreas);
	SubAreaMasterModel getTerritoryByDistrictAndTaluka(String district, String taluka);
	Collection<SubAreaMasterModel> getCurrentTerritory();
	void setCurrentTerritory(Collection<SubAreaMasterModel> subAreas);
	SubAreaMasterModel getTerritoryByTaluka(String taluka);
	SclUserModel getSOforCustomer(SclCustomerModel customer);

	List<SubAreaMasterModel> getSubAreaForDealers(List<SclCustomerModel> dealers);

	List<SclUserModel> getUsersForSubAreas(List<SubAreaMasterModel> subAreaMasters);
	List<SclCustomerModel> getRetailerListForDealer();
	List<SclCustomerModel> getInfluencerListForDealer();
	List<SclCustomerModel> getInfluencerListForRetailer();

	SearchPageData<SclCustomerModel> getCustomerByTerritoriesAndCounterType(SearchPageData searchPageData,
			List<SubAreaMasterModel> subAreaMaster, String counterType, String networkType, boolean isNew,
			String filter, String influencerType, String dealerCategory);
	List<SclCustomerModel> getCustomerByTerritoriesAndCounterTypeWithoutPagination(String counterType, String networkType,
																  String filter, String influencerType, String dealerCategory,boolean isNew);

	SearchPageData<SclCustomerModel> getCustomerByTerritoriesAndCounterType(SearchPageData searchPageData,
			String counterType, String networkType, boolean isNew, String filter, String influencerType,
			String dealerCategory);

	SearchPageData<SclCustomerModel> getInfluencerListForRetailerPagination(SearchPageData searchPageData,
			String networkType, boolean isNew, String filter, String influencerType, String dealerCategory);
	SearchPageData<SclCustomerModel> getInfluencerListForDealerPagination(SearchPageData searchPageData,
			String networkType, boolean isNew, String filter, String influencerType, String dealerCategory);
	SearchPageData<SclCustomerModel> getRetailerListForDealerPagination(SearchPageData searchPageData,
			String networkType, boolean isNew, String filter, Boolean isTop);
	Integer getDealerCountForRetailer();

	Integer getRetailerCountForDealer();

	Integer getInfluencerCountForDealer();

	SearchPageData<SclCustomerModel> getDealerListForRetailerPagination(SearchPageData searchPageData, String filter);
	List<DistrictMasterModel> getDistrictForUser(FilterDistrictData filterDistrictData);
	List<SubAreaMasterModel> getTaulkaForUser(FilterTalukaData filterTalukaData);
	List<SclCustomerModel> getCustomerforUser(RequestCustomerData requestCustomerData);

	List<SclCustomerModel> getAllSalesOfficersByTaluka(String taluka);

	List<RegionMasterModel> getRegionsForUser(FilterRegionData filterRegionData);

	Integer getInfluencerCountForRetailer();
	List<UserSubAreaMappingModel> getSOSubAreaMappingForUser(FilterTalukaData filterTalukaData);
	List<TsmDistrictMappingModel> getTSMDistrcitMappingForUser(FilterDistrictData filterDistrictData);
	Collection<DistrictMasterModel> getCurrentDistrict();

	void setCurrentDistrict(Collection<DistrictMasterModel> districts);

	Collection<RegionMasterModel> getCurrentRegion();

	void setCurrentRegion(Collection<RegionMasterModel> regions);
	SearchPageData<SclCustomerModel> getCustomerForUserPagination(SearchPageData searchPageData,
			RequestCustomerData customerData);
	List<SclUserModel> getTSMForUser(FilterDistrictData filterDistrictData);
	List<SclUserModel> getSOForUser(FilterTalukaData filterTalukaData);
	CustDepotMasterModel getCustDepotForCustomer(SclCustomerModel customer);
	CustDepotMasterModel getCustDepotForCode(String code);
	List<CustDepotMasterModel> getCustDepotForSP(B2BCustomerModel sp);
	void setCurrentCustDepot(Collection<CustDepotMasterModel> custDepotModelList);
	Collection<CustDepotMasterModel> getCurrentCustDepot();
	List<CustDepotMasterModel> getCustDepotForSPInLocalView(B2BCustomerModel sp);
	SclUserModel getSalesOfficerforTaluka(String taluka, BaseSiteModel brand);

	List<DistrictMasterModel> getDistrictForSP(FilterDistrictData filterDistrictData);
	CounterVisitMasterModel findCounterVisitForUnFlaggedDealer(SclCustomerModel sclCustomer, boolean isUnFlagRequsetRaised, Date unFlagtime, String remarkForUnFlag);

	SclCustomerModel getSpForCustomer(SclCustomerModel dealer);

	SclCustomerModel getSpForDealer(SclCustomerModel dealer, BaseSiteModel currentBaseSite);

	SclCustomerModel getSpForCustomerAndBrand(SclCustomerModel dealer, BaseSiteModel brand);

	List<SclUserModel> getTSMforDistrict(DistrictMasterModel district,BaseSiteModel brand);

	List<SclUserModel> getRHforRegion(RegionMasterModel region, BaseSiteModel brand);
}