package com.scl.occ.controllers;

import de.hybris.platform.commercefacades.order.SaveCartFacade;
import de.hybris.platform.commercefacades.order.data.CommerceSaveCartParameterData;
import de.hybris.platform.commercefacades.order.data.CommerceSaveCartResultData;
import de.hybris.platform.commerceservices.order.CommerceSaveCartException;
import de.hybris.platform.commercewebservicescommons.dto.order.SaveCartResultWsDTO;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.scl.facades.cart.SclB2BCartFacade;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;


/**
 * Controller for saved cart related requests such as saving a cart or retrieving/restoring/... a saved cart
 */
@Controller
@RequestMapping(value = "/{baseSiteId}/users/{userId}/carts")
@Tag(name = "Scl Save Cart")
public class SclSaveCartController extends SclBaseController
{
	@Resource(name = "saveCartFacade")
	private SaveCartFacade saveCartFacade;
	
	@Resource
	private SclB2BCartFacade sclB2BCartFacade;


	@RequestMapping(value = "/{cartId}/save/{employeeCode}", method = RequestMethod.PATCH)
	@ResponseBody
	@Operation(operationId = "doSaveCart", summary = "Explicitly saves a cart.", description = "Explicitly saves a cart.")
	@ApiBaseSiteIdAndUserIdParam
	public boolean doSaveCart(
			@Parameter(description = "Cart identifier: cart code for logged in user, cart guid for anonymous user, 'current' for the last modified cart", required = true) @PathVariable final String cartId,
			@Parameter(description = "The name that should be applied to the saved cart.") @RequestParam(value = "saveCartName", required = false) final String saveCartName,
			@Parameter(description = "The description that should be applied to the saved cart.") @RequestParam(value = "saveCartDescription", required = false) final String saveCartDescription,
			@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields, @PathVariable String employeeCode) throws CommerceSaveCartException
	{

		final CommerceSaveCartParameterData parameters = new CommerceSaveCartParameterData();
		parameters.setCartId(cartId);
		parameters.setName(saveCartName);
		parameters.setDescription(saveCartDescription);

		return sclB2BCartFacade.saveCart(parameters, employeeCode);
	}

}
