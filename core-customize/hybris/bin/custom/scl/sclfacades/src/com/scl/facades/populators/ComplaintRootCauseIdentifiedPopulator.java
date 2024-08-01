package com.scl.facades.populators;

import com.scl.core.model.ComplaintRootCauseIdentifiedModel;
import com.scl.facades.data.ComplaintRootCauseIndentifiedData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class ComplaintRootCauseIdentifiedPopulator implements Populator<ComplaintRootCauseIdentifiedModel, ComplaintRootCauseIndentifiedData> {
    @Override
    public void populate(ComplaintRootCauseIdentifiedModel source, ComplaintRootCauseIndentifiedData target) throws ConversionException {
        target.setId(source.getId());
        target.setIsRootCauseIdentified(source.getIsRootCauseIdentified());
        target.setRootCause(String.valueOf(source.getRootCause()));
        target.setAccountableDepartment(source.getAccountableDepartment());
        target.setEmailId(source.getEmailId());
        target.setComplaintId(source.getComplaint().getRequestId());

    }
}
