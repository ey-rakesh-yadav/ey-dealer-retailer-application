package com.scl.facades.populators;


import com.scl.core.enums.Activity;
import com.scl.core.enums.MeetStatus;
import com.scl.core.enums.MeetingType;
import com.scl.core.model.MeetingCompletionFormModel;
import com.scl.core.model.MeetingScheduleModel;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.model.SclUserModel;
import com.scl.core.services.InfluencerService;
import com.scl.core.services.NetworkService;
import com.scl.facades.data.MeetingCompletionFormData;
import com.scl.facades.data.MeetingScheduleData;
import com.scl.facades.data.SCLAddressData;
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
            if(meetingCompletionFormData.getMeetingScheduleId()!=null && !meetingCompletionFormData.getMeetingScheduleId().isEmpty()){
                MeetingScheduleModel meetingScheduleByCode = influencerService.getMeetingScheduleByCode(meetingCompletionFormData.getMeetingScheduleId());
                if(meetingScheduleByCode!=null) {
                    if(meetingCompletionFormData.getRetailer()!=null && !meetingCompletionFormData.getRetailer().isEmpty()) {
                        SclCustomerModel retailerId = (SclCustomerModel) userService.getUserForUID(meetingCompletionFormData.getRetailer());
                        if(retailerId!=null) {
                            meetingCompletionFormModel.setRetailer(retailerId);
                        }
                    }
                    if(meetingCompletionFormData.getSp()!=null && !meetingCompletionFormData.getSp().isEmpty()) {
                        SclCustomerModel spId = (SclCustomerModel) userService.getUserForUID(meetingCompletionFormData.getSp());
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
                    if(meetingCompletionFormData.getSo()!=null && !meetingCompletionFormData.getSo().isEmpty()) {
                        SclUserModel soId = (SclUserModel) userService.getUserForUID(meetingCompletionFormData.getSo());
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
