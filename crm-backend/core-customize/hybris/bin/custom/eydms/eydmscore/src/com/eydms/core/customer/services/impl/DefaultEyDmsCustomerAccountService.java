package com.eydms.core.customer.services.impl;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.customer.dao.EyDmsCustomerAccountDao;
import com.eydms.core.customer.services.EyDmsCustomerAccountService;
import com.eydms.core.event.UpdateContactNumberEvent;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.services.SmsOtpService;
import de.hybris.platform.commerceservices.customer.impl.DefaultCustomerAccountService;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.enums.PhoneContactInfoType;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.PhoneContactInfoModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.time.TimeService;
import de.hybris.platform.store.BaseStoreModel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;


public class DefaultEyDmsCustomerAccountService extends DefaultCustomerAccountService implements EyDmsCustomerAccountService {


    private EyDmsCustomerAccountDao eydmsCustomerAccountDao;
    private TimeService timeService;
    private ModelService modelService;
    private SmsOtpService smsOtpService;

    private static final Logger LOGGER = Logger.getLogger(DefaultEyDmsCustomerAccountService.class);

    /**
     * updtes customer average order value for last six month
     * @param customerModel
     * @param baseStoreModel
     * @param status
     */
    @Override
    public void updateCustomerAverageOrderValue(final CustomerModel customerModel, final BaseStoreModel baseStoreModel,final OrderStatus[] status){
        //Add Condition for calculating by taking in account all orders

        if(customerModel.getName().equalsIgnoreCase("ming")){
            final Date fromDate = getSixMonthsBeforeDate();
            final List<OrderModel> orderList = getEyDmsCustomerAccountDao().findLastSixMonthsOrdersByCustomerAndStore(customerModel,baseStoreModel,status,fromDate);
            BigDecimal totalOrderPrice = orderList.stream().map(order -> BigDecimal.valueOf(order.getTotalPrice()))
                              .reduce(BigDecimal.ZERO,BigDecimal::add);
            BigDecimal averageOrderPrice = totalOrderPrice.divide(BigDecimal.valueOf(orderList.size()));

            customerModel.setAverageOrderValue(averageOrderPrice.doubleValue());

            getModelService().save(customerModel);
            getModelService().refresh(customerModel);
            //BigDecimal currentOrderPrice = BigDecimal.valueOf(7879);
            //BigDecimal erronousThreshold = currentOrderPrice.multiply(BigDecimal.valueOf(1.5));
            //int comparisonValue = currentOrderPrice.compareTo(erronousThreshold);
            //if(comparisonValue == 1){
                //Order is erronous
            //}
        }
        //Logic for calculating average with the help of last calculated average and current order
        else{
            //(currentOrderValue.add(lastAverageValue)).divide(BigDecimal.valueOf(2));
        }


    }

    /**
     * gets six months before date
     * @return
     */
    private Date getSixMonthsBeforeDate() {
        Date currentDate = getTimeService().getCurrentTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.MONTH,-6);
        return calendar.getTime();
    }


    /**
     * Update Users Contact Number
     * @param user
     * @param newContactNumber
     */
    @Override
    public void updateUsersContactNumber(final UserModel user , final String newContactNumber){
            PhoneContactInfoModel phoneContactInfoModel = getModelService().create(PhoneContactInfoModel.class);
            phoneContactInfoModel.setCode(newContactNumber);
            phoneContactInfoModel.setPhoneNumber(newContactNumber);
            phoneContactInfoModel.setUser(user);
            phoneContactInfoModel.setApproved(Boolean.FALSE);
            phoneContactInfoModel.setType(PhoneContactInfoType.WORK);

            getModelService().save(phoneContactInfoModel);

        final UpdateContactNumberEvent event = new UpdateContactNumberEvent(phoneContactInfoModel);
        getEventService().publishEvent(event);

        if(LOGGER.isDebugEnabled()){
            if(null != event.getPhoneContactInfoModel()){
                LOGGER.debug("Published UpdateContactNumberEvent for Contact number : "+ event.getPhoneContactInfoModel().getPhoneNumber());
            }

        }

    }

    /**
     * Checks if provided contact number is already existing
     * @param contactNumber
     * @return
     */
    @Override
    public boolean isExistingContactNumber(final String contactNumber){

        final List<PhoneContactInfoModel> phoneContactInfos = getEyDmsCustomerAccountDao().findContactInfoByNumber(contactNumber);
        if(CollectionUtils.isEmpty(phoneContactInfos)){
            return Boolean.FALSE;
        }
        else if(CollectionUtils.isNotEmpty(phoneContactInfos) && phoneContactInfos.size()>0){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public SearchPageData<OrderEntryModel> fetchSOOrdersEntriesByCustomerAndStore(final CustomerModel user, final BaseStoreModel store, final String statuses, final SearchPageData searchPageData){
        final Set<OrderStatus> statusSet = extractOrderStatuses(statuses);

        return getEyDmsCustomerAccountDao().findSOOrdersEntriesByCustomerAndStore(user,store,statusSet.toArray(new OrderStatus[statusSet.size()]),searchPageData);
    }
    protected Set<OrderStatus> extractOrderStatuses(final String statuses)
    {
        final String[] statusesStrings = statuses.split(EyDmsCoreConstants.ORDER.ENUM_VALUES_SEPARATOR);

        final Set<OrderStatus> statusesEnum = new HashSet<>();
        for (final String status : statusesStrings)
        {
            statusesEnum.add(OrderStatus.valueOf(status));
        }
        return statusesEnum;
    }

    @Override
    public void setDefaultAddressEntry(final CustomerModel customerModel, final AddressModel addressModel){
        validateParameterNotNull(customerModel, "Customer model cannot be null");
        validateParameterNotNull(addressModel, "Address model cannot be null");
        List<AddressModel> currentCustomerAddresses = new ArrayList<>(customerModel.getAddresses());

        if(StringUtils.isBlank(addressModel.getRetailerUid())){
            List<AddressModel> primaryAddressesFordealer = currentCustomerAddresses.stream().filter(addr -> addr.getIsPrimaryAddress() && StringUtils.isBlank(addr.getRetailerUid())).collect(Collectors.toList());
            primaryAddressesFordealer.forEach( addr -> addr.setIsPrimaryAddress(Boolean.FALSE));
            getModelService().saveAll(primaryAddressesFordealer);
        }
        else{
            List<AddressModel> primaryAddressesForRetailer = currentCustomerAddresses.stream()
                    .filter(addr -> addr.getIsPrimaryAddress() && StringUtils.isNotBlank(addr.getRetailerUid()) && addressModel.getRetailerUid().equals(addr.getRetailerUid()))
                    .collect(Collectors.toList());
            primaryAddressesForRetailer.forEach( addr -> addr.setIsPrimaryAddress(Boolean.FALSE));
            getModelService().saveAll(primaryAddressesForRetailer);
        }

        if (customerModel.getAddresses().contains(addressModel))
        {
            addressModel.setIsPrimaryAddress(Boolean.TRUE);
            getModelService().save(addressModel);
        }
        else
        {
            final AddressModel clone = getModelService().clone(addressModel);
            clone.setOwner(customerModel);
            clone.setIsPrimaryAddress(Boolean.TRUE);
            getModelService().save(clone);
            final List<AddressModel> customerAddresses = new ArrayList<AddressModel>();
            customerAddresses.addAll(customerModel.getAddresses());
            customerAddresses.add(clone);
            customerModel.setAddresses(customerAddresses);
        }
        getModelService().save(customerModel);
        getModelService().refresh(customerModel);
    }

    @Override
    public void saveAddressEntryForRetailer(final EyDmsCustomerModel retailer , final AddressModel addressModel , final boolean makeThisAddressDefault){

            final AddressModel clone = getModelService().clone(addressModel);
            clone.setOwner(retailer);
            clone.setErpAddressId(StringUtils.EMPTY);
            clone.setErpAddressStatus(StringUtils.EMPTY);
            clone.setErpAddressStatusDesc(StringUtils.EMPTY);
            clone.setRetailerUid(StringUtils.EMPTY);
            clone.setRetailerName(StringUtils.EMPTY);
            clone.setDuplicate(Boolean.FALSE);
            clone.setOriginal(null);
            getModelService().save(clone);
            final List<AddressModel> customerAddresses = new ArrayList<AddressModel>();
            customerAddresses.addAll(retailer.getAddresses());
            customerAddresses.add(clone);
            retailer.setAddresses(customerAddresses);
            getModelService().save(retailer);
            getModelService().refresh(retailer);

            if(makeThisAddressDefault){
                setDefaultAddressEntry(retailer,clone);
            }
            if(clone.getPk()!=null) {
            	addressModel.setRetailerAddressPk(clone.getPk().toString());
            	 getModelService().save(addressModel);
                 getModelService().refresh(addressModel);
            }
    }

    @Override
    public List<AddressModel> getEyDmsAddressBookDeliveryEntries(final CustomerModel customerModel)
    {
        validateParameterNotNull(customerModel, "Customer model cannot be null");
        return eydmsCustomerAccountDao.findEyDmsAddressBookDeliveryEntriesForCustomer(customerModel,
                getCommerceCommonI18NService().getAllCountries());
    }

    public EyDmsCustomerAccountDao getEyDmsCustomerAccountDao() {
        return eydmsCustomerAccountDao;
    }

    public void setEyDmsCustomerAccountDao(EyDmsCustomerAccountDao eydmsCustomerAccountDao) {
        this.eydmsCustomerAccountDao = eydmsCustomerAccountDao;
    }
    @Override
    public TimeService getTimeService() {
        return timeService;
    }

    @Override
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    @Override
    public ModelService getModelService() {
        return modelService;
    }

    @Override
    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

    public SmsOtpService getSmsOtpService() {
        return smsOtpService;
    }

    public void setSmsOtpService(SmsOtpService smsOtpService) {
        this.smsOtpService = smsOtpService;
    }

}
