package com.scl.facades.populators;

import com.scl.core.model.DealerDriverDetailsModel;
import com.scl.facades.data.order.vehicle.DealerDriverDetailsData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class SCLDriverDetailsReversePopulator implements Populator<DealerDriverDetailsData, DealerDriverDetailsModel> {
    @Override
    public void populate(DealerDriverDetailsData source, DealerDriverDetailsModel target) throws ConversionException {
    target.setDriverName(source.getDriverName());
    target.setContactNumber(source.getContactNumber());
    }
}
