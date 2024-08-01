package com.scl.core.strategies.impl;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.enums.CheckoutPaymentType;
import de.hybris.platform.b2bacceleratorservices.strategies.impl.DefaultB2BDeliveryAddressesLookupStrategy;
import de.hybris.platform.commerceservices.strategies.DeliveryAddressesLookupStrategy;
import de.hybris.platform.commerceservices.util.ItemComparator;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import org.springframework.beans.factory.annotation.Required;

import com.scl.core.enums.OrderType;

import java.util.*;

public class DefaultSCLB2BDeliveryAddressesLookupStrategy implements DeliveryAddressesLookupStrategy {

    private DeliveryAddressesLookupStrategy fallbackDeliveryAddressesLookupStrategy;
    @Override
    public List<AddressModel> getDeliveryAddressesForOrder(final AbstractOrderModel abstractOrder, final boolean visibleAddressesOnly)
    {
    	if(OrderType.ISO.equals(abstractOrder.getOrderType())) {
    		return Collections.singletonList(abstractOrder.getDeliveryAddress());
    	}
    	else if (CheckoutPaymentType.ACCOUNT.equals(abstractOrder.getPaymentType()))
        {
            // Lookup the
                final Set<AddressModel> addresses = collectShippingAddressesForCustomer((B2BCustomerModel) abstractOrder.getUser());
                if (addresses != null && !addresses.isEmpty()) {
                    return sortAddresses(addresses);
                }
            return Collections.emptyList();
        }
        else
        {
            // Use fallback
            return getFallbackDeliveryAddressesLookupStrategy().getDeliveryAddressesForOrder(abstractOrder, visibleAddressesOnly);
        }
    }

    protected Set<AddressModel> collectShippingAddressesForCustomer(final B2BCustomerModel customerModel)
    {
        final Set<AddressModel> shippingAddresses = new HashSet<AddressModel>();
        Set<AddressModel> addresses = new HashSet<>(customerModel.getAddresses());
        for (final AddressModel address : addresses)
        {
            if (address.getShippingAddress())
            {
                shippingAddresses.add(address);
            }
        }
        return shippingAddresses;
    }
    protected List<AddressModel> sortAddresses(final Collection<AddressModel> addresses)
    {
        final ArrayList<AddressModel> result = new ArrayList<AddressModel>(addresses);
        Collections.sort(result, ItemComparator.INSTANCE);
        return result;
    }

    protected DeliveryAddressesLookupStrategy getFallbackDeliveryAddressesLookupStrategy()
    {
        return fallbackDeliveryAddressesLookupStrategy;
    }

    public void setFallbackDeliveryAddressesLookupStrategy(final DeliveryAddressesLookupStrategy fallbackDeliveryAddressesLookupStrategy)
    {
        this.fallbackDeliveryAddressesLookupStrategy = fallbackDeliveryAddressesLookupStrategy;
    }
}
