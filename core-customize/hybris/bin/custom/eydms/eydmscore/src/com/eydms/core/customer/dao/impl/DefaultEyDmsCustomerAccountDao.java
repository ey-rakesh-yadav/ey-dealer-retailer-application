package com.eydms.core.customer.dao.impl;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.customer.dao.EyDmsCustomerAccountDao;
import com.eydms.core.dao.DataConstraintDao;
import com.eydms.core.jalo.DataConstraint;
import com.eydms.core.job.AutoCancellingRequestRaisedByInfluencerJob;
import com.eydms.core.model.EyDmsCustomerModel;
import de.hybris.platform.cms2.data.PageableData;
import de.hybris.platform.commerceservices.customer.dao.impl.DefaultCustomerAccountDao;
import de.hybris.platform.commerceservices.search.flexiblesearch.data.SortQueryData;
import de.hybris.platform.core.GenericSearchConstants;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.PhoneContactInfoModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchParameter;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.store.BaseStoreModel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * class for eydms Customer Related DAO
 */
public class DefaultEyDmsCustomerAccountDao extends DefaultCustomerAccountDao implements EyDmsCustomerAccountDao {

    private static final Logger LOG = Logger.getLogger(DefaultEyDmsCustomerAccountDao.class);

    private PaginatedFlexibleSearchService paginatedFlexibleSearchService;
    private FlexibleSearchService flexibleSearchService;
    private DataConstraintDao dataConstraintDao;
    private UserService userService;

    private static final String FIND_ORDERS_BY_CUSTOMER_STORE_QUERY = "SELECT {" + OrderModel.PK + "}, {"
            + OrderModel.CREATIONTIME + "}, {" + OrderModel.CODE + "} FROM {" + OrderModel._TYPECODE + "} WHERE {" + OrderModel.USER
            + "} = ?customer AND {" + OrderModel.VERSIONID + "} IS NULL AND {" + OrderModel.STORE + "} = ?store";

    private static final String FIND_ORDERS_BY_CUSTOMER_STORE_QUERY_AND_STATUS = FIND_ORDERS_BY_CUSTOMER_STORE_QUERY + " AND {"
            + OrderModel.STATUS + "} IN (?statusList)";

    private static final String FIND_ORDERS_BY_CUSTOMER_STORE_QUERY_AND_STATUS_AND_TIME = FIND_ORDERS_BY_CUSTOMER_STORE_QUERY_AND_STATUS + " AND {"
            + OrderModel.CREATIONTIME + "} >= ?fromDate";

    private static final String FIND_PHONE_CONTACT_INFO_BY_NUMBER = "SELECT {" + PhoneContactInfoModel.PK + "} FROM {" + PhoneContactInfoModel._TYPECODE + "} WHERE {" + PhoneContactInfoModel.PHONENUMBER
            + "} = ?contactNumber";

    private static final String FILTER_ORDER_ENTRY_STATUS = " AND {" + OrderEntryModel.STATUS + "} NOT IN (?filterStatusList)";

    private static final String FIND_ORDERS_ENTRIES_BY_CUSTOMER_STORE_QUERY = "SELECT {" + OrderEntryModel.PK + "}, {"
            + OrderEntryModel.CREATIONTIME + "} FROM {" + OrderEntryModel._TYPECODE + " AS oe JOIN " + OrderModel._TYPECODE+
            " AS o ON {oe:" +OrderEntryModel.ORDER+ "}={o:"+OrderModel.PK+"}}  ";

    private static final String FIND_ORDERS_ENTRIES_BY_CUSTOMER_STORE_QUERY_AND_STATUS = FIND_ORDERS_ENTRIES_BY_CUSTOMER_STORE_QUERY + " WHERE {"
            + OrderEntryModel.STATUS + "} IN (?statusList)";

    private static final String SORT_ORDERS_ENTRIES_BY_DATE = " ORDER BY {" + OrderEntryModel.CREATIONTIME + "} DESC, {" + OrderEntryModel.PK + "}";

    private static final String FIND_ADDRESS_BOOK_DELIVERY_ENTRIES = "SELECT {address:" + AddressModel.PK + "} FROM {"
            + AddressModel._TYPECODE + " AS address LEFT JOIN " + CustomerModel._TYPECODE + " AS customer ON {address:"
            + AddressModel.OWNER + "}={customer:" + CustomerModel.PK + "}} WHERE {customer:" + CustomerModel.PK
            + "} = ?customer AND {address:" + AddressModel.SHIPPINGADDRESS + "} = ?shippingAddress AND {address:"
            + AddressModel.VISIBLEINADDRESSBOOK + "} = ?visibleInAddressBook AND {address:" + AddressModel.COUNTRY
            + "} IN (?deliveryCountries) AND {address:" + AddressModel.LASTUSEDDATE + "} >= ?date";
    /**
     * dao method to find last six month order for customer and basestore
     * @param customerModel
     * @param store
     * @param status
     * @param fromDate
     * @return
     */
    @Override
    public List<OrderModel> findLastSixMonthsOrdersByCustomerAndStore(final CustomerModel customerModel, final BaseStoreModel store,
                                                              final OrderStatus[] status, final Date fromDate){

        validateParameterNotNull(customerModel, "Customer must not be null");
        validateParameterNotNull(store, "Store must not be null");

        final Map<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put("customer", customerModel);
        queryParams.put("store", store);
        queryParams.put("fromDate", fromDate);
        queryParams.put("statusList", Arrays.asList(status));

        String query = FIND_ORDERS_BY_CUSTOMER_STORE_QUERY_AND_STATUS_AND_TIME;

        final SearchResult<OrderModel> result = getFlexibleSearchService().search(query, queryParams);
        return result.getResult();
    }

    /**
     * DAO methods to find Phone Contact Info By Number
     * @param contactNumber
     * @return
     */
    @Override
    public List<PhoneContactInfoModel> findContactInfoByNumber(final String contactNumber){

        validateParameterNotNull(contactNumber, "Contact Number must not be null");

        final Map<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put("contactNumber", contactNumber);

        String query = FIND_PHONE_CONTACT_INFO_BY_NUMBER;

        final SearchResult<PhoneContactInfoModel> result = getFlexibleSearchService().search(query, queryParams);
        return result.getResult();
    }

    public SearchPageData<OrderEntryModel> findSOOrdersEntriesByCustomerAndStore(final CustomerModel user, final BaseStoreModel store, final OrderStatus[] status, final SearchPageData searchPageData)
    {

        validateParameterNotNull(user, "Customer must not be null");
        validateParameterNotNull(store, "Store must not be null");
        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);
        final FlexibleSearchQuery query ;
        if (ArrayUtils.isNotEmpty(status)) {
            query = new FlexibleSearchQuery(FIND_ORDERS_ENTRIES_BY_CUSTOMER_STORE_QUERY_AND_STATUS);
            query.addQueryParameter("statusList", Arrays.asList(status));
        }
        else
        {
            query = new FlexibleSearchQuery(FIND_ORDERS_ENTRIES_BY_CUSTOMER_STORE_QUERY);

        }

        query.addQueryParameter("user", user);
        query.addQueryParameter("store", store);

        parameter.setFlexibleSearchQuery(query);

        return getPaginatedFlexibleSearchService().search(parameter);
    }

    @Override
    public List<AddressModel> findEyDmsAddressBookDeliveryEntriesForCustomer(final CustomerModel customerModel,
                                                                        final Collection<CountryModel> deliveryCountries)
    {
        validateParameterNotNull(customerModel, "Customer must not be null");
        final Map<String, Object> queryParams = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT {a:pk} FROM {Address as a left join Customer as c on {a:owner}={c:pk}} where {c:pk} =?customer and {a:shippingAddress} =?shippingAddress and {a:visibleInAddressBook}=?visibleInAddressBook and {a:country} in (?deliveryCountries)");
        queryParams.put("customer", customerModel);
        queryParams.put("shippingAddress", Boolean.TRUE);
        queryParams.put("visibleInAddressBook", Boolean.TRUE);
        queryParams.put("deliveryCountries", deliveryCountries);
        if(!(customerModel.getGroups().contains(getUserService().getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))))
        {
            LOG.info("inside findEyDmsAddressBookDeliveryEntriesForCustomer incase of dealer");
            LocalDate currentDate = LocalDate.now();
            Integer lastXDays = dataConstraintDao.findDaysByConstraintName("LAST_ADDRESS_USED_DATE");
            LocalDate last6MonthsDate = currentDate.minusDays(lastXDays);
            Date date = Date.from(last6MonthsDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            builder.append(" and {a:lastUsedDate} >= ?date ");
            queryParams.put("date",date);
        }
        FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(AddressModel.class));
        query.addQueryParameters(queryParams);
        final SearchResult<AddressModel> searchResult = flexibleSearchService.search(query);
        List<AddressModel> result = searchResult.getResult();
        return result!=null && !result.isEmpty() ? result : Collections.emptyList();
    }

    @Override
    public FlexibleSearchService getFlexibleSearchService() {
        return flexibleSearchService;
    }

    @Override
    public void setFlexibleSearchService(FlexibleSearchService flexibleSearchService) {
        this.flexibleSearchService = flexibleSearchService;
    }

    public PaginatedFlexibleSearchService getPaginatedFlexibleSearchService() {
        return paginatedFlexibleSearchService;
    }

    public void setPaginatedFlexibleSearchService(PaginatedFlexibleSearchService paginatedFlexibleSearchService) {
        this.paginatedFlexibleSearchService = paginatedFlexibleSearchService;
    }

    public DataConstraintDao getDataConstraintDao() {
        return dataConstraintDao;
    }

    public void setDataConstraintDao(DataConstraintDao dataConstraintDao) {
        this.dataConstraintDao = dataConstraintDao;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
