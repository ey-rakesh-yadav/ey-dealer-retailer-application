package com.eydms.facades.populators;

import com.eydms.core.model.PartnershipModel;
import com.eydms.facades.data.EYDMSPartnerData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class EYDMSPartnerPopulator implements Populator<PartnershipModel, EYDMSPartnerData> {
    @Override
    public void populate(PartnershipModel source, EYDMSPartnerData target) throws ConversionException {
        target.setName(source.getName());
        target.setRelationship(source.getRelation());
    }
}
