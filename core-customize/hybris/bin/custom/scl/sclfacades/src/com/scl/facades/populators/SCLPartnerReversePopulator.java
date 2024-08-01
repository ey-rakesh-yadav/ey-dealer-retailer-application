package com.scl.facades.populators;

import com.scl.core.model.PartnershipModel;
import com.scl.facades.data.SCLPartnerData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;

import javax.annotation.Resource;

public class  SCLPartnerReversePopulator implements Populator<SCLPartnerData, PartnershipModel> {
    @Resource
    private KeyGenerator customCodeGenerator;
    @Override
    public void populate(SCLPartnerData source, PartnershipModel target) throws ConversionException {
        target.setPartnerID(customCodeGenerator.generate().toString());
        target.setName(source.getName());
        target.setRelation(source.getRelationship());
    }
}
