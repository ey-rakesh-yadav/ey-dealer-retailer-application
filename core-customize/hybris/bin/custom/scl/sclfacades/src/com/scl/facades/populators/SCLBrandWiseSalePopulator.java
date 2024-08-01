package com.scl.facades.populators;

import com.scl.core.model.BrandWiseSaleModel;
import com.scl.facades.prosdealer.data.BrandWiseSaleData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import java.util.Objects;

public class SCLBrandWiseSalePopulator implements Populator<BrandWiseSaleModel, BrandWiseSaleData> {
    @Override
    public void populate(BrandWiseSaleModel source, BrandWiseSaleData target) throws ConversionException {
        if (Objects.nonNull(source.getBrand())) {
            target.setBrandCode(source.getBrand().getIsocode());
        }
        target.setSaleInMT(source.getSaleInMT());
    }
}
