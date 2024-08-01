package com.scl.core.dao;

import com.scl.core.model.*;
import com.scl.facades.data.FilterDistrictData;
import com.scl.facades.data.FilterRegionData;
import com.scl.facades.data.FilterTalukaData;
import com.scl.facades.data.RequestCustomerData;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.Date;
import java.util.List;

public interface TerritoryManagementDao {

	List<String> getAllStatesForSO(UserModel sclUser, BaseSiteModel site);
	List<List<Object>> getAllStateDistrictSubAreaForSO(BaseSiteModel site);

	SclUserModel getSOForSubArea(SclCustomerModel sclCustomer);

	List<SubAreaMasterModel> getTalukasForUser(String state);
	SclUserModel getSclUserForSubArea(String subArea, BaseSiteModel brand);

	List<SclUserModel> getAllSalesOfficersByState(String state, BaseSiteModel site);

	List<SubAreaMasterModel> getTerritoriesForSO(SclUserModel user);
	List<SubAreaMasterModel> getTerritoriesForCustomer(UserModel customer);
	List<SubAreaMasterModel> getTerritoriesForPromoter(UserModel customer);

	SubAreaMasterModel getTerritoryById(String territoryId);
	List<SclCustomerModel> getSCLAndNonSCLCustomerForSubArea(List<SubAreaMasterModel> subAreaMaster);
	SubAreaMasterModel getTerritoryByDistrictAndTaluka(String district, String taluka);
	List<SclCustomerModel> getAllRetailersForSubAreaTOP(List<SubAreaMasterModel> subAreas, BaseSiteModel site,
														String dealerCode);

	List<SubAreaMasterModel> getTerritoriesForSOInLocalView();
	SubAreaMasterModel getTerritoryByIdInLocalView(String territoryId);

	List<SclUserModel> getSclUsersForSubArea(List<SubAreaMasterModel> subAreaMasters);

	List<SclCustomerModel> getRetailerListForDealer(SclCustomerModel sclCustomer);
	List<SclCustomerModel> getInfluencerListForDealer(SclCustomerModel sclCustomer, BaseSiteModel site);
	List<SclCustomerModel> getInfluencerListForRetailer(SclCustomerModel sclCustomer, BaseSiteModel site);

	SearchPageData<SclCustomerModel> getInfluencerListForRetailerPagination(SearchPageData searchPageData,
																			SclCustomerModel sclCustomer, BaseSiteModel site, String networkType, boolean isNew, String filter,
																			String influencerType, String dealerCategory);
	SearchPageData<SclCustomerModel> getInfluencerListForDealerPagination(SearchPageData searchPageData,
																		  SclCustomerModel sclCustomer, BaseSiteModel site, String networkType, boolean isNew, String filter,
																		  String influencerType, String dealerCategory);
	SearchPageData<SclCustomerModel> getRetailerListForDealerPagination(SearchPageData searchPageData,
																		SclCustomerModel sclCustomer, BaseSiteModel site, String networkType, boolean isNew, String filter, Boolean isTop);
	List<SclCustomerModel> getAllCustomerForTerritoriesInLocalView(List<SubAreaMasterModel> subAreaMaster);

	Integer getDealerCountForRetailer(SclCustomerModel currentUser, BaseSiteModel currentSite);

	Integer getRetailerCountForDealer(SclCustomerModel currentUser, BaseSiteModel currentSite);

	Integer getInfluencerCountForDealer(SclCustomerModel currentUser, BaseSiteModel currentSite);

	SearchPageData<SclCustomerModel> getDealerListForRetailerPagination(SearchPageData searchPageData,
																		SclCustomerModel currentUser, BaseSiteModel currentSite, String filter);
    List<SclCustomerModel> getDealerListForRetailer(SclCustomerModel currentUser, BaseSiteModel currentSite);
	List<SubAreaMasterModel> getTalukaForUser(FilterTalukaData filterTalukaData);

	List<DistrictMasterModel> getDistrictForUser(FilterDistrictData filterDistrictData);

	List<SclCustomerModel> getCustomerForUser(RequestCustomerData requestCustomerData, List<SubAreaMasterModel> subAreaMasterList);
	SearchPageData<SclCustomerModel> getPaginatedCustomerForUser(SearchPageData searchPageData,
																 RequestCustomerData requestCustomerData, List<SubAreaMasterModel> subAreaMasterList);

	List<RegionMasterModel> getRegionsForRH(FilterRegionData filterRegionData);

	List<RegionMasterModel> getRegionsForTSM(FilterRegionData filterRegionData);

	List<RegionMasterModel> getRegionsForSO(FilterRegionData filterRegionData);

	Integer getInfluencerCountForRetailer(SclCustomerModel currentUser, BaseSiteModel currentSite);

	List<UserSubAreaMappingModel> getSOSubAreaMappingForUser(FilterTalukaData filterTalukaData);
	List<TsmDistrictMappingModel> getTSMDistrcitMappingForUser(FilterDistrictData filterDistrictData);


	List<SclCustomerModel> getDealersForSP(RequestCustomerData requestCustomerData);

	SearchPageData<SclCustomerModel> getPaginatedDealersForSP(SearchPageData searchPageData,
															  RequestCustomerData requestCustomerData);
	List<SubAreaMasterModel> getTalukaForUserInLocalView(B2BCustomerModel user, FilterTalukaData filterTalukaData);

	List<SubAreaMasterModel> getTalukaForSP(FilterTalukaData filterTalukaData);

	List<SclUserModel> getSOForSP(FilterTalukaData filterTalukaData);
	List<CustDepotMasterModel> getCustDepotForSP(B2BCustomerModel spCode);
	CustDepotMasterModel getCustDepotForCode(String code);
	List<SclCustomerModel> getDealersForSPInLocalView(B2BCustomerModel userForUID);
	CustDepotMasterModel getCustDepotForCustomer(SclCustomerModel customer);
	List<SubAreaMasterModel> getTerritoriesForCustomerIncludingNonActive(UserModel customer);

	SearchPageData<CreditAndOutstandingModel> getDealerOutstandingDetailsForTSMRH(SearchPageData searchPageData,List<SclUserModel> soList,List<SclUserModel> tsmList);

	List<List<Object>> getOutstandingDataForTSMRH();

	List<List<Double>> getBucketListForTSMRH();

	SclUserModel getSalesOfficerforTaluka(String taluka,BaseSiteModel brand);

	List<DistrictMasterModel> getDistrictForSP(FilterDistrictData filterDistrictData);
	CounterVisitMasterModel findCounterVisitForUnFlaggedDealer(SclCustomerModel sclCustomer, boolean isUnFlagRequsetRaised, Date unFlagtime, String remarkForUnFlag);


	SclCustomerModel getSPForCustomer(SclCustomerModel dealer);


	SearchPageData<SclCustomerModel> getAllCustomerForStateDistrict(SearchPageData searchPageData, String site, String state, String district, String city, String pincode, String influencerType, String counterType);

	SclCustomerModel getSPForCustomerAndBrand(SclCustomerModel dealer,BaseSiteModel brand);

	List<SclUserModel> getTSMforDistrict(DistrictMasterModel district, BaseSiteModel brand);

	List<SclUserModel> getRHforRegion(RegionMasterModel region, BaseSiteModel brand);
	List<SclUserModel> getTSOforSubArea(SubAreaMasterModel subArea);
	List<SclCustomerModel> getAllCustomerForTerritoriesSO(List<SubAreaMasterModel> subArea);
	SearchPageData<SclCustomerModel> fetchRetailersForDealer(SearchPageData searchPageData,SclCustomerModel sclCustomer, BaseSiteModel site, String searchKey);

	List<SubAreaMasterModel> getTerritoryForCustWithAllBrands(SclCustomerModel customer);

	List<SclUserModel> getTSOforSubAreas(List<SubAreaMasterModel> subArea);

	List<SclUserModel> getSOforSite(TerritoryMasterModel territoryMaster);

}