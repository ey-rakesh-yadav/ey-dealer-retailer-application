package com.eydms.core.services.impl;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import com.eydms.core.customer.dao.EyDmsEndCustomerDao;
import com.eydms.core.dao.DataConstraintDao;
import de.hybris.platform.b2b.model.B2BCustomerModel;

import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.google.common.cache.LoadingCache;
import com.eydms.core.event.SendSMSEvent;
import com.eydms.core.model.SMSProcessModel;
import com.eydms.core.services.SmsOtpService;

import de.hybris.platform.servicelayer.event.EventService;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.user.UserService;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

public class SmsOtpServiceImpl implements SmsOtpService{

	@Resource
	private EventService eventService;
	@Resource
	FlexibleSearchService flexibleSearchService;
	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	UserService userService;

	@Autowired
	ModelService modelService;

	@Autowired
	EyDmsEndCustomerDao eydmsEndCustomerDao;

	/*@Resource
	EyDmsCustomerService eydmsCustomerService;

	@Resource
	ConfigurationService configurationService;*/

	@Autowired
	private DataConstraintDao dataConstraintDao;

	private static final Logger LOG = Logger.getLogger(SmsOtpServiceImpl.class);

	private static final Integer EXPIRE_MINS = 10;
	private static final Integer EXPIRE_SECS = 75;
	private LoadingCache<String,String> otpCache;
	private LoadingCache<String,Integer> otpCounter;
	private static final long otpExpirationTime = 600000l;
	long otpExpirationTime1;

	public SmsOtpServiceImpl() {
		super();
		otpCache = CacheBuilder.newBuilder().
				expireAfterWrite(EXPIRE_SECS, TimeUnit.SECONDS).build(new CacheLoader<String, String>() {
					public String load(String key) {
						return "";
					}
				});
		otpCounter = CacheBuilder.newBuilder().
				expireAfterWrite(EXPIRE_SECS, TimeUnit.SECONDS).build(new CacheLoader<String, Integer>() {
					public Integer load(String key) {
						return -1;
					}
				});

		int otpExpireTime = getOtpExpirationTime();
		otpExpirationTime1 = (long) otpExpireTime;
		LOG.info("otp expiration time through data constraint :: " + otpExpirationTime1);

	}

	public String getOtp(String key)
	{
		try{
			return otpCache.get(key);
		}catch (Exception e)
		{
			return "";
		}
	}

	public Integer getCounter(String key)
	{
		try{
			return otpCounter.get(key);
		}catch (Exception e)
		{
			return -1;
		}
	}

	public void increaseCounter(String key)
	{
		Integer counter = getCounter(key);
		if(counter>-1) {
			counter = counter+1;
			otpCounter.put(key, counter);
		}
	}

	public void clearOTP(String key)
	{
		otpCache.invalidate(key);
	}

	public void clearCounter(String key)
	{
		otpCounter.invalidate(key);
	}

	public Integer getOtpExpirationTime()
	{
		try{
			return dataConstraintDao.findDaysByConstraintName("OTP_EXPIRATION_TIME");
		}catch (Exception e)
		{
			return 0;
		}
	}

	@Override
	public String generateOTP(String key) {
		Random random = new Random();
		int otp = 100000 + random.nextInt(900000);
		String encodedOTP = passwordEncoder.encode(String.valueOf(otp));
		otpCache.put(key, encodedOTP);
		otpCounter.put(key, 0);
		return String.valueOf(otp);
	}

	@Override
	public boolean validateOTP(String otpNum, String uid) {
		Date date = new Date();
		long dateDifference = 0l;
		boolean result = false;
		String mobileNo = null;
		B2BCustomerModel customer = null;

		try {
			customer = (B2BCustomerModel) userService.getUserForUID(uid);
		}catch(Exception e)
		{
			LOG.error(e);
		}

		if(!Objects.isNull(customer))
		{
			mobileNo=customer.getMobileNumber();
		}
		else
		{
			mobileNo=uid;
		}
		if(mobileNo!=null) {
			//String serverOtp = getOtp(mobileNo);
			//new addition
			if(customer.getOtpCreationTime()!=null)
				dateDifference = date.getTime() - customer.getOtpCreationTime().getTime();

			if (customer.getOtpCreationTime() != null &&  dateDifference <= otpExpirationTime) {

				String serverOtp = "";

				if (customer.getEncodedOtp() != null)
					serverOtp = String.valueOf(customer.getEncodedOtp());

				if (passwordEncoder.matches(otpNum, serverOtp)) {
					result = true;
					clearOTP(customer, result);
				} else
					increaseCounter(customer);

				if (customer.getOtpCounter()!=null && customer.getOtpCounter() > 2) {
					clearOTP(customer, result);
				}
			}
			/*else
			{
				throw new UnknownIdentifierException("Your OTP has expired. Please request a new OTP.");
			}*/
		}
		return result;
	}

	private void increaseCounter(B2BCustomerModel customer) {
		if(customer.getOtpCounter()!=null) {
			int i = customer.getOtpCounter() + 1;
			customer.setOtpCounter(i);
			modelService.save(customer);
			modelService.refresh(customer);
		}
	}

	private void clearOTP(B2BCustomerModel customer, boolean result) {
		if(customer!=null)
		{
			customer.setEncodedOtp(null);
			customer.setOtpCreationTime(null);
			customer.setOtpCounter(null);
			customer.setLoginOtpVerified(result);
			if(result)
				customer.setOtpVerifiedDate(new Date());
			modelService.save(customer);
			modelService.refresh(customer);
		}
	}

	@Override
	public String sendSmsOtp(String mobileNo, String smsTemplate, String templateId) {
		//new addition
		Date date = new Date();
		String otp = "";
		String exisitingOtp ="";
		long dateDifference =0l;
		B2BCustomerModel b2BCustomerModel = eydmsEndCustomerDao.getEndCustomerDetails(mobileNo);
		//String exisitingOtp = getOtp(mobileNo);

		exisitingOtp = String.valueOf(b2BCustomerModel.getEncodedOtp());

		if(b2BCustomerModel.getOtpCreationTime()!=null) {
			dateDifference = date.getTime() - b2BCustomerModel.getOtpCreationTime().getTime();
		}

		if(StringUtils.isNotBlank(exisitingOtp))
		{
			clearOTP(b2BCustomerModel,false);
		}

		if(StringUtils.isBlank(exisitingOtp) || b2BCustomerModel.getOtpCreationTime()==null || dateDifference > otpExpirationTime1) {
			//String otp = generateOTP(mobileNo);
			if(b2BCustomerModel!=null) {
				otp = generateOTPFromDb(mobileNo, b2BCustomerModel);
			}

			final SMSProcessModel process = new SMSProcessModel();
			process.setNumber(mobileNo);
			process.setMessageContent("Your OTP for logging on to website is "+otp+". OTP is confidential please do not share it with anyone. OTP powered by Mtalkz.com");
			process.setTemplateId("1407162745305733462");

			final SendSMSEvent event = new SendSMSEvent(process);
			eventService.publishEvent(event);

			return otp;
		}
		else {
			throw new UnknownIdentifierException("An OTP has already been sent. Please try again later.");
		}
	}

	@Override
	public boolean sendChangePwdSmsOtp(String mobileNumber) {
		Date date = new Date();
		String exisitingOtp = "";
		long dateDifference = 0l;

		B2BCustomerModel customer = new B2BCustomerModel();
		customer.setMobileNumber(mobileNumber);

		B2BCustomerModel customerModel = null;

		try{
			customerModel = flexibleSearchService.getModelByExample(customer);
		}
		catch(ModelNotFoundException e)
		{
			LOG.warn("Mobile Number "+mobileNumber+" not found!");
		}

		if(Objects.isNull(customerModel))
		{
			return false;
		}

		exisitingOtp = String.valueOf(customerModel.getEncodedOtp());

		if(customerModel.getOtpCreationTime()!=null) {
			dateDifference = date.getTime() - customerModel.getOtpCreationTime().getTime();
		}

		if(StringUtils.isNotBlank(exisitingOtp))
		{
			clearOTP(customerModel,false);
		}

		//new addition
		//String otp = generateOTP(customerModel.getMobileNumber());
		if(StringUtils.isBlank(exisitingOtp) || customerModel.getOtpCreationTime()==null || dateDifference > otpExpirationTime1) {
			String otp = generateOTPFromDb(customerModel.getMobileNumber(), customerModel);

			final SMSProcessModel process = new SMSProcessModel();
			process.setNumber(mobileNumber);
			process.setMessageContent("Your OTP for logging on to website is " + otp + ". OTP is confidential please do not share it with anyone. OTP powered by Mtalkz.com");
			process.setTemplateId("1407162745305733462");

			final SendSMSEvent event = new SendSMSEvent(process);
			eventService.publishEvent(event);

			return true;
		}
		/*else if (customerModel.getOtpCreationTime() != null && dateDifference <= otpExpirationTime)
		{
			throw new UnknownIdentifierException("An OTP has already been sent. Please try again later.");
		}*/
		return false;
	}

	@Override
	public boolean sendOnboardingSmsOtp(String name, String mobileNo) {
		//new addition
		Date date = new Date();
		String otp ="";
		String exisitingOtp = "";
		long dateDifference = 0l;
		B2BCustomerModel b2BCustomerModel = eydmsEndCustomerDao.getEndCustomerDetails(mobileNo);

		if(b2BCustomerModel!=null)
		{
			exisitingOtp = String.valueOf(b2BCustomerModel.getEncodedOtp());

			if(b2BCustomerModel.getOtpCreationTime()!=null) {
				dateDifference = date.getTime() - b2BCustomerModel.getOtpCreationTime().getTime();
			}
		}
		if(StringUtils.isNotBlank(exisitingOtp))
		{
			clearOTP(b2BCustomerModel,false);
		}
		//String exisitingOtp = getOtp(mobileNo);
		if(StringUtils.isBlank(exisitingOtp) || b2BCustomerModel.getOtpCreationTime()==null || dateDifference > otpExpirationTime1) {
			//String otp = generateOTP(mobileNo);
			if(b2BCustomerModel!=null) {
				otp = generateOTPFromDb(mobileNo,b2BCustomerModel);
				String namee = "User-".concat(b2BCustomerModel.getName());

				String sms = "One Time Password for login " + namee + " is " + otp + ". This OTP expires in " + EXPIRE_MINS + " minutes. Pls. Call 1800276666 for any query. Shree Cement Ltd";
				final SMSProcessModel process = new SMSProcessModel();
				process.setNumber(mobileNo);
				process.setMessageContent(sms);
				process.setTemplateId("1707168362994139698");
				LOG.debug(sms);
				final SendSMSEvent event = new SendSMSEvent(process);
				eventService.publishEvent(event);
			}
			return Boolean.TRUE;
		}
		/*else if (b2BCustomerModel.getOtpCreationTime() != null && dateDifference <= otpExpirationTime)
		{
			throw new UnknownIdentifierException("An OTP has already been sent. Please try again later.");
		}*/
		return Boolean.FALSE;
	}

	@Override
	public boolean sendLoginSmsOtp(String uid, String mobileNo, String customerNo) {
		Date date = new Date();
		long dateDifference = 0l;
		B2BCustomerModel b2BCustomer = (B2BCustomerModel) userService.getUserForUID(uid);
		//String customerNo=b2BCustomer.getCustomerID()!=null?b2BCustomer.getCustomerID():null;
		//String exisitingOtp = getOtp(mobileNo);
		String exisitingOtp = "";
		if(b2BCustomer!=null)
		{
			exisitingOtp = String.valueOf(b2BCustomer.getEncodedOtp());

			if(b2BCustomer.getOtpCreationTime()!=null) {
				dateDifference = date.getTime() - b2BCustomer.getOtpCreationTime().getTime();
			}
		}

		if(StringUtils.isNotBlank(exisitingOtp))
		{
			clearOTP(b2BCustomer,false);
		}
		if(b2BCustomer!=null && !b2BCustomer.isLoginDisabled()) {
			if (StringUtils.isBlank(exisitingOtp) || b2BCustomer.getOtpCreationTime() == null || dateDifference > otpExpirationTime) {
				//String otp = generateOTP(mobileNo);
				String otp = generateOTPFromDb(mobileNo, b2BCustomer);

				String mobile = "number-".concat(mobileNo);
				//String sms = "Dear Reg. No. " + uid + ", One Time password for login " + mobile + " is " + otp + ". This OTP expires in " + EXPIRE_MINS + " minutes. Pls. Call 1800276666 for any query. Shree Cement Ltd";
				String sms = "Dear Reg. No. " + customerNo + ", One Time password for login " + mobile + " is " + otp + ". This OTP expires in " + EXPIRE_MINS + " minutes. Pls. Call 1800276666 for any query. Shree Cement Ltd";
				final SMSProcessModel process = new SMSProcessModel();
				process.setNumber(mobileNo);
				process.setMessageContent(sms);
				process.setTemplateId("1707168362973483369");
				LOG.debug(sms);
				final SendSMSEvent event = new SendSMSEvent(process);
				eventService.publishEvent(event);

				return Boolean.TRUE;
			}
		}
		/*else if (b2BCustomer.getOtpCreationTime() != null && dateDifference <= otpExpirationTime1)
		{
			throw new UnknownIdentifierException("An OTP has already been sent. Please try again later.");
		}*/
		return Boolean.FALSE;
	}

	@Override
	public boolean sendRedeemRequestSmsOtp(String uid, String reqValue, String reqType,
										   List<String> giftItems) {

		String otp="";
		String exisitingOtp = "";
		Date date = new Date();
		long dateDifference = 0l;
		B2BCustomerModel user = (B2BCustomerModel) userService.getUserForUID(uid);
		String customerNo=user.getCustomerID()!=null?user.getCustomerID():null;
		if(!Objects.isNull(user))
		{
			String mobileNo = user.getMobileNumber();

			exisitingOtp = String.valueOf(user.getEncodedOtp());

			if(user.getOtpCreationTime()!=null) {
				dateDifference = date.getTime() - user.getOtpCreationTime().getTime();
			}

			if(StringUtils.isNotBlank(exisitingOtp))
			{
				clearOTP(user,false);
			}

			//String exisitingOtp = getOtp(mobileNo);
			if(StringUtils.isBlank(exisitingOtp) || user.getOtpCreationTime()==null || dateDifference > otpExpirationTime1) {
				//String otp = generateOTP(mobileNo);

				//eydmsCustomerService.checkMobileNumberValidation(mobileNo);
				otp = generateOTPFromDb(mobileNo,user);
				String sms = null;

				if(reqType.equalsIgnoreCase("CASH"))
				{
					sms = "Dear Reg. No. " + ((customerNo!=null)?customerNo:uid) + ", Gift redeem request value " + reqValue + " Pts. For item " + reqType + ", " + reqType +  " is submitted. OTP is " + otp + ". Pls. Call 1800276666 for any query. Shree Cement Ltd";
				}
				else
				{
					String gifts = null;
					for(String str: giftItems)
					{
						if(gifts==null)
						{
							gifts = str;
						}
						else
						{
							gifts = gifts + "," + str;

						}

					}

					sms = "Dear Reg. No. " + ((customerNo!=null)?customerNo:uid) + ", Gift redeem request value " + reqValue + " Pts. For item " + reqType + ", " + gifts +  " is submitted. OTP is " + otp + ". Pls. Call 1800276666 for any query. Shree Cement Ltd";
				}

				final SMSProcessModel process = new SMSProcessModel();
				process.setNumber(mobileNo);
				process.setMessageContent(sms);
				process.setTemplateId("1707168326617211114");
				LOG.debug(sms);
				final SendSMSEvent event = new SendSMSEvent(process);
				eventService.publishEvent(event);

				return Boolean.TRUE;
			}
		/*	else if (user.getOtpCreationTime() != null && dateDifference <= otpExpirationTime)
			{
				throw new UnknownIdentifierException("An OTP has already been sent. Please try again later.");
			}*/
			return Boolean.FALSE;
		}
		else
		{
			return Boolean.FALSE;
		}

	}

	@Override
	public boolean sendRedeemRequestPlacedSms(String uid, String reqValue, String reqType,
											  List<String> giftItems) {

		B2BCustomerModel user = (B2BCustomerModel) userService.getUserForUID(uid);
		String customerNo=user.getCustomerID()!=null?user.getCustomerID():null;
		String otp="";
		String exisitingOtp = "";
		Date date = new Date();
		long dateDifference = 0l;
		if(!Objects.isNull(user))
		{
			String mobileNo = user.getMobileNumber();

			exisitingOtp = String.valueOf(user.getEncodedOtp());

			if(user.getOtpCreationTime()!=null) {
				dateDifference = date.getTime() - user.getOtpCreationTime().getTime();
			}

			if(StringUtils.isNotBlank(exisitingOtp))
			{
				clearOTP(user,false);
			}

			if(StringUtils.isBlank(exisitingOtp) || user.getOtpCreationTime()==null || dateDifference > otpExpirationTime1) {

				//eydmsCustomerService.checkMobileNumberValidation(mobileNo);
				otp = generateOTPFromDb(mobileNo, user);

				String sms = null;

				if (reqType.equalsIgnoreCase("CASH")) {
					sms = "Dear Reg. No. " + ((customerNo!=null)?customerNo:uid) + ", Gift redeem request value Pts. For item " + reqType + ", " + reqType + " is successfully placed. Pls. Call 1800276666 for any query. Shree Cement Ltd";
				} else {
					String gifts = null;
					for (String str : giftItems) {
						if (gifts == null) {
							gifts = str;
						} else {
							gifts = gifts + "," + str;

						}

					}

					sms = "Dear Reg. No. " + ((customerNo!=null)?customerNo:uid) + ", Gift redeem request value Pts. For item " + reqType + ", " + gifts + " is successfully placed. Pls. Call 1800276666 for any query. Shree Cement Ltd";
				}

				final SMSProcessModel process = new SMSProcessModel();
				process.setNumber(mobileNo);
				process.setMessageContent(sms);
				process.setTemplateId("1707168326605209538");
				LOG.debug(sms);
				final SendSMSEvent event = new SendSMSEvent(process);
				eventService.publishEvent(event);

				return Boolean.TRUE;
			}
			/*else if (user.getOtpCreationTime() != null && dateDifference <= otpExpirationTime)
			{
				throw new UnknownIdentifierException("An OTP has already been sent. Please try again later.");
			}*/
			return Boolean.FALSE;
		}
		else
		{
			return Boolean.FALSE;
		}
	}

	@Override
	public boolean sendRedeemRequestApprovedSms(String uid, String reqValue, String reqType, List<String> giftItems) {
		String exisitingOtp = "";
		Date date = new Date();
		long dateDifference = 0l;
		String otp="";

		B2BCustomerModel user = (B2BCustomerModel) userService.getUserForUID(uid);
		String customerNo=user.getCustomerID()!=null?user.getCustomerID():null;
		if (!Objects.isNull(user)) {
			String mobileNo = user.getMobileNumber();

			exisitingOtp = String.valueOf(user.getEncodedOtp());

			if (user.getOtpCreationTime() != null) {
				dateDifference = date.getTime() - user.getOtpCreationTime().getTime();
			}

			if(StringUtils.isNotBlank(exisitingOtp))
			{
				clearOTP(user,false);
			}

			if (StringUtils.isBlank(exisitingOtp) || user.getOtpCreationTime() == null || dateDifference > otpExpirationTime1) {
				String sms = null;

				//eydmsCustomerService.checkMobileNumberValidation(mobileNo);
				otp = generateOTPFromDb(mobileNo, user);

				if (reqType.equalsIgnoreCase("CASH")) {
					sms = "Dear Reg. No. " + ((customerNo!=null)?customerNo:uid) + ", Gift redeem request value " + reqValue + " Pts. For item " + reqType + ", " + reqType + " is accepted. Pls. Call 1800276666 for any query. Shree Cement Ltd";
				} else {
					String gifts = null;
					for (String str : giftItems) {
						if (gifts == null) {
							gifts = str;
						} else {
							gifts = gifts + "," + str;

						}

					}

					sms = "Dear Reg. No. " + ((customerNo!=null)?customerNo:uid) + ", Gift redeem request value " + reqValue + " Pts. For item " + reqType + ", " + gifts + " is accepted. Pls. Call 1800276666 for any query. Shree Cement Ltd";
				}

				final SMSProcessModel process = new SMSProcessModel();
				process.setNumber(mobileNo);
				process.setMessageContent(sms);
				process.setTemplateId("1707168326622556172");
				LOG.debug(sms);
				final SendSMSEvent event = new SendSMSEvent(process);
				eventService.publishEvent(event);

				return Boolean.TRUE;
			}
			/*else if (user.getOtpCreationTime() != null && dateDifference <= otpExpirationTime) {
				throw new UnknownIdentifierException("An OTP has already been sent. Please try again later.");
			}*/
		}
		return Boolean.FALSE;
	}

	@Override
	public boolean sendRedeemRequestRejectedSms(String uid, String reqValue, String reqType, List<String> giftItems) {
		String exisitingOtp = "";
		Date date = new Date();
		long dateDifference = 0l;
		String otp="";

		B2BCustomerModel user = (B2BCustomerModel) userService.getUserForUID(uid);
		String customerNo=user.getCustomerID()!=null?user.getCustomerID():null;
		if(!Objects.isNull(user))
		{
			String mobileNo = user.getMobileNumber();

			exisitingOtp = String.valueOf(user.getEncodedOtp());

			if (user.getOtpCreationTime() != null) {
				dateDifference = date.getTime() - user.getOtpCreationTime().getTime();
			}

			if(StringUtils.isNotBlank(exisitingOtp))
			{
				clearOTP(user,false);
			}

			if (StringUtils.isBlank(exisitingOtp) || user.getOtpCreationTime() == null || dateDifference > otpExpirationTime) {
				String sms = null;

				//eydmsCustomerService.checkMobileNumberValidation(mobileNo);
				otp = generateOTPFromDb(mobileNo, user);
				if (reqType.equalsIgnoreCase("CASH")) {
					sms = "Dear Reg. No. " + ((customerNo!=null)?customerNo:uid) + ", Gift redeem request value " + reqValue + " Pts. For item " + reqType + ", " + reqType + " is declined. Pls. Call 1800276666 for any query. Shree Cement Ltd";
				} else {
					String gifts = null;
					for (String str : giftItems) {
						if (gifts == null) {
							gifts = str;
						} else {
							gifts = gifts + "," + str;

						}

					}

					sms = "Dear Reg. No. " + ((customerNo!=null)?customerNo:uid) + ", Gift redeem request value " + reqValue + " Pts. For item " + reqType + ", " + gifts + " is declined. Pls. Call 1800276666 for any query. Shree Cement Ltd";
				}

				final SMSProcessModel process = new SMSProcessModel();
				process.setNumber(mobileNo);
				process.setMessageContent(sms);
				process.setTemplateId("1707168326630202418");
				LOG.debug(sms);
				final SendSMSEvent event = new SendSMSEvent(process);
				eventService.publishEvent(event);

				return Boolean.TRUE;
			}
			/*else if (user.getOtpCreationTime() != null && dateDifference <= otpExpirationTime) {
				throw new UnknownIdentifierException("An OTP has already been sent. Please try again later.");
			}*/
			return Boolean.FALSE;
		}
		else
		{
			return Boolean.FALSE;
		}
	}

	@Override
	public String generateOTPFromDb(String key, B2BCustomerModel b2BCustomer) {
		Random random = new Random();
		int otp = 100000 + random.nextInt(900000);
		String encodedOTP = passwordEncoder.encode(String.valueOf(otp));

		if(b2BCustomer!=null)
		{
			b2BCustomer.setEncodedOtp(encodedOTP);
			b2BCustomer.setOtpCreationTime(new Date());
			b2BCustomer.setOtpCounter(0);
			modelService.save(b2BCustomer);
		}
		return String.valueOf(otp);
	}

	/*public ConfigurationService getConfigurationService() {
		return configurationService;
	}

	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}*/
}