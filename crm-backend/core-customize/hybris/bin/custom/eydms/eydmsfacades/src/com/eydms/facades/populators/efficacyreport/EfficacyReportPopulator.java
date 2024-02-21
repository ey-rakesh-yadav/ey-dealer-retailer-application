package com.eydms.facades.populators.efficacyreport;

import com.eydms.core.model.EfficacyReportMasterModel;
import com.eydms.facades.data.EfficacyReportData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class EfficacyReportPopulator implements Populator<EfficacyReportMasterModel, EfficacyReportData> {

    @Override
    public void populate(EfficacyReportMasterModel source, EfficacyReportData target) throws ConversionException {
        target.setId(source.getPk().toString());
        target.setMonth(source.getMonth());
        target.setYear(source.getYear());
        target.setTotalCounterVisited(source.getTotalCounterVisited());
        target.setTotalNumberOfVisits(source.getTotalNumberOfVisits());
        target.setSalesBeforeVisit(source.getSalesBeforeVisit());
        target.setSalesAfterVisit(source.getSalesAfterVisit());
        target.setGrowth(source.getGrowth());
        target.setObseleteCountersVisited(source.getObseleteCountersVisited());
        target.setObseleteCountersRevived(source.getObseleteCountersRevived());
        target.setSalesVolumeRevived(source.getSalesVolumeRevived());
        target.setTotalOutstandingStartMonth(source.getTotalOutstandingStartMonth());
        target.setTotalOutstandingEndMonth(source.getTotalOutstandingEndMonth());
        target.setOutstandingCleared(source.getOutstandingCleared());
        target.setAgeingDaysStartMonth(source.getAgeingDaysStartMonth());
        target.setAgeingDaysEndMonth(source.getAgeingDaysEndMonth());
        target.setOrdersBookedForNewProductOrSKU(source.getOrdersBookedForNewProductOrSKU());
        target.setSalesVolumeRevived(source.getSalesVolumeRevived());
    }
}

