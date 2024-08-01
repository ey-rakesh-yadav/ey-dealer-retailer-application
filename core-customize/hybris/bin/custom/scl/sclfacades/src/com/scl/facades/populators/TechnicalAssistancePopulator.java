package com.scl.facades.populators;

import java.text.SimpleDateFormat;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;

import com.scl.facades.data.SCLAddressData;
import com.scl.core.constants.SclCoreConstants;
import com.scl.core.model.TechnicalAssistanceModel;
import com.scl.facades.data.TechnicalAssistanceData;


import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.user.UserService;

import org.springframework.beans.factory.annotation.Autowired;

public class TechnicalAssistancePopulator implements Populator<TechnicalAssistanceModel,TechnicalAssistanceData>{

	@Autowired
	Populator<AddressModel, SCLAddressData> sclAddressPopulator;
	
	@Autowired
	EnumerationService enumerationService;
	
	@Autowired
	I18NService i18NService;
	
	@Autowired
	UserService userService;
	
	@Override
	public void populate(TechnicalAssistanceModel source, TechnicalAssistanceData target) throws ConversionException {
		
		if (source.getTsoStatus()!=null && userService.getCurrentUser().getGroups()!=null && userService.getCurrentUser().getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.TSO_GROUP_ID))) {
			target.setRequestStatus(null != enumerationService.getEnumerationName(source.getTsoStatus(), i18NService.getCurrentLocale()) ? enumerationService.getEnumerationName(source.getTsoStatus(), i18NService.getCurrentLocale()) : source.getTsoStatus().getCode());
		}
		else if(source.getRequestStatus()!=null){
			target.setRequestStatus(null != enumerationService.getEnumerationName(source.getRequestStatus(), i18NService.getCurrentLocale()) ? enumerationService.getEnumerationName(source.getRequestStatus(), i18NService.getCurrentLocale()) : source.getRequestStatus().getCode());
		}
		if(source.getConstructionAdvisorys()!=null) {
			//target.setConstructionAdvisory(null != enumerationService.getEnumerationName(source.getConstructionAdvisory(), i18NService.getCurrentLocale()) ? enumerationService.getEnumerationName(source.getConstructionAdvisory(), i18NService.getCurrentLocale()) : source.getConstructionAdvisory().getCode());
			target.setConstructionAdvisory(source.getConstructionAdvisorys());
		}
		target.setRequestNo(source.getRequestNo());
		
		SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss");  
		
	    if(source.getRequestDate()!=null)
	    {
	    	target.setRequestDate(formatter.format(source.getRequestDate()));
	    }
	    if(source.getRaisedBy()!=null)
	    {
	    	target.setRaisedBy(source.getRaisedBy().getName());
	    }
	    
	    target.setName(source.getName());
	    if(source.getSite()!=null) {
	    	target.setSiteId(source.getSite().getUid());
	    	target.setSiteName(source.getSite().getName());
		}
		target.setContactNumber(source.getCellPhone());
		target.setLine1(source.getLine1());
		target.setLine2(source.getLine2());
		target.setState(source.getState());
		target.setDistrict(source.getDistrict());
		target.setTaluka(source.getTaluka());
		target.setCity(source.getCity());
		target.setPostalCode(source.getPostalCode());
	    
	    if(source.getDateOfSupervisionRequired()!=null)
	    {
	    	 target.setDateOfSupervisionRequired(formatter.format(source.getDateOfSupervisionRequired()));
	    }
	    
	    if(source.getTsoAssignedDate()!=null)
	    {
	    	target.setTsoAssignedDate(formatter.format(source.getTsoAssignedDate()));
	    }
	    if(source.getTsoAssigned()!=null) {
	    	target.setTsoName(source.getTsoAssigned().getName());
	    	target.setTsoContactNumber(source.getTsoAssigned().getMobileNumber());
			target.setTsoAssignedEmail(source.getTsoAssigned().getUid());
	    }
	    target.setServiceStartDate(null);
	    target.setServiceEndDate(null);
	    
	    target.setRejectionReason(source.getRejectedReason());
	    target.setIsCloseEnabled(false);
	    if(source.getLastVisitedDate()!=null)
	    {
	    	target.setIsCloseEnabled(true);
	    	
	    	SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd");  
	    	String lastVisitDate = formatter1.format(source.getLastVisitedDate());
	    	target.setLastVisitDate(lastVisitDate);
	    }
		if(source.getModifiedtime()!=null){
			target.setModifiedTime(String.valueOf(source.getModifiedtime()));
		}
	}
	
}
