package com.scl.facades.populators;

import com.scl.core.model.SchemeDetailsModel;
import com.scl.facades.data.OngoingSchemeDetailsData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import java.text.SimpleDateFormat;
import java.util.Date;

public class OngoingSchemesPopulator implements Populator<SchemeDetailsModel, OngoingSchemeDetailsData> {
    @Override
    public void populate(SchemeDetailsModel source, OngoingSchemeDetailsData target) throws ConversionException {
        //target.setName(source.getObjective());
        target.setExpiry(getFormattedDate(source.getEndDate()));
        target.setStart(getFormattedDate(source.getStartDate()));
        target.setApplicableFor(source.getPartnerLevel().getCode());
    }
    private String getFormattedDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        return sdf.format(date);
    }
}
