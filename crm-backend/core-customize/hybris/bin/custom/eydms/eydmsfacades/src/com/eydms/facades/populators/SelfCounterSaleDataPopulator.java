package com.eydms.facades.populators;

import com.eydms.core.model.RetailerSalesSummaryModel;
import com.eydms.facades.data.SelfCounterSaleData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.springframework.util.Assert;

public class SelfCounterSaleDataPopulator implements Populator<RetailerSalesSummaryModel, SelfCounterSaleData> {
    @Override
    public void populate(RetailerSalesSummaryModel source, SelfCounterSaleData target) throws ConversionException {
        Assert.notNull(source, "Parameter source cannot be null.");
        Assert.notNull(target, "Parameter target cannot be null.");

        target.setCustomerCode(source.getDealerCode());
        target.setCustomerName(source.getDealerName());
        target.setCustomerPotential(source.getDealerPotential());
        target.setErpCustomerNo(source.getDealerErpCustomerNo());
    }
}
