package com.scl.facades.populators;

import org.springframework.beans.factory.annotation.Autowired;

import de.hybris.platform.commercefacades.order.data.OrderHistoryData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;

public class SclOrderHistoryPopulator implements Populator<OrderModel, OrderHistoryData> {

	@Autowired
	Converter<AddressModel, AddressData> addressConverter;
	
	@Override
	public void populate(OrderModel source, OrderHistoryData target) throws ConversionException {
		
		target.setCounterCode(source.getUser().getUid());
		target.setCounterName(source.getUser().getName());
		target.setDeliveryAddress(addressConverter.convert(source.getDeliveryAddress()));
		//target.setEntryNumber(source.getEntries().get(0).getEntryNumber());
		//target.setProductCode(source.getEntries().get(0).getProduct().getCode());
		//target.setProductName(source.getEntries().get(0).getProduct().getName());
		//target.setQuantity(source.getEntries().get(0).getQuantity());
		target.setRejectionReason(source.getRejectionReason());

		//target.setTransporterName(source.getTransporterName());
		//target.setTruckNumber(source.getTruckNo());
		//target.setDriverName(source.getDriverName());
		
	}

}
