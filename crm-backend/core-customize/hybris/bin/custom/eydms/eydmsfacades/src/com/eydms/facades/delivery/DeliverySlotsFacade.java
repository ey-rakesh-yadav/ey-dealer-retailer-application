package com.eydms.facades.delivery;

import java.util.List;
import java.util.Map;

import com.eydms.core.enums.DeliverySlots;


public interface DeliverySlotsFacade
{
	Map<DeliverySlots, String> getAllDeliverySlots();
}
