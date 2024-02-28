package com.eydms.facades.depot.operations;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.eydms.facades.data.DropdownData;
import com.eydms.facades.data.ISOVisibilityData;
import com.eydms.facades.data.VisitMasterData;
import com.eydms.facades.depot.operations.data.DailyCapacityUtilizationData;
import com.eydms.facades.depot.operations.data.DepotStockData;

import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.warehousingfacades.storelocator.data.WarehouseData;


public interface DepotOperationsFacade 
{
	List<DepotStockData> getStockAvailability(List<String> productCode, List<String> depotCode);
	DailyCapacityUtilizationData getDailyCapacityUtilization(List<String> productCode, List<String> depotCode);
	
	Map<String,Map<String,Integer>> getDispatchTATAndDeliveryTime();

	Long getStockAvailabilityTotal();
	List<DropdownData> getDepotListForUser();
	List<AddressData> getDepotAddresses(String depotCode);

	List<DropdownData> getWareHouseDataFromDepotSubAreaMapping();
	List<String> getDepotListOfGrades();

	SearchPageData<ISOVisibilityData> getISOVisibilityDetails(SearchPageData<ISOVisibilityData> searchPageData, String status, List<String> depotFilter, List<String> deliveryMode, List<String> productFilter);
	
	Integer getMRNPendingCount();
}
