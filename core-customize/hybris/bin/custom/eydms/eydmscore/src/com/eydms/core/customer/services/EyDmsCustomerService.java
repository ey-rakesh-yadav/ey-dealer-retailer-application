package com.eydms.core.customer.services;

import com.eydms.core.model.DealerModel;
import com.eydms.core.model.ProspectiveDealerModel;
import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.model.EyDmsUserModel;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2b.services.B2BCustomerService;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.model.CatalogUnawareMediaModel;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.user.AddressModel;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EyDmsCustomerService extends B2BCustomerService<B2BCustomerModel, B2BUnitModel> {

    /**
     * Get dealer from dealer code
     * @param dealerCode
     * @return
     */
    DealerModel getDealerForCode(final String dealerCode);

    List<ProspectiveDealerModel> getProspectiveDealersList(final EyDmsUserModel eydmsUser);
    
    List<EyDmsCustomerModel> getDealersList(final EyDmsUserModel eydmsUser);
    
    String saveProfilePicture(MultipartFile file);

    List<EyDmsCustomerModel> getEyDmsCustomersListForFeedback(EyDmsUserModel eydmsUser);

	List<B2BCustomerModel> getSitesTaggedtoInfluencers(EyDmsCustomerModel sites);

	List<EyDmsCustomerModel> getEyDmsCustomersListForSO(EyDmsUserModel eydmsUser);

	List<B2BCustomerModel> getTaggedPartnersForSite(EyDmsCustomerModel site);

    List<EyDmsCustomerModel> getInfluencerListForSO(EyDmsUserModel eydmsUser);

    void triggerShipToPartyAddress(String addressId);

	List<B2BCustomerModel> getRetailersTaggedtoSO(EyDmsUserModel so);

    CatalogUnawareMediaModel createMediaFromFile(final String uid, final String documentType, final MultipartFile file );

    EyDmsCustomerModel getEyDmsCustomerForUid(final String uid);

    EyDmsCustomerModel getCurrentEyDmsCustomer();

    List<EyDmsCustomerModel>  getCustomerCards(String dealerCategory, String leadType, String onboardingStatus,
                                             BaseSiteModel site, String searchKey);
    
    AddressModel getAddressByErpId(String erpAddressId, EyDmsCustomerModel customer);

	List<AddressData> filterAddressByLpSource(List<AddressData> filteredAddressList);
	List<EyDmsCustomerModel> getCountOfCreditLimitBreachedUser(EyDmsUserModel eydmsUser);
	
	Boolean sendOnboardingSmsOtp(String name, String mobileNo);
	
	Boolean sendLoginSmsOtp(String uid);

    void checkMobileNumberValidation(String mobileNo);
}
