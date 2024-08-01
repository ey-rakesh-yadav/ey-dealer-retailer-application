package com.scl.facades.populators;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.model.DeliveryItemModel;
import com.scl.core.services.SCLProductService;
import com.scl.facades.data.DeliveryItemData;
import com.scl.facades.data.SclIncoTermMasterData;

import de.hybris.platform.commercefacades.order.converters.populator.OrderEntryPopulator;
import de.hybris.platform.commercefacades.order.data.DeliveryModeData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;

public class SclOrderEntryPopulator extends OrderEntryPopulator {

	private EnumerationService enumerationService;
	
    private CommonI18NService commonI18NService;
    
    @Autowired
    private Converter<AddressModel, AddressData> addressConverter;

    @Autowired
    SCLProductService sclProductService;
    
	@Override
	public void populate(AbstractOrderEntryModel source, OrderEntryData target) throws ConversionException {
		if(source.getExpectedDeliveryDate()!=null) {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
			String strDate = dateFormat.format(source.getExpectedDeliveryDate());  
			target.setSelectedDeliveryDate(strDate);
		}
		if(source.getExpectedSlot()!=null) {
			target.setSelectedDeliverySlot(source.getExpectedSlot().getCentreTime());
		}
		if(source.getCalculatedDeliveryDate()!=null) {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
			String strDate = dateFormat.format(source.getCalculatedDeliveryDate());  
			target.setCalculatedDeliveryDate(strDate);
		}
		if(source.getCalculatedSlot()!=null) {
			target.setCalculatedDeliverySlot(source.getCalculatedSlot().getCentreTime());
		}
		target.setSequence(source.getSequence());
		target.setTruckNo(source.getTruckNo());
		target.setDriverContactNo(source.getDriverContactNo());
		target.setQuantityMT(source.getRemainingQuantity());

		addCommon(source, target);
		if(source.getProduct()!=null) {
			addProduct(source, target);
			ProductData productData = target.getProduct();
			if (source.getDeliveryAddress() != null && source.getDeliveryAddress().getGeographicalMaster()!=null) {
        		String aliasName = sclProductService.getProductAlias(source.getProduct(), 
        				source.getDeliveryAddress().getGeographicalMaster().getState(), 
        				source.getDeliveryAddress().getGeographicalMaster().getDistrict());
        		if(StringUtils.isNotBlank(aliasName)) {
        			productData.setName(aliasName);
        		}
        	}
			target.setProduct(productData);
		}
		target.setErpLineItemId(source.getErpLineItemId());
		if(source.getSource()!=null) {
			target.setWarehouseCode(source.getSource().getCode());
			target.setWarehouseName(source.getSource().getName());
		}
		target.setRouteId(source.getRouteId());
		if(source instanceof OrderEntryModel && ((OrderEntryModel)source).getStatus()!=null) {
			target.setStatus(enumerationService.getEnumerationName(((OrderEntryModel)source).getStatus())!=null
					?enumerationService.getEnumerationName(((OrderEntryModel)source).getStatus())
							:((OrderEntryModel)source).getStatus().getCode());
		}
		
		target.setRemarks(source.getRemarks());
		target.setCancelReason(source.getCancelReason());
		super.addTotals(source, target);

		if (source.getDeliveryAddress() != null)
		{
			target.setDeliveryAddress(addressConverter.convert(source.getDeliveryAddress()));
		}
		if(source.getDeliveryMode()!=null) {
			target.setDeliveryMode(getDeliveryModeConverter().convert(source.getDeliveryMode()));
		}
		if(source.getRetailer()!=null) {
			target.setRetailerUid(source.getRetailer().getUid());
			target.setRetailerName(source.getRetailer().getName());
		}
		if(source.getIncoTerm()!=null) {
			SclIncoTermMasterData sclIncoTermMasterData = new SclIncoTermMasterData();
			sclIncoTermMasterData.setIncoTerm(source.getIncoTerm().getIncoTerm());
			sclIncoTermMasterData.setName(source.getIncoTerm().getName());
			target.setSclIncoTermMaster(sclIncoTermMasterData);
		}
		if(source.getIsDealerProvidingTransport()!=null) {
			target.setIsDealerProvidingOwnTransport(source.getIsDealerProvidingTransport().getCode());
		}
		if(source.getOrderFor()!=null) {
			target.setOrderFor(source.getOrderFor().getCode());
		}
		if(CollectionUtils.isNotEmpty(source.getDeliveriesItem())) {
			List<DeliveryItemData> list = new ArrayList<>();
			for(DeliveryItemModel deliveryItem: source.getDeliveriesItem()) {
				DeliveryItemData deliveryItemData = new DeliveryItemData();
				deliveryItemData.setDiNumber(deliveryItem.getDiNumber());
				deliveryItemData.setDeliveryLineNumber(deliveryItem.getDeliveryLineNumber());
				deliveryItemData.setInvoiceNumber(deliveryItem.getInvoiceNumber());
				deliveryItemData.setInvoiceLineNumber(deliveryItem.getInvoiceLineNumber());
				deliveryItemData.setDiQuanity(deliveryItem.getDiQuantity());
				if(deliveryItem.getStatus()!=null) {
					deliveryItemData.setStatus(deliveryItem.getStatus().getCode());
				}
				list.add(deliveryItemData);
			}
			target.setDeliveryItems(list);
		}
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
