package com.scl.core.job;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Resource;


import com.scl.core.constants.SclCoreConstants;
import com.scl.core.customer.dao.SclCustomerDao;
import com.scl.core.dao.EfficacyReportDao;
import com.scl.core.dao.SalesPerformanceDao;
import com.scl.core.enums.NetworkType;

import com.scl.core.model.DeliveryItemModel;
import com.scl.core.model.SclCustomerModel;


import com.scl.core.model.TerritoryMasterModel;
import com.scl.core.services.TerritoryMasterService;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;

import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.hsqldb.rights.User;
import org.springframework.beans.factory.annotation.Autowired;

public class SclCustomerNetworkTypeUpdateJob extends AbstractJobPerformable<CronJobModel> {
	private static final Logger LOGGER = Logger.getLogger(SclCustomerNetworkTypeUpdateJob.class);
	@Resource
	SclCustomerDao sclCustomerDao;
	
	@Resource
	ModelService modelService;
	
	@Resource
	FlexibleSearchService flexibleSearchService;
	
	@Resource
	EfficacyReportDao efficacyReportDao;
	@Autowired
	SalesPerformanceDao salesPerformanceDao;
	@Autowired
	UserService userService;
	@Autowired
	TerritoryMasterService territoryMasterService;
	
	@Override
	public PerformResult perform(CronJobModel arg0) {
		try {
			List<SclCustomerModel> customerList = new ArrayList<>();
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			cal.add(Calendar.MONTH, -1);
			Date oneMonthDate = cal.getTime();
			cal.add(Calendar.MONTH, -2);
			Date threeMonthDate = cal.getTime();

			List<SclCustomerModel> collect = sclCustomerDao.getCustomerList();
			List<SclCustomerModel> dealerretailerInfluencerList = collect.stream().filter(list -> list.getDefaultB2BUnit() != null).collect(Collectors.toList());


			if (CollectionUtils.isNotEmpty(dealerretailerInfluencerList)){
				LOGGER.info("Dealer Retaier and influencer  List Size:"+dealerretailerInfluencerList.size());
				for (SclCustomerModel sclCustomer : dealerretailerInfluencerList) {
						if (sclCustomer.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
							Map<String, Object> resultMax = salesPerformanceDao.findMaxInvoicedDateAndQunatityDeliveryItem(sclCustomer);
							if (resultMax.get(DeliveryItemModel.INVOICECREATIONDATEANDTIME) != null) {
								sclCustomer.setLastLiftingDate((Date) resultMax.get(DeliveryItemModel.INVOICECREATIONDATEANDTIME));
								double qty = (double) resultMax.get(DeliveryItemModel.INVOICEQUANTITY);
								sclCustomer.setLastLiftingQuantity(qty);
							}
						} /*else if (sclCustomer.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
							Map<String, Object> resultMaxRetailer = salesPerformanceDao.findMaxInvoicedDateAndQuantityForRetailer(sclCustomer);
							if (resultMaxRetailer.get("liftingDate") != null) {
								String dateString = (String) resultMaxRetailer.get("liftingDate");
								String pattern = "yyyy-MM-dd HH:mm";
								DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
								try {
									LocalDate date = LocalDate.parse(dateString, formatter);
									Date lastLiftingDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
									sclCustomer.setLastLiftingDate(lastLiftingDate);
									LOGGER.info(String.format("Last Lifting Date for Retailer:%s:::Date:%s", sclCustomer.getUid(), lastLiftingDate));
								} catch (DateTimeParseException e) {
									LOGGER.info(String.format("Parse Exception handled in cron job last lifting date Cause:%s :: Message ::%s", e.getCause(), e.getMessage()));
								}
								double qty = (double) resultMaxRetailer.get("liftingQty");
								sclCustomer.setLastLiftingQuantity(qty);
							}
						}*/
						modelService.save(sclCustomer);
						modelService.refresh(sclCustomer);
						if (sclCustomer.getLastLiftingDate() != null) {
							if (oneMonthDate.compareTo(sclCustomer.getLastLiftingDate()) < 0)
								sclCustomer.setNetworkType(NetworkType.ACTIVE.getCode());
							else if (threeMonthDate.compareTo(sclCustomer.getLastLiftingDate()) < 0)
								sclCustomer.setNetworkType(NetworkType.INACTIVE.getCode());
							else
								sclCustomer.setNetworkType(NetworkType.DORMANT.getCode());
						} else {
							sclCustomer.setNetworkType(NetworkType.DORMANT.getCode());
						}
						modelService.save(sclCustomer);
						LOGGER.info(String.format("customer saving into model:%s", sclCustomer.getUid()));
						customerList.add(sclCustomer);
				}

			modelService.saveAll(customerList);
		}else{
				LOGGER.info("Dealer retailer list is empty");
			}
		}catch (Exception e){
			LOGGER.info(String.format("Exception in Network type Cron job:%s,%s",e.getMessage(),e.getCause()));
		}
	        return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}

}
