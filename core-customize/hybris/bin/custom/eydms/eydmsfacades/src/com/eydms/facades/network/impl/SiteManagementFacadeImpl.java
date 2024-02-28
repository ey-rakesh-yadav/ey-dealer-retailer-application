package com.eydms.facades.network.impl;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.customer.services.EyDmsCustomerService;
import com.eydms.core.dao.DataConstraintDao;
import com.eydms.core.dao.VisitMasterDao;
import com.eydms.core.enums.*;
import com.eydms.core.model.*;
import com.eydms.core.services.DJPVisitService;
import com.eydms.core.services.SiteManagementService;
import com.eydms.core.services.TechnicalAssistanceService;
import com.eydms.core.services.TerritoryManagementService;
import com.eydms.facades.data.*;
import com.eydms.facades.network.SiteManagementFacade;
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
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

public class SiteManagementFacadeImpl implements SiteManagementFacade {

    private static final Logger LOG = Logger.getLogger(SiteManagementFacadeImpl.class);

    private static final String NOT_EYDMS_USER_MESSAGE = "Current user is not an EYDMS user";
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
    private Populator<MapNewSiteData, EyDmsSiteMasterModel> siteReversePopulator;

    @Autowired
    UserService userService;

    @Autowired
    TerritoryManagementService territoryManagementService;

    @Autowired
    KeyGenerator counterVisitIdGenerator;

    @Autowired
    B2BUnitService b2bUnitService;

    @Autowired
    private EyDmsCustomerService eydmsCustomerService;

    @Autowired
    private EnumerationService enumerationService;

    @Autowired
    private Populator<MapNewSiteData, SiteVisitMasterModel> siteVisitReversePopulator;

    @Autowired
    private Populator<AddressData, AddressModel> addressReversePopulator;

    @Autowired
    private Converter<EyDmsSiteMasterModel, MapNewSiteData> siteConverter;

    @Autowired
    TechnicalAssistanceService technicalAssistanceService;
    
    @Autowired
    VisitMasterDao visitMasterDao;

    @Autowired
    DataConstraintDao dataConstraintDao;
    
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
        List<SiteCategoryTypeModel> list = getSiteManagementService().getSiteCategoryType();
        List<DropdownData> dropdownDataList = new ArrayList<>();
        for (SiteCategoryTypeModel model : list) {
            DropdownData dropdownData = new DropdownData();
            dropdownData.setCode(model.getCode());
            dropdownData.setName(model.getName());

            dropdownDataList.add(dropdownData);
        }
        dropdownListData.setDropdown(dropdownDataList);
        return dropdownListData;
    }

    @Override
    public DropdownListData getSiteCementType(String siteCategoryType) {
        DropdownListData dropdownListData = new DropdownListData();
        List<SiteCementTypeModel> list = getSiteManagementService().getSiteCementType(siteCategoryType);
        List<DropdownData> dropdownDataList = new ArrayList<>();
        for (SiteCementTypeModel model : list) {
            DropdownData dropdownData = new DropdownData();
            dropdownData.setCode(model.getCode());
            dropdownData.setName(model.getName());

            dropdownDataList.add(dropdownData);
        }
        dropdownListData.setDropdown(dropdownDataList);
        return dropdownListData;
    }

    @Override
    public DropdownListData getSiteCementBrand(String siteCementType) {
        DropdownListData dropdownListData = new DropdownListData();
        List<SiteCementBrandModel> list = getSiteManagementService().getSiteCementBrand(siteCementType);
        List<DropdownData> dropdownDataList = new ArrayList<>();
        for (SiteCementBrandModel model : list) {
            DropdownData dropdownData = new DropdownData();
            dropdownData.setCode(model.getCode());
            dropdownData.setName(model.getName());

            dropdownDataList.add(dropdownData);
        }
        dropdownListData.setDropdown(dropdownDataList);
        return dropdownListData;
    }

    @Override
    public MapNewSiteData mapNewSite(MapNewSiteData siteData) {
    	EyDmsUserModel user = (EyDmsUserModel) userService.getCurrentUser();
    	String siteId = "";
    	EyDmsSiteMasterModel siteToAdd = null;
    	CustomerSubAreaMappingModel customerSubAreaMapping = null;
    	AddressModel newAddress = null;
    	String state = StringUtils.EMPTY;
    	String taluka = StringUtils.EMPTY;
    	String district = StringUtils.EMPTY;

    	LocalDate current = LocalDate.now();
    	Date currentDate = Date.from(current.atStartOfDay(ZoneId.systemDefault()).toInstant());

    	if (Objects.nonNull(siteData)) {
    		if (siteData.getSiteId() != null) {
    			siteToAdd = (EyDmsSiteMasterModel) userService.getUserForUID(siteData.getSiteId());
    			AddressModel address = siteToAdd.getAddresses().iterator().next();
    			if (address != null) {
    				state = address.getState();
    				taluka = address.getTaluka();
    				district = address.getDistrict();
    			}

    			if(!siteData.getIsSiteConverted()) {
    				Integer visitsPerDay = dataConstraintDao.findDaysByConstraintName("VISITS_PER_DAY");
    				if(visitsPerDay!=0) {
    					if (siteToAdd.getLastVisitTime()!=null && siteToAdd.getLastVisitTime().equals(currentDate)) {
    						throw new IllegalArgumentException("Site is already visited for today");
    					}
    				}
    			}
    		} else {
    			siteToAdd = modelService.create(EyDmsSiteMasterModel.class);
    			siteToAdd.setUid(String.valueOf(customCodeGenerator.generate()));
    			siteData.setSiteId(siteToAdd.getUid());
    			siteToAdd.setCounterType(CounterType.SITE);
    			siteToAdd.setTypeOfVisit(TypeOfVisit.valueOf(siteData.getTypeOfVisit()));
    			siteToAdd.setCreatedBy(user);
    			populateDummyEmail(siteToAdd);
    			populateDefaultUnitAndGroup(siteToAdd, siteData.getBrand(), EyDmsCoreConstants.CUSTOMER.SITE_USER_GROUP_UID);
    			AddressData addressData = siteData.getSiteAddress();
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
    			}

    			customerSubAreaMapping = djpVisitService.createCustomerSubAreaMapping(addressData.getState(), addressData.getDistrict(), addressData.getTaluka(), siteToAdd, (CMSSiteModel) baseSiteService.getCurrentBaseSite());
    		}
    		siteReversePopulator.populate(siteData, siteToAdd);
    		siteToAdd.setSynced(false);
    		SubAreaMasterModel subArea = territoryManagementService.getTerritoryByDistrictAndTaluka(district, taluka);
    		updateTotalSalesDetails(siteToAdd);

    		if(!siteData.getIsSiteConverted()) {
    			DateFormat dateFormat = new SimpleDateFormat(EyDmsCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
    			String planDate = dateFormat.format(new Date());
    			TechnicalAssistanceModel technicalAssistanceModel =  null;
    			VisitMasterModel visit = djpVisitService.createVisitMasterData(null, null, user, subArea.getPk().toString(), planDate);
    			if(StringUtils.isNotBlank(siteData.getTechnicalAssistanceRequestNo())) {
    				visit.setVisitType(TypeOfVisitMaster.ASSITANCE_VISIT);
    				technicalAssistanceModel = technicalAssistanceService.getTechnicalAssistanceRequestDetails(siteData.getTechnicalAssistanceRequestNo());
    				if(technicalAssistanceModel!=null) {
    					visit.setTechnicalAssistance(technicalAssistanceModel);
    					technicalAssistanceModel.setSite(siteToAdd);
    					technicalAssistanceModel.setLastVisitedDate(new Date());
    					technicalAssistanceModel.setLastVisit(visit);
    				}
    			}else {
    				visit.setVisitType(TypeOfVisitMaster.TRADE_VISIT);
    			}
    			visit.setEndVisitTime(new Date());
    			visit.setStatus(VisitStatus.COMPLETED);

    			SiteVisitMasterModel siteVisit = createCounterVisitMasterData(siteToAdd, siteData);
    			siteVisit.setVisit(visit);
    			siteToAdd.setLastCounterVisit(siteVisit);
    			updateTotalVisitDetails(siteToAdd, user);
    			if (customerSubAreaMapping != null)
    				modelService.saveAll(siteToAdd, customerSubAreaMapping, visit, siteVisit, user);
    			else
    				modelService.saveAll(siteToAdd, visit, siteVisit, user);
    			if(technicalAssistanceModel!=null) {
    				modelService.save(technicalAssistanceModel);
    			}
    		}
    		else {
    			if (customerSubAreaMapping != null)
    				modelService.saveAll(siteToAdd, customerSubAreaMapping, user);
    			else
    				modelService.saveAll(siteToAdd, user);
    		}

    		if (newAddress != null)
    			customerAccountService.saveAddressEntry(siteToAdd, newAddress);
    	}

    	if (siteToAdd != null) {
    		siteId = siteToAdd.getUid();
    		siteData.setSiteId(siteId);
    	}
    	return siteData;
    }

    @Override
    public SiteManagementHomePageData getTotalAndActualTargetForSiteVisit(String filter) {

        EyDmsUserModel user = (EyDmsUserModel) userService.getCurrentUser();
        SiteManagementHomePageData data = new SiteManagementHomePageData();
        String constructionStage = null;
        String code = null;
        int counts = 0;
       if(StringUtils.isBlank(filter))
       {
           LocalDate cal = LocalDate.now();
           int noOfDaysInTheMonth = cal.lengthOfMonth();
           int noOfDaysGoneByInTheMonth = cal.getDayOfMonth();

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
           data.setSiteVisitActual(actualTarget);

           totalTarget = getSiteManagementService().getMonthlySalesTarget(user);
           data.setSiteVisitTarget(totalTarget != 0.0 ? totalTarget : 0.0);

           if (actualTarget != 0.0 && totalTarget != 0.0)
               achievementPercentage = (actualTarget / totalTarget) * 100;
           data.setSiteVisitPercentage(achievementPercentage != 0.0 ? achievementPercentage : 0.0);

           proratedTarget = (totalTarget / noOfDaysInTheMonth) * noOfDaysGoneByInTheMonth;

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
               LocalDate cal = LocalDate.now().minusMonths(1);
               int noOfDaysInTheMonth = cal.lengthOfMonth();
               int noOfDaysGoneByInTheMonth = cal.getDayOfMonth();

               double actualTarget = 0.0, totalTarget = 0.0, proratedTarget = 0.0;
               int newSitesVisits = 0, existingSitesVisits = 0;
               double achievementPercentage = 0.0, proratedAchievementPercentage = 0.0;

               if (user.getLastSiteVisitDate() != null) {
                   Instant instant = user.getLastSiteVisitDate().toInstant();
                   LocalDate lastVisitDate = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                   if (lastVisitDate.getMonthValue() == cal.getMonthValue() && cal.getYear() == lastVisitDate.getYear() && user.getLastMonthSiteVisit() != null) {
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

    public SiteVisitMasterModel createCounterVisitMasterData(EyDmsSiteMasterModel site, MapNewSiteData siteData) {

        SiteVisitMasterModel siteVisitMasterModel = modelService.create(SiteVisitMasterModel.class);
        siteVisitMasterModel.setId(counterVisitIdGenerator.generate().toString());
        siteVisitMasterModel.setSequence(1);
        siteVisitMasterModel.setEyDmsCustomer(site);
        siteVisitMasterModel.setCounterType(CounterType.SITE);
        siteVisitMasterModel.setStartVisitTime(new Date());
        siteVisitMasterModel.setEndVisitTime(new Date());
        siteVisitReversePopulator.populate(siteData, siteVisitMasterModel);
        return siteVisitMasterModel;
    }

    private void populateDummyEmail(EyDmsCustomerModel siteToAdd) {
        Random random = new Random();
        int randomInt = random.nextInt(999999);
        siteToAdd.setEmail("dummyemail_" + randomInt + "@gmail.com");
    }

    private void populateDefaultUnitAndGroup(EyDmsCustomerModel customer, String brand, String userGroupUid) {

        Set<PrincipalGroupModel> groups = new HashSet<>();
        String defaultUnitId = StringUtils.EMPTY;
        if (StringUtils.isNotBlank(brand)) {
            switch (brand) {
                case EyDmsCoreConstants.SITE.SHREE_SITE:
                    defaultUnitId = EyDmsCoreConstants.B2B_UNIT.EYDMS_SHREE_UNIT_UID;
                    break;
                case EyDmsCoreConstants.SITE.BANGUR_SITE:
                    defaultUnitId = EyDmsCoreConstants.B2B_UNIT.EYDMS_BANGUR_UNIT_UID;
                    break;
                case EyDmsCoreConstants.SITE.ROCKSTRONG_SITE:
                    defaultUnitId = EyDmsCoreConstants.B2B_UNIT.EYDMS_ROCKSTRONG_UNIT_UID;
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
            UserGroupModel eydmsSiteGroup = userService.getUserGroupForUID(userGroupUid);
            groups.add(eydmsSiteGroup);
        } catch (UnknownIdentifierException ex) {
            LOG.error(String.format("User group not found with uid %s", userGroupUid));
        }
        customer.setGroups(groups);
    }


    private void updateTotalVisitDetails(EyDmsSiteMasterModel site, EyDmsUserModel user) {
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

        //EyDms user

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
    
    private void updateTotalSalesDetails(EyDmsSiteMasterModel site) {
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
            } else if (lastFormSubmitYear == currentYear && lastFormSubmitMonth == currentMonth) {
                site.setCurrentMonthSale(site.getCurrentMonthSale() != null ? site.getCurrentMonthSale() + site.getNumberOfBagsPurchased() : site.getNumberOfBagsPurchased());
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
    public SearchPageData<EyDmsSiteMasterData> getPaginatedSiteMasterList(SearchPageData searchPageData, SiteRequestData siteRequestData,Boolean plannedVisitForToday) {
        return siteManagementService.getPaginatedSiteMasterList(searchPageData, siteRequestData, plannedVisitForToday);
    }

    @Override
    public EyDmsSiteMasterData toCloseTheSite(String siteId, String closeComment) {
        EyDmsSiteMasterData data = new EyDmsSiteMasterData();
        EyDmsSiteMasterModel siteMasterModel = null;
        if (siteId != null) {
            siteMasterModel = (EyDmsSiteMasterModel) userService.getUserForUID(siteId);
            siteMasterModel.setSiteStatus(SiteStatus.CLOSED);
            siteMasterModel.setClosedBy((B2BCustomerModel) userService.getCurrentUser());
            siteMasterModel.setClosedDate(new Date());
            siteMasterModel.setClosedComment(closeComment);
            modelService.save(siteMasterModel);
        }
        if (siteMasterModel != null)
            data.setCode(siteMasterModel.getUid());
        return data;
    }

    @Override
    public MapNewSiteData getSiteDetailsById(String siteId) {
        MapNewSiteData mapNewSiteData = new MapNewSiteData();
        EyDmsSiteMasterModel siteMasterModel=null;
        if(siteId!=null){
            siteMasterModel= (EyDmsSiteMasterModel) userService.getUserForUID(siteId);
            mapNewSiteData = siteConverter.convert(siteMasterModel);

        }
        return mapNewSiteData;
    }
    
   @Override
    public SiteManagementHomePageData getTotalPremiumOfSitesAndBags() {
        SiteManagementHomePageData data = new SiteManagementHomePageData();
        EyDmsUserModel eydmsUser = (EyDmsUserModel) userService.getCurrentUser();
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

            cmConvertedTargetVisitPreminum = siteManagementService.cmConvertedTargetVisitPremium(eydmsUser);
            if(cmConvertedTargetVisitPreminum!=0) {
                data.setCmConvertedTargetVisitPreminum(cmConvertedTargetVisitPreminum);
            }
            else
                data.setCmConvertedTargetVisitPreminum(0);
            lmConvertedTargetVisitPreminum = siteManagementService.lmConvertedActualVisitPremium(eydmsUser);
            if(lmConvertedTargetVisitPreminum!=0) {
                data.setLmConvertedTargetVisitPreminum(lmConvertedTargetVisitPreminum);
            }
            else
                data.setLmConvertedTargetVisitPreminum(0);
            cmConvertedTargetBagPreminum = siteManagementService.cmConvertedActualBagTotal(eydmsUser);
            if(cmConvertedTargetBagPreminum!=0.0) {
                data.setCmConvertedTargetBagPreminum(cmConvertedTargetBagPreminum);
            }
            else
                data.setCmConvertedTargetBagPreminum(0.0);
            lmConvertedTargetBagPreminum = siteManagementService.lmConvertedActualBagTotal(eydmsUser);
            if(lmConvertedTargetBagPreminum!=0.0) {
                data.setLmConvertedTargetBagPreminum(lmConvertedTargetBagPreminum);
            }
            else
                data.setLmConvertedTargetBagPreminum(0.0);

       LocalDate firstDayOfMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
       LocalDate lastDayOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).plusDays(1);
       Double count = siteManagementService.getTotalPremiumOfSite(eydmsUser,firstDayOfMonth.toString(),lastDayOfMonth.toString());
       LOG.info("count ::" + count);
       LocalDate firstDayOfLastMonth = LocalDate.now().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
       LocalDate lastDayOfLastMonth = LocalDate.now().minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
       Double count1 = siteManagementService.getTotalPremiumOfSite(eydmsUser,firstDayOfLastMonth.toString(),lastDayOfLastMonth.toString());
       LOG.info("count1::" + count1);
       cmConvertedActualBagPreminum=count;
       lmConvertedActualBagPreminum=count1;

            List<List<Object>> premium = siteManagementService.getTotalPremiumOfSitesAndBags(eydmsUser);
            for (List<Object> objects : premium) {
            	if(objects.get(0)!=null && objects.get(1)!=null) {
            		int year = (int) objects.get(0);
            		int month = (int) objects.get(1);
            		double bag = objects.get(4)!=null?(double) objects.get(4):0.0;
            		int visit = objects.get(3)!=null?(int) objects.get(3):0;
            		boolean isPremium = objects.get(2)!=null?(boolean)objects.get(2):false;
            		if (currentYear == year && currentMonth == month) {
                        cmConvertedActualBagTotal += bag;
                        LOG.info("Bags ::" +cmConvertedActualBagTotal);
            			cmConvertedActualVisitTotal += visit;
            			if (isPremium) {
                            cmConvertedActualBagPreminum += bag ;
                            LOG.info("Bags1 ::" +cmConvertedActualBagPreminum);
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
            double cmConvertedActualBagTotalLast = cmConvertedActualBagTotal+count;
        data.setCmConvertedActualBagTotal(cmConvertedActualBagTotalLast != 0.0 ? cmConvertedActualBagTotalLast : 0.0);
        data.setCmConvertedActualVisitTotal(cmConvertedActualVisitTotal != 0 ? cmConvertedActualVisitTotal : 0);
        data.setCmConvertedActualBagPreminum(cmConvertedActualBagPreminum != 0.0 ? cmConvertedActualBagPreminum : 0.0);
        data.setCmConvertedActualVisitPreminum(cmConvertedActualVisitPreminum != 0 ? cmConvertedActualVisitPreminum : 0);
        data.setLmConvertedActualBagTotal(lmConvertedActualBagTotal != 0.0 ? lmConvertedActualBagTotal : 0.0);
        data.setLmConvertedActualVisitTotal(lmConvertedActualVisitTotal != 0 ? lmConvertedActualVisitTotal : 0);
        data.setLmConvertedTargetBagPreminum(lmConvertedActualBagPreminum != 0.0 ? lmConvertedActualBagPreminum : 0.0);
        data.setLmConvertedActualVisitPreminum(lmConvertedActualVisitPreminum != 0 ? lmConvertedActualVisitPreminum : 0);

        return data;

    }

    @Override
    public Boolean addTaggedInfluencersForSite(List<String> influencer, String site) throws DuplicateUidException {
       final B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
        EyDmsCustomerModel tsoSite = (EyDmsCustomerModel) userService.getUserForUID(site);
        List<B2BCustomerModel> taggedPartnersList = new ArrayList<>();
        if(influencer!=null) {
            for (String customer : influencer) {
                EyDmsCustomerModel taggedPartner = (EyDmsCustomerModel) userService.getUserForUID(customer);
                /*if (currentUser instanceof EyDmsCustomerModel) {
                    site = (EyDmsCustomerModel) currentUser;
                    if (eydmsCustomerService.getTaggedPartnersForSite(site) != null && !eydmsCustomerService.getTaggedPartnersForSite(site).isEmpty())
                        taggedPartnersList = eydmsCustomerService.getTaggedPartnersForSite(site);
                } else {
                    throw new ModelNotFoundException(NOT_EYDMS_USER_MESSAGE);
                }*/

                if (null != taggedPartner) {
                    if (taggedPartner.getEyDmsCustomers()!=null && !taggedPartner.getEyDmsCustomers().isEmpty() && taggedPartner.getEyDmsCustomers().contains(tsoSite)) {
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
    		EyDmsSiteMasterModel site = (EyDmsSiteMasterModel)counterVisit.getEyDmsCustomer();
    		EyDmsUserModel user = (EyDmsUserModel)userService.getCurrentUser();
        	updateTotalVisitDetails(site, user);
    		modelService.saveAll(counterVisit, site, user);

    	}
    	VisitMasterData visitMasterData = new VisitMasterData();
    	visitMasterData.setId(model.getId());
    	return visitMasterData;     
    }

}
