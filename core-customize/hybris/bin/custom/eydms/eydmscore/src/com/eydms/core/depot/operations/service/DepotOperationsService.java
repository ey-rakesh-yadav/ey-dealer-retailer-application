package com.eydms.core.depot.operations.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.eydms.core.model.DepotSubAreaMappingModel;
import com.eydms.core.model.ISOMasterModel;
import com.eydms.core.model.VisitMasterModel;
import com.eydms.facades.data.ISOVisibilityData;
import com.eydms.facades.depot.operations.data.DailyCapacityUtilizationData;
import com.eydms.facades.depot.operations.data.DepotStockData;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.ordersplitting.model.WarehouseModel;

public interface DepotOperationsService
{
	List<DepotStockData> getStockAvailability(List<String> productCode, List<String> depotCode);
	DailyCapacityUtilizationData getDailyCapacityUtilization(List<String> productCode, List<String> depotCode, UserModel user);

	Map<String,Map<String,Integer>> getDispatchTATAndDeliveryTime();

	Long getStockAvailabilityTotal();
	Collection<AddressModel> getDepotAddresses(WarehouseModel warehouse);

	List<DepotSubAreaMappingModel> findDepotSubAreaMappingByBrandAndSubArea(final BaseSiteModel brand);
	List<String> getDepotListOfGrades(BaseSiteModel brand);

	SearchPageData<ISOMasterModel> getISOVisibilityDetails(SearchPageData<ISOVisibilityData> searchPageData, String status, List<String> depotFilter,  List<String> deliveryMode);

	Integer getMRNPendingCount();

	Date getEtaDateForIsoMaster(String deliveryId);
}