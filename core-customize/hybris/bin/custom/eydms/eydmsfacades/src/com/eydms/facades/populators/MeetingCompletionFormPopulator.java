package com.eydms.facades.populators;
import org.springframework.beans.factory.annotation.Autowired;

import com.eydms.core.model.MeetingCompletionFormModel;
import com.eydms.facades.data.MeetingCompletionFormData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class MeetingCompletionFormPopulator implements Populator<MeetingCompletionFormModel, MeetingCompletionFormData> {

	@Autowired
	EnumerationService enumerationService;
	
    @Override
    public void populate(MeetingCompletionFormModel source, MeetingCompletionFormData target) throws ConversionException {
        if(source!=null) {
            target.setAvgLifting(source.getAvgLifting());
            target.setNameOfGiftTypeDistributed(source.getNameOfGiftTypeDistributed());
            target.setNoOfGiftsDistributed(source.getNoOfGiftsDistributed());
            target.setNoOfParticipantsAttended(String.valueOf(source.getNoOfParticipantsAttended()));
            target.setTotalAdminCost(source.getTotalAdminCost());
            if(source.getTypeOfActivityConducted()!=null){
                target.setTypeOfActivityConducted(source.getTypeOfActivityConducted().getCode());
                target.setTypeOfActivityConductedName(enumerationService.getEnumerationName(source.getTypeOfActivityConducted())!=null?enumerationService.getEnumerationName(source.getTypeOfActivityConducted()):source.getTypeOfActivityConducted().getCode());
            }
            if(source.getRetailer()!=null) {
                target.setRetailer(source.getRetailer().getName());
            }
            if(source.getSo()!=null) {
                target.setSo(source.getSo().getName());
            }
            if(source.getSp()!=null) {
                target.setSp(source.getSp().getName());
                target.setSpCode(source.getSp().getUid());
            }
        }
    }
}
