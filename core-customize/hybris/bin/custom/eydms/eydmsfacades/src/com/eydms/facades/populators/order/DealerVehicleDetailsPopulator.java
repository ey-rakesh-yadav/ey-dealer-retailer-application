package com.eydms.facades.populators.order;

import com.eydms.core.model.DealerVehicleDetailsModel;
import com.eydms.facades.data.order.vehicle.DealerVehicleDetailsData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.apache.commons.lang.StringUtils;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

public class DealerVehicleDetailsPopulator implements Populator<DealerVehicleDetailsModel, DealerVehicleDetailsData> {

    @Override
    public void populate(DealerVehicleDetailsModel source, DealerVehicleDetailsData target) throws ConversionException {

        validateParameterNotNullStandardMessage("source", source);
        validateParameterNotNullStandardMessage("target", target);

        target.setCapacity(null!= source.getCapacity() ? String.valueOf(source.getCapacity()) : StringUtils.EMPTY);
        target.setVehicleNumber(source.getVehicleNumber());
        target.setMake(source.getMake());
        target.setModel(source.getModel());
    }
}
