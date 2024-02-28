package com.eydms.core.order.services;

import com.eydms.core.model.DealerDriverDetailsModel;
import com.eydms.core.model.DealerVehicleDetailsModel;
import com.eydms.core.model.EyDmsCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.enums.OrderStatus;

import java.util.List;
import java.util.Map;

import com.eydms.core.enums.CustomerCategory;

public interface EyDmsOrderService {

	public Integer getOrderCountByStatus(String orderStatus,Boolean approvalPending);

	public Map<String, Long> getOrderDeliveredCountAndQty(OrderStatus delivered);
	
	public Map<String,Object> getDirectDispatchOrdersMTDPercentage(int month, int year);

	public Integer getOrderEntryCountByStatus(String orderStatus);

	public Boolean checkOrderQuantityForSO(Integer orderQty, String districtCode);
	
	public Double getBillingPriceForProduct(BaseSiteModel brand, String inventoryItemId, String erpCity, CustomerCategory customerCategory, String packagingCondition, String state);

    public Integer getCancelOrderCountByStatus(String orderStatus);

	public Integer getCancelOrderEntryCountByStatus(String orderStatus);

	public Boolean checkMaxOrderQuantityForSO(Double orderQty, EyDmsCustomerModel eydmsCustomer);

}
