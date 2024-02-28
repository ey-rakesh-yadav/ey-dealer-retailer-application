package com.eydms.facades;

import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.model.EyDmsUserModel;
import com.eydms.facades.data.*;
import com.eydms.occ.dto.DealerCurrentNetworkDto;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

public interface SalesPerformanceFacade {

    SalesAndAchievementData getTotalAndActualTargetForSales(String filter, Integer year, Integer month, List<String> doList,List<String> subAreaList);

    SalesAndAchievementData getProratedActualAndActualTargetForSales(String filter, Integer yearFilter, Integer monthFilter,List<String> doList,List<String> subAreaList);

    CurrentAskingPredicatedData getPredicatedAchievementCurrentAndAskingRate(String filter, List<String> doList,List<String> subAreaList);
    CurrentAskingPredicatedData getPredicatedAchievementCurrentAndAskingRateDealerRetailer(String filter,String bgpFilter);

    Double getSecondaryLeadDistanceCount(String filter, Integer month1, Integer year1,List<String> doList,List<String> subAreaList);

    SalesLeaderboardListData getTop5LeaderboardEmpList(String state, String filter, String soFilter, List<String> doList,List<String> subAreaList);

    MarketCounterShareData getDealerCounterShareForMarket(String filter,Integer year, Integer month,List<String> doList,List<String> subAreaList);

    ProductMixVolumeAndRatioListData getProductwiseSalesPercentRatioAndVolumeRatio(String filter, Integer month1, Integer year1,List<String> doList,List<String> subAreaList);

    LowPerformingNetworkData getLowPerformingNetworkDataForDealerRetailerInfluencers(String leadType,int month,int year,String filter,List<String> doList, List<String> subAreaList);
    LowPerformingNetworkData getLowPerformingCountDetailsDealer(String leadType);

    SalesPerformNetworkDetailsListData getListOfAllLowPerformingDealerRetailerInfluencers( String fields,String leadType,String searchKey,List<String> doList, List<String> subAreaList);
    SalesPerformNetworkDetailsListData getListOfAllLowPerformingRetailerInfluencersForDealer( String fields,String leadType,String searchKey);

    SearchPageData<SalesPerformNetworkDetailsData> getLowPerformingViewDetailsWithPagination(String fields, String customerType, String searchKey, List<String> doList, List<String> subAreaList,SearchPageData searchPageData);
    ZeroLiftingNetworkData getCountAndPotentialForZeroLifting(String leadType,int month,int year,String filter, List<String> doList, List<String> subAreaList);
    ZeroLiftingNetworkData getCountAndPotentialForZeroLiftingDealer(String leadType);
    SalesPerformNetworkDetailsListData getListOfAllZeroLiftingDealerRetailerInfluencers( String fields,String leadType,String searchKey,List<String> doList, List<String> subAreaList);
    SalesPerformNetworkDetailsListData getListOfAllZeroLiftingRetailersInfluencerForDealers( String fields,String leadType,String searchKey);
    SalesPerformamceCockpitSaleData getSalesHistoryForDealer(String subArea);
    ProductMixVolumeAndRatioListData getProductwiseSalesPercentRatioAndVolumeRatioForCustomer(EyDmsCustomerModel customer, String customerType,List<String> doList,List<String> subAreaList);
    ProductMixVolumeAndRatioListData getProductwiseSalesPercentRatioAndVolumeRatioForCust(String filter, int month1, int year1,List<String> doList,List<String> subAreaList);
    NetworkCounterShareData getDealerCounterShareForNetwork(String filter, int month1, int year1,List<String> doList,List<String> subAreaList);

    SalesPerformNetworkDetailsListData getListOfAllDealerRetailerInfluencers( String fields, String leadType, String filter,List<String> doList, List<String> subAreaList);
    public List<SalesPerformNetworkDetailsData> getRetailerDetailedSummaryListData(List<EyDmsCustomerModel> retailerList,List<String> doList,List<String> subAreaList);
    public List<SalesPerformNetworkDetailsData> getInfluencerDetailedSummaryListData(List<EyDmsCustomerModel> influencerList,List<String> doList,List<String> subAreaList);
    SalesPerformNetworkDetailsListData getListOfAllRetailerInfluencersForDealer( String fields, String leadType, String filter,List<String> doList, List<String> subAreaList);

    Integer getCountForAllDealerRetailerInfluencers(String leadType,List<String> subAreaList, List<String> doList);

    Integer getCountOfAllRetailersInfluencers(String leadType);
    ProductMixVolumeAndRatioListData getProductMixVolumeAndRatioListDataForMTD(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, ProductMixVolumeAndRatioListData productMixVolumeAndRatioListData, EyDmsCustomerModel customer,List<String> doList,List<String> subAreaList);
    ProductMixVolumeAndRatioListData getProductMixVolumeAndRatioListDataForYTD(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, ProductMixVolumeAndRatioListData productMixVolumeAndRatioListData,EyDmsCustomerModel customerModel,List<String> doList,List<String> subAreaList);
    SalesPerformNetworkDetailsListData getBottomLaggingCounters(List<String> doList, List<String> subAreaList);
    //ProductMixVolumeAndRatioListData getProductMixVolumeAndRatioListDataForMTD(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, ProductMixVolumeAndRatioListData productMixVolumeAndRatioListData, EyDmsCustomerModel customer);
    //ProductMixVolumeAndRatioListData getProductMixVolumeAndRatioListDataForYTD(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, ProductMixVolumeAndRatioListData productMixVolumeAndRatioListData,EyDmsCustomerModel customerModel);
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
    public Map<String,Object> getDirectDispatchOrdersMTDPercentage(int month, int year,List<String> doList, List<String> subAreaList);
    SearchPageData<SalesPerformNetworkDetailsData> getZeroLiftingViewDetailsWithPagination(String fields, String customerType, String searchKey, List<String> doList, List<String> subAreaList, SearchPageData searchPageData);
    
     ProductMixVolumeAndRatioListData getProductMixVolumeAndRatioListDataMTDForCustomer(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, ProductMixVolumeAndRatioListData productMixVolumeAndRatioListData, List<String> doList,List<String> subAreaList, EyDmsCustomerModel customer);

    NetworkCounterShareData getDealerCounterShareForSP(String filter, int month1, int year1,List<String> doList,List<String> subAreaList);

    LeaderboardListData getSpSalesLeaderboardEmpList(String filter,String district);

    TsmLeaderboardListData getTsmSalesLeaderboardEmpList(String filter);

    OverallPerformanceListData getInfluencerOverallPerformance(String filter, String bgpFilter);
}
