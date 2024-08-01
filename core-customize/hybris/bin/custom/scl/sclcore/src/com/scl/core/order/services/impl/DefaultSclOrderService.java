package com.scl.core.order.services.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.scl.core.customer.services.SclCustomerService;
import com.scl.core.dao.SCLMastersDao;
import com.scl.core.model.DealerDriverDetailsModel;
import com.scl.core.model.DealerVehicleDetailsModel;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.services.TerritoryManagementService;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.customer.dao.SclCustomerAccountDao;
import com.scl.core.dao.BillingPriceMasterDao;
import com.scl.core.cart.service.SclCartService;
import com.scl.core.enums.CustomerCategory;
import com.scl.core.enums.DeliveryItemStatus;
import com.scl.core.enums.WarehouseType;
import com.scl.core.model.BillingPriceMasterModel;
import com.scl.core.order.dao.SclOrderCountDao;
import com.scl.core.order.services.SclOrderService;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.store.services.BaseStoreService;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

public class DefaultSclOrderService implements SclOrderService{

	private static final Logger LOG = LoggerFactory.getLogger(DefaultSclOrderService.class.getName());
	private UserService userService;
	private SclOrderCountDao sclOrderCountDao;
	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private BaseStoreService baseStoreService;

	@Autowired
    private SclCustomerAccountDao sclCustomerAccountDao;

	@Resource(name="sclCartService")
	private SclCartService sclCartService;
	
	@Autowired
	BillingPriceMasterDao billingPriceMasterDao;

	@Autowired
	TerritoryManagementService territoryManagementService;

	@Resource
	private SclCustomerService sclCustomerService;

	@Resource
	private SCLMastersDao sclMastersDao;

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
		return getSclOrderCountDao().findOrdersByStatusForSO(currentUser,statusSet.toArray(new OrderStatus[statusSet.size()]),approvalPending);
	}

	@Override
	public Integer getOrderEntryCountByStatus(String status) {
		final UserModel currentUser = getUserService().getCurrentUser();
		String statues = validateAndMapOrderStatuses(status);
		final Set<OrderStatus> statusSet = extractOrderStatuses(statues);
		return getSclOrderCountDao().findOrderEntriesByStatusForSO(currentUser,statusSet.toArray(new OrderStatus[statusSet.size()]));
	}
	
	@Override
	public Integer getDeliveryItemCountByStatus(String status) {
		final UserModel currentUser = getUserService().getCurrentUser();
		String statues = validateAndMapOrderStatuses(status);
		final Set<DeliveryItemStatus> statusSet = extractDeliveryItemStatuses(statues);
		return getSclOrderCountDao().findDeliveryItemByStatusForSO(currentUser,statusSet.toArray(new DeliveryItemStatus[statusSet.size()]));
	}
	
    protected Set<OrderStatus> extractOrderStatuses(final String statuses)
    {
        final String[] statusesStrings = statuses.split(SclCoreConstants.ORDER.ENUM_VALUES_SEPARATOR);

        final Set<OrderStatus> statusesEnum = new HashSet<>();
        for (final String status : statusesStrings)
        {
            statusesEnum.add(OrderStatus.valueOf(status));
        }
        return statusesEnum;
    }
    
    protected Set<DeliveryItemStatus> extractDeliveryItemStatuses(final String statuses)
    {
        final String[] statusesStrings = statuses.split(SclCoreConstants.ORDER.ENUM_VALUES_SEPARATOR);

        final Set<DeliveryItemStatus> statusesEnum = new HashSet<>();
        for (final String status : statusesStrings)
        {
            statusesEnum.add(DeliveryItemStatus.valueOf(status));
        }
        return statusesEnum;
    }

	public String validateAndMapOrderStatuses(final String inputStatus){
		String statuses;
		switch(inputStatus){
		case SclCoreConstants.ORDER.PENDING_FOR_APPROVAL_STATUS:
			statuses = getConfigurationService().getConfiguration().getString(SclCoreConstants.ORDER.PENDING_FOR_APPROVAL_STATUS_MAPPING);
			break;

		case SclCoreConstants.ORDER.VEHICLE_ARRIVAL_CONFIRMATION_STATUS:
			statuses = getConfigurationService().getConfiguration().getString(SclCoreConstants.ORDER.VEHICLE_ARRIVAL_CONFIRMATION_STATUS_MAPPING);
			break;

		case SclCoreConstants.ORDER.WAITING_FOR_DISPATCH_STATUS:
			statuses = getConfigurationService().getConfiguration().getString(SclCoreConstants.ORDER.WAITING_FOR_DISPATCH_STATUS_MAPPING);
			break;

		case SclCoreConstants.ORDER.TO_BE_DELIVERED_BY_TODAY_STATUS:
			statuses = getConfigurationService().getConfiguration().getString(SclCoreConstants.ORDER.TO_BE_DELIVERED_BY_TODAY_STATUS_MAPPING);
			break;

			case SclCoreConstants.ORDER.ORDER_CANCELLATION_STATUS:
				statuses = getConfigurationService().getConfiguration().getString(SclCoreConstants.ORDER.ORDER_CANCELLATION_STATUS_MAPPING);
				break;

			case SclCoreConstants.ORDER.ORDER_LINE_CANCELLATION_STATUS:
				statuses = getConfigurationService().getConfiguration().getString(SclCoreConstants.ORDER.ORDER_LINE_CANCELLATION_STATUS_MAPPING);
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

	
	public SclCustomerAccountDao getSclCustomerAccountDao() {
		return sclCustomerAccountDao;
	}

	public void setSclCustomerAccountDao(SclCustomerAccountDao sclCustomerAccountDao) {
		this.sclCustomerAccountDao = sclCustomerAccountDao;
	}

	/**
	 * Get Order count for current month for SO
	 * @param status
	 * @return
	 */
	@Override
	public Map<String, Long> getOrderDeliveredCountAndQty(OrderStatus status) {
		final UserModel currentUser = getUserService().getCurrentUser();
		return getSclOrderCountDao().findOrdersInAnyStatusByDateRange(currentUser, status);
	}
	
	@Override
	public Map<String, Object>  getDirectDispatchOrdersMTDPercentage(int month, int year) {
		Map<String, Object> map = new HashMap<>();
		final UserModel currentUser = getUserService().getCurrentUser();
		Integer directDispatchOrdersMTD = getSclOrderCountDao().findDirectDispatchOrdersMTDCount(currentUser, WarehouseType.PLANT, month, year);
		Integer secondaryDispatchOrdersMTD = getSclOrderCountDao().findDirectDispatchOrdersMTDCount(currentUser, WarehouseType.DEPOT, month, year);
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
		Integer orderCount = getSclOrderCountDao().checkOrderCountBeforeThreeMonths(currentUser, startDate, endDate);
		endDate = Calendar.getInstance().getTime();
		List<String> listOfERPCityByDistrictCode = sclCartService.getListOfERPCityByDistrict(districtCode);
		Set<String> erpCity = listOfERPCityByDistrictCode.stream().collect(Collectors.toSet());

		if (orderCount>0) {
			maxOrderQuantity = getSclOrderCountDao().findMaxOrderQuantityForSO(currentUser, startDate, endDate);
		}
		else {
			List<Double> orderQuantityList = getSclOrderCountDao().findOrderQuantityListForSO(currentUser, startDate, endDate, erpCity);
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
		return getSclOrderCountDao().findCancelOrdersByStatusForSO(currentUser,statusSet.toArray(new OrderStatus[statusSet.size()]));
	}

	@Override
	public Integer getCancelOrderEntryCountByStatus(String status) {
		final UserModel currentUser = getUserService().getCurrentUser();
		String statues = validateAndMapOrderStatuses(status);
		final Set<OrderStatus> statusSet = extractOrderStatuses(statues);
		return getSclOrderCountDao().findCancelOrderEntriesByStatusForSO(currentUser,statusSet.toArray(new OrderStatus[statusSet.size()]));
	}

	@Override
	public Boolean checkMaxOrderQuantityForSO(Double orderQty, SclCustomerModel sclCustomer) {
		validateParameterNotNull(orderQty, "order quantity must not be null");
		Double maxOrderQuantity = 0.0;

		//getordercount
		Integer orderCount = getSclOrderCountDao().checkOrderCountBeforeThreeMonths(sclCustomer);

		//count >0
		if (orderCount>0) {
			maxOrderQuantity = getSclOrderCountDao().findMaxOrderQuantityForSO(sclCustomer);
		}
		//count == 0
		//New Territory Change
//		else {
//			List<String> subArea = territoryManagementService.getAllSubAreaForCustomer(sclCustomer.getUid());
//			List<Double> orderQuantityList = getSclOrderCountDao().findOrderQuantityListForSO(subArea.get(0));
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

	public SclOrderCountDao getSclOrderCountDao() {
		return sclOrderCountDao;
	}

	public void setSclOrderCountDao(SclOrderCountDao sclOrderCountDao) {
		this.sclOrderCountDao = sclOrderCountDao;
	}


}
