package com.scl.facades.populators;

import com.scl.core.model.PartnershipModel;
import com.scl.facades.data.SCLPartnerData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class SCLPartnerPopulator implements Populator<PartnershipModel, SCLPartnerData> {
    @Override
    public void populate(PartnershipModel source, SCLPartnerData target) throws ConversionException {
        target.setName(source.getName());
        target.setRelationship(source.getRelation());
    }
}
