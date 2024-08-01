package com.scl.facades;

import com.scl.facades.djp.data.CounterMappingData;

public interface MarketMappingFacade {
	
	CounterMappingData addCounter(CounterMappingData counterData, String routeId,String leadId);
}
