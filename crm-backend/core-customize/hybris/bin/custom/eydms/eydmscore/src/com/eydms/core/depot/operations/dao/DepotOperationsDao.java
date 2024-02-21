package com.eydms.core.depot.operations.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.eydms.core.model.DepotSubAreaMappingModel;
import com.eydms.core.model.ISOMasterModel;
import com.eydms.core.model.SubAreaMasterModel;
import com.eydms.facades.data.ISOVisibilityData;
import com.eydms.facades.depot.operations.data.DailyCapacityUtilizationData;
import com.eydms.core.enums.WarehouseType;

import com.eydms.facades.depot.operations.data.DepotStockData;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.ordersplitting.model.WarehouseModel;


public interface DepotOperationsDao
{
	List<DepotStockData> getStockAvailability(List<String> productCode, List<String> depotCode);
	DailyCapacityUtilizationData getDailyCapacityUtilization(List<String> productCode, List<String> depotCode, UserModel user, Date startDate, Date endDate);

	Map<String,Map<String,Integer>> getDispatchTATAndDeliveryTime();

	Long findStockAvailabilityCounts(WarehouseType type);
	Collection<AddressModel> findDepotAddress(WarehouseModel warehouse);
	AddressModel findDepotAddressByPk(String pk);

	List<String> getDepotListOfGrades(List<SubAreaMasterModel> list);
	List<DepotSubAreaMappingModel> findDepotSubAreaMappingByBrandAndSubArea(List<SubAreaMasterModel> subAreas);

	SearchPageData<ISOMasterModel> getISOVisibilityDetails(SearchPageData<ISOVisibilityData> searchPageData, List<String> depotCodes, String Status,  List<String> deliveryMode);

	String getProductForISOVisibility(String grade, String state, String packCondition, String brand);

	Integer getMRNPendingCount(List<String> depotCodes);

	Date getEtaDateForIsoMaster(String deliveryId);
}