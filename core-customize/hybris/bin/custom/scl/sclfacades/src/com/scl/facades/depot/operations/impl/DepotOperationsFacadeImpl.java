package com.scl.facades.depot.operations.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.scl.core.model.DepotSubAreaMappingModel;
import com.scl.core.model.ISOMasterModel;
import com.scl.core.model.VisitMasterModel;
import com.scl.core.services.TerritoryManagementService;
import com.scl.facades.depot.operations.data.DepotProductData;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.warehousingfacades.storelocator.data.WarehouseData;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.depot.operations.service.DepotOperationsService;
import com.scl.facades.data.DropdownData;
import com.scl.facades.data.ISOVisibilityData;
import com.scl.facades.data.VisitMasterData;
import com.scl.facades.depot.operations.DepotOperationsFacade;
import com.scl.facades.depot.operations.data.DailyCapacityUtilizationData;
import com.scl.facades.depot.operations.data.DepotStockData;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.ordersplitting.WarehouseService;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.user.UserService;

import javax.annotation.Resource;

public class DepotOperationsFacadeImpl implements DepotOperationsFacade {

	private DepotOperationsService depotOperationsService;
	private UserService userService;
    @Autowired
    private WarehouseService warehouseService;
    @Autowired
	private Converter<AddressModel, AddressData> addressConverter;

	@Resource
	private TerritoryManagementService territoryManagementService;

	@Resource
	private BaseSiteService baseSiteService;
	
	@Autowired
	private Converter<ISOMasterModel, ISOVisibilityData> isoVisibilityConverter;

	@Autowired
	ModelService modelService;

	private static final Logger LOG = Logger.getLogger(DepotOperationsFacadeImpl.class);
	@Override
	public List<DepotStockData> getStockAvailability(List<String> productCode, List<String> depotCode) {
		List<DepotStockData> stockAvailability = new ArrayList<>();

		if(CollectionUtils.isNotEmpty(getDepotOperationsService().getStockAvailability(productCode, depotCode))){
			stockAvailability = getDepotOperationsService().getStockAvailability(productCode, depotCode).stream().filter(obj -> obj.getDepotCode() != null).sorted(Comparator.comparing(DepotStockData::getDepotCode)).collect(Collectors.toList());
		}
		return  stockAvailability;
	}
	
	@Override
	public DailyCapacityUtilizationData getDailyCapacityUtilization(List<String> productCode, List<String> depotCode)
	{
		final UserModel user = getUserService().getCurrentUser();
		return getDepotOperationsService().getDailyCapacityUtilization(productCode, depotCode,user);
	}

	@Override
	public Map<String, Map<String, Integer>> getDispatchTATAndDeliveryTime() {
		return getDepotOperationsService().getDispatchTATAndDeliveryTime();
	}
	
	@Override
	public String getStockAvailabilityTotal() {
		return getDepotOperationsService().getStockAvailabilityTotal();
	}
	
	public DepotOperationsService getDepotOperationsService() {
		return depotOperationsService;
	}
	public void setDepotOperationsService(DepotOperationsService depotOperationsService) {
		this.depotOperationsService = depotOperationsService;
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	@Override
	public List<DropdownData> getDepotListForUser() {
		List<DropdownData> result = new ArrayList<DropdownData>();
		if(userService.getCurrentUser() instanceof B2BCustomerModel) {
			B2BCustomerModel user = (B2BCustomerModel) userService.getCurrentUser();
			Collection<WarehouseModel> depotList = user.getDepots();
			if(CollectionUtils.isNotEmpty(depotList)) {
				depotList.stream().forEach(depot -> {
					DropdownData data = new DropdownData();
					data.setCode(depot.getCode());
					data.setName(depot.getName());
					result.add(data);
				});
			}
		}
		if(CollectionUtils.isNotEmpty(result)) {
			return result.stream().sorted(Comparator.comparing(DropdownData::getCode)).collect(Collectors.toList());
		}else{
			return result;
		}
	}

	@Override
	public List<AddressData> getDepotAddresses(String depotCode) {
		WarehouseModel warehouse =  warehouseService.getWarehouseForCode(depotCode); 
		final Collection<AddressModel> addresses = getDepotOperationsService().getDepotAddresses(warehouse);

		if (CollectionUtils.isNotEmpty(addresses))
		{
			final List<AddressData> result = new ArrayList<AddressData>();
			for (final AddressModel address : addresses)
			{
				final AddressData addressData = addressConverter.convert(address);
				result.add(addressData);
			}
			return result;
		}
		return Collections.emptyList();
	}

	@Override
	public List<DropdownData> getWareHouseDataFromDepotSubAreaMapping(){
		BaseSiteModel baseSiteModel = baseSiteService.getCurrentBaseSite();
		List<DepotSubAreaMappingModel> depotSubAreaMappingModels = depotOperationsService.findDepotSubAreaMappingByBrandAndSubArea(baseSiteModel);
		return getWarehouseDataFromDepotSubAreaMapping(depotSubAreaMappingModels);
	}

	private List<DropdownData> getWarehouseDataFromDepotSubAreaMapping(List<DepotSubAreaMappingModel> depotSubAreaMappingModels){
		List<DropdownData> warehouseDataList = new ArrayList<>();
		List<WarehouseModel> warehouseModels = depotSubAreaMappingModels.stream().map(DepotSubAreaMappingModel :: getDepot ).distinct().collect(Collectors.toList());
		if(CollectionUtils.isNotEmpty(warehouseModels)){
			for(WarehouseModel warehouseModel : warehouseModels){
				DropdownData warehouseData = new DropdownData();
				warehouseData.setCode(warehouseModel.getCode());
				warehouseData.setName(warehouseModel.getName());
				warehouseDataList.add(warehouseData);
			}
		}
		if(CollectionUtils.isNotEmpty(warehouseDataList)) {
			return warehouseDataList.stream().sorted(Comparator.comparing(DropdownData::getCode)).collect(Collectors.toList());
		}else{
			return warehouseDataList;
		}
	}

	@Override
	public List<DropdownData> getDepotListOfGrades(boolean forISOOrders) {
		List<List<Object>> depotList =depotOperationsService.getDepotListOfGrades(forISOOrders);
		 List<DropdownData> dropdownDataList=new ArrayList<>();
		  if(CollectionUtils.isNotEmpty(depotList)) {
			  for (List<Object> obj : depotList) {
				  DropdownData depotData=new DropdownData();
				  depotData.setCode((String)obj.get(0));
				  depotData.setName((String) obj.get(1));
				  dropdownDataList.add(depotData);
			  }
		  }
		  if(CollectionUtils.isNotEmpty(dropdownDataList)) {
			  return dropdownDataList.stream().sorted(Comparator.comparing(DropdownData::getName)).collect(Collectors.toList());
		  }else{
			  return dropdownDataList;
		  }
	}

	@Override
	public SearchPageData<ISOVisibilityData> getISOVisibilityDetails(SearchPageData<ISOVisibilityData> searchPageData, String status, List<String> depotFilter, List<String> deliveryMode, List<String> productCodes) {
		SearchPageData<ISOMasterModel> isoVisibilityDetails = depotOperationsService.getISOVisibilityDetails(searchPageData, status, depotFilter, deliveryMode, productCodes);
		if(isoVisibilityDetails!=null && isoVisibilityDetails.getResults()!=null && !isoVisibilityDetails.getResults().isEmpty()) {
			List<ISOMasterModel> isoMasterModels = isoVisibilityDetails.getResults();
			for(ISOMasterModel isoMasterModel : isoMasterModels) {
				if(isoMasterModel.getDeliveryId()!=null & null == isoMasterModel.getEtaDate()) {
					Date etaDate = depotOperationsService.getEtaDateForIsoMaster(isoMasterModel.getDeliveryId());
					isoMasterModel.setEtaDate(etaDate);
					modelService.save(isoMasterModel);
				}
			}
		}
		SearchPageData<ISOVisibilityData> result = new SearchPageData<ISOVisibilityData>();
		result.setPagination(isoVisibilityDetails.getPagination());
		result.setSorts(isoVisibilityDetails.getSorts());
		List<ISOVisibilityData> list = isoVisibilityConverter.convertAll(isoVisibilityDetails.getResults());
		
		if(list!=null)		{
			for(ISOVisibilityData data : list)
			{
				data.setStatus(status);
			}
		}
		
		result.setResults(list);
		return result;
	}

	@Override
	public Integer getMRNPendingCount() {
		return depotOperationsService.getMRNPendingCount();
	}

	@Override
	public List<DepotProductData> getProductListForDepot(boolean forISOOrders) {
		return depotOperationsService.getProductListForDepot(forISOOrders);
	}

}
