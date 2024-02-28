package com.eydms.facades.populators;

import com.eydms.core.model.DealerDriverDetailsModel;
import com.eydms.facades.data.order.vehicle.DealerDriverDetailsData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class EYDMSDriverDetailsPopulator implements Populator<DealerDriverDetailsModel, DealerDriverDetailsData> {
    @Override
    public void populate(DealerDriverDetailsModel source, DealerDriverDetailsData target) throws ConversionException {
        target.setContactNumber(source.getContactNumber());
        target.setDriverName(source.getDriverName());
    }
}
