package com.eydms.facades.populators;

import com.eydms.core.model.DealerVehicleDetailsModel;
import com.eydms.facades.data.order.vehicle.DealerVehicleDetailsData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class EYDMSVehicleDetailsReversePopulator implements Populator<DealerVehicleDetailsData, DealerVehicleDetailsModel> {

    @Override
    public void populate(DealerVehicleDetailsData source, DealerVehicleDetailsModel target) throws ConversionException {
        target.setCapacity(Double.valueOf(source.getCapacity()));
        target.setMake(source.getMake());
        target.setVehicleNumber(source.getVehicleNumber());
        target.setModel(source.getModel());
    }
}
