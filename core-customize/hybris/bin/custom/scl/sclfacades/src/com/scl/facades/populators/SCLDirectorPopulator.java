package com.scl.facades.populators;

import com.scl.core.model.ProsDealerCompanyModel;
import com.scl.facades.data.SCLDirectorData;
import com.scl.facades.data.SCLImageData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import javax.annotation.Resource;

public class SCLDirectorPopulator implements Populator<ProsDealerCompanyModel, SCLDirectorData> {
    @Resource
    private Converter<MediaModel,SCLImageData> sclImageConverter;
    @Override
    public void populate(ProsDealerCompanyModel source, SCLDirectorData target) throws ConversionException {
        target.setName(source.getNameOfDirector());
        target.setFatherName(source.getFatherName());
        target.setLine1(source.getAddress());
        target.setLine2(source.getAddressLine2());
        target.setState(source.getState());
        target.setDistrict(source.getDistrict());
        target.setCity(source.getCity());
        target.setTaluka(source.getTaluka());
        target.setDin(source.getDinNo());
        target.setPanDoc(sclImageConverter.convert(source.getPanDoc()));
        target.setPanNumber(source.getPanNo());
    }


}
