package com.eydms.facades.populators.outstandingreport;

import com.eydms.core.model.OutstandingDueReportModel;
import com.eydms.facades.data.OutstandingDueReportData;
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
        target.setCounterName(source.getEyDmsCustomer().getName());
        target.setCounterCode(source.getEyDmsCustomer().getUid());
        target.setCustomerNo(source.getEyDmsCustomer().getCustomerNo());
    }
}
