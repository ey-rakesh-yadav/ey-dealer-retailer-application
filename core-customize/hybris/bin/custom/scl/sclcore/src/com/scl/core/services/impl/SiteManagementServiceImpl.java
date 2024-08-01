package com.scl.core.services.impl;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.DataConstraintDao;
import com.scl.core.dao.SiteManagementDao;
import com.scl.core.dao.TechnicalAssistanceDao;
import com.scl.core.enums.*;
import com.scl.core.model.*;
import com.scl.core.services.DJPVisitService;
import com.scl.core.services.SiteManagementService;
import com.scl.core.services.TerritoryManagementService;
import com.scl.facades.data.SclSiteMasterData;
import com.scl.facades.data.SiteRequestData;
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
    private Converter<SclSiteMasterModel, SclSiteMasterData> sclSiteMasterConverter;

    public List<SiteServiceTypeModel> getSiteServiceType() {
        return siteManagementDao.getSiteServiceType();
    }

    @Override
    public List<SiteServiceTestModel> getSiteServiceTest(String serviceTypeCode) {
        return siteManagementDao.getSiteServiceTest(serviceTypeCode);
    }

    @Override
    public List<CompetitorProductType> getSiteCategoryType() {
        return siteManagementDao.getSiteCategoryType();
    }

    @Override
    public List<PremiumProductType> getSiteCementType(String siteCategoryType) {
        return siteManagementDao.getSiteCementType(siteCategoryType);
    }

    @Override
    public List<CompetitorProductModel> getSiteCementBrand(String siteCementType) {
        return siteManagementDao.getSiteCementBrand(siteCementType);
    }

    @Override
    public Double getActualTargetForSalesMTD(SclUserModel sclUser) {

        return siteManagementDao.getActualTargetForSalesMTD(sclUser);
    }

    @Override
    public Double getMonthlySalesTarget(SclUserModel sclUser) {
        return siteManagementDao.getMonthlySalesTarget(sclUser);
    }

    @Override
    public Double getLastMonthSalesTarget(SclUserModel sclUser) {
        return siteManagementDao.getLastMonthSalesTarget(sclUser);
    }

    @Override
    public Integer getNewSiteVists(SclUserModel user) {
        return siteManagementDao.getNewSiteVists(user);
    }

    @Override
    public Integer getNewSiteVistsForLastMonth(SclUserModel user) {
        return siteManagementDao.getNewSiteVistsForLastMonth(user);
    }

    @Override
    public List<List<Object>> getSiteTypeStagesCount(SclUserModel user) {

        return siteManagementDao.getSiteTypeStagesCount(user);
    }

    @Override
    public VisitMasterModel createAndStartComplaintVisit(String siteId, String requestId) {
        EndCustomerComplaintModel complaint = null;
        if (requestId != null) {
            complaint = technicalAssistanceDao.getEndCustomerComplaintForRequestNo(requestId);
            Integer visitsPerDay = dataConstraintDao.findDaysByConstraintName("COMPLAINT_VISITS_PER_DAY");
            if (visitsPerDay > 0 && complaint.getLastVisitDate() != null) {
                LocalDate vistdate = complaint.getLastVisitDate().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                if (LocalDate.now().toString().equals(vistdate.toString())) {
                    throw new IllegalArgumentException("Site is already visited for today");
                }

            }

            SclUserModel sclUser = (SclUserModel) userService.getCurrentUser();
            SclSiteMasterModel site = (SclSiteMasterModel) userService.getUserForUID(siteId);
            List<SubAreaMasterModel> subareas = territoryManagementService.getTerritoriesForCustomer(site);
            SubAreaMasterModel subMasterModel = CollectionUtils.isNotEmpty(subareas) ? subareas.get(0) : null;

            DateFormat dateFormat = new SimpleDateFormat(SclCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
            String planDate = dateFormat.format(new Date());

            VisitMasterModel visitModel = djpVisitService.createVisitMasterData(null, null, sclUser, subMasterModel.getPk().toString(), planDate);
            visitModel.setVisitType(TypeOfVisitMaster.COMPLAINT_VISIT);
            visitModel.setStatus(VisitStatus.STARTED);
            visitModel.setStartVisitTime(new Date());
            if (complaint != null) {
                visitModel.setCustomerComplaint(complaint);
            }
            ComplaintCounterVisitModel complaintCounterVisit = createComplaintCounterVisitData(site);
            complaintCounterVisit.setVisit(visitModel);

            modelService.saveAll(visitModel, complaintCounterVisit, site);

            return visitModel;
        }

        return null;

    }

    public ComplaintCounterVisitModel createComplaintCounterVisitData(SclSiteMasterModel site) {
        ComplaintCounterVisitModel counterVisit = modelService.create(ComplaintCounterVisitModel.class);
        counterVisit.setId(counterVisitIdGenerator.generate().toString());
        counterVisit.setSequence(1);
        counterVisit.setSclCustomer(site);
        counterVisit.setCounterType(CounterType.SITE);
        counterVisit.setStartVisitTime(new Date());
        return counterVisit;
    }

    @Override
    public SearchPageData<SclSiteMasterData> getPaginatedSiteMasterList(SearchPageData searchPageData, SiteRequestData siteRequestData, Boolean plannedVisitForToday, List<String> filterBySubAreas) {
        final SearchPageData<SclSiteMasterData> results = new SearchPageData<>();
        List<SclSiteMasterData> list = new ArrayList<>();
        SearchPageData<SclSiteMasterModel> sclSiteMasterList = siteManagementDao.getPaginatedSiteMasterList(searchPageData, siteRequestData, plannedVisitForToday, filterBySubAreas);
        if (sclSiteMasterList.getResults() != null && !sclSiteMasterList.getResults().isEmpty()) {
            list = sclSiteMasterConverter.convertAll(sclSiteMasterList.getResults());
        }
        results.setSorts(sclSiteMasterList.getSorts());
        results.setResults(list);
        results.setPagination(sclSiteMasterList.getPagination());
        return results;
    }

    @Override
    public List<List<Object>> getTotalPremiumOfSitesAndBags(SclUserModel sclUser) {

        return siteManagementDao.getNumberOfBagsPurchased(sclUser);
    }


    @Override
    public Integer cmConvertedTargetVisitPremium(SclUserModel sclUser) {
        LocalDate localDate = LocalDate.now();
        int currentYear = localDate.getYear();
        int currentMonth = localDate.getMonthValue();
        return siteManagementDao.cmConvertedTargetVisitPremium(sclUser, currentMonth, currentYear);
    }

    @Override
    public Integer lmConvertedActualVisitPremium(SclUserModel sclUser) {
        LocalDate localDate = LocalDate.now().minusMonths(1);
        int currentYear = localDate.getYear();
        int lastMonth = localDate.getMonthValue();
        return siteManagementDao.cmConvertedTargetVisitPremium(sclUser, lastMonth, currentYear);

    }

    @Override
    public Double cmConvertedActualBagTotal(SclUserModel sclUser) {
        LocalDate localDate = LocalDate.now();
        int currentYear = localDate.getYear();
        int currentMonth = localDate.getMonthValue();
        return siteManagementDao.cmConvertedActualBagTotal(sclUser, currentMonth, currentYear);

    }

    @Override
    public Double lmConvertedActualBagTotal(SclUserModel sclUser) {
        LocalDate localDate = LocalDate.now().minusMonths(1);
        int currentYear = localDate.getYear();
        int lastMonth = localDate.getMonthValue();
        return siteManagementDao.cmConvertedActualBagTotal(sclUser, lastMonth, currentYear);

    }


    @Override
    public Double getTotalPremiumOfSite(SclUserModel sclUser, String startDate, String endDate, String conversionType) {
        double totalBagsCurrentMonth = 0.0;
        double premBagsCurrentMonth = 0.0;
        Date currDate = new Date();

        List<SclSiteMasterModel> currentMonthSiteMasters = siteManagementDao.getTotalPremiumOfSite(sclUser, startDate, endDate, conversionType);

        for (SclSiteMasterModel siteMasterModel : currentMonthSiteMasters) {
            totalBagsCurrentMonth += calculateBagsCount(siteMasterModel, currDate, "total");
            premBagsCurrentMonth += calculateBagsCount(siteMasterModel, currDate, "premium");
        }

        return conversionType.equalsIgnoreCase("premium") ? premBagsCurrentMonth : totalBagsCurrentMonth;
    }

    @Override
    public double calculateBagsCount(SclSiteMasterModel siteMasterModel, Date currDate, String type) {
        double bagsCount = 0.0;

        for (Map.Entry<Date, String> entry : siteMasterModel.getSiteBagQtyMap().entrySet()) {
            if (isSameMonth(currDate, entry.getKey())) {
                List<String> qtyValues = Arrays.asList(entry.getValue().split(":"));
                if(qtyValues.size() == 2) {
                    String premOrNonPrem = qtyValues.get(0);
                    double qty = Double.parseDouble(qtyValues.get(1));

                    bagsCount += (type.equalsIgnoreCase("premium") && premOrNonPrem.equalsIgnoreCase("PREMIUM")) ||
                            (type.equalsIgnoreCase("total")) ? qty : 0.0;
                }

            }
        }

        return bagsCount;
    }


    private static boolean isSameMonth(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();

        cal1.setTime(date1);
        cal2.setTime(date2);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
    }

    @Override
    public Double getSiteConversionSale(SclUserModel tsoUser, String startDate, String endDate, String conversionType) {
        return siteManagementDao.getSiteConversionSale(tsoUser, startDate, endDate, conversionType);
    }
      public List<String> getSiteMasterListTaluka(){
        return siteManagementDao.getSiteMasterListTaluka();
    }
}
