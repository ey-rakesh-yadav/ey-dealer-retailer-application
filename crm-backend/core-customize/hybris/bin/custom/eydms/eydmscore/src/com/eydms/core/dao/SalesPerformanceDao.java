package com.eydms.core.dao;

import com.eydms.core.enums.CustomerCategory;
import com.eydms.core.enums.OrderType;
import com.eydms.core.enums.WarehouseType;
import com.eydms.core.jalo.PointRequisition;
import com.eydms.core.jalo.EyDmsUser;
import com.eydms.core.model.*;
import com.eydms.facades.data.FilterDistrictData;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface SalesPerformanceDao {
    Double getActualTargetForSalesMTD(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite);

    Double getActualTargetForSalesYTD(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite, Date startDate, Date endDate);

    Double getMonthlySalesTarget(EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite, String month, String year);
    Double getMonthlySalesTarget(EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite, String month, String year,List<String> doList,List<String> subAreaList);
    Double getMonthlySalesTargetForDealer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel currentBaseSite, String month, String year,String bgpFilter);
    Double getMonthlySalesTargetForSP(List<EyDmsCustomerModel> eydmsCustomer,String month, String year);
    Double getMonthlySalesTargetForRetailer(String retailerCode, BaseSiteModel currentBaseSite, String monthYear,String bgpFilter);

    Double getLastMonthSalesTarget(EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite, String month, String year);

    List<List<Object>> getAnnualSalesTarget(EyDmsUserModel eydmsUser, String financialYear);
    Double getAnnualSalesTargetForDealer(EyDmsCustomerModel eydmsCustomerModel, String financialYear,String bgpFilter);
    Double getAnnualSalesTargetForSP(List<EyDmsCustomerModel> eydmsCustomerModel, String financialYear);
    Double getAnnualSalesTargetForRetailer(EyDmsCustomerModel eydmsCustomerModel, String financialYear,String bgpFilter);
    List<DealerRevisedAnnualSalesModel> getAnnualSalesTargetForDealerWithBGP(EyDmsCustomerModel eydmsCustomerModel, String financialYear,String bgpFilter);
    List<RetailerRevisedAnnualSalesModel> getAnnualSalesTargetForRetailerWithBGP(EyDmsCustomerModel eydmsCustomerModel, String financialYear,String bgpFilter);

    List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatio(EyDmsUserModel eydmsUser, BaseSiteModel baseSite,EyDmsCustomerModel customer,List<String> doList,List<String> subAreaList);

    double getSalesQuantity(String customerNo, String startDate, String endDate, BaseSiteModel brand);
    
    List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForCustomer(EyDmsCustomerModel customer, BaseSiteModel baseSite, String customerType,List<String> doList,List<String> subAreaList);
    List<List<Object>> getBrandwiseSalesPercentRatioAndVolumeRatioForCustomer(EyDmsCustomerModel customer);
    List<List<Object>> getProductMixPercentRatioAndVolumeRatioWithPoints(EyDmsCustomerModel customer,String filter,Date startDate,Date endDate);

    List<String> getStateWiseProductForSummaryPage(String subArea, String catalogId, String version, String prodStatus);

    Double getSalesHistoryDataForDealer(int monthValue, int year, CustomerCategory category, BaseSiteModel currentBaseSite);

    Double getSalesHistoryDataForDealerWithSubArea(String subArea, int month, int year, CustomerCategory category, BaseSiteModel brand);

    List<List<Object>> getSalesHistoryModelList(String state,Date startDate,Date endDate,BaseSiteModel site);

    List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForYTD(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, Date startDate, Date endDate,EyDmsCustomerModel customer,List<String> doList,List<String> subAreaList);
    List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForYTD(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, Date startDate, Date endDate,List<String> doList,List<String> subAreaList);

    List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForMonthPicker(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, int month, int year,List<String> doList,List<String> subAreaList);

    Double getActualTargetSalesForSelectedMonthAndYear(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel baseSite, int month, int year);
    Double getActualTargetSalesForSelectedMonthAndYearForDealer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel baseSite, int month, int year,String bgpFilter);
    Double getActualTargetSalesForSelectedMonthAndYearForSP(List<EyDmsCustomerModel> eydmsCustomer, int month, int year);
   // Double getActualTargetSalesForSelectedMonthAndYear(EyDmsCustomerModel eydmsCustomer, BaseSiteModel baseSite, int month, int year,String bgpFilter);
    Double getActualTargetSalesForSelectedMonthAndYearForRetailer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel baseSite, int month, int year,String bgpFilter);
   Double getNCRThreshold(String state,BaseSiteModel siteModel,String yearMonth);
    Double getActualTargetSalesForSelectedMonthAndYear(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, int month, int year,List<String> doList,List<String> subAreaList);
    Double getActualTargetPartnerSalesForSelectedMonthAndYear(String code,EyDmsUserModel eydmsUser, BaseSiteModel baseSite, int month, int year,List<String> doList,List<String> subAreaList);
    Double getActualTargetForPartnerSalesMTD(String code,EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite,List<String> doList,List<String> subAreaList);
    Double getActualTargetForSalesMTD(EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite,List<String> doList,List<String> subAreaList);
    Double getActualTargetForSalesDealerMTD(EyDmsCustomerModel eydmsCustomer, BaseSiteModel currentBaseSite,String bgpFilter);
    Double getActualTargetForSalesSPMTD(List<EyDmsCustomerModel> eydmsCustomer);
    Double getActualTargetForSalesRetailerMTD(EyDmsCustomerModel eydmsCustomer, BaseSiteModel currentBaseSite,String bgpFilter);
    Double getActualTargetForSalesLastMonth(EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite,int year, int month,List<String> doList,List<String> subAreaList);
    Double getActualTargetForSalesYTD(EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite, Date startDate, Date endDate,List<String> doList,List<String> subAreaList);
    Double getActualTargetForPartnerSalesYTD(String code,EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite, Date startDate, Date endDate,List<String> doList,List<String> subAreaList);

    Double getActualTargetForSalesDealerYTD(EyDmsCustomerModel eydmsCustomer, BaseSiteModel currentBaseSite, Date startDate, Date endDate,String bgpFilter);
    Double getActualTargetForSalesRetailerYTD(EyDmsCustomerModel eydmsCustomer, BaseSiteModel currentBaseSite, Date startDate, Date endDate,String bgpFilter);

    Double getActualTargetForSalesLeaderYTD(EyDmsCustomerModel eydmsUser, BaseSiteModel currentBaseSite, Date startDate, Date endDate);

    Double getActualTargetForSalesLastYear(EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite, String startDate, String endDate);

    List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatio(EyDmsUserModel eydmsUser, BaseSiteModel baseSite,List<String> doList,List<String> subAreaList);
    Double getDealerOutstandingAmount(String customerNo);
    List<List<Object>> getPartnerDetailsForSales(String searchKeyWord);

    List<List<Object>> getSecondaryLeadDistanceForMonth(EyDmsUserModel eydmsUser,WarehouseType warehouseType, BaseSiteModel baseSite,Integer year1, Integer month1,List<String> doList, List<String> subAreaList);

    DestinationSourceMasterModel getDestinationSourceBySource(OrderType orderType, CustomerCategory customerCategory, WarehouseModel source, DeliveryModeModel deliveryMode, String city, String district, String state, BaseSiteModel brand, String grade, String packaging);
    List<List<Object>> getSecondaryLeadDistance(EyDmsUserModel eydmsUser, WarehouseType warehouseType,BaseSiteModel baseSite,List<String> doList, List<String> subAreaList);

    List<List<Object>> getSecondaryLeadDistanceMTD(EyDmsUserModel eydmsUser,WarehouseType warehouseType, BaseSiteModel baseSite,List<String> doList, List<String> subAreaList);

    List<List<Object>> getSecondaryLeadDistanceYTD(EyDmsUserModel eydmsUser, WarehouseType warehouseType,BaseSiteModel baseSite,Date startDate, Date endDate,List<String> doList, List<String> subAreaList);

    double getDistance(String source, String destination);
    Double getActualTargetForSalesMTDDealer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel currentBaseSite,String bgpFilter);
    Double getActualTargetForSalesMTDRetailer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel currentBaseSite,String bgpFilter);
    Double getActualTargetForSalesYTDDealer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel currentBaseSite,Date startDate,Date endDate,String bgpFilter);
    Double getActualTargetForSalesYTDSP(List<EyDmsCustomerModel> eydmsCustomer, BaseSiteModel currentBaseSite,Date startDate,Date endDate);
    Double getActualTargetForSalesYTDRetailer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel currentBaseSite,Date startDate,Date endDate,String bgpFilter);
    List<DealerRevisedMonthlySalesModel> getMonthlySalesTargetForDealerWithBGP(EyDmsCustomerModel eydmsCustomer, BaseSiteModel currentBaseSite, String month, String year, String bgpFilter);
    List<MonthWiseAnnualTargetModel> getMonthlySalesTargetForRetailerWithBGP(EyDmsCustomerModel eydmsCustomer, BaseSiteModel currentBaseSite, String month, String year,String bgpFilter);
    double getActualSaleForDealerGraphYTD(EyDmsCustomerModel customer, Date startDate, Date endDate, BaseSiteModel currentBaseSite, String bgpFilter);
    double getActualSaleForGraphYTD(EyDmsUserModel user, Date startDate, Date endDate, BaseSiteModel currentBaseSite, List<String> doList,List<String> subAreaList);
    double getActualTargetFor10DayBucketForDealer(EyDmsCustomerModel eydmsCustomer, String bgpFilter, Date startDate1, Date endDate1);
    double getActualTargetFor10DayBucket(EyDmsUserModel eydmsUser,Date startDate1, Date endDate1,List<String> doList,List<String>  subAreaList);

    ProductSaleModel getTotalTargetForProductBGPFilter(String customerCode, String productCode);
    ProductSaleModel getTotalTargetForProductBGPFilterMTD(String customerCode, String productCode, String formattedMonth, String s);

    DealerRevisedMonthlySalesModel getMonthlySaleTargetGraphForDealer(String customer, String month, String year);

    List<DealerRevisedMonthlySalesModel> getMonthlySaleTargetGraphForSP(List<EyDmsCustomerModel> eydmsCustomer, String month, String year);
    DealerRevisedMonthlySalesModel getMonthlySaleTargetGraph(EyDmsUserModel eydmsUserModel,String month, String year,List<String> doList,List<String> subAreaList);

    Integer findDirectDispatchOrdersMTDCount(UserModel currentUser, WarehouseType warehouseType, int month, int year,List<String> doList,List<String> subAreaList);

    Double getActualTargetForSalesLeaderYTDRetailer(EyDmsCustomerModel eydmsUser, BaseSiteModel currentBaseSite, Date startDate, Date endDate);


    List<List<Object>> getActualTargetForSalesLeader(DistrictMasterModel district,BaseSiteModel currentBaseSite, Date startDate, Date endDate);

    List<List<Object>> getPremiumForSalesLeader(DistrictMasterModel district, BaseSiteModel currentBaseSite, Date startDate, Date endDate);
    List<EyDmsCustomerModel> getOrderRequisitionSalesDataForRetailer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel brand,List<String> doList,List<String> subAreaList);
    EyDmsCustomerModel getRetailerSalesForDealer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel brand);
    EyDmsCustomerModel getRetailerSalesForDealerLowPerform(EyDmsCustomerModel eydmsCustomer, BaseSiteModel brand);
    List<DealerRetailerMapModel> getRetailerSalesForDealerZeroLift(EyDmsCustomerModel eydmsCustomer, BaseSiteModel brand);
    List<DealerInfluencerMapModel> getInfluencerSalesForDealer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel brand);
    List<DealerInfluencerMapModel> getInfluencerSalesForDealerLowPerform(EyDmsCustomerModel eydmsCustomer, BaseSiteModel brand);
    List<DealerInfluencerMapModel> getInfluencerSalesForDealerZeroLift(EyDmsCustomerModel eydmsCustomer, BaseSiteModel brand);
    List<EyDmsCustomerModel> getOrderRequisitionSalesDataForZero(EyDmsCustomerModel eydmsCustomer, BaseSiteModel brand,List<String> doList,List<String> subAreaList);
    List<EyDmsCustomerModel> getOrderRequisitionSalesDataForLowPerform(EyDmsCustomerModel eydmsCustomer, BaseSiteModel brand,List<String> doList,List<String> subAreaList);
    List<EyDmsCustomerModel> getPointRequisitionSalesDataForInfluencer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel brand,List<String> doList,List<String> subAreaList);
    List<EyDmsCustomerModel> getPointRequisitionSalesDataForZero(EyDmsCustomerModel eydmsCustomer, BaseSiteModel brand,List<String> doList,List<String> subAreaList);
    List<EyDmsCustomerModel> getPointRequisitionSalesDataForLowPerform(EyDmsCustomerModel eydmsCustomer, BaseSiteModel brand,List<String> doList,List<String> subAreaList);
    List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForMonthPickerForDealer(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, int month, int year,List<String> doList,List<String> subAreaList, EyDmsCustomerModel customer);
    List<EyDmsCustomerModel> getEyDmsCustomerLastLiftingList(List<EyDmsCustomerModel> customerFilteredList);

    List<EyDmsCustomerModel> getEyDmsCustomerZeroLiftingList(List<EyDmsCustomerModel> customerFilteredList);
    OrderRequisitionModel getEyDmsCustomerFromOrderReq(EyDmsCustomerModel customerModel);
    Double getRetailerFromOrderReq(EyDmsCustomerModel customerModel,Date startDate,Date endDate);

    OrderRequisitionModel getRetailerFromOrderReqDateConstraint(EyDmsCustomerModel customerModel,Date startDate,Date endDate);
    Double getInfluencerFromOrderReq(EyDmsCustomerModel customerModel,Date startDate,Date endDate);
    PointRequisitionModel getEyDmsCustomerFromPointReq(EyDmsCustomerModel customerModel);

    Double getActualTargetForPremiumSalesLeaderYTD(EyDmsCustomerModel so, BaseSiteModel currentBaseSite, Date startDate, Date endDate);

    Double getActualTargetForPremiumSalesLeaderYTDRetailer(EyDmsCustomerModel eydmsUser, BaseSiteModel currentBaseSite, Date startDate, Date endDate);

    Double getTotalCDAvailedForDealer(EyDmsCustomerModel so, Date startDate, Date endDate);

    Double getTotalCDLostForDealer(EyDmsCustomerModel so, Date startDate, Date endDate);

    double getActualTargetFor10DayBucketForSP(List<EyDmsCustomerModel> eydmsCustomer, Date startDate1, Date endDate1);

    Double getActualTargetSalesForSelectedMonthAndYearForMTDSP(List<EyDmsCustomerModel> eydmsCustomer, BaseSiteModel currentBaseSite, int month, int year);

    Double getMonthlySalesTargetForMtdSp(List<EyDmsCustomerModel> eydmsUser, BaseSiteModel currentBaseSite, String monthName, String yearName);

    double getActualSaleForDealerGraphYTDSP(List<EyDmsCustomerModel> eydmsCustomer, Date startDate, Date endDate, BaseSiteModel currentBaseSite);

    List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForCustomerMonthPicker(EyDmsCustomerModel eydmsUser, BaseSiteModel baseSite, int month, int year);

    double getMonthWiseForRetailerYTD(EyDmsCustomerModel eydmsCustomer, Date startDate1, Date endDate1);

    Double getActualTargetForSalesYTDRetailerList(EyDmsCustomerModel currentUser, BaseSiteModel currentBaseSite, String startDate, String endDate);

    Double getActualTargetForSalesRetailerMTDList(EyDmsCustomerModel currentUser, BaseSiteModel currentBaseSite);

    Double getActualTargetForSalesYTRetailerList(EyDmsCustomerModel currentUser, BaseSiteModel currentBaseSite, String startDate, String endDate, String bgpFilter);

    List<List<Object>> getActualTargetForSalesLeaderRetailer(SubAreaMasterModel district, BaseSiteModel currentBaseSite, Date startDate, Date endDate);

    List<List<Object>> getPremiumForSalesLeaderRetailer(SubAreaMasterModel district, BaseSiteModel currentBaseSite, Date startDate, Date endDate);

    DistrictMasterModel getDistrictForSP(FilterDistrictData filterDistrictData);

    List<EyDmsCustomerModel> getSPForDistrict(DistrictMasterModel districtForSP);

    List<EyDmsUserModel> getTsmByRegion(String district);

    List<EyDmsUserModel> getRHByState(String district);

    List<EyDmsCustomerModel> getCustomerForTsm(String district, EyDmsUserModel eydmsUserModel);

    List<EyDmsCustomerModel> getCustomerForRH(String district, EyDmsUserModel eydmsUserModel);

    List<List<Object>> getSalesDealerByDate(String district, EyDmsUserModel eydmsUserModel,Date startDate, Date endDate);

    List<List<Object>> getRHSalesDealerByDate(EyDmsUserModel eydmsUserModel,Date startDate, Date endDate);

    List<EyDmsCustomerModel> getCustomerForSp(EyDmsCustomerModel eydmsCustomer);

    Double getActualTargetForSalesSPMTDSearch(List<EyDmsCustomerModel> eydmsCustomer);

    List<List<Object>> getWeeklyOverallPerformance(String bgpFilter);

    List<List<Object>> getMonthlyOverallPerformance(String bgpFilter);
}
