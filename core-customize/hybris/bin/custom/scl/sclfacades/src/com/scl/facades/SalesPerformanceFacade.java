package com.scl.facades;

import com.scl.core.model.SclCustomerModel;
import com.scl.core.model.SclUserModel;
import com.scl.facades.data.*;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

public interface SalesPerformanceFacade {

    SalesAndAchievementData getTotalAndActualTargetForSales(String filter, Integer year, Integer month,List<String> territoryList);

    SalesAndAchievementData getProratedActualAndActualTargetForSales(String filter, Integer yearFilter, Integer monthFilter,List<String> territoryList);

    CurrentAskingPredicatedData getPredicatedAchievementCurrentAndAskingRate(String filter,List<String> territoryList,int month,int year);
    CurrentAskingPredicatedData getPredicatedAchievementCurrentAndAskingRateDealerRetailer(String filter,String bgpFilter,int month,int year);

    Double getSecondaryLeadDistanceCount(String filter, Integer month1, Integer year1,List<String> territoryList);

    SalesLeaderboardListData getTop5LeaderboardEmpList(String state, String filter, String soFilter, List<String> doList,List<String> subAreaList);

    MarketCounterShareData getDealerCounterShareForMarket(String filter,Integer year, Integer month,List<String> doList,List<String> territoryList);

    ProductMixVolumeAndRatioListData getProductwiseSalesPercentRatioAndVolumeRatio(String filter, Integer month1, Integer year1,List<String> territoryList);

    LowPerformingNetworkData getLowPerformingNetworkDataForDealerRetailerInfluencers(String leadType,int month,int year,String filter,List<String> territoryList,List<String> subAreaList,List<String> districtList);
    LowPerformingNetworkData getLowPerformingCountDetailsDealer(String leadType);

    SalesPerformNetworkDetailsListData getListOfAllLowPerformingDealerRetailerInfluencers( String fields,String leadType,String searchKey, List<String> territoryList,List<String> subAreaList,List<String> districtList);
    SalesPerformNetworkDetailsListData getListOfAllLowPerformingRetailerInfluencersForDealer( String fields,String leadType,String searchKey);

    SearchPageData<SalesPerformNetworkDetailsData> getLowPerformingViewDetailsWithPagination(String fields, String customerType, String searchKey, List<String> doList, List<String> subAreaList,SearchPageData searchPageData);
    ZeroLiftingNetworkData getCountAndPotentialForZeroLifting(String leadType,int month,int year,String filter,  List<String> territoryList,List<String> subAreaList,List<String> districtList);
    ZeroLiftingNetworkData getCountAndPotentialForZeroLiftingDealer(String leadType);
    SalesPerformNetworkDetailsListData getListOfAllZeroLiftingDealerRetailerInfluencers(String fields,String leadType,String searchKey,List<String> territoryList,List<String> subAreaList,List<String> districtList);
    SalesPerformNetworkDetailsListData getListOfAllZeroLiftingRetailersInfluencerForDealers( String fields,String leadType,String searchKey);
    SalesPerformamceCockpitSaleData getSalesHistoryForDealer(String subArea);
    ProductMixVolumeAndRatioListData getProductwiseSalesPercentRatioAndVolumeRatioForCustomer(SclCustomerModel customer, String customerType,List<String> territoryList,List<String> subAreaList,List<String> districtList);
    ProductMixVolumeAndRatioListData getProductwiseSalesPercentRatioAndVolumeRatioForCust(String filter, int month1, int year1,List<String> territoryList,List<String> subAreaList,List<String> districtList);
    NetworkCounterShareData getDealerCounterShareForNetwork(String filter, int month1, int year1, List<String> subAreaList, List<String> doList,List<String> territoryList);
    List<SalesPerformNetworkDetailsData> getDealerDetailedSummaryListDataForSP(List<SclCustomerModel> dealersList);
    SalesPerformNetworkDetailsListData getListOfAllDealerRetailerInfluencers( String fields, String leadType, String filter,List<String> territoryList,List<String> subAreaList,List<String> districtList);
    public List<SalesPerformNetworkDetailsData> getRetailerDetailedSummaryListDataForSP(List<SclCustomerModel> retailerList);
    public List<SalesPerformNetworkDetailsData> getInfluencerDetailedSummaryListData(List<SclCustomerModel> influencerList,List<String> doList,List<String> subAreaList);
    SalesPerformNetworkDetailsListData getListOfAllRetailerInfluencersForDealer( String fields, String leadType, String filter,List<String> doList, List<String> subAreaList);

    Integer getCountForAllDealerRetailerInfluencers(String leadType,List<String> territoryList,List<String> subAreaList,List<String> districtList);

    Integer getCountOfAllRetailersInfluencers(String leadType);
    ProductMixVolumeAndRatioListData getProductMixVolumeAndRatioListDataForMTD(SclUserModel sclUser, BaseSiteModel baseSite, ProductMixVolumeAndRatioListData productMixVolumeAndRatioListData, SclCustomerModel customer,List<String> subAreaList);
    ProductMixVolumeAndRatioListData getProductMixVolumeAndRatioListDataForYTD(SclUserModel sclUser, BaseSiteModel baseSite, ProductMixVolumeAndRatioListData productMixVolumeAndRatioListData,SclCustomerModel customerModel,List<String> territoryList,List<String> subAreaList,List<String> districtList);
    SalesPerformNetworkDetailsListData getBottomLaggingCounters(List<String> territoryList);
    //ProductMixVolumeAndRatioListData getProductMixVolumeAndRatioListDataForMTD(SclUserModel sclUser, BaseSiteModel baseSite, ProductMixVolumeAndRatioListData productMixVolumeAndRatioListData, SclCustomerModel customer);
    //ProductMixVolumeAndRatioListData getProductMixVolumeAndRatioListDataForYTD(SclUserModel sclUser, BaseSiteModel baseSite, ProductMixVolumeAndRatioListData productMixVolumeAndRatioListData,SclCustomerModel customerModel);
    NCRTrendListData getNCRTrendList();
    NCRTrendListData getNCRTrendListForOneMonth();
    PartnerDetailsDataForSales getPartnerDetailsForSales(String searchKeyWord, String filter, Integer year, Integer month,List<String> doList,List<String> subAreaList);
    ProductMixVolumeAndRatioListData getBrandWiseSalesPercentRatioAndVolumeRatio(String customerCode);
    ProductMixVolumeAndRatioListData getProductMixPercentRatioAndVolumeRatioWithPoints(String filter);

    SalesAndAchievementData getProratedActualAndTargetForSalesDealerRetailer(String filter, Integer yearFilter, Integer monthFilter,String bgpFilter);
   // LeaderboardListData getSalesLeaderboardEmpList(String taluka, String filter, String district);
    LeaderboardListData getSalesLeaderboardEmpList(String filter, String leadType);
    SalesAndAchievementData getTotalActualAndTargetSaleForCustomer(String filter, int year1, int month1, String bgpFilter, String counterType,String retailerId);

    MonthlySalesListData getActualVsTargetSalesGraph(String filter, String counterType, String bgpFilter);
    MonthlySalesListData getActualVsTargetSalesGraphForTSMRH(String filter,List<String> doList,List<String> subAreaList);
    ProratedBreachData getProratedBreach(List<String> doList,List<String> subAreaList);
    public Map<String,Object> getDirectDispatchOrdersMTDPercentage(int month, int year,List<String> doList, List<String> territoryList);
    SearchPageData<SalesPerformNetworkDetailsData> getZeroLiftingViewDetailsWithPagination(String fields, String customerType, String searchKey, List<String> doList, List<String> subAreaList, SearchPageData searchPageData);
    
     ProductMixVolumeAndRatioListData getProductMixVolumeAndRatioListDataMTDForCustomer(SclUserModel sclUser, BaseSiteModel baseSite, ProductMixVolumeAndRatioListData productMixVolumeAndRatioListData, List<String> doList,List<String> subAreaList, SclCustomerModel customer);

    NetworkCounterShareData getDealerCounterShareForSP(String filter, int month1, int year1,List<String> doList,List<String> subAreaList);

    LeaderboardListData getSpSalesLeaderboardEmpList(String filter,String district);

    TsmLeaderboardListData getTsmSalesLeaderboardEmpList(String filter);

    OverallPerformanceListData getInfluencerOverallPerformance(String filter, String bgpFilter);

    CounterShareResponseData getCounterShareData(CounterShareData counterShareData);
    DealerCurrentNetworkListData getCurrentNetworkCustomers(String leadType,String networkType,String searchKeyFilter, boolean sclExclusiveCustomer);
   List<Map<String,Object>> getLastLiftingDateAndQtyForCustomers(String sclCustomerCode,String leadType);
}
