package com.eydms.facades.populators;

import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.facades.data.InfluencerFinanceData;
import com.eydms.facades.util.GenericMediaUtil;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

public class InfluencerFinancialsReversePopulator implements Populator<InfluencerFinanceData, EyDmsCustomerModel> {
    @Resource
    private Converter<MultipartFile, MediaModel> eydmsMediaReverseConverter;
    @Resource
    private GenericMediaUtil genericMediaUtil;
    @Override
    public void populate(InfluencerFinanceData source, EyDmsCustomerModel target) throws ConversionException {
        target.setBankAccountNo(source.getAccountNo());
        target.setIfscCode(source.getIfscCode());
        target.setPanCard(source.getPanNo());
        target.setPanDoc(eydmsMediaReverseConverter.convert(genericMediaUtil.getMultipartFile(source.getPanDoc().getByteStream(),source.getPanDoc().getFileName())));
        target.setAadharNo(source.getAadharNo());
        target.setAadharPhoto(eydmsMediaReverseConverter.convert(genericMediaUtil.getMultipartFile(source.getAadharDoc().getByteStream(),source.getAadharDoc().getFileName())));
    }
}
