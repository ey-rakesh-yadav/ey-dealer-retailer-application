package com.scl.core.dao;

import com.scl.core.model.*;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;

public interface SclUserDao {
	
	double getOutstandingAmountForSO(List<String> customerCode);
	List<List<Double>> getOutstandingBucketsForSO(List<String> customerCode);
	
	List<SclUserModel> getAllActiveSO();
	Double getCustomerSaleQTY(String customerUID,  BaseSiteModel site);
	SclCustomerModel getCustomerModelAndOrdersForSpecificDate(String customerUID,BaseSiteModel site,String startDate,String endDate);
	List<SclCustomerModel> getAllCustomerForSubArea(String subArea, BaseSiteModel site);
	List<CustomerSubAreaMappingModel> getAllDistrictForSO(UserModel sclUser, BaseSiteModel site);
	double getSalesQuantity(String customerNo, String currentDate, String financialDate);
	double getCustomerTarget(String customerNo, String month, String year);
	List<SclCustomerModel> getAllCustomersForSubAreaByOnboardingStatus(BaseSiteModel site, String onboardingStatus);
	List<SclCustomerModel> getCustomerwithoutCustomerNumber(BaseSiteModel site);
	/**
	 *
	 * @param currentUser
	 * @return
	 */
	List<MeetingScheduleModel> getInfluencerMeetCards(UserModel currentUser);
	double getSalesQuantityForSalesPerformance(String customerUID, String startDate, String endDate, List<String> doList, List<String> subAreaList);
	double getSalesQuantityForBottomLogging(String customerUID, String startDate, String endDate, List<String> doList, List<String> subAreaList);
	AddressModel getAddressByErpId(String erpAddressId, SclCustomerModel customer);
	List<String> filterAddressByLpSource(List<String> stateDistrictTalukaList);

	List<SclCustomerModel> getCountOfCreditLimitBreachedUser(List<SubAreaMasterModel> subAreas, String string);
	AddressModel getAddressByPk(String pk);
	AddressModel getDealerAddressByRetailerPk(String retailerAddressPk, SclCustomerModel customer);
	
	Integer getDealersCountForDSOGreaterThanThirty(List<String> customerNos);
	
	double getSalesQuantityForCustomerList(List<String> customerNos, String startDate, String endDate, BaseSiteModel brand);

	List<SclUserModel> getUserListForEmptyUserSubArea();

	List<SclUserModel> getUserListForTerritoryUserMap();

	List<UserSubAreaMappingModel> getUserSubareaListForUpdatedByJob();

	List<TerritoryMasterModel> getTMListForTerritoryUserMap(SclUserModel su);
	DealerRetailerMappingModel getDealerRetailerMapping(SclCustomerModel dealer, SclCustomerModel retailer,String partnerFunctionId);

	List<SclUserModel> getSCLUserListBasedOnType(String type, Date date2);
}
