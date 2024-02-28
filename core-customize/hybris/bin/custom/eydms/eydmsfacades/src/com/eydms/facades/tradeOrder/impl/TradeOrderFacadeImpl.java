package com.eydms.facades.tradeOrder.impl;

import java.util.List;

import javax.annotation.Resource;

import com.eydms.core.enums.OrderType;
import com.eydms.core.tradeOrder.service.TradeOrderService;
import com.eydms.facades.tradeOrder.TradeOrderFacade;

public class TradeOrderFacadeImpl implements TradeOrderFacade  {

	@Resource
	TradeOrderService tradeOrderService;
	
	@Override
	public List<OrderType> listAllTradeOrderType() 
	{
		List<OrderType> tradeOrderlist = tradeOrderService.listAllTradeOrderType();
		return tradeOrderlist;
	}

}
