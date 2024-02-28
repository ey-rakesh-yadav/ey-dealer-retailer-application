package com.eydms.facades.populators;

import com.eydms.core.model.NominationModel;
import com.eydms.core.model.UidMediaModel;
import com.eydms.facades.data.EYDMSImageData;
import com.eydms.facades.data.UIDData;
import com.eydms.facades.prosdealer.data.NominationData;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import javax.annotation.Resource;

public class EYDMSNominationPopulator implements Populator<NominationModel,NominationData> {
    @Resource
    private Converter<UidMediaModel, UIDData>  uidMediaConverter;
    
    @Resource
    private Populator<MediaModel, EYDMSImageData> eydmsImagePopulator;
    
    @Override
    public void populate(NominationModel source, NominationData target) throws ConversionException {
        target.setNomineeID(source.getNomineeID());
        target.setName(source.getName());
        target.setFathersName(source.getFathersName());
        target.setLine1(source.getAddressLine1());
        target.setLine2(source.getAddressLine2());
        target.setState(source.getState());
        target.setCity(source.getCity());
        target.setDistrict(source.getDistrict());
        target.setTaluka(source.getTaluka());
        target.setAadharCard(source.getAadharCard());
        target.setPanCard(source.getPanCard());
        target.setIdProof(source.getIdProofDoc());
        EYDMSImageData eydmsIdProofDoc = new EYDMSImageData();
        if (null != source.getIdProof()) {
        	eydmsImagePopulator.populate(source.getIdProof(), eydmsIdProofDoc);
           	target.setPanDoc(eydmsIdProofDoc);
    	}
        else
        	target.setPanDoc(new EYDMSImageData());
        
        target.setUids(uidMediaConverter.convertAll(source.getUidMedias()));
    }

}
