package com.scl.occ.controllers;

import java.util.List;

import javax.annotation.security.PermitAll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.scl.core.model.SMSProcessModel;
import com.scl.core.services.SmsOtpService;
import com.scl.facades.SchemesAndDiscountFacade;
import com.scl.facades.otp.SmsOtpFacade;

import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
@RestController
@RequestMapping(value = "/{baseSiteId}/otp")
@ApiVersion("v2")
@Tag(name = "otp Management")
@PermitAll
public class OtpLoginController 
{
	@Autowired
	SmsOtpFacade smsOtpFacade;
	
	@Autowired
	SchemesAndDiscountFacade schemesFacade;
	
	
//	@PostMapping("register/send")
//	@ApiBaseSiteIdParam
//	public void sendRegisterOTP(@RequestParam String username) throws DuplicateUidException
//	{
//		if(smsOtpFacade.isUserExisting(username)) {
//			throw new DuplicateUidException(String.format("Duplicate User ID %s", username)) ;
//		}
//		smsOtpFacade.sendSmsOtp(username,null);
//
//	}
//	
//	@PostMapping("login/send")
//	@ApiBaseSiteIdParam
//	public void sendLoginOTP(@RequestParam String username)
//	{
//
//		if(smsOtpFacade.isUserExisting(username)) {
//			smsOtpFacade.sendSmsOtp(username,null);
//		}	
//		else {
//		throw new UsernameNotFoundException(String.format("User ID %s does not exists", username));
//		}
//	}

	
	 @ResponseStatus(value = HttpStatus.CREATED)
	 @PostMapping(value = "/validateOtp")
	 @Operation(operationId = "validateOtp", summary = "Validate Otp")
	 @ApiBaseSiteIdParam
	 @ResponseBody
	public boolean validateOTP(@Parameter(description = "otpNum,") @RequestParam final String otpNum, @Parameter(description = "mobileNo") @RequestParam final String mobileNo,@Parameter(description = "partnerId") @RequestParam(required = false) final String partnerId)
	{
		if(smsOtpFacade.validateOTP(otpNum, mobileNo,partnerId))
		{
			return true;
		}
		else
		{	
			return false;
		}
	}
	 
	 	@ResponseStatus(value = HttpStatus.CREATED)
	    @PostMapping(value = "/sendRedeemReqSmsOtp")
	    @Operation(operationId = "sendRedeemReqSmsOtp", summary = "Send Redeem Request Sms Otp", description = "Send Redeem Request Sms Otp")
	    @ResponseBody
	    @ApiBaseSiteIdParam
	    public ResponseEntity<Boolean> sendRedeemReqSmsOtp(@Parameter(description = "uid", required = true) @RequestParam final String uid, @Parameter(description = "reqValue", required = true) @RequestParam final String reqValue, @Parameter(description = "reqType (CASH/KIND)", required = true) @RequestParam final String reqType,
	    		@Parameter(description = "giftItems", required = false) @RequestParam final List<String> giftItems) {
	        Boolean smsSent = schemesFacade.sendRedeemRequestSmsOtp(uid,reqValue, reqType, giftItems);
	        
	        if (smsSent.equals(Boolean.TRUE)) {
	            return ResponseEntity.status(HttpStatus.CREATED).body(Boolean.TRUE);
	        }
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Boolean.FALSE);
	    }
		
		@ResponseStatus(value = HttpStatus.CREATED)
	    @PostMapping(value = "/sendRedeemReqSubmittedSms")
	    @Operation(operationId = "sendRedeemReqSubmittedSms", summary = "Send Redeem Request Submitted Sms", description = "Send Redeem Request Submitted Sms")
	    @ResponseBody
	    @ApiBaseSiteIdParam
	    public ResponseEntity<Boolean> sendRedeemReqSubmittedSms(@Parameter(description = "uid", required = true) @RequestParam final String uid, @Parameter(description = "reqValue", required = true) @RequestParam final String reqValue, @Parameter(description = "reqType (CASH/KIND)", required = true) @RequestParam final String reqType,
	    		@Parameter(description = "giftItems", required = false) @RequestParam final List<String> giftItems) {
	        Boolean smsSent = schemesFacade.sendRedeemRequestPlacedSms(uid,reqValue, reqType, giftItems);
	        
	        if (smsSent.equals(Boolean.TRUE)) {
	            return ResponseEntity.status(HttpStatus.CREATED).body(Boolean.TRUE);
	        }
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Boolean.FALSE);
	    }
		
		@ResponseStatus(value = HttpStatus.CREATED)
	    @PostMapping(value = "/sendRedeemReqApprovedSms")
	    @Operation(operationId = "sendRedeemReqApprovedSms", summary = "Send Redeem Request Approved Sms", description = "Send Redeem Request Approved Sms")
	    @ResponseBody
	    @ApiBaseSiteIdParam
	    public ResponseEntity<Boolean> sendRedeemReqAcceptedSms(@Parameter(description = "uid", required = true) @RequestParam final String uid, @Parameter(description = "reqValue", required = true) @RequestParam final String reqValue, @Parameter(description = "reqType (CASH/KIND)", required = true) @RequestParam final String reqType,
	    		@Parameter(description = "giftItems", required = false) @RequestParam final List<String> giftItems) {
	        Boolean smsSent = smsOtpFacade.sendRedeemRequestApprovedSms(uid,reqValue, reqType, giftItems);
	        
	        if (smsSent.equals(Boolean.TRUE)) {
	            return ResponseEntity.status(HttpStatus.CREATED).body(Boolean.TRUE);
	        }
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Boolean.FALSE);
	    }
	
		@ResponseStatus(value = HttpStatus.CREATED)
	    @PostMapping(value = "/sendRedeemReqRejectedSms")
	    @Operation(operationId = "sendRedeemReqRejectedSms", summary = "Send Redeem Request Rejected Sms", description = "Send Redeem Request Rejected Sms")
	    @ResponseBody
	    @ApiBaseSiteIdParam
	    public ResponseEntity<Boolean> sendRedeemReqRejectedSms(@Parameter(description = "uid", required = true) @RequestParam final String uid, @Parameter(description = "reqValue", required = true) @RequestParam final String reqValue, @Parameter(description = "reqType (CASH/KIND)", required = true) @RequestParam final String reqType,
	    		@Parameter(description = "giftItems", required = false) @RequestParam final List<String> giftItems) {
	        Boolean smsSent = smsOtpFacade.sendRedeemRequestRejectedSms(uid,reqValue, reqType, giftItems);
	        
	        if (smsSent.equals(Boolean.TRUE)) {
	            return ResponseEntity.status(HttpStatus.CREATED).body(Boolean.TRUE);
	        }
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Boolean.FALSE);
	    }

}
