package com.scl.core.dao;

import com.scl.core.enums.CounterType;
import com.scl.core.enums.CustomerCategory;
import com.scl.core.enums.OrderType;
import com.scl.core.enums.WarehouseType;
import com.scl.core.jalo.PointRequisition;
import com.scl.core.jalo.SclUser;
import com.scl.core.model.*;
import com.scl.facades.data.FilterDistrictData;
import com.scl.facades.data.RequestCustomerData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.c2l.BrandModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface SalesPerformanceDao {
    Double getActualTargetForSalesMTD(String subArea, SclUserModel sclUser, BaseSiteModel currentBaseSite);

    Double getActualTargetForSalesYTD(String subArea, SclUserModel sclUser, BaseSiteModel currentBaseSite, Date startDate, Date endDate);

    Double getMonthlySalesTarget(SclUserModel sclUser, BaseSiteModel currentBaseSite, String month, String year);
    Double getMonthlySalesTarget(SclUserModel sclUser, BaseSiteModel currentBaseSite, String month, String year,List<String> territoryList);
    Double getMonthlySalesTargetForDealer(SclCustomerModel sclCustomer, BaseSiteModel currentBaseSite, String month, String year,String bgpFilter);
    List<List<Object>> getMonthlySalesTargetForDealerList(List<SclCustomerModel> sclCustomer,String month, String year);
    public Double getMonthlySalesTargetForDealerList(List<B2BCustomerModel> sclCustomer, BaseSiteModel currentBaseSite, String month, String year, String bgpFilter) ;

    Double getMonthlySalesTargetForSP(List<SclCustomerModel> sclCustomer,String month, String year);
    Double getMonthlySalesTargetForRetailer(String retailerCode, BaseSiteModel currentBaseSite, String monthYear,String bgpFilter);
    Double getMonthlySalesTargetForRetailer(List<SclCustomerModel> retailerCode, BaseSiteModel currentBaseSite, String monthYear,String bgpFilter);
    List<List<Object>> getMonthlySalesTargetForRetailer(List<SclCustomerModel> retailerCode,  String monthYear);
    Double getLastMonthSalesTarget(SclUserModel sclUser, BaseSiteModel currentBaseSite, String month, String year);

    Double getAnnualSalesTarget(SclUserModel sclUser, String financialYear,List<String> territoryList);
    Double getAnnualSalesTargetForDealer(SclCustomerModel sclCustomerModel, String financialYear,String bgpFilter);
    Double getAnnualSalesTargetForSP(List<SclCustomerModel> sclCustomerModel, String financialYear);
    Double getAnnualSalesTargetForRetailer(SclCustomerModel sclCustomerModel, String financialYear,String bgpFilter);
    Double getAnnualSalesTargetForRetailer(List<SclCustomerModel> sclCustomerModel, String financialYear,String bgpFilter);
    List<DealerRevisedAnnualSalesModel> getAnnualSalesTargetForDealerWithBGP(SclCustomerModel sclCustomerModel, String financialYear,String bgpFilter);
    List<RetailerRevisedAnnualSalesModel> getAnnualSalesTargetForRetailerWithBGP(SclCustomerModel sclCustomerModel, String financialYear,String bgpFilter);

    List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatio(SclUserModel sclUser, BaseSiteModel baseSite,SclCustomerModel customer,List<String> doList,List<String> subAreaList);

    double getSalesQuantity(String customerNo, String startDate, String endDate, BaseSiteModel brand);
    
    List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForCustomer(SclCustomerModel customer, BaseSiteModel baseSite, String customerType,List<String> doList,List<String> subAreaList);
    List<List<Object>> getBrandwiseSalesPercentRatioAndVolumeRatioForCustomer(SclCustomerModel customer);
    List<List<Object>> getProductMixPercentRatioAndVolumeRatioWithPoints(SclCustomerModel customer,String filter,Date startDate,Date endDate);

    List<String> getStateWiseProductForSummaryPage(String subArea, String catalogId, String version, String prodStatus);

    Double getSalesHistoryDataForDealer(int monthValue, int year, CustomerCategory category, BaseSiteModel currentBaseSite);

    Double getSalesHistoryDataForDealerWithSubArea(String subArea, int month, int year, CustomerCategory category, BaseSiteModel brand);

    List<List<Object>> getSalesHistoryModelList(String state,Date startDate,Date endDate,BaseSiteModel site);

    List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForYTD(SclUserModel sclUser, BaseSiteModel baseSite, Date startDate, Date endDate,SclCustomerModel customer,List<String> doList,List<String> subAreaList);
    List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForYTD(SclUserModel sclUser, BaseSiteModel baseSite, Date startDate, Date endDate,List<String> doList,List<String> subAreaList);

    List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForMonthPicker(SclUserModel sclUser, BaseSiteModel baseSite, int month, int year,List<String> doList,List<String> subAreaList);

    Double getActualTargetSalesForSelectedMonthAndYear(String subArea, SclUserModel sclUser, BaseSiteModel baseSite, int month, int year);
    Double getActualTargetSalesForSelectedMonthAndYearForDealer(SclCustomerModel sclCustomer, BaseSiteModel baseSite, int month, int year,String bgpFilter);
    Double getActualTargetSalesForSelectedMonthAndYearForSP(List<SclCustomerModel> sclCustomer, int month, int year);
   // Double getActualTargetSalesForSelectedMonthAndYear(SclCustomerModel sclCustomer, BaseSiteModel baseSite, int month, int year,String bgpFilter);
    Double getActualTargetSalesForSelectedMonthAndYearForRetailer(SclCustomerModel sclCustomer, BaseSiteModel baseSite, int month, int year,String bgpFilter);
   Double getNCRThreshold(String state,BaseSiteModel siteModel,String yearMonth);
    Double getActualTargetSalesForSelectedMonthAndYear(SclUserModel sclUser, BaseSiteModel baseSite, int month, int year,List<String> doList,List<String> subAreaList);
    Double getActualTargetPartnerSalesForSelectedMonthAndYear(String code,SclUserModel sclUser, BaseSiteModel baseSite, int month, int year,List<String> doList,List<String> subAreaList);
    Double getActualTargetForPartnerSalesMTD(String code,SclUserModel sclUser, BaseSiteModel currentBaseSite,List<String> doList,List<String> subAreaList);
    Double getActualTargetForSalesMTD(SclUserModel sclUser, BaseSiteModel currentBaseSite,List<String> doList,List<String> subAreaList);
    Double getActualTargetForSalesDealerMTD(SclCustomerModel sclCustomer, BaseSiteModel currentBaseSite,String bgpFilter);
    Double getActualTargetForSalesSPMTD(List<SclCustomerModel> sclCustomer);
    Double getActualTargetForSalesRetailerMTD(SclCustomerModel sclCustomer, BaseSiteModel currentBaseSite,String bgpFilter);
    Double getActualTargetForSalesLastMonth(SclUserModel sclUser, BaseSiteModel currentBaseSite,int year, int month,List<String> doList,List<String> subAreaList);
    Double getActualTargetForSalesYTD(SclUserModel sclUser, BaseSiteModel currentBaseSite, Date startDate, Date endDate,List<String> doList,List<String> subAreaList);
    Double getActualTargetForPartnerSalesYTD(String code,SclUserModel sclUser, BaseSiteModel currentBaseSite, Date startDate, Date endDate,List<String> doList,List<String> subAreaList);

    Double getActualTargetForSalesDealerYTD(SclCustomerModel sclCustomer, BaseSiteModel currentBaseSite, Date startDate, Date endDate,String bgpFilter);
    Double getActualTargetForSalesRetailerYTD(SclCustomerModel sclCustomer, BaseSiteModel currentBaseSite, Date startDate, Date endDate,String bgpFilter);

    Double getActualTargetForSalesLeaderYTD(SclCustomerModel sclUser, BaseSiteModel currentBaseSite, Date startDate, Date endDate);

    Double getActualTargetForSalesLastYear(SclUserModel sclUser, BaseSiteModel currentBaseSite, String startDate, String endDate);

    List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatio(SclUserModel sclUser, BaseSiteModel baseSite,List<String> doList,List<String> subAreaList);
    Double getDealerOutstandingAmount(String customerNo);
    List<List<Object>> getPartnerDetailsForSales(String searchKeyWord);

    List<List<Object>> getSecondaryLeadDistanceForMonth(SclUserModel sclUser,WarehouseType warehouseType, BaseSiteModel baseSite,Integer year1, Integer month1, List<String> subAreaList);

    DestinationSourceMasterModel getDestinationSourceBySource(OrderType orderType, CustomerCategory customerCategory, WarehouseModel source, DeliveryModeModel deliveryMode, String city, String district, String state, BaseSiteModel brand, String grade, String packaging);
    List<List<Object>> getSecondaryLeadDistance(SclUserModel sclUser, WarehouseType warehouseType,BaseSiteModel baseSite, List<String> territoryList);

    List<List<Object>> getSecondaryLeadDistanceMTD(SclUserModel sclUser,WarehouseType warehouseType, BaseSiteModel baseSite, List<String> territoryList);

    List<List<Object>> getSecondaryLeadDistanceYTD(SclUserModel sclUser, WarehouseType warehouseType,BaseSiteModel baseSite,Date startDate, Date endDate,List<String> territoryList);

    double getDistance(String source, String destination);
    Double getActualTargetForSalesMTDDealer(SclCustomerModel sclCustomer, BaseSiteModel currentBaseSite,String bgpFilter);
    Double getActualTargetForSalesMTDRetailer(SclCustomerModel sclCustomer, BaseSiteModel currentBaseSite,String bgpFilter);
    Double getActualTargetForSalesYTDDealer(SclCustomerModel sclCustomer, BaseSiteModel currentBaseSite,Date startDate,Date endDate,String bgpFilter);
    Double getActualTargetForSalesYTDSP(List<SclCustomerModel> sclCustomer, BaseSiteModel currentBaseSite,Date startDate,Date endDate);
    Double getActualTargetForSalesYTDRetailer(SclCustomerModel sclCustomer, BaseSiteModel currentBaseSite,Date startDate,Date endDate,String bgpFilter);
    List<DealerRevisedMonthlySalesModel> getMonthlySalesTargetForDealerWithBGP(SclCustomerModel sclCustomer, BaseSiteModel currentBaseSite, String month, String year, String bgpFilter);
    List<MonthWiseAnnualTargetModel> getMonthlySalesTargetForRetailerWithBGP(SclCustomerModel sclCustomer, BaseSiteModel currentBaseSite, String month, String year,String bgpFilter);
    double getActualSaleForDealerGraphYTD(SclCustomerModel customer, Date startDate, Date endDate, BaseSiteModel currentBaseSite, String bgpFilter);
    double getActualSaleForGraphYTD(SclUserModel user, Date startDate, Date endDate, BaseSiteModel currentBaseSite, List<String> doList,List<String> subAreaList);
    double getActualTargetFor10DayBucketForDealer(SclCustomerModel sclCustomer, String bgpFilter, Date startDate1, Date endDate1);
    double getActualTargetFor10DayBucket(SclUserModel sclUser,Date startDate1, Date endDate1,List<String> doList,List<String>  subAreaList);

    ProductSaleModel getTotalTargetForProductBGPFilter(String customerCode, String productCode);
    ProductSaleModel getTotalTargetForProductBGPFilterMTD(String customerCode, String productCode, String formattedMonth, String s);

    DealerRevisedMonthlySalesModel getMonthlySaleTargetGraphForDealer(String customer, String month, String year);

    List<DealerRevisedMonthlySalesModel> getMonthlySaleTargetGraphForSP(List<SclCustomerModel> sclCustomer, String month, String year);
    DealerRevisedMonthlySalesModel getMonthlySaleTargetGraph(SclUserModel sclUserModel,String month, String year,List<String> doList,List<String> subAreaList);

    Integer findDirectDispatchOrdersMTDCount(UserModel currentUser, WarehouseType warehouseType, int month, int year,List<String> doList,List<String> subAreaList);

    Double getActualTargetForSalesLeaderYTDRetailer(SclCustomerModel sclUser, BaseSiteModel currentBaseSite, Date startDate, Date endDate);


    List<List<Object>> getActualTargetForSalesLeader(DistrictMasterModel district,BaseSiteModel currentBaseSite, Date startDate, Date endDate);

    List<List<Object>> getPremiumForSalesLeader(DistrictMasterModel district, BaseSiteModel currentBaseSite, Date startDate, Date endDate);
    List<SclCustomerModel> getOrderRequisitionSalesDataForRetailer(SclCustomerModel sclCustomer, BaseSiteModel brand,List<String> doList,List<String> subAreaList);
    SclCustomerModel getRetailerSalesForDealer(SclCustomerModel sclCustomer, BaseSiteModel brand);
    SclCustomerModel getRetailerSalesForDealerLowPerform(SclCustomerModel sclCustomer, BaseSiteModel brand);
    List<DealerRetailerMapModel> getRetailerSalesForDealerZeroLift(SclCustomerModel sclCustomer, BaseSiteModel brand);
    List<DealerInfluencerMapModel> getInfluencerSalesForDealer(SclCustomerModel sclCustomer, BaseSiteModel brand);
    List<DealerInfluencerMapModel> getInfluencerSalesForDealerLowPerform(SclCustomerModel sclCustomer, BaseSiteModel brand);
    List<DealerInfluencerMapModel> getInfluencerSalesForDealerZeroLift(SclCustomerModel sclCustomer, BaseSiteModel brand);
    List<SclCustomerModel> getOrderRequisitionSalesDataForZero(SclCustomerModel sclCustomer, BaseSiteModel brand,List<String> doList,List<String> subAreaList);
    List<SclCustomerModel> getOrderRequisitionSalesDataForLowPerform(SclCustomerModel sclCustomer, BaseSiteModel brand,List<String> doList,List<String> subAreaList);
    List<SclCustomerModel> getPointRequisitionSalesDataForInfluencer(SclCustomerModel sclCustomer, BaseSiteModel brand,List<String> doList,List<String> subAreaList);
    List<SclCustomerModel> getPointRequisitionSalesDataForZero(SclCustomerModel sclCustomer, BaseSiteModel brand,List<String> doList,List<String> subAreaList);
    List<SclCustomerModel> getPointRequisitionSalesDataForLowPerform(SclCustomerModel sclCustomer, BaseSiteModel brand,List<String> doList,List<String> subAreaList);
    List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForMonthPickerForDealer(SclUserModel sclUser, BaseSiteModel baseSite, int month, int year,List<String> doList,List<String> subAreaList, SclCustomerModel customer);
    List<SclCustomerModel> getSclCustomerLastLiftingList(List<SclCustomerModel> customerFilteredList);

    List<SclCustomerModel> getSclCustomerZeroLiftingList(List<SclCustomerModel> customerFilteredList);
    OrderRequisitionModel getSclCustomerFromOrderReq(SclCustomerModel customerModel);
    Double getRetailerFromOrderReq(SclCustomerModel customerModel,Date startDate,Date endDate);

    OrderRequisitionModel getRetailerFromOrderReqDateConstraint(SclCustomerModel customerModel,Date startDate,Date endDate);
    Double getInfluencerFromOrderReq(SclCustomerModel customerModel,Date startDate,Date endDate);
    PointRequisitionModel getSclCustomerFromPointReq(SclCustomerModel customerModel);

    Double getActualTargetForPremiumSalesLeaderYTD(SclCustomerModel so, BaseSiteModel currentBaseSite, Date startDate, Date endDate);

    Double getActualTargetForPremiumSalesLeaderYTDRetailer(SclCustomerModel sclUser, BaseSiteModel currentBaseSite, Date startDate, Date endDate);

    Double getTotalCDAvailedForDealer(SclCustomerModel so, Date startDate, Date endDate);

    Double getTotalCDLostForDealer(SclCustomerModel so, Date startDate, Date endDate);

    double getActualTargetFor10DayBucketForSP(List<SclCustomerModel> sclCustomer, Date startDate1, Date endDate1);

    Double getActualTargetSalesForSelectedMonthAndYearForMTDSP(List<SclCustomerModel> sclCustomer, BaseSiteModel currentBaseSite, int month, int year);

    Double getMonthlySalesTargetForMtdSp(List<SclCustomerModel> sclUser, BaseSiteModel currentBaseSite, String monthName, String yearName);

    double getActualSaleForDealerGraphYTDSP(List<SclCustomerModel> sclCustomer, Date startDate, Date endDate, BaseSiteModel currentBaseSite);

    List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForCustomerMonthPicker(SclCustomerModel sclUser, BaseSiteModel baseSite, int month, int year,List<String> subAreaList,List<String> districtMaster);

    double getMonthWiseForRetailerYTD(SclCustomerModel sclCustomer, Date startDate1, Date endDate1);

    Double getActualTargetForSalesYTDRetailerList(SclCustomerModel currentUser, BaseSiteModel currentBaseSite, String startDate, String endDate);

    Double getActualTargetForSalesRetailerMTDList(SclCustomerModel currentUser, BaseSiteModel currentBaseSite);

    Double getActualTargetForSalesYTRetailerList(SclCustomerModel currentUser, BaseSiteModel currentBaseSite, String startDate, String endDate, String bgpFilter);

    List<List<Object>> getActualTargetForSalesLeaderRetailer(SubAreaMasterModel district, BaseSiteModel currentBaseSite, Date startDate, Date endDate);

    List<List<Object>> getPremiumForSalesLeaderRetailer(SubAreaMasterModel district, BaseSiteModel currentBaseSite, Date startDate, Date endDate);

    DistrictMasterModel getDistrictForSP(FilterDistrictData filterDistrictData);

    List<SclCustomerModel> getSPForDistrict(DistrictMasterModel districtForSP);

    List<SclUserModel> getTsmByRegion(String district);

    List<SclUserModel> getRHByState(String district);

    List<SclCustomerModel> getCustomerForTsm(String district, SclUserModel sclUserModel);

    List<SclCustomerModel> getCustomerForRH(String district, SclUserModel sclUserModel);

    List<List<Object>> getSalesDealerByDate(String district, SclUserModel sclUserModel,Date startDate, Date endDate);

    List<List<Object>> getRHSalesDealerByDate(SclUserModel sclUserModel,Date startDate, Date endDate);

    List<SclCustomerModel> getCustomerForSp(SclCustomerModel sclCustomer);

    Double getActualTargetForSalesSPMTDSearch(List<SclCustomerModel> sclCustomer);

    List<List<Object>> getWeeklyOverallPerformance(String bgpFilter);

    List<List<Object>> getMonthlyOverallPerformance(String bgpFilter);

    ReceiptAllocaltionModel getDealerAllocation(ProductModel productCode, SclCustomerModel dealerCode);
    Map<String, Object> findMaxInvoicedDateAndQunatityDeliveryItem(final UserModel user);
    Map<String, Object> getPotentialForCustomer(SclCustomerModel sclCustomer,String startDate,String endDate);
    Map<String, Object> getSelfBrandSaleforCustomer(SclCustomerModel sclCustomer, String countervistId, BrandModel brand);
    List<SclCustomerModel> getCurrentNetworkCustomers(String counterType,String networkType,List<TerritoryMasterModel> territoryMasterModels,String searchKeyFilter, boolean sclExclusiveCustomer,boolean isZeroLift,boolean isLowPerform);
    List<SclCustomerModel> getAllNonSclCustomers(List<SubAreaMasterModel> subAreaMasterModels,String subAreaMasterModel,String route,RequestCustomerData customerData);
    Map<String, Object> findMaxInvoicedDateAndQuantityForRetailer(final UserModel user,SclCustomerModel dealer);
    B2BUnitModel getB2BUnitPk(String unitUid);
    List<SclCustomerModel> getCurrentNetworkCustomersForDJP(String counterType,List<TerritoryMasterModel> territoryMasterModels, boolean sclExclusiveCustomer,String subAreaMasterId,String routeId,String searchKeyFilter);
    List<SclCustomerModel> getCurrentNetworkCustomersForTSMRH(RequestCustomerData requestCustomerData);
}
