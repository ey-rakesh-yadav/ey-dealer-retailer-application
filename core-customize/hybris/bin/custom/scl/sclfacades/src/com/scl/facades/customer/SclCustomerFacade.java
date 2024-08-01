package com.scl.facades.customer;

import com.scl.core.model.SclCustomerModel;
import com.scl.facades.data.*;
import com.scl.facades.prosdealer.data.DealerListData;
import com.scl.facades.prosdealer.data.ProsDealerData;
import com.scl.facades.prosdealer.data.ProsDealerListData;
import com.scl.occ.dto.LoginOTPWsDTO;
import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface SclCustomerFacade extends CustomerFacade {

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

    List<SclSiteData> getSitesTaggedtoInfluencers();
    List<SclSiteData> getSitesTaggedtoInfluencers(SclCustomerModel influencer);

    DealerListData getSclCustomersListForFeedback();

    DealerListData getSclCustomersListForSO();

    List<SclTaggedPartnersData> getTaggedPartnersForSite();

    DealerListData getInfluencersListForSO();

    DealerListData getRetailersTaggedToSO();

    List<AddressData> filterAddressBookData(final List<AddressData> addressData, final String retailerUid);

    boolean addTaggedPartnersForSite(String uid) throws DuplicateUidException;

    String addRetailerdata(SCLRetailerData sclRetailerData);

    SCLRetailerData getRetailerData(String uid);

    String addCompanyDetails(SCLCompanyDetailsData sclRetailerData);

    SCLCompanyDetailsData getCompanyDetails(String uid);

    String addBusinessInformation(SCLBusinessInfoData businessInfoData);

    SCLBusinessInfoData getBusinessInfo(String uid);

    String addFinancialInformation(SCLFinancialInfoData infoData);

    SCLFinancialInfoData getFinancialInfo(String uid);
    
    CustomerStagingData getCustomerStagingData(String uid);

    CustomerDetailedData getCustomerDetailedData(String uid);
    InfluencerDetailedData getInfluencerDetailedData(String uid);

	List<AddressData> filterAddressByLpSource(List<AddressData> filteredAddressList);


    CreditBreachedData getCountOfCreditLimitBreachedUser();
    
    String addDealerData(SCLDealerData sclDealerData);
    
    Boolean sendOnboardingSmsOtp(String name, String mobileNo);

    LoginOTPWsDTO sendLoginSmsOtp(SclCustomerModel customerModel, String partnerFlag, String pcuid);

    String addSalesPromoterData(SCLSalesPromoterData sclSalesPromoterData);

    void checkMobileNumberValidation(String mobileNo);

    CustomerData getCustomerData(UserModel userModel) throws ConversionException;
}
