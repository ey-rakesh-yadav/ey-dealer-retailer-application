package com.scl.facades.delivery.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Resource;

import com.scl.core.enums.DeliverySlots;
import com.scl.facades.delivery.DeliverySlotsFacade;

import de.hybris.platform.enumeration.EnumerationService;

public class DeliverySlotsFacadeImpl implements DeliverySlotsFacade 
{
	@Resource
	EnumerationService enumerationService;
	
	@Override
	public Map<DeliverySlots,String> getAllDeliverySlots() {
		
		List<DeliverySlots> deliverySlots = enumerationService.getEnumerationValues(com.scl.core.enums.DeliverySlots.class);
		Map<DeliverySlots,String> deliverySlotsList = new HashMap<>();
				
		for(DeliverySlots deliverySlot: deliverySlots )
		{
			deliverySlotsList.put(deliverySlot,enumerationService.getEnumerationName(deliverySlot));	
		}
		
		return Objects.nonNull(deliverySlotsList) ? deliverySlotsList : Collections.emptyMap();
		
	}

}
