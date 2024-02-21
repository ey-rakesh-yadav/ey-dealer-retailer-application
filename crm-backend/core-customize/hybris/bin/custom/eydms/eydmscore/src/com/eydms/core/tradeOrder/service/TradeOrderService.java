package com.eydms.core.tradeOrder.service;

import java.util.List;

import com.eydms.core.enums.OrderType;

public interface TradeOrderService 
{
	List<OrderType> listAllTradeOrderType();
}
