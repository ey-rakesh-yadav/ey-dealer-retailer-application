package com.eydms.facades.customer.impl;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.customer.services.EyDmsCustomerAccountService;
import com.eydms.core.customer.services.EyDmsCustomerService;
import com.eydms.core.dao.EYDMSGenericDao;
import com.eydms.core.dao.TerritoryManagementDao;
import com.eydms.core.enums.CompanyType;
import com.eydms.core.enums.CustomerOnboardingStatus;
import com.eydms.core.model.*;
import com.eydms.core.prosdealer.service.ProsDealerService;
import com.eydms.core.region.service.RegionService;
import com.eydms.core.services.DJPVisitService;
import com.eydms.facades.customer.EyDmsCustomerFacade;
import com.eydms.facades.data.*;
import com.eydms.facades.data.order.vehicle.DealerDriverDetailsData;
import com.eydms.facades.data.order.vehicle.DealerVehicleDetailsData;
import com.eydms.facades.populators.EyDmsCustomerReversePopulator;
import com.eydms.facades.prosdealer.data.*;
import com.eydms.facades.util.GenericMediaUtil;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2bacceleratorfacades.customer.impl.DefaultB2BCustomerFacade;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.commercefacades.product.data.ImageData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.user.PasswordEncoderConstants;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class DefaultEyDmsCustomerFacade extends DefaultB2BCustomerFacade implements EyDmsCustomerFacade {

    private EyDmsCustomerAccountService eydmsCustomerAccountService;
    private UserService userService;
    private BaseStoreService baseStoreService;
    private EyDmsCustomerService eydmsCustomerService;
    private ProsDealerService prosDealerService;
    private FlexibleSearchService flexibleSearchService;
    private Converter<ProspectiveDealerModel, ProsDealerData> prosDealerListConverter;
    private Converter<ProspectiveDealerModel, ProsDealerData> prosDealerDetailsConverter;
    private Converter<UserModel, CustomerData> customerConverter;
    private Converter<B2BCustomerModel, CustomerData> dealerBasicConverter;
    private EyDmsCustomerReversePopulator eydmsCustomerReversePopulator;
    private String passwordEncoding = PasswordEncoderConstants.DEFAULT_ENCODING;
    private RegionService regionService;
    @Resource
    private Converter<MediaModel, ImageData> imageConverter;
    
    @Resource
    private Populator<MediaModel, EYDMSImageData> eydmsImagePopulator;
    
    @Resource
    private ModelService modelService;
    @Resource
    private Converter<EYDMSRetailerData, EyDmsCustomerModel> eydmsRetailerReverseConverter;
    private static final String NOT_EYDMS_USER_MESSAGE = "Current user is not an EYDMS user";

    private static final Logger LOGGER = Logger.getLogger(DefaultEyDmsCustomerFacade.class);
    @Resource
    private Converter<EYDMSDirectorData, ProsDealerCompanyModel> eydmsDirectorReverseConverter;
    @Resource
    private Converter<EYDMSPartnerData, PartnershipModel> eydmsPartnerReverseConverter;
    @Resource
    private Converter<ProsDealerCompanyModel, EYDMSDirectorData> eydmsDirectorConverter;
    @Resource
    private Converter<PartnershipModel, EYDMSPartnerData> eydmsPartnerConverter;
    @Resource
    private Converter<BrandWiseSaleData, BrandWiseSaleModel> brandWiseSaleReverseConverter;
    @Resource
    private Converter<BrandWiseSaleModel, BrandWiseSaleData> brandWiseSaleConverter;
    @Resource
    private Converter<DealerVehicleDetailsData, DealerVehicleDetailsModel> vehicleDetailsReverseConverter;
    @Resource
    private Converter<DealerVehicleDetailsModel, DealerVehicleDetailsData> vehicleDetailsConverter;
    @Resource
    private Converter<DealerDriverDetailsData, DealerDriverDetailsModel> driveDetailsReverseConverter;
    @Resource
    private Converter<DealerDriverDetailsModel, DealerDriverDetailsData> driveDetailsConverter;
    @Resource
    private Converter<MultipartFile, MediaModel> eydmsMediaReverseConverter;
    @Resource
    private Converter<NominationData, NominationModel> nominationConverter;
    @Resource
    private EYDMSGenericDao eydmsGenericDao;
    @Resource
    private GenericMediaUtil genericMediaUtil;
    @Resource
    private Converter<EyDmsCustomerModel, EYDMSRetailerData> eydmsRetailerConverter;
    @Resource
    private Converter<EyDmsCustomerModel, CustomerStagingData> customerStagingDataConverter;
    @Resource
    private Converter<EyDmsCustomerModel, CustomerDetailedData> customerDetailedDataConverter;

    @Resource
    private Converter<EyDmsCustomerModel, InfluencerData> influncerConverter;
    @Resource
    private Populator<EyDmsCustomerModel,InfluencerFinanceData> influencerFinancialsPopulator;
    
    @Resource
    private Converter<EYDMSDealerData, EyDmsCustomerModel> eydmsDealerReverseConverter;

    @Resource
    private Converter<EYDMSSalesPromoterData, EyDmsCustomerModel> eydmsSalesPromoterReverseConverter;
    
    @Resource
    Populator<NominationData, NominationModel> eydmsNominationReversePopulator;
    
    @Resource
    Populator<NominationModel,NominationData> eydmsNominationPopulator;
    
    @Resource
    BaseSiteService baseSiteService;
    
    @Resource
    DJPVisitService djpVisitService;
    
    @Resource
    TerritoryManagementDao territoryManagementDao;
    /**
     * calculates and saves customer's last six month avaerage order value
     */
    @Override
    public void calculateLastSixMonthsAverageOrderValue() {

        OrderStatus[] statuses = {OrderStatus.COMPLETED, OrderStatus.CREATED};
        BaseStoreModel baseStoreModel = getBaseStoreService().getCurrentBaseStore();
        CustomerModel customerModel = (CustomerModel) getUserService().getCurrentUser();
        getEyDmsCustomerAccountService().updateCustomerAverageOrderValue(customerModel, baseStoreModel, statuses);
    }


    @Override
    public ProsDealerListData getProspectiveDealersForCurrentuser() {

        ProsDealerListData prosDealerListData = new ProsDealerListData();
        List<ProspectiveDealerModel> prospectiveDealersList = new ArrayList<>();
        final UserModel currentUser = getUserService().getCurrentUser();
        if (currentUser instanceof EyDmsUserModel) {
            final EyDmsUserModel eydmsUser = (EyDmsUserModel) currentUser;
            prospectiveDealersList = getEyDmsCustomerService().getProspectiveDealersList(eydmsUser);
        } else {
            throw new ModelNotFoundException(NOT_EYDMS_USER_MESSAGE);
        }
        List<ProsDealerData> prosDealerData = Optional.of(prospectiveDealersList.stream()
                .map(prosDealer -> getProsDealerListConverter()
                        .convert(prosDealer)).collect(Collectors.toList())).get();
        prosDealerListData.setProsDealers(prosDealerData);
        return prosDealerListData;
    }

    @Override
    public ProsDealerData getProsDealerDetailsByUid(final String uid) {
        final ProspectiveDealerModel prospectiveDealer = getProsDealerService().getProsDealerByUid(uid);
        if (null == prospectiveDealer) {
            throw new ModelNotFoundException("No Prospective dealer found with uid : " + uid);
        }
        return getProsDealerDetailsConverter().convert(prospectiveDealer);
    }

    @Override
    public ProsDealerListData getProsDealerPendingForSOAssignment() {
        List<ProspectiveDealerModel> prosDealers = new ArrayList<>();
        ProsDealerListData prosDealerDataList = new ProsDealerListData();
        EyDmsUserModel districtInCharge;
        try {
            districtInCharge = (EyDmsUserModel) userService.getCurrentUser();
            LOGGER.info("TRY JREBEL");
        } catch (ClassCastException e) {
            LOGGER.error(NOT_EYDMS_USER_MESSAGE);
            districtInCharge = null;
        }
        //TODO:: CHECK IF CURRENT USER HAS DI ROLE

        if (null != districtInCharge) {
            prosDealers = getProsDealerService().fetchProsDealerPendingForSOAssignment(districtInCharge);
        }
        if (CollectionUtils.isNotEmpty(prosDealers)) {
            List<ProsDealerData> prosDealerData = Optional.of(prosDealers.stream()
                    .map(prosDealer -> getProsDealerDetailsConverter()
                            .convert(prosDealer)).collect(Collectors.toList())).get();
            prosDealerDataList.setProsDealers(prosDealerData);
        }

        return prosDealerDataList;
    }

    @Override
    public CustomerData getUserDetailsForProfilePage() {
        CustomerModel currentUser = (CustomerModel) getUserService().getCurrentUser();
        return getCustomerConverter().convert(currentUser);
    }

    @Override
    public DealerListData getDealersForCurrentUser() {
        DealerListData dealerListData = new DealerListData();
        List<EyDmsCustomerModel> dealersList = new ArrayList<>();
        final UserModel currentUser = getUserService().getCurrentUser();

        if (currentUser instanceof EyDmsUserModel) {
            final EyDmsUserModel eydmsUser = (EyDmsUserModel) currentUser;
            dealersList = getEyDmsCustomerService().getDealersList(eydmsUser);
        } else {
            throw new ModelNotFoundException(NOT_EYDMS_USER_MESSAGE);
        }

        List<CustomerData> dealerData = Optional.of(dealersList.stream()
                .map(b2BCustomer -> getDealerBasicConverter()
                        .convert(b2BCustomer)).collect(Collectors.toList())).get();
        dealerListData.setDealers(dealerData);
        return dealerListData;

    }

    @Override
    public void updateUserDetailsOnProfilePage(final CustomerData customerData) throws DuplicateUidException {
        final CustomerModel customer = getCurrentSessionCustomer();
        getEyDmsCustomerReversePopulator().populate(customerData, customer);
        getModelService().save(customer);
    }

    @Override
    public void setNewPassword(String mobileNumber, String newPassword) {

        B2BCustomerModel customer = new B2BCustomerModel();
        customer.setMobileNumber(mobileNumber);

        B2BCustomerModel customerModel = new B2BCustomerModel();

        try {
            customerModel = getFlexibleSearchService().getModelByExample(customer);
        } catch (ModelNotFoundException e) {
            LOGGER.warn("Mobile number not found");
        }

        String uid = customerModel.getUid();

        getUserService().setPassword(uid, newPassword, getPasswordEncoding());
    }

    /**
     * Create new contact number for user and send to for approval
     *
     * @param userId
     * @param newContactNumber
     * @return
     */
    @Override
    public CustomerData updateUsersContactNumber(final String userId, final String newContactNumber) {

        final UserModel user = getUserService().getUserForUID(userId);
        final EyDmsUserModel eydmsUser = (EyDmsUserModel) user;
        final Optional<EyDmsUserModel> reporter = eydmsUser.getReporter().stream().findFirst();
        EyDmsUserModel supervisor = reporter.orElse(null);
        if (null == supervisor) {
            return null;
        }
        getEyDmsCustomerAccountService().updateUsersContactNumber(user, newContactNumber);
        return getCustomerConverter().convert(supervisor);


    }

    @Override
    public DealerListData getRetailersTaggedToSO() {

        DealerListData dealerListData = new DealerListData();
        List<B2BCustomerModel> dealersList = new ArrayList<>();
        final UserModel currentUser = getUserService().getCurrentUser();

        if (currentUser instanceof EyDmsUserModel) {
            final EyDmsUserModel eydmsUser = (EyDmsUserModel) currentUser;
            dealersList = getEyDmsCustomerService().getRetailersTaggedtoSO(eydmsUser);
        } else {
            throw new ModelNotFoundException(NOT_EYDMS_USER_MESSAGE);
        }

        List<CustomerData> dealerData = Optional.of(dealersList.stream()
                .map(b2BCustomer -> getDealerBasicConverter()
                        .convert(b2BCustomer)).collect(Collectors.toList())).get();
        dealerListData.setDealers(dealerData);
        return dealerListData;

    }

    @Override
    public DealerListData getEyDmsCustomersListForFeedback() {
        DealerListData dealerListData = new DealerListData();
        List<EyDmsCustomerModel> eydmsCustomersList = new ArrayList<>();
        final UserModel currentUser = getUserService().getCurrentUser();

        if (currentUser instanceof EyDmsUserModel) {
            final EyDmsUserModel eydmsUser = (EyDmsUserModel) currentUser;
            eydmsCustomersList = getEyDmsCustomerService().getEyDmsCustomersListForFeedback(eydmsUser);
        } else {
            throw new ModelNotFoundException(NOT_EYDMS_USER_MESSAGE);
        }

        List<CustomerData> dealerData = Optional.of(eydmsCustomersList.stream()
                .map(b2BCustomer -> getDealerBasicConverter()
                        .convert(b2BCustomer)).collect(Collectors.toList())).get();
        dealerListData.setDealers(dealerData);
        return dealerListData;
    }

    @Override
    public DealerListData getEyDmsCustomersListForSO() {
        DealerListData dealerListData = new DealerListData();
        List<EyDmsCustomerModel> eydmsCustomersList = new ArrayList<>();
        final UserModel currentUser = getUserService().getCurrentUser();

        if (currentUser instanceof EyDmsUserModel) {
            final EyDmsUserModel eydmsUser = (EyDmsUserModel) currentUser;
            eydmsCustomersList = getEyDmsCustomerService().getEyDmsCustomersListForSO(eydmsUser);
        } else {
            throw new ModelNotFoundException(NOT_EYDMS_USER_MESSAGE);
        }

        List<CustomerData> dealerData = Optional.of(eydmsCustomersList.stream()
                .map(b2BCustomer -> getDealerBasicConverter()
                        .convert(b2BCustomer)).collect(Collectors.toList())).get();
        dealerListData.setDealers(dealerData);
        return dealerListData;
    }


    @Override
    public List<EyDmsSiteData> getSitesTaggedtoInfluencers() {
        List<B2BCustomerModel> influencerList = new ArrayList<>();
        final UserModel currentUser = getUserService().getCurrentUser();

        if (currentUser instanceof EyDmsCustomerModel) {
            final EyDmsCustomerModel influencer = (EyDmsCustomerModel) currentUser;
            influencerList = getEyDmsCustomerService().getSitesTaggedtoInfluencers(influencer);
        } else {
            throw new ModelNotFoundException(NOT_EYDMS_USER_MESSAGE);
        }
        List<EyDmsSiteData> list = new ArrayList<EyDmsSiteData>();
        if (influencerList != null) {
            influencerList.stream().forEach(cust -> {
                EyDmsSiteData data = new EyDmsSiteData();
                data.setCode(cust.getUid());
                data.setName(cust.getName());

                Collection<AddressModel> listOfAddresses = cust.getAddresses();
                if (CollectionUtils.isNotEmpty(listOfAddresses)) {
                    List<AddressModel> billingAddressList = listOfAddresses.stream().filter(address -> address.getBillingAddress()).collect(Collectors.toList());
                    if (billingAddressList != null && !billingAddressList.isEmpty()) {
                        AddressModel billingAddress = billingAddressList.get(0);
                        if (null != billingAddress) {
                            AddressData address = getAddressConverter().convert(billingAddress);
                            data.setAddress(address);
                        }
                    }
                }
                list.add(data);
            });
        }
        return list;

    }

    @Override
    public List<EyDmsSiteData> getSitesTaggedtoInfluencers(EyDmsCustomerModel influencer){
        List<B2BCustomerModel> influencerList = new ArrayList<>();

        if (influencer instanceof EyDmsCustomerModel) {
            influencerList = getEyDmsCustomerService().getSitesTaggedtoInfluencers(influencer);
        } else {
            throw new ModelNotFoundException(NOT_EYDMS_USER_MESSAGE);
        }
        List<EyDmsSiteData> list = new ArrayList<EyDmsSiteData>();
        if (influencerList != null) {
            influencerList.stream().forEach(cust -> {
                EyDmsSiteData data = new EyDmsSiteData();
                data.setCode(cust.getUid());
                data.setName(cust.getName());

                Collection<AddressModel> listOfAddresses = cust.getAddresses();
                if (CollectionUtils.isNotEmpty(listOfAddresses)) {
                    List<AddressModel> billingAddressList = listOfAddresses.stream().filter(address -> address.getBillingAddress()).collect(Collectors.toList());
                    if (billingAddressList != null && !billingAddressList.isEmpty()) {
                        AddressModel billingAddress = billingAddressList.get(0);
                        if (null != billingAddress) {
                            AddressData address = getAddressConverter().convert(billingAddress);
                            data.setAddress(address);
                        }
                    }
                }
                list.add(data);
            });
        }
        return list;
    }

    @Override
    public List<EyDmsTaggedPartnersData> getTaggedPartnersForSite() {
        List<B2BCustomerModel> taggedPartnersList = new ArrayList<>();
        final UserModel currentUser = getUserService().getCurrentUser();

        if (currentUser instanceof EyDmsCustomerModel) {
            final EyDmsCustomerModel site = (EyDmsCustomerModel) currentUser;
            taggedPartnersList = getEyDmsCustomerService().getTaggedPartnersForSite(site);
        } else {
            throw new ModelNotFoundException(NOT_EYDMS_USER_MESSAGE);
        }
        List<EyDmsTaggedPartnersData> list = new ArrayList<EyDmsTaggedPartnersData>();
        UserGroupModel influencerGroup = getUserService().getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID);
        if (taggedPartnersList != null) {
            taggedPartnersList.stream().forEach(cust -> {
                EyDmsTaggedPartnersData data = new EyDmsTaggedPartnersData();
                data.setCode(null != cust.getUid() ? cust.getUid() : "");
                data.setName(null != cust.getName() ? cust.getName() : "");
                data.setContactNo(null != ((EyDmsCustomerModel) cust).getContactNumber() ? ((EyDmsCustomerModel) cust).getContactNumber() : "");
                if (cust.getGroups().contains(influencerGroup)) {
                    data.setInfluencerCategory(null != ((EyDmsCustomerModel) cust).getInfluencerType() ? ((EyDmsCustomerModel) cust).getInfluencerType().getCode() : "");
                }
                if (((EyDmsCustomerModel) cust).getProfilePicture() != null) {
                    populateProfilePicture(((EyDmsCustomerModel) cust).getProfilePicture(), data);
                }
                data.setOrderHistory(null != ((EyDmsCustomerModel) cust).getOrderHistory() ? ((EyDmsCustomerModel) cust).getOrderHistory() : "");
                list.add(data);
            });
        }
        return list;
    }

    @Override
    public boolean addTaggedPartnersForSite(String uid) throws DuplicateUidException {
        EyDmsCustomerModel taggedPartner = (EyDmsCustomerModel) getUserService().getUserForUID(uid);
        final UserModel currentUser = getUserService().getCurrentUser();
        EyDmsCustomerModel site = modelService.create(EyDmsCustomerModel.class);
        List<B2BCustomerModel> taggedPartnersList = new ArrayList<>();

        if (currentUser instanceof EyDmsCustomerModel) {
            site = (EyDmsCustomerModel) currentUser;
            if (getEyDmsCustomerService().getTaggedPartnersForSite(site) != null && !getEyDmsCustomerService().getTaggedPartnersForSite(site).isEmpty())
                taggedPartnersList = getEyDmsCustomerService().getTaggedPartnersForSite(site);
        } else {
            throw new ModelNotFoundException(NOT_EYDMS_USER_MESSAGE);
        }

        if (null != taggedPartner) {
            if (taggedPartner.getEyDmsCustomers().contains(site)) {
                throw new DuplicateUidException("This user already tagged to the site" + site.getName());
            } else {
                try {
                    taggedPartnersList.add(taggedPartner);
                    site.setTaggedPartners(taggedPartnersList);
                    modelService.save(site);
                    return true;
                } catch (ModelSavingException mse) {
                    LOGGER.error("Error occured while updating Site " + site.getName() + "\n");
                    LOGGER.error("Exception is: " + mse.getMessage());
                    return false;
                }
            }
        } else {
            throw new ModelNotFoundException("No User found with uid : " + uid);
        }
    }

    @Override
    public String addRetailerdata(EYDMSRetailerData eydmsRetailerData) {
    	EyDmsCustomerModel eydmsCustomer = eydmsRetailerReverseConverter.convert(eydmsRetailerData);
        if (Objects.nonNull(eydmsCustomer)) {

        	
//        	if(eydmsCustomer.getOnboardingPlacedBy()==null)
//            {
//            	List<SubAreaMasterModel> subAreas = territoryManagementDao.getTerritoriesForCustomer(eydmsCustomer);
//    			List<EyDmsUserModel> eydmsUsers = territoryManagementDao.getEyDmsUsersForSubArea(subAreas);
//    			eydmsCustomer.setOnboardingPlacedBy(eydmsUsers.get(0));
//    			eydmsCustomer.setOnboardingPartner(eydmsUsers.get(0));
//    			modelService.save(eydmsCustomer);
//            }
        	
            return eydmsCustomer.getUid();
        }
        return null;
    }

    @Override
    public EYDMSRetailerData getRetailerData(String uid) {
        EyDmsCustomerModel retailer = (EyDmsCustomerModel) getUserService().getUserForUID(uid);
        return eydmsRetailerConverter.convert(retailer);
    }

    @Override
    public EYDMSCompanyDetailsData getCompanyDetails(String uid) {
        EyDmsCustomerModel retailer = (EyDmsCustomerModel) getUserService().getUserForUID(uid);
        EYDMSCompanyDetailsData data = new EYDMSCompanyDetailsData();
        if(Objects.nonNull(retailer.getCompanyType())) {
            data.setCompanyType(retailer.getCompanyType().getCode());
            if (retailer.getCompanyType().equals(CompanyType.COMPANY)) {
                if (CollectionUtils.isNotEmpty(retailer.getDirectors())) {
                    data.setDirectors(eydmsDirectorConverter.convertAll(retailer.getDirectors()));
                }
            } else {
                if (Objects.nonNull(retailer.getPartners())) {
                    data.setPartners(eydmsPartnerConverter.convertAll(retailer.getPartners()));
                }
            }
        }
        return data;
    }

    @Override
    public String addCompanyDetails(EYDMSCompanyDetailsData companyDetailsData) {
        EyDmsCustomerModel retailer = (EyDmsCustomerModel) getUserService().getUserForUID(companyDetailsData.getRetailerId());
        retailer.setCompanyType(CompanyType.valueOf(companyDetailsData.getCompanyType()));
        if (Objects.nonNull(companyDetailsData.getDirectors())) {
            List<ProsDealerCompanyModel> directorList = new ArrayList<>();
            if (Objects.nonNull(retailer.getDirectors())) {
                directorList.addAll(retailer.getDirectors());
            }
            directorList.addAll(eydmsDirectorReverseConverter.convertAll(companyDetailsData.getDirectors()));
            retailer.setDirectors(directorList);
        }
        if (Objects.nonNull(companyDetailsData.getPartners())) {
            List<PartnershipModel> partnerList = new ArrayList<>();
            if (Objects.nonNull(retailer.getPartners())) {
                partnerList.addAll(retailer.getPartners());
            }
            partnerList.addAll(eydmsPartnerReverseConverter.convertAll(companyDetailsData.getPartners()));
            retailer.setPartners(partnerList);
        }
        
        if(companyDetailsData.getFormSS()!=null)
        {
        	List<MediaModel> formImages = null;
        	if((retailer.getOnboardingFormsImages() == null) || (retailer.getOnboardingFormsImages().isEmpty()))
            {
        		formImages = new ArrayList<>();
            }
        	else
        	{
        		formImages = new ArrayList<>(retailer.getOnboardingFormsImages());
        	}
        	
        	formImages.add(eydmsMediaReverseConverter.convert(genericMediaUtil.getMultipartFile(companyDetailsData.getFormSS().getByteStream(),companyDetailsData.getFormSS().getFileName())));
        	retailer.setOnboardingFormsImages(formImages);
        }
        
        modelService.save(retailer);

        return retailer.getUid();
    }

    @Override
    public String addBusinessInformation(EYDMSBusinessInfoData businessInfoData) {
        EyDmsCustomerModel retailer = (EyDmsCustomerModel) getUserService().getUserForUID(businessInfoData.getRetailerId());
        retailer.setWarehouseCapacity(businessInfoData.getWarehouseCapacity());
        if (Objects.nonNull(businessInfoData.getBrandWiseSales())) {
            List<BrandWiseSaleModel> brandWiseSaleModels = new ArrayList<>();
            if (Objects.nonNull(retailer.getBrandWiseSales())) {
                brandWiseSaleModels.addAll(retailer.getBrandWiseSales());
            }
            brandWiseSaleModels.addAll(brandWiseSaleReverseConverter.convertAll(businessInfoData.getBrandWiseSales()));
            retailer.setCounterPotential(brandWiseSaleModels.stream().mapToDouble(BrandWiseSaleModel::getSaleInMT).sum());
            retailer.setBrandWiseSales(brandWiseSaleModels);
        }
        if (Objects.nonNull(businessInfoData.getVehicleDetails())) {
            businessInfoData.getVehicleDetails().forEach(v -> {
                var vehicle = (DealerVehicleDetailsModel) eydmsGenericDao.findItemByTypeCodeAndUidParam(DealerVehicleDetailsModel._TYPECODE, DealerVehicleDetailsModel.VEHICLENUMBER, v.getVehicleNumber());
                if (Objects.nonNull(vehicle)) {
                    vehicleDetailsReverseConverter.convert(v, vehicle);
                } else {
                    vehicle = vehicleDetailsReverseConverter.convert(v);
                }
                vehicle.setDealer(retailer);
                modelService.save(vehicle);
            });

        }
        if (Objects.nonNull(businessInfoData.getDriveDetails())) {
            businessInfoData.getDriveDetails().forEach(drv -> {
                DealerDriverDetailsModel driver = (DealerDriverDetailsModel) eydmsGenericDao.findItemByTypeCodeAndUidParam(DealerDriverDetailsModel._TYPECODE, DealerDriverDetailsModel.CONTACTNUMBER, drv.getContactNumber());
                if (Objects.nonNull(driver)) {
                    driveDetailsReverseConverter.convert(drv, driver);
                } else {
                    driver = driveDetailsReverseConverter.convert(drv);
                }
                driver.setDealer(retailer);
                modelService.save(driver);
            });

        }
        
        if(businessInfoData.getFormSS()!=null)
        {
        	List<MediaModel> formImages = null;
        	if((retailer.getOnboardingFormsImages() == null) || (retailer.getOnboardingFormsImages().isEmpty()))
            {
        		formImages = new ArrayList<>();
            }
        	else
        	{
        		formImages = new ArrayList<>(retailer.getOnboardingFormsImages());
        	}
        	
        	formImages.add(eydmsMediaReverseConverter.convert(genericMediaUtil.getMultipartFile(businessInfoData.getFormSS().getByteStream(),businessInfoData.getFormSS().getFileName())));
        	retailer.setOnboardingFormsImages(formImages);
        }
        
        modelService.save(retailer);

        return retailer.getUid();
    }

    @Override
    public EYDMSBusinessInfoData getBusinessInfo(String uid) {
        var retailer = getCustomerForUid(uid);
        EYDMSBusinessInfoData infoData = new EYDMSBusinessInfoData();
        infoData.setWarehouseCapacity(retailer.getWarehouseCapacity());
        if (Objects.nonNull(retailer.getBrandWiseSales())) {
            infoData.setBrandWiseSales(brandWiseSaleConverter.convertAll(retailer.getBrandWiseSales()));
        }
        List<ItemModel> vehicleList = eydmsGenericDao.findListItemByTypeCodeAndUidParam(DealerVehicleDetailsModel._TYPECODE, DealerVehicleDetailsModel.DEALER, String.valueOf(retailer.getPk()));
        if (Objects.nonNull(vehicleList)) {
            infoData.setVehicleDetails(vehicleList.stream().map(v -> vehicleDetailsConverter.convert((DealerVehicleDetailsModel) v)).collect(Collectors.toList()));

        }
        List<ItemModel> drivers = eydmsGenericDao.findListItemByTypeCodeAndUidParam(DealerDriverDetailsModel._TYPECODE, DealerDriverDetailsModel.DEALER, String.valueOf(retailer.getPk()));
        if (Objects.nonNull(drivers)) {
            infoData.setDriveDetails(drivers.stream().map(d -> driveDetailsConverter.convert((DealerDriverDetailsModel) d)).collect(Collectors.toList()));

        }
        return infoData;
    }

    private EyDmsCustomerModel getCustomerForUid(String uid) {
        return (EyDmsCustomerModel) getUserService().getUserForUID(uid);
    }

    @Override
    public String addFinancialInformation(EYDMSFinancialInfoData infoData) {
        EyDmsCustomerModel retailer = (EyDmsCustomerModel) getUserService().getUserForUID(infoData.getRetailerId());
        retailer.setPanCard(infoData.getFirmPan());
        retailer.setTanNo(infoData.getFirmTan());
        retailer.setBankAccountNo(infoData.getFirmAccountNo());
        retailer.setIfscCode(infoData.getFirmIFSC());

        retailer.setDdNeftDoc(eydmsMediaReverseConverter.convert(genericMediaUtil.getMultipartFile(infoData.getDdNeftDoc().getByteStream(), infoData.getDdNeftDoc().getFileName())));
        retailer.setBlankChequeDoc(eydmsMediaReverseConverter.convert(genericMediaUtil.getMultipartFile(infoData.getBlankChequeDoc().getByteStream(), infoData.getBlankChequeDoc().getFileName())));
        retailer.setPanDoc(eydmsMediaReverseConverter.convert(genericMediaUtil.getMultipartFile(infoData.getFirmPanDoc().getByteStream(), infoData.getFirmPanDoc().getFileName())));
        retailer.setNominee(nominationConverter.convert(infoData.getNomineeDetails()));
        retailer.setCustomerOnboardingStatus(CustomerOnboardingStatus.PENDING_FOR_APPROVAL);
        
        if(infoData.getFormSS()!=null)
        {
        	List<MediaModel> formImages = null;
        	if((retailer.getOnboardingFormsImages() == null) || (retailer.getOnboardingFormsImages().isEmpty()))
            {
        		formImages = new ArrayList<>();
            }
        	else
        	{
        		formImages = new ArrayList<>(retailer.getOnboardingFormsImages());
        	}
        	
        	formImages.add(eydmsMediaReverseConverter.convert(genericMediaUtil.getMultipartFile(infoData.getFormSS().getByteStream(),infoData.getFormSS().getFileName())));
        	retailer.setOnboardingFormsImages(formImages);
        }
        modelService.save(retailer);
        
        return retailer.getApplicationNo();
    }

    @Override
    public EYDMSFinancialInfoData getFinancialInfo(String uid) {
    	EyDmsCustomerModel retailer = getCustomerForUid(uid);
    	EYDMSFinancialInfoData infoFinancialData = new EYDMSFinancialInfoData();

    	infoFinancialData.setFirmPan(retailer.getPanCard());
    	infoFinancialData.setFirmTan(retailer.getTanNo());
    	infoFinancialData.setFirmAccountNo(retailer.getBankAccountNo());
    	infoFinancialData.setFirmIFSC(retailer.getIfscCode());
    	
    	final EYDMSImageData firmPanDocImageData = new EYDMSImageData();
    	eydmsImagePopulator.populate(retailer.getPanDoc(), firmPanDocImageData);
        if (null != firmPanDocImageData)
            infoFinancialData.setFirmPanDoc(firmPanDocImageData);
        else
        	infoFinancialData.setFirmPanDoc(new EYDMSImageData());
    	EYDMSImageData blankChequeDocImageData = new EYDMSImageData();
    	eydmsImagePopulator.populate(retailer.getBlankChequeDoc(), blankChequeDocImageData);
        if (null != blankChequeDocImageData)
            infoFinancialData.setBlankChequeDoc(blankChequeDocImageData);
        else
        	infoFinancialData.setBlankChequeDoc(new EYDMSImageData());
    	
        EYDMSImageData ddNeftDocImageData = new EYDMSImageData();
    	eydmsImagePopulator.populate(retailer.getDdNeftDoc(), ddNeftDocImageData);
    	if (null != ddNeftDocImageData)
            infoFinancialData.setDdNeftDoc(ddNeftDocImageData);
        else
        	infoFinancialData.setDdNeftDoc(new EYDMSImageData());
    	
    	NominationData nomineeDetails = new NominationData();
    	eydmsNominationPopulator.populate(retailer.getNominee(), nomineeDetails);
    	infoFinancialData.setNomineeDetails(nomineeDetails);
    	
    	return infoFinancialData;
    }
    
    @Override
    public DealerListData getInfluencersListForSO() {
        DealerListData dealerListData = new DealerListData();
        List<EyDmsCustomerModel> eydmsCustomersList = new ArrayList<>();
        final UserModel currentUser = getUserService().getCurrentUser();

        if (currentUser instanceof EyDmsUserModel) {
            final EyDmsUserModel eydmsUser = (EyDmsUserModel) currentUser;
            eydmsCustomersList = getEyDmsCustomerService().getInfluencerListForSO(eydmsUser);
        } else {
            throw new ModelNotFoundException(NOT_EYDMS_USER_MESSAGE);
        }

        List<CustomerData> dealerData = Optional.of(eydmsCustomersList.stream()
                .map(b2BCustomer -> getDealerBasicConverter()
                        .convert(b2BCustomer)).collect(Collectors.toList())).get();
        dealerListData.setDealers(dealerData);
        return dealerListData;
    }

    @Override
    public String setProfilePicture(MultipartFile file) {
        return getEyDmsCustomerService().saveProfilePicture(file);
    }

    /**
     * Create new contact number for user and send to for approval
     *
     * @param contactNumber
     * @return
     */
    @Override
    public boolean isContactInfoExisting(final String contactNumber) {

        return getEyDmsCustomerAccountService().isExistingContactNumber(contactNumber);
    }

    @Override
    public List<AddressData> filterAddressBookData(final List<AddressData> addressData, final String retailerUid) {

        return addressData.stream().filter(addr -> retailerUid!=null && retailerUid.equals(addr.getRetailerUid())).collect(Collectors.toList());
    }

    private void populateProfilePicture(final MediaModel profilePicture, final EyDmsTaggedPartnersData data) {
        final ImageData profileImageData = imageConverter.convert(profilePicture);
        if (null != profileImageData)
            data.setProfilePicture(profileImageData);
    }

    public EyDmsCustomerAccountService getEyDmsCustomerAccountService() {
        return eydmsCustomerAccountService;
    }

    public void setEyDmsCustomerAccountService(EyDmsCustomerAccountService eydmsCustomerAccountService) {
        this.eydmsCustomerAccountService = eydmsCustomerAccountService;
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
    public BaseStoreService getBaseStoreService() {
        return baseStoreService;
    }

    @Override
    public void setBaseStoreService(BaseStoreService baseStoreService) {
        this.baseStoreService = baseStoreService;
    }

    public EyDmsCustomerService getEyDmsCustomerService() {
        return eydmsCustomerService;
    }

    public void setEyDmsCustomerService(EyDmsCustomerService eydmsCustomerService) {
        this.eydmsCustomerService = eydmsCustomerService;
    }

    public Converter<ProspectiveDealerModel, ProsDealerData> getProsDealerListConverter() {
        return prosDealerListConverter;
    }

    public void setProsDealerListConverter(Converter<ProspectiveDealerModel, ProsDealerData> prosDealerListConverter) {
        this.prosDealerListConverter = prosDealerListConverter;
    }

    public ProsDealerService getProsDealerService() {
        return prosDealerService;
    }

    public void setProsDealerService(ProsDealerService prosDealerService) {
        this.prosDealerService = prosDealerService;
    }

    public Converter<ProspectiveDealerModel, ProsDealerData> getProsDealerDetailsConverter() {
        return prosDealerDetailsConverter;
    }

    public void setProsDealerDetailsConverter(Converter<ProspectiveDealerModel, ProsDealerData> prosDealerDetailsConverter) {
        this.prosDealerDetailsConverter = prosDealerDetailsConverter;
    }


    public Converter<B2BCustomerModel, CustomerData> getDealerBasicConverter() {
        return dealerBasicConverter;
    }

    public void setDealerBasicConverter(Converter<B2BCustomerModel, CustomerData> dealerBasicConverter) {
        this.dealerBasicConverter = dealerBasicConverter;
    }


    public Converter<UserModel, CustomerData> getCustomerConverter() {
        return customerConverter;
    }


    public void setCustomerConverter(Converter<UserModel, CustomerData> customerConverter) {
        this.customerConverter = customerConverter;
    }


    public EyDmsCustomerReversePopulator getEyDmsCustomerReversePopulator() {
        return eydmsCustomerReversePopulator;
    }


    public void setEyDmsCustomerReversePopulator(EyDmsCustomerReversePopulator eydmsCustomerReversePopulator) {
        this.eydmsCustomerReversePopulator = eydmsCustomerReversePopulator;
    }


    public String getPasswordEncoding() {
        return passwordEncoding;
    }


    public void setPasswordEncoding(String passwordEncoding) {
        this.passwordEncoding = passwordEncoding;
    }

    public FlexibleSearchService getFlexibleSearchService() {
        return flexibleSearchService;
    }

    public void setFlexibleSearchService(FlexibleSearchService flexibleSearchService) {
        this.flexibleSearchService = flexibleSearchService;
    }


    public RegionService getRegionService() {
        return regionService;
    }


    public void setRegionService(RegionService regionService) {
        this.regionService = regionService;
    }

    @Override
    public CustomerStagingData getCustomerStagingData(String uid){
        EyDmsCustomerModel eydmsCustomerModel = (EyDmsCustomerModel) getUserService().getUserForUID(uid);
        return customerStagingDataConverter.convert(eydmsCustomerModel);
    }

    @Override
    public CustomerDetailedData getCustomerDetailedData(String uid){
        EyDmsCustomerModel eydmsCustomerModel = (EyDmsCustomerModel) getUserService().getUserForUID(uid);
        return customerDetailedDataConverter.convert(eydmsCustomerModel);

    }

    @Override
    public InfluencerDetailedData getInfluencerDetailedData(String uid) {
        var customer = (EyDmsCustomerModel) getUserService().getUserForUID(uid);
        InfluencerDetailedData data = new InfluencerDetailedData();
        data.setPartnerDetails(influncerConverter.convert(customer));
        InfluencerFinanceData financeData = new InfluencerFinanceData();
        influencerFinancialsPopulator.populate(customer, financeData);
        data.setFinancialDetails(financeData);
        return data;
    }


	@Override
	public List<AddressData> filterAddressByLpSource(List<AddressData> filteredAddressList) {
		// TODO Auto-generated method stub
		return getEyDmsCustomerService().filterAddressByLpSource(filteredAddressList);
	}

    @Override
    public CreditBreachedData getCountOfCreditLimitBreachedUser() {

        CreditBreachedData data1 = new CreditBreachedData();
        List<EyDmsCustomerModel> eydmsCustomersList = new ArrayList<>();
        List<CreditBreachDetailsData> dataList = new ArrayList<>();

        final UserModel currentUser = getUserService().getCurrentUser();

        if (currentUser instanceof EyDmsUserModel) {
            final EyDmsUserModel eydmsUser = (EyDmsUserModel) currentUser;
            eydmsCustomersList = getEyDmsCustomerService().getCountOfCreditLimitBreachedUser(eydmsUser);
            for (EyDmsCustomerModel eydmsCustomerModel : eydmsCustomersList) {
                CreditBreachDetailsData data = new CreditBreachDetailsData();
                data.setId(eydmsCustomerModel.getUid());
                data.setName(eydmsCustomerModel.getName());
                /*data.setCreditBreachedAmount(eydmsCustomerModel.getCreditLimit());*/
                dataList.add(data);

            }
            data1.setCount(eydmsCustomersList.size());
            data1.setCustomerList(dataList);

        } else {
            throw new ModelNotFoundException(NOT_EYDMS_USER_MESSAGE);
        }
        /*List<CustomerData> dealerData = Optional.of(eydmsCustomersList.stream()
                .map(b2BCustomer -> getDealerBasicConverter()
                        .convert(b2BCustomer)).collect(Collectors.toList())).get();*/

        return data1;
    }


	@Override
	public String addDealerData(EYDMSDealerData eydmsDealerData) {
		 EyDmsCustomerModel eydmsCustomer = eydmsDealerReverseConverter.convert(eydmsDealerData);
	        if (Objects.nonNull(eydmsCustomer)) {
//	        	 if(eydmsCustomer.getOnboardingPlacedBy()==null)
//	             {
//	             	List<SubAreaMasterModel> subAreas = territoryManagementDao.getTerritoriesForCustomer(eydmsCustomer);
//	     			List<EyDmsUserModel> eydmsUsers = territoryManagementDao.getEyDmsUsersForSubArea(subAreas);
//	     			eydmsCustomer.setOnboardingPlacedBy(eydmsUsers.get(0));
//	     			eydmsCustomer.setOnboardingPartner(eydmsUsers.get(0));
//	     			modelService.save(eydmsCustomer);
//	             }
	            return eydmsCustomer.getUid();
	        }
	        return null;
	}


	@Override
	public Boolean sendOnboardingSmsOtp(String name, String mobileNo) {
		return eydmsCustomerService.sendOnboardingSmsOtp(name, mobileNo);
	}


	@Override
	public Boolean sendLoginSmsOtp(String uid) {
		return eydmsCustomerService.sendLoginSmsOtp(uid);
	}


    @Override
    public String addSalesPromoterData(EYDMSSalesPromoterData eydmsSalesPromoterData) {
        EyDmsCustomerModel eydmsCustomer = eydmsSalesPromoterReverseConverter.convert(eydmsSalesPromoterData);
        if (Objects.nonNull(eydmsCustomer)) {
//	        	 if(eydmsCustomer.getOnboardingPlacedBy()==null)
//	             {
//	             	List<SubAreaMasterModel> subAreas = territoryManagementDao.getTerritoriesForCustomer(eydmsCustomer);
//	     			List<EyDmsUserModel> eydmsUsers = territoryManagementDao.getEyDmsUsersForSubArea(subAreas);
//	     			eydmsCustomer.setOnboardingPlacedBy(eydmsUsers.get(0));
//	     			eydmsCustomer.setOnboardingPartner(eydmsUsers.get(0));
//	     			modelService.save(eydmsCustomer);
//	             }
            return eydmsCustomer.getUid();
        }
        return null;
    }

    @Override
     public void checkMobileNumberValidation(String mobileNo) {
      eydmsCustomerService.checkMobileNumberValidation(mobileNo);
    }
}
