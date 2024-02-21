package com.eydms.core.services.impl;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.dao.DataConstraintDao;
import com.eydms.core.dao.SiteManagementDao;
import com.eydms.core.dao.TechnicalAssistanceDao;
import com.eydms.core.enums.CounterType;
import com.eydms.core.enums.TypeOfVisitMaster;
import com.eydms.core.enums.VisitStatus;
import com.eydms.core.model.*;
import com.eydms.core.services.DJPVisitService;
import com.eydms.core.services.SiteManagementService;
import com.eydms.core.services.TerritoryManagementService;
import com.eydms.facades.data.MapNewSiteData;
import com.eydms.facades.data.EyDmsSiteMasterData;
import com.eydms.facades.data.SiteRequestData;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class SiteManagementServiceImpl implements SiteManagementService {
    private static final Logger LOG = Logger.getLogger(SiteManagementServiceImpl.class);

    @Autowired
    SiteManagementDao siteManagementDao;

    @Autowired
    DJPVisitService djpVisitService;
    
    @Autowired
    UserService userService;
    
    @Autowired
    TerritoryManagementService territoryManagementService;

    @Autowired
    ModelService modelService;
    
    @Autowired
    KeyGenerator counterVisitIdGenerator;
    @Autowired
    DataConstraintDao dataConstraintDao;

    @Autowired
    TechnicalAssistanceDao technicalAssistanceDao;
    
    @Autowired
    private Converter<EyDmsSiteMasterModel, EyDmsSiteMasterData> eydmsSiteMasterConverter;

    public List<SiteServiceTypeModel> getSiteServiceType(){
        return siteManagementDao.getSiteServiceType() ;
    }

    @Override
    public List<SiteServiceTestModel> getSiteServiceTest(String serviceTypeCode) {
        return siteManagementDao.getSiteServiceTest(serviceTypeCode);
    }

    @Override
    public List<SiteCategoryTypeModel> getSiteCategoryType() {
        return siteManagementDao.getSiteCategoryType();
    }

    @Override
    public List<SiteCementTypeModel> getSiteCementType(String siteCategoryType) {
        return siteManagementDao.getSiteCementType(siteCategoryType);
    }

    @Override
    public List<SiteCementBrandModel> getSiteCementBrand(String siteCementType) {
        return siteManagementDao.getSiteCementBrand(siteCementType);
    }

    @Override
    public Double getActualTargetForSalesMTD(EyDmsUserModel eydmsUser) {

        return  siteManagementDao.getActualTargetForSalesMTD(eydmsUser);
    }

    @Override
    public Double getMonthlySalesTarget(EyDmsUserModel eydmsUser) {
        return siteManagementDao.getMonthlySalesTarget(eydmsUser);
    }

    @Override
    public Double getLastMonthSalesTarget(EyDmsUserModel eydmsUser) {
        return siteManagementDao.getLastMonthSalesTarget(eydmsUser);
    }

    @Override
    public Integer getNewSiteVists(EyDmsUserModel user) {
        return siteManagementDao.getNewSiteVists(user);
    }

    @Override
    public Integer getNewSiteVistsForLastMonth(EyDmsUserModel user) {
        return siteManagementDao.getNewSiteVistsForLastMonth(user);
    }

    @Override
    public List<List<Object>> getSiteTypeStagesCount(EyDmsUserModel user) {

        return siteManagementDao.getSiteTypeStagesCount(user);
    }
    
	@Override
    public VisitMasterModel createAndStartComplaintVisit(String siteId, String requestId) {
        EndCustomerComplaintModel complaint = null;
        if(requestId!=null) {
            complaint = technicalAssistanceDao.getEndCustomerComplaintForRequestNo(requestId);
            Integer visitsPerDay = dataConstraintDao.findDaysByConstraintName("COMPLAINT_VISITS_PER_DAY");
            if(visitsPerDay>0 && complaint.getLastVisitDate()!=null) {
                LocalDate vistdate =  complaint.getLastVisitDate().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                if(LocalDate.now().toString().equals(vistdate.toString())) {
                    throw new IllegalArgumentException("Site is already visited for today");
                }

            }

            EyDmsUserModel eydmsUser = (EyDmsUserModel) userService.getCurrentUser();
            EyDmsSiteMasterModel site = (EyDmsSiteMasterModel) userService.getUserForUID(siteId);
            List<SubAreaMasterModel> subareas = territoryManagementService.getTerritoriesForCustomer(site);
            SubAreaMasterModel subMasterModel = CollectionUtils.isNotEmpty(subareas)?subareas.get(0):null;

            DateFormat dateFormat = new SimpleDateFormat(EyDmsCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
            String planDate = dateFormat.format(new Date());

            VisitMasterModel visitModel = djpVisitService.createVisitMasterData(null, null, eydmsUser, subMasterModel.getPk().toString(), planDate);
            visitModel.setVisitType(TypeOfVisitMaster.COMPLAINT_VISIT);
            visitModel.setStatus(VisitStatus.STARTED);
            visitModel.setStartVisitTime(new Date());
            if(complaint!=null) {
                visitModel.setCustomerComplaint(complaint);
            }
            ComplaintCounterVisitModel complaintCounterVisit = createComplaintCounterVisitData(site);
            complaintCounterVisit.setVisit(visitModel);

            modelService.saveAll(visitModel, complaintCounterVisit, site);

            return visitModel;
        }

        return null;

    }

    public ComplaintCounterVisitModel createComplaintCounterVisitData(EyDmsSiteMasterModel site) {
		ComplaintCounterVisitModel counterVisit = modelService.create(ComplaintCounterVisitModel.class);
		counterVisit.setId(counterVisitIdGenerator.generate().toString());
		counterVisit.setSequence(1);
		counterVisit.setEyDmsCustomer(site);
		counterVisit.setCounterType(CounterType.SITE);
		counterVisit.setStartVisitTime(new Date());
		return counterVisit;
	}

    @Override
    public SearchPageData<EyDmsSiteMasterData> getPaginatedSiteMasterList(SearchPageData searchPageData, SiteRequestData siteRequestData,Boolean plannedVisitForToday) {
        final SearchPageData<EyDmsSiteMasterData> results = new SearchPageData<>();
        List<EyDmsSiteMasterData> list = new ArrayList<>();
        SearchPageData<EyDmsSiteMasterModel> eydmsSiteMasterList = siteManagementDao.getPaginatedSiteMasterList(searchPageData,siteRequestData, plannedVisitForToday);
        if(eydmsSiteMasterList.getResults()!=null && !eydmsSiteMasterList.getResults().isEmpty())
        {
            list= eydmsSiteMasterConverter.convertAll(eydmsSiteMasterList.getResults());
        }
        results.setSorts(eydmsSiteMasterList.getSorts());
        results.setResults(list);
        results.setPagination(eydmsSiteMasterList.getPagination());
        return results;
    }

    @Override
    public List<List<Object>> getTotalPremiumOfSitesAndBags(EyDmsUserModel eydmsUser) {

        return siteManagementDao.getNumberOfBagsPurchased(eydmsUser);
    }


    @Override
    public Integer cmConvertedTargetVisitPremium(EyDmsUserModel eydmsUser) {
        LocalDate localDate=LocalDate.now();
        int currentYear=localDate.getYear();
        int currentMonth = localDate.getMonthValue();
        return siteManagementDao.cmConvertedTargetVisitPremium(eydmsUser,currentMonth,currentYear);
    }

    @Override
    public Integer lmConvertedActualVisitPremium(EyDmsUserModel eydmsUser) {
        LocalDate localDate=LocalDate.now().minusMonths(1);
        int currentYear=localDate.getYear();
        int lastMonth = localDate.getMonthValue();
        return siteManagementDao.cmConvertedTargetVisitPremium(eydmsUser,lastMonth,currentYear);

    }

    @Override
    public Double cmConvertedActualBagTotal(EyDmsUserModel eydmsUser) {
        LocalDate localDate=LocalDate.now();
        int currentYear=localDate.getYear();
        int currentMonth = localDate.getMonthValue();
        return siteManagementDao.cmConvertedActualBagTotal(eydmsUser,currentMonth,currentYear);

    }

    @Override
    public Double lmConvertedActualBagTotal(EyDmsUserModel eydmsUser) {
        LocalDate localDate=LocalDate.now().minusMonths(1);
        int currentYear=localDate.getYear();
        int lastMonth = localDate.getMonthValue();
        return siteManagementDao.cmConvertedActualBagTotal(eydmsUser,lastMonth,currentYear);

    }

    @Override
    public Double getTotalPremiumOfSite(EyDmsUserModel eydmsUser,String startDate,String endDate) {
        return siteManagementDao.getTotalPremiumOfSite(eydmsUser, startDate, endDate);
    }
}
