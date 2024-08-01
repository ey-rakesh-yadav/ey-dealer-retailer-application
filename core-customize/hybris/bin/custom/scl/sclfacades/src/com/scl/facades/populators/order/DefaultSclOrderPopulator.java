package com.scl.facades.populators.order;

import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

public class DefaultSclOrderPopulator implements Populator<OrderModel, OrderData> {


    @Override
    public void populate(OrderModel source, OrderData target) throws ConversionException {

        validateParameterNotNullStandardMessage("source", source);
        target.setOrderSource(null != source.getOrderSource() ? source.getOrderSource().getCode():null);
        //target.setEstimatedDeliveryDate(source.getEstimatedDeliveryDate());
       // target.setSelectedDeliveryDate(source.getSelectedDeliveryDate());
     //   target.setSelectedDeliverySlot(source.getSelectedDeliverySlot());
        target.setCustomerCategory(null != source.getCustomerCategory() ? source.getCustomerCategory().getCode():null);

    }
}
