package com.scl.facades.populators;

import com.scl.core.model.ComplaintSiteVisitNotRequiredModel;
import com.scl.facades.data.ComplaintSiteVisitNotRequiredData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class ComplaintSiteVisitNotRequiredPopulator implements Populator<ComplaintSiteVisitNotRequiredModel, ComplaintSiteVisitNotRequiredData> {
    @Override
    public void populate(ComplaintSiteVisitNotRequiredModel source, ComplaintSiteVisitNotRequiredData target) throws ConversionException {
        target.setId(source.getId());
        target.setRootCause(String.valueOf(source.getRootCause()));
        target.setVisitNotRequiredReason(source.getVisitNotRequiredReason());
        target.setSolution(source.getSolution());
        target.setIscallHappenedWithTE(source.getIsCallHappenedWithTE());
        target.setTeComment(source.getTeComments());
        target.setAdditionalComment(source.getAdditionalComment());
        target.setComplaintId(source.getComplaint().getRequestId());
        target.setProblemAsReportedByCustomer(source.getProblemAsReportedByCustomer());
    }
}
