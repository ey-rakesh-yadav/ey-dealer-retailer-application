package com.eydms.core.services;

import de.hybris.platform.b2b.model.B2BCustomerModel;

import java.util.List;

public interface SmsOtpService {
	
 String generateOTP(String key);
 
 boolean validateOTP(String otpNum, String mobileNo);
 
 String sendSmsOtp(String mobileNo, String smsTemplate, String templateId);
 
 boolean sendChangePwdSmsOtp(String mobileNumber);
 
 boolean sendOnboardingSmsOtp(String name, String mobileNumber);
 
 boolean sendLoginSmsOtp(String uid, String mobileNumber, String customerNo);
 
 boolean sendRedeemRequestSmsOtp(String uid, String reqValue, String reqType, List<String> giftItems);
 
 boolean sendRedeemRequestPlacedSms(String uid, String reqValue, String reqType, List<String> giftItems);
 
 boolean sendRedeemRequestApprovedSms(String uid, String reqValue, String reqType, List<String> giftItems);
 
 boolean sendRedeemRequestRejectedSms(String uid, String reqValue, String reqType, List<String> giftItems);

 String generateOTPFromDb(String key, B2BCustomerModel b2BCustomer);
}
