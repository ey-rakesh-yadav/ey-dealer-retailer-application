package com.eydms.core.customer.services;

import com.eydms.core.model.EyDmsCustomerModel;
import de.hybris.platform.commerceservices.customer.CustomerAccountService;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.store.BaseStoreModel;

import java.util.List;

public interface EyDmsCustomerAccountService extends CustomerAccountService {

    /**
     * updtes customer average order value for last six month
     * @param customerModel
     * @param baseStoreModel
     * @param status
     */
    void updateCustomerAverageOrderValue(final CustomerModel customerModel, final BaseStoreModel baseStoreModel, final OrderStatus[] status);


    /**
     * Update Users Contact Number
     * @param user
     * @param newContactNumber
     */
    void updateUsersContactNumber(final UserModel user , final String newContactNumber );

    /**
     * Checks if provided contact number is already existing
     * @param contactNumber
     * @return
     */
    boolean isExistingContactNumber(final String contactNumber);

    SearchPageData<OrderEntryModel> fetchSOOrdersEntriesByCustomerAndStore(final CustomerModel user, final BaseStoreModel store, final String statuses, final SearchPageData searchPageData);

    void saveAddressEntryForRetailer(final EyDmsCustomerModel retailer , final AddressModel addressModel , final boolean isDefault);

    List<AddressModel> getEyDmsAddressBookDeliveryEntries(CustomerModel customerModel);
}
