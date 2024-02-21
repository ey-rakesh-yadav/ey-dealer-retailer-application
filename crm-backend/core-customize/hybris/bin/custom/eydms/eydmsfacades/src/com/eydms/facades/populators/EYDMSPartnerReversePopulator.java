package com.eydms.facades.populators;

import com.eydms.core.model.PartnershipModel;
import com.eydms.facades.data.EYDMSPartnerData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;

import javax.annotation.Resource;

public class  EYDMSPartnerReversePopulator implements Populator<EYDMSPartnerData, PartnershipModel> {
    @Resource
    private KeyGenerator customCodeGenerator;
    @Override
    public void populate(EYDMSPartnerData source, PartnershipModel target) throws ConversionException {
        target.setPartnerID(customCodeGenerator.generate().toString());
        target.setName(source.getName());
        target.setRelation(source.getRelationship());
    }
}
