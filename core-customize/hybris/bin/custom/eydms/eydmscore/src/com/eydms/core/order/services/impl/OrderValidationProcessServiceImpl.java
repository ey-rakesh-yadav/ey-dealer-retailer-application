package com.eydms.core.order.services.impl;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import com.eydms.core.enums.*;
import com.eydms.core.event.SendSMSEvent;
import com.eydms.core.model.*;
import com.eydms.core.notifications.service.EyDmsNotificationService;
import com.eydms.core.order.services.EyDmsOrderService;
import com.eydms.core.services.TerritoryManagementService;
import com.eydms.core.services.impl.EyDmsWorkflowServiceImpl;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.event.EventService;
import de.hybris.platform.servicelayer.time.TimeService;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.jayway.jsonpath.Option;
import com.eydms.core.cart.dao.EyDmsSalesOrderDeliverySLADao;
import com.eydms.core.dao.DJPVisitDao;
import com.eydms.core.dao.DeliverySlotMasterDao;
import com.eydms.core.dao.OrderRequisitionDao;
import com.eydms.core.order.EYDMSB2BOrderService;
import com.eydms.core.order.dao.OrderValidationProcessDao;
import com.eydms.core.order.services.OrderValidationProcessService;
import com.eydms.core.source.dao.DestinationSourceMasterDao;

import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.util.Config;

public class OrderValidationProcessServiceImpl implements OrderValidationProcessService
{
	private static final Logger LOG = Logger.getLogger(OrderValidationProcessServiceImpl.class);

	private OrderValidationProcessDao orderValidationProcessDao;
	private ModelService modelService;

	@Autowired
	EYDMSB2BOrderService b2bOrderService;

	EnumerationService enumerationService;

	@Autowired
	DestinationSourceMasterDao destinationSourceMasterDao;

	@Autowired
	private DeliverySlotMasterDao deliverySlotMasterDao;

	@Autowired
	private EyDmsSalesOrderDeliverySLADao eydmsSalesOrderDeliverySLADao;

	@Autowired
	private ProductService productService;

	@Autowired
	CatalogVersionService catalogVersionService;

	@Autowired
	DJPVisitDao djpVisitDao;

	@Autowired
	EyDmsNotificationService eydmsNotificationService;

	@Autowired
	ConfigurationService configurationService;

	@Autowired
	EventService eventService;

	@Autowired
	EyDmsOrderService eydmsOrderService;

	@Autowired
	TimeService timeService;

	@Autowired
	EyDmsWorkflowServiceImpl eydmsWorkflowService;

	@Autowired
	UserService userService;

	@Autowired
	TerritoryManagementService territoryManagementService;

	@Override
	public boolean validateOrder(OrderModel order) {

		final String subject = "Credit Limit Breached";

		NotificationCategory category = NotificationCategory.ORDER_CREDIT_LIMIT_BREACHED;


		if(order.getCrmOrderType()==null || !order.getCrmOrderType().equals(CRMOrderType.GIFT)) {
			boolean isFirstTime = order.getOrderValidatedDate()==null && order.getOrderFailedValidationDate()==null;
			List<String> rejectionReasons = new ArrayList<>();
			Map<String,String> suggestions = new HashMap<>();
			boolean isCreditBreached  = false;
			double creditBreachedPercentage = 0.0;
			boolean isMaxOrderQtyBreached  = false;
			double maxOrderQtyBreachedPercentage  = 0.0;
			boolean isL1SourceBreached  = false;
			String sourcePriority = "";
			boolean isDeliveryWindowSLABreached  = false;

			if(order.getWarehouse()==null) {
				rejectionReasons.add("Source not populated");
			}

			if(order.getEntries()==null) {
				rejectionReasons.add("Delivery Window not populated");
			}

			if(order.getDeliveryAddress()==null) {
				rejectionReasons.add("Delivery Address not populated");
			}

			if(order.getDeliveryMode()==null) {
				rejectionReasons.add("Delivery Mode not populated");
			}

			String suggestion = Config.getString("order.validation.suggestion",StringUtils.EMPTY);

			if(order.getProductCode()!=null) {
				catalogVersionService.setSessionCatalogVersion(order.getSite().getUid() + "ProductCatalog", "Online");
				CatalogVersionModel version = catalogVersionService.getCatalogVersion(order.getSite().getUid() + "ProductCatalog", "Online");
				ProductModel product = productService.getProductForCode(version,order.getProductCode());
				String grade = product.getGrade();
				String packaging= product.getBagType();

				List<DestinationSourceMasterModel> allPossibleSource = destinationSourceMasterDao.findDestinationSourceByCode(order.getDeliveryAddress().getErpCity(), order.getDeliveryMode(), order.getOrderType(), CustomerCategory.TR ,grade, packaging, order.getDeliveryAddress().getDistrict(), order.getDeliveryAddress().getState(), order.getSite(), order.getDeliveryAddress().getTaluka());

				if(allPossibleSource==null || allPossibleSource.isEmpty()) {
					rejectionReasons.add("Source not found for selected delivery address");
				}

				List<DestinationSourceMasterModel> source = destinationSourceMasterDao.findL1Source(order.getDeliveryAddress().getErpCity(), order.getDeliveryMode(), order.getOrderType(), CustomerCategory.TR ,grade, packaging, order.getDeliveryAddress().getDistrict(), order.getDeliveryAddress().getState(), order.getSite(), order.getDeliveryAddress().getTaluka());

				if(source==null || source.isEmpty()) {
					rejectionReasons.add(String.format("L1 source not found for city %s", order.getDeliveryAddress().getErpCity()));
				}
				else if(order.getWarehouse()!=null && source.get(0).getSource()!=null && !source.get(0).getSource().getCode().equals(order.getWarehouse().getCode()))
				{
					rejectionReasons.add(Config.getString("order.validation.rejection.reason.source",StringUtils.EMPTY));
					suggestions.put("source", Config.getString("order.validation.rejection.suggestion.source",StringUtils.EMPTY));
					isL1SourceBreached = true;
					Optional<DestinationSourceMasterModel> selectedSourceOpt = allPossibleSource.stream().filter(source1 -> source1.getSource()!=null && source1.getSource().getCode().equals(order.getWarehouse().getCode())).findAny();
					if(selectedSourceOpt.isPresent()) {
						DestinationSourceMasterModel selectedSource = selectedSourceOpt.get();
						sourcePriority = selectedSource.getSourcePriority();
					}
				}
			}
			else {
				rejectionReasons.add("Product not populated");
			}

			if(order.getTotalQuantity()==null) {
				rejectionReasons.add("Totat quantity not populated");
			}

			if(order.getRouteId()!=null) {
				SalesOrderDeliverySLAModel deliverySlaModel = eydmsSalesOrderDeliverySLADao.findByRoute(order.getRouteId());
				if(deliverySlaModel==null) {
					rejectionReasons.add(String.format("Route not found %s", order.getRouteId()));
				}
			}
			else {
				rejectionReasons.add("Route not populated");
			}

			for(AbstractOrderEntryModel entry : order.getEntries()) {
				if(entry.getExpectedDeliveryDate()!=null && entry.getCalculatedDeliveryDate()!=null) {
					LocalDate expectedDeliveryDate = entry.getExpectedDeliveryDate().toInstant()
							.atZone(ZoneId.systemDefault())
							.toLocalDate();

					LocalDate calculatedDeliveryDate = entry.getCalculatedDeliveryDate().toInstant()
							.atZone(ZoneId.systemDefault())
							.toLocalDate();

					if(expectedDeliveryDate.isBefore(calculatedDeliveryDate)) {
						rejectionReasons.add(Config.getString("order.validation.rejection.reason.deliveryWindow",StringUtils.EMPTY));
						suggestions.put("deliveryWindow", Config.getString("order.validation.rejection.suggestion.deliveryWindow",StringUtils.EMPTY));
						isDeliveryWindowSLABreached = true;
						try {
							EyDmsUserModel so = territoryManagementService.getSOforCustomer((EyDmsCustomerModel) order.getUser());
							StringBuilder builder = new StringBuilder();
							Double orderQty = order.getEntries().stream().collect(Collectors.summingDouble(each -> each.getQuantityInMT()));
							builder.append("Order number "+ order.getCode() + " of product "+ entry.getProduct().getName() + " with quantity " + orderQty);
							builder.append(" in the name of "+ order.getUser().getName());
							//builder.append(", cannot be delivered with the given window. Kindly change source to be able to meet the SLA or change the source to one with the earliest SLA");
							builder.append(", cannot be delivered with the given window. Kindly contact your DO "+ so.getName());
							String body = builder.toString();
							String sub ="Delivery window SLA Breached";
							eydmsNotificationService.submitOrderNotification(order,so,body,sub,NotificationCategory.DELIVERY_WINDOW_NOT_MET);
							eydmsNotificationService.submitOrderNotification(order, (B2BCustomerModel) order.getUser(),body,sub,NotificationCategory.DELIVERY_WINDOW_NOT_MET);
						}
						catch(Exception e) {
							LOG.error("Error while sending delivery sla breach notification");
						}
						break;
					}
					if(expectedDeliveryDate.isEqual(calculatedDeliveryDate)) {
						int expectedSlot = deliverySlotMasterDao.findByEnum(entry.getExpectedDeliveryslot().getCode()).getSequence();
						int calculatedSlot = deliverySlotMasterDao.findByEnum(entry.getCalculatedDeliveryslot().getCode()).getSequence();
						if(calculatedSlot>expectedSlot) {
							rejectionReasons.add(Config.getString("order.validation.rejection.reason.deliveryWindow",StringUtils.EMPTY));
							suggestions.put("deliveryWindow", Config.getString("order.validation.rejection.suggestion.deliveryWindow",StringUtils.EMPTY));
							isDeliveryWindowSLABreached = true;
							try {
								EyDmsUserModel so = territoryManagementService.getSOforCustomer((EyDmsCustomerModel) order.getUser());
								StringBuilder builder = new StringBuilder();
								Double orderQty = order.getEntries().stream().collect(Collectors.summingDouble(each -> each.getQuantityInMT()));
								builder.append("Order number "+ order.getCode() + " of product "+ entry.getProduct().getName() + " with quantity " + orderQty);
								builder.append(" in the name of "+ order.getUser().getName());
								//builder.append(", cannot be delivered with the given window. Kindly change source to be able to meet the SLA or change the source to one with the earliest SLA");
								builder.append(", cannot be delivered with the given window. Kindly contact your DO "+ so.getName());
								String body = builder.toString();
								String sub ="Delivery window SLA Breached";

								eydmsNotificationService.submitOrderNotification(order, (B2BCustomerModel) order.getUser(),body,sub,NotificationCategory.DELIVERY_WINDOW_NOT_MET);
								eydmsNotificationService.submitOrderNotification(order,so,body,sub,NotificationCategory.DELIVERY_WINDOW_NOT_MET);
							}
							catch(Exception e) {
								LOG.error("Error while sending delivery sla breach notification");
							}
							break;
						}
					}

				}
			}
			if(isDeliveryWindowSLABreached) {
				order.setIsSLABreached(isDeliveryWindowSLABreached);
			}
//			if(order.getTotalPrice()!=null) {
			Double totalCreditLimit = djpVisitDao.getDealerCreditLimit(((EyDmsCustomerModel)order.getUser()).getCustomerNo());
			Double currentOutstanding  = djpVisitDao.getDealerOutstandingAmount(((EyDmsCustomerModel)order.getUser()).getCustomerNo());
			Double orderAmount = order.getTotalPrice()!=null ? order.getTotalPrice() : 0.0;
			Double pendingOrderAmount = getOrderValidationProcessDao().getPendingOrderAmount(order.getUser().getPk().toString());

			double checkAmount = currentOutstanding+orderAmount+pendingOrderAmount;
			if((checkAmount)>totalCreditLimit)
			{
				String exceedAmount = new DecimalFormat("##.##").format((checkAmount) - totalCreditLimit);
				rejectionReasons.add(String.format(Config.getString("order.validation.rejection.reason.credit",StringUtils.EMPTY), exceedAmount));
				Double billingPrice = order.getEntries().get(0).getBasePrice()!=null?order.getEntries().get(0).getBasePrice():0.0;//eydmsOrderService.getBillingPriceForProduct(order.getSite(), product.getInventoryId(), order.getDeliveryAddress().getErpCity(), CustomerCategory.TR, product.getBagType(), product.getState());
				int qty = 0 ;

				if(billingPrice!=null && billingPrice>0) {
					qty = (int) (((checkAmount) - totalCreditLimit)/billingPrice);
				}
				if(((checkAmount) - totalCreditLimit)<billingPrice) {
					suggestions.put("credit", String.format(Config.getString("order.validation.rejection.suggestion.credit",StringUtils.EMPTY), qty, exceedAmount));
				}
				else {
					suggestions.put("credit", String.format(Config.getString("order.validation.rejection.suggestion.second.credit",StringUtils.EMPTY), exceedAmount));

				}
				isCreditBreached = true;
				creditBreachedPercentage = (checkAmount/totalCreditLimit ) * 100;
				order.setCreditLimitBreached(true);

				if(isFirstTime) {
					if(((EyDmsCustomerModel)order.getUser()).getIsCreditLimitBreached()==null || !((EyDmsCustomerModel)order.getUser()).getIsCreditLimitBreached()) {
						((EyDmsCustomerModel)order.getUser()).setIsCreditLimitBreached(Boolean.TRUE);
						((EyDmsCustomerModel)order.getUser()).setCreditLimitBreachedDate(new Date());
						modelService.save(order.getUser());
					}

					//Call Credit Limit Breach In App Notification
					//eydmsNotificationService.sendInAppNotificationForCreditLimitBreach(order,totalCreditLimit,currentOutstanding,orderAmount,pendingOrderAmount);
					try {
						B2BCustomerModel user = null;
						String amount = exceedAmount;
						String formattedAmount = formatIndianNumber(Double.parseDouble(amount));


						Double orderQty = order.getEntries().stream().collect(Collectors.summingDouble(each -> each.getQuantityInMT()));
						StringBuilder builder = new StringBuilder();

						builder.append("Order number "+ order.getCode() + " with product "+ order.getEntries().get(0).getProduct().getName() + " of quantity " + orderQty);
						builder.append(" MT has exceeded the credit limit by ₹"+ formattedAmount + " for "+ order.getUser().getUid());


						String body = builder.toString();

						eydmsNotificationService.submitOrderNotification(order, (B2BCustomerModel) order.getUser(),body,subject,category);

						StringBuilder builder1 = new StringBuilder();
						EyDmsUserModel so = territoryManagementService.getSOforCustomer((EyDmsCustomerModel) order.getUser());
						builder1.append("Order number "+ order.getCode() + " with product "+ order.getEntries().get(0).getProduct().getName() + " of quantity " + orderQty);
						builder1.append(" MT has exceeded the credit limit by ₹"+ formattedAmount + " for "+ so.getUid());

						String body1 = builder1.toString();

						eydmsNotificationService.submitOrderNotification(order,so,body1,subject,category);

						StringBuilder builder2 = new StringBuilder();
						EyDmsCustomerModel sp = territoryManagementService.getSpForCustomerAndBrand((EyDmsCustomerModel) order.getUser(),order.getSite());
						builder2.append("Order number "+ order.getCode() + " with product "+ order.getEntries().get(0).getProduct().getName() + " of quantity " + orderQty);
						builder2.append(" MT has exceeded the credit limit by ₹"+ formattedAmount + " for "+ sp.getUid());

						String body2 = builder2.toString();

						eydmsNotificationService.submitOrderNotification(order,sp,body2,subject,category);
					}
					catch(Exception e) {
						LOG.error("Error while sending credit limit breach notification");
					}
					//Trigger SMS for Credit Limit Breach
					//sendSmsForOrderCreditLimitBreach(order,totalCreditLimit,currentOutstanding,orderAmount,pendingOrderAmount);
				}
			}
//			}
//			else {
//				rejectionReasons.add("Price Processing");
//			}
			//validateMaxQuatity(order, rejectionReasons, suggestions);

			//boolean isOrderValidationFailed  = updateApprovalDetail(isFirstTime, order, isCreditBreached, isMaxOrderQtyBreached, isL1SourceBreached, creditBreachedPercentage, maxOrderQtyBreachedPercentage, sourcePriority, isDeliveryWindowSLABreached);
			//if(!(rejectionReasons.isEmpty()) && isOrderValidationFailed)
			if(!(rejectionReasons.isEmpty()))
			{
				order.setRejectionReasons(rejectionReasons);
				order.setStatus(OrderStatus.ORDER_FAILED_VALIDATION);
				Date orderFailedValidationDate = new Date();
				order.setOrderFailedValidationDate(orderFailedValidationDate);
				order.setSuggestions(suggestions);

				for(int i=0;i<order.getEntries().size();i++) {
					OrderEntryModel entry = (OrderEntryModel) order.getEntries().get(i);
					entry.setStatus(OrderStatus.ORDER_FAILED_VALIDATION);
					getModelService().save(entry);
				}
				getModelService().save(order);
				getModelService().refresh(order);
				return false;
			}
			else if(isFirstTime){
				order.setStatus(OrderStatus.ORDER_VALIDATED);
				Date orderValidationDate = new Date();
				order.setOrderValidatedDate(orderValidationDate);

				for(int i=0;i<order.getEntries().size();i++) {
					OrderEntryModel entry = (OrderEntryModel) order.getEntries().get(i);
					entry.setStatus(OrderStatus.ORDER_VALIDATED);
					getModelService().save(entry);
				}
				getModelService().save(order);
				getModelService().refresh(order);
			}
		}
		return true;
	}

	private void sendSmsForOrderCreditLimitBreach(OrderModel order, Double totalCreditLimit, Double currentOutstanding, Double orderAmount, Double pendingOrderAmount) {
		B2BCustomerModel user = (B2BCustomerModel) order.getUser();
		String mobileNumber = user.getMobileNumber();
		String messageContent = String.format( (configurationService.getConfiguration().getString("order.credit.limit.breach.message")),order.getUser().getName().concat("/").concat(((EyDmsCustomerModel)order.getUser()).getCustomerNo()),order.getTotalPrice().toString(),totalCreditLimit.toString(),(String.valueOf(Math.abs(totalCreditLimit - (currentOutstanding + orderAmount + pendingOrderAmount)))) );
		final SMSProcessModel process = new SMSProcessModel();
		process.setNumber(mobileNumber);
		//process.setTemplateId("1407165848067556857");
		process.setMessageContent(messageContent);
		final SendSMSEvent event = new SendSMSEvent(process);
		eventService.publishEvent(event);
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

	public OrderValidationProcessDao getOrderValidationProcessDao() {
		return orderValidationProcessDao;
	}

	public void setOrderValidationProcessDao(OrderValidationProcessDao orderValidationProcessDao) {
		this.orderValidationProcessDao = orderValidationProcessDao;
	}

	public ModelService getModelService() {
		return modelService;
	}

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	public EnumerationService getEnumerationService() {
		return enumerationService;
	}

	public void setEnumerationService(EnumerationService enumerationService) {
		this.enumerationService = enumerationService;
	}

	private int getDICountForOrder(final SalesOrderDeliverySLAModel salesOrderDeliverySLA , int orderQty) {
		double truckCapacity = 50;
		if(salesOrderDeliverySLA.getCommonTruckCapacity()>0) {
			truckCapacity =salesOrderDeliverySLA.getCommonTruckCapacity();
			return (int) Math.ceil((double)orderQty/(double)truckCapacity);
		}
		return 0;
	}

	private boolean updateApprovalDetail(boolean isFirstTime, OrderModel order, boolean isCreditBreached, boolean isMaxOrderQtyBreached, boolean isL1SourceBreached,
										 double creditBreachedPercentage, double maxOrderQtyBreachedPercentage, String sourcePriority, boolean isDeliveryWindowSLABreached) {
		if(isCreditBreached || isMaxOrderQtyBreached || isL1SourceBreached || isDeliveryWindowSLABreached){
			boolean state = false, region = false, district = false;
			if(isCreditBreached){
//				if(creditBreachedPercentage>175){
//					state = true;
//				}
//				else if(creditBreachedPercentage>150){
				if(creditBreachedPercentage>150){	
					region = true;
				}
				else if(creditBreachedPercentage>120){
					district = true;
				}
			}

			if(isMaxOrderQtyBreached){
//				if(maxOrderQtyBreachedPercentage>50){
//					state = true;
//				}
//				else if(maxOrderQtyBreachedPercentage>25){
				if(maxOrderQtyBreachedPercentage>25){	
					region = true;
				}
				else if(maxOrderQtyBreachedPercentage>10){
					district = true;
				}
			}

			if(isL1SourceBreached && sourcePriority!=null){
				int priority = Integer.valueOf(sourcePriority.substring(1));
				if(priority>3){
					region = true;
				}
				else if(priority==3){
					district = true;
				}
			}
			int maxApprovelLevelNumber = 1;
			if(state) {
				maxApprovelLevelNumber = 4;
			}
			else if(region) {
				maxApprovelLevelNumber = 3;
			}
			else if(district) {
				maxApprovelLevelNumber = 2;
			}
			int approvedLevel = getApprovedLevel(order.getApprovedLevel());
			if(!isFirstTime && maxApprovelLevelNumber<=approvedLevel) {
				order.setStatus(OrderStatus.APPROVED);
				order.setOrderSentForApprovalDate(new Date());

				for(int i=0;i<order.getEntries().size();i++) {
					OrderEntryModel entry = (OrderEntryModel) order.getEntries().get(i);
					entry.setStatus(OrderStatus.APPROVED);
					getModelService().save(entry);
					getModelService().refresh(entry);
				}

				getModelService().save(order);
				getModelService().refresh(order);
				return false;
			}
			else {
				TerritoryLevels approvalLevel = getNextApprovedLevel(order.getApprovedLevel());
				order.setApprovalLevel(approvalLevel);

				EyDmsWorkflowModel approvalWorkflowModel = null;
				if(order.getApprovalWorkflow()==null)
				{
					approvalWorkflowModel= eydmsWorkflowService.saveWorkflow("ORDER_APPROVAL_WORKFLOW", WorkflowStatus.START, WorkflowType.ORDER);
					order.setApprovalWorkflow(approvalWorkflowModel);
					modelService.save(order);
					getModelService().refresh(order);
				}
				else
				{
					approvalWorkflowModel = order.getApprovalWorkflow();
				}

				eydmsWorkflowService.saveWorkflowAction(approvalWorkflowModel, "", order.getSite(), order.getSubAreaMaster(), approvalLevel);
				return true;
			}
		}
		else if(!isFirstTime){
			order.setStatus(OrderStatus.APPROVED);
			order.setOrderSentForApprovalDate(new Date());

			for(int i=0;i<order.getEntries().size();i++) {
				OrderEntryModel entry = (OrderEntryModel) order.getEntries().get(i);
				entry.setStatus(OrderStatus.APPROVED);
				getModelService().save(entry);
				getModelService().refresh(entry);
			}

			getModelService().save(order);
			getModelService().refresh(order);
			return false;
		}
		return true;
	}

	private int getApprovedLevel(TerritoryLevels level) {
		if(level!=null) {
			if(level.equals(TerritoryLevels.STATE)) {
				return 4;
			}
			else if(level.equals(TerritoryLevels.REGION)) {
				return 3;
			}
			else if(level.equals(TerritoryLevels.DISTRICT)) {
				return 2;
			}
			else if(level.equals(TerritoryLevels.SUBAREA)) {
				return 1;
			}
			else{
				return 0;
			}
		}
		else {
			return 0;
		}
	}

	private TerritoryLevels getNextApprovedLevel(TerritoryLevels level) {
		if(level!=null) {
			if(level.equals(TerritoryLevels.REGION)) {
				return TerritoryLevels.STATE;
			}
			else if(level.equals(TerritoryLevels.DISTRICT)) {
				return TerritoryLevels.REGION;
			}
			else if(level.equals(TerritoryLevels.SUBAREA)) {
				return TerritoryLevels.DISTRICT;
			}
			else{
				return TerritoryLevels.SUBAREA;
			}
		}
		else {
			return TerritoryLevels.SUBAREA;
		}
	}
	private void validateMaxQuatity(OrderModel order, List<String> rejectionReasons, Map<String,String> suggestions) {
		//		Map<Date, List<AbstractOrderEntryModel>> map = order.getEntries().stream().collect(Collectors.groupingBy(entry->entry.getExpectedDeliveryDate()));
		//		ProductModel product = order.getEntries().get(0).getProduct();
		//		double maxInvoicedQty = orderValidationProcessDao.getMaxQuantityThreshold(order.getSite(), (EyDmsCustomerModel)order.getUser(), product);
		//		double minMaxThresholdQty = 100;
		//		double maxInvoicedThresholdQty = maxInvoicedQty + (maxInvoicedQty * 0.1);
		//		double maxThresholdQty = maxInvoicedThresholdQty>minMaxThresholdQty?maxInvoicedThresholdQty:minMaxThresholdQty;
		//		map.forEach((key, value) -> {
		//			Double orderQty = value.stream().collect(Collectors.summingDouble(each -> each.getQuantityInMT()));
		//			double deliveryDateQty = orderValidationProcessDao.getQtyForExpectedDeliveredDate(order.getSite(), (EyDmsCustomerModel)order.getUser(), product, null);
		//			if((deliveryDateQty+ orderQty) > maxThresholdQty) {
		//				rejectionReasons.add("Max Quantity Validation Failed");
		//				break;
		//			}
		//
		//		});

		ProductModel product = order.getEntries().get(0).getProduct();
		double maxInvoicedQty = orderValidationProcessDao.getMaxQuantityThreshold(order.getSite(), (EyDmsCustomerModel)order.getUser(), product);
		double minMaxThresholdQty = 100;
		double maxInvoicedThresholdQty = maxInvoicedQty + (maxInvoicedQty * 0.1);
		double maxThresholdQty = maxInvoicedThresholdQty>minMaxThresholdQty?maxInvoicedThresholdQty:minMaxThresholdQty;
		Double orderQty = order.getEntries().stream().collect(Collectors.summingDouble(each -> each.getQuantityInMT()));
		double deliveryDateQty = orderValidationProcessDao.getQtyForExpectedDeliveredDate(order.getSite(), (EyDmsCustomerModel)order.getUser(), product, order.getEntries().get(0).getExpectedDeliveryDate());
		if((deliveryDateQty+ orderQty) > maxThresholdQty) {
			rejectionReasons.add(String.format(Config.getString("order.validation.rejection.reason.quantity",StringUtils.EMPTY),product.getName()));
			double delta =(deliveryDateQty+ orderQty) - maxThresholdQty;
			if(delta>orderQty) {
				suggestions.put("quantity", Config.getString("order.validation.rejection.suggestion.quantity1",StringUtils.EMPTY));

			}else {
				suggestions.put("quantity", String.format(Config.getString("order.validation.rejection.suggestion.quantity",StringUtils.EMPTY),delta));
			}
		}
	}

}