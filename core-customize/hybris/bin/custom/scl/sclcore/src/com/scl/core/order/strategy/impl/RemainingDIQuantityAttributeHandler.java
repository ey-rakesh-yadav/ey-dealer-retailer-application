/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.scl.core.order.strategy.impl;

import com.scl.core.model.DeliveryItemModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.servicelayer.model.attribute.AbstractDynamicAttributeHandler;

import org.apache.commons.collections4.CollectionUtils;

/**
 * Provides calculation of the dynamic {@code rootItem} attribute on the {@code IntegrationObjectModel}
 */
public class RemainingDIQuantityAttributeHandler
		extends AbstractDynamicAttributeHandler<Double, OrderEntryModel>
{
	@Override
	public Double get(final OrderEntryModel orderEntryModel)
	{
        Double remainingDIQty = orderEntryModel.getQuantity().doubleValue();

        if(CollectionUtils.isNotEmpty(orderEntryModel.getDeliveriesItem())){

			for(DeliveryItemModel di:orderEntryModel.getDeliveriesItem() ){
				remainingDIQty-=di.getDiQuantity();
			}
		}


		return remainingDIQty;
	}
}
