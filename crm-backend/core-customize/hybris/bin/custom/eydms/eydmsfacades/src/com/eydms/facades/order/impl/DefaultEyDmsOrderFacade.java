package com.eydms.facades.order.impl;

import com.eydms.core.order.services.EyDmsOrderService;

import com.eydms.facades.order.EyDmsOrderFacade;

import de.hybris.platform.core.enums.OrderStatus;

import java.util.Map;


public class DefaultEyDmsOrderFacade implements EyDmsOrderFacade{

	private EyDmsOrderService eydmsOrderService;


	/**
	 * Get Order Count by Status for SO
	 * @param orderStatus
	 * @return
	 */
	@Override
	public Integer getOrderCountByStatus(String orderStatus, Boolean approvalPending) {
		return getEyDmsOrderService().getOrderCountByStatus(orderStatus, approvalPending);
	}
	
	@Override
	public Integer getOrderEntryCountByStatus(String orderStatus) {
		return getEyDmsOrderService().getOrderEntryCountByStatus(orderStatus);
	}
	
	
	/**
	 * Get Order count for current month for SO
	 * @return
	 */
	@Override
	public Map<String, Long> getOrderDeliveredCountAndQty() {
		return getEyDmsOrderService().getOrderDeliveredCountAndQty(OrderStatus.DELIVERED);
	}
	
	@Override
	public Map<String, Object> getDirectDispatchOrdersMTDPercentage(int month, int year) {
		return getEyDmsOrderService().getDirectDispatchOrdersMTDPercentage(month, year);
	}	

	@Override
	public Boolean checkOrderQuantityForSO(Integer orderQty, String districtCode) {
		return getEyDmsOrderService().checkOrderQuantityForSO(orderQty, districtCode);
	}

	@Override
	public Integer getCancelOrderCountByStatus(String orderStatus) {
		return getEyDmsOrderService().getCancelOrderCountByStatus(orderStatus);
	}

	@Override
	public Integer getCancelOrderEntryCountByStatus(String orderStatus) {
		return getEyDmsOrderService().getCancelOrderEntryCountByStatus(orderStatus);
	}




	public EyDmsOrderService getEyDmsOrderService() {
		return eydmsOrderService;
	}

	public void setEyDmsOrderService(EyDmsOrderService eydmsOrderService) {
		this.eydmsOrderService = eydmsOrderService;
	}

}
