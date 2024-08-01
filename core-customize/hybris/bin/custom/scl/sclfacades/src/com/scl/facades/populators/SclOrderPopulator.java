package com.scl.facades.populators;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.enums.TerritoryLevels;
import com.scl.core.model.OrderRequisitionModel;
import com.scl.core.model.SclUserModel;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.model.SclCustomerModel;

import de.hybris.platform.commercefacades.order.converters.populator.AbstractOrderPopulator;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.DeliveryModeData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.data.PriceData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.PrincipalData;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.warehousingfacades.storelocator.data.WarehouseData;

public class SclOrderPopulator<T extends CartData> extends AbstractOrderPopulator<OrderModel, T> {

	private EnumerationService enumerationService;
	
	private Converter<AbstractOrderEntryModel, OrderEntryData> orderEntryConverter;
	
	@Autowired
	private Converter<AddressModel, AddressData> addressConverter;
	
	@Autowired
	private Converter<DeliveryModeModel, DeliveryModeData> deliveryModeConverter;

	@Autowired
	UserService userService;


	@Override
	public void populate(OrderModel source, T target) throws ConversionException {
		if (source.getOrderType() != null) {
			target.setOrderType(source.getOrderType().getCode());
		}
		
		if (source.getSubAreaMaster() != null) {
			target.setSubAreaCode(source.getSubAreaMaster().getPk().toString());
		}
//		target.setTotalQuantity(source.getTotalQuantity());
		
//		if (source.getStatus() != null && source.getRejectionReasons() != null && (source.getStatus().equals(OrderStatus.ORDER_SENT_TO_SO) || source.getStatus().equals(OrderStatus.ORDER_RECEIVED) || source.getStatus().equals(OrderStatus.ORDER_FAILED_VALIDATION) || source.getStatus().equals(OrderStatus.ORDER_MODIFIED))) {
//			target.setRejectionReasons(source.getRejectionReasons().stream().collect(Collectors.toList()));
//			target.setSuggestionMap(source.getSuggestions());
//		}
		if (source.getEntries() != null) {
			addEntries(source, target);
		}

		if (source.getUser() != null) {
			PrincipalData user = new PrincipalData();
			user.setUid(source.getUser().getUid());
			user.setName(source.getUser().getName());
			target.setUser(user);
		}

		if (StringUtils.isNotBlank(source.getErpOrderNumber())) {
			target.setErpOrderNo(String.valueOf(source.getErpOrderNumber()));
		}

		if (source.getOrderAcceptedDate() == null)
			target.setUiStatus("PENDING_FOR_APPROVAL");
		else
			target.setUiStatus("ORDER_ACCEPTED");

		PriceData price = new PriceData();
		price.setValue(BigDecimal.valueOf(source.getTotalPrice()));
		target.setTotalPrice(price);

		target.setStatusDisplay(source.getStatusDisplay());
		target.setBankAccountNo(source.getUser() != null ? ((SclCustomerModel) source.getUser()).getBankAccountNo() : null);

		if (null != source.getPlacedBy()) {
			target.setPlacedBy(source.getPlacedBy().getUid());
		}
		if(null != source.getPlacedByCustomer()){
			target.setPlacedByCustomer(source.getPlacedByCustomer());
		}
		if(null != source.getIsPartnerCustomer()){
			target.setIsPartnerCustomer(source.getIsPartnerCustomer());
		}
		target.setCreditLimitBreached(source.getCreditLimitBreached());
//		target.setIsSLABreached(source.getIsSLABreached());
//		target.setSpApprovalStatus(source.getSpApprovalStatus() != null ? source.getSpApprovalStatus().getCode() : "");
		

//			if (userService.getCurrentUser() instanceof SclUserModel) {
//				SclUserModel currentUser = (SclUserModel) userService.getCurrentUser();
//				if (currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SALES_OFFICER_GROUP_ID))) {
//					target.setShowApprovalButton(true);
//				} else if (currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.TSM_GROUP_ID))) {
//					target.setShowApprovalButton(false);
//				} else if (currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RH_GROUP_ID))) {
//					target.setShowApprovalButton(false);
//				} else {
//					target.setShowApprovalButton(false);
//				}
//			}
//			target.setShowApprovalButton(true);
		if((source.getEntries()!=null && source.getEntries().get(0).getRequisitions()!=null) && !source.getEntries().get(0).getRequisitions().isEmpty()) {
			String requisitionId = "";
			for(OrderRequisitionModel orderRequisitionModel : source.getEntries().get(0).getRequisitions()) {
				if(requisitionId.isEmpty()) {
					requisitionId = orderRequisitionModel.getRequisitionId();
				}
				else {
					requisitionId = requisitionId + "," + orderRequisitionModel.getRequisitionId();
				}
			}
			target.setOrderRequisitionId(requisitionId);
		}

			
	}

	protected void addEntries(final AbstractOrderModel source, final AbstractOrderData prototype)
	{
		prototype.setEntries(getOrderEntryConverter().convertAll(source.getEntries()));
	}

	public EnumerationService getEnumerationService() {
		return enumerationService;
	}

	public void setEnumerationService(EnumerationService enumerationService) {
		this.enumerationService = enumerationService;
	}

	public Converter<AbstractOrderEntryModel, OrderEntryData> getOrderEntryConverter() {
		return orderEntryConverter;
	}

	public void setOrderEntryConverter(Converter<AbstractOrderEntryModel, OrderEntryData> orderEntryConverter) {
		this.orderEntryConverter = orderEntryConverter;
	}
	
	
}
