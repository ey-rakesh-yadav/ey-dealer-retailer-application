package com.scl.facades.depot.operations;

import java.util.List;
import java.util.Map;

import com.scl.facades.depot.operations.data.DepotProductData;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.facades.data.DropdownData;
import com.scl.facades.data.ISOVisibilityData;
import com.scl.facades.data.VisitMasterData;
import com.scl.facades.depot.operations.data.DailyCapacityUtilizationData;
import com.scl.facades.depot.operations.data.DepotStockData;

import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.warehousingfacades.storelocator.data.WarehouseData;


public interface DepotOperationsFacade 
{
	List<DepotStockData> getStockAvailability(List<String> productCode, List<String> depotCode);
	DailyCapacityUtilizationData getDailyCapacityUtilization(List<String> productCode, List<String> depotCode);
	
	Map<String,Map<String,Integer>> getDispatchTATAndDeliveryTime();

	String getStockAvailabilityTotal();
	List<DropdownData> getDepotListForUser();
	List<AddressData> getDepotAddresses(String depotCode);

	List<DropdownData> getWareHouseDataFromDepotSubAreaMapping();
	List<DropdownData> getDepotListOfGrades(boolean forISOOrders);

	SearchPageData<ISOVisibilityData> getISOVisibilityDetails(SearchPageData<ISOVisibilityData> searchPageData, String status, List<String> depotFilter, List<String> deliveryMode, List<String> productFilter);
	
	Integer getMRNPendingCount();
	List<DepotProductData> getProductListForDepot(boolean forISOOrders);
}
