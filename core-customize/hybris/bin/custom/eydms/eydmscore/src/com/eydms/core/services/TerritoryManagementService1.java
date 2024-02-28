package com.eydms.core.services;

import com.eydms.core.model.*;
import com.eydms.facades.data.FilterDistrictData;
import com.eydms.facades.data.FilterRegionData;
import com.eydms.facades.data.FilterTalukaData;
import com.eydms.facades.data.RequestCustomerData;

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
	List<EyDmsCustomerModel> getAllCustomerForSubArea(String subArea);
	List<EyDmsCustomerModel> getDealersForSubArea();
	List<EyDmsCustomerModel> getRetailersForSubArea();
	List<EyDmsCustomerModel> getDealersForSubArea(String subAreaMaster);

	List<EyDmsCustomerModel> getAllEyDmsAndNonEyDmsCustomerForTerritories(String subAreaMaster);

	List<EyDmsCustomerModel> getAllEyDmsAndNonEyDmsCustomerForTerritory(List<SubAreaMasterModel> subAreas);
	List<EyDmsCustomerModel> getRetailersForSubArea(String subAreaMaster);
	List<EyDmsCustomerModel> getInfluencersForSubArea();

	List<EyDmsCustomerModel> getInfluencersForSubArea(String subAreaMaster);

	SearchPageData<EyDmsCustomerModel> getPaginatedInfluencersForSubArea(SearchPageData searchPageData);
	List<EyDmsCustomerModel> getSitesForSubArea();

	List<EyDmsCustomerModel> getSitesForSubAreaSO();
	List<EyDmsCustomerModel> getSalesPromotersForSubArea();
	List<EyDmsCustomerModel> getAllRetailersForSubAreaTOP(String subArea, String dealerCode);
	List<EyDmsCustomerModel> getDealerRetailerInfluencerForSubArea();
	List<EyDmsCustomerModel> getEYDMSAndNonEYDMSDealersRetailersForSubArea();
	 List<EyDmsCustomerModel> getEYDMSAndNonEYDMSDealersRetailersForSubArea(String LeadType);
	List<List<Object>> getAllTerritoryForSO();
	List<String> getAllDistrictForSO(UserModel eydmsUser);
	List<EyDmsUserModel> getAllUserForDistrict(List<String> districts);

    List<EyDmsUserModel> getAllSalesOfficersByState(String state);
	List<EyDmsCustomerModel> getAllSalesOfficersByDistrict(String district);

    List<String> getAllStatesForSO();
    
	List<SubAreaMasterModel> getTerritoriesForSO();
	List<SubAreaMasterModel> getTerritoriesForSO(String uid);
	List<SubAreaMasterModel> getTerritoriesForCustomer(String customerId);
	List<SubAreaMasterModel> getTerritoriesForCustomer(EyDmsCustomerModel customer);
	List<SubAreaMasterModel> getTerritoriesForPromoter(EyDmsCustomerModel customer);
	List<EyDmsCustomerModel> getAllCustomerForSubArea(List<SubAreaMasterModel> subAreas);

	List<EyDmsCustomerModel> getAllCustomerForSubAreaSO(List<SubAreaMasterModel> subAreas);
	SubAreaMasterModel getTerritoryById(String territoryId);
	List<EyDmsCustomerModel> getAllCustomerForSO();
	List<EyDmsCustomerModel> getAllCustomerForSO(String uid);
	List<EyDmsCustomerModel> getEYDMSAndNonEYDMSAllForSO();
	List<EyDmsCustomerModel> getEYDMSAndNonEYDMSAllForSubArea(List<SubAreaMasterModel> subSreas);
	SubAreaMasterModel getTerritoryByDistrictAndTaluka(String district, String taluka);
	Collection<SubAreaMasterModel> getCurrentTerritory();
	void setCurrentTerritory(Collection<SubAreaMasterModel> subAreas);
	SubAreaMasterModel getTerritoryByTaluka(String taluka);
	EyDmsUserModel getSOforCustomer(EyDmsCustomerModel customer);

	List<SubAreaMasterModel> getSubAreaForDealers(List<EyDmsCustomerModel> dealers);

	List<EyDmsUserModel> getUsersForSubAreas(List<SubAreaMasterModel> subAreaMasters);
	List<EyDmsCustomerModel> getRetailerListForDealer();
	List<EyDmsCustomerModel> getInfluencerListForDealer();
	List<EyDmsCustomerModel> getInfluencerListForRetailer();

	SearchPageData<EyDmsCustomerModel> getCustomerByTerritoriesAndCounterType(SearchPageData searchPageData,
			List<SubAreaMasterModel> subAreaMaster, String counterType, String networkType, boolean isNew,
			String filter, String influencerType, String dealerCategory);
	List<EyDmsCustomerModel> getCustomerByTerritoriesAndCounterTypeWithoutPagination(String counterType, String networkType,
																  String filter, String influencerType, String dealerCategory,boolean isNew);

	SearchPageData<EyDmsCustomerModel> getCustomerByTerritoriesAndCounterType(SearchPageData searchPageData,
			String counterType, String networkType, boolean isNew, String filter, String influencerType,
			String dealerCategory);

	SearchPageData<EyDmsCustomerModel> getInfluencerListForRetailerPagination(SearchPageData searchPageData,
			String networkType, boolean isNew, String filter, String influencerType, String dealerCategory);
	SearchPageData<EyDmsCustomerModel> getInfluencerListForDealerPagination(SearchPageData searchPageData,
			String networkType, boolean isNew, String filter, String influencerType, String dealerCategory);
	SearchPageData<EyDmsCustomerModel> getRetailerListForDealerPagination(SearchPageData searchPageData,
			String networkType, boolean isNew, String filter, Boolean isTop);
	Integer getDealerCountForRetailer();

	Integer getRetailerCountForDealer();

	Integer getInfluencerCountForDealer();

	SearchPageData<EyDmsCustomerModel> getDealerListForRetailerPagination(SearchPageData searchPageData, String filter);
	List<DistrictMasterModel> getDistrictForUser(FilterDistrictData filterDistrictData);
	List<SubAreaMasterModel> getTaulkaForUser(FilterTalukaData filterTalukaData);
	List<EyDmsCustomerModel> getCustomerforUser(RequestCustomerData requestCustomerData);

	List<EyDmsCustomerModel> getAllSalesOfficersByTaluka(String taluka);

	List<RegionMasterModel> getRegionsForUser(FilterRegionData filterRegionData);

	Integer getInfluencerCountForRetailer();
	List<UserSubAreaMappingModel> getSOSubAreaMappingForUser(FilterTalukaData filterTalukaData);
	List<TsmDistrictMappingModel> getTSMDistrcitMappingForUser(FilterDistrictData filterDistrictData);
	Collection<DistrictMasterModel> getCurrentDistrict();

	void setCurrentDistrict(Collection<DistrictMasterModel> districts);

	Collection<RegionMasterModel> getCurrentRegion();

	void setCurrentRegion(Collection<RegionMasterModel> regions);
	SearchPageData<EyDmsCustomerModel> getCustomerForUserPagination(SearchPageData searchPageData,
			RequestCustomerData customerData);
	List<EyDmsUserModel> getTSMForUser(FilterDistrictData filterDistrictData);
	List<EyDmsUserModel> getSOForUser(FilterTalukaData filterTalukaData);
	CustDepotMasterModel getCustDepotForCustomer(EyDmsCustomerModel customer);
	CustDepotMasterModel getCustDepotForCode(String code);
	List<CustDepotMasterModel> getCustDepotForSP(B2BCustomerModel sp);
	void setCurrentCustDepot(Collection<CustDepotMasterModel> custDepotModelList);
	Collection<CustDepotMasterModel> getCurrentCustDepot();
	List<CustDepotMasterModel> getCustDepotForSPInLocalView(B2BCustomerModel sp);
	EyDmsUserModel getSalesOfficerforTaluka(String taluka, BaseSiteModel brand);

	List<DistrictMasterModel> getDistrictForSP(FilterDistrictData filterDistrictData);
	CounterVisitMasterModel findCounterVisitForUnFlaggedDealer(EyDmsCustomerModel eydmsCustomer, boolean isUnFlagRequsetRaised, Date unFlagtime, String remarkForUnFlag);

	EyDmsCustomerModel getSpForCustomer(EyDmsCustomerModel dealer);

	EyDmsCustomerModel getSpForDealer(EyDmsCustomerModel dealer, BaseSiteModel currentBaseSite);

	EyDmsCustomerModel getSpForCustomerAndBrand(EyDmsCustomerModel dealer, BaseSiteModel brand);

	List<EyDmsUserModel> getTSMforDistrict(DistrictMasterModel district,BaseSiteModel brand);

	List<EyDmsUserModel> getRHforRegion(RegionMasterModel region, BaseSiteModel brand);
}