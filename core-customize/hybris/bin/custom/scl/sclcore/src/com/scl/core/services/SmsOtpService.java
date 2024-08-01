package com.scl.core.services;

import com.scl.core.jalo.CustomerGeneratedOtp;
import com.scl.core.model.CustomerGeneratedOtpModel;
import com.scl.core.model.PartnerCustomerModel;
import com.scl.core.model.SclCustomerModel;
import de.hybris.platform.b2b.model.B2BCustomerModel;

import java.util.List;

public interface SmsOtpService {
	
 String generateOTP(String key);
 
 boolean validateOTP(String otpNum, String mobileNo, String partnerID);
 
 String sendSmsOtp(String mobileNo, String smsTemplate, String templateId);
 
 boolean sendChangePwdSmsOtp(String mobileNumber);
 
 boolean sendOnboardingSmsOtp(String name, String mobileNumber);
 
 boolean sendLoginSmsOtp(B2BCustomerModel customerModel, PartnerCustomerModel selectedPartner,String partnerCustomerFlag);
 
 boolean sendRedeemRequestSmsOtp(String uid, String reqValue, String reqType, List<String> giftItems);
 
 boolean sendRedeemRequestPlacedSms(String uid, String reqValue, String reqType, List<String> giftItems);
 
 boolean sendRedeemRequestApprovedSms(String uid, String reqValue, String reqType, List<String> giftItems);
 
 boolean sendRedeemRequestRejectedSms(String uid, String reqValue, String reqType, List<String> giftItems);

 String generateOTPFromDb(String key, B2BCustomerModel b2BCustomer);

 int generateCustomerOTPFromDb(String key, B2BCustomerModel customer,CustomerGeneratedOtpModel customerGeneratedOtpModel);
}
