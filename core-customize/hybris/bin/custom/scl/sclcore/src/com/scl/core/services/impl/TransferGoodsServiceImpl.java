package com.scl.core.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.scl.core.model.UserSubAreaMappingModel;
import com.scl.core.region.dao.GeographicalRegionDao;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.dao.BulkGoodsDao;
import com.scl.core.dao.TerritoryManagementDao;
import com.scl.core.dao.TransferGoodsDao;
import com.scl.core.model.SclUserModel;
import com.scl.core.model.SubAreaMasterModel;
import com.scl.core.services.TerritoryManagementService;
import com.scl.core.services.TransferGoodsService;
import com.scl.facades.data.InventoryStockData;
import com.scl.facades.data.InventoryStockListData;

import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.ordersplitting.model.VendorModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;

public class TransferGoodsServiceImpl implements TransferGoodsService {

	
	@Autowired
	UserService userService;
	
	@Autowired
    TerritoryManagementService territoryManagementService;
	
	@Autowired
    TerritoryManagementDao territoryManagementDao;
	
	@Autowired
	BaseSiteService baseSiteService;
	
	@Autowired
	TransferGoodsDao transferGoodsDao;
	
	@Autowired
	BulkGoodsDao bulkGoodsDao;
	
	@Autowired
	ModelService modelService;
	
	@Autowired
	FlexibleSearchService flexibleSearchService;

	@Autowired
	GeographicalRegionDao geographicalRegionDao;
	
	@Override
	public List<SclUserModel> getSalesOfficers(String userId, String key) {
		
		SclUserModel user = (SclUserModel) userService.getUserForUID(userId);
		
		List<String> states = new ArrayList<>();
		List<UserSubAreaMappingModel> userSubAreaList =  geographicalRegionDao.getUserSubAreaMappingForUser(user);
		if(CollectionUtils.isNotEmpty(userSubAreaList)) {
			states.addAll(userSubAreaList.stream().map(data -> data.getState()).distinct().collect(Collectors.toList()));
		}
		List<SclUserModel> salesOfficers = new ArrayList<>();
		
		for(String state : states)
		{
			salesOfficers.addAll(territoryManagementService.getAllSalesOfficersByState(state));
		}
		
		List<String> soCodes = new ArrayList<>();
		
		for(SclUserModel salesOfficer : salesOfficers)
		{
			soCodes.add(salesOfficer.getEmployeeCode());
		}
		
		salesOfficers = new ArrayList<>(transferGoodsDao.searchForSalesOfficers(key, soCodes));
		return salesOfficers;
	}

	@Override
	public InventoryStockListData getInventoryStock(String userId, String soUid) {
		
		SclUserModel pocSo = (SclUserModel) userService.getUserForUID(userId);
		
		SclUserModel sclUser = (SclUserModel) userService.getUserForUID(soUid);
		
		List<InventoryStockData> listData = new ArrayList<>();
		
		InventoryStockListData list = new InventoryStockListData();
		
		String pocSoWarehouseCode = pocSo.getEmployeeCode()!=null ? pocSo.getEmployeeCode().concat("Warehouse") : null;
		
		String soWarehouseCode = sclUser.getEmployeeCode()!=null ? sclUser.getEmployeeCode().concat("Warehouse") : null;
		
		WarehouseModel pocSoWarehouse = null;
		
		WarehouseModel soWarehouse = null;
		
		if(pocSoWarehouseCode!=null)
		{
			pocSoWarehouse = bulkGoodsDao.getWarehouseByCode(pocSoWarehouseCode);
			
		}
		
		if(soWarehouseCode!=null)
		{
			soWarehouse = bulkGoodsDao.getWarehouseByCode(soWarehouseCode);
			
		}
		
		Map<String,StockLevelModel> soStocks =  soWarehouse.getStockLevels().stream().collect(Collectors.toMap(s->s.getProductCode(), s->s));
		
		List<SubAreaMasterModel> subAreas = territoryManagementService.getTerritoriesForSO();
		
		Set<StockLevelModel> stocks = null;
		
		if(pocSoWarehouse!=null)
		{
			stocks = pocSoWarehouse.getStockLevels();
			
			for(StockLevelModel stock : stocks)
			{
				InventoryStockData data = new InventoryStockData();
				data.setInTransit(stock.getInTransit());
				data.setProductName(stock.getProduct().getName());
				data.setCurrentStock(stock.getAvailable());
				
				int qtyInOrders = (transferGoodsDao.getPendingGiftStockForSO(stock.getProductCode(), subAreas)).intValue();
				int stockQty = soStocks.get(stock.getProductCode()).getAvailable();
				
				data.setQtyPending( (qtyInOrders-stockQty)>0 ? qtyInOrders-stockQty : 0 );
				
				listData.add(data);
			}
			
			list.setStocks(listData);
		}
		
		return list;
	}

}
