package com.eydms.core.order.services.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.eydms.core.customer.services.EyDmsCustomerService;
import com.eydms.core.dao.EYDMSMastersDao;
import com.eydms.core.model.DealerDriverDetailsModel;
import com.eydms.core.model.DealerVehicleDetailsModel;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.services.TerritoryManagementService;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.customer.dao.EyDmsCustomerAccountDao;
import com.eydms.core.dao.BillingPriceMasterDao;
import com.eydms.core.cart.service.EyDmsCartService;
import com.eydms.core.enums.CustomerCategory;
import com.eydms.core.enums.WarehouseType;
import com.eydms.core.model.BillingPriceMasterModel;
import com.eydms.core.order.dao.EyDmsOrderCountDao;
import com.eydms.core.order.services.EyDmsOrderService;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.store.services.BaseStoreService;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

public class DefaultEyDmsOrderService implements EyDmsOrderService{

	private static final Logger LOG = LoggerFactory.getLogger(DefaultEyDmsOrderService.class.getName());
	private UserService userService;
	private EyDmsOrderCountDao eydmsOrderCountDao;
	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private BaseStoreService baseStoreService;

	@Autowired
    private EyDmsCustomerAccountDao eydmsCustomerAccountDao;

	@Resource(name="eydmsCartService")
	private EyDmsCartService eydmsCartService;
	
	@Autowired
	BillingPriceMasterDao billingPriceMasterDao;

	@Autowired
	TerritoryManagementService territoryManagementService;

	@Resource
	private EyDmsCustomerService eydmsCustomerService;

	@Resource
	private EYDMSMastersDao eydmsMastersDao;

	@Resource
	private ModelService modelService;

	
	/**
	 * Get Order Count by Status for SO
	 * @param status
	 * @return
	 */
	@Override
	public Integer getOrderCountByStatus(String status, Boolean approvalPending) {
		final UserModel currentUser = getUserService().getCurrentUser();
		String statues = validateAndMapOrderStatuses(status);
		final Set<OrderStatus> statusSet = extractOrderStatuses(statues);
		return getEyDmsOrderCountDao().findOrdersByStatusForSO(currentUser,statusSet.toArray(new OrderStatus[statusSet.size()]),approvalPending);
	}

	@Override
	public Integer getOrderEntryCountByStatus(String status) {
		final UserModel currentUser = getUserService().getCurrentUser();
		String statues = validateAndMapOrderStatuses(status);
		final Set<OrderStatus> statusSet = extractOrderStatuses(statues);
		return getEyDmsOrderCountDao().findOrderEntriesByStatusForSO(currentUser,statusSet.toArray(new OrderStatus[statusSet.size()]));
	}
	
    protected Set<OrderStatus> extractOrderStatuses(final String statuses)
    {
        final String[] statusesStrings = statuses.split(EyDmsCoreConstants.ORDER.ENUM_VALUES_SEPARATOR);

        final Set<OrderStatus> statusesEnum = new HashSet<>();
        for (final String status : statusesStrings)
        {
            statusesEnum.add(OrderStatus.valueOf(status));
        }
        return statusesEnum;
    }

	public String validateAndMapOrderStatuses(final String inputStatus){
		String statuses;
		switch(inputStatus){
		case EyDmsCoreConstants.ORDER.PENDING_FOR_APPROVAL_STATUS:
			statuses = getConfigurationService().getConfiguration().getString(EyDmsCoreConstants.ORDER.PENDING_FOR_APPROVAL_STATUS_MAPPING);
			break;

		case EyDmsCoreConstants.ORDER.VEHICLE_ARRIVAL_CONFIRMATION_STATUS:
			statuses = getConfigurationService().getConfiguration().getString(EyDmsCoreConstants.ORDER.VEHICLE_ARRIVAL_CONFIRMATION_STATUS_MAPPING);
			break;

		case EyDmsCoreConstants.ORDER.WAITING_FOR_DISPATCH_STATUS:
			statuses = getConfigurationService().getConfiguration().getString(EyDmsCoreConstants.ORDER.WAITING_FOR_DISPATCH_STATUS_MAPPING);
			break;

		case EyDmsCoreConstants.ORDER.TO_BE_DELIVERED_BY_TODAY_STATUS:
			statuses = getConfigurationService().getConfiguration().getString(EyDmsCoreConstants.ORDER.TO_BE_DELIVERED_BY_TODAY_STATUS_MAPPING);
			break;

			case EyDmsCoreConstants.ORDER.ORDER_CANCELLATION_STATUS:
				statuses = getConfigurationService().getConfiguration().getString(EyDmsCoreConstants.ORDER.ORDER_CANCELLATION_STATUS_MAPPING);
				break;

			case EyDmsCoreConstants.ORDER.ORDER_LINE_CANCELLATION_STATUS:
				statuses = getConfigurationService().getConfiguration().getString(EyDmsCoreConstants.ORDER.ORDER_LINE_CANCELLATION_STATUS_MAPPING);
				break;

		default :
			statuses = inputStatus;
		}
		return statuses;
	}
	
	
	public ConfigurationService getConfigurationService() {
		return configurationService;
	}


	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	

	public BaseStoreService getBaseStoreService() {
		return baseStoreService;
	}

	public void setBaseStoreService(BaseStoreService baseStoreService) {
		this.baseStoreService = baseStoreService;
	}

	
	public EyDmsCustomerAccountDao getEyDmsCustomerAccountDao() {
		return eydmsCustomerAccountDao;
	}

	public void setEyDmsCustomerAccountDao(EyDmsCustomerAccountDao eydmsCustomerAccountDao) {
		this.eydmsCustomerAccountDao = eydmsCustomerAccountDao;
	}

	/**
	 * Get Order count for current month for SO
	 * @param status
	 * @return
	 */
	@Override
	public Map<String, Long> getOrderDeliveredCountAndQty(OrderStatus status) {
		final UserModel currentUser = getUserService().getCurrentUser();
		return getEyDmsOrderCountDao().findOrdersInAnyStatusByDateRange(currentUser, status);
	}
	
	@Override
	public Map<String, Object>  getDirectDispatchOrdersMTDPercentage(int month, int year) {
		Map<String, Object> map = new HashMap<>();
		final UserModel currentUser = getUserService().getCurrentUser();
		Integer directDispatchOrdersMTD = getEyDmsOrderCountDao().findDirectDispatchOrdersMTDCount(currentUser, WarehouseType.PLANT, month, year);
		Integer secondaryDispatchOrdersMTD = getEyDmsOrderCountDao().findDirectDispatchOrdersMTDCount(currentUser, WarehouseType.DEPOT, month, year);
		if(directDispatchOrdersMTD<=0 && secondaryDispatchOrdersMTD<=0) {
			throw new IllegalArgumentException("directDispatchOrdersMTD and secondaryDispatchOrdersMTD must be a positive non-zero value");
		}
		else {
			 map.put("directDispatch", ((double)directDispatchOrdersMTD/((double)directDispatchOrdersMTD+(double)secondaryDispatchOrdersMTD)) * 100);
			 map.put("secondaryDispatch", ((double)secondaryDispatchOrdersMTD/((double)directDispatchOrdersMTD+(double)secondaryDispatchOrdersMTD)) * 100);
			 return map;
		}
	}
	
	@Override
	public Boolean checkOrderQuantityForSO(Integer orderQty, String districtCode) {
		final UserModel currentUser = getUserService().getCurrentUser();
		validateParameterNotNull(orderQty, "order quantity must not be null");
		Date startDate = new Date();
		Date endDate = new Date();
		Double maxOrderQuantity = 0.0;
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -6);
		startDate = cal.getTime();
		cal.add(Calendar.MONTH, 3);
		endDate = cal.getTime();
		Integer orderCount = getEyDmsOrderCountDao().checkOrderCountBeforeThreeMonths(currentUser, startDate, endDate);
		endDate = Calendar.getInstance().getTime();
		List<String> listOfERPCityByDistrictCode = eydmsCartService.getListOfERPCityByDistrict(districtCode);
		Set<String> erpCity = listOfERPCityByDistrictCode.stream().collect(Collectors.toSet());

		if (orderCount>0) {
			maxOrderQuantity = getEyDmsOrderCountDao().findMaxOrderQuantityForSO(currentUser, startDate, endDate);
		}
		else {
			List<Double> orderQuantityList = getEyDmsOrderCountDao().findOrderQuantityListForSO(currentUser, startDate, endDate, erpCity);
			maxOrderQuantity = orderQuantityList.stream().collect(Collectors.averagingDouble(d->d));
		}
		if (maxOrderQuantity>0.0) {
			return orderQty <= maxOrderQuantity ? true : false;
		}
		else {
			throw new IllegalArgumentException("maxOrderQuantity must be a positive non-zero value");
		}
		
	}
	

	@Override
	public Double getBillingPriceForProduct(BaseSiteModel brand, String inventoryItemId, String erpCity, CustomerCategory customerCategory, String packagingCondition, String state){
		Calendar cal = Calendar.getInstance();
        Date currentDate=cal.getTime();
      
        BillingPriceMasterModel billingPriceMasterModel = billingPriceMasterDao.getBillingPriceMasterForProduct(brand, inventoryItemId, erpCity, customerCategory, packagingCondition, state, currentDate);
		return billingPriceMasterModel!=null ? billingPriceMasterModel.getBillingPrice() : 0.0;
	}

	@Override
	public Integer getCancelOrderCountByStatus(String status) {
		final UserModel currentUser = getUserService().getCurrentUser();
		String statues = validateAndMapOrderStatuses(status);
		final Set<OrderStatus> statusSet = extractOrderStatuses(statues);
		return getEyDmsOrderCountDao().findCancelOrdersByStatusForSO(currentUser,statusSet.toArray(new OrderStatus[statusSet.size()]));
	}

	@Override
	public Integer getCancelOrderEntryCountByStatus(String status) {
		final UserModel currentUser = getUserService().getCurrentUser();
		String statues = validateAndMapOrderStatuses(status);
		final Set<OrderStatus> statusSet = extractOrderStatuses(statues);
		return getEyDmsOrderCountDao().findCancelOrderEntriesByStatusForSO(currentUser,statusSet.toArray(new OrderStatus[statusSet.size()]));
	}

	@Override
	public Boolean checkMaxOrderQuantityForSO(Double orderQty, EyDmsCustomerModel eydmsCustomer) {
		validateParameterNotNull(orderQty, "order quantity must not be null");
		Double maxOrderQuantity = 0.0;

		//getordercount
		Integer orderCount = getEyDmsOrderCountDao().checkOrderCountBeforeThreeMonths(eydmsCustomer);

		//count >0
		if (orderCount>0) {
			maxOrderQuantity = getEyDmsOrderCountDao().findMaxOrderQuantityForSO(eydmsCustomer);
		}
		//count == 0
		//New Territory Change
//		else {
//			List<String> subArea = territoryManagementService.getAllSubAreaForCustomer(eydmsCustomer.getUid());
//			List<Double> orderQuantityList = getEyDmsOrderCountDao().findOrderQuantityListForSO(subArea.get(0));
//			Optional<Double>  maxQty= orderQuantityList.stream().max(Comparator.naturalOrder());
//			if(maxQty.isPresent()) {
//				maxOrderQuantity = maxQty.get();
//			}
//		}
		if (maxOrderQuantity>0.0) {
			return orderQty <= maxOrderQuantity ? true : false;
		}
		else {
			throw new IllegalArgumentException("maxOrderQuantity must be a positive non-zero value");
		}
	}

	protected static Date getDateConstraint(LocalDate localDate) {
		ZoneId zone = ZoneId.systemDefault();
		ZonedDateTime dateTime = localDate.atStartOfDay(zone);
		Date date = Date.from(dateTime.toInstant());
		return date;
	}



	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public EyDmsOrderCountDao getEyDmsOrderCountDao() {
		return eydmsOrderCountDao;
	}

	public void setEyDmsOrderCountDao(EyDmsOrderCountDao eydmsOrderCountDao) {
		this.eydmsOrderCountDao = eydmsOrderCountDao;
	}


}
