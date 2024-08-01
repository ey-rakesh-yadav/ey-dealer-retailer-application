package com.scl.core.services.impl;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.TerritoryManagementDao;
import com.scl.core.event.SclBrandingEvent;
import com.scl.core.model.*;
import com.scl.core.region.dao.GeographicalRegionDao;
import com.scl.core.services.SlctCrmIntegrationService;
import com.scl.core.services.TerritoryMasterService;
import com.scl.facades.data.*;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.media.MediaFolderModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.event.EventService;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import com.scl.core.dao.BrandingDao;
import com.scl.core.enums.*;
import com.scl.core.services.BrandingService;
import com.scl.core.services.TerritoryManagementService;
import de.hybris.platform.catalog.model.CatalogUnawareMediaModel;
import de.hybris.platform.core.HybrisEnumValue;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class BrandingServiceImpl implements BrandingService {
    private static final Logger LOG = Logger.getLogger(BrandingServiceImpl.class);
    private static final String MEDIA_BASE_URL = "media.host.base.url";
    @Resource
    private BrandingDao brandingDao;
    @Autowired
    TerritoryMasterService territoryMasterService;
    @Resource
    GeographicalRegionDao geographicalRegionDao;
    @Resource
    private EnumerationService enumerationService;
    @Resource
    private I18NService i18NService;
    @Resource
    private ModelService modelService;
    @Resource
    MediaService mediaService;
    @Resource
    private KeyGenerator brandCustomCodeGenerator;
    @Autowired
    KeyGenerator brandImageCodeGenerator;
    @Resource
    FlexibleSearchService flexibleSearchService;
    @Resource
    BaseSiteService baseSiteService;
    @Resource
    UserService userService;
    @Resource
    TerritoryManagementDao territoryManagementDao;
    @Resource
    TerritoryManagementService territoryManagementService;

    @Autowired
    EventService eventService;

    @Resource
    BusinessProcessService businessProcessService;

    @Autowired
    ConfigurationService configurationService;

    @Autowired
    Converter<SCLAddressData, AddressModel> sclAddressReverseConverter;

    @Autowired
    SlctCrmIntegrationService slctCrmIntegrationService;

    public GeographicalRegionDao getGeographicalRegionDao() {
        return geographicalRegionDao;
    }

    public void setGeographicalRegionDao(GeographicalRegionDao geographicalRegionDao) {
        this.geographicalRegionDao = geographicalRegionDao;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public TerritoryManagementDao getTerritoryManagementDao() {
        return territoryManagementDao;
    }

    public void setTerritoryManagementDao(TerritoryManagementDao territoryManagementDao) {
        this.territoryManagementDao = territoryManagementDao;
    }

    public BaseSiteService getBaseSiteService() {
        return baseSiteService;
    }

    public void setBaseSiteService(BaseSiteService baseSiteService) {
        this.baseSiteService = baseSiteService;
    }

    public FlexibleSearchService getFlexibleSearchService() {
        return flexibleSearchService;
    }

    public void setFlexibleSearchService(FlexibleSearchService flexibleSearchService) {
        this.flexibleSearchService = flexibleSearchService;
    }

    public MediaService getMediaService() {
        return mediaService;
    }

    public void setMediaService(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    public KeyGenerator getBrandCustomCodeGenerator() {
        return brandCustomCodeGenerator;
    }

    public void setBrandCustomCodeGenerator(KeyGenerator brandCustomCodeGenerator) {
        this.brandCustomCodeGenerator = brandCustomCodeGenerator;
    }

    public BrandingDao getBrandingDao() {
        return brandingDao;
    }

    public void setBrandingDao(BrandingDao brandingDao) {
        this.brandingDao = brandingDao;
    }

    public ModelService getModelService() {
        return modelService;
    }

    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

    public EnumerationService getEnumerationService() {
        return enumerationService;
    }

    public void setEnumerationService(EnumerationService enumerationService) {
        this.enumerationService = enumerationService;
    }

    public I18NService getI18NService() {
        return i18NService;
    }

    public void setI18NService(I18NService i18NService) {
        this.i18NService = i18NService;
    }

    public static final String WAITING_FOR_BRANDING_APPROVAL="WAIT_FOR_BRANDING_APPROVAL";
    @Override
    public List<List<Object>> getCounterDetailsForPointOfSale(String searchKeyWord) {
        List<List<Object>> counterDetailsForPointOfSale = brandingDao.getCounterDetailsForPointOfSale(searchKeyWord);
        return counterDetailsForPointOfSale;
    }

    @Override
    public SclCustomerModel getCounterDetailsForPointOfSaleNew(String searchKeyWord) {
        SclCustomerModel counterDetailsForPointOfSale = brandingDao.getCounterDetailsForPointOfSaleNew(searchKeyWord);
        return counterDetailsForPointOfSale;
    }

    @Override
    public boolean submitBrandingRequisition(BrandingRequestDetailsData brandingRequestDetailsData){
        B2BCustomerModel user = (B2BCustomerModel) userService.getCurrentUser();
        BrandingRequestDetailsModel brandRequestDetailsModel=null;
        SubAreaMasterModel subAreaMaster = null;
        DistrictMasterModel districtMaster = null;
        RegionMasterModel regionMaster = null;
        StateMasterModel stateMaster = null;
        boolean isModify=false;

        if(brandingRequestDetailsData!=null) {
            if (brandingRequestDetailsData.getIsModifyRequest()!=null && brandingRequestDetailsData.getIsModifyRequest() && brandingRequestDetailsData.getRequisitionNumber() != null) {
                brandRequestDetailsModel = brandingDao.getBrandingRequestDetailsByReqNumber(brandingRequestDetailsData.getRequisitionNumber());
                isModify=true;
            } else {
                brandRequestDetailsModel = modelService.create(BrandingRequestDetailsModel.class);
                brandRequestDetailsModel.setRequisitionNumber(String.valueOf(brandCustomCodeGenerator.generate()));
                if ((user.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) || (user.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))) {
                    brandRequestDetailsModel.setRequestStatus(BrandingRequestStatus.REQUEST_RAISED);
                    brandRequestDetailsModel.setDealerRequestStatus(DealerRequestStatus.PENDING);
                } else {
                    brandRequestDetailsModel.setRequestStatus(BrandingRequestStatus.REQUISITION_RAISED);
                    brandRequestDetailsModel.setRequisitionRaisedDate(new Date());
                }
                brandRequestDetailsModel.setRequestRaisedDate(new Date());
                brandRequestDetailsModel.setRequestRaisedBy(user);
            }

            if (brandRequestDetailsModel != null) {
                String reqTitle = getEnumerationService().getEnumerationName(BrandingSiteType.valueOf(brandingRequestDetailsData.getBrandingSiteType())).concat("-").concat(getEnumerationService().getEnumerationName(BrandingType.valueOf(brandingRequestDetailsData.getBrandingType())));
                brandRequestDetailsModel.setRequestTitle(reqTitle);
                brandRequestDetailsModel.setBrandSiteType(BrandingSiteType.valueOf(brandingRequestDetailsData.getBrandingSiteType()));
                if(brandingRequestDetailsData.getPrimaryContactNumber()!=null) {
                    brandRequestDetailsModel.setPrimaryContactNumber(brandingRequestDetailsData.getPrimaryContactNumber());
                }
                brandRequestDetailsModel.setSecondaryContactNumber(brandingRequestDetailsData.getSecondaryContactNumber());
                brandRequestDetailsModel.setQuantity(brandingRequestDetailsData.getQuantity());
                brandRequestDetailsModel.setDetails(brandingRequestDetailsData.getDetails());
                brandRequestDetailsModel.setBrandingType(BrandingType.valueOf(brandingRequestDetailsData.getBrandingType()));
                brandRequestDetailsModel.setLength(brandingRequestDetailsData.getLength());
                brandRequestDetailsModel.setHeight(brandingRequestDetailsData.getHeight());

                if (brandingRequestDetailsData.getBrandingSiteType().equals(BrandingSiteType.OUTDOORS.getCode())) {
                    brandRequestDetailsModel.setSiteName(brandingRequestDetailsData.getCounterName());
                    brandRequestDetailsModel.setCounterName(brandingRequestDetailsData.getCounterName());
                    brandRequestDetailsModel.setSiteAddressLine1(brandingRequestDetailsData.getSiteAddressLine1());
                    brandRequestDetailsModel.setSiteAddressLine2(brandingRequestDetailsData.getSiteAddressLine2());
                    brandRequestDetailsModel.setState(brandingRequestDetailsData.getState());
                    brandRequestDetailsModel.setCity(brandingRequestDetailsData.getCity());
                    brandRequestDetailsModel.setTaluka(brandingRequestDetailsData.getTaluka());
                    brandRequestDetailsModel.setDistrict(brandingRequestDetailsData.getDistrict());
                    brandRequestDetailsModel.setSiteLatitude(brandingRequestDetailsData.getLatitude());
                    brandRequestDetailsModel.setSiteLongitude(brandingRequestDetailsData.getLongitude());
                    brandRequestDetailsModel.setLongitude(brandingRequestDetailsData.getLongitude());
                    brandRequestDetailsModel.setLatitude(brandingRequestDetailsData.getLatitude());

                    //customer subarea add
                    if (!isModify) {
                        subAreaMaster = territoryManagementService.getTerritoryByDistrictAndTaluka(brandingRequestDetailsData.getDistrict(), brandingRequestDetailsData.getTaluka());
                        if (subAreaMaster != null) {
                            updateSubAreaForBrandingReq(brandRequestDetailsModel, subAreaMaster);
                        }
                    }
                } else if (!isModify) {
                    List<SubAreaMasterModel> territoriesForCustomer = territoryManagementService.getTerritoriesForCustomer(brandingRequestDetailsData.getCounterCode());
                    if (territoriesForCustomer != null && !territoriesForCustomer.isEmpty()) {
                        updateSubAreaForBrandingReq(brandRequestDetailsModel, territoriesForCustomer.get(0));
                    }
                }


                if (brandingRequestDetailsData.getBrandingSiteType().equals(BrandingSiteType.POINT_OF_SALE.getCode()) ||
                        brandingRequestDetailsData.getBrandingSiteType().equals(BrandingSiteType.DEALER_COSTSHARING_BRANDING.getCode())) {
                    if (brandingRequestDetailsData.getCounterName() != null) {
                        brandRequestDetailsModel.setCounterName(brandingRequestDetailsData.getCounterName());
                    }
                    if(brandingRequestDetailsData.getCounterCode()!=null) {
                        brandRequestDetailsModel.setCounterCode(brandingRequestDetailsData.getCounterCode());
                    }
                }
            if(brandingRequestDetailsData.getCounterCode()!=null) {
                SclCustomerModel sclCustomer = (SclCustomerModel) getUserService().getUserForUID(brandingRequestDetailsData.getCounterCode());
                if (sclCustomer != null && sclCustomer.getCustomerNo() != null) {
                    brandRequestDetailsModel.setCounterErpCustNo(sclCustomer.getCustomerNo());
                }
            }
          /*  if(brandingRequestDetailsData.getLocation()!=null){
                SCLAddressData data = brandingRequestDetailsData.getLocation();
                if(Objects.nonNull(data)) {
                    AddressModel convert = sclAddressReverseConverter.convert(data);
                    if (Objects.nonNull(convert)) {
                        convert.setOwner(getUserService().getUserForUID(brandingRequestDetailsData.getCounterCode()));
                        convert.setBillingAddress(Boolean.TRUE);
                        convert.setVisibleInAddressBook(Boolean.TRUE);
                        convert.setIsPrimaryAddress(Boolean.TRUE);
                        modelService.save(convert);
                    }
                    brandRequestDetailsModel.setLocation(convert);
                }
            }*/
            if(brandingRequestDetailsData.getStartDate()!=null) {
                brandRequestDetailsModel.setStartDate(getParsedDate(brandingRequestDetailsData.getStartDate()));
            }
            if(brandingRequestDetailsData.getPlanningDateOfCompletion()!=null) {
                brandRequestDetailsModel.setPlanningDateOfCompletion(getParsedDate(brandingRequestDetailsData.getPlanningDateOfCompletion()));
            }
            brandRequestDetailsModel.setObjectiveOfActivity(brandingRequestDetailsData.getObjectiveOfActivity());
            brandRequestDetailsModel.setObjectiveOfTargetPercentage(brandingRequestDetailsData.getObjectiveTargetPercentage());
            brandRequestDetailsModel.setBudgetPlanned(brandingRequestDetailsData.getBudgetPlanned());

            modelService.save(brandRequestDetailsModel);
            if(!isModify) {
                if (submitBrandingUploadImagesBeforeActivity(brandRequestDetailsModel.getRequisitionNumber(), brandingRequestDetailsData.getBeforeBrandingPhotos())) {
                    brandRequestDetailsModel.setImageUploadStatus(true);
                } else {
                    brandRequestDetailsModel.setImageUploadStatus(false);
                }
            }
            brandRequestDetailsModel.setBrand(baseSiteService.getCurrentBaseSite());

                if (brandingRequestDetailsData.getBrandingSiteType().equals(BrandingSiteType.POINT_OF_SALE.getCode()) || brandingRequestDetailsData.getBrandingSiteType().equals(BrandingSiteType.DEALER_COSTSHARING_BRANDING.getCode())) {
                    List<SubAreaMasterModel> territoriesForCustomer = territoryManagementService.getTerritoriesForCustomer(brandingRequestDetailsData.getCounterCode());
                    if (territoriesForCustomer != null && !territoriesForCustomer.isEmpty()) {
                        SubAreaMasterModel subAreaMasterModel = territoriesForCustomer.get(0);
                        if(subAreaMasterModel!=null)
                        {
                            brandRequestDetailsModel.setTaluka(subAreaMasterModel.getTaluka());
                            brandRequestDetailsModel.setDistrict(subAreaMasterModel.getDistrict());
                            if(brandingRequestDetailsData.getCounterCode()!=null) {
                                SclCustomerModel sclCustomer = (SclCustomerModel) getUserService().getUserForUID(brandingRequestDetailsData.getCounterCode());
                                brandRequestDetailsModel.setState(sclCustomer.getState());
                            }
                        }
                        }
                        }
                    }
            modelService.save(brandRequestDetailsModel);

                //trigger for punch branding request to slct
                if(brandRequestDetailsModel.getRequisitionRaisedDate()!=null && !isModify) {
                    SclBrandingProcessModel sclBrandingProcessModel = new SclBrandingProcessModel();
                    sclBrandingProcessModel.setBrandingRequestDetails(brandRequestDetailsModel);
                    eventService.publishEvent(new SclBrandingEvent(sclBrandingProcessModel));
                }
                else if(brandRequestDetailsModel.getRequisitionRaisedDate()!=null && isModify)
                {
                    if(brandRequestDetailsModel.getSclBrandingProcess()!=null && brandRequestDetailsModel.getSclBrandingProcess().iterator().hasNext()) {
                        SclBrandingProcessModel sclBrandingProcess = brandRequestDetailsModel.getSclBrandingProcess().iterator().next();
                        if(sclBrandingProcess!=null) {
                            sclBrandingProcess.setAction("MODIFY");
                            modelService.save(sclBrandingProcess);
                            modelService.refresh(sclBrandingProcess);
                            businessProcessService.triggerEvent(sclBrandingProcess.getCode() + "_" + WAITING_FOR_BRANDING_APPROVAL);
                        }
                    }
                }
            }

        return true;
    }

    private Date getParsedDate(String date) {
        Date startDate = null;
        if(date!=null) {
            try {
                startDate = new SimpleDateFormat("dd/MM/yyyy").parse(date);

            } catch (ParseException e) {
                LOG.error("Error Parsing Date", e);
                throw new IllegalArgumentException(String.format("Please provide valid date %s", date));
            }
        }
        return startDate;
    }


    private void updateSubAreaForBrandingReq(BrandingRequestDetailsModel brandRequestDetailsModel, SubAreaMasterModel subAreaMaster) {
        DistrictMasterModel districtMaster;
        RegionMasterModel regionMaster;
        StateMasterModel stateMaster;
        brandRequestDetailsModel.setSubAreaMaster(subAreaMaster);
        districtMaster = subAreaMaster.getDistrictMaster();
        if (districtMaster != null) {
            brandRequestDetailsModel.setDistrictMaster(districtMaster);
            regionMaster = districtMaster.getRegion();
            if (regionMaster != null) {
                brandRequestDetailsModel.setRegionMaster(regionMaster);
                stateMaster = regionMaster.getState();
                if (stateMaster != null) {
                    brandRequestDetailsModel.setStateMaster(stateMaster);
                }
            }
        }
        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
        if(currentUser instanceof  SclUserModel) {
            List<TerritoryMasterModel> territoryForUser = territoryMasterService.getTerritoryForUser(null);
            if (CollectionUtils.isNotEmpty(territoryForUser)) {
                brandRequestDetailsModel.setTerritoryMaster(territoryForUser.get(0));
            }
        }
    }

    @Override
    public List<BrandingRequestDetailsModel> viewBrandingRequestDetails() {
        return brandingDao.viewBrandingRequestDetails();
    }

    @Override
    public SearchPageData<BrandingRequestDetailsModel> getBrandingRequestDetails(String filter, String startDate, String endDate, List<String> requestStatus, List<String> brandingSiteType,SearchPageData searchPageData) {
        return brandingDao.getBrandingRequestDetails(filter,startDate,endDate,requestStatus,brandingSiteType,searchPageData);
    }

    @Override
    public BrandingRequestDetailsModel getBrandingRequestDetailsByReqNumber(String reqNumber) {
        return brandingDao.getBrandingRequestDetailsByReqNumber(reqNumber);
    }

    @Override
    public BrandingActivityVerficationModel getActivityDetailsForRequest(String requisitionNumber) {
        return brandingDao.getActivityDetailsForRequest(requisitionNumber);
    }

    @Override
    public DropdownListData getEnumTypes(String type) {
        DropdownListData dropdownListData= new DropdownListData();
        List<DropdownData> dropdownDataList= new ArrayList<DropdownData>();
        if(null != type) {
            Class<? extends HybrisEnumValue> enumType=null;
            if(String.valueOf("brandingSiteType").toUpperCase().equals(type.toUpperCase()))
            {
                enumType= BrandingSiteType.class;
            }
            else if ("pointOfSale".toUpperCase().equals(type.toUpperCase()))
            {
                enumType= PointOfSale.class;
            }
            else if ("outdoors".toUpperCase().equals(type.toUpperCase()))
            {
                enumType= Outdoors.class;
            }
            else if ("dealerCostSharingBranding".toUpperCase().equals(type.toUpperCase()))
            {
                enumType= DealerCostSharingBranding.class;
            }
            dropdownDataList=enumerationService.getEnumerationValues(enumType).stream().map(e-> getDropdownData(e)).collect(Collectors.toList());
            dropdownDataList.sort(Comparator.comparing(DropdownData::getName));
        }
        dropdownListData.setDropdown(dropdownDataList);
        return dropdownListData;
    }
    @Override
    public DropdownListData getBrandingSiteTypeDropDownList(String brandingSiteType) {
        DropdownListData data=new DropdownListData();
        if(brandingSiteType.equals(BrandingSiteType.POINT_OF_SALE.getCode())){
            //send the code
            List<PointOfSale> pos = enumerationService.getEnumerationValues(PointOfSale.class);
            List<DropdownData> dataList = pos.stream().map(this::getDropdownData).sorted(Comparator.comparing(DropdownData::getName)).collect(Collectors.toList());
            data.setDropdown(dataList);
        }
        else  if(brandingSiteType.equals(BrandingSiteType.OUTDOORS.getCode())){
            List<Outdoors> out = enumerationService.getEnumerationValues(Outdoors.class);
            List<DropdownData> dataList = out.stream().map(this::getDropdownData).sorted(Comparator.comparing(DropdownData::getName)).collect(Collectors.toList());
            data.setDropdown(dataList);
        }
        else  if(brandingSiteType.equals(BrandingSiteType.DEALER_COSTSHARING_BRANDING.getCode())){
            List<DealerCostSharingBranding> dcs = enumerationService.getEnumerationValues(DealerCostSharingBranding.class);
            List<DropdownData> dataList = dcs.stream().map(this::getDropdownData).sorted(Comparator.comparing(DropdownData::getName)).collect(Collectors.toList());
            data.setDropdown(dataList);
        }
        else
        {
            LOG.error("No Data is provided for this type");
        }
        return data;
    }

    public DropdownData getDropdownData(HybrisEnumValue e)
    {
        DropdownData dropdownData= new DropdownData();
        dropdownData.setCode(e.getCode());
        dropdownData.setName(null != enumerationService.getEnumerationName(e, i18NService.getCurrentLocale()) ? enumerationService.getEnumerationName(e, i18NService.getCurrentLocale()) : e.getCode());
        return dropdownData;
    }

    public boolean submitBrandingUploadImagesBeforeActivity(String requestNo,List<String> uploadPhoto)
    {
        List<MediaModel> catalogUnawareMediaModels=new ArrayList<>();
        String imgName="beforeBrandImage".concat((String) brandImageCodeGenerator.generate());
        BrandingRequestDetailsModel brandingRequestDetails = brandingDao.getBrandingRequestDetailsByReqNumber(requestNo);
        if(uploadPhoto!=null && !uploadPhoto.isEmpty()){
            for (String s : uploadPhoto) {
                MediaModel brandingPicture = getBrandingPicture(s,imgName);
                catalogUnawareMediaModels.add(brandingPicture);
            }
            brandingRequestDetails.setBeforeBrandingPhotos(catalogUnawareMediaModels);
        }
        modelService.save(brandingRequestDetails);
        return true;
    }
    public boolean submitBrandingUploadImagesForAfterActivity(String requestNo,List<String> uploadPhoto)
    {
        List<MediaModel> catalogUnawareMediaModels=new ArrayList<>();
        String imgName="AfterBrandImage".concat((String) brandImageCodeGenerator.generate());
        BrandingRequestDetailsModel brandingRequestDetails = brandingDao.getBrandingRequestDetailsByReqNumber(requestNo);
        if(uploadPhoto!=null && !uploadPhoto.isEmpty()){
            for (String s : uploadPhoto) {
                MediaModel brandingPicture = getBrandingPicture(s,imgName);
                catalogUnawareMediaModels.add(brandingPicture);
            }
            brandingRequestDetails.setAfterBrandingPhotos(catalogUnawareMediaModels);
        }
        modelService.save(brandingRequestDetails);
        return true;
    }
    public boolean submitBrandingUploadInvoiceForActivity(String requestNo,List<String> uploadPhoto)
    {
        List<MediaModel> brandingPic=new ArrayList<>();
        String imgName="invoiceBrandImage".concat((String) brandImageCodeGenerator.generate());
        BrandingRequestDetailsModel brandingRequestDetails = brandingDao.getBrandingRequestDetailsByReqNumber(requestNo);
        if(uploadPhoto!=null && !uploadPhoto.isEmpty()){
            for (String s : uploadPhoto) {
                MediaModel brandingPicture = getBrandingPicture(s,imgName);
                brandingPic.add(brandingPicture);
            }
            brandingRequestDetails.setUploadInvoice(brandingPic);
            modelService.save(brandingRequestDetails);
        }
        return true;
    }
    private MediaModel getBrandingPicture(String data,String imgName) {
        byte[] bytes = Base64.getDecoder().decode(data);
        MultipartFile multipartFile = getMultipartFile(imgName, bytes);
        return createMediaFromFile(imgName, "img", multipartFile);
    }


    public MultipartFile getMultipartFile(String name, byte[] bytes) {
        MultipartFile mfile = null;
        ByteArrayInputStream in = null;
        try {
            in = new ByteArrayInputStream(bytes);
            FileItemFactory factory = new DiskFileItemFactory(16, null);
            //FileItem fileItem = factory.createItem("mainFile", "jpeg", false, name);
            FileItem fileItem = factory.createItem(name, "jpeg", false, name);
            IOUtils.copy(new ByteArrayInputStream(bytes), fileItem.getOutputStream());
            mfile = new CommonsMultipartFile(fileItem);
            in.close();
        }catch (IOException e){
            LOG.error("unexpected error for getting multipart file" + e.getMessage());
        }
        return mfile;
    }

    private CatalogUnawareMediaModel createMediaFromFile(final String uid, final String documentType, final MultipartFile file) {

        Long currentTimeInMillis = System.currentTimeMillis();
        final String mediaCode = documentType.concat(SclCoreConstants.UNDERSCORE_CHARACTER).concat(uid).concat(SclCoreConstants.UNDERSCORE_CHARACTER).concat(String.valueOf(currentTimeInMillis));

        final MediaFolderModel imageMediaFolder = mediaService.getFolder(SclCoreConstants.IMAGE_MEDIA_FOLDER_NAME);
        CatalogUnawareMediaModel documentMedia = null;

        try {
            documentMedia = (CatalogUnawareMediaModel) mediaService.getMedia(mediaCode);
        } catch (AmbiguousIdentifierException ex) {
            LOG.error("More than one media found with code : " + mediaCode);
            LOG.error("Removing duplicate media : " + mediaCode);
            CatalogUnawareMediaModel duplicateMedia = new CatalogUnawareMediaModel();
            duplicateMedia.setCode(mediaCode);
            duplicateMedia.setRealFileName(uid);
            List<CatalogUnawareMediaModel> duplicateMedias = flexibleSearchService.getModelsByExample(duplicateMedia);
            modelService.removeAll(duplicateMedias);
        } catch (UnknownIdentifierException uie) {
            if (LOG.isDebugEnabled()) {
                LOG.error("No Media found with code : " + mediaCode);
            }
        } finally {
            if (null == documentMedia) {
                documentMedia = modelService.create(CatalogUnawareMediaModel.class);
                documentMedia.setCode(mediaCode);
                documentMedia.setRealFileName(file.getName());
            }
        }
        documentMedia.setFolder(imageMediaFolder);
        documentMedia.setMime(file.getContentType());
        documentMedia.setRealFileName(file.getName());
        modelService.save(documentMedia);
        try {
            mediaService.setStreamForMedia(documentMedia, file.getInputStream());
        } catch (IOException ioe) {
            LOG.error("IO Exception occured while creating: " + documentType + " ,for dealer with uid: " + uid);
        }

        return (CatalogUnawareMediaModel) mediaService.getMedia(mediaCode);
    }

    @Override
    public boolean submitActivityVerificationDetailsForRequest(BrandingRequestDetailsData data) {
        if (data != null) {
            if (data.getRequisitionNumber() != null) {
                BrandingRequestDetailsModel detailsModel = brandingDao.getBrandingRequestDetailsByReqNumber(data.getRequisitionNumber());
                if (detailsModel.getBrandComments() == null) {
                    detailsModel.setBrandComments(data.getComments());
                    detailsModel.setExperience(data.getExperience());
                    detailsModel.setResponsiveness(data.getResponsiveness());
                    detailsModel.setQualityOfWork(data.getQualityOfWork());
                    detailsModel.setCompletionTime(data.getCompletionTime());
                    detailsModel.setFeedback(data.getFeedBack());
                    if (data.getPhotoAfterBranding() != null && !data.getPhotoAfterBranding().isEmpty()) {
                        detailsModel.setPhotoAfterBranding(submitBrandingUploadImagesForAfterActivity(detailsModel.getRequisitionNumber(), data.getPhotoAfterBranding()));
                    }
                }
                try {
                    if(data.getDateOfCompletion()!=null) {
                        detailsModel.setDateOfCompletion(getParsedDate(data.getDateOfCompletion()));
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }

                if (data.getLatitude() != null) {
                    detailsModel.setLatitude(data.getLatitude());
                }
                if (data.getLongitude() != null) {
                    detailsModel.setLongitude(data.getLongitude());
                }
                if (data.getLatitude() != null && data.getLongitude() != null) {
                    detailsModel.setPinLocationVerifiedStatus(true);
                } else {
                    detailsModel.setPinLocationVerifiedStatus(false);
                }
                if (data.getUploadInvoice() != null && !data.getUploadInvoice().isEmpty()) {
                    if (submitBrandingUploadInvoiceForActivity(detailsModel.getRequisitionNumber(), data.getUploadInvoice())) {
                        detailsModel.setImageUploadStatus(true);
                    } else {
                        detailsModel.setImageUploadStatus(false);
                    }
                }

                if (detailsModel.getUploadInvoice() != null && !detailsModel.getUploadInvoice().isEmpty()) {
                    detailsModel.setInvoiceUploadedDate(new Date());
                 //   detailsModel.setRequestStatus(BrandingRequestStatus.INVOICE_UPLOAD);
                    detailsModel.setInvoiceUploadedBy((B2BCustomerModel) userService.getCurrentUser());
                }
                if (data.getInvoiceAmount() != null) {
                    detailsModel.setInvoiceAmount(data.getInvoiceAmount());
                }

                if (data.getDateOfCompletion()!=null) {
                    detailsModel.setActivityVerificationDate(new Date());
                    detailsModel.setRequestStatus(BrandingRequestStatus.ACTIVITY_VERIFIED);
                }
                modelService.save(detailsModel);
                //trigger for punch branding request feedback to slct
                if(detailsModel.getInvoiceUploadedDate()!=null)
                {
                    if(detailsModel.getSclBrandingProcess()!=null && detailsModel.getSclBrandingProcess().iterator().hasNext()) {
                        SclBrandingProcessModel sclBrandingProcess = detailsModel.getSclBrandingProcess().iterator().next();
                        if(sclBrandingProcess!=null) {
                            sclBrandingProcess.setAction("FEEDBACK");
                            modelService.save(sclBrandingProcess);
                            modelService.refresh(sclBrandingProcess);
                            businessProcessService.triggerEvent(sclBrandingProcess.getCode() + "_" + WAITING_FOR_BRANDING_APPROVAL);
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public DropdownListData findAllState() {
        DropdownListData data=new DropdownListData();
        List<String> allState = geographicalRegionDao.findAllState();
        List<DropdownData> dataList = allState.stream().map(this::getDropdownData).collect(Collectors.toList());
        data.setDropdown(dataList);
        return data;
    }

    @Override
    public DropdownListData findAllDistrict(String state) {
        DropdownListData data=new DropdownListData();
        List<String> allDistrict = geographicalRegionDao.findAllDistrict(state);
        List<DropdownData> dataList = allDistrict.stream().map(this::getDropdownData).collect(Collectors.toList());
        data.setDropdown(dataList);
        return data;
    }

    @Override
    public DropdownListData findAllTaluka(String state, String district) {
        DropdownListData data=new DropdownListData();
        List<String> allTaluka = geographicalRegionDao.findAllTaluka(state, district);
        List<DropdownData> dataList = allTaluka.stream().map(this::getDropdownData).collect(Collectors.toList());
        data.setDropdown(dataList);
        return data;
    }

    @Override
    public DropdownListData findAllErpCity(String state, String district, String taluka) {
        DropdownListData data=new DropdownListData();
        List<String> allErpCity = geographicalRegionDao.findAllErpCity(state, district, taluka);
        List<DropdownData> dataList = allErpCity.stream().map(this::getDropdownData).collect(Collectors.toList());
        data.setDropdown(dataList);
        return data;
    }

    public DropdownData getDropdownData(String e)
    {
        DropdownData dropdownData= new DropdownData();
        dropdownData.setCode(e);
        dropdownData.setName(e);
        return dropdownData;
    }

    @Override
    public BrandingTrackingStatusModel getTrackingStatusDetailsForRequest(String requisitionNumber, String requestStatus) {
        return brandingDao.getTrackingStatusDetailsForRequest(requisitionNumber,requestStatus);
    }

    @Override
    public List<BrandingTrackStatusStageGateModel> getBrandingTrackHistoryDetails(String requestNumber) {
        List<BrandingTrackStatusStageGateModel> brandingTrackHistoryDetails = brandingDao.getBrandingTrackHistoryDetails(requestNumber);
        return brandingTrackHistoryDetails;
    }

    @Override
    public boolean updateBrandingRequestStatus(String requisitionNo, String status, String comments) {
    	B2BCustomerModel user = (B2BCustomerModel) userService.getCurrentUser();
    	if(user instanceof SclUserModel) {
    		BrandingRequestDetailsModel brandingRequestDetails = brandingDao.getBrandingRequestDetailsByReqNumber(requisitionNo);
    		try{
    			if(brandingRequestDetails!=null) {
    				if(brandingRequestDetails.getLbtApprovedDate() != null)
    				{
    					throw new UnknownIdentifierException("This Branding Request can not modified at this stage");
    				}
    				if (status.equalsIgnoreCase("APPROVE")) {
    					brandingRequestDetails.setRequestApprovedBy(user);
    					brandingRequestDetails.setRequestApprovedDate(new Date());
    					brandingRequestDetails.setApproveComment(comments);
    					brandingRequestDetails.setRequestStatus(BrandingRequestStatus.REQUISITION_RAISED);
    					brandingRequestDetails.setRequisitionRaisedDate(new Date());
    					modelService.save(brandingRequestDetails);

    					SclBrandingProcessModel sclBrandingProcessModel = new SclBrandingProcessModel();
    					sclBrandingProcessModel.setBrandingRequestDetails(brandingRequestDetails);
    					eventService.publishEvent(new SclBrandingEvent(sclBrandingProcessModel));

    				} else if (status.equalsIgnoreCase("REJECT")) {
    					brandingRequestDetails.setRequestRejectedBy(user);
    					brandingRequestDetails.setRequestRejectedDate(new Date());
    					brandingRequestDetails.setRejectComment(comments);
    					brandingRequestDetails.setRequestStatus(BrandingRequestStatus.REQUEST_REJECTED);
    					modelService.save(brandingRequestDetails);
    				}
    				else if (status.equalsIgnoreCase("CANCEL")) {
    					brandingRequestDetails.setRequestCancelledBy(user);
    					brandingRequestDetails.setRequestCancelledDate(new Date());
    					brandingRequestDetails.setCancelComment(comments);
    					brandingRequestDetails.setRequestStatus(BrandingRequestStatus.REQUISITION_CANCELLED);
    					modelService.save(brandingRequestDetails);

    					if(brandingRequestDetails.getSclBrandingProcess()!=null && brandingRequestDetails.getSclBrandingProcess().iterator().hasNext()) {
    						SclBrandingProcessModel sclBrandingProcess = brandingRequestDetails.getSclBrandingProcess().iterator().next();
    						if(sclBrandingProcess!=null) {
    							sclBrandingProcess.setAction("CANCEL");
    							modelService.save(sclBrandingProcess);
    							modelService.refresh(sclBrandingProcess);
    							businessProcessService.triggerEvent(sclBrandingProcess.getCode() + "_" + WAITING_FOR_BRANDING_APPROVAL);
    						}
    					}
    				}

    			}
    			else
    			{
    				throw new UnknownIdentifierException("This Branding Requisition can not be null");
    			}
    			return true;
    		}
    		catch (ModelSavingException e)
    		{
    			LOG.error("Error occurred while updating status for branding request requisition::"+brandingRequestDetails.getRequisitionNumber()+"\n");
    			LOG.error("Exception is: "+e.getMessage());
    			return false;
    		}
    	}
    	else {
    		throw new UnknownIdentifierException("Access not allowed");
    	}
    }

    @Override
    public BrandingRequestData submitBrandingRequisitionSlct(BrandingRequestDetailsData brandingRequestDetailsData) {
        B2BCustomerModel user = (B2BCustomerModel) userService.getCurrentUser();
        BrandingRequestDetailsModel brandRequestDetailsModel=null;
        SubAreaMasterModel subAreaMaster = null;
        DistrictMasterModel districtMaster = null;
        RegionMasterModel regionMaster = null;
        StateMasterModel stateMaster = null;
        boolean isModify=false;

        Date reqRaisedDate = null;
        if(brandingRequestDetailsData!=null) {
            if (brandingRequestDetailsData.getIsModifyRequest()!=null && brandingRequestDetailsData.getIsModifyRequest() && brandingRequestDetailsData.getRequisitionNumber() != null) {
                brandRequestDetailsModel = brandingDao.getBrandingRequestDetailsByReqNumber(brandingRequestDetailsData.getRequisitionNumber());
                isModify=true;
            } else {
                brandRequestDetailsModel = modelService.create(BrandingRequestDetailsModel.class);
                brandRequestDetailsModel.setRequisitionNumber(String.valueOf(brandCustomCodeGenerator.generate()));
                if ((user.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) || (user.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))) {
                    brandRequestDetailsModel.setRequestStatus(BrandingRequestStatus.REQUEST_RAISED);
                    brandRequestDetailsModel.setDealerRequestStatus(DealerRequestStatus.PENDING);
                } else {
                    brandRequestDetailsModel.setRequestStatus(BrandingRequestStatus.REQUISITION_RAISED);
                    brandRequestDetailsModel.setRequisitionRaisedDate(new Date());
                }
                brandRequestDetailsModel.setRequestRaisedDate(new Date());
                brandRequestDetailsModel.setRequestRaisedBy(user);
            }

            if (brandRequestDetailsModel != null) {

                if(StringUtils.isNotBlank(brandingRequestDetailsData.getSlctReqNo()))
                {
                    brandRequestDetailsModel.setSlctReqNo(brandingRequestDetailsData.getSlctReqNo());
                    if(brandingRequestDetailsData.getBrand()!=null) {
                        BaseSiteModel baseSite = baseSiteService.getBaseSiteForUID(brandingRequestDetailsData.getBrand());
                        brandRequestDetailsModel.setBrand(baseSite);
                    }
                    brandRequestDetailsModel.setReqRaisedRole(brandingRequestDetailsData.getReqRaisedRole());
                    brandRequestDetailsModel.setRequestStatus(BrandingRequestStatus.NSH_APPROVED);
                    try {
                        reqRaisedDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(brandingRequestDetailsData.getRequestRaisedDate());
                        brandRequestDetailsModel.setRequisitionRaisedDate(reqRaisedDate);
                        brandRequestDetailsModel.setRequestRaisedDate(reqRaisedDate);
                    }
                    catch (ParseException e)
                    {
                        LOG.error("not able to parse the date :" + reqRaisedDate);
                    }
                }

                String reqTitle = getEnumerationService().getEnumerationName(BrandingSiteType.valueOf(brandingRequestDetailsData.getBrandingSiteType())).concat("-").concat(getEnumerationService().getEnumerationName(BrandingType.valueOf(brandingRequestDetailsData.getBrandingType())));
                brandRequestDetailsModel.setRequestTitle(reqTitle);
                brandRequestDetailsModel.setBrandSiteType(BrandingSiteType.valueOf(brandingRequestDetailsData.getBrandingSiteType()));
                if(brandingRequestDetailsData.getPrimaryContactNumber()!=null) {
                    brandRequestDetailsModel.setPrimaryContactNumber(brandingRequestDetailsData.getPrimaryContactNumber());
                }
                brandRequestDetailsModel.setSecondaryContactNumber(brandingRequestDetailsData.getSecondaryContactNumber());
                brandRequestDetailsModel.setQuantity(brandingRequestDetailsData.getQuantity());
                brandRequestDetailsModel.setDetails(brandingRequestDetailsData.getDetails());
                brandRequestDetailsModel.setBrandingType(BrandingType.valueOf(brandingRequestDetailsData.getBrandingType()));
                if(brandingRequestDetailsData.getBrandingType()!=null && brandingRequestDetailsData.getBrandingType().equals("OTHERS"))
                {
                    brandRequestDetailsModel.setOtherBrandingType(brandingRequestDetailsData.getBrandingSiteType());
                }
                brandRequestDetailsModel.setLength(brandingRequestDetailsData.getLength());
                brandRequestDetailsModel.setHeight(brandingRequestDetailsData.getHeight());

                if (brandingRequestDetailsData.getBrandingSiteType()!=null && brandingRequestDetailsData.getBrandingSiteType().equals(BrandingSiteType.OUTDOORS.getCode())) {
                    brandRequestDetailsModel.setSiteName(brandingRequestDetailsData.getCounterName());
                    brandRequestDetailsModel.setCounterName(brandingRequestDetailsData.getCounterName());
                    brandRequestDetailsModel.setSiteAddressLine1(brandingRequestDetailsData.getSiteAddressLine1());
                    brandRequestDetailsModel.setSiteAddressLine2(brandingRequestDetailsData.getSiteAddressLine2());
                    brandRequestDetailsModel.setState(brandingRequestDetailsData.getState());
                    brandRequestDetailsModel.setCity(brandingRequestDetailsData.getCity());
                    brandRequestDetailsModel.setTaluka(brandingRequestDetailsData.getTaluka());
                    brandRequestDetailsModel.setDistrict(brandingRequestDetailsData.getDistrict());
                    brandRequestDetailsModel.setSiteLatitude(brandingRequestDetailsData.getLatitude());
                    brandRequestDetailsModel.setSiteLongitude(brandingRequestDetailsData.getLongitude());
                    brandRequestDetailsModel.setLongitude(brandingRequestDetailsData.getLongitude());
                    brandRequestDetailsModel.setLatitude(brandingRequestDetailsData.getLatitude());

                    //customer subarea add
                    if (!isModify) {
                        if(brandingRequestDetailsData.getTaluka()!=null && brandingRequestDetailsData.getDistrict()!=null)
                        subAreaMaster = territoryManagementService.getTerritoryByDistrictAndTaluka(brandingRequestDetailsData.getDistrict(), brandingRequestDetailsData.getTaluka());
                        if (subAreaMaster != null) {
                            updateSubAreaForBrandingReq(brandRequestDetailsModel, subAreaMaster);
                        }
                    }
                } else if (!isModify) {
                    List<SubAreaMasterModel> territoriesForCustomer = territoryManagementService.getTerritoriesForCustomer(brandingRequestDetailsData.getCounterCode());
                    if (territoriesForCustomer != null && !territoriesForCustomer.isEmpty()) {
                        updateSubAreaForBrandingReq(brandRequestDetailsModel, territoriesForCustomer.get(0));
                    }
                }


                if (brandingRequestDetailsData.getBrandingSiteType().equals(BrandingSiteType.POINT_OF_SALE.getCode()) ||
                        brandingRequestDetailsData.getBrandingSiteType().equals(BrandingSiteType.DEALER_COSTSHARING_BRANDING.getCode())) {

                    if(brandingRequestDetailsData.getCounterCode()!=null && brandingRequestDetailsData.getSlctReqNo()!=null) {
                        SclCustomerModel sclCustomer=slctCrmIntegrationService.findCustomerByCustomerNo(brandingRequestDetailsData.getCounterCode());
                        if(sclCustomer!=null && sclCustomer.getUid()!=null) {
                            brandingRequestDetailsData.setCounterCode(sclCustomer.getUid());
                            brandingRequestDetailsData.setCounterName(sclCustomer.getName());
                        }
                    }

                    if (brandingRequestDetailsData.getCounterName() != null) {
                        brandRequestDetailsModel.setCounterName(brandingRequestDetailsData.getCounterName());
                    }
                    if(brandingRequestDetailsData.getCounterCode()!=null) {
                        brandRequestDetailsModel.setCounterCode(brandingRequestDetailsData.getCounterCode());
                    }
                }

                if(brandingRequestDetailsData.getCounterCode()!=null) {
                    SclCustomerModel sclCustomer =slctCrmIntegrationService.findCustomerByCustomerNo(brandingRequestDetailsData.getCounterCode());
                    if (sclCustomer != null && sclCustomer.getCustomerNo() != null) {
                        brandRequestDetailsModel.setCounterErpCustNo(sclCustomer.getCustomerNo());
                    }
                }
              /*  if(brandingRequestDetailsData.getLocation()!=null){
                    SCLAddressData data = brandingRequestDetailsData.getLocation();
                    if(Objects.nonNull(data)) {
                        AddressModel convert = sclAddressReverseConverter.convert(data);
                        if (Objects.nonNull(convert)) {
                            convert.setOwner(getUserService().getUserForUID(brandingRequestDetailsData.getCounterCode()));
                            convert.setBillingAddress(Boolean.TRUE);
                            convert.setVisibleInAddressBook(Boolean.TRUE);
                            convert.setIsPrimaryAddress(Boolean.TRUE);
                            modelService.save(convert);
                        }
                        brandRequestDetailsModel.setLocation(convert);
                    }
                }*/
                if(brandingRequestDetailsData.getStartDate()!=null) {
                    brandRequestDetailsModel.setStartDate(getParsedDate(brandingRequestDetailsData.getStartDate()));
                }
                if(brandingRequestDetailsData.getPlanningDateOfCompletion()!=null) {
                    brandRequestDetailsModel.setPlanningDateOfCompletion(getParsedDate(brandingRequestDetailsData.getPlanningDateOfCompletion()));
                }
                brandRequestDetailsModel.setObjectiveOfActivity(brandingRequestDetailsData.getObjectiveOfActivity());
                brandRequestDetailsModel.setObjectiveOfTargetPercentage(brandingRequestDetailsData.getObjectiveTargetPercentage());
                brandRequestDetailsModel.setBudgetPlanned(brandingRequestDetailsData.getBudgetPlanned());

                modelService.save(brandRequestDetailsModel);
                if(!isModify) {
                    if (submitBrandingUploadImagesBeforeActivity(brandRequestDetailsModel.getRequisitionNumber(), brandingRequestDetailsData.getBeforeBrandingPhotos())) {
                        brandRequestDetailsModel.setImageUploadStatus(true);
                    } else {
                        brandRequestDetailsModel.setImageUploadStatus(false);
                    }
                }

                if (brandingRequestDetailsData.getBrandingSiteType().equals(BrandingSiteType.POINT_OF_SALE.getCode()) || brandingRequestDetailsData.getBrandingSiteType().equals(BrandingSiteType.DEALER_COSTSHARING_BRANDING.getCode())) {
                    List<SubAreaMasterModel> territoriesForCustomer = territoryManagementService.getTerritoriesForCustomer(brandingRequestDetailsData.getCounterCode());
                    if (territoriesForCustomer != null && !territoriesForCustomer.isEmpty()) {
                        SubAreaMasterModel subAreaMasterModel = territoriesForCustomer.get(0);
                        if(subAreaMasterModel!=null)
                        {
                            brandRequestDetailsModel.setTaluka(subAreaMasterModel.getTaluka());
                            brandRequestDetailsModel.setDistrict(subAreaMasterModel.getDistrict());
                            if(brandingRequestDetailsData.getCounterCode()!=null) {
                                SclCustomerModel sclCustomer = (SclCustomerModel) getUserService().getUserForUID(brandingRequestDetailsData.getCounterCode());
                                brandRequestDetailsModel.setState(sclCustomer.getState());
                            }
                        }
                    }
                }
            }
            brandRequestDetailsModel.setVendorName(brandingRequestDetailsData.getVendorName());
            brandRequestDetailsModel.setVendorDetails(brandingRequestDetailsData.getVendorDetails());
            modelService.save(brandRequestDetailsModel);

            //trigger for punch branding request to slct
           // if(StringUtils.isBlank(brandingRequestDetailsData.getSlctReqNo()))
            //{
                if (brandRequestDetailsModel.getRequisitionRaisedDate() != null && !isModify) {
                    SclBrandingProcessModel sclBrandingProcessModel = new SclBrandingProcessModel();
                    sclBrandingProcessModel.setBrandingRequestDetails(brandRequestDetailsModel);
                    eventService.publishEvent(new SclBrandingEvent(sclBrandingProcessModel));
                } else if (brandRequestDetailsModel.getRequisitionRaisedDate() != null && isModify) {
                    if (brandRequestDetailsModel.getSclBrandingProcess() != null && brandRequestDetailsModel.getSclBrandingProcess().iterator().hasNext()) {
                        SclBrandingProcessModel sclBrandingProcess = brandRequestDetailsModel.getSclBrandingProcess().iterator().next();
                        if (sclBrandingProcess != null) {
                            sclBrandingProcess.setAction("MODIFY");
                            modelService.save(sclBrandingProcess);
                            modelService.refresh(sclBrandingProcess);
                            businessProcessService.triggerEvent(sclBrandingProcess.getCode() + "_" + WAITING_FOR_BRANDING_APPROVAL);
                        }
                    }
                }
            //}
        }

        BrandingRequestData brandingRequestData = new BrandingRequestData();
        if(brandRequestDetailsModel!=null)
        {
            brandingRequestData.setRequisitionNumber(brandRequestDetailsModel.getRequisitionNumber());
        }
        return brandingRequestData;
    }

    public TerritoryManagementService getTerritoryManagementService() {
        return territoryManagementService;
    }

    public void setTerritoryManagementService(TerritoryManagementService territoryManagementService) {
        this.territoryManagementService = territoryManagementService;
    }
}
