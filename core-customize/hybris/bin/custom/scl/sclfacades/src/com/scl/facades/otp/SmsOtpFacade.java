package com.scl.facades.otp;

import java.util.List;

public interface SmsOtpFacade {

	public String sendSmsOtp(String username ,String otpType);
	
	public boolean validateOTP(String otpNum, String mobileNo,String partnerId);

	boolean isUserExisting(String username);
	
	public boolean sendChangePwdSmsOtp(String mobileNumber);
	
	boolean sendRedeemRequestApprovedSms(String uid, String reqValue, String reqType, List<String> giftItems);
	 
	boolean sendRedeemRequestRejectedSms(String uid, String reqValue, String reqType, List<String> giftItems);

	
}
