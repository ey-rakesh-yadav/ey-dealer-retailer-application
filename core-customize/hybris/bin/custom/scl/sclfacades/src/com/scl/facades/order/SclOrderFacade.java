package com.scl.facades.order;

import com.scl.facades.data.order.vehicle.DealerDriverDetailsData;
import com.scl.facades.data.order.vehicle.DealerDriverDetailsListData;
import com.scl.facades.data.order.vehicle.DealerVehicleDetailsListData;

import java.util.List;
import java.util.Map;

public interface SclOrderFacade {
	
	public Integer getOrderCountByStatus(String orderStatus, Boolean approvalPending);
	public Map<String, Long> getOrderDeliveredCountAndQty();
	public Map<String,Object> getDirectDispatchOrdersMTDPercentage(int month, int year);
	public Integer getOrderEntryCountByStatus(String orderStatus);
	public Boolean checkOrderQuantityForSO(Integer orderQty, String districtCode);

	public Integer getCancelOrderCountByStatus(String orderStatus);
	public Integer getCancelOrderEntryCountByStatus(String orderStatus);
	Integer getDeliveryItemCountByStatus(String orderStatus);

}
