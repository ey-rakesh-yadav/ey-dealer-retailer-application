package com.scl.facades.populators;

import com.scl.core.model.DealerDriverDetailsModel;
import com.scl.facades.data.order.vehicle.DealerDriverDetailsData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class SCLDriverDetailsPopulator implements Populator<DealerDriverDetailsModel, DealerDriverDetailsData> {
    @Override
    public void populate(DealerDriverDetailsModel source, DealerDriverDetailsData target) throws ConversionException {
        target.setContactNumber(source.getContactNumber());
        target.setDriverName(source.getDriverName());
    }
}
