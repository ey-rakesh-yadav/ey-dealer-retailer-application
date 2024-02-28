package com.eydms.occ.controllers;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.customer.services.EyDmsCustomerService;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.facades.customer.EyDmsCustomerFacade;
import com.eydms.facades.user.EYDMSUserFacade;
import de.hybris.platform.commercefacades.address.AddressVerificationFacade;
import de.hybris.platform.commercefacades.address.data.AddressVerificationResult;
import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.address.AddressVerificationDecision;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.commercewebservicescommons.dto.user.AddressListWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.user.AddressValidationWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.user.AddressWsDTO;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.RequestParameterException;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.event.EventService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.webservicescommons.cache.CacheControl;
import de.hybris.platform.webservicescommons.cache.CacheControlDirective;
import de.hybris.platform.webservicescommons.dto.error.ErrorListWsDTO;
import de.hybris.platform.webservicescommons.dto.error.ErrorWsDTO;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import de.hybris.platform.commercewebservices.core.user.data.AddressDataList;
import de.hybris.platform.commercewebservices.core.validation.data.AddressValidationData;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@RequestMapping(value = "/{baseSiteId}/users/{userId}/dealerAddresses")
@ApiVersion("v2")
@CacheControl(directive = CacheControlDirective.PRIVATE)
@Tag(name = "Dealer Address")
@PermitAll
public class DealerAddressController extends EyDmsBaseController
{
	public static final String ADDRESS_DOES_NOT_EXIST = "Address with given id: '%s' doesn't exist or belong to another user";
	private static final Logger LOG = LoggerFactory.getLogger(DealerAddressController.class);

	private static final String ADDRESS_MAPPING = "line1,line2,city,district,taluka,state,retailerAddressPk,postalCode,latitude,longitude,erpCity,country,retailerUid,retailerName,defaultAddress,isPrimaryAddress,accountName,cellphone,email";
	private static final String OBJECT_NAME_ADDRESS = "address";
	private static final String OBJECT_NAME_ADDRESS_ID = "addressId";
	private static final String OBJECT_NAME_ADDRESS_DATA = "addressData";
	
	@Resource(name = "addressValidator")
	private Validator addressValidator;
	@Resource(name = "addressDTOValidator")
	private Validator addressDTOValidator;
	@Resource(name = "userFacade")
	private UserFacade userFacade;

	@Resource
	private EyDmsCustomerFacade eydmsCustomerFacade;

	@Autowired
	private Populator<AddressData, AddressModel> addressReversePopulator;

	@Resource(name = "addressVerificationFacade")
	private AddressVerificationFacade addressVerificationFacade;
	@Resource(name = "dealerAddressDataErrorsPopulator")
	private Populator<AddressVerificationResult<AddressVerificationDecision>, Errors> dealerAddressDataErrorsPopulator;
	@Resource(name = "validationErrorConverter")
	private Converter<Object, List<ErrorWsDTO>> validationErrorConverter;

	@Autowired
	EventService eventService;

	@Autowired
	UserService userService;

	@Autowired
	EyDmsCustomerService eydmsCustomerService;

	@Autowired
	EYDMSUserFacade eydmsUserFacade;

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	@Operation(operationId = "getAddresses", summary = "Get customer's addresses", description = "Returns customer's addresses.")
	@ApiBaseSiteIdAndUserIdParam
	@ApiResponse(responseCode = "200",description = "List of customer's addresses")
	public AddressListWsDTO getAddresses(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
										@Parameter(description = "retailer uid") @RequestParam(required = false) final String retailerUid,
										@Parameter(description = "retailerAddressPk") @RequestParam(required = false) final String retailerAddressPk )
	{
		//final List<AddressData> addressList = userFacade.getAddressBook();
		final List<AddressData> addressList = eydmsUserFacade.getEyDmsAddressBook();
		List<AddressData> filteredAddressList = addressList;
		if(StringUtils.isNotBlank(retailerUid)){
			filteredAddressList = eydmsCustomerFacade.filterAddressBookData(addressList,retailerUid);
		}
		if(StringUtils.isNotBlank(retailerAddressPk)){
			filteredAddressList = filteredAddressList.stream().filter(address->address.getRetailerAddressPk()!=null && address.getRetailerAddressPk().equals(retailerAddressPk)).collect(Collectors.toList());			
		}

		EyDmsCustomerModel currentUser = (EyDmsCustomerModel) userService.getCurrentUser();

		if(!(currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))) {
			filteredAddressList = filteredAddressList.stream().filter(address->address.getErpId()!=null).collect(Collectors.toList());
		}
		filteredAddressList = eydmsCustomerFacade.filterAddressByLpSource(filteredAddressList);


		final AddressDataList addressDataList = new AddressDataList();
		addressDataList.setAddresses(filteredAddressList);
		return getDataMapper().map(addressDataList, AddressListWsDTO.class, fields);
	}

	
	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.CREATED)
	@Operation(operationId = "createAddress", summary = "Creates a new address.", description = "Creates a new address.")
	@ApiBaseSiteIdAndUserIdParam
	public AddressWsDTO createAddress(
			@Parameter(description = "Address object.", required = true) @RequestBody final AddressWsDTO address,
			@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		//validate(address, OBJECT_NAME_ADDRESS, getAddressDTOValidator());

		final AddressData addressData = getDataMapper().map(address, AddressData.class, ADDRESS_MAPPING);
		addressData.setShippingAddress(true);
		addressData.setVisibleInAddressBook(true);

		getUserFacade().addAddress(addressData);

		EyDmsCustomerModel currentUser = (EyDmsCustomerModel) userService.getCurrentUser();

		if((currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) {
			eydmsCustomerService.triggerShipToPartyAddress(addressData.getId());
		}

		return getDataMapper().map(addressData, AddressWsDTO.class, fields);
	}


	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{addressId}", method = RequestMethod.GET)
	@ResponseBody
	@Operation(operationId = "getAddress", summary = "Get info about address", description = "Returns detailed information about address with a given id.")
	@ApiBaseSiteIdAndUserIdParam
	public AddressWsDTO getAddress(@Parameter(description = "Address identifier.", required = true) @PathVariable final String addressId,
			@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		LOG.debug("getAddress: id={}", sanitize(addressId));
		final AddressData addressData = getAddressData(addressId);
		return getDataMapper().map(addressData, AddressWsDTO.class, fields);
	}

	
	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{addressId}", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@ResponseStatus(HttpStatus.OK)
	@Operation(operationId = "replaceAddress", summary = "Updates the address", description = "Updates the address. Attributes not provided in the request will be defined again (set to null or default).")
	@ApiBaseSiteIdAndUserIdParam
	public void replaceAddress(@Parameter(description = "Address identifier.", required = true) @PathVariable final String addressId,
			@Parameter(description = "Address object.", required = true) @RequestBody final AddressWsDTO address)
	{
		validate(address, OBJECT_NAME_ADDRESS, getAddressDTOValidator());
		final AddressData addressData = getAddressData(addressId);
		final boolean isAlreadyDefaultAddress = addressData.isDefaultAddress();
		addressData.setFormattedAddress(null);
		getDataMapper().map(address, addressData, ADDRESS_MAPPING, true);

		getUserFacade().editAddress(addressData);

		if (!isAlreadyDefaultAddress && addressData.isDefaultAddress())
		{
			getUserFacade().setDefaultAddress(addressData);
		}
	}

	
	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{addressId}", method = RequestMethod.PATCH, consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(operationId = "updateAddress", summary = "Updates the address", description = "Updates the address. Only attributes provided in the request body will be changed.")
	@ApiBaseSiteIdAndUserIdParam
	@ResponseStatus(HttpStatus.OK)
	public void updateAddress(@Parameter(description = "Address identifier.", required = true) @PathVariable final String addressId,
			@Parameter(description = "Address object", required = true) @RequestBody final AddressWsDTO address)
	{
		final AddressData addressData = getAddressData(addressId);
		final boolean isAlreadyDefaultAddress = addressData.isDefaultAddress();
		addressData.setFormattedAddress(null);

		getDataMapper().map(address, addressData, ADDRESS_MAPPING, false);
		validate(addressData, OBJECT_NAME_ADDRESS, getAddressValidator());

		if (addressData.getId().equals(getUserFacade().getDefaultAddress().getId()))
		{
			addressData.setDefaultAddress(true);
			addressData.setVisibleInAddressBook(true);
		}
		if (!isAlreadyDefaultAddress && addressData.isDefaultAddress())
		{
			getUserFacade().setDefaultAddress(addressData);
		}
		getUserFacade().editAddress(addressData);
	}

	
	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{addressId}", method = RequestMethod.DELETE)
	@Operation(operationId = "removeAddress", summary = "Delete customer's address.", description = "Removes customer's address.")
	@ApiBaseSiteIdAndUserIdParam
	@ResponseStatus(HttpStatus.OK)
	public void removeAddress(@Parameter(description = "Address identifier.", required = true) @PathVariable final String addressId)
	{
		LOG.debug("removeAddress: id={}", sanitize(addressId));
		final AddressData address = getAddressData(addressId);
		getUserFacade().removeAddress(address);
	}

	private AddressData getAddressData(final String addressId)
	{
		final AddressData addressData = getUserFacade().getAddressForCode(addressId);
		if (addressData == null)
		{
			throw new RequestParameterException(String.format(ADDRESS_DOES_NOT_EXIST, sanitize(addressId)),
					RequestParameterException.INVALID, OBJECT_NAME_ADDRESS_ID);
		}
		return addressData;
	}

	
	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/verification", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@Operation(operationId = "validateAddress", summary = "Verifies address.", description = "Verifies address.")
	@ApiBaseSiteIdAndUserIdParam
	@ResponseBody
	public AddressValidationWsDTO validateAddress(
			@Parameter(description = "Address object.", required = true) @RequestBody final AddressWsDTO address,
			@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		// validation is a bit different here
		final AddressData addressData = getDataMapper().map(address, AddressData.class, ADDRESS_MAPPING);
		final Errors errors = new BeanPropertyBindingResult(addressData, OBJECT_NAME_ADDRESS_DATA);
		AddressValidationData validationData = new AddressValidationData();

		if (isAddressValid(addressData, errors, validationData))
		{
			validationData = verifyAddresByService(addressData, errors, validationData);
		}
		return getDataMapper().map(validationData, AddressValidationWsDTO.class, fields);
	}

	/**
	 * Checks if address is valid by a validators
	 *
	 * @return <code>true</code> - address is valid , <code>false</code> - address is invalid
	 * @formparam addressData
	 * @formparam errors
	 * @formparam validationData
	 */
	
	
	protected boolean isAddressValid(final AddressData addressData, final Errors errors,
			final AddressValidationData validationData)
	{
		getAddressValidator().validate(addressData, errors);

		if (errors.hasErrors())
		{
			validationData.setDecision(AddressVerificationDecision.REJECT.toString());
			validationData.setErrors(createResponseErrors(errors));
			return false;
		}
		return true;
	}

	/**
	 * Verifies address by commerce service
	 *
	 * @return object with verification errors and suggested addresses list
	 * @formparam addressData
	 * @formparam errors
	 * @formparam validationData
	 */
	
	protected AddressValidationData verifyAddresByService(final AddressData addressData, final Errors errors,
			final AddressValidationData validationData)
	{
		final AddressVerificationResult<AddressVerificationDecision> verificationDecision = addressVerificationFacade
				.verifyAddressData(addressData);
		if (verificationDecision.getErrors() != null && !verificationDecision.getErrors().isEmpty())
		{
			populateErrors(errors, verificationDecision);
			validationData.setErrors(createResponseErrors(errors));
		}

		validationData.setDecision(verificationDecision.getDecision().toString());

		if (verificationDecision.getSuggestedAddresses() != null && !verificationDecision.getSuggestedAddresses().isEmpty())
		{
			final AddressDataList addressDataList = new AddressDataList();
			addressDataList.setAddresses(verificationDecision.getSuggestedAddresses());
			validationData.setSuggestedAddressesList(addressDataList);
		}

		return validationData;
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public AddressListWsDTO getAddresses1(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
										  @RequestParam(required = false) final String retailerUid,
										 @RequestParam(required = false) final String retailerAddressPk )
	{
		final List<AddressData> addressList = eydmsUserFacade.getEyDmsAddressBook();
		List<AddressData> filteredAddressList = addressList;
		if(StringUtils.isNotBlank(retailerUid)){
			filteredAddressList = eydmsCustomerFacade.filterAddressBookData(addressList,retailerUid);
		}
		if(StringUtils.isNotBlank(retailerAddressPk)){
			filteredAddressList = filteredAddressList.stream().filter(address->address.getRetailerAddressPk()!=null && address.getRetailerAddressPk().equals(retailerAddressPk)).collect(Collectors.toList());
		}

		EyDmsCustomerModel currentUser = (EyDmsCustomerModel) userService.getCurrentUser();

		if(!(currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))) {
			filteredAddressList = filteredAddressList.stream().filter(address->address.getErpId()!=null).collect(Collectors.toList());
		}
		filteredAddressList = eydmsCustomerFacade.filterAddressByLpSource(filteredAddressList);


		final AddressDataList addressDataList = new AddressDataList();
		addressDataList.setAddresses(filteredAddressList);
		return getDataMapper().map(addressDataList, AddressListWsDTO.class, fields);
	}
	
	
	protected ErrorListWsDTO createResponseErrors(final Errors errors)
	{
		final List<ErrorWsDTO> webserviceErrorDto = new ArrayList<>();
		validationErrorConverter.convert(errors, webserviceErrorDto);
		final ErrorListWsDTO webserviceErrorList = new ErrorListWsDTO();
		webserviceErrorList.setErrors(webserviceErrorDto);
		return webserviceErrorList;
	}

	/**
	 * Populates Errors object
	 *
	 * @param errors
	 * @param addressVerificationResult
	 */
	
	protected void populateErrors(final Errors errors,
			final AddressVerificationResult<AddressVerificationDecision> addressVerificationResult)
	{
		dealerAddressDataErrorsPopulator.populate(addressVerificationResult, errors);
	}
	
	public Validator getAddressValidator() {
		return addressValidator;
	}

	public void setAddressValidator(Validator addressValidator) {
		this.addressValidator = addressValidator;
	}
	
	public UserFacade getUserFacade() {
		return userFacade;
	}

	public void setUserFacade(UserFacade userFacade) {
		this.userFacade = userFacade;
	}
	
	public Validator getAddressDTOValidator() {
		return addressDTOValidator;
	}

	public void setAddressDTOValidator(Validator addressDTOValidator) {
		this.addressDTOValidator = addressDTOValidator;
	}

}
