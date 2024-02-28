package com.eydms.facades.populators;

import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.facades.data.InfluencerFinanceData;
import com.eydms.facades.data.EYDMSImageData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import javax.annotation.Resource;

public class InfluencerFinancialsPopulator implements Populator<EyDmsCustomerModel,InfluencerFinanceData> {
    @Resource
    private Converter<MediaModel, EYDMSImageData> eydmsImageConverter;
    @Override
    public void populate(EyDmsCustomerModel source,InfluencerFinanceData target) throws ConversionException {
        target.setAccountNo(source.getBankAccountNo());
        target.setIfscCode(source.getIfscCode());
        target.setPanNo(source.getPanCard());
        target.setPanDoc(eydmsImageConverter.convert(source.getPanDoc()));
        target.setAadharNo(source.getAadharNo());
        target.setAadharDoc(eydmsImageConverter.convert(source.getAadharPhoto()));
    }
}
