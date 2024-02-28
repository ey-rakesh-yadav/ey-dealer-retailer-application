package com.eydms.core.dao;

import com.eydms.core.model.*;
import com.eydms.facades.data.FilterDistrictData;
import com.eydms.facades.data.FilterRegionData;
import com.eydms.facades.data.FilterTalukaData;
import com.eydms.facades.data.RequestCustomerData;
import com.eydms.facades.prosdealer.data.DealerListData;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.Date;
import java.util.List;

public interface TerritoryManagementDao {

	List<String> getAllStatesForSO(UserModel eydmsUser, BaseSiteModel site);
	List<List<Object>> getAllStateDistrictSubAreaForSO(BaseSiteModel site);

	EyDmsUserModel getSOForSubArea(EyDmsCustomerModel eydmsCustomer);
	EyDmsUserModel getEyDmsUserForSubArea(String subArea, BaseSiteModel brand);

	List<EyDmsUserModel> getAllSalesOfficersByState(String state, BaseSiteModel site);

	List<SubAreaMasterModel> getTerritoriesForSO(EyDmsUserModel user);
	List<SubAreaMasterModel> getTerritoriesForCustomer(UserModel customer);
	List<SubAreaMasterModel> getTerritoriesForPromoter(UserModel customer);

	SubAreaMasterModel getTerritoryById(String territoryId);
	List<EyDmsCustomerModel> getEYDMSAndNonEYDMSCustomerForSubArea(List<SubAreaMasterModel> subAreaMaster);
	SubAreaMasterModel getTerritoryByDistrictAndTaluka(String district, String taluka);
	List<EyDmsCustomerModel> getAllRetailersForSubAreaTOP(List<SubAreaMasterModel> subAreas, BaseSiteModel site,
														String dealerCode);

	List<SubAreaMasterModel> getTerritoriesForSOInLocalView();
	SubAreaMasterModel getTerritoryByIdInLocalView(String territoryId);

	List<EyDmsUserModel> getEyDmsUsersForSubArea(List<SubAreaMasterModel> subAreaMasters);

	List<EyDmsCustomerModel> getRetailerListForDealer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel site);
	List<EyDmsCustomerModel> getInfluencerListForDealer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel site);
	List<EyDmsCustomerModel> getInfluencerListForRetailer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel site);

	SearchPageData<EyDmsCustomerModel> getInfluencerListForRetailerPagination(SearchPageData searchPageData,
																			EyDmsCustomerModel eydmsCustomer, BaseSiteModel site, String networkType, boolean isNew, String filter,
																			String influencerType, String dealerCategory);
	SearchPageData<EyDmsCustomerModel> getInfluencerListForDealerPagination(SearchPageData searchPageData,
																		  EyDmsCustomerModel eydmsCustomer, BaseSiteModel site, String networkType, boolean isNew, String filter,
																		  String influencerType, String dealerCategory);
	SearchPageData<EyDmsCustomerModel> getRetailerListForDealerPagination(SearchPageData searchPageData,
																		EyDmsCustomerModel eydmsCustomer, BaseSiteModel site, String networkType, boolean isNew, String filter, Boolean isTop);
	List<EyDmsCustomerModel> getAllCustomerForTerritoriesInLocalView(List<SubAreaMasterModel> subAreaMaster);

	Integer getDealerCountForRetailer(EyDmsCustomerModel currentUser, BaseSiteModel currentSite);

	Integer getRetailerCountForDealer(EyDmsCustomerModel currentUser, BaseSiteModel currentSite);

	Integer getInfluencerCountForDealer(EyDmsCustomerModel currentUser, BaseSiteModel currentSite);

	SearchPageData<EyDmsCustomerModel> getDealerListForRetailerPagination(SearchPageData searchPageData,
																		EyDmsCustomerModel currentUser, BaseSiteModel currentSite, String filter);

	List<SubAreaMasterModel> getTalukaForUser(FilterTalukaData filterTalukaData);

	List<DistrictMasterModel> getDistrictForUser(FilterDistrictData filterDistrictData);

	List<EyDmsCustomerModel> getCustomerForUser(RequestCustomerData requestCustomerData, List<SubAreaMasterModel> subAreaMasterList);
	SearchPageData<EyDmsCustomerModel> getPaginatedCustomerForUser(SearchPageData searchPageData,
																 RequestCustomerData requestCustomerData, List<SubAreaMasterModel> subAreaMasterList);

	List<RegionMasterModel> getRegionsForRH(FilterRegionData filterRegionData);

	List<RegionMasterModel> getRegionsForTSM(FilterRegionData filterRegionData);

	List<RegionMasterModel> getRegionsForSO(FilterRegionData filterRegionData);

	Integer getInfluencerCountForRetailer(EyDmsCustomerModel currentUser, BaseSiteModel currentSite);

	List<UserSubAreaMappingModel> getSOSubAreaMappingForUser(FilterTalukaData filterTalukaData);
	List<TsmDistrictMappingModel> getTSMDistrcitMappingForUser(FilterDistrictData filterDistrictData);


	List<EyDmsCustomerModel> getDealersForSP(RequestCustomerData requestCustomerData);

	SearchPageData<EyDmsCustomerModel> getPaginatedDealersForSP(SearchPageData searchPageData,
															  RequestCustomerData requestCustomerData);
	List<SubAreaMasterModel> getTalukaForUserInLocalView(B2BCustomerModel user, FilterTalukaData filterTalukaData);

	List<SubAreaMasterModel> getTalukaForSP(FilterTalukaData filterTalukaData);

	List<EyDmsUserModel> getSOForSP(FilterTalukaData filterTalukaData);
	List<CustDepotMasterModel> getCustDepotForSP(B2BCustomerModel spCode);
	CustDepotMasterModel getCustDepotForCode(String code);
	List<EyDmsCustomerModel> getDealersForSPInLocalView(B2BCustomerModel userForUID);
	CustDepotMasterModel getCustDepotForCustomer(EyDmsCustomerModel customer);
	List<SubAreaMasterModel> getTerritoriesForCustomerIncludingNonActive(UserModel customer);

	SearchPageData<CreditAndOutstandingModel> getDealerOutstandingDetailsForTSMRH(SearchPageData searchPageData,List<EyDmsUserModel> soList,List<EyDmsUserModel> tsmList);

	List<List<Object>> getOutstandingDataForTSMRH();

	List<List<Double>> getBucketListForTSMRH();

	EyDmsUserModel getSalesOfficerforTaluka(String taluka,BaseSiteModel brand);

	List<DistrictMasterModel> getDistrictForSP(FilterDistrictData filterDistrictData);
	CounterVisitMasterModel findCounterVisitForUnFlaggedDealer(EyDmsCustomerModel eydmsCustomer, boolean isUnFlagRequsetRaised, Date unFlagtime, String remarkForUnFlag);


	EyDmsCustomerModel getSPForCustomer(EyDmsCustomerModel dealer);


	SearchPageData<EyDmsCustomerModel> getAllCustomerForStateDistrict(SearchPageData searchPageData, String site, String state, String district, String city, String pincode, String influencerType, String counterType);

	EyDmsCustomerModel getSPForCustomerAndBrand(EyDmsCustomerModel dealer,BaseSiteModel brand);

	List<EyDmsUserModel> getTSMforDistrict(DistrictMasterModel district, BaseSiteModel brand);

	List<EyDmsUserModel> getRHforRegion(RegionMasterModel region, BaseSiteModel brand);
	List<EyDmsUserModel> getTSOforSubArea(SubAreaMasterModel subArea);
	List<EyDmsCustomerModel> getAllCustomerForTerritoriesSO(List<SubAreaMasterModel> subArea);

	List<SubAreaMasterModel> getTerritoryForCustWithAllBrands(EyDmsCustomerModel customer);

	List<EyDmsUserModel> getTSOforSubAreas(List<SubAreaMasterModel> subArea);
}