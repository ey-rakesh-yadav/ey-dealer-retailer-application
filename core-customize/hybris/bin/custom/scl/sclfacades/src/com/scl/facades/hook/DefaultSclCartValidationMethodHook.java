package com.scl.facades.hook;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import de.hybris.platform.commerceservices.order.CommerceCartModification;
import de.hybris.platform.commerceservices.service.data.CommerceCartParameter;
import de.hybris.platform.commerceservices.strategies.hooks.CartValidationHook;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.CartEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;


public class DefaultSclCartValidationMethodHook implements CartValidationHook
{
	@Autowired
	UserService userService;

	@Autowired
	ModelService modelService;
	
	@Override
	public void beforeValidateCart(final CommerceCartParameter parameter, final List<CommerceCartModification> modifications)
	{
		//not implemented
	}

	@Override
	public void afterValidateCart(final CommerceCartParameter parameter, final List<CommerceCartModification> modifications)
	{
		final CartModel cartModel = parameter.getCart();
		if(cartModel!=null && CollectionUtils.isNotEmpty(cartModel.getEntries())) {
			for(AbstractOrderEntryModel cartEntryModel: cartModel.getEntries()) {
				if(cartEntryModel.getDeliveryAddress()!=null && !userService.getCurrentUser().equals(cartEntryModel.getDeliveryAddress().getOwner()))
				{
					cartEntryModel.setDeliveryAddress(null);
					modelService.save(cartEntryModel);
				}
			}
		}
	}
}
