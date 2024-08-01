package com.scl.facades.populators;

import com.scl.core.enums.*;
import com.scl.core.model.MeetingScheduleModel;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.model.SclUserModel;
import com.scl.core.model.SubAreaMasterModel;
import com.scl.core.notifications.service.SclNotificationService;
import com.scl.core.services.InfluencerService;
import com.scl.core.utility.SclDateUtility;
import com.scl.facades.data.FilterTalukaData;
import com.scl.facades.data.MeetingScheduleData;
import com.scl.facades.data.SCLAddressData;
import com.scl.facades.populators.djp.SchemeDetailsReversePopulator;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.*;

public class MeetScheduleReversePopulator implements Populator<MeetingScheduleData, MeetingScheduleModel> {
    Logger LOG = Logger.getLogger(MeetScheduleReversePopulator.class);
    @Resource
    private UserService userService;
    @Resource
    private ModelService modelService;

    @Autowired
    InfluencerService influencerService;

    @Autowired
    SclNotificationService sclNotificationService;
    @Resource
    private Converter<SCLAddressData, AddressModel> sclAddressReverseConverter;
    @Autowired
    KeyGenerator meetingFormIdGenerator;
    @Override
    public void populate(MeetingScheduleData source, MeetingScheduleModel target) throws ConversionException {
           if (source.getMeetingScheduleId() != null &&  source.getIsModify() != null && source.getIsModify().equals(Boolean.TRUE)) {
            target = influencerService.getMeetingScheduleByCode(source.getMeetingScheduleId());
            if(target!=null &&  target.getStatus()!=null && !target.getStatus().equals(MeetStatus.COMPLETED)) {
                if(source.getAttendees()!=null && !source.getAttendees().isEmpty()) {
                    target.setAttendance(source.getAttendees().size());
                    target.setAttendees(getAttendees(source.getAttendees()));
                }
                if (Objects.nonNull(source.getDate()) && Objects.nonNull(source.getEndTime())) {
                    LOG.info(String.format("updating meeting time for %s and time:%s", source.getMeetingScheduleId(), source.getDate()));
                    var newStartDateTime = SclDateUtility.parseMeetingDate(source.getDate());
                    var newEndDateTime = SclDateUtility.parseMeetingDate(source.getEndTime());
                    if (target.getEventDate() != null) {
                        if (Objects.nonNull(newStartDateTime) && newStartDateTime.after(target.getEventDate())) {
                            target.setStatus(MeetStatus.UPCOMING);
                            target.setEventDate(newStartDateTime);
                            target.setEndTime(newEndDateTime);
                        } if (Objects.nonNull(newStartDateTime) && newStartDateTime.before(target.getEventDate()) && target.getMeetingForm()==null) {
                            target.setStatus(MeetStatus.NOT_HAPPENED);
                        } if (Objects.nonNull(newStartDateTime) && newStartDateTime.before(target.getEventDate()) && target.getMeetingForm()!=null
                                && target.getMeetingForm().getMeetingFormId()!=null) {
                            target.setAttendance(target.getMeetingForm().getNoOfParticipantsAttended());
                            target.setStatus(MeetStatus.COMPLETED);
                        }
                    }
                }
                modelService.save(target);
            }else{
                LOG.info("Meeting Id not found");
            }
        }else{
            target= modelService.create(MeetingScheduleModel.class);
            target.setMeetingScheduleId((String) meetingFormIdGenerator.generate());
            target.setStatus(MeetStatus.UPCOMING);
        }
        if(target!=null) {
            target.setScheduledBy((SclUserModel) userService.getCurrentUser());
            if (source.getDate() != null) {
                target.setEventDate(SclDateUtility.parseMeetingDate(source.getDate()));
            }
            if (source.getEndTime() != null) {
                target.setEndTime(SclDateUtility.parseMeetingDate(source.getEndTime()));
            }
            if (source.getCategory() != null) {
                target.setCategory(MeetCategory.valueOf(source.getCategory()));
            }
            target.setReason(source.getReason());
            if (source.getVenue() != null) {
                target.setVenue(VenueType.valueOf(source.getVenue()));
            }
            target.setScheduledBy((SclUserModel) userService.getCurrentUser());
            target.setLine1(source.getLine1());
            target.setLine2(source.getLine2());
            target.setState(source.getState());
            target.setTaluka(source.getTaluka());
            target.setCity(source.getCity());
            target.setDistrict(source.getDistrict());
            target.setPincode(source.getPincode());
            if (source.getInfluencers() != null && !source.getInfluencers().isEmpty()) {
                target.setCustomers(getInvitees(source.getInfluencers()));
            }
            if (Objects.nonNull(source.getAssociatedCounter())) {
                target.setAssociatedCounter((SclCustomerModel) userService.getUserForUID(source.getAssociatedCounter()));
            }
            if (source.getMeetType() != null) {
                target.setMeetType(MeetingType.valueOf(source.getMeetType()));
            }
            modelService.save(target);
            try {
                StringBuilder builder = new StringBuilder();
                Map<String,String> suggestion = new HashMap<>();
                suggestion.put("MeetId",target.getMeetingScheduleId());
                builder.append("You have an Influencer Meet " + target.getMeetingScheduleId());
                builder.append(" scheduled by "+target.getScheduledBy().getName() + " on " +target.getEventDate());
                builder.append(",Venue Address - "+target.getLine1() + "," +target.getLine2() + "," +target.getCity()+ "," +target.getTaluka());
                builder.append( "," +target.getDistrict()+ "," +target.getState());
                String body = builder.toString();
                String sub ="New Meeting Invite has been sent";
                List<SclCustomerModel> invitees= target.getCustomers();
                for (SclCustomerModel invitee : invitees) {
                    sclNotificationService.submitDealerNotification((B2BCustomerModel) invitee,body,sub, NotificationCategory.INVITE_SENT,suggestion);
                }
            }
            catch(Exception e) {
                LOG.error("Error while sending Invitees Notification");
            }
        }
    }

    private List<SclCustomerModel> getInvitees(List<String> influencers) {
        List<SclCustomerModel> invitees=new ArrayList<>();
        influencers.forEach(inf->{
           var usermodel= userService.getUserForUID(inf);
           if(Objects.nonNull(usermodel)){
               invitees.add((SclCustomerModel) usermodel);
           }
        });
        return invitees;
    }
    private List<SclCustomerModel> getAttendees(List<String> attendedCustomers) {
        List<SclCustomerModel> attendees=new ArrayList<>();
        attendedCustomers.forEach(code->{
            var usermodel= userService.getUserForUID(code);
            if(Objects.nonNull(usermodel)){
                attendees.add((SclCustomerModel) usermodel);
            }
        });
        return attendees;
    }

}
