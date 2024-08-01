package com.scl.facades.populators;

import com.scl.core.model.SclCustomerModel;
import com.scl.facades.data.InfluencerSummaryData;
import com.scl.facades.data.SalesPerformNetworkDetailsData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import javax.annotation.Resource;
import java.util.Objects;

public class InfluencerSummarySalesPerformancePopulator implements Populator<SclCustomerModel, SalesPerformNetworkDetailsData> {
    @Resource
    private EnumerationService enumerationService;
    @Override
    public void populate(SclCustomerModel source, SalesPerformNetworkDetailsData target) throws ConversionException {
        target.setCode(source.getUid());
        target.setName(source.getName());
        target.setCategory(enumerationService.getEnumerationName(source.getInfluencerType()));
        target.setContactNumber(source.getContactNumber());
        target.setTimesContacted(source.getTimesContacted());
        target.setType(source.getNetworkType());
        target.setPoints(Objects.nonNull(source.getAvailablePoints())?source.getAvailablePoints():0);
    }
}
