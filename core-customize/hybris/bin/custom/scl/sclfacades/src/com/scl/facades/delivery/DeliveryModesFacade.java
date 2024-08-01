package com.scl.facades.delivery;

import java.util.Collection;

import de.hybris.platform.commercefacades.order.data.DeliveryModeData;


public interface DeliveryModesFacade
{
	Collection<DeliveryModeData> getAllDeliveryModes();
}