package com.scl.facades.delivery;

import java.util.List;
import java.util.Map;

import com.scl.core.enums.DeliverySlots;


public interface DeliverySlotsFacade
{
	Map<DeliverySlots, String> getAllDeliverySlots();
}
