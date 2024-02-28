package com.eydms.core.services;

import com.eydms.core.model.*;
import com.eydms.facades.data.*;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface SalesPerformanceService {
    Double getActualTargetForSalesMTD(String sublArea, EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite);
    Double getActualTargetForSalesYTD(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite);
    Double getMonthlySalesTarget(EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite);
    Double getMonthlySalesTargetForDealer(EyDmsCustomerModel eydmsCustomerModel, BaseSiteModel currentBaseSite,String bgpFilter);
    Double getMonthlySalesTargetForSP(List<EyDmsCustomerModel> eydmsCustomerModel);
    Double getMonthlySalesTargetForRetailer(String retailerCode, BaseSiteModel currentBaseSite,String bgpFilter, String monthYear);
    Double getLastMonthSalesTarget(EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite);
    Double getMonthlySalesForPartnerTarget(String code, EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite);
    Double getAnnualSalesTarget(EyDmsUserModel eydmsUser);
    Double getAnnualSalesTargetForDealer(EyDmsCustomerModel eydmsCustomerModel,String bgpFilter);
    Double getAnnualSalesTargetForSP(List<EyDmsCustomerModel> eydmsCustomerModel);
    Double getAnnualSalesTargetForRetailer(EyDmsCustomerModel eydmsCustomerModel,String bgpFilter);
    Double getLastYearSalesTarget(EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite);
    Double getAnnualSalesForPartnerTarget(String code, EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite);
    List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatio(EyDmsUserModel eydmsUser, BaseSiteModel baseSite,EyDmsCustomerModel customer,List<String> doList,List<String> subAreaList);

    SalesPerformNetworkDetailsListData getListOfAllLowPerforming(String fields,String leadType,String searchKey,List<String> doList, List<String> subAreaList);
    SalesPerformNetworkDetailsListData getListOfAllLowPerformingRetailerInfForDealers(String fields,String leadType,String searchKey);
    SearchPageData<SalesPerformNetworkDetailsData> getLowPerformingViewDetailsWithPagination(String fields, BaseSiteModel site,String customerType, String searchKey, List<String> doList, List<String> subAreaList, SearchPageData searchPageData);
    Double getPotentialForDealersNetwork(String subArea, BaseSiteModel baseSite);
    SalesPerformNetworkDetailsListData getListOfAllDealerRetailerInfluencers(String fields,BaseSiteModel currentBaseSite,String leadType,String filter,List<String> doList, List<String> subAreaList);
    SalesPerformNetworkDetailsListData getListOfAllRetailerInfluencersForDealer(String fields,BaseSiteModel currentBaseSite,String leadType,String filter,List<String> doList, List<String> subAreaList);
    SalesPerformNetworkDetailsListData getListOfAllZeroLifting(String fields,BaseSiteModel currentBaseSite,String leadType,String searchKey,List<String> doList, List<String> subAreaList);
    SalesPerformNetworkDetailsListData getListOfAllZeroLiftingRetailerInfForDealer(String fields,String leadType,String searchKey);
    Double getSalesByDeliveryDate(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite, Date startDate, Date endDate);
    public List<Date> getCurrentFinancialYear();

    public List<Date> getCurrentFinancialYearSales();
    List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForCustomer(EyDmsCustomerModel customer, BaseSiteModel baseSite, String customerType,List<String> doList,List<String> subAreaList);
    List<List<Object>> getBrandwiseSalesPercentRatioAndVolumeRatioForCustomer(EyDmsCustomerModel customer);
    List<List<Object>> getProductMixPercentRatioAndVolumeRatioWithPoints(EyDmsCustomerModel customer,String filter);
    Integer getCountForAllDealerRetailerInfluencers(String leadType, B2BCustomerModel currentUser, BaseSiteModel currentBaseSite,List<String> subAreaList,List<String> doList);
    Integer getCountOfAllRetailersInfluencers(String leadType);
    List<String> getStateWiseProductForSummaryPage(String subArea);
    SalesPerformNetworkDetailsListData getBottomLaggingCounters(List<String> doList, List<String> subAreaList);
    SalesPerformamceCockpitSaleData getSalesHistoryForDealers(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite);

    Double getNCRThreshold(String state,BaseSiteModel site,String yearMonth);
    List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForYTD(EyDmsUserModel eydmsUser, BaseSiteModel baseSite,EyDmsCustomerModel customer,List<String> doList,List<String> subAreaList);

    List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForMonthPicker(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, int month, int year,List<String> doList,List<String> subAreaList);

    List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForCustomerMonthPicker(EyDmsCustomerModel eydmsUser, BaseSiteModel baseSite, int month, int year);

    Double getActualTargetSalesForSelectedMonthAndYear(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel baseSite, int month, int year);


    LowPerformingNetworkData getLowPerformingSummaryData(int month, int year, String filter,  BaseSiteModel site, String SOFilter,List<String> doList, List<String> subAreaList);
    LowPerformingNetworkData getLowPerformingSummaryDataDealer(String SOFilter);

    Double getActualTargetForSalesForMonth(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite, int year, int month);

    Double getActualTargetForSalesForMonth(EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite, int year, int month,List<String> doList,List<String> subAreaList);
    Double getActualTargetForSalesForMonthDealer(EyDmsCustomerModel eydmsUser, BaseSiteModel currentBaseSite, int year, int month,String bgpFilter);

    Double getActualTargetForSalesForMtdSp(List<EyDmsCustomerModel> eydmsUser, BaseSiteModel currentBaseSite, int year, int month);
    Double getActualTargetForSalesForMonthSP(List<EyDmsCustomerModel> eydmsUser, int year, int month);
    //Double getActualTargetForSalesForMonth(EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite, int year, int month);
    Double getActualTargetForSalesForMonthRetailer(EyDmsCustomerModel eydmsUser, BaseSiteModel currentBaseSite, int year, int month,String bgpFilter);
    Double getActualTargetForPartnerSalesForMonth(String code,EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite, int year, int month,List<String> doList,List<String> subAreaList);

    Double getSalesTargetForMonth(EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite, Integer year, Integer month);
    Double getSalesTargetForMonth(EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite, Integer year, Integer month,List<String> soList,List<String> subAreaList);
    Double getSalesTargetForMonthDealer(EyDmsCustomerModel eydmsUser, BaseSiteModel currentBaseSite, Integer year, Integer month,String bgpFilter);

    Double getSalesTargetForMtdSP(List<EyDmsCustomerModel> eydmsUser, BaseSiteModel currentBaseSite, Integer year, Integer month);
    Double getSalesTargetForMonthSP(List<EyDmsCustomerModel> eydmsCustomerModel, Integer year, Integer month);

    Double getSalesTargetForMonthRetailer(String retailerCode, BaseSiteModel currentBaseSite, Integer year, Integer month,String bgpFilter);

    Double getSalesTargetForPartnerMonth(String code, EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite, Integer year, Integer month);

   // ZeroLiftingNetworkData getZeroLiftingSummaryData(int month, int year, String filter, BaseSiteModel site, String SOFilter);
    Double getActualTargetForSalesMTD(EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite, List<String> doList,List<String> subAreaList);

    ZeroLiftingNetworkData getZeroLiftingSummaryData(int month, int year, String filter, BaseSiteModel site, String SOFilter,List<String> doList, List<String> subAreaList);
    ZeroLiftingNetworkData getZeroLiftingSummaryDataDealer(String leadType);

    Double getActualTargetForSalesMTDDealer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel currentBaseSite,String bgpFilter);
    Double getActualTargetForSalesMTDRetailer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel currentBaseSite,String bgpFilter);
    Double getActualTargetForSalesDealerMTD(EyDmsCustomerModel eydmsCustomer, BaseSiteModel currentBaseSite,String bgpFilter);
    Double getActualTargetForSalesSPMTD(List<EyDmsCustomerModel> eydmsCustomer);

    Double getActualTargetForSalesSPMTDSearch(List<EyDmsCustomerModel> eydmsCustomer);
    Double getActualTargetForSalesRetailerMTD(EyDmsCustomerModel eydmsCustomer, BaseSiteModel currentBaseSite,String bgpFilter);
    Double getActualTargetForSalesLastMonth(EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite,int year, int month,List<String> doList,List<String> subAreaList);
    Double getActualTargetForSalesYTD(EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite,List<String> doList,List<String> subAreaList);
    Double getActualTargetForSalesYTDDealer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel currentBaseSite,String bgpFilter);
    Double getActualTargetForSalesYTDSP(List<EyDmsCustomerModel> eydmsCustomer, BaseSiteModel currentBaseSite);
    Double getActualTargetForSalesYTDRetailer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel currentBaseSite,String bgpFilter);
    Double getActualTargetForSalesDealerYTD(EyDmsCustomerModel eydmsCustomer, BaseSiteModel currentBaseSite,String bgpFilter);
    Double getActualTargetForSalesRetailerYTD(EyDmsCustomerModel eydmsCustomer, BaseSiteModel currentBaseSite,String bgpFilter);
    Double getActualTargetForSalesLastYear(EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite,List<String> doList,List<String> subAreaList);
    Double getActualTargetForPartnerSalesMTD(String code,EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite,List<String> doList,List<String> subAreaList);
    Double getActualTargetForPartnerSalesYTD(String code,EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite,List<String> doList,List<String> subAreaList);
    Double getActualTargetSalesForSelectedMonthAndYear(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, int month, int year,List<String> doList,List<String> subAreaList);

    List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForYTD(EyDmsUserModel eydmsUser, BaseSiteModel baseSite,List<String> doList,List<String> subAreaList);

    List<EyDmsCustomerModel> filterEyDmsCustomersWithSearchTerm(List<EyDmsCustomerModel> customers,String searchTerm);
    Double getActualDealerCounterShareMarket(EyDmsCustomerModel dealer,BaseSiteModel site,List<String> doList, List<String> subAreaList);

    Double getSalesByDeliveryDate(EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite, Date startDate, Date endDate,List<String> doList,List<String> subAreaList);

    Double getSalesLeaderByDeliveryDate(EyDmsCustomerModel eydmsUser, BaseSiteModel currentBaseSite, Date startDate, Date endDate);

    List<List<Object>> getSalesLeaderByDate(DistrictMasterModel district,BaseSiteModel currentBaseSite, Date startDate, Date endDate);

    List<List<Object>> getSalesLeaderByDateRetailer(SubAreaMasterModel district,BaseSiteModel currentBaseSite, Date startDate, Date endDate);

    List<List<Object>> getPremiumSalesLeaderByDate(DistrictMasterModel district, BaseSiteModel currentBaseSite, Date startDate, Date endDate);

    List<List<Object>> getPremiumSalesLeaderByDateRetailer(SubAreaMasterModel district, BaseSiteModel currentBaseSite, Date startDate, Date endDate);

    List<List<Object>> getSalesHistoryModelList(String state, Date statDate,Date endDate, BaseSiteModel siteModel);
    List<List<Object>> getPartnerDetailsForSales(String searchKeyWord);

    double getSecondaryLeadDistanceForMonth(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, Integer year1, Integer month1,List<String> doList, List<String> subAreaList);

    double getSecondaryLeadDistance(EyDmsUserModel eydmsUser, BaseSiteModel baseSite,List<String> doList, List<String> subAreaList);

    double getSecondaryLeadDistanceMTD(EyDmsUserModel eydmsUser, BaseSiteModel baseSite,List<String> doList, List<String> subAreaList);

    double getSecondaryLeadDistanceYTD(EyDmsUserModel eydmsUser, BaseSiteModel baseSite,List<String> doList, List<String> subAreaList);

    DealerRevisedMonthlySalesModel getMonthlySaleTargetGraphForDealer(String customer, String monthName, String yearName);

    List<DealerRevisedMonthlySalesModel> getMonthlySaleTargetGraphForSP(List<EyDmsCustomerModel> eydmsCustomer, String monthName, String yearName);
    DealerRevisedMonthlySalesModel getMonthlySaleTargetGraph(EyDmsUserModel user,String monthName, String yearName,List<String> doList,List<String> subAreaList);

    double getActualSaleForDealerGraphYTD(EyDmsCustomerModel customer, Date startDate, Date endDate, BaseSiteModel currentBaseSite,String bgpFilter);

    double getActualSaleForDealerGraphYTDSP(List<EyDmsCustomerModel> eydmsCustomer, Date startDate, Date endDate, BaseSiteModel currentBaseSite);
    double getActualSaleForGraphYTD(EyDmsUserModel eydmsUserModel, Date startDate, Date endDate, BaseSiteModel currentBaseSite,List<String> doList,List<String> subAreaList);

    double getActualTargetFor10DayBucketForDealer(EyDmsCustomerModel eydmsCustomer, String bgpFilter, Date startDate1, Date endDate1);

    double getActualTargetFor10DayBucketForSP(List<EyDmsCustomerModel>  eydmsCustomer,Date startDate1, Date endDate1);
    double getActualTargetFor10DayBucket(EyDmsUserModel eydmsUser, Date startDate1, Date endDate1,List<String> doList,List<String> subAreaList);

    Map<String, Object> getDirectDispatchOrdersMTDPercentage(int month, int year,List<String> doList, List<String> subAreaList);
    String getDaysSinceLastLiftingForRetailer(List<EyDmsCustomerModel> saleQty);
    String getDaysSinceLastLiftingForInfluencer(List<EyDmsCustomerModel> saleQty);
    Double getSalesLeaderByDeliveryDateRetailer(EyDmsCustomerModel eydmsUser, BaseSiteModel currentBaseSite, Date startDate, Date endDate);
    SearchPageData<SalesPerformNetworkDetailsData> getZeroLiftingViewDetailsWithPagination(String fields, BaseSiteModel site, String customerType, String searchKey, List<String> doList, List<String> subAreaList, SearchPageData searchPageData);

    Double getPremiumSalesLeaderByDeliveryDate(EyDmsCustomerModel so, BaseSiteModel currentBaseSite, Date startDate, Date endDate);

    Double getPremiumSalesLeaderByDeliveryDateRetailer(EyDmsCustomerModel so, BaseSiteModel currentBaseSite, Date startDate, Date endDate);

    Double getCashDiscountAvailedPercentage(EyDmsCustomerModel so, BaseSiteModel currentBaseSite, Date startDate, Date endDate);

    Double getMonthlySalesTargetForRetailer(String uid, BaseSiteModel currentBaseSite, String bgpFilter);

    Double getActualTargetForSalesYTDRetailerList(EyDmsCustomerModel currentUser, BaseSiteModel currentBaseSite);

    Double getActualTargetForSalesMTDRetailerList(EyDmsCustomerModel currentUser, BaseSiteModel currentBaseSite);

    Double getActualTargetForSalesYTRetailer(EyDmsCustomerModel currentUser, BaseSiteModel currentBaseSite, String bgpFilter);

    DistrictMasterModel getDistrictForSP(FilterDistrictData filterDistrictData);

    List<EyDmsCustomerModel> getSPForDistrict(DistrictMasterModel districtForSP);

    Double getAnnualSalesTargetForDealerFY(EyDmsCustomerModel eydmsCustomerModel,String bgpFilter, String financialYear);

    Double getAnnualSalesTargetForRetailerFY(EyDmsCustomerModel eydmsCustomerModel,String bgpFilter, String financialYear);

    List<EyDmsUserModel> getTsmByRegion(String district);

    List<EyDmsUserModel> getRHByState(String district);

    List<EyDmsCustomerModel> getCustomerForTsm(String district, EyDmsUserModel eydmsUserModel);

    List<EyDmsCustomerModel> getCustomerForRH(String district, EyDmsUserModel eydmsUserModel);

    List<List<Object>> getSalesDealerByDate(String district, EyDmsUserModel eydmsUserModel,Date startDate, Date endDate);

    List<List<Object>> getRHSalesDealerByDate(EyDmsUserModel eydmsUserModel,Date startDate, Date endDate);

    List<EyDmsCustomerModel> getCustomerForSp(EyDmsCustomerModel eydmsCustomer);

    List<List<Object>> getWeeklyOverallPerformance(String bgpFilter);

    List<List<Object>> getMonthlyOverallPerformance(String bgpFilter);
}

