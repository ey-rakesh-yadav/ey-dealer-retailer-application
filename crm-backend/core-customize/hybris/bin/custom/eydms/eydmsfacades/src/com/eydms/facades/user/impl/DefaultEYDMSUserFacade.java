package com.eydms.facades.user.impl;

import com.eydms.core.customer.services.EyDmsCustomerAccountService;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.user.EYDMSUserService;
import com.eydms.facades.data.SOCockpitData;
import com.eydms.facades.user.EYDMSUserFacade;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.impl.DefaultUserFacade;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

public class DefaultEYDMSUserFacade extends DefaultUserFacade implements EYDMSUserFacade  {

    private static final Logger LOG = Logger.getLogger(DefaultEYDMSUserFacade.class);

    @Resource(name="customerAccountService")
    private EyDmsCustomerAccountService eydmsCustomerAccountService;
    
    @Resource
    EYDMSUserService eydmsUserService;

    private UserService userService;

    @Resource
    Converter<AddressModel, AddressData> addressConverter;
    
    @Override
    public void addAddress(final AddressData addressData)
    {
        validateParameterNotNullStandardMessage("addressData", addressData);

        final CustomerModel currentCustomer = getCurrentUserForCheckout();
        boolean makeThisAddressTheDefault;

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
        }

        

        // Create the new address model
        final AddressModel newAddress = getModelService().create(AddressModel.class);
        getAddressReversePopulator().populate(addressData, newAddress);

        // Store the address against the user
        newAddress.setLastUsedDate(new Date());
        getCustomerAccountService().saveAddressEntry(currentCustomer, newAddress);
        if(addressData.getRetailerAddressPk() == null && StringUtils.isNotBlank(addressData.getRetailerUid())){
            EyDmsCustomerModel retailer;
            try{
                retailer = (EyDmsCustomerModel) getUserService().getUserForUID(addressData.getRetailerUid());
            }
            catch (UnknownIdentifierException | AmbiguousIdentifierException | ClassCastException ex ){
                retailer = null;
                LOG.error("Exception occured while fetching retailer with UID: "+addressData.getRetailerUid());
                LOG.error("Exception is : "+ex);
            }
            if(null!= retailer){
                eydmsCustomerAccountService.saveAddressEntryForRetailer(retailer,newAddress,makeThisAddressTheDefault);
            }

        }

        // Update the address ID in the newly created address
        addressData.setId(newAddress.getPk().toString());

        if (makeThisAddressTheDefault)
        {
            getCustomerAccountService().setDefaultAddressEntry(currentCustomer, newAddress);
        }
    }

    private boolean checkForExistingPrimaryAddress(List<AddressModel> dealerAddress) {

        List<AddressModel> dealerPrimaryAddressList = dealerAddress.stream().filter(addr ->addr.getIsPrimaryAddress()).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(dealerPrimaryAddressList)){
            return true;
        }
        else{
            return false;
        }
    }

	@Override
	public SOCockpitData getOutstandingAmountAndBucketsForSO(String uid) {
		return eydmsUserService.getOutstandingAmountAndBucketsForSO(uid);
	}

	@Override
	public Integer getDealersCountForDSOGreaterThanThirty(String userId) {
		return eydmsUserService.getDealersCountForDSOGreaterThanThirty(userId);
	}

    @Override
    public List<AddressData> getEyDmsAddressBook()
    {
        // Get the current customer's addresses
        final CustomerModel currentUser = (CustomerModel) getUserService().getCurrentUser();
       // final Collection<AddressModel> addresses = getCustomerAccountService().getAddressBookDeliveryEntries(currentUser);
        final Collection<AddressModel> addresses = eydmsCustomerAccountService.getEyDmsAddressBookDeliveryEntries(currentUser);
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

    @Override
    public UserService getUserService() {
        return userService;
    }

    @Override
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Converter<AddressModel, AddressData> getAddressConverter() {
        return addressConverter;
    }

    @Override
    public void setAddressConverter(Converter<AddressModel, AddressData> addressConverter) {
        this.addressConverter = addressConverter;
    }
}
