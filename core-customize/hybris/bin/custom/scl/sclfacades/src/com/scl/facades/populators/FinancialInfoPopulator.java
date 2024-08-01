package com.scl.facades.populators;

import com.scl.core.model.NominationModel;
import com.scl.core.model.SclCustomerModel;
import com.scl.facades.data.*;
import com.scl.facades.prosdealer.data.NominationData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import javax.annotation.Resource;

public class FinancialInfoPopulator implements Populator<SclCustomerModel, SCLFinancialInfoData> {
    @Resource
    private Converter<MediaModel, SCLImageData> sclImageConverter;

    @Resource
    private Converter<NominationModel, NominationData>  nominationsConverter;

    @Override
    public void populate(SclCustomerModel source, SCLFinancialInfoData target) {
        target.setFirmPan(source.getPanCard());
        target.setFirmTan(source.getTanNo());
        target.setFirmAccountNo(source.getBankAccountNo());
        target.setFirmIFSC(source.getIfscCode());
        if(null!=source.getDdNeftDoc()){
            target.setDdNeftDoc(sclImageConverter.convert(source.getDdNeftDoc()));
        }
        if(null!=source.getBlankChequeDoc()){
            target.setBlankChequeDoc(sclImageConverter.convert(source.getBlankChequeDoc()));
        }
        if(null!=source.getPanDoc()){
            target.setFirmPanDoc(sclImageConverter.convert(source.getPanDoc()));
        }
        if(null!=source.getNominee()){
            target.setNomineeDetails(nominationsConverter.convert(source.getNominee()));
        }
    }

}
