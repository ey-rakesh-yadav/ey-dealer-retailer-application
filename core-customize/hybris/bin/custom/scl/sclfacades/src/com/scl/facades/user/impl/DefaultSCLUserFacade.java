package com.scl.facades.user.impl;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.customer.services.SclCustomerAccountService;
import com.scl.core.dao.SlctCrmIntegrationDao;
import com.scl.core.model.DealerRetailerMappingModel;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.services.SclDealerRetailerService;
import com.scl.core.user.SCLUserService;
import com.scl.facades.data.SOCockpitData;
import com.scl.facades.user.SCLUserFacade;
import com.scl.core.enums.CustomerGrouping;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.impl.DefaultUserFacade;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

/**
 * The type Default scl user facade.
 */
public class DefaultSCLUserFacade extends DefaultUserFacade implements SCLUserFacade  {

    private static final Logger LOG = Logger.getLogger(DefaultSCLUserFacade.class);

    @Resource(name="customerAccountService")
    private SclCustomerAccountService sclCustomerAccountService;

    @Resource
    private SlctCrmIntegrationDao slctCrmIntegrationDao;

    @Resource
    SCLUserService sclUserService;

    private UserService userService;

    @Resource
    Converter<AddressModel, AddressData> addressConverter;

    private SclDealerRetailerService sclDealerRetailerService;


    /**
     * Add address.
     *
     * @param addressData the address data
     */
    @Override
    public void addAddress(final AddressData addressData)
    {
        validateParameterNotNullStandardMessage("addressData", addressData);

        CustomerModel currentCustomer = getCurrentUserForCheckout();

        if(Objects.nonNull(currentCustomer) && ((SclCustomerModel)currentCustomer).getCustomerGrouping().equals(CustomerGrouping.ZRET)){
            currentCustomer= (CustomerModel)userService.getUserForUID(addressData.getDealerUid());
        }

/*     boolean makeThisAddressTheDefault;

     if(CollectionUtils.isEmpty(currentCustomer.getAddresses())){
         makeThisAddressTheDefault = true;
     }

     else{
         if(StringUtils.isBlank(addressData.getRetailerUid())){

             List<AddressModel> dealerAddress = currentCustomer.getAddresses().stream().filter(addr -> StringUtils.isBlank(addr.getRetailerUid()) && addr.getIsPrimaryAddress() ).collect(Collectors.toList());


             makeThisAddressTheDefault  =  null!= addressData.getIsPrimaryAddress() ?
                     (addressData.getIsPrimaryAddress() || CollectionUtils.isEmpty(dealerAddress) || !checkForExistingPrimaryAddress(dealerAddress))
                     : (CollectionUtils.isEmpty(dealerAddress) || !checkForExistingPrimaryAddress(dealerAddress));

         }
         else{
             List<AddressModel> retailerAddress = currentCustomer.getAddresses().stream().filter(addr -> StringUtils.isNotBlank(addr.getRetailerUid()) && addressData.getRetailerUid().equals(addr.getRetailerUid())).collect(Collectors.toList());


             makeThisAddressTheDefault  =  null!= addressData.getIsPrimaryAddress()?
                     (addressData.getIsPrimaryAddress() || CollectionUtils.isEmpty(retailerAddress) || !checkForExistingPrimaryAddress(retailerAddress))
                     :(CollectionUtils.isEmpty(retailerAddress) || !checkForExistingPrimaryAddress(retailerAddress));

         }
     }*/



        // Create the new address model
        final AddressModel newAddress = getModelService().create(AddressModel.class);
        getAddressReversePopulator().populate(addressData, newAddress);

        // Store the address against the user
        newAddress.setLastUsedDate(new Date());
        getCustomerAccountService().saveAddressEntry(currentCustomer, newAddress);
        //Store Dealer Retailer Mapping
        //setDealerRetailerMapping(currentCustomer,newAddress);

     /*if(addressData.getRetailerAddressPk() == null && StringUtils.isNotBlank(addressData.getRetailerUid())){
         SclCustomerModel retailer;
         try{
             retailer = (SclCustomerModel) getUserService().getUserForUID(addressData.getRetailerUid());
         }
         catch (UnknownIdentifierExcep'tion | AmbiguousIdentifierException | ClassCastException ex ){
             retailer = null;
             LOG.error("Exception occured while fetching retailer with UID: "+addressData.getRetailerUid());
             LOG.error("Exception is : "+ex);
         }
         if(null!= retailer){
             sclCustomerAccountService.saveAddressEntryForRetailer(retailer,newAddress,makeThisAddressTheDefault);
         }

     }*/


        // Update the address ID in the newly created address
        addressData.setId(newAddress.getPk().toString());

        //no need to set isPrimary
    /* if (makeThisAddressTheDefault)
     {
         getCustomerAccountService().setDefaultAddressEntry(currentCustomer, newAddress);
     }*/
    }


    /**
     * Sets dealer retailer mapping.
     *
     * @param currentCustomer the current customer
     * @param newAddress      the new address
     */
    private void setDealerRetailerMapping(CustomerModel currentCustomer, AddressModel newAddress) {
        try {
            DealerRetailerMappingModel dealerRetailerMapping = getModelService().create(DealerRetailerMappingModel.class);
            SclCustomerModel currentUser = (SclCustomerModel) currentCustomer;
            if ((currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) {
                dealerRetailerMapping.setDealer(currentUser);
                if(StringUtils.isNotBlank(newAddress.getPartnerFunctionId())){
                    SclCustomerModel retailerCustomer =slctCrmIntegrationDao.getCustomerByCustNo(newAddress.getPartnerFunctionId());
                    dealerRetailerMapping.setRetailer(retailerCustomer);
                }
                dealerRetailerMapping.setShipTo(newAddress);
                dealerRetailerMapping.setState(newAddress.getState());
                dealerRetailerMapping.setDistrict(newAddress.getDistrict());
                dealerRetailerMapping.setLastUsed(new Date());
                getModelService().save(dealerRetailerMapping);
                getModelService().refresh(dealerRetailerMapping);
            }
        } catch (ModelSavingException ex) {
            LOG.error(String.format("Model saving exception for dealerRetailerMapping::%s", ex.getMessage()));
        }
    }

    /**
     * Check for existing primary address boolean.
     *
     * @param dealerAddress the dealer address
     * @return the boolean
     */
    private boolean checkForExistingPrimaryAddress(List<AddressModel> dealerAddress) {

        List<AddressModel> dealerPrimaryAddressList = dealerAddress.stream().filter(addr ->addr.getIsPrimaryAddress()).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(dealerPrimaryAddressList)){
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * Gets outstanding amount and buckets for so.
     *
     * @param uid the uid
     * @return the outstanding amount and buckets for so
     */
    @Override
	public SOCockpitData getOutstandingAmountAndBucketsForSO(String uid) {
		return sclUserService.getOutstandingAmountAndBucketsForSO(uid);
	}

    /**
     * Gets dealers count for dso greater than thirty.
     *
     * @param userId the user id
     * @return the dealers count for dso greater than thirty
     */
    @Override
	public Integer getDealersCountForDSOGreaterThanThirty(String userId) {
		return sclUserService.getDealersCountForDSOGreaterThanThirty(userId);
	}

    /**
     * Gets scl address book.
     *
     * @return the scl address book
     */
    @Override
    public List<AddressData> getSclAddressBook()
    {
        // Get the current customer's addresses
        final CustomerModel currentUser = (CustomerModel) getUserService().getCurrentUser();
       // final Collection<AddressModel> addresses = getCustomerAccountService().getAddressBookDeliveryEntries(currentUser);
        final Collection<AddressModel> addresses = sclCustomerAccountService.getSclAddressBookDeliveryEntries(currentUser);
        if (CollectionUtils.isNotEmpty(addresses))
        {
            final List<AddressData> result = new ArrayList<AddressData>();
           // final AddressData defaultAddress = getDefaultAddress();

            for (final AddressModel address : addresses)
            {
                final AddressData addressData = getAddressConverter().convert(address);

                /*if (defaultAddress != null && defaultAddress.getId() != null && defaultAddress.getId().equals(addressData.getId()))
                {
                    addressData.setDefaultAddress(true);
                    result.add(0, addressData);
                }*/
                /*else
                {
                    result.add(addressData);
                }*/
                result.add(addressData);
            }
            return result;
        }
        return Collections.emptyList();
    }

    /**
     * Gets scl address for user.
     *
     * @param searchPageData     the search page data
     * @param retailerUid        the retailer uid
     * @param filter             the filter
     * @param transportationZone the transportation zone
     * @return the scl address for user
     */
    @Override
    public List<AddressData> getSclAddressForUser(SearchPageData searchPageData, String retailerUid, String filter, String transportationZone)
    {

        final CustomerModel currentUser = (CustomerModel) getUserService().getCurrentUser();
        final Collection<AddressModel> addresses;
        addresses=getSclDealerRetailerService().getAddressListForDealerAndRetailer(searchPageData,currentUser,retailerUid,transportationZone,filter);

         if (CollectionUtils.isNotEmpty(addresses))
        {
            final List<AddressData> result = new ArrayList<AddressData>();
            
            for (final AddressModel address : addresses)
            {
                final AddressData addressData = getAddressConverter().convert(address);
                result.add(addressData);
            }
            return result;
        }
        return Collections.emptyList();
    }

    /**
     * Gets user service.
     *
     * @return the user service
     */
    @Override
    public UserService getUserService() {
        return userService;
    }

    /**
     * Sets user service.
     *
     * @param userService the user service
     */
    @Override
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Gets address converter.
     *
     * @return the address converter
     */
    @Override
    public Converter<AddressModel, AddressData> getAddressConverter() {
        return addressConverter;
    }

    /**
     * Sets address converter.
     *
     * @param addressConverter the address converter
     */
    @Override
    public void setAddressConverter(Converter<AddressModel, AddressData> addressConverter) {
        this.addressConverter = addressConverter;
    }

    /**
     * Gets scl dealer retailer service.
     *
     * @return the scl dealer retailer service
     */
    public SclDealerRetailerService getSclDealerRetailerService() {
        return sclDealerRetailerService;
    }

    /**
     * Sets scl dealer retailer service.
     *
     * @param sclDealerRetailerService the scl dealer retailer service
     */
    public void setSclDealerRetailerService(SclDealerRetailerService sclDealerRetailerService) {
        this.sclDealerRetailerService = sclDealerRetailerService;
    }
}
