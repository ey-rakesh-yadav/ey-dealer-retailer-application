package com.eydms.facades.populators;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import de.hybris.platform.commercefacades.order.converters.populator.OrderEntryPopulator;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;

public class EyDmsOrderEntryPopulator extends OrderEntryPopulator {

	private EnumerationService enumerationService;
	
    private CommonI18NService commonI18NService;

	@Override
	public void populate(AbstractOrderEntryModel source, OrderEntryData target) throws ConversionException {
		if(source.getExpectedDeliveryDate()!=null) {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
			String strDate = dateFormat.format(source.getExpectedDeliveryDate());  
			target.setSelectedDeliveryDate(strDate);
		}
		if(source.getExpectedDeliveryslot()!=null) {
			target.setSelectedDeliverySlot(source.getExpectedDeliveryslot().getCode());
		}
		if(source.getCalculatedDeliveryDate()!=null) {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
			String strDate = dateFormat.format(source.getCalculatedDeliveryDate());  
			target.setCalculatedDeliveryDate(strDate);
		}
		if(source.getCalculatedDeliveryslot()!=null) {
			target.setCalculatedDeliverySlot(source.getCalculatedDeliveryslot().getCode());
		}
		target.setSequence(source.getSequence());
		target.setTruckNo(source.getTruckNo());
		target.setDriverContactNo(source.getDriverContactNo());
		target.setTruckDispatchDate(source.getTruckDispatcheddate());
		target.setQuantityMT(source.getQuantityInMT());
		target.setDeliveryQty(source.getDeliveryQty());
		addCommon(source, target);
		if(source.getProduct()!=null) {
			addProduct(source, target);
		}
		target.setErpLineItemId(source.getErpLineItemId());
		if(source.getSource()!=null) {
			target.setWarehouseCode(source.getSource().getCode());
			target.setWarehouseName(source.getSource().getName());
		}
		target.setRouteId(source.getRouteId());
		if(source instanceof OrderEntryModel && ((OrderEntryModel)source).getStatus()!=null) {
			target.setStatus(((OrderEntryModel)source).getStatus().getCode());
		}
		//if(source.getRemarks()!=null)
		target.setRemarks(source.getRemarks());
		super.addTotals(source, target);
	}

	public EnumerationService getEnumerationService() {
		return enumerationService;
	}

	public void setEnumerationService(EnumerationService enumerationService) {
		this.enumerationService = enumerationService;
	}

	public CommonI18NService getCommonI18NService() {
		return commonI18NService;
	}

	public void setCommonI18NService(CommonI18NService commonI18NService) {
		this.commonI18NService = commonI18NService;
	}
		
}
