package com.scl.facades.tradeOrder.impl;

import java.util.List;

import javax.annotation.Resource;

import com.scl.core.enums.OrderType;
import com.scl.core.tradeOrder.service.TradeOrderService;
import com.scl.facades.tradeOrder.TradeOrderFacade;

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
