package com.scl.core.job;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.dao.BulkGoodsDao;
import com.scl.core.dao.SclUserDao;
import com.scl.core.model.SclUserModel;

import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.ordersplitting.model.VendorModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.stock.model.StockLevelHistoryEntryModel;

public class GiftStockUpdateJob extends AbstractJobPerformable<CronJobModel>{

	@Autowired
	SclUserDao sclUserDao;
	
	@Autowired
	BulkGoodsDao bulkGoodsDao;
	
	@Override
	public PerformResult perform(CronJobModel arg0) {
		
		List<SclUserModel> users = sclUserDao.getAllActiveSO();
		
		for(SclUserModel user : users)
		{
			String warehouseCode = user.getEmployeeCode()!=null ? user.getEmployeeCode().concat("Warehouse") : null;
			
			WarehouseModel warehouse = null;
			
			if(warehouseCode!=null)
			{
				warehouse = bulkGoodsDao.getWarehouseByCode(warehouseCode);
				
				if(Objects.isNull(warehouse))
				{
					warehouse = modelService.create(WarehouseModel.class);
					warehouse.setCode(warehouseCode);
					
					VendorModel v = new VendorModel();
					v.setCode("default");	
					
					VendorModel vendor = flexibleSearchService.getModelByExample(v);
					
					warehouse.setVendor(vendor);
					warehouse.setActive(Boolean.TRUE);
					modelService.save(warehouse);	
				}
			}
			
			List<StockLevelModel> stocks = new ArrayList<>(warehouse.getStockLevels());
			
			for(StockLevelModel stock : stocks)
			{
				stock.setOpeningStock(stock.getAvailable());
				stock.setGoodsIn(0);
				stock.setGoodsOut(0);
				stock.setDisbursed(0);
				
				List<StockLevelHistoryEntryModel> stockEntries = stock.getStockLevelHistoryEntries();
				
				for(StockLevelHistoryEntryModel entry : stockEntries)
				{
					if(entry.getInTransit()==null||entry.getInTransit()==0)
					{
						stockEntries.remove(entry);
					}
					else
					{
						entry.setIsPreviousYearStock(Boolean.TRUE);
					}
					
				}
				
				modelService.saveAll(stockEntries);
				
				stock.setStockLevelHistoryEntries(stockEntries);
				
			}
			
			modelService.saveAll(stocks);
		}
		
		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}

}
