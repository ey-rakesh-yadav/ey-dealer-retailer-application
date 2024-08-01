package com.scl.facades.populators;

import java.util.ArrayList;
import java.util.List;

import com.scl.core.model.PurchaseOrderEntryModel;
import com.scl.core.model.PurchaseOrderModel;
import com.scl.facades.data.PurchaseOrderData;
import com.scl.facades.data.PurchaseOrderEntryData;
import com.scl.facades.data.PurchaseOrderEntryListData;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class PurchaseOrderPopulator implements Populator<PurchaseOrderModel, PurchaseOrderData>{

	@Override
	public void populate(PurchaseOrderModel source, PurchaseOrderData target) throws ConversionException {
		target.setPurchaseOrderNo(source.getCode());
		target.setVendorName(source.getVendorName());
		target.setItemType(source.getIncentiveType()!=null ? source.getIncentiveType().getCode() : null);
		
		int batchNo = ( source.getBatches()!=null ? source.getBatches().size() : 0) + 1;
		
		target.setBatchNo(Integer.toString(batchNo));
		
		List<PurchaseOrderEntryModel> entries = (List<PurchaseOrderEntryModel>) source.getPurchaseOrderEntries();
		
		List<PurchaseOrderEntryData> listData = new ArrayList<>();
		
		for(PurchaseOrderEntryModel entry : entries)
		{
			
			PurchaseOrderEntryData data = new PurchaseOrderEntryData();
			data.setEntryNo(entry.getEntryNumber());
			data.setItemName(entry.getProduct()!=null ? entry.getProduct().getName() : null);
			data.setQtyScheduled(entry.getQuantity()!=null ? entry.getQuantity().intValue() : null);
			data.setQtyAlreadyReceived(entry.getQtyAlreadyReceived());
	
			listData.add(data);
		}
		
		PurchaseOrderEntryListData list = new PurchaseOrderEntryListData();
		list.setPurchaseOrderEntries(listData);

		target.setEntries(list);
		
		
	}

}
