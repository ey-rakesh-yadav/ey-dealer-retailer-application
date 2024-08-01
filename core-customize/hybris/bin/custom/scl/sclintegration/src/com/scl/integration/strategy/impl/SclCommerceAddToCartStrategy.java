package com.scl.integration.strategy.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.scl.core.dao.DeliverySlotMasterDao;
import com.scl.core.dao.SclUserDao;
import com.scl.core.enums.CustomerCategory;
import com.scl.core.enums.DeliverySlots;
import com.scl.core.enums.FreightTerms;
import com.scl.core.enums.IncoTerms;
import com.scl.core.enums.IsDealerProvidingTransport;
import com.scl.core.enums.OrderFor;
import com.scl.core.enums.OrderType;
import com.scl.core.enums.SpecialProcessIndicator;
import com.scl.core.model.*;
import com.scl.core.source.dao.DestinationSourceMasterDao;
import com.scl.integration.service.impl.DefaultSclintegrationService;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commerceservices.delivery.DeliveryService;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.commerceservices.service.data.CommerceCartParameter;
import de.hybris.platform.core.model.order.CartEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.ordersplitting.WarehouseService;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.selectivecartservices.strategies.SelectiveCartAddToCartStrategy;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.selectivecartservices.SelectiveCartService;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;


public class SclCommerceAddToCartStrategy extends SelectiveCartAddToCartStrategy{

	private static final Logger LOG = LoggerFactory.getLogger(SclCommerceAddToCartStrategy.class);

	private SelectiveCartService selectiveCartService;

	@Autowired
	EnumerationService enumerationService;
	
	@Autowired
	WarehouseService warehouseService;
	
	@Autowired
	UserService userService;

    @Autowired
    FlexibleSearchService flexibleSearchService;
    
    @Autowired
    SclUserDao sclUserDao;
    
    @Autowired
    DestinationSourceMasterDao destinationSourceMasterDao;
    
    @Autowired
    DeliveryService deliveryService;
    
    @Autowired
    DeliverySlotMasterDao deliverySlotMasterDao;

	protected CartEntryModel addCartEntry(final CommerceCartParameter parameter, final long actualAllowedQuantityChange)
			throws CommerceCartModificationException
	{
		final CartEntryModel cartEntryModel = super.addCartEntry(parameter, actualAllowedQuantityChange);
		cartEntryModel.setTruckNo(parameter.getTruckNo());
		cartEntryModel.setDriverContactNo(parameter.getDriverContactNo());
		if(parameter.getSelectedDeliverySlot()!=null) {
//			cartEntryModel.setExpectedDeliveryslot(DeliverySlots.valueOf(parameter.getSelectedDeliverySlot()));
			cartEntryModel.setExpectedSlot(deliverySlotMasterDao.findByCentreTime(parameter.getSelectedDeliverySlot()));
		}
		if(parameter.getSelectedDeliveryDate()!=null) {
			cartEntryModel.setExpectedDeliveryDate(setSelectedDeliveryDate(parameter.getSelectedDeliveryDate()));
		}
		if(parameter.getCalculatedDeliveryDate()!=null) {
			cartEntryModel.setCalculatedDeliveryDate(setSelectedDeliveryDate(parameter.getCalculatedDeliveryDate()));
		}
		if(parameter.getCalculatedDeliverySlot()!=null) {
//			cartEntryModel.setCalculatedDeliveryslot(DeliverySlots.valueOf(parameter.getCalculatedDeliverySlot()));
			cartEntryModel.setCalculatedSlot(deliverySlotMasterDao.findByCentreTime(parameter.getCalculatedDeliverySlot()));
		}
		if(parameter.getOrderRequisitionId()!=null){
			cartEntryModel.setOrderRequisitionId(parameter.getOrderRequisitionId());
		}
		if(parameter.getProductAliasName()!=null){
			cartEntryModel.setProductAliasName(parameter.getProductAliasName());
		}
		cartEntryModel.setRemarks(parameter.getRemarks());
		cartEntryModel.setSequence(parameter.getSequence());
		cartEntryModel.setQuantityInMT(parameter.getQuantityMT());
		cartEntryModel.setRemainingDiQty(cartEntryModel.getQuantityInMT());
		cartEntryModel.setRemainingQuantity(cartEntryModel.getQuantityInMT());
		if(StringUtils.isNotEmpty(parameter.getPlacedByCustomer())) {
			SclCustomerModel sclCustomerModel;
			UserModel currentUser = userService.getCurrentUser();
				if(cartEntryModel.getOrder().getUser() instanceof SclCustomerModel) {
					sclCustomerModel = (SclCustomerModel) currentUser;
					if (sclCustomerModel.getPartnerCustomer() != null) {
						List<PartnerCustomerModel> partnerCustomerModel = sclCustomerModel.getPartnerCustomer().stream().filter(a -> a.getId().equalsIgnoreCase(parameter.getPlacedByCustomer())).collect(Collectors.toList());
						String partnerName = partnerCustomerModel.get(0).getName();
						cartEntryModel.setPlacedByCustomer(partnerName);
						cartEntryModel.setIsPartnerCustomer(parameter.getIsPartnerCustomer());
					}
					else{
						if(Objects.nonNull(parameter.getPlacedByCustomer()) && Objects.nonNull(userService.getUserForUID(parameter.getPlacedByCustomer()))) {
							sclCustomerModel = (SclCustomerModel) userService.getUserForUID(parameter.getPlacedByCustomer());
							cartEntryModel.setPlacedByCustomer(sclCustomerModel.getName());
							cartEntryModel.setIsPartnerCustomer(parameter.getIsPartnerCustomer());
						}
				   }
				}
			else if(cartEntryModel.getOrder().getUser() instanceof SclUserModel){
					if(Objects.nonNull(parameter.getPlacedByCustomer()) && Objects.nonNull(userService.getUserForUID(parameter.getPlacedByCustomer()))) {
						SclUserModel sclUserModel = (SclUserModel) userService.getUserForUID(parameter.getPlacedByCustomer());
						cartEntryModel.setPlacedByCustomer(sclUserModel.getContactEmail());
						cartEntryModel.setIsPartnerCustomer(Objects.nonNull(parameter.getIsPartnerCustomer()) ? parameter.getIsPartnerCustomer() : Boolean.FALSE);

					}
				}
			}
		if(parameter.getWarehouseCode()!=null) {
			WarehouseModel warehouse = warehouseService.getWarehouseForCode(parameter.getWarehouseCode());
			cartEntryModel.setSource(warehouse);
			if(warehouse!=null){
				cartEntryModel.setSourceType(warehouse.getType());
			}
		}
		if(parameter.getOrderFor()!=null) {
			cartEntryModel.setOrderFor(OrderFor.valueOf(parameter.getOrderFor()));
		}
		if(parameter.getRetailerUid()!=null) {
			cartEntryModel.setRetailer((SclCustomerModel) userService.getUserForUID(parameter.getRetailerUid()));
		}
		if(parameter.getIncoTerm()!=null) {
			SclIncoTermMasterModel sclIncoTerm = destinationSourceMasterDao.findIncoTermByCode(parameter.getIncoTerm());
			if(sclIncoTerm!=null) {
				cartEntryModel.setIncoTerm(sclIncoTerm);
			}
		}
		if(parameter.getDeliveryMode()!=null && parameter.getDeliveryMode().getCode()!=null) {
			final DeliveryModeModel deliveryModeModel = deliveryService.getDeliveryModeForCode(parameter.getDeliveryMode().getCode());
			cartEntryModel.setDeliveryMode(deliveryModeModel);
		}
		//cartEntryModel.setRouteId(parameter.getRouteId());

		if(parameter.getAddressPk()!=null) {
			cartEntryModel.setDeliveryAddress(sclUserDao.getAddressByPk(parameter.getAddressPk()));
		}

		if(parameter.getIsDealerProvidingOwnTransport()!=null) {
			cartEntryModel.setIsDealerProvidingTransport(IsDealerProvidingTransport.valueOf(parameter.getIsDealerProvidingOwnTransport()));
		}
		
		if(cartEntryModel.getProduct()!=null && cartEntryModel.getDeliveryMode()!=null && cartEntryModel.getDeliveryAddress()!=null 
				&& cartEntryModel.getOrder().getSite()!=null && cartEntryModel.getSource()!=null
				&& cartEntryModel.getDeliveryAddress().getTransportationZone()!=null && cartEntryModel.getIncoTerm()!=null) {
			DestinationSourceMasterModel destinationSource =  
					destinationSourceMasterDao.getDestinationSourceBySourceAndSapProductCode(OrderType.SO, CustomerCategory.TR, 
							cartEntryModel.getSource(), cartEntryModel.getDeliveryMode(),
							cartEntryModel.getDeliveryAddress().getTransportationZone(),
							cartEntryModel.getProduct().getCode(), cartEntryModel.getOrder().getSite(), cartEntryModel.getIncoTerm());
			if(destinationSource!=null) {
				if(destinationSource.getDistance()!=null) {
					cartEntryModel.setDistance(destinationSource.getDistance().doubleValue());
				}
				//setting source at order booking & priority
				cartEntryModel.setSourceAtOrderBooking(destinationSource.getSource().getCode());
				cartEntryModel.setSourceRank(StringUtils.isNotBlank(destinationSource.getSourcePriority())?destinationSource.getSourcePriority(): Strings.EMPTY);
				cartEntryModel.setRouteId(destinationSource.getRoute());
				cartEntryModel.setEquivalenceProductCode(destinationSource.getEquivalenceProductCode());
				if(Objects.nonNull(destinationSource.getFreightType())) {
					cartEntryModel.setFreightType(destinationSource.getFreightType());
					FreightSPIMappingModel spi = destinationSourceMasterDao.getSPIFromFreightType(destinationSource.getFreightType());
					if (Objects.nonNull(spi) && Objects.nonNull(spi.getSpi())){
						cartEntryModel.setSpecialProcessIndicator(spi.getSpi());
					}
				}
				
			}
		}		
  		return cartEntryModel;
	}

	private Date setSelectedDeliveryDate(String selectedDeliveryDate) {
		Date date = null;
		try {
			date = new SimpleDateFormat("yyyy-MM-dd").parse(selectedDeliveryDate);
		} catch (ParseException e) {
			LOG.error("Error Parsing Selected Delivery Date", e);
			throw new IllegalArgumentException(String.format("Please provide valid date %s", selectedDeliveryDate));
		} 
		return date;
	}


	protected SelectiveCartService getSelectiveCartService()
	{
		return selectiveCartService;
	}

	@Required
	public void setSelectiveCartService(final SelectiveCartService selectiveCartService)
	{
		this.selectiveCartService = selectiveCartService;
	}
}
