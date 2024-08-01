package com.scl.core.order.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

import com.hybris.yprofile.dto.Order;
import com.scl.core.cart.dao.SclERPCityDao;
import com.scl.core.cart.dao.SclISODeliverySLADao;
import com.scl.core.cart.dao.SclSalesOrderDeliverySLADao;
import com.scl.core.cart.dao.SclWarehouseDao;
import com.scl.core.dao.*;
import com.scl.core.enums.*;
import com.scl.core.event.SclOrderCancelEvent;
import com.scl.core.event.SclOrderLineCancelEvent;
import com.scl.core.model.*;
import com.scl.core.notifications.service.SclNotificationService;
import com.scl.core.order.dao.RejectionReasonsDao;
import com.scl.core.order.strategy.SclModifyOrderStrategy;
import com.scl.core.region.dao.ERPCityDao;
import com.scl.core.services.OrderRequisitionService;
import com.scl.core.services.SclWorkflowService;
import com.scl.core.services.TerritoryManagementService;
import com.scl.facades.data.EpodFeedbackData;
import com.scl.facades.order.data.SclOrderHistoryData;
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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.cart.service.SclCartService;
import com.scl.core.constants.SclCoreConstants;
import com.scl.core.order.SCLB2BOrderService;
import com.scl.core.order.dao.SclOrderCountDao;
import com.scl.facades.data.DeliveryDateAndSlotData;
import com.scl.facades.data.DeliveryDateAndSlotListData;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.services.impl.DefaultB2BOrderService;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BOrderApprovalData;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.enums.DeliveryStatus;
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
 *  Interface for SCL B2BOrder Services
 */
public class DefaultSCLB2BOrderService extends DefaultB2BOrderService implements SCLB2BOrderService {

	/**
	 * 
	 */
	private static final Logger LOG = Logger.getLogger(DefaultSCLB2BOrderService.class);
	private static final long serialVersionUID = 1L;
	private I18NService i18NService;
	private BusinessProcessService businessProcessService;
	private ConfigurationService configurationService;
	private FlexibleSearchService flexibleSearchService;

	@Autowired
	SclWorkflowService sclWorkflowService;
	
	private SCLMastersDao sclMastersDao;
	@Autowired
	private SclCartService sclCartService;

	@Autowired
	private SclOrderCountDao orderCountDao;

	@Autowired
	B2BOrderService b2BOrderService;

	@Autowired
	OrderHistoryService orderHistoryService;

	@Autowired
	ERPCityDao erpCityDao;

	@Autowired
	SclWarehouseDao sclWarehouseDao;

	@Autowired
	ProductService productService;

	@Autowired
	BaseStoreService baseStoreService;

	@Autowired
	SclOrderCountDao sclOrderCountDao;
	
	@Resource
	DealerDao dealerDao;
	
	@Autowired
    private SclERPCityDao sclERPCityDao;
	
	@Autowired
	private DeliveryModeDao deliverModeDao;

	@Autowired
	TerritoryManagementService territoryManagementService;

	@Autowired
    private SclSalesOrderDeliverySLADao sclSalesOrderDeliverySLADao;

	@Autowired
    private SclISODeliverySLADao sclISODeliverySLADao;
	
	@Autowired
	Converter<OrderEntryModel, SclOrderHistoryData> sclOrderEntryHistoryCardConverter;

	@Autowired
	Converter<OrderModel, SclOrderHistoryData> sclOrderHistoryCardConverter;

	@Autowired
	Converter<DeliveryItemModel, SclOrderHistoryData> sclDeliveryItemHistoryCardConverter;

	
	@Resource
	private EnumerationService enumerationService;

	@Resource
	private SclModifyOrderStrategy sclModifyOrderStrategy;

	@Resource
	private CalculationService calculationService;
	
    @Autowired
    private WarehouseService warehouseService;
    
    @Autowired
    private DeliverySlotMasterDao deliverySlotMasterDao;

	@Autowired
	private RejectionReasonsDao rejectionReasonsDao;

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
	SclNotificationService sclNotificationService;

	@Autowired
	SCLGenericDao sclGenericDao;

	@Autowired
	DataConstraintDao dataConstraintDao;

	private SCLB2BOrderService sclB2BOrderService;

	public SCLB2BOrderService getSclB2BOrderService() {
		return sclB2BOrderService;
	}

	private static final Logger LOGGER = Logger.getLogger(DefaultSCLB2BOrderService.class);
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
			getBusinessProcessService().triggerEvent(orderProcessModel.getCode()+"_"+ SclCoreConstants.APPROVAL_CONSTANT.ORDER_REVIEW_DECISION_EVENT_NAME);
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

		order.getEntries().forEach(entry->{
			if(entry.getRequisitions()!=null && !entry.getRequisitions().isEmpty() && entry.getRequisitions().size()==1) {
				OrderRequisitionModel orderRequisitionModel = entry.getRequisitions().get(0);
				orderRequisitionModel.setStatus(RequisitionStatus.CANCELLED);
				orderRequisitionModel.setCancelledDate(new Date());
				orderRequisitionModel.setCancelledBy(b2BCustomer);
				orderRequisitionModel.setCancelReason(reason);
				getModelService().save(orderRequisitionModel);
			}
		});


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
			SclOrderCancelProcessModel processModel = new SclOrderCancelProcessModel();
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
			sclNotificationService.submitOrderNotification(order, (B2BCustomerModel) order.getUser(),body,subject,category);

			StringBuilder builder2 = new StringBuilder();
			SclUserModel so = territoryManagementService.getSOforCustomer((SclCustomerModel) order.getUser());
			builder2.append("Order no. " + order.getCode() +" of product "+order.getEntries().get(0).getProduct().getName() +" with order of "+ orderQty  + " MT of ₹"+ formattedAmount );
			builder2.append(" has been successfully cancelled for "+so.getUid());

			String body2 = builder2.toString();
			sclNotificationService.submitOrderNotification(order,so,body2,subject,category);

			StringBuilder builder3 = new StringBuilder();
			SclCustomerModel sp = territoryManagementService.getSpForCustomerAndBrand((SclCustomerModel) order.getUser(),order.getSite());
			builder3.append("Order no. " + order.getCode() +" of product "+order.getEntries().get(0).getProduct().getName() +" with order of "+ orderQty  + " MT of ₹"+ formattedAmount );
			builder3.append(" has been successfully cancelled for "+sp.getUid());

			String body3 = builder3.toString();
			sclNotificationService.submitOrderNotification(order,sp,body3,subject,category);
		}
		catch(Exception e) {
			LOGGER.error("Error while sending cancel order notification");
		}
		return Boolean.TRUE;
	}

	@Override
	public boolean cancelOrderEntry(String orderCode, Integer orderEntryNo, String reason) {

		//SclUserModel sclUser = (SclUserModel) getUserService().getCurrentUser();
		OrderModel order = getOrderForCode(orderCode);
		Optional<AbstractOrderEntryModel> orderEntryOptional = order.getEntries().stream().filter(entry-> orderEntryNo == entry.getEntryNumber()).findFirst();
		String errorMsg;
		if(orderEntryOptional.isPresent()) {
			OrderEntryModel orderEntryModel = (OrderEntryModel) orderEntryOptional.get();
			Double remainingDiQty = orderEntryModel.getRemainingQuantity();
			if(remainingDiQty!=null || remainingDiQty>0)
			{
				B2BCustomerModel b2BCustomer = (B2BCustomerModel) getUserService().getCurrentUser();

				boolean isOrderEntryCancelled = cancelOrderEntryFromCRM(orderEntryModel, reason, b2BCustomer, false);
				if(isOrderEntryCancelled) {
					getRequisitionStatusByOrderLines(orderEntryModel);
					try {

						StringBuilder builder = new StringBuilder();

						builder.append("Order no. " + order.getCode() + "/" + orderEntryModel.getEntryNumber() + " of product "+orderEntryModel.getProduct().getName()+" with order of "+orderEntryModel.getQuantityInMT() + " MT of Rs. "+ orderEntryModel.getTotalPrice() );
						builder.append(" has been successfully cancelled.");

						String body = builder.toString();

						StringBuilder builder1 = new StringBuilder("Order Line is Cancelled");

						String subject = builder1.toString();

						NotificationCategory category = NotificationCategory.ORDER_CANCELLED_CRM;
						sclNotificationService.submitOrderNotification(order, (B2BCustomerModel) order.getUser(),body,subject,category);

						SclUserModel so = territoryManagementService.getSOforCustomer((SclCustomerModel) order.getUser());
						sclNotificationService.submitOrderNotification(order,so,body,subject,category);

						SclCustomerModel sp = territoryManagementService.getSpForCustomerAndBrand((SclCustomerModel) order.getUser(),order.getSite());
						sclNotificationService.submitOrderNotification(order,sp,body,subject,category);
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

		Integer reasonCode =dataConstraintDao.findDaysByConstraintName(SclCoreConstants.ORDER.ORDER_LINE_CANCELLATION_REASON_CODE);
		if(null!=reasonCode) {
			RejectionReasonModel rejectionReasonModel = (RejectionReasonModel) sclGenericDao.findItemByTypeCodeAndUidParam(RejectionReasonModel._TYPECODE, RejectionReasonModel.CODE, String.valueOf(reasonCode));
			if (Objects.nonNull(rejectionReasonModel)) {
				orderEntryModel.setRejectedReason(rejectionReasonModel);
			}
		}
			orderEntryModel.setCancelReason(reason);
			orderEntryModel.setCancelledDate(new Date());
			orderEntryModel.setLatestStatusUpdate(new Date());
		    getModelService().save(orderEntryModel);
		if((orderEntryModel.getRequisitions()!=null && !orderEntryModel.getRequisitions().isEmpty()) && orderEntryModel.getRequisitions().size()==1) {
			OrderRequisitionModel orderRequisitionModel = orderEntryModel.getRequisitions().get(0);
			orderRequisitionModel.setStatus(RequisitionStatus.CANCELLED);
			orderRequisitionModel.setCancelledDate(new Date());
			orderRequisitionModel.setCancelledBy(user);
			orderRequisitionModel.setCancelReason(reason);
			getModelService().save(orderRequisitionModel);
		}
		//saveOrderRequisitionEntryDetails(order, orderEntryModel, "LINE_CANCELLED");

		//Trigger Order Line cancellation
		if(null != orderEntryModel.getErpLineItemId() && !orderEntryModel.getErpLineItemId().isEmpty()) {
			SclOrderLineCancelProcessModel processModel = new SclOrderLineCancelProcessModel();
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
//		final SalesOrderDeliverySLAModel salesOrderDeliverySLA = sclSalesOrderDeliverySLADao.findByRoute(routeId);
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
	public DeliveryDateAndSlotListData getOptimalDeliveryWindow(final double orderQuantity,final String routeId, B2BCustomerModel user, LocalDateTime  orderPunchedDate, final String sourceCode, String isDealerProvidingTruck){
		orderPunchedDate = orderPunchedDate.plusHours(5).plusMinutes(30);
		LOG.error("DELIVERY_WINDOW time 1: "+ orderPunchedDate.toString() );
		
		DeliveryDateAndSlotListData list = new DeliveryDateAndSlotListData();
		List<DeliveryDateAndSlotData> dataList = new ArrayList<DeliveryDateAndSlotData>();
		final SalesOrderDeliverySLAModel salesOrderDeliverySLA = sclSalesOrderDeliverySLADao.findByRoute(routeId);
			WarehouseModel sourceMaster =  warehouseService.getWarehouseForCode(sourceCode);
			if(sourceMaster!=null) {
//				String sourceType = sourceMaster.getType().getCode();
				String workingHourStartTime = "00:00";
				String workingHourEndTime = "23:59";
				if(sourceMaster.getWorkingHourStartTime()!=null) {
					workingHourStartTime = sourceMaster.getWorkingHourStartTime();
				}
				if(sourceMaster.getWorkingHourEndTime()!=null) {
					workingHourEndTime = sourceMaster.getWorkingHourEndTime();
				}
				int diCount = 1;
				//				if(salesOrderDeliverySLA.getCommonTruckCapacity()!=null)
				//					diCount = getDICountForOrder(salesOrderDeliverySLA.getCommonTruckCapacity(),orderQuantity);
				int maxTruckCount = 1000;
				double deliverySla;
				double dispatchSla;
				LocalDateTime orderDateTime = orderPunchedDate;
				//setting deliverySla & dispatchSla from rank file
				DestinationSourceMasterModel sourceMasterModel=getDeliveryAndDispatchSlaHour(routeId);
				if(Objects.nonNull(sourceMasterModel)){
					 deliverySla=(sourceMasterModel.getDistance())/25;
					 dispatchSla=Double.valueOf(sourceMasterModel.getPlantDispatchSlaHour());
				}else{
					 deliverySla= 0;
					 dispatchSla=0;
				}

				String noStart1 =Objects.nonNull(salesOrderDeliverySLA) && Objects.nonNull(salesOrderDeliverySLA.getNoEntryRestrictionStart1()) ? salesOrderDeliverySLA.getNoEntryRestrictionStart1(): Strings.EMPTY ;
				String noEnd1 = Objects.nonNull(salesOrderDeliverySLA) && Objects.nonNull(salesOrderDeliverySLA.getNoEntryRestrictionEnd1()) ? salesOrderDeliverySLA.getNoEntryRestrictionEnd1(): Strings.EMPTY ;
				String noStart2 = Objects.nonNull(salesOrderDeliverySLA) && Objects.nonNull(salesOrderDeliverySLA.getNoEntryRestrictionStart2()) ? salesOrderDeliverySLA.getNoEntryRestrictionStart2(): Strings.EMPTY ;
				String noEnd2 = Objects.nonNull(salesOrderDeliverySLA) && Objects.nonNull(salesOrderDeliverySLA.getNoEntryRestrictionEnd2()) ? salesOrderDeliverySLA.getNoEntryRestrictionEnd2(): Strings.EMPTY ;
				String noStart3 = Objects.nonNull(salesOrderDeliverySLA) && Objects.nonNull(salesOrderDeliverySLA.getNoEntryRestrictionStart3()) ? salesOrderDeliverySLA.getNoEntryRestrictionStart3(): Strings.EMPTY ;
				String noEnd3 = Objects.nonNull(salesOrderDeliverySLA) && Objects.nonNull(salesOrderDeliverySLA.getNoEntryRestrictionEnd3()) ? salesOrderDeliverySLA.getNoEntryRestrictionEnd3(): Strings.EMPTY ;
				String noStart4 = Objects.nonNull(salesOrderDeliverySLA) && Objects.nonNull(salesOrderDeliverySLA.getNoEntryRestrictionStart4()) ? salesOrderDeliverySLA.getNoEntryRestrictionStart4(): Strings.EMPTY ;
				String noEnd4 = Objects.nonNull(salesOrderDeliverySLA) && Objects.nonNull(salesOrderDeliverySLA.getNoEntryRestrictionEnd4()) ? salesOrderDeliverySLA.getNoEntryRestrictionEnd4(): Strings.EMPTY ;

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
				LOG.error("DELIVERY_WINDOW time 2: "+ orderDateTime.toString() );
				LocalDateTime orderWithSla = orderDateTime.plusSeconds(slaInSeconds);
				LOG.error("DELIVERY_WINDOW time 3: "+ orderWithSla.toString() );

				LocalDate orderDate =orderWithSla.toLocalDate();
				LOG.error("DELIVERY_WINDOW time 4: "+ orderDate.toString() );

				LocalDateTime tempPromisedDate = orderWithSla;

				if(localTimeNoStart1!=null && localTimeNoEnd1!=null && localTimeNoStart1.compareTo(localTimeNoEnd1)!=0) {
					LocalDateTime tempNoStart1Date = LocalDateTime.of(orderDate,localTimeNoStart1);
					LocalDateTime tempNoEnd1Date = LocalDateTime.of(orderDate,localTimeNoEnd1);
					LOG.error("DELIVERY_WINDOW time start no 1: "+ tempNoStart1Date.toString() );
					LOG.error("DELIVERY_WINDOW time end no 1: "+ tempNoEnd1Date.toString() );
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
						orderDate =tempPromisedDate.toLocalDate();
						LOG.error("DELIVERY_WINDOW time 5: "+ tempPromisedDate.toString() );
					}
				}

				if(localTimeNoStart2!=null && localTimeNoEnd2!=null && localTimeNoStart2.compareTo(localTimeNoEnd2)!=0) {
					LocalDateTime tempNoStart2Date = LocalDateTime.of(orderDate,localTimeNoStart2);
					LocalDateTime tempNoEnd2Date = LocalDateTime.of(orderDate,localTimeNoEnd2);
					LOG.error("DELIVERY_WINDOW time start no 2: "+ tempNoStart2Date.toString() );
					LOG.error("DELIVERY_WINDOW time end no 2: "+ tempNoEnd2Date.toString() );
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
							orderDate =tempPromisedDate.toLocalDate();
							LOG.error("DELIVERY_WINDOW time 6: "+ tempPromisedDate.toString() );
						}				
					}
				}

				if(localTimeNoStart3!=null && localTimeNoEnd3!=null && localTimeNoStart3.compareTo(localTimeNoEnd3)!=0) {
					LocalDateTime tempNoStart3Date = LocalDateTime.of(orderDate,localTimeNoStart3);
					LocalDateTime tempNoEnd3Date = LocalDateTime.of(orderDate,localTimeNoEnd3);

					LOG.error("DELIVERY_WINDOW time start no 3: "+ tempNoStart3Date.toString() );
					LOG.error("DELIVERY_WINDOW time end no 3: "+ tempNoEnd3Date.toString() );
					
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
							orderDate =tempPromisedDate.toLocalDate();
							LOG.error("DELIVERY_WINDOW time 7: "+ tempPromisedDate.toString() );
						}
					}
				}
				if(localTimeNoStart4!=null && localTimeNoEnd4!=null && localTimeNoStart4.compareTo(localTimeNoEnd4)!=0) {
					LocalDateTime tempNoStart4Date = LocalDateTime.of(orderDate,localTimeNoStart4);
					LocalDateTime tempNoEnd4Date = LocalDateTime.of(orderDate,localTimeNoEnd4);

					LOG.error("DELIVERY_WINDOW time start no 4: "+ tempNoStart4Date.toString() );
					LOG.error("DELIVERY_WINDOW time end no 4: "+ tempNoEnd4Date.toString() );
					
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
							orderDate =tempPromisedDate.toLocalDate();
							LOG.error("DELIVERY_WINDOW time 8: "+ tempPromisedDate.toString() );
						}
					}
				}

				LocalDate tempDate = tempPromisedDate.toLocalDate();
				LocalDateTime tempDeliveryDate = tempPromisedDate;
				LOG.error("DELIVERY_WINDOW time 9: "+ tempDeliveryDate.toString() );
				List<DeliverySlotMasterModel> slotList = deliverySlotMasterDao.findAll();
				slotList = slotList.stream().sorted(Comparator.comparing(DeliverySlotMasterModel::getSequence)).collect(Collectors.toList());

				//				if((isDealerProvidingTruck!=null && isDealerProvidingTruck.equals("true")) || salesOrderDeliverySLA.getCommonTruckCapacity()==null ||salesOrderDeliverySLA.getCommonTruckCapacity()==0) {

				DeliveryDateAndSlotData data = new DeliveryDateAndSlotData();
				tempDeliveryDate = getPossibleDay(tempDeliveryDate, slotList);
				LOG.error("DELIVERY_WINDOW time 10: "+ tempDeliveryDate.toString() );
				data.setDeliveryDate(tempDeliveryDate.toString());
				data.setDeliverySlot(getPossibleSlot(tempDeliveryDate, slotList));
//				data.setDeliveryDate(tempDeliveryDate.plusDays(2).toString());
//				data.setDeliverySlot(slotList.get(1).getCentreTime());
				data.setOrder(1);
				data.setQuantity(orderQuantity);
				//data.setMaxTruckPerDay(1000);
				dataList.add(data);
				list.setDeliveryDateAndSlots(dataList);
				
				//							}

				//				double tempQty = orderQuantity;
				//				double truckCapacity =salesOrderDeliverySLA.getCommonTruckCapacity();
				//
				//
				//				int sequence = 1;
				//				while(diCount>0) {
				//					int count = 0;
				//					int pendingCount = 0;//orderCountDao.findOrderByExpectedDeliveryDate(user,setSelectedDeliveryDate(tempDate.toString()), routeId);
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
				//						data.setDeliveryDate(getPossibleDay(tempDeliveryDate, slotList).toString());
				//						data.setDeliverySlot(getPossibleSlot(tempDeliveryDate, slotList));
				//						data.setOrder(sequence);
				//						data.setTruckCapcity(truckCapacity);
				//						if(tempQty>truckCapacity)
				//							data.setQuantity(truckCapacity);
				//						else
				//							data.setQuantity((double)tempQty);
				//						tempQty -= truckCapacity;
				//						dataList.add(data);
				//						sequence++;
				//					}
				//					tempDate =  tempDate.plusDays(1);
				//					tempDeliveryDate = LocalDateTime.of(tempDate,LocalTime.parse("07:00"));
				//
				//				}

				//				list.setDeliveryDateAndSlots(dataList);
			}

		return list;
	}

	/**
	 *
	 * @param routeId
	 * @return
	 */
	private DestinationSourceMasterModel getDeliveryAndDispatchSlaHour(String routeId){
		return sclSalesOrderDeliverySLADao.getDeliverySlaHour(routeId);
	}
	
	@Override
	public DeliveryDateAndSlotListData getOptimalISODeliveryWindow(final double orderQuantity,final String routeId, B2BCustomerModel user, final LocalDateTime  orderPunchedDate, final String sourceCode, final String depotCode){
		DeliveryDateAndSlotListData list = new DeliveryDateAndSlotListData();
		List<DeliveryDateAndSlotData> dataList = new ArrayList<DeliveryDateAndSlotData>();
		final ISODeliverySLAModel salesOrderDeliverySLA = sclISODeliverySLADao.findByRoute(routeId);
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
		String morningSlot = "";
		LocalTime deliveryTime = tempDeliveryDate.toLocalTime();
		
		for(DeliverySlotMasterModel slot: list) {
			LocalTime startTime = LocalTime.parse(slot.getCentreTime());
			if(deliveryTime.isBefore(startTime) || deliveryTime.equals(startTime)) {
				morningSlot = slot.getCentreTime();
				break;
			}
		}
		return morningSlot;
	}
	
	private LocalDateTime getPossibleDay(LocalDateTime tempDeliveryDate , List<DeliverySlotMasterModel> list) {
		LocalDateTime possibleDate = tempDeliveryDate;
		if(list!=null && !list.isEmpty()) {
			DeliverySlotMasterModel lastSlot= list.get(list.size()-1);
			LocalDateTime endTime = LocalDateTime.of(tempDeliveryDate.toLocalDate(),LocalTime.parse(lastSlot.getCentreTime()));
			if(tempDeliveryDate.isAfter(endTime)) {
				DeliverySlotMasterModel firstSlot= list.get(0);
				possibleDate = LocalDateTime.of(tempDeliveryDate.plusDays(1).toLocalDate(),LocalTime.parse(firstSlot.getCentreTime()));
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
			 sclModifyOrderStrategy.modiyOrderEntry(orderModel,parameter);
	}

	@Override
	public void  modifyOrderDetails(OrderModel order , OrderData orderData, Double basePrice) throws CalculationException {
		order.setTotalQuantity(orderData.getTotalQuantity());
		if(StringUtils.isNotBlank(orderData.getOrderSource())){
			WarehouseModel warehouseByCode = sclWarehouseDao.findWarehouseByCode(orderData.getOrderSource());
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
				businessProcessService.triggerEvent(orderProcessModel.getCode()+"_"+ SclCoreConstants.APPROVAL_CONSTANT.ORDER_REVIEW_DECISION_EVENT_NAME);
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

			sclNotificationService.submitOrderNotification(order, (B2BCustomerModel) order.getUser(),body,subject,category);

			SclUserModel so = territoryManagementService.getSOforCustomer((SclCustomerModel) order.getUser());
			sclNotificationService.submitOrderNotification(order,so,body,subject,category);

			SclCustomerModel sp = territoryManagementService.getSpForCustomerAndBrand((SclCustomerModel) order.getUser(),order.getSite());
			sclNotificationService.submitOrderNotification(order,sp,body,subject,category);

			List<SclUserModel> tsm = territoryManagementService.getTSMforDistrict(order.getDistrictMaster(), order.getSite());
			if(tsm!=null && tsm.isEmpty()){
				for(SclUserModel TSM : tsm){
					sclNotificationService.submitOrderNotification(order,TSM,body,subject,category);
				}
			}
			List<SclUserModel> rh = territoryManagementService.getRHforRegion(order.getRegionMaster(), order.getSite());
			if(rh!=null && rh.isEmpty()){
				for(SclUserModel RH: rh){
					sclNotificationService.submitOrderNotification(order,RH,body,subject,category);
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
			WarehouseModel warehouseByCode = sclWarehouseDao.findWarehouseByCode(orderData.getWarehouse().getCode());
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
				Date date = new SimpleDateFormat(SclCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1).parse(selectedDeliveryDate);
				entry.setExpectedDeliveryDate(date);
			}
			catch (ParseException ex){
				LOGGER.error("Date is not in correct format: "+selectedDeliveryDate);
				throw new AmbiguousIdentifierException("Date is not in the correct format: "+SclCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
			}
		}
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

	protected Set<DeliveryItemStatus> extractDeliveryStatuses(final String statuses)
	{
		final String[] statusesStrings = statuses.split(SclCoreConstants.ORDER.ENUM_VALUES_SEPARATOR);

		final Set<DeliveryItemStatus> statusesEnum = new HashSet<>();
		for (final String status : statusesStrings)
		{
			statusesEnum.add(DeliveryItemStatus.valueOf(status));
		}
		return statusesEnum;
	}

	@Override
	public SearchPageData<SclOrderHistoryData> getOrderHistoryForOrder(SearchPageData searchPageData, String orderStatus, String filter,String productName , String orderType, Boolean isCreditLimitBreached, String spApprovalFilter, Boolean approvalPending) {
		final UserModel currentUser = getUserService().getCurrentUser();
		String statues = validateAndMapOrderStatuses(orderStatus);
		final BaseStoreModel currentBaseStore = baseStoreService.getCurrentBaseStore();
		final Set<OrderStatus> statusSet = extractOrderStatuses(statues);
		final SearchPageData<SclOrderHistoryData> result = new SearchPageData<>();

		if(StringUtils.isBlank(filter) && StringUtils.isBlank(productName) && StringUtils.isBlank(orderType)) {
			SearchPageData<OrderModel> ordersListByStatusForSO = sclOrderCountDao.findOrdersListByStatusForSO(currentUser, currentBaseStore, statusSet.toArray(new OrderStatus[statusSet.size()]), searchPageData, isCreditLimitBreached, spApprovalFilter, approvalPending);
			result.setPagination(ordersListByStatusForSO.getPagination());
			result.setSorts(ordersListByStatusForSO.getSorts());
			final List<SclOrderHistoryData> sclOrderHistoryData = sclOrderHistoryCardConverter.convertAll(ordersListByStatusForSO.getResults());
			result.setResults(sclOrderHistoryData);
		}
		else
		{
			OrderType orderTypeEnum = null;
			if(StringUtils.isNotBlank(orderType)){
				orderTypeEnum = OrderType.valueOf(orderType);
			}

			SearchPageData<OrderModel> ordersListByStatusForSO = sclOrderCountDao.findOrdersListByStatusForSO(currentUser, currentBaseStore, statusSet.toArray(new OrderStatus[statusSet.size()]), searchPageData, filter,productName,orderTypeEnum,isCreditLimitBreached, spApprovalFilter, approvalPending);
			result.setPagination(ordersListByStatusForSO.getPagination());
			result.setSorts(ordersListByStatusForSO.getSorts());
			final List<SclOrderHistoryData> sclOrderHistoryData = sclOrderHistoryCardConverter.convertAll(ordersListByStatusForSO.getResults());
			result.setResults(sclOrderHistoryData);
		}
		return result;
	}

	@Override
	public SearchPageData<SclOrderHistoryData> getOrderHistoryForOrderEntry(SearchPageData searchPageData, String orderStatus, String filter,String productName , String orderType, String spApprovalFilter) {
		final UserModel currentUser = getUserService().getCurrentUser();
		String statues = validateAndMapOrderStatuses(orderStatus);
		LOG.info(String.format("getOrderHistoryForOrderEntry Statuses::%s for user ::%s",statues,currentUser));
		final BaseStoreModel currentBaseStore = baseStoreService.getCurrentBaseStore();
		final Set<OrderStatus> statusSet = extractOrderStatuses(statues);

		final SearchPageData<SclOrderHistoryData> result = new SearchPageData<>();

		if(StringUtils.isBlank(filter) && StringUtils.isBlank(productName) && StringUtils.isBlank(orderType))
		{
			try{
			SearchPageData<OrderEntryModel> orderEntriesListByStatusForSO = sclOrderCountDao.findOrderEntriesListByStatusForSO(currentUser, currentBaseStore, statusSet.toArray(new OrderStatus[statusSet.size()]), searchPageData, spApprovalFilter);
				LOG.info("Order Entries List By Status For SO" + orderEntriesListByStatusForSO);
			result.setPagination(orderEntriesListByStatusForSO.getPagination());
			result.setSorts(orderEntriesListByStatusForSO.getSorts());
			List<SclOrderHistoryData> sclOrderHistoryData = sclOrderEntryHistoryCardConverter.convertAll(orderEntriesListByStatusForSO.getResults());
				LOG.info("Order Entries List By Status For SO" + orderEntriesListByStatusForSO);
			result.setResults(sclOrderHistoryData);
			}catch (Exception ex){
				LOG.error(String.format("Exception occur in order entries list for so::%s and get cause::%s",ex.getMessage(),ex.getCause()));
			}
		}
		else {
			OrderType orderTypeEnum = null;
			if(StringUtils.isNotBlank(orderType)){
				orderTypeEnum = OrderType.valueOf(orderType);
			}
			try {
				SearchPageData<OrderEntryModel> orderEntriesListByStatusForSO = sclOrderCountDao.findOrderEntriesListByStatusForSO(currentUser, currentBaseStore, statusSet.toArray(new OrderStatus[statusSet.size()]), searchPageData, filter, productName, orderTypeEnum, spApprovalFilter);
				LOG.info("Order Entries List By Status For SO" + orderEntriesListByStatusForSO);
				result.setPagination(orderEntriesListByStatusForSO.getPagination());
				result.setSorts(orderEntriesListByStatusForSO.getSorts());
				List<SclOrderHistoryData> sclOrderHistoryData = sclOrderEntryHistoryCardConverter.convertAll(orderEntriesListByStatusForSO.getResults());
				result.setResults(sclOrderHistoryData);
			}catch (Exception ex){
				LOG.error(String.format("Exception occur in order entries list for ::%s and get cause::%s",ex.getMessage(),ex.getCause()));
			}
		}
		return result;
	}

	@Override
	public SearchPageData<SclOrderHistoryData> getOrderHistoryForDeliveryItem(SearchPageData searchPageData, String deliveryStatus, String filter, String spApprovalFilter) {
		final UserModel currentUser = getUserService().getCurrentUser();
		String statues = validateAndMapOrderStatuses(deliveryStatus);
		final BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
		final Set<DeliveryItemStatus> statusSet = extractDeliveryStatuses(statues);
		LOG.info(String.format("getOrderHistoryForDeliveryItem Statuses::%s for user ::%s",statues,currentUser));
		final SearchPageData<SclOrderHistoryData> result = new SearchPageData<>();
		try {
			SearchPageData<DeliveryItemModel> orderEntriesListByStatusForSO = sclOrderCountDao.findDeliveryItemListByStatus(currentUser, currentBaseSite, statusSet.toArray(new DeliveryItemStatus[statusSet.size()]), searchPageData, spApprovalFilter, filter);
			LOG.info("Order Entries List By Status For SO" + orderEntriesListByStatusForSO);
			result.setPagination(orderEntriesListByStatusForSO.getPagination());
			result.setSorts(orderEntriesListByStatusForSO.getSorts());
			List<SclOrderHistoryData> sclOrderHistoryData = sclDeliveryItemHistoryCardConverter.convertAll(orderEntriesListByStatusForSO.getResults());
			LOG.info(String.format("converted order entry data::%s",sclOrderHistoryData));
			result.setResults(sclOrderHistoryData);
		}catch (Exception ex){
			LOG.error(String.format("Exception occur in delivery item list for ::%s and get cause::%s",ex.getMessage(),ex.getCause()));
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

	public SCLMastersDao getSclMastersDao() {
		return sclMastersDao;
	}

	public void setSclMastersDao(SCLMastersDao sclMastersDao) {
		this.sclMastersDao = sclMastersDao;
	}

	public SclCartService getSclCartService() {
		return sclCartService;
	}

	public void setSclCartService(SclCartService sclCartService) {
		this.sclCartService = sclCartService;
	}

	@Override
	public List<DeliverySlotMasterModel> getDeliverySlotList() {
		return deliverySlotMasterDao.findAll();
	}

	@Override
	public void submitOrderForCancellation(SclOrderCancelProcessModel sclOrderCancelProcessModel) {
		eventService.publishEvent(new SclOrderCancelEvent(sclOrderCancelProcessModel));
	}

	@Override
	public void submitOrderLineForCancellation(SclOrderLineCancelProcessModel sclOrderCancelLineProcessModel) {
		eventService.publishEvent(new SclOrderLineCancelEvent(sclOrderCancelLineProcessModel));
	}

	@Override
	public void updateTotalQuantity(long quantity) {
		final CartModel cart = cartService.getSessionCart();		
		cart.setTotalQuantity((double)quantity);
		getModelService().save(cart);
	}

	@Override
	public SearchPageData<SclOrderHistoryData> getCancelOrderHistoryForOrder(SearchPageData searchPageData, String orderStatus, String filter, String productName, String orderType, String spApprovalFilter, Integer month, Integer year) {
		final UserModel currentUser = getUserService().getCurrentUser();
		String statues = validateAndMapOrderStatuses(orderStatus);
		final BaseStoreModel currentBaseStore = baseStoreService.getCurrentBaseStore();
		final Set<OrderStatus> statusSet = extractOrderStatuses(statues);
		final SearchPageData<SclOrderHistoryData> result = new SearchPageData<>();

		String monthYear = null;
		if(month!=0 && year!=0) {
			int fYear = Integer.parseInt(year.toString());
			int fMonth = Integer.parseInt(month.toString());

			String singleDigitMonth = Integer.toString(fYear) + "-0" + Integer.toString(fMonth) + "-%";
			String doubleDigitMonth = Integer.toString(fYear) + "-" + Integer.toString(fMonth) + "-%";

			monthYear = (fMonth >= 1 && fMonth <= 9) ? singleDigitMonth : doubleDigitMonth;
		}

		if(StringUtils.isBlank(filter) && StringUtils.isBlank(productName) && StringUtils.isBlank(orderType)) {
			SearchPageData<OrderModel> ordersListByStatusForSO = sclOrderCountDao.findCancelOrdersListByStatusForSO(currentUser, currentBaseStore, statusSet.toArray(new OrderStatus[statusSet.size()]), searchPageData, spApprovalFilter, monthYear);
			result.setPagination(ordersListByStatusForSO.getPagination());
			result.setSorts(ordersListByStatusForSO.getSorts());
			final List<SclOrderHistoryData> sclOrderHistoryData = sclOrderHistoryCardConverter.convertAll(ordersListByStatusForSO.getResults());
			result.setResults(sclOrderHistoryData);
		}
		else
		{
			OrderType orderTypeEnum = null;
			if(StringUtils.isNotBlank(orderType)){
				orderTypeEnum = OrderType.valueOf(orderType);
			}

			SearchPageData<OrderModel> ordersListByStatusForSO = sclOrderCountDao.findCancelOrdersListByStatusForSO(currentUser, currentBaseStore, statusSet.toArray(new OrderStatus[statusSet.size()]), searchPageData, filter,productName,orderTypeEnum, spApprovalFilter, monthYear);
			result.setPagination(ordersListByStatusForSO.getPagination());
			result.setSorts(ordersListByStatusForSO.getSorts());
			final List<SclOrderHistoryData> sclOrderHistoryData = sclOrderHistoryCardConverter.convertAll(ordersListByStatusForSO.getResults());
			result.setResults(sclOrderHistoryData);
		}
		return result;
	}

	@Override
	public SearchPageData<SclOrderHistoryData> getCancelOrderHistoryForOrderEntry(SearchPageData searchPageData, String orderStatus, String filter, String productName, String orderType, String spApprovalFilter, Integer month, Integer year) {
		final UserModel currentUser = getUserService().getCurrentUser();
		String statues = validateAndMapOrderStatuses(orderStatus);
		final BaseStoreModel currentBaseStore = baseStoreService.getCurrentBaseStore();
		final Set<OrderStatus> statusSet = extractOrderStatuses(statues);

		final SearchPageData<SclOrderHistoryData> result = new SearchPageData<>();

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
			SearchPageData<OrderEntryModel> orderEntriesListByStatusForSO = sclOrderCountDao.findCancelOrderEntriesListByStatusForSO(currentUser, currentBaseStore, statusSet.toArray(new OrderStatus[statusSet.size()]), searchPageData, spApprovalFilter, monthYear);
			result.setPagination(orderEntriesListByStatusForSO.getPagination());
			result.setSorts(orderEntriesListByStatusForSO.getSorts());
			List<SclOrderHistoryData> sclOrderHistoryData = sclOrderEntryHistoryCardConverter.convertAll(orderEntriesListByStatusForSO.getResults());
			result.setResults(sclOrderHistoryData);
		}
		else {
			OrderType orderTypeEnum = null;
			if(StringUtils.isNotBlank(orderType)){
				orderTypeEnum = OrderType.valueOf(orderType);
			}
			SearchPageData<OrderEntryModel> orderEntriesListByStatusForSO = sclOrderCountDao.findCancelOrderEntriesListByStatusForSO(currentUser, currentBaseStore, statusSet.toArray(new OrderStatus[statusSet.size()]), searchPageData,filter,productName,orderTypeEnum, spApprovalFilter, monthYear);
			result.setPagination(orderEntriesListByStatusForSO.getPagination());
			result.setSorts(orderEntriesListByStatusForSO.getSorts());
			List<SclOrderHistoryData> sclOrderHistoryData = sclOrderEntryHistoryCardConverter.convertAll(orderEntriesListByStatusForSO.getResults());
			result.setResults(sclOrderHistoryData);
		}
		return result;
	}

	private void cancelOrder(OrderModel order) {
		B2BCustomerModel currentUser = (B2BCustomerModel) order.getPlacedBy();
		final StringBuilder builder = new StringBuilder(NOTIFICATION_GREETING);
		if(null!=order) {
			SiteMessageModel notification = getModelService().create(SiteMessageModel.class);
			notification.setNotificationType(NotificationType.NOTIFICATION);
			notification.setSubject(SclCoreConstants.ORDER_NOTIFICATION.ORDER_CANCELLED_NOTIFICATION);
			builder.append(order.getPlacedBy().getUid()).append(ORDER_PLACED_BY);
			builder.append(order.getUser().getUid()).append(ORDER_DATE);
			DateFormat dateFormat = new SimpleDateFormat(SclCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
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
	
	@Override
	public Boolean getVehicleArrivalConfirmationForOrder(boolean vehicleArrived, String orderCode, String entryNumber, String deliveryItemCode, String deliveryLineNumber){
		OrderModel order = getOrderForCode(orderCode);
		int entryNum = Integer.valueOf(entryNumber);
		OrderEntryModel orderEntry = (OrderEntryModel) order.getEntries().stream().filter(abstractOrderEntryModel -> abstractOrderEntryModel.getEntryNumber()==entryNum).findAny().get();
		DeliveryItemModel deliveryItemModel = orderEntry.getDeliveriesItem().stream().filter(i -> i.getDiNumber().equalsIgnoreCase(deliveryItemCode) && i.getDeliveryLineNumber().equalsIgnoreCase(deliveryLineNumber)).findAny().get();
		Date date = new Date();
		if(vehicleArrived == true){
			deliveryItemModel.setIsVehicleArrived(true);
			deliveryItemModel.setStatus(DeliveryItemStatus.EPOD_PENDING);
			deliveryItemModel.setEpodInitiateDate(date);
			deliveryItemModel.setEpodStatus(EpodStatus.PENDING);
			deliveryItemModel.setLatestStatusUpdate(date);
			getModelService().save(deliveryItemModel);
		}
		else{
			//TODO: Dependent on other persona
		}
		return true;
	}

	/**
	 *
	 * @param shortageQuantity
	 * @param orderCode
	 * @param entryNumber
	 * @param deliveryItemCode
	 * @return
	 */
	public Boolean updateEpodStatusForOrder(double shortageQuantity, String orderCode, int entryNumber, String deliveryItemCode, String deliveryLineNumber){
		OrderModel order = getOrderForCode(orderCode);
		OrderEntryModel orderEntry = (OrderEntryModel) order.getEntries().stream().filter(abstractOrderEntryModel -> abstractOrderEntryModel.getEntryNumber()==entryNumber).findAny().get();
		if(Objects.nonNull(deliveryItemCode)){
			Optional<DeliveryItemModel> deliveryItemModelOptional = orderEntry.getDeliveriesItem().stream().filter(i -> i.getDiNumber().equalsIgnoreCase(deliveryItemCode) && i.getDeliveryLineNumber().equalsIgnoreCase(deliveryLineNumber)).findFirst();
			DeliveryItemModel deliveryItemModel = new DeliveryItemModel();
			if(deliveryItemModelOptional.isPresent()){
				 deliveryItemModel = getDeliveryItemModel(shortageQuantity, deliveryItemModelOptional);
				getModelService().save(deliveryItemModel);
				LOGGER.info(String.format("Delivery Item Saved Successfully For Delivery Item Code :: %s",deliveryItemCode));
			}
			else {
				LOGGER.info(String.format("Invalid Delivery Item Code :: %s",deliveryItemCode));
			}
			saveOrderRequisitionEntryDetails(orderEntry, deliveryItemModel, "EPOD");
			return true;
		}
		return false;
	}

	/**
	 *
	 * @param shortageQuantity
	 * @param deliveryItemModelOptional
	 * @return
	 */
	private DeliveryItemModel getDeliveryItemModel(double shortageQuantity, Optional<DeliveryItemModel> deliveryItemModelOptional) {
		DeliveryItemModel deliveryItemModel = deliveryItemModelOptional.get();

		deliveryItemModel.setShortageQuantity(shortageQuantity);
		if(shortageQuantity > 0){
			deliveryItemModel.setEpodStatus(EpodStatus.DISPUTED);
		}
		else {
			deliveryItemModel.setEpodStatus(EpodStatus.APPROVED);
		}
		deliveryItemModel.setStatus(DeliveryItemStatus.DELIVERED);
		deliveryItemModel.setDeliveredDate(new Date());
		deliveryItemModel.setEpodCompleted(true);
		deliveryItemModel.setEpodCompletedDate(deliveryItemModel.getDeliveredDate());
		deliveryItemModel.setLatestStatusUpdate(deliveryItemModel.getDeliveredDate());
		return deliveryItemModel;
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
						receiptAllocate.setYear(LocalDate.now().getYear());
						receiptAllocate.setMonth(LocalDate.now().getMonthValue());
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
					receiptRetailerAllocateNew.setYear(LocalDate.now().getYear());
					receiptRetailerAllocateNew.setMonth(LocalDate.now().getMonthValue());
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
					receiptRetailerAllocate.setYear(LocalDate.now().getYear());
					receiptRetailerAllocate.setMonth(LocalDate.now().getMonthValue());
		  			LOGGER.info("9. Retailer RECEIPT:::Updated " + retailerReceiptQty
		  			+ " Available stock for influencer -->" + receiptRetailerAllocate.getStockAvlForInfluencer()
		  			+ " Allocated or sales to influencer -->" + receiptRetailerAllocate.getSalesToInfluencer());
		  			getModelService().save(receiptRetailerAllocate);
		  		} else {
		  		//If product and dealer is not found in the RetailerRecAllocate 
		  			//then it means new entry has to be made as orderrequisition is placed with this combination
		  			RetailerRecAllocateModel receiptRetailerAllocateNew = getModelService().create(RetailerRecAllocateModel.class);
		  			receiptRetailerAllocateNew.setProduct(model.getProduct().getPk().toString());
		  			receiptRetailerAllocateNew.setDealerCode(model.getToCustomer().getPk().toString());
		  			updatedQty = receiptRetailerAllocateNew.getReceipt() + model.getQuantity();
		  			receiptRetailerAllocateNew.setReceipt((null != updatedQty)?updatedQty.intValue():0);
		  			receiptRetailerAllocateNew.setSalesToInfluencer(0);
		  			int stockRetailerInfluencer = (int) ((1.0 * (receiptRetailerAllocateNew.getReceipt() - receiptRetailerAllocateNew.getSalesToInfluencer())));
		  			receiptRetailerAllocateNew.setStockAvlForInfluencer(stockRetailerInfluencer);
		  			receiptRetailerAllocateNew.setYear(LocalDate.now().getYear());
					receiptRetailerAllocateNew.setMonth(LocalDate.now().getMonthValue());
					  getModelService().save(receiptRetailerAllocateNew);
		  			getModelService().refresh(receiptRetailerAllocateNew);
		  		}
			}	
			Date date = new Date();
			if(model.getAcceptedDate()==null) {
				model.setAcceptedDate(date);
			}
			model.setFulfilledDate(date);
			model.setDeliveredDate(date);
		//	orderRequisitionService.orderCountIncrementForDealerRetailerMap(model.getDeliveredDate(),model.getFromCustomer(),model.getToCustomer(), baseSiteService.getCurrentBaseSite());
		} else if (status!=null && status.equals("REJECT_REQUEST")) {
			model.setStatus(RequisitionStatus.REJECTED);
			model.setRejectedDate(new Date());
			model.setRejectedBy((SclCustomerModel) getUserService().getCurrentUser());
		}
		getModelService().save(model);

		return true;
	}

	public SearchPageData<SclOrderHistoryData> getEpodListBasedOnOrderStatus(SearchPageData searchPageData, List<String> Status, String filter) {
		final UserModel currentUser = getUserService().getCurrentUser();
		final BaseStoreModel currentBaseStore = baseStoreService.getCurrentBaseStore();

		final SearchPageData<SclOrderHistoryData> result = new SearchPageData<>();


			SearchPageData<DeliveryItemModel> orderEntriesListByStatusForSO = sclOrderCountDao.findOrderEntriesListByStatusForEPOD(currentUser, currentBaseStore, Status, searchPageData, filter);
			result.setPagination(orderEntriesListByStatusForSO.getPagination());
			result.setSorts(orderEntriesListByStatusForSO.getSorts());
			List<SclOrderHistoryData> sclOrderHistoryData = null;
		    sclOrderHistoryData = sclDeliveryItemHistoryCardConverter.convertAll(orderEntriesListByStatusForSO.getResults());
			result.setResults(sclOrderHistoryData);

		return result;
	}

	@Override
	public Boolean getEpodFeedback(EpodFeedbackData epodFeedbackData) {
		Map<String,String> epodFeedback= new HashMap<>();

		String deliveryItemCode = epodFeedbackData.getDeliveryItemCode();
		String deliveryLineNumber = epodFeedbackData.getDeliveryLineNumber();
		String orderCode = epodFeedbackData.getOrderCode();
		int entryNumber = epodFeedbackData.getEntryNumber();

		OrderModel order = getOrderForCode(orderCode);
		OrderEntryModel orderEntry = (OrderEntryModel) order.getEntries().stream().filter(abstractOrderEntryModel -> abstractOrderEntryModel.getEntryNumber()==entryNumber).findAny().get();
		if(!deliveryItemCode.isEmpty()){
			Optional<DeliveryItemModel> deliveryItemModelOptional = orderEntry.getDeliveriesItem().stream().filter(i -> i.getDiNumber().equalsIgnoreCase(deliveryItemCode) && i.getDeliveryLineNumber().equalsIgnoreCase(deliveryLineNumber)).findFirst();
			if(deliveryItemModelOptional.isPresent()){
				DeliveryItemModel deliveryItemModel = deliveryItemModelOptional.get();

				epodFeedback.put("driverRating",epodFeedbackData.getDriverRating());
				epodFeedback.put("deliveryProcess", epodFeedbackData.getDeliveryProcess());
				epodFeedback.put("materialReceipt", epodFeedbackData.getMaterialReceipt());
				epodFeedback.put("serviceLevel",epodFeedbackData.getServiceLevel());
				epodFeedback.put("overallDeliveryExperience", epodFeedbackData.getOverallDeliveryExperience());
				epodFeedback.put("comments", epodFeedbackData.getComments());

				deliveryItemModel.setEpodFeedback(epodFeedback);
				getModelService().save(deliveryItemModel);
				LOGGER.info(String.format("Feedback Saved Successfully for delivery Item Code :: %s",deliveryItemCode));
				return true;
			}
			else {
				LOGGER.info(String.format("Invalid Delivery Item Code :: %s",deliveryItemCode));
				return false;
			}
		}
		LOGGER.info(String.format("Delivery Item Code is NULL"));
		return false;

	}

	 //remove
		/*public void saveOrderRequisitionEntryDetails(OrderModel order, OrderEntryModel orderEntry, String status) {
			if(orderEntry.getRequisitions()!=null && !orderEntry.getRequisitions().isEmpty()) {
				if(orderEntry.getRequisitions().size() == 1) {
					boolean isDeliveredDateNull = false;
					OrderRequisitionModel orderRequisitionModel = orderEntry.getRequisitions().get(0);

					OrderRequisitionEntryModel orderRequisitionEntryModel = getModelService().create(OrderRequisitionEntryModel.class);
					orderRequisitionEntryModel.setQuantity(orderEntry.getDeliveryQty() * 20); //di item quantity
					orderRequisitionEntryModel.setEntryNumber(orderEntry.getDiNumber());
					orderRequisitionEntryModel.setEntry(orderEntry); //di number and delieryItem
					orderRequisitionEntryModel.setOrderRequisition(orderRequisitionModel);
					getModelService().save(orderRequisitionEntryModel);

					if(status.equals("EPOD")) {   //di quantity insted of invoice quantity
						orderRequisitionModel.setReceivedQty(orderRequisitionModel.getReceivedQty() + (orderEntry.getInvoiceQuantity() * 20));
						if(orderRequisitionModel.getFulfilledDate()==null) {
							orderRequisitionModel.setStatus(RequisitionStatus.PENDING_DELIVERY);
							orderRequisitionModel.setFulfilledDate(new Date());
						}
	                   //delivery item  pe for loop delivery date
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
							
							SclCustomerModel currentUser = (SclCustomerModel) userService.getCurrentUser();
			                LOGGER.info("1. Retailer RECEIPT::: In DefaultSCLB2BOrderService:: Record found--- Requisition Status... " + orderRequisitionModel.getStatus()
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
		}*/

	/**
	 *
	 * @param orderEntry
	 * @param deliveryItem
	 * @param status
	 */
	@Override
	public void saveOrderRequisitionEntryDetails(OrderEntryModel orderEntry, DeliveryItemModel deliveryItem, String status) {
		if(orderEntry.getRequisitions()!=null && !orderEntry.getRequisitions().isEmpty()) {
			if(orderEntry.getRequisitions().size() == 1) {
				boolean isDeliveredDateNull = false;
				OrderRequisitionModel orderRequisitionModel = orderEntry.getRequisitions().get(0);

				OrderRequisitionEntryModel orderRequisitionEntryModel = getModelService().create(OrderRequisitionEntryModel.class);
				orderRequisitionEntryModel.setQuantity(deliveryItem.getInvoiceQuantity() * 20); //di item quantity
				orderRequisitionEntryModel.setDiNumber(Integer.parseInt(deliveryItem.getDiNumber()));
				//delivery line number add
				orderRequisitionEntryModel.setDeliveryItem(deliveryItem); //di number and delieryItem
				orderRequisitionEntryModel.setOrderRequisition(orderRequisitionModel);
				getModelService().save(orderRequisitionEntryModel);

				if(status.equals("EPOD")) {
					orderRequisitionModel.setReceivedQty(orderRequisitionModel.getReceivedQty() + (deliveryItem.getInvoiceQuantity() * 20));
					if(orderRequisitionModel.getFulfilledDate()==null) {
						orderRequisitionModel.setStatus(RequisitionStatus.PENDING_DELIVERY);
						orderRequisitionModel.setFulfilledDate(new Date());
					}

					for(DeliveryItemModel entry : orderEntry.getDeliveriesItem()) {
						if(entry.getDeliveredDate() == null) {
							isDeliveredDateNull = true;
							break;
						}
					}
					if(!isDeliveredDateNull) {
						orderRequisitionModel.setStatus(RequisitionStatus.DELIVERED);
						orderRequisitionModel.setDeliveredDate(new Date());
				//		orderRequisitionService.orderCountIncrementForDealerRetailerMap(orderRequisitionModel.getDeliveredDate(),orderRequisitionModel.getFromCustomer(), orderRequisitionModel.getToCustomer(), baseSiteService.getCurrentBaseSite());

						SclCustomerModel currentUser = (SclCustomerModel) userService.getCurrentUser();
						LOGGER.info("1. Retailer RECEIPT::: In DefaultSCLB2BOrderService:: Record found--- Requisition Status... " + orderRequisitionModel.getStatus()
								+ " Current customer No -->" + currentUser.getCustomerNo());
						if (null != orderRequisitionModel.getFromCustomer() && null != currentUser) {
							updateRetailerReceipts(orderRequisitionModel.getProduct(), orderRequisitionModel.getFromCustomer(), orderRequisitionModel.getReceivedQty());
						}
					}
					else {
						getRequisitionStatusByOrderLines(orderEntry);
					}

				}
				getModelService().save(orderRequisitionModel);
			}
		}
	}
	
	//To update the quantity as receipts for allocation calculation
  	private void updateRetailerReceipts(ProductModel productCode, SclCustomerModel dealerCode, Double receivedQuantity) {
  		RetailerRecAllocateModel receiptRetailerAllocate = dealerDao.getRetailerAllocation(productCode, dealerCode);
  		if (null != receiptRetailerAllocate) {
  			LOGGER.info("1. Retailer RECEIPT:::DefaultSCLB2BOrderService:: Record found--- Receipts for Dealer " + receiptRetailerAllocate.getReceipt()
  			+ " Dealer No -->" + receiptRetailerAllocate.getDealerCode()
  			+ " Available stock for influencer -->" + receiptRetailerAllocate.getStockAvlForInfluencer()
  			+ " Available allocated or sales to influencer -->" + receiptRetailerAllocate.getSalesToInfluencer());
  			receiptRetailerAllocate.setSalesToInfluencer((new Double(receivedQuantity)).intValue());
			if(receiptRetailerAllocate.getReceipt()!=null && receiptRetailerAllocate.getSalesToInfluencer()!=null) {
				int stockRetailerToInfluencer = Math.abs((int) ((1.0 * (receiptRetailerAllocate.getReceipt() - receiptRetailerAllocate.getSalesToInfluencer()))));
				receiptRetailerAllocate.setStockAvlForInfluencer(stockRetailerToInfluencer);
			}
  			LOGGER.info("2. Retailer RECEIPT:::DefaultSCLB2BOrderService:: Updated " + receiptRetailerAllocate.getReceipt()
  			+ " Available stock for influencer -->" + receiptRetailerAllocate.getStockAvlForInfluencer()
  			+ " Allocated or sales to influencer -->" + receiptRetailerAllocate.getSalesToInfluencer());
			receiptRetailerAllocate.setYear(LocalDate.now().getYear());
			receiptRetailerAllocate.setMonth(LocalDate.now().getMonthValue());
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
			receiptRetailerAllocateNew.setYear(LocalDate.now().getYear());
			receiptRetailerAllocateNew.setMonth(LocalDate.now().getMonthValue());
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
					SclUserModel so = territoryManagementService.getSOforCustomer((SclCustomerModel) orderModel.getUser());
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
	public void getRequisitionStatusByOrderLines(OrderEntryModel orderEntry) {
		int orderLineCancelledCount = 0;
		int orderLineDeliveredCount = 0;
		Date partialDeliveredDate = null;

		if(orderEntry.getRequisitions()!=null && !orderEntry.getRequisitions().isEmpty() && orderEntry.getRequisitions().size()==1) {
			OrderRequisitionModel orderRequisitionModel = orderEntry.getRequisitions().get(0);

			for(DeliveryItemModel entryModel : orderEntry.getDeliveriesItem()) {
				if(entryModel.getCancelledDate()!=null) {
					orderLineCancelledCount += 1;
				}
				else if(entryModel.getDeliveredDate()!=null) {
					orderLineDeliveredCount += 1;
					partialDeliveredDate = entryModel.getDeliveredDate();
				}
			}

			int orderLineCancelledAndDeliveredCount = orderLineDeliveredCount + orderLineCancelledCount;
			if(orderLineCancelledAndDeliveredCount >= orderEntry.getDeliveriesItem().size()) {  //di item
				if(orderLineCancelledCount  >= 1 && orderLineDeliveredCount >= 1) {
					orderRequisitionModel.setStatus(RequisitionStatus.PARTIAL_DELIVERED);
					orderRequisitionModel.setPartialDeliveredDate(partialDeliveredDate);
					orderRequisitionModel.setDeliveredDate(partialDeliveredDate);
				//	orderRequisitionService.orderCountIncrementForDealerRetailerMap(orderRequisitionModel.getDeliveredDate(),orderRequisitionModel.getFromCustomer(), orderRequisitionModel.getToCustomer(), baseSiteService.getCurrentBaseSite());
				}

				if(orderLineCancelledCount == orderEntry.getDeliveriesItem().size()) {
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
		String stockInfluencerMultiplyValue=dataConstraintDao.findVersionByConstraintName("STOCK_INFLUENCER_MULTIPLY_VALUE");
		if(StringUtils.isNotBlank(stockInfluencerMultiplyValue)) {
			stockInfluencer = Math.abs((int) ((Double.valueOf(stockInfluencerMultiplyValue) * (receipt - saleToRetailer)) - saleToInfluencer));
		}
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
		if(user instanceof SclUserModel) {
			SclWorkflowModel approvalWorkflow = order.getApprovalWorkflow();
			if(approvalWorkflow!=null) {
				SclWorkflowActionModel currentAction = approvalWorkflow.getCurrent();
				if(currentAction!=null) {
					sclWorkflowService.updateWorkflowAction(currentAction, user, WorkflowActions.APPROVED, b2bOrderApprovalData.getApprovalComments());
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
				getBusinessProcessService().triggerEvent(orderProcessModel.getCode()+"_"+ SclCoreConstants.APPROVAL_CONSTANT.ORDER_REVIEW_DECISION_EVENT_NAME);
			}		
		}
		else {
			throw new UnknownIdentifierException("Access not allowed");
		}
	}

	@Override
	public List<SalesHistoryModel> getNCREntriesExistingInOrderEntry() {
		return sclOrderCountDao.getNCREntriesExistingInOrderEntry();
	}

	@Override
	public List<String> getNCREntriesNotExistingInOrderEntry() {
		return sclOrderCountDao.getNCREntriesNotExistingInOrderEntry();
	}

	/**
	 * @return
	 */
	@Override
	public List<RejectionReasonModel> getCancelReasons() {
		return rejectionReasonsDao.getRejectionReasons();
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
	
	@Override
	public Boolean updateTripEndedForDeliveryItem(String orderCode, String entryNumber, String deliveryItemCode, String deliveryLineNumber){
		OrderModel order = getOrderForCode(orderCode);
		int entryNum = Integer.valueOf(entryNumber);
		OrderEntryModel orderEntry = (OrderEntryModel) order.getEntries().stream().filter(abstractOrderEntryModel -> abstractOrderEntryModel.getEntryNumber()==entryNum).findAny().get();
		DeliveryItemModel deliveryItemModel = orderEntry.getDeliveriesItem().stream().filter(i -> i.getDiNumber().equalsIgnoreCase(deliveryItemCode) && i.getDeliveryLineNumber().equalsIgnoreCase(deliveryLineNumber)).findAny().get();
		deliveryItemModel.setTripEnded(true);
		getModelService().save(deliveryItemModel);
		return true;
	}
}
