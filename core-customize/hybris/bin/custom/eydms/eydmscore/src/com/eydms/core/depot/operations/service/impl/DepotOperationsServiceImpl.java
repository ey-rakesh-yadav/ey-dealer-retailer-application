package com.eydms.core.depot.operations.service.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.eydms.core.dao.TerritoryManagementDao;
import com.eydms.core.depot.operations.dao.DepotOperationsDao;
import com.eydms.core.depot.operations.service.DepotOperationsService;

import com.eydms.core.model.DepotSubAreaMappingModel;
import com.eydms.core.model.ISOMasterModel;
import com.eydms.core.services.TerritoryManagementService;
import com.eydms.facades.data.FilterTalukaData;
import com.eydms.facades.data.ISOVisibilityData;
import com.eydms.facades.depot.operations.data.DailyCapacityUtilizationData;
import com.eydms.core.enums.WarehouseType;
import com.eydms.facades.depot.operations.data.DepotStockData;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.time.TimeService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;

public class DepotOperationsServiceImpl implements DepotOperationsService 
{
	private DepotOperationsDao depotOperationsDao;
	private TimeService timeService;
	
	@Autowired
	UserService userService;
	@Autowired 
	TerritoryManagementDao territoryManagementDao;

	@Autowired 
	TerritoryManagementService territoryService;
	
	@Autowired
	BaseSiteService baseSiteService;
	
	@Override
	public List<DepotStockData> getStockAvailability(List<String> productCode, List<String> depotCode) {
		return getDepotOperationsDao().getStockAvailability(productCode,depotCode);
	}
	@Override
	public DailyCapacityUtilizationData getDailyCapacityUtilization(List<String> productCode, List<String> depotCode, UserModel user)
	{
		Date startDate=getTimeService().getCurrentDateWithTimeNormalized();
		Calendar cal = Calendar.getInstance();
		cal.setTime(startDate);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 23);
		Date endDate = cal.getTime();
		return getDepotOperationsDao().getDailyCapacityUtilization(productCode, depotCode, user, startDate, endDate);
	}
	@Override
	public Map<String, Map<String, Integer>> getDispatchTATAndDeliveryTime() {
		return getDepotOperationsDao().getDispatchTATAndDeliveryTime();
	}
	
	@Override
	public Long getStockAvailabilityTotal() {
		return getDepotOperationsDao().findStockAvailabilityCounts(WarehouseType.DEPOT);
	}

	public DepotOperationsDao getDepotOperationsDao() {
		return depotOperationsDao;
	}

	public void setDepotOperationsDao(DepotOperationsDao depotOperationsDao) {
		this.depotOperationsDao = depotOperationsDao;
	}
	public TimeService getTimeService() {
		return timeService;
	}
	public void setTimeService(TimeService timeService) {
		this.timeService = timeService;
	}
	@Override
	public Collection<AddressModel> getDepotAddresses(WarehouseModel warehouse) {
		validateParameterNotNull(warehouse, "Depot model cannot be null");
		return depotOperationsDao.findDepotAddress(warehouse);
	}

	@Override
	public List<DepotSubAreaMappingModel> findDepotSubAreaMappingByBrandAndSubArea(final BaseSiteModel brand){
		//New Territory Change
		FilterTalukaData filterTalukaData = new FilterTalukaData();
		List<DepotSubAreaMappingModel> resultList = depotOperationsDao.findDepotSubAreaMappingByBrandAndSubArea(territoryService.getTaulkaForUser(filterTalukaData));
		return resultList;
	}
	
	@Override
	public List<String> getDepotListOfGrades(final BaseSiteModel brand) {
		//New Territory Change
		FilterTalukaData filterTalukaData = new FilterTalukaData();
		return depotOperationsDao.getDepotListOfGrades(territoryService.getTaulkaForUser(filterTalukaData));
	}
		
	@Override
	public SearchPageData<ISOMasterModel> getISOVisibilityDetails(SearchPageData<ISOVisibilityData> searchPageData, String status, List<String> depotFilter,  List<String> deliveryMode) {
		
		
		List<String> depotCodes = new ArrayList<>();
		
		if(Objects.isNull(depotFilter))
		{
			depotFilter = Collections.emptyList();
		}
		
		if(depotFilter.isEmpty())
		{
			BaseSiteModel baseSiteModel = baseSiteService.getCurrentBaseSite();
			List<DepotSubAreaMappingModel> depotSubAreaMappingModels = findDepotSubAreaMappingByBrandAndSubArea(baseSiteModel);
			List<WarehouseModel> warehouseModels = depotSubAreaMappingModels.stream().map(DepotSubAreaMappingModel :: getDepot ).distinct().collect(Collectors.toList());
			
			for(WarehouseModel model : warehouseModels)
			{
				if(model.getCode()!=null)
				{
					depotCodes.add(model.getCode());
				}
			}
		}
		else
		{
			depotCodes = new ArrayList<>(depotFilter);
		}
		
		
		return depotOperationsDao.getISOVisibilityDetails(searchPageData, depotCodes, status, deliveryMode);
	}
	@Override
	public Integer getMRNPendingCount() {
		
		List<String> depotCodes = new ArrayList<>();
		
		BaseSiteModel baseSiteModel = baseSiteService.getCurrentBaseSite();
		List<DepotSubAreaMappingModel> depotSubAreaMappingModels = findDepotSubAreaMappingByBrandAndSubArea(baseSiteModel);
		List<WarehouseModel> warehouseModels = depotSubAreaMappingModels.stream().map(DepotSubAreaMappingModel :: getDepot ).distinct().collect(Collectors.toList());
		
		for(WarehouseModel model : warehouseModels)
		{
			if(model.getCode()!=null)
			{
				depotCodes.add(model.getCode());
			}
		}
		
		return depotOperationsDao.getMRNPendingCount(depotCodes);
	}

	@Override
	public Date getEtaDateForIsoMaster(String deliveryId) {
		return depotOperationsDao.getEtaDateForIsoMaster(deliveryId);
	}
}
