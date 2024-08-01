package com.scl.core.services;

import com.scl.core.model.*;
import com.scl.facades.data.*;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.c2l.BrandModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import org.apache.commons.collections.CollectionUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface SalesPerformanceService {
    Double getActualTargetForSalesMTD(String sublArea, SclUserModel sclUser, BaseSiteModel currentBaseSite);
    Double getActualTargetForSalesYTD(String subArea, SclUserModel sclUser, BaseSiteModel currentBaseSite);
    Double getMonthlySalesTarget(SclUserModel sclUser, BaseSiteModel currentBaseSite,List<String> territoryList);
    Double getMonthlySalesTarget(SclUserModel sclUser, BaseSiteModel currentBaseSite,int month,int year,List<String> territoryList);
    Double getMonthlySalesTargetForDealer(SclCustomerModel sclCustomerModel, BaseSiteModel currentBaseSite,String bgpFilter);

    Double getMonthlySalesTargetForDealer(SclCustomerModel sclCustomerModel, BaseSiteModel currentBaseSite,int month,int year,String bgpFilter);
    Double getMonthlySalesTargetForDealerList(List<B2BCustomerModel> b2BCustomerModel, BaseSiteModel currentBaseSite,int month,int year);
    Double getMonthlySalesTargetForDealerLastMonth(List<B2BCustomerModel> b2BCustomerModel, BaseSiteModel currentBaseSite,int month,int year);
    Double getMonthlySalesTargetForSP(List<SclCustomerModel> sclCustomerModel);
    Double getMonthlySalesTargetForRetailer(String retailerCode, BaseSiteModel currentBaseSite,String bgpFilter, String monthYear);
    Double getMonthlySalesTargetForRetailer(List<SclCustomerModel> retailerCode, BaseSiteModel currentBaseSite,String bgpFilter, String monthYear);
    Double getLastMonthSalesTarget(SclUserModel sclUser, BaseSiteModel currentBaseSite,List<String> territoryList);
    Double getMonthlySalesForPartnerTarget(String code, SclUserModel sclUser, BaseSiteModel currentBaseSite);
    Double getAnnualSalesTarget(SclUserModel sclUser,List<String> territoryList);
    Double getAnnualSalesTargetForDealer(SclCustomerModel sclCustomerModel,String bgpFilter);
    Double getAnnualSalesTargetForSP(List<SclCustomerModel> sclCustomerModel);
    Double getAnnualSalesTargetForRetailer(SclCustomerModel sclCustomerModel,String bgpFilter);
    Double getLastYearSalesTarget(SclUserModel sclUser, BaseSiteModel currentBaseSite,List<String> territoryList);
    Double getAnnualSalesForPartnerTarget(String code, SclUserModel sclUser, BaseSiteModel currentBaseSite);
    List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatio(SclUserModel sclUser, BaseSiteModel baseSite,SclCustomerModel customer,List<String> doList,List<String> subAreaList);

    SalesPerformNetworkDetailsListData getListOfAllLowPerforming(String fields,String leadType,String searchKey,List<String> territoryList,List<String> subAreaList,List<String> districtList);
    SalesPerformNetworkDetailsListData getListOfAllLowPerformingRetailerInfForDealers(String fields,String leadType,String searchKey);
    SearchPageData<SalesPerformNetworkDetailsData> getLowPerformingViewDetailsWithPagination(String fields, BaseSiteModel site,String customerType, String searchKey, List<String> doList, List<String> subAreaList, SearchPageData searchPageData);
    Double getPotentialForDealersNetwork(String subArea, BaseSiteModel baseSite);

    SalesPerformNetworkDetailsListData getListOfAllDealerRetailerInfluencers(String fields,BaseSiteModel currentBaseSite,String leadType,String filter, List<String> territoryList,List<String> subAreaList,List<String> districtList);
    SalesPerformNetworkDetailsListData getListOfAllRetailerInfluencersForDealer(String fields,BaseSiteModel currentBaseSite,String leadType,String filter,List<String> doList, List<String> subAreaList);
    SalesPerformNetworkDetailsListData getListOfAllZeroLifting(String fields,BaseSiteModel currentBaseSite,String leadType,String searchKey, List<String> territoryList,List<String> subAreaList,List<String> districtList);
    SalesPerformNetworkDetailsListData getListOfAllZeroLiftingRetailerInfForDealer(String fields,String leadType,String searchKey);
    Double getSalesByDeliveryDate(String subArea, SclUserModel sclUser, BaseSiteModel currentBaseSite, Date startDate, Date endDate);
    public List<Date> getCurrentFinancialYear();

    public List<Date> getCurrentFinancialYearSales();
    List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForCustomer(SclCustomerModel customer, BaseSiteModel baseSite, String customerType,List<String> doList,List<String> subAreaList);
    List<List<Object>> getBrandwiseSalesPercentRatioAndVolumeRatioForCustomer(SclCustomerModel customer);
    List<List<Object>> getProductMixPercentRatioAndVolumeRatioWithPoints(SclCustomerModel customer,String filter);
    Integer getCountForAllDealerRetailerInfluencers(String leadType, B2BCustomerModel currentUser, BaseSiteModel currentBaseSite,List<String> territoryList,List<String> subAreaList,List<String> districtList);
    Integer getCountOfAllRetailersInfluencers(String leadType);
    List<String> getStateWiseProductForSummaryPage(String subArea);
    SalesPerformNetworkDetailsListData getBottomLaggingCounters(List<String> territoryList);
    SalesPerformamceCockpitSaleData getSalesHistoryForDealers(String subArea, SclUserModel sclUser, BaseSiteModel currentBaseSite);

    Double getNCRThreshold(String state,BaseSiteModel site,String yearMonth);
    List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForYTD(SclUserModel sclUser, BaseSiteModel baseSite,SclCustomerModel customer,List<String> doList,List<String> subAreaList);

    List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForMonthPicker(SclUserModel sclUser, BaseSiteModel baseSite, int month, int year,List<String> doList,List<String> subAreaList);

    List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForCustomerMonthPicker(SclCustomerModel sclUser, BaseSiteModel baseSite, int month, int year,List<String> subAreaList,List<String> districtList);

    Double getActualTargetSalesForSelectedMonthAndYear(String subArea, SclUserModel sclUser, BaseSiteModel baseSite, int month, int year);


    LowPerformingNetworkData getLowPerformingSummaryData(int month, int year, String filter,  BaseSiteModel site, String SOFilter, List<String> territoryList,List<String> subAreaList,List<String> districtList);
    LowPerformingNetworkData getLowPerformingSummaryDataDealer(String SOFilter);

    Double getActualTargetForSalesForMonth(String subArea, SclUserModel sclUser, BaseSiteModel currentBaseSite, int year, int month);

    Double getActualTargetForSalesForMonth(SclUserModel sclUser, BaseSiteModel currentBaseSite, int year, int month,List<String> doList,List<String> subAreaList);
    Double getActualTargetForSalesForMonthDealer(SclCustomerModel sclUser, BaseSiteModel currentBaseSite, int year, int month,String bgpFilter);

    Double getActualTargetForSalesForMtdSp(List<SclCustomerModel> sclUser, BaseSiteModel currentBaseSite, int year, int month);
    Double getActualTargetForSalesForMonthSP(List<SclCustomerModel> sclUser, int year, int month);
    //Double getActualTargetForSalesForMonth(SclUserModel sclUser, BaseSiteModel currentBaseSite, int year, int month);
    Double getActualTargetForSalesForMonthRetailer(SclCustomerModel sclUser, BaseSiteModel currentBaseSite, int year, int month,String bgpFilter);
    Double getActualTargetForPartnerSalesForMonth(String code,SclUserModel sclUser, BaseSiteModel currentBaseSite, int year, int month,List<String> doList,List<String> subAreaList);

    Double getSalesTargetForMonth(SclUserModel sclUser, BaseSiteModel currentBaseSite, Integer year, Integer month);
    Double getSalesTargetForMonth(SclUserModel sclUser, BaseSiteModel currentBaseSite, Integer year, Integer month,List<String> territoryList);
    Double getSalesTargetForMonthDealer(SclCustomerModel sclUser, BaseSiteModel currentBaseSite, Integer year, Integer month,String bgpFilter);

    Double getSalesTargetForMtdSP(List<SclCustomerModel> sclUser, BaseSiteModel currentBaseSite, Integer year, Integer month);
    Double getSalesTargetForMonthSP(List<SclCustomerModel> sclCustomerModel, Integer year, Integer month);

    Double getSalesTargetForMonthRetailer(String retailerCode, BaseSiteModel currentBaseSite, Integer year, Integer month,String bgpFilter);

    Double getSalesTargetForPartnerMonth(String code, SclUserModel sclUser, BaseSiteModel currentBaseSite, Integer year, Integer month);

   // ZeroLiftingNetworkData getZeroLiftingSummaryData(int month, int year, String filter, BaseSiteModel site, String SOFilter);
    Double getActualTargetForSalesMTD(SclUserModel sclUser, BaseSiteModel currentBaseSite, List<String> doList,List<String> subAreaList);

    ZeroLiftingNetworkData getZeroLiftingSummaryData(int month, int year, String filter, BaseSiteModel site, String SOFilter, List<String> territoryList,List<String> subAreaList,List<String> districtList);
    ZeroLiftingNetworkData getZeroLiftingSummaryDataDealer(String leadType);

    Double getActualTargetForSalesMTDDealer(SclCustomerModel sclCustomer, BaseSiteModel currentBaseSite,String bgpFilter);
    Double getActualTargetForSalesMTDRetailer(SclCustomerModel sclCustomer, BaseSiteModel currentBaseSite,String bgpFilter);
    Double getActualTargetForSalesDealerMTD(SclCustomerModel sclCustomer, BaseSiteModel currentBaseSite,String bgpFilter);
    Double getActualTargetForSalesSPMTD(List<SclCustomerModel> sclCustomer);

    Double getActualTargetForSalesSPMTDSearch(List<SclCustomerModel> sclCustomer);
    Double getActualTargetForSalesRetailerMTD(SclCustomerModel sclCustomer, BaseSiteModel currentBaseSite,String bgpFilter);
    Double getActualTargetForSalesLastMonth(SclUserModel sclUser, BaseSiteModel currentBaseSite,int year, int month,List<String> doList,List<String> subAreaList);
    Double getActualTargetForSalesYTD(SclUserModel sclUser, BaseSiteModel currentBaseSite,List<String> doList,List<String> subAreaList);
    Double getActualTargetForSalesYTDDealer(SclCustomerModel sclCustomer, BaseSiteModel currentBaseSite,String bgpFilter);
    Double getActualTargetForSalesYTDSP(List<SclCustomerModel> sclCustomer, BaseSiteModel currentBaseSite);
    Double getActualTargetForSalesYTDRetailer(SclCustomerModel sclCustomer, BaseSiteModel currentBaseSite,String bgpFilter);
    Double getActualTargetForSalesDealerYTD(SclCustomerModel sclCustomer, BaseSiteModel currentBaseSite,String bgpFilter);
    Double getActualTargetForSalesRetailerYTD(SclCustomerModel sclCustomer, BaseSiteModel currentBaseSite,String bgpFilter);
    Double getActualTargetForSalesLastYear(SclUserModel sclUser, BaseSiteModel currentBaseSite,List<String> doList,List<String> subAreaList);
    Double getActualTargetForPartnerSalesMTD(String code,SclUserModel sclUser, BaseSiteModel currentBaseSite,List<String> doList,List<String> subAreaList);
    Double getActualTargetForPartnerSalesYTD(String code,SclUserModel sclUser, BaseSiteModel currentBaseSite,List<String> doList,List<String> subAreaList);
    Double getActualTargetSalesForSelectedMonthAndYear(SclUserModel sclUser, BaseSiteModel baseSite, int month, int year,List<String> doList,List<String> subAreaList);

    List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForYTD(SclUserModel sclUser, BaseSiteModel baseSite,List<String> doList,List<String> subAreaList);

    List<SclCustomerModel> filterSclCustomersWithSearchTerm(List<SclCustomerModel> customers,String searchTerm);
    Double getActualDealerCounterShareMarket(SclCustomerModel dealer,BaseSiteModel site,List<String> doList, List<String> subAreaList);

    Double getSalesByDeliveryDate(SclUserModel sclUser, BaseSiteModel currentBaseSite, Date startDate, Date endDate,List<String> doList,List<String> subAreaList);

    Double getSalesLeaderByDeliveryDate(SclCustomerModel sclUser, BaseSiteModel currentBaseSite, Date startDate, Date endDate);

    List<List<Object>> getSalesLeaderByDate(DistrictMasterModel district,BaseSiteModel currentBaseSite, Date startDate, Date endDate);

    List<List<Object>> getSalesLeaderByDateRetailer(SubAreaMasterModel district,BaseSiteModel currentBaseSite, Date startDate, Date endDate);

    List<List<Object>> getPremiumSalesLeaderByDate(DistrictMasterModel district, BaseSiteModel currentBaseSite, Date startDate, Date endDate);

    List<List<Object>> getPremiumSalesLeaderByDateRetailer(SubAreaMasterModel district, BaseSiteModel currentBaseSite, Date startDate, Date endDate);

    List<List<Object>> getSalesHistoryModelList(String state, Date statDate,Date endDate, BaseSiteModel siteModel);
    List<List<Object>> getPartnerDetailsForSales(String searchKeyWord);

    double getSecondaryLeadDistanceForMonth(SclUserModel sclUser, BaseSiteModel baseSite, Integer year1, Integer month1, List<String> territoryList);

    double getSecondaryLeadDistance(SclUserModel sclUser, BaseSiteModel baseSite, List<String> territoryList);

    double getSecondaryLeadDistanceMTD(SclUserModel sclUser, BaseSiteModel baseSite, List<String> territoryList);

    double getSecondaryLeadDistanceYTD(SclUserModel sclUser, BaseSiteModel baseSite, List<String> territoryList);

    DealerRevisedMonthlySalesModel getMonthlySaleTargetGraphForDealer(String customer, String monthName, String yearName);

    List<DealerRevisedMonthlySalesModel> getMonthlySaleTargetGraphForSP(List<SclCustomerModel> sclCustomer, String monthName, String yearName);
    DealerRevisedMonthlySalesModel getMonthlySaleTargetGraph(SclUserModel user,String monthName, String yearName,List<String> doList,List<String> subAreaList);

    double getActualSaleForDealerGraphYTD(SclCustomerModel customer, Date startDate, Date endDate, BaseSiteModel currentBaseSite,String bgpFilter);

    double getActualSaleForDealerGraphYTDSP(List<SclCustomerModel> sclCustomer, Date startDate, Date endDate, BaseSiteModel currentBaseSite);
    double getActualSaleForGraphYTD(SclUserModel sclUserModel, Date startDate, Date endDate, BaseSiteModel currentBaseSite,List<String> doList,List<String> subAreaList);

    double getActualTargetFor10DayBucketForDealer(SclCustomerModel sclCustomer, String bgpFilter, Date startDate1, Date endDate1);

    double getActualTargetFor10DayBucketForSP(List<SclCustomerModel>  sclCustomer,Date startDate1, Date endDate1);
    double getActualTargetFor10DayBucket(SclUserModel sclUser, Date startDate1, Date endDate1,List<String> doList,List<String> subAreaList);

    Map<String, Object> getDirectDispatchOrdersMTDPercentage(int month, int year,List<String> doList, List<String> territoryList);
    String getDaysSinceLastLiftingForRetailer(List<SclCustomerModel> saleQty);
    String getDaysSinceLastLiftingForInfluencer(List<SclCustomerModel> saleQty);
    Double getSalesLeaderByDeliveryDateRetailer(SclCustomerModel sclUser, BaseSiteModel currentBaseSite, Date startDate, Date endDate);
    SearchPageData<SalesPerformNetworkDetailsData> getZeroLiftingViewDetailsWithPagination(String fields, BaseSiteModel site, String customerType, String searchKey, List<String> doList, List<String> subAreaList, SearchPageData searchPageData);

    Double getPremiumSalesLeaderByDeliveryDate(SclCustomerModel so, BaseSiteModel currentBaseSite, Date startDate, Date endDate);

    Double getPremiumSalesLeaderByDeliveryDateRetailer(SclCustomerModel so, BaseSiteModel currentBaseSite, Date startDate, Date endDate);

    Double getCashDiscountAvailedPercentage(SclCustomerModel so, BaseSiteModel currentBaseSite, Date startDate, Date endDate);

    Double getMonthlySalesTargetForRetailer(String uid, BaseSiteModel currentBaseSite, String bgpFilter);
    List<List<Object>> getMonthlySalesTargetForRetailer(List<SclCustomerModel> sclCustomerModels);

    Double getActualTargetForSalesYTDRetailerList(SclCustomerModel currentUser, BaseSiteModel currentBaseSite);

    Double getActualTargetForSalesMTDRetailerList(SclCustomerModel currentUser, BaseSiteModel currentBaseSite);

    Double getActualTargetForSalesYTRetailer(SclCustomerModel currentUser, BaseSiteModel currentBaseSite, String bgpFilter);

    DistrictMasterModel getDistrictForSP(FilterDistrictData filterDistrictData);

    List<SclCustomerModel> getSPForDistrict(DistrictMasterModel districtForSP);

    Double getAnnualSalesTargetForDealerFY(SclCustomerModel sclCustomerModel,String bgpFilter, String financialYear);

    Double getAnnualSalesTargetForRetailerFY(SclCustomerModel sclCustomerModel,String bgpFilter, String financialYear);

    List<SclUserModel> getTsmByRegion(String district);

    List<SclUserModel> getRHByState(String district);

    List<SclCustomerModel> getCustomerForTsm(String district, SclUserModel sclUserModel);

    List<SclCustomerModel> getCustomerForRH(String district, SclUserModel sclUserModel);

    List<List<Object>> getSalesDealerByDate(String district, SclUserModel sclUserModel,Date startDate, Date endDate);

    List<List<Object>> getRHSalesDealerByDate(SclUserModel sclUserModel,Date startDate, Date endDate);

    List<SclCustomerModel> getCustomerForSp(SclCustomerModel sclCustomer);

    List<List<Object>> getWeeklyOverallPerformance(String bgpFilter);

    List<List<Object>> getMonthlyOverallPerformance(String bgpFilter);
    SalesQuantityData setSalesQuantityForCustomer(SclCustomerModel customerModel, String customerType, List<String> territoryList);
    Double getYearToYearGrowthForDealer(SclCustomerModel customerModel);
    List<SclCustomerModel> getCustomersByLeadType(String leadType,List<String> territoryList,List<String> subAreaList,List<String> districtList);
    void updateReceipts(ProductModel productCode, SclCustomerModel dealerCode, Double invoicedQuantity);

    public List<SclCustomerModel> getCustomersByTerritoryCode(List<SclCustomerModel> customerList, List<String> territoryList);
    Map<String, Object> getPotentialForCustomer(SclCustomerModel sclCustomer,String startDate,String endDate);
    Map<String, Object> getSelfBrandSaleforCustomer(SclCustomerModel sclCustomer, String countervistId, BrandModel brand);
    DealerCurrentNetworkListData getCurrentNetworkCustomers(String leadType,String networkType,String searchKeyFilter, boolean sclExclusiveCustomer);
    CounterShareResponseData getCounterShareData(CounterShareData counterShareData);
    Double getCounterShareForDealer(SclCustomerModel dealer,int month,int year);
    List<Map<String, Object>> getLastLiftingDateAndQtyForCustomers(String sclCustomerCode,String leadType);
    String getDaysFromLastOrder(SclCustomerModel sclCustomerModel);
    List<DealerCurrentNetworkData> getDealerDetailedSummaryListData(List<SclCustomerModel> dealerList);
    List<DealerCurrentNetworkData> getRetailerDetailedSummaryListData(List<SclCustomerModel> retailerList);
    List<SalesPerformNetworkDetailsData> getRetailerDetailedSummaryListDataForSP(List<SclCustomerModel> retailerList);
    List<SalesPerformNetworkDetailsData> getInfluencerDetailedSummaryListData(List<SclCustomerModel> influencerList,List<String> doList,List<String> territoryList);
    List<SclCustomerModel> getCurrentNetworkCustomersForTSMRH(RequestCustomerData requestCustomerData);
}

