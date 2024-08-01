package com.scl.core.customer.services;

import com.scl.core.model.DealerModel;
import com.scl.core.model.ProspectiveDealerModel;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.model.SclUserModel;
import com.scl.occ.dto.LoginOTPWsDTO;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2b.services.B2BCustomerService;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.model.CatalogUnawareMediaModel;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.user.AddressModel;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SclCustomerService extends B2BCustomerService<B2BCustomerModel, B2BUnitModel> {

    /**
     * Get dealer from dealer code
     * @param dealerCode
     * @return
     */
    DealerModel getDealerForCode(final String dealerCode);

    List<ProspectiveDealerModel> getProspectiveDealersList(final SclUserModel sclUser);
    
    List<SclCustomerModel> getDealersList(final SclUserModel sclUser);
    
    String saveProfilePicture(MultipartFile file);

    List<SclCustomerModel> getSclCustomersListForFeedback(SclUserModel sclUser);

	List<B2BCustomerModel> getSitesTaggedtoInfluencers(SclCustomerModel sites);

	List<SclCustomerModel> getSclCustomersListForSO(SclUserModel sclUser);

	List<B2BCustomerModel> getTaggedPartnersForSite(SclCustomerModel site);

    List<SclCustomerModel> getInfluencerListForSO(SclUserModel sclUser);

    void triggerShipToPartyAddress(String addressId,SclCustomerModel dealer);

	List<B2BCustomerModel> getRetailersTaggedtoSO(SclUserModel so);

    CatalogUnawareMediaModel createMediaFromFile(final String uid, final String documentType, final MultipartFile file );

    SclCustomerModel getSclCustomerForUid(final String uid);

    SclCustomerModel getCurrentSclCustomer();

    List<SclCustomerModel>  getCustomerCards(String dealerCategory, String leadType, String onboardingStatus,
                                             BaseSiteModel site, String searchKey);
    
    AddressModel getAddressByErpId(String erpAddressId, SclCustomerModel customer);

	List<AddressData> filterAddressByLpSource(List<AddressData> filteredAddressList);
	List<SclCustomerModel> getCountOfCreditLimitBreachedUser(SclUserModel sclUser);
	
	Boolean sendOnboardingSmsOtp(String name, String mobileNo);

    LoginOTPWsDTO sendLoginSmsOtp(SclCustomerModel customerModel, String partnerCustomerFlag, String pcuid);

    void checkMobileNumberValidation(String mobileNo);
}
