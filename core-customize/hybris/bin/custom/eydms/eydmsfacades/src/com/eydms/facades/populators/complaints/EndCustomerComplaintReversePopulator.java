package com.eydms.facades.populators.complaints;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.enums.NatureOfComplaint;
import com.eydms.core.enums.NotificationCategory;
import com.eydms.core.enums.TAServiceRequestStatus;
import com.eydms.core.enums.CustomerComplaintTSOStatus;
import com.eydms.core.model.*;
import com.eydms.core.notifications.service.EyDmsNotificationService;
import com.eydms.core.services.TechnicalAssistanceService;
import com.eydms.core.services.TerritoryManagementService;
import com.eydms.core.services.impl.BrandingServiceImpl;
import com.eydms.facades.data.CustomerComplaintData;
import com.eydms.facades.data.EndCustomerComplaintData;
import com.eydms.facades.data.FilterTalukaData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogUnawareMediaModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.media.MediaFolderModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;


import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class EndCustomerComplaintReversePopulator implements Populator<EndCustomerComplaintData, EndCustomerComplaintModel> {

    @Autowired
    TechnicalAssistanceService technicalAssistanceService;

    @Autowired
    EyDmsNotificationService eydmsNotificationService;
    @Autowired
    private KeyGenerator customCodeGenerator;
    @Autowired
    FlexibleSearchService flexibleSearchService;
    @Autowired
    private ModelService modelService;
    @Autowired
    TerritoryManagementService territoryManagementService;
    @Autowired
    UserService userService;
    @Autowired
    BaseSiteService baseSiteService;
    @Autowired
    ProductService productService;
    @Autowired
    CatalogVersionService catalogVersionService;
    @Autowired
    MediaService mediaService;
    private static final Logger LOG = Logger.getLogger(EndCustomerComplaintReversePopulator.class);
    @Override
    public void populate(EndCustomerComplaintData source, EndCustomerComplaintModel target) throws ConversionException {
        target.setBrand(baseSiteService.getCurrentBaseSite());
        target.setRequestId(String.valueOf(customCodeGenerator.generate()));
        target.setCustomerNo(source.getCustomerNo());
        target.setCustomerName(source.getName());
        target.setAddressLine1(source.getSiteAddressLine1());
        target.setAddressLine2(source.getSiteAddressLine2());
        target.setState(source.getState());
        target.setDistrict(source.getDistrict());
        target.setTaluka(source.getTaluka());
        target.setCity(source.getCity());
        target.setPincode(source.getPinCode());
        target.setPhoneNumber(source.getPhoneNumber());
        if(source.getDistrict()!=null && source.getTaluka()!=null) {
            SubAreaMasterModel subArea = territoryManagementService.getTerritoryByDistrictAndTaluka(source.getDistrict(), source.getTaluka());
            if (subArea != null) {
            	List<EyDmsUserModel> tsoList =  territoryManagementService.getTSOforSubArea(subArea);
            	if(tsoList!=null && !tsoList.isEmpty()) {
            		target.setTsoAssigned(tsoList.get(0));
            		target.setTsoAssignedDate(new Date());
            	}
            	target.setSubArea(subArea);
            	DistrictMasterModel districtMaster = subArea.getDistrictMaster();
            	if (districtMaster != null) {
            		target.setDistrictMaster(districtMaster);
            		RegionMasterModel regionMaster = districtMaster.getRegion();
            		if (regionMaster != null) {
            			target.setRegionMaster(regionMaster);
            			StateMasterModel stateMaster = regionMaster.getState();
            			if (stateMaster != null) {
            				target.setStateMaster(stateMaster);
            			}
            		}
            	}
            }
        }
        modelService.save(target);

        //2nd Form
        if(source.getRetailer()!=null) {
            EyDmsCustomerModel userForUID = (EyDmsCustomerModel) userService.getUserForUID(source.getRetailer());
            target.setRetailer(userForUID);
        }
        if(source.getDealer()!=null) {
            EyDmsCustomerModel user = (EyDmsCustomerModel) userService.getUserForUID(source.getDealer());
            target.setDealer(user);
        }
        if(source.getProduct()!=null) {
            CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
            ProductModel product = productService.getProductForCode(catalogVersion, source.getProduct());
            target.setProduct(product);
        }
        if(source.getDateOfPurchase()!=null) {
            target.setDateOfPurchase(getParsedDate(source.getDateOfPurchase()));
        }
        target.setNumberOfPurchase(source.getNumberOfPurchase());
        if(source.getIsBillAvailable()) {
            //image upload file (single)
            if(target.getRequestId()!=null && source.getUploadInvoice()!=null) {
                submitEndCustomerUploadImagesActivity(target.getRequestId(), source.getUploadInvoice());
                target.setIsBillAvailable(source.getIsBillAvailable());
            }
        }
        target.setBatchNo(source.getBatchNo());
        target.setDepot(source.getDepot());
        modelService.save(target);

        //3rd form
         if(source.getDateOfUse()!=null) {
             target.setDateOfUse(getParsedDate(source.getDateOfUse()));
         }
        target.setNatureOfComplaint(NatureOfComplaint.valueOf(source.getNatureOfComplaint()));
        target.setAreaOfApplication(source.getAreaOfApplication());
        target.setProblemPerceivedByCustomer(source.getProblemPerceivedByCustomer());
        target.setProblemReportedByDepot(source.getProblemReportedByDepot());
        target.setAmountOfDamagePerceivedByCustomer(source.getAmountOfDamagePerceivedByCustomer());
        target.setAmountOfDamagePerceivedByDepot(source.getAmountOfDamagePerceivedByDepot());
        target.setCommentsAboutSurrounding(source.getCommentsAboutSurrounding());

        target.setRequestRaisedDate(new Date());
        target.setRaisedBy((EyDmsUserModel) userService.getCurrentUser());
        target.setStatus(TAServiceRequestStatus.REQUEST_RAISED);
        if(target.getRaisedBy()!=null && target.getRaisedBy().getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.SALES_OFFICER_GROUP_ID))) {
            target.setSoTaggedName(target.getRaisedBy().getName());
        }
        target.setTsoStatus(CustomerComplaintTSOStatus.PENDING);
        //target.setDepot(source.getDepot());
        if(source.getProductName()!=null){
            target.setProductName(source.getProductName());
        }
        if(source.getInvoiceNo()!=null){
            target.setInvoiceNo(source.getInvoiceNo());
        }
        if(source.getTransporter()!=null){
            target.setTransporter(source.getTransporter());
        }
        if(source.getQuantity()!=null){
            target.setQuantity(source.getQuantity());
        }
        if(source.getNoOfBagsConsumed()!=null){
            target.setNoOfBagsConsumed(source.getNoOfBagsConsumed());
        }
        if(source.getPlant()!=null){
            target.setPlant(source.getPlant());
        }
        modelService.save(target);

        try {

            StringBuilder builder = new StringBuilder();
            Map<String,String> suggestion = new HashMap<>();
            builder.append("You have a complaint request " +target.getRequestId());
            builder.append(" raised by " +target.getRaisedBy().getName() + " " +target.getRaisedBy().getUid());
            builder.append(",Taluka - " + target.getTaluka());
            builder.append(",Nature of Complaint - "+target.getNatureOfComplaint().getCode());
            String body = builder.toString();
            String sub ="New Complaint Request has been raised";
            FilterTalukaData filterTalukaData = new FilterTalukaData();
            suggestion.put("ComplaintId",target.getRequestId());
            List<SubAreaMasterModel> subAreaList = territoryManagementService.getTaulkaForUser(filterTalukaData);
            List<EyDmsUserModel> tsoList =  territoryManagementService.getTSOforSubAreas(subAreaList);
            for (EyDmsUserModel tsoUser : tsoList) {
                eydmsNotificationService.submitDealerNotification((B2BCustomerModel) tsoUser, body, sub, NotificationCategory.COMPLAINT_REGISTERED,suggestion);
            }

        }
        catch(Exception e) {
            LOG.error("Error while sending  Complaint Request  notification");
        }
    }
    public boolean submitEndCustomerUploadImagesActivity(String requestNo,String uploadPhoto)
    {
        EndCustomerComplaintModel endCustomerComplaintForRequestNo = technicalAssistanceService.getEndCustomerComplaintForRequestNo(requestNo);
        if(uploadPhoto!=null){
            MediaModel brandingPicture = getTAUploadInvoice(uploadPhoto);
            endCustomerComplaintForRequestNo.setUploadInvoice(brandingPicture);
        }
        modelService.save(endCustomerComplaintForRequestNo);
        return true;
    }


    private MediaModel getTAUploadInvoice(String data) {
        byte[] bytes = Base64.getDecoder().decode(data);
        MultipartFile multipartFile = getMultipartFile("taUploadPicture", bytes);
        return createMediaFromFile("taUploadPicture", "taUploadPicturedoc", multipartFile);
    }

    private CatalogUnawareMediaModel createMediaFromFile(final String uid, final String documentType, final MultipartFile file) {

        Long currentTimeInMillis = System.currentTimeMillis();
        final String mediaCode = documentType.concat(EyDmsCoreConstants.UNDERSCORE_CHARACTER).concat(uid).concat(String.valueOf(currentTimeInMillis));

        final MediaFolderModel imageMediaFolder = mediaService.getFolder(EyDmsCoreConstants.IMAGE_MEDIA_FOLDER_NAME);
        CatalogUnawareMediaModel documentMedia = null;

        try {
            documentMedia = (CatalogUnawareMediaModel) mediaService.getMedia(mediaCode);
        } catch (AmbiguousIdentifierException ex) {
            LOG.error("More than one media found with code : " + mediaCode);
            LOG.error("Removing duplicate media : " + mediaCode);
            CatalogUnawareMediaModel duplicateMedia = new CatalogUnawareMediaModel();
            duplicateMedia.setCode(mediaCode);
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

    public MultipartFile getMultipartFile(String name, byte[] bytes) {

        MultipartFile mfile = null;
        ByteArrayInputStream in = null;
        try {
            in = new ByteArrayInputStream(bytes);
            FileItemFactory factory = new DiskFileItemFactory(16, null);
            FileItem fileItem = factory.createItem("name", "jpeg", false, name);
            IOUtils.copy(new ByteArrayInputStream(bytes), fileItem.getOutputStream());
            mfile = new CommonsMultipartFile(fileItem);
            in.close();
        }catch (IOException e){
            LOG.error("unexpected error for getting multipart file" + e.getMessage());
        }
        return mfile;
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
}
