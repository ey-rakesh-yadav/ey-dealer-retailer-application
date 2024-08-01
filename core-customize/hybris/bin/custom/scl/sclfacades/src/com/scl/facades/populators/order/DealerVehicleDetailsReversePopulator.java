package com.scl.facades.populators.order;

import com.scl.core.customer.services.SclCustomerService;
import com.scl.core.model.DealerVehicleDetailsModel;
import com.scl.facades.data.order.vehicle.DealerVehicleDetailsData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Resource;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

public class DealerVehicleDetailsReversePopulator implements Populator<DealerVehicleDetailsData,DealerVehicleDetailsModel> {

    @Resource
    private SclCustomerService sclCustomerService;

    @Override
    public void populate(DealerVehicleDetailsData source, DealerVehicleDetailsModel target) throws ConversionException {

        validateParameterNotNullStandardMessage("source", source);
        validateParameterNotNullStandardMessage("target", target);

        if(StringUtils.isNotBlank(source.getCapacity())){
            target.setCapacity(Double.valueOf(source.getCapacity()));
        }
        target.setVehicleNumber(source.getVehicleNumber());
        target.setMake(source.getMake());
        target.setModel(source.getModel());
    }
}
