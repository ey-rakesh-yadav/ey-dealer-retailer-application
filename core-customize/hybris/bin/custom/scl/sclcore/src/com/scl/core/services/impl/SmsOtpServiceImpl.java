package com.scl.core.services.impl;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import com.scl.core.constants.SclCoreConstants;
import com.scl.core.customer.dao.SclEndCustomerDao;
import com.scl.core.dao.DataConstraintDao;
import com.scl.core.enums.OTPStatus;
import com.scl.core.event.SendOTPEmailEvent;
import com.scl.core.model.*;
import de.hybris.platform.b2b.model.B2BCustomerModel;

import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.google.common.cache.LoadingCache;
import com.scl.core.event.SendSMSEvent;
import com.scl.core.services.SmsOtpService;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.event.EventService;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.user.UserService;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.store.services.BaseStoreService;

public class SmsOtpServiceImpl implements SmsOtpService{

	private static final String SEND_OTP_OVER_SMS_TO = "sendOtpOverSmsTo";
	private static final String SEND_OTP_OVER_EMAIL_TO = "sendOtpOverEmailTo";

	private static final String SEND_LOGIN_OTP_OVER_SMS_TO = "sendLoginOtpOverSmsTo";
	private static final String SEND_LOGIN_OTP_OVER_EMAIL_TO = "sendLoginOtpOverEmailTo";
        private static final String  SEND_PARTNER_LOGIN_CUSTOMER_OTP_OVER_EMAIL_TO ="sendPartnerLoginOtpOverEmailTo";
	private static final String SEND_PARTNER_LOGIN_OTP_OVER_SMS_TO = "sendPartnerLoginOtpOverSmsTo";
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
	SclEndCustomerDao sclEndCustomerDao;

	/*@Resource
	SclCustomerService sclCustomerService;

	@Resource
	ConfigurationService configurationService;*/

	@Autowired
	private DataConstraintDao dataConstraintDao;

	@Autowired
	private KeyGenerator customerGeneratedOtpIdGenerator;

	@Autowired
	private CommonI18NService commonI18NService;
	@Autowired
	private BaseSiteService baseSiteService;
	@Autowired
	private BaseStoreService baseStoreService;

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
	public boolean validateOTP(String otpNum, String uid, String partnerID) {
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
		if(StringUtils.isNotEmpty(partnerID)){
			SclCustomerModel sclCustomerModel= (SclCustomerModel) customer;
			List<PartnerCustomerModel> partnerCustomerModel = sclCustomerModel.getPartnerCustomer().stream().filter(a -> a.getId().equalsIgnoreCase(partnerID)).collect(Collectors.toList());
			mobileNo = partnerCustomerModel.get(0).getMobileNumber();
			if(mobileNo!=null) {
				if(partnerCustomerModel.get(0).getOtpCreationTime()!=null)
					dateDifference = date.getTime() - partnerCustomerModel.get(0).getOtpCreationTime().getTime();
				if (partnerCustomerModel.get(0).getOtpCreationTime() != null &&  dateDifference <= otpExpirationTime) {

					String serverOtp = "";

					if (partnerCustomerModel.get(0).getEncodedOtp() != null)
						serverOtp = String.valueOf(partnerCustomerModel.get(0).getEncodedOtp());

					if (passwordEncoder.matches(otpNum, serverOtp)) {
						updateCustomerGeneratedOtp(otpNum, partnerCustomerModel.get(0),sclCustomerModel);
						result = true;
						clearOTPPartner(partnerCustomerModel.get(0), result);
					} else
						increaseCounterPartner(partnerCustomerModel.get(0));

					if (partnerCustomerModel.get(0).getOtpCounter()!=null && partnerCustomerModel.get(0).getOtpCounter() > 2) {
						clearOTPPartner(partnerCustomerModel.get(0), result);
					}
				}
			}
		}
       else {
			if (!Objects.isNull(customer)) {
				mobileNo = customer.getMobileNumber();
			} else {
				mobileNo = uid;
			}
			if (mobileNo != null) {
				//String serverOtp = getOtp(mobileNo);
				//new addition
				if (customer.getOtpCreationTime() != null)
					dateDifference = date.getTime() - customer.getOtpCreationTime().getTime();

				if (customer.getOtpCreationTime() != null && dateDifference <= otpExpirationTime) {

					String serverOtp = "";

					if (customer.getEncodedOtp() != null)
						serverOtp = String.valueOf(customer.getEncodedOtp());

					if (passwordEncoder.matches(otpNum, serverOtp)) {
						updateCustomerGeneratedOtp(otpNum, null,customer);
						result = true;
						clearOTP(customer, result);
					} else
						increaseCounter(customer);

					if (customer.getOtpCounter() != null && customer.getOtpCounter() > 2) {
						clearOTP(customer, result);
					}
				}
			}
		}
		return result;
	}

	private void updateCustomerGeneratedOtp(String otpNum, PartnerCustomerModel partnerCustomerModel,B2BCustomerModel customerModel) {
		List<CustomerGeneratedOtpModel> generatedOtpModels = null;
		if(Objects.nonNull(partnerCustomerModel)) {
			LOG.info(String.format("updateCustomerGeneratedOtp for partner customer::%s",partnerCustomerModel.getId()));
			generatedOtpModels = sclEndCustomerDao.fetchGeneratedOtpForCustomer(null, partnerCustomerModel);
		} else {
			LOG.info(String.format("updateCustomerGeneratedOtp for customer::%s",customerModel.getUid()));
			generatedOtpModels = sclEndCustomerDao.fetchGeneratedOtpForCustomer(customerModel,null);
		}
		if(CollectionUtils.isNotEmpty(generatedOtpModels)){
			Optional<CustomerGeneratedOtpModel> matchedModel=generatedOtpModels.stream().filter(generatedOtpModel->
					StringUtils.isNotBlank(generatedOtpModel.getEncodedOtpCode()) &&  passwordEncoder.matches(otpNum,generatedOtpModel.getEncodedOtpCode())).findFirst();
			 if(matchedModel.isPresent()){
				 CustomerGeneratedOtpModel generatedOtpModel= matchedModel.get();
				 generatedOtpModel.setOtpStatus(OTPStatus.CONSUMED);
				 generatedOtpModel.setOtpVerifiedDate(new Date());
				 generatedOtpModel.setLoginOtpVerified(Boolean.TRUE);
				 modelService.save(generatedOtpModel);
				 modelService.refresh(generatedOtpModel);
				 customerModel.setLastLoginByOtp(new Date());
				 modelService.save(customerModel);
			 }
		}
	}

	private void increaseCounterPartner(PartnerCustomerModel partnerCustomerModel) {
		if(partnerCustomerModel.getOtpCounter()!=null) {
			int i = partnerCustomerModel.getOtpCounter() + 1;
			partnerCustomerModel.setOtpCounter(i);
			modelService.save(partnerCustomerModel);
			modelService.refresh(partnerCustomerModel);
		}
	}

	private void clearOTPPartner(PartnerCustomerModel partnerCustomerModel, boolean result) {
		if(partnerCustomerModel!=null)
		{
			partnerCustomerModel.setEncodedOtp(null);
			partnerCustomerModel.setOtpCreationTime(null);
			partnerCustomerModel.setOtpCounter(null);
			partnerCustomerModel.setLoginOtpVerified(result);
			if(result)
				partnerCustomerModel.setOtpVerifiedDate(new Date());
			modelService.save(partnerCustomerModel);
			modelService.refresh(partnerCustomerModel);
		}
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
		B2BCustomerModel b2BCustomerModel = sclEndCustomerDao.getEndCustomerDetails(mobileNo);
		UserGroupModel dealerGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID);
		UserGroupModel retailerGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID);
		boolean isEmailsend=false;
		boolean isSMSsend=false;

		if(b2BCustomerModel.getGroups() !=null && b2BCustomerModel.getGroups().contains(dealerGroup)) {
			isSMSsend= dataConstraintDao.findVersionByConstraintName(SEND_OTP_OVER_SMS_TO +SclCoreConstants.COUNTER_TYPE.DEALER).equalsIgnoreCase("true");
			isEmailsend= dataConstraintDao.findVersionByConstraintName(SEND_OTP_OVER_EMAIL_TO +SclCoreConstants.COUNTER_TYPE.DEALER).equalsIgnoreCase("true");
		}else if(b2BCustomerModel.getGroups() !=null && b2BCustomerModel.getGroups().contains(retailerGroup)){
			isSMSsend= dataConstraintDao.findVersionByConstraintName(SEND_OTP_OVER_SMS_TO+SclCoreConstants.COUNTER_TYPE.RETAILER).equalsIgnoreCase("true");
			isEmailsend= dataConstraintDao.findVersionByConstraintName(SEND_OTP_OVER_EMAIL_TO+SclCoreConstants.COUNTER_TYPE.RETAILER).equalsIgnoreCase("true");
		}



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

			final OTPEmailProcessModel emailProcess = new OTPEmailProcessModel();
			emailProcess.setOtpCode(otp);
			emailProcess.setCustomer(b2BCustomerModel);

			//final SendOTPEmailEvent emailEvent = new SendOTPEmailEvent(emailProcess);
			SendOTPEmailEvent emailEvent = new SendOTPEmailEvent(emailProcess);
			emailEvent.setSite(getBaseSiteService().getCurrentBaseSite());
			emailEvent.setBaseStore(getBaseStoreService().getCurrentBaseStore());
			emailEvent.setCustomer(b2BCustomerModel);
			emailEvent.setLanguage(getCommonI18NService().getCurrentLanguage());
			emailEvent.setCurrency(getCommonI18NService().getCurrentCurrency());
			if(isSMSsend) {
				eventService.publishEvent(event);
			}
			if(isEmailsend) {
				eventService.publishEvent(emailEvent);
			}

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
		B2BCustomerModel b2BCustomerModel = sclEndCustomerDao.getEndCustomerDetails(mobileNo);

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
	public boolean sendLoginSmsOtp(B2BCustomerModel customer,PartnerCustomerModel selectedPartner,String partnerCustomerFlag) {
		Date date = new Date();
		long dateDifference = 0l;
		CustomerGeneratedOtpModel  customerGeneratedOtp = modelService.create(CustomerGeneratedOtpModel.class);
		List<CustomerGeneratedOtpModel> generatedOtpModels = null;
		if (Objects.nonNull(selectedPartner) &&  (StringUtils.isNotBlank(partnerCustomerFlag)&& partnerCustomerFlag.equalsIgnoreCase("TRUE"))) {
			generatedOtpModels=sclEndCustomerDao.fetchGeneratedOtpForCustomer(null,selectedPartner);
		}else {
			generatedOtpModels=sclEndCustomerDao.fetchGeneratedOtpForCustomer(customer,null);
		}

		String exisitingOtp = "";
		if(CollectionUtils.isNotEmpty(generatedOtpModels)){
			for (CustomerGeneratedOtpModel generatedOtp:generatedOtpModels) {
				if(StringUtils.isNotBlank(generatedOtp.getEncodedOtpCode()) && Objects.nonNull(generatedOtp.getOtpCreationDate())){
					exisitingOtp = String.valueOf(generatedOtp.getEncodedOtpCode());
					dateDifference = date.getTime() - generatedOtp.getOtpCreationDate().getTime();
				   if(dateDifference > otpExpirationTime){
					   generatedOtp.setOtpStatus(OTPStatus.AUTO_EXPIRED);
					   generatedOtp.setOtpExpiredDate(new Date());
				   } else if (dateDifference < otpExpirationTime) {
					   generatedOtp.setOtpStatus(OTPStatus.EXPIRED_NEW_OTP_GENERATED);
					   generatedOtp.setOtpExpiredDate(new Date());
				   }
					modelService.save(generatedOtp);
					modelService.refresh(generatedOtp);
				}
			}
		}
		/*if(b2BCustomer!=null)
		{
			exisitingOtp = String.valueOf(b2BCustomer.getEncodedOtp());
		}*/
		if(StringUtils.isNotBlank(exisitingOtp))
		{
			if(Objects.nonNull(selectedPartner))
				clearOTPPartner(selectedPartner,false);
			else
				clearOTP(customer,false);
		}
		if(Objects.nonNull(selectedPartner)) {

				int generatedOtp = generateOTPFromPartner(selectedPartner.getMobileNumber(), selectedPartner,customerGeneratedOtp);
				boolean isEmailsend = false;
				boolean isSMSsend = false;
				isSMSsend = dataConstraintDao.findVersionByConstraintName(SEND_PARTNER_LOGIN_OTP_OVER_SMS_TO).equalsIgnoreCase("true");
			    isEmailsend = dataConstraintDao.findVersionByConstraintName(SEND_PARTNER_LOGIN_CUSTOMER_OTP_OVER_EMAIL_TO).equalsIgnoreCase("true");
				sendPartnerCustomerOTPVia_SMS_Email(selectedPartner.getMobileNumber(), customer, selectedPartner, String.valueOf(generatedOtp), isEmailsend, isSMSsend,customerGeneratedOtp);
				return Boolean.TRUE;
		}
		else if(customer!=null && !customer.isLoginDisabled()) {
			//if (StringUtils.isBlank(exisitingOtp) || b2BCustomer.getOtpCreationTime() == null || dateDifference > otpExpirationTime) {
				//String otp = generateOTP(mobileNo);
				//String otp = generateOTPFromDb(mobileNo,  b2BCustomer);
				try {

					int generatedOtp   = generateCustomerOTPFromDb(customer.getMobileNumber(), customer,customerGeneratedOtp);
					UserGroupModel dealerGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID);
					UserGroupModel retailerGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID);
					boolean isEmailsend = false;
					boolean isSMSsend = false;
					if (customer.getGroups() != null && customer.getGroups().contains(dealerGroup)) {
						isSMSsend = dataConstraintDao.findVersionByConstraintName(SEND_LOGIN_OTP_OVER_SMS_TO + SclCoreConstants.COUNTER_TYPE.DEALER).equalsIgnoreCase("true");
						isEmailsend = dataConstraintDao.findVersionByConstraintName(SEND_LOGIN_OTP_OVER_EMAIL_TO + SclCoreConstants.COUNTER_TYPE.DEALER).equalsIgnoreCase("true");
					} else if (customer.getGroups() != null && customer.getGroups().contains(retailerGroup)) {
						isSMSsend = dataConstraintDao.findVersionByConstraintName(SEND_LOGIN_OTP_OVER_SMS_TO + SclCoreConstants.COUNTER_TYPE.RETAILER).equalsIgnoreCase("true");
						isEmailsend = dataConstraintDao.findVersionByConstraintName(SEND_LOGIN_OTP_OVER_EMAIL_TO + SclCoreConstants.COUNTER_TYPE.RETAILER).equalsIgnoreCase("true");
					}
                     String emailId=StringUtils.isNotBlank(customer.getEmail())?customer.getEmail(): Strings.EMPTY;
					sendOTPVia_SMS_Email(customer.getMobileNumber(),customer, String.valueOf(generatedOtp), isEmailsend, isSMSsend,customerGeneratedOtp,emailId);

					return Boolean.TRUE;
				}catch (Exception ex){
                 LOG.error(String.format("Exception occurred in sendLoginSmsOtp service with message::%s ",ex.getMessage()));
				}
			//}
		}

		/*else if (b2BCustomer.getOtpCreationTime() != null && dateDifference <= otpExpirationTime1)
		{
			throw new UnknownIdentifierException("An OTP has already been sent. Please try again later.");
		}*/
		return Boolean.FALSE;
	}

	private void sendPartnerCustomerOTPVia_SMS_Email(String mobileNo, B2BCustomerModel customerModel, PartnerCustomerModel selectedPartner, String otp, boolean isEmailsend, boolean isSMSsend, CustomerGeneratedOtpModel customerGeneratedOtp) {
		if(isSMSsend) {
			triggerLoginOTPSMSEvent(mobileNo, customerModel, otp, customerGeneratedOtp);
		}else{
			LOG.info(String.format("Login OTP SMS feature is disabled for user with Uid %s", customerModel.getUid()));
		}
		if(isEmailsend) {
			triggerLoginPartnerOTPEmailEvent(selectedPartner,customerModel, otp,customerGeneratedOtp);
		}else{
			LOG.info(String.format("Login OTP Email feature is disabled for user with Uid %s", customerModel.getUid()));
		}
	}

	private void triggerLoginPartnerOTPEmailEvent(PartnerCustomerModel selectedPartner, B2BCustomerModel customerModel, String otp, CustomerGeneratedOtpModel customerGeneratedOtp) {
		final OTPEmailProcessModel emailProcess = new OTPEmailProcessModel();
		emailProcess.setOtpCode(otp);
		emailProcess.setCustomer(selectedPartner.getSclcustomer());
		emailProcess.setCode("sendOTPEmailProcess-partner-" + customerModel.getUid() + "-" + System.currentTimeMillis());
		emailProcess.setProcessDefinitionName("sendOTPEmailProcess");
		modelService.save(emailProcess);
		SendOTPEmailEvent emailEvent = new SendOTPEmailEvent(emailProcess);
		emailEvent.setSite(getBaseSiteService().getCurrentBaseSite());
		emailEvent.setBaseStore(getBaseStoreService().getCurrentBaseStore());
		emailEvent.setCustomer(selectedPartner.getSclcustomer());
		emailEvent.setLanguage(getCommonI18NService().getCurrentLanguage());
		emailEvent.setCurrency(getCommonI18NService().getCurrentCurrency());

		customerGeneratedOtp.setEmailProcess(emailProcess);
		customerGeneratedOtp.setEmailId(customerModel.getEmail());
		modelService.save(customerGeneratedOtp);
		eventService.publishEvent(emailEvent);
	}

	private int generateOTPFromPartner(String mobileNo, PartnerCustomerModel selectedPartner,CustomerGeneratedOtpModel customerGeneratedOtp) {
		Random random = new Random();
		int otp = 100000 + random.nextInt(900000);
		String encodedOTP = passwordEncoder.encode(String.valueOf(otp));
		if (selectedPartner != null) {
			selectedPartner.setEncodedOtp(encodedOTP);
			selectedPartner.setOtpCreationTime(new Date());
			selectedPartner.setOtpCounter(0);
			modelService.save(selectedPartner);
			// customerGeneratedOtp = modelService.create(CustomerGeneratedOtpModel.class);
			customerGeneratedOtp.setId(customerGeneratedOtpIdGenerator.generate().toString());
			 customerGeneratedOtp.setPartnerCustomer(selectedPartner);
			 if(selectedPartner.getSclcustomer() instanceof SclCustomerModel)
			  customerGeneratedOtp.setCustomer((SclCustomerModel)selectedPartner.getSclcustomer());
			 String generatedOtp=String.valueOf(otp);
			 String maskedOtp=generatedOtp.replaceAll( "\\d(?=(?:\\D*\\d){0,3}\\D*$)", "*" );
			 customerGeneratedOtp.setOtpCode(generatedOtp);
			customerGeneratedOtp.setEncodedOtpCode(encodedOTP);
			customerGeneratedOtp.setOtpStatus(OTPStatus.GENERATED);
			customerGeneratedOtp.setOtpCreationDate(new Date());
			customerGeneratedOtp.setMobileNo(mobileNo);
			modelService.save(customerGeneratedOtp);
			modelService.refresh(customerGeneratedOtp);
		}
		return otp ;
	}

	private PartnerCustomerModel getpartnerUid(String uid) {
		final Map<String, Object> params = new HashMap<String, Object>();
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT {pe.pk} FROM {PartnerCustomer AS pe} WHERE {pe:id}=?uid");
		params.put("uid", uid);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(PartnerCustomerModel.class));
		query.addQueryParameters(params);
		final SearchResult<PartnerCustomerModel> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult()!=null?searchResult.getResult().get(0):null;
	}

	/**
	 * Send OTP over SMS or Email
	 *
	 * @param mobileNo
	 * @param b2BCustomer
	 * @param otp
	 * @param isEmailsend
	 * @param isSMSsend
	 * @param customerGeneratedOtp
	 * @param emailId
	 */
	private void sendOTPVia_SMS_Email(String mobileNo, B2BCustomerModel b2BCustomer, String otp, boolean isEmailsend, boolean isSMSsend, CustomerGeneratedOtpModel customerGeneratedOtp, String emailId) {
		if(isSMSsend) {
			triggerLoginOTPSMSEvent(mobileNo, b2BCustomer, otp,customerGeneratedOtp);
		}else{
			LOG.info(String.format("Login OTP SMS feature is disabled for user with Uid %s", b2BCustomer.getUid()));
		}
		if(isEmailsend) {
			triggerLoginOTPEmailEvent(b2BCustomer, otp,customerGeneratedOtp,emailId);
		}else{
			LOG.info(String.format("Login OTP Email feature is disabled for user with Uid %s", b2BCustomer.getUid()));
		}
	}

	/**
	 * Trigger LOGIN OTP Email Event
	 *
	 * @param b2BCustomer
	 * @param otp
	 * @param customerGeneratedOtp
	 * @param emailId
	 */
	private void triggerLoginOTPEmailEvent(B2BCustomerModel b2BCustomer, String otp, CustomerGeneratedOtpModel customerGeneratedOtp, String emailId) {
		final OTPEmailProcessModel emailProcess = new OTPEmailProcessModel();
		emailProcess.setOtpCode(otp);
		emailProcess.setCustomer(b2BCustomer);
		emailProcess.setCode("sendOTPEmailProcess-" + b2BCustomer.getUid() + "-" + System.currentTimeMillis());
		emailProcess.setProcessDefinitionName("sendOTPEmailProcess");
		modelService.save(emailProcess);
		SendOTPEmailEvent emailEvent = new SendOTPEmailEvent(emailProcess);
		emailEvent.setSite(getBaseSiteService().getCurrentBaseSite());
		emailEvent.setBaseStore(getBaseStoreService().getCurrentBaseStore());
		emailEvent.setCustomer(b2BCustomer);
		emailEvent.setLanguage(getCommonI18NService().getCurrentLanguage());
		emailEvent.setCurrency(getCommonI18NService().getCurrentCurrency());
		customerGeneratedOtp.setEmailProcess(emailProcess);
		customerGeneratedOtp.setEmailId(emailId);
		modelService.save(customerGeneratedOtp);
		eventService.publishEvent(emailEvent);
	}

	/**
	 * Trigger Login OTP SMS Message
	 *
	 * @param mobileNo
	 * @param customerModel
	 * @param otp
	 * @param customerGeneratedOtp
	 */
	private void triggerLoginOTPSMSEvent(String mobileNo, B2BCustomerModel customerModel, String otp, CustomerGeneratedOtpModel customerGeneratedOtp) {
		String customerNo = customerModel.getUid();

		String mobile = "number-".concat(mobileNo);
		//String sms = "Dear Reg. No. " + uid + ", One Time password for login " + mobile + " is " + otp + ". This OTP expires in " + EXPIRE_MINS + " minutes. Pls. Call 1800276666 for any query. Shree Cement Ltd";
		/*StringBuilder sms = new StringBuilder("Dear Reg. No. ").append(customerNo).append(", One Time password for login ").append(mobile).append(" is ").append(otp)
                .append(". This OTP expires in " + EXPIRE_MINS + " minutes. Pls. Call 1800276666 for any query. Shree Cement Ltd");
		*/
		//B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();

		
		final SMSProcessModel process = new SMSProcessModel();
		process.setNumber(mobileNo);
		process.setOtp(otp);
		//process.setUser(currentUser);
		//process.setMessageContent(sms.toString());
		//process.setTemplateId("1707168362973483369");
		process.setCustomer(customerModel);
		process.setCode("sms-process-"+customerNo+ "-" +System.currentTimeMillis());
		process.setProcessDefinitionName("sms-process");
		modelService.save(process);
		final SendSMSEvent event = new SendSMSEvent(process);
		customerGeneratedOtp.setSmsProcess(process);
		modelService.save(customerGeneratedOtp);
		modelService.refresh(customerGeneratedOtp);
		eventService.publishEvent(event);
        LOG.info(String.format("Login OTP SMS sent to user with uid :%s having mobile number :%s",customerNo,mobileNo));
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

				//sclCustomerService.checkMobileNumberValidation(mobileNo);
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

				//sclCustomerService.checkMobileNumberValidation(mobileNo);
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

				//sclCustomerService.checkMobileNumberValidation(mobileNo);
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

				//sclCustomerService.checkMobileNumberValidation(mobileNo);
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

	/**
	 * @param key
	 * @param b2BCustomer
	 * @param customerGeneratedOtp
	 * @return
	 */
	@Override
	public int generateCustomerOTPFromDb(String key, B2BCustomerModel b2BCustomer,CustomerGeneratedOtpModel generatedOtpModel) {

		Random random = new Random();
		int otp = 100000 + random.nextInt(900000);
		String encodedOTP = passwordEncoder.encode(String.valueOf(otp));
		if(b2BCustomer!=null)
		{
			b2BCustomer.setEncodedOtp(encodedOTP);
			b2BCustomer.setOtpCreationTime(new Date());
			b2BCustomer.setOtpCounter(0);
			modelService.save(b2BCustomer);
			//generatedOtpModel = modelService.create(CustomerGeneratedOtpModel.class);
			generatedOtpModel.setId(customerGeneratedOtpIdGenerator.generate().toString());
			generatedOtpModel.setCustomer(b2BCustomer);
			String generatedOtp=String.valueOf(otp);
			String maskedOtp=generatedOtp.replaceAll( "\\d(?=(?:\\D*\\d){0,3}\\D*$)", "*" );
			generatedOtpModel.setOtpCode(maskedOtp);
			generatedOtpModel.setOtpCreationDate(new Date());
			generatedOtpModel.setEncodedOtpCode(encodedOTP);
			generatedOtpModel.setOtpStatus(OTPStatus.GENERATED);
			generatedOtpModel.setMobileNo(b2BCustomer.getMobileNumber());
			modelService.save(generatedOtpModel);
			modelService.refresh(generatedOtpModel);


			LOG.info(String.format("Customer generated otp model saved for customer ::%s with mobile no::%s",b2BCustomer.getUid(),key));
		}
		return otp;
	}

	/*public ConfigurationService getConfigurationService() {
		return configurationService;
	}

	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}*/

		public CommonI18NService getCommonI18NService() {
		return commonI18NService;
	}

	public void setCommonI18NService(CommonI18NService commonI18NService) {
		this.commonI18NService = commonI18NService;
	}

	public BaseSiteService getBaseSiteService() {
		return baseSiteService;
	}

	public void setBaseSiteService(BaseSiteService baseSiteService) {
		this.baseSiteService = baseSiteService;
	}

	public BaseStoreService getBaseStoreService() {
		return baseStoreService;
	}

	public void setBaseStoreService(BaseStoreService baseStoreService) {
		this.baseStoreService = baseStoreService;
	}
}
