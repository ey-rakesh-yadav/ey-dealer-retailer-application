package com.eydms.facades.otp.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.eydms.core.services.SmsOtpService;
import com.eydms.facades.otp.SmsOtpFacade;

import de.hybris.platform.commercefacades.user.UserFacade;


public class SmsOtpFacadeImpl implements SmsOtpFacade{
	
	@Autowired
	SmsOtpService smsOtpService;
	
	@Autowired
	UserFacade userFacade;
	
	@Override
	public boolean isUserExisting(String username) {
		return userFacade.isUserExisting(username);
	}

	@Override
	public String sendSmsOtp(String username , String otpType) {
		return smsOtpService.sendSmsOtp(username,otpType,null);
	}
	
	public boolean validateOTP(String otpNum, String mobileNo) {
		return smsOtpService.validateOTP(otpNum, mobileNo);
	}

	@Override
	public boolean sendChangePwdSmsOtp(String mobileNumber) {
		return smsOtpService.sendChangePwdSmsOtp(mobileNumber);
	}

	@Override
	public boolean sendRedeemRequestApprovedSms(String uid, String reqValue, String reqType, List<String> giftItems) {
		return smsOtpService.sendRedeemRequestApprovedSms(uid, reqValue, reqType, giftItems);
	}

	@Override
	public boolean sendRedeemRequestRejectedSms(String uid, String reqValue, String reqType, List<String> giftItems) {
		return smsOtpService.sendRedeemRequestRejectedSms(uid, reqValue, reqType, giftItems);
	}

}
