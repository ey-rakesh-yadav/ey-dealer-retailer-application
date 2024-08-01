package com.scl.facades.populators;

import com.scl.core.model.SclCustomerModel;
import com.scl.facades.data.InfluencerFinanceData;
import com.scl.facades.data.SCLImageData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import javax.annotation.Resource;

public class InfluencerFinancialsPopulator implements Populator<SclCustomerModel,InfluencerFinanceData> {
    @Resource
    private Converter<MediaModel, SCLImageData> sclImageConverter;
    @Override
    public void populate(SclCustomerModel source,InfluencerFinanceData target) throws ConversionException {
        target.setAccountNo(source.getBankAccountNo());
        target.setIfscCode(source.getIfscCode());
        target.setPanNo(source.getPanCard());
        target.setPanDoc(sclImageConverter.convert(source.getPanDoc()));
        target.setAadharNo(source.getAadharNo());
        target.setAadharDoc(sclImageConverter.convert(source.getAadharPhoto()));
    }
}
