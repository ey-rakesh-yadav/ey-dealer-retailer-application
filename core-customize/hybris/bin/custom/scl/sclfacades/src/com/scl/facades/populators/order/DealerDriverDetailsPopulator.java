package com.scl.facades.populators.order;

import com.scl.core.model.DealerDriverDetailsModel;
import com.scl.core.model.DealerVehicleDetailsModel;
import com.scl.facades.data.order.vehicle.DealerDriverDetailsData;
import com.scl.facades.data.order.vehicle.DealerVehicleDetailsData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.apache.commons.lang.StringUtils;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

public class DealerDriverDetailsPopulator implements Populator<DealerDriverDetailsModel, DealerDriverDetailsData> {

    @Override
    public void populate(DealerDriverDetailsModel source, DealerDriverDetailsData target) throws ConversionException {

        validateParameterNotNullStandardMessage("source", source);
        validateParameterNotNullStandardMessage("target", target);

        target.setDriverName(source.getDriverName());
        target.setContactNumber(source.getContactNumber());
    }
}
