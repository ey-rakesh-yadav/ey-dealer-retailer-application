package com.scl.core.services.impl;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.scl.core.enums.CounterType;
import com.scl.core.model.*;
import com.scl.core.order.dao.OrderValidationProcessDao;
import com.scl.core.services.*;
import com.scl.facades.data.*;

import com.scl.facades.depot.operations.data.DepotProductData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.customer.services.SclCustomerService;
import com.scl.core.dao.CollectionDao;
import com.scl.core.dao.DJPVisitDao;
import com.scl.core.dao.SclUserDao;
import com.scl.core.dao.TerritoryManagementDao;
import com.scl.core.enums.CollectionDealerOrderApprovalType;
import com.scl.core.enums.QuarterEndOverdueStatus;
import com.scl.core.model.LedgerDetailsModel;
import com.scl.core.model.RhRegionMappingModel;
import com.scl.core.model.SPInvoiceModel;
//import com.scl.core.model.LedgerReportEmailProcessModel;
import com.scl.core.enums.SclUserType;
import com.scl.core.order.dao.SclOrderCountDao;
import com.scl.core.user.impl.SCLUserServiceImpl;
import com.scl.core.utility.SclDateUtility;

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
	DecimalFormat df = new DecimalFormat("#0.00");
	@Resource
	CollectionDao collectionDao;

	@Resource
	TerritoryManagementDao territoryManagementDao;

	@Autowired
	TerritoryManagementService territoryService;
	@Autowired
	SalesPerformanceService salesPerformanceService;

	@Resource
	BaseSiteService baseSiteService;

	@Autowired
	UserService userService;

	@Resource
	SclUserDao sclUserDao;

	@Resource
	DJPVisitDao djpVisitDao;

	@Resource
	SclCustomerService sclCustomerService;

	@Resource
	SclOrderCountDao sclOrderCountDao;

	@Autowired
	TerritoryMasterService territoryMasterService;
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
		if(user instanceof SclCustomerModel)
		{
			customerNo.add(((SclCustomerModel) user).getCustomerNo());
		}

		else
		{
			//New Territory Change
			subAreas = territoryService.getTerritoriesForSO();

			List<SclCustomerModel> dealerList = new ArrayList<>();

			//New Territory Change
			RequestCustomerData requestCustomerData = new RequestCustomerData();
			requestCustomerData.setCounterType(List.of("Dealer"));
			dealerList = territoryService.getCustomerforUser(requestCustomerData);

			//dealerList = territoryManagementDao.getAllCustomerForSubArea(subAreas, baseSiteService.getCurrentBaseSite()).stream().filter(d->(d.getGroups()).contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList());

			//territory code check only for dealer in SO
			Collection<TerritoryMasterModel> territoryMasterModels=territoryMasterService.getCurrentTerritory();
			LOG.info(String.format("territoryMasterModels:: %s",territoryMasterModels));
			List<SclCustomerModel> filterdList=new ArrayList<>();

			if (CollectionUtils.isNotEmpty(territoryMasterModels)) {
				List<TerritoryMasterModel> territoryMasterModelList=territoryMasterModels.stream().distinct().collect(Collectors.toList());
				dealerList.forEach(sclCustomer -> {
					//dealer
					if(Objects.nonNull(sclCustomer.getDefaultB2BUnit()) && sclCustomer.getDefaultB2BUnit().getUid().equalsIgnoreCase(SclCoreConstants.B2B_UNIT.SCL_SHREE_UNIT_UID)) {
						if (sclCustomer.getCounterType().equals(CounterType.DEALER) && Objects.nonNull(sclCustomer.getTerritoryCode()))
						{
							LOG.info(String.format("Inside dealer check ::%s and territoryCode::%s",sclCustomer,sclCustomer.getTerritoryCode()));
							if (territoryMasterModelList.contains(sclCustomer.getTerritoryCode())) {
								LOG.info(String.format("territoryModels dealer check ::%s ",territoryMasterModelList.contains(sclCustomer.getTerritoryCode())));
								filterdList.add(sclCustomer);
							}
						}
					}
				});
			}

			if(CollectionUtils.isNotEmpty(filterdList)) {
				for (SclCustomerModel customer : filterdList.stream().distinct().collect(Collectors.toList())) {
					if (customer.getCustomerNo() != null)
						customerNo.add(customer.getCustomerNo());
				}
			}
		}

		try {
			List<List<Double>> bucketList = new ArrayList<>();
			double bucketsTotal;

			double totalOutstanding =0.0;
			if(!customerNo.isEmpty())
			{
				totalOutstanding =sclUserDao.getOutstandingAmountForSO(customerNo);
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
				bucketList= sclUserDao.getOutstandingBucketsForSO(customerNo);
				bucketsTotal = bucketList.get(0).stream().filter(b->b!=null).mapToDouble(b->b.doubleValue()).sum();
				data.setBucketsTotal(df.format(bucketsTotal));
			}

			if(!bucketList.isEmpty()&&!Objects.isNull(bucketList))
			{
				try {

					data.setBucket1(bucketList.get(0).get(0)!=null?df.format(bucketList.get(0).get(0)):df.format("0"));
					data.setBucket2(bucketList.get(0).get(1)!=null?df.format(bucketList.get(0).get(1)):df.format("0"));
					data.setBucket3(bucketList.get(0).get(2)!=null?df.format(bucketList.get(0).get(2)):df.format("0"));
					data.setBucket4(bucketList.get(0).get(3)!=null?df.format(bucketList.get(0).get(3)):df.format("0"));
					data.setBucket5(bucketList.get(0).get(4)!=null?df.format(bucketList.get(0).get(4)):df.format("0"));
					data.setBucket6(bucketList.get(0).get(5)!=null?df.format(bucketList.get(0).get(5)):df.format("0"));
					data.setBucket7(bucketList.get(0).get(6)!=null?df.format(bucketList.get(0).get(6)):df.format("0"));
					data.setBucket8(bucketList.get(0).get(7)!=null?df.format(bucketList.get(0).get(7)):df.format("0"));
					data.setBucket9(bucketList.get(0).get(8)!=null?df.format(bucketList.get(0).get(8)):df.format("0"));
					data.setBucket10(bucketList.get(0).get(9)!=null?df.format(bucketList.get(0).get(9)):df.format("0"));
				}catch(Exception e)
				{
					LOG.info(e);
				}

			}
			else
			{
				data.setBucket1(df.format(0));
				data.setBucket2(df.format(0));
				data.setBucket3(df.format(0));
				data.setBucket4(df.format(0));
				data.setBucket5(df.format(0));
				data.setBucket6(df.format(0));
				data.setBucket7(df.format(0));
				data.setBucket8(df.format(0));
				data.setBucket9(df.format(0));
				data.setBucket10(df.format(0));
			}

			data.setTotalOutstanding(df.format(totalOutstanding));
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
		List<SclCustomerModel> dealerList = territoryService.getAllCustomerForSubArea(subAreas).stream().filter(d->(d.getGroups()).contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList());
		//List<SclCustomerModel> dealerList = territoryManagementDao.getAllCustomerForSubArea(subAreas, baseSiteService.getCurrentBaseSite()).stream().filter(d->(d.getGroups()).contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList());
*/
		RequestCustomerData requestCustomerData = new RequestCustomerData();
		requestCustomerData.setCounterType(List.of("Dealer"));
		List<SclCustomerModel> dealerList = territoryService.getCustomerforUser(requestCustomerData);
		
		Collection<TerritoryMasterModel> territoryMasterModels=territoryMasterService.getCurrentTerritory();
		LOG.info(String.format("territoryMasterModels:: %s",territoryMasterModels));
		List<SclCustomerModel> filterdList=new ArrayList<>();
		if (CollectionUtils.isNotEmpty(territoryMasterModels)) {
			List<TerritoryMasterModel> territoryMasterModelList=territoryMasterModels.stream().distinct().collect(Collectors.toList());
			dealerList.forEach(sclCustomer -> {
				//dealer
				if(Objects.nonNull(sclCustomer.getDefaultB2BUnit()) && sclCustomer.getDefaultB2BUnit().getUid().equalsIgnoreCase(SclCoreConstants.B2B_UNIT.SCL_SHREE_UNIT_UID)) {
					if (sclCustomer.getCounterType().equals(CounterType.DEALER) && Objects.nonNull(sclCustomer.getTerritoryCode()))
					{
						LOG.info(String.format("Inside dealer check ::%s and territoryCode::%s",sclCustomer,sclCustomer.getTerritoryCode()));
						if (territoryMasterModelList.contains(sclCustomer.getTerritoryCode())) {
							LOG.info(String.format("territoryModels dealer check ::%s ",territoryMasterModelList.contains(sclCustomer.getTerritoryCode())));
							filterdList.add(sclCustomer);
						}

					}
				}
			});
		}

		Double totalCreditLimitSum = 0.0;
		List<SclCustomerModel> sortedFilterdList= filterdList.stream().filter(o->o.getName()!=null).sorted(Comparator.comparing(obj->obj.getName())).distinct().toList();
		for(SclCustomerModel dealer : sortedFilterdList)
		{
			String customerNo = dealer.getCustomerNo();

			if(customerNo!=null)
			{

				CollectionDealerOutstandingDetailsData data = new CollectionDealerOutstandingDetailsData();

				List<SclCustomerModel> dList = new ArrayList<>();
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
				data.setTotalOutstanding(df.format(totalOutstanding));

				data.setDaysSaleOutstanding(outstandingDays);
				data.setSecurityDeposit(df.format(securityDeposit));

				if(!bucketList.isEmpty()&&!Objects.isNull(bucketList))
				{
					try {
						data.setBucket1(bucketList.get(0).get(0)!=null?df.format(bucketList.get(0).get(0)):df.format("0"));
						data.setBucket2(bucketList.get(0).get(1)!=null?df.format(bucketList.get(0).get(1)):df.format("0"));
						data.setBucket3(bucketList.get(0).get(2)!=null?df.format(bucketList.get(0).get(2)):df.format("0"));
						data.setBucket4(bucketList.get(0).get(3)!=null?df.format(bucketList.get(0).get(3)):df.format("0"));
						data.setBucket5(bucketList.get(0).get(4)!=null?df.format(bucketList.get(0).get(4)):df.format("0"));
						data.setBucket6(bucketList.get(0).get(5)!=null?df.format(bucketList.get(0).get(5)):df.format("0"));
						data.setBucket7(bucketList.get(0).get(6)!=null?df.format(bucketList.get(0).get(6)):df.format("0"));
						data.setBucket8(bucketList.get(0).get(7)!=null?df.format(bucketList.get(0).get(7)):df.format("0"));
						data.setBucket9(bucketList.get(0).get(8)!=null?df.format(bucketList.get(0).get(8)):df.format("0"));
						data.setBucket10(bucketList.get(0).get(9)!=null?df.format(bucketList.get(0).get(9)):df.format("0"));
					}catch(Exception e)
					{
						LOG.info(e);
					}

				}
				else
				{
				data.setBucket1(df.format(0));
				data.setBucket2(df.format(0));
				data.setBucket3(df.format(0));
				data.setBucket4(df.format(0));
				data.setBucket5(df.format(0));
				data.setBucket6(df.format(0));
				data.setBucket7(df.format(0));
				data.setBucket8(df.format(0));
				data.setBucket9(df.format(0));
				data.setBucket10(df.format(0));
				}

//			Integer creditBreachedMTDCount = sclOrderCountDao.findCreditBreachCountMTD(dealer);
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

				data.setTotalCreditLimit(df.format(totalCreditLimit));
				totalCreditLimitSum = totalCreditLimitSum+totalCreditLimit;

				list.add(data);

			}
		}

		dataList.setCollectionDealerOutstandingDetailsData(list);
		dataList.setTotalCreditLimitSum(df.format(totalCreditLimitSum));

		return dataList;
	}



	@Override
	public CollectionDealerDetailsData getDealerDetails(String dealerCode) {

		CollectionDealerDetailsData data = new CollectionDealerDetailsData();

		SclCustomerModel dealer = sclCustomerService.getSclCustomerForUid(dealerCode);

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
			data.setBucketsTotal(df.format(bucketsTotal));

			Double securityDeposit = collectionDao.getSecurityDepositForDealer(customerNo);
			//Double creditLimitMultiplier = 2.0;
			Double totalCreditLimit = djpVisitDao.getDealerCreditLimit(customerNo);

			Integer creditBreachedMTDCount = sclOrderCountDao.findCreditBreachCountMTD(dealer);

			data.setCounterName(dealer.getName());
			data.setCounterCode(dealerCode);
			data.setCounterNumber(dealer.getCustomerNo());
			data.setMobileNumber(dealer.getMobileNumber());
			data.setOutstanding(df.format(totalOutstandingAmount));
			data.setSecurityDeposit(df.format(securityDeposit));

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
				data.setUtilizedCredit(df.format(utilizedCredit));
			}
			else
			{
				data.setUtilizedCredit(String.valueOf(0.0));
			}

			data.setTotalCreditLimit(df.format(totalCreditLimit));

			data.setCreditLimitBreachedOrderCount(creditBreachedMTDCount);

			if(!bucketList.isEmpty()&&!Objects.isNull(bucketList))
			{
				try {
					data.setBucket1(bucketList.get(0).get(0)!=null?df.format(bucketList.get(0).get(0)):df.format("0"));
					data.setBucket2(bucketList.get(0).get(1)!=null?df.format(bucketList.get(0).get(1)):df.format("0"));
					data.setBucket3(bucketList.get(0).get(2)!=null?df.format(bucketList.get(0).get(2)):df.format("0"));
					data.setBucket4(bucketList.get(0).get(3)!=null?df.format(bucketList.get(0).get(3)):df.format("0"));
					data.setBucket5(bucketList.get(0).get(4)!=null?df.format(bucketList.get(0).get(4)):df.format("0"));
					data.setBucket6(bucketList.get(0).get(5)!=null?df.format(bucketList.get(0).get(5)):df.format("0"));
					data.setBucket7(bucketList.get(0).get(6)!=null?df.format(bucketList.get(0).get(6)):df.format("0"));
					data.setBucket8(bucketList.get(0).get(7)!=null?df.format(bucketList.get(0).get(7)):df.format("0"));
					data.setBucket9(bucketList.get(0).get(8)!=null?df.format(bucketList.get(0).get(8)):df.format("0"));
					data.setBucket10(bucketList.get(0).get(9)!=null?df.format(bucketList.get(0).get(9)):df.format("0"));
				}catch(Exception e)
				{
					LOG.info(e);
				}

			}
			else
			{
				data.setBucket1(df.format(0));
				data.setBucket2(df.format(0));
				data.setBucket3(df.format(0));
				data.setBucket4(df.format(0));
				data.setBucket5(df.format(0));
				data.setBucket6(df.format(0));
				data.setBucket7(df.format(0));
				data.setBucket8(df.format(0));
				data.setBucket9(df.format(0));
				data.setBucket10(df.format(0));
			}

			data.setNetProvisionalOutstanding(df.format(collectionDao.getDealerNetOutstandingAmount(customerNo)));
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

			data.setAvailedMtd(df.format(availedMtd));
			data.setLostMtd(df.format(lostMtd));
			data.setCdAvailed(availedPercent);
			data.setPaymentForCDAvailment(df.format(totalOutstandingAmount));
			data.setAvailableForAvailment(df.format(totalEligibleDiscount-totalLost));
		}

		return data;
	}

	@Override
	public CollectionCreditDetailsListData getDealerCreditDetails(String dealerCode) {

		CollectionCreditDetailsListData listData = new CollectionCreditDetailsListData();

		List<CollectionCreditDetailsData> list = new ArrayList<>();

		SclCustomerModel dealer = sclCustomerService.getSclCustomerForUid(dealerCode);

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

		SclCustomerModel dealer = sclCustomerService.getSclCustomerForUid(dealerCode);

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
		List<SclCustomerModel> dealerList = territoryService.getAllCustomerForSubArea(subAreas).stream().filter(d->(d.getGroups()).contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList());
		//List<SclCustomerModel> dealerList = territoryManagementDao.getAllCustomerForSubArea(subAreas, baseSiteService.getCurrentBaseSite()).stream().filter(d->(d.getGroups()).contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList());
		*/

		RequestCustomerData requestCustomerData = new RequestCustomerData();
		requestCustomerData.setCounterType(List.of("Dealer"));
		List<SclCustomerModel> dealerList = territoryService.getCustomerforUser(requestCustomerData);

		for(SclCustomerModel dealer : dealerList)
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
//		List<SclCustomerModel> dealerList = territoryService.getAllCustomerForSubArea(subAreas).stream().filter(d->(d.getGroups()).contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList());
//		//List<SclCustomerModel> dealerList = territoryManagementDao.getAllCustomerForSubArea(subAreas, baseSiteService.getCurrentBaseSite()).stream().filter(d->(d.getGroups()).contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList());

		RequestCustomerData requestCustomerData = new RequestCustomerData();
		//requestCustomerData.setCounterType(List.of("Dealer"));
		//List<SclCustomerModel> dealerList = territoryService.getCustomerforUser(requestCustomerData);
		List<SclCustomerModel> dealerList=salesPerformanceService.getCustomersByLeadType("DEALER",null,null,null);

		Double totalCreditLimitSum = 0.0;

		for(SclCustomerModel dealer : dealerList)
		{
			String customerNo = dealer.getCustomerNo();

			if(customerNo!=null)
			{

				TopDealerOutstandingData data = new TopDealerOutstandingData();

				List<SclCustomerModel> dList = new ArrayList<>();
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
				//data.setDebitBalance(amountFormatService.getFormattedValue(totalOutstanding));
				data.setDebitBalance(df.format(totalOutstanding));
				//need to be checked
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
		List<SclCustomerModel> dealerList = new ArrayList<>();
		//New Territory Change
		dealerList = territoryService.getAllCustomerForSubArea(subAreas).stream().filter(d->(d.getGroups()).contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList());
		//dealerList = territoryManagementDao.getAllCustomerForSubArea(subAreas, baseSiteService.getCurrentBaseSite()).stream().filter(d->(d.getGroups()).contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList());
*/
		RequestCustomerData requestCustomerData = new RequestCustomerData();
		requestCustomerData.setCounterType(List.of("Dealer"));
		List<SclCustomerModel> dealerList = territoryService.getCustomerforUser(requestCustomerData);
		List<String> customerNo = new ArrayList<>();

		for(SclCustomerModel customer : dealerList)
		{
			if(customer.getCustomerNo()!=null)
				customerNo.add(customer.getCustomerNo());
		}

		List<List<Double>> bucketList = new ArrayList<>();

		double bucketsTotal;

		if(!customerNo.isEmpty())
		{
			bucketList= sclUserDao.getOutstandingBucketsForSO(customerNo);
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
			data.setBucket1(df.format(0));
			data.setBucket2(df.format(0));
			data.setBucket3(df.format(0));
			data.setBucket4(df.format(0));
			data.setBucket5(df.format(0));
			data.setBucket6(df.format(0));
			data.setBucket7(df.format(0));
			data.setBucket8(df.format(0));
			data.setBucket9(df.format(0));
			data.setBucket10(df.format(0));
		}
		double totalOutstanding =0.0;
		if(!customerNo.isEmpty())
		{
			totalOutstanding =sclUserDao.getOutstandingAmountForSO(customerNo);
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
	public SearchPageData<CreditAndOutstandingModel> getDealerOutstandingDetailsForTSMRH(SearchPageData searchPageData, List<SclUserModel> soList, List<SclUserModel> tsmList) {

		return territoryManagementDao.getDealerOutstandingDetailsForTSMRH(searchPageData,soList,tsmList);
	}
	@Override
	public SearchPageData<SPInvoiceModel> getSPInvoiceList(SearchPageData searchPageData, String startDate,
														   String endDate, String userId, String sortKey, String sort) {
		SclCustomerModel sp = (SclCustomerModel) userService.getUserForUID(userId);
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
