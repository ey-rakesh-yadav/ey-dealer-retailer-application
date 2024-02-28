package com.eydms.core.services.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.eydms.core.model.*;
import com.eydms.core.order.dao.OrderValidationProcessDao;
import com.eydms.facades.data.*;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.customer.services.EyDmsCustomerService;
import com.eydms.core.dao.CollectionDao;
import com.eydms.core.dao.DJPVisitDao;
import com.eydms.core.dao.EyDmsUserDao;
import com.eydms.core.dao.TerritoryManagementDao;
import com.eydms.core.enums.CollectionDealerOrderApprovalType;
import com.eydms.core.enums.QuarterEndOverdueStatus;
import com.eydms.core.model.LedgerDetailsModel;
import com.eydms.core.model.RhRegionMappingModel;
import com.eydms.core.model.SPInvoiceModel;
//import com.eydms.core.model.LedgerReportEmailProcessModel;
import com.eydms.core.enums.EyDmsUserType;
import com.eydms.core.order.dao.EyDmsOrderCountDao;
import com.eydms.core.services.AmountFormatService;
import com.eydms.core.services.CollectionService;
import com.eydms.core.services.TerritoryManagementService;
import com.eydms.core.user.impl.EYDMSUserServiceImpl;
import com.eydms.core.utility.EyDmsDateUtility;

import de.hybris.platform.acceleratorservices.model.email.EmailAttachmentModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.store.services.BaseStoreService;

public class CollectionServiceImpl implements CollectionService{

	private static final Logger LOG = Logger.getLogger(CollectionServiceImpl.class);

	@Resource
	CollectionDao collectionDao;

	@Resource
	TerritoryManagementDao territoryManagementDao;

	@Autowired
	TerritoryManagementService territoryService;

	@Resource
	BaseSiteService baseSiteService;

	@Autowired
	UserService userService;

	@Resource
	EyDmsUserDao eydmsUserDao;

	@Resource
	DJPVisitDao djpVisitDao;

	@Resource
	EyDmsCustomerService eydmsCustomerService;

	@Resource
	EyDmsOrderCountDao eydmsOrderCountDao;

	@Resource
	BusinessProcessService businessProcessService;

	@Resource
	ModelService modelService;

	@Resource
	BaseStoreService baseStoreService;

	@Resource
	CommonI18NService commonI18NService;

	@Resource
	AmountFormatService amountFormatService;

	@Resource
	OrderValidationProcessDao orderValidationProcessDao;

	@Override
	public CollectionOutstandingData getOutstandingData() {

		CollectionOutstandingData data = new CollectionOutstandingData();
		List<SubAreaMasterModel> subAreas = new ArrayList<>();

		Calendar cal = Calendar.getInstance();
		Date endDate = cal.getTime();
		cal.add(Calendar.MONTH,-1);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date startDate = cal.getTime();

		List<String> customerNo = new ArrayList<>();
		B2BCustomerModel user = (B2BCustomerModel) userService.getCurrentUser();
		if(user instanceof EyDmsCustomerModel)
		{
			customerNo.add(((EyDmsCustomerModel) user).getCustomerNo());
		}

		else
		{
			//New Territory Change
			subAreas = territoryService.getTerritoriesForSO();

			List<EyDmsCustomerModel> dealerList = new ArrayList<>();

			//New Territory Change
			RequestCustomerData requestCustomerData = new RequestCustomerData();
			requestCustomerData.setCounterType(List.of("Dealer"));
			dealerList = territoryService.getCustomerforUser(requestCustomerData);

			//dealerList = territoryManagementDao.getAllCustomerForSubArea(subAreas, baseSiteService.getCurrentBaseSite()).stream().filter(d->(d.getGroups()).contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList());

			for(EyDmsCustomerModel customer : dealerList)
			{
				if(customer.getCustomerNo()!=null)
					customerNo.add(customer.getCustomerNo());
			}
		}

		try {
			List<List<Double>> bucketList = new ArrayList<>();
			double bucketsTotal;

			double totalOutstanding =0.0;
			if(!customerNo.isEmpty())
			{
				totalOutstanding =eydmsUserDao.getOutstandingAmountForSO(customerNo);
			}

			double dailyAverageSales = collectionDao.getDailyAverageSalesForListOfDealers(customerNo);
			Date lastUpdateDate = collectionDao.getLastUpdateDateForOutstanding(customerNo);

			double outstandingDays=0.0;
			if(dailyAverageSales!=0.0)
			{
				outstandingDays = totalOutstanding/dailyAverageSales;
			}

			data.setDaysSaleOutstanding(outstandingDays);
			if(lastUpdateDate!=null) {
				Calendar cal1 = Calendar.getInstance();
				cal1.setTime(lastUpdateDate);
				cal1.add(Calendar.HOUR,5);
				cal1.add(Calendar.MINUTE,30);
				data.setLastUpdateDate(cal1.getTime());
			}

			if(!customerNo.isEmpty())
			{
				bucketList= eydmsUserDao.getOutstandingBucketsForSO(customerNo);
				bucketsTotal = bucketList.get(0).stream().filter(b->b!=null).mapToDouble(b->b.doubleValue()).sum();
				data.setBucketsTotal(amountFormatService.getFormattedValue(bucketsTotal));
			}

			if(!bucketList.isEmpty()&&!Objects.isNull(bucketList))
			{
				try {

					data.setBucket1(bucketList.get(0).get(0)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(0)):"0");
					data.setBucket2(bucketList.get(0).get(1)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(1)):"0");
					data.setBucket3(bucketList.get(0).get(2)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(2)):"0");
					data.setBucket4(bucketList.get(0).get(3)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(3)):"0");
					data.setBucket5(bucketList.get(0).get(4)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(4)):"0");
					data.setBucket6(bucketList.get(0).get(5)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(5)):"0");
					data.setBucket7(bucketList.get(0).get(6)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(6)):"0");
					data.setBucket8(bucketList.get(0).get(7)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(7)):"0");
					data.setBucket9(bucketList.get(0).get(8)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(8)):"0");
					data.setBucket10(bucketList.get(0).get(9)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(9)):"0");
				}catch(Exception e)
				{
					LOG.info(e);
				}

			}
			else
			{
				data.setBucket1("0");
				data.setBucket2("0");
				data.setBucket3("0");
				data.setBucket4("0");
				data.setBucket5("0");
				data.setBucket6("0");
				data.setBucket7("0");
				data.setBucket8("0");
				data.setBucket9("0");
				data.setBucket10("0");
			}

			data.setTotalOutstanding(amountFormatService.getFormattedValue(totalOutstanding));
		}catch(Exception e)
		{
			LOG.error(e);
		}


		return data;
	}

	@Override
	public CollectionDealerOutstandingDetailsListData getDealerOutstandingDetails() {

		CollectionDealerOutstandingDetailsListData dataList = new CollectionDealerOutstandingDetailsListData();
		List<CollectionDealerOutstandingDetailsData> list = new ArrayList<>();

		Calendar cal = Calendar.getInstance();
		Date endDate = cal.getTime();
		cal.add(Calendar.MONTH,-1);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date startDate = cal.getTime();

		/*List<SubAreaMasterModel> subAreas = new ArrayList<>();
		//New Territory Change
		subAreas = territoryService.getTerritoriesForSO();
		//New Territory Change
		List<EyDmsCustomerModel> dealerList = territoryService.getAllCustomerForSubArea(subAreas).stream().filter(d->(d.getGroups()).contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList());
		//List<EyDmsCustomerModel> dealerList = territoryManagementDao.getAllCustomerForSubArea(subAreas, baseSiteService.getCurrentBaseSite()).stream().filter(d->(d.getGroups()).contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList());
*/
		RequestCustomerData requestCustomerData = new RequestCustomerData();
		requestCustomerData.setCounterType(List.of("Dealer"));
		List<EyDmsCustomerModel> dealerList = territoryService.getCustomerforUser(requestCustomerData);

		Double totalCreditLimitSum = 0.0;

		for(EyDmsCustomerModel dealer : dealerList)
		{
			String customerNo = dealer.getCustomerNo();

			if(customerNo!=null)
			{

				CollectionDealerOutstandingDetailsData data = new CollectionDealerOutstandingDetailsData();

				List<EyDmsCustomerModel> dList = new ArrayList<>();
				dList.add(dealer);

				double totalOutstanding = djpVisitDao.getDealerOutstandingAmount(customerNo);
				List<List<Double>> bucketList = djpVisitDao.getOutstandingBucketsForDealer(customerNo);
				double sales = collectionDao.getSalesForDealer(dList, startDate, endDate);
				double dailyAverageSales = collectionDao.getDailyAverageSalesForDealer(customerNo);
				double outstandingDays = 0.0;
				if(dailyAverageSales!=0.0)
				{
					outstandingDays = totalOutstanding/dailyAverageSales;
				}

				double securityDeposit = collectionDao.getSecurityDepositForDealer(customerNo);
				data.setCounterName(dealer.getName());
				data.setCounterCode(dealer.getUid());
				data.setCustomerNo(dealer.getCustomerNo());
				data.setTotalOutstanding(amountFormatService.getFormattedValue(totalOutstanding));
				data.setDaysSaleOutstanding(outstandingDays);
				data.setSecurityDeposit(amountFormatService.getFormattedValue(securityDeposit));

				if(!bucketList.isEmpty()&&!Objects.isNull(bucketList))
				{
					try {
						data.setBucket1(bucketList.get(0).get(0)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(0)):"0");
						data.setBucket2(bucketList.get(0).get(1)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(1)):"0");
						data.setBucket3(bucketList.get(0).get(2)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(2)):"0");
						data.setBucket4(bucketList.get(0).get(3)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(3)):"0");
						data.setBucket5(bucketList.get(0).get(4)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(4)):"0");
						data.setBucket6(bucketList.get(0).get(5)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(5)):"0");
						data.setBucket7(bucketList.get(0).get(6)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(6)):"0");
						data.setBucket8(bucketList.get(0).get(7)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(7)):"0");
						data.setBucket9(bucketList.get(0).get(8)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(8)):"0");
						data.setBucket10(bucketList.get(0).get(9)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(9)):"0");
					}catch(Exception e)
					{
						LOG.info(e);
					}

				}
				else
				{
					data.setBucket1("0");
					data.setBucket2("0");
					data.setBucket3("0");
					data.setBucket4("0");
					data.setBucket5("0");
					data.setBucket6("0");
					data.setBucket7("0");
					data.setBucket8("0");
					data.setBucket9("0");
					data.setBucket10("0");
				}

//			Integer creditBreachedMTDCount = eydmsOrderCountDao.findCreditBreachCountMTD(dealer);
//
//			if(creditBreachedMTDCount==0 || creditBreachedMTDCount==null)
//			{
//				data.setIsCreditLimitBreached(Boolean.FALSE);
//			}
//			else
//			{
//				data.setIsCreditLimitBreached(Boolean.TRUE);
//			}

				Double totalOutstandingAmount = djpVisitDao.getDealerOutstandingAmount(customerNo);
				double pendingOrderAmount = orderValidationProcessDao.getPendingOrderAmount(dealer.getPk().toString());

				Double utilizeCredit = totalOutstandingAmount + pendingOrderAmount;
				//Double creditLimitMultiplier = 2.0;
				Double totalCreditLimit = djpVisitDao.getDealerCreditLimit(customerNo);

				if(totalCreditLimit-utilizeCredit<0)
				{
					data.setIsCreditLimitBreached(Boolean.TRUE);
				}
				else
				{
					data.setIsCreditLimitBreached(Boolean.FALSE);
				}

				data.setMobileNumber(dealer.getMobileNumber());

				data.setTotalCreditLimit(amountFormatService.getFormattedValue(totalCreditLimit));
				totalCreditLimitSum = totalCreditLimitSum+totalCreditLimit;

				list.add(data);

			}
		}

		dataList.setCollectionDealerOutstandingDetailsData(list);
		dataList.setTotalCreditLimitSum(amountFormatService.getFormattedValue(totalCreditLimitSum));

		return dataList;
	}



	@Override
	public CollectionDealerDetailsData getDealerDetails(String dealerCode) {

		CollectionDealerDetailsData data = new CollectionDealerDetailsData();

		EyDmsCustomerModel dealer = eydmsCustomerService.getEyDmsCustomerForUid(dealerCode);

		String customerNo = dealer.getCustomerNo();

		if(customerNo!=null) {

			Double totalOutstandingAmount = djpVisitDao.getDealerOutstandingAmount(customerNo);

			Double dailyAverageSales = djpVisitDao.getDailyAverageSales(customerNo);

			double outstandingDays = 0.0;
			if(dailyAverageSales!=0.0){
				outstandingDays = totalOutstandingAmount/dailyAverageSales;
			}
			data.setDaysSaleOutstanding(outstandingDays);

			List<List<Double>> bucketList = djpVisitDao.getOutstandingBucketsForDealer(customerNo);

			double bucketsTotal = bucketList.get(0).stream().filter(b->b!=null).mapToDouble(b->b.doubleValue()).sum();
			data.setBucketsTotal(amountFormatService.getFormattedValue(bucketsTotal));

			Double securityDeposit = collectionDao.getSecurityDepositForDealer(customerNo);
			//Double creditLimitMultiplier = 2.0;
			Double totalCreditLimit = djpVisitDao.getDealerCreditLimit(customerNo);

			Integer creditBreachedMTDCount = eydmsOrderCountDao.findCreditBreachCountMTD(dealer);

			data.setCounterName(dealer.getName());
			data.setCounterCode(dealerCode);
			data.setCounterNumber(dealer.getCustomerNo());
			data.setMobileNumber(dealer.getMobileNumber());
			data.setOutstanding(amountFormatService.getFormattedValue(totalOutstandingAmount));
			data.setSecurityDeposit(amountFormatService.getFormattedValue(securityDeposit));

			if(customerNo!=null){
				Date lastUpdateDate = collectionDao.getLastUpdateDateForOutstanding(Collections.singletonList(customerNo));
				if(lastUpdateDate!=null) {
					Calendar cal1 = Calendar.getInstance();
					cal1.setTime(lastUpdateDate);
					cal1.add(Calendar.HOUR,5);
					cal1.add(Calendar.MINUTE,30);
					data.setLastUpdateDate(cal1.getTime());
				}
			}


			double pendingOrderAmount = orderValidationProcessDao.getPendingOrderAmount(dealer.getPk().toString());
			//Double utilizedCredit = totalOutstandingAmount;
			if(totalOutstandingAmount !=0.0) {
				double utilizedCredit = totalOutstandingAmount + pendingOrderAmount;
				data.setUtilizedCredit(amountFormatService.getFormattedValue(utilizedCredit));
			}
			else
			{
				data.setUtilizedCredit(String.valueOf(0.0));
			}

			data.setTotalCreditLimit(amountFormatService.getFormattedValue(totalCreditLimit));

			data.setCreditLimitBreachedOrderCount(creditBreachedMTDCount);

			if(!bucketList.isEmpty()&&!Objects.isNull(bucketList))
			{
				try {
					data.setBucket1(bucketList.get(0).get(0)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(0)):"0");
					data.setBucket2(bucketList.get(0).get(1)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(1)):"0");
					data.setBucket3(bucketList.get(0).get(2)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(2)):"0");
					data.setBucket4(bucketList.get(0).get(3)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(3)):"0");
					data.setBucket5(bucketList.get(0).get(4)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(4)):"0");
					data.setBucket6(bucketList.get(0).get(5)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(5)):"0");
					data.setBucket7(bucketList.get(0).get(6)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(6)):"0");
					data.setBucket8(bucketList.get(0).get(7)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(7)):"0");
					data.setBucket9(bucketList.get(0).get(8)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(8)):"0");
					data.setBucket10(bucketList.get(0).get(9)!=null?amountFormatService.getFormattedValue(bucketList.get(0).get(9)):"0");
				}catch(Exception e)
				{
					LOG.info(e);
				}

			}
			else
			{
				data.setBucket1("0");
				data.setBucket2("0");
				data.setBucket3("0");
				data.setBucket4("0");
				data.setBucket5("0");
				data.setBucket6("0");
				data.setBucket7("0");
				data.setBucket8("0");
				data.setBucket9("0");
				data.setBucket10("0");
			}

			data.setNetProvisionalOutstanding(amountFormatService.getFormattedValue(collectionDao.getDealerNetOutstandingAmount(customerNo)));
			QuarterEndOverdueStatus status = collectionDao.getDealerQuarterEndOverdueStatus(customerNo);

			if(status!=null)
			{
				data.setQuarterEndOverdueStatus(status.getCode());
			}


			Calendar cal = Calendar.getInstance();
			Date endDate = cal.getTime();

			cal.set(Calendar.DATE, 1);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);

			Date startDate = cal.getTime();

			double availedMtd = collectionDao.getTotalCDAvailedForDealer(dealerCode, startDate, endDate);
			double lostMtd = collectionDao.getTotalCDLostForDealer(dealerCode, startDate, endDate);

			double availedPercent = 0.0;

			if((availedMtd+lostMtd)!=0.0)
			{
				availedPercent = (availedMtd/(availedMtd+lostMtd))*100;
			}

			List<List<Double>> invoiceAmt = collectionDao.getTotalEligibleCDForDealer(customerNo);

			double totalEligibleDiscount = 0.0;

			try {
				if(!invoiceAmt.isEmpty())
				{
					totalEligibleDiscount = invoiceAmt.get(0).get(0) - invoiceAmt.get(0).get(1);
				}
			}catch(Exception e)
			{
				LOG.error(e);
			}

			double totalLost = collectionDao.getTotalCDLostForDealer(customerNo,null,null);

			data.setAvailedMtd(amountFormatService.getFormattedValue(availedMtd));
			data.setLostMtd(amountFormatService.getFormattedValue(lostMtd));
			data.setCdAvailed(availedPercent);
			data.setPaymentForCDAvailment(amountFormatService.getFormattedValue(totalOutstandingAmount));
			data.setAvailableForAvailment(amountFormatService.getFormattedValue(totalEligibleDiscount-totalLost));
		}

		return data;
	}

	@Override
	public CollectionCreditDetailsListData getDealerCreditDetails(String dealerCode) {

		CollectionCreditDetailsListData listData = new CollectionCreditDetailsListData();

		List<CollectionCreditDetailsData> list = new ArrayList<>();

		EyDmsCustomerModel dealer = eydmsCustomerService.getEyDmsCustomerForUid(dealerCode);

		List<OrderModel> orderList = collectionDao.getCreditBreachedOrders(dealer);

		for(OrderModel order : orderList)
		{
			CollectionCreditDetailsData data = new CollectionCreditDetailsData();
			data.setOrderNo(order.getCode());
			data.setOrderAmount(amountFormatService.getFormattedValue(order.getTotalPrice()));
			data.setOrderDate(order.getDate());

			if(order.getStatus().equals(OrderStatus.APPROVED))
			{
				data.setApproval(CollectionDealerOrderApprovalType.EXCEPTION.getCode());
			}
			else
			{
				data.setApproval(CollectionDealerOrderApprovalType.WITHIN_LIMIT.getCode());
			}
			list.add(data);
		}
		listData.setCollectionCreditDetailsData(list);
		return listData;
	}

	@Override
	public List<LedgerDetailsModel> getLedgerDetails(String dealerCode, Boolean isDebit, Boolean isCredit, String startDate, String endDate) {

		EyDmsCustomerModel dealer = eydmsCustomerService.getEyDmsCustomerForUid(dealerCode);

		Date date1 = null;
		Date date2 = null;

		if(startDate==null || endDate==null)
		{
			Calendar cal = Calendar.getInstance();
			date2 = cal.getTime();

			cal.add(Calendar.MONTH, -1);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);

			date1 = cal.getTime();
		}
		else
		{
			try {
				date1 = new SimpleDateFormat("dd/MM/yyyy").parse(startDate);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			try {
				date2 = new SimpleDateFormat("dd/MM/yyyy").parse(endDate);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		List<LedgerDetailsModel> modelList = collectionDao.getLedgerForDealer(dealer.getCustomerNo(), isDebit, isCredit, date1, date2);

		return modelList;
	}

	@Override
	public CollectionCDDetailsListData getCashDiscountDetails(Boolean isMTD, Boolean isYTD) {

		Calendar cal = Calendar.getInstance();

		Date endDate = null;
		Date startDate = null;

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		if((isMTD==Boolean.TRUE))
		{
			endDate = cal.getTime();
			cal.add(Calendar.MONTH, -1);
			startDate = cal.getTime();
		}
		else if(isYTD==Boolean.TRUE)
		{
			endDate = cal.getTime();
			cal.add(Calendar.YEAR, -1);
			startDate = cal.getTime();
		}

		CollectionCDDetailsListData listData = new CollectionCDDetailsListData();

		List<CollectionCDDetailsData> list = new ArrayList<>();

		/*List<SubAreaMasterModel> subAreas = new ArrayList<>();

		//New Territory Change
		subAreas = territoryService.getTerritoriesForSO();
		//New Territory Change
		List<EyDmsCustomerModel> dealerList = territoryService.getAllCustomerForSubArea(subAreas).stream().filter(d->(d.getGroups()).contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList());
		//List<EyDmsCustomerModel> dealerList = territoryManagementDao.getAllCustomerForSubArea(subAreas, baseSiteService.getCurrentBaseSite()).stream().filter(d->(d.getGroups()).contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList());
		*/

		RequestCustomerData requestCustomerData = new RequestCustomerData();
		requestCustomerData.setCounterType(List.of("Dealer"));
		List<EyDmsCustomerModel> dealerList = territoryService.getCustomerforUser(requestCustomerData);

		for(EyDmsCustomerModel dealer : dealerList)
		{
			CollectionCDDetailsData data = new CollectionCDDetailsData();

			String customerNo = dealer.getCustomerNo();

			double totalAvailed = collectionDao.getTotalCDAvailedForDealer(customerNo,startDate,endDate);
			double totalLost = collectionDao.getTotalCDLostForDealer(customerNo,startDate,endDate);

			double availedPercent = 0.0;

			if((totalAvailed+totalLost)!=0.0)
			{
				availedPercent = (totalAvailed/(totalAvailed+totalLost))*100;
			}

			List<List<Double>> invoiceAmt = collectionDao.getTotalEligibleCDForDealer(customerNo);

			double totalEligibleDiscount = 0.0;

			try {
				if(!invoiceAmt.isEmpty())
				{
					totalEligibleDiscount = invoiceAmt.get(0).get(0) - invoiceAmt.get(0).get(1);
				}
			}catch(Exception e)
			{
				LOG.error(e);
			}

			data.setCounterName(dealer.getName());
			data.setCounterCode(dealer.getUid());
			data.setCustomerNo(customerNo);
			data.setTotalAvailed(amountFormatService.getFormattedValue(totalAvailed));
			data.setTotalLost(amountFormatService.getFormattedValue(totalLost));
			data.setAvailedPercentage(availedPercent);
			data.setAvailableCDAfterPayment(amountFormatService.getFormattedValue(totalEligibleDiscount-totalLost));
			data.setPaymentToAvailCD(amountFormatService.getFormattedValue(djpVisitDao.getDealerOutstandingAmount(customerNo)));

			list.add(data);
		}



		listData.setCollectionCDDetailsData(list);
		return listData;
	}

	@Override
	public TopDealerOutstandingListData topDealerOutstanding() {

		TopDealerOutstandingListData dataList = new TopDealerOutstandingListData();
		List<TopDealerOutstandingData> list = new ArrayList<>();

		Calendar cal = Calendar.getInstance();
		Date endDate = cal.getTime();
		cal.add(Calendar.MONTH,-1);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date startDate = cal.getTime();

//		List<SubAreaMasterModel> subAreas = new ArrayList<>();
//		//New Territory Change
//		subAreas = territoryService.getTerritoriesForSO();
//		//New Territory Change
//		List<EyDmsCustomerModel> dealerList = territoryService.getAllCustomerForSubArea(subAreas).stream().filter(d->(d.getGroups()).contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList());
//		//List<EyDmsCustomerModel> dealerList = territoryManagementDao.getAllCustomerForSubArea(subAreas, baseSiteService.getCurrentBaseSite()).stream().filter(d->(d.getGroups()).contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList());

		RequestCustomerData requestCustomerData = new RequestCustomerData();
		requestCustomerData.setCounterType(List.of("Dealer"));
		List<EyDmsCustomerModel> dealerList = territoryService.getCustomerforUser(requestCustomerData);
		Double totalCreditLimitSum = 0.0;

		for(EyDmsCustomerModel dealer : dealerList)
		{
			String customerNo = dealer.getCustomerNo();

			if(customerNo!=null)
			{

				TopDealerOutstandingData data = new TopDealerOutstandingData();

				List<EyDmsCustomerModel> dList = new ArrayList<>();
				dList.add(dealer);

				double totalOutstanding = djpVisitDao.getDealerOutstandingAmount(customerNo);

				double sales = collectionDao.getSalesForDealer(dList, startDate, endDate);
				double dailyAverageSales = collectionDao.getDailyAverageSalesForDealer(customerNo);
				double outstandingDays = 0.0;
				if(dailyAverageSales!=0.0)
				{
					outstandingDays = totalOutstanding/dailyAverageSales;
				}


				data.setCounterName(dealer.getName());
				data.setDebitBalance(amountFormatService.getFormattedValue(totalOutstanding));
				data.setOutstanding(totalOutstanding);
				data.setAgeing(String.valueOf(getDaysFromLastOrder(dealer.getOrders())));
				data.setDaySale(String.valueOf(outstandingDays));
				list.add(data);

			}
		}
		/*list.stream().sorted(Comparator.comparing(TopDealerOutstandingData::getOutstanding)).limit(5).collect(Collectors.toList());
		dataList.setTopDealerOutstandingData(list);
		return dataList;*/
		List<TopDealerOutstandingData> data= list.stream().sorted(Comparator.comparing(TopDealerOutstandingData::getOutstanding).reversed()).collect(Collectors.toList());
		dataList.setTopDealerOutstandingData(data.stream().limit(5).collect(Collectors.toList()));
		return dataList;

	}

//	@Override
//	public void sendLedgerReportMail(String dealerCode, Boolean isDebit, Boolean isCredit, String startDate,
//			String endDate) {
//
//		if( ((isDebit==null) && (isCredit==null)) || ((isDebit==Boolean.FALSE) && (isDebit==Boolean.FALSE)) )
//		{
//
//		}
//		else if(isDebit==Boolean.TRUE)
//		{
//
//		}
//		else if(isCredit==Boolean.TRUE)
//		{
//
//		}
//
//
//		LedgerReportEmailProcessModel ledgerReportEmailProcessModel = businessProcessService.createProcess("ledgerReportEmail-" + System.currentTimeMillis(), "ledgerReportEmailProcess");
//
//		ledgerReportEmailProcessModel.setStore(baseStoreService.getCurrentBaseStore());
//		ledgerReportEmailProcessModel.setSite(baseSiteService.getCurrentBaseSite());
//		ledgerReportEmailProcessModel.setLanguage(commonI18NService.getCurrentLanguage());
//		ledgerReportEmailProcessModel.setCurrency(commonI18NService.getCurrentCurrency());
//
//		ledgerReportEmailProcessModel.setEmailAttachments(null);
//
//		modelService.save(ledgerReportEmailProcessModel);
//		businessProcessService.startProcess(ledgerReportEmailProcessModel);
//	}

	private int getDaysFromLastOrder(Collection<OrderModel> orderList) {
		if(CollectionUtils.isEmpty(orderList)){
			return 0;
		}
		Date lastOrderDate =orderList.stream().sorted(Comparator.comparing(OrderModel::getCreationtime).reversed()).collect(Collectors.toList()).get(0).getCreationtime();
		int numberOfDays;
		Date currentDate = new Date(System.currentTimeMillis());
		LocalDateTime current = LocalDateTime.ofInstant(currentDate.toInstant(), ZoneId.systemDefault());
		LocalDateTime last = LocalDateTime.ofInstant(lastOrderDate.toInstant(), ZoneId.systemDefault());
		numberOfDays = (int) ChronoUnit.DAYS.between(last, current);
		return numberOfDays;

	}

	@Override
	public CollectionOutstandingData getOutstandingDataForSP() {

		CollectionOutstandingData data = new CollectionOutstandingData();
		List<SubAreaMasterModel> subAreas = new ArrayList<>();

		Calendar cal = Calendar.getInstance();
		Date endDate = cal.getTime();
		cal.add(Calendar.MONTH,-1);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date startDate = cal.getTime();


		/*//New Territory Change
		subAreas = territoryService.getTerritoriesForSO();
		List<EyDmsCustomerModel> dealerList = new ArrayList<>();
		//New Territory Change
		dealerList = territoryService.getAllCustomerForSubArea(subAreas).stream().filter(d->(d.getGroups()).contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList());
		//dealerList = territoryManagementDao.getAllCustomerForSubArea(subAreas, baseSiteService.getCurrentBaseSite()).stream().filter(d->(d.getGroups()).contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList());
*/
		RequestCustomerData requestCustomerData = new RequestCustomerData();
		requestCustomerData.setCounterType(List.of("Dealer"));
		List<EyDmsCustomerModel> dealerList = territoryService.getCustomerforUser(requestCustomerData);
		List<String> customerNo = new ArrayList<>();

		for(EyDmsCustomerModel customer : dealerList)
		{
			if(customer.getCustomerNo()!=null)
				customerNo.add(customer.getCustomerNo());
		}

		List<List<Double>> bucketList = new ArrayList<>();

		double bucketsTotal;

		if(!customerNo.isEmpty())
		{
			bucketList= eydmsUserDao.getOutstandingBucketsForSO(customerNo);
			bucketsTotal = bucketList.get(0).stream().filter(b->b!=null).mapToDouble(b->b.doubleValue()).sum();
			data.setBucketsTotal(amountFormatService.getFormattedValue(bucketsTotal));
		}

		if(!bucketList.isEmpty()&&!Objects.isNull(bucketList))
		{
			try {

				data.setBucket1(bucketList.get(0).get(0)!=null? amountFormatService.getFormattedValue(bucketList.get(0).get(0)):"0");
				data.setBucket2(bucketList.get(0).get(1)!=null? amountFormatService.getFormattedValue(bucketList.get(0).get(1)) :"0");
				data.setBucket3(bucketList.get(0).get(2)!=null? amountFormatService.getFormattedValue(bucketList.get(0).get(2)) :"0");
				data.setBucket4(bucketList.get(0).get(3)!=null? amountFormatService.getFormattedValue(bucketList.get(0).get(3)) :"0");
				data.setBucket5(bucketList.get(0).get(4)!=null? amountFormatService.getFormattedValue(bucketList.get(0).get(4)) :"0");
				data.setBucket6(bucketList.get(0).get(5)!=null? amountFormatService.getFormattedValue(bucketList.get(0).get(5)) :"0");
				data.setBucket7(bucketList.get(0).get(6)!=null? amountFormatService.getFormattedValue(bucketList.get(0).get(6)) :"0");
				data.setBucket8(bucketList.get(0).get(7)!=null? amountFormatService.getFormattedValue(bucketList.get(0).get(7)) :"0");
				data.setBucket9(bucketList.get(0).get(8)!=null? amountFormatService.getFormattedValue(bucketList.get(0).get(8)) :"0");
				data.setBucket10(bucketList.get(0).get(9)!=null? amountFormatService.getFormattedValue(bucketList.get(0).get(9)) :"0");
			}catch(Exception e)
			{
				LOG.info(e);
			}

		}
		else
		{
			data.setBucket1("0");
			data.setBucket2("0");
			data.setBucket3("0");
			data.setBucket4("0");
			data.setBucket5("0");
			data.setBucket6("0");
			data.setBucket7("0");
			data.setBucket8("0");
			data.setBucket9("0");
			data.setBucket10("0");
		}
		double totalOutstanding =0.0;
		if(!customerNo.isEmpty())
		{
			totalOutstanding =eydmsUserDao.getOutstandingAmountForSO(customerNo);
		}

		double dailyAverageSales = collectionDao.getDailyAverageSalesForListOfDealers(customerNo);
		Date lastUpdateDate = collectionDao.getLastUpdateDateForOutstanding(customerNo);


		double outstandingDays=0.0;
		if(dailyAverageSales!=0.0)
		{
			outstandingDays = totalOutstanding/dailyAverageSales;
		}

		data.setTotalOutstanding(amountFormatService.getFormattedValue(totalOutstanding));
		if(lastUpdateDate!=null) {
			Calendar cal1 = Calendar.getInstance();
			cal1.setTime(lastUpdateDate);
			cal1.add(Calendar.HOUR,5);
			cal1.add(Calendar.MINUTE,30);
			data.setLastUpdateDate(cal1.getTime());
		}

		if(outstandingDays!=0.0)
		{
			data.setDaysSaleOutstanding(outstandingDays);
		}


		return data;
	}

	@Override
	public SearchPageData<CreditAndOutstandingModel> getDealerOutstandingDetailsForTSMRH(SearchPageData searchPageData, List<EyDmsUserModel> soList, List<EyDmsUserModel> tsmList) {

		return territoryManagementDao.getDealerOutstandingDetailsForTSMRH(searchPageData,soList,tsmList);
	}
	@Override
	public SearchPageData<SPInvoiceModel> getSPInvoiceList(SearchPageData searchPageData, String startDate,
														   String endDate, String userId, String sortKey, String sort) {
		EyDmsCustomerModel sp = (EyDmsCustomerModel) userService.getUserForUID(userId);
		Date date1 = null;
		Date date2 = null;

		DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		if(startDate!=null && endDate!=null)
		{
			try {
				date1 = format.parse(startDate);
				date2 = format.parse(endDate);
			} catch (ParseException e) {
				e.printStackTrace();
			}

		}

		return collectionDao.getSPInvoiceList(sp,searchPageData,date1,date2,sortKey,sort);

	}

	@Override
	public List<List<Object>> getOutstandingDataForTSMRH(){
		return territoryManagementDao.getOutstandingDataForTSMRH();

	}

}
