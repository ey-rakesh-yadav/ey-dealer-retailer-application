package com.eydms.integration.strategy.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eydms.core.enums.DeliverySlots;
import com.eydms.core.enums.FreightTerms;
import com.eydms.core.enums.IncoTerms;
import com.eydms.core.model.FreightAndIncoTermsMasterModel;
import com.eydms.integration.service.impl.DefaultEyDmsintegrationService;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.commerceservices.service.data.CommerceCartParameter;
import de.hybris.platform.core.model.order.CartEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.ordersplitting.WarehouseService;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.selectivecartservices.strategies.SelectiveCartAddToCartStrategy;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.selectivecartservices.SelectiveCartService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;


public class EyDmsCommerceAddToCartStrategy extends SelectiveCartAddToCartStrategy{

	private static final Logger LOG = LoggerFactory.getLogger(EyDmsCommerceAddToCartStrategy.class);

	private SelectiveCartService selectiveCartService;

	@Autowired
	EnumerationService enumerationService;
	
	@Autowired
	WarehouseService warehouseService;
	
    
    @Autowired
    FlexibleSearchService flexibleSearchService;

	protected CartEntryModel addCartEntry(final CommerceCartParameter parameter, final long actualAllowedQuantityChange)
			throws CommerceCartModificationException
	{
		final CartEntryModel cartEntryModel = super.addCartEntry(parameter, actualAllowedQuantityChange);
		cartEntryModel.setTruckNo(parameter.getTruckNo());
		cartEntryModel.setDriverContactNo(parameter.getDriverContactNo());
		if(parameter.getSelectedDeliverySlot()!=null) {
			cartEntryModel.setExpectedDeliveryslot(DeliverySlots.valueOf(parameter.getSelectedDeliverySlot()));
		}
		if(parameter.getSelectedDeliveryDate()!=null) {
			cartEntryModel.setExpectedDeliveryDate(setSelectedDeliveryDate(parameter.getSelectedDeliveryDate()));
		}
		if(parameter.getCalculatedDeliveryDate()!=null) {
			cartEntryModel.setCalculatedDeliveryDate(setSelectedDeliveryDate(parameter.getCalculatedDeliveryDate()));
		}
		if(parameter.getCalculatedDeliverySlot()!=null) {
			cartEntryModel.setCalculatedDeliveryslot(DeliverySlots.valueOf(parameter.getCalculatedDeliverySlot()));
		}
		cartEntryModel.setRemarks(parameter.getRemarks());
		cartEntryModel.setSequence(parameter.getSequence());
		cartEntryModel.setQuantityInMT(parameter.getQuantityMT());
		if(parameter.getWarehouseCode()!=null) {
			WarehouseModel warehouse = warehouseService.getWarehouseForCode(parameter.getWarehouseCode());
			cartEntryModel.setSource(warehouse);
			if(warehouse!=null){
				cartEntryModel.setSourceType(warehouse.getType());
			}
		}
		//cartEntryModel.setRouteId(parameter.getRouteId());

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
