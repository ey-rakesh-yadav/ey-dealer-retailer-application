package com.eydms.core.services.impl;

import com.eydms.core.brand.dao.BrandDao;
import com.eydms.core.cart.dao.EyDmsTruckDao;
import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.customer.services.EyDmsCustomerService;
import com.eydms.core.dao.*;
import com.eydms.core.enums.*;
import com.eydms.core.model.*;
import com.eydms.core.notifications.service.EyDmsNotificationService;
import com.eydms.core.order.dao.OrderValidationProcessDao;
import com.eydms.core.order.dao.EyDmsOrderCountDao;
import com.eydms.core.region.service.RegionService;
import com.eydms.core.services.DJPVisitService;
import com.eydms.core.services.SalesPlanningService;
import com.eydms.core.services.TerritoryManagementService;
import com.eydms.core.utility.EyDmsDateUtility;
import com.eydms.facades.data.*;
import com.eydms.facades.data.marketvisit.MarketVisitDetailsData;
import com.eydms.facades.data.marketvisit.MarketVisitDeviationData;
import com.eydms.facades.data.marketvisit.VisitSummaryPlannedVsActualData;
import com.eydms.facades.djp.data.AddNewSiteData;
import com.eydms.facades.djp.data.CounterDetailsData;
import com.eydms.facades.djp.data.ObjectiveData;
import com.eydms.facades.djp.data.RouteData;
import com.eydms.facades.djp.data.marketvisit.VisitSummaryData;
import com.eydms.facades.visit.data.DealerSummaryData;
import com.eydms.facades.visit.data.InfluencerSummaryData;
import com.eydms.facades.visit.data.RetailerSummaryData;
import com.eydms.facades.visit.data.SiteSummaryData;
import com.eydms.occ.dto.djp.*;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2b.services.B2BUnitService;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.model.CatalogUnawareMediaModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.customer.CustomerAccountService;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.HybrisEnumValue;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.c2l.BrandModel;
import de.hybris.platform.core.model.media.MediaFolderModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.search.restriction.SearchRestrictionService;
import de.hybris.platform.searchservices.core.service.impl.DefaultSnQueryContextProviderDefinition;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.time.TimeService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.webservicescommons.dto.error.ErrorListWsDTO;
import de.hybris.platform.webservicescommons.dto.error.ErrorWsDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;


public class DJPVisitServiceImpl implements DJPVisitService {

    private static final Logger LOG = Logger.getLogger(DJPVisitServiceImpl.class);

    private static final String NOT_EYDMS_USER_MESSAGE = "EyDmsCustomer is not present";

    @Autowired
    ModelService modelService;
    @Autowired
    NetworkDao networkDao;
    @Autowired
    EnumerationService enumerationService;

    @Autowired
    EyDmsNotificationService eydmsNotificationService;

    @Autowired
    I18NService i18NService;

    @Autowired
    BrandDao brandDao;

    @Autowired
    CounterVisitMasterDao counterVisitDao;

    @Autowired
    UserService userService;

    @Autowired
    KeyGenerator customCodeGenerator;

    @Autowired
    RegionService regionService;

    @Autowired
    DJPVisitDao djpVisitDao;

    @Autowired
    EyDmsTruckDao eydmsTruckDao;

    @Autowired
    TimeService timeService;

    @Autowired
    ProductService productService;

    @Autowired
    KeyGenerator visitIdGenerator;

    @Autowired
    KeyGenerator counterVisitIdGenerator;

    @Autowired
    KeyGenerator marketIntelligenceIdGenerator;

    @Resource
    private FlexibleSearchService flexibleSearchService;

    @Autowired
    MediaService mediaService;

    @Autowired
    DjpCounterScoreDao djpCounterScoreDao;

    @Autowired
    DjpRouteScoreDao djpRouteScoreDao;

    @Autowired
    DjpRunDao djpRunDao;

    @Autowired
    VisitMasterDao visitMasterDao;

    @Autowired
    BaseSiteService baseSiteService;

    @Autowired
    CompetitorProductDao competitorProductDao;
    @Resource
    SalesPlanningDao salesPlanningDao;

    @Resource
    private B2BUnitService<B2BUnitModel, B2BCustomerModel> b2bUnitService;

    @Resource
    private DjpRouteDao djpRouteDao;

    @Autowired
    private Populator<AddressData, AddressModel> addressReversePopulator;

    @Autowired
    private EyDmsOrderCountDao eydmsOrderCountDao;

    @Resource
    private EyDmsCustomerService eydmsCustomerService;

    @Autowired
    CounterRouteMappingDao counterRouteMappingDao;

    @Resource
    private SchemeDetailsDao schemeDetailsDao;

    @Resource
    private CustomerAccountService customerAccountService;

    @Autowired
    TerritoryManagementDao territoryManagementDao;

    @Autowired
    SessionService sessionService;

    @Autowired
    private SearchRestrictionService searchRestrictionService;

    @Autowired
    TerritoryManagementService territoryManagementService;

    @Autowired
    RouteMasterDao routeMasterDao;

    @Autowired
    CollectionDao collectionDao;
    @Resource
    private SalesPlanningService salesPlanningService;

    @Autowired
    OrderValidationProcessDao orderValidationProcessDao;

    @Autowired
    ObjectiveDao objectiveDao;

    @Autowired
    SalesPerformanceDao salesPerformanceDao;

    @Override
    public boolean submitMarketMappingDetails(CounterVisitMasterData data) {
        CounterVisitMasterModel counterVisitModel = counterVisitDao.findCounterVisitById(data.getId());
        counterVisitModel.setCounterPotential(data.getTotalSale());
        counterVisitModel.setTotalSale(data.getTotalSale());
        counterVisitModel.setWholeSale(data.getWholeSale());
        counterVisitModel.setCounterSale(data.getCounterSale());
        counterVisitModel.setCounterShare(data.getCounterShare());
        counterVisitModel.setOrderGenerated(data.getOrderGenerated());
        counterVisitModel.setIsShreeCounter(data.getIsShreeCounter());
        counterVisitModel.setIsBangurCounter(data.getIsBangurCounter());
        counterVisitModel.setIsRockstrongCounter(data.getIsRockstrongCounter());

        EyDmsCustomerModel customer = counterVisitModel.getEyDmsCustomer();
        customer.setWholeSale(data.getWholeSale());
        customer.setRetailSale(data.getCounterSale());
        customer.setCounterPotential(data.getTotalSale());
        modelService.save(customer);

        if (data.getMarketMappingList() != null && !data.getMarketMappingList().isEmpty()) {
            data.getMarketMappingList().forEach(marketMappingData -> {
                MarketMappingDetailsModel model = modelService.create(MarketMappingDetailsModel.class);
                model.setId(marketIntelligenceIdGenerator.generate().toString());
                BrandModel brandById = brandDao.findBrandById(marketMappingData.getBrand());
                model.setBrand(brandById);
                EyDmsUserModel eydmsUser =(EyDmsUserModel) userService.getCurrentUser();
                List<String> states = new ArrayList<>();
                states.add(eydmsUser.getState());
                CompetitorProductModel compProduct = competitorProductDao.findCompetitorProductById(marketMappingData.getProduct(),brandById,states);
                if(compProduct!=null)
                    model.setProduct(compProduct);
                model.setWholesalePrice(marketMappingData.getWholesalePrice());
                model.setRetailsalePrice(marketMappingData.getRetailsalePrice());
                model.setDiscount(marketMappingData.getDiscount());
                model.setBilling(marketMappingData.getBilling());
                model.setStock(marketMappingData.getStock());
                model.setWholeSales(marketMappingData.getWholeSales());
                model.setRetailSales(marketMappingData.getRetailSales());
                model.setCounterVisit(counterVisitModel);
                modelService.save(model);
            });
            modelService.save(counterVisitModel);
        }
        /*CounterAggregationData counterAggregationData = getCounterAggregationData(data.getId());
        if (counterAggregationData != null) {
            if (counterAggregationData.getDiffRetailSaleVolume() == 0 && counterAggregationData.getDiffWholesaleVolume() == 0) {
                modelService.save(counterVisitModel);
            } else {
                throw new IllegalArgumentException("please provide correct value for self check");
            }
        }*/
        return true;
    }

    @Override
    public boolean submitBrandInsightDetails(String counterVisitId, BrandingInsightData brandingInsightData) {
        BrandingInsightsDetailsModel brandingInsightsDetailsModel = modelService.create(BrandingInsightsDetailsModel.class);

        BrandModel brandById = brandDao.findBrandById(brandingInsightData.getBrandId());
        brandingInsightsDetailsModel.setBrand(brandById);
        brandingInsightsDetailsModel.setBrandingType(BrandingType.valueOf(brandingInsightData.getBrandingType()));
        brandingInsightsDetailsModel.setOtherBrandingDetails(brandingInsightData.getOtherBrandDetails());
        if(brandingInsightData.getImage() != null) {
            CatalogUnawareMediaModel brandingPicture = getBrandingPicture(brandingInsightData);
            brandingInsightsDetailsModel.setBrandingImage(brandingPicture);
        }
        CounterVisitMasterModel counterVisitById = counterVisitDao.findCounterVisitById(counterVisitId);
        brandingInsightsDetailsModel.setCounterVisit(counterVisitById);
        modelService.save(brandingInsightsDetailsModel);
        return true;
    }

    @Override
    public String submitSchemeDetails(String counterVisitId, SchemeDetailsModel schemeDetailsModel) {
        CounterVisitMasterModel counterVisitById = counterVisitDao.findCounterVisitById(counterVisitId);
        schemeDetailsModel.setCounterVisit(counterVisitById);
        modelService.save(schemeDetailsModel);
        return schemeDetailsModel.getPk().toString();
    }

    @Override
    public void submitSchemeDocuments(final String schemeID, MultipartFile[] files) {

        SchemeDetailsModel schemeDetailsModel = schemeDetailsDao.findSchemeDetailsByPK(schemeID);
        if(null == schemeDetailsModel){
            throw new ModelNotFoundException("Scheme Not found with PK : "+schemeID);
        }
        if(null != files && files.length>0){
            populateSchmeDocuments(schemeDetailsModel,Arrays.asList(files),null!= schemeDetailsModel.getBrand() ?schemeDetailsModel.getBrand().getIsocode():StringUtils.EMPTY);
        }

        modelService.save(schemeDetailsModel);
    }

    private void populateSchmeDocuments(final SchemeDetailsModel schemeDetailsModel, List<MultipartFile> documents , final String brand) {
        List<MediaModel> schemeDocuments = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(schemeDetailsModel.getDocuments())){
            schemeDocuments.addAll(schemeDetailsModel.getDocuments());
        }
        for(MultipartFile file : documents){
            Long currentTimeInMillis = System.currentTimeMillis();
            final String documentName = EyDmsCoreConstants.SCHEME.SCHME_ATTR.concat(EyDmsCoreConstants.UNDERSCORE_CHARACTER)
                    .concat(String.valueOf(currentTimeInMillis));
            CatalogUnawareMediaModel mediaModel = eydmsCustomerService.createMediaFromFile(null!= brand ?brand:StringUtils.EMPTY,documentName,file);
            schemeDocuments.add(mediaModel);

        }
        schemeDetailsModel.setDocuments(schemeDocuments);

    }

    @Override
    public boolean submitLeadGeneration(LeadMasterData leadMasterData, String counterVisitId) {
        LOG.info("creating New Lead for lead Type:"+leadMasterData.getLeadType());
        if(StringUtils.isBlank(leadMasterData.getLeadType())){
            return false;
        }
        LeadMasterModel leadModel = modelService.create(LeadMasterModel.class);
        leadModel.setBrand(baseSiteService.getCurrentBaseSite());
        leadModel.setLeadId(String.valueOf(customCodeGenerator.generate()));
        leadModel.setLeadType(LeadType.valueOf(leadMasterData.getLeadType()));
        leadModel.setName(leadMasterData.getName());
        leadModel.setContactNo(leadMasterData.getContactNo());
        leadModel.setEmail(leadMasterData.getEmail());
        leadModel.setLeadStage(LeadStage.LEAD_GENERATED);
        leadModel.setCreatedBy((B2BCustomerModel) userService.getCurrentUser());
        if(userService.getCurrentUser() instanceof EyDmsCustomerModel) {
            if(!((EyDmsCustomerModel) userService.getCurrentUser()).getCounterType().getCode().equalsIgnoreCase("SP")) {
                EyDmsUserModel soForCustomer = territoryManagementService.getSOforCustomer((EyDmsCustomerModel) userService.getCurrentUser());
                leadModel.setRaisedTo(soForCustomer);
            }
        }
        if(Objects.nonNull(counterVisitId)) {
            CounterVisitMasterModel counterVisitById = counterVisitDao.findCounterVisitById(counterVisitId);
            leadModel.setCounterVisit(counterVisitById);
        }
        if(null!=leadMasterData.getAssociatedBrandId()) {
            BrandModel brandModel = brandDao.findBrandById(String.valueOf(leadMasterData.getAssociatedBrandId().getIsocode()));
            if (null != brandModel)
                leadModel.setAssociatedBrand(brandModel);
        }

       /* final AddressModel addressModel = modelService.create(AddressModel.class);
        addressModel.setOwner(userService.getCurrentUser());
        addressReversePopulator.populate(leadMasterData.getAddress(), addressModel);
        leadModel.setAddress(addressModel);*/

        if(leadMasterData.getAddress()!=null) {
            AddressData data = leadMasterData.getAddress();
            leadModel.setLine1(data.getLine1());
            leadModel.setLine2(data.getLine2());
            leadModel.setState(data.getState());
            leadModel.setDistrict(data.getDistrict());
            leadModel.setTaluka(data.getTaluka());
            leadModel.setCity(data.getErpCity());
            leadModel.setPostalCode(data.getPostalCode());
        }


        if(leadMasterData.getPotential()!=null) {
            leadModel.setPotential(leadMasterData.getPotential());
        }
        if(null != leadMasterData.getLeadType() && LeadType.valueOf(leadMasterData.getLeadType()).equals(LeadType.TPC)) {
            if(leadMasterData.getComment()!=null) {
                leadModel.setComment(leadMasterData.getComment());
            }
        }
        if(null != leadMasterData.getLeadType() && LeadType.valueOf(leadMasterData.getLeadType()).equals(LeadType.SITE))
        {
            if(leadMasterData.getSiteStage()!=null) {
                leadModel.setSiteStage(SiteStage.valueOf(leadMasterData.getSiteStage()));
            }
            if(leadMasterData.getSiteType()!=null) {
                leadModel.setSiteType(SiteType.valueOf(leadMasterData.getSiteType()));
            }
        }
        if(Objects.nonNull(leadMasterData.getAddress().getTaluka()) && Objects.nonNull(leadMasterData.getAddress().getDistrict())) {
            leadModel.setSubAreaMaster(territoryManagementService.getTerritoryByDistrictAndTaluka(leadMasterData.getAddress().getDistrict(), leadMasterData.getAddress().getTaluka()));
        }
        if(null != leadMasterData.getLeadType() && LeadType.valueOf(leadMasterData.getLeadType()).equals(LeadType.ENGINEER)
                || LeadType.valueOf(leadMasterData.getLeadType()).equals(LeadType.MASON)
                || LeadType.valueOf(leadMasterData.getLeadType()).equals(LeadType.CONTRACTOR)
                || LeadType.valueOf(leadMasterData.getLeadType()).equals(LeadType.ARCHITECT)
                || LeadType.valueOf(leadMasterData.getLeadType()).equals(LeadType.INFLUENCER))
        {
            leadModel.setCounterType(CounterType.INFLUENCER);
        }
        else if(null != leadMasterData.getLeadType() && LeadType.valueOf(leadMasterData.getLeadType()).equals(LeadType.DEALER))
        {
            leadModel.setCounterType(CounterType.DEALER);
        }
        else if(null != leadMasterData.getLeadType() && LeadType.valueOf(leadMasterData.getLeadType()).equals(LeadType.RETAILER))
        {
            leadModel.setCounterType(CounterType.RETAILER);
        }

        else if(null != leadMasterData.getLeadType() && LeadType.valueOf(leadMasterData.getLeadType()).equals(LeadType.SITE))
        {
            leadModel.setCounterType(CounterType.SITE);
        }

        leadModel.setStatus(LeadStatus.PENDING);
        modelService.save(leadModel);
        try {
            StringBuilder builder = new StringBuilder();
            Map<String,String> suggestion = new HashMap<>();
            builder.append(" You have a Lead Request " +leadModel.getLeadId());
            builder.append(" Created by : " + leadModel.getCreatedBy().getName()+ "  " +leadModel.getCreatedBy().getUid());
            builder.append(" ,Lead type : " +leadModel.getLeadType());
            builder.append(" ,Address : "+leadModel.getLine1() + " " +leadModel.getLine2());
            builder.append(" ,Lead contact no : "+leadModel.getContactNo());
            String body = builder.toString();
            String sub ="New Lead Request has been raised ";
            suggestion.put("leadId",leadModel.getLeadId());
            if(userService.getCurrentUser() instanceof EyDmsCustomerModel) {
                B2BCustomerModel dealer = (B2BCustomerModel) userService.getCurrentUser();
                B2BCustomerModel so = territoryManagementService.getSOforCustomer((EyDmsCustomerModel) leadModel.getCreatedBy());
                eydmsNotificationService.submitDealerNotification((B2BCustomerModel) leadModel.getCreatedBy(),body,sub, NotificationCategory.LEAD_GENERATED,suggestion);
                eydmsNotificationService.submitDealerNotification(so ,body,sub,NotificationCategory.LEAD_GENERATED,suggestion);

                FilterTalukaData filterTalukaData = new FilterTalukaData();
                List<SubAreaMasterModel> subAreaList = territoryManagementService.getTaulkaForUser(filterTalukaData);
                List<EyDmsUserModel> tsoList =  territoryManagementService.getTSOforSubAreas(subAreaList);
                for (EyDmsUserModel tsoUser : tsoList) {
                    eydmsNotificationService.submitDealerNotification((B2BCustomerModel) tsoUser, body, sub, NotificationCategory.LEAD_GENERATED,suggestion);
                }
            }
            else if(userService.getCurrentUser() instanceof EyDmsUserModel){

                FilterTalukaData filterTalukaData = new FilterTalukaData();
                    List<SubAreaMasterModel> subAreaList = territoryManagementService.getTaulkaForUser(filterTalukaData);
                    List<EyDmsUserModel> tsoList =  territoryManagementService.getTSOforSubAreas(subAreaList);
                    for (EyDmsUserModel tsoUser : tsoList) {
                        eydmsNotificationService.submitDealerNotification((B2BCustomerModel) tsoUser, body, sub, NotificationCategory.LEAD_GENERATED,suggestion);
                    }


            }




        }
        catch(Exception e) {
            LOG.error("Error while sending New Lead generated  notification");
        }


        return true;
    }

    @Override
    public boolean submitTruckFleetDetails(DealerFleetListData dealerFleetListData, String counterVisitId) {
        CounterVisitMasterModel counterVisitById = counterVisitDao.findCounterVisitById(counterVisitId);

        List<TruckModelMasterModel> truckModels = new ArrayList<>();
        if(dealerFleetListData.getDealerFleetList() != null && !dealerFleetListData.getDealerFleetList().isEmpty()) {
            for(DealerFleetData dealerFleetData: dealerFleetListData.getDealerFleetList())
            {
                DealersFleetDetailsModel dealerFleetModel = modelService.create(DealersFleetDetailsModel.class);
                TruckModelMasterModel truckModel = eydmsTruckDao.findTruckModelByModelNo(dealerFleetData.getTruckModel()!=null?dealerFleetData.getTruckModel().getTruckModel():null);
                dealerFleetModel.setTruckModel(truckModel);
                dealerFleetModel.setCount(dealerFleetData.getCount());
                dealerFleetModel.setCounterVisit(counterVisitById);
                if(counterVisitById!=null)
                    dealerFleetModel.setEyDmsCustomer(counterVisitById.getEyDmsCustomer());

                modelService.save(dealerFleetModel);
            }

        }

        return true;
    }

    @Override
    public boolean submitFlagDealer(String counterVisitId, boolean isFlagged, String remarkForFlag) {
        EyDmsUserModel eydmsUser = (EyDmsUserModel) userService.getCurrentUser();
        if(isFlagged) {
            try {
                CounterVisitMasterModel counterVisitModel = counterVisitDao.findCounterVisitById(counterVisitId);
                counterVisitModel.setIsDealerFlag(isFlagged);
                counterVisitModel.setRemarkForFlag(remarkForFlag);
                counterVisitModel.setFlaggedBy(eydmsUser);
                counterVisitModel.setFlagTime(new Date());
                modelService.save(counterVisitModel);
                //setting in eydms customer
                if (null != counterVisitModel.getEyDmsCustomer()) {
                    EyDmsCustomerModel eydmsCustomer = (EyDmsCustomerModel) userService.getUserForUID(counterVisitModel.getEyDmsCustomer().getUid());
                    eydmsCustomer.setIsDealerFlag(isFlagged);
                    eydmsCustomer.setRemarkForFlag(remarkForFlag);
                    eydmsCustomer.setFlaggedBy(eydmsUser);
                    eydmsCustomer.setFlagTime(new Date());
                    modelService.save(eydmsCustomer);
                }
            }catch (ModelSavingException s){
                LOG.info(s.getMessage());
            }
            return true;
        }
        else{
            return  false;
        }
    }

    @Override
    public boolean submitUnflagDealer(String counterVisitId, boolean isUnFlagged, String remarkForUnflag) {
        EyDmsUserModel eydmsUser = (EyDmsUserModel) userService.getCurrentUser();
        if(isUnFlagged) {
            try {
                CounterVisitMasterModel counterVisitModel = counterVisitDao.findCounterVisitById(counterVisitId);
                counterVisitModel.setRemarkForUnflag(remarkForUnflag);
                counterVisitModel.setIsUnFlagRequestRaised(true);
                counterVisitModel.setUnFlagRequestRaisedBy(eydmsUser);
                counterVisitModel.setUnflagRequestTime(new Date());
                // counterVisitModel.setUnFlagApprovalStatus(NetworkAdditionStatus.PENDING_FOR_APPROVAL_BY_TSM);
                modelService.save(counterVisitModel);

                if (null != counterVisitModel.getEyDmsCustomer()) {
                    EyDmsCustomerModel eydmsCustomer = (EyDmsCustomerModel) userService.getUserForUID(counterVisitModel.getEyDmsCustomer().getUid());
                    eydmsCustomer.setRemarkForUnflag(remarkForUnflag);
                    eydmsCustomer.setIsUnFlagRequestRaised(true);
                    eydmsCustomer.setUnFlagRequestRaisedBy(eydmsUser);
                    eydmsCustomer.setUnflagRequestTime(new Date());
                    eydmsCustomer.setUnFlagApprovalStatus(NetworkAdditionStatus.PENDING_FOR_APPROVAL_BY_TSM);
                    modelService.save(eydmsCustomer);
                }
            }catch (ModelSavingException e){
                LOG.info(e.getMessage());
            }
            return true;
        }
        else{
            return false;
        }

    }


    @Override
    public boolean submitFeedbackAndComplaints(String counterVisitId, FeedbackAndComplaintsListData data) {
        EyDmsUserModel eydmsUser = (EyDmsUserModel) userService.getCurrentUser();
        CounterVisitMasterModel counterVisitModel = counterVisitDao.findCounterVisitById(counterVisitId);
        if(null != data.getFeedbackAndComplaintsList() && !data.getFeedbackAndComplaintsList().isEmpty())
            for (FeedbackAndComplaintsData feedbackAndComplaintsData : data.getFeedbackAndComplaintsList()) {
                FeedbackAndComplaintsModel feedbackAndComplaintsModel = modelService.create(FeedbackAndComplaintsModel.class);
                feedbackAndComplaintsModel.setFeedbackBy((EyDmsCustomerModel) userService.getUserForUID(feedbackAndComplaintsData.getFeedbackByCode()));
                feedbackAndComplaintsModel.setComplaintsComment(feedbackAndComplaintsData.getCounterComplaints());
                feedbackAndComplaintsModel.setCounterVisit(counterVisitModel);
                feedbackAndComplaintsModel.setFeedbackCategory(FeedbackCategory.valueOf(FeedbackCategory.DJP_VISIT.getCode()));
                if(eydmsUser != null)
                    feedbackAndComplaintsModel.setFeedbackRaisedBy(eydmsUser);
                modelService.save(feedbackAndComplaintsModel);
            }
        return true;
    }

    @Override
    public DropdownListData getEnumTypes(String type) {
        DropdownListData dropdownListData= new DropdownListData();
        List<DropdownData> dropdownDataList= new ArrayList<DropdownData>();
        if(null != type) {
            Class<? extends HybrisEnumValue> enumType=null;
            if("orderStatus".equals(type))
            {
                enumType= OrderStatus.class;
            }
            else if ("incoTerms".equals(type))
            {
                enumType= IncoTerms.class;
            }
            else if ("leadType".equals(type))
            {
                enumType= LeadType.class;
            }
            else if ("brandingType".equals(type))
            {
                enumType= BrandingType.class;
            }
            else if ("siteStage".equals(type))
            {
                enumType= SiteStage.class;
            }
            else if ("siteType".equals(type))
            {
                enumType= SiteType.class;
            }
            else if ("counterType".equals(type))
            {
                enumType= CounterType.class;
            }
            else if("siteCustomerType".equals(type))
            {
                enumType=SiteCustomerType.class;
            }
            else if("targetOfConversion".equals(type))
            {
                enumType= TargetOfConversion.class;
            }
            else if("activitiesPerformedAtSite".equals(type))
            {
                enumType= ActivitiesPerformedAtSite.class;
            }
            else if("currentStageOfSiteConstruction".equals(type))
            {
                enumType= CurrentStageOfSiteConstruction.class;
            }
            else if ("partnerLevel".equals(type))
            {
                enumType= PartnerLevel.class;
            }
            else if ("schemeType".equals(type))
            {
                enumType= SchemeType.class;
            }
            else if ("schemeUnit".equals(type)){
                enumType= SchemeUnit.class;
            }
            else if ("incentiveType".equals(type)){
                enumType= IncentiveType.class;
            }
            else if ("schemeObjective".equals(type)){
                enumType= SchemeObjective.class;
            }
            else if ("routeDeviationReason".equals(type)){
                enumType= DJPRouteDeviationReason.class;
            }
            else if ("adHocAdditionReason".equals(type)){
                enumType= DJPAdHocAdditionReason.class;
            }
            else if ("counterNotVisitedReason".equals(type)){
                enumType= DJPCounterNotVisitedReason.class;
            }
            else if("taCurrentConstructionStage".equals(type))
            {
                enumType= TACurrentConstructionStage.class;
            }
            else if("refereeType".equals(type))
            {
                enumType= RefereeType.class;
            }
            else if("stageOfConstruction".equals(type))
            {
                enumType= StageOfConstruction.class;
            }
            else if("siteLocation".equals(type))
            {
                enumType= SiteLocation.class;
            }
            else if("materialTestSubCategory".equals(type))
            {
                enumType= MaterialTestSubCategory.class;
            }
            else if("materialTestName".equals(type))
            {
                enumType= MaterialTestName.class;
            }
            else if("concreteTestName".equals(type))
            {
                enumType= ConcreteTestName.class;
            }
            else if("gradeOfConcrete".equals(type))
            {
                enumType= GradeOfConcrete.class;
            }
            else if("SPRejectionReason".equals(type))
            {
                enumType= SPRejectionReason.class;
            }
            else if("TAExpertise".equals(type))

            {
                enumType= TAExpertise.class;
            }
            else if("NatureOfComplaint".equals(type))
            {
                enumType= NatureOfComplaint.class;
            }
            else if("AssitanceStatus".equals(type)){
                enumType=AssitanceStatus.class;
            }
            else if("TypeOfVisit".equals(type)){
                enumType= TypeOfVisit.class;
            }
            else if("PersonMetAtSite".equals(type)){
                enumType= PersonMetAtSite.class;
            }
            else if("CompetitionSiteStatus".equals(type)){
                enumType= CompetitionSiteStatus.class;
            }
            else if("OwnSiteStatus".equals(type)){
                enumType= OwnSiteStatus.class;
            }
            else if("ConstructionStage".equals(type)){
                enumType= ConstructionStage.class;
            }
            else if("ReasonOfNotConversion".equals(type)){
                enumType= ReasonOfNotConversion.class;
            }
            else if("CustomerComplaintTSOStatus".equals(type)){
                enumType= CustomerComplaintTSOStatus.class;
            }
            else if("DetailsOfTaskToBeDone".equals(type)){
                enumType= DetailsOfTaskToBeDone.class;
            }
            else if("SiteRootCause".equals(type)){
                enumType= SiteRootCause.class;
            }
            else if("TATSOStatus".equals(type)){
                enumType= TATSOStatus.class;
            }
            else if("ConstructionAdvisory".equals(type)){
                enumType= ConstructionAdvisory.class;
            }
            else if("Activity".equals(type)){
                enumType= Activity.class;
            }
            else if("MeetingType".equals(type)){
                enumType= MeetingType.class;
            }
            else if("InfluencerType".equals(type)){
                enumType= InfluencerType.class;
            }
            dropdownDataList=enumerationService.getEnumerationValues(enumType).stream().map(e-> getDropdownData(e)).collect(Collectors.toList());
            dropdownDataList.sort(Comparator.comparing(DropdownData::getName));

        }
        dropdownListData.setDropdown(dropdownDataList);
        return dropdownListData;
    }

    public DropdownData getDropdownData(HybrisEnumValue e)
    {
        DropdownData dropdownData= new DropdownData();
        dropdownData.setCode(e.getCode());
        dropdownData.setName(null != enumerationService.getEnumerationName(e, i18NService.getCurrentLocale()) ? enumerationService.getEnumerationName(e, i18NService.getCurrentLocale()) : e.getCode());
        return dropdownData;
    }

    @Override
    public Collection<CompetitorProductModel> getCompetitorProducts(String brandId) {
        BrandModel brand=brandDao.findBrandById(brandId);
        EyDmsUserModel eydmsUser =(EyDmsUserModel) userService.getCurrentUser();
        List<String> states = new ArrayList<>();
        states.add(eydmsUser.getState());
        List<CompetitorProductModel> products = competitorProductDao.getCompetitorProductsByState(brand, states);
        return products != null && !products.isEmpty() ? products : Collections.emptyList();
    }

    @Override

    public SearchPageData<VisitMasterModel> getMarketVisitDetails(SearchPageData searchPageData) {
        return djpVisitDao.getMarketVisitDetails(searchPageData, userService.getCurrentUser());
    }

    public List<TruckModelMasterModel> findAllTrucks() {
        List<TruckModelMasterModel> truckModelList = eydmsTruckDao.findAllTrucks();
        return Objects.nonNull(truckModelList) ? truckModelList : Collections.emptyList();
    }

    @Override
    public CounterAggregationData getCounterAggregationData(String counterVisitId) {
        CounterAggregationData counterAggregationData = new CounterAggregationData();
        List<BrandSalesAggregationData> listOfBrand = new ArrayList<>();
        List<List<Object>> listOfBrandWiseSales = counterVisitDao.fetchBrandWiseAggregatedData(counterVisitId);
        if(!listOfBrandWiseSales.isEmpty() && listOfBrandWiseSales != null) {
            listOfBrandWiseSales.stream().forEach(list -> {
                BrandSalesAggregationData brandSalesAggregationData = new BrandSalesAggregationData();
                brandSalesAggregationData.setWholesale((Double) list.get(0));
                brandSalesAggregationData.setRetailsale((Double) list.get(1));
                brandSalesAggregationData.setBrand((String) list.get(2));
                if(list.get(3) != null)
                    brandSalesAggregationData.setUid((String) list.get(3));
                listOfBrand.add(brandSalesAggregationData);
            });
        }
        counterAggregationData.setBrandSalesAggregateList(listOfBrand);
        CounterVisitMasterModel counterVisitById = counterVisitDao.findCounterVisitById(counterVisitId);
        BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();

        DoubleStream wholeSale = listOfBrand.stream().filter(brand -> brand.getUid().equals(currentBaseSite.getUid())).mapToDouble(b -> b.getWholesale());
        double totalBrandWholeSale = wholeSale.findAny().getAsDouble();

        DoubleStream retailSale = listOfBrand.stream().filter(brand -> brand.getUid().equals(currentBaseSite.getUid())).mapToDouble(b -> b.getRetailsale());
        double totalBrandRetailSale = retailSale.findAny().getAsDouble();

        counterAggregationData.setDiffWholesaleVolume(Math.abs(counterVisitById.getWholeSale() - totalBrandWholeSale));
        counterAggregationData.setDiffRetailSaleVolume(Math.abs(counterVisitById.getCounterSale() - totalBrandRetailSale));
        counterAggregationData.setCounterShare(totalBrandWholeSale + totalBrandRetailSale * 100 / counterVisitById.getTotalSale());
        return counterAggregationData;
    }

    @Override
    public VisitMasterModel getCounterList(String id) {
        VisitMasterModel visit = djpVisitDao.getCounterList(id,userService.getCurrentUser());
        LocalDate date = visit.getVisitPlannedDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        if(!date.isEqual(LocalDate.now())) {
            throw new IllegalArgumentException("This visit is not planned for today");
        }
        if(visit.getStartVisitTime()==null) {
            visit.setStatus(VisitStatus.STARTED);
            visit.setStartVisitTime(timeService.getCurrentTime());
            modelService.save(visit);
        }
        return visit;
    }

    @Override
    public void createAndSaveFinalizedCounterVisitPlan(final DJPFinalizedPlanWsDTO finalizedPlan){

        final EyDmsUserModel eydmsUserModel = (EyDmsUserModel) userService.getCurrentUser();
        DJPRouteScoreMasterModel djpRouteScoreMaster = null;
        if(eydmsUserModel.getUserType()!=null && eydmsUserModel.getUserType().equals(EyDmsUserType.SO) && finalizedPlan.getRouteScoreId()!=null) {
            djpRouteScoreMaster = new DJPRouteScoreMasterModel();
            djpRouteScoreMaster.setId(finalizedPlan.getRouteScoreId());
            djpRouteScoreMaster = flexibleSearchService.getModelByExample(djpRouteScoreMaster);
        }

//      if(visitMasterDao.findByRouteAndDate(eydmsUserModel, djpRouteScoreMaster.getRun().getPlanDate())!=null) {
//          throw new IllegalArgumentException("Planned visit exists for selected planned date");
//      }

        VisitMasterModel visitMasterModel = createVisitMasterData(djpRouteScoreMaster , finalizedPlan.getObjectiveId(),eydmsUserModel, finalizedPlan.getSubAreaMasterId(), finalizedPlan.getPlanDate());
        if(eydmsUserModel.getUserType()!=null && eydmsUserModel.getUserType().equals(EyDmsUserType.SO) && finalizedPlan.getRoute()!=null && finalizedPlan.getRouteScoreId()==null) {
            visitMasterModel.setRoute(routeMasterDao.findByRouteId(finalizedPlan.getRoute()));
        }
        modelService.save(visitMasterModel);
        if(CollectionUtils.isNotEmpty(finalizedPlan.getCounters())){
            for(DJPCounterWsDTO djpCounterWsDTO: finalizedPlan.getCounters()){
                CounterVisitMasterModel counterVisitMasterModel = createCounterVisitMasterData(djpCounterWsDTO);
                counterVisitMasterModel.setVisit(visitMasterModel);
                modelService.save(counterVisitMasterModel);
            }
        }
    }

    @Override
    public List<EyDmsCustomerModel> getFilteredCounters( DJPFinalizedPlanWsDTO plannedData){
        RouteMasterModel routeMasterModel = null;
        if(StringUtils.isNotBlank(plannedData.getRouteScoreId())) {
            final DJPRouteScoreMasterModel djpRouteScoreMasterModel = djpRouteScoreDao.findByRouteScoreId(plannedData.getRouteScoreId());

            if(null == djpRouteScoreMasterModel){
                throw new ModelNotFoundException(String.format("DJPRouteScoreMasterModel not found for id %s", plannedData.getRouteScoreId()));
            }
            routeMasterModel = djpRouteScoreMasterModel.getRoute();
            if(null == routeMasterModel){
                throw new ModelNotFoundException(String.format("RouteMasterModel not found for DJPRouteScoreMasterModel with id %s", plannedData.getRouteScoreId()));
            }
        }
        else if(StringUtils.isNotBlank(plannedData.getRoute())) {
            routeMasterModel = routeMasterDao.findByRouteId(plannedData.getRoute());
        }
        List<EyDmsCustomerModel> allCounters = new ArrayList<EyDmsCustomerModel>();

        if(routeMasterModel!=null) {
            allCounters = counterRouteMappingDao.findCounterBySubAreaIdAndRoute(plannedData.getSubAreaMasterId(), routeMasterModel.getRouteId(),  baseSiteService.getCurrentBaseSite());
        }
        if(CollectionUtils.isNotEmpty(plannedData.getCounters())){
            final List<String> counterIdList = plannedData.getCounters().stream().map(DJPCounterWsDTO :: getCustomerId).collect(Collectors.toList());
            return allCounters.stream().filter(counter -> !(counterIdList.contains(counter.getUid()))).collect(Collectors.toList());
        }
        else{
            return allCounters;
        }
    }

    @Override
    public String getCustomerType(final EyDmsCustomerModel eydmsCustomerModel) {
        String type = StringUtils.EMPTY;
        List<UserGroupModel> userGroupModelList = eydmsCustomerModel.getGroups().stream().filter(UserGroupModel.class ::isInstance).map(UserGroupModel.class::cast).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(userGroupModelList)){
            for(UserGroupModel userGroupModel : userGroupModelList){
                switch(userGroupModel.getUid()){

                    case EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID:
                        type = CounterType.DEALER.getCode();
                        break;

                    case EyDmsCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID:
                        type = CounterType.INFLUENCER.getCode();
                        break;

                    case EyDmsCoreConstants.CUSTOMER.SITE_USER_GROUP_UID:
                        type = CounterType.SITE.getCode();
                        break;

                    case EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID:
                        type = CounterType.RETAILER.getCode();
                        break;
                }
            }
        }
        return type;
    }

    @Override
    public void createAndSaveSite(final AddNewSiteData newSiteData){

        EyDmsCustomerModel siteToAdd = modelService.create(EyDmsCustomerModel.class);
        siteToAdd.setUid(String.valueOf(customCodeGenerator.generate()));
        siteToAdd.setName(newSiteData.getName());
        if(StringUtils.isNotBlank(newSiteData.getRefereeType())){
            siteToAdd.setRefereeType(RefereeType.valueOf(newSiteData.getRefereeType()));
        }
        siteToAdd.setRefereeName(newSiteData.getRefereeName());
        siteToAdd.setContactPersonName(newSiteData.getContactPersonName());
        siteToAdd.setCompletionPeriod(newSiteData.getCompletionPeriod());
        siteToAdd.setAreaOfConstruction(newSiteData.getAreaOfConstruction());
        siteToAdd.setBalancePotential(newSiteData.getBalancePotential());
        siteToAdd.setMonthlyConsumption(newSiteData.getMonthlyConsumption());
        siteToAdd.setCounterPotential(newSiteData.getTargetSale());
        siteToAdd.setPreferredBrandsList(getPreferredBrandListModels(newSiteData.getPreferredBrands()));
        siteToAdd.setMobileNumber(newSiteData.getContactNumber());
        siteToAdd.setCreatedBy((B2BCustomerModel) userService.getCurrentUser());
        siteToAdd.setCounterType(CounterType.SITE);
        populateDummyEmail(siteToAdd);
        final String defaultOtherUnitId = EyDmsCoreConstants.B2B_UNIT.EYDMS_OTHER_UNIT_UID;
        populateDefaultUnitAndGroup(siteToAdd,defaultOtherUnitId,EyDmsCoreConstants.CUSTOMER.SITE_USER_GROUP_UID);

        if(StringUtils.isNotBlank(newSiteData.getServicingDealer())){
            populateServicingDealer(siteToAdd,newSiteData.getServicingDealer());
        }
        if(StringUtils.isNotBlank(newSiteData.getRoute())){
            populateSelectedRouteToSite(siteToAdd, newSiteData.getRoute());
        }
        if(StringUtils.isNotBlank(newSiteData.getCurrentStageOfConstruction())){
            siteToAdd.setCurrentStageOfConstruction(CurrentStageOfSiteConstruction.valueOf(newSiteData.getCurrentStageOfConstruction()));
        }
        siteToAdd.setLoginDisabled(Boolean.TRUE);
        AddressData addressData = newSiteData.getAddress();
        if(addressData!=null) {
            siteToAdd.setLatitude(addressData.getLatitude());
            siteToAdd.setLongitude(addressData.getLongitude());
        }
        if(Objects.nonNull(newSiteData.getSiteType())) {
            siteToAdd.setSiteCategory(SiteCategory.valueOf(newSiteData.getSiteType()));
        }
        //Include the TSO data
        populateSiteTSOData(siteToAdd, newSiteData);
        siteToAdd.setCounterType(CounterType.SITE);
        modelService.save(siteToAdd);
        EyDmsUserModel user = (EyDmsUserModel)userService.getCurrentUser();
        String routeId = StringUtils.EMPTY;
        String routeName = StringUtils.EMPTY;
        String state = StringUtils.EMPTY;
        String district = StringUtils.EMPTY;
        String taluka = StringUtils.EMPTY;

        if(null!= siteToAdd.getRoute()){
            routeId = siteToAdd.getRoute().getRouteId();
            routeName = siteToAdd.getRoute().getRouteName();
        }
        if(addressData!=null) {
            AddressModel newAddress = modelService.create(AddressModel.class);
            addressReversePopulator.populate(addressData, newAddress);
            newAddress.setBillingAddress(true);
            newAddress.setDuplicate(true);
            newAddress.setShippingAddress(false);
            newAddress.setIsPrimaryAddress(false);
            newAddress.setVisibleInAddressBook(false);
            customerAccountService.saveAddressEntry(siteToAdd, newAddress);
            state = addressData.getState();
            district = addressData.getDistrict();
            taluka = addressData.getTaluka();
        }
        //New Territory Change
        createCounterRouteMapping(state,district,taluka,baseSiteService.getCurrentBaseSite(),siteToAdd.getUid(),user,routeId,routeName);
        CustomerSubAreaMappingModel customerSubAreaMapping =  createCustomerSubAreaMapping(state, district, taluka, siteToAdd, (CMSSiteModel) baseSiteService.getCurrentBaseSite());
        modelService.save(customerSubAreaMapping);
    }

    private void populateSiteTSOData(EyDmsCustomerModel siteToAdd, final AddNewSiteData newSiteData) {
        //TSO attributes -->
        siteToAdd.setSinglePOCName(newSiteData.getSiteSinglePocName());
        siteToAdd.setSinglePOCEmail(newSiteData.getSiteSinglePocEmail());
        siteToAdd.setSinglePOCNumber(newSiteData.getSiteSinglePocNumber());
        siteToAdd.setSinglePOCRole(newSiteData.getSiteSinglePocRole());
        siteToAdd.setSiteProjectOwner(newSiteData.getSiteProjectOwner());
        siteToAdd.setSitetAreaConstructUnit(newSiteData.getSiteAreaInUnits());
        siteToAdd.setSiteStartMonth(newSiteData.getSiteStartMonth());
        siteToAdd.setSiteCompletionMonth(newSiteData.getSiteCompletionDate());
        siteToAdd.setSitePotential(newSiteData.getSitePotential());
    }

    private String getDefaultB2BUnitId() {
        String site = null!= baseSiteService.getCurrentBaseSite()? baseSiteService.getCurrentBaseSite().getUid():null;
        String defaultUnitId = StringUtils.EMPTY;
        if(StringUtils.isNotBlank(site)){
            switch(site)
            {
                case EyDmsCoreConstants.SITE.SHREE_SITE :
                    defaultUnitId = EyDmsCoreConstants.B2B_UNIT.EYDMS_SHREE_UNIT_UID;
                    break;
                case EyDmsCoreConstants.SITE.BANGUR_SITE :
                    defaultUnitId = EyDmsCoreConstants.B2B_UNIT.EYDMS_BANGUR_UNIT_UID;
                    break;
                case EyDmsCoreConstants.SITE.ROCKSTRONG_SITE:
                    defaultUnitId =EyDmsCoreConstants.B2B_UNIT.EYDMS_ROCKSTRONG_UNIT_UID;
                    break;
            }
        }
        return defaultUnitId;
    }

    @Override
    public Set<RouteMasterModel> findAllRoutesForSO(EyDmsUserModel eydmsUserModel){
        List<EyDmsCustomerModel> associatedCustomers = new ArrayList<>(eydmsUserModel.getDealers());
        return associatedCustomers.stream().map(EyDmsCustomerModel::getRoute).collect(Collectors.toSet());
    }

    private void populateServicingDealer(final EyDmsCustomerModel siteToAdd,final  String servicingDealer) {

        try{
            EyDmsCustomerModel eydmsCustomerModel = (EyDmsCustomerModel)userService.getUserForUID(servicingDealer);
            if(null!=eydmsCustomerModel){
                siteToAdd.setEyDmsCustomers(new ArrayList<>(List.of(eydmsCustomerModel)));
            }
        }
        catch (UnknownIdentifierException | ClassCastException ex){
            LOG.error("Error Occured while getting eydmscustomer with uid: "+servicingDealer);
            LOG.error("Exception is : "+ex.getMessage());
        }
    }

    //TODO:: Update it after getting clarity
    private void populateDummyEmail(EyDmsCustomerModel siteToAdd) {
        Random random = new Random();
        int randomInt = random.nextInt(999999);
        siteToAdd.setEmail("dummyemail_"+randomInt+"@gmail.com");
    }

    private void populateDefaultUnitAndGroup(EyDmsCustomerModel customer, String defaultUnitId, String userGroupUid) {

        Set<PrincipalGroupModel> groups = new HashSet<>();
        B2BUnitModel defaultUnit = b2bUnitService.getUnitForUid(defaultUnitId);
        if(null== defaultUnit){
            throw new ModelNotFoundException(String.format("Default  unit not found with uid %s",defaultUnitId));
        }
        groups.add(defaultUnit);
        try{
            UserGroupModel eydmsSiteGroup = userService.getUserGroupForUID(userGroupUid);
            groups.add(eydmsSiteGroup);
        }
        catch (UnknownIdentifierException ex){
            LOG.error(String.format("User group not found with uid %s",userGroupUid));
        }
        customer.setGroups(groups);
    }

    private void populateSelectedRouteToSite(final EyDmsCustomerModel siteToAdd, final String routeId){
        RouteMasterModel routeModel = djpRouteDao.findRouteById(routeId);
        if(null!= routeModel){
            siteToAdd.setRoute(routeModel);
        }
    }
    private List<PreferredBrandMasterModel> getPreferredBrandListModels(List<SitePreferredBrandData> sitePreferredBrandDataList){
        List<PreferredBrandMasterModel> preferredBrandList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(sitePreferredBrandDataList)){
            for(SitePreferredBrandData sitePreferredBrandData : sitePreferredBrandDataList){
                final BrandModel brand = brandDao.findBrandById(sitePreferredBrandData.getPreferredBrand());
                if(null!= brand) {
                    PreferredBrandMasterModel preferredBrand = modelService.create(PreferredBrandMasterModel.class);
                    preferredBrand.setPreferredBrand(brand);
                    preferredBrand.setReasonOfPreference(sitePreferredBrandData.getReasonOfPreference());
                    preferredBrandList.add(preferredBrand);
                }
            }
        }
        return  preferredBrandList;
    }

    private List<EyDmsCustomerModel> getAvailableCountersOnThisRouteScore(final String routeScoreId){
        final DJPRouteScoreMasterModel djpRouteScoreMasterModel = djpRouteScoreDao.findByRouteScoreId(routeScoreId);

        if(null == djpRouteScoreMasterModel){
            throw new ModelNotFoundException(String.format("DJPRouteScoreMasterModel not found for id %s", routeScoreId));
        }
        final RouteMasterModel routeMasterModel = djpRouteScoreMasterModel.getRoute();
        if(null == routeMasterModel){
            throw new ModelNotFoundException(String.format("RouteMasterModel not found for DJPRouteScoreMasterModel with id %s", routeScoreId));
        }
        return routeMasterModel.getCustomer();
    }

    @Override
    public VisitMasterModel createVisitMasterData(final DJPRouteScoreMasterModel djpRouteScoreMaster , final String objectiveId,final EyDmsUserModel eydmsUserModel, String subAreaMasterId, String planDate){

        RouteMasterModel routeMasterModel = null;
        if(djpRouteScoreMaster!=null) {
            routeMasterModel = djpRouteScoreMaster.getRoute();
        }

        final VisitMasterModel visitMasterModel = modelService.create(VisitMasterModel.class);
        visitMasterModel.setRoute(routeMasterModel);
        visitMasterModel.setId(visitIdGenerator.generate().toString());
        visitMasterModel.setRouteScore(djpRouteScoreMaster);

        if(objectiveId!=null) {
            ObjectiveModel objectiveModel = new ObjectiveModel();
            objectiveModel.setObjectiveId(objectiveId);
            objectiveModel = flexibleSearchService.getModelByExample(objectiveModel);
            visitMasterModel.setObjective(objectiveModel);
        }

        //change for tso
        if(djpRouteScoreMaster == null)
        {
            visitMasterModel.setVisitPlannedDate(new Date());
        }

        if(djpRouteScoreMaster!=null && null!= djpRouteScoreMaster.getRun() && null != djpRouteScoreMaster.getRun().getPlanDate()){
            Calendar cal = Calendar.getInstance(); // creates calendar
            cal.setTime(djpRouteScoreMaster.getRun().getPlanDate());               // sets calendar time/date
            cal.add(Calendar.HOUR_OF_DAY, 8);
            visitMasterModel.setVisitPlannedDate(cal.getTime());
        }
        else {
            Date visitPlanDate = setNextFollowUpDate(planDate);
            Calendar cal = Calendar.getInstance(); // creates calendar
            cal.setTime(visitPlanDate);               // sets calendar time/date
            cal.add(Calendar.HOUR_OF_DAY, 8);
            visitMasterModel.setVisitPlannedDate(cal.getTime());
        }
        visitMasterModel.setStatus(VisitStatus.NOT_STARTED);
//        visitMasterModel.setApprovalStatus(ApprovalStatus.AUTO_APPROVED);
        visitMasterModel.setUser(eydmsUserModel);

        //New Territory Change
        if(subAreaMasterId!=null) {
            SubAreaMasterModel subAreaMaster = territoryManagementService.getTerritoryById(subAreaMasterId);
            visitMasterModel.setSubAreaMaster(subAreaMaster);
            DistrictMasterModel districtMaster = subAreaMaster.getDistrictMaster();
            if(districtMaster!=null) {
                visitMasterModel.setDistrictMaster(districtMaster);
                RegionMasterModel regionMasterModel = districtMaster.getRegion();
                if(regionMasterModel!=null) {
                    visitMasterModel.setRegionMaster(regionMasterModel);
//                    StateMasterModel stateMaster = regionMasterModel.getState();
//                    if(stateMaster!=null) {
//                        visitMasterModel.setStateMaster(stateMaster);
//                    }
                }
            }
        }
        return visitMasterModel;

    }

    public  CounterVisitMasterModel createCounterVisitMasterData(final DJPCounterWsDTO counter){

        CounterVisitMasterModel counterVisitMasterModel = modelService.create(CounterVisitMasterModel.class);
        counterVisitMasterModel.setId(counterVisitIdGenerator.generate().toString());
        DJPCounterScoreMasterModel djpCounterScoreMasterModel = new DJPCounterScoreMasterModel();
        if(null!= counter.getCounterScoreId()){
            djpCounterScoreMasterModel.setId(counter.getCounterScoreId());
            djpCounterScoreMasterModel = flexibleSearchService.getModelByExample(djpCounterScoreMasterModel);
            counterVisitMasterModel.setCounterScore(djpCounterScoreMasterModel);
        }


        counterVisitMasterModel.setSequence(djpCounterScoreMasterModel==null || null != counter.getVisitSequence()? counter.getVisitSequence() : djpCounterScoreMasterModel.getVisitSequence());
        if(StringUtils.isNotBlank(counter.getCustomerId())){
            try{
                counterVisitMasterModel.setEyDmsCustomer((EyDmsCustomerModel)userService.getUserForUID(counter.getCustomerId()));
            }
            catch (UnknownIdentifierException | ClassCastException ex){
                LOG.error("Error Occured while setting eydmscustomer with uid: "+counter.getCustomerId());
                LOG.error("Exception is : "+ex.getMessage());
            }

        }
        if(null == counterVisitMasterModel.getEyDmsCustomer() && null != djpCounterScoreMasterModel.getCustomer() ){
            counterVisitMasterModel.setEyDmsCustomer(djpCounterScoreMasterModel.getCustomer());
        }
        if(StringUtils.isNotBlank(counter.getCounterType())){
            counterVisitMasterModel.setCounterType(CounterType.valueOf(counter.getCounterType()));
        }

        return counterVisitMasterModel;
    }
    private CatalogUnawareMediaModel getBrandingPicture(BrandingInsightData brandingInsightData) {
        byte[] bytes = Base64.getDecoder().decode(brandingInsightData.getImage());
        MultipartFile multipartFile = getMultipartFile("brandingPicture", bytes);
        CatalogUnawareMediaModel mediaFromFile = createMediaFromFile("branding", "brandingPicture", multipartFile);
        return mediaFromFile;
    }


    public MultipartFile getMultipartFile(String name, byte[] bytes) {

        MultipartFile mfile = null;
        ByteArrayInputStream in = null;
        try {

            in = new ByteArrayInputStream(bytes);
            FileItemFactory factory = new DiskFileItemFactory(16, null);
            FileItem fileItem = factory.createItem("mainFile", "jpeg", false, name);
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
            }
        }
        documentMedia.setFolder(imageMediaFolder);
        documentMedia.setMime(file.getContentType());
        modelService.save(documentMedia);
        try {
            mediaService.setStreamForMedia(documentMedia, file.getInputStream());
        } catch (IOException ioe) {
            LOG.error("IO Exception occured while creating: " + documentType + " ,for dealer with uid: " + uid);
        }

        return (CatalogUnawareMediaModel) mediaService.getMedia(mediaCode);
    }

    @Override
    public List<DJPCounterScoreMasterModel> getDJPCounterScores(String routeScoreId,String objectiveId) {
        return djpCounterScoreDao.findCounterByRouteAndObjective(routeScoreId, objectiveId);
    }

    @Override
    public boolean startDjpVisit(String visitId) {
        VisitMasterModel model = visitMasterDao.findById(visitId);
        model.setStartVisitTime(new Date());
        model.setStatus(VisitStatus.STARTED);
        model.setSynced(false);
        modelService.save(model);
        return true;
    }

    @Override
    public boolean completeDjpVisit(String visitId) {
        VisitMasterModel model = visitMasterDao.findById(visitId);
        model.setEndVisitTime(new Date());
        model.setStatus(VisitStatus.COMPLETED);
        model.setApprovalStatus(ApprovalStatus.AUTO_APPROVED);
        model.setSynced(false);
//        if(model.getUser().getUserType().getCode().equals("SO")) {
//            if(model.getRouteDeviationReason()!=null || model.getObjectiveDeviationReason()!=null) {
//                model.setApprovalStatus(ApprovalStatus.PENDING_APPROVAL);
//            }
//            else {
//                long counterDeviationReasons = model.getCounterVisits().stream().filter(counter -> ((counter.getDeviationReason()!=null) && !(counter.getDeviationReason().isEmpty()))).count();
//                if(counterDeviationReasons > 0) {
//                    model.setApprovalStatus(ApprovalStatus.PENDING_APPROVAL);
//                }
//            }
//        }

        modelService.save(model);
        return true;
    }

    @Override
    public boolean startCounterVisit(String counterVisitId) {
        CounterVisitMasterModel model = counterVisitDao.findCounterVisitById(counterVisitId);
        model.setStartVisitTime(new Date());
        model.setNetworkType(model.getEyDmsCustomer().getNetworkType());
        model.setLastLiftingDate(model.getEyDmsCustomer().getLastLiftingDate());
        model.setSynced(false);
        modelService.save(model);
        return true;
    }

    @Override
    public long completeCounterVisit(String counterVisitId) {
        CounterVisitMasterModel model = counterVisitDao.findCounterVisitById(counterVisitId);
        if(model!=null) {
            if(model.getStartVisitTime()!=null) {
                if(model.getEndVisitTime()==null) {
                    Date endTime = new Date();
                    model.setEndVisitTime(endTime);
                    model.setSynced(false);
                    modelService.save(model);
                    EyDmsCustomerModel customer = model.getEyDmsCustomer();
                    if(customer!=null) {
                        customer.setLastVisitTime(endTime);
                        customer.setLastCounterVisit(model);
                        modelService.save(customer);
                    }
                    long difference_In_Time = model.getEndVisitTime().getTime() - model.getStartVisitTime().getTime();
                    long difference_In_Minutes = (difference_In_Time/ (1000 * 60)) % 60;
                    return difference_In_Minutes;
                }
                else {
//                  throw new IllegalArgumentException(String.format("Counter visit is already completed for pk %s", counterVisitId));
                }
            }
            else {
//              throw new IllegalArgumentException(String.format("Counter visit cannot be completed for not started counter visit for pk %s", counterVisitId));
            }
        }
        else {
            throw new ModelNotFoundException(String.format("Counter Visit Master Not found for pk %s", counterVisitId));
        }
        return 0;
    }

    @Override
    public Collection<DJPRouteScoreMasterModel> getDJPRouteScores(String plannedDate, List<DJPRouteScoreMasterModel> recommendedRoute, String district, String taluka) {
        List<VisitMasterModel>  list = null;
        try {
            list = djpVisitDao.getPlannedVisitForToday(userService.getCurrentUser(), plannedDate);
        }
        catch(Exception e) {

        }
        //      if(list!=null && !list.isEmpty()) {
        //          throw new IllegalArgumentException(String.format("Visit is already planned for this date %s", plannedDate));
        //      }
        DJPRunMasterModel model = djpRunDao.findByPlannedDateAndUser(plannedDate, district, taluka, baseSiteService.getCurrentBaseSite().getUid());
        List<DJPRouteScoreMasterModel> output = new ArrayList<DJPRouteScoreMasterModel>();

        if(model!=null) {
            List<DJPRouteScoreMasterModel> routeScoreList = djpVisitDao.findAllRouteForPlannedDate(model);
            RouteMasterModel recommendedRoute1 = model.getRecommendedRoute1();
            RouteMasterModel recommendedRoute2 = model.getRecommendedRoute2();
            Optional<DJPRouteScoreMasterModel> recommendedDjpRoute1 = routeScoreList.stream().filter(djpRoute -> recommendedRoute1!=null && djpRoute.getRoute()!=null && djpRoute.getRoute().getRouteId().equals(recommendedRoute1.getRouteId())).findFirst();
            Optional<DJPRouteScoreMasterModel> recommendedDjpRoute2 = routeScoreList.stream().filter(djpRoute -> recommendedRoute2!=null && djpRoute.getRoute()!=null && djpRoute.getRoute().getRouteId().equals(recommendedRoute2.getRouteId())).findFirst();
            if(recommendedDjpRoute1.isPresent()) {
                output.add(recommendedDjpRoute1.get());
                recommendedRoute.add(recommendedDjpRoute1.get());
            }
            if(recommendedDjpRoute2.isPresent()) {
                output.add(recommendedDjpRoute2.get());
                recommendedRoute.add(recommendedDjpRoute2.get());
            }
            output.addAll(routeScoreList.stream().filter(djpRoute-> !output.contains(djpRoute)).collect(Collectors.toList()));
        }
        return output;
    }

    @Override
    public Collection<ObjectiveModel> getDJPObjective(String routeScoreId, List<ObjectiveModel> recommendedObj) {
        Collection<ObjectiveModel> output = new ArrayList<ObjectiveModel>();

        DJPRouteScoreMasterModel djpRouteScore = djpRouteScoreDao.findByRouteScoreId(routeScoreId);
        ObjectiveModel recommendedObj1 = djpVisitDao.findOjectiveById(djpRouteScore.getRecommendedObj1());
        ObjectiveModel recommendedObj2 = djpVisitDao.findOjectiveById(djpRouteScore.getRecommendedObj2());

        if(recommendedObj1!=null) {
            output.add(recommendedObj1);
            recommendedObj.add(recommendedObj1);
        }
        if(recommendedObj2!=null) {
            output.add(recommendedObj2);
            recommendedObj.add(recommendedObj2);
        }
        List<DJPCounterScoreMasterModel> modelList = djpCounterScoreDao.findCounterByRoute(routeScoreId);
        if(modelList!=null && !modelList.isEmpty()) {
            Collection<ObjectiveModel> objModelList =  modelList.stream().map(model->model.getObjective()).distinct().collect(Collectors.toList());
            if(objModelList!=null) {
                output.addAll(objModelList.stream().filter(each->!output.contains(each)).collect(Collectors.toList()));
            }

        }
        return output;
    }

    protected static Date getDateConstraint(LocalDate localDate) {
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime dateTime = localDate.atStartOfDay(zone);
        Date date = Date.from(dateTime.toInstant());
        return date;
    }

    @Override
    public SearchPageData<VisitMasterModel> getReviewLogs(SearchPageData searchPageData, String startDate, String endDate, String searchKey, boolean isDjpApprovalWidget) {
        LocalDate startMonth = LocalDate.now().minusMonths(3);
        Date newStartDate = null;
        Date newEndDate = null;

        if(Objects.isNull(startDate))
        {
            LocalDate firstDayOfMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
            LocalDate lastDayOfMonth = LocalDate.now().plusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
            newStartDate = getDateConstraint(firstDayOfMonth);
            newEndDate = getDateConstraint(lastDayOfMonth);
        }
        else
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            try {
                newStartDate = sdf.parse(startDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            try {
                newEndDate = sdf.parse(endDate);
                Calendar c = Calendar.getInstance();
                c.setTime(newEndDate);
                c.add(Calendar.DATE, 1);
                newEndDate = c.getTime();

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        EyDmsUserModel currentUser = (EyDmsUserModel) userService.getCurrentUser();
        if(isDjpApprovalWidget) {
            if(currentUser.getUserType().getCode().equals("TSM")) {
                return djpVisitDao.getReviewLogsForTSM(searchPageData,currentUser,newStartDate,newEndDate,searchKey);
            }
            if(currentUser.getUserType().getCode().equals("RH")) {
                return djpVisitDao.getReviewLogsForRH(searchPageData,currentUser,newStartDate,newEndDate,searchKey);
            }
        }
        return djpVisitDao.getReviewLogs(searchPageData, userService.getCurrentUser(), newStartDate, newEndDate);
    }

    @Override
    public Long getCountOfCounterNotVisited() {
        EyDmsUserModel user = (EyDmsUserModel)userService.getCurrentUser();
        //New Territory Change
        Collection<EyDmsCustomerModel> counterList = territoryManagementService.getEYDMSAndNonEYDMSDealersRetailersForSubArea();
        //Collection<EyDmsCustomerModel> counterList = territoryManagementService.getDealersRetailersForSubArea(territoryManagementService.getAllSubAreaForSO(user.getUid()));
        Long count = 0L;
        if(counterList!=null) {
            YearMonth currentMonth = YearMonth.now(ZoneId.systemDefault());

            count = counterList.stream().filter(counter-> {
                if(counter.getLastVisitTime()!=null) {
                    LocalDate date = counter.getLastVisitTime().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    return !currentMonth.equals(YearMonth.from(date));
                }
                return true;
            }).count();
        }
        return count;
    }

    @Override
    public Long getCountOfTotalJouneyPlanned() {
        return djpVisitDao.getCountOfTotalJouneyPlanned(userService.getCurrentUser());
    }

    @Override
    public Map<String, Double> getAvgTimeSpent() {
        Map<String, Double> obj = new HashMap<String, Double>();
        obj.put(CounterType.DEALER.getCode(), 0.0);
        obj.put(CounterType.RETAILER.getCode(), 0.0);
        obj.put(CounterType.SITE.getCode(), 0.0);
        obj.put(CounterType.INFLUENCER.getCode(), 0.0);

        LocalDate startMonth = LocalDate.now().minusMonths(3);
        Date startDate = getDateConstraint(startMonth);
        Date endDate = timeService.getCurrentTime();
        List<List<Object>>  result = djpVisitDao.getAvgTimeSpent(userService.getCurrentUser(), startDate, endDate);
        if(result!=null && !result.isEmpty()) {
            result.forEach(output-> {
                if(output.get(2)!=null) {
                    int countOfTotalCounterVisited = (int) output.get(0);
                    long totalMinutesVisited = (long) output.get(1);
                    double avgTimeVisted = 0;
                    if(countOfTotalCounterVisited!=0) {
                        avgTimeVisted = (double)totalMinutesVisited/(double)countOfTotalCounterVisited;
                    }
                    if(output.get(2) instanceof CounterType)
                        obj.put(((CounterType)output.get(2)).getCode(), avgTimeVisted);
                    else
                        obj.put((String)output.get(2), avgTimeVisted);
                }

            });
        }
        return obj;
    }

    @Override
    public Collection<EyDmsCustomerModel> getAdHocExistingCounters( DJPFinalizedPlanWsDTO plannedData){
        //New Territory Change
        Collection<EyDmsCustomerModel> allCounters = new ArrayList<EyDmsCustomerModel>();
        if(plannedData.getVisitId()!=null) {
            VisitMasterModel visitMaster = visitMasterDao.findById(plannedData.getVisitId());
            if(visitMaster!=null && visitMaster.getSubAreaMaster()!=null) {
                List<SubAreaMasterModel> subAreas = new ArrayList<SubAreaMasterModel>();
                subAreas.add(visitMaster.getSubAreaMaster());
                allCounters = territoryManagementService.getEYDMSAndNonEYDMSAllForSubArea(subAreas);
                LOG.info("All Counters getAdHocExistingCounters" + allCounters);
            }
        }
        if(allCounters==null || allCounters.isEmpty()) {
            allCounters = territoryManagementService.getEYDMSAndNonEYDMSAllForSO();
        }
        List<EyDmsCustomerModel> allCountersFilteredList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(plannedData.getCounters())){
            final List<String> counterIdList = plannedData.getCounters().stream().map(DJPCounterWsDTO :: getCustomerId).collect(Collectors.toList());
            LOG.info("counter id list :" +counterIdList);
            if(allCounters!=null && !allCounters.isEmpty())
            {
                allCountersFilteredList = allCounters.stream().filter(counter -> counter!=null && counter.getUid()!=null && !(counterIdList.contains(counter.getUid()))).collect(Collectors.toList());
            }
            return allCountersFilteredList;
        }
        else{
            return allCounters;
        }
    }

    @Override
    public boolean submitSiteVisitForm(String counterVisitId, SiteVisitFormData data) {
        CounterVisitMasterModel counterVisitModel = counterVisitDao.findCounterVisitById(counterVisitId);
        EyDmsCustomerModel eydmsCustomer = counterVisitModel.getEyDmsCustomer();

        if(data != null && eydmsCustomer != null) {
            eydmsCustomer.setCustomerType(SiteCustomerType.valueOf(data.getCustomerType()));
            eydmsCustomer.setContactPersonName(data.getContactPersonName());
            eydmsCustomer.setContactNumber(data.getContactNumber());
            eydmsCustomer.setIsShreeSite(data.getIsShreeCounter());
            eydmsCustomer.setIsBangurSite(data.getIsBangurCounter());
            eydmsCustomer.setIsRockstrongSite(data.getIsRockstrongCounter());
            BrandModel brandUnderUse = brandDao.findBrandById(data.getBrandUnderUse());
            eydmsCustomer.setBrandUnderUse(brandUnderUse);
            eydmsCustomer.setForPrice(data.getForPrice());

            List<PreferredBrandMasterModel> preferredBrandMasterModels= new ArrayList<>();
            if (data.getPreferredBrandAndReason() != null && !data.getPreferredBrandAndReason().isEmpty()) {
                data.getPreferredBrandAndReason().forEach(preferredBrand -> {
                    PreferredBrandMasterModel preferredBrandMasterModel = modelService.create(PreferredBrandMasterModel.class);
                    BrandModel brand = brandDao.findBrandById(preferredBrand.getPreferredBrand());
                    preferredBrandMasterModel.setPreferredBrand(brand);
                    preferredBrandMasterModel.setReasonOfPreference(preferredBrand.getReasonOfPreference());
                    preferredBrandMasterModel.setCounterVisit(counterVisitModel);
                    preferredBrandMasterModels.add(preferredBrandMasterModel);
                    modelService.save(preferredBrandMasterModel);
                });
            }

            eydmsCustomer.setPreferredBrandsList(preferredBrandMasterModels);
            eydmsCustomer.setAreaOfConstruction(data.getAreaOfConstruction());
            eydmsCustomer.setCurrentStageOfConstruction(CurrentStageOfSiteConstruction.valueOf(data.getCurrentStageOfConstruction()));
            eydmsCustomer.setBalancePotential(data.getBalancePotential());
            eydmsCustomer.setMonthlyConsumption(data.getMonthlyConsumption());
            eydmsCustomer.setCompletionPeriod(data.getCompletionPeriod());
            eydmsCustomer.setTargetOfConversion(TargetOfConversion.valueOf(data.getTargetOfConversion()));
            eydmsCustomer.setActivitiesPerformedAtSite(ActivitiesPerformedAtSite.valueOf(data.getActivitiesPerformedAtSite()));
            eydmsCustomer.setNextFollowUp(setNextFollowUpDate(data.getNextFollowUp()));
            if (data.getConversion().equalsIgnoreCase("yes")) {
                UserModel dealerName = userService.getUserForUID(data.getDealerNameOfConversion());
                eydmsCustomer.setDealerNameOfConversion((EyDmsCustomerModel) dealerName);

                EyDmsUserModel eydmsUser =(EyDmsUserModel) userService.getCurrentUser();
                List<String> states = new ArrayList<>();
                states.add(eydmsUser.getState());
                CompetitorProductModel compProductsByCode = competitorProductDao.findCompetitorProductById(data.getProductSelection(), brandUnderUse, states);
                if(compProductsByCode!=null)
                    eydmsCustomer.setProductSelection(compProductsByCode);
                eydmsCustomer.setOrderReceived(data.getOrderReceived());
            }
            modelService.save(eydmsCustomer);
            return true;
        }
        return false;
    }

    private Date setNextFollowUpDate(String nextFollowUpDate) {
        Date date = null;
        try {
            date = new SimpleDateFormat("dd/MM/yyyy").parse(nextFollowUpDate);
        } catch (ParseException e) {
            LOG.error("Error Parsing next follow up Date", e);
            throw new IllegalArgumentException(String.format("Please provide valid date %s", nextFollowUpDate));
        }
        return date;
    }



    @Override
    public Collection<EyDmsCustomerModel> getcounterNotVisitedList(int month, int year) {
        EyDmsUserModel user = (EyDmsUserModel)userService.getCurrentUser();
        Collection<EyDmsCustomerModel> counterList = user.getDealers();
        Long count = 0L;
        if(counterList!=null) {
            YearMonth currentMonth = YearMonth.now(ZoneId.systemDefault());
            if(month!=0 && year!=0) {
                currentMonth = YearMonth.of(year, month);
            }
            count = counterList.stream().filter(counter-> {
                if(counter.getLastVisitTime()!=null) {
                    LocalDate date = counter.getLastVisitTime().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    YearMonth lastVisit = YearMonth.from(date);
                    if(year==date.getYear() && month>date.getMonthValue()) {
                        return true;
                    }
                    else if(year>date.getYear()) {
                        return true;
                    }
                    else {
                        return false;
                    }
                }
                return true;
            }).count();
        }
        return null;
    }

    @Override
    public Map<String, String> getLastSixCounterVisitDates(String customerId) {
        Map<String, String> map = new HashMap<String, String>();
        EyDmsCustomerModel customer = (EyDmsCustomerModel) userService.getUserForUID(customerId);
        List<Date> list = djpVisitDao.getLastSixCounterVisitDates(customer);
        if(list!=null) {
            list.forEach(result -> {
                if(result!=null) {
                    Date date = (Date) result;
                    DateFormat dateFormat = new SimpleDateFormat(EyDmsCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
                    String visitDate = dateFormat.format(date);
                    dateFormat = new SimpleDateFormat("EEEE");
                    String fullDayName = dateFormat.format(date);
                    map.put(visitDate, fullDayName);
                }
            });
        }
        return map;
    }

    @Override
    public Map<String, Object> counterVisitedForSelectedRoutes(String routeScoreId) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("lastVisitDate", "");
        map.put("counterVisited", Integer.toString(0));
        map.put("dayName", "");
        DJPRouteScoreMasterModel routeScore = djpRouteScoreDao.findByRouteScoreId(routeScoreId);
        RouteMasterModel route = routeScore.getRoute();
        if(route!=null) {
            List<List<Object>> list = djpVisitDao.counterVisitedForSelectedRoutes(route, userService.getCurrentUser());
            if(list!=null) {
                list.forEach(result -> {
                    if(result.get(0)!=null) {
                        Date date = (Date) result.get(0);
                        DateFormat dateFormat = new SimpleDateFormat(EyDmsCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
                        String visitDate = dateFormat.format(date);
                        dateFormat = new SimpleDateFormat("EEEE");
                        String fullDayName = dateFormat.format(date);
                        map.put("lastVisitDate", visitDate);
                        map.put("counterVisited", result.get(1));
                        map.put("dayName", fullDayName);
                    }
                });
            }
        }
        return map;
    }


    @Override
    public boolean saveOrderRequisitionForTaggedSites(OrderRequistionMasterData orderRequisitionData, String counterVisitId, String siteCode) {
        OrderRequisitionMasterModel orderRequisition = modelService.create(OrderRequisitionMasterModel.class);
        CounterVisitMasterModel counterVisitById = counterVisitDao.findCounterVisitById(counterVisitId);

        final Map<String, Double> productQuantityMap = new HashMap<>();
        orderRequisitionData.getProductQuantityList().forEach(productQty -> {
            if (null != productService.getProductForCode(productQty.getCode())) {
                productQuantityMap.put(productQty.getCode(), productQty.getQuantity());
            }
        });

        orderRequisition.setProductQuantity(productQuantityMap);
        Double sum = orderRequisition.getProductQuantity().entrySet().stream().collect(Collectors.summingDouble(p -> p.getValue()));
        if(siteCode!=null) {
            EyDmsCustomerModel eydmsCustomer = (EyDmsCustomerModel) userService.getUserForUID(siteCode);
            //eydmsCustomer.setTotalOrderGenerated(sum);
            modelService.save(eydmsCustomer);
            orderRequisition.setCounterVisit(counterVisitById);
            if(orderRequisitionData.getServingPartner()!=null)
                orderRequisition.setServingPartner((EyDmsCustomerModel) userService.getUserForUID(orderRequisitionData.getServingPartner()));
            orderRequisition.setSiteCode(siteCode);
            orderRequisition.setTotalOrderGenerate(sum);
            orderRequisition.setEyDmsCustomer(eydmsCustomer);
            modelService.save(orderRequisition);
        }
        return true;
    }


    @Override
    public ErrorListWsDTO updateAdhocCounters(final UpdateCountersWsDTO adHocCountersWsDTO){

        validateParameterNotNullStandardMessage("adHocCounters",adHocCountersWsDTO);
        validateParameterNotNullStandardMessage("visitId",adHocCountersWsDTO.getVisitId());

        ErrorListWsDTO errorListWsDTO = new ErrorListWsDTO();
        List<ErrorWsDTO> errorWsDTOS = new ArrayList<>();

        final String visitId = adHocCountersWsDTO.getVisitId();
        VisitMasterModel visitMasterModel = null;
        if(StringUtils.isNotBlank(visitId)){
            visitMasterModel = visitMasterDao.findById(visitId);
        }
        if(null == visitMasterModel){
            ErrorWsDTO errorWsDTO = getErrors(visitId,"Visit not found with given id",ModelNotFoundException.class.getName());
            errorWsDTOS.add(errorWsDTO);
            errorListWsDTO.setErrors(errorWsDTOS);
            return errorListWsDTO;
        }
        if(!VisitStatus.STARTED.equals(visitMasterModel.getStatus())){
            ErrorWsDTO errorWsDTO = getErrors(visitId,"Could not update adhoc counters as  visit not started yet.", ModelNotFoundException.class.getName());
            errorWsDTOS.add(errorWsDTO);
            errorListWsDTO.setErrors(errorWsDTOS);
            return errorListWsDTO;
        }
        if(CollectionUtils.isNotEmpty(adHocCountersWsDTO.getCounters())){
            for(DJPCounterWsDTO counter : adHocCountersWsDTO.getCounters()){
                if(EyDmsCoreConstants.DJP.DELETE_UI_ACTION.equals(counter.getUiAction())){
                    removeCounterVisitFromList(counter.getCounterVisitId(),errorWsDTOS,true);
                }
                else if(EyDmsCoreConstants.DJP.ADD_UI_ACTION.equals(counter.getUiAction())){
                    CounterVisitMasterModel counterVisitMasterModel = createCounterVisitMasterData(counter);
                    counterVisitMasterModel.setIsAdHoc(Boolean.TRUE);
                    counterVisitMasterModel.setVisit(visitMasterModel);
                    modelService.save(counterVisitMasterModel);
                }
            }
        }
        errorListWsDTO.setErrors(errorWsDTOS);
        return errorListWsDTO;
    }

    @Override
    public ErrorListWsDTO updateCounters(final UpdateCountersWsDTO updateCountersWsDTO){

        validateParameterNotNullStandardMessage("updateCountersWsDTO",updateCountersWsDTO);
        validateParameterNotNullStandardMessage("visitId",updateCountersWsDTO.getVisitId());

        ErrorListWsDTO errorListWsDTO = new ErrorListWsDTO();
        List<ErrorWsDTO> errorWsDTOS = new ArrayList<>();

        final String visitId = updateCountersWsDTO.getVisitId();
        VisitMasterModel visitMasterModel = null;
        if(StringUtils.isNotBlank(visitId)){
            visitMasterModel = visitMasterDao.findById(visitId);
        }
        if(null == visitMasterModel){
            ErrorWsDTO errorWsDTO = getErrors(visitId,"Visit not found with given id",ModelNotFoundException.class.getName());
            errorWsDTOS.add(errorWsDTO);
            errorListWsDTO.setErrors(errorWsDTOS);
            return errorListWsDTO;
        }
        if(!VisitStatus.NOT_STARTED.equals(visitMasterModel.getStatus())){
            ErrorWsDTO errorWsDTO = getErrors(visitId,"Could not update  counters as visit is not in NOT_STARTED status", ModelNotFoundException.class.getName());
            errorWsDTOS.add(errorWsDTO);
            errorListWsDTO.setErrors(errorWsDTOS);
            return errorListWsDTO;
        }
        if(CollectionUtils.isNotEmpty(updateCountersWsDTO.getCounters())){
            for(DJPCounterWsDTO counter : updateCountersWsDTO.getCounters()){
                if(EyDmsCoreConstants.DJP.DELETE_UI_ACTION.equals(counter.getUiAction())){
                    removeCounterVisitFromList(counter.getCounterVisitId(),errorWsDTOS,false);
                }
                else if(EyDmsCoreConstants.DJP.ADD_UI_ACTION.equals(counter.getUiAction())){
                    CounterVisitMasterModel counterVisitMasterModel = createCounterVisitMasterData(counter);
                    counterVisitMasterModel.setIsAdHoc(Boolean.FALSE);
                    counterVisitMasterModel.setVisit(visitMasterModel);
                    modelService.save(counterVisitMasterModel);
                }
            }
        }
        errorListWsDTO.setErrors(errorWsDTOS);
        return errorListWsDTO;
    }

    private ErrorWsDTO getErrors(final String errorCode, final String reason , final String type){
        ErrorWsDTO errorWsDTO = new ErrorWsDTO();
        errorWsDTO.setReason(reason);
        errorWsDTO.setType(type);
        errorWsDTO.setErrorCode(errorCode);
        return errorWsDTO;
    }
    private void removeCounterVisitFromList(final String counterVisitId, final List<ErrorWsDTO> errors , final boolean adHocCounterRemoval) {
        final CounterVisitMasterModel counterVisit = counterVisitDao.findCounterVisitById(counterVisitId);
        if (null == counterVisit) {
            ErrorWsDTO errorWsDTO = getErrors(counterVisitId, "Counter Visit not found with given id", ModelNotFoundException.class.getName());
            errors.add(errorWsDTO);
            return;
        } else if (adHocCounterRemoval && !Boolean.TRUE.equals(counterVisit.getIsAdHoc())) {
            ErrorWsDTO errorWsDTO = getErrors(counterVisitId, "Counter Visit is not adhoc", ModelNotFoundException.class.getName());
            errors.add(errorWsDTO);
            return;
        } else if (!adHocCounterRemoval && Boolean.TRUE.equals(counterVisit.getIsAdHoc())) {
            ErrorWsDTO errorWsDTO = getErrors(counterVisitId, "Counter Visit is  adhoc", ModelNotFoundException.class.getName());
            errors.add(errorWsDTO);
            return;
        } else {
            modelService.remove(counterVisit);
        }
    }
    @Override
    public Collection<CounterVisitMasterModel> getSelectedCounterList(String id){
        VisitMasterModel visit = djpVisitDao.getCounterList(id,userService.getCurrentUser());
        return visit.getCounterVisits();
    }

    @Override
    public CounterVisitAnalyticsWsDTO getCompletedVisitStatisticsDataForSO(final EyDmsUserModel eydmsUserModel){

        LocalDate now = LocalDate.now();
        //dates to fetch the visit data
        LocalDate endDate = now.minusDays(1);
        LocalDate startDate = now.minusMonths(7);
        startDate = startDate.with(TemporalAdjusters.firstDayOfMonth());
        Date formattedStartDate = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date formattedEndDate = Date.from(endDate.atTime(23,59,59).atZone(ZoneId.systemDefault()).toInstant());

        List<VisitMasterModel> visitMasterModels = djpVisitDao.getCompletedPlannedVisitsBetweenDatesForSO(eydmsUserModel,formattedStartDate,formattedEndDate);

        //daily plan data
        LocalDate startDateForDailyPlan = now.minusDays(7);
        final List<VisitMasterModel> filteredListForDailyVisits = visitMasterModels.stream().filter(visit -> visit.getVisitPlannedDate() != null ).filter(visit -> visit.getVisitPlannedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().compareTo(startDateForDailyPlan) >=0 && visit.getVisitPlannedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().compareTo(endDate) <=0).collect(Collectors.toList());
        CounterVisitAnalyticsDataWsDTO  dailyData = getDailyVisitAnalytics(filteredListForDailyVisits,7,startDateForDailyPlan);

        //Weekly  plan data
        LocalDate firstDateOfCurrentWeek = now.with(DayOfWeek.MONDAY);
        final LocalDate  startDateForWeeklyPlan = firstDateOfCurrentWeek.minusWeeks(7);
        final List<VisitMasterModel> filteredListForWeeklyVisits = visitMasterModels.stream().filter(visit -> visit.getVisitPlannedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().compareTo(startDateForWeeklyPlan) >=0 && visit.getVisitPlannedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().compareTo(endDate) <=0).collect(Collectors.toList());
        CounterVisitAnalyticsDataWsDTO  weeklyData = getWeeklyVisitAnalytics(filteredListForWeeklyVisits,7,startDateForWeeklyPlan);

        //monthly data
        CounterVisitAnalyticsDataWsDTO  monthlyData = getMonthlyVisitAnalytics(visitMasterModels,7,startDate);

        CounterVisitAnalyticsWsDTO counterVisitAnalyticsWsDTO = new CounterVisitAnalyticsWsDTO();
        counterVisitAnalyticsWsDTO.setDaily(dailyData);
        counterVisitAnalyticsWsDTO.setWeekly(weeklyData);
        counterVisitAnalyticsWsDTO.setMonthly(monthlyData);

        return counterVisitAnalyticsWsDTO;
    }
    private CounterVisitAnalyticsDataWsDTO getDailyVisitAnalytics(List<VisitMasterModel> visits ,int days ,  LocalDate startDate){
        CounterVisitAnalyticsDataWsDTO dailyData = new CounterVisitAnalyticsDataWsDTO();
        Map<String,String> visitMap = new LinkedHashMap<>();

        BigDecimal totalVisitCount = new BigDecimal(0);
        int remainingDays = days;
        LOG.error("Start Daily Avg visit - ");

        while(remainingDays>0){
            long visitCountOnThisDate = getCountOfVisitOnThisDate(visits,startDate);
            visitMap.put(getFormattedDate(startDate),String.valueOf(visitCountOnThisDate));
            startDate = startDate.plusDays(1);
            remainingDays--;
            totalVisitCount = totalVisitCount.add(BigDecimal.valueOf(visitCountOnThisDate));
            LOG.error("Daily Visit count on  - " + startDate.toString() + " is " + totalVisitCount);
        }

        int averageVisitPerDay  = (int)(Math.round(totalVisitCount.doubleValue())/days);
        LOG.error("End Daily Avg visit - " + averageVisitPerDay);

        dailyData.setAverage(String.valueOf(averageVisitPerDay));
        dailyData.setVisits(visitMap);
        return dailyData;
    }

    private CounterVisitAnalyticsDataWsDTO getWeeklyVisitAnalytics(final List<VisitMasterModel> visits ,final int count , final LocalDate date){
        CounterVisitAnalyticsDataWsDTO weeklyData = new CounterVisitAnalyticsDataWsDTO();
        Map<String,String> visitMap = new LinkedHashMap<>();

        LocalDate startDate = date;
        LocalDate endDate = startDate.plusDays(6);

        LOG.error("Start Weekly Avg visit - ");

        BigDecimal totalVisitCount = new BigDecimal(0);
        int remainingCount = count;
        while(remainingCount>0){
            long visitCountBetweenThisDate = getCountOfVisitBetweenThisDate(visits,startDate,endDate);
            visitMap.put(getFormattedDateForWeek(startDate),String.valueOf(visitCountBetweenThisDate));
            startDate = startDate.plusDays(7);
            endDate = endDate.plusDays(7);
            remainingCount--;
            totalVisitCount = totalVisitCount.add(BigDecimal.valueOf(visitCountBetweenThisDate));
            LOG.error("Weekly Visit count on  - " + startDate.toString() + " is " + totalVisitCount);
        }

        int averageVisitPerDay  = (int)(Math.round(totalVisitCount.doubleValue())/count);
        LOG.error("End Weekly Avg visit - " + averageVisitPerDay);

        weeklyData.setAverage(String.valueOf(averageVisitPerDay));
        weeklyData.setVisits(visitMap);
        return weeklyData;
    }

    private CounterVisitAnalyticsDataWsDTO getMonthlyVisitAnalytics(final List<VisitMasterModel> visits ,final int count , final LocalDate date){
        CounterVisitAnalyticsDataWsDTO weeklyData = new CounterVisitAnalyticsDataWsDTO();
        Map<String,String> visitMap = new LinkedHashMap<>();

        LocalDate startDate = date;
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());
        LOG.error("Start Monthly Avg visit - ");

        BigDecimal totalVisitCount = new BigDecimal(0);
        int remainingCount = count;
        while(remainingCount>0){
            long visitCountBetweenThisDate = getCountOfVisitBetweenThisDate(visits,startDate,endDate);
            String month = getFormattedMonth(startDate);
            if(month!=null) {
                month = month.replace(",", "'");
            }
            visitMap.put(month,String.valueOf(visitCountBetweenThisDate));
            startDate = startDate.plusMonths(1);
            endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());
            remainingCount--;
            totalVisitCount = totalVisitCount.add(BigDecimal.valueOf(visitCountBetweenThisDate));
            LOG.error("Monthly Visit count on  - " + startDate.toString() + " is " + totalVisitCount);
        }

        int averageVisitPerDay  = (int)(Math.round(totalVisitCount.doubleValue())/count);
        LOG.error("End Monthly Avg visit - " + averageVisitPerDay);
        weeklyData.setAverage(String.valueOf(averageVisitPerDay));
        weeklyData.setVisits(visitMap);
        return weeklyData;
    }

    private String getFormattedMonth(final LocalDate date) {
        DateTimeFormatter pattern = DateTimeFormatter.ofPattern(EyDmsCoreConstants.DJP.STATISTICS_MONTH_PATTERN);
        return date.format(pattern);
    }


    private String getFormattedDate(final LocalDate date){
        if(null!= date){
            DateTimeFormatter pattern = DateTimeFormatter.ofPattern(EyDmsCoreConstants.DJP.STATISTICS_DATE_PATTERN);
            return date.format(pattern);
        }
        else return StringUtils.EMPTY;

    }
    private String getFormattedDateForWeek(final LocalDate startDate){
        if(null!= startDate){
            DateTimeFormatter pattern = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            return startDate.format(pattern);
        }
        else return StringUtils.EMPTY;

    }
    private long getCountOfVisitOnThisDate(final List<VisitMasterModel> visits, final LocalDate date){
        long counterVisitsCompleted = 0;
        List<VisitMasterModel> visitsPlanned = visits.stream().filter( visit -> visit.getVisitPlannedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().compareTo(date) == 0).collect(Collectors.toList());
//        return visitsPlanned.stream().mapToLong(vp -> vp.getCounterVisits().size()).sum();
        for(VisitMasterModel visitMasterModel : visitsPlanned) {
            if(visitMasterModel.getCounterVisits()!=null && !visitMasterModel.getCounterVisits().isEmpty()) {
                counterVisitsCompleted += visitMasterModel.getCounterVisits().stream().filter(cvm -> (cvm.getStartVisitTime() != null && cvm.getEndVisitTime() != null)).count();
            }
        }
        return counterVisitsCompleted;
    }

    private long getCountOfVisitBetweenThisDate(final List<VisitMasterModel> visits, final LocalDate startDate, final LocalDate endDate){
        long counterVisitsCompleted = 0;
        List<VisitMasterModel> visitsPlanned = visits.stream().filter( visit -> visit.getVisitPlannedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().compareTo(startDate) >= 0 && visit.getVisitPlannedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().compareTo(endDate) <= 0).collect(Collectors.toList());
        for(VisitMasterModel visitMasterModel : visitsPlanned) {
            if(visitMasterModel.getCounterVisits()!=null && !visitMasterModel.getCounterVisits().isEmpty()) {
                counterVisitsCompleted += visitMasterModel.getCounterVisits().stream().filter(cvm -> (cvm.getStartVisitTime() != null && cvm.getEndVisitTime() != null)).count();
            }
        }
        return counterVisitsCompleted;
    }



    public SiteSummaryData getSiteSummary(String counterVisitId) {
        CounterVisitMasterModel counterVisit = counterVisitDao.findCounterVisitById(counterVisitId);
        SiteSummaryData summaryData = new SiteSummaryData();
        try {
            EyDmsCustomerModel eydmsCustomer = counterVisit.getEyDmsCustomer();

            summaryData.setLastVisitDate(eydmsCustomer.getLastVisitTime());
            summaryData.setNextFollowUpDate(eydmsCustomer.getNextFollowUp());
            summaryData.setConstructionArea(eydmsCustomer.getAreaOfConstruction());
            try {
                summaryData.setConstructionStatus(eydmsCustomer.getCurrentStageOfConstruction().getCode());
            }
            catch(NullPointerException n)
            {
                LOG.debug(n);
            }
            summaryData.setMonthlyConsumption(eydmsCustomer.getMonthlyConsumption());
            summaryData.setBalancePotential(eydmsCustomer.getBalancePotential());
        }
        catch(NullPointerException e)
        {
            LOG.debug(e);
        }
        return summaryData;
    }

    @Override
    public String getLastVisitDate(String counterVisitId) {
        CounterVisitMasterModel counterVisitModel = counterVisitDao.findCounterVisitById(counterVisitId);
        EyDmsCustomerModel eydmsCustomer;
        String lastVisitDate;
        if (null != counterVisitModel.getEyDmsCustomer()) {
            eydmsCustomer = (EyDmsCustomerModel) userService.getUserForUID(counterVisitModel.getEyDmsCustomer().getUid());
            Date date = djpVisitDao.getLastVisitDate(eydmsCustomer);
            DateFormat dateFormat = new SimpleDateFormat(EyDmsCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
            lastVisitDate = dateFormat.format(date);
        }
        else{
            throw new ModelNotFoundException(NOT_EYDMS_USER_MESSAGE);
        }
        return lastVisitDate;
    }

    @Override
    public Integer getVisitCountMTD(String counterVisitId) {
        CounterVisitMasterModel counterVisitModel = counterVisitDao.findCounterVisitById(counterVisitId);
        EyDmsCustomerModel eydmsCustomer;

        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);

        Integer mtdVisitCount;
        if (null != counterVisitModel.getEyDmsCustomer()) {
            eydmsCustomer = counterVisitModel.getEyDmsCustomer();
            mtdVisitCount = djpVisitDao.getVisitCountMTD(eydmsCustomer, month, year);
        }
        else{
            throw new ModelNotFoundException(NOT_EYDMS_USER_MESSAGE);
        }
        return mtdVisitCount;
    }

    @Override
    public EyDmsCustomerModel getProductMixForDealerSummary(String counterVisitId){
        CounterVisitMasterModel counterVisitModel = counterVisitDao.findCounterVisitById(counterVisitId);
        return counterVisitModel.getEyDmsCustomer();
    }
    @Override
    public DealerSummaryData getDealerSummary(String counterVisitId) {
        Calendar cal = Calendar.getInstance();
        int month1 = cal.get(Calendar.MONTH);
        int year1 = cal.get(Calendar.YEAR);

        cal.add(Calendar.MONTH, -1);
        int month2 = cal.get(Calendar.MONTH);
        int year2 = cal.get(Calendar.YEAR);

        DealerSummaryData data = new DealerSummaryData();

        CounterVisitMasterModel counterVisitModel = counterVisitDao.findCounterVisitById(counterVisitId);
        try {
            data.setSales(counterVisitModel.getTotalSale()!=null?counterVisitModel.getTotalSale():0.0);
            EyDmsCustomerModel eydmsCustomer = counterVisitModel.getEyDmsCustomer();
            if(Objects.nonNull(eydmsCustomer))
            {
                if(Objects.nonNull(eydmsCustomer.getDealerCategory())){
                    data.setDealerCategory(eydmsCustomer.getDealerCategory().getCode());
                }
                int visits = djpVisitDao.getVisitCountMTD(eydmsCustomer, month1, year1);
                data.setVisits(visits);
                data.setPotential(eydmsCustomer.getCounterPotential());
                data.setLastVisitDate(eydmsCustomer.getLastVisitTime());
                data.setWholeSale(eydmsCustomer.getWholeSale());
                data.setCounterSale(eydmsCustomer.getRetailSale());

                List<List<Object>> currentMonthCounterShareInfo = djpVisitDao.getCounterSharesForDealerOrRetailer(counterVisitModel.getEyDmsCustomer().getPk().toString(), month1, year1);
                try {
                    data.setCurrentMonthCounterShare((Double)currentMonthCounterShareInfo.get(0).get(0)!=null?((Double)currentMonthCounterShareInfo.get(0).get(0)/Double.valueOf((Integer)currentMonthCounterShareInfo.get(0).get(1))):0.0);
                }catch(Exception e)
                {
                    data.setCurrentMonthCounterShare(0.0);
                }

                List<List<Object>> lastMonthCounterShareInfo = djpVisitDao.getCounterSharesForDealerOrRetailer(counterVisitModel.getEyDmsCustomer().getPk().toString(), month2, year2);
                try {
                    data.setLastMonthCounterShare((Double)lastMonthCounterShareInfo.get(0).get(0)!=null?((Double)lastMonthCounterShareInfo.get(0).get(0)/Double.valueOf((Integer)lastMonthCounterShareInfo.get(0).get(1))):0.0);
                }catch(Exception e)
                {
                    data.setLastMonthCounterShare(0.0);
                }

                BaseSiteModel brand = baseSiteService.getCurrentBaseSite();
                CustomerCategory category = CustomerCategory.TR;

                Double totalOutstandingAmount = 0.0;
                if(eydmsCustomer.getCustomerNo()!=null) {
                    totalOutstandingAmount = djpVisitDao.getDealerOutstandingAmount(eydmsCustomer.getCustomerNo());
                }

                cal = Calendar.getInstance();
                Date curDate = cal.getTime();
                cal.add(Calendar.YEAR, -1);
                Date prevYear = cal.getTime();

                Double dailyAverageSales = 0.0;
                if(eydmsCustomer.getCustomerNo()!=null) {
                    dailyAverageSales = collectionDao.getDailyAverageSalesForDealer(eydmsCustomer.getCustomerNo());
                }

                Double outstandingDays = 0.0;
                if(dailyAverageSales!=0.0)
                {
                    outstandingDays = totalOutstandingAmount/dailyAverageSales;
                }
                data.setOutstandingDays(outstandingDays);

                data.setOutstandingAmount(totalOutstandingAmount);
                //Credit consumed/credit utilized = [(outstanding amount + pending orders)/Total credit limit]*100

                //Credit utilized = outstanding amount+pending orders

                // Double securityDeposit = collectionDao.getSecurityDepositForDealer(eydmsCustomer.getCustomerNo());
                // Double creditLimitMultiplier = 2.0;
                Double creditLimit = 0.0;
                if(eydmsCustomer.getCustomerNo()!=null) {
                    creditLimit = djpVisitDao.getDealerCreditLimit(eydmsCustomer.getCustomerNo());
                }
                Double availableCredit = creditLimit - totalOutstandingAmount;
                double pendingOrderAmount = orderValidationProcessDao.getPendingOrderAmount(eydmsCustomer.getPk().toString());
                if (creditLimit != 0.0) {
                    //Double creditConsumed = (totalOutstandingAmount / (totalOutstandingAmount + availableCredit)) * 100;
                    double creditConsumed = ((totalOutstandingAmount + pendingOrderAmount) / creditLimit) * 100;
                    data.setCreditConsumed(creditConsumed);
                } else {
                    data.setCreditConsumed(0.0);
                }
                if (availableCredit > 0.0) {
                    data.setAvailableCredit(availableCredit);
                } else {
                    data.setAvailableCredit(0.0);
                }

                if(eydmsCustomer.getCustomerNo()!=null) {
                    setOutStandingBuckets(data,eydmsCustomer.getCustomerNo());

                    Date maxDate=djpVisitDao.getLastLiftingDateForDealer(eydmsCustomer.getCustomerNo(), brand);
                    if(maxDate != null) {
                        data.setLastLiftingDate(maxDate);

                        Double lastLiftingQuantity= djpVisitDao.getLastLiftingQuantityForDealer(eydmsCustomer.getCustomerNo(),brand,maxDate);
                        data.setLastLiftingQuantity(lastLiftingQuantity);

                        cal=Calendar.getInstance();
                        cal.add(Calendar.MONTH, -1);
                        Date date = cal.getTime();
                        cal.add(Calendar.MONTH, -2);
                        Date date2 = cal.getTime();

                        if(data.getLastLiftingDate()!=null)
                        {
                            if(date.compareTo(data.getLastLiftingDate())<0)
                                data.setNetworkType(NetworkType.ACTIVE.getCode());
                            else if(date2.compareTo(data.getLastLiftingDate())<0)
                                data.setNetworkType(NetworkType.INACTIVE.getCode());
                            else
                                data.setNetworkType(NetworkType.DORMANT.getCode());
                        }
                    }

                    Integer creditBreachedMTDCount = eydmsOrderCountDao.findCreditBreachCountMTD(eydmsCustomer);
                    data.setCreditBreachCount(creditBreachedMTDCount);
                    List<List<Double>> bucketList = djpVisitDao.getOutstandingBucketsForDealer(eydmsCustomer.getCustomerNo());
                    if (CollectionUtils.isNotEmpty(bucketList) && CollectionUtils.isNotEmpty(bucketList.get(0))) {
                        double bucketsTotal = bucketList.get(0).stream().filter(b -> b != null).mapToDouble(b -> b.doubleValue()).sum();
                        data.setBucketsTotal(String.valueOf(bucketsTotal));
                    }
                }
                else {
                    data.setCreditBreachCount(0);
                    data.setBucketsTotal("0.0");
                    data.setLastLiftingQuantity(0.0);
                    data.setLastLiftingDate(null);
                }

                //flag/unflag
                data.setIsDealerFlag(eydmsCustomer.getIsDealerFlag());
                data.setRemarkForFlag(eydmsCustomer.getRemarkForFlag());
                //data.setCurrentRemarkForFlag(eydmsCustomer.getCurrentRemarkForFlag());


                UserModel user = userService.getCurrentUser();
                userService.setCurrentUser(eydmsCustomer);
                Integer retailerNetwork = territoryManagementService.getRetailerCountForDealer();
                data.setRetailerNetwork(retailerNetwork!=null?retailerNetwork:0);

                Integer influencerNetwork = territoryManagementService.getInfluencerCountForDealer();
                data.setInfluencerNetwork(influencerNetwork!=null?influencerNetwork:0);
                userService.setCurrentUser(user);

            }


        }catch(NullPointerException e)
        {
            LOG.info("CounterVisitModel not found");
        }
        return data;
    }


    @Override
    public RetailerSummaryData getRetailerSummary(String code) {


        RetailerSummaryData data = new RetailerSummaryData();
        BaseSiteModel brand = baseSiteService.getCurrentBaseSite();
        String transactionType = EyDmsCoreConstants.DJP.RETAILER_TRANSACTION_TYPE;


       /* EyDmsUserModel eydmsUser = (EyDmsUserModel) userService.getCurrentUser();

        List<EyDmsCustomerModel> influencer = (List<EyDmsCustomerModel>) eydmsUser.getDealers().stream().filter(r->(r.getGroups()).contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID))).collect(Collectors.toList());
        data.setInfluencerNetwork(influencer!=null?influencer.size():0);*/

        EyDmsCustomerModel eydmsCustomer = eydmsCustomerService.getEyDmsCustomerForUid(code);
        try {
            if (eydmsCustomer != null) {
                String customerNo = eydmsCustomer.getCustomerNo();

                CustomerCategory category = CustomerCategory.TR;

                Calendar cal = Calendar.getInstance();
                int month1 = cal.get(Calendar.MONTH);
                int year1 = cal.get(Calendar.YEAR);
                int daysInMonth = cal.getActualMaximum(Calendar.DATE);

                cal.add(Calendar.MONTH, -1);
                int month2 = cal.get(Calendar.MONTH);
                int year2 = cal.get(Calendar.YEAR);

                data.setName(eydmsCustomer.getName());

                if (eydmsCustomer.getProfilePicture()!=null && eydmsCustomer.getProfilePicture().getURL() != null && !eydmsCustomer.getProfilePicture().getURL().isEmpty()) {
                    data.setProfilePicUrl(eydmsCustomer.getProfilePicture().getURL());
                } else {
                    data.setProfilePicUrl("");
                }

                if(customerNo!=null)
                {
                    //Date maxDate=djpVisitDao.getLastLiftingDateForRetailerOrInfluencer(eydmsCustomer.getCustomerNo(), brand, transactionType);
                    Date maxDate=djpVisitDao.getLastLiftingDateForRetailerFromOrderReq(eydmsCustomer, brand, transactionType);
                    if(maxDate != null) {
                        data.setLastLiftingDate(maxDate);

                        //Double lastLiftingQuantity= djpVisitDao.getLastLiftingQuantityForRetailerOrInfluencer(eydmsCustomer.getCustomerNo(),brand,maxDate , transactionType);
                        Double lastLiftingQuantity= djpVisitDao.getLastLiftingQuantityForRetailerFromOrderReq(eydmsCustomer,brand,maxDate , transactionType);
                        if (lastLiftingQuantity != null) {
                            data.setLastLiftingQuantity(lastLiftingQuantity);
                        } else {
                            data.setLastLiftingQuantity(0.0);
                        }
                    }
                }

                if (eydmsCustomer.getLastVisitTime() != null) {
                    data.setLastVisitDate(eydmsCustomer.getLastVisitTime());
                }
                if (eydmsCustomer.getNetworkType() != null) {
                    data.setNetworkType(eydmsCustomer.getNetworkType());
                } else {
                    data.setNetworkType("");
                }
                if (eydmsCustomer.getCounterPotential() != null) {
                    data.setPotential(eydmsCustomer.getCounterPotential());
                } else {
                    data.setPotential(0.0);
                }
                Integer djpVisits = djpVisitDao.getVisitCountMTD(eydmsCustomer, month1, year1);
                if (djpVisits != null && djpVisits > 0) {
                    data.setVisits(djpVisits);    //mtd
                } else {
                    data.setVisits(0);
                }
                if (eydmsCustomer.getWholeSale() != null) {
                    data.setWholeSale(eydmsCustomer.getWholeSale());
                } else {
                    data.setWholeSale(0.0);
                }
                if (eydmsCustomer.getRetailSale() != null) {
                    data.setCounterSale(eydmsCustomer.getRetailSale());
                } else {
                    data.setCounterSale(0.0);
                }

                if(customerNo!=null)
                {
                    Double monthlySalesTarget=djpVisitDao.getSalesTargetFor360(customerNo, CounterType.DEALER.getCode(), month1, year1);
                    if (monthlySalesTarget != null && monthlySalesTarget != 0.0 && daysInMonth > 0) {
                        data.setAskingRate(monthlySalesTarget/daysInMonth);
                    } else {
                        data.setAskingRate(0.0);
                    }
                }


                cal.add(Calendar.MONTH, -1);
                int month3 = cal.get(Calendar.MONTH);
                int year3 = cal.get(Calendar.YEAR);

                if(customerNo!=null)
                {
                    //double lastMonthSales = djpVisitDao.getSalesHistoryDataFor360(eydmsCustomer.getCustomerNo(), brand, transactionType, month2, year2);
                    //double secondLastMonthSales = djpVisitDao.getSalesHistoryDataFor360(eydmsCustomer.getCustomerNo(), brand, transactionType, month3, year3);
                    //double lastYearSameMonthSales = djpVisitDao.getSalesHistoryDataFor360(eydmsCustomer.getCustomerNo(), brand, transactionType, month1, year1-1);

                    double lastMonthSales = networkDao.getSalesQuantityForRetailerMonthYear(eydmsCustomer,brand,month2,year2);
                    double secondLastMonthSales =networkDao.getSalesQuantityForRetailerMonthYear(eydmsCustomer,brand,month3,year3);
                    double lastYearSameMonthSales =networkDao.getSalesQuantityForRetailerMonthYear(eydmsCustomer,brand,month1,year1-1);

                    if(lastMonthSales !=0.0 && secondLastMonthSales!=0.0) {
                        double growth = ((lastMonthSales - secondLastMonthSales) / secondLastMonthSales) * 100;
                        if (growth != 0.0) {
                            data.setGrowth(growth);
                        } else {
                            data.setGrowth(0.0);
                        }
                    }
                    if (lastMonthSales != 0.0) {
                        data.setLastMonthSales(lastMonthSales);
                    } else {
                        data.setLastMonthSales(0.0);
                    }
                    if (lastYearSameMonthSales != 0.0) {
                        data.setLastYearCurrentMonthSales(lastYearSameMonthSales);
                    } else {
                        data.setLastYearCurrentMonthSales(0.0);
                    }
                }

                List<List<Object>> currentMonthCounterShareInfo = djpVisitDao.getCounterSharesForDealerOrRetailer(eydmsCustomer.getPk().toString(), month1, year1);
                try {
                    if (currentMonthCounterShareInfo != null && currentMonthCounterShareInfo.size() > 0) {
                        List<Object> currentMonthCounterShareInfoList = currentMonthCounterShareInfo.get(0);
                        if (currentMonthCounterShareInfoList != null && currentMonthCounterShareInfoList.size() > 0 && currentMonthCounterShareInfoList.get(0) != null && currentMonthCounterShareInfoList.get(1) != null) {
                            Double numerator = (Double)currentMonthCounterShareInfoList.get(0);
                            Double denominator = Double.valueOf((Integer)currentMonthCounterShareInfoList.get(1));
                            Double currentMonthCounterShare = (numerator !=null && denominator != null )?(numerator/denominator):0.0;
                            if (currentMonthCounterShare != 0.0) {
                                data.setCurrentMonthCounterShare(currentMonthCounterShare);
                            } else {
                                data.setCurrentMonthCounterShare(0.0);
                            }
                        }
                    }
                }catch(Exception e)
                {
                    data.setCurrentMonthCounterShare(0.0);
                }

                List<List<Object>> lastMonthCounterShareInfo = djpVisitDao.getCounterSharesForDealerOrRetailer(eydmsCustomer.getPk().toString(), month2, year2);
                try {
                    if (lastMonthCounterShareInfo != null && lastMonthCounterShareInfo.size() > 0) {
                        List<Object> lastMonthCounterShareInfoList = lastMonthCounterShareInfo.get(0);
                        if (lastMonthCounterShareInfoList != null && lastMonthCounterShareInfoList.size() > 0 && lastMonthCounterShareInfoList.get(0) != null && lastMonthCounterShareInfoList.get(1) != null) {
                            Double numerator = (Double)lastMonthCounterShareInfoList.get(0);
                            Double denominator = Double.valueOf((Integer)lastMonthCounterShareInfoList.get(1));
                            Double lastMonthCounterShare = (numerator!=null && denominator != null )?(numerator/denominator):0.0;
                            if (lastMonthCounterShare != 0.0) {
                                data.setLastMonthCounterShare(lastMonthCounterShare);
                            } else {
                                data.setLastMonthCounterShare(0.0);
                            }
                        }
                    }
                }catch(Exception e)
                {
                    data.setLastMonthCounterShare(0.0);
                }

                UserModel user = userService.getCurrentUser();
                userService.setCurrentUser(eydmsCustomer);
                Integer influencerNetwork = territoryManagementService.getInfluencerCountForRetailer();
                data.setInfluencerNetwork((influencerNetwork != null && influencerNetwork != 0) ? influencerNetwork : 0);
                userService.setCurrentUser(user);

                //last 6 months sale for retailer
                List<MonthlySalesData> dataList = new ArrayList<>();
                getLastSixMonthSalesForRetailer(dataList,eydmsCustomer);
                MonthlySalesListData salesListData=new MonthlySalesListData();
                if (dataList != null && !dataList.isEmpty()) {
                    salesListData.setSales(dataList);
                }
                data.setSalesData(salesListData);
            }
        }catch(Exception e)
        {
            LOG.error("DJPVisitServiceImpl: populated--> Error Message:" + e.getMessage() + " Caused : " + e.getCause() + " Error stack Trace::" +  e.getStackTrace());
        }

        return data;
    }

    @Override
    public InfluencerSummaryData getInfluencerSummary(String counterVisitId) {

        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);

        InfluencerSummaryData data = new InfluencerSummaryData();
        CounterVisitMasterModel counterVisitModel = counterVisitDao.findCounterVisitById(counterVisitId);

        BaseSiteModel brand = baseSiteService.getCurrentBaseSite();
        String transactionType = EyDmsCoreConstants.DJP.INFLEUNCER_TRANSACTION_TYPE;
        try {
            data.setSales(counterVisitModel.getTotalSale());
            EyDmsCustomerModel eydmsCustomer = counterVisitModel.getEyDmsCustomer();
            if(Objects.nonNull(eydmsCustomer)) {
                int visits = djpVisitDao.getVisitCountMTD(eydmsCustomer, month, year);
                data.setVisits(visits);
                data.setPotential(eydmsCustomer.getCounterPotential());
                data.setLastVisitDate(eydmsCustomer.getLastVisitTime());

                //Date maxDate=djpVisitDao.getLastLiftingDateForRetailerOrInfluencer(eydmsCustomer.getNirmanMitraCode(), brand, transactionType);
                Date maxDate=djpVisitDao.getLastLiftingDateForInfluencerFromPointReq(eydmsCustomer, brand, transactionType);
                if(maxDate != null) {
                    data.setLastLiftingDate(maxDate);

                    // Double lastLiftingQuantity= djpVisitDao.getLastLiftingQuantityForRetailerOrInfluencer(eydmsCustomer.getNirmanMitraCode(),brand,maxDate , transactionType);
                    Double lastLiftingQuantity= djpVisitDao.getLastLiftingQuantityForInfluencerFromPointReq(eydmsCustomer,brand,maxDate , transactionType);
                    data.setLastLiftingQuantity(lastLiftingQuantity);

                    cal = Calendar.getInstance();
                    cal.add(Calendar.MONTH, -1);
                    Date date = cal.getTime();
                    cal.add(Calendar.MONTH, -2);
                    Date date2 = cal.getTime();

                    if(data.getLastLiftingDate()!=null)
                    {
                        if(date.compareTo(data.getLastLiftingDate())<0)
                            data.setNetworkType(NetworkType.ACTIVE.getCode());
                        else if(date2.compareTo(data.getLastLiftingDate())<0)
                            data.setNetworkType(NetworkType.INACTIVE.getCode());
                        else
                            data.setNetworkType(NetworkType.DORMANT.getCode());
                    }
                }
            }
        }catch(NullPointerException e)
        {
            LOG.info("CounterVisitModel not found");
        }

        return data;
    }

    @Override
    public CounterVisitMasterModel getCounterVisitMasterForLastVisitDate(String counterVisitId) {
        CounterVisitMasterModel counterVisitModel = counterVisitDao.findCounterVisitById(counterVisitId);
        if(counterVisitModel!=null) {
            if (null != counterVisitModel.getEyDmsCustomer()) {
                return counterVisitModel.getEyDmsCustomer().getLastCounterVisit();
            }
            else{
                throw new ModelNotFoundException("EyDmsCustomer not found");
            }
        }
        else
        {
            throw new ModelNotFoundException("CounterVisitMaster for ID:" + counterVisitId + " not found");
        }
    }

    @Override
    public List<DealersFleetDetailsModel> getDealerFleetDetails(String counterVisitId) {

        CounterVisitMasterModel counterVisitModel = getCounterVisitMasterForLastVisitDate(counterVisitId);
        if(counterVisitModel!=null)
        {
            List<DealersFleetDetailsModel> list = counterVisitModel.getEyDmsCustomer()!=null ? (List<DealersFleetDetailsModel>) counterVisitModel.getEyDmsCustomer().getDealerFleetDetails() : Collections.emptyList();
            return list;
        }
        return Collections.emptyList();
    }

    @Override
    public String calculatePlanComplianceForSODJP(EyDmsUserModel eydmsUserModel , final Date planStartDate, final Date planEndDate){

        BigDecimal recommendedCompletedVisitCount = BigDecimal.ZERO;
        BigDecimal recommendedPlannedVisitCount = BigDecimal.ZERO;

        List<VisitMasterModel> completedVisits = djpVisitDao.getCompletedPlannedVisitsBetweenDatesForSO(eydmsUserModel,planStartDate,planEndDate);
        if(CollectionUtils.isNotEmpty(completedVisits)){
            for(VisitMasterModel visitMasterModel : completedVisits){
                if(visitMasterModel.getRouteScore()!=null) {
                    List<DJPCounterScoreMasterModel> counterScoreList = visitMasterModel.getRouteScore().getCounterScore();
                    List<EyDmsCustomerModel> recommendedCustomer = counterScoreList.stream().filter(each->each.getObjective()!=null && each.getObjective().equals(visitMasterModel.getObjective())).map(each -> each.getCustomer()).collect(Collectors.toList());
                    recommendedPlannedVisitCount = recommendedPlannedVisitCount.add(BigDecimal.valueOf(recommendedCustomer.size()));
                    Collection<CounterVisitMasterModel> counterVisitList = visitMasterModel.getCounterVisits();
                    long countOfCompleted = counterVisitList.stream().filter(each->each.getEndVisitTime()!=null && recommendedCustomer!=null && recommendedCustomer.contains(each.getEyDmsCustomer())).map(each -> each.getEyDmsCustomer()).count();
                    recommendedCompletedVisitCount = recommendedCompletedVisitCount.add(BigDecimal.valueOf(countOfCompleted));
                }
            }
        }
        return calculatePlanCompliancePercentage(recommendedPlannedVisitCount,recommendedCompletedVisitCount);
    }

    private String calculatePlanCompliancePercentage(BigDecimal recommendedPlannedVisitCount, BigDecimal recommendedCompletedVisitCount) {
        if(recommendedPlannedVisitCount.compareTo(BigDecimal.ZERO) == 0){
            return String.valueOf(0);
        }
        return recommendedCompletedVisitCount.multiply(BigDecimal.valueOf(100)).divide(recommendedPlannedVisitCount,2,RoundingMode.HALF_EVEN).stripTrailingZeros().toPlainString();
    }

    @Override
    public EyDmsUserModel getCurrentSalesOfficer(){
        EyDmsUserModel eydmsUserModel = null;
        try{
            eydmsUserModel = (EyDmsUserModel) userService.getCurrentUser();
        }
        catch (ClassCastException ex){
            LOG.error("Error Occured while getting current sales officer");
            LOG.error("Exception is : "+ex.getMessage());
        }
        return eydmsUserModel;
    }

    @Override
    public List<B2BCustomerModel> getInfluencerDetails(String counterVisitId) {
        CounterVisitMasterModel counterVisitModel = getCounterVisitMasterForLastVisitDate(counterVisitId);
        List<B2BCustomerModel> siteList =  (List<B2BCustomerModel>) counterVisitModel.getEyDmsCustomer().getTaggedPartners().stream().filter(o->o.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.SITE_USER_GROUP_UID))).collect(Collectors.toList());
        return siteList;
    }

    private void setOutStandingBuckets(DealerSummaryData data, String customerId)
    {
        List<List<Double>> bucketList = djpVisitDao.getOutstandingBucketsForDealer(customerId);
        if(!bucketList.isEmpty()&&!Objects.isNull(bucketList))
        {
            try {
                data.setBucket1(bucketList.get(0).get(0)!=null?bucketList.get(0).get(0):0.0);
                data.setBucket2(bucketList.get(0).get(1)!=null?bucketList.get(0).get(1):0.0);
                data.setBucket3(bucketList.get(0).get(2)!=null?bucketList.get(0).get(2):0.0);
                data.setBucket4(bucketList.get(0).get(3)!=null?bucketList.get(0).get(3):0.0);
                data.setBucket5(bucketList.get(0).get(4)!=null?bucketList.get(0).get(4):0.0);
                data.setBucket6(bucketList.get(0).get(5)!=null?bucketList.get(0).get(5):0.0);
                data.setBucket7(bucketList.get(0).get(6)!=null?bucketList.get(0).get(6):0.0);
                data.setBucket8(bucketList.get(0).get(7)!=null?bucketList.get(0).get(7):0.0);
                data.setBucket9(bucketList.get(0).get(8)!=null?bucketList.get(0).get(8):0.0);
                data.setBucket10(bucketList.get(0).get(9)!=null?bucketList.get(0).get(9):0.0);
            }catch(Exception e)
            {
                LOG.info(e);
            }

        }
        else
        {
            data.setBucket1(0.0);
            data.setBucket2(0.0);
            data.setBucket3(0.0);
            data.setBucket4(0.0);
            data.setBucket5(0.0);
            data.setBucket6(0.0);
            data.setBucket7(0.0);
            data.setBucket8(0.0);
            data.setBucket9(0.0);
            data.setBucket10(0.0);
        }
    }

    @Override
    public Collection<CounterVisitMasterModel> getTodaysPlan() {
        Collection<CounterVisitMasterModel> counterVisitList = new ArrayList<>();
        List<VisitMasterModel> list = djpVisitDao.getPlannedVisitForToday(userService.getCurrentUser(), LocalDate.now().toString());
        if(list!=null && !list.isEmpty()) {
            for(VisitMasterModel visit : list) {
                if(visit.getCounterVisits()!=null && !visit.getCounterVisits().isEmpty()) {
                    counterVisitList.addAll(visit.getCounterVisits());
                }
            }
        }
        return counterVisitList;
    }

    @Override
    public String getRouteForId(String id) {
        DJPRouteScoreMasterModel djpRoute = djpRouteScoreDao.findByRouteScoreId(id);
        return djpRoute.getRoute().getRouteId();
    }

    @Override
    public DropdownListData getListOfRoutes(List<String> subAreas) {
        BaseSiteModel site = baseSiteService.getCurrentBaseSite();
        List<SubAreaMasterModel> subAreaList = new ArrayList<SubAreaMasterModel>();
        if(subAreas!=null && !subAreas.isEmpty()) {
            for(String subArea : subAreas) {
                subAreaList.add(territoryManagementService.getTerritoryById(subArea));
            }
        }else {
            FilterTalukaData filterTalukaData = new FilterTalukaData();
            subAreaList = territoryManagementService.getTaulkaForUser(filterTalukaData);
        }

        List<List<Object>> list = djpVisitDao.getAllRoutesForSO(subAreaList);
        List<DropdownData> dropdownList = new ArrayList<DropdownData>();
        list.forEach(obj -> {
            if(obj!=null && !obj.isEmpty()) {
                String routeId = (String) obj.get(0);
                String routeName = obj.size()>1 ? (String) obj.get(1) : routeId;
                DropdownData data = new DropdownData();
                data.setCode(routeId);
                data.setName(routeName);
                dropdownList.add(data);
            }
        });
        DropdownListData listData = new DropdownListData();
        listData.setDropdown(dropdownList);
        return listData;
    }

    @Override
    public List<RouteMasterModel> getRouteMasterList(String plannedDate, String subAreaMasterPk, List<RouteMasterModel> recommendedRoute) {
        SubAreaMasterModel subAreaMaster = territoryManagementService.getTerritoryById(subAreaMasterPk);
        BaseSiteModel site = baseSiteService.getCurrentBaseSite();
        List<RouteMasterModel> routeMasterList = routeMasterDao.findBySubAreaAndBrand(subAreaMaster, site);
        List<RouteMasterModel> output = new ArrayList<>();
        DJPRunMasterModel model = djpRunDao.findByPlannedDateAndUser(plannedDate, subAreaMaster.getDistrict(), subAreaMaster.getTaluka(), site.getUid());
        if(model!=null) {
            RouteMasterModel recommendedRoute1 = model.getRecommendedRoute1();
            RouteMasterModel recommendedRoute2 = model.getRecommendedRoute2();
            if(recommendedRoute1!=null) {
                recommendedRoute.add(recommendedRoute1);
                output.add(recommendedRoute1);
            }
            if(recommendedRoute2!=null) {
                recommendedRoute.add(recommendedRoute2);
                output.add(recommendedRoute2);
            }
        }
        output.addAll(routeMasterList.stream().filter(route-> !output.contains(route)).collect(Collectors.toList()));

        return output;
    }

    @Override
    public Integer flaggedDealerCount() {
        FilterTalukaData filterTalukaData=new FilterTalukaData();
        List<SubAreaMasterModel> taulkaForUser = territoryManagementService.getTaulkaForUser(filterTalukaData);
        return djpVisitDao.flaggedDealerCount(taulkaForUser);
    }

    @Override
    public Integer unFlaggedDealerRequestCount() {
        FilterTalukaData filterTalukaData=new FilterTalukaData();
        List<SubAreaMasterModel> taulkaForUser = territoryManagementService.getTaulkaForUser(filterTalukaData);
        return djpVisitDao.unFlaggedDealerRequestCount(taulkaForUser);
    }

    @Override
    public boolean updateUnFlagRequestApprovalByTSM(UnFlagRequestApprovalData unFlagRequestApprovalData) {
        EyDmsUserModel currentUser = (EyDmsUserModel) userService.getCurrentUser();
        if (unFlagRequestApprovalData != null) {
            if (unFlagRequestApprovalData.getApprovalStatus().equals(Boolean.TRUE) &&
                    unFlagRequestApprovalData.getDealerCode() != null) {
                try{
                    EyDmsCustomerModel eydmsCustomerModel = (EyDmsCustomerModel) userService.getUserForUID(unFlagRequestApprovalData.getDealerCode());
                    if(eydmsCustomerModel!=null) {
                        if(eydmsCustomerModel.getIsDealerFlag()) {
                            eydmsCustomerModel.setIsUnFlagRequestRaised(false);
                            eydmsCustomerModel.setIsDealerFlag(false);
                            eydmsCustomerModel.setUnflagTime(new Date());
                            eydmsCustomerModel.setUnFlaggedBy(currentUser);
                            eydmsCustomerModel.setUnFlagApprovalStatus(NetworkAdditionStatus.APPROVED_BY_TSM);
                            modelService.save(eydmsCustomerModel);
                        }
                    }
                   /* CounterVisitMasterModel counterVisitModel = counterVisitDao.findCounterVisitById(unFlagRequestApprovalData.getCounterVisitId());
                    if(counterVisitModel!=null) {
                        if (counterVisitModel.getIsDealerFlag().equals(Boolean.TRUE) && counterVisitModel.getIsUnFlagRequestRaised().equals(Boolean.TRUE)){
                        counterVisitModel.setIsUnFlagRequestRaised(false);
                        counterVisitModel.setIsDealerFlag(false);
                        counterVisitModel.setUnflagTime(new Date());
                        counterVisitModel.setUnFlaggedBy(currentUser);
                        counterVisitModel.setUnFlagApprovalStatus(NetworkAdditionStatus.APPROVED_BY_TSM);
                        modelService.save(counterVisitModel);
                        }
                    }*/
                } catch (Exception e) {
                    LOG.info(e.getMessage());
                }
            }
            return true;
        }
        else{
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public VisitMasterModel updateStatusForApprovalByTsm(String visitId) {

        return (VisitMasterModel) sessionService.executeInLocalView(new SessionExecutionBody()
        {
            @Override
            public VisitMasterModel execute()
            {

                try {
                    searchRestrictionService.disableSearchRestrictions();

                    return djpVisitDao.updateStatusForApprovalByTsm(visitId);
                }
                finally
                {
                    searchRestrictionService.enableSearchRestrictions();
                }
            }

        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public VisitMasterModel updateStatusForRejectedByTsm(String visitId) {
        return (VisitMasterModel) sessionService.executeInLocalView(new SessionExecutionBody() {
            @Override
            public VisitMasterModel execute() {

                try {
                    searchRestrictionService.disableSearchRestrictions();

                    return djpVisitDao.updateStatusForRejectedByTsm(visitId);
                } finally {
                    searchRestrictionService.enableSearchRestrictions();
                }
            }

        });
    }
    @Override
    public Double getTotalOrderGenerated(String siteCode, String counterVisitId) {
        CounterVisitMasterModel counterVisit=counterVisitDao.findCounterVisitById(counterVisitId);
        EyDmsCustomerModel eydmsCustomer= (EyDmsCustomerModel) userService.getUserForUID(siteCode);
        return djpVisitDao.getTotalOrderGenerated(eydmsCustomer,counterVisit);
    }



    @Override
    public SalesHistoryData getSalesHistoryForDealer(String counterVisitId) {
        CounterVisitMasterModel counterVisitModel = counterVisitDao.findCounterVisitById(counterVisitId);
        String customerNo = counterVisitModel.getEyDmsCustomer().getCustomerNo();

        BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
        EyDmsCustomerModel customer = counterVisitModel.getEyDmsCustomer();

        CustomerCategory category = CustomerCategory.TR;

        SalesHistoryData data= new SalesHistoryData();
        LocalDate date=LocalDate.now();
        LocalDate lastMonth=date.minusMonths(1);
        LocalDate lastTolastMonth = date.minusMonths(2);
        LocalDate lastYearSameMonth = date.minusYears(1);
        double lastYearCurrentMonth=0.0,salesMtd=0.0;

        double lastMonthSales = salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYearForDealer(customer, currentBaseSite, lastMonth.getMonthValue(), lastMonth.getYear(),null);
        //double lastMonthSales = djpVisitDao.getSalesHistoryDataForDealer(customerNo, lastMonth.getMonthValue(), lastMonth.getYear(), category, currentBaseSite);

        //double secondLastMonthSales = djpVisitDao.getSalesHistoryDataForDealer(customerNo, lastTolastMonth.getMonthValue(), lastTolastMonth.getYear(), category, currentBaseSite);
        double secondLastMonthSales = salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYearForDealer(customer, currentBaseSite, lastTolastMonth.getMonthValue(), lastTolastMonth.getYear(),null);
        data.setLastMonthSales(lastMonthSales);
        lastYearCurrentMonth = salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYearForDealer(customer, currentBaseSite, lastYearSameMonth.getMonthValue(), lastYearSameMonth.getYear(), null);
        data.setLastYearCurrentMonthSales(lastYearCurrentMonth!=0.0?lastYearCurrentMonth:0.0);
        //data.setLastYearCurrentMonthSales(djpVisitDao.getSalesHistoryDataForDealer(customerNo, lastYearSameMonth.getMonthValue(), lastYearSameMonth.getYear(), category, currentBaseSite));
        salesMtd = salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYearForDealer(customer, currentBaseSite, date.getMonthValue(), date.getYear(), null);
        data.setSalesMTD(salesMtd!=0.0?salesMtd:0.0);
        //data.setSalesMTD(djpVisitDao.getSalesHistoryDataForDealer(customerNo, date.getMonthValue(), date.getYear(), category, currentBaseSite));

        if(lastMonthSales !=0.0 && secondLastMonthSales!=0.0) {
            double growth = ((lastMonthSales - secondLastMonthSales) / secondLastMonthSales) * 100;
            if (growth != 0.0)
                data.setGrowth(growth);
            else
                data.setGrowth(0.0);
        }
        else
        {
            data.setGrowth(0.0);
        }

        //Double actualMonthSale = djpVisitDao.getSalesHistoryDataForDealer(customerNo, date.getMonthValue(), date.getYear(), category, currentBaseSite);
        double actualMonthSale = salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYearForDealer(customer, currentBaseSite, date.getMonthValue(), date.getYear(), null);
        Double monthlySalesTarget=0.0;
        if(customerNo!=null) {
            monthlySalesTarget = djpVisitDao.getSalesTargetFor360(customerNo, CounterType.DEALER.getCode(), date.getMonthValue(), date.getYear());
        }

        Calendar cal=Calendar.getInstance();
        int noOfDaysOfTheMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int pastDay = cal.get(Calendar.DAY_OF_MONTH) - 1;
        int remainingDays = noOfDaysOfTheMonth - pastDay;

        double askingRate = (monthlySalesTarget - actualMonthSale) / remainingDays;
        if(askingRate != 0.0) {
            data.setAskingRate(askingRate);
        }
        else
        {
            data.setAskingRate(0.0);
        }
        data.setWholeSale(customer.getWholeSale());
        data.setCounterSale(customer.getRetailSale());
        return data;
    }

    @Override
    public MarketVisitDetailsData getMarketVisitDetailsData(final String visitId){
        MarketVisitDetailsData marketVisitDetailsData = new MarketVisitDetailsData();
        VisitMasterModel visitMasterModel = visitMasterDao.findById(visitId);

        if(null!= visitMasterModel.getStartVisitTime()){
            DateFormat dateFormat = new SimpleDateFormat(EyDmsCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
            marketVisitDetailsData.setDateOfJourney(dateFormat.format(visitMasterModel.getStartVisitTime()));
        }
        if(null!= visitMasterModel.getRoute()){
            populateRouteInMarketVisitDetails(marketVisitDetailsData,visitMasterModel.getRoute());
        }
        if(null!= visitMasterModel.getObjective()){
            populateObjectiveInMarketVisitDetails(marketVisitDetailsData,visitMasterModel.getObjective());
        }
        populateDeviationsInMarketVisitDetailsData(marketVisitDetailsData,visitMasterModel, false);

        populateCountersNotVisitedData(marketVisitDetailsData,visitMasterModel);

        populateAdHocVisitsData(marketVisitDetailsData,visitMasterModel);

        return marketVisitDetailsData;
    }
    @Override
    public void saveVisitSummary(final VisitSummaryData visitSummaryData){

        validateParameterNotNullStandardMessage("visitSummaryData", visitSummaryData);
        VisitMasterModel visitMasterModel = visitMasterDao.findById(visitSummaryData.getVisitID());
        if(null== visitMasterModel){
            throw new ModelNotFoundException("Visit not found with ID: "+visitSummaryData.getVisitID());
        }
        if(StringUtils.isNotBlank(visitSummaryData.getRouteDeviationReason())){
            visitMasterModel.setRouteDeviationReason(visitSummaryData.getRouteDeviationReason());
        }
        if(StringUtils.isNotBlank(visitSummaryData.getRouteDeviationComment())){
            visitMasterModel.setRouteDeviationComment(visitSummaryData.getRouteDeviationComment());
        }
        if(StringUtils.isNotBlank(visitSummaryData.getObjectiveDeviationReason())){
            visitMasterModel.setObjectiveDeviationReason(visitSummaryData.getObjectiveDeviationReason());
        }
        if(StringUtils.isNotBlank(visitSummaryData.getObjectiveDeviationComment())){
            visitMasterModel.setObjectiveDeviationComment(visitSummaryData.getObjectiveDeviationComment());
        }

        saveCounterVisitDetails(visitSummaryData);
        modelService.save(visitMasterModel);
    }

    private void saveCounterVisitDetails(VisitSummaryData visitSummaryData) {
        if(CollectionUtils.isNotEmpty(visitSummaryData.getCounterDeviationReason())){
            for(CounterDetailsData counterDetailsData : visitSummaryData.getCounterDeviationReason()){
                CounterVisitMasterModel counterVisitMasterModel = counterVisitDao.findCounterVisitById(counterDetailsData.getCounterVisitId());
                if(null == counterVisitMasterModel){
                    LOG.error("Could Not Find Counter Visit With ID: "+counterVisitMasterModel.getPk().toString());
                    throw new ModelNotFoundException("Could Not Find Counter Visit With ID: "+counterVisitMasterModel.getPk().toString());
                }
                counterVisitMasterModel.setDeviationReason(counterDetailsData.getDeviationReason());
                counterVisitMasterModel.setDeviationComment(counterDetailsData.getDeviationComment());
                modelService.save(counterVisitMasterModel);
            }
        }
    }

    private void populateAdHocVisitsData(MarketVisitDetailsData marketVisitDetailsData, VisitMasterModel visitMasterModel) {
        if(CollectionUtils.isEmpty(visitMasterModel.getCounterVisits())){
            throw new ModelNotFoundException("No Counter Visits assigned to current visit: "+visitMasterModel.getPk());
        }
        List<CounterDetailsData> adHocCountersDetailsList = new ArrayList<>();
        Collection<CounterVisitMasterModel> adHocCountersVisitedList = visitMasterModel.getCounterVisits().stream().filter(counter -> (null == counter.getIsAdHoc() || counter.getIsAdHoc()) && null != counter.getEndVisitTime()).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(adHocCountersVisitedList)){
            for(CounterVisitMasterModel counterVisitMasterModel: adHocCountersVisitedList ){
                CounterDetailsData adHocCounterVisitedData = new CounterDetailsData();
                adHocCounterVisitedData.setCounterVisitId(counterVisitMasterModel.getPk().toString());
                adHocCounterVisitedData.setCustomerName(null!= counterVisitMasterModel.getEyDmsCustomer()?counterVisitMasterModel.getEyDmsCustomer().getName():StringUtils.EMPTY);
                adHocCounterVisitedData.setCustomerCode(null!= counterVisitMasterModel.getEyDmsCustomer()?counterVisitMasterModel.getEyDmsCustomer().getUid():StringUtils.EMPTY);
                adHocCounterVisitedData.setCustomerType(getCustomerType(counterVisitMasterModel.getEyDmsCustomer()));

                adHocCountersDetailsList.add(adHocCounterVisitedData);
            }
        }
        marketVisitDetailsData.setAdHocVisits(adHocCountersDetailsList);
    }

    private void populateCountersNotVisitedData(MarketVisitDetailsData marketVisitDetailsData, VisitMasterModel visitMasterModel) {
        if(CollectionUtils.isEmpty(visitMasterModel.getCounterVisits())){
            throw new ModelNotFoundException("No Counter Visits assigned to current visit: "+visitMasterModel.getPk());
        }
        List<CounterDetailsData> counterNotVisitedList = new ArrayList<>();
        Collection<CounterVisitMasterModel> countersNotVisited = visitMasterModel.getCounterVisits().stream().filter(counter -> !(null != counter.getIsAdHoc() && counter.getIsAdHoc()) && null == counter.getEndVisitTime()).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(countersNotVisited)){
            for(CounterVisitMasterModel counterVisitMasterModel: countersNotVisited ){
                CounterDetailsData counterNotVisitedData = new CounterDetailsData();
                counterNotVisitedData.setCounterVisitId(counterVisitMasterModel.getPk().toString());
                counterNotVisitedData.setCustomerName(null!= counterVisitMasterModel.getEyDmsCustomer()?counterVisitMasterModel.getEyDmsCustomer().getName():StringUtils.EMPTY);
                counterNotVisitedData.setCustomerCode(null!= counterVisitMasterModel.getEyDmsCustomer()?counterVisitMasterModel.getEyDmsCustomer().getUid():StringUtils.EMPTY);
                counterNotVisitedData.setCustomerType(getCustomerType(counterVisitMasterModel.getEyDmsCustomer()));
                counterNotVisitedList.add(counterNotVisitedData);
            }
        }
        marketVisitDetailsData.setCountersNotVisited(counterNotVisitedList);
    }

    private void populateDeviationsInMarketVisitDetailsData(MarketVisitDetailsData marketVisitDetailsData, VisitMasterModel visitMasterModel, boolean journeyDetails) {
        List<MarketVisitDeviationData> marketVisitDeviationDataList = new ArrayList<>();
        String routeAdherence="YES", objectiveAdherence="YES", counterAdherence="YES";
        if(visitMasterModel.getRouteScore()!=null) {
            MarketVisitDeviationData routeDeviationData = getRouteDeviationData(visitMasterModel, journeyDetails);
            if(null!= routeDeviationData){
                if(!routeDeviationData.getSuggested().contains(routeDeviationData.getSelected().get(0))) {
                    routeAdherence = "NO";
                }
                marketVisitDeviationDataList.add(routeDeviationData);
            }

            MarketVisitDeviationData objectiveDeviationData = getObjectiveDeviationData(visitMasterModel, journeyDetails);
            if(null!= objectiveDeviationData){
                if(!objectiveDeviationData.getSuggested().contains(objectiveDeviationData.getSelected().get(0))) {
                    objectiveAdherence = "NO";
                }
                marketVisitDeviationDataList.add(objectiveDeviationData);
            }

            MarketVisitDeviationData counterDeviationData = getCounterDeviationData(visitMasterModel, journeyDetails);
            if(null!= counterDeviationData){
                if(!counterDeviationData.getSuggested().contains(counterDeviationData.getSelected().get(0))) {
                    counterAdherence = "NO";
                }
                marketVisitDeviationDataList.add(counterDeviationData);
            }

            marketVisitDetailsData.setRouteAdherence(routeAdherence);
            marketVisitDetailsData.setObjectiveAdherence(objectiveAdherence);
            marketVisitDetailsData.setCounterAdherence(counterAdherence);
            marketVisitDetailsData.setDeviations(marketVisitDeviationDataList);
        }
    }

    @Override
    public MarketVisitDeviationData getCounterDeviationData(VisitMasterModel visitMasterModel, boolean journeyDetails) {

        final String routeScoreId = null!= visitMasterModel.getRouteScore()? String.valueOf(visitMasterModel.getRouteScore().getId()):null;
        final String objectiveId = null!= visitMasterModel.getObjective()? visitMasterModel.getObjective().getObjectiveId():null;

        if(null == routeScoreId || null == objectiveId){
            throw new ModelNotFoundException(String.format("RouteScore %s Id and objective id %s can't be null",routeScoreId,objectiveId));
        }
        List<DJPCounterScoreMasterModel> recommendedList  = getDJPCounterScores(routeScoreId, objectiveId);

        List<EyDmsCustomerModel> recommendedCounters = recommendedList.stream().map(DJPCounterScoreMasterModel::getCustomer).collect(Collectors.toList());

        List<EyDmsCustomerModel> plannedCustomer = visitMasterModel.getCounterVisits().stream().map(CounterVisitMasterModel::getEyDmsCustomer).collect(Collectors.toList());

        List<EyDmsCustomerModel> deviationCounters = recommendedCounters.stream().filter(counter -> !plannedCustomer.contains(counter)).collect(Collectors.toList());

        if(journeyDetails || CollectionUtils.isNotEmpty(deviationCounters)){
            MarketVisitDeviationData counterDeviationData = new MarketVisitDeviationData();
            counterDeviationData.setSuggested(recommendedCounters.stream().filter(data->data!=null).map(EyDmsCustomerModel::getName).collect(Collectors.toList()));
            List<String> selectedList = new ArrayList<String>();
            for(EyDmsCustomerModel customer: plannedCustomer) {
                selectedList.add(customer.getName());
            }
            counterDeviationData.setSelected(selectedList);
            counterDeviationData.setName("Counter Deviation");
            return counterDeviationData;
        }
        else{
            return null;
        }

    }

    @Override
    public MarketVisitDeviationData getRouteDeviationData(VisitMasterModel visitMasterModel, boolean journeyDetails) {

        if(null== visitMasterModel.getRouteScore()){
            throw new ModelNotFoundException("No RouteScore attached to current visit: "+visitMasterModel.getPk());
        }
        DJPRouteScoreMasterModel selectedRouteScore = visitMasterModel.getRouteScore();

        DJPRunMasterModel djpRunMasterModel = selectedRouteScore.getRun();
        List<DJPRouteScoreMasterModel> allRouteScoreInThisRun = djpRunMasterModel.getRouteScore();

        if(CollectionUtils.isEmpty(allRouteScoreInThisRun)){
            throw new ModelNotFoundException("No RouteScore attached to selected Run: "+djpRunMasterModel.getId());
        }
        RouteMasterModel recommendedRoute1 = djpRunMasterModel.getRecommendedRoute1();
        RouteMasterModel recommendedRoute2 = djpRunMasterModel.getRecommendedRoute2();
        RouteMasterModel selectedRouteMaster = selectedRouteScore.getRoute();

        if(journeyDetails || !((recommendedRoute1!=null && selectedRouteMaster.getRouteId().equalsIgnoreCase(recommendedRoute1.getRouteId()))||(recommendedRoute2!=null && selectedRouteMaster.getRouteId().equalsIgnoreCase(recommendedRoute2.getRouteId()))) ){
            MarketVisitDeviationData routeDeviationData = new MarketVisitDeviationData();
            routeDeviationData.setName("Route Deviation");
            List<String> suggestedList = new ArrayList<String>();
            if(recommendedRoute1!=null) {
                suggestedList.add(djpRunMasterModel.getRecommendedRoute1().getRouteName());
            }
            if(recommendedRoute2!=null) {
                suggestedList.add(djpRunMasterModel.getRecommendedRoute2().getRouteName());
            }
            String routeName = "";
            List<String> selectedList = new ArrayList<String>();
            if(selectedRouteScore.getRoute()!=null) {
                routeName = selectedRouteScore.getRoute().getRouteName()!=null? selectedRouteScore.getRoute().getRouteName(): selectedRouteScore.getRoute().getRouteId();
            }
            selectedList.add(routeName);
            routeDeviationData.setSuggested(suggestedList);
            routeDeviationData.setSelected(selectedList);
            return routeDeviationData;
        }
        return null;
    }

    @Override
    public MarketVisitDeviationData getObjectiveDeviationData(VisitMasterModel visitMasterModel, boolean journeyDetails) {
        DJPRouteScoreMasterModel djpRouteScoreMasterModel = visitMasterModel.getRouteScore();
        final ObjectiveModel recommendedObj1 = djpVisitDao.findOjectiveById(djpRouteScoreMasterModel.getRecommendedObj1());
        final ObjectiveModel recommendedObj2 = djpVisitDao.findOjectiveById(djpRouteScoreMasterModel.getRecommendedObj2());
        final ObjectiveModel selectedObjective = visitMasterModel.getObjective();
        if(null == selectedObjective){
            throw new ModelNotFoundException("No Objective assigned to current visit: "+visitMasterModel.getPk());
        }
        if(journeyDetails || !((recommendedObj1!=null && selectedObjective.getObjectiveId().equalsIgnoreCase(recommendedObj1.getObjectiveId()))||(recommendedObj2!=null && selectedObjective.getObjectiveId().equalsIgnoreCase(recommendedObj2.getObjectiveId()))) ){
            MarketVisitDeviationData marketVisitDeviationData = new MarketVisitDeviationData();
            marketVisitDeviationData.setName("Objective deviation");
            List<String> suggestedList = new ArrayList<String>();
            if(recommendedObj1!=null) {
                suggestedList.add(recommendedObj1.getObjectiveName());
            }
            if(recommendedObj2!=null) {
                suggestedList.add(recommendedObj2.getObjectiveName());
            }
            List<String> selectedList = new ArrayList<String>();
            selectedList.add(selectedObjective.getObjectiveName());

            marketVisitDeviationData.setSuggested(suggestedList);
            marketVisitDeviationData.setSelected(selectedList);
            return marketVisitDeviationData;
        }
        else{
            return null;
        }
    }

    private void populateObjectiveInMarketVisitDetails(MarketVisitDetailsData marketVisitDetailsData, ObjectiveModel objective) {
        ObjectiveData objectiveData = new ObjectiveData();
        objectiveData.setId(objective.getObjectiveId());
        objectiveData.setName(objective.getObjectiveName());
        marketVisitDetailsData.setObjective(objectiveData);
    }

    private void populateRouteInMarketVisitDetails(MarketVisitDetailsData marketVisitDetailsData, RouteMasterModel route) {
        RouteData routeData = new RouteData();
        routeData.setId(route.getRouteId());
        routeData.setName(route.getRouteName());
        marketVisitDetailsData.setRoute(routeData);
    }

    @Override
    public void createCounterRouteMapping(String state, String district, String taluka, BaseSiteModel brand, String counterCode,EyDmsUserModel employee, String routeId, String routeName) {
        CounterRouteMappingModel counterRouteMapping = modelService.create(CounterRouteMappingModel.class);
        counterRouteMapping.setState(state);
        counterRouteMapping.setDistrict(district);
        counterRouteMapping.setTaluka(taluka);
        counterRouteMapping.setBrand(brand);
        counterRouteMapping.setEmployeeCode(null!=employee?employee.getEmployeeCode():StringUtils.EMPTY);
        counterRouteMapping.setCounterCode(counterCode);
        counterRouteMapping.setRoute(routeId);
        counterRouteMapping.setRouteName(routeName);
        counterRouteMapping.setChannel("IN_APP");
        counterRouteMapping.setCreatedBy(employee);
        counterRouteMapping.setIsOtherBrand(Boolean.TRUE);
        modelService.save(counterRouteMapping);
    }

    private String getMonthName(int month){
        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sept", "Oct", "Nov", "Dec"};
        return monthNames[month];
    }

    @Override
    public List<MonthlySalesData> getLastSixMonthSalesForDealer(String counterVisitId) {

        List<MonthlySalesData> dataList = new ArrayList<>();

        CounterVisitMasterModel counterVisitModel = counterVisitDao.findCounterVisitById(counterVisitId);
        if (Objects.nonNull(counterVisitModel) && Objects.nonNull(counterVisitModel.getEyDmsCustomer())) {
            EyDmsCustomerModel customer = counterVisitModel.getEyDmsCustomer();
            getMonthlySalesForDealer(dataList, customer);
        }

        return dataList;
    }
    @Override
    public void getMonthlySalesForDealer(List<MonthlySalesData> dataList, EyDmsCustomerModel customer) {
        if (customer != null) {
            String customerNo = customer.getCustomerNo() != null ? customer.getCustomerNo() : " ";

            BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();

            CustomerCategory category = CustomerCategory.TR;

            LocalDate currentMonth = LocalDate.now();
            //var salesOfficer=(EyDmsUserModel) userService.getCurrentUser();
            //var subAreas = territoryManagementService.getTerritoriesForSO();
            for (int i = 1; i <= 6; i++) {
                MonthlySalesData data = new MonthlySalesData();
                //Double currentMonthSale = djpVisitDao.getSalesHistoryData(customerNo, currentMonth.getMonthValue(), currentMonth.getYear(), category, currentBaseSite);
                Double currentMonthSale = salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYearForDealer(customer, currentBaseSite, currentMonth.getMonthValue(), currentMonth.getYear(),null);
                LOG.info("currentMonthSale:"+currentMonthSale);
                LocalDate lastMonth = currentMonth.minusMonths(1);
                //Double lastMonthSale = djpVisitDao.getSalesHistoryData(customerNo, lastMonth.getMonthValue(), lastMonth.getYear(), category, currentBaseSite);
                Double lastMonthSale= salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYearForDealer(customer, currentBaseSite, lastMonth.getMonthValue(), lastMonth.getYear(),null);
                data.setActualSales(currentMonthSale);
                double growth = currentMonthSale - lastMonthSale;
                data.setGrowth(growth);

                Date date = Date.from(currentMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
                var monthYear = EyDmsDateUtility.getFormattedDate(date, "MMM-YYYY");
                Double targetSale = salesPlanningDao.getDealerSalesAnnualTarget(customer.getUid(),monthYear);
                data.setTargetSales(targetSale);
                if (targetSale > 0) {
                    data.setPercentage((currentMonthSale / targetSale) * 100);
                }else {
                    data.setPercentage(0.0);
                }

                if(Objects.nonNull(monthYear)) {
                    data.setMonthYear(monthYear.replace("-", " "));
                }
                dataList.add(data);
                currentMonth = lastMonth;
            }
        }
    }

    @Override
    public MarketVisitDetailsData getJounreyDetailsData(final String visitId) {
        MarketVisitDetailsData marketVisitDetailsData = new MarketVisitDetailsData();
        VisitMasterModel visitMasterModel = visitMasterDao.findVisitMasterByIdInLocalView(visitId);

        if (null != visitMasterModel.getStartVisitTime()) {
            DateFormat dateFormat = new SimpleDateFormat(EyDmsCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
            marketVisitDetailsData.setDateOfJourney(dateFormat.format(visitMasterModel.getStartVisitTime()));

            SimpleDateFormat formatTime = new SimpleDateFormat("hh.mm aa");
            String time = formatTime.format(visitMasterModel.getStartVisitTime());
            marketVisitDetailsData.setStartTime(time);
        }
        if (null != visitMasterModel.getEndVisitTime()) {
            SimpleDateFormat formatTime = new SimpleDateFormat("hh.mm aa");
            String time = formatTime.format(visitMasterModel.getEndVisitTime());
            marketVisitDetailsData.setEndTime(time);
        }
        if (null != visitMasterModel.getRoute()) {
            populateRouteInMarketVisitDetails(marketVisitDetailsData, visitMasterModel.getRoute());
        }
        if (null != visitMasterModel.getObjective()) {
            populateObjectiveInMarketVisitDetails(marketVisitDetailsData, visitMasterModel.getObjective());
        }
        marketVisitDetailsData.setApprovalStatus(visitMasterModel.getApprovalStatus().toString());
        int visited = (int) visitMasterModel.getCounterVisits().stream().filter(each -> each.getEndVisitTime() != null).count();
        int notVisited = (int) visitMasterModel.getCounterVisits().stream().filter(each -> each.getEndVisitTime() == null).count();

        marketVisitDetailsData.setVisited(visited);
        marketVisitDetailsData.setNotVisited(notVisited);
        //New Territory Change
        if(visitMasterModel.getSubAreaMaster()!=null) {
            marketVisitDetailsData.setSubArea(visitMasterModel.getSubAreaMaster().getTaluka());
        }
        populateDeviationsInMarketVisitDetailsData(marketVisitDetailsData, visitMasterModel, true);

        populateVisitSummaryPlanVsActual(marketVisitDetailsData, visitMasterModel);

        populateVisitDetailsPlanVsActual(marketVisitDetailsData, visitMasterModel);

        int counters=0;
        int sites=0;
        int influencers=0;

        CounterVisitListData counterVisitListData = new CounterVisitListData();

        if (null != visitMasterModel.getCounterVisits() && !visitMasterModel.getCounterVisits().isEmpty())
        {
            Map<CounterType,List<CounterVisitMasterModel>> counterList = visitMasterModel.getCounterVisits().stream().filter(l->l.getCounterType()!=null).collect(Collectors.groupingBy(l -> l.getCounterType()));
            if(counterList.containsKey(CounterType.DEALER))
                counters+=counterList.get(CounterType.DEALER).size();
            if(counterList.containsKey(CounterType.RETAILER))
                counters+=counterList.get(CounterType.RETAILER).size();
            if(counterList.containsKey(CounterType.INFLUENCER))
                influencers=counterList.get(CounterType.INFLUENCER).size();
            if(counterList.containsKey(CounterType.SITE))
                sites=counterList.get(CounterType.SITE).size();


            Calendar cal = Calendar.getInstance();


            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            Date startDate1 = new Date();
            try {
                startDate1 = formatter.parse(marketVisitDetailsData.getDateOfJourney().toString());

            } catch (Exception e) {
                e.printStackTrace();
            }
            cal.setTime(startDate1);
            cal.add(Calendar.DATE,1);
            Date startDate2 = cal.getTime();
            cal.add(Calendar.DATE,-4);
            Date last3DaysStartDate = cal.getTime();
            cal.add(Calendar.DATE,2);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            Date last3DaysEndDate = cal.getTime();
            cal.add(Calendar.DATE,1);
            Date endDate1 = cal.getTime();
            cal.add(Calendar.DATE,1);
            Date endDate2 = cal.getTime();


            List<CounterVisitData> counterVisitList = new ArrayList<>();
            for(CounterVisitMasterModel counterVisit : visitMasterModel.getCounterVisits())
            {
                CounterVisitData data = new CounterVisitData();
                if(counterVisit.getEyDmsCustomer()!=null) {
                    data.setCounterName(counterVisit.getEyDmsCustomer().getName());
                    data.setCounterCode(counterVisit.getEyDmsCustomer().getUid());
                    data.setCustomerNo(counterVisit.getEyDmsCustomer().getCustomerNo());
                }
                data.setOrderBooked(djpVisitDao.getOrderCapturedForCounter(counterVisit.getEyDmsCustomer().getPk().toString(),startDate1,endDate1));
                data.setOrderCaptured(counterVisit.getOrderGenerated()!=null?counterVisit.getOrderGenerated():0.0);
                if(data.getOrderCaptured()!=null)
                    data.setDifference(data.getOrderBooked()-data.getOrderCaptured());
                else
                    data.setDifference(data.getOrderBooked()-0.0);
                data.setPotentialBefore(djpVisitDao.getPreviousCounterPotentialForCounter(startDate1));
                data.setPotentialAfter(counterVisit.getTotalSale()!=null?counterVisit.getTotalSale():0.0);

                Double todaysSales = data.getOrderBooked();
                Double last3DaysSales = djpVisitDao.getOrderCapturedForCounter(counterVisit.getEyDmsCustomer().getPk().toString(),last3DaysStartDate,last3DaysEndDate);
                if(last3DaysSales!=0.0)
                    data.setGrowth(((todaysSales-last3DaysSales)/last3DaysSales)*100);
                else
                    data.setGrowth(0.0);

                if(counterVisit.getCounterType()!=null && counterVisit.getCounterType().equals(CounterType.DEALER) && !counterVisit.getEyDmsCustomer().getGroups().contains(b2bUnitService.getUnitForUid(EyDmsCoreConstants.B2B_UNIT.EYDMS_OTHER_UNIT_UID)))
                {

                    Double outstandingBefore = djpVisitDao.getOutstandingAmountBetweenDates(counterVisit.getEyDmsCustomer().getCustomerNo(),startDate1,endDate1);
                    Double outstandingAfter = djpVisitDao.getOutstandingAmountBetweenDates(counterVisit.getEyDmsCustomer().getCustomerNo(),startDate2,endDate2);

                    BaseSiteModel brand = baseSiteService.getCurrentBaseSite();
                    CustomerCategory category = CustomerCategory.TR;



                    cal = Calendar.getInstance();
                    cal.setTime(startDate1);
                    cal.add(Calendar.YEAR, -1);
                    Date prevYear = cal.getTime();
                    cal.add(Calendar.DATE, 1);
                    Date prevYear2 = cal.getTime();

                    double averageDailySalesBefore = djpVisitDao.getAvgSalesDataForDealer(counterVisit.getEyDmsCustomer().getCustomerNo(), startDate1, prevYear, category, brand)/365;
                    double averageDailySalesAfter = djpVisitDao.getAvgSalesDataForDealer(counterVisit.getEyDmsCustomer().getCustomerNo(), startDate2, prevYear2, category, brand)/365;

                    if(averageDailySalesBefore!=0.0)
                    {
                        Double ageingDaysBefore = outstandingBefore/averageDailySalesBefore;
                        data.setAgingDayBefore(ageingDaysBefore);
                    }
                    else
                    {
                        data.setAgingDayBefore(0.0);
                    }

                    if(averageDailySalesAfter!=0.0)
                    {
                        Double ageingDaysAfter = outstandingAfter/averageDailySalesAfter;
                        data.setAgingDayAfter(ageingDaysAfter);
                    }
                    else
                    {
                        data.setAgingDayAfter(0.0);
                    }

                    data.setOutstandingBefore(outstandingBefore);
                    data.setOutstandingAfter(outstandingAfter);

                }
                counterVisitList.add(data);

            }
            counterVisitListData.setCounterVisitList(counterVisitList);
        }

        marketVisitDetailsData.setCounters(counters);
        marketVisitDetailsData.setInfluencers(influencers);
        marketVisitDetailsData.setSites(sites);
        marketVisitDetailsData.setCounterDetails(counterVisitListData);

        return marketVisitDetailsData;
    }
    private void populateVisitSummaryPlanVsActual(MarketVisitDetailsData marketVisitDetailsData, VisitMasterModel visitMasterModel) {
        if(CollectionUtils.isEmpty(visitMasterModel.getCounterVisits())){
            throw new ModelNotFoundException("No Counter Visits assigned to current visit: "+visitMasterModel.getPk());
        }
        List<VisitSummaryPlannedVsActualData> list = new ArrayList<VisitSummaryPlannedVsActualData>();

        int totalPlannedVisits = (int) visitMasterModel.getCounterVisits().stream().filter(each->!each.getIsAdHoc()).count();
        int totalVisited = (int) visitMasterModel.getCounterVisits().stream().filter(each->!each.getIsAdHoc() && each.getEndVisitTime()!=null).count();
        int totalAdHocVisted = (int) visitMasterModel.getCounterVisits().stream().filter(each->each.getIsAdHoc() && each.getEndVisitTime()!=null).count();
        int totalActualVisit = totalVisited + totalAdHocVisted;
        int totalNotVisited = (int) visitMasterModel.getCounterVisits().stream().filter(each->!each.getIsAdHoc() && each.getEndVisitTime()==null).count();
        VisitSummaryPlannedVsActualData data = new VisitSummaryPlannedVsActualData();
        data.setPlannedVisits(totalPlannedVisits);
        data.setPlannedAndVisited(totalVisited);
        data.setAdHocVisited(totalAdHocVisted);
        data.setActualVisits(totalActualVisit);
        data.setPlannedNotVisited(totalNotVisited);
        data.setCounterType("ALL");
        list.add(data);

        Map<CounterType, List<CounterVisitMasterModel>> map = visitMasterModel.getCounterVisits().stream().filter(l->l.getCounterType()!=null).collect(Collectors.groupingBy(CounterVisitMasterModel::getCounterType));
        map.forEach((counterType, counterList)->{
            int plannedVisits = (int) counterList.stream().filter(each->!each.getIsAdHoc()).count();
            int visited = (int) counterList.stream().filter(each->!each.getIsAdHoc() && each.getEndVisitTime()!=null).count();
            int adHocVisited = (int) counterList.stream().filter(each->each.getIsAdHoc() && each.getEndVisitTime()!=null).count();
            int actualVisit = visited + adHocVisited;
            int plannedNotVisited = (int) counterList.stream().filter(each->!each.getIsAdHoc() && each.getEndVisitTime()==null).count();
            VisitSummaryPlannedVsActualData visitSummary = new VisitSummaryPlannedVsActualData();
            visitSummary.setPlannedVisits(plannedVisits);
            visitSummary.setPlannedAndVisited(visited);
            visitSummary.setAdHocVisited(adHocVisited);
            visitSummary.setActualVisits(actualVisit);
            visitSummary.setPlannedNotVisited(plannedNotVisited);
            visitSummary.setCounterType(counterType.getCode());
            list.add(visitSummary);
        });
        marketVisitDetailsData.setVisitSummary(list);
    }

    private void populateVisitDetailsPlanVsActual(MarketVisitDetailsData marketVisitDetailsData, VisitMasterModel visitMasterModel) {
        if(CollectionUtils.isEmpty(visitMasterModel.getCounterVisits())){
            throw new ModelNotFoundException("No Counter Visits assigned to current visit: "+visitMasterModel.getPk());
        }
        List<CounterDetailsData> countersDetailsList = new ArrayList<>();
        Collection<CounterVisitMasterModel> countersDetailsModelList = visitMasterModel.getCounterVisits();
        if(CollectionUtils.isNotEmpty(countersDetailsModelList)){
            for(CounterVisitMasterModel counterVisitMasterModel: countersDetailsModelList ){
                CounterDetailsData counterVisitedData = new CounterDetailsData();
                counterVisitedData.setCustomerName(null!= counterVisitMasterModel.getEyDmsCustomer()?counterVisitMasterModel.getEyDmsCustomer().getName():StringUtils.EMPTY);
                counterVisitedData.setCustomerCode(null!= counterVisitMasterModel.getEyDmsCustomer()?counterVisitMasterModel.getEyDmsCustomer().getUid():StringUtils.EMPTY);
                counterVisitedData.setCustomerType(getCustomerType(counterVisitMasterModel.getEyDmsCustomer()));
                counterVisitedData.setCustomerNo(null!= counterVisitMasterModel.getEyDmsCustomer()?counterVisitMasterModel.getEyDmsCustomer().getCustomerNo():StringUtils.EMPTY);
                counterVisitedData.setCounterVisitId(counterVisitMasterModel.getPk().toString());
                counterVisitedData.setPlannedStatus(counterVisitMasterModel.getIsAdHoc() ? "Ad Hoc" : "Planned");
                counterVisitedData.setVisitedStatus(counterVisitMasterModel.getEndVisitTime()!=null ? "Visited" : "Not Visited");
                counterVisitedData.setDeviationComment(counterVisitMasterModel.getDeviationComment());
                counterVisitedData.setDeviationReason(counterVisitMasterModel.getDeviationReason());
                countersDetailsList.add(counterVisitedData);
            }
        }
        marketVisitDetailsData.setVisitDetails(countersDetailsList);
    }

    /* Sales History for Retailer And Influencer 360 */
    @Override
    public SalesHistoryData getSalesHistoryDataFor360(String counterVisitId) {
        CounterVisitMasterModel counterVisitModel = counterVisitDao.findCounterVisitById(counterVisitId);
        EyDmsCustomerModel eydmsCustomer = counterVisitModel.getEyDmsCustomer();

        UserGroupModel retailerGroup = userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID);
        UserGroupModel influencerGroup = userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID);

        BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();

        //String transactionType = "";
        String customerCode="";
        LocalDate date=LocalDate.now();
        LocalDate lastMonth=date.minusMonths(1);
        LocalDate lastTolastMonth = date.minusMonths(2);
        LocalDate lastYearSameMonth = date.minusYears(1);

        SalesHistoryData data= new SalesHistoryData();
        double lastMonthSales=0.0,secondLastMonthSales=0.0,lastYearSameMonthSales=0.0,actualMonthSale=0.0;
        if(eydmsCustomer.getGroups()!=null && eydmsCustomer.getGroups().contains(retailerGroup))
        {
            //  transactionType=EyDmsCoreConstants.DJP.RETAILER_TRANSACTION_TYPE;
            customerCode=eydmsCustomer.getCustomerNo();
            lastMonthSales = networkDao.getSalesQuantityForRetailerMonthYear(eydmsCustomer,currentBaseSite,lastMonth.getMonthValue(), lastMonth.getYear());
            secondLastMonthSales =networkDao.getSalesQuantityForRetailerMonthYear(eydmsCustomer,currentBaseSite, lastTolastMonth.getMonthValue(), lastTolastMonth.getYear());
            lastYearSameMonthSales =networkDao.getSalesQuantityForRetailerMonthYear(eydmsCustomer,currentBaseSite, lastYearSameMonth.getMonthValue(), lastYearSameMonth.getYear());
            actualMonthSale = networkDao.getSalesQuantityForRetailerMonthYear(eydmsCustomer, currentBaseSite, date.getMonthValue(), date.getYear());
        }
        else if(eydmsCustomer.getGroups() !=null && eydmsCustomer.getGroups().contains(influencerGroup))
        {
            //transactionType = EyDmsCoreConstants.DJP.INFLEUNCER_TRANSACTION_TYPE;
            customerCode=eydmsCustomer.getNirmanMitraCode();
            lastMonthSales = networkDao.getSalesQuantityForInfluencerMonthYear(eydmsCustomer,currentBaseSite,lastMonth.getMonthValue(), lastMonth.getYear());
            secondLastMonthSales =networkDao.getSalesQuantityForInfluencerMonthYear(eydmsCustomer,currentBaseSite, lastTolastMonth.getMonthValue(), lastTolastMonth.getYear());
            lastYearSameMonthSales =networkDao.getSalesQuantityForInfluencerMonthYear(eydmsCustomer,currentBaseSite, lastYearSameMonth.getMonthValue(), lastYearSameMonth.getYear());
            actualMonthSale = networkDao.getSalesQuantityForInfluencerMonthYear(eydmsCustomer, currentBaseSite, date.getMonthValue(), date.getYear());
        }


       /* double lastMonthSales = djpVisitDao.getSalesHistoryDataFor360(customerCode, currentBaseSite, transactionType, lastMonth.getMonthValue(), lastMonth.getYear());
        double secondLastMonthSales = djpVisitDao.getSalesHistoryDataFor360(customerCode, currentBaseSite, transactionType, lastTolastMonth.getMonthValue(), lastTolastMonth.getYear());*/

        data.setLastMonthSales(lastMonthSales);

        //  data.setLastYearCurrentMonthSales(djpVisitDao.getSalesHistoryDataFor360(customerCode, currentBaseSite, transactionType, lastYearSameMonth.getMonthValue(), lastYearSameMonth.getYear()));
        data.setLastYearCurrentMonthSales(lastYearSameMonthSales);

        //Double actualMonthSale = djpVisitDao.getSalesHistoryDataFor360(customerCode, currentBaseSite, transactionType, date.getMonthValue(), date.getYear());

        data.setSalesMTD(actualMonthSale);
        data.setWholeSale(eydmsCustomer.getWholeSale());
        data.setCounterSale(eydmsCustomer.getRetailSale());

        if(lastMonthSales !=0.0 && secondLastMonthSales!=0.0) {
            double growth = ((lastMonthSales - secondLastMonthSales) / secondLastMonthSales) * 100;
            if (growth != 0.0)
                data.setGrowth(growth);
            else
                data.setGrowth(0.0);
        }
        else
        {
            data.setGrowth(0.0);
        }

        if(eydmsCustomer.getGroups()!=null && eydmsCustomer.getGroups().contains(retailerGroup)) {
            Double monthlySalesTarget=0.0;
            if(customerCode!=null) {
                monthlySalesTarget = djpVisitDao.getSalesTargetFor360(customerCode, CounterType.RETAILER.getCode(), date.getMonthValue(), date.getYear());
            }

            Calendar cal = Calendar.getInstance();
            int noOfDaysOfTheMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            int pastDay = cal.get(Calendar.DAY_OF_MONTH) - 1;
            int remainingDays = noOfDaysOfTheMonth - pastDay;

            double askingRate = (monthlySalesTarget - actualMonthSale) / remainingDays;
            if (askingRate != 0.0) {
                data.setAskingRate(askingRate);
            } else {
                data.setAskingRate(0.0);
            }
        }
        return data;
    }

    @Override
    public List<MonthlySalesData> getLastSixMonthSalesForRetailer(String counterVisitId) {

        List<MonthlySalesData> dataList = new ArrayList<>();

        CounterVisitMasterModel counterVisitModel = counterVisitDao.findCounterVisitById(counterVisitId);
        try {

            if(Objects.nonNull(counterVisitModel.getEyDmsCustomer()))
            {

                String customerNo = counterVisitModel.getEyDmsCustomer().getCustomerNo() != null ? counterVisitModel.getEyDmsCustomer().getCustomerNo() : " ";

                BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();

                String transactionType = EyDmsCoreConstants.DJP.RETAILER_TRANSACTION_TYPE;

                List<Double> monthlySale= new ArrayList<>();
                List<Double> lastMonthWiseList = new ArrayList<>();
                List<Double> listOfCurrentMonthSalesTarget = new ArrayList<>();
                List<Double> listOfLastMonthSalesTarget = new ArrayList<>();
                LocalDate date=LocalDate.now();
                for(int i=0;i<=6;i++) {
                    LocalDate lastMonth = date.minusMonths(i);

                    int month = lastMonth.getMonthValue();
                    int year = lastMonth.getYear();
                    Double monthSale=0.0,lastMonthWiseSale=0.0,currentMonthSalesTarget=0.0,lastMonthSalesTarget=0.0;
                    if (customerNo != null){
                        monthSale = djpVisitDao.getSalesHistoryDataFor360(customerNo, currentBaseSite, transactionType, date.getMonthValue(), date.getYear());
                        lastMonthWiseSale = djpVisitDao.getSalesHistoryDataFor360(customerNo, currentBaseSite, transactionType, lastMonth.getMonthValue(), lastMonth.getYear());                    ;
                        currentMonthSalesTarget = djpVisitDao.getSalesTargetFor360(customerNo, CounterType.RETAILER.getCode(), date.getMonthValue(), date.getYear());
                        lastMonthSalesTarget = djpVisitDao.getSalesTargetFor360(customerNo, CounterType.RETAILER.getCode(), lastMonth.getMonthValue(), lastMonth.getYear());
                    }
                    monthlySale.add(monthSale);
                    lastMonthWiseList.add(lastMonthWiseSale);
                    listOfCurrentMonthSalesTarget.add(currentMonthSalesTarget);
                    listOfLastMonthSalesTarget.add(lastMonthSalesTarget);
                }

                for(int i=0;i<=5;i++)
                {
                    MonthlySalesData data = new MonthlySalesData();
                    LocalDate lastMonth=date.minusMonths(i);
                    int month =lastMonth.getMonthValue();
                    int year = lastMonth.getYear();

                    data.setMonthYear(getMonthName(month-1).concat(" ").concat(String.valueOf(year)));

                    double currentMonthSales= lastMonthWiseList.get(i);
                    double lastMonthSales=lastMonthWiseList.get(i+1);
                    double monthWiseAvgSale=  monthlySale.get(i);
                    double lastMonthAvgSale = lastMonthWiseList.get(i);

                    /* to do - optimize */
                    if(date.getMonthValue() != 0 && monthWiseAvgSale  !=0)
                        data.setActualSales(monthWiseAvgSale);
                    else
                        data.setActualSales(0.0);

                    if(lastMonth.getMonthValue() !=0 && lastMonthAvgSale !=0)
                        data.setActualSales(lastMonthAvgSale);
                    else
                        data.setActualSales(0.0);

                    double growth=currentMonthSales-lastMonthSales;
                    if(growth!=0.0)
                        data.setGrowth(growth);
                    else
                        data.setGrowth(0.0);

                    double monthlySalesTarget= listOfCurrentMonthSalesTarget.get(i);
                    double lastMonthSalesTarget= listOfLastMonthSalesTarget.get(i);

                    if(date.getMonthValue() != 0 && monthlySalesTarget != 0.0)
                        data.setTargetSales(monthlySalesTarget);
                    else
                        data.setTargetSales(0.0);

                    if(lastMonth.getMonthValue() != 0 && lastMonthSalesTarget != 0.0)
                        data.setTargetSales(lastMonthSalesTarget);
                    else
                        data.setTargetSales(0.0);

                   /* double actualSale = 0;
                    double targetSale = 0;
                    double percentageAdherence = 0;
                    if(data.getActualSales()!=null)
                        actualSale = data.getActualSales();
                    if(data.getTargetSales()!=null)
                        targetSale = data.getTargetSales();
                    if(actualSale!=0 && targetSale!=0) {
                        percentageAdherence= (actualSale/targetSale)*100;
                    }
                    data.setPercentage(percentageAdherence);*/
                    data.setPercentage(0.0);
                    dataList.add(data);

                    if(month!=0)
                        --month;
                    else
                    {
                        month=11;
                        --year;
                    }
                }
            }
        }catch(Exception e)
        {
            LOG.info("CounterVisitModel not found");
        }
        return dataList;
    }


    @Override
    public CustomerSubAreaMappingModel createCustomerSubAreaMapping(String state, String district, String taluka, EyDmsCustomerModel eydmsCustomer, CMSSiteModel site) {
        CustomerSubAreaMappingModel customerSubAreaMapping = modelService.create(CustomerSubAreaMappingModel.class);
        customerSubAreaMapping.setState(state);
        customerSubAreaMapping.setDistrict(district);
        customerSubAreaMapping.setSubArea(taluka);
        customerSubAreaMapping.setEyDmsCustomer(eydmsCustomer);
        customerSubAreaMapping.setIsActive(Boolean.TRUE);
        customerSubAreaMapping.setBrand(site);
        if(eydmsCustomer.getCounterType()!=null) {
            customerSubAreaMapping.setCounterType(eydmsCustomer.getCounterType().getCode());
        }
        if(eydmsCustomer.getGroups()!=null && eydmsCustomer.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.B2B_UNIT.EYDMS_OTHER_UNIT_UID))) {
            customerSubAreaMapping.setIsOtherBrand(Boolean.TRUE);
        }
        else {
            customerSubAreaMapping.setIsOtherBrand(Boolean.FALSE);
        }
        customerSubAreaMapping.setSubAreaMaster(territoryManagementService.getTerritoryByDistrictAndTaluka(district, taluka));
        return customerSubAreaMapping;
    }


    @Override
    public List<VisitMasterModel> getAllVisit(String startDate, String endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
        Date newStartDate = null, newEndDate =null;
        sdf.setLenient(false);
        try {
            newStartDate = sdf.parse(startDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            newEndDate = sdf.parse(endDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        List<VisitMasterModel> list = djpVisitDao.getAllVisit(userService.getCurrentUser(), newStartDate, newEndDate);
        return list;
    }

    @Override
    public Boolean saveCustomerCoordinates(String customerId, Double latitude, Double longitude) {
        EyDmsCustomerModel customer = eydmsCustomerService.getEyDmsCustomerForUid(customerId);
        customer.setLatitude(latitude);
        customer.setLongitude(longitude);
        customer.setGeoUpdateTime(new Date());
        customer.setGeoModifiedBy((B2BCustomerModel) userService.getCurrentUser());
        modelService.save(customer);
        return Boolean.TRUE;
    }
    @Override
    public List<ObjectiveModel> getAllObjective(){
        return objectiveDao.findAllObjective();
    }

    @Override
    public Integer getPendingApprovalVisitsCountForTsmorRh() {
        EyDmsUserModel currentUser = (EyDmsUserModel) userService.getCurrentUser();
        return djpVisitDao.getPendingApprovalVisitsCountForTsmorRh(currentUser);
    }

    @Override
    public Double calculateDJPCompliance(){

        int visitCountStatus=0;
        long allVisitCount=0;
        double djpAdherence = 0.0;
        ApprovalStatus approvalStatus=null;
        Map<ApprovalStatus,Integer> map = new HashMap<>();
        List<List<Object>> completedVisits = djpVisitDao.getCompletedPlannedVisitsByApprovalStatus();
        if(CollectionUtils.isNotEmpty(completedVisits)){
            allVisitCount += completedVisits.stream().filter(objects1 -> objects1 != null && !objects1.isEmpty()).mapToInt(objects2 -> (int) objects2.get(0)).sum();

            for (List<Object> visit : completedVisits) {
                LOG.info("visit list size " + visit.size());
                int count = (int) visit.get(0) != 0.0 ? (int) visit.get(0) : 0;
                approvalStatus= (ApprovalStatus) visit.get(1);
                map.put(approvalStatus,count);
            }
            for (Map.Entry<ApprovalStatus,Integer> keyVal : map.entrySet()) {
                LOG.info("map key " + keyVal.getKey() + " " + "map value " +keyVal.getValue());
            }
            LOG.info("All Visit Count" + allVisitCount);
            visitCountStatus = map.entrySet().stream().filter(v -> v.getKey()!=null).filter(v->v.getKey().equals(ApprovalStatus.DI_APPROVED) || v.getKey().equals(ApprovalStatus.SYSTEM_APPROVED) || v.getKey().equals(ApprovalStatus.AUTO_APPROVED)).mapToInt(o -> o.getValue()).sum();
            LOG.info("Visit count as per status" + visitCountStatus);
            if(visitCountStatus!=0 && allVisitCount!=0)
                djpAdherence = (visitCountStatus/allVisitCount)*100;
            LOG.info("DJP Adherence" +djpAdherence);

        }
        return djpAdherence;
    }

    @Override
    public DropdownListData getPartnerType() {
        List<CounterType> filteredCounterList = enumerationService.getEnumerationValues(CounterType.class)
                .stream()
                .filter(counterType -> counterType != CounterType.INFLUENCER && counterType != CounterType.SP)
                .collect(Collectors.toList());

        List<InfluencerType> influencerTypeList = enumerationService.getEnumerationValues(InfluencerType.class)
                .stream()
                .collect(Collectors.toList());

        List<HybrisEnumValue> mergedList = Stream.concat(filteredCounterList.stream(), influencerTypeList.stream())
                .collect(Collectors.toList());

        List<DropdownData> dropdownDataList = mergedList.stream()
                .map(e -> {
                    DropdownData dropdownData = new DropdownData();
                    dropdownData.setCode(e.getCode());
                    dropdownData.setName(null != enumerationService.getEnumerationName(e, i18NService.getCurrentLocale()) ? enumerationService.getEnumerationName(e, i18NService.getCurrentLocale()) : e.getCode());
                    return dropdownData;
                })
                .collect(Collectors.toList());

        DropdownListData dropdownListData = new DropdownListData();
        dropdownListData.setDropdown(dropdownDataList);
        return dropdownListData;
    }

    @Override
    public void getLastSixMonthSalesForRetailer(List<MonthlySalesData> dataList, EyDmsCustomerModel customer) {
        BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
        LocalDate currentMonth = LocalDate.now();

        for (int i = 1; i <= 6; i++) {
            MonthlySalesData data = new MonthlySalesData();
            Double currentMonthSale = salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYearForRetailer(customer,currentBaseSite,currentMonth.getMonthValue(), currentMonth.getYear(),null);
            LocalDate lastMonth = currentMonth.minusMonths(1);
            Double lastMonthSale= salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYearForRetailer(customer,currentBaseSite,lastMonth.getMonthValue(), lastMonth.getYear(),null);
            data.setActualSales(currentMonthSale);
            double growth = currentMonthSale - lastMonthSale;
            data.setGrowth(growth);

            Date date = Date.from(currentMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
            var monthYear = EyDmsDateUtility.getFormattedDate(date, "MMM-YYYY");
            Double targetSale = salesPerformanceDao.getMonthlySalesTargetForRetailer(customer.getUid(),currentBaseSite,monthYear,null);
            data.setTargetSales(targetSale);
            if (targetSale > 0) {
                data.setPercentage((currentMonthSale / targetSale) * 100);
            }else {
                data.setPercentage(0.0);
            }

            if(Objects.nonNull(monthYear)) {
                data.setMonthYear(monthYear.replace("-", " "));
            }
            dataList.add(data);
            currentMonth = lastMonth;
        }
    }

    @Override
    public Map<String, Object> counterVisitedForRoutes(String routeId) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("lastVisitDate", "");
        map.put("counterVisited", Integer.toString(0));
        map.put("dayName", "");
        //DJPRouteScoreMasterModel routeScore = djpRouteScoreDao.findByRouteScoreId(routeScoreId);
        RouteMasterModel route = routeMasterDao.findByRouteId(routeId);
        if(route!=null) {
            List<List<Object>> list = djpVisitDao.counterVisitedForSelectedRoutes(route, userService.getCurrentUser());
            if(list!=null) {
                list.forEach(result -> {
                    if(result.get(0)!=null) {
                        Date date = (Date) result.get(0);
                        DateFormat dateFormat = new SimpleDateFormat(EyDmsCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
                        String visitDate = dateFormat.format(date);
                        dateFormat = new SimpleDateFormat("EEEE");
                        String fullDayName = dateFormat.format(date);
                        map.put("lastVisitDate", visitDate);
                        map.put("counterVisited", result.get(1));
                        map.put("dayName", fullDayName);
                    }
                });
            }
        }
        return map;
    }
}