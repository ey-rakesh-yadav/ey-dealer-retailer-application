package com.eydms.core.dao;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import com.eydms.core.enums.CustomerCategory;
import com.eydms.core.enums.SecurityDepositStatus;
import com.eydms.core.enums.SiteCustomerType;
import com.eydms.core.model.*;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

public interface DJPVisitDao {
	
	SearchPageData<VisitMasterModel> getMarketVisitDetails(SearchPageData searchPageData, UserModel user);
	VisitMasterModel getCounterList(String id, UserModel user);
	SearchPageData<VisitMasterModel> getReviewLogs(SearchPageData searchPageData, UserModel user, Date startDate, Date endDate);
	Long getCountOfTotalJouneyPlanned(UserModel user);		
	List<List<Object>> getAvgTimeSpent(UserModel user, Date startDate, Date endDate);
	List<Date> getLastSixCounterVisitDates(EyDmsCustomerModel customer);
	List<List<Object>> counterVisitedForSelectedRoutes(RouteMasterModel route, UserModel user);

	List<VisitMasterModel> getCompletedPlannedVisitsBetweenDatesForSO(final EyDmsUserModel eydmsUserModel , final Date startDate , final Date endDate);

	Date getLastVisitDate(EyDmsCustomerModel eydmsCustomer);
	Integer getVisitCountMTD(EyDmsCustomerModel eydmsCustomer, int month, int year);

	List<DJPCounterScoreMasterModel> getRecommendedPlanVisitForSalesOfficer(final EyDmsUserModel eydmsUserModel , final Date planStartDate, final Date planEndDate);
	
	Double getDealerOutstandingAmount(String customer);
	
	List<List<Object>> getCounterSharesForDealerOrRetailer(String userId, int month, int year);
	
	Double getDealerCreditLimit(String customer);
	
	List<List<Double>> getOutstandingBucketsForDealer(String customer);
	
	List<OrderModel> getLastAcceptedOrderForDealer(String customer);
	
	List<OrderModel> getLastAcceptedOrderForRetailer(String customer);
	
	List<VisitMasterModel> getPlannedVisitForToday(UserModel user, String plannedDate);

	Double getTotalOrderGenerated(EyDmsCustomerModel eydmsCustomer, CounterVisitMasterModel counterVisit);
  
    Double getSalesHistoryData(String customerNo, int monthValue, int year, CustomerCategory category, BaseSiteModel brand);
	ObjectiveModel findOjectiveById(String objectiveId);
	List<DJPRouteScoreMasterModel> findAllRouteForPlannedDate(DJPRunMasterModel djpRun);

	double getSalesHistoryDataFor360(String customerNo, BaseSiteModel brand, String transactionType, int month, int year);
	Date getLastLiftingDateForDealer(String customerNo, BaseSiteModel brand);

	Double getLastLiftingQuantityForDealer(String customerNo, BaseSiteModel brand, Date maxDate);

	Date getLastLiftingDateForRetailerOrInfluencer(String customerNo, BaseSiteModel brand, String transactionType);

	Double getLastLiftingQuantityForRetailerOrInfluencer(String customerNo, BaseSiteModel brand, Date maxDate, String transactionType);

	Double getAvgSalesDataForDealer(String customerNo, Date startDate, Date endDate, CustomerCategory category, BaseSiteModel brand);

	Double getSalesHistoryDataForDealer(String customerNo, int monthValue, int year, CustomerCategory category, BaseSiteModel brand);

	Double getSalesTargetFor360(String customerNo, String customerType, int monthValue, int year);

	Double getOrderCapturedForCounter(String customerNo, Date startDate, Date endDate);

	Double getPreviousCounterPotentialForCounter(Date currentDate);

	List<List<Object>> getAllRoutesForSO(List<SubAreaMasterModel> subAreas);

	Integer getDealerToRetailerNetwork(String customerNo);

	Integer getDealerToInfluencerNetwork(String customerNo);

	Integer getRetailerToInfluencerNetwork(String customerNo);
	
	Double getOutstandingAmountBetweenDates(String customerCode, Date startDate, Date endDate);

	List<DealerRetailerMapModel> getDealerRetailerMappingRecords(Date startDate, Date endDate);

	List<DealerInfluencerMapModel> getDealerInfluMappingRecords(Date startDate, Date endDate);

	List<RetailerInfluencerMapModel> getRetailerInfluMappingRecords(Date startDate, Date endDate);

	List<List<String>> getMappingRecordsByTransType();
	List<VisitMasterModel> getAllVisit(UserModel user, Date startDate, Date endDate);
	
	Integer getVisitCountBetweenDates(EyDmsCustomerModel eydmsCustomer, Date startDate, Date endDate);
	
	SecurityDepositStatus getSecurityDepositStatusForDealer(String customerNo);
	
	Double getSalesHistoryDataForDealerList(List<String> customerNos, int monthValue, int year, CustomerCategory category, BaseSiteModel brand);

	Double getDailyAverageSales(String customer);
	Date getLastLiftingDateForRetailerFromOrderReq(EyDmsCustomerModel customerNo, BaseSiteModel brand, String transactionType);
	Double getLastLiftingQuantityForRetailerFromOrderReq(EyDmsCustomerModel customerNo, BaseSiteModel brand, Date maxDate, String transactionType);
	Date getLastLiftingDateForInfluencerFromPointReq(EyDmsCustomerModel EyDmsCustomerModel, BaseSiteModel brand, String transactionType);
	Double getLastLiftingQuantityForInfluencerFromPointReq(EyDmsCustomerModel customerNo, BaseSiteModel brand, Date maxDate, String transactionType);

    Integer getPendingApprovalVisitsCountForTsmorRh(EyDmsUserModel currentUser);

	Integer flaggedDealerCount(List<SubAreaMasterModel> subAreaMaster);
	Integer unFlaggedDealerRequestCount(List<SubAreaMasterModel> subAreaMaster);

	VisitMasterModel updateStatusForApprovalByTsm(String visitId);

	VisitMasterModel updateStatusForRejectedByTsm(String visitId);

    List<List<Object>> getCompletedPlannedVisitsByApprovalStatus();
	
	SearchPageData<VisitMasterModel> getReviewLogsForTSM(SearchPageData searchPageData, EyDmsUserModel user, Date startDate, Date endDate, String searchKey);
	SearchPageData<VisitMasterModel> getReviewLogsForRH(SearchPageData searchPageData, EyDmsUserModel user, Date startDate, Date endDate, String searchKey);

    List<VisitMasterModel> getVisitsForDJPUnflaggedSLA(Date date);
}
