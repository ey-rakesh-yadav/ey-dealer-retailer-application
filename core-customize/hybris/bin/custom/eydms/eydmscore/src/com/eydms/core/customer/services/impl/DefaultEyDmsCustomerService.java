package com.eydms.core.customer.services.impl;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.customer.services.EyDmsCustomerService;
import com.eydms.core.dao.EyDmsUserDao;
import com.eydms.core.dao.TerritoryManagementDao;
import com.eydms.core.enums.CounterType;
import com.eydms.core.event.EyDmsAddressEvent;
import com.eydms.core.model.*;
import com.eydms.core.services.*;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.services.impl.DefaultB2BCustomerService;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.model.CatalogUnawareMediaModel;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.customer.CustomerAccountService;
import de.hybris.platform.core.model.media.MediaFolderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.event.EventService;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.ClassMismatchException;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DefaultEyDmsCustomerService extends DefaultB2BCustomerService implements EyDmsCustomerService {

    private UserService userService;
    private ModelService modelService;
 
	private MediaService mediaService;

    private FlexibleSearchService flexibleSearchService;

	@Autowired
	CustomerAccountService customerAccountService;

	@Autowired
	EventService eventService;

	@Autowired
	BaseSiteService baseSiteService;

	@Resource
	private DJPVisitService djpVisitService;
	
    @Autowired
    TerritoryManagementService territoryManagementService;
    
    @Autowired
    SmsOtpService smsOtpService;

	@Autowired
	TerritoryManagementDao territoryManagementDao;

	@Resource
	private EyDmsUserDao eydmsUserDao;
	@Resource
	private NetworkService networkService;

	@Autowired
	SlctCrmIntegrationService slctCrmIntegrationService;

    private static final String NOT_EYDMS_USER_MESSAGE = "Current user is not an eydms user";
	private static final String DEALER = "DEALER";
	private static final String RETAILER = "RETAILER";
	private static final String INFLUENCER = "INFLUENCER";
	private static final String DEALER_ONBOARDING = "DEALER_ONBOARDING";
	private static final String RETAILER_ONBOARDING = "RETAILER_ONBOARDING";
	private static final String INFLUENCER_ONBOARDING = "INFLUENCER_ONBOARDING";
	


    private static final Logger LOG = Logger.getLogger(DefaultEyDmsCustomerService.class);

    @Override
    public DealerModel getDealerForCode(final String dealerCode) {
        DealerModel dealer = null;
        try {
            dealer = getUserService().getUserForUID(dealerCode, DealerModel.class);
        }
        catch (final UnknownIdentifierException | ClassMismatchException e) {
            dealer = null;
            LOG.error("Failed to get dealer with code : "+dealerCode);
        }
        return dealer;

    }
	@Override
	public EyDmsCustomerModel getEyDmsCustomerForUid(final String uid) {
		EyDmsCustomerModel eydmsCustomer = null;
		try {
			eydmsCustomer = getUserService().getUserForUID(uid, EyDmsCustomerModel.class);
		}
		catch (final UnknownIdentifierException | ClassMismatchException e) {
			LOG.error("Failed to get EyDmsCustomer with uid : "+uid);
			LOG.error("Exception is : "+e.getMessage());
			throw new ModelNotFoundException("Failed to get EyDmsCustomer with uid : "+uid);
		}
		return eydmsCustomer;

	}
	@Override
	public EyDmsCustomerModel getCurrentEyDmsCustomer(){
		EyDmsCustomerModel eydmsCustomer = null;
		try {
			eydmsCustomer = (EyDmsCustomerModel) getUserService().getCurrentUser();
		}
		catch (final ClassCastException ex ) {
			LOG.error("Failed to get Cuurent EyDmsCustomer");
			LOG.error("Exception is : "+ex.getMessage());
			throw new ModelNotFoundException("Failed to get Current EyDmsCustomer");
		}
		return eydmsCustomer;
	}


    @Override
    public List<ProspectiveDealerModel> getProspectiveDealersList(EyDmsUserModel eydmsUser) {
        Set<B2BCustomerModel> reportees = eydmsUser.getReportees();
        List<ProspectiveDealerModel> reporteesRet = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(reportees)){
            reporteesRet = reportees.stream()
                    .filter(ProspectiveDealerModel.class::isInstance)
                    .map(ProspectiveDealerModel.class::cast)
                    .collect(Collectors.toList());
        }
        return reporteesRet;
    }
    
    @Override
    public List<EyDmsCustomerModel> getDealersList(final EyDmsUserModel eydmsUser) 
    {
//    	Collection<EyDmsCustomerModel> eydmsCustomers = eydmsUser.getDealers();
//    	List<EyDmsCustomerModel> eydmsCustomersRet = new ArrayList<>();
//        UserGroupModel userGroupForUID = userService.getUserGroupForUID("EyDmsDealerGroup");
//        eydmsCustomersRet = eydmsCustomers.stream().filter(customer -> customer.getGroups().contains(userGroupForUID)).collect(Collectors.toList());
//    	return eydmsCustomersRet;
//    	
    	//New Territory Change
   		List<EyDmsCustomerModel> dealerList = territoryManagementService.getDealersForSubArea();
		return dealerList;
	
    }
    
	@Override
	public String saveProfilePicture(MultipartFile file) {
	
		final UserModel currentUser = getUserService().getCurrentUser();
		 if(currentUser instanceof EyDmsUserModel || currentUser instanceof EyDmsCustomerModel){

	            CatalogUnawareMediaModel profilePicture = createMediaFromFile(currentUser.getUid(),"ProfilePicture",file);
	            profilePicture.setAltText("ProfilePicture");
                getModelService().save(profilePicture);
                currentUser.setProfilePicture(profilePicture);
                getModelService().save(currentUser);
                
                return currentUser.getProfilePicture().getURL();
	        }
	        else{
	            throw new ModelNotFoundException(NOT_EYDMS_USER_MESSAGE);
	        }
		 
	}

	@Override
	public List<EyDmsCustomerModel> getEyDmsCustomersListForFeedback(EyDmsUserModel eydmsUser) {
//		Collection<EyDmsCustomerModel> eydmsCustomers = eydmsUser.getDealers();
//		List<EyDmsCustomerModel> eydmsCustomersRet = new ArrayList<>();
//		UserGroupModel dealerGroup = userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID);
//		UserGroupModel retailerGroup = userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID);
//		UserGroupModel influencerGroup = userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID);
//		UserGroupModel siteGroup = userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.SITE_USER_GROUP_UID);
//		eydmsCustomersRet = eydmsCustomers.stream()
//				.filter(customer -> customer.getGroups().contains(dealerGroup)
//						|| customer.getGroups().contains(retailerGroup)
//						|| customer.getGroups().contains(influencerGroup) || customer.getGroups().contains(siteGroup))
//				.collect(Collectors.toList());
//		return eydmsCustomersRet;
		
    	//List<String> subAreas = territoryManagementService.getAllSubAreaForSO(eydmsUser.getUid());
		//New Territory Change
   		List<EyDmsCustomerModel> eydmsCustomersRet = territoryManagementService.getAllCustomerForSO();
		return eydmsCustomersRet;
	}
    
	@Override
	public List<EyDmsCustomerModel> getEyDmsCustomersListForSO(EyDmsUserModel eydmsUser) {
//		Collection<EyDmsCustomerModel> eydmsCustomers = eydmsUser.getDealers();
//		List<EyDmsCustomerModel> eydmsCustomersRet = new ArrayList<>();
//		UserGroupModel dealerGroup = userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID);
//		UserGroupModel retailerGroup = userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID);
//		UserGroupModel influencerGroup = userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID);
//		eydmsCustomersRet = eydmsCustomers.stream()
//				.filter(customer -> customer.getGroups().contains(dealerGroup)
//						|| customer.getGroups().contains(retailerGroup)
//						|| customer.getGroups().contains(influencerGroup))
//				.collect(Collectors.toList());
//		return eydmsCustomersRet;
		//New Territory Change
   		List<EyDmsCustomerModel> eydmsCustomersRet = territoryManagementService.getDealerRetailerInfluencerForSubArea();
		return eydmsCustomersRet;
	}
	    
	@Override
	public List<B2BCustomerModel> getSitesTaggedtoInfluencers(final EyDmsCustomerModel influencer) {
		Collection<EyDmsCustomerModel> taggedPartners = influencer.getEyDmsCustomers();
		List<B2BCustomerModel> siteList = new ArrayList<>();
		UserGroupModel userGroupForUID = userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.SITE_USER_GROUP_UID);
		siteList = taggedPartners.stream().filter(partner -> partner.getGroups().contains(userGroupForUID))
				.collect(Collectors.toList());
		return siteList;
	}
	
	@Override
	public List<B2BCustomerModel> getTaggedPartnersForSite(final EyDmsCustomerModel site) {
		Collection<B2BCustomerModel> taggedPartners = site.getTaggedPartners();
		List<B2BCustomerModel> taggedPartnerList = new ArrayList<>();
		UserGroupModel dealerGroup = userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID);
		UserGroupModel retailerGroup = userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID);
		UserGroupModel influencerGroup = userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID);
		taggedPartnerList = taggedPartners.stream()
				.filter(customer -> customer.getGroups().contains(dealerGroup)
						|| customer.getGroups().contains(retailerGroup)
						|| customer.getGroups().contains(influencerGroup))
				.collect(Collectors.toList());
		return taggedPartnerList;
	}

	@Override
	public List<EyDmsCustomerModel> getInfluencerListForSO(EyDmsUserModel eydmsUser) {
//		Collection<EyDmsCustomerModel> influencers = eydmsUser.getDealers();
//		List<EyDmsCustomerModel> eydmsCustomersRet = new ArrayList<>();
//		UserGroupModel influencerGroup = userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID);
//		eydmsCustomersRet = influencers.stream()
//				.filter(customer -> customer.getGroups().contains(influencerGroup))
//				.collect(Collectors.toList());
//		return eydmsCustomersRet;
		//New Territory Change
   		List<EyDmsCustomerModel> influencerList = territoryManagementService.getInfluencersForSubArea();
		return influencerList;
	}

	@Override
	public void triggerShipToPartyAddress(String addressId) {
		EyDmsAddressProcessModel eydmsAddressProcessModel = new EyDmsAddressProcessModel();

		UserModel currentUser = userService.getCurrentUser();

		if(currentUser instanceof EyDmsCustomerModel){
			EyDmsCustomerModel eydmsCustomer = (EyDmsCustomerModel) currentUser;
			final String customerType = djpVisitService.getCustomerType(eydmsCustomer);
			if(StringUtils.isNotBlank(customerType) && customerType.equals(CounterType.DEALER.getCode())){
				CustomerModel customer = (CustomerModel) currentUser;
				AddressModel address = customerAccountService.getAddressForCode(customer, addressId);
				eydmsAddressProcessModel.setAddress(address);
				eydmsAddressProcessModel.setCustomer(customer);
				eydmsAddressProcessModel.setBaseSite(baseSiteService.getCurrentBaseSite());
				EyDmsUserModel eydmsUser = territoryManagementDao.getSOForSubArea(eydmsCustomer);

				LOG.error("ADDRESS_DUPLICATE_ISSUE" + " Current User is " + currentUser.getUid() + "-" + currentUser.getName() + " and the address pk is " + address.getPk().toString());

				if(eydmsUser!=null)
				eydmsAddressProcessModel.setEyDmsUser(eydmsUser);
				eventService.publishEvent(new EyDmsAddressEvent(eydmsAddressProcessModel));
			}
		}

	}

	@Override
	public CatalogUnawareMediaModel createMediaFromFile(final String uid, final String documentType,final MultipartFile file )  {

        final String mediaCode = documentType.concat(EyDmsCoreConstants.UNDERSCORE_CHARACTER).concat(uid);

        final MediaFolderModel imageMediaFolder = mediaService.getFolder(EyDmsCoreConstants.IMAGE_MEDIA_FOLDER_NAME);
        CatalogUnawareMediaModel documentMedia = null;

        try{
            documentMedia = (CatalogUnawareMediaModel) getMediaService().getMedia(mediaCode);
        }
        catch (AmbiguousIdentifierException ex){
            LOG.error("More than one media found with code : "+mediaCode);
            LOG.error("Removing duplicate media : "+mediaCode);
            CatalogUnawareMediaModel duplicateMedia = new CatalogUnawareMediaModel();
            duplicateMedia.setCode(mediaCode);
            List<CatalogUnawareMediaModel> duplicateMedias = getFlexibleSearchService().getModelsByExample(duplicateMedia);
            getModelService().removeAll(duplicateMedias);
        }
        catch (UnknownIdentifierException uie){
            if(LOG.isDebugEnabled()){
                LOG.error("No Media found with code : "+mediaCode);
            }
        }
        finally {
            if(null == documentMedia){
                documentMedia = getModelService().create(CatalogUnawareMediaModel.class);
                documentMedia.setCode(mediaCode);
            }
        }
        documentMedia.setFolder(imageMediaFolder);
        documentMedia.setMime(file.getContentType());
		documentMedia.setRealFileName(file.getName());
        getModelService().save(documentMedia);
        try{
            getMediaService().setStreamForMedia(documentMedia,file.getInputStream());
        }
        catch (IOException ioe){
            LOG.error("IO Exception occured while creating: "+documentType+ " ,for dealer with uid: "+uid);
        }

        return (CatalogUnawareMediaModel) getMediaService().getMedia(mediaCode);

    }
	
    @Override
    public UserService getUserService() {
        return userService;
    }

    @Override
    public void setUserService(UserService userService) {
        this.userService = userService;
    }


	public MediaService getMediaService() {
		return mediaService;
	}


	public void setMediaService(MediaService mediaService) {
		this.mediaService = mediaService;
	}


	public FlexibleSearchService getFlexibleSearchService() {
		return flexibleSearchService;
	}


	public void setFlexibleSearchService(FlexibleSearchService flexibleSearchService) {
		this.flexibleSearchService = flexibleSearchService;
	}

	public ModelService getModelService() {
		return modelService;
	}

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}


	@Override
	public List<B2BCustomerModel> getRetailersTaggedtoSO(EyDmsUserModel eydmsUser) {
    	Collection<EyDmsCustomerModel> eydmsCustomers = eydmsUser.getDealers();
    	List<B2BCustomerModel> eydmsCustomersRet = new ArrayList<>();
        UserGroupModel userGroupForUID = userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID);
        eydmsCustomersRet = eydmsCustomers.stream().filter(customer -> customer.getGroups().contains(userGroupForUID)).collect(Collectors.toList());
    	return eydmsCustomersRet;
    }

	//To be Checked
	@Override
	public List<EyDmsCustomerModel>  getCustomerCards(String dealerCategory, String leadType, String onboardingStatus, BaseSiteModel site, String searchKey){
		List<EyDmsCustomerModel> customerList = eydmsUserDao.getAllCustomersForSubAreaByOnboardingStatus( site, onboardingStatus);
		List<EyDmsCustomerModel> customerFilteredList=new ArrayList<>();
		if(leadType.equalsIgnoreCase(RETAILER)){
			customerFilteredList = customerList.stream().filter(eydmsCustomerModel -> eydmsCustomerModel.getGroups().contains(getUserService().getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))).collect(Collectors.toList());
		}
		else if(leadType.equalsIgnoreCase(DEALER)){
			customerFilteredList = customerList.stream().filter(eydmsCustomerModel -> eydmsCustomerModel.getGroups().contains(getUserService().getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList());
		}
		else if(leadType.equalsIgnoreCase(INFLUENCER)){
			customerFilteredList = customerList.stream().filter(eydmsCustomerModel -> eydmsCustomerModel.getGroups().contains(getUserService().getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID))).collect(Collectors.toList());
		}
		else if(leadType.equalsIgnoreCase(RETAILER_ONBOARDING)){
			customerFilteredList = customerList.stream().filter(eydmsCustomerModel -> eydmsCustomerModel.getGroups().contains(getUserService().getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_ONBOARDING_USER_GROUP_UID))).collect(Collectors.toList());
		}
		else if(leadType.equalsIgnoreCase(DEALER_ONBOARDING)){
			customerFilteredList = customerList.stream().filter(eydmsCustomerModel -> eydmsCustomerModel.getGroups().contains(getUserService().getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_ONBOARDING_USER_GROUP_UID))).collect(Collectors.toList());
		}
		else if(leadType.equalsIgnoreCase(INFLUENCER_ONBOARDING)){
			customerFilteredList = customerList.stream().filter(eydmsCustomerModel -> eydmsCustomerModel.getGroups().contains(getUserService().getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.INFLUENCER_ONBOARDING_USER_GROUP_UID))).collect(Collectors.toList());
		}
		if(Objects.nonNull(searchKey)) {
			customerFilteredList = networkService.filterEyDmsCustomersWithSearchTerm(customerFilteredList,searchKey);
		}
		if(Objects.nonNull(dealerCategory)){
			customerFilteredList=networkService.filterEyDmsCustomersWithDealerCategory(customerFilteredList,dealerCategory);
		}
		return customerFilteredList;

	}
	
	@Override
	public AddressModel getAddressByErpId(String erpAddressId, EyDmsCustomerModel customer) {
		return eydmsUserDao.getAddressByErpId(erpAddressId, customer);
	}
	@Override
	public List<AddressData> filterAddressByLpSource(List<AddressData> filteredAddressList) {
		if(filteredAddressList!=null && !filteredAddressList.isEmpty()) {
			List<String> stateDistrictTalukaList = filteredAddressList.stream()
					.filter(data-> data.getState()!=null && data.getDistrict()!=null && data.getErpCity()!=null)
					.map(data-> data.getState().toUpperCase() + data.getDistrict().toUpperCase() + data.getTaluka().toUpperCase() + data.getErpCity().toUpperCase())
					.distinct()
					.collect(Collectors.toList());
			List<String> lpStateDistrictTalukaList = eydmsUserDao.filterAddressByLpSource(stateDistrictTalukaList);
			if(lpStateDistrictTalukaList!=null && !lpStateDistrictTalukaList.isEmpty()) {
				filteredAddressList = filteredAddressList.stream()
						.filter(data1-> {
							if(data1.getState()!=null && data1.getDistrict()!=null && data1.getTaluka()!=null && data1.getErpCity()!=null) {
								String s = data1.getState().toUpperCase() + data1.getDistrict().toUpperCase()  + data1.getTaluka().toUpperCase() + data1.getErpCity().toUpperCase();
								if(lpStateDistrictTalukaList.contains(s))
									return true;
								else
									return false;
							}
							else
							{
								return false;
							}
						} ).collect(Collectors.toList());
			}
			else {
				return Collections.emptyList();
			}
		}
		return filteredAddressList;
	}
	
    @Override
    public List<EyDmsCustomerModel> getCountOfCreditLimitBreachedUser(EyDmsUserModel eydmsUser) {

		List<EyDmsCustomerModel> eydmsCustomersRet = getCountOfCreditLimitBreachedUser(getTerritoriesForSO()).stream().filter(d->!(d.getGroups()).contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.SITE_USER_GROUP_UID))).collect(Collectors.toList());
		return eydmsCustomersRet;
    }

	public List<EyDmsCustomerModel> getCountOfCreditLimitBreachedUser(List<SubAreaMasterModel> subAreas) {
		List<EyDmsCustomerModel> customerList = new ArrayList<EyDmsCustomerModel>();
		/*SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String formatted = df.format(new Date());*/
		if(subAreas!=null && !subAreas.isEmpty()) {
			customerList = eydmsUserDao.getCountOfCreditLimitBreachedUser(subAreas,LocalDate.now().toString());
		}
		return customerList;
	}


	public List<SubAreaMasterModel> getTerritoriesForSO() {
		return territoryManagementService.getTerritoriesForSO();
	}
	@Override
	public Boolean sendOnboardingSmsOtp(String name, String mobileNo) {
		
		return smsOtpService.sendOnboardingSmsOtp(name,mobileNo);
		
	}
	@Override
	public Boolean sendLoginSmsOtp(String uid) {
		//EyDmsCustomerModel customer  = (EyDmsCustomerModel) userService.getUserForUID(uid);
		EyDmsCustomerModel customer = slctCrmIntegrationService.findCustomerByCustomerNo(uid);
		if(!Objects.isNull(customer))
		{
			//checkMobileNumberValidation(customer.getMobileNumber());
			if(customer.getMobileNumber()!=null)
			{
				return smsOtpService.sendLoginSmsOtp(customer.getUid(),customer.getMobileNumber(),customer.getCustomerNo());
			}
		}
		
		return Boolean.FALSE;
	}

	@Override
	public void checkMobileNumberValidation(String mobileNo) {
		String regex="^[0-9]{10}$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(mobileNo);
		if(StringUtils.isBlank(mobileNo) || !matcher.matches()) {
			throw new UnknownIdentifierException("Registered Mobile No. Invalid");
		}
	}
}
