package com.scl.facades.populators.order;

import com.scl.core.customer.services.SclCustomerService;
import com.scl.core.model.DealerDriverDetailsModel;
import com.scl.facades.data.order.vehicle.DealerDriverDetailsData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Resource;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

public class DealerDriverDetailsReversePopulator implements Populator<DealerDriverDetailsData,DealerDriverDetailsModel> {


    @Override
    public void populate(DealerDriverDetailsData source, DealerDriverDetailsModel target) throws ConversionException {


        validateParameterNotNullStandardMessage("source", source);
        validateParameterNotNullStandardMessage("target", target);

        target.setDriverName(source.getDriverName());
        target.setContactNumber(source.getContactNumber());
    }
}
