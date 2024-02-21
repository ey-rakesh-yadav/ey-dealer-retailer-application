package com.eydms.facades.populators;

import com.eydms.core.model.ProsDealerCompanyModel;
import com.eydms.facades.data.EYDMSDirectorData;
import com.eydms.facades.util.GenericMediaUtil;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Objects;

public class EYDMSDirectorReversePopulator implements Populator<EYDMSDirectorData, ProsDealerCompanyModel> {
    @Resource
    private Converter<MultipartFile, MediaModel> eydmsMediaReverseConverter;
    @Resource
    private KeyGenerator customCodeGenerator;
    @Resource
    private GenericMediaUtil genericMediaUtil;
    @Override
    public void populate(EYDMSDirectorData source, ProsDealerCompanyModel target) throws ConversionException {
    target.setCompanyID(customCodeGenerator.generate().toString());
    target.setNameOfDirector(source.getName());
    target.setFatherName(source.getFatherName());
    target.setAddress(source.getLine1());
    target.setAddressLine2(source.getLine2());
    target.setState(source.getState());
    target.setDistrict(source.getDistrict());
    target.setCity(source.getCity());
    target.setTaluka(source.getTaluka());
    target.setDinNo(source.getDin());
    var media=genericMediaUtil.getMultipartFile(source.getPanDoc().getByteStream(),source.getPanDoc().getFileName());
        if (Objects.nonNull(media)) {
            target.setPanDoc(eydmsMediaReverseConverter.convert(media));
        }
    target.setPanNo(source.getPanNumber());
    }
}
