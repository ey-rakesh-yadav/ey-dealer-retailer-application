package com.scl.facades.network.impl;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.customer.services.SclCustomerService;
import com.scl.core.dao.DataConstraintDao;
import com.scl.core.dao.VisitMasterDao;
import com.scl.core.enums.*;
import com.scl.core.model.*;
import com.scl.core.notifications.service.SclNotificationService;
import com.scl.core.services.DJPVisitService;
import com.scl.core.services.SiteManagementService;
import com.scl.core.services.TechnicalAssistanceService;
import com.scl.core.services.TerritoryManagementService;
import com.scl.facades.data.*;
import com.scl.facades.network.SiteManagementFacade;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2b.services.B2BUnitService;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.customer.CustomerAccountService;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.css.Counter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

public class SiteManagementFacadeImpl implements SiteManagementFacade {

    private static final Logger LOG = Logger.getLogger(SiteManagementFacadeImpl.class);

    private static final String NOT_SCL_USER_MESSAGE = "Current user is not an SCL user";
    SiteManagementService siteManagementService;

    @Autowired
    ModelService modelService;

    @Autowired
    KeyGenerator customCodeGenerator;

    @Autowired
    BaseSiteService baseSiteService;

    @Autowired
    CustomerAccountService customerAccountService;

    @Autowired
    DJPVisitService djpVisitService;

    @Autowired
    private Populator<MapNewSiteData, SclSiteMasterModel> siteReversePopulator;

    @Autowired
    UserService userService;

    @Autowired
    TerritoryManagementService territoryManagementService;

    @Autowired
    KeyGenerator counterVisitIdGenerator;

    @Autowired
    B2BUnitService b2bUnitService;

    @Autowired
    private SclCustomerService sclCustomerService;

    @Autowired
    private EnumerationService enumerationService;

    @Autowired
    private Populator<MapNewSiteData, SiteVisitMasterModel> siteVisitReversePopulator;

    @Autowired
    private Populator<AddressData, AddressModel> addressReversePopulator;

    @Autowired
    private Converter<SclSiteMasterModel, MapNewSiteData> siteConverter;

    @Autowired
    TechnicalAssistanceService technicalAssistanceService;
    
    @Autowired
    VisitMasterDao visitMasterDao;

    @Autowired
    DataConstraintDao dataConstraintDao;

    @Autowired
    I18NService i18NService;

    @Autowired
    SclNotificationService sclNotificationService;

    public static final String SITEACTIVE_NO = "NO";

    public static final String SITEACTIVE_YES = "YES";

    @Autowired
    private Populator<MapNewSiteData, SiteTransactionModel> siteTransactionReversePopulator;

    @Autowired
    KeyGenerator siteTransactionIdGenerator;
    
    @Override
    public DropdownListData getSiteServiceType() {
        DropdownListData dropdownListData = new DropdownListData();
        List<SiteServiceTypeModel> list = getSiteManagementService().getSiteServiceType();
        List<DropdownData> dropdownDataList = new ArrayList<>();
        for (SiteServiceTypeModel model : list) {
            DropdownData dropdownData = new DropdownData();
            dropdownData.setCode(model.getCode());
            dropdownData.setName(model.getName());

            dropdownDataList.add(dropdownData);
        }
        dropdownListData.setDropdown(dropdownDataList);
        return dropdownListData;
    }

    @Override
    public DropdownListData getSiteServiceTest(String serviceTypeCode) {
        DropdownListData dropdownListData = new DropdownListData();
        List<SiteServiceTestModel> list = getSiteManagementService().getSiteServiceTest(serviceTypeCode);
        List<DropdownData> dropdownDataList = new ArrayList<>();
        for (SiteServiceTestModel model : list) {
            DropdownData dropdownData = new DropdownData();
            dropdownData.setCode(model.getCode());
            dropdownData.setName(model.getName());

            dropdownDataList.add(dropdownData);
        }
        dropdownListData.setDropdown(dropdownDataList);
        return dropdownListData;
    }

    @Override
    public DropdownListData getSiteCategoryType() {
        DropdownListData dropdownListData = new DropdownListData();
        List<CompetitorProductType> list = getSiteManagementService().getSiteCategoryType();
        List<DropdownData> dropdownDataList = new ArrayList<>();
        for (CompetitorProductType model : list) {
            DropdownData dropdownData = new DropdownData();
            dropdownData.setCode(model.getCode());
            dropdownData.setName(enumerationService.getEnumerationName(model,i18NService.getCurrentLocale()));

            dropdownDataList.add(dropdownData);
        }
        dropdownListData.setDropdown(dropdownDataList);
        return dropdownListData;
    }

    @Override
    public DropdownListData getSiteCementType(String siteCategoryType) {
        DropdownListData dropdownListData = new DropdownListData();
        List<PremiumProductType> list = getSiteManagementService().getSiteCementType(siteCategoryType);
        List<DropdownData> dropdownDataList = new ArrayList<>();
        for (PremiumProductType model : list) {
            DropdownData dropdownData = new DropdownData();
            dropdownData.setCode(model.getCode());
            dropdownData.setName(enumerationService.getEnumerationName(model,i18NService.getCurrentLocale()));

            dropdownDataList.add(dropdownData);
        }
        dropdownListData.setDropdown(dropdownDataList);
        return dropdownListData;
    }

    @Override
    public DropdownListData getSiteCementBrand(String siteCementType) {
        DropdownListData dropdownListData = new DropdownListData();
        List<CompetitorProductModel> list = getSiteManagementService().getSiteCementBrand(siteCementType);
        List<DropdownData> dropdownDataList = new ArrayList<>();
        for (CompetitorProductModel model : list) {
            DropdownData dropdownData = new DropdownData();
            dropdownData.setCode(model.getCode());
            dropdownData.setName(model.getCode());

            dropdownDataList.add(dropdownData);
        }
        if(CollectionUtils.isNotEmpty(dropdownDataList)){
            dropdownListData.setDropdown(dropdownDataList.stream().filter(dropdownData -> Objects.nonNull(dropdownData.getName())).sorted(Comparator.comparing(DropdownData::getName)).collect(Collectors.toList()));
        }else {
            dropdownListData.setDropdown(dropdownDataList);
        }
        return dropdownListData;
    }

    @Override
    public MapNewSiteData mapNewSite(MapNewSiteData siteData) {
        SclUserModel user = (SclUserModel) userService.getCurrentUser();
    	try {
            String siteId = "";
            Double totalNoOfBagsPurchased = 0.0;
            SclSiteMasterModel siteToAdd = null;
            CustomerSubAreaMappingModel customerSubAreaMapping = null;
            AddressModel newAddress = null;
            String state = StringUtils.EMPTY;
            String taluka = StringUtils.EMPTY;
            String district = StringUtils.EMPTY;

            LocalDate current = LocalDate.now();
            Date currentDate = Date.from(current.atStartOfDay(ZoneId.systemDefault()).toInstant());

            if (Objects.nonNull(siteData)) {
                if (siteData.getSiteId() != null) {
                    siteToAdd = (SclSiteMasterModel) userService.getUserForUID(siteData.getSiteId());
                    AddressModel address = siteToAdd.getAddresses().iterator().next();
                    if (address != null) {
                        state = address.getState();
                        taluka = address.getTaluka();
                        district = address.getDistrict();
                    }

                    if (!siteData.getIsSiteConverted()) {
                        Integer visitsPerDay = dataConstraintDao.findDaysByConstraintName("VISITS_PER_DAY");
                        if (visitsPerDay != 0) {
                            if (siteToAdd.getLastVisitTime() != null && siteToAdd.getLastVisitTime().equals(currentDate)) {
                                throw new IllegalArgumentException("Site is already visited for today");
                            }
                        }
                    }
                } else {
                    siteToAdd = modelService.create(SclSiteMasterModel.class);
                    siteToAdd.setUid(String.valueOf(customCodeGenerator.generate()));
                    siteData.setSiteId(siteToAdd.getUid());
                    LOG.info("Site ID : " + siteData.getSiteId());
                    siteToAdd.setCounterType(CounterType.SITE);
                    if (siteData.getTypeOfVisit() != null) {
                        siteToAdd.setTypeOfVisit(TypeOfVisit.valueOf(siteData.getTypeOfVisit()));
                    }
                    siteToAdd.setCreatedBy(user);
                    populateDummyEmail(siteToAdd);
                    populateDefaultUnitAndGroup(siteToAdd, siteData.getBrand(), SclCoreConstants.CUSTOMER.SITE_USER_GROUP_UID);
                    AddressData addressData = null;
                    if (siteData.getSiteAddress() != null) {
                        addressData = siteData.getSiteAddress();
                    }
                    if (addressData != null) {
                        newAddress = modelService.create(AddressModel.class);
                        addressReversePopulator.populate(addressData, newAddress);
                        newAddress.setBillingAddress(true);
                        newAddress.setDuplicate(true);
                        newAddress.setShippingAddress(false);
                        newAddress.setIsPrimaryAddress(false);
                        newAddress.setVisibleInAddressBook(false);
                        state = addressData.getState();
                        taluka = addressData.getTaluka();
                        district = addressData.getDistrict();
                        newAddress.setLine3(addressData.getLine1());
                        newAddress.setLine4(addressData.getLine2());
                        customerSubAreaMapping = djpVisitService.createCustomerSubAreaMapping(addressData.getState(), addressData.getDistrict(), addressData.getTaluka(), siteToAdd, (CMSSiteModel) baseSiteService.getCurrentBaseSite());
                    }
                }
                siteReversePopulator.populate(siteData, siteToAdd);
                siteToAdd.setSynced(false);
                SubAreaMasterModel subArea = territoryManagementService.getTerritoryByDistrictAndTaluka(district, taluka);
                updateTotalSalesDetails(siteToAdd);

                //Creating SiteTransactionModel in case of site conversion
                if (siteToAdd.getSiteStatus() != null && siteToAdd.getSiteStatus().equals(SiteStatus.SITE_CONVERTED)) {
                    SiteTransactionModel siteTransactionModel = createSiteTransactionData(siteToAdd, siteData, user);
                    modelService.save(siteTransactionModel);
                    modelService.refresh(siteTransactionModel);
                }

                //Coming from Map new site and Existing Site
                if (!siteData.getIsSiteConverted()) {
                    DateFormat dateFormat = new SimpleDateFormat(SclCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
                    String planDate = dateFormat.format(new Date());
                    TechnicalAssistanceModel technicalAssistanceModel = null;
                    VisitMasterModel visit = djpVisitService.createVisitMasterData(null, null, user, subArea.getPk().toString(), planDate);
                    if (StringUtils.isNotBlank(siteData.getTechnicalAssistanceRequestNo())) {
                        visit.setVisitType(TypeOfVisitMaster.ASSITANCE_VISIT);
                        technicalAssistanceModel = technicalAssistanceService.getTechnicalAssistanceRequestDetails(siteData.getTechnicalAssistanceRequestNo());
                        if (technicalAssistanceModel != null) {
                            visit.setTechnicalAssistance(technicalAssistanceModel);
                            technicalAssistanceModel.setSite(siteToAdd);
                            technicalAssistanceModel.setLastVisitedDate(new Date());
                            technicalAssistanceModel.setLastVisit(visit);
                        }
                    } else {
                        visit.setVisitType(TypeOfVisitMaster.TRADE_VISIT);
                    }
                    visit.setEndVisitTime(new Date());
                    visit.setStatus(VisitStatus.COMPLETED);

                    SiteVisitMasterModel siteVisit = createCounterVisitMasterData(siteToAdd, siteData);
                    siteVisit.setVisit(visit);

                    siteToAdd.setLastCounterVisit(siteVisit);
                    updateTotalVisitDetails(siteToAdd, user);
                    modelService.save(siteVisit);

                    if (customerSubAreaMapping != null) {
                        modelService.saveAll(siteToAdd, customerSubAreaMapping, visit, siteVisit, user);
                        LOG.info(String.format("Saved model instance : %s", customerSubAreaMapping.getSclCustomer().getUid()));
                    }
                    else {
                        modelService.saveAll(siteToAdd, visit, siteVisit, user);
                    }
                    if (technicalAssistanceModel != null) {
                        modelService.save(technicalAssistanceModel);
                    }
                } else {

                    if (customerSubAreaMapping != null) {
                        modelService.saveAll(siteToAdd, customerSubAreaMapping, user);
                        LOG.info(String.format("Saved model instance : %s", customerSubAreaMapping.getSclCustomer().getUid()));
                    }
                    else {
                        modelService.saveAll(siteToAdd, user);
                    }
                }

                if (newAddress != null)
                    customerAccountService.saveAddressEntry(siteToAdd, newAddress);
            }

            if (siteToAdd != null) {
                siteId = siteToAdd.getUid();
                siteData.setSiteId(siteId);
            }

            SimpleDateFormat purchaseDateFormatter = new SimpleDateFormat("dd-MM-yyyy");
            SimpleDateFormat submitDateFormatter=new SimpleDateFormat("dd-MM-yyyy hh:mm a");
            submitDateFormatter.setTimeZone(TimeZone.getTimeZone("IST"));
            SclCustomerModel retailer = null;
            if (siteToAdd != null && siteToAdd.getDealer() != null && siteToAdd.getDealer().getTerritoryCode() != null) {
                List<SclUserModel> mappedSO = territoryManagementService.getSOforSite(siteToAdd.getDealer().getTerritoryCode());
                if (CollectionUtils.isNotEmpty(mappedSO)) {
                    if (StringUtils.isNotBlank(siteToAdd.getRetailer()) && userService.getUserForUID(siteToAdd.getRetailer()) != null) {
                        retailer = (SclCustomerModel) userService.getUserForUID(siteToAdd.getRetailer());
                    }
                    for (SclUserModel sclUser : mappedSO) {

                        if (sclUser.getUserType().getCode().equalsIgnoreCase("SO")) {
                            CompetitorProductModel cementProduct = siteToAdd.getCementProduct();
                            if (siteToAdd.getSiteStatus()!= null && siteToAdd.getSiteStatus().equals(SiteStatus.SITE_CONVERTED)) {
                                if (cementProduct != null && cementProduct.getPremiumProductType() != null) {
                                    if (cementProduct.getPremiumProductType().equals(PremiumProductType.SS_PREMIUM)) {
                                        StringBuilder builder = new StringBuilder();
                                        Map<String, String> suggestion = new HashMap<>();
                                        if (siteToAdd.getCementProduct() != null) {
                                            builder.append("Product Name : ").append(siteToAdd.getCementProduct().getCode()).append("; ");
                                        }
                                        builder.append("Qty (bags) : ").append(siteData.getNumberOfBagsPurchased().intValue()).append("; ");
                                        builder.append("TSO Email : ").append(user.getUid()).append("; ");
                                        builder.append("Site Code : ").append(siteToAdd.getUid()).append("; ");
                                        builder.append("IHB Name : ").append(siteToAdd.getName()).append("; ");
                                        if (StringUtils.isNotEmpty(siteToAdd.getMobileNumber())) {
                                            builder.append("IHB Mobile : ").append(siteToAdd.getMobileNumber()).append("; ");
                                        }
                                        if (StringUtils.isNotEmpty(siteToAdd.getContractorName())) {
                                            builder.append("Contractor Name : ").append(siteToAdd.getContractorName()).append("; ");
                                        }
                                        if (StringUtils.isNotEmpty(siteToAdd.getContractorPhoneNumber())) {
                                            builder.append("Contractor Mobile : ").append(siteToAdd.getContractorPhoneNumber()).append("; ");
                                        }
                                        builder.append("Site Address : ").append(siteData.getSiteAddress().getLine1()).append(", ");
                                        if (StringUtils.isNotEmpty(siteData.getSiteAddress().getLine2())) {
                                            builder.append(siteData.getSiteAddress().getLine2()).append(", ");
                                        }
                                        builder.append(siteData.getSiteAddress().getTaluka()).append(", ").append(siteData.getSiteAddress().getDistrict()).append(", ").append(siteData.getSiteAddress().getPostalCode()).append("; ");
                                        if (siteToAdd.getDealer() != null) {
                                            builder.append("Dealer Name : ").append(siteToAdd.getDealer().getName()).append(" | ").append(siteToAdd.getDealer().getUid()).append("; ");
                                        }
                                        if (retailer != null) {
                                            builder.append("Retailer Name : ").append(retailer.getName()).append(" | ").append(retailer.getUid()).append("; ");
                                        }
                                        builder.append("Date of Purchase : ").append(purchaseDateFormatter.format(siteToAdd.getDateOfPurchase())).append("; ");
                                        builder.append("Submitted on : ").append(submitDateFormatter.format(siteToAdd.getFormSubmitDate())).append(" ");
                                        String body = builder.toString();
                                        String subject = "Premium Conversion at Site by TSO ";
                                        sclNotificationService.siteConversionNotification(siteToAdd, (B2BCustomerModel) sclUser, body, subject, NotificationCategory.PREMIUM_SITE_CONVERSION);

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception ex){
            LOG.error(String.format("Exception got in mapNewSite => SiteCode::%s and IHB Name::%s, DealerUid::%s and TSO::%s with errorMessage::%s and cause::%s",siteData.getSiteId(),siteData.getName(),siteData.getDealer(),user.getUid(),ex.getMessage(),ex.getCause()));
            throw new RuntimeException(String.format("Error occured while mapping new site => SiteCode::%s and IHB Name::%s, DealerUid::%s and TSO::%s with reason::%s",siteData.getSiteId(),siteData.getName(),siteData.getDealer(),user.getUid(),ex.getMessage()));
        }
        return siteData;
    }
  
    public static boolean isWorkingDay(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return (dow != DayOfWeek.SUNDAY);
    }
    @Override
    public SiteManagementHomePageData getTotalAndActualTargetForSiteVisit(String filter) {

        SclUserModel user = (SclUserModel) userService.getCurrentUser();
        SiteManagementHomePageData data = new SiteManagementHomePageData();
        String constructionStage = null;
        String code = null;
        int counts = 0;
       if(StringUtils.isBlank(filter))
       {
           LocalDate cal = LocalDate.now();
           //int noOfDaysInTheMonth = cal.lengthOfMonth();
           //int noOfDaysGoneByInTheMonth = cal.getDayOfMonth();
           LocalDate localDate=LocalDate.now();
           LocalDate endDate =localDate.plusDays(1);

           LocalDate startDate = LocalDate.of(localDate.getYear(),localDate.getMonthValue(),1);
           LocalDate endOfTheMonth=LocalDate.of(localDate.getYear(),localDate.getMonthValue(),localDate.lengthOfMonth());

           int  noOfDaysGoneByInTheMonth = (int) startDate.datesUntil(endDate)
                   .filter(date -> isWorkingDay(date))
                   .count();
           LOG.info("noOfDaysGoneByInTheMonth:"+noOfDaysGoneByInTheMonth);

           int  noOfDaysInTheMonth = (int) startDate.datesUntil(endOfTheMonth)
                   .filter(date -> isWorkingDay(date))
                   .count();
           LOG.info("noOfDaysInTheMonth:"+noOfDaysInTheMonth);

           double actualTarget = 0.0, totalTarget = 0.0, proratedTarget = 0.0;
           int newSitesVisits = 0, existingSitesVisits = 0;
           double achievementPercentage = 0.0, proratedAchievementPercentage = 0.0;

           if (user.getLastSiteVisitDate() != null) {
               Instant instant = user.getLastSiteVisitDate().toInstant();
               LocalDate lastVisitDate = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate();
               if (lastVisitDate.getMonthValue() == cal.getMonthValue() && cal.getYear() == lastVisitDate.getYear() && user.getCurrentMonthSiteVisit() != null) {
                   actualTarget = user.getCurrentMonthSiteVisit();// getSiteManagementService().getActualTargetForSalesMTD(user);
               }
           }
           data.setSiteVisitActual(actualTarget);//32

           totalTarget = getSiteManagementService().getMonthlySalesTarget(user);//250
           data.setSiteVisitTarget(totalTarget != 0.0 ? totalTarget : 0.0);

           if (actualTarget != 0.0 && totalTarget != 0.0)
               achievementPercentage = (actualTarget / totalTarget) * 100;//12.8
           data.setSiteVisitPercentage(achievementPercentage != 0.0 ? achievementPercentage : 0.0);

           proratedTarget = (totalTarget / noOfDaysInTheMonth) * noOfDaysGoneByInTheMonth;//

           data.setSiteVisitProratedTarget(proratedTarget != 0.0 ? proratedTarget : 0.0);

           if (actualTarget != 0.0 && totalTarget != 0.0)
               proratedAchievementPercentage = (actualTarget / proratedTarget) * 100;
           data.setSiteVisitProratedPercentage(proratedAchievementPercentage != 0.0 ? proratedAchievementPercentage : 0.0);

           newSitesVisits = siteManagementService.getNewSiteVists(user);
           data.setNewSitesVisits(newSitesVisits);
           existingSitesVisits = (int) (actualTarget - newSitesVisits);
           data.setExistingSitesVisits(existingSitesVisits);
           // Set the list in the SiteManagementHomePageData

       }

       else
       {
           if(filter.contains("LastMonth"))
           {
              /* LocalDate cal = LocalDate.now().minusMonths(1);
               int noOfDaysInTheMonth = cal.lengthOfMonth();
               int noOfDaysGoneByInTheMonth = cal.getDayOfMonth();*/

               LocalDate localDate=LocalDate.now().minusMonths(1);
               LocalDate endDate =localDate.plusDays(1);

               LocalDate startDate = LocalDate.of(localDate.getYear(),localDate.getMonthValue(),1);
               LocalDate endOfTheMonth=LocalDate.of(localDate.getYear(),localDate.getMonthValue(),localDate.lengthOfMonth());

               int  noOfDaysGoneByInTheMonth = (int) startDate.datesUntil(endDate)
                       .filter(date -> isWorkingDay(date))
                       .count();
               LOG.info("noOfDaysGoneByInTheMonth:"+noOfDaysGoneByInTheMonth);

               int  noOfDaysInTheMonth = (int) startDate.datesUntil(endOfTheMonth)
                       .filter(date -> isWorkingDay(date))
                       .count();
               LOG.info("noOfDaysInTheMonth:"+noOfDaysInTheMonth);

               double actualTarget = 0.0, totalTarget = 0.0, proratedTarget = 0.0;
               int newSitesVisits = 0, existingSitesVisits = 0;
               double achievementPercentage = 0.0, proratedAchievementPercentage = 0.0;

               if (user.getLastSiteVisitDate() != null) {
                   Instant instant = user.getLastSiteVisitDate().toInstant();
                   LocalDate lastVisitDate = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                   if (lastVisitDate.getMonthValue() == localDate.getMonthValue() && localDate.getYear() == lastVisitDate.getYear() && user.getLastMonthSiteVisit() != null) {
                       actualTarget = user.getLastMonthSiteVisit();// getSiteManagementService().getActualTargetForSalesMTD(user);
                   }
               }
               data.setSiteVisitActual(actualTarget);

               totalTarget = getSiteManagementService().getLastMonthSalesTarget(user);
               data.setSiteVisitTarget(totalTarget != 0.0 ? totalTarget : 0.0);

               if (actualTarget != 0.0 && totalTarget != 0.0)
                   achievementPercentage = (actualTarget / totalTarget) * 100;
               data.setSiteVisitPercentage(achievementPercentage != 0.0 ? achievementPercentage : 0.0);

               proratedTarget = (totalTarget / noOfDaysInTheMonth) * noOfDaysGoneByInTheMonth;

               data.setSiteVisitProratedTarget(proratedTarget != 0.0 ? proratedTarget : 0.0);

               if (actualTarget != 0.0 && totalTarget != 0.0)
                   proratedAchievementPercentage = (actualTarget / proratedTarget) * 100;
               data.setSiteVisitProratedPercentage(proratedAchievementPercentage != 0.0 ? proratedAchievementPercentage : 0.0);

               newSitesVisits = siteManagementService.getNewSiteVistsForLastMonth(user);
               data.setNewSitesVisits(newSitesVisits);
               existingSitesVisits = (int) (actualTarget - newSitesVisits);
               data.setExistingSitesVisits(existingSitesVisits);
             // Set the list in the SiteManagementHomePageData
           }
       }

        List<List<Object>> count = siteManagementService.getSiteTypeStagesCount(user);
        List<SiteConstructionStageData> stageDataList = new ArrayList<>();
        for (List<Object> objects : count) {
            SiteConstructionStageData stageData = new SiteConstructionStageData();

            constructionStage = enumerationService.getEnumerationName(ConstructionStage.valueOf((String) objects.get(1)));
            code = String.valueOf(ConstructionStage.valueOf((String) objects.get(1)));
            counts = (int) objects.get(2);

            stageData.setConstructionStage(constructionStage);
            stageData.setCount(counts);
            stageData.setCode(code);
            stageDataList.add(stageData); // Add the constructed SiteConstructionStageData object to the list
        }
        data.setSiteConstructionStageList(stageDataList);

        return data;

    }

    public SiteVisitMasterModel createCounterVisitMasterData(SclSiteMasterModel site, MapNewSiteData siteData) {

        SiteVisitMasterModel siteVisitMasterModel = modelService.create(SiteVisitMasterModel.class);
        siteVisitMasterModel.setId(counterVisitIdGenerator.generate().toString());
        siteVisitMasterModel.setSequence(1);
        siteVisitMasterModel.setSclCustomer(site);
        siteVisitMasterModel.setCounterType(CounterType.SITE);
        siteVisitMasterModel.setStartVisitTime(new Date());
        siteVisitMasterModel.setEndVisitTime(new Date());
        siteVisitReversePopulator.populate(siteData, siteVisitMasterModel);
        return siteVisitMasterModel;
    }

    private void populateDummyEmail(SclCustomerModel siteToAdd) {
        Random random = new Random();
        int randomInt = random.nextInt(999999);
        siteToAdd.setEmail("dummyemail_" + randomInt + "@gmail.com");
    }

    private void populateDefaultUnitAndGroup(SclCustomerModel customer, String brand, String userGroupUid) {

        Set<PrincipalGroupModel> groups = new HashSet<>();
        String defaultUnitId = StringUtils.EMPTY;
        if (StringUtils.isNotBlank(brand)) {
            switch (brand) {
                case SclCoreConstants.SITE.SHREE_SITE:
                    defaultUnitId = SclCoreConstants.B2B_UNIT.SCL_SHREE_UNIT_UID;
                    break;
                case SclCoreConstants.SITE.BANGUR_SITE:
                    defaultUnitId = SclCoreConstants.B2B_UNIT.SCL_BANGUR_UNIT_UID;
                    break;
                case SclCoreConstants.SITE.ROCKSTRONG_SITE:
                    defaultUnitId = SclCoreConstants.B2B_UNIT.SCL_ROCKSTRONG_UNIT_UID;
                    break;
                case SclCoreConstants.SITE.SCL_SITE:
                    defaultUnitId = SclCoreConstants.B2B_UNIT.SCL_CUSTOMER_UNIT_UID;
                    break;
            }
        }
        B2BUnitModel defaultUnit = (B2BUnitModel) b2bUnitService.getUnitForUid(defaultUnitId);
        customer.setDefaultB2BUnit(defaultUnit);
        if (null == defaultUnit) {
            throw new ModelNotFoundException(String.format("Default  unit not found with uid %s", defaultUnitId));
        }
        groups.add(defaultUnit);
        try {
            UserGroupModel sclSiteGroup = userService.getUserGroupForUID(userGroupUid);
            groups.add(sclSiteGroup);
        } catch (UnknownIdentifierException ex) {
            LOG.error(String.format("User group not found with uid %s", userGroupUid));
        }
        customer.setGroups(groups);
    }


    private void updateTotalVisitDetails(SclSiteMasterModel site, SclUserModel user) {
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();
        if (site.getLastVisitTime() != null) {
            Instant instant = site.getLastVisitTime().toInstant();
            LocalDate lastVisitDate = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            int lastVisitYear = lastVisitDate.getYear();
            int lastVisitMonth = lastVisitDate.getMonthValue();

            //Last Visit Last Month
            if ((lastVisitYear == currentYear && lastVisitMonth == currentMonth - 1) || (lastVisitYear == currentYear - 1 && lastVisitMonth == 12 && currentMonth == 1)) {

                //Site Master
                site.setLastMonthSiteVisit(site.getCurrentMonthSiteVisit());
                site.setCurrentMonthSiteVisit(1);
            } else if (lastVisitYear == currentYear && lastVisitMonth == currentMonth) {
                //Site Master
                site.setCurrentMonthSiteVisit(site.getCurrentMonthSiteVisit() != null ? site.getCurrentMonthSiteVisit() + 1 : 1);
            } else {
                //Site Master
                site.setLastMonthSiteVisit(0);
                site.setCurrentMonthSiteVisit(1);
            }
        } else {
            //Site Master
            site.setLastMonthSiteVisit(0);
            site.setCurrentMonthSiteVisit(1);
        }

        //Scl user

        if (user.getLastSiteVisitDate() != null) {
            Instant instant = user.getLastSiteVisitDate().toInstant();
            LocalDate lastVisitDate = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            int lastVisitYear = lastVisitDate.getYear();
            int lastVisitMonth = lastVisitDate.getMonthValue();

            if ((lastVisitYear == currentYear && lastVisitMonth == currentMonth - 1) || (lastVisitYear == currentYear - 1 && lastVisitMonth == 12 && currentMonth == 1)) {
                user.setLastMonthSiteVisit(user.getCurrentMonthSiteVisit());
                user.setCurrentMonthSiteVisit(1);
            } else if (lastVisitYear == currentYear && lastVisitMonth == currentMonth) {
                user.setCurrentMonthSiteVisit(user.getCurrentMonthSiteVisit() != null ? user.getCurrentMonthSiteVisit() + 1 : 1);
            } else {
                user.setLastMonthSiteVisit(0);
                user.setCurrentMonthSiteVisit(1);
            }
        } else {
            user.setLastMonthSiteVisit(0);
            user.setCurrentMonthSiteVisit(1);
        }
        site.setLastVisitTime(new Date());
        user.setLastSiteVisitDate(new Date());
    }
    
    private void updateTotalSalesDetails(SclSiteMasterModel site) {
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();
        if (site.getFormSubmitDate() != null) {
            Instant instant = site.getFormSubmitDate().toInstant();
            LocalDate lastFormSubmitDate = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            int lastFormSubmitYear = lastFormSubmitDate.getYear();
            int lastFormSubmitMonth = lastFormSubmitDate.getMonthValue();
            if ((lastFormSubmitYear == currentYear && lastFormSubmitMonth == currentMonth - 1) || (lastFormSubmitYear == currentYear - 1 && lastFormSubmitMonth == 12 && currentMonth == 1)) {
            	site.setLastMonthSale(site.getCurrentMonthSale());
                site.setCurrentMonthSale(site.getNumberOfBagsPurchased());
            } else {
                site.setLastMonthSale(0.0);
                site.setCurrentMonthSale(site.getNumberOfBagsPurchased());
            }
        } else {
            site.setLastMonthSale(0.0);
            site.setCurrentMonthSale(site.getNumberOfBagsPurchased());
        }
        site.setFormSubmitDate(new Date());
    }

    public SiteManagementService getSiteManagementService() {
        return siteManagementService;
    }

    public void setSiteManagementService(SiteManagementService siteManagementService) {
        this.siteManagementService = siteManagementService;
    }


    @Override
    public VisitMasterData createAndStartComplaintVisit(String siteId, String requestId) {
        VisitMasterModel visitModel = siteManagementService.createAndStartComplaintVisit(siteId, requestId);
        VisitMasterData visitData = new VisitMasterData();
        visitData.setId(visitModel.getPk().toString());
        return visitData;
    }

    @Override
    public SearchPageData<SclSiteMasterData> getPaginatedSiteMasterList(SearchPageData searchPageData, SiteRequestData siteRequestData,Boolean plannedVisitForToday, List<String> filterBySubAreas) {
        return siteManagementService.getPaginatedSiteMasterList(searchPageData, siteRequestData, plannedVisitForToday,filterBySubAreas);
    }

    @Override
    public SclSiteMasterData toCloseTheSite(String siteId, String closeComment) {
        SclSiteMasterData data = new SclSiteMasterData();
        SclSiteMasterModel siteMasterModel = null;
        if (siteId != null) {
            siteMasterModel = (SclSiteMasterModel) userService.getUserForUID(siteId);
            siteMasterModel.setSiteStatus(SiteStatus.CLOSED);
            siteMasterModel.setClosedBy((B2BCustomerModel) userService.getCurrentUser());
            siteMasterModel.setClosedDate(new Date());
            siteMasterModel.setClosedComment(closeComment);
            siteMasterModel.setSiteActive(SITEACTIVE_NO);
            modelService.save(siteMasterModel);
        }
        if (siteMasterModel != null)
            data.setCode(siteMasterModel.getUid());
        return data;
    }

    @Override
    public MapNewSiteData getSiteDetailsById(String siteId) {
        MapNewSiteData mapNewSiteData = new MapNewSiteData();
        SclSiteMasterModel siteMasterModel=null;
        if(siteId!=null){
            siteMasterModel= (SclSiteMasterModel) userService.getUserForUID(siteId);
            mapNewSiteData = siteConverter.convert(siteMasterModel);

        }
        return mapNewSiteData;
    }

    /**
     * This method calculates the Actual and Target data of site visits and bags purchased (Both Premium and Total).
     * It retrieves current and last month's data and sets the values in the SiteManagementHomePageData object.
     *
     * @return SiteManagementHomePageData object containing premium data.
     */
   @Override
    public SiteManagementHomePageData getTotalPremiumOfSitesAndBags() {
       SiteManagementHomePageData data = new SiteManagementHomePageData();
       SclUserModel sclUser = (SclUserModel) userService.getCurrentUser();
       int cmConvertedActualVisitPreminum = 0;
       int lmConvertedActualVisitPreminum = 0;
       int cmConvertedTargetVisitPreminum = 0;
       int lmConvertedTargetVisitPreminum = 0;
       int cmConvertedActualVisitTotal = 0;
       int lmConvertedActualVisitTotal = 0;
       double cmConvertedActualBagPreminum = 0.0;
       double lmConvertedActualBagPreminum = 0.0;
       double cmConvertedTargetBagPreminum = 0.0;
       double lmConvertedTargetBagPreminum = 0.0;
       double cmConvertedActualBagTotal = 0.0;
       double lmConvertedActualBagTotal = 0.0;

       LocalDate localDate = LocalDate.now();
       int currentYear = localDate.getYear();
       int currentMonth = localDate.getMonthValue();

       cmConvertedTargetVisitPreminum = siteManagementService.cmConvertedTargetVisitPremium(sclUser);
       if(cmConvertedTargetVisitPreminum!=0) {
           data.setCmConvertedTargetVisitPreminum(cmConvertedTargetVisitPreminum);
       }
       else
           data.setCmConvertedTargetVisitPreminum(0);
       lmConvertedTargetVisitPreminum = siteManagementService.lmConvertedActualVisitPremium(sclUser);
       if(lmConvertedTargetVisitPreminum!=0) {
           data.setLmConvertedTargetVisitPreminum(lmConvertedTargetVisitPreminum);
       }
       else
           data.setLmConvertedTargetVisitPreminum(0);
       cmConvertedTargetBagPreminum = siteManagementService.cmConvertedActualBagTotal(sclUser);
       if(cmConvertedTargetBagPreminum!=0.0) {
           data.setCmConvertedTargetBagPreminum(cmConvertedTargetBagPreminum);
       }
       else
           data.setCmConvertedTargetBagPreminum(0.0);
       lmConvertedTargetBagPreminum = siteManagementService.lmConvertedActualBagTotal(sclUser);
       if(lmConvertedTargetBagPreminum!=0.0) {
           data.setLmConvertedTargetBagPreminum(lmConvertedTargetBagPreminum);
       }
       else
           data.setLmConvertedTargetBagPreminum(0.0);

       LocalDate firstDayOfMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
       LocalDate lastDayOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).plusDays(1);
       Double count = siteManagementService.getTotalPremiumOfSite(sclUser,firstDayOfMonth.toString(),lastDayOfMonth.toString(), "premium");
       LOG.info("count ::" + count);
       LocalDate firstDayOfLastMonth = LocalDate.now().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
       LocalDate lastDayOfLastMonth = LocalDate.now().minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
       Double count1 = siteManagementService.getTotalPremiumOfSite(sclUser,firstDayOfLastMonth.toString(),lastDayOfLastMonth.toString(), "premium");
       cmConvertedActualBagTotal = siteManagementService.getTotalPremiumOfSite(sclUser, firstDayOfMonth.toString(), lastDayOfMonth.toString(), "total");
       LOG.info("count1::" + count1);
       cmConvertedActualBagPreminum=count;
       lmConvertedActualBagPreminum=count1;

       List<List<Object>> premium = siteManagementService.getTotalPremiumOfSitesAndBags(sclUser);
       for (List<Object> objects : premium) {
           if(objects.get(0)!=null && objects.get(1)!=null) {
               int year = (int) objects.get(0);
               int month = (int) objects.get(1);
               double bag = objects.get(4)!=null?(double) objects.get(4):0.0;
               int visit = objects.get(3)!=null?(int) objects.get(3):0;
               boolean isPremium = objects.get(2)!=null?(boolean)objects.get(2):false;
               if (currentYear == year && currentMonth == month) {
//                        cmConvertedActualBagTotal += bag;
//                        LOG.info("Bags ::" +cmConvertedActualBagTotal);
                   cmConvertedActualVisitTotal += visit;
                   if (isPremium) {
//                            cmConvertedActualBagPreminum = count ;
//                            LOG.info("Bags1 ::" +cmConvertedActualBagPreminum);
                       cmConvertedActualVisitPreminum = visit;
                   }
               } else {
                   double lastMonthBags = bag + count1;
                   lmConvertedActualBagTotal += lastMonthBags;
                   lmConvertedActualVisitTotal += visit;
                   if (isPremium) {
                       lmConvertedActualBagPreminum += bag ;
                       lmConvertedActualVisitPreminum = visit;
                   }
               }
           }
       }
//            double cmConvertedActualBagTotalLast = cmConvertedActualBagTotal+count;
        data.setCmConvertedActualBagTotal(cmConvertedActualBagTotal != 0.0 ? cmConvertedActualBagTotal : 0.0);
        data.setCmConvertedActualVisitTotal(cmConvertedActualVisitTotal != 0 ? cmConvertedActualVisitTotal : 0);
        data.setCmConvertedActualBagPreminum(cmConvertedActualBagPreminum != 0.0 ? cmConvertedActualBagPreminum : 0.0);
        data.setCmConvertedActualVisitPreminum(cmConvertedActualVisitPreminum != 0 ? cmConvertedActualVisitPreminum : 0);
        data.setLmConvertedActualBagTotal(lmConvertedActualBagTotal != 0.0 ? lmConvertedActualBagTotal : 0.0);
        data.setLmConvertedActualVisitTotal(lmConvertedActualVisitTotal != 0 ? lmConvertedActualVisitTotal : 0);
        data.setLmConvertedTargetBagPreminum(lmConvertedActualBagPreminum != 0.0 ? lmConvertedActualBagPreminum : 0.0);
        data.setLmConvertedActualVisitPreminum(lmConvertedActualVisitPreminum != 0 ? lmConvertedActualVisitPreminum : 0);

       /*LocalDate currentDate = LocalDate.now();
       int currentYear = currentDate.getYear();
       int currentMonth = currentDate.getMonthValue();

       // Define date ranges for the current and last month
       LocalDate firstDayOfMonth = currentDate.with(TemporalAdjusters.firstDayOfMonth());
       LocalDate lastDayOfMonth = currentDate.with(TemporalAdjusters.lastDayOfMonth()).plusDays(1);
       LocalDate firstDayOfLastMonth = currentDate.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
       LocalDate lastDayOfLastMonth = currentDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());

       // Retrieve premium and total actual data for current and last month
       double cmConvertedActualBagPremium = siteManagementService.getSiteConversionSale(tsoUser, firstDayOfMonth.toString(), lastDayOfMonth.toString(), "premium");
       double lmConvertedActualBagPremium = siteManagementService.getSiteConversionSale(tsoUser, firstDayOfLastMonth.toString(), lastDayOfLastMonth.toString(), "premium");
       double cmConvertedActualBagTotal = siteManagementService.getSiteConversionSale(tsoUser, firstDayOfMonth.toString(), lastDayOfMonth.toString(), "total");
       double lmConvertedActualBagTotal = siteManagementService.getSiteConversionSale(tsoUser, firstDayOfLastMonth.toString(), lastDayOfLastMonth.toString(), "total");

       LOG.info(String.format("cmConvertedActualBagPremium for TSO %s :: %f",tsoUser.getUid(),cmConvertedActualBagPremium));
       LOG.info(String.format("lmConvertedActualBagPremium for TSO %s :: %f",tsoUser.getUid(),lmConvertedActualBagPremium));
       LOG.info(String.format("cmConvertedActualBagTotal for TSO %s :: %f",tsoUser.getUid(),cmConvertedActualBagTotal));
       LOG.info(String.format("lmConvertedActualBagTotal for TSO %s :: %f",tsoUser.getUid(),lmConvertedActualBagTotal));

       // Set premium target values
       setPremiumTargets(data, tsoUser);

       // Process the Actual data generated from CRM APP and set values in the data object
       setPremiumActualData(data, tsoUser, currentYear, currentMonth, cmConvertedActualBagPremium, lmConvertedActualBagPremium, cmConvertedActualBagTotal, lmConvertedActualBagTotal);*/

       return data;
    }

    @Override
    public Boolean addTaggedInfluencersForSite(List<String> influencer, String site) throws DuplicateUidException {
       final B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
        SclCustomerModel tsoSite = (SclCustomerModel) userService.getUserForUID(site);
        List<B2BCustomerModel> taggedPartnersList = new ArrayList<>();
        if(influencer!=null) {
            for (String customer : influencer) {
                SclCustomerModel taggedPartner = (SclCustomerModel) userService.getUserForUID(customer);
                /*if (currentUser instanceof SclCustomerModel) {
                    site = (SclCustomerModel) currentUser;
                    if (sclCustomerService.getTaggedPartnersForSite(site) != null && !sclCustomerService.getTaggedPartnersForSite(site).isEmpty())
                        taggedPartnersList = sclCustomerService.getTaggedPartnersForSite(site);
                } else {
                    throw new ModelNotFoundException(NOT_SCL_USER_MESSAGE);
                }*/

                if (null != taggedPartner) {
                    if (taggedPartner.getSclCustomers()!=null && !taggedPartner.getSclCustomers().isEmpty() && taggedPartner.getSclCustomers().contains(tsoSite)) {
                        throw new DuplicateUidException("This user already tagged to the site" + tsoSite.getName());
                    } else {
                        try {
                            taggedPartnersList.add(taggedPartner);
                            tsoSite.setTaggedPartners(taggedPartnersList);
                            modelService.save(tsoSite);
                            return true;
                        } catch (ModelSavingException mse) {
                            LOG.error("Error occurred while updating Site " + tsoSite.getName() + "\n");
                            LOG.error("Exception is: " + mse.getMessage());
                            return false;
                        }
                    }
                } else {
                    throw new ModelNotFoundException("No User found with uid : " + customer);
                }
            }
            return true;
        }
        else {
            return false;
        }
        }


    @Override
    public VisitMasterData endComplaintVisit(String visitId) {
    	VisitMasterModel model = visitMasterDao.findById(visitId);
    	model.setEndVisitTime(new Date());
    	model.setStatus(VisitStatus.COMPLETED);
    	model.setApprovalStatus(ApprovalStatus.AUTO_APPROVED);
    	model.setSynced(false);
    	modelService.save(model);
        EndCustomerComplaintModel customerComplaint = model.getCustomerComplaint();
        if(customerComplaint!=null){
            if(customerComplaint.getNoOfVisitDone()!=null){
                int noOfVisitDone = customerComplaint.getNoOfVisitDone();
                customerComplaint.setNoOfVisitDone(noOfVisitDone+1);
            }
            else{
                customerComplaint.setNoOfVisitDone(1);
            }
            customerComplaint.setLastVisitDate(new Date());
            modelService.saveAll(model, customerComplaint);
        }
        else{
            modelService.save(model);
        }

    	if(model.getCounterVisits()!=null && !model.getCounterVisits().isEmpty()) {
    		ComplaintCounterVisitModel counterVisit = (ComplaintCounterVisitModel) model.getCounterVisits().iterator().next();
    		counterVisit.setEndVisitTime(new Date());    		
    		SclSiteMasterModel site = (SclSiteMasterModel)counterVisit.getSclCustomer();
    		SclUserModel user = (SclUserModel)userService.getCurrentUser();
        	updateTotalVisitDetails(site, user);
    		modelService.saveAll(counterVisit, site, user);

    	}
    	VisitMasterData visitMasterData = new VisitMasterData();
    	visitMasterData.setId(model.getId());
    	return visitMasterData;     
    }


    /**
     * Create a new SiteTransactionModel incase of Site conversion
     * @param site
     * @param siteData
     * @param tsoUser
     * @return newly created SiteTransactionModel incase of Site conversion
     */
    public SiteTransactionModel createSiteTransactionData(SclSiteMasterModel site, MapNewSiteData siteData, SclUserModel tsoUser) {
        SiteTransactionModel siteTransactionModel = modelService.create(SiteTransactionModel.class);
        siteTransactionModel.setId(siteTransactionIdGenerator.generate().toString());
        siteData.setSiteTransactionId(siteTransactionModel.getId());
        LOG.info(String.format("Site Transaction ID %s for TSO %s",siteData.getSiteTransactionId(),tsoUser.getUid()));
        siteTransactionModel.setSite(site);
        siteTransactionReversePopulator.populate(siteData, siteTransactionModel);
        return siteTransactionModel;
    }

    /**
     * Sets the premium target values for the current and last month in the data object.
     *
     * @param data    The SiteManagementHomePageData object to set the values.
     * @param sclUser The current user.
     */
    private void setPremiumTargets(SiteManagementHomePageData data, SclUserModel sclUser) {
        int cmConvertedTargetVisitPremium = siteManagementService.cmConvertedTargetVisitPremium(sclUser);
        data.setCmConvertedTargetVisitPreminum(cmConvertedTargetVisitPremium);

        int lmConvertedTargetVisitPremium = siteManagementService.lmConvertedActualVisitPremium(sclUser);
        data.setLmConvertedTargetVisitPreminum(lmConvertedTargetVisitPremium);

        double cmConvertedTargetBagPremium = siteManagementService.cmConvertedActualBagTotal(sclUser);
        data.setCmConvertedTargetBagPreminum(cmConvertedTargetBagPremium);

        double lmConvertedTargetBagPremium = siteManagementService.lmConvertedActualBagTotal(sclUser);
        data.setLmConvertedTargetBagPreminum(lmConvertedTargetBagPremium);
    }

    /**
     * Processes the premium and actual data for the current and last month and sets the values in the data object.
     *
     * @param data                      The SiteManagementHomePageData object to set the values.
     * @param sclUser                   The current user.
     * @param currentYear               The current year.
     * @param currentMonth              The current month.
     * @param cmConvertedActualBagPremium The premium value for the current month.
     * @param lmConvertedActualBagPremium The premium value for the last month.
     * @param cmConvertedActualBagTotal   The total bag value for the current month.
     * @param lmConvertedActualBagTotal   The total bag value for the last month.
     */
    private void setPremiumActualData(SiteManagementHomePageData data, SclUserModel sclUser, int currentYear, int currentMonth,
                                    double cmConvertedActualBagPremium, double lmConvertedActualBagPremium, double cmConvertedActualBagTotal, double lmConvertedActualBagTotal) {

        int cmConvertedActualVisitTotal = 0;
        int lmConvertedActualVisitTotal = 0;
        int cmConvertedActualVisitPremium = 0;
        int lmConvertedActualVisitPremium = 0;

        List<List<Object>> premiumData = siteManagementService.getTotalPremiumOfSitesAndBags(sclUser);

        for (List<Object> record : premiumData) {
            if (record.get(0) != null && record.get(1) != null) {
                int year = (int) record.get(0);
                int month = (int) record.get(1);
                int visit = record.get(3) != null ? (int) record.get(3) : 0;
                boolean isPremium = record.get(2) != null && (boolean) record.get(2);

                if (currentYear == year && currentMonth == month) {
                    cmConvertedActualVisitTotal += visit;
                    if (isPremium) {
                        cmConvertedActualVisitPremium = visit;
                    }
                } else {
                    lmConvertedActualVisitTotal += visit;
                    if (isPremium) {
                        lmConvertedActualVisitPremium = visit;
                    }
                }
            }
        }

        // Set the current month values in the data object
        data.setCmConvertedActualBagTotal(cmConvertedActualBagTotal);
        data.setCmConvertedActualVisitTotal(cmConvertedActualVisitTotal);
        data.setCmConvertedActualBagPreminum(cmConvertedActualBagPremium);
        data.setCmConvertedActualVisitPreminum(cmConvertedActualVisitPremium);

        // Set the last month values in the data object
        data.setLmConvertedActualBagTotal(lmConvertedActualBagTotal);
        data.setLmConvertedActualVisitTotal(lmConvertedActualVisitTotal);
        data.setLmConvertedTargetBagPreminum(lmConvertedActualBagPremium);
        data.setLmConvertedActualVisitPreminum(lmConvertedActualVisitPremium);
    }
  
    @Override
    public List<String> getSiteMasterListTaluka(){
        return siteManagementService.getSiteMasterListTaluka();
    }

}
