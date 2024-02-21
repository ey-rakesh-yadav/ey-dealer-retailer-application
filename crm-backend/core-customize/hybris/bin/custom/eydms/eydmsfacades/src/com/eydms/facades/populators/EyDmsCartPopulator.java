package com.eydms.facades.populators;

import org.springframework.beans.factory.annotation.Autowired;

import de.hybris.platform.commercefacades.order.converters.populator.AbstractOrderPopulator;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.warehousingfacades.storelocator.data.WarehouseData;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class EyDmsCartPopulator<T extends CartData> extends AbstractOrderPopulator<CartModel, T> {

	private EnumerationService enumerationService;

	@Autowired
	private Converter<WarehouseModel, WarehouseData> warehousingWarehouseConverter;

	private Converter<AbstractOrderEntryModel, OrderEntryData> orderEntryConverter;

	@Override
	public void populate(CartModel source, T target) throws ConversionException {
		if(source.getOrderType()!=null) {
			target.setOrderType(source.getOrderType().getCode());	
		}
		if(source.getSubAreaMaster()!=null) {
        	target.setSubAreaCode(source.getSubAreaMaster().getPk().toString());
        }
		target.setIsDealerProvideOwnTransport(source.getIsDealerProvideOwnTransport());
		if(source.getWarehouse()!=null) {
			WarehouseData warehouse = new WarehouseData();
			warehouse.setCode(source.getWarehouse().getCode());
			target.setWarehouse(warehouse);
		}
		if(source.getDestination()!=null) {
			target.setDestination(source.getDestination().getCode());
			target.setDestinationName(source.getDestination().getName());
		}
		target.setProductName(source.getProductName());
		target.setProductCode(source.getProductCode());
		target.setTotalQuantity(source.getTotalQuantity());
		if(null != source.getRetailer()){
			target.setRetailerName(source.getRetailer().getName());
			target.setRetailerCode(source.getRetailer().getUid());
		}
		if (source.getRequestedDeliveryslot() != null) {
			target.setRequestedDeliverySlot(source.getRequestedDeliveryslot().getCode());
			target.setRequestDeliverySlotName(enumerationService.getEnumerationName(source.getRequestedDeliveryslot()));
		}

		if (source.getRequestedDeliveryDate() != null) {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			target.setRequestedDeliveryDate(dateFormat.format(source.getRequestedDeliveryDate()));
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
