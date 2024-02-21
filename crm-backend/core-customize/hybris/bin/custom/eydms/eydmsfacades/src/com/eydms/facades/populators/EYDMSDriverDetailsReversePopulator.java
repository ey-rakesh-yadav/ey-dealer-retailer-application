package com.eydms.facades.populators;

import com.eydms.core.model.DealerDriverDetailsModel;
import com.eydms.facades.data.order.vehicle.DealerDriverDetailsData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class EYDMSDriverDetailsReversePopulator implements Populator<DealerDriverDetailsData, DealerDriverDetailsModel> {
    @Override
    public void populate(DealerDriverDetailsData source, DealerDriverDetailsModel target) throws ConversionException {
    target.setDriverName(source.getDriverName());
    target.setContactNumber(source.getContactNumber());
    }
}
