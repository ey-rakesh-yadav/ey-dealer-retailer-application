package com.scl.facades.populators;

import com.scl.core.model.NominationModel;
import com.scl.core.model.UidMediaModel;
import com.scl.facades.data.SCLImageData;
import com.scl.facades.data.UIDData;
import com.scl.facades.prosdealer.data.NominationData;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import javax.annotation.Resource;

public class SCLNominationPopulator implements Populator<NominationModel,NominationData> {
    @Resource
    private Converter<UidMediaModel, UIDData>  uidMediaConverter;
    
    @Resource
    private Populator<MediaModel, SCLImageData> sclImagePopulator;
    
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
        SCLImageData sclIdProofDoc = new SCLImageData();
        if (null != source.getIdProof()) {
        	sclImagePopulator.populate(source.getIdProof(), sclIdProofDoc);
           	target.setPanDoc(sclIdProofDoc);
    	}
        else
        	target.setPanDoc(new SCLImageData());
        
        target.setUids(uidMediaConverter.convertAll(source.getUidMedias()));
    }

}
