package com.scl.facades;

import java.util.List;

import com.scl.facades.data.*;
import com.scl.facades.prosdealer.data.CustomerListData;
import com.scl.facades.prosdealer.data.DealerListData;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.core.servicelayer.data.SearchPageData;


public interface TerritoryManagementFacade {

	List<String> getAllSubAreaForSO(String userId);
	List<String> getAllSubAreaForCustomer(String customerId);
	DealerListData getAllCustomerForSubArea();
	DealerListData getAllDealersForSubArea(List<String> territoryList);
	DealerListData getAllRetailersForSubArea();
	DealerListData getAllInfluencersForSubArea();
	DealerListData getAllSitesForSubArea();
	DealerListData getAllRetailersForSubAreaTOP(String subArea, String dealerCode);
	TerritoryListData getAllTerritoryForSO(String subArea);

    DealerListData getAllSalesOfficersByState(String state);

    List<String> getAllStatesForSO();
    
    TerritoryListData getTerritoriesForSO();

	DealerListData getRetailerListForDealer();
	DealerListData getInfluencerListForDealer();
	DealerListData getInfluencerListForRetailer();
	SearchPageData<CustomerData> getRetailerListForDealerPagination(SearchPageData searchPageData, String networkType,
			boolean isNew, String filter, Boolean isTOP);
	SearchPageData<CustomerData> getInfluencerListForDealerPagination(SearchPageData searchPageData, String networkType,
			boolean isNew, String filter, String influencerType, String dealerCategory);
	SearchPageData<CustomerData> getInfluencerListForRetailerPagination(SearchPageData searchPageData,
			String networkType, boolean isNew, String filter, String influencerType, String dealerCategory);
	Integer getDealerCountForRetailer();

	Integer getRetailerCountForDealer();

	Integer getInfluencerCountForDealer();

	SearchPageData<CustomerData> getDealerListForRetailerPagination(SearchPageData searchPageData, String filter);

	TerritoryListData getTerritoriesForCustomer();


	RegionListData getRegionsForUser(FilterRegionData filterRegionData);
	
	Integer getInfluencerCountForRetailer();

	TerritoryListData getTalukaForUser(FilterTalukaData filterTalukaData);

	DistrictMasterListData getDistrictForUser(FilterDistrictData filterDistrictData);
	CustomerListData getCustomerForUser(RequestCustomerData customerData);
	CustomerListData getCustomerForWJP(RequestCustomerData customerData);
	SearchPageData<CustomerData> getCustomerForUserPagination(SearchPageData searchPageData,
			RequestCustomerData customerData);

	SOSubAreaMappingListData getSOSubAreaMappingForUser(FilterTalukaData filterTalukaData);
	TSMDistrictMappingListData getTSMDistrcitMappingForUser(FilterDistrictData filterDistrictData);
	SclUserListData getSOForUser(FilterTalukaData filterTalukaData);
	SclUserListData getTSMForUser(FilterDistrictData filterDistrictData);

	DistrictMasterListData getDistrictForSP(FilterDistrictData filterDistrictData);

    SclCustomerData getSPForCustomer(String uid);
	SearchPageData<CustomerData> fetchRetailerListForDealer(SearchPageData searchPageData,String searchKey);
	List<CustomerData> getDealerListForRetailer();
}
