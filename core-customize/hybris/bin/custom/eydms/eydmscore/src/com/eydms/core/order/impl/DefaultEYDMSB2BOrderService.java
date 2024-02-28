package com.eydms.core.order.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

import com.hybris.yprofile.dto.Order;
import com.eydms.core.cart.dao.EyDmsERPCityDao;
import com.eydms.core.cart.dao.EyDmsISODeliverySLADao;
import com.eydms.core.cart.dao.EyDmsSalesOrderDeliverySLADao;
import com.eydms.core.cart.dao.EyDmsWarehouseDao;
import com.eydms.core.dao.OrderRequisitionDao;
import com.eydms.core.dao.DealerDao;
import com.eydms.core.enums.*;
import com.eydms.core.event.EyDmsOrderCancelEvent;
import com.eydms.core.event.EyDmsOrderLineCancelEvent;
import com.eydms.core.model.*;
import com.eydms.core.notifications.service.EyDmsNotificationService;
import com.eydms.core.order.strategy.EyDmsModifyOrderStrategy;
import com.eydms.core.region.dao.ERPCityDao;
import com.eydms.core.services.OrderRequisitionService;
import com.eydms.core.services.EyDmsWorkflowService;
import com.eydms.core.services.TerritoryManagementService;
import com.eydms.facades.data.EpodFeedbackData;
import com.eydms.facades.order.data.EyDmsOrderHistoryData;
import de.hybris.platform.b2b.services.B2BOrderService;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.commerceservices.service.data.CommerceCartParameter;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.product.UnitModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.notificationservices.enums.NotificationType;
import de.hybris.platform.notificationservices.enums.SiteMessageType;
import de.hybris.platform.notificationservices.model.SiteMessageForCustomerModel;
import de.hybris.platform.notificationservices.model.SiteMessageModel;
import de.hybris.platform.order.CalculationService;
import de.hybris.platform.order.CartService;
import de.hybris.platform.order.daos.DeliveryModeDao;
import de.hybris.platform.order.exceptions.CalculationException;
import de.hybris.platform.orderhistory.OrderHistoryService;
import de.hybris.platform.orderhistory.model.OrderHistoryEntryModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.event.EventService;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.eydms.core.cart.service.EyDmsCartService;
import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.dao.DeliverySlotMasterDao;
import com.eydms.core.dao.EYDMSMastersDao;
import com.eydms.core.order.EYDMSB2BOrderService;
import com.eydms.core.order.dao.EyDmsOrderCountDao;
import com.eydms.facades.data.DeliveryDateAndSlotData;
import com.eydms.facades.data.DeliveryDateAndSlotListData;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.services.impl.DefaultB2BOrderService;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BOrderApprovalData;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.ordersplitting.WarehouseService;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;

import javax.annotation.Resource;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;
import static de.hybris.platform.util.localization.Localization.getLocalizedString;

/**
 *  Interface for eydms B2BOrder Services
 */
public class DefaultEYDMSB2BOrderService extends DefaultB2BOrderService implements EYDMSB2BOrderService {

	/**
	 * 
	 */
	private static final Logger LOG = Logger.getLogger(DefaultEYDMSB2BOrderService.class);
	private static final long serialVersionUID = 1L;
	private I18NService i18NService;
	private BusinessProcessService businessProcessService;
	private ConfigurationService configurationService;
	private FlexibleSearchService flexibleSearchService;

	@Autowired
	EyDmsWorkflowService eydmsWorkflowService;
	
	private EYDMSMastersDao eydmsMastersDao;
	@Autowired
	private EyDmsCartService eydmsCartService;

	@Autowired
	private EyDmsOrderCountDao orderCountDao;

	@Autowired
	B2BOrderService b2BOrderService;

	@Autowired
	OrderHistoryService orderHistoryService;

	@Autowired
	ERPCityDao erpCityDao;

	@Autowired
	EyDmsWarehouseDao eydmsWarehouseDao;

	@Autowired
	ProductService productService;

	@Autowired
	BaseStoreService baseStoreService;

	@Autowired
	EyDmsOrderCountDao eydmsOrderCountDao;
	
	@Resource
	DealerDao dealerDao;
	
	@Autowired
    private EyDmsERPCityDao eydmsERPCityDao;
	
	@Autowired
	private DeliveryModeDao deliverModeDao;

	@Autowired
	TerritoryManagementService territoryManagementService;

	@Autowired
    private EyDmsSalesOrderDeliverySLADao eydmsSalesOrderDeliverySLADao;

	@Autowired
    private EyDmsISODeliverySLADao eydmsISODeliverySLADao;
	
	@Autowired
	Converter<OrderEntryModel, EyDmsOrderHistoryData> eydmsOrderEntryHistoryCardConverter;

	@Autowired
	Converter<OrderModel, EyDmsOrderHistoryData> eydmsOrderHistoryCardConverter;

	@Resource
	private EnumerationService enumerationService;

	@Resource
	private EyDmsModifyOrderStrategy eydmsModifyOrderStrategy;

	@Resource
	private CalculationService calculationService;
	
    @Autowired
    private WarehouseService warehouseService;
    
    @Autowired
    private DeliverySlotMasterDao deliverySlotMasterDao;

	@Autowired
	EventService eventService;

	@Autowired
	private CartService cartService;

	@Autowired
	KeyGenerator siteMessageUidGenerator;

	@Autowired
	OrderRequisitionDao orderRequisitionDao;

	@Autowired
	OrderRequisitionService orderRequisitionService;

	@Autowired
	BaseSiteService baseSiteService;

	@Autowired
    UserService userService;

	@Autowired
	EyDmsNotificationService eydmsNotificationService;

	private EYDMSB2BOrderService eydmsB2BOrderService;

	public EYDMSB2BOrderService getEyDmsB2BOrderService() {
		return eydmsB2BOrderService;
	}

	private static final Logger LOGGER = Logger.getLogger(DefaultEYDMSB2BOrderService.class);
	protected static final int APPEND_AS_LAST = -1;

	private static final String NOTIFICATION_GREETING = "Dear ";

	private static final String ORDER_PLACED_BY = ", order placed by Dealer ";

	private static final String ORDER_QUANTITY = " of quantity ";

	private static final String ORDER_DATE = " on ";

	private static final String ORDER_CANCELLED = ", has been cancelled.";
	/**
	 * Method to update the permissionResult oncce SO Takes action
	 * @param order
	 * @param b2BOrderApprovalData
	 * @return
	 */
	@Override
	public OrderModel updateOrderWithPermissionResult(final OrderModel order , B2BOrderApprovalData b2BOrderApprovalData){
		OrderProcessModel orderProcessModel;
		Optional<OrderProcessModel> orderProcess = order.getOrderProcess().stream().filter(op -> op instanceof OrderProcessModel).findFirst();
		if(orderProcess.isPresent()){
			orderProcessModel = orderProcess.get();
		}
		else{
			throw new ModelNotFoundException("No Order Process Found for Order: "+order.getCode());
		}
		if(order.getStatus()!=null && order.getStatus().equals(OrderStatus.ORDER_FAILED_VALIDATION)) {
			order.setStatus(OrderStatus.APPROVED);
			order.setOrderSentForApprovalDate(new Date());
			getModelService().save(order);
			getModelService().refresh(order);

			for(int i=0;i<order.getEntries().size();i++) {
				OrderEntryModel entry = (OrderEntryModel) order.getEntries().get(i);
				entry.setStatus(OrderStatus.APPROVED);
				getModelService().save(entry);
				getModelService().refresh(entry);
			}
			getModelService().refresh(order);
			getBusinessProcessService().triggerEvent(orderProcessModel.getCode()+"_"+ EyDmsCoreConstants.APPROVAL_CONSTANT.ORDER_REVIEW_DECISION_EVENT_NAME);
			return order;			
		}
		else {
			throw new UnsupportedOperationException(String.format("Order %s cannot be approved at this stage", order.getCode()));
		}
		/*if(CollectionUtils.isEmpty(order.getPermissionResults())){
			throw new ModelNotFoundException("No Approval Object Assigned to the Order : "+order.getCode());
		}
		else if(order.getPermissionResults().size()>1){
			throw new AmbiguousIdentifierException("More than One approval result found for order: "+order.getCode());
		}
		List<B2BPermissionResultModel> persmissionResults = new ArrayList<>(order.getPermissionResults());
		B2BPermissionResultModel b2BPermissionResultModel = persmissionResults.get(0);
		b2BPermissionResultModel.setNote(b2BOrderApprovalData.getApprovalComments(),getI18NService().getCurrentLocale());

		b2BPermissionResultModel.setStatus(PermissionStatus.APPROVED);*/

	}


	/**
	 * Method to map order statuses with input status
	 * @param inputStatus
	 * @return
	 */
	@Override
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

	@Override
	public boolean cancelOrderByCode(String orderCode, String reason) {
		B2BCustomerModel b2BCustomer = (B2BCustomerModel) getUserService().getCurrentUser();
		OrderModel order = getOrderForCode(orderCode);
		return cancelOrderFromCrm(order, reason, b2BCustomer,false);
	}
	
	@Override
	public boolean cancelOrderFromCrm(OrderModel order, String reason, B2BCustomerModel b2BCustomer, Boolean fromCancelJob) {
		long count = order.getEntries().stream().filter(entry -> entry.getTruckDispatcheddate()!=null).count();
		if(count>0) {
			String errorMsg = String.format("Order Cannot be cancelled. One or More Order Entry %s is dispatched for order ", order.getCode());
			LOGGER.error(errorMsg);
			throw new UnsupportedOperationException(errorMsg);
		}
		order.setStatus(OrderStatus.CANCELLED);
		order.setCancelledBy(b2BCustomer);
		order.setCancelReason(reason);
		order.setCancelledDate(new Date());

		if(order.getRequisitions()!=null && !order.getRequisitions().isEmpty() && order.getRequisitions().size()==1) {
			OrderRequisitionModel orderRequisitionModel = order.getRequisitions().get(0);
			orderRequisitionModel.setStatus(RequisitionStatus.CANCELLED);
			orderRequisitionModel.setCancelledDate(new Date());
			orderRequisitionModel.setCancelledBy(b2BCustomer);
			orderRequisitionModel.setCancelReason(reason);
			getModelService().save(orderRequisitionModel);
		}

		for(int i=0;i<order.getEntries().size();i++) {
			OrderEntryModel entry = (OrderEntryModel) order.getEntries().get(i);
			entry.setStatus(OrderStatus.CANCELLED);
			entry.setCancelledDate(new Date());
			entry.setCancelReason(reason);
			getModelService().save(entry);
		}
		getModelService().save(order);
		//Trigger Order cancellation
		if(null != order.getErpOrderNumber()) {
			EyDmsOrderCancelProcessModel processModel = new EyDmsOrderCancelProcessModel();
			processModel.setOrder(order);
			submitOrderForCancellation(processModel);
		}
		else
		{
			LOGGER.error("Erp Order Number is not found for :" + order.getCode());
		}
		//cancelOrder(order);
		try {
			StringBuilder builder = new StringBuilder();
			Double amount= order.getTotalPrice();
			String formattedAmount = formatIndianNumber(amount);
			Double orderQty = order.getEntries().stream().collect(Collectors.summingDouble(each -> each.getQuantityInMT()));
			builder.append("Order no. " + order.getCode() +" of product "+order.getEntries().get(0).getProduct().getName() +" with order of "+ orderQty  + " MT of ₹"+ formattedAmount );
			builder.append(" has been successfully cancelled for " +order.getUser().getUid());

			String body = builder.toString();

			StringBuilder builder1 = new StringBuilder("Order is Cancelled");

			String subject = builder1.toString();


			NotificationCategory category = NotificationCategory.ORDER_CANCELLED_CRM;
			eydmsNotificationService.submitOrderNotification(order, (B2BCustomerModel) order.getUser(),body,subject,category);

			StringBuilder builder2 = new StringBuilder();
			EyDmsUserModel so = territoryManagementService.getSOforCustomer((EyDmsCustomerModel) order.getUser());
			builder2.append("Order no. " + order.getCode() +" of product "+order.getEntries().get(0).getProduct().getName() +" with order of "+ orderQty  + " MT of ₹"+ formattedAmount );
			builder2.append(" has been successfully cancelled for "+so.getUid());

			String body2 = builder2.toString();
			eydmsNotificationService.submitOrderNotification(order,so,body2,subject,category);

			StringBuilder builder3 = new StringBuilder();
			EyDmsCustomerModel sp = territoryManagementService.getSpForCustomerAndBrand((EyDmsCustomerModel) order.getUser(),order.getSite());
			builder3.append("Order no. " + order.getCode() +" of product "+order.getEntries().get(0).getProduct().getName() +" with order of "+ orderQty  + " MT of ₹"+ formattedAmount );
			builder3.append(" has been successfully cancelled for "+sp.getUid());

			String body3 = builder3.toString();
			eydmsNotificationService.submitOrderNotification(order,sp,body3,subject,category);
		}
		catch(Exception e) {
			LOGGER.error("Error while sending cancel order notification");
		}
		return Boolean.TRUE;
	}

	@Override
	public boolean cancelOrderEntry(String orderCode, Integer orderEntryNo, String reason) {

		//EyDmsUserModel eydmsUser = (EyDmsUserModel) getUserService().getCurrentUser();
		OrderModel order = getOrderForCode(orderCode);
		Optional<AbstractOrderEntryModel> orderEntryOptional = order.getEntries().stream().filter(entry-> orderEntryNo == entry.getEntryNumber()).findFirst();
		String errorMsg;
		if(orderEntryOptional.isPresent()) {
			OrderEntryModel orderEntryModel = (OrderEntryModel) orderEntryOptional.get();

			Double deliveryQty = orderEntryModel.getDeliveryQty();

			if(deliveryQty==null || deliveryQty<=0)
			{
				B2BCustomerModel b2BCustomer = (B2BCustomerModel) getUserService().getCurrentUser();

				boolean isOrderEntryCancelled = cancelOrderEntryFromCRM(orderEntryModel, reason, b2BCustomer, false);
				if(isOrderEntryCancelled) {
					getRequisitionStatusByOrderLines(order);
					try {

						StringBuilder builder = new StringBuilder();

						builder.append("Order no. " + order.getCode() + "/" + orderEntryModel.getEntryNumber() + " of product "+orderEntryModel.getProduct().getName()+" with order of "+orderEntryModel.getQuantityInMT() + " MT of Rs. "+ orderEntryModel.getTotalPrice() );
						builder.append(" has been successfully cancelled.");

						String body = builder.toString();

						StringBuilder builder1 = new StringBuilder("Order Line is Cancelled");

						String subject = builder1.toString();

						NotificationCategory category = NotificationCategory.ORDER_CANCELLED_CRM;
						eydmsNotificationService.submitOrderNotification(order, (B2BCustomerModel) order.getUser(),body,subject,category);

						EyDmsUserModel so = territoryManagementService.getSOforCustomer((EyDmsCustomerModel) order.getUser());
						eydmsNotificationService.submitOrderNotification(order,so,body,subject,category);

						EyDmsCustomerModel sp = territoryManagementService.getSpForCustomerAndBrand((EyDmsCustomerModel) order.getUser(),order.getSite());
						eydmsNotificationService.submitOrderNotification(order,sp,body,subject,category);
					}
					catch(Exception e) {
						LOGGER.error("Error while sending cancel order entry notification");
					}
				}
				return isOrderEntryCancelled;

			}
			errorMsg = String.format("Order Cannot be cancelled. Order Entry %s is dispatched", orderCode + "_" + orderEntryNo );
			LOGGER.error(errorMsg);
			throw new UnsupportedOperationException(errorMsg);
		}
		errorMsg = String.format("Order Cannot be cancelled. Order Entry %s not found",  orderCode + "_" + orderEntryNo );
		LOGGER.error(String.format(errorMsg));
		throw new UnsupportedOperationException(String.format(errorMsg));
	}

	@Override
	public boolean cancelOrderEntryFromCRM(OrderEntryModel orderEntryModel, String reason, B2BCustomerModel user, Boolean fromCancelJob) {
		OrderModel order =  orderEntryModel.getOrder();
		orderEntryModel.setStatus(OrderStatus.CANCELLED);
		if(user!=null) {
			orderEntryModel.setCancelledBy(user);
		}
		orderEntryModel.setCancelReason(reason);
		orderEntryModel.setCancelledDate(new Date());
		getModelService().save(orderEntryModel);

		saveOrderRequisitionEntryDetails(order, orderEntryModel, "LINE_CANCELLED");

		//Trigger Order Line cancellation
		if(null != orderEntryModel.getErpLineItemId() && !orderEntryModel.getErpLineItemId().isEmpty()) {
			EyDmsOrderLineCancelProcessModel processModel = new EyDmsOrderLineCancelProcessModel();
			processModel.setOrder(order);
			processModel.setCrmEntryNumber(orderEntryModel.getEntryNumber());
			processModel.setEntryNumber(Integer.valueOf(orderEntryModel.getErpLineItemId()));
			submitOrderLineForCancellation(processModel);
		}

		else
		{
			LOGGER.error("Erp order line item id is not found for" + order.getCode() + "and" + orderEntryModel.getEntryNumber());
		}
		return Boolean.TRUE;
	}
	
	@Override
	public DeliveryDateAndSlotListData getOptimalDeliveryDateAndSlot(final int orderQuantity,final String routeId, B2BCustomerModel user, final LocalDateTime  orderdate, final String sourceCode){
		DeliveryDateAndSlotListData list = new DeliveryDateAndSlotListData();
//		List<DeliveryDateAndSlotData> dataList = new ArrayList<DeliveryDateAndSlotData>();
//		final SalesOrderDeliverySLAModel salesOrderDeliverySLA = eydmsSalesOrderDeliverySLADao.findByRoute(routeId);
//		if(salesOrderDeliverySLA!=null) {
//			int diCount = getDICountForOrder(salesOrderDeliverySLA,orderQuantity);
//			int maxTruckCount = salesOrderDeliverySLA.getMaxTruckPerDayPerCustomer();
//
//			WarehouseModel sourceMaster =  warehouseService.getWarehouseForCode(sourceCode);//getSourceMasterForSourceAndBrand(source,brand, erpCityCode);
//			if(sourceMaster!=null && sourceMaster.getWorkingHourStartTime()!=null && sourceMaster.getWorkingHourEndTime()!=null) {
//				int sla = salesOrderDeliverySLA.getDeliverySLA();
//				long slaInMin = sla*60;
//				int tempQty = orderQuantity;
//				double truckCapacity =salesOrderDeliverySLA.getCommonTruckCapacity();
//
//				String workingHourStartTime = sourceMaster.getWorkingHourStartTime();
//				String workingHourEndTime = sourceMaster.getWorkingHourEndTime();
//
//				LocalTime localTimeWorkingStartTime = LocalTime.parse(workingHourStartTime);
//				LocalTime localTimeWorkingEndTime = LocalTime.parse(workingHourEndTime);
//
//				long minutes = ChronoUnit.MINUTES.between(localTimeWorkingStartTime, localTimeWorkingEndTime);
//
//				LocalTime currentTime = LocalTime.now();
//
//				long todayLeftminutes = 0;
//				if(localTimeWorkingStartTime.isBefore(currentTime) && currentTime.isBefore(localTimeWorkingEndTime)) {
//					todayLeftminutes = ChronoUnit.MINUTES.between(currentTime, localTimeWorkingEndTime);
//				}        
//				else if(currentTime.isBefore(localTimeWorkingStartTime)) {
//					todayLeftminutes = minutes;
//				}
//				long leftMin = slaInMin;
//
//				int counter=0;
//
//				while(leftMin>0) {
//					if(counter==0)
//						leftMin = leftMin-todayLeftminutes;
//					else
//						leftMin = leftMin-minutes;
//					counter++;
//				}
//				LocalTime deliveryTime = localTimeWorkingEndTime.plusMinutes(leftMin);
//
//				LocalDate tempDate = LocalDate.now().plusDays(counter-1);
//				LocalDateTime tempDeliveryDate = LocalDateTime.of(tempDate, deliveryTime);
//
//				int sequence = 1;
//				while(diCount>0) {
//					int count = 0;
//					int pendingCount = orderCountDao.findOrderByExpectedDeliveryDate(user,setSelectedDeliveryDate(tempDate.toString()));
//					if(pendingCount>0) {
//						if(pendingCount<maxTruckCount) {
//							int diff = maxTruckCount-pendingCount;
//							if(diCount<diff)
//								count = diCount;
//							else
//								count = diff;
//							diCount -=diff;
//						}
//					}
//					else {
//						if(diCount<maxTruckCount) {
//							count = diCount;
//						}
//						else {
//							count = maxTruckCount;
//						}
//						diCount -=maxTruckCount;
//					}
//
//					for(int i=1;i<=count;i++) {
//						DeliveryDateAndSlotData data = new DeliveryDateAndSlotData();
//						data.setDeliveryDate(tempDeliveryDate.toString());
//						data.setDeliverySlot(getPossibleSlot(tempDeliveryDate));
//						data.setOrder(sequence);
//						if(tempQty>truckCapacity)
//							data.setQuantity(truckCapacity);
//						else
//							data.setQuantity((double)tempQty);
//						tempQty -= truckCapacity;
//						dataList.add(data);
//						sequence++;
//					}
//					tempDate =  tempDate.plusDays(1);
//					tempDeliveryDate = LocalDateTime.of(tempDate,localTimeWorkingStartTime);
//				}
//			}
//			list.setDeliveryDateAndSlots(dataList);
//		}
		return list;
	}
	
	@Override
	public DeliveryDateAndSlotListData getOptimalDeliveryWindow(final double orderQuantity,final String routeId, B2BCustomerModel user, final LocalDateTime  orderPunchedDate, final String sourceCode, String isDealerProvidingTruck){
		DeliveryDateAndSlotListData list = new DeliveryDateAndSlotListData();
		List<DeliveryDateAndSlotData> dataList = new ArrayList<DeliveryDateAndSlotData>();
		final SalesOrderDeliverySLAModel salesOrderDeliverySLA = eydmsSalesOrderDeliverySLADao.findByRoute(routeId);
		if(salesOrderDeliverySLA!=null) {

			WarehouseModel sourceMaster =  warehouseService.getWarehouseForCode(sourceCode);
			/////////////////////////////////////////			
			if(sourceMaster!=null) {
				String sourceType = sourceMaster.getType().getCode();
				if(sourceType.equals("PLANT") || (sourceType.equals("DEPOT") && sourceMaster.getWorkingHourStartTime()!=null && sourceMaster.getWorkingHourEndTime()!=null)) {
					int diCount = 0;
					if(salesOrderDeliverySLA.getCommonTruckCapacity()!=null)
						diCount = getDICountForOrder(salesOrderDeliverySLA.getCommonTruckCapacity(),orderQuantity);
					int maxTruckCount = 1000;

					LocalDateTime orderDateTime = orderPunchedDate;

					double deliverySla=salesOrderDeliverySLA.getDeliverySlaHour();
					double dispatchSla=salesOrderDeliverySLA.getDispatchSlaHour();

					String noStart1 =salesOrderDeliverySLA.getNoEntryRestrictionStart1();
					String noEnd1 = salesOrderDeliverySLA.getNoEntryRestrictionEnd1();
					String noStart2 = salesOrderDeliverySLA.getNoEntryRestrictionStart2();
					String noEnd2 = salesOrderDeliverySLA.getNoEntryRestrictionEnd2();
					String noStart3 = salesOrderDeliverySLA.getNoEntryRestrictionStart3();
					String noEnd3 = salesOrderDeliverySLA.getNoEntryRestrictionEnd3();
					String noStart4 = salesOrderDeliverySLA.getNoEntryRestrictionStart4();
					String noEnd4 = salesOrderDeliverySLA.getNoEntryRestrictionEnd4();			

					LocalTime localTimeNoStart1=null, localTimeNoEnd1=null, localTimeNoStart2=null, localTimeNoEnd2=null, localTimeNoStart3=null, localTimeNoEnd3=null, localTimeNoStart4=null, localTimeNoEnd4=null;

					if(noStart1!=null && !noStart1.isBlank())
						localTimeNoStart1 = LocalTime.parse(noStart1);
					if(noEnd1!=null && !noEnd1.isBlank())
						localTimeNoEnd1 = LocalTime.parse(noEnd1);
					if(noStart2!=null && !noStart2.isBlank())
						localTimeNoStart2 = LocalTime.parse(noStart2);
					if(noEnd2!=null && !noEnd2.isBlank())
						localTimeNoEnd2 = LocalTime.parse(noEnd2);
					if(noStart3!=null && !noStart3.isBlank())
						localTimeNoStart3 = LocalTime.parse(noStart3);
					if(noEnd3!=null && !noEnd1.isBlank())
						localTimeNoEnd3 = LocalTime.parse(noEnd3);
					if(noStart4!=null && !noStart4.isBlank())	
						localTimeNoStart4 = LocalTime.parse(noStart4);
					if(noEnd4!=null && !noEnd4.isBlank())
						localTimeNoEnd4 = LocalTime.parse(noEnd4);



					long slaInSeconds = (long) ((deliverySla+dispatchSla)*60*60);
					if(sourceType.equals("DEPOT") && sourceMaster.getWorkingHourStartTime()!=null && sourceMaster.getWorkingHourEndTime()!=null) {
						String workingHourStartTime = sourceMaster.getWorkingHourStartTime();
						String workingHourEndTime = sourceMaster.getWorkingHourEndTime();

						LocalTime localTimeWorkingStartTime = LocalTime.parse(workingHourStartTime);
						LocalTime localTimeWorkingEndTime = LocalTime.parse(workingHourEndTime);	

						LocalDateTime workingStartDate;
						LocalDateTime workingEndDate;
						if(localTimeWorkingStartTime.isAfter(localTimeWorkingEndTime)) {
							workingStartDate = LocalDateTime.of(orderDateTime.toLocalDate(), localTimeWorkingStartTime);
							workingEndDate = LocalDateTime.of(orderDateTime.toLocalDate().plusDays(1), localTimeWorkingEndTime);
						}
						else {
							workingStartDate = LocalDateTime.of(orderDateTime.toLocalDate(), localTimeWorkingStartTime);
							workingEndDate = LocalDateTime.of(orderDateTime.toLocalDate(), localTimeWorkingEndTime);
						}
						LocalDateTime orderDispatchEndDate= orderDateTime.plusSeconds((long) (dispatchSla*60*60));
						if(orderDispatchEndDate.isAfter(workingEndDate)) {
							orderDateTime = workingStartDate.plusDays(1);
						}
					}

					LocalDateTime orderWithSla = orderDateTime.plusSeconds(slaInSeconds);
					LocalDate orderDate =orderWithSla.toLocalDate();

					LocalDateTime tempPromisedDate = orderWithSla;

					if(localTimeNoStart1!=null && localTimeNoEnd1!=null && localTimeNoStart1.compareTo(localTimeNoEnd1)!=0) {
						LocalDateTime tempNoStart1Date = LocalDateTime.of(orderDate,localTimeNoStart1);
						LocalDateTime tempNoEnd1Date = LocalDateTime.of(orderDate,localTimeNoEnd1);

						if(localTimeNoStart1.isAfter(localTimeNoEnd1)) {
							tempNoEnd1Date = LocalDateTime.of(orderDate,localTimeNoEnd1);
							if(orderWithSla.toLocalTime().isAfter(localTimeNoEnd1) && orderWithSla.toLocalTime().isAfter(localTimeNoStart1)) {	
								tempNoEnd1Date = LocalDateTime.of(orderDate.plusDays(1),localTimeNoEnd1);
							}
							else if(orderWithSla.isBefore(tempNoStart1Date) && orderWithSla.isBefore(tempNoEnd1Date) ){
								tempNoStart1Date = LocalDateTime.of(orderDate.minusDays(1),localTimeNoStart1);
							}
						}
						if((orderWithSla.isEqual(tempNoStart1Date) || orderWithSla.isAfter(tempNoStart1Date)) && orderWithSla.isBefore(tempNoEnd1Date)){
							tempPromisedDate = tempNoEnd1Date;
						}
					}

					if(localTimeNoStart2!=null && localTimeNoEnd2!=null && localTimeNoStart2.compareTo(localTimeNoEnd2)!=0) {
						LocalDateTime tempNoStart2Date = LocalDateTime.of(orderDate,localTimeNoStart2);
						LocalDateTime tempNoEnd2Date = LocalDateTime.of(orderDate,localTimeNoEnd2);

						if(localTimeNoStart2.isAfter(localTimeNoEnd2)) {
							if(orderWithSla.toLocalTime().isAfter(localTimeNoEnd2) && orderWithSla.toLocalTime().isAfter(localTimeNoStart2)) {	
								tempNoEnd2Date = LocalDateTime.of(orderDate.plusDays(1),localTimeNoEnd2);
							}
							else if(orderWithSla.isBefore(tempNoStart2Date) && orderWithSla.isBefore(tempNoEnd2Date) ){
								tempNoStart2Date = LocalDateTime.of(orderDate.minusDays(1),localTimeNoStart2);
							}
						}

						if((orderWithSla.isEqual(tempNoStart2Date) || orderWithSla.isAfter(tempNoStart2Date)) && orderWithSla.isBefore(tempNoEnd2Date)){
							if(tempNoEnd2Date.isAfter(tempPromisedDate)) {
								tempPromisedDate = tempNoEnd2Date;
							}				
						}
					}

					if(localTimeNoStart3!=null && localTimeNoEnd3!=null && localTimeNoStart3.compareTo(localTimeNoEnd3)!=0) {
						LocalDateTime tempNoStart3Date = LocalDateTime.of(orderDate,localTimeNoStart3);
						LocalDateTime tempNoEnd3Date = LocalDateTime.of(orderDate,localTimeNoEnd3);

						if(localTimeNoStart3.isAfter(localTimeNoEnd3)) {
							if(orderWithSla.toLocalTime().isAfter(localTimeNoEnd3) && orderWithSla.toLocalTime().isAfter(localTimeNoStart3)) {	
								tempNoEnd3Date = LocalDateTime.of(orderDate.plusDays(1),localTimeNoEnd3);
							}
							else if(orderWithSla.isBefore(tempNoStart3Date) && orderWithSla.isBefore(tempNoEnd3Date) ){
								tempNoStart3Date = LocalDateTime.of(orderDate.minusDays(1),localTimeNoStart3);
							}
						}

						if((orderWithSla.isEqual(tempNoStart3Date) || orderWithSla.isAfter(tempNoStart3Date)) && orderWithSla.isBefore(tempNoEnd3Date)){
							if(tempNoEnd3Date.isAfter(tempPromisedDate)) {
								tempPromisedDate = tempNoEnd3Date;
							}
						}
					}
					if(localTimeNoStart4!=null && localTimeNoEnd4!=null && localTimeNoStart4.compareTo(localTimeNoEnd4)!=0) {
						LocalDateTime tempNoStart4Date = LocalDateTime.of(orderDate,localTimeNoStart4);
						LocalDateTime tempNoEnd4Date = LocalDateTime.of(orderDate,localTimeNoEnd4);

						if(localTimeNoStart4.isAfter(localTimeNoEnd4)) {
							if(orderWithSla.toLocalTime().isAfter(localTimeNoEnd4) && orderWithSla.toLocalTime().isAfter(localTimeNoStart4)) {	
								tempNoEnd4Date = LocalDateTime.of(orderDate.plusDays(1),localTimeNoEnd4);
							}
							else if(orderWithSla.isBefore(tempNoStart4Date) && orderWithSla.isBefore(tempNoEnd4Date) ){
								tempNoStart4Date = LocalDateTime.of(orderDate.minusDays(1),localTimeNoStart4);
							}
						}

						if((orderWithSla.isEqual(tempNoStart4Date) || orderWithSla.isAfter(tempNoStart4Date)) && orderWithSla.isBefore(tempNoEnd4Date)){
							if(tempNoEnd4Date.isAfter(tempPromisedDate)) {
								tempPromisedDate = tempNoEnd4Date;
							}
						}
					}

					LocalDate tempDate = tempPromisedDate.toLocalDate();
					LocalDateTime tempDeliveryDate = tempPromisedDate;
					
					List<DeliverySlotMasterModel> slotList = deliverySlotMasterDao.findAll();
					slotList = slotList.stream().sorted(Comparator.comparing(DeliverySlotMasterModel::getSequence)).collect(Collectors.toList());

					if((isDealerProvidingTruck!=null && isDealerProvidingTruck.equals("true")) || salesOrderDeliverySLA.getCommonTruckCapacity()==null ||salesOrderDeliverySLA.getCommonTruckCapacity()==0) {

						DeliveryDateAndSlotData data = new DeliveryDateAndSlotData();
						data.setDeliveryDate(getPossibleDay(tempDeliveryDate, slotList).toString());
						data.setDeliverySlot(getPossibleSlot(tempDeliveryDate, slotList));
						data.setOrder(1);
						data.setQuantity(orderQuantity);
						data.setMaxTruckPerDay(1000);
						dataList.add(data);
						list.setDeliveryDateAndSlots(dataList);
						return list;
					}
					//////////////////////////////////////////////////////////////////////			

					double tempQty = orderQuantity;
					double truckCapacity =salesOrderDeliverySLA.getCommonTruckCapacity();


					int sequence = 1;
					while(diCount>0) {
						int count = 0;
						int pendingCount = 0;//orderCountDao.findOrderByExpectedDeliveryDate(user,setSelectedDeliveryDate(tempDate.toString()), routeId);
						if(pendingCount>0) {
							if(pendingCount<maxTruckCount) {
								int diff = maxTruckCount-pendingCount;
								if(diCount<diff)
									count = diCount;
								else
									count = diff;
								diCount -=diff;
							}
						}
						else {
							if(diCount<maxTruckCount) {
								count = diCount;
							}
							else {
								count = maxTruckCount;
							}
							diCount -=maxTruckCount;
						}

						for(int i=1;i<=count;i++) {
							DeliveryDateAndSlotData data = new DeliveryDateAndSlotData();
							data.setDeliveryDate(getPossibleDay(tempDeliveryDate, slotList).toString());
							data.setDeliverySlot(getPossibleSlot(tempDeliveryDate, slotList));
							data.setOrder(sequence);
							data.setTruckCapcity(truckCapacity);
							if(tempQty>truckCapacity)
								data.setQuantity(truckCapacity);
							else
								data.setQuantity((double)tempQty);
							tempQty -= truckCapacity;
							dataList.add(data);
							sequence++;
						}
						tempDate =  tempDate.plusDays(1);
						tempDeliveryDate = LocalDateTime.of(tempDate,LocalTime.parse("07:00"));

					}

					list.setDeliveryDateAndSlots(dataList);
				}
			}
		}
		return list;
	}
	
	@Override
	public DeliveryDateAndSlotListData getOptimalISODeliveryWindow(final double orderQuantity,final String routeId, B2BCustomerModel user, final LocalDateTime  orderPunchedDate, final String sourceCode, final String depotCode){
		DeliveryDateAndSlotListData list = new DeliveryDateAndSlotListData();
		List<DeliveryDateAndSlotData> dataList = new ArrayList<DeliveryDateAndSlotData>();
		final ISODeliverySLAModel salesOrderDeliverySLA = eydmsISODeliverySLADao.findByRoute(routeId);
		if(salesOrderDeliverySLA!=null) {

			WarehouseModel sourceMaster =  warehouseService.getWarehouseForCode(sourceCode);

			/////////////////////////////////////////			
			if(sourceMaster!=null) {
				String sourceType = sourceMaster.getType().getCode();
				if(sourceType.equals("PLANT") || (sourceType.equals("DEPOT") && sourceMaster.getWorkingHourStartTime()!=null && sourceMaster.getWorkingHourEndTime()!=null)) {
					LocalDateTime orderDateTime = orderPunchedDate;
					double deliverySla=salesOrderDeliverySLA.getDeliverySLAHour();
					double dispatchSla=salesOrderDeliverySLA.getDispatchSlaHour();

					String noStart1 =salesOrderDeliverySLA.getNoEntryRestrictionStart1();
					String noEnd1 = salesOrderDeliverySLA.getNoEntryRestrictionEnd1();
					String noStart2 = salesOrderDeliverySLA.getNoEntryRestrictionStart2();
					String noEnd2 = salesOrderDeliverySLA.getNoEntryRestrictionEnd2();
					String noStart3 = salesOrderDeliverySLA.getNoEntryRestrictionStart3();
					String noEnd3 = salesOrderDeliverySLA.getNoEntryRestrictionEnd3();
					String noStart4 = salesOrderDeliverySLA.getNoEntryRestrictionStart4();
					String noEnd4 = salesOrderDeliverySLA.getNoEntryRestrictionEnd4();			

					LocalTime localTimeNoStart1=null, localTimeNoEnd1=null, localTimeNoStart2=null, localTimeNoEnd2=null, localTimeNoStart3=null, localTimeNoEnd3=null, localTimeNoStart4=null, localTimeNoEnd4=null;

					if(noStart1!=null && !noStart1.isBlank())
						localTimeNoStart1 = LocalTime.parse(noStart1);
					if(noEnd1!=null && !noEnd1.isBlank())
						localTimeNoEnd1 = LocalTime.parse(noEnd1);
					if(noStart2!=null && !noStart2.isBlank())
						localTimeNoStart2 = LocalTime.parse(noStart2);
					if(noEnd2!=null && !noEnd2.isBlank())
						localTimeNoEnd2 = LocalTime.parse(noEnd2);
					if(noStart3!=null && !noStart3.isBlank())
						localTimeNoStart3 = LocalTime.parse(noStart3);
					if(noEnd3!=null && !noEnd1.isBlank())
						localTimeNoEnd3 = LocalTime.parse(noEnd3);
					if(noStart4!=null && !noStart4.isBlank())	
						localTimeNoStart4 = LocalTime.parse(noStart4);
					if(noEnd4!=null && !noEnd4.isBlank())
						localTimeNoEnd4 = LocalTime.parse(noEnd4);



					long slaInSeconds = (long) ((deliverySla+dispatchSla)*60*60);
					if(sourceType.equals("DEPOT")) {
						String workingHourStartTime = sourceMaster.getWorkingHourStartTime();
						String workingHourEndTime = sourceMaster.getWorkingHourEndTime();

						LocalTime localTimeWorkingStartTime = LocalTime.parse(workingHourStartTime);
						LocalTime localTimeWorkingEndTime = LocalTime.parse(workingHourEndTime);	

						LocalDateTime workingStartDate;
						LocalDateTime workingEndDate;
						if(localTimeWorkingStartTime.isAfter(localTimeWorkingEndTime)) {
							workingStartDate = LocalDateTime.of(orderDateTime.toLocalDate(), localTimeWorkingStartTime);
							workingEndDate = LocalDateTime.of(orderDateTime.toLocalDate().plusDays(1), localTimeWorkingEndTime);
						}
						else {
							workingStartDate = LocalDateTime.of(orderDateTime.toLocalDate(), localTimeWorkingStartTime);
							workingEndDate = LocalDateTime.of(orderDateTime.toLocalDate(), localTimeWorkingEndTime);
						}
						LocalDateTime orderDispatchEndDate= orderDateTime.plusSeconds((long) (dispatchSla*60*60));
						if(orderDispatchEndDate.isAfter(workingEndDate)) {
							orderDateTime = workingStartDate.plusDays(1);
						}
					}

					LocalDateTime orderWithSla = orderDateTime.plusSeconds(slaInSeconds);
					LocalDate orderDate =orderWithSla.toLocalDate();

					LocalDateTime tempPromisedDate = orderWithSla;

					if(localTimeNoStart1!=null && localTimeNoEnd1!=null && localTimeNoStart1.compareTo(localTimeNoEnd1)!=0) {
						LocalDateTime tempNoStart1Date = LocalDateTime.of(orderDate,localTimeNoStart1);
						LocalDateTime tempNoEnd1Date = LocalDateTime.of(orderDate,localTimeNoEnd1);

						if(localTimeNoStart1.isAfter(localTimeNoEnd1)) {
							tempNoEnd1Date = LocalDateTime.of(orderDate,localTimeNoEnd1);
							if(orderWithSla.toLocalTime().isAfter(localTimeNoEnd1) && orderWithSla.toLocalTime().isAfter(localTimeNoStart1)) {	
								tempNoEnd1Date = LocalDateTime.of(orderDate.plusDays(1),localTimeNoEnd1);
							}
							else if(orderWithSla.isBefore(tempNoStart1Date) && orderWithSla.isBefore(tempNoEnd1Date) ){
								tempNoStart1Date = LocalDateTime.of(orderDate.minusDays(1),localTimeNoStart1);
							}
						}
						if((orderWithSla.isEqual(tempNoStart1Date) || orderWithSla.isAfter(tempNoStart1Date)) && orderWithSla.isBefore(tempNoEnd1Date)){
							tempPromisedDate = tempNoEnd1Date;
						}
					}

					if(localTimeNoStart2!=null && localTimeNoEnd2!=null && localTimeNoStart2.compareTo(localTimeNoEnd2)!=0) {
						LocalDateTime tempNoStart2Date = LocalDateTime.of(orderDate,localTimeNoStart2);
						LocalDateTime tempNoEnd2Date = LocalDateTime.of(orderDate,localTimeNoEnd2);

						if(localTimeNoStart2.isAfter(localTimeNoEnd2)) {
							if(orderWithSla.toLocalTime().isAfter(localTimeNoEnd2) && orderWithSla.toLocalTime().isAfter(localTimeNoStart2)) {	
								tempNoEnd2Date = LocalDateTime.of(orderDate.plusDays(1),localTimeNoEnd2);
							}
							else if(orderWithSla.isBefore(tempNoStart2Date) && orderWithSla.isBefore(tempNoEnd2Date) ){
								tempNoStart2Date = LocalDateTime.of(orderDate.minusDays(1),localTimeNoStart2);
							}
						}

						if((orderWithSla.isEqual(tempNoStart2Date) || orderWithSla.isAfter(tempNoStart2Date)) && orderWithSla.isBefore(tempNoEnd2Date)){
							if(tempNoEnd2Date.isAfter(tempPromisedDate)) {
								tempPromisedDate = tempNoEnd2Date;
							}				
						}
					}

					if(localTimeNoStart3!=null && localTimeNoEnd3!=null && localTimeNoStart3.compareTo(localTimeNoEnd3)!=0) {
						LocalDateTime tempNoStart3Date = LocalDateTime.of(orderDate,localTimeNoStart3);
						LocalDateTime tempNoEnd3Date = LocalDateTime.of(orderDate,localTimeNoEnd3);

						if(localTimeNoStart3.isAfter(localTimeNoEnd3)) {
							if(orderWithSla.toLocalTime().isAfter(localTimeNoEnd3) && orderWithSla.toLocalTime().isAfter(localTimeNoStart3)) {	
								tempNoEnd3Date = LocalDateTime.of(orderDate.plusDays(1),localTimeNoEnd3);
							}
							else if(orderWithSla.isBefore(tempNoStart3Date) && orderWithSla.isBefore(tempNoEnd3Date) ){
								tempNoStart3Date = LocalDateTime.of(orderDate.minusDays(1),localTimeNoStart3);
							}
						}

						if((orderWithSla.isEqual(tempNoStart3Date) || orderWithSla.isAfter(tempNoStart3Date)) && orderWithSla.isBefore(tempNoEnd3Date)){
							if(tempNoEnd3Date.isAfter(tempPromisedDate)) {
								tempPromisedDate = tempNoEnd3Date;
							}
						}
					}
					if(localTimeNoStart4!=null && localTimeNoEnd4!=null && localTimeNoStart4.compareTo(localTimeNoEnd4)!=0) {
						LocalDateTime tempNoStart4Date = LocalDateTime.of(orderDate,localTimeNoStart4);
						LocalDateTime tempNoEnd4Date = LocalDateTime.of(orderDate,localTimeNoEnd4);

						if(localTimeNoStart4.isAfter(localTimeNoEnd4)) {
							if(orderWithSla.toLocalTime().isAfter(localTimeNoEnd4) && orderWithSla.toLocalTime().isAfter(localTimeNoStart4)) {	
								tempNoEnd4Date = LocalDateTime.of(orderDate.plusDays(1),localTimeNoEnd4);
							}
							else if(orderWithSla.isBefore(tempNoStart4Date) && orderWithSla.isBefore(tempNoEnd4Date) ){
								tempNoStart4Date = LocalDateTime.of(orderDate.minusDays(1),localTimeNoStart4);
							}
						}

						if((orderWithSla.isEqual(tempNoStart4Date) || orderWithSla.isAfter(tempNoStart4Date)) && orderWithSla.isBefore(tempNoEnd4Date)){
							if(tempNoEnd4Date.isAfter(tempPromisedDate)) {
								tempPromisedDate = tempNoEnd4Date;
							}
						}
					}

					LocalDate tempDate = tempPromisedDate.toLocalDate();
					LocalDateTime tempDeliveryDate = tempPromisedDate;

					List<DeliverySlotMasterModel> slotList = deliverySlotMasterDao.findAll();
					slotList = slotList.stream().sorted(Comparator.comparing(DeliverySlotMasterModel::getSequence)).collect(Collectors.toList());

					DeliveryDateAndSlotData data = new DeliveryDateAndSlotData();
					data.setDeliveryDate(getPossibleDay(tempDeliveryDate, slotList).toString());
					data.setDeliverySlot(getPossibleSlot(tempDeliveryDate, slotList));
					data.setOrder(1);
					data.setQuantity(orderQuantity);
					dataList.add(data);
					list.setDeliveryDateAndSlots(dataList);
				}
			}
		}
		return list;
	}
		

	private int getDICountForOrder(final double truckCapacity , double orderQty) {
		if(truckCapacity>0) {
			return (int) Math.ceil(orderQty/(double)truckCapacity);
		}
		return 0;
	}	
	private String getPossibleSlot(LocalDateTime tempDeliveryDate, List<DeliverySlotMasterModel> list) {
		String morningSlot = DeliverySlots.SEVENTOELEVEN.getCode();
		for(DeliverySlotMasterModel slot: list) {
			LocalTime startTime = LocalTime.parse(slot.getStart()).minusSeconds(1);
			LocalTime endTime = LocalTime.parse(slot.getEnd());
			LocalTime deliveryTime = tempDeliveryDate.toLocalTime();
			if(deliveryTime.isAfter(startTime) && deliveryTime.isBefore(endTime)) {
				morningSlot = slot.getSlot().getCode();
			}
		}
		return morningSlot;
	}
	
	private LocalDateTime getPossibleDay(LocalDateTime tempDeliveryDate , List<DeliverySlotMasterModel> list) {
		LocalDateTime possibleDate = tempDeliveryDate;
		if(list!=null && !list.isEmpty()) {
			DeliverySlotMasterModel lastSlot= list.get(list.size()-1);
			LocalDateTime endTime = LocalDateTime.of(tempDeliveryDate.toLocalDate(),LocalTime.parse(lastSlot.getEnd()));
			if(tempDeliveryDate.isEqual(endTime) || tempDeliveryDate.isAfter(endTime)) {
				DeliverySlotMasterModel firstSlot= list.get(0);
				possibleDate = LocalDateTime.of(tempDeliveryDate.plusDays(1).toLocalDate(),LocalTime.parse(firstSlot.getStart()));
			}
		}
		return possibleDate;
	}

    private Date setSelectedDeliveryDate(String selectedDeliveryDate) {
		Date date = null;
		try {
			date = new SimpleDateFormat("yyyy-MM-dd").parse(selectedDeliveryDate);
		} catch (ParseException e) {
			throw new IllegalArgumentException(String.format("Please provide valid date %s", selectedDeliveryDate));
		} 
		return date;
	}

	@Override
	public  OrderModel createOrderSnapshot(final String code){

		OrderModel processedOrder = b2BOrderService.getOrderForCode(code);

		if (processedOrder == null) {
			throw new UnknownIdentifierException(
					"Order with code " + code + " not found for current user in current  BaseStore");
		}

		final OrderModel version = orderHistoryService.createHistorySnapshot(processedOrder);
		LOGGER.info("version id " + version.getVersionID());

		final OrderHistoryEntryModel historyEntry = getModelService().create(OrderHistoryEntryModel.class);
		historyEntry.setOrder(processedOrder);
		historyEntry.setPreviousOrderVersion(version);
		historyEntry.setTimestamp(new Date());
		historyEntry.setOwner(getUserService().getCurrentUser());
		historyEntry.setDescription("Creating History Entry For Modified Order");

		orderHistoryService.saveHistorySnapshot(version);
		getModelService().saveAll(version,historyEntry);

		return processedOrder;
	}
	@Override
	public void modifyOrderEntry(OrderModel orderModel , CommerceCartParameter parameter) throws CommerceCartModificationException{
			 eydmsModifyOrderStrategy.modiyOrderEntry(orderModel,parameter);
	}

	@Override
	public void  modifyOrderDetails(OrderModel order , OrderData orderData, Double basePrice) throws CalculationException {
		order.setTotalQuantity(orderData.getTotalQuantity());
		if(StringUtils.isNotBlank(orderData.getOrderSource())){
			WarehouseModel warehouseByCode = eydmsWarehouseDao.findWarehouseByCode(orderData.getOrderSource());
			order.setWarehouse(warehouseByCode);
		}


		if(StringUtils.isNotBlank(orderData.getErpCityCode())){
			//			List<ERPCityModel> erpCityByCode = erpCityDao.findERPCityByCode(orderData.getErpCityCode());
			//			if(CollectionUtils.isNotEmpty(erpCityByCode) && erpCityByCode.size()>0){
			//				AddressModel orderAddress = order.getDeliveryAddress();
			//				orderAddress.setErpCity(orderData.getErpCityCode());
			//				getModelService().save(orderAddress);
			//			}
			AddressModel orderAddress = order.getDeliveryAddress();
			orderAddress.setErpCity(orderData.getErpCityCode());

		}

		order.setStatus(OrderStatus.ORDER_MODIFIED);
		order.setModificationReason(orderData.getModificationReason());
		order.setOrderModifiedDate(new Date());
		order.setOrderModifiedBy((B2BCustomerModel) getUserService().getCurrentUser());
		if(orderData.getRequestedDeliverySlot()!=null)
			order.setRequestedDeliveryslot(DeliverySlots.valueOf(orderData.getRequestedDeliverySlot()));
		if(orderData.getRequestedDeliveryDate()!=null)
			order.setRequestedDeliveryDate(setRequestedDeliveryDate(orderData.getRequestedDeliveryDate()));
		getModelService().save(order);
		getModelService().refresh(order);
//		calculationService.calculate(order);

		LOGGER.info("The base price is "+basePrice);

		order.getEntries().forEach(entry -> {
			entry.setBasePrice(basePrice);
			entry.setTotalPrice(basePrice * entry.getQuantityInMT());
			getModelService().save(entry);

		});

		order.setTotalPrice(order.getEntries().stream().collect(Collectors.summingDouble(AbstractOrderEntryModel::getTotalPrice)));

		getModelService().save(order);
		getModelService().refresh(order);
		if(order.getOrderProcess()!=null) {
			Optional<OrderProcessModel> orderProcess = order.getOrderProcess().stream().filter(op -> op instanceof OrderProcessModel).findFirst();
			if(orderProcess.isPresent()){
				OrderProcessModel orderProcessModel = orderProcess.get();
				businessProcessService.triggerEvent(orderProcessModel.getCode()+"_"+ EyDmsCoreConstants.APPROVAL_CONSTANT.ORDER_REVIEW_DECISION_EVENT_NAME);
			}
		}
		try{
			StringBuilder builder = new StringBuilder();
			String modifiedUserName = "";
			if(order.getOrderModifiedBy()!=null){
				modifiedUserName = order.getOrderModifiedBy().getName();
			}
			String source = "";
			if(order.getEntries().get(0).getSource()!=null){
				source = order.getEntries().get(0).getSource().getCode()+" - "+ order.getEntries().get(0).getSource().getName();
			}
			Double orderQty = order.getEntries().stream().collect(Collectors.summingDouble(each -> each.getQuantityInMT()));

			NotificationCategory category = NotificationCategory.ORDER_MODIFIED;
			builder.append("Order no. "+order.getCode() +"has been modified by "+ modifiedUserName +"to "+ order.getEntries().get(0).getProduct().getName() + " with "+ orderQty+ " MT with source ("+source+")");

			String body = builder.toString();

			StringBuilder builder1= new StringBuilder();
			builder1.append("Order Modified");

			String subject = builder1.toString();

			eydmsNotificationService.submitOrderNotification(order, (B2BCustomerModel) order.getUser(),body,subject,category);

			EyDmsUserModel so = territoryManagementService.getSOforCustomer((EyDmsCustomerModel) order.getUser());
			eydmsNotificationService.submitOrderNotification(order,so,body,subject,category);

			EyDmsCustomerModel sp = territoryManagementService.getSpForCustomerAndBrand((EyDmsCustomerModel) order.getUser(),order.getSite());
			eydmsNotificationService.submitOrderNotification(order,sp,body,subject,category);

			List<EyDmsUserModel> tsm = territoryManagementService.getTSMforDistrict(order.getDistrictMaster(), order.getSite());
			if(tsm!=null && tsm.isEmpty()){
				for(EyDmsUserModel TSM : tsm){
					eydmsNotificationService.submitOrderNotification(order,TSM,body,subject,category);
				}
			}
			List<EyDmsUserModel> rh = territoryManagementService.getRHforRegion(order.getRegionMaster(), order.getSite());
			if(rh!=null && rh.isEmpty()){
				for(EyDmsUserModel RH: rh){
					eydmsNotificationService.submitOrderNotification(order,RH,body,subject,category);
				}
			}

		}
		catch (Exception e){
			LOG.error("Error while sending order modified notification");
		}

	}





		/*Collection<AbstractOrderEntryModel> toRemove = Lists.newArrayList();
		Collection<AbstractOrderEntryModel> toSave = Lists.newArrayList();
		if (Objects.nonNull(processedOrder.getEntries()) && !processedOrder.getEntries().isEmpty()) {
			for (AbstractOrderEntryModel entry : processedOrder.getEntries())
			{
				for(final OrderEntryData entryData : orderData.getEntries()){
					if(entry.getEntryNumber().equals(entryData.getEntryNumber())){
						if (entryData.getQuantity() != null && entryData.getQuantity() >= 1L){
							entry.setQuantity(entryData.getQuantity());
							entry.setExpectedDeliveryslot(DeliverySlots.valueOf(entryData.getSelectedDeliverySlot()));
							populateDeliveryDate(entry,entryData.getSelectedDeliveryDate());
							toSave.add(entry);
						}
						else{
							toRemove.add(entry);
						}
						break;
					}
				}

				getModelService().removeAll(toRemove);
				getModelService().saveAll(toSave);
				getModelService().refresh(processedOrder);

			}
		}*/

		/*orderModel.setTotalQuantity(orderData.getTotalQuantity());

		if(null!= orderData.getWarehouse() && StringUtils.isNotBlank(orderData.getWarehouse().getCode())){
			WarehouseModel warehouseByCode = eydmsWarehouseDao.findWarehouseByCode(orderData.getWarehouse().getCode());
			processedOrder.setWarehouse(warehouseByCode);
		}

		if(StringUtils.isNotBlank(orderData.getErpCityCode())){
			List<ERPCityModel> erpCityByCode = erpCityDao.findERPCityByCode(orderData.getErpCityCode());
			if(CollectionUtils.isNotEmpty(erpCityByCode) && erpCityByCode.size()>0){
				processedOrder.getDeliveryAddress().setErpCity(erpCityByCode.get(0));
			}
		}

		processedOrder.setStatus(OrderStatus.ORDER_MODIFIED);

		getModelService().save(processedOrder);
		return true;
	}*/


	private void populateDeliveryDate(AbstractOrderEntryModel entry, String selectedDeliveryDate) {
		if(StringUtils.isNotBlank(selectedDeliveryDate)){
			try{
				Date date = new SimpleDateFormat(EyDmsCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1).parse(selectedDeliveryDate);
				entry.setExpectedDeliveryDate(date);
			}
			catch (ParseException ex){
				LOGGER.error("Date is not in correct format: "+selectedDeliveryDate);
				throw new AmbiguousIdentifierException("Date is not in the correct format: "+EyDmsCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
			}
		}
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

	@Override
	public SearchPageData<EyDmsOrderHistoryData> getOrderHistoryForOrder(SearchPageData searchPageData, String orderStatus, String filter,String productName , String orderType, Boolean isCreditLimitBreached, String spApprovalFilter, Boolean approvalPending) {
		final UserModel currentUser = getUserService().getCurrentUser();
		String statues = validateAndMapOrderStatuses(orderStatus);
		final BaseStoreModel currentBaseStore = baseStoreService.getCurrentBaseStore();
		final Set<OrderStatus> statusSet = extractOrderStatuses(statues);
		final SearchPageData<EyDmsOrderHistoryData> result = new SearchPageData<>();

		if(StringUtils.isBlank(filter) && StringUtils.isBlank(productName) && StringUtils.isBlank(orderType)) {
			SearchPageData<OrderModel> ordersListByStatusForSO = eydmsOrderCountDao.findOrdersListByStatusForSO(currentUser, currentBaseStore, statusSet.toArray(new OrderStatus[statusSet.size()]), searchPageData, isCreditLimitBreached, spApprovalFilter, approvalPending);
			result.setPagination(ordersListByStatusForSO.getPagination());
			result.setSorts(ordersListByStatusForSO.getSorts());
			final List<EyDmsOrderHistoryData> eydmsOrderHistoryData = eydmsOrderHistoryCardConverter.convertAll(ordersListByStatusForSO.getResults());
			result.setResults(eydmsOrderHistoryData);
		}
		else
		{
			OrderType orderTypeEnum = null;
			if(StringUtils.isNotBlank(orderType)){
				orderTypeEnum = OrderType.valueOf(orderType);
			}

			SearchPageData<OrderModel> ordersListByStatusForSO = eydmsOrderCountDao.findOrdersListByStatusForSO(currentUser, currentBaseStore, statusSet.toArray(new OrderStatus[statusSet.size()]), searchPageData, filter,productName,orderTypeEnum,isCreditLimitBreached, spApprovalFilter, approvalPending);
			result.setPagination(ordersListByStatusForSO.getPagination());
			result.setSorts(ordersListByStatusForSO.getSorts());
			final List<EyDmsOrderHistoryData> eydmsOrderHistoryData = eydmsOrderHistoryCardConverter.convertAll(ordersListByStatusForSO.getResults());
			result.setResults(eydmsOrderHistoryData);
		}
		return result;
	}

	@Override
	public SearchPageData<EyDmsOrderHistoryData> getOrderHistoryForOrderEntry(SearchPageData searchPageData, String orderStatus, String filter,String productName , String orderType, String spApprovalFilter) {
		final UserModel currentUser = getUserService().getCurrentUser();
		String statues = validateAndMapOrderStatuses(orderStatus);
		final BaseStoreModel currentBaseStore = baseStoreService.getCurrentBaseStore();
		final Set<OrderStatus> statusSet = extractOrderStatuses(statues);

		final SearchPageData<EyDmsOrderHistoryData> result = new SearchPageData<>();

		if(StringUtils.isBlank(filter) && StringUtils.isBlank(productName) && StringUtils.isBlank(orderType))
		{
			SearchPageData<OrderEntryModel> orderEntriesListByStatusForSO = eydmsOrderCountDao.findOrderEntriesListByStatusForSO(currentUser, currentBaseStore, statusSet.toArray(new OrderStatus[statusSet.size()]), searchPageData, spApprovalFilter);
			result.setPagination(orderEntriesListByStatusForSO.getPagination());
			result.setSorts(orderEntriesListByStatusForSO.getSorts());
			List<EyDmsOrderHistoryData> eydmsOrderHistoryData = eydmsOrderEntryHistoryCardConverter.convertAll(orderEntriesListByStatusForSO.getResults());
			result.setResults(eydmsOrderHistoryData);
		}
		else {
			OrderType orderTypeEnum = null;
			if(StringUtils.isNotBlank(orderType)){
				orderTypeEnum = OrderType.valueOf(orderType);
			}
			SearchPageData<OrderEntryModel> orderEntriesListByStatusForSO = eydmsOrderCountDao.findOrderEntriesListByStatusForSO(currentUser, currentBaseStore, statusSet.toArray(new OrderStatus[statusSet.size()]), searchPageData,filter,productName,orderTypeEnum, spApprovalFilter);
			result.setPagination(orderEntriesListByStatusForSO.getPagination());
			result.setSorts(orderEntriesListByStatusForSO.getSorts());
			List<EyDmsOrderHistoryData> eydmsOrderHistoryData = eydmsOrderEntryHistoryCardConverter.convertAll(orderEntriesListByStatusForSO.getResults());
			result.setResults(eydmsOrderHistoryData);
		}
		return result;
	}


	@Override
	public AbstractOrderEntryModel addNewOrderEntry(final OrderModel order, final ProductModel product,
								 final long qty, final UnitModel unit, final int number){

		validateParameterNotNullStandardMessage("product", product);
		validateParameterNotNullStandardMessage("order", order);

		ComposedTypeModel entryType = getAbstractOrderEntryTypeService().getAbstractOrderEntryType(order);
		validateParameterNotNullStandardMessage("entryType", entryType);

		if (qty <= 0)
		{
			throw new IllegalArgumentException("Quantity must be a positive non-zero value");
		}
		if (number < APPEND_AS_LAST)
		{
			throw new IllegalArgumentException("Number must be greater or equal -1");
		}
		UnitModel usedUnit = unit;
		if (usedUnit == null)
		{
			LOGGER.debug("No unit passed, trying to get product unit");
			usedUnit = product.getUnit();
			validateParameterNotNullStandardMessage("usedUnit", usedUnit);
		}

		AbstractOrderEntryModel ret = null;
		// search for present entries for this product if needed
        /*if (addToPresent)
        {
            for (final AbstractOrderEntryModel e : orderService.getEntriesForProduct(order, product))
            {
                // Ensure that order entry is not a 'give away', and has same units
                if (Boolean.FALSE.equals(e.getGiveAway()) && usedUnit.equals(e.getUnit()))
                {
                    e.setQuantity(Long.valueOf(e.getQuantity().longValue() + qty));
                    ret = e;
                    break;
                }
            }
        }*/

			ret = getAbstractOrderEntryService().createEntry(entryType, order);
			ret.setQuantity(Long.valueOf(qty));
			ret.setProduct(product);
			ret.setUnit(usedUnit);
			addEntryAtPosition(order, ret, number);

		order.setCalculated(Boolean.FALSE);
		return ret;
	}


	public I18NService getI18NService() {
		return i18NService;
	}

	public void setI18NService(I18NService i18NService) {
		this.i18NService = i18NService;
	}
	public BusinessProcessService getBusinessProcessService() {
		return businessProcessService;
	}

	public void setBusinessProcessService(BusinessProcessService businessProcessService) {
		this.businessProcessService = businessProcessService;
	}

	public ConfigurationService getConfigurationService() {
		return configurationService;
	}

	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	public FlexibleSearchService getFlexibleSearchService() {
		return flexibleSearchService;
	}

	public void setFlexibleSearchService(FlexibleSearchService flexibleSearchService) {
		this.flexibleSearchService = flexibleSearchService;
	}

	public EYDMSMastersDao getEyDmsMastersDao() {
		return eydmsMastersDao;
	}

	public void setEyDmsMastersDao(EYDMSMastersDao eydmsMastersDao) {
		this.eydmsMastersDao = eydmsMastersDao;
	}

	public EyDmsCartService getEyDmsCartService() {
		return eydmsCartService;
	}

	public void setEyDmsCartService(EyDmsCartService eydmsCartService) {
		this.eydmsCartService = eydmsCartService;
	}

	@Override
	public List<DeliverySlotMasterModel> getDeliverySlotList() {
		return deliverySlotMasterDao.findAll();
	}

	@Override
	public void submitOrderForCancellation(EyDmsOrderCancelProcessModel eydmsOrderCancelProcessModel) {
		eventService.publishEvent(new EyDmsOrderCancelEvent(eydmsOrderCancelProcessModel));
	}

	@Override
	public void submitOrderLineForCancellation(EyDmsOrderLineCancelProcessModel eydmsOrderCancelLineProcessModel) {
		eventService.publishEvent(new EyDmsOrderLineCancelEvent(eydmsOrderCancelLineProcessModel));
	}

	@Override
	public void updateTotalQuantity(long quantity) {
		final CartModel cart = cartService.getSessionCart();		
		cart.setTotalQuantity((double)quantity);
		getModelService().save(cart);
	}

	@Override
	public SearchPageData<EyDmsOrderHistoryData> getCancelOrderHistoryForOrder(SearchPageData searchPageData, String orderStatus, String filter, String productName, String orderType, String spApprovalFilter, Integer month, Integer year) {
		final UserModel currentUser = getUserService().getCurrentUser();
		String statues = validateAndMapOrderStatuses(orderStatus);
		final BaseStoreModel currentBaseStore = baseStoreService.getCurrentBaseStore();
		final Set<OrderStatus> statusSet = extractOrderStatuses(statues);
		final SearchPageData<EyDmsOrderHistoryData> result = new SearchPageData<>();

		String monthYear = null;
		if(month!=0 && year!=0) {
			int fYear = Integer.parseInt(year.toString());
			int fMonth = Integer.parseInt(month.toString());

			String singleDigitMonth = Integer.toString(fYear) + "-0" + Integer.toString(fMonth) + "-%";
			String doubleDigitMonth = Integer.toString(fYear) + "-" + Integer.toString(fMonth) + "-%";

			monthYear = (fMonth >= 1 && fMonth <= 9) ? singleDigitMonth : doubleDigitMonth;
		}

		if(StringUtils.isBlank(filter) && StringUtils.isBlank(productName) && StringUtils.isBlank(orderType)) {
			SearchPageData<OrderModel> ordersListByStatusForSO = eydmsOrderCountDao.findCancelOrdersListByStatusForSO(currentUser, currentBaseStore, statusSet.toArray(new OrderStatus[statusSet.size()]), searchPageData, spApprovalFilter, monthYear);
			result.setPagination(ordersListByStatusForSO.getPagination());
			result.setSorts(ordersListByStatusForSO.getSorts());
			final List<EyDmsOrderHistoryData> eydmsOrderHistoryData = eydmsOrderHistoryCardConverter.convertAll(ordersListByStatusForSO.getResults());
			result.setResults(eydmsOrderHistoryData);
		}
		else
		{
			OrderType orderTypeEnum = null;
			if(StringUtils.isNotBlank(orderType)){
				orderTypeEnum = OrderType.valueOf(orderType);
			}

			SearchPageData<OrderModel> ordersListByStatusForSO = eydmsOrderCountDao.findCancelOrdersListByStatusForSO(currentUser, currentBaseStore, statusSet.toArray(new OrderStatus[statusSet.size()]), searchPageData, filter,productName,orderTypeEnum, spApprovalFilter, monthYear);
			result.setPagination(ordersListByStatusForSO.getPagination());
			result.setSorts(ordersListByStatusForSO.getSorts());
			final List<EyDmsOrderHistoryData> eydmsOrderHistoryData = eydmsOrderHistoryCardConverter.convertAll(ordersListByStatusForSO.getResults());
			result.setResults(eydmsOrderHistoryData);
		}
		return result;
	}

	@Override
	public SearchPageData<EyDmsOrderHistoryData> getCancelOrderHistoryForOrderEntry(SearchPageData searchPageData, String orderStatus, String filter, String productName, String orderType, String spApprovalFilter, Integer month, Integer year) {
		final UserModel currentUser = getUserService().getCurrentUser();
		String statues = validateAndMapOrderStatuses(orderStatus);
		final BaseStoreModel currentBaseStore = baseStoreService.getCurrentBaseStore();
		final Set<OrderStatus> statusSet = extractOrderStatuses(statues);

		final SearchPageData<EyDmsOrderHistoryData> result = new SearchPageData<>();

		String monthYear = null;
		if(month!=0 && year!=0) {
			int fYear = Integer.parseInt(year.toString());
			int fMonth = Integer.parseInt(month.toString());

			String singleDigitMonth = Integer.toString(fYear) + "-0" + Integer.toString(fMonth) + "-%";
			String doubleDigitMonth = Integer.toString(fYear) + "-" + Integer.toString(fMonth) + "-%";

			monthYear = (fMonth >= 1 && fMonth <= 9) ? singleDigitMonth : doubleDigitMonth;
		}

		if(StringUtils.isBlank(filter) && StringUtils.isBlank(productName) && StringUtils.isBlank(orderType))
		{
			SearchPageData<OrderEntryModel> orderEntriesListByStatusForSO = eydmsOrderCountDao.findCancelOrderEntriesListByStatusForSO(currentUser, currentBaseStore, statusSet.toArray(new OrderStatus[statusSet.size()]), searchPageData, spApprovalFilter, monthYear);
			result.setPagination(orderEntriesListByStatusForSO.getPagination());
			result.setSorts(orderEntriesListByStatusForSO.getSorts());
			List<EyDmsOrderHistoryData> eydmsOrderHistoryData = eydmsOrderEntryHistoryCardConverter.convertAll(orderEntriesListByStatusForSO.getResults());
			result.setResults(eydmsOrderHistoryData);
		}
		else {
			OrderType orderTypeEnum = null;
			if(StringUtils.isNotBlank(orderType)){
				orderTypeEnum = OrderType.valueOf(orderType);
			}
			SearchPageData<OrderEntryModel> orderEntriesListByStatusForSO = eydmsOrderCountDao.findCancelOrderEntriesListByStatusForSO(currentUser, currentBaseStore, statusSet.toArray(new OrderStatus[statusSet.size()]), searchPageData,filter,productName,orderTypeEnum, spApprovalFilter, monthYear);
			result.setPagination(orderEntriesListByStatusForSO.getPagination());
			result.setSorts(orderEntriesListByStatusForSO.getSorts());
			List<EyDmsOrderHistoryData> eydmsOrderHistoryData = eydmsOrderEntryHistoryCardConverter.convertAll(orderEntriesListByStatusForSO.getResults());
			result.setResults(eydmsOrderHistoryData);
		}
		return result;
	}

	private void cancelOrder(OrderModel order) {
		B2BCustomerModel currentUser = (B2BCustomerModel) order.getPlacedBy();
		final StringBuilder builder = new StringBuilder(NOTIFICATION_GREETING);
		if(null!=order) {
			SiteMessageModel notification = getModelService().create(SiteMessageModel.class);
			notification.setNotificationType(NotificationType.NOTIFICATION);
			notification.setSubject(EyDmsCoreConstants.ORDER_NOTIFICATION.ORDER_CANCELLED_NOTIFICATION);
			builder.append(order.getPlacedBy().getUid()).append(ORDER_PLACED_BY);
			builder.append(order.getUser().getUid()).append(ORDER_DATE);
			DateFormat dateFormat = new SimpleDateFormat(EyDmsCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
			String orderDate = dateFormat.format(order.getDate());
			builder.append(orderDate).append(ORDER_QUANTITY);
			builder.append(order.getTotalQuantity().toString()).append(ORDER_CANCELLED);
			notification.setBody(builder.toString());
			SiteMessageForCustomerModel customer = getModelService().create(SiteMessageForCustomerModel.class);
			notification.setOwner(currentUser);
			notification.setEntryNumber(order.getEntries().get(0).getEntryNumber());
			notification.setDealerCode(order.getUser().getUid());
			notification.setOrderCode(order.getCode());
		    notification.setCategory(NotificationCategory.ORDER_CANCELLED_CRM);
			notification.setType(SiteMessageType.SYSTEM);
			notification.setUid(String.valueOf(siteMessageUidGenerator.generate()));
			notification.setExpiryDate(getExpiryDate());
			notification.setOrderStatus(order.getStatus().getCode());
			notification.setOrderType(order.getOrderType().getCode());
			customer.setMessage(notification);
			customer.setSentDate(new Date());
			customer.setCustomer(currentUser);
			getModelService().save(notification);
			getModelService().save(customer);
			LOGGER.info(String.format("Order Cancelled In App notification sent successfully for Order %s", order.getCode()));
		}
		else
		{
			LOGGER.error(String.format("Error occured in Order Cancelled In App notification for User %s", currentUser));
		}
	}

	private Date getExpiryDate(){
		LocalDate date = LocalDate.now().plusDays(30);
		Date expiryDate = null;
		try {
			expiryDate = new SimpleDateFormat("yyyy-MM-dd").parse(String.valueOf(date));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return expiryDate;
	}
	public Boolean getVehicleArrivalConfirmationForOrder(boolean vehicleArrived, String orderCode, String entryNumber){
		OrderModel order = getOrderForCode(orderCode);
		int entryNum = Integer.valueOf(entryNumber);
		OrderEntryModel orderEntry = (OrderEntryModel) order.getEntries().stream().filter(abstractOrderEntryModel -> abstractOrderEntryModel.getEntryNumber()==entryNum).findAny().get();
		Date date = new Date();
		if(vehicleArrived == true){
			orderEntry.setIsVehicleArrived(true);
			orderEntry.setStatus(OrderStatus.EPOD_PENDING);
			orderEntry.setEpodInitiateDate(date);
			orderEntry.setEpodStatus(EpodStatus.PENDING);
			getModelService().save(orderEntry);
		}
		else{
			//TODO: Dependent on other persona
		}
		return true;
	}

	public Boolean updateEpodStatusForOrder(double shortageQuantity, String orderCode, int entryNumber){
		OrderModel order = getOrderForCode(orderCode);
		OrderEntryModel orderEntry = (OrderEntryModel) order.getEntries().stream().filter(abstractOrderEntryModel -> abstractOrderEntryModel.getEntryNumber()==entryNumber).findAny().get();
		orderEntry.setShortageQuantity(shortageQuantity);

		if(shortageQuantity>0){
			orderEntry.setEpodStatus(EpodStatus.DISPUTED);
		}
		else {
			orderEntry.setEpodStatus(EpodStatus.APPROVED);
		}
		orderEntry.setStatus(OrderStatus.DELIVERED);
		orderEntry.setDeliveredDate(new Date());
		orderEntry.setEpodCompleted(true);
		orderEntry.setEpodCompletedDate(new Date());
		getModelService().save(orderEntry);

		saveOrderRequisitionEntryDetails(order, orderEntry, "EPOD");

		return true;
	}

	public Boolean getOrderFromRetailersRequest(String requisitionId, String status){
		OrderRequisitionModel model = orderRequisitionDao.findByRequisitionId(requisitionId);

		if(status!=null && status.equals("SELF_STOCK")){
			model.setStatus(RequisitionStatus.DELIVERED);
			model.setServiceType(ServiceType.SELF_STOCK);
			LOGGER.info("1. Stock allocation on getOrderFromRetailersRequest--> on SELF STOCK-->> qty="
					+ model.getQuantity() + ":::Requisition Id:::" + requisitionId);
			if (null != model
					&& null != model.getProduct() && null != model.getFromCustomer() && null != model.getToCustomer()) {
				LOGGER.info("2. Stock allocation --> Before getting DealerAllocation::: Product:::" + model.getProduct() + ":::Dealer Code:::"
						+ model.getFromCustomer());
				ReceiptAllocaltionModel receiptAllocate = dealerDao.getDealerAllocation(model.getProduct(),model.getFromCustomer());
				RetailerRecAllocateModel receiptRetailerAllocate = dealerDao.getRetailerAllocation(model.getProduct(),model.getToCustomer());
				Double updatedQty = 0.0;
				int stockAvailableForRetailer = 0;
				int stockAvailableForInfluencer = 0;
				Integer orderRequisitionQtyToUpdate = model.getQuantity().intValue();

				int receiptQty = 0;
				if (null!= receiptAllocate && null != receiptAllocate.getReceipt()) {
					receiptQty = receiptAllocate.getReceipt();
				}

				int retailerReceiptQty = 0;
				if (null!=receiptRetailerAllocate && null != receiptRetailerAllocate.getReceipt()) {
					retailerReceiptQty = receiptRetailerAllocate.getReceipt();
				}

				if (null != receiptAllocate) {
					LOGGER.info("3. Stock allocation Record found--- avl qty="
							+ model.getQuantity() + " Available receipt stock-->" + receiptAllocate.getReceipt()
							+ " Available stock for retailer -->" + receiptAllocate.getStockAvlForRetailer()
							+ " Available stock for influencer -->" + receiptAllocate.getStockAvlForInfluencer());

					Integer qtyToUpdate = receiptQty - orderRequisitionQtyToUpdate;
					LOGGER.info("4. Stock allocation Record found--- qtyToUpdate=" + qtyToUpdate);
					if(qtyToUpdate > 0) {
						//receiptAllocate.setReceipt(qtyToUpdate);
						updatedQty = receiptAllocate.getSalesToRetailer() + model.getQuantity();
		    			receiptAllocate.setSalesToRetailer(updatedQty.intValue());
		    			stockAvailableForRetailer = getStockAvailForRetailer(receiptQty,
		    					updatedQty.intValue(), receiptAllocate.getSalesToInfluencer());
		    			stockAvailableForInfluencer = getStockAvailForInfluencer(receiptQty,
		    					receiptAllocate.getSalesToRetailer(), receiptAllocate.getSalesToInfluencer());
		    			receiptAllocate.setStockAvlForInfluencer(stockAvailableForInfluencer);
		    			receiptAllocate.setStockAvlForRetailer(stockAvailableForRetailer);
		    			LOGGER.info("5. Stock allocation Record found--- After updating--"
								+ " Available Receipt stock-->" + receiptQty
								+ " Available stock for retailer (reduce) -->" + receiptAllocate.getStockAvlForRetailer()
								+ " Available stock for influencer -->" + receiptAllocate.getStockAvlForInfluencer());
		    			getModelService().save(receiptAllocate);
					}
				} else {
					LOGGER.info("6. Stock allocation Stock allocation on getOrderFromRetailersRequest--> on SELF STOCK-->> "
							+ " New entry---OrderRequisition for Product or Dealer not found qty= ");
					updatedQty = model.getQuantity();
					receiptQty = (null != updatedQty || 0.0 <= updatedQty)?updatedQty.intValue():0;
					//If product and dealer is not found in the RetailerRecAllocate 
		  			//then it means new entry has to be made as orderrequisition is placed with this combination
					ReceiptAllocaltionModel receiptRetailerAllocateNew = getModelService().create(ReceiptAllocaltionModel.class);
		  			receiptRetailerAllocateNew.setProduct(model.getProduct().getPk().toString());
		  			receiptRetailerAllocateNew.setDealerCode(model.getFromCustomer().getPk().toString());
		  			receiptRetailerAllocateNew.setReceipt(receiptQty);
		  			receiptRetailerAllocateNew.setSalesToInfluencer(0);
		  			receiptRetailerAllocateNew.setSalesToRetailer(0);
		  			stockAvailableForRetailer = getStockAvailForRetailer(receiptQty,
	    					updatedQty.intValue(), receiptAllocate.getSalesToInfluencer());
	    			stockAvailableForInfluencer = getStockAvailForInfluencer(receiptQty,
	    					receiptAllocate.getSalesToRetailer(), receiptAllocate.getSalesToInfluencer());
	    			receiptAllocate.setStockAvlForInfluencer(stockAvailableForInfluencer);
	    			receiptAllocate.setStockAvlForRetailer(stockAvailableForRetailer);
		  			getModelService().save(receiptRetailerAllocateNew);
		  			getModelService().refresh(receiptRetailerAllocateNew);
				}
				LOGGER.info("7. Stock allocation After update stocks values----- on SELF STOCK-->> qty="
						+ model.getQuantity());
				//Update for Retailer and Influencer as well
				if (null != receiptRetailerAllocate) {
		  			LOGGER.info("8. Retailer RECEIPT:::Record found--- Receipts for Retailer when dealer self stock " + receiptRetailerAllocate.getReceipt()
			  			+ " Dealer No -->" + receiptRetailerAllocate.getDealerCode()
			  			+ " Available stock for influencer -->" + receiptRetailerAllocate.getStockAvlForInfluencer()
			  			+ " Available allocated or sales to influencer -->" + receiptRetailerAllocate.getSalesToInfluencer());
		  			updatedQty = retailerReceiptQty + model.getQuantity();
		  			receiptRetailerAllocate.setReceipt((null != updatedQty)?updatedQty.intValue():0);
					if(receiptRetailerAllocate.getSalesToInfluencer()!=null) {
						int stockRetailerToInfluencer = Math.abs((int) ((1.0 * (retailerReceiptQty - receiptRetailerAllocate.getSalesToInfluencer()))));
						receiptRetailerAllocate.setStockAvlForInfluencer(stockRetailerToInfluencer);
					}
		  			LOGGER.info("9. Retailer RECEIPT:::Updated " + retailerReceiptQty
		  			+ " Available stock for influencer -->" + receiptRetailerAllocate.getStockAvlForInfluencer()
		  			+ " Allocated or sales to influencer -->" + receiptRetailerAllocate.getSalesToInfluencer());
		  			modelService.save(receiptRetailerAllocate);
		  		} else {
		  			//If product and dealer is not found in the RetailerRecAllocate 
		  			//then it means new entry has to be made as orderrequisition is placed with this combination
		  			RetailerRecAllocateModel receiptRetailerAllocateNew = modelService.create(RetailerRecAllocateModel.class);
		  			receiptRetailerAllocateNew.setProduct(model.getProduct().getPk().toString());
		  			receiptRetailerAllocateNew.setDealerCode(model.getToCustomer().getPk().toString());
		  			updatedQty = retailerReceiptQty + model.getQuantity();
					retailerReceiptQty = (null != updatedQty)?updatedQty.intValue():0;
		  			receiptRetailerAllocateNew.setReceipt(retailerReceiptQty);
		  			receiptRetailerAllocateNew.setSalesToInfluencer(0);
					if(receiptRetailerAllocateNew.getSalesToInfluencer()!=null) {
						int stockRetailerInfluencer = Math.abs((int) ((1.0 * (retailerReceiptQty - receiptRetailerAllocateNew.getSalesToInfluencer()))));
						receiptRetailerAllocateNew.setStockAvlForInfluencer(stockRetailerInfluencer);
					}
		  			modelService.save(receiptRetailerAllocateNew);
		  			modelService.refresh(receiptRetailerAllocateNew);
		  		}
			}	
			Date date = new Date();
			if(model.getAcceptedDate()==null) {
				model.setAcceptedDate(date);
			}
			model.setFulfilledDate(date);
			model.setDeliveredDate(date);
			orderRequisitionService.orderCountIncrementForDealerRetailerMap(model.getDeliveredDate(),model.getFromCustomer(),model.getToCustomer(), baseSiteService.getCurrentBaseSite());
		} else if (status!=null && status.equals("REJECT_REQUEST")) {
			model.setStatus(RequisitionStatus.REJECTED);
			model.setRejectedDate(new Date());
			model.setRejectedBy((EyDmsCustomerModel) getUserService().getCurrentUser());
		}
		getModelService().save(model);

		return true;
	}

	public SearchPageData<EyDmsOrderHistoryData> getEpodListBasedOnOrderStatus(SearchPageData searchPageData, List<String> Status, String filter) {
		final UserModel currentUser = getUserService().getCurrentUser();
		final BaseStoreModel currentBaseStore = baseStoreService.getCurrentBaseStore();

		final SearchPageData<EyDmsOrderHistoryData> result = new SearchPageData<>();


			SearchPageData<OrderEntryModel> orderEntriesListByStatusForSO = eydmsOrderCountDao.findOrderEntriesListByStatusForEPOD(currentUser, currentBaseStore, Status, searchPageData, filter);
			result.setPagination(orderEntriesListByStatusForSO.getPagination());
			result.setSorts(orderEntriesListByStatusForSO.getSorts());
			List<EyDmsOrderHistoryData> eydmsOrderHistoryData = eydmsOrderEntryHistoryCardConverter.convertAll(orderEntriesListByStatusForSO.getResults());
			result.setResults(eydmsOrderHistoryData);

		return result;
	}

	@Override
	public Boolean getEpodFeedback(EpodFeedbackData epodFeedbackData) {
		Map<String,String> epodFeedback= new HashMap<>();

		String orderCode = epodFeedbackData.getOrderCode();
		int entryNumber = epodFeedbackData.getEntryNumber();

		OrderModel order = getOrderForCode(orderCode);
		OrderEntryModel orderEntry = (OrderEntryModel) order.getEntries().stream().filter(abstractOrderEntryModel -> abstractOrderEntryModel.getEntryNumber()==entryNumber).findAny().get();

		epodFeedback.put("driverRating",epodFeedbackData.getDriverRating());
		epodFeedback.put("deliveryProcess", epodFeedbackData.getDeliveryProcess());
		epodFeedback.put("materialReceipt", epodFeedbackData.getMaterialReceipt());
		epodFeedback.put("serviceLevel",epodFeedbackData.getServiceLevel());
		epodFeedback.put("overallDeliveryExperience", epodFeedbackData.getOverallDeliveryExperience());

		orderEntry.setEpodFeedback(epodFeedback);

		getModelService().save(orderEntry);

		return true;
	}

	@Override
	public void saveOrderRequisitionEntryDetails(OrderModel order, OrderEntryModel orderEntry, String status) {
		if(order.getRequisitions()!=null && !order.getRequisitions().isEmpty()) {
			if(order.getRequisitions().size() == 1) {
				boolean isDeliveredDateNull = false;
				OrderRequisitionModel orderRequisitionModel = order.getRequisitions().get(0);

				OrderRequisitionEntryModel orderRequisitionEntryModel = getModelService().create(OrderRequisitionEntryModel.class);
				orderRequisitionEntryModel.setQuantity(orderEntry.getQuantityInMT() * 20);
				orderRequisitionEntryModel.setEntryNumber(orderEntry.getEntryNumber());
				orderRequisitionEntryModel.setEntry(orderEntry);
				orderRequisitionEntryModel.setOrderRequisition(orderRequisitionModel);
				getModelService().save(orderRequisitionEntryModel);

				if(status.equals("EPOD")) {
					orderRequisitionModel.setReceivedQty(orderRequisitionModel.getReceivedQty() + (orderEntry.getQuantityInMT() * 20));
					if(orderRequisitionModel.getFulfilledDate()==null) {
						orderRequisitionModel.setStatus(RequisitionStatus.PENDING_DELIVERY);
						orderRequisitionModel.setFulfilledDate(new Date());
					}

					for(AbstractOrderEntryModel entry : order.getEntries()) {
						if(entry.getDeliveredDate() == null) {
							isDeliveredDateNull = true;
							break;
						}
					}
					if(!isDeliveredDateNull) {
						orderRequisitionModel.setStatus(RequisitionStatus.DELIVERED);
						orderRequisitionModel.setDeliveredDate(new Date());
						orderRequisitionService.orderCountIncrementForDealerRetailerMap(orderRequisitionModel.getDeliveredDate(),orderRequisitionModel.getFromCustomer(), orderRequisitionModel.getToCustomer(), baseSiteService.getCurrentBaseSite());
						
						EyDmsCustomerModel currentUser = (EyDmsCustomerModel) userService.getCurrentUser();
		                LOGGER.info("1. Retailer RECEIPT::: In DefaultEYDMSB2BOrderService:: Record found--- Requisition Status... " + orderRequisitionModel.getStatus()
		    					+ " Current customer No -->" + currentUser.getCustomerNo());
						if (null != orderRequisitionModel.getFromCustomer() && null != currentUser) {
							updateRetailerReceipts(orderRequisitionModel.getProduct(), orderRequisitionModel.getFromCustomer(), orderRequisitionModel.getReceivedQty());
						}
					}
					else {
						getRequisitionStatusByOrderLines(order);
					}

				}

				getModelService().save(orderRequisitionModel);

			}
		}
	}
	
	//To update the quantity as receipts for allocation calculation
  	private void updateRetailerReceipts(ProductModel productCode, EyDmsCustomerModel dealerCode, Double receivedQuantity) {
  		RetailerRecAllocateModel receiptRetailerAllocate = dealerDao.getRetailerAllocation(productCode, dealerCode);
  		if (null != receiptRetailerAllocate) {
  			LOGGER.info("1. Retailer RECEIPT:::DefaultEYDMSB2BOrderService:: Record found--- Receipts for Dealer " + receiptRetailerAllocate.getReceipt()
  			+ " Dealer No -->" + receiptRetailerAllocate.getDealerCode()
  			+ " Available stock for influencer -->" + receiptRetailerAllocate.getStockAvlForInfluencer()
  			+ " Available allocated or sales to influencer -->" + receiptRetailerAllocate.getSalesToInfluencer());
  			receiptRetailerAllocate.setSalesToInfluencer((new Double(receivedQuantity)).intValue());
			if(receiptRetailerAllocate.getReceipt()!=null && receiptRetailerAllocate.getSalesToInfluencer()!=null) {
				int stockRetailerToInfluencer = Math.abs((int) ((1.0 * (receiptRetailerAllocate.getReceipt() - receiptRetailerAllocate.getSalesToInfluencer()))));
				receiptRetailerAllocate.setStockAvlForInfluencer(stockRetailerToInfluencer);
			}
  			LOGGER.info("2. Retailer RECEIPT:::DefaultEYDMSB2BOrderService:: Updated " + receiptRetailerAllocate.getReceipt()
  			+ " Available stock for influencer -->" + receiptRetailerAllocate.getStockAvlForInfluencer()
  			+ " Allocated or sales to influencer -->" + receiptRetailerAllocate.getSalesToInfluencer());
  			getModelService().save(receiptRetailerAllocate);
  		} else {
  			//If product and dealer is not found in the RetailerRecAllocate 
  			//then it means new entry has to be made as orderrequisition is placed with this combination
  			RetailerRecAllocateModel receiptRetailerAllocateNew = modelService.create(RetailerRecAllocateModel.class);
  			receiptRetailerAllocateNew.setProduct(productCode.getPk().toString());
  			receiptRetailerAllocateNew.setDealerCode(dealerCode.getPk().toString());
  			Double updatedQty = receivedQuantity;
  			receiptRetailerAllocateNew.setReceipt((null != updatedQty)?updatedQty.intValue():0);
  			receiptRetailerAllocateNew.setSalesToInfluencer(0);
			if(receiptRetailerAllocateNew.getReceipt()!=null && receiptRetailerAllocateNew.getSalesToInfluencer()!=null) {
				int stockRetailerInfluencer = Math.abs((int) ((1.0 * (receiptRetailerAllocateNew.getReceipt() - receiptRetailerAllocateNew.getSalesToInfluencer()))));
				receiptRetailerAllocateNew.setStockAvlForInfluencer(stockRetailerInfluencer);
			}
  			getModelService().save(receiptRetailerAllocateNew);
  			getModelService().refresh(receiptRetailerAllocateNew);
  		}
  	}

	@Override
	public boolean updateSpApprovalStatus(String orderCode, String status, String spRejectionReason) {
		OrderModel orderModel = getOrderForCode(orderCode);
		if(orderModel!=null) {
			if(orderModel.getCreditLimitBreached()) {
				B2BCustomerModel currentUser = (B2BCustomerModel) getUserService().getCurrentUser();
				if(status.equals("APPROVED")) {
					if(orderModel.getRejectionReasons()!=null && !(orderModel.getRejectionReasons().isEmpty())) {
						if(orderModel.getRejectionReasons().size()==1) {
							orderModel.setSpApprovalStatus(SPApprovalStatus.APPROVED);
							orderModel.setSpApprovalActionBy(currentUser);
							updateOrderWithPermissionResult(orderModel,null);
						}
						else if(orderModel.getRejectionReasons().size()>1) {
							orderModel.setSpApprovalStatus(SPApprovalStatus.APPROVED);
							orderModel.setSpApprovalActionBy(currentUser);
						}

					}
				}
				else if(status.equals("REJECTED")) {
					orderModel.setSpApprovalStatus(SPApprovalStatus.REJECTED);
					orderModel.setSpApprovalActionBy(currentUser);
					if(spRejectionReason!=null && !spRejectionReason.isEmpty()) {
						orderModel.setSpRejectionReason(spRejectionReason);
					}

					final StringBuilder builder = new StringBuilder(NOTIFICATION_GREETING);

					SiteMessageModel notification = getModelService().create(SiteMessageModel.class);
					notification.setNotificationType(NotificationType.NOTIFICATION);
					notification.setSubject("SP refused credit risk");
					EyDmsUserModel so = territoryManagementService.getSOforCustomer((EyDmsCustomerModel) orderModel.getUser());
					builder.append(so.getUid()).append(", ");
					builder.append(currentUser.getName());
					builder.append(" refused credit risk for the order ").append(orderCode);
					notification.setBody(builder.toString());
					SiteMessageForCustomerModel customer = getModelService().create(SiteMessageForCustomerModel.class);
					notification.setOwner(so);
					customer.setCustomer(so);
					notification.setDealerCode(orderModel.getUser().getUid());
					notification.setOrderCode(orderModel.getCode());
					notification.setCategory(NotificationCategory.REFUSE_CREDIT_RISK);
					notification.setType(SiteMessageType.SYSTEM);
					notification.setUid(String.valueOf(siteMessageUidGenerator.generate()));
					notification.setExpiryDate(getExpiryDate());
					notification.setOrderStatus(orderModel.getStatus().getCode());
					notification.setOrderType(orderModel.getOrderType().getCode());
					customer.setMessage(notification);
					customer.setSentDate(new Date());
					customer.setCustomer(so);
					getModelService().save(notification);
					getModelService().save(customer);
					LOGGER.info(String.format("%s refused credit risk for Order %s", currentUser.getName(), orderModel.getCode()));
				}
				getModelService().save(orderModel);

			}
			else {
				throw new UnsupportedOperationException();
			}
		}

		return true;
	}

	@Override
	public void getRequisitionStatusByOrderLines(OrderModel order) {
		int orderLineCancelledCount = 0;
		int orderLineDeliveredCount = 0;
		Date partialDeliveredDate = null;

		if(order.getRequisitions()!=null && !order.getRequisitions().isEmpty() && order.getRequisitions().size()==1) {
			OrderRequisitionModel orderRequisitionModel = order.getRequisitions().get(0);
			for(AbstractOrderEntryModel entryModel : order.getEntries()) {
				if(entryModel.getCancelledDate()!=null) {
					orderLineCancelledCount += 1;
				}
				else if(entryModel.getDeliveredDate()!=null) {
					orderLineDeliveredCount += 1;
					partialDeliveredDate = entryModel.getDeliveredDate();
				}
			}

			int orderLineCancelledAndDeliveredCount = orderLineDeliveredCount + orderLineCancelledCount;
			if(orderLineCancelledAndDeliveredCount >= order.getEntries().size()) {
				if(orderLineCancelledCount  >= 1 && orderLineDeliveredCount >= 1) {
					orderRequisitionModel.setStatus(RequisitionStatus.PARTIAL_DELIVERED);
					orderRequisitionModel.setPartialDeliveredDate(partialDeliveredDate);
					orderRequisitionModel.setDeliveredDate(partialDeliveredDate);
					orderRequisitionService.orderCountIncrementForDealerRetailerMap(orderRequisitionModel.getDeliveredDate(),orderRequisitionModel.getFromCustomer(), orderRequisitionModel.getToCustomer(), baseSiteService.getCurrentBaseSite());
				}

				if(orderLineCancelledCount == order.getEntries().size()) {
					orderRequisitionModel.setStatus(RequisitionStatus.CANCELLED);
				}
				getModelService().save(orderRequisitionModel);
			}

		}
	}

	 //To get the stock available for Retailer
	private int getStockAvailForRetailer(int receipt, int saleToRetailer, int saleToInfluencer) {
		int stockRetailer = 0;
		stockRetailer = Math.abs(receipt - saleToRetailer - saleToInfluencer);
		return stockRetailer;
	}
    
	//To calculate the stock for Influencer when requisition placed
	private int getStockAvailForInfluencer(int receipt, int saleToRetailer, int saleToInfluencer) {
		int stockInfluencer = 0;
		stockInfluencer = Math.abs((int) ((0.7 * (receipt - saleToRetailer)) - saleToInfluencer));
		return stockInfluencer;
	}

	protected static Date getDateConstraint(LocalDate localDate) {
		ZoneId zone = ZoneId.systemDefault();
		ZonedDateTime dateTime = localDate.atStartOfDay(zone);
		Date date = Date.from(dateTime.toInstant());
		return date;
	}
	
	@Override
	public void approveOrder(OrderModel order, B2BOrderApprovalData b2bOrderApprovalData) {
		B2BCustomerModel user = (B2BCustomerModel) userService.getCurrentUser();
		if(user instanceof EyDmsUserModel) {
			EyDmsWorkflowModel approvalWorkflow = order.getApprovalWorkflow();
			if(approvalWorkflow!=null) {
				EyDmsWorkflowActionModel currentAction = approvalWorkflow.getCurrent();
				if(currentAction!=null) {
					eydmsWorkflowService.updateWorkflowAction(currentAction, user, WorkflowActions.APPROVED, b2bOrderApprovalData.getApprovalComments());
				}
				order.setApprovedLevel(order.getApprovalLevel());
				order.setApprovalLevel(null);
				approvalWorkflow.setCurrent(null);
				getModelService().save(approvalWorkflow);
				getModelService().save(order);
				getModelService().refresh(order);
			}

			OrderProcessModel orderProcessModel;
			Optional<OrderProcessModel> orderProcess = order.getOrderProcess().stream().filter(op -> op instanceof OrderProcessModel).findFirst();
			if(orderProcess.isPresent()){
				orderProcessModel = orderProcess.get();
				getBusinessProcessService().triggerEvent(orderProcessModel.getCode()+"_"+ EyDmsCoreConstants.APPROVAL_CONSTANT.ORDER_REVIEW_DECISION_EVENT_NAME);
			}		
		}
		else {
			throw new UnknownIdentifierException("Access not allowed");
		}
	}

	@Override
	public List<SalesHistoryModel> getNCREntriesExistingInOrderEntry() {
		return eydmsOrderCountDao.getNCREntriesExistingInOrderEntry();
	}

	@Override
	public List<String> getNCREntriesNotExistingInOrderEntry() {
		return eydmsOrderCountDao.getNCREntriesNotExistingInOrderEntry();
	}

	private Date setRequestedDeliveryDate(String requestedDeliveryDate) {
		Date date=null;
		try {
			date = new SimpleDateFormat("yyyy-MM-dd").parse(requestedDeliveryDate);
			return date;
		} catch (ParseException e) {
			LOGGER.error("Error Parsing Requested Delivery Date", e);
			throw new IllegalArgumentException(String.format("Please provide valid date %s", requestedDeliveryDate));
		}
	}
	public static String formatIndianNumber(double number) {
		if (number < 100000) {
			return String.format("%.0f", number);
		} else {
			int quotient = (int) (number / 100000);
			int remainder = (int) (number % 100000);
			return String.format("%d,%02d,%03d", quotient, (remainder / 1000), (remainder % 1000));
		}
	}
}
