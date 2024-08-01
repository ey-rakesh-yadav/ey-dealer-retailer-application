package com.scl.facades.populators.outstandingreport;

import com.scl.core.model.OutstandingDueReportModel;
import com.scl.facades.data.OutstandingDueReportData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class OutstandingDueReportPopulator implements Populator<OutstandingDueReportModel, OutstandingDueReportData> {

    @Override
    public void populate(OutstandingDueReportModel source, OutstandingDueReportData target) throws ConversionException {
        target.setNumberOfVisits(source.getNumberOfVisits());
        target.setOutstandingStartMonth(source.getOutstandingStartMonth());
        target.setOutstandingCleared(source.getOutstandingCleared());
        target.setOutstandingEndMonth(source.getOutstandingEndMonth());
        target.setAgeingDaysStartMonth(source.getAgeingDaysStartMonth());
        target.setAgeingDaysEndMonth(source.getAgeingDaysEndMonth());
        target.setCounterName(source.getSclCustomer().getName());
        target.setCounterCode(source.getSclCustomer().getUid());
        target.setCustomerNo(source.getSclCustomer().getCustomerNo());
    }
}
