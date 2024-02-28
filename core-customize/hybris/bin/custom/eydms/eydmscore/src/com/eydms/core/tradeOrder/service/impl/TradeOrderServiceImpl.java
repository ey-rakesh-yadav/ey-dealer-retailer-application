package com.eydms.core.tradeOrder.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.annotation.Resource;

import com.eydms.core.enums.OrderType;
import com.eydms.core.tradeOrder.service.TradeOrderService;

import de.hybris.platform.enumeration.EnumerationService;

public class TradeOrderServiceImpl implements TradeOrderService {
	
	@Resource
	EnumerationService enumerationService;

	@Override
	public List<OrderType> listAllTradeOrderType() {
			
			List<OrderType> tradeOrderList = enumerationService.getEnumerationValues(com.eydms.core.enums.OrderType.class);
		return Objects.nonNull(tradeOrderList) ? tradeOrderList : Collections.emptyList();
		
	}

}
