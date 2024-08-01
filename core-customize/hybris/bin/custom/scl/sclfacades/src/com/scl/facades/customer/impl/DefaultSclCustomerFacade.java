package com.scl.facades.customer.impl;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.customer.services.SclCustomerAccountService;
import com.scl.core.customer.services.SclCustomerService;
import com.scl.core.dao.SCLGenericDao;
import com.scl.core.dao.TerritoryManagementDao;
import com.scl.core.enums.CompanyType;
import com.scl.core.enums.CustomerOnboardingStatus;
import com.scl.core.model.*;
import com.scl.core.prosdealer.service.ProsDealerService;
import com.scl.core.region.service.RegionService;
import com.scl.core.services.DJPVisitService;
import com.scl.facades.customer.SclCustomerFacade;
import com.scl.facades.data.*;
import com.scl.facades.data.order.vehicle.DealerDriverDetailsData;
import com.scl.facades.data.order.vehicle.DealerVehicleDetailsData;
import com.scl.facades.populators.SclCustomerReversePopulator;
import com.scl.facades.prosdealer.data.*;
import com.scl.facades.util.GenericMediaUtil;
import com.scl.occ.dto.LoginOTPWsDTO;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2bacceleratorfacades.customer.impl.DefaultB2BCustomerFacade;
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
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
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
import java.util.stream.Collectors;


public class DefaultSclCustomerFacade extends DefaultB2BCustomerFacade implements SclCustomerFacade {

    private SclCustomerAccountService sclCustomerAccountService;
    private UserService userService;
    private BaseStoreService baseStoreService;
    private SclCustomerService sclCustomerService;
    private ProsDealerService prosDealerService;
    private FlexibleSearchService flexibleSearchService;
    private Converter<ProspectiveDealerModel, ProsDealerData> prosDealerListConverter;
    private Converter<ProspectiveDealerModel, ProsDealerData> prosDealerDetailsConverter;
    private Converter<UserModel, CustomerData> customerConverter;
    private Converter<B2BCustomerModel, CustomerData> dealerBasicConverter;
    private SclCustomerReversePopulator sclCustomerReversePopulator;
    private String passwordEncoding = PasswordEncoderConstants.DEFAULT_ENCODING;
    private RegionService regionService;
    @Resource
    private Converter<MediaModel, ImageData> imageConverter;
    
    @Resource
    private Populator<MediaModel, SCLImageData> sclImagePopulator;


    @Resource
    private ModelService modelService;
    @Resource
    private Converter<SCLRetailerData, SclCustomerModel> sclRetailerReverseConverter;
    private static final String NOT_SCL_USER_MESSAGE = "Current user is not an SCL user";

    private static final Logger LOGGER = Logger.getLogger(DefaultSclCustomerFacade.class);
    @Resource
    private Converter<SCLDirectorData, ProsDealerCompanyModel> sclDirectorReverseConverter;
    @Resource
    private Converter<SCLPartnerData, PartnershipModel> sclPartnerReverseConverter;
    @Resource
    private Converter<ProsDealerCompanyModel, SCLDirectorData> sclDirectorConverter;
    @Resource
    private Converter<PartnershipModel, SCLPartnerData> sclPartnerConverter;
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
    private Converter<MultipartFile, MediaModel> sclMediaReverseConverter;
    @Resource
    private Converter<NominationData, NominationModel> nominationConverter;
    @Resource
    private SCLGenericDao sclGenericDao;
    @Resource
    private GenericMediaUtil genericMediaUtil;
    @Resource
    private Converter<SclCustomerModel, SCLRetailerData> sclRetailerConverter;
    @Resource
    private Converter<SclCustomerModel, CustomerStagingData> customerStagingDataConverter;
    @Resource
    private Converter<SclCustomerModel, CustomerDetailedData> customerDetailedDataConverter;

    @Resource
    private Converter<SclCustomerModel, InfluencerData> influncerConverter;
    @Resource
    private Populator<SclCustomerModel,InfluencerFinanceData> influencerFinancialsPopulator;
    
    @Resource
    private Converter<SCLDealerData, SclCustomerModel> sclDealerReverseConverter;

    @Resource
    private Converter<SCLSalesPromoterData, SclCustomerModel> sclSalesPromoterReverseConverter;
    
    @Resource
    Populator<NominationData, NominationModel> sclNominationReversePopulator;
    
    @Resource
    Populator<NominationModel,NominationData> sclNominationPopulator;
    
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
        getSclCustomerAccountService().updateCustomerAverageOrderValue(customerModel, baseStoreModel, statuses);
    }


    @Override
    public ProsDealerListData getProspectiveDealersForCurrentuser() {

        ProsDealerListData prosDealerListData = new ProsDealerListData();
        List<ProspectiveDealerModel> prospectiveDealersList = new ArrayList<>();
        final UserModel currentUser = getUserService().getCurrentUser();
        if (currentUser instanceof SclUserModel) {
            final SclUserModel sclUser = (SclUserModel) currentUser;
            prospectiveDealersList = getSclCustomerService().getProspectiveDealersList(sclUser);
        } else {
            throw new ModelNotFoundException(NOT_SCL_USER_MESSAGE);
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
        SclUserModel districtInCharge;
        try {
            districtInCharge = (SclUserModel) userService.getCurrentUser();
            LOGGER.info("TRY JREBEL");
        } catch (ClassCastException e) {
            LOGGER.error(NOT_SCL_USER_MESSAGE);
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
        List<SclCustomerModel> dealersList = new ArrayList<>();
        final UserModel currentUser = getUserService().getCurrentUser();

        if (currentUser instanceof SclUserModel) {
            final SclUserModel sclUser = (SclUserModel) currentUser;
            dealersList = getSclCustomerService().getDealersList(sclUser);
        } else {
            throw new ModelNotFoundException(NOT_SCL_USER_MESSAGE);
        }
        if(CollectionUtils.isNotEmpty(dealersList)) {
            List<CustomerData> dealerData = Optional.of(dealersList.stream().distinct()
                    .map(b2BCustomer -> getDealerBasicConverter()
                            .convert(b2BCustomer)).sorted(Comparator.comparing(CustomerData::getName)).collect(Collectors.toList())).get();
            dealerListData.setDealers(dealerData);
        }
        return dealerListData;
    }

    @Override
    public void updateUserDetailsOnProfilePage(final CustomerData customerData) throws DuplicateUidException {
        final CustomerModel customer = getCurrentSessionCustomer();
        getSclCustomerReversePopulator().populate(customerData, customer);
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
        final SclUserModel sclUser = (SclUserModel) user;
        final Optional<SclUserModel> reporter = sclUser.getReporter().stream().findFirst();
        SclUserModel supervisor = reporter.orElse(null);
        if (null == supervisor) {
            return null;
        }
        getSclCustomerAccountService().updateUsersContactNumber(user, newContactNumber);
        return getCustomerConverter().convert(supervisor);


    }

    @Override
    public DealerListData getRetailersTaggedToSO() {

        DealerListData dealerListData = new DealerListData();
        List<B2BCustomerModel> dealersList = new ArrayList<>();
        final UserModel currentUser = getUserService().getCurrentUser();

        if (currentUser instanceof SclUserModel) {
            final SclUserModel sclUser = (SclUserModel) currentUser;
            dealersList = getSclCustomerService().getRetailersTaggedtoSO(sclUser);
        } else {
            throw new ModelNotFoundException(NOT_SCL_USER_MESSAGE);
        }

        List<CustomerData> dealerData = Optional.of(dealersList.stream()
                .map(b2BCustomer -> getDealerBasicConverter()
                        .convert(b2BCustomer)).collect(Collectors.toList())).get();
        dealerListData.setDealers(dealerData);
        return dealerListData;

    }

    @Override
    public DealerListData getSclCustomersListForFeedback() {
        DealerListData dealerListData = new DealerListData();
        List<SclCustomerModel> sclCustomersList = new ArrayList<>();
        final UserModel currentUser = getUserService().getCurrentUser();

        if (currentUser instanceof SclUserModel) {
            final SclUserModel sclUser = (SclUserModel) currentUser;
            sclCustomersList = getSclCustomerService().getSclCustomersListForFeedback(sclUser);
        } else {
            throw new ModelNotFoundException(NOT_SCL_USER_MESSAGE);
        }

        List<CustomerData> dealerData = Optional.of(sclCustomersList.stream()
                .map(b2BCustomer -> getDealerBasicConverter()
                        .convert(b2BCustomer)).sorted(Comparator.comparing(CustomerData::getName)).collect(Collectors.toList())).get();
        dealerListData.setDealers(dealerData);
        return dealerListData;
    }

    @Override
    public DealerListData getSclCustomersListForSO() {
        DealerListData dealerListData = new DealerListData();
        List<SclCustomerModel> sclCustomersList = new ArrayList<>();
        final UserModel currentUser = getUserService().getCurrentUser();

        if (currentUser instanceof SclUserModel) {
            final SclUserModel sclUser = (SclUserModel) currentUser;
            sclCustomersList = getSclCustomerService().getSclCustomersListForSO(sclUser);
        } else {
            throw new ModelNotFoundException(NOT_SCL_USER_MESSAGE);
        }
        if(CollectionUtils.isNotEmpty(sclCustomersList)){
        List<CustomerData> dealerData = Optional.of(sclCustomersList.stream().distinct()
                .map(b2BCustomer -> getDealerBasicConverter()
                        .convert(b2BCustomer)).sorted(Comparator.comparing(CustomerData::getName)).collect(Collectors.toList())).get();
        dealerListData.setDealers(dealerData);
        }
        return dealerListData;
    }


    @Override
    public List<SclSiteData> getSitesTaggedtoInfluencers() {
        List<B2BCustomerModel> influencerList = new ArrayList<>();
        final UserModel currentUser = getUserService().getCurrentUser();

        if (currentUser instanceof SclCustomerModel) {
            final SclCustomerModel influencer = (SclCustomerModel) currentUser;
            influencerList = getSclCustomerService().getSitesTaggedtoInfluencers(influencer);
        } else {
            throw new ModelNotFoundException(NOT_SCL_USER_MESSAGE);
        }
        List<SclSiteData> list = new ArrayList<SclSiteData>();
        if (influencerList != null) {
            influencerList.stream().forEach(cust -> {
                SclSiteData data = new SclSiteData();
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
    public List<SclSiteData> getSitesTaggedtoInfluencers(SclCustomerModel influencer){
        List<B2BCustomerModel> influencerList = new ArrayList<>();

        if (influencer instanceof SclCustomerModel) {
            influencerList = getSclCustomerService().getSitesTaggedtoInfluencers(influencer);
        } else {
            throw new ModelNotFoundException(NOT_SCL_USER_MESSAGE);
        }
        List<SclSiteData> list = new ArrayList<SclSiteData>();
        if (influencerList != null) {
            influencerList.stream().forEach(cust -> {
                SclSiteData data = new SclSiteData();
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
    public List<SclTaggedPartnersData> getTaggedPartnersForSite() {
        List<B2BCustomerModel> taggedPartnersList = new ArrayList<>();
        final UserModel currentUser = getUserService().getCurrentUser();

        if (currentUser instanceof SclCustomerModel) {
            final SclCustomerModel site = (SclCustomerModel) currentUser;
            taggedPartnersList = getSclCustomerService().getTaggedPartnersForSite(site);
        } else {
            throw new ModelNotFoundException(NOT_SCL_USER_MESSAGE);
        }
        List<SclTaggedPartnersData> list = new ArrayList<SclTaggedPartnersData>();
        UserGroupModel influencerGroup = getUserService().getUserGroupForUID(SclCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID);
        if (taggedPartnersList != null) {
            taggedPartnersList.stream().forEach(cust -> {
                SclTaggedPartnersData data = new SclTaggedPartnersData();
                data.setCode(null != cust.getUid() ? cust.getUid() : "");
                data.setName(null != cust.getName() ? cust.getName() : "");
                data.setContactNo(null != ((SclCustomerModel) cust).getContactNumber() ? ((SclCustomerModel) cust).getContactNumber() : "");
                if (cust.getGroups().contains(influencerGroup)) {
                    data.setInfluencerCategory(null != ((SclCustomerModel) cust).getInfluencerType() ? ((SclCustomerModel) cust).getInfluencerType().getCode() : "");
                }
                if (((SclCustomerModel) cust).getProfilePicture() != null) {
                    populateProfilePicture(((SclCustomerModel) cust).getProfilePicture(), data);
                }
                data.setOrderHistory(null != ((SclCustomerModel) cust).getOrderHistory() ? ((SclCustomerModel) cust).getOrderHistory() : "");
                list.add(data);
            });
        }
        return list;
    }

    @Override
    public boolean addTaggedPartnersForSite(String uid) throws DuplicateUidException {
        SclCustomerModel taggedPartner = (SclCustomerModel) getUserService().getUserForUID(uid);
        final UserModel currentUser = getUserService().getCurrentUser();
        SclCustomerModel site = modelService.create(SclCustomerModel.class);
        List<B2BCustomerModel> taggedPartnersList = new ArrayList<>();

        if (currentUser instanceof SclCustomerModel) {
            site = (SclCustomerModel) currentUser;
            if (getSclCustomerService().getTaggedPartnersForSite(site) != null && !getSclCustomerService().getTaggedPartnersForSite(site).isEmpty())
                taggedPartnersList = getSclCustomerService().getTaggedPartnersForSite(site);
        } else {
            throw new ModelNotFoundException(NOT_SCL_USER_MESSAGE);
        }

        if (null != taggedPartner) {
            if (taggedPartner.getSclCustomers().contains(site)) {
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
    public String addRetailerdata(SCLRetailerData sclRetailerData) {
    	SclCustomerModel sclCustomer = sclRetailerReverseConverter.convert(sclRetailerData);
        if (Objects.nonNull(sclCustomer)) {

        	
//        	if(sclCustomer.getOnboardingPlacedBy()==null)
//            {
//            	List<SubAreaMasterModel> subAreas = territoryManagementDao.getTerritoriesForCustomer(sclCustomer);
//    			List<SclUserModel> sclUsers = territoryManagementDao.getSclUsersForSubArea(subAreas);
//    			sclCustomer.setOnboardingPlacedBy(sclUsers.get(0));
//    			sclCustomer.setOnboardingPartner(sclUsers.get(0));
//    			modelService.save(sclCustomer);
//            }
        	
            return sclCustomer.getUid();
        }
        return null;
    }

    @Override
    public SCLRetailerData getRetailerData(String uid) {
        SclCustomerModel retailer = (SclCustomerModel) getUserService().getUserForUID(uid);
        return sclRetailerConverter.convert(retailer);
    }

    @Override
    public SCLCompanyDetailsData getCompanyDetails(String uid) {
        SclCustomerModel retailer = (SclCustomerModel) getUserService().getUserForUID(uid);
        SCLCompanyDetailsData data = new SCLCompanyDetailsData();
        if(Objects.nonNull(retailer.getCompanyType())) {
            data.setCompanyType(retailer.getCompanyType().getCode());
            if (retailer.getCompanyType().equals(CompanyType.COMPANY)) {
                if (CollectionUtils.isNotEmpty(retailer.getDirectors())) {
                    data.setDirectors(sclDirectorConverter.convertAll(retailer.getDirectors()));
                }
            } else {
                if (Objects.nonNull(retailer.getPartners())) {
                    data.setPartners(sclPartnerConverter.convertAll(retailer.getPartners()));
                }
            }
        }
        return data;
    }

    @Override
    public String addCompanyDetails(SCLCompanyDetailsData companyDetailsData) {
        SclCustomerModel retailer = (SclCustomerModel) getUserService().getUserForUID(companyDetailsData.getRetailerId());
        retailer.setCompanyType(CompanyType.valueOf(companyDetailsData.getCompanyType()));
        if (Objects.nonNull(companyDetailsData.getDirectors())) {
            List<ProsDealerCompanyModel> directorList = new ArrayList<>();
            if (Objects.nonNull(retailer.getDirectors())) {
                directorList.addAll(retailer.getDirectors());
            }
            directorList.addAll(sclDirectorReverseConverter.convertAll(companyDetailsData.getDirectors()));
            retailer.setDirectors(directorList);
        }
        if (Objects.nonNull(companyDetailsData.getPartners())) {
            List<PartnershipModel> partnerList = new ArrayList<>();
            if (Objects.nonNull(retailer.getPartners())) {
                partnerList.addAll(retailer.getPartners());
            }
            partnerList.addAll(sclPartnerReverseConverter.convertAll(companyDetailsData.getPartners()));
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
        	
        	formImages.add(sclMediaReverseConverter.convert(genericMediaUtil.getMultipartFile(companyDetailsData.getFormSS().getByteStream(),companyDetailsData.getFormSS().getFileName())));
        	retailer.setOnboardingFormsImages(formImages);
        }
        
        modelService.save(retailer);

        return retailer.getUid();
    }

    @Override
    public String addBusinessInformation(SCLBusinessInfoData businessInfoData) {
        SclCustomerModel retailer = (SclCustomerModel) getUserService().getUserForUID(businessInfoData.getRetailerId());
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
                var vehicle = (DealerVehicleDetailsModel) sclGenericDao.findItemByTypeCodeAndUidParam(DealerVehicleDetailsModel._TYPECODE, DealerVehicleDetailsModel.VEHICLENUMBER, v.getVehicleNumber());
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
                DealerDriverDetailsModel driver = (DealerDriverDetailsModel) sclGenericDao.findItemByTypeCodeAndUidParam(DealerDriverDetailsModel._TYPECODE, DealerDriverDetailsModel.CONTACTNUMBER, drv.getContactNumber());
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
        	
        	formImages.add(sclMediaReverseConverter.convert(genericMediaUtil.getMultipartFile(businessInfoData.getFormSS().getByteStream(),businessInfoData.getFormSS().getFileName())));
        	retailer.setOnboardingFormsImages(formImages);
        }
        
        modelService.save(retailer);

        return retailer.getUid();
    }

    @Override
    public SCLBusinessInfoData getBusinessInfo(String uid) {
        var retailer = getCustomerForUid(uid);
        SCLBusinessInfoData infoData = new SCLBusinessInfoData();
        infoData.setWarehouseCapacity(retailer.getWarehouseCapacity());
        if (Objects.nonNull(retailer.getBrandWiseSales())) {
            infoData.setBrandWiseSales(brandWiseSaleConverter.convertAll(retailer.getBrandWiseSales()));
        }
        List<ItemModel> vehicleList = sclGenericDao.findListItemByTypeCodeAndUidParam(DealerVehicleDetailsModel._TYPECODE, DealerVehicleDetailsModel.DEALER, String.valueOf(retailer.getPk()));
        if (Objects.nonNull(vehicleList)) {
            infoData.setVehicleDetails(vehicleList.stream().map(v -> vehicleDetailsConverter.convert((DealerVehicleDetailsModel) v)).collect(Collectors.toList()));

        }
        List<ItemModel> drivers = sclGenericDao.findListItemByTypeCodeAndUidParam(DealerDriverDetailsModel._TYPECODE, DealerDriverDetailsModel.DEALER, String.valueOf(retailer.getPk()));
        if (Objects.nonNull(drivers)) {
            infoData.setDriveDetails(drivers.stream().map(d -> driveDetailsConverter.convert((DealerDriverDetailsModel) d)).collect(Collectors.toList()));

        }
        return infoData;
    }

    private SclCustomerModel getCustomerForUid(String uid) {
        return (SclCustomerModel) getUserService().getUserForUID(uid);
    }

    @Override
    public String addFinancialInformation(SCLFinancialInfoData infoData) {
        SclCustomerModel retailer = (SclCustomerModel) getUserService().getUserForUID(infoData.getRetailerId());
        retailer.setPanCard(infoData.getFirmPan());
        retailer.setTanNo(infoData.getFirmTan());
        retailer.setBankAccountNo(infoData.getFirmAccountNo());
        retailer.setIfscCode(infoData.getFirmIFSC());

        retailer.setDdNeftDoc(sclMediaReverseConverter.convert(genericMediaUtil.getMultipartFile(infoData.getDdNeftDoc().getByteStream(), infoData.getDdNeftDoc().getFileName())));
        retailer.setBlankChequeDoc(sclMediaReverseConverter.convert(genericMediaUtil.getMultipartFile(infoData.getBlankChequeDoc().getByteStream(), infoData.getBlankChequeDoc().getFileName())));
        retailer.setPanDoc(sclMediaReverseConverter.convert(genericMediaUtil.getMultipartFile(infoData.getFirmPanDoc().getByteStream(), infoData.getFirmPanDoc().getFileName())));
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
        	
        	formImages.add(sclMediaReverseConverter.convert(genericMediaUtil.getMultipartFile(infoData.getFormSS().getByteStream(),infoData.getFormSS().getFileName())));
        	retailer.setOnboardingFormsImages(formImages);
        }
        modelService.save(retailer);
        
        return retailer.getApplicationNo();
    }

    @Override
    public SCLFinancialInfoData getFinancialInfo(String uid) {
    	SclCustomerModel retailer = getCustomerForUid(uid);
    	SCLFinancialInfoData infoFinancialData = new SCLFinancialInfoData();

    	infoFinancialData.setFirmPan(retailer.getPanCard());
    	infoFinancialData.setFirmTan(retailer.getTanNo());
    	infoFinancialData.setFirmAccountNo(retailer.getBankAccountNo());
    	infoFinancialData.setFirmIFSC(retailer.getIfscCode());
    	
    	final SCLImageData firmPanDocImageData = new SCLImageData();
    	sclImagePopulator.populate(retailer.getPanDoc(), firmPanDocImageData);
        if (null != firmPanDocImageData)
            infoFinancialData.setFirmPanDoc(firmPanDocImageData);
        else
        	infoFinancialData.setFirmPanDoc(new SCLImageData());
    	SCLImageData blankChequeDocImageData = new SCLImageData();
    	sclImagePopulator.populate(retailer.getBlankChequeDoc(), blankChequeDocImageData);
        if (null != blankChequeDocImageData)
            infoFinancialData.setBlankChequeDoc(blankChequeDocImageData);
        else
        	infoFinancialData.setBlankChequeDoc(new SCLImageData());
    	
        SCLImageData ddNeftDocImageData = new SCLImageData();
    	sclImagePopulator.populate(retailer.getDdNeftDoc(), ddNeftDocImageData);
    	if (null != ddNeftDocImageData)
            infoFinancialData.setDdNeftDoc(ddNeftDocImageData);
        else
        	infoFinancialData.setDdNeftDoc(new SCLImageData());
    	
    	NominationData nomineeDetails = new NominationData();
    	sclNominationPopulator.populate(retailer.getNominee(), nomineeDetails);
    	infoFinancialData.setNomineeDetails(nomineeDetails);
    	
    	return infoFinancialData;
    }
    
    @Override
    public DealerListData getInfluencersListForSO() {
        DealerListData dealerListData = new DealerListData();
        List<SclCustomerModel> sclCustomersList = new ArrayList<>();
        final UserModel currentUser = getUserService().getCurrentUser();

        if (currentUser instanceof SclUserModel) {
            final SclUserModel sclUser = (SclUserModel) currentUser;
            sclCustomersList = getSclCustomerService().getInfluencerListForSO(sclUser);
        } else {
            throw new ModelNotFoundException(NOT_SCL_USER_MESSAGE);
        }

        List<CustomerData> dealerData = Optional.of(sclCustomersList.stream()
                .map(b2BCustomer -> getDealerBasicConverter()
                        .convert(b2BCustomer)).collect(Collectors.toList())).get();
        dealerListData.setDealers(dealerData);
        return dealerListData;
    }

    @Override
    public String setProfilePicture(MultipartFile file) {
        return getSclCustomerService().saveProfilePicture(file);
    }

    /**
     * Create new contact number for user and send to for approval
     *
     * @param contactNumber
     * @return
     */
    @Override
    public boolean isContactInfoExisting(final String contactNumber) {

        return getSclCustomerAccountService().isExistingContactNumber(contactNumber);
    }

    @Override
    public List<AddressData> filterAddressBookData(final List<AddressData> addressData, final String retailerUid) {

        return addressData.stream().filter(addr -> retailerUid!=null && retailerUid.equals(addr.getRetailerUid())).collect(Collectors.toList());
    }

    private void populateProfilePicture(final MediaModel profilePicture, final SclTaggedPartnersData data) {
        final ImageData profileImageData = imageConverter.convert(profilePicture);
        if (null != profileImageData)
            data.setProfilePicture(profileImageData);
    }

    public SclCustomerAccountService getSclCustomerAccountService() {
        return sclCustomerAccountService;
    }

    public void setSclCustomerAccountService(SclCustomerAccountService sclCustomerAccountService) {
        this.sclCustomerAccountService = sclCustomerAccountService;
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

    public SclCustomerService getSclCustomerService() {
        return sclCustomerService;
    }

    public void setSclCustomerService(SclCustomerService sclCustomerService) {
        this.sclCustomerService = sclCustomerService;
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


    public SclCustomerReversePopulator getSclCustomerReversePopulator() {
        return sclCustomerReversePopulator;
    }


    public void setSclCustomerReversePopulator(SclCustomerReversePopulator sclCustomerReversePopulator) {
        this.sclCustomerReversePopulator = sclCustomerReversePopulator;
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
        SclCustomerModel sclCustomerModel = (SclCustomerModel) getUserService().getUserForUID(uid);
        return customerStagingDataConverter.convert(sclCustomerModel);
    }

    @Override
    public CustomerDetailedData getCustomerDetailedData(String uid){
        SclCustomerModel sclCustomerModel = (SclCustomerModel) getUserService().getUserForUID(uid);
        return customerDetailedDataConverter.convert(sclCustomerModel);

    }

    @Override
    public InfluencerDetailedData getInfluencerDetailedData(String uid) {
        var customer = (SclCustomerModel) getUserService().getUserForUID(uid);
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
		return getSclCustomerService().filterAddressByLpSource(filteredAddressList);
	}

    @Override
    public CreditBreachedData getCountOfCreditLimitBreachedUser() {

        CreditBreachedData data1 = new CreditBreachedData();
        List<SclCustomerModel> sclCustomersList = new ArrayList<>();
        List<CreditBreachDetailsData> dataList = new ArrayList<>();

        final UserModel currentUser = getUserService().getCurrentUser();

        if (currentUser instanceof SclUserModel) {
            final SclUserModel sclUser = (SclUserModel) currentUser;
            sclCustomersList = getSclCustomerService().getCountOfCreditLimitBreachedUser(sclUser);
            //new territory changes
            if (CollectionUtils.isNotEmpty(sclCustomersList)){
                List<SclCustomerModel> filterCustomerByDOTerritoryCode = djpVisitService.filterCustomerByDOTerritoryCode(sclCustomersList);
            for (SclCustomerModel sclCustomerModel : filterCustomerByDOTerritoryCode) {
                CreditBreachDetailsData data = new CreditBreachDetailsData();
                data.setId(sclCustomerModel.getUid());
                data.setName(sclCustomerModel.getName());
                /*data.setCreditBreachedAmount(sclCustomerModel.getCreditLimit());*/
                dataList.add(data);

            }
            data1.setCount(sclCustomersList.size());
            data1.setCustomerList(dataList);
        }
        } else {
            throw new ModelNotFoundException(NOT_SCL_USER_MESSAGE);
        }
        /*List<CustomerData> dealerData = Optional.of(sclCustomersList.stream()
                .map(b2BCustomer -> getDealerBasicConverter()
                        .convert(b2BCustomer)).collect(Collectors.toList())).get();*/

        return data1;
    }


	@Override
	public String addDealerData(SCLDealerData sclDealerData) {
		 SclCustomerModel sclCustomer = sclDealerReverseConverter.convert(sclDealerData);
	        if (Objects.nonNull(sclCustomer)) {
//	        	 if(sclCustomer.getOnboardingPlacedBy()==null)
//	             {
//	             	List<SubAreaMasterModel> subAreas = territoryManagementDao.getTerritoriesForCustomer(sclCustomer);
//	     			List<SclUserModel> sclUsers = territoryManagementDao.getSclUsersForSubArea(subAreas);
//	     			sclCustomer.setOnboardingPlacedBy(sclUsers.get(0));
//	     			sclCustomer.setOnboardingPartner(sclUsers.get(0));
//	     			modelService.save(sclCustomer);
//	             }
	            return sclCustomer.getUid();
	        }
	        return null;
	}


	@Override
	public Boolean sendOnboardingSmsOtp(String name, String mobileNo) {
		return sclCustomerService.sendOnboardingSmsOtp(name, mobileNo);
	}


	@Override
	public LoginOTPWsDTO sendLoginSmsOtp(SclCustomerModel customerModel, String partnerCustomerFlag, String pcuid) {
		return sclCustomerService.sendLoginSmsOtp(customerModel,partnerCustomerFlag,pcuid);
	}


    @Override
    public String addSalesPromoterData(SCLSalesPromoterData sclSalesPromoterData) {
        SclCustomerModel sclCustomer = sclSalesPromoterReverseConverter.convert(sclSalesPromoterData);
        if (Objects.nonNull(sclCustomer)) {
//	        	 if(sclCustomer.getOnboardingPlacedBy()==null)
//	             {
//	             	List<SubAreaMasterModel> subAreas = territoryManagementDao.getTerritoriesForCustomer(sclCustomer);
//	     			List<SclUserModel> sclUsers = territoryManagementDao.getSclUsersForSubArea(subAreas);
//	     			sclCustomer.setOnboardingPlacedBy(sclUsers.get(0));
//	     			sclCustomer.setOnboardingPartner(sclUsers.get(0));
//	     			modelService.save(sclCustomer);
//	             }
            return sclCustomer.getUid();
        }
        return null;
    }

    @Override
     public void checkMobileNumberValidation(String mobileNo) {
      sclCustomerService.checkMobileNumberValidation(mobileNo);
    }

    /**
     * @return
     * @throws ConversionException
     */
    @Override
    public CustomerData getCustomerData(UserModel userModel) throws ConversionException {
        return getCustomerConverter().convert(userModel);
    }
}
