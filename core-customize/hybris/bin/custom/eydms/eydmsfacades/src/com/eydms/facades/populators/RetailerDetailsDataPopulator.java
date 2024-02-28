package com.eydms.facades.populators;

import com.eydms.core.model.RetailerSalesSummaryModel;
import com.eydms.facades.data.RetailerDetailsData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.springframework.util.Assert;

public class RetailerDetailsDataPopulator implements Populator<RetailerSalesSummaryModel, RetailerDetailsData> {
    @Override
    public void populate(RetailerSalesSummaryModel source, RetailerDetailsData target) throws ConversionException {
        Assert.notNull(source, "Parameter source cannot be null.");
        Assert.notNull(target, "Parameter target cannot be null.");

        target.setCustomerCode(source.getRetailerCode());
        target.setCustomerName(source.getRetailerName());
        target.setErpCustomerNo(source.getRetailerErpCustomerNo());
        target.setCustomerPotential(source.getRetailerPotential());
        target.setCySales(source.getSale());
    }
}
