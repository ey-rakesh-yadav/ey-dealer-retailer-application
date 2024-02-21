package com.eydms.facades.populators;

import com.eydms.core.model.NominationModel;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.facades.data.*;
import com.eydms.facades.prosdealer.data.NominationData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import javax.annotation.Resource;

public class FinancialInfoPopulator implements Populator<EyDmsCustomerModel, EYDMSFinancialInfoData> {
    @Resource
    private Converter<MediaModel, EYDMSImageData> eydmsImageConverter;

    @Resource
    private Converter<NominationModel, NominationData>  nominationsConverter;

    @Override
    public void populate(EyDmsCustomerModel source, EYDMSFinancialInfoData target) {
        target.setFirmPan(source.getPanCard());
        target.setFirmTan(source.getTanNo());
        target.setFirmAccountNo(source.getBankAccountNo());
        target.setFirmIFSC(source.getIfscCode());
        if(null!=source.getDdNeftDoc()){
            target.setDdNeftDoc(eydmsImageConverter.convert(source.getDdNeftDoc()));
        }
        if(null!=source.getBlankChequeDoc()){
            target.setBlankChequeDoc(eydmsImageConverter.convert(source.getBlankChequeDoc()));
        }
        if(null!=source.getPanDoc()){
            target.setFirmPanDoc(eydmsImageConverter.convert(source.getPanDoc()));
        }
        if(null!=source.getNominee()){
            target.setNomineeDetails(nominationsConverter.convert(source.getNominee()));
        }
    }

}
