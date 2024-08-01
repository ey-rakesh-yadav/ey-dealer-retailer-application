package com.scl.facades.delivery.impl;

import java.util.Collection;

import javax.annotation.Resource;

import com.scl.facades.delivery.DeliveryModesFacade;

import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.commercefacades.order.data.DeliveryModeData;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.order.DeliveryModeService;


public class DeliveryModesFacadeImpl implements DeliveryModesFacade
{	
	
	
	@Resource
	private Converter<DeliveryModeModel, DeliveryModeData> deliveryModeConverter;

	@Resource
	DeliveryModeService deliveryModeService;
	
	@Override
	public Collection<DeliveryModeData> getAllDeliveryModes()
	{
		Collection<DeliveryModeModel> deliveryModeModel =deliveryModeService.getAllDeliveryModes();
		Collection<DeliveryModeData> deliveryModeData=deliveryModeConverter.convertAll(deliveryModeModel);
		return deliveryModeData;
	}

}