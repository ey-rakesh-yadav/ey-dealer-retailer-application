package com.eydms.core.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.eydms.core.dao.BulkGoodsDao;
import com.eydms.core.dao.TerritoryManagementDao;
import com.eydms.core.dao.TransferGoodsDao;
import com.eydms.core.model.EyDmsUserModel;
import com.eydms.core.model.SubAreaMasterModel;
import com.eydms.core.services.TerritoryManagementService;
import com.eydms.core.services.TransferGoodsService;
import com.eydms.facades.data.InventoryStockData;
import com.eydms.facades.data.InventoryStockListData;

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
	
	@Override
	public List<EyDmsUserModel> getSalesOfficers(String userId, String key) {
		
		EyDmsUserModel user = (EyDmsUserModel) userService.getUserForUID(userId);
		
		List<String> states = new ArrayList<>();
		states.add(user.getState());
		
		List<EyDmsUserModel> salesOfficers = new ArrayList<>();
		
		for(String state : states)
		{
			salesOfficers.addAll(territoryManagementService.getAllSalesOfficersByState(state));
		}
		
		List<String> soCodes = new ArrayList<>();
		
		for(EyDmsUserModel salesOfficer : salesOfficers)
		{
			soCodes.add(salesOfficer.getEmployeeCode());
		}
		
		salesOfficers = new ArrayList<>(transferGoodsDao.searchForSalesOfficers(key, soCodes));
		return salesOfficers;
	}

	@Override
	public InventoryStockListData getInventoryStock(String userId, String soUid) {
		
		EyDmsUserModel pocSo = (EyDmsUserModel) userService.getUserForUID(userId);
		
		EyDmsUserModel eydmsUser = (EyDmsUserModel) userService.getUserForUID(soUid);
		
		List<InventoryStockData> listData = new ArrayList<>();
		
		InventoryStockListData list = new InventoryStockListData();
		
		String pocSoWarehouseCode = pocSo.getEmployeeCode()!=null ? pocSo.getEmployeeCode().concat("Warehouse") : null;
		
		String soWarehouseCode = eydmsUser.getEmployeeCode()!=null ? eydmsUser.getEmployeeCode().concat("Warehouse") : null;
		
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
