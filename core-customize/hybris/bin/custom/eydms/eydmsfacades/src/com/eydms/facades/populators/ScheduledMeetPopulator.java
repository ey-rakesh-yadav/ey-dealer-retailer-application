package com.eydms.facades.populators;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.enums.MeetStatus;
import com.eydms.core.model.MeetingCompletionFormModel;
import com.eydms.core.model.MeetingScheduleModel;
import com.eydms.core.services.InfluencerService;
import com.eydms.core.services.NetworkService;
import com.eydms.core.utility.EyDmsDateUtility;
import com.eydms.facades.data.MeetingCompletionFormData;
import com.eydms.facades.data.EYDMSAddressData;
import com.eydms.facades.data.ScheduledMeetData;
import com.eydms.facades.network.EYDMSNetworkFacade;
import com.eydms.facades.populators.sales.MeetingCompletionFormPopulator;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

public class ScheduledMeetPopulator implements Populator<MeetingScheduleModel, ScheduledMeetData> {
    public static final String HH_MM = "HH:mm";
    Logger LOG= Logger.getLogger(ScheduledMeetPopulator.class);
    @Resource
    Converter<AddressModel, EYDMSAddressData> eydmsAddressConverter;
    @Autowired
    Converter<MeetingCompletionFormModel, MeetingCompletionFormData> meetingCompletionFormConverter;
    @Autowired
    private EYDMSNetworkFacade networkFacade;
    @Autowired
    UserService userService;

    @Resource
    protected BaseSiteService baseSiteService;
    @Override
    public void populate(MeetingScheduleModel source, ScheduledMeetData target) throws ConversionException {
        if(source.getScheduledBy()!=null) {
            target.setNameOnTSO(source.getScheduledBy().getName());
            target.setEmployeeCode(source.getScheduledBy().getUid());
        }
        if(source.getEventDate()!=null) {
        	target.setMeetDate(getFormattedDate(source.getEventDate(),"dd MMM yyyy"));
        	target.setStartDate(getFormattedDate(source.getEventDate(),"dd MMM yyyy"));
        	target.setStartTime(getFormattedDate(source.getEventDate(), HH_MM));
        	target.setEndTime(getFormattedDate(source.getEndTime(), HH_MM));
        	target.setMeetMonth(getFormattedDate(source.getEventDate(),"MMM"));
        }

        if(source.getCategory()!=null) {
            target.setMeetType(source.getCategory().getCode());
        }
      //  target.setInviteeList(networkService.getInviteesForMeeting(source,source.getCustomers()));
        if(source.getCustomers()!=null && !source.getCustomers().isEmpty()) {
            target.setInvitees(source.getCustomers().size());
            target.setInviteeList(networkFacade.getInfluencerDetailedSummaryListData(source, source.getCustomers(),true,true));
        }
       // target.setInviteeList(influencerService.getInviteesForMeeting(source,source.getCustomers()));
        if(source.getMeetingForm()!=null) {
            target.setAttendance(String.valueOf(source.getMeetingForm().getNoOfParticipantsAttended()));
        }
        target.setMeetCode(String.valueOf(source.getPk()));
        target.setMeetingScheduleId(source.getMeetingScheduleId());
        if(source.getAddress()!=null) {
            target.setAddress(eydmsAddressConverter.convert(source.getAddress()));
        }else{
            target.setLine1(source.getLine1());
            target.setLine2(source.getLine2());
            target.setCity(source.getCity());
            target.setTaluka(source.getTaluka());
            target.setDistrict(source.getDistrict());
            target.setState(source.getState());
            target.setPincode(source.getPincode());
        }
        if(Objects.nonNull(source.getAssociatedCounter())) {
            target.setAssociatedCounter(source.getAssociatedCounter().getName());
            /*if(source.getAssociatedCounter().getGroups().contains(EyDmsCoreConstants.B2B_UNIT.EYDMS_SHREE_UNIT_UID)){
                target.setBrand(EyDmsCoreConstants.SITE.SHREE_SITE + " - " +EyDmsCoreConstants.B2B_UNIT.EYDMS_SHREE_UNIT_UID);
            }
            else if(source.getAssociatedCounter().getGroups().contains(EyDmsCoreConstants.B2B_UNIT.EYDMS_BANGUR_UNIT_UID)){
                target.setBrand(EyDmsCoreConstants.SITE.BANGUR_SITE + " - " +EyDmsCoreConstants.B2B_UNIT.EYDMS_BANGUR_UNIT_UID);
            }
            else if(source.getAssociatedCounter().getGroups().contains(EyDmsCoreConstants.B2B_UNIT.EYDMS_SHREE_UNIT_UID)){
                target.setBrand(EyDmsCoreConstants.SITE.ROCKSTRONG_SITE + " - " +EyDmsCoreConstants.B2B_UNIT.EYDMS_ROCKSTRONG_UNIT_UID);
            }*/

            UserModel user =  userService.getUserForUID(source.getAssociatedCounter().getUid());
            String brand = ((B2BCustomerModel)user).getDefaultB2BUnit().getUid();

            switch (brand) {
                case EyDmsCoreConstants.B2B_UNIT.EYDMS_SHREE_UNIT_UID :
                {
                    target.setBrand(EyDmsCoreConstants.SITE.SHREE_SITE);
                    target.setBrandName("SHREE");
                }
                case EyDmsCoreConstants.B2B_UNIT.EYDMS_BANGUR_UNIT_UID : {
                    target.setBrand(EyDmsCoreConstants.SITE.BANGUR_SITE);
                    target.setBrandName("BANGUR");
                }
                case EyDmsCoreConstants.B2B_UNIT.EYDMS_ROCKSTRONG_UNIT_UID : {
                    target.setBrand(EyDmsCoreConstants.SITE.ROCKSTRONG_SITE);
                    target.setBrandName("ROCKSTRONG");
                }
            }
        }
        //TODO add budget info here
        target.setProposedBudget(0);
        target.setActualSpent(0);

        if(source.getMeetType()!=null) {
            target.setMeetType(source.getMeetType().getCode());
            target.setTypeOfActivityConducted(source.getMeetType().getCode());
        }
        if(Objects.nonNull(source.getStatus())) {
            /*if (source.getStatus().equals(MeetStatus.UPCOMING)) {
                if (source.getEventDate().before(new Date())) {
                    target.setMeetStatus(MeetStatus.NOT_HAPPENED.getCode());
                }
                else {
                    target.setMeetStatus(source.getStatus().getCode());
                }
            } else {
                target.setMeetStatus(source.getStatus().getCode());
            }*/
            target.setMeetStatus(source.getStatus().getCode());
        }
        target.setIsMeetingFormEnabled(!source.getStatus().equals(MeetStatus.NOT_HAPPENED));
    }

    private String getFormattedDate(Date date, String format) {
        SimpleDateFormat dateFormat=new SimpleDateFormat(format);
         return dateFormat.format(date);
    }


    private String getFormattedDate(Date eventDate) {
        SimpleDateFormat dateFormat=new SimpleDateFormat("dd MMM yyyy");
        String date=dateFormat.format(eventDate);
        SimpleDateFormat timeFormat=new SimpleDateFormat(HH_MM);
        String time=timeFormat.format(eventDate);
        return String.format("%s at %s",date,time);
    }

}
