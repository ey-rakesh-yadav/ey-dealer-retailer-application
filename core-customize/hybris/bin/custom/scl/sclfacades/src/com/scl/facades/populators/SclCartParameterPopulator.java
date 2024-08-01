package com.scl.facades.populators;

import de.hybris.platform.commercefacades.order.data.AddToCartParams;
import de.hybris.platform.commerceservices.service.data.CommerceCartParameter;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class SclCartParameterPopulator implements Populator<AddToCartParams, CommerceCartParameter>{

	@Override
	public void populate(AddToCartParams source, CommerceCartParameter target) throws ConversionException {
		target.setSelectedDeliveryDate(source.getSelectedDeliveryDate());
		target.setSelectedDeliverySlot(source.getSelectedDeliverySlot());
		target.setTruckNo(source.getTruckNo());
		target.setDriverContactNo(source.getDriverContactNo());
		target.setCalculatedDeliveryDate(source.getCalculatedDeliveryDate());
		target.setCalculatedDeliverySlot(source.getCalculatedDeliverySlot());
		target.setSequence(source.getSequence());
		target.setQuantityMT(source.getQuantityMT());
		target.setWarehouseCode(source.getWarehouseCode());
		target.setRouteId(source.getRouteId());
		target.setRemarks(source.getRemarks());
		target.setAddressPk(source.getAddressPk());
		target.setRetailerUid(source.getRetailerUid());
		target.setIncoTerm(source.getIncoTerm());
		target.setOrderFor(source.getOrderFor());
		target.setIsDealerProvidingOwnTransport(source.getIsDealerProvidingOwnTransport());
		target.setDeliveryMode(source.getDeliveryMode());
		target.setOrderRequisitionId(source.getOrderRequisitionId());
		target.setProductAliasName(source.getProductAliasName());
		target.setIsPartnerCustomer(source.getIsPartnerCustomer());
		target.setPlacedByCustomer(source.getPlacedByCustomer());
	}

}
