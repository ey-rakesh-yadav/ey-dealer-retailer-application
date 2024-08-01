package com.scl.facades.populators;

import com.scl.core.enums.IsDealerProvidingTransport;
import com.scl.facades.data.IsDealerProvidingTransportData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.type.TypeService;
import org.springframework.beans.factory.annotation.Autowired;

public class DealerProvidingTransportPopulator implements Populator<IsDealerProvidingTransport, IsDealerProvidingTransportData> {


    @Autowired
    private TypeService typeService;
    /**
     * Populate the target instance with values from the source instance.
     *
     * @param source object
     * @param target to fill
     * @throws ConversionException if an error occurs
     */
    @Override
    public void populate(IsDealerProvidingTransport source, IsDealerProvidingTransportData target) throws ConversionException {
        target.setCode(source.getCode());
        target.setName(typeService.getEnumerationValue(source).getName());
    }
}
