package com.scl.facades.order.impl;

import com.scl.core.order.services.SclOrderService;

import com.scl.facades.order.SclOrderFacade;

import de.hybris.platform.core.enums.OrderStatus;

import java.util.Map;


public class DefaultSclOrderFacade implements SclOrderFacade{

	private SclOrderService sclOrderService;


	/**
	 * Get Order Count by Status for SO
	 * @param orderStatus
	 * @return
	 */
	@Override
	public Integer getOrderCountByStatus(String orderStatus, Boolean approvalPending) {
		return getSclOrderService().getOrderCountByStatus(orderStatus, approvalPending);
	}
	
	@Override
	public Integer getOrderEntryCountByStatus(String orderStatus) {
		return getSclOrderService().getOrderEntryCountByStatus(orderStatus);
	}
	
	@Override
	public Integer getDeliveryItemCountByStatus(String orderStatus) {
		return getSclOrderService().getDeliveryItemCountByStatus(orderStatus);
	}
	
	/**
	 * Get Order count for current month for SO
	 * @return
	 */
	@Override
	public Map<String, Long> getOrderDeliveredCountAndQty() {
		return getSclOrderService().getOrderDeliveredCountAndQty(OrderStatus.DELIVERED);
	}
	
	@Override
	public Map<String, Object> getDirectDispatchOrdersMTDPercentage(int month, int year) {
		return getSclOrderService().getDirectDispatchOrdersMTDPercentage(month, year);
	}	

	@Override
	public Boolean checkOrderQuantityForSO(Integer orderQty, String districtCode) {
		return getSclOrderService().checkOrderQuantityForSO(orderQty, districtCode);
	}

	@Override
	public Integer getCancelOrderCountByStatus(String orderStatus) {
		return getSclOrderService().getCancelOrderCountByStatus(orderStatus);
	}

	@Override
	public Integer getCancelOrderEntryCountByStatus(String orderStatus) {
		return getSclOrderService().getCancelOrderEntryCountByStatus(orderStatus);
	}




	public SclOrderService getSclOrderService() {
		return sclOrderService;
	}

	public void setSclOrderService(SclOrderService sclOrderService) {
		this.sclOrderService = sclOrderService;
	}

}
