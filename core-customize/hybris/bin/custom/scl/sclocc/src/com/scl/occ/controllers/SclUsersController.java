package com.scl.occ.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.scl.facades.data.*;
import com.scl.occ.annotation.ApiBaseSiteIdAndUserIdAndTerritoryParam;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import com.scl.facades.cart.SclCartFacade;
import com.scl.facades.customer.SclCustomerFacade;
import com.scl.facades.otp.SmsOtpFacade;
import com.scl.facades.prosdealer.data.DealerListData;
import com.scl.facades.prosdealer.data.ProsDealerListData;
import com.scl.facades.user.SCLUserFacade;
import com.scl.occ.dto.DropdownListWsDTO;
import com.scl.occ.dto.dealer.DealerListWsDTO;
import com.scl.occ.dto.prosdealer.ProsDealerListWsDTO;
import com.scl.occ.security.SclSecuredAccessConstants;

import de.hybris.platform.b2bacceleratorfacades.customer.impl.DefaultB2BCustomerFacade;
import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.commerceservices.request.mapping.annotation.RequestMappingOverride;
import de.hybris.platform.commercewebservicescommons.dto.user.UserSignUpWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.user.UserWsDTO;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.RequestParameterException;
import de.hybris.platform.webservicescommons.errors.exceptions.WebserviceValidationException;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller class for Scl users
 */
@Controller
@RequestMapping(value = "/{baseSiteId}/users")
@ApiVersion("v2")
@Tag(name = "Scl Users Management")
public class SclUsersController extends SclBaseController {

	private static final Logger LOG = LoggerFactory.getLogger(SclUsersController.class);

    @Resource(name = "passwordStrengthValidator")
    private Validator passwordStrengthValidator;

    @Resource(name = "wsCustomerFacade")
    private CustomerFacade customerFacade;
	
	@Resource
	private SclCustomerFacade sclCustomerFacade;
	@Resource
	private DefaultB2BCustomerFacade b2bCustomerFacade;
	@Resource
	public SmsOtpFacade smsOtpFacade;
	@Resource(name = "sclCartFacade") 
	private SclCartFacade sclCartFacade;
	
	@Resource
	SCLUserFacade sclUserFacade;

    //@Secured({ SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CLIENT })
    @RequestMapping(method = RequestMethod.GET,value = "/{userId}/prosdealerslist")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Operation(operationId = "getProspectiveDealersList", summary = "Returns Prospective dealers List ", description = "Returns List of prospective dealer attached with the user.")
    @ApiBaseSiteIdAndUserIdParam
    public ProsDealerListWsDTO getProspectiveDealers(){

		ProsDealerListData prosDealers = sclCustomerFacade.getProspectiveDealersForCurrentuser();
		return getDataMapper().map(prosDealers, ProsDealerListWsDTO.class, BASIC_FIELD_SET);

	}

    //TODO:: ONLY DI ROLE WOULD HAVE  ACCESS TO THIS API
    //@Secured({ SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CLIENT })
    @RequestMapping(method = RequestMethod.GET,value = "/{userId}/prosdealerspendingsoassignment")
    @ResponseBody
    @Operation(operationId = "getProspectiveDealersListPendingSOAssignment", summary = "Returns Prospective dealers List which are Pending for SO Assignment", description = "Returns List of prospective dealer associated with Current DI that are pending for SO Assignment.")
    @ApiBaseSiteIdAndUserIdParam
    public ResponseEntity<ProsDealerListWsDTO> getProspectiveDealersPendingForSOAssignment(){
		ProsDealerListData prosDealers = sclCustomerFacade.getProsDealerPendingForSOAssignment();
		ProsDealerListWsDTO prosDealersWsDTO = getDataMapper().map(prosDealers, ProsDealerListWsDTO.class,
				BASIC_FIELD_SET);
		return ResponseEntity.status(HttpStatus.OK).body(prosDealersWsDTO);

	}
    
    @RequestMapping(method = RequestMethod.GET,value = "/{userId}/dealerslist")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Operation(operationId = "getDealersList", summary = "Returns Dealers List ", description = "Returns List of dealers attached with the user.")
    @ApiBaseSiteIdAndUserIdParam
    public DealerListWsDTO getDealers(){
        DealerListData dealers = sclCustomerFacade.getDealersForCurrentUser();
        return getDataMapper().map(dealers,DealerListWsDTO.class, BASIC_FIELD_SET);
    }

    @RequestMapping(method = RequestMethod.GET,value = "/{userId}/sclCustomersListForFeedback")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public DealerListWsDTO getSclCustomersListForFeedback(){

        DealerListData dealers = sclCustomerFacade.getSclCustomersListForFeedback();
        return getDataMapper().map(dealers,DealerListWsDTO.class, BASIC_FIELD_SET);

    }
    
    @RequestMapping(method = RequestMethod.GET,value = "/{userId}/sclCustomersListForSO")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public DealerListWsDTO getSclCustomersListForSO(){

        DealerListData dealers = sclCustomerFacade.getSclCustomersListForSO();
        return getDataMapper().map(dealers,DealerListWsDTO.class, BASIC_FIELD_SET);

    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/{userId}/getCountOfCreditLimitBreachedUser", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public CreditBreachedData getCountOfCreditLimitBreachedUser()
    {
        return sclCustomerFacade.getCountOfCreditLimitBreachedUser();
    }

    @RequestMapping(method = RequestMethod.GET,value = "/{userId}/retailersTaggedToSO")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public DealerListWsDTO getRetailersTaggedToSO(){
    	DealerListData retailers = sclCustomerFacade.getRetailersTaggedToSO();
        return getDataMapper().map(retailers,DealerListWsDTO.class, BASIC_FIELD_SET);
    }
    
    @RequestMapping(method = RequestMethod.GET,value = "/{userId}/sitesTaggedtoInfluencers")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public SclSiteListData getSitesTaggedtoInfluencers(){
    	List<SclSiteData> influencers = sclCustomerFacade.getSitesTaggedtoInfluencers();
    	SclSiteListData siteList = new SclSiteListData();
    	siteList.setSites(influencers);
    	return siteList;
    }
    
    @RequestMapping(method = RequestMethod.GET,value = "/{userId}/taggedPartnersForSite")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public SclTaggedPartnersListData getTaggedPartnersForSite(){
    	List<SclTaggedPartnersData> taggedPartners = sclCustomerFacade.getTaggedPartnersForSite();
    	SclTaggedPartnersListData taggedPartnersList = new SclTaggedPartnersListData();
    	taggedPartnersList.setTaggedPartners(taggedPartners);
    	return taggedPartnersList;
    }

    
    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/{userId}/password", method = RequestMethod.PUT)
    @RequestMappingOverride
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    @Operation(operationId  = "replaceUserPassword", summary = "Changes customer's password", description = "Changes customer's password.")
    @ApiBaseSiteIdAndUserIdParam
    public void replaceUserPassword(@Parameter(description = "User identifier.", required = true) @PathVariable final String userId,
    		@Parameter(description="Old password") @RequestParam(required = false) final String old,
    		@Parameter(description = "New password.", required = true) @RequestParam(value = "new") final String newPassword)
    {

        final UserSignUpWsDTO customer = new UserSignUpWsDTO();
        customer.setPassword(newPassword);
        validate(customer, "password", passwordStrengthValidator);
        if (StringUtils.isEmpty(old))
        {
            throw new RequestParameterException("Request parameter 'old' is missing.", RequestParameterException.MISSING, "old");
        }
        customerFacade.changePassword(old, newPassword);
    }
    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/{userId}/contactnumber", method = RequestMethod.PUT)
    @Operation(operationId = "updateUserContactNumber", summary = "Changes customer's Contact Number", description = "Changes customer's contact number.")
    @ApiBaseSiteIdAndUserIdParam
    public ResponseEntity<Object> updateUserContactNumber(@Parameter(description = "User identifier.", required = true) @PathVariable final String userId,
    		@Parameter(description = "new contact number" , required = true) @RequestParam(required = true) final String newContactNumber,
                                                           @Parameter(description = "OTP" , required = true) @RequestParam(required = true) final String otp,@Parameter(description = "partnerId") @RequestParam(required = false) final String partnerId)
    {


        final boolean isvalid = smsOtpFacade.validateOTP(otp,newContactNumber,partnerId);
        if(!isvalid){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("OTP Invalid");
        }
        final CustomerData approver = sclCustomerFacade.updateUsersContactNumber(userId,newContactNumber);
        if(null == approver){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Supervisor not found!");
        }
        final UserWsDTO approverDTO = getDataMapper().map(approver,UserWsDTO.class, BASIC_FIELD_SET);
        getDataMapper().map(approver,UserWsDTO.class);
        return ResponseEntity.status(HttpStatus.OK).body(approverDTO);
    }
    
    
	@RequestMapping(value = "/{userId}/sendSmsOtp", method = RequestMethod.PUT)
	@ResponseStatus(value = HttpStatus.ACCEPTED)
	@Operation(operationId = "sendSmsOtp", summary = "Send SMS OTP", description = "Sends SMS OTP for verification")
	@ApiBaseSiteIdAndUserIdParam
	public ResponseEntity<Boolean> sendSmsOtp(@RequestParam String mobileNumber) {
        //sclCustomerFacade.checkMobileNumberValidation(mobileNumber);
        if(smsOtpFacade.sendChangePwdSmsOtp(mobileNumber))
        {
            return ResponseEntity.status(HttpStatus.OK).body(Boolean.TRUE);
        }
				//returns false if mobile number not found
			return ResponseEntity.status(HttpStatus.OK).body(Boolean.FALSE);
	
	}
	
	@RequestMapping(value = "/{userId}/forgotPassword", method = RequestMethod.PUT)
	@ResponseStatus(value = HttpStatus.ACCEPTED)
	@Operation(operationId = "forgotPassword", summary = "Forgot Password", description = "Sets new password")
	@ApiBaseSiteIdAndUserIdParam
	@ResponseBody
	public void forgotPassword(@RequestParam String mobileNumber, @RequestParam String newPassword) {
		
			sclCustomerFacade.setNewPassword(mobileNumber, newPassword);
	}
	
	@RequestMapping(value = "/{userId}/verifyOtp", method = RequestMethod.PUT)
	@ResponseStatus(value = HttpStatus.ACCEPTED)
	@Operation(operationId = "verifyOtp", summary = "Verify Otp", description = "Verify the SMS OTP")
	@ApiBaseSiteIdAndUserIdParam
	@ResponseBody
	public ResponseEntity<Boolean> verifyOtp(@RequestParam String mobileNumber, @RequestParam String otp,@RequestParam(required = false) String partnerId) {
        //sclCustomerFacade.checkMobileNumberValidation(mobileNumber);
		if(smsOtpFacade.validateOTP(otp, mobileNumber,partnerId))
		{
			return ResponseEntity.status(HttpStatus.OK).body(Boolean.TRUE);
		}
		
			//returns false if otp is invalid
		return ResponseEntity.status(HttpStatus.OK).body(Boolean.FALSE);
		
	}

    @RequestMapping(value = "/{userId}/updatecontactotpsms", method = RequestMethod.PUT)
    @Operation(operationId = "sendUpdateContactSmsOtp", summary = "Send Update Contact SMS OTP", description = "Sends SMS OTP for verification For Contact update")
    @ApiBaseSiteIdAndUserIdParam
    public ResponseEntity<String> sendUpdateContactOTPSMS(
            @Parameter(description = "Contact Number" ,required = true) @RequestParam(required = true) final String contactNumber,
            @Parameter(description = "OTP Type") @RequestParam(required = false) final String otpType) {

        if(sclCustomerFacade.isContactInfoExisting(contactNumber)){
            if(LOG.isDebugEnabled()){
                LOG.debug("Provided Contact Number already existing");
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Contact Number already existing");
        }

        //sclCustomerFacade.checkMobileNumberValidation(contactNumber);
        if((StringUtils.isNotBlank(smsOtpFacade.sendSmsOtp(contactNumber,otpType))))
        {
            return ResponseEntity.status(HttpStatus.OK).body("OTP Sent Successfully");
        }

        //returns Internal Server error if OTP not sent
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could Not Send OTP");

    }
	
	@RequestMapping(value = "/{userId}/setProfilePicture", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.ACCEPTED)
	@Operation(operationId = "setProfilePicture", summary = "Set Profile Picture", description = "Set Profile Picture")
	@ApiBaseSiteIdAndUserIdParam
	@ResponseBody
	public ResponseEntity<String> setProfilePicture(@RequestParam("file") MultipartFile file ) {
		validateDocument(file);
		String url = sclCustomerFacade.setProfilePicture(file);
		return ResponseEntity.status(HttpStatus.CREATED).body(url);
	}
	
	@Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(method = RequestMethod.GET, value = "/{userId}/getListOfERPCity")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@Operation(operationId = "getListOfERPCity", summary = "Returns List of ERPCity", description = "Returns List of ERP City models")
	@ApiBaseSiteIdAndUserIdParam
	public DropdownListWsDTO getListOfERPCity(
			@Parameter(description = "ISO code of district.", required = true) @RequestParam final String districtIsoCode) {
		return getDataMapper().map(sclCartFacade.getListOfERPCityByDistrictCode(districtIsoCode), DropdownListWsDTO.class, BASIC_FIELD_SET);
	}
	
	@Override
	protected void validateDocument(final MultipartFile file)
	{
		final Map<String, String> params = new HashMap<>();
        params.put("FILE_EMPTY_ERROR",FILE_EMPTY_ERROR);
        params.put("INVALID_FILE_TYPE_ERROR",INVALID_FILE_TYPE_ERROR);
        params.put("DOC_SIZE_MAX_UPLOAD_SIZE_ERROR",DOC_SIZE_MAX_UPLOAD_SIZE_ERROR);

        final Errors errors = new MapBindingResult(params, "params");

        if(file.isEmpty()){
            errors.rejectValue(params.get("FILE_EMPTY_ERROR"),FILE_EMPTY_ERROR);
        }
        else if(!(PNG_MIME_TYPE.equalsIgnoreCase(file.getContentType())
                ||  JPEG_MIME_TYPE.equalsIgnoreCase(file.getContentType())
                || JPG_MIME_TYPE.equalsIgnoreCase(file.getContentType())
                )){

            errors.rejectValue(params.get("INVALID_FILE_TYPE_ERROR"),INVALID_FILE_TYPE_ERROR);

        }
        else if(file.getSize() > FIVE_MB_IN_BYTES){
            errors.rejectValue(params.get("DOC_SIZE_MAX_UPLOAD_SIZE_ERROR"),DOC_SIZE_MAX_UPLOAD_SIZE_ERROR);
            }
        if(errors.hasErrors()){
            throw new WebserviceValidationException(errors);
        }
	}

    @RequestMapping(method = RequestMethod.GET,value = "/{userId}/influencerListForSO")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public DealerListWsDTO getInfluencersListForSO(){

        DealerListData dealers = sclCustomerFacade.getInfluencersListForSO();
        return getDataMapper().map(dealers,DealerListWsDTO.class, BASIC_FIELD_SET);

    }


    
	@RequestMapping(value = "/{userId}/addTaggedPartnersForSite", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public boolean addTaggedPartnersForSite(@Parameter(description = "code") @RequestParam(required = true) String uid) throws DuplicateUidException {
		return sclCustomerFacade.addTaggedPartnersForSite(uid);
	}


	
	@RequestMapping(value = "/{userId}/outstanding", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public SOCockpitData getOutstandingAmountAndBucketsForSO(@Parameter(description = "userId") @PathVariable(required = true) String userId) {
		return sclUserFacade.getOutstandingAmountAndBucketsForSO(userId);
	}
	
	@RequestMapping(value = "/{userId}/dsoHighPriority", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public ResponseEntity<Integer> getDealersCountForDSOGreaterThanThirty(@Parameter(description = "userId") @PathVariable(required = true) String userId) {
		return ResponseEntity.status(HttpStatus.CREATED).body(sclUserFacade.getDealersCountForDSOGreaterThanThirty(userId));
	}
}
