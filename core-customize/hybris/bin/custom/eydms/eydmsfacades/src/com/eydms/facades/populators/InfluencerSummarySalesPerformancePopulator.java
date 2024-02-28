package com.eydms.facades.populators;

import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.facades.data.InfluencerSummaryData;
import com.eydms.facades.data.SalesPerformNetworkDetailsData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import javax.annotation.Resource;
import java.util.Objects;

public class InfluencerSummarySalesPerformancePopulator implements Populator<EyDmsCustomerModel, SalesPerformNetworkDetailsData> {
    @Resource
    private EnumerationService enumerationService;
    @Override
    public void populate(EyDmsCustomerModel source, SalesPerformNetworkDetailsData target) throws ConversionException {
        target.setCode(source.getUid());
        target.setName(source.getName());
        target.setCategory(enumerationService.getEnumerationName(source.getInfluencerType()));
        target.setContactNumber(source.getContactNumber());
        target.setTimesContacted(source.getTimesContacted());
        target.setType(source.getNetworkType());
        target.setPoints(Objects.nonNull(source.getAvailablePoints())?source.getAvailablePoints():0);
    }
}
