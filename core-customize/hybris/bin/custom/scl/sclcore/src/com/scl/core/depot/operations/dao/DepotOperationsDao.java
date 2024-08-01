package com.scl.core.depot.operations.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.scl.core.model.DepotSubAreaMappingModel;
import com.scl.core.model.ISOMasterModel;
import com.scl.core.model.SubAreaMasterModel;
import com.scl.facades.data.ISOVisibilityData;
import com.scl.facades.depot.operations.data.DailyCapacityUtilizationData;
import com.scl.core.enums.WarehouseType;

import com.scl.facades.depot.operations.data.DepotStockData;

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

	String findStockAvailabilityCounts(WarehouseType type);
	Collection<AddressModel> findDepotAddress(WarehouseModel warehouse);
	AddressModel findDepotAddressByPk(String pk);

	List<List<Object>> getDepotListOfGrades(List<SubAreaMasterModel> list,List<String> depotCodes,boolean forISOOrders);
	List<DepotSubAreaMappingModel> findDepotSubAreaMappingByBrandAndSubArea(List<SubAreaMasterModel> subAreas);


	SearchPageData<ISOMasterModel> getISOVisibilityDetails(SearchPageData<ISOVisibilityData> searchPageData, List<String> depotCodes, String Status,  List<String> deliveryMode, List<String> productCodes);

	String getProductForISOVisibility(String grade, String state, String packCondition, String brand);

	Integer getMRNPendingCount(List<String> depotCodes);

	Date getEtaDateForIsoMaster(String deliveryId);
	List<List<Object>> getProductListForDepot(List<String> depotCodes,boolean forISOOrders);
	String getDepotCodeName(String depotCode);

}