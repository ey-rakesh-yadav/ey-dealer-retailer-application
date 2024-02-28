package com.eydms.facades.populators;

import com.eydms.core.model.ScheduleNextVisitModel;
import com.eydms.facades.data.ScheduleNextVisitData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class ScheduleNextVisitDataPopulator implements Populator<ScheduleNextVisitModel, ScheduleNextVisitData> {
    @Override
    public void populate(ScheduleNextVisitModel scheduleNextVisitModel, ScheduleNextVisitData scheduleNextVisitData) throws ConversionException {
        scheduleNextVisitData.setId(scheduleNextVisitModel.getId());
        scheduleNextVisitData.setNextSiteVisitDate(String.valueOf(scheduleNextVisitModel.getNextSiteVisitDate()));
        scheduleNextVisitData.setNextSiteVisitTime(scheduleNextVisitModel.getNextSiteVisitTime());
        scheduleNextVisitData.setDetailsOfTaskToBeDone(String.valueOf(scheduleNextVisitModel.getDetailsOfTaskToBeDone()));
        scheduleNextVisitData.setComplaintId(scheduleNextVisitModel.getComplaint().getRequestId());
        if(scheduleNextVisitModel.getSite()!=null) {
        	scheduleNextVisitData.setSiteId(scheduleNextVisitModel.getSite().getUid());
        	scheduleNextVisitData.setSiteName(scheduleNextVisitModel.getSite().getName());
        }
    }
}
