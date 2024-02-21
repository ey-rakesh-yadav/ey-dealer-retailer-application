package com.eydms.occ.controllers;

import com.eydms.core.dao.EyDmsUserDao;
import com.eydms.core.enums.OrderType;
import com.eydms.facades.cart.impl.DefaultEyDmsCartFacade;
import com.eydms.facades.data.DestinationSourceListData;
import com.eydms.occ.dto.source.DestinationSourceListWsDTO;
import com.eydms.occ.security.EyDmsSecuredAccessConstants;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.CheckoutFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.commercewebservicescommons.dto.order.CartWsDTO;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.CartAddressException;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.CartException;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdUserIdAndCartIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import org.apache.commons.lang3.exception.ExceptionUtils;
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
import org.springframework.web.bind.annotation.*;

import java.util.Date;

import javax.annotation.Resource;

@Controller
@RequestMapping(value = "/{baseSiteId}/users/{userId}/carts")
@ApiVersion("v2")
@Tag(name = "EyDms Carts")
public class EyDmsCartsController extends EyDmsBaseController {

    private static final Logger LOG = LoggerFactory.getLogger(EyDmsCartsController.class);

    @Autowired
    CartFacade cartFacade;

    @Autowired
    DefaultEyDmsCartFacade eydmsCartFacade;

    @Autowired
    UserService userService;

    @Autowired
    SessionService sessionService;

    @Resource(name = "cartDetailsValidator")
    private Validator cartDetailsValidator;
    
	@Resource(name = "checkoutFacade")
	private CheckoutFacade checkoutFacade;
	
	@Resource(name = "deliveryAddressValidator")
	private Validator deliveryAddressValidator;

	@Autowired
	EyDmsUserDao eydmsUserDao;

	@Autowired
	ModelService modelService;

    private static final String ACTING_USER_UID = "ACTING_USER_UID";

    @Secured({EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT, EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
    @RequestMapping(method = RequestMethod.POST,  value="/{cartId}/confirmOrderDetails", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @Operation(operationId = "confirmOrderDetails", summary = "Confirm Order Details", description = "Confirm Order Details and save it to the cart")
    @ApiBaseSiteIdUserIdAndCartIdParam
    public CartWsDTO confirmOrderDetails(
            @Parameter(description = "Data object that contains information necessary for confirm order details", required = true) @RequestBody final CartWsDTO cartDetails,
            @ApiFieldsParam @RequestParam(defaultValue = FULL_FIELD_SET) final String fields) throws CommerceCartModificationException {
    	try {
    	LOG.debug("confirm order details");
        validate(cartDetails, "cartDetails", cartDetailsValidator);
        setCartDeliveryAddressInternal(cartDetails);		
        CartData cartData = setCartDetailsInternal(cartDetails);
        eydmsCartFacade.setOrderRequistionOnOrder(cartDetails);
        return getDataMapper().map(cartData, CartWsDTO.class, fields);
    	}
    	catch(NullPointerException n) {
    		CartWsDTO nn = new CartWsDTO();
    		nn.setName(ExceptionUtils.getStackTrace(n));
    		return nn;
    	}
    }

	private CartData setCartDetailsInternal(CartWsDTO cartDetails) {

        sessionService.getAttribute(ACTING_USER_UID);
        UserModel currentUser = userService.getCurrentUser();

            if (eydmsCartFacade.setCartDetails(cartDetails)) {
                return cartFacade.getSessionCart();
            }
        return null; //todo
    }
    
    /*private void setSelectedDeliveryDate(CartWsDTO cartDetails) {
    	 Date date;
		try {
			date = new SimpleDateFormat("yyyy-MM-dd").parse(cartDetails.getExpectedDeliveryDate());
	    	 cartDetails.setSelectedDeliveryDate(date);
		} catch (ParseException e) {
			throw new CartException("Selected Delivery date " + cartDetails.getExpectedDeliveryDate() + "  format is not valid",
					CartAddressException.NOT_VALID, cartDetails.getExpectedDeliveryDate());
		}  
    }*/
    
	private void setCartDeliveryAddressInternal(CartWsDTO cartDetails)
	{
		if(!OrderType.ISO.getCode().equals(cartDetails.getOrderType())) {
			String addressId = null;
			if (cartDetails.getDeliveryAddress()==null)
			{
				throw new CartAddressException("Address given by id " + sanitize(addressId) + " is not valid",
						CartAddressException.NOT_VALID, addressId);
			}
			addressId = cartDetails.getDeliveryAddress().getId();
			LOG.debug("setCartDeliveryAddressInternal: {}", logParam("addressId", addressId));
			final AddressData address = new AddressData();
			address.setId(addressId);
			//address last used date change
			AddressModel addressModel = eydmsUserDao.getAddressByPk(addressId);
			addressModel.setLastUsedDate(new Date());
			modelService.save(addressModel);
			final Errors errors = new BeanPropertyBindingResult(address, "addressData");
			deliveryAddressValidator.validate(address, errors);
			if (errors.hasErrors())
			{
				throw new CartAddressException("Address given by id " + sanitize(addressId) + " is not valid",
						CartAddressException.NOT_VALID, addressId);
			}

			if (!checkoutFacade.setDeliveryAddress(address))
			{
				throw new CartAddressException(
						"Address given by id " + sanitize(addressId) + " cannot be set as delivery address in this cart",
						CartAddressException.CANNOT_SET, addressId);
			}
		}
	}
}