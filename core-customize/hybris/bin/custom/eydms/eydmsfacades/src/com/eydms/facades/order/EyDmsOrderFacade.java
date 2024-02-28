package com.eydms.facades.order;

import com.eydms.facades.data.order.vehicle.DealerDriverDetailsData;
import com.eydms.facades.data.order.vehicle.DealerDriverDetailsListData;
import com.eydms.facades.data.order.vehicle.DealerVehicleDetailsListData;

import java.util.List;
import java.util.Map;

public interface EyDmsOrderFacade {
	
	public Integer getOrderCountByStatus(String orderStatus, Boolean approvalPending);
	public Map<String, Long> getOrderDeliveredCountAndQty();
	public Map<String,Object> getDirectDispatchOrdersMTDPercentage(int month, int year);
	public Integer getOrderEntryCountByStatus(String orderStatus);
	public Boolean checkOrderQuantityForSO(Integer orderQty, String districtCode);

	public Integer getCancelOrderCountByStatus(String orderStatus);
	public Integer getCancelOrderEntryCountByStatus(String orderStatus);

}
