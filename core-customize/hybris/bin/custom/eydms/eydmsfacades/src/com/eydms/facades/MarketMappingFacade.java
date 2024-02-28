package com.eydms.facades;

import com.eydms.facades.djp.data.CounterMappingData;

public interface MarketMappingFacade {
	
	CounterMappingData addCounter(CounterMappingData counterData, String routeId,String leadId);
}
