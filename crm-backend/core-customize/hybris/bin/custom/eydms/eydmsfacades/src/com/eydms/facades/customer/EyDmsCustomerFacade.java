package com.eydms.facades.customer;

import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.facades.data.*;
import com.eydms.facades.prosdealer.data.DealerListData;
import com.eydms.facades.prosdealer.data.ProsDealerData;
import com.eydms.facades.prosdealer.data.ProsDealerListData;
import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface EyDmsCustomerFacade extends CustomerFacade {

    /**
     * calculates and saves customer's last six month avaerage order value
     */
    void calculateLastSixMonthsAverageOrderValue();

    ProsDealerListData getProspectiveDealersForCurrentuser();

    ProsDealerData getProsDealerDetailsByUid(final String uid);

    ProsDealerListData getProsDealerPendingForSOAssignment();

    DealerListData getDealersForCurrentUser();

    CustomerData getUserDetailsForProfilePage();

    void updateUserDetailsOnProfilePage(final CustomerData customerData) throws DuplicateUidException;

    void setNewPassword(String uid, String newPassword);

    /**
     * Create new contact number for user and send to for approval
     *
     * @param userId
     * @param newContactNumber
     * @return
     */
    CustomerData updateUsersContactNumber(final String userId, final String newContactNumber);


    String setProfilePicture(MultipartFile file);

    /**
     * Create new contact number for user and send to for approval
     *
     * @param contactNumber
     * @return
     */
    boolean isContactInfoExisting(final String contactNumber);

    List<EyDmsSiteData> getSitesTaggedtoInfluencers();
    List<EyDmsSiteData> getSitesTaggedtoInfluencers(EyDmsCustomerModel influencer);

    DealerListData getEyDmsCustomersListForFeedback();

    DealerListData getEyDmsCustomersListForSO();

    List<EyDmsTaggedPartnersData> getTaggedPartnersForSite();

    DealerListData getInfluencersListForSO();

    DealerListData getRetailersTaggedToSO();

    List<AddressData> filterAddressBookData(final List<AddressData> addressData, final String retailerUid);

    boolean addTaggedPartnersForSite(String uid) throws DuplicateUidException;

    String addRetailerdata(EYDMSRetailerData eydmsRetailerData);

    EYDMSRetailerData getRetailerData(String uid);

    String addCompanyDetails(EYDMSCompanyDetailsData eydmsRetailerData);

    EYDMSCompanyDetailsData getCompanyDetails(String uid);

    String addBusinessInformation(EYDMSBusinessInfoData businessInfoData);

    EYDMSBusinessInfoData getBusinessInfo(String uid);

    String addFinancialInformation(EYDMSFinancialInfoData infoData);

    EYDMSFinancialInfoData getFinancialInfo(String uid);
    
    CustomerStagingData getCustomerStagingData(String uid);

    CustomerDetailedData getCustomerDetailedData(String uid);
    InfluencerDetailedData getInfluencerDetailedData(String uid);

	List<AddressData> filterAddressByLpSource(List<AddressData> filteredAddressList);


    CreditBreachedData getCountOfCreditLimitBreachedUser();
    
    String addDealerData(EYDMSDealerData eydmsDealerData);
    
    Boolean sendOnboardingSmsOtp(String name, String mobileNo);
    
    Boolean sendLoginSmsOtp(String uid);

    String addSalesPromoterData(EYDMSSalesPromoterData eydmsSalesPromoterData);

    void checkMobileNumberValidation(String mobileNo);
}
