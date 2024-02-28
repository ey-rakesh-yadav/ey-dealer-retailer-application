package com.eydms.facades.populators;

import com.eydms.core.model.DealerVehicleDetailsModel;
import com.eydms.facades.data.order.vehicle.DealerVehicleDetailsData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class EYDMSVehicleDetailsPopulator implements Populator<DealerVehicleDetailsModel, DealerVehicleDetailsData> {
    @Override
    public void populate(DealerVehicleDetailsModel source, DealerVehicleDetailsData target) throws ConversionException {
        target.setVehicleNumber(source.getVehicleNumber());
        target.setCapacity(String.valueOf(source.getCapacity()));
        target.setMake(source.getMake());
        target.setModel(source.getModel());
    }
}
