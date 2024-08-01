package com.scl.core.customer.services.impl;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.customer.services.SclCustomerService;
import com.scl.core.dao.DataConstraintDao;
import com.scl.core.dao.SclUserDao;
import com.scl.core.dao.TerritoryManagementDao;
import com.scl.core.enums.CounterType;
import com.scl.core.event.SclAddressEvent;
import com.scl.core.model.*;
import com.scl.core.services.*;
import com.scl.occ.dto.LoginOTPWsDTO;
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
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DefaultSclCustomerService extends DefaultB2BCustomerService implements SclCustomerService {

	public static final String ID = "%id";
	public static final String DUMMY = "dummy";

	private UserService userService;
    private ModelService modelService;
 
	private MediaService mediaService;
	@Autowired
	DataConstraintDao dataConstraintDao;

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
	private SclUserDao sclUserDao;
	@Resource
	private NetworkService networkService;

	@Autowired
	SlctCrmIntegrationService slctCrmIntegrationService;

    private static final String NOT_SCL_USER_MESSAGE = "Current user is not an SCL user";
	private static final String DEALER = "DEALER";
	private static final String RETAILER = "RETAILER";
	private static final String INFLUENCER = "INFLUENCER";
	private static final String DEALER_ONBOARDING = "DEALER_ONBOARDING";
	private static final String RETAILER_ONBOARDING = "RETAILER_ONBOARDING";
	private static final String INFLUENCER_ONBOARDING = "INFLUENCER_ONBOARDING";
	


    private static final Logger LOG = Logger.getLogger(DefaultSclCustomerService.class);

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
	public SclCustomerModel getSclCustomerForUid(final String uid) {
		SclCustomerModel sclCustomer = null;
		try {
			sclCustomer = getUserService().getUserForUID(uid, SclCustomerModel.class);
		}
		catch (final UnknownIdentifierException | ClassMismatchException e) {
			LOG.error("Failed to get SclCustomer with uid : "+uid);
			LOG.error("Exception is : "+e.getMessage());
			throw new ModelNotFoundException("Failed to get SclCustomer with uid : "+uid);
		}
		return sclCustomer;

	}
	@Override
	public SclCustomerModel getCurrentSclCustomer(){
		SclCustomerModel sclCustomer = null;
		try {
			sclCustomer = (SclCustomerModel) getUserService().getCurrentUser();
		}
		catch (final ClassCastException ex ) {
			LOG.error("Failed to get Cuurent SclCustomer");
			LOG.error("Exception is : "+ex.getMessage());
			throw new ModelNotFoundException("Failed to get Current SclCustomer");
		}
		return sclCustomer;
	}


    @Override
    public List<ProspectiveDealerModel> getProspectiveDealersList(SclUserModel sclUser) {
        Set<B2BCustomerModel> reportees = sclUser.getReportees();
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
    public List<SclCustomerModel> getDealersList(final SclUserModel sclUser) 
    {
//    	Collection<SclCustomerModel> sclCustomers = sclUser.getDealers();
//    	List<SclCustomerModel> sclCustomersRet = new ArrayList<>();
//        UserGroupModel userGroupForUID = userService.getUserGroupForUID("SclDealerGroup");
//        sclCustomersRet = sclCustomers.stream().filter(customer -> customer.getGroups().contains(userGroupForUID)).collect(Collectors.toList());
//    	return sclCustomersRet;
//    	
    	//New Territory Change
   		List<SclCustomerModel> dealerList = territoryManagementService.getDealersForSubArea();
		return dealerList;
	
    }
    
	@Override
	public String saveProfilePicture(MultipartFile file) {
	
		final UserModel currentUser = getUserService().getCurrentUser();
		 if(currentUser instanceof SclUserModel || currentUser instanceof SclCustomerModel){

	            CatalogUnawareMediaModel profilePicture = createMediaFromFile(currentUser.getUid(),"ProfilePicture",file);
	            profilePicture.setAltText("ProfilePicture");
                getModelService().save(profilePicture);
                currentUser.setProfilePicture(profilePicture);
                getModelService().save(currentUser);
                
                return currentUser.getProfilePicture().getURL();
	        }
	        else{
	            throw new ModelNotFoundException(NOT_SCL_USER_MESSAGE);
	        }
		 
	}

	@Override
	public List<SclCustomerModel> getSclCustomersListForFeedback(SclUserModel sclUser) {
//		Collection<SclCustomerModel> sclCustomers = sclUser.getDealers();
//		List<SclCustomerModel> sclCustomersRet = new ArrayList<>();
//		UserGroupModel dealerGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID);
//		UserGroupModel retailerGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID);
//		UserGroupModel influencerGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID);
//		UserGroupModel siteGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SITE_USER_GROUP_UID);
//		sclCustomersRet = sclCustomers.stream()
//				.filter(customer -> customer.getGroups().contains(dealerGroup)
//						|| customer.getGroups().contains(retailerGroup)
//						|| customer.getGroups().contains(influencerGroup) || customer.getGroups().contains(siteGroup))
//				.collect(Collectors.toList());
//		return sclCustomersRet;
		
    	//List<String> subAreas = territoryManagementService.getAllSubAreaForSO(sclUser.getUid());
		//New Territory Change
   		List<SclCustomerModel> sclCustomersRet = territoryManagementService.getAllCustomerForSO();
		return sclCustomersRet;
	}
    
	@Override
	public List<SclCustomerModel> getSclCustomersListForSO(SclUserModel sclUser) {
//		Collection<SclCustomerModel> sclCustomers = sclUser.getDealers();
//		List<SclCustomerModel> sclCustomersRet = new ArrayList<>();
//		UserGroupModel dealerGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID);
//		UserGroupModel retailerGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID);
//		UserGroupModel influencerGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID);
//		sclCustomersRet = sclCustomers.stream()
//				.filter(customer -> customer.getGroups().contains(dealerGroup)
//						|| customer.getGroups().contains(retailerGroup)
//						|| customer.getGroups().contains(influencerGroup))
//				.collect(Collectors.toList());
//		return sclCustomersRet;
		//New Territory Change
   		List<SclCustomerModel> sclCustomersRet = territoryManagementService.getDealerRetailerInfluencerForSubArea();
		return sclCustomersRet;
	}
	    
	@Override
	public List<B2BCustomerModel> getSitesTaggedtoInfluencers(final SclCustomerModel influencer) {
		Collection<SclCustomerModel> taggedPartners = influencer.getSclCustomers();
		List<B2BCustomerModel> siteList = new ArrayList<>();
		UserGroupModel userGroupForUID = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SITE_USER_GROUP_UID);
		siteList = taggedPartners.stream().filter(partner -> partner.getGroups().contains(userGroupForUID))
				.collect(Collectors.toList());
		return siteList;
	}
	
	@Override
	public List<B2BCustomerModel> getTaggedPartnersForSite(final SclCustomerModel site) {
		Collection<B2BCustomerModel> taggedPartners = site.getTaggedPartners();
		List<B2BCustomerModel> taggedPartnerList = new ArrayList<>();
		UserGroupModel dealerGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID);
		UserGroupModel retailerGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID);
		UserGroupModel influencerGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID);
		taggedPartnerList = taggedPartners.stream()
				.filter(customer -> customer.getGroups().contains(dealerGroup)
						|| customer.getGroups().contains(retailerGroup)
						|| customer.getGroups().contains(influencerGroup))
				.collect(Collectors.toList());
		return taggedPartnerList;
	}

	@Override
	public List<SclCustomerModel> getInfluencerListForSO(SclUserModel sclUser) {
//		Collection<SclCustomerModel> influencers = sclUser.getDealers();
//		List<SclCustomerModel> sclCustomersRet = new ArrayList<>();
//		UserGroupModel influencerGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID);
//		sclCustomersRet = influencers.stream()
//				.filter(customer -> customer.getGroups().contains(influencerGroup))
//				.collect(Collectors.toList());
//		return sclCustomersRet;
		//New Territory Change
   		List<SclCustomerModel> influencerList = territoryManagementService.getInfluencersForSubArea();
		return influencerList;
	}

	@Override
	public void triggerShipToPartyAddress(String addressId, SclCustomerModel dealer) {
		SclAddressProcessModel sclAddressProcessModel = new SclAddressProcessModel();

		UserModel currentUser = userService.getCurrentUser();
		if(currentUser instanceof SclCustomerModel){
			SclCustomerModel sclCustomer = (SclCustomerModel) currentUser;
			final String customerType = djpVisitService.getCustomerType(sclCustomer);
			if(StringUtils.isNotBlank(customerType) && customerType.equals(CounterType.DEALER.getCode())){
				CustomerModel customer = (CustomerModel) currentUser;
				AddressModel address = customerAccountService.getAddressForCode(customer, addressId);
				sclAddressProcessModel.setAddress(address);
				sclAddressProcessModel.setCustomer(customer);
				sclAddressProcessModel.setBaseSite(baseSiteService.getCurrentBaseSite());
				SclUserModel sclUser = territoryManagementDao.getSOForSubArea(sclCustomer);
				LOG.error("ADDRESS_DUPLICATE_ISSUE" + " Current User is " + currentUser.getUid() + "-" + currentUser.getName() + " and the address pk is " + address.getPk().toString());
				if(sclUser!=null)
					sclAddressProcessModel.setSclUser(sclUser);
				eventService.publishEvent(new SclAddressEvent(sclAddressProcessModel));
			}
			else if(StringUtils.isNotBlank(customerType) && customerType.equals(CounterType.RETAILER.getCode())){
				CustomerModel customer = (CustomerModel) dealer;
				AddressModel address = customerAccountService.getAddressForCode(customer, addressId);
				sclAddressProcessModel.setAddress(address);
				sclAddressProcessModel.setCustomer(customer);
				sclAddressProcessModel.setBaseSite(baseSiteService.getCurrentBaseSite());
				SclUserModel sclUser = territoryManagementDao.getSOForSubArea(sclCustomer);

				LOG.error("ADDRESS_DUPLICATE_ISSUE" + " Current User is " + currentUser.getUid() + "-" + currentUser.getName() + " and the address pk is " + address.getPk().toString());

				if(sclUser!=null)
					sclAddressProcessModel.setSclUser(sclUser);
				eventService.publishEvent(new SclAddressEvent(sclAddressProcessModel));
			}
		}

	}

	@Override
	public CatalogUnawareMediaModel createMediaFromFile(final String uid, final String documentType,final MultipartFile file )  {

        final String mediaCode = documentType.concat(SclCoreConstants.UNDERSCORE_CHARACTER).concat(uid);

        final MediaFolderModel imageMediaFolder = mediaService.getFolder(SclCoreConstants.IMAGE_MEDIA_FOLDER_NAME);
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
	public List<B2BCustomerModel> getRetailersTaggedtoSO(SclUserModel sclUser) {
    	Collection<SclCustomerModel> sclCustomers = sclUser.getDealers();
    	List<B2BCustomerModel> sclCustomersRet = new ArrayList<>();
        UserGroupModel userGroupForUID = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID);
        sclCustomersRet = sclCustomers.stream().filter(customer -> customer.getGroups().contains(userGroupForUID)).collect(Collectors.toList());
    	return sclCustomersRet;
    }

	//To be Checked
	@Override
	public List<SclCustomerModel>  getCustomerCards(String dealerCategory, String leadType, String onboardingStatus, BaseSiteModel site, String searchKey){
		List<SclCustomerModel> customerList = sclUserDao.getAllCustomersForSubAreaByOnboardingStatus( site, onboardingStatus);
		List<SclCustomerModel> customerFilteredList=new ArrayList<>();
		if(leadType.equalsIgnoreCase(RETAILER)){
			customerFilteredList = customerList.stream().filter(sclCustomerModel -> sclCustomerModel.getGroups().contains(getUserService().getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))).collect(Collectors.toList());
		}
		else if(leadType.equalsIgnoreCase(DEALER)){
			customerFilteredList = customerList.stream().filter(sclCustomerModel -> sclCustomerModel.getGroups().contains(getUserService().getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).collect(Collectors.toList());
		}
		else if(leadType.equalsIgnoreCase(INFLUENCER)){
			customerFilteredList = customerList.stream().filter(sclCustomerModel -> sclCustomerModel.getGroups().contains(getUserService().getUserGroupForUID(SclCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID))).collect(Collectors.toList());
		}
		else if(leadType.equalsIgnoreCase(RETAILER_ONBOARDING)){
			customerFilteredList = customerList.stream().filter(sclCustomerModel -> sclCustomerModel.getGroups().contains(getUserService().getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_ONBOARDING_USER_GROUP_UID))).collect(Collectors.toList());
		}
		else if(leadType.equalsIgnoreCase(DEALER_ONBOARDING)){
			customerFilteredList = customerList.stream().filter(sclCustomerModel -> sclCustomerModel.getGroups().contains(getUserService().getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_ONBOARDING_USER_GROUP_UID))).collect(Collectors.toList());
		}
		else if(leadType.equalsIgnoreCase(INFLUENCER_ONBOARDING)){
			customerFilteredList = customerList.stream().filter(sclCustomerModel -> sclCustomerModel.getGroups().contains(getUserService().getUserGroupForUID(SclCoreConstants.CUSTOMER.INFLUENCER_ONBOARDING_USER_GROUP_UID))).collect(Collectors.toList());
		}
		if(Objects.nonNull(searchKey)) {
			customerFilteredList = networkService.filterSclCustomersWithSearchTerm(customerFilteredList,searchKey);
		}
		if(Objects.nonNull(dealerCategory)){
			customerFilteredList=networkService.filterSclCustomersWithDealerCategory(customerFilteredList,dealerCategory);
		}
		return customerFilteredList;

	}
	
	@Override
	public AddressModel getAddressByErpId(String erpAddressId, SclCustomerModel customer) {
		return sclUserDao.getAddressByErpId(erpAddressId, customer);
	}
	@Override
	public List<AddressData> filterAddressByLpSource(List<AddressData> filteredAddressList) {
		if(filteredAddressList!=null && !filteredAddressList.isEmpty()) {
			List<String> stateDistrictTalukaList = filteredAddressList.stream()
					.filter(data-> data.getState()!=null && data.getDistrict()!=null && data.getErpCity()!=null)
					.map(data-> data.getState().toUpperCase() + data.getDistrict().toUpperCase() + data.getTaluka().toUpperCase() + data.getErpCity().toUpperCase())
					.distinct()
					.collect(Collectors.toList());
			List<String> lpStateDistrictTalukaList = sclUserDao.filterAddressByLpSource(stateDistrictTalukaList);
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
    public List<SclCustomerModel> getCountOfCreditLimitBreachedUser(SclUserModel sclUser) {

		List<SclCustomerModel> sclCustomersRet = getCountOfCreditLimitBreachedUser(getTerritoriesForSO()).stream().filter(d->!(d.getGroups()).contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SITE_USER_GROUP_UID))).collect(Collectors.toList());
		return sclCustomersRet;
    }

	public List<SclCustomerModel> getCountOfCreditLimitBreachedUser(List<SubAreaMasterModel> subAreas) {
		List<SclCustomerModel> customerList = new ArrayList<SclCustomerModel>();
		/*SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String formatted = df.format(new Date());*/
		if(subAreas!=null && !subAreas.isEmpty()) {
			customerList = sclUserDao.getCountOfCreditLimitBreachedUser(subAreas,LocalDate.now().toString());
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
	public LoginOTPWsDTO sendLoginSmsOtp(SclCustomerModel customer, String partnerCustomerFlag, String pcuid) {
		//SclCustomerModel customer  = (SclCustomerModel) userService.getUserForUID(uid);
		PartnerCustomerModel selectedPartner=null;
		LoginOTPWsDTO loginOTPWsDTO=new LoginOTPWsDTO();
		if(StringUtils.isNotEmpty(partnerCustomerFlag) && partnerCustomerFlag.equalsIgnoreCase("TRUE")) {
			if (Objects.nonNull(customer.getPartnerCustomer())) {
				Collection<PartnerCustomerModel> partnerCustomer = customer.getPartnerCustomer();
				List<PartnerCustomerModel> collect = partnerCustomer.stream().filter(a -> a.getId().equalsIgnoreCase(pcuid)).collect(Collectors.toList());
				selectedPartner = collect.get(0);
				LOG.info("selectedPartner" + selectedPartner.getId());
			}
			if (Objects.nonNull(selectedPartner)) {
				if (selectedPartner.getMobileNumber() != null) {
					sendOtpForPartnerCustomer(partnerCustomerFlag, customer,selectedPartner, loginOTPWsDTO);
				} else {
					loginOTPWsDTO.setErrorMessage(String.format("No valid mobile number found for partner customer::%s", pcuid));
					loginOTPWsDTO.setIsOtpSent(Boolean.FALSE);
					loginOTPWsDTO.setIsLoginAllowed(Boolean.FALSE);
				}
			}else{
				loginOTPWsDTO.setErrorMessage(String.format("No valid partner customer found with ::%s", pcuid));
				loginOTPWsDTO.setIsOtpSent(Boolean.FALSE);
				loginOTPWsDTO.setIsLoginAllowed(Boolean.FALSE);
			}
		}
		else
		{
			if(Objects.nonNull(customer))
			{
					if(StringUtils.isNotBlank(customer.getMobileNumber()))
					{
						sendOtp(customer,loginOTPWsDTO,partnerCustomerFlag);
					}else{
						loginOTPWsDTO.setErrorMessage(String.format("No valid mobile number found for customer::%s",customer.getUid()));
						loginOTPWsDTO.setIsOtpSent(Boolean.FALSE);
						loginOTPWsDTO.setIsLoginAllowed(Boolean.FALSE);
					}
			}else{
				loginOTPWsDTO.setErrorMessage(String.format("No valid customer found with id::%s",customer.getUid()));
				loginOTPWsDTO.setIsOtpSent(Boolean.FALSE);
				loginOTPWsDTO.setIsLoginAllowed(Boolean.FALSE);
			}
		}

		return loginOTPWsDTO;
	}

	private void sendOtpForPartnerCustomer(String partnerCustomerFlag, SclCustomerModel customer, PartnerCustomerModel selectedPartner, LoginOTPWsDTO loginOTPWsDTO) {
		boolean	 isOtpSent= smsOtpService.sendLoginSmsOtp(customer,selectedPartner,partnerCustomerFlag);
		if(BooleanUtils.isTrue(isOtpSent)){
			loginOTPWsDTO.setIsOtpSent(isOtpSent);
			loginOTPWsDTO.setIsLoginAllowed(Boolean.TRUE);
			loginOTPWsDTO.setEmailId(customer.getEmail());
			loginOTPWsDTO.setMobileNo(selectedPartner.getMobileNumber());
		}else{
			loginOTPWsDTO.setErrorMessage(String.format("send login otp is failed for partner customer::%s", selectedPartner.getId()));
			loginOTPWsDTO.setIsOtpSent(isOtpSent);
			loginOTPWsDTO.setIsLoginAllowed(Boolean.FALSE);
		}
	}


		private void sendOtp(SclCustomerModel customer, LoginOTPWsDTO loginOTPWsDTO,String partnerCustomerFlag) {
		boolean	 isOtpSent= smsOtpService.sendLoginSmsOtp(customer ,null, partnerCustomerFlag);
		if(BooleanUtils.isTrue(isOtpSent)){
			loginOTPWsDTO.setIsOtpSent(isOtpSent);
			loginOTPWsDTO.setIsLoginAllowed(Boolean.TRUE);
			loginOTPWsDTO.setEmailId(customer.getEmail());
			loginOTPWsDTO.setMobileNo(customer.getMobileNumber());
		}else{
			loginOTPWsDTO.setErrorMessage(String.format("send login otp is failed for customer::%s", customer.getUid()));
			loginOTPWsDTO.setIsOtpSent(isOtpSent);
			loginOTPWsDTO.setIsLoginAllowed(Boolean.FALSE);
		}
	}
	protected boolean checkIsCustomerBlocked(SclCustomerModel sclCustomer) {
		if(Objects.nonNull(sclCustomer)) {
			UserGroupModel dealerGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID);
			UserGroupModel retailerGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID);
			if (sclCustomer.getGroups() != null && (sclCustomer.getGroups().contains(dealerGroup) || sclCustomer.getGroups().contains(retailerGroup))) {
				return slctCrmIntegrationService.isCustomerBlocked(sclCustomer);
			}
		}
		return false;
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
