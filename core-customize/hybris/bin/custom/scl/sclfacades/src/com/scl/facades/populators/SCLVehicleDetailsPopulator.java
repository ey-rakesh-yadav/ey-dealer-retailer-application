package com.scl.facades.populators;

import com.scl.core.model.DealerVehicleDetailsModel;
import com.scl.facades.data.order.vehicle.DealerVehicleDetailsData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class SCLVehicleDetailsPopulator implements Populator<DealerVehicleDetailsModel, DealerVehicleDetailsData> {
    @Override
    public void populate(DealerVehicleDetailsModel source, DealerVehicleDetailsData target) throws ConversionException {
        target.setVehicleNumber(source.getVehicleNumber());
        target.setCapacity(String.valueOf(source.getCapacity()));
        target.setMake(source.getMake());
        target.setModel(source.getModel());
    }
}
