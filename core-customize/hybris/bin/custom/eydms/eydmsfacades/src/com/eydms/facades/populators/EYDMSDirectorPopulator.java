package com.eydms.facades.populators;

import com.eydms.core.model.ProsDealerCompanyModel;
import com.eydms.facades.data.EYDMSDirectorData;
import com.eydms.facades.data.EYDMSImageData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import javax.annotation.Resource;

public class EYDMSDirectorPopulator implements Populator<ProsDealerCompanyModel, EYDMSDirectorData> {
    @Resource
    private Converter<MediaModel,EYDMSImageData> eydmsImageConverter;
    @Override
    public void populate(ProsDealerCompanyModel source, EYDMSDirectorData target) throws ConversionException {
        target.setName(source.getNameOfDirector());
        target.setFatherName(source.getFatherName());
        target.setLine1(source.getAddress());
        target.setLine2(source.getAddressLine2());
        target.setState(source.getState());
        target.setDistrict(source.getDistrict());
        target.setCity(source.getCity());
        target.setTaluka(source.getTaluka());
        target.setDin(source.getDinNo());
        target.setPanDoc(eydmsImageConverter.convert(source.getPanDoc()));
        target.setPanNumber(source.getPanNo());
    }


}
