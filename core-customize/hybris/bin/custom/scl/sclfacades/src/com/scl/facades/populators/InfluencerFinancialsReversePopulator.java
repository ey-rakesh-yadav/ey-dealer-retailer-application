package com.scl.facades.populators;

import com.scl.core.model.SclCustomerModel;
import com.scl.facades.data.InfluencerFinanceData;
import com.scl.facades.util.GenericMediaUtil;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

public class InfluencerFinancialsReversePopulator implements Populator<InfluencerFinanceData, SclCustomerModel> {
    @Resource
    private Converter<MultipartFile, MediaModel> sclMediaReverseConverter;
    @Resource
    private GenericMediaUtil genericMediaUtil;
    @Override
    public void populate(InfluencerFinanceData source, SclCustomerModel target) throws ConversionException {
        target.setBankAccountNo(source.getAccountNo());
        target.setIfscCode(source.getIfscCode());
        target.setPanCard(source.getPanNo());
        target.setPanDoc(sclMediaReverseConverter.convert(genericMediaUtil.getMultipartFile(source.getPanDoc().getByteStream(),source.getPanDoc().getFileName())));
        target.setAadharNo(source.getAadharNo());
        target.setAadharPhoto(sclMediaReverseConverter.convert(genericMediaUtil.getMultipartFile(source.getAadharDoc().getByteStream(),source.getAadharDoc().getFileName())));
    }
}
