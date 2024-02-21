package com.eydms.core.prosdealer.service.impl;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.enums.OnboardingStatus;
import com.eydms.core.enums.StatusOfApplicant;
import com.eydms.core.enums.VehicleType;
import com.eydms.core.event.SendSMSEvent;
import com.eydms.core.model.*;
import com.eydms.core.prosdealer.dao.ProsDealerDao;
import com.eydms.core.prosdealer.service.ProsDealerService;
import com.eydms.facades.prosdealer.data.*;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2b.model.B2BUserGroupModel;
import de.hybris.platform.b2b.services.B2BUnitService;
import de.hybris.platform.catalog.model.CatalogUnawareMediaModel;
import de.hybris.platform.catalog.model.CompanyModel;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.core.model.c2l.*;
import de.hybris.platform.core.model.media.MediaFolderModel;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.jalo.user.UserGroup;
import de.hybris.platform.jalo.user.UserManager;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.event.EventService;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.ClassMismatchException;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.servicelayer.user.daos.UserDao;
import de.hybris.platform.servicelayer.user.daos.UserGroupDao;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

public class DefaultProsDealerService implements ProsDealerService {

    private ProsDealerDao prosDealerDao;

    private ModelService modelService;

    private MediaService mediaService ;

    private FlexibleSearchService flexibleSearchService;

    private UserService userService;

    private B2BUnitService b2bUnitService;

    @Autowired
    ConfigurationService configurationService;

    @Autowired
    UserGroupDao userGroupDao;

    @Autowired
    KeyGenerator customCodeGenerator;

    private EventService eventService;

    private static final Logger LOG = Logger.getLogger(DefaultProsDealerService.class);

    @Override
    public ProspectiveDealerModel getProsDealerByCode(final String prosDealerCode){
        return getProsDealerDao().findProsDealerByCode(prosDealerCode);
    }

    @Override
    public ProspectiveDealerModel getProsDealerByUid(final String uid){

        ProspectiveDealerModel prospectiveDealer;

        try{
             prospectiveDealer= getUserService().getUserForUID(uid,ProspectiveDealerModel.class);
        }
        catch (UnknownIdentifierException | IllegalArgumentException e){
            LOG.error(String.format("No Prospective dealers found with uid %s",uid));
            throw new ModelNotFoundException(e);
        }
        catch (AmbiguousIdentifierException e){
            LOG.error(String.format("Multiple Prospective dealers found with uid %s",uid));
            throw new AmbiguousIdentifierException(e);
        }
        catch (ClassMismatchException e){
            LOG.error(String.format("The uid %s does not belong to prospective dealer",uid));
            throw new AmbiguousIdentifierException(e);
        }

        return prospectiveDealer;
    }

    @Override
    public void uploadDealerDocument(ProspectiveDealerModel prosDealer, String documentType, MultipartFile file) {

        final String uid = prosDealer.getUid();
        if(LOG.isDebugEnabled()){
            LOG.debug(String.format("Uploading dealer document  %s for prospective dealer with uid %s", documentType, uid));
        }
        validateParameterNotNull(file, "file must not be null");

        switch (documentType){
            case EyDmsCoreConstants.DOCUMENT_PAN_CARD:
                CatalogUnawareMediaModel panCard = createMediaFromFile(uid,EyDmsCoreConstants.DOCUMENT_PAN_CARD,file);
                panCard.setAltText(EyDmsCoreConstants.DOCUMENT_PAN_CARD_NAME);
                getModelService().save(panCard);
                prosDealer.setPanCardDoc(panCard);
                break;

            case EyDmsCoreConstants.DOCUMENT_TYPE_BANK_STATEMENT:
                CatalogUnawareMediaModel bankStatement = createMediaFromFile(uid,EyDmsCoreConstants.DOCUMENT_TYPE_BANK_STATEMENT,file);
                bankStatement.setAltText(EyDmsCoreConstants.DOCUMENT_TYPE_BANK_STATEMENT_NAME);
                getModelService().save(bankStatement);
                prosDealer.setBankStatementDoc(bankStatement);
                break;

            case EyDmsCoreConstants.DOCUMENT_TYPE_BLANK_CHEQUE:
                CatalogUnawareMediaModel blankCheque = createMediaFromFile(uid,EyDmsCoreConstants.DOCUMENT_TYPE_BLANK_CHEQUE,file);
                blankCheque.setAltText(EyDmsCoreConstants.DOCUMENT_TYPE_BLANK_CHEQUE_NAME);
                getModelService().save(blankCheque);
                prosDealer.setBlankChequeDoc(blankCheque);
                break;

            case EyDmsCoreConstants.DOCUMENT_TYPE_DD_DETAILS:
                CatalogUnawareMediaModel ddDetails = createMediaFromFile(uid,EyDmsCoreConstants.DOCUMENT_TYPE_DD_DETAILS,file);
                ddDetails.setAltText(EyDmsCoreConstants.DOCUMENT_TYPE_DD_DETAILS_NAME);
                getModelService().save(ddDetails);
                prosDealer.setDdDetailsDoc(ddDetails);
                break;

            case EyDmsCoreConstants.DOCUMENT_TYPE_GODOWN_DETAILS:
                CatalogUnawareMediaModel godownDetails = createMediaFromFile(uid,EyDmsCoreConstants.DOCUMENT_TYPE_GODOWN_DETAILS,file);
                godownDetails.setAltText(EyDmsCoreConstants.DOCUMENT_TYPE_GODOWN_DETAILS_NAME);
                getModelService().save(godownDetails);
                prosDealer.setGodownSpaceDoc(godownDetails);
                break;

            case EyDmsCoreConstants.DOCUMENT_TYPE_GST_DETAILS:
                CatalogUnawareMediaModel gstDetails = createMediaFromFile(uid,EyDmsCoreConstants.DOCUMENT_TYPE_GST_DETAILS,file);
                gstDetails.setAltText(EyDmsCoreConstants.DOCUMENT_TYPE_GST_DETAILS_NAME);
                getModelService().save(gstDetails);
                prosDealer.setGstDetailsDoc(gstDetails);
                break;

            case EyDmsCoreConstants.DOCUMENT_TYPE_LETTER_HEAD_COPY:
                CatalogUnawareMediaModel letterHead = createMediaFromFile(uid,EyDmsCoreConstants.DOCUMENT_TYPE_LETTER_HEAD_COPY,file);
                letterHead.setAltText(EyDmsCoreConstants.DOCUMENT_TYPE_LETTER_HEAD_COPY_NAME);
                getModelService().save(letterHead);
                prosDealer.setLetterHeadCopyDoc(letterHead);
                break;
        }

        getModelService().save(prosDealer);

    }
    private CatalogUnawareMediaModel createMediaFromFile(final String uid, final String documentType,final MultipartFile file )  {

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
    public List<ProspectiveDealerModel> getProsDealerForSOCustomerQueryAlert() {
        return getProsDealerDao().getProsDealerForSOCustomerQueryAlert();
    }

    @Override
    public List<ProspectiveDealerModel> getProsDealerForSHCustomerQueryAlert() {
        return getProsDealerDao().getProsDealerForSHCustomerQueryAlert();
    }

    @Override
    public Boolean saveBasicDetails(BasicProsDealerData basicProsDealerData) {

        final ProspectiveDealerModel prosDealer = modelService.create(ProspectiveDealerModel.class);
        final AddressModel address = modelService.create(AddressModel.class);

           CityModel city = new CityModel();
           city.setIsocode(basicProsDealerData.getCityCode());
           city = flexibleSearchService.getModelByExample(city);

           TalukaModel taluka = new TalukaModel();
           taluka.setIsocode(basicProsDealerData.getTalukaCode());
           taluka = flexibleSearchService.getModelByExample(taluka);

           DistrictModel district = new DistrictModel();
           district.setIsocode(basicProsDealerData.getDistrictCode());
           district = flexibleSearchService.getModelByExample(district);

           StateModel state = new StateModel();
           state.setIsocode(basicProsDealerData.getStateCode());
           state = flexibleSearchService.getModelByExample(state);

           prosDealer.setUid(basicProsDealerData.getMobileNo()); //mobile no used for login

           CompanyModel dummyEyDmsUnit = b2bUnitService.getUnitForUid(EyDmsCoreConstants.EYDMS_PROS_DEALER_DUMMY_UNIT);
           prosDealer.setDefaultB2BUnit((B2BUnitModel) dummyEyDmsUnit);

           String userGroup=EyDmsCoreConstants.EYDMS_PROS_DEALER_DUMMY_USER_GROUP;
           final Set<PrincipalGroupModel> groups = new HashSet<PrincipalGroupModel>(dummyEyDmsUnit.getGroups());
           final B2BUserGroupModel userGroupModel = getUserService().getUserGroupForUID(userGroup, B2BUserGroupModel.class);
           groups.add(userGroupModel);
           prosDealer.setGroups(groups);

           prosDealer.setEmail(basicProsDealerData.getEmail());
           prosDealer.setName(basicProsDealerData.getName());
           address.setOwner(prosDealer);
           address.setLine1(basicProsDealerData.getLine1());
           address.setLine2(basicProsDealerData.getLine2());
          // address.setCity(city);
          // address.setTaluka(taluka);
           //address.setDistricts(district);
         //  address.setState(state);
          // address.setPincode(basicProsDealerData.getPinCode());
           //address.setMobileNo(basicProsDealerData.getMobileNo());
           final List<AddressModel> customerAddresses = new ArrayList<AddressModel>();
           customerAddresses.add(address);
           prosDealer.setAddresses(customerAddresses);
           modelService.save(prosDealer);
        return true;
    }

    @Override
    public Boolean saveApplicantDetails(ApplicantProsDealerData applicantProsDealerData) {
        ProspectiveDealerModel prosDealer= new ProspectiveDealerModel();
        prosDealer.setUid(applicantProsDealerData.getCode());
        prosDealer = flexibleSearchService.getModelByExample(prosDealer);

        if (prosDealer.getStatusOfApplicant() != null && !prosDealer.getStatusOfApplicant().equals(StatusOfApplicant.valueOf(applicantProsDealerData.getStatusOfApplicant())))
        {
            prosDealer.setCompanies(Collections.EMPTY_LIST);
            prosDealer.setPartners(Collections.EMPTY_LIST);
        }
        prosDealer.setStatusOfApplicant(StatusOfApplicant.valueOf(applicantProsDealerData.getStatusOfApplicant()));

        Collection<ProsDealerCompanyModel> listOfCompanies = new ArrayList<ProsDealerCompanyModel>();
        Collection<PartnershipModel> listOfPartners = new ArrayList<PartnershipModel>();

        if (Objects.nonNull(applicantProsDealerData.getCompanies()) && StatusOfApplicant.valueOf(applicantProsDealerData.getStatusOfApplicant()).equals(StatusOfApplicant.COMPANY)) {

            for (ProsDealerCompanyData companyData :
                    applicantProsDealerData.getCompanies()) {
                ProsDealerCompanyModel companyModel;
                if (companyData.getUiStatus().equals("NEW")) {
                    companyModel = modelService.create(ProsDealerCompanyModel.class);
                    setCompanyDetails(companyData, companyModel);
                    companyModel.setProsDealer(prosDealer);
                    listOfCompanies.add(companyModel);
                }
                else if (companyData.getUiStatus().equals("EDIT")) {
                    companyModel = prosDealer.getCompanies().stream().filter(companies -> companies.getCompanyID().equals(companyData.getCompanyID())).findFirst().get();
                    setEditedCompanyDetails(companyData, companyModel);
                    modelService.save(companyModel);
                }
                else if(companyData.getUiStatus().equals("DELETE"))
                {
                    companyModel = prosDealer.getCompanies().stream().filter(companies -> companies.getCompanyID().equals(companyData.getCompanyID())).findFirst().get();
                    modelService.remove(companyModel);
                }
            }
        }
        else if (Objects.nonNull(applicantProsDealerData.getPartnerships()) && StatusOfApplicant.valueOf(applicantProsDealerData.getStatusOfApplicant()).equals(StatusOfApplicant.PARTNERSHIP_FIRM))
        {
            for (PartnershipData partnershipData :
                    applicantProsDealerData.getPartnerships()) {
                PartnershipModel partnershipModel;
                if (partnershipData.getUiStatus().equals("NEW")) {
                    partnershipModel = modelService.create(PartnershipModel.class);
                    setPartnerDetails(partnershipData, partnershipModel);
                    partnershipModel.setProsDealer(prosDealer);
                    listOfPartners.add(partnershipModel);
                }
                else if (partnershipData.getUiStatus().equals("EDIT")) {
                    partnershipModel = prosDealer.getPartners().stream().filter(partners -> partners.getPartnerID().equals(partnershipData.getPartnerID())).findFirst().get();
                    setEditedPartnerDetails(partnershipData, partnershipModel);
                    modelService.save(partnershipModel);
                }
                else if (partnershipData.getUiStatus().equals("DELETE")) {
                    partnershipModel = prosDealer.getPartners().stream().filter(partners -> partners.getPartnerID().equals(partnershipData.getPartnerID())).findFirst().get();
                    modelService.remove(partnershipModel);
                }
            }
        }
        modelService.save(prosDealer);
        modelService.refresh(prosDealer);
        modelService.saveAll(listOfCompanies);
        modelService.saveAll(listOfPartners);
        return true;
    }

    @Override
    public Boolean saveDealerFinancialDetails(FinancialDetailsData financialDetailsData) {
        ProspectiveDealerModel prosDealer= new ProspectiveDealerModel();
        prosDealer.setUid(financialDetailsData.getCode());
        prosDealer = flexibleSearchService.getModelByExample(prosDealer);

        setDealerFinancialDetails(financialDetailsData, prosDealer);

        Collection<NominationModel> listOfNominees = new ArrayList<NominationModel>();

        if(Objects.nonNull(financialDetailsData.getNominees())) {
            for (NominationData nominationData : financialDetailsData.getNominees()) {
                NominationModel nomineeModel;
                if (nominationData.getUiStatus().equals("NEW")) {
                    nomineeModel = modelService.create(NominationModel.class);
                    setNominationDetails(nominationData, nomineeModel);
                    nomineeModel.setProsDealer(prosDealer);
                    listOfNominees.add(nomineeModel);
                }
                else if (nominationData.getUiStatus().equals("EDIT")) {
                    nomineeModel = prosDealer.getNominees().stream().filter(nominees -> nominees.getNomineeID().equals(nominationData.getNomineeID())).findFirst().get();
                    setEditedNominationDetails(nominationData, nomineeModel);
                    modelService.save(nomineeModel);
                }
                else if (nominationData.getUiStatus().equals("DELETE")) {
                    nomineeModel = prosDealer.getNominees().stream().filter(nominees -> nominees.getNomineeID().equals(nominationData.getNomineeID())).findFirst().get();
                    modelService.remove(nomineeModel);
                }
            }
        }

        modelService.save(prosDealer);
        modelService.refresh(prosDealer);
        modelService.saveAll(listOfNominees);
        return true;
    }

    @Override
    public Boolean saveDealerBusinessDetails(DealerBusinessDetailsData dealerBusinessDetailsData) {
        ProspectiveDealerModel prosDealer= new ProspectiveDealerModel();
        prosDealer.setUid(dealerBusinessDetailsData.getCode());
        prosDealer = flexibleSearchService.getModelByExample(prosDealer);

        Collection<BrandWiseSaleModel> listOfBrandWiseSale = new ArrayList<BrandWiseSaleModel>();

        if(Objects.nonNull(dealerBusinessDetailsData.getBrandWiseSale())) {
            for (BrandWiseSaleData brandSaleWiseData:
                    dealerBusinessDetailsData.getBrandWiseSale()) {

                BrandWiseSaleModel brandWiseSaleModel;

                if (brandSaleWiseData.getUiStatus().equals("NEW")) {
                    brandWiseSaleModel = modelService.create(BrandWiseSaleModel.class);
                    getBrandWiseSaleDetails(brandSaleWiseData, brandWiseSaleModel);
                    brandWiseSaleModel.setProsDealer(prosDealer);
                    listOfBrandWiseSale.add(brandWiseSaleModel);
                }
                else if (brandSaleWiseData.getUiStatus().equals("EDIT")) {
                    brandWiseSaleModel = prosDealer.getBrandWiseSale().stream().filter(brandSale -> brandSale.getBrandWiseSaleID().equals(brandSaleWiseData.getBrandWiseSaleID())).findFirst().get();
                    getEditedBrandWiseSaleDetails(brandSaleWiseData, brandWiseSaleModel);
                    modelService.save(brandWiseSaleModel);
                }
                else if (brandSaleWiseData.getUiStatus().equals("DELETE")) {
                    brandWiseSaleModel = prosDealer.getBrandWiseSale().stream().filter(brandSale -> brandSale.getBrandWiseSaleID().equals(brandSaleWiseData.getBrandWiseSaleID())).findFirst().get();
                    modelService.remove(brandWiseSaleModel);
                }
            }
        }

        Collection<StorageModel> listOfStorages= new ArrayList<StorageModel>();

        if(Objects.nonNull(dealerBusinessDetailsData.getStorage())) {

            prosDealer.setWarehouseSpace(dealerBusinessDetailsData.getWarehouseSpace());

            for (StorageAndInfraStructureData storageData:
                    dealerBusinessDetailsData.getStorage())
            {
                StorageModel storageModel;
                if (storageData.getUiStatus().equals("NEW")) {
                    storageModel = modelService.create(StorageModel.class);
                    getStorageDetails(storageData, storageModel);
                    storageModel.setProsDealer(prosDealer);
                    listOfStorages.add(storageModel);
                }
                else if (storageData.getUiStatus().equals("EDIT")) {
                    storageModel = prosDealer.getStorages().stream().filter(storages -> storages.getStorageID().equals(storageData.getStorageID())).findFirst().get();
                    getEditedStorageDetails(storageData, storageModel);
                    modelService.save(storageModel);
                }
                else if (storageData.getUiStatus().equals("DELETE")) {
                    storageModel = prosDealer.getStorages().stream().filter(storages -> storages.getStorageID().equals(storageData.getStorageID())).findFirst().get();
                    modelService.remove(storageModel);
                }
            }
        }

        modelService.save(prosDealer);
        modelService.refresh(prosDealer);
        modelService.saveAll(listOfStorages);
        modelService.saveAll(listOfBrandWiseSale);
        return true;
    }

    @Override
    public List<ProspectiveDealerModel> fetchProsDealerPendingForSOAssignment(final EyDmsUserModel districtInCharge){
//        DistrictModel district = districtInCharge.getAddresses().stream()
//                .map(AddressModel::getDistricts).findFirst().orElse(null);
//        List<ProspectiveDealerModel>  prospectiveDealers = null;
//        if(null!=district){
//            //TODO:: Change Onboarding status criteria to PENDING_FOR_SO_ASSIGNMENT
//            prospectiveDealers =  fetchProsDealerByDistrictAndOnboardingStatus(OnboardingStatus.PENDING_APPROVAL_SH,district);
//        }
        List<ProspectiveDealerModel>  prospectiveDealers = null;
        return prospectiveDealers;
    }

    @Override
    public List<ProspectiveDealerModel> fetchProsDealerByDistrictAndOnboardingStatus(final OnboardingStatus onboardingStatus, final DistrictModel district){
        return getProsDealerDao().findProsDealerByDistrictAndOnboardingStatus(onboardingStatus,district);
	}
    public boolean sendSmsForDealerBasicDetails(String username) {
        ProspectiveDealerModel prosDealer= new ProspectiveDealerModel();
        prosDealer.setUid(username);
        prosDealer = flexibleSearchService.getModelByExample(prosDealer);

        String messageContent= configurationService.getConfiguration().getString("basic.details.form.message");
        messageContent = messageContent.replace("{#var#}", prosDealer.getName());

            final SMSProcessModel process = new SMSProcessModel();
            process.setNumber(prosDealer.getUid());
            process.setTemplateId("DealerBasicFormDetails");
            process.setMessageContent(messageContent);
            final SendSMSEvent event = new SendSMSEvent(process);
            eventService.publishEvent(event);
            return true;
    }

    @Override
    public boolean sendSmsForDealerFinancialDetails(String username) {
        ProspectiveDealerModel prosDealer= new ProspectiveDealerModel();
        prosDealer.setUid(username);
        prosDealer = flexibleSearchService.getModelByExample(prosDealer);

        String messageContent= configurationService.getConfiguration().getString("submission.form.message");
        messageContent = messageContent.replace("{#var#}", prosDealer.getName());

            final SMSProcessModel process = new SMSProcessModel();
            process.setNumber(prosDealer.getUid());
            process.setTemplateId("DealerOnBoardingFormSubmission");
            process.setMessageContent(messageContent);
            final SendSMSEvent event = new SendSMSEvent(process);
            eventService.publishEvent(event);
            return true;
    }

    private void getBrandWiseSaleDetails(BrandWiseSaleData brandSaleWiseData, BrandWiseSaleModel brandWiseSale) {
        BrandModel brand = new BrandModel();
        brand.setIsocode(brandSaleWiseData.getBrandCode());
        brand= flexibleSearchService.getModelByExample(brand);

        brandWiseSale.setBrandWiseSaleID(String.valueOf(customCodeGenerator.generate()));
        brandWiseSale.setBrand(brand);
        brandWiseSale.setSaleInMT(brandSaleWiseData.getSaleInMT());
    }
    private void getEditedBrandWiseSaleDetails(BrandWiseSaleData brandSaleWiseData, BrandWiseSaleModel brandWiseSale) {
        BrandModel brand = new BrandModel();
        brand.setIsocode(brandSaleWiseData.getBrandCode());
        brand=flexibleSearchService.getModelByExample(brand);

        brandWiseSale.setBrand(brand);
        brandWiseSale.setSaleInMT(brandSaleWiseData.getSaleInMT());
    }

    private void getStorageDetails(StorageAndInfraStructureData storageData, StorageModel storage) {
        storage.setStorageID(String.valueOf(customCodeGenerator.generate()));
        storage.setVehicleType(VehicleType.valueOf(storageData.getVehicleType()));
        storage.setNoOfVehicles(storageData.getNoOfVehicles());
    }

    private void getEditedStorageDetails(StorageAndInfraStructureData storageData, StorageModel storage) {
        storage.setVehicleType(VehicleType.valueOf(storageData.getVehicleType()));
        storage.setNoOfVehicles(storageData.getNoOfVehicles());
    }

    private void setPartnerDetails(PartnershipData partnershipData, PartnershipModel partnership) {
        partnership.setPartnerID(String.valueOf(customCodeGenerator.generate()));
        partnership.setName(partnershipData.getNameOfPartner());
        partnership.setRelation(partnershipData.getRelation());
    }

    private void setEditedPartnerDetails(PartnershipData partnershipData, PartnershipModel partnership) {
        partnership.setName(partnershipData.getNameOfPartner());
        partnership.setRelation(partnershipData.getRelation());
    }

    private void setCompanyDetails(ProsDealerCompanyData companyData, ProsDealerCompanyModel company) {
        company.setCompanyID(String.valueOf(customCodeGenerator.generate())); //4003
        company.setNameOfDirector(companyData.getNameOfDirector());
        company.setFatherName(companyData.getFathersName());
        company.setAddress(companyData.getAddress());
        company.setPanNo(companyData.getPanNo());
        company.setBanker(companyData.getBanker());
        company.setDinNo(companyData.getDinNo());
    }

    private void setEditedCompanyDetails(ProsDealerCompanyData companyData, ProsDealerCompanyModel company) {
        company.setNameOfDirector(companyData.getNameOfDirector());
        company.setFatherName(companyData.getFathersName());
        company.setAddress(companyData.getAddress());
        company.setPanNo(companyData.getPanNo());
        company.setBanker(companyData.getBanker());
        company.setDinNo(companyData.getDinNo());
    }

    private void setDealerFinancialDetails(FinancialDetailsData financialDetailsData, ProspectiveDealerModel prosDealer) {
        prosDealer.setPanCard(financialDetailsData.getPanNumber());
        prosDealer.setGstIN(financialDetailsData.getGstIN());
        prosDealer.setStateOfRegistration(financialDetailsData.getStateOfRegistration());
        prosDealer.setBankAccountNo(financialDetailsData.getAccountNo());
        prosDealer.setIfscCode(financialDetailsData.getIfscCode());
    }

    private void setNominationDetails(NominationData nominationData, NominationModel nominee) {
        nominee.setNomineeID(String.valueOf(customCodeGenerator.generate()));
        nominee.setPanCard(nominationData.getPanCard());
        nominee.setAadharCard(nominationData.getAadharCard());
        nominee.setFathersName(nominationData.getFathersName());
        nominee.setRelation(nominationData.getRelation());
        nominee.setName(nominationData.getName());
    }

    private void setEditedNominationDetails(NominationData nominationData, NominationModel nominee) {
        nominee.setPanCard(nominationData.getPanCard());
        nominee.setAadharCard(nominationData.getAadharCard());
        nominee.setFathersName(nominationData.getFathersName());
        nominee.setRelation(nominationData.getRelation());
        nominee.setName(nominationData.getName());
    }

    public ProsDealerDao getProsDealerDao() {
        return prosDealerDao;
    }

    public void setProsDealerDao(ProsDealerDao prosDealerDao) {
        this.prosDealerDao = prosDealerDao;
    }

    public ModelService getModelService() {
        return modelService;
    }

    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
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

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public B2BUnitService getB2bUnitService() {
        return b2bUnitService;
    }

    public void setB2bUnitService(B2BUnitService b2bUnitService) {
        this.b2bUnitService = b2bUnitService;
    }

    public EventService getEventService() {
        return eventService;
    }

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }


}
