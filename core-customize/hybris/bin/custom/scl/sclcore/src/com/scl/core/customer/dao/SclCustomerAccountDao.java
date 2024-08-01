package com.scl.core.customer.dao;

import de.hybris.platform.commerceservices.customer.dao.CustomerAccountDao;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.PhoneContactInfoModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.store.BaseStoreModel;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Interface for SCL Customer Related DAO
 */
public interface SclCustomerAccountDao extends CustomerAccountDao {

     /**
      * dao method to find last six month order for customer and basestore
      * @param customerModel
      * @param store
      * @param status
      * @param fromDate
      * @return
      */
     List<OrderModel> findLastSixMonthsOrdersByCustomerAndStore(final CustomerModel customerModel, final BaseStoreModel store,
                                                                     final OrderStatus[] status, final Date fromDate);

     /**
      * DAO methods to find Phone Contact Info By Number
      * @param contactNumber
      * @return
      */
     List<PhoneContactInfoModel> findContactInfoByNumber(final String contactNumber);

     SearchPageData<OrderEntryModel> findSOOrdersEntriesByCustomerAndStore(final CustomerModel user, final BaseStoreModel store, final OrderStatus[] status, final SearchPageData searchPageData);
     List<AddressModel> findSclAddressBookDeliveryEntriesForCustomer(CustomerModel customerModel,
                                                                  Collection<CountryModel> deliveryCountries);
}
