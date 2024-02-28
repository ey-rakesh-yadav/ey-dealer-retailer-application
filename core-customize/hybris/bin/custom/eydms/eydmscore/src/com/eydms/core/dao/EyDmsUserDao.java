package com.eydms.core.dao;

import com.eydms.core.model.*;
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

public interface EyDmsUserDao {
	
	double getOutstandingAmountForSO(List<String> customerCode);
	List<List<Double>> getOutstandingBucketsForSO(List<String> customerCode);
	
	List<EyDmsUserModel> getAllActiveSO();
	Double getCustomerSaleQTY(String customerUID,  BaseSiteModel site);
	EyDmsCustomerModel getCustomerModelAndOrdersForSpecificDate(String customerUID,BaseSiteModel site,String startDate,String endDate);
	List<EyDmsCustomerModel> getAllCustomerForSubArea(String subArea, BaseSiteModel site);
	List<CustomerSubAreaMappingModel> getAllDistrictForSO(UserModel eydmsUser, BaseSiteModel site);
	double getSalesQuantity(String customerNo, String currentDate, String financialDate);
	double getCustomerTarget(String customerNo, String month, String year);
	List<EyDmsCustomerModel> getAllCustomersForSubAreaByOnboardingStatus(BaseSiteModel site, String onboardingStatus);
	List<EyDmsCustomerModel> getCustomerwithoutCustomerNumber(BaseSiteModel site);
	/**
	 *
	 * @param currentUser
	 * @return
	 */
	List<MeetingScheduleModel> getInfluencerMeetCards(UserModel currentUser);
	double getSalesQuantityForSalesPerformance(String customerUID, String startDate, String endDate, List<String> doList, List<String> subAreaList);
	double getSalesQuantityForBottomLogging(String customerUID, String startDate, String endDate, List<String> doList, List<String> subAreaList);
	AddressModel getAddressByErpId(String erpAddressId, EyDmsCustomerModel customer);
	List<String> filterAddressByLpSource(List<String> stateDistrictTalukaList);

	List<EyDmsCustomerModel> getCountOfCreditLimitBreachedUser(List<SubAreaMasterModel> subAreas, String string);
	AddressModel getAddressByPk(String pk);
	AddressModel getDealerAddressByRetailerPk(String retailerAddressPk, EyDmsCustomerModel customer);
	
	Integer getDealersCountForDSOGreaterThanThirty(List<String> customerNos);
	
	double getSalesQuantityForCustomerList(List<String> customerNos, String startDate, String endDate, BaseSiteModel brand);
		
}
