package com.eydms.facades.populators;


import com.eydms.core.enums.Activity;
import com.eydms.core.enums.MeetStatus;
import com.eydms.core.enums.MeetingType;
import com.eydms.core.model.MeetingCompletionFormModel;
import com.eydms.core.model.MeetingScheduleModel;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.model.EyDmsUserModel;
import com.eydms.core.services.InfluencerService;
import com.eydms.core.services.NetworkService;
import com.eydms.facades.data.MeetingCompletionFormData;
import com.eydms.facades.data.MeetingScheduleData;
import com.eydms.facades.data.EYDMSAddressData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;

public class MeetingCompletionFormReversePopulator implements Populator<MeetingCompletionFormData, MeetingCompletionFormModel>  {
    Logger LOG= Logger.getLogger(MeetingCompletionFormReversePopulator.class);

    @Autowired
    UserService userService;

    @Autowired
    ModelService modelService;
    @Autowired
    KeyGenerator meetingFormIdGenerator;
    @Autowired
    InfluencerService influencerService;

    @Override
    public void populate(MeetingCompletionFormData meetingCompletionFormData, MeetingCompletionFormModel meetingCompletionFormModel) throws ConversionException {
            if(meetingCompletionFormData.getMeetingScheduleId()!=null){
                MeetingScheduleModel meetingScheduleByCode = influencerService.getMeetingScheduleByCode(meetingCompletionFormData.getMeetingScheduleId());
                if(meetingScheduleByCode!=null) {
                    if(meetingCompletionFormData.getRetailer()!=null) {
                        EyDmsCustomerModel retailerId = (EyDmsCustomerModel) userService.getUserForUID(meetingCompletionFormData.getRetailer());
                        if(retailerId!=null) {
                            meetingCompletionFormModel.setRetailer(retailerId);
                        }
                    }
                    if(meetingCompletionFormData.getSp()!=null) {
                        EyDmsCustomerModel spId = (EyDmsCustomerModel) userService.getUserForUID(meetingCompletionFormData.getSp());
                        if(spId!=null)
                            meetingCompletionFormModel.setSp(spId);
                    }
                    meetingCompletionFormModel.setMeetingFormId((String) meetingFormIdGenerator.generate());
                    meetingCompletionFormModel.setTypeOfActivityConducted(meetingScheduleByCode.getMeetType());
                    meetingCompletionFormModel.setNoOfParticipantsAttended(Integer.parseInt(meetingCompletionFormData.getNoOfParticipantsAttended()));
                    meetingCompletionFormModel.setTotalAdminCost(meetingCompletionFormData.getTotalAdminCost());
                    meetingCompletionFormModel.setAvgLifting(meetingCompletionFormData.getAvgLifting());
                    meetingCompletionFormModel.setNameOfGiftTypeDistributed(meetingCompletionFormData.getNameOfGiftTypeDistributed());
                    meetingCompletionFormModel.setNoOfGiftsDistributed(meetingCompletionFormData.getNoOfGiftsDistributed());
                    if(meetingCompletionFormData.getSo()!=null) {
                        EyDmsUserModel soId = (EyDmsUserModel) userService.getUserForUID(meetingCompletionFormData.getSo());
                        if(soId!=null)
                            meetingCompletionFormModel.setSo(soId);
                    }
                    modelService.save(meetingCompletionFormModel);
                    meetingScheduleByCode.setMeetingForm(meetingCompletionFormModel);
                    meetingScheduleByCode.setStatus(MeetStatus.COMPLETED);
                    modelService.save(meetingScheduleByCode);
                }
            }
    }
}
