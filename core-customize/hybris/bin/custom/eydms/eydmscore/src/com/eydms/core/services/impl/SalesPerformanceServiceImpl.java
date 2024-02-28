package com.eydms.core.services.impl;

import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.dao.*;
import com.eydms.core.enums.CounterType;
import com.eydms.core.enums.CustomerCategory;
import com.eydms.core.enums.WarehouseType;
import com.eydms.core.model.*;
import com.eydms.core.region.dao.DistrictMasterDao;
import com.eydms.core.services.NetworkService;
import com.eydms.core.services.SalesPerformanceService;
import com.eydms.core.services.TerritoryManagementService;
import de.hybris.platform.search.restriction.SearchRestrictionService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import com.eydms.core.utility.EyDmsDateUtility;
import com.eydms.facades.data.*;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class SalesPerformanceServiceImpl implements SalesPerformanceService {

    private static final Logger LOGGER = Logger.getLogger(SalesPerformanceServiceImpl.class);
    DecimalFormat df = new DecimalFormat("#.#");
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String MT = "MT";
    private static final String DAYS = " days";
    private static final String ZERO = " 0";
    private static final String PERCENTAGE = "%";
    private static final String DEFAULT = "DEFAULT";
    private static final String LACS = " lacs";
    private static final String FULL = "FULL";
    private static final String DEALER = "DEALER";
    private static final int LOWPERCENTAGE = 20;
    private static final String INFLUENCER = "INFLUENCER";
    private static final String SITE = "SITE";
    private static final String RETAILER = "RETAILER";
    @Resource
    private SalesPerformanceDao salesPerformanceDao;
    @Resource
    private NetworkService networkService;
    @Resource
    private TerritoryManagementService territoryManagementService;
    @Resource
    private EyDmsUserDao eydmsUserDao;
    @Autowired
    TerritoryManagementDao territoryManagementDao;
    @Autowired
    DistrictMasterDao districtMasterDao;
    @Resource
    DJPVisitDao djpVisitDao;
    @Resource
    private BaseSiteService baseSiteService;
    @Resource
    EnumerationService enumerationService;
    @Resource
    CatalogVersionService catalogVersionService;
    @Resource
    ProductService productService;

    @Autowired
    SessionService sessionService;


    @Autowired
    private SearchRestrictionService searchRestrictionService;
    

    public ProductService getProductService() {
        return productService;
    }

    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    public EnumerationService getEnumerationService() {
        return enumerationService;
    }

    public void setEnumerationService(EnumerationService enumerationService) {
        this.enumerationService = enumerationService;
    }

    public SalesPlanningDao getSalesPlanningDao() {
        return salesPlanningDao;
    }

    public void setSalesPlanningDao(SalesPlanningDao salesPlanningDao) {
        this.salesPlanningDao = salesPlanningDao;
    }

    @Resource
    private SalesPlanningDao salesPlanningDao;

    public DecimalFormat getDf() {
        return df;
    }

    public void setDf(DecimalFormat df) {
        this.df = df;
    }

    public DJPVisitDao getDjpVisitDao() {
        return djpVisitDao;
    }

    public void setDjpVisitDao(DJPVisitDao djpVisitDao) {
        this.djpVisitDao = djpVisitDao;
    }

    public EyDmsUserDao getEyDmsUserDao() {
        return eydmsUserDao;
    }

    public void setEyDmsUserDao(EyDmsUserDao eydmsUserDao) {
        this.eydmsUserDao = eydmsUserDao;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Resource
    private Converter<EyDmsCustomerModel, SalesPerformNetworkDetailsData> influencerSummarySalesPerformanceConverter;

    public Converter<EyDmsCustomerModel, SalesPerformNetworkDetailsData> getInfluencerSummarySalesPerformanceConverter() {
        return influencerSummarySalesPerformanceConverter;
    }

    public void setInfluencerSummarySalesPerformanceConverter(Converter<EyDmsCustomerModel, SalesPerformNetworkDetailsData> influencerSummarySalesPerformanceConverter) {
        this.influencerSummarySalesPerformanceConverter = influencerSummarySalesPerformanceConverter;
    }

    @Resource
    private UserService userService;

    @Autowired
    AmountFormatServiceImpl amountFormatService;

    public BaseSiteService getBaseSiteService() {
        return baseSiteService;
    }

    public void setBaseSiteService(BaseSiteService baseSiteService) {
        this.baseSiteService = baseSiteService;
    }

    public NetworkService getNetworkService() {
        return networkService;
    }

    public void setNetworkService(NetworkService networkService) {
        this.networkService = networkService;
    }

    public TerritoryManagementService getTerritoryManagementService() {
        return territoryManagementService;
    }

    public void setTerritoryManagementService(TerritoryManagementService territoryManagementService) {
        this.territoryManagementService = territoryManagementService;
    }

    @Override
    public Double getActualTargetForSalesMTD(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite) {
        return salesPerformanceDao.getActualTargetForSalesMTD(subArea, eydmsUser, currentBaseSite);
    }

    @Override
    public Double getActualTargetForSalesYTD(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite) {
        List<Date> dates = getCurrentFinancialYear();
        Date startDate = null;
        Date endDate = null;
        startDate = dates.get(0);
        endDate = dates.get(1);
        return salesPerformanceDao.getActualTargetForSalesYTD(subArea, eydmsUser, currentBaseSite, startDate, endDate);
    }

    @Override
    public Double getMonthlySalesTarget(EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite) {
        //String month = getMonth(new Date());
        // String year = getYear(new Date());
        LocalDate localDate = LocalDate.now();
        int currentYear = localDate.getYear();
        String month = getMonth(new Date());
        String formattedMonth = month.concat("-").concat(String.valueOf(currentYear));
        return salesPerformanceDao.getMonthlySalesTarget(eydmsUser, currentBaseSite, formattedMonth, String.valueOf(currentYear));
    }

    @Override
    public Double getMonthlySalesTargetForDealer(EyDmsCustomerModel eydmsCustomerModel, BaseSiteModel currentBaseSite, String bgpFilter) {
        List<DealerRevisedMonthlySalesModel> monthlySalesTargetForDealerWithBGP = new ArrayList<>();
        LocalDate localDate = LocalDate.now();
        int currentYear = localDate.getYear();
        String month = getMonth(new Date());
        double revisedTarget = 0.0, sumRevisedTarget = 0.0;
        String formattedMonth = month.concat("-").concat(String.valueOf(currentYear));
        if (StringUtils.isNotBlank(bgpFilter) && !bgpFilter.equalsIgnoreCase("ALL")) {
            CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
            ProductModel product = productService.getProductForCode(catalogVersion, bgpFilter);
            if (bgpFilter.equalsIgnoreCase(product.getCode())) {
                monthlySalesTargetForDealerWithBGP = salesPerformanceDao.getMonthlySalesTargetForDealerWithBGP(eydmsCustomerModel, currentBaseSite, formattedMonth, String.valueOf(currentYear), bgpFilter);
                for (DealerRevisedMonthlySalesModel dealerRevisedMonthlySalesModel : monthlySalesTargetForDealerWithBGP) {
                    if (dealerRevisedMonthlySalesModel != null) {
                        for (ProductModel listOfSkus : dealerRevisedMonthlySalesModel.getListOfSkus()) {
                            if (listOfSkus.getCode().equalsIgnoreCase(product.getCode())) {
                                ProductSaleModel productSaleModel = salesPerformanceDao.getTotalTargetForProductBGPFilterMTD(dealerRevisedMonthlySalesModel.getCustomerCode(), listOfSkus.getCode(), formattedMonth, String.valueOf(currentYear));
                                if (productSaleModel != null) {
                                    revisedTarget = productSaleModel.getRevisedTarget();
                                    sumRevisedTarget += revisedTarget;
                                }
                            }
                        }
                    }
                }
            }
            return sumRevisedTarget;
        } else {
           // return salesPerformanceDao.getMonthlySalesTargetForDealer(eydmsCustomerModel, currentBaseSite, formattedMonth, String.valueOf(currentYear), bgpFilter);
            return (Double) sessionService.executeInLocalView(new SessionExecutionBody()
            {
                @Override
                public Double execute()
                {
                    try {
                        searchRestrictionService.disableSearchRestrictions();
                        return salesPerformanceDao.getMonthlySalesTargetForDealer(eydmsCustomerModel, currentBaseSite, formattedMonth, String.valueOf(currentYear), bgpFilter);
                    }
                    finally {
                        searchRestrictionService.enableSearchRestrictions();
                    }
                }
            });
        }
    }

    @Override
    public Double getMonthlySalesTargetForSP(List<EyDmsCustomerModel> eydmsCustomerModel) {
        LocalDate localDate = LocalDate.now();
        int currentYear = localDate.getYear();
        String month = getMonth(new Date());
        double revisedTarget = 0.0, sumRevisedTarget = 0.0;
        String formattedMonth = month.concat("-").concat(String.valueOf(currentYear));
        return salesPerformanceDao.getMonthlySalesTargetForSP(eydmsCustomerModel,formattedMonth, String.valueOf(currentYear));
    }

    @Override
    public Double getMonthlySalesTargetForRetailer(String retailerCode, BaseSiteModel currentBaseSite, String bgpFilter, String monthYear) {
/*        LocalDate localDate = LocalDate.now();
        int currentYear = localDate.getYear();
        String month = getMonth(new Date());
        String formattedMonth = month.concat("-").concat(String.valueOf(currentYear));*/
        if (StringUtils.isNotBlank(bgpFilter) && !bgpFilter.equalsIgnoreCase("ALL")) {
            return 0.0;
        } else {
            return salesPerformanceDao.getMonthlySalesTargetForRetailer(retailerCode, currentBaseSite, monthYear, bgpFilter);
        }

    }

    @Override
    public Double getMonthlySalesTargetForRetailer(String retailerCode, BaseSiteModel currentBaseSite, String bgpFilter) {
        LocalDate localDate = LocalDate.now();
        int currentYear = localDate.getYear();
        String month = getMonth(new Date());
        String formattedMonth = month.concat("-").concat(String.valueOf(currentYear));
        if (StringUtils.isNotBlank(bgpFilter) && !bgpFilter.equalsIgnoreCase("ALL")) {
            return 0.0;
        } else {
            return salesPerformanceDao.getMonthlySalesTargetForRetailer(retailerCode, currentBaseSite, formattedMonth, bgpFilter);
        }

    }

    @Override
    public Double getActualTargetForSalesYTDRetailerList(EyDmsCustomerModel currentUser, BaseSiteModel currentBaseSite) {

            LocalDate currentYearCurrentDate= LocalDate.now();
            LocalDate currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear(), Month.APRIL, 1);
            if(currentYearCurrentDate.getMonth().compareTo(Month.APRIL)<0) {
                currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear()-1, Month.APRIL, 1);
            }
            LOGGER.info(String.format("CurrentFinancialYearDate :: %s ",currentFinancialYearDate.toString()));
        LOGGER.info(String.format("currentYearCurrentDate :: %s ",currentYearCurrentDate.toString()));
        return salesPerformanceDao.getActualTargetForSalesYTDRetailerList(currentUser, currentBaseSite,currentFinancialYearDate.toString(), currentYearCurrentDate.toString());

        }

    @Override
    public Double getActualTargetForSalesMTDRetailerList(EyDmsCustomerModel currentUser, BaseSiteModel currentBaseSite) {
        return salesPerformanceDao.getActualTargetForSalesRetailerMTDList(currentUser, currentBaseSite);
    }

    @Override
    public Double getActualTargetForSalesYTRetailer(EyDmsCustomerModel currentUser, BaseSiteModel currentBaseSite, String bgpFilter) {
        LocalDate currentYearCurrentDate= LocalDate.now();
        LocalDate currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear(), Month.APRIL, 1);
        if(currentYearCurrentDate.getMonth().compareTo(Month.APRIL)<0) {
            currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear()-1, Month.APRIL, 1);
        }

        return salesPerformanceDao.getActualTargetForSalesYTRetailerList(currentUser, currentBaseSite,currentFinancialYearDate.toString(), currentYearCurrentDate.toString(),bgpFilter);

    }

    @Override
    public DistrictMasterModel getDistrictForSP(FilterDistrictData filterDistrictData) {

        return salesPerformanceDao.getDistrictForSP(filterDistrictData);
    }

    @Override
    public List<EyDmsCustomerModel> getSPForDistrict(DistrictMasterModel districtForSP) {
        return salesPerformanceDao.getSPForDistrict(districtForSP);
    }

    @Override
    public Double getAnnualSalesTargetForDealerFY(EyDmsCustomerModel eydmsCustomerModel, String bgpFilter, String financialYear) {

        List<List<Object>> list = new ArrayList<>();
        List<DealerRevisedAnnualSalesModel> dealerRevisedAnnualSalesModels = new ArrayList<>();
        Double totalTarget = 0.0, sumTotalTarget = 0.0;
        if (StringUtils.isNotBlank(bgpFilter) && !bgpFilter.equalsIgnoreCase("ALL")) {
            CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
            ProductModel product = productService.getProductForCode(catalogVersion, bgpFilter);
            if (bgpFilter.equalsIgnoreCase(product.getCode())) {
                dealerRevisedAnnualSalesModels = salesPerformanceDao.getAnnualSalesTargetForDealerWithBGP(eydmsCustomerModel, financialYear, bgpFilter);
                if (dealerRevisedAnnualSalesModels != null && !dealerRevisedAnnualSalesModels.isEmpty()) {
                    for (DealerRevisedAnnualSalesModel dealerRevisedAnnualSalesModel : dealerRevisedAnnualSalesModels) {
                        if (dealerRevisedAnnualSalesModel != null) {
                            for (ProductModel listOfSkus : dealerRevisedAnnualSalesModel.getListOfSkus()) {
                                if (listOfSkus != null) {
                                    if (listOfSkus.getCode().equalsIgnoreCase(product.getCode())) {
                                        ProductSaleModel productSaleModel = salesPerformanceDao.getTotalTargetForProductBGPFilter(dealerRevisedAnnualSalesModel.getCustomerCode(), listOfSkus.getCode());
                                        if (productSaleModel != null) {
                                            totalTarget = productSaleModel.getTotalTarget();
                                            if (totalTarget != null)
                                                sumTotalTarget += totalTarget;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return sumTotalTarget;
        } else {
            Double annualSalesTarget = salesPerformanceDao.getAnnualSalesTargetForDealer(eydmsCustomerModel, financialYear, bgpFilter);
            return annualSalesTarget;
        }
    }

    @Override
    public Double getAnnualSalesTargetForRetailerFY(EyDmsCustomerModel eydmsCustomerModel, String bgpFilter, String financialYear) {
        double annualSalesTarget = 0.0, totalSale = 0.0;
        List<List<Object>> list = new ArrayList<>();
        if (StringUtils.isNotBlank(bgpFilter) && !bgpFilter.equalsIgnoreCase("ALL")) {
            return annualSalesTarget;
        } else {
            annualSalesTarget = salesPerformanceDao.getAnnualSalesTargetForRetailer(eydmsCustomerModel, financialYear, bgpFilter);
            return annualSalesTarget;
        }

    }

    @Override
    public List<EyDmsUserModel> getTsmByRegion(String district) {

        return salesPerformanceDao.getTsmByRegion(district);
    }

    @Override
    public List<EyDmsUserModel> getRHByState(String district) {

        return salesPerformanceDao.getRHByState(district);
    }

    @Override
    public List<EyDmsCustomerModel> getCustomerForTsm(String district, EyDmsUserModel eydmsUserModel) {
        return salesPerformanceDao.getCustomerForTsm(district,eydmsUserModel);
    }

    @Override
    public List<EyDmsCustomerModel> getCustomerForRH(String district, EyDmsUserModel eydmsUserModel) {
        return salesPerformanceDao.getCustomerForRH(district,eydmsUserModel);
    }

    @Override
    public List<List<Object>> getSalesDealerByDate(String district, EyDmsUserModel eydmsUserModel,Date startDate, Date endDate) {
        return salesPerformanceDao.getSalesDealerByDate(district,eydmsUserModel,startDate,endDate);

    }


    @SuppressWarnings("unchecked")
    @Override
    public List<List<Object>> getRHSalesDealerByDate(EyDmsUserModel eydmsUserModel,Date startDate, Date endDate)  {
        return (List<List<Object>>) sessionService.executeInLocalView(new SessionExecutionBody()
        {
            @Override
            public List<List<Object>> execute()
            {
                try {
                    searchRestrictionService.disableSearchRestrictions();
                    return salesPerformanceDao.getRHSalesDealerByDate(eydmsUserModel,startDate,endDate);
                }
                finally {
                    searchRestrictionService.enableSearchRestrictions();
                }
            }
        });
    }

    @Override
    public List<EyDmsCustomerModel> getCustomerForSp(EyDmsCustomerModel eydmsCustomer) {

        return salesPerformanceDao.getCustomerForSp(eydmsCustomer);
    }

    @Override
    public List<List<Object>> getWeeklyOverallPerformance(String bgpFilter) {
        return salesPerformanceDao.getWeeklyOverallPerformance(bgpFilter);
    }

    @Override
    public List<List<Object>> getMonthlyOverallPerformance(String bgpFilter) {
        return salesPerformanceDao.getMonthlyOverallPerformance(bgpFilter);
    }


    @Override
    public Double getLastMonthSalesTarget(EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite) {

        LocalDate date1 = LocalDate.now();
        LocalDate month1 = date1.minusMonths(1);
        Date date = Date.from(month1.atStartOfDay(ZoneId.systemDefault()).toInstant());
        String month = getMonth(date);
        String year = getYear(new Date());
        return salesPerformanceDao.getLastMonthSalesTarget(eydmsUser, currentBaseSite, month, year);
    }

    @Override
    public Double getMonthlySalesForPartnerTarget(String code, EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite) {
        String month = getMonth(new Date());
        String year = getYear(new Date());
        return salesPerformanceDao.getMonthlySalesTarget(eydmsUser, currentBaseSite, month, year);
    }

    @Override
    public Double getAnnualSalesTarget(EyDmsUserModel eydmsUser) {
        String financialYear = findNextFinancialYear();
        List<List<Object>> list = salesPerformanceDao.getAnnualSalesTarget(eydmsUser, financialYear);
        double annualSalesTarget = 0.0, totalSale = 0.0;
        if (list != null && !list.isEmpty()) {
            for (List<Object> objects : list) {
                if(objects.get(0)!=null) {
                    totalSale = (double) objects.get(0);
                }
            }
            if (totalSale != 0.0)
                annualSalesTarget = totalSale;
        }
        return annualSalesTarget;
    }

    @Override
    public Double getAnnualSalesTargetForDealer(EyDmsCustomerModel eydmsCustomerModel, String bgpFilter) {
        String financialYear = findNextFinancialYearForActualTargetSales();
        List<List<Object>> list = new ArrayList<>();
        List<DealerRevisedAnnualSalesModel> dealerRevisedAnnualSalesModels = new ArrayList<>();
        Double totalTarget = 0.0, sumTotalTarget = 0.0;
        if (StringUtils.isNotBlank(bgpFilter) && !bgpFilter.equalsIgnoreCase("ALL")) {
            CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
            ProductModel product = productService.getProductForCode(catalogVersion, bgpFilter);
            if (bgpFilter.equalsIgnoreCase(product.getCode())) {
                dealerRevisedAnnualSalesModels = salesPerformanceDao.getAnnualSalesTargetForDealerWithBGP(eydmsCustomerModel, financialYear, bgpFilter);
                if (dealerRevisedAnnualSalesModels != null && !dealerRevisedAnnualSalesModels.isEmpty()) {
                    for (DealerRevisedAnnualSalesModel dealerRevisedAnnualSalesModel : dealerRevisedAnnualSalesModels) {
                        if (dealerRevisedAnnualSalesModel != null) {
                            for (ProductModel listOfSkus : dealerRevisedAnnualSalesModel.getListOfSkus()) {
                                if (listOfSkus != null) {
                                    if (listOfSkus.getCode().equalsIgnoreCase(product.getCode())) {
                                        ProductSaleModel productSaleModel = salesPerformanceDao.getTotalTargetForProductBGPFilter(dealerRevisedAnnualSalesModel.getCustomerCode(), listOfSkus.getCode());
                                        if (productSaleModel != null) {
                                            totalTarget = productSaleModel.getTotalTarget();
                                            if (totalTarget != null)
                                                sumTotalTarget += totalTarget;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return sumTotalTarget;
        } else {
            Double annualSalesTarget = salesPerformanceDao.getAnnualSalesTargetForDealer(eydmsCustomerModel, financialYear, bgpFilter);
            return annualSalesTarget;
        }
    }

    @Override
    public Double getAnnualSalesTargetForSP(List<EyDmsCustomerModel> eydmsCustomerModel) {
        String financialYear = findNextFinancialYearForActualTargetSales();
        Double annualSalesTarget = salesPerformanceDao.getAnnualSalesTargetForSP(eydmsCustomerModel, financialYear);
        return annualSalesTarget;
    }

    @Override
    public Double getAnnualSalesTargetForRetailer(EyDmsCustomerModel eydmsCustomerModel, String bgpFilter) {
        String financialYear = findNextFinancialYearForActualTargetSales();
        double annualSalesTarget = 0.0, totalSale = 0.0;
        List<List<Object>> list = new ArrayList<>();
        if (StringUtils.isNotBlank(bgpFilter) && !bgpFilter.equalsIgnoreCase("ALL")) {
            return annualSalesTarget;
        } else {
            annualSalesTarget = salesPerformanceDao.getAnnualSalesTargetForRetailer(eydmsCustomerModel, financialYear, bgpFilter);
            return annualSalesTarget;
        }

    }

    @Override
    public Double getLastYearSalesTarget(EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite) {
        String financialYear = findPreviousFinancialYear();
        List<List<Object>> list = salesPerformanceDao.getAnnualSalesTarget(eydmsUser, financialYear);
        double annualSalesTarget = 0.0;
        double totalCySales = 0.0, totalPlanSales = 0.0;
        if (list != null && !list.isEmpty()) {
            for (List<Object> objects : list) {
                if(objects!=null) {
                    if (objects.get(0) != null) {
                        totalCySales = (double) objects.get(0);
                    }
                    if (objects.get(1) != null) {
                        totalPlanSales = (double) objects.get(1);
                    }
                }
            }
            if (totalCySales != 0.0 && totalPlanSales != 0.0)
                annualSalesTarget = totalCySales + totalPlanSales;
        }
        return annualSalesTarget;
    }

    @Override
    public Double getAnnualSalesForPartnerTarget(String code, EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite) {
        String financialYear = findNextFinancialYear();
        List<List<Object>> list = salesPerformanceDao.getAnnualSalesTarget(eydmsUser, financialYear);
        double annualSalesTarget = 0.0;
        double totalCySales = 0.0, totalPlanSales = 0.0;
        if (list != null && !list.isEmpty()) {
            for (List<Object> objects : list) {
                if(objects!=null) {
                    if (objects.get(0) != null) {
                        totalCySales = (double) objects.get(0);
                    }
                    if (objects.get(1) != null) {
                        totalPlanSales = (double) objects.get(1);
                    }
                }
            }
            if (totalCySales != 0.0 && totalPlanSales != 0.0)
                annualSalesTarget = totalCySales + totalPlanSales;
        }
        return annualSalesTarget;
    }

    @Override
    public List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatio(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, EyDmsCustomerModel customer, List<String> doList, List<String> subAreaList) {
        return salesPerformanceDao.getProductwiseSalesPercentRatioAndVolumeRatio(eydmsUser, baseSite, customer, doList, subAreaList);
    }


    public List<SalesPerformNetworkDetailsData> getAllLowPerformingDetails(String subArea, BaseSiteModel site, String leadType, int month, int year, String filter) {
/*
        Double yoyGrowth;
        Double zero = 0.0;
        List<SalesPerformNetworkDetailsData> currentNetworkWsDataList = new ArrayList<>();
        List<EyDmsCustomerModel> customerFilteredList = new ArrayList<>();
        if (leadType.equalsIgnoreCase(RETAILER)) {
            customerFilteredList = networkService.getCustomerListFromSubArea(subArea, site).stream().filter(eydmsCustomerModel -> eydmsCustomerModel.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))
                    .filter(eydmsCustomerModel -> eydmsCustomerModel.getCustomerNo() != null).collect(Collectors.toList());
        } else if (leadType.equalsIgnoreCase(DEALER)) {
            customerFilteredList = networkService.getCustomerListFromSubArea(subArea, site).stream().filter(eydmsCustomerModel -> eydmsCustomerModel.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))
                    .filter(eydmsCustomerModel -> eydmsCustomerModel.getCustomerNo() != null).collect(Collectors.toList());
        }

        for (EyDmsCustomerModel customerModel : customerFilteredList) {
            double sum=0.0;
            if (month != 0 && year != 0) {
                SalesPerformNetworkDetailsData dealerCurrentNetworkData = new SalesPerformNetworkDetailsData();
                Date startDate = getFirstDateOfMonth(month, year);
                Date endDate = getLastDateOfMonth(month, year);
               // EyDmsCustomerModel customerModelAndOrdersForSpecificDate = eydmsUserDao.getCustomerModelAndOrdersForSpecificDate(customerModel.getUid(), subArea, site, startDate, endDate);
                if (customerModelAndOrdersForSpecificDate != null)
                   dealerCurrentNetworkData.setCode(customerModelAndOrdersForSpecificDate.getUid());
                   dealerCurrentNetworkData.setPotential(Objects.nonNull(customerModel.getCounterPotential()) ? String.valueOf(customerModel.getCounterPotential()) : String.valueOf(0.0));
                   sum+=customerModelAndOrdersForSpecificDate.getCounterPotential();
            if (leadType.equalsIgnoreCase(DEALER)) {
                if (Objects.nonNull(customerModel.getLastCounterVisit())) {
                    if (Objects.nonNull(customerModel.getLastCounterVisit().getCounterShare())) {
                        dealerCurrentNetworkData.setCounterSharePercentage(String.valueOf(customerModel.getLastCounterVisit().getCounterShare()));
                    } else dealerCurrentNetworkData.setCounterSharePercentage(String.valueOf(0));
                } else {
                    dealerCurrentNetworkData.setCounterSharePercentage(String.valueOf(0));
                }
                //target
                double target = eydmsUserDao.getCustomerTarget(customerModel.getCustomerNo(), subArea, EyDmsDateUtility.getMonth(new Date()), EyDmsDateUtility.getYear(new Date()));
                dealerCurrentNetworkData.setTarget(Objects.nonNull(target) ? String.valueOf(target) : ZERO);
            }
            customerOrder.sort(Comparator.comparing(OrderModel::getCreationtime).reversed());
            //salesQuantity for dealer & retailer
            dealerCurrentNetworkData.setSalesQuantity(setSalesQuantityForCustomer(customerModel, subArea, leadType, site));
            //growth rate for dealer & retailer
            yoyGrowth = getYearToYearGrowth(customerModel.getUid(), site);
            if (Double.compare(yoyGrowth, zero) == 0) {
                dealerCurrentNetworkData.setGrowthRateYoYPercentage(ZERO);
            } else {
                dealerCurrentNetworkData.setGrowthRateYoYPercentage(yoyGrowth + PERCENTAGE);
            }
            //outstanding Amount for dealer
            if (leadType.equalsIgnoreCase(DEALER)) {
                dealerCurrentNetworkData.setOutstandingAmount(Objects.nonNull(customerModel.getOutstandingAmount()) ? String.valueOf(customerModel.getOutstandingAmount()) + LACS : ZERO);
            }
            //day since last order for dealer & retailer
            if (customerOrder.size() == 0) {
                dealerCurrentNetworkData.setDaysSinceLastOrder(DASH);
            } else {
                dealerCurrentNetworkData.setDaysSinceLastOrder(String.valueOf(getDaysFromLastOrder(customerOrder)) + DAYS);
            }

            List<SubAreaMasterModel> subAreaMasterModelList = territoryManagementService.getTerritoriesForCustomer(customerModel);
            if (CollectionUtils.isNotEmpty(subAreaMasterModelList) && subAreaMasterModelList.get(0) != null) {
                if (subAreaMasterModelList.get(0).getDistrict() != null && subAreaMasterModelList.get(0).getTaluka() != null) {
                    dealerCurrentNetworkData.setDistrict(subAreaMasterModelList.get(0).getDistrict());
                    dealerCurrentNetworkData.setTaluka(subAreaMasterModelList.get(0).getTaluka());
                }
            }
            currentNetworkWsDataList.add(dealerCurrentNetworkData);
        }
    }
*/
        return null;
    }

    public static Date getFirstDateOfMonth(int month, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.DAY_OF_MONTH,
                calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        return calendar.getTime();
    }

    public static Date getLastDateOfMonth(int month, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return calendar.getTime();
    }

    @Override
    public SalesPerformNetworkDetailsListData getListOfAllDealerRetailerInfluencers(String fields, BaseSiteModel currentBaseSite, String leadType, String searchKey, List<String> doList, List<String> subAreaList) {
        SalesPerformNetworkDetailsListData dealerCurrentNetworkListData = new SalesPerformNetworkDetailsListData();
        List<SalesPerformNetworkDetailsData> currentNetworkWsDataList = new ArrayList<>();
        RequestCustomerData requestData = new RequestCustomerData();
        List<String> counterType = List.of(leadType);
        requestData.setCounterType(counterType);
        List<SubAreaMasterModel> soList=new ArrayList<>();
        List<DistrictMasterModel> districtList=new ArrayList<>();
        if(subAreaList!=null && !subAreaList.isEmpty()){
            for (String id : subAreaList) {
                soList.add(territoryManagementDao.getTerritoryById(id));
            }
            if(soList.get(0)!=null)
            requestData.setSubAreaMasterPk(soList.get(0).getPk().toString());
        }
        if (doList != null && !doList.isEmpty()) {
            for (String code : doList) {
                districtList.add(districtMasterDao.findByCode(code));
            }
            if(doList.get(0)!=null)
            requestData.setDistrict(districtList.get(0).getName());
        }

        List<EyDmsCustomerModel> customerFilteredList = territoryManagementService.getCustomerforUser(requestData);

        if (StringUtils.isNotBlank(searchKey)) {
            customerFilteredList = filterEyDmsCustomersWithSearchTerm(customerFilteredList, searchKey);
        }

        if (customerFilteredList != null && !customerFilteredList.isEmpty()) {
            for (EyDmsCustomerModel customerModel : customerFilteredList) {
                SalesPerformNetworkDetailsData dealerCurrentNetworkData = new SalesPerformNetworkDetailsData();
                if(customerModel.getUid()!=null) {
                    dealerCurrentNetworkData.setCode(customerModel.getUid());
                }
                dealerCurrentNetworkData.setName(customerModel.getName());
                dealerCurrentNetworkData.setPotential(Objects.nonNull(customerModel.getCounterPotential()) ? String.valueOf(customerModel.getCounterPotential()) : ZERO);

                //countershare and target for dealer
                if (leadType.equalsIgnoreCase(DEALER)) {
                /*    LocalDate  startDateTarget = null;
                    LocalDate currentMonth = LocalDate.now();
                  startDateTarget = LocalDate.of(currentMonth.getYear(), currentMonth.minusMonths(1).getMonthValue(), 1);
                    Date startDate1Target = Date.from(startDateTarget.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    String monthName = getMonth(startDate1Target);
                    String yearName = getYear(startDate1Target);*/
                    dealerCurrentNetworkData.setCounterSharePercentage(getCounterShareForDealer(customerModel,  doList, subAreaList));
                    double target = eydmsUserDao.getCustomerTarget(customerModel.getUid(), EyDmsDateUtility.getMonth(new Date()), EyDmsDateUtility.getYear(new Date()));
                    LOGGER.info(String.format("Target ::%s", target));
                    dealerCurrentNetworkData.setTarget(String.valueOf(target));

                    dealerCurrentNetworkData.setSalesQuantity(setSalesQuantityForCustomer(customerModel, leadType,  doList, subAreaList));
                    if(customerModel.getCustomerNo()!=null) {
                        double totalOutstanding = djpVisitDao.getDealerOutstandingAmount(customerModel.getCustomerNo());
                        dealerCurrentNetworkData.setOutstandingAmount(df.format(totalOutstanding));
                    }

                    dealerCurrentNetworkData.setGrowthRateYoYPercentage(df.format(getYearToYearGrowthForDealer(customerModel.getUid())));
                    //dealerCurrentNetworkData.setDaysSinceLastOrder(String.valueOf(getDaysFromLastOrder(customerModel.getOrders())));
                    dealerCurrentNetworkData.setDaysSinceLastOrder(getDaysSinceLastLiftingInfForDealer(customerModel));
                }
                List<SubAreaMasterModel> subAreaMasterModelList = territoryManagementService.getTerritoriesForCustomer(customerModel);
                if (CollectionUtils.isNotEmpty(subAreaMasterModelList) && subAreaMasterModelList.get(0) != null) {
                    if (subAreaMasterModelList.get(0).getDistrict() != null && subAreaMasterModelList.get(0).getTaluka() != null) {
                        dealerCurrentNetworkData.setDistrict(subAreaMasterModelList.get(0).getDistrict());
                        dealerCurrentNetworkData.setTaluka(subAreaMasterModelList.get(0).getTaluka());
                    }
                }
                currentNetworkWsDataList.add(dealerCurrentNetworkData);
            }
        }
       /* if (leadType.equalsIgnoreCase(RETAILER)) {
            computeRankForRetailer(currentNetworkWsDataList);
        } else if (leadType.equalsIgnoreCase(DEALER)) {
            computeRankForDealer(currentNetworkWsDataList);
        }
       currentNetworkWsDataList= currentNetworkWsDataList.stream().sorted(Comparator.comparing(SalesPerformNetworkDetailsData::getRank)).collect(Collectors.toList());*/
       currentNetworkWsDataList.sort(Comparator.comparing(SalesPerformNetworkDetailsData::getName));
        dealerCurrentNetworkListData.setNetworkDetails(currentNetworkWsDataList);
        return dealerCurrentNetworkListData;
    }

    @Override
    public SalesPerformNetworkDetailsListData getListOfAllRetailerInfluencersForDealer(String fields, BaseSiteModel currentBaseSite, String leadType, String searchKey, List<String> doList, List<String> subAreaList) {
        {
            SalesPerformNetworkDetailsListData dealerCurrentNetworkListData = new SalesPerformNetworkDetailsListData();
            List<SalesPerformNetworkDetailsData> currentNetworkWsDataList = new ArrayList<>();
            List<EyDmsCustomerModel> customerFilteredList = new ArrayList<>();
            EyDmsCustomerModel currentUser = (EyDmsCustomerModel) userService.getCurrentUser();
            if(currentUser != null) {
                if (currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                    if(!currentUser.getCounterType().equals(CounterType.SP)) {
                        if (leadType.equalsIgnoreCase(RETAILER)) {
                            customerFilteredList = territoryManagementService.getRetailerListForDealer();
                        } else if (leadType.equalsIgnoreCase(INFLUENCER)) {
                            customerFilteredList = territoryManagementService.getInfluencerListForDealer();
                        }
                    }
                }
            }
            LOGGER.info(String.format("Customer List:%s",customerFilteredList.size()));
            for (EyDmsCustomerModel customerModel : customerFilteredList) {
                LOGGER.info(String.format("Customer List uid:%s",customerModel.getUid()));
            }

            if (StringUtils.isNotBlank(searchKey)) {
                customerFilteredList = filterEyDmsCustomersWithSearchTerm(customerFilteredList, searchKey);
            }

            if (customerFilteredList != null && !customerFilteredList.isEmpty()) {
                for (EyDmsCustomerModel customerModel : customerFilteredList) {
                    SalesPerformNetworkDetailsData dealerCurrentNetworkData = new SalesPerformNetworkDetailsData();
                    dealerCurrentNetworkData.setCode(customerModel.getUid());
                    dealerCurrentNetworkData.setName(customerModel.getName());
                    if(customerModel.getCounterPotential()!=null) {
                        double potential = customerModel.getCounterPotential() / 20;
                        dealerCurrentNetworkData.setPotential(String.valueOf(potential));
                    }
                    else{
                        dealerCurrentNetworkData.setPotential(ZERO);
                    }

                    double counterShareRetailerForDealer = 0.0, current = 0.0, lastYear = 0.0;
                    if (leadType.equalsIgnoreCase(RETAILER)) {
                        EyDmsCustomerModel orderRequisitionSalesDataForRetailer = salesPerformanceDao.getRetailerSalesForDealer(customerModel, baseSiteService.getCurrentBaseSite());
                 if (orderRequisitionSalesDataForRetailer != null) {
                        counterShareRetailerForDealer = getCounterShareRetailerForDealer(orderRequisitionSalesDataForRetailer);
                        SalesQuantityData sales = new SalesQuantityData();
                        current = getSalesQuantityCurrentMonthRetailer(orderRequisitionSalesDataForRetailer);
                        lastYear = getSalesQuantityLastYearCurrentMonthRetailer(orderRequisitionSalesDataForRetailer);

                        sales.setCurrent(current);
                        sales.setLastYear(lastYear);
                        dealerCurrentNetworkData.setSalesQuantity(sales);
                        dealerCurrentNetworkData.setCounterSharePercentage(counterShareRetailerForDealer);
                        dealerCurrentNetworkData.setDaysSinceLastOrder(getDaysSinceLastLiftingRetailerForDealer(customerModel));
                        dealerCurrentNetworkData.setGrowthRateYoYPercentage(df.format(getYearToYearGrowthRetailerForDealer(customerModel)));
                    } else {
                        SalesQuantityData sales = new SalesQuantityData();
                        sales.setCurrent(0.0);
                        sales.setLastYear(0.0);
                        dealerCurrentNetworkData.setSalesQuantity(sales);
                        dealerCurrentNetworkData.setDaysSinceLastOrder("-");
                        dealerCurrentNetworkData.setGrowthRateYoYPercentage("-");
                        dealerCurrentNetworkData.setCounterSharePercentage(0.0);
                    }
                }
                    else if (leadType.equalsIgnoreCase(INFLUENCER)) {
                        double bagLifted=0.0,salesQuantity=0.0;
                        dealerCurrentNetworkData.setCategory(customerModel.getInfluencerType() != null ?
                                enumerationService.getEnumerationName(customerModel.getInfluencerType()) : "-");
                        dealerCurrentNetworkData.setContactNumber(customerModel.getContactNumber());
                        dealerCurrentNetworkData.setTimesContacted(customerModel.getTimesContacted());
                        dealerCurrentNetworkData.setType(customerModel.getNetworkType());
                        dealerCurrentNetworkData.setPoints(Objects.nonNull(customerModel.getAvailablePoints()) ? customerModel.getAvailablePoints() : 0.0);

                        List<DealerInfluencerMapModel> pointRequisitionSalesDataForRetailer = salesPerformanceDao.getInfluencerSalesForDealer(customerModel, baseSiteService.getCurrentBaseSite());
                        if (pointRequisitionSalesDataForRetailer != null && !pointRequisitionSalesDataForRetailer.isEmpty()) {
                        for (DealerInfluencerMapModel dealerInfluencerMapModel : pointRequisitionSalesDataForRetailer) {
                            LOGGER.info(String.format("Influencer model from inner query:%s", dealerInfluencerMapModel.getInfluencer().getUid()));
                            bagLifted = getSalesQuantityInfForDealer(dealerInfluencerMapModel.getInfluencer());
                            salesQuantity += bagLifted;
                        }
                        dealerCurrentNetworkData.setBagLiftedNo(salesQuantity);
                        dealerCurrentNetworkData.setBagLifted(salesQuantity);
                        dealerCurrentNetworkData.setBagLiftedQty(String.valueOf(salesQuantity));
                        dealerCurrentNetworkData.setDaySinceLastLifting(getDaysSinceLastLiftingInfForDealer(customerModel));
                        }
                        else{
                            dealerCurrentNetworkData.setBagLifted(0.0);
                            dealerCurrentNetworkData.setBagLiftedNo(0.0);
                            dealerCurrentNetworkData.setBagLiftedQty(String.valueOf(0.0));
                            dealerCurrentNetworkData.setDaySinceLastLifting("-");
                        }
                    }

                    List<SubAreaMasterModel> subAreaMasterModelList = territoryManagementService.getTerritoriesForCustomer(customerModel);
                    if (CollectionUtils.isNotEmpty(subAreaMasterModelList) && subAreaMasterModelList.get(0) != null) {
                        if (subAreaMasterModelList.get(0).getDistrict() != null && subAreaMasterModelList.get(0).getTaluka() != null) {
                            dealerCurrentNetworkData.setDistrict(subAreaMasterModelList.get(0).getDistrict());
                            dealerCurrentNetworkData.setTaluka(subAreaMasterModelList.get(0).getTaluka());
                        }
                    }
                    currentNetworkWsDataList.add(dealerCurrentNetworkData);
                }
            }
            LOGGER.info(String.format("Customer List before set into list:%s",currentNetworkWsDataList.size()));
            /*if (leadType.equalsIgnoreCase(RETAILER)) {
                computeRankForRetailer(currentNetworkWsDataList);
            } else if (leadType.equalsIgnoreCase(DEALER)) {
                computeRankForDealer(currentNetworkWsDataList);
            }*/
             currentNetworkWsDataList.sort(Comparator.comparing(SalesPerformNetworkDetailsData::getName));
             dealerCurrentNetworkListData.setNetworkDetails(currentNetworkWsDataList);
             return dealerCurrentNetworkListData;
        }
    }

    private void computeRankForRetailer(List<SalesPerformNetworkDetailsData> currentNetworkWsDataList) {
        AtomicInteger rank = new AtomicInteger(1);
        //Retailer-change
        currentNetworkWsDataList.stream().filter(nw->nw.getSalesQuantity()!=null).filter(nw->nw.getSalesQuantity().getCurrent()!=null).sorted(Comparator.comparing(nw -> nw.getSalesQuantity().getCurrent())).forEach(data -> data.setRank(String.valueOf(rank.getAndIncrement())));
    }

    private void computeRankForDealer(List<SalesPerformNetworkDetailsData> currentNetworkWsDataList) {
        AtomicInteger rank = new AtomicInteger(1);
        currentNetworkWsDataList.stream().filter(nw->nw.getSalesQuantity()!=null).filter(nw->nw.getSalesQuantity().getActual()!=null).sorted(Comparator.comparing(nw -> nw.getSalesQuantity().getActual(), Comparator.reverseOrder())).forEach(data -> data.setRank(String.valueOf(rank.getAndIncrement())));
    }

    @Override
    public List<EyDmsCustomerModel> filterEyDmsCustomersWithSearchTerm(List<EyDmsCustomerModel> customers, String searchTerm) {
        return customers.stream().filter(user -> containsIgnoreCase(user.getName(), searchTerm) || containsIgnoreCase(user.getUid() , searchTerm) || (user.getInfluencerType()!=null && containsIgnoreCase(String.valueOf(user.getInfluencerType()),searchTerm))).collect(Collectors.toList());
    }

    @Override
    public Double getActualDealerCounterShareMarket(EyDmsCustomerModel dealer, BaseSiteModel site, List<String> doList, List<String> subAreaList) {
        var totalSales = getCurrentMonthSaleQty(dealer.getCustomerNo(),  doList, subAreaList);
        if (Objects.nonNull(dealer.getCounterPotential()) && dealer.getCounterPotential() > 0.0) {
            return (double) (totalSales * 100 / dealer.getCounterPotential());
        }
        return 0.0;
    }

   /* @Override
    public List<List<Object>> getSalesHistoryModelList(String district, String state, int month, int year, BaseSiteModel site) {
        List<List<Object>> salesHistoryModelList = salesPerformanceDao.getSalesHistoryModelList(district, state, month, year, site);
        return salesHistoryModelList;
    }*/

    private boolean containsIgnoreCase(String name, String searchKey) {
        return name.toLowerCase().contains(searchKey.toLowerCase());
    }

    private Double getCounterShareForDealer(EyDmsCustomerModel dealer,  List<String> doList, List<String> subAreaList) {
        double totalSales = getCurrentMonthSaleQty(dealer.getUid(),  doList, subAreaList);
        LOGGER.info(String.format("Total Sales %s",String.valueOf(totalSales)));
        if (Objects.nonNull(dealer.getCounterPotential()) && dealer.getCounterPotential() > 0.0) {
            LOGGER.info(String.format("Dealer Counter Potential %s",String.valueOf(dealer.getCounterPotential())));
            LOGGER.info(String.format("Dealer Counter Share %s",String.valueOf(totalSales * 100 / dealer.getCounterPotential())));
            return (totalSales * 100 / dealer.getCounterPotential());
        }
        return 0.0;
    }

    private Integer getCounterShareForDealerYTD(EyDmsCustomerModel dealer, BaseSiteModel site, Date sd, Date ed, List<String> doList, List<String> subAreaList) {
        var totalSales = getCurrentMonthSaleQty(dealer.getCustomerNo(),  doList, subAreaList);
        if (Objects.nonNull(dealer.getCounterPotential()) && dealer.getCounterPotential() > 0.0) {
            return (int) (totalSales * 100 / dealer.getCounterPotential());
        }
        return 0;
    }

    private Integer getCounterShareForDealerMY(EyDmsCustomerModel dealer, BaseSiteModel site, int month, int year, List<String> doList, List<String> subAreaList) {
        var totalSales = getCurrentMonthSaleQty(dealer.getCustomerNo(),  month, year, doList, subAreaList);
        if (Objects.nonNull(dealer.getCounterPotential()) && dealer.getCounterPotential() > 0.0) {
            return (int) (totalSales * 100 / dealer.getCounterPotential());
        }
        return 0;
    }

    private String getStringDate(LocalDate localDate) {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime dateTime = localDate.atStartOfDay(zone);
        return formatter.format(Date.from(dateTime.toInstant()));
    }

    private Date getDateToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Double getYearToYearGrowthForDealer(String customerCode) {
        LocalDate currentYearCurrentDate = LocalDate.now();//2023-11-02
        LocalDate currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear(), Month.APRIL, 1);//2023-04-01
        if (currentYearCurrentDate.getMonth().compareTo(Month.APRIL) < 0) {
            currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear() - 1, Month.APRIL, 1);
        }
        LocalDate lastYearCurrentDate = LocalDate.now().minusYears(1);//2022-11-02

        LocalDate lastFinancialYearDate = currentFinancialYearDate.minusYears(1);//2022-04-01

        LOGGER.info(String.format("Getting yoy growth for customerNo :: %s currentYearCurrentDate :: %s currentFinancialYearDate :: %s lastFinancialYearDate :: %s", customerCode, currentYearCurrentDate, currentFinancialYearDate, lastFinancialYearDate));
        double lastSale = eydmsUserDao.getSalesQuantity(customerCode, getStringDate(lastFinancialYearDate), getStringDate(lastYearCurrentDate));
        double currentSale = eydmsUserDao.getSalesQuantity(customerCode, getStringDate(currentFinancialYearDate), getStringDate(currentYearCurrentDate));
        LOGGER.info(String.format("For CustomerNo :: %s lastSale :: %s currentSale :: %s", customerCode, lastSale, currentSale));
       /* if (lastSale > 0) {
            return (((lastSale - currentSale) / lastSale) * 100);
        }*/
        if (currentSale > 0 && lastSale > 0) {
            return (((currentSale - lastSale) / lastSale) * 100);
        }
        return 0.0;
    }

    private Double getYearToYearGrowthForRetailer(List<NirmanMitraSalesHistoryModel> sales) {
        LocalDate currentYearCurrentDate = LocalDate.now();
        LocalDate currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear(), Month.APRIL, 1);
        if (currentYearCurrentDate.getMonth().compareTo(Month.APRIL) < 0) {
            currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear() - 1, Month.APRIL, 1);
        }
        LocalDate lastYearCurrentDate = LocalDate.now().minusYears(1);

        LocalDate lastFinancialYearDate = currentFinancialYearDate.minusYears(1);
        double lastSale = sales.stream().filter(sale -> sale.getTransactionDate().after(getDateToDate(lastFinancialYearDate)) && sale.getTransactionDate().before(getDateToDate(lastYearCurrentDate))).mapToDouble(NirmanMitraSalesHistoryModel::getTransactionQuantity).sum();
        Date currentFYDate = getDateToDate(currentFinancialYearDate);
        double currentSale = sales.stream().filter(sale -> sale.getTransactionDate().after(currentFYDate) && sale.getTransactionDate().before(getDateToDate(currentYearCurrentDate))).mapToDouble(NirmanMitraSalesHistoryModel::getTransactionQuantity).sum();
        LOGGER.info(String.format("lastSale :: %s currentSale :: %s", lastSale, currentSale));
        if (lastSale > 0) {
            return (((lastSale - currentSale) / lastSale) * 100);
        }
        return 0.0;
    }

    private Double getYearToYearGrowthForRetailerFromOrderReq(List<EyDmsCustomerModel> sales) {
        LocalDate currentYearCurrentDate = LocalDate.now();
        LocalDate currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear(), Month.APRIL, 1);
        if (currentYearCurrentDate.getMonth().compareTo(Month.APRIL) < 0) {
            currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear() - 1, Month.APRIL, 1);
        }
        LocalDate lastYearCurrentDate = LocalDate.now().minusYears(1);

        LocalDate lastFinancialYearDate = currentFinancialYearDate.minusYears(1);
        List<EyDmsCustomerModel> collect = sales.stream().filter(sale -> sale.getLastLiftingDate().after(getDateToDate(lastFinancialYearDate)) && sale.getLastLiftingDate().before(getDateToDate(lastYearCurrentDate))).collect(Collectors.toList());
        double lastSale=0.0,currentSale=0.0;
        for (EyDmsCustomerModel customerModel : collect) {
            OrderRequisitionModel eydmsCustomerFromOrderReq = salesPerformanceDao.getEyDmsCustomerFromOrderReq(customerModel);
            if(eydmsCustomerFromOrderReq.getQuantity()!=null){
                lastSale+=eydmsCustomerFromOrderReq.getQuantity();
            }
            else{
                lastSale=0.0;
            }
        }
        Date currentFYDate = getDateToDate(currentFinancialYearDate);
        List<EyDmsCustomerModel> eydmsCustomerModelStream = sales.stream().filter(sale -> sale.getLastLiftingDate().after(currentFYDate) && sale.getLastLiftingDate().before(getDateToDate(currentYearCurrentDate))).collect(Collectors.toList());
        for (EyDmsCustomerModel customerModel : eydmsCustomerModelStream) {
            OrderRequisitionModel eydmsCustomerFromOrderReq = salesPerformanceDao.getEyDmsCustomerFromOrderReq(customerModel);
            if(eydmsCustomerFromOrderReq.getQuantity()!=null)
                 currentSale+=eydmsCustomerFromOrderReq.getQuantity();
        }

        if (lastSale > 0) {
            return (((lastSale - currentSale) / lastSale) * 100);
        }
        return 0.0;
    }

    /*private Double getYearToYearGrowthRetailerForDealer(EyDmsCustomerModel customerModel) {
        LocalDate currentdate=LocalDate.now();
        LocalDate lastYearCurrentMonth=currentdate.minusYears(1);
        LocalDate startDate1=LocalDate.of(currentdate.getYear(),currentdate.getMonth(),1);
        LocalDate endDate1=LocalDate.of(currentdate.getYear(),currentdate.getMonth(),currentdate.getDayOfMonth());

        LocalDate startDate2=LocalDate.of(lastYearCurrentMonth.getYear(),lastYearCurrentMonth.getMonth(),1);
        LocalDate endDate2=LocalDate.of(lastYearCurrentMonth.getYear(),lastYearCurrentMonth.getMonth(),currentdate.getDayOfMonth());
        double currentSale=0.0,lastSale=0.0;
        OrderRequisitionModel eydmsCustomerFromOrderReqForLast = salesPerformanceDao.getRetailerFromOrderReqDateConstraint(customerModel, getDateToDate(startDate2),getDateToDate(endDate2));
        if(eydmsCustomerFromOrderReqForLast!=null) {
            if (eydmsCustomerFromOrderReqForLast.getQuantity() != null)
                lastSale += eydmsCustomerFromOrderReqForLast.getQuantity();
        }
        OrderRequisitionModel eydmsCustomerFromOrderReqForCur = salesPerformanceDao.getRetailerFromOrderReqDateConstraint(customerModel, getDateToDate(startDate1),getDateToDate(endDate1));
        if(eydmsCustomerFromOrderReqForCur!=null) {
            if (eydmsCustomerFromOrderReqForCur.getQuantity() != null)
                currentSale += eydmsCustomerFromOrderReqForCur.getQuantity();
        }
        LOGGER.info("SALES Cur::: Last "+ currentSale + " "+lastSale);
        double yoy=(((currentSale - lastSale) / lastSale) * 100);
        return yoy;
    }*/
   private Double getYearToYearGrowthRetailerForDealer(EyDmsCustomerModel customerModel) {
        LocalDate currentYearCurrentDate = LocalDate.now();
        LocalDate currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear(), Month.APRIL, 1);
        if (currentYearCurrentDate.getMonth().compareTo(Month.APRIL) < 0) {
            currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear() - 1, Month.APRIL, 1);
        }
        LocalDate lastYearCurrentDate = LocalDate.now().minusYears(1);
        LocalDate lastFinancialYearDate = currentFinancialYearDate.minusYears(1);
        double lastSale=0.0,currentSale=0.0;

           lastSale= salesPerformanceDao.getRetailerFromOrderReq(customerModel,getDateToDate(lastFinancialYearDate),getDateToDate(lastYearCurrentDate));
            LOGGER.info("Last Sale:::"+lastSale);
            currentSale = salesPerformanceDao.getRetailerFromOrderReq(customerModel, getDateToDate(currentFinancialYearDate),getDateToDate(currentYearCurrentDate));
            LOGGER.info("Current Sale:::"+currentSale);
        if ((lastSale) > 0) {
            return ((((currentSale) - (lastSale)) / (lastSale)) * 100);
       }
       return 0.0;
    }

    private double getCounterShareForRetailer(EyDmsCustomerModel customerModel, List<NirmanMitraSalesHistoryModel> salesHistry) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date startTime = cal.getTime();
        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date end = new Date();
        var saleQuantity = salesHistry.stream().filter(sale -> sale.getTransactionDate().after(startTime) && sale.getTransactionDate().before(end)).mapToDouble(NirmanMitraSalesHistoryModel::getTransactionQuantity).sum();
        var counterPotential = customerModel.getCounterPotential();
        if (Objects.nonNull(counterPotential)) {
            return (saleQuantity * 100) / counterPotential;
        }
        return 0.0;
    }

    private double getCounterShareForRetailerFromOR(EyDmsCustomerModel customerModel, List<EyDmsCustomerModel> saleQty) {
        Double saleQuantity=0.0;
        LocalDate currentDate = LocalDate.now();
        LocalDate of = LocalDate.of(currentDate.getYear(), currentDate.getMonth(), 1);
        LocalDate of1 = LocalDate.of(currentDate.getYear(), currentDate.getMonth(), currentDate.lengthOfMonth());
        Date startDate = Date.from(of.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(of1.atStartOfDay(ZoneId.systemDefault()).toInstant());
        for (EyDmsCustomerModel eydmsCustomerModel : saleQty) {
            OrderRequisitionModel eydmsCustomerFromOrderReq = salesPerformanceDao.getEyDmsCustomerFromOrderReq(eydmsCustomerModel);
            saleQuantity += eydmsCustomerFromOrderReq.getQuantity();
        }
            var counterPotential = customerModel.getCounterPotential();
            if (Objects.nonNull(counterPotential)) {
                return (saleQuantity * 100) / counterPotential;
            }
            else {
                return 0.0;
            }
    }
    private double getCounterShareRetailerForDealer(EyDmsCustomerModel customerModel) {
        Double saleQuantity=0.0;
        LOGGER.info(String.format("Customer id:%s",String.valueOf(customerModel.getUid())));
        LocalDate currentDate = LocalDate.now();
        LocalDate of = LocalDate.of(currentDate.getYear(), currentDate.getMonth(), 1);
      //  LocalDate of1 = LocalDate.of(currentDate.getYear(), currentDate.getMonth(), currentDate.lengthOfMonth());
        Date startDate = Date.from(of.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        LOGGER.info(String.format("Customer id:%s",String.valueOf(customerModel.getUid())));
        saleQuantity = salesPerformanceDao.getRetailerFromOrderReq(customerModel,startDate,endDate);
           
        var counterPotential = customerModel.getCounterPotential()!=null?customerModel.getCounterPotential():0.0;
        LOGGER.info(String.format("Customer potential:%s",String.valueOf(customerModel.getCounterPotential()!=null?customerModel.getCounterPotential():0.0)));
        if(counterPotential!=0.0)
            return    ((saleQuantity/20) * 100) / (counterPotential/20);
        else
            return 0.0;
    }

    private double getCounterShareForRetailerFromORMonthYear(EyDmsCustomerModel customerModel, List<OrderRequisitionModel> saleQty, int month, int year) {
        LocalDate of = LocalDate.of(year, month, 1);
        LocalDate of1 = LocalDate.of(of.getYear(), of.getMonth(), of.lengthOfMonth());
        Date startDate = Date.from(of.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(of1.atStartOfDay(ZoneId.systemDefault()).toInstant());

        var saleQuantity = saleQty.stream().filter(sale -> sale.getDeliveredDate().after(startDate) && sale.getDeliveredDate().before(endDate)).mapToDouble(OrderRequisitionModel::getQuantity).sum();
        var counterPotential = customerModel.getCounterPotential();
        if (Objects.nonNull(counterPotential)) {
            return (saleQuantity * 100) / counterPotential;
        }
        return 0.0;
    }

    private double getCounterShareForRetailerYTDFromOR(EyDmsCustomerModel customerModel, List<OrderRequisitionModel> saleQty) {
        List<Date> fiscalYearforYTDCY = findFiscalYearforYTDCY();
        Date date1 = fiscalYearforYTDCY.get(0);
        Date date2 = fiscalYearforYTDCY.get(1);
        var saleQuantity = saleQty.stream().filter(sale -> sale.getDeliveredDate().after(date1) && sale.getDeliveredDate().before(date2)).mapToDouble(OrderRequisitionModel::getQuantity).sum();
        var counterPotential = customerModel.getCounterPotential();
        if (Objects.nonNull(counterPotential)) {
            return (saleQuantity * 100) / counterPotential;
        }
        return 0.0;
    }

    @Override
    public SalesPerformNetworkDetailsListData getListOfAllZeroLifting(String fields, BaseSiteModel site, String customerType, String searchKey, List<String> doList, List<String> subAreaList) {
            SalesPerformNetworkDetailsListData dealerCurrentNetworkListData = new SalesPerformNetworkDetailsListData();
            List<SalesPerformNetworkDetailsData> currentNetworkWsDataList = new ArrayList<>();

            List<SalesPerformNetworkDetailsData> collect1, collect;
            List<SalesPerformNetworkDetailsData> data = new ArrayList<>();
            RequestCustomerData requestData = new RequestCustomerData();
            List<SubAreaMasterModel> soList=new ArrayList<>();
            List<DistrictMasterModel> districtList=new ArrayList<>();
            if(subAreaList!=null && !subAreaList.isEmpty()){
            for (String id : subAreaList) {
                soList.add(territoryManagementDao.getTerritoryById(id));
            }
            if(soList.get(0)!=null)
                requestData.setSubAreaMasterPk(soList.get(0).getPk().toString());
             }
            if (doList != null && !doList.isEmpty()) {
            for (String code : doList) {
                districtList.add(districtMasterDao.findByCode(code));
            }
            if(districtList.get(0)!=null)
                requestData.setDistrict(districtList.get(0).getName());
            }
            List<String> counterType = List.of(customerType);
            requestData.setCounterType(counterType);
            List<EyDmsCustomerModel> eydmsCustomerFilteredList = territoryManagementService.getCustomerforUser(requestData);
            LOGGER.info(String.format("size of Cust before:%s",eydmsCustomerFilteredList.size()));
            List<EyDmsCustomerModel> customerFilteredList=new ArrayList<>();
            if(eydmsCustomerFilteredList!=null && !eydmsCustomerFilteredList.isEmpty()) {
                customerFilteredList = salesPerformanceDao.getEyDmsCustomerZeroLiftingList(eydmsCustomerFilteredList);
            }
            LOGGER.info(String.format("size of Cust after:%s",customerFilteredList.size()));

            if (StringUtils.isNotBlank(searchKey)) {
                customerFilteredList = filterEyDmsCustomersWithSearchTerm(customerFilteredList, searchKey);
            }

            if (customerFilteredList != null && !customerFilteredList.isEmpty()) {

                for (EyDmsCustomerModel customerModel : customerFilteredList) {

                    SalesPerformNetworkDetailsData dealerCurrentNetworkData = new SalesPerformNetworkDetailsData();

                    dealerCurrentNetworkData.setCode(customerModel.getUid());
                    dealerCurrentNetworkData.setName(customerModel.getName());
                    dealerCurrentNetworkData.setPotential(Objects.nonNull(customerModel.getCounterPotential()) ? String.valueOf(customerModel.getCounterPotential()) : ZERO);

                    if (customerType.equalsIgnoreCase(DEALER)) {
                        dealerCurrentNetworkData.setCounterSharePercentage(getCounterShareForDealer(customerModel,  doList, subAreaList));
                        double target = eydmsUserDao.getCustomerTarget(customerModel.getUid(), EyDmsDateUtility.getMonth(new Date()), EyDmsDateUtility.getYear(new Date()));
                        dealerCurrentNetworkData.setTarget(Objects.nonNull(target) ? String.valueOf(target) : ZERO);

                        dealerCurrentNetworkData.setSalesQuantity(setSalesQuantityForCustomer(customerModel, customerType,  doList, subAreaList));
                        if(customerModel.getCustomerNo()!=null) {
                            double totalOutstanding = djpVisitDao.getDealerOutstandingAmount(customerModel.getCustomerNo());
                            dealerCurrentNetworkData.setOutstandingAmount(df.format(totalOutstanding));
                        }

                        dealerCurrentNetworkData.setGrowthRateYoYPercentage(df.format(getYearToYearGrowthForDealer(customerModel.getUid())));
                        dealerCurrentNetworkData.setDaysSinceLastOrder(String.valueOf(getDaysFromLastOrder(customerModel.getOrders())));
                    }

                    List<SubAreaMasterModel> subAreaMasterModelList = territoryManagementService.getTerritoriesForCustomer(customerModel);
                    if (CollectionUtils.isNotEmpty(subAreaMasterModelList) && subAreaMasterModelList.get(0) != null) {
                        if (subAreaMasterModelList.get(0).getDistrict() != null && subAreaMasterModelList.get(0).getTaluka() != null) {
                            dealerCurrentNetworkData.setDistrict(subAreaMasterModelList.get(0).getDistrict());
                            dealerCurrentNetworkData.setTaluka(subAreaMasterModelList.get(0).getTaluka());
                        }
                    }
                    currentNetworkWsDataList.add(dealerCurrentNetworkData);
                 }
            }
        if (customerType.equalsIgnoreCase(DEALER)) {
            collect = currentNetworkWsDataList.stream().filter(nw -> Objects.nonNull(nw.getSalesQuantity())).filter(nw -> Objects.nonNull(nw.getSalesQuantity().getActual())).filter(nw -> nw.getSalesQuantity().getActual() == 0).collect(Collectors.toList());
            data = collect.stream().sorted(Comparator.comparing(nw -> nw.getSalesQuantity().getActual())).collect(Collectors.toList());
        }
        if (data.size() > 0) {
         dealerCurrentNetworkListData.setNetworkDetails(data);
        }
        return dealerCurrentNetworkListData;
    }

    @Override
    public SalesPerformNetworkDetailsListData getListOfAllZeroLiftingRetailerInfForDealer(String fields, String customerType, String searchKey) {

        SalesPerformNetworkDetailsListData dealerCurrentNetworkListData = new SalesPerformNetworkDetailsListData();
        List<SalesPerformNetworkDetailsData> currentNetworkWsDataList = new ArrayList<>();

        List<SalesPerformNetworkDetailsData> collect1, collect;
        List<SalesPerformNetworkDetailsData> data = new ArrayList<>();

        List<EyDmsCustomerModel> eydmsCustomerFilteredList = new ArrayList<>();
        List<EyDmsCustomerModel> customerFilteredList = new ArrayList<>();

        EyDmsCustomerModel currentUser = (EyDmsCustomerModel) userService.getCurrentUser();
        if(currentUser != null) {
            if (currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                if(!currentUser.getCounterType().equals(CounterType.SP)) {
                    if (customerType.equalsIgnoreCase(RETAILER)) {
                        eydmsCustomerFilteredList = territoryManagementService.getRetailerListForDealer();
                    } else if (customerType.equalsIgnoreCase(INFLUENCER)) {
                        eydmsCustomerFilteredList = territoryManagementService.getInfluencerListForDealer();
                    }
                }
            }
        }
        if(eydmsCustomerFilteredList!=null && !eydmsCustomerFilteredList.isEmpty()) {
            customerFilteredList = salesPerformanceDao.getEyDmsCustomerZeroLiftingList(eydmsCustomerFilteredList);
        }

        if (StringUtils.isNotBlank(searchKey)) {
            customerFilteredList = filterEyDmsCustomersWithSearchTerm(customerFilteredList, searchKey);
        }

        if (customerFilteredList != null && !customerFilteredList.isEmpty()) {

            for (EyDmsCustomerModel customerModel : customerFilteredList) {

                SalesPerformNetworkDetailsData dealerCurrentNetworkData = new SalesPerformNetworkDetailsData();

                dealerCurrentNetworkData.setCode(customerModel.getUid());
                dealerCurrentNetworkData.setName(customerModel.getName());
                if(customerModel.getCounterPotential()!=null) {
                    dealerCurrentNetworkData.setPotential(String.valueOf(customerModel.getCounterPotential()/20));
                }
                else{
                    dealerCurrentNetworkData.setPotential(ZERO);
                }
                if (customerType.equalsIgnoreCase(RETAILER)) {
                    SalesQuantityData sales = new SalesQuantityData();
                    sales.setCurrent(0.0);
                    sales.setLastYear(0.0);
                        dealerCurrentNetworkData.setSalesQuantity(sales);
                        dealerCurrentNetworkData.setCounterSharePercentage(0.0);
                        dealerCurrentNetworkData.setDaysSinceLastOrder("-");
                        dealerCurrentNetworkData.setGrowthRateYoYPercentage(df.format(getYearToYearGrowthRetailerForDealer(customerModel)));
                    }
                 else if (customerType.equalsIgnoreCase(INFLUENCER)) {
                    double salesQuantity=0.0;
                    dealerCurrentNetworkData.setCategory(customerModel.getInfluencerType() != null ?
                            enumerationService.getEnumerationName(customerModel.getInfluencerType()) : "-");
                    dealerCurrentNetworkData.setContactNumber(customerModel.getContactNumber());
                    dealerCurrentNetworkData.setTimesContacted(customerModel.getTimesContacted());
                    dealerCurrentNetworkData.setType(customerModel.getNetworkType());
                    dealerCurrentNetworkData.setPoints(Objects.nonNull(customerModel.getAvailablePoints()) ? customerModel.getAvailablePoints() : 0.0);
                    dealerCurrentNetworkData.setBagLifted(salesQuantity);
                    dealerCurrentNetworkData.setBagLiftedNo(salesQuantity);
                    dealerCurrentNetworkData.setBagLiftedQty(String.valueOf(salesQuantity));
                    dealerCurrentNetworkData.setDaySinceLastLifting("-");
                }

                List<SubAreaMasterModel> subAreaMasterModelList = territoryManagementService.getTerritoriesForCustomer(customerModel);
                if (CollectionUtils.isNotEmpty(subAreaMasterModelList) && subAreaMasterModelList.get(0) != null) {
                    if (subAreaMasterModelList.get(0).getDistrict() != null && subAreaMasterModelList.get(0).getTaluka() != null) {
                        dealerCurrentNetworkData.setDistrict(subAreaMasterModelList.get(0).getDistrict());
                        dealerCurrentNetworkData.setTaluka(subAreaMasterModelList.get(0).getTaluka());
                    }
                }
                currentNetworkWsDataList.add(dealerCurrentNetworkData);
            }
        }

        if (customerType.equalsIgnoreCase(INFLUENCER)) {
            collect = currentNetworkWsDataList.stream().filter(nw -> Objects.nonNull(nw.getBagLifted())).filter(nw -> nw.getBagLifted() == 0 || nw.getBagLifted() == 0.0).collect(Collectors.toList());
            data = collect.stream().sorted(Comparator.comparing(SalesPerformNetworkDetailsData::getBagLifted)).collect(Collectors.toList());
        } else if (customerType.equalsIgnoreCase(RETAILER)) {
            collect = currentNetworkWsDataList.stream().filter(nw -> Objects.nonNull(nw.getSalesQuantity())).filter(nw -> Objects.nonNull(nw.getSalesQuantity().getCurrent())).filter(nw -> nw.getSalesQuantity().getCurrent() == 0 || nw.getSalesQuantity().getCurrent() == 0.0 ).collect(Collectors.toList());
            data = collect.stream().sorted(Comparator.comparing(nw -> nw.getSalesQuantity().getCurrent())).collect(Collectors.toList());
        }
        dealerCurrentNetworkListData.setNetworkDetails(data);
        return dealerCurrentNetworkListData;
    }


    @Override
    public SalesPerformNetworkDetailsListData getListOfAllLowPerforming(String fields, String customerType, String searchKey, List<String> doList, List<String> subAreaList) {
       SalesPerformNetworkDetailsListData dealerCurrentNetworkListData = new SalesPerformNetworkDetailsListData();
            List<SalesPerformNetworkDetailsData> currentNetworkWsDataList = new ArrayList<>();
            List<SalesPerformNetworkDetailsData> collect1, collect;
            List<SalesPerformNetworkDetailsData> data = new ArrayList<>();

            RequestCustomerData requestData = new RequestCustomerData();
            List<String> counterType = List.of(customerType);
            requestData.setCounterType(counterType);


            List<EyDmsCustomerModel> eydmsCustomerFilteredList = territoryManagementService.getCustomerforUser(requestData);
            LOGGER.info(String.format("Size before last lifting from eydms customer:%s",String.valueOf(eydmsCustomerFilteredList.size())));

            List<EyDmsCustomerModel> customerFilteredList=new ArrayList<>();
            if(eydmsCustomerFilteredList!=null && !eydmsCustomerFilteredList.isEmpty()) {
                customerFilteredList = salesPerformanceDao.getEyDmsCustomerLastLiftingList(eydmsCustomerFilteredList);
            }
            LOGGER.info(String.format("Size after last lifting from eydms customer:%s",String.valueOf(customerFilteredList.size())));


            if (StringUtils.isNotBlank(searchKey)) {
                customerFilteredList = filterEyDmsCustomersWithSearchTerm(customerFilteredList, searchKey);
            }

            if (customerFilteredList != null && !customerFilteredList.isEmpty()) {

                for (EyDmsCustomerModel customerModel : customerFilteredList) {

                    SalesPerformNetworkDetailsData dealerCurrentNetworkData = new SalesPerformNetworkDetailsData();

                    dealerCurrentNetworkData.setCode(customerModel.getUid());
                    dealerCurrentNetworkData.setName(customerModel.getName());
                    dealerCurrentNetworkData.setPotential(Objects.nonNull(customerModel.getCounterPotential()) ? String.valueOf(customerModel.getCounterPotential()) : ZERO);

                    if (customerType.equalsIgnoreCase(DEALER)) {
                        dealerCurrentNetworkData.setCounterSharePercentage(getCounterShareForDealer(customerModel,  doList, subAreaList));
                        double target = eydmsUserDao.getCustomerTarget(customerModel.getUid(), EyDmsDateUtility.getMonth(new Date()), EyDmsDateUtility.getYear(new Date()));
                        dealerCurrentNetworkData.setTarget(Objects.nonNull(target) ? String.valueOf(target) : ZERO);

                        dealerCurrentNetworkData.setSalesQuantity(setSalesQuantityForCustomer(customerModel, customerType,  doList, subAreaList));
                        if(customerModel.getCustomerNo()!=null) {
                            double totalOutstanding = djpVisitDao.getDealerOutstandingAmount(customerModel.getCustomerNo());
                            dealerCurrentNetworkData.setOutstandingAmount(df.format(totalOutstanding));
                        }

                        dealerCurrentNetworkData.setGrowthRateYoYPercentage(df.format(getYearToYearGrowthForDealer(customerModel.getUid())));
                        dealerCurrentNetworkData.setDaysSinceLastOrder(getDaysSinceLastLiftingInfForDealer(customerModel));
                    }

                    List<SubAreaMasterModel> subAreaMasterModelList = territoryManagementService.getTerritoriesForCustomer(customerModel);
                    if (CollectionUtils.isNotEmpty(subAreaMasterModelList) && subAreaMasterModelList.get(0) != null) {
                        if (subAreaMasterModelList.get(0).getDistrict() != null && subAreaMasterModelList.get(0).getTaluka() != null) {
                            dealerCurrentNetworkData.setDistrict(subAreaMasterModelList.get(0).getDistrict());
                            dealerCurrentNetworkData.setTaluka(subAreaMasterModelList.get(0).getTaluka());
                        }
                    }
                    currentNetworkWsDataList.add(dealerCurrentNetworkData);
                }
            }

            if (customerType.equalsIgnoreCase(DEALER)) {
                LOGGER.info((String.format("Size for DEALER:%s", currentNetworkWsDataList.size())));
                for (SalesPerformNetworkDetailsData detailsData : currentNetworkWsDataList) {
                    LOGGER.info((String.format("code for DEALER:%s.", detailsData.getCode())));
                }
                collect = currentNetworkWsDataList.stream().filter(nw -> Objects.nonNull(nw.getSalesQuantity())).filter(nw -> Objects.nonNull(nw.getSalesQuantity().getActual())).filter(nw -> nw.getSalesQuantity().getActual() != 0).collect(Collectors.toList());
                if (collect.size() > 0)
                    data = collect.stream().sorted(Comparator.comparing(nw -> nw.getSalesQuantity().getActual())).collect(Collectors.toList());
            }
            if (data.size() > 0) {
                dealerCurrentNetworkListData.setNetworkDetails(data.stream().limit(((long) data.size() * LOWPERCENTAGE) / 100).collect(Collectors.toList()));
            }
            else{
                List<SalesPerformNetworkDetailsData> salesPerformNetworkDetailsData = new ArrayList<>();
                dealerCurrentNetworkListData.setNetworkDetails(salesPerformNetworkDetailsData);
            }
            return dealerCurrentNetworkListData;
        }

    @Override
    public SalesPerformNetworkDetailsListData getListOfAllLowPerformingRetailerInfForDealers(String fields, String customerType, String searchKey) {
        SalesPerformNetworkDetailsListData dealerCurrentNetworkListData = new SalesPerformNetworkDetailsListData();
        List<SalesPerformNetworkDetailsData> currentNetworkWsDataList = new ArrayList<>();
        List<SalesPerformNetworkDetailsData> collect1, collect;
        List<SalesPerformNetworkDetailsData> data = new ArrayList<>();
        List<EyDmsCustomerModel> eydmsCustomerFilteredList = new ArrayList<>();
        List<EyDmsCustomerModel> customerFilteredList = new ArrayList<>();

        EyDmsCustomerModel currentUser = (EyDmsCustomerModel) userService.getCurrentUser();
        if(currentUser != null) {
            if (currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                if(!currentUser.getCounterType().equals(CounterType.SP)) {
                    if (customerType.equalsIgnoreCase(RETAILER)) {
                        eydmsCustomerFilteredList = territoryManagementService.getRetailerListForDealer();
                    } else if (customerType.equalsIgnoreCase(INFLUENCER)) {
                        eydmsCustomerFilteredList = territoryManagementService.getInfluencerListForDealer();
                    }
                }
            }
        }
        LOGGER.info(String.format("Size before last lifting from eydms customer:%s",String.valueOf(eydmsCustomerFilteredList.size())));

        if(eydmsCustomerFilteredList!=null && !eydmsCustomerFilteredList.isEmpty()) {
            customerFilteredList = salesPerformanceDao.getEyDmsCustomerLastLiftingList(eydmsCustomerFilteredList);
        }
        LOGGER.info(String.format("Size before last lifting from eydms customer:%s",String.valueOf(customerFilteredList.size())));


        if (StringUtils.isNotBlank(searchKey)) {
            customerFilteredList = filterEyDmsCustomersWithSearchTerm(customerFilteredList, searchKey);
        }

        if (customerFilteredList != null && !customerFilteredList.isEmpty()) {

            for (EyDmsCustomerModel customerModel : customerFilteredList) {

                SalesPerformNetworkDetailsData dealerCurrentNetworkData = new SalesPerformNetworkDetailsData();

                dealerCurrentNetworkData.setCode(customerModel.getUid());
                dealerCurrentNetworkData.setName(customerModel.getName());
                if(customerModel.getCounterPotential()!=null) {
                    dealerCurrentNetworkData.setPotential(String.valueOf(customerModel.getCounterPotential()/20));
                }
                else{
                    dealerCurrentNetworkData.setPotential(ZERO);
                }
                double counterShareRetailerForDealer=0.0,current=0.0,lastYear=0.0;
               if (customerType.equalsIgnoreCase(RETAILER)) {
                    EyDmsCustomerModel orderRequisitionSalesDataForRetailer = salesPerformanceDao.getRetailerSalesForDealerLowPerform(customerModel, baseSiteService.getCurrentBaseSite());
                    if (orderRequisitionSalesDataForRetailer != null) {
                        SalesQuantityData sales = new SalesQuantityData();
                        counterShareRetailerForDealer = getCounterShareRetailerForDealer(orderRequisitionSalesDataForRetailer);
                            current=getSalesQuantityCurrentMonthRetailer(orderRequisitionSalesDataForRetailer);
                            lastYear=getSalesQuantityLastYearCurrentMonthRetailer(orderRequisitionSalesDataForRetailer);
                        dealerCurrentNetworkData.setCounterSharePercentage(counterShareRetailerForDealer);
                        sales.setCurrent(current);
                        sales.setLastYear(lastYear);
                        dealerCurrentNetworkData.setSalesQuantity(sales);
                        dealerCurrentNetworkData.setDaysSinceLastOrder(getDaysSinceLastLiftingRetailerForDealer(customerModel));
                        dealerCurrentNetworkData.setGrowthRateYoYPercentage(df.format(getYearToYearGrowthRetailerForDealer(customerModel)));
                    }
                } else if (customerType.equalsIgnoreCase(INFLUENCER)) {
                   double salesQuantity=0.0;
                    dealerCurrentNetworkData.setCategory(customerModel.getInfluencerType() != null ?
                            enumerationService.getEnumerationName(customerModel.getInfluencerType()) : "-");
                    dealerCurrentNetworkData.setContactNumber(customerModel.getContactNumber());
                    dealerCurrentNetworkData.setTimesContacted(customerModel.getTimesContacted());
                    dealerCurrentNetworkData.setType(customerModel.getNetworkType());
                    dealerCurrentNetworkData.setPoints(Objects.nonNull(customerModel.getAvailablePoints()) ? customerModel.getAvailablePoints() : 0.0);

                    List<DealerInfluencerMapModel> pointRequisitionSalesDataForRetailer = salesPerformanceDao.getInfluencerSalesForDealerLowPerform(customerModel, baseSiteService.getCurrentBaseSite());
                    if (pointRequisitionSalesDataForRetailer != null && !pointRequisitionSalesDataForRetailer.isEmpty()) {
                        for (DealerInfluencerMapModel dealerInfluencerMapModel : pointRequisitionSalesDataForRetailer) {
                            salesQuantity += getSalesQuantityInfForDealer(dealerInfluencerMapModel.getInfluencer());
                        }
                        dealerCurrentNetworkData.setBagLiftedNo(salesQuantity);
                        dealerCurrentNetworkData.setBagLifted(salesQuantity);
                        dealerCurrentNetworkData.setBagLiftedQty(String.valueOf(salesQuantity));
                        dealerCurrentNetworkData.setDaySinceLastLifting(getDaysSinceLastLiftingInfForDealer(customerModel));
                    }
                    else{
                        dealerCurrentNetworkData.setBagLiftedQty(String.valueOf(salesQuantity));
                        dealerCurrentNetworkData.setDaySinceLastLifting("-");
                    }
                }

                List<SubAreaMasterModel> subAreaMasterModelList = territoryManagementService.getTerritoriesForCustomer(customerModel);
                if (CollectionUtils.isNotEmpty(subAreaMasterModelList) && subAreaMasterModelList.get(0) != null) {
                    if (subAreaMasterModelList.get(0).getDistrict() != null && subAreaMasterModelList.get(0).getTaluka() != null) {
                        dealerCurrentNetworkData.setDistrict(subAreaMasterModelList.get(0).getDistrict());
                        dealerCurrentNetworkData.setTaluka(subAreaMasterModelList.get(0).getTaluka());
                    }
                }
                currentNetworkWsDataList.add(dealerCurrentNetworkData);
            }
        }

        if (customerType.equalsIgnoreCase(INFLUENCER)) {
            LOGGER.info((String.format("Size for INFLUENCER:%s", currentNetworkWsDataList.size())));//6
            collect1 = currentNetworkWsDataList.stream().filter(nw -> Objects.nonNull(nw.getBagLifted())).filter(nw -> nw.getBagLifted() != 0 || nw.getBagLifted()!=0.0).collect(Collectors.toList());
            LOGGER.info((String.format("Size for INFLUENCER after filter:%s", collect1.size())));
            if (collect1.size() > 0)
                data = collect1.stream().sorted(Comparator.comparing(SalesPerformNetworkDetailsData::getBagLifted)).collect(Collectors.toList());
        } else if (customerType.equalsIgnoreCase(RETAILER)) {
            LOGGER.info((String.format("Size for RETAILER:%s", currentNetworkWsDataList.size())));
            collect = currentNetworkWsDataList.stream().filter(nw -> Objects.nonNull(nw.getSalesQuantity())).filter(nw -> Objects.nonNull(nw.getSalesQuantity().getCurrent())).filter(nw -> nw.getSalesQuantity().getCurrent() != 0 || nw.getSalesQuantity().getCurrent()!=0.0).collect(Collectors.toList());
            LOGGER.info((String.format("Size for RETAILER after filter:%s", collect.size())));
            if (collect.size() > 0)
                data = collect.stream().sorted(Comparator.comparing(nw -> nw.getSalesQuantity().getCurrent())).collect(Collectors.toList());
        }
        if (data.size() > 0) {
            dealerCurrentNetworkListData.setNetworkDetails(data.stream().limit(((long) data.size() * LOWPERCENTAGE) / 100).collect(Collectors.toList()));
        }
        else{
            List<SalesPerformNetworkDetailsData> salesPerformNetworkDetailsData = new ArrayList<>();
            dealerCurrentNetworkListData.setNetworkDetails(salesPerformNetworkDetailsData);
        }
        return dealerCurrentNetworkListData;
    }


    @Override
    public SearchPageData<SalesPerformNetworkDetailsData> getLowPerformingViewDetailsWithPagination(String fields, BaseSiteModel site, String customerType, String searchKey, List<String> doList, List<String> subAreaList, SearchPageData searchPageData) {
            final SearchPageData<SalesPerformNetworkDetailsData> result = new SearchPageData<>();
            /*
            result.setResults(data);
            result.setPagination(customerFilteredListSearch.getPagination());
            result.setSorts(customerFilteredListSearch.getSorts());*/
        return result;
    }
    @Override
    public SearchPageData<SalesPerformNetworkDetailsData> getZeroLiftingViewDetailsWithPagination(String fields, BaseSiteModel site, String customerType, String searchKey, List<String> doList, List<String> subAreaList, SearchPageData searchPageData) {
        final SearchPageData<SalesPerformNetworkDetailsData> result = new SearchPageData<>();
        /*
        result.setResults(data);
        result.setPagination(customerFilteredListSearch.getPagination());
        result.setSorts(customerFilteredListSearch.getSorts());
       */
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Double getPremiumSalesLeaderByDeliveryDate(EyDmsCustomerModel so, BaseSiteModel currentBaseSite, Date startDate, Date endDate) {
        return (Double) sessionService.executeInLocalView(new SessionExecutionBody()
        {
            @Override
            public Double execute()
            {
                try {
                    searchRestrictionService.disableSearchRestrictions();
                    return getSalesPerformanceDao().getActualTargetForPremiumSalesLeaderYTD(so, currentBaseSite, startDate, endDate);
                }
                finally {
                    searchRestrictionService.enableSearchRestrictions();
                }
            }
        });
    }

    @Override
    public Double getPremiumSalesLeaderByDeliveryDateRetailer(EyDmsCustomerModel eydmsUser, BaseSiteModel currentBaseSite, Date startDate, Date endDate) {
        return getSalesPerformanceDao().getActualTargetForPremiumSalesLeaderYTDRetailer(eydmsUser, currentBaseSite, startDate, endDate);
    }

    @Override
    public Double getCashDiscountAvailedPercentage(EyDmsCustomerModel so, BaseSiteModel currentBaseSite, Date startDate, Date endDate) {
        double totalAvailed = salesPerformanceDao.getTotalCDAvailedForDealer(so,startDate,endDate);
        double totalLost = salesPerformanceDao.getTotalCDLostForDealer(so,startDate,endDate);

        double availedPercent = 0.0;

        if((totalAvailed+totalLost)!=0.0)
        {
            availedPercent = (totalAvailed/(totalAvailed+totalLost))*100;
        }
        return availedPercent;
    }

    @Override
    public String getDaysSinceLastLiftingForRetailer(List<EyDmsCustomerModel> saleQty) {
        Optional<EyDmsCustomerModel> salesModel = saleQty.stream().max(Comparator.comparing(EyDmsCustomerModel::getLastLiftingDate));
        if (salesModel.isPresent()) {
            LocalDate today = LocalDate.now();
            LocalDate deliveredDate = salesModel.get().getLastLiftingDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            return String.valueOf(Math.toIntExact(ChronoUnit.DAYS.between(deliveredDate, today)));
        }
        return "-";
    }

    public String getDaysSinceLastLiftingRetailerForDealer(EyDmsCustomerModel cust) {
       // Optional<DealerRetailerMapModel> salesModel = saleQty.stream().max(Comparator.comparing(DealerRetailerMapModel::getLastLiftingDate));
       if (cust.getLastLiftingDate()!=null) {
           LocalDate today = LocalDate.now();
           LocalDate deliveredDate = cust.getLastLiftingDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
           return String.valueOf(Math.toIntExact(ChronoUnit.DAYS.between(deliveredDate, today)));
       }
        else{
            return "-";
        }
    }

    @Override
    public String getDaysSinceLastLiftingForInfluencer(List<EyDmsCustomerModel> saleQty) {

        Optional<EyDmsCustomerModel> salesModel = saleQty.stream().max(Comparator.comparing(EyDmsCustomerModel::getLastLiftingDate));
        if (salesModel.isPresent()) {
            LocalDate today = LocalDate.now();
            LocalDate deliveredDate = salesModel.get().getLastLiftingDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            return String.valueOf(Math.toIntExact(ChronoUnit.DAYS.between(deliveredDate, today)));
        }
        return "-";
    }
    public String getDaysSinceLastLiftingInfForDealer(EyDmsCustomerModel saleQty) {
        if (saleQty.getLastLiftingDate()!=null) {
            LocalDate today = LocalDate.now();
            LocalDate deliveredDate = saleQty.getLastLiftingDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            return String.valueOf(Math.toIntExact(ChronoUnit.DAYS.between(deliveredDate, today)));
        }
        return "-";
    }

    @Override
    public Double getPotentialForDealersNetwork(String subArea, BaseSiteModel baseSite) {
        return null;
    }

    private SalesQuantityData setSalesQuantityForCustomerForFY(EyDmsCustomerModel customerModel, String leadType, BaseSiteModel site, List<String> doList, List<String> subAreaList) {
        SalesQuantityData salesQuantityData = new SalesQuantityData();
        LOGGER.info(String.format("getting Sale Quantity for leadType :: %s customerUID :: %s", leadType, customerModel.getUid()));
        if (leadType.equalsIgnoreCase(DEALER)) {
            double lastMonth = getlastYearSaleQty(customerModel.getCustomerNo(), site, doList, subAreaList);
            double actual = getCurrentYearSaleQty(customerModel.getCustomerNo(), site, doList, subAreaList);
            salesQuantityData.setLastMonth(Objects.nonNull(lastMonth) ? lastMonth : 0.0);
            salesQuantityData.setActual(Objects.nonNull(actual) ? actual : 0.0);
            LOGGER.info(String.format("Sale Quantity for leadType :: %s customerUID :: %s lastMonth :: %s actual :: %s", leadType, customerModel.getUid(), lastMonth, actual));
        }
        return salesQuantityData;
    }

    private SalesQuantityData setSalesQuantityForCustomerForSpecificDate(EyDmsCustomerModel customerModel, String leadType, BaseSiteModel site, int month, int year, List<String> doList, List<String> subAreaList) {
        SalesQuantityData salesQuantityData = new SalesQuantityData();
        LOGGER.info(String.format("getting Sale Quantity for leadType :: %s customerUID :: %s", leadType, customerModel.getUid()));
        if (leadType.equalsIgnoreCase(DEALER)) {
            double lastMonth = getlastMonthSaleQty(customerModel.getCustomerNo(),  month, year, doList, subAreaList);
            double actual = getCurrentMonthSaleQty(customerModel.getCustomerNo(),  month, year, doList, subAreaList);
            salesQuantityData.setLastMonth(Objects.nonNull(lastMonth) ? lastMonth : 0.0);
            salesQuantityData.setActual(Objects.nonNull(actual) ? actual : 0.0);
            LOGGER.info(String.format("Sale Quantity for leadType :: %s customerUID :: %s lastMonth :: %s actual :: %s", leadType, customerModel.getUid(), lastMonth, actual));
        }
        return salesQuantityData;
    }

    private SalesQuantityData setSalesQuantityForCustomerForBot(EyDmsCustomerModel customerModel, String leadType,  List<String> doList, List<String> subAreaList) {
        SalesQuantityData salesQuantityData = new SalesQuantityData();
        LOGGER.info(String.format("getting Sale Quantity for leadType :: %s customerUID :: %s", leadType, customerModel.getUid()));
        if (leadType.equalsIgnoreCase(DEALER)) {
            double lastMonth = getlastMonthSaleQty(customerModel.getUid(),doList, subAreaList);
            double actual = getCurrentMonthSaleQty(customerModel.getUid(), doList, subAreaList);
            salesQuantityData.setLastMonth(Objects.nonNull(lastMonth) ? lastMonth : 0.0);
            salesQuantityData.setActual(Objects.nonNull(actual) ? actual : 0.0);
            LOGGER.info(String.format("Sale Quantity for leadType :: %s customerUID :: %s lastMonth :: %s actual :: %s", leadType, customerModel.getUid(), lastMonth, actual));
        }
        return salesQuantityData;
    }

    private SalesQuantityData setSalesQuantityForCustomer(EyDmsCustomerModel customerModel, String customerType, List<String> doList, List<String> subAreaList) {
        SalesQuantityData salesQuantityData = new SalesQuantityData();
        LOGGER.info(String.format("getting Sale Quantity for leadType :: %s customerUID :: %s", customerType, customerModel.getUid()));
        if (customerType.equalsIgnoreCase(DEALER)) {
            double lastMonth = getlastMonthSaleQty(customerModel.getUid(),  doList, subAreaList);
            double actual = getCurrentMonthSaleQty(customerModel.getUid(),  doList, subAreaList);
            salesQuantityData.setLastMonth(Objects.nonNull(lastMonth) ? lastMonth : 0.0);
            salesQuantityData.setActual(Objects.nonNull(actual) ? actual : 0.0);
            LOGGER.info(String.format("Sale Quantity for leadType :: %s customerUID :: %s lastMonth :: %s actual :: %s", customerType, customerModel.getUid(), lastMonth, actual));
        }
        return salesQuantityData;
    }

    private int getDaysFromLastOrder(Collection<OrderModel> orderList) {
        if (CollectionUtils.isEmpty(orderList)) {
            return 0;
        }
        Date lastOrderDate = orderList.stream().sorted(Comparator.comparing(OrderModel::getCreationtime).reversed()).collect(Collectors.toList()).get(0).getCreationtime();
        int numberOfDays;
        Date currentDate = new Date(System.currentTimeMillis());
        LocalDateTime current = LocalDateTime.ofInstant(currentDate.toInstant(), ZoneId.systemDefault());
        LocalDateTime last = LocalDateTime.ofInstant(lastOrderDate.toInstant(), ZoneId.systemDefault());
        LOGGER.info(String.format("Getting Number Of Days last :: %s current :: %s ", last, current));
        numberOfDays = (int) ChronoUnit.DAYS.between(last, current);
        return numberOfDays;

    }

    @Override
    public Integer getCountForAllDealerRetailerInfluencers(String leadType, B2BCustomerModel user, BaseSiteModel site,List<String> subAreaList,List<String> doList) {

        RequestCustomerData requestData = new RequestCustomerData();
        List<SubAreaMasterModel> soList=new ArrayList<>();
        List<DistrictMasterModel> districtList=new ArrayList<>();
        if(subAreaList!=null && !subAreaList.isEmpty()){
            for (String id : subAreaList) {
                soList.add(territoryManagementDao.getTerritoryById(id));
            }
            if(soList.get(0)!=null)
                requestData.setSubAreaMasterPk(soList.get(0).getPk().toString());
        }
        if (doList != null && !doList.isEmpty()) {
            for (String code : doList) {
                districtList.add(districtMasterDao.findByCode(code));
            }
            if(doList.get(0)!=null)
                requestData.setDistrict(districtList.get(0).getName());
        }
        List<String> counterType = List.of(leadType);
        requestData.setCounterType(counterType);
        List<EyDmsCustomerModel> customerFilteredList = territoryManagementService.getCustomerforUser(requestData);
        if(customerFilteredList.size()>0) {
            return customerFilteredList.size();
        }
        else {
            return 0;
        }

        /*int countOfInfluencer = 0;
        if (leadType.equalsIgnoreCase(RETAILER)) {
            customerFilteredList = territoryManagementService.getRetailersForSubArea();
            return customerFilteredList.isEmpty() ? 0 : customerFilteredList.size();
        } else if (leadType.equalsIgnoreCase(DEALER)) {
            customerFilteredList = territoryManagementService.getDealersForSubArea();
            return customerFilteredList.isEmpty() ? 0 : customerFilteredList.size();
        } else if (leadType.equalsIgnoreCase(INFLUENCER)) {
            customerFilteredList = territoryManagementService.getInfluencersForSubArea().stream().filter(cust -> Objects.nonNull(cust.getInfluencerType()) && Objects.nonNull(cust.getNirmanMitraCode())).collect(Collectors.toList());
            var architectList = customerFilteredList.stream().filter(cust -> cust.getInfluencerType().equals(InfluencerType.ARCHITECT)).collect(Collectors.toList());
            var engineerList = customerFilteredList.stream().filter(cust -> cust.getInfluencerType().equals(InfluencerType.ENGINEER)).collect(Collectors.toList());
            var masonList = customerFilteredList.stream().filter(cust -> cust.getInfluencerType().equals(InfluencerType.MASON)).collect(Collectors.toList());
            var contractorList = customerFilteredList.stream().filter(cust -> cust.getInfluencerType().equals(InfluencerType.CONTRACTOR)).collect(Collectors.toList());
            if (architectList != null && engineerList != null && masonList != null && contractorList != null)
                countOfInfluencer = architectList.size() + engineerList.size() + masonList.size() + contractorList.size();
            return countOfInfluencer;
        }*/
    }

    @Override
    public Integer getCountOfAllRetailersInfluencers(String leadType) {
        List<EyDmsCustomerModel> customerFilteredList = new ArrayList<>();
        EyDmsCustomerModel currentUser = (EyDmsCustomerModel) userService.getCurrentUser();
        if(currentUser != null) {
            if (currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                if(!currentUser.getCounterType().equals(CounterType.SP)) {
                    if (leadType.equalsIgnoreCase(RETAILER)) {
                        customerFilteredList = territoryManagementService.getRetailerListForDealer();
                    } else if (leadType.equalsIgnoreCase(INFLUENCER)) {
                        customerFilteredList = territoryManagementService.getInfluencerListForDealer();
                    }
                }
            }
        }
        return Math.max(customerFilteredList.size(), 0);
    }


    @Override
    public List<String> getStateWiseProductForSummaryPage(String subArea) {
        String baseSite = baseSiteService.getCurrentBaseSite().getUid();
        String catalogId = baseSite.concat("ProductCatalog");
        String prodStatus = "approved";
        String version = "Online";
        return salesPerformanceDao.getStateWiseProductForSummaryPage(subArea, catalogId, version, prodStatus);
    }

    @Override
    public SalesPerformNetworkDetailsListData getBottomLaggingCounters(List<String> doList, List<String> subAreaList) {
        String leadType = DEALER;
        SalesPerformNetworkDetailsListData dealerCurrentNetworkListData = new SalesPerformNetworkDetailsListData();
        List<SalesPerformNetworkDetailsData> currentNetworkWsDataList = new ArrayList<>();
        List<EyDmsCustomerModel> customerFilteredList=new ArrayList<>();
        RequestCustomerData requestData = new RequestCustomerData();
        List<String> counterType = List.of(leadType);
        requestData.setCounterType(counterType);
        List<EyDmsCustomerModel> eydmsCustomerFilteredList = territoryManagementService.getCustomerforUser(requestData);
        if(eydmsCustomerFilteredList!=null && !eydmsCustomerFilteredList.isEmpty()) {
          customerFilteredList = salesPerformanceDao.getEyDmsCustomerLastLiftingList(eydmsCustomerFilteredList);
        }
        for (EyDmsCustomerModel customerModel : customerFilteredList) {
            SalesPerformNetworkDetailsData dealerCurrentNetworkData = new SalesPerformNetworkDetailsData();
            dealerCurrentNetworkData.setCode(customerModel.getUid());
            dealerCurrentNetworkData.setMobileNumber(customerModel.getMobileNumber());
            dealerCurrentNetworkData.setName(customerModel.getName());
            dealerCurrentNetworkData.setPotential(Objects.nonNull(customerModel.getCounterPotential()) ? String.valueOf(customerModel.getCounterPotential()) : ZERO);

            //countershare and Sales quantity for dealer
            dealerCurrentNetworkData.setCounterSharePercentage(getCounterShareForDealer(customerModel,  doList, subAreaList));
            dealerCurrentNetworkData.setSalesQuantity(setSalesQuantityForCustomerForBot(customerModel, leadType,  doList, subAreaList));

            currentNetworkWsDataList.add(dealerCurrentNetworkData);
        }
        if(currentNetworkWsDataList!=null && !currentNetworkWsDataList.isEmpty()){
            LOGGER.info(String.format("Size of List Before Limit:%s", currentNetworkWsDataList.size()));
            for (SalesPerformNetworkDetailsData detailsData : currentNetworkWsDataList) {
                LOGGER.info(String.format("Sales Qty:%s",detailsData.getSalesQuantity().getActual()));
            }
        }
        dealerCurrentNetworkListData.setNetworkDetails(currentNetworkWsDataList);
        List<SalesPerformNetworkDetailsData> collect = currentNetworkWsDataList.stream().filter(nw -> nw.getSalesQuantity().getActual() != 0 || nw.getSalesQuantity().getActual() != 0.0).collect(Collectors.toList());
        if(collect.size()>0){
            LOGGER.info(String.format("Size of List Before Limit:%s", collect.size()));
        }
        List<SalesPerformNetworkDetailsData> data = collect.stream().sorted(Comparator.comparing(nw -> nw.getSalesQuantity().getActual())).collect(Collectors.toList());
        if(userService.getCurrentUser().getGroups().contains((userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.SP_GROUP_ID)))) {
            dealerCurrentNetworkListData.setNetworkDetails(data.stream().limit(5).collect(Collectors.toList()));
        }else{
            dealerCurrentNetworkListData.setNetworkDetails(data.stream().limit(3).collect(Collectors.toList()));
        }
        if(dealerCurrentNetworkListData.getNetworkDetails() !=null && !dealerCurrentNetworkListData.getNetworkDetails().isEmpty()){
            LOGGER.info(String.format("Size of Bottom List:%s", dealerCurrentNetworkListData.getNetworkDetails().size()));
        }
        return dealerCurrentNetworkListData;
    }

    @Override
    public SalesPerformamceCockpitSaleData getSalesHistoryForDealers(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite) {

        CustomerCategory category = CustomerCategory.TR;

        SalesPerformamceCockpitSaleData data = new SalesPerformamceCockpitSaleData();
        LocalDate date = LocalDate.now();
        LocalDate lastMonth = date.minusMonths(1);
        LocalDate lastTolastMonth = date.minusMonths(2);
        LocalDate lastYearSameMonth = date.minusYears(1);
        double lastMonthSales = 0.0;
        double secondLastMonthSales = 0.0;
        double lastYearCurrentMonthSale = 0.0;
        if (StringUtils.isBlank(subArea)) {
            lastMonthSales = salesPerformanceDao.getSalesHistoryDataForDealer(lastMonth.getMonthValue(), lastMonth.getYear(), category, currentBaseSite);
            //secondLastMonthSales = salesPerformanceDao.getSalesHistoryDataForDealer(lastTolastMonth.getMonthValue(), lastTolastMonth.getYear(), category, currentBaseSite);

            String lastMonthSalesFormat = format(lastMonthSales);

            //data.setLastMonthSales(amountFormatService.rupeeFormat(String.valueOf(lastMonthSales)));
            lastYearCurrentMonthSale = salesPerformanceDao.getSalesHistoryDataForDealer(lastYearSameMonth.getMonthValue(), lastYearSameMonth.getYear(), category, currentBaseSite);
            String lastYearCurrentMonthSaleFormat = format(lastYearCurrentMonthSale);
            //data.setLastYearCurrentMonthSales(Double.valueOf(amountFormatService.rupeeFormat(String.valueOf(lastYearCurrentMonthSale))));
            data.setLastMonthSales(lastMonthSalesFormat);
            data.setLastYearCurrentMonthSales(lastYearCurrentMonthSaleFormat);
        } else {
            lastMonthSales = salesPerformanceDao.getSalesHistoryDataForDealerWithSubArea(subArea, lastMonth.getMonthValue(), lastMonth.getYear(), category, currentBaseSite);
            //secondLastMonthSales = salesPerformanceDao.getSalesHistoryDataForDealerWithSubArea(subArea, lastMonth.getMonthValue(), lastMonth.getYear(), category, currentBaseSite);
            String lastMonthSalesFormat = format(lastMonthSales);
            data.setLastMonthSales(lastMonthSalesFormat);
            lastYearCurrentMonthSale = salesPerformanceDao.getSalesHistoryDataForDealerWithSubArea(subArea, lastYearSameMonth.getMonthValue(), lastYearSameMonth.getYear(), category, currentBaseSite);
            String lastYearCurrentMonthSaleFormat = format(lastYearCurrentMonthSale);
            data.setLastYearCurrentMonthSales(lastYearCurrentMonthSaleFormat);
        }
        return data;
    }

    @Override
    public Double getNCRThreshold(String state, BaseSiteModel site, String yearMonth) {
        Double ncrThreshold = salesPerformanceDao.getNCRThreshold(state, site, yearMonth);
        return ncrThreshold;
    }

    public List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForYTD(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, EyDmsCustomerModel customer, List<String> doList, List<String> subAreaList) {
        List<Date> dates = getCurrentFinancialYear();
        Date startDate = dates.get(0);
        Date endDate = dates.get(1);
        return salesPerformanceDao.getProductwiseSalesPercentRatioAndVolumeRatioForYTD(null, baseSite, startDate, endDate, customer, doList, subAreaList);
    }

    @Override
    public List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForMonthPicker(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, int month, int year, List<String> doList, List<String> subAreaList) {
        return salesPerformanceDao.getProductwiseSalesPercentRatioAndVolumeRatioForMonthPicker(eydmsUser, baseSite, month, year, doList, subAreaList);
    }

    @Override
    public List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForCustomerMonthPicker(EyDmsCustomerModel eydmsUser, BaseSiteModel baseSite, int month, int year) {
        return salesPerformanceDao.getProductwiseSalesPercentRatioAndVolumeRatioForCustomerMonthPicker(eydmsUser, baseSite, month, year);
    }

    private List<SalesPerformNetworkDetailsData> getDealerNetworkWithRank(LinkedHashMap<Double, List<SalesPerformNetworkDetailsData>> currentNetworkMap) {
        LOGGER.info(String.format("Creating Dealer Data with Rank"));
        int count = 1;
        Map<Double, List<SalesPerformNetworkDetailsData>> reverseSortedMap = new TreeMap<Double, List<SalesPerformNetworkDetailsData>>(Collections.reverseOrder());
        List<SalesPerformNetworkDetailsData> dealerNetworkDataList = new ArrayList<>();
        reverseSortedMap.putAll(currentNetworkMap);
        for (Map.Entry<Double, List<SalesPerformNetworkDetailsData>> entry : reverseSortedMap.entrySet()) {
            List<SalesPerformNetworkDetailsData> dealerNetworkData = entry.getValue();
            for (SalesPerformNetworkDetailsData dealer : dealerNetworkData) {
                LOGGER.info(String.format("DealerNetworkData code :: %s saleQuantity :: %s ", dealer.getCode(), dealer.getSalesQuantity().getActual()));
                dealer.setRank(String.valueOf(count));
                dealerNetworkDataList.add(dealer);
            }
            count++;
        }
        return dealerNetworkDataList;
    }


    private int getNumberOfDays(List<OrderModel> orderList) {
        int numberOfDays;
        Date currentDate = new Date(System.currentTimeMillis());
        Date lastOrderDate = orderList.get(0).getCreationtime();
        LocalDateTime current = LocalDateTime.ofInstant(currentDate.toInstant(), ZoneId.systemDefault());
        LocalDateTime last = LocalDateTime.ofInstant(lastOrderDate.toInstant(), ZoneId.systemDefault());
        LOGGER.info(String.format("Getting Number Of Days last :: %s current :: %s ", last, current));
        numberOfDays = (int) ChronoUnit.DAYS.between(last, current);
        return numberOfDays;
    }


    private double getlastMonthSaleQty(String customerCode,  List<String> doList, List<String> subAreaList) {
       /* LocalDate currentMonthdate = LocalDate.now();
        LocalDate lastMonthdate = LocalDate.now().minusMonths(1);
        //LocalDate endDate = LocalDate.of(currentMonthdate.getYear(), currentMonthdate.getMonth(), 1).minusDays(1);//2023-05-31
        //LocalDate endDate = LocalDate.of(currentMonthdate.getYear(), currentMonthdate.getMonth(), 1);//2023-05-31
        LocalDate startDate = LocalDate.of(lastMonthdate.getYear(), lastMonthdate.getMonth(), 1);//01-05-2023*/

        LocalDate date = LocalDate.now().minusMonths(1);
        LocalDate startDate = LocalDate.of(date.getYear(), date.getMonth(), 1);
        LocalDate endDate = LocalDate.of(date.getYear(), date.getMonth(), date.lengthOfMonth()).plusDays(1);
        LOGGER.info(String.format("Getting LastMonthSaleQuantity for customerCode :: %s startDate :: endDate :: %s ", customerCode, startDate, endDate));
        double lastMonthSales = eydmsUserDao.getSalesQuantityForSalesPerformance(customerCode, getDate(startDate), getDate(endDate),  doList, subAreaList);
        return lastMonthSales;
    }

    /*  private double getCurrentMonthSaleQty(String customerCode, BaseSiteModel site){
          LocalDate date=LocalDate.now();
          LocalDate startDate = LocalDate.of(date.getYear(),date.getMonth(),1);
          LocalDate endDate = date;
          LOGGER.info(String.format("Getting CurrentMonthSaleQuantity for customerCode :: %s startDate :: endDate :: %s ",customerCode,startDate,endDate));
          double currentMonthSales = eydmsUserDao.getSalesQuantityForSalesPerformance(customerCode, getDate(startDate), getDate(endDate), site);
          return currentMonthSales;
      }*/
   /* private double getlastMonthSaleQty(String customerCode, BaseSiteModel site){
        LocalDate currentMonthdate=LocalDate.now();
        LocalDate lastMonthdate=LocalDate.now().minusMonths(1);
        LocalDate startDate = LocalDate.of(lastMonthdate.getYear(),lastMonthdate.getMonth(),1);
        LocalDate endDate = LocalDate.of(currentMonthdate.getYear(),currentMonthdate.getMonth(),1).minusDays(1);
        LOGGER.info(String.format("Getting LastMonthSaleQuantity for customerCode :: %s startDate :: endDate :: %s ",customerCode,startDate,endDate));
        double lastMonthSales = eydmsUserDao.getSalesQuantityForBottomLogging(customerCode, getDate(startDate), getDate(endDate), site);
        // double lastMonthSales = eydmsUserDao.getSalesQuantity(customerCode, getDate(startDate), getDate(endDate), site);
        return lastMonthSales;
    }
*/
    private double getCurrentMonthSaleQty(String customerCode, List<String> doList, List<String> subAreaList) {
        LocalDate date = LocalDate.now();
        LocalDate startDate = LocalDate.of(date.getYear(), date.getMonth(), 1);
        LocalDate endDate = LocalDate.of(date.getYear(), date.getMonth(), date.lengthOfMonth()).plusDays(1);
        LOGGER.info(String.format("Getting CurrentMonthSaleQuantity for customerCode :: %s startDate :: endDate :: %s ", customerCode, startDate, endDate));
        double currentMonthSales = eydmsUserDao.getSalesQuantityForBottomLogging(customerCode, getDate(startDate), getDate(endDate),  doList, subAreaList);
        return currentMonthSales;
    }

    private double getlastMonthSaleQty(String customerCode,  int month, int year, List<String> doList, List<String> subAreaList) {
        LocalDate startDate = LocalDate.of(year, month, 1).minusMonths(1);
        LocalDate endDate = LocalDate.of(year, month, 1).minusDays(1);
        LOGGER.info(String.format("Getting LastMonthSaleQuantity for customerCode :: %s startDate :: endDate :: %s ", customerCode, startDate, endDate));
        double lastMonthSales = eydmsUserDao.getSalesQuantityForSalesPerformance(customerCode, getDate(startDate), getDate(endDate),  doList, subAreaList);
        return lastMonthSales;
    }

    private double getCurrentMonthSaleQty(String customerCode,  int month, int year, List<String> doList, List<String> subAreaList) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1);
        LOGGER.info(String.format("Getting CurrentMonthSaleQuantity for customerCode :: %s startDate :: endDate :: %s ", customerCode, startDate, endDate));
        double currentMonthSales = eydmsUserDao.getSalesQuantityForSalesPerformance(customerCode, getDate(startDate), getDate(endDate),  doList, subAreaList);
        //double currentMonthSales = eydmsUserDao.getSalesQuantity(customerCode, getDate(startDate), getDate(endDate), site);
        return currentMonthSales;
    }

    private double getlastYearSaleQty(String customerCode, BaseSiteModel site, List<String> doList, List<String> subAreaList) {
        List<Date> date = findFiscalYearforYTDLY();
        Date d1 = date.get(0);
        Date d2 = date.get(1);
        LocalDate startDateLY = d1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDateLY = d2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        LOGGER.info(String.format("Getting LastMonthSaleQuantity for customerCode :: %s startDate :: endDate :: %s ", customerCode, startDateLY, endDateLY));
        double lastMonthSales = eydmsUserDao.getSalesQuantityForSalesPerformance(customerCode, getDate(startDateLY), getDate(endDateLY),  doList, subAreaList);
        return lastMonthSales;
    }

    private double getCurrentYearSaleQty(String customerCode, BaseSiteModel site, List<String> doList, List<String> subAreaList) {
        List<Date> dates = findFiscalYearforYTDCY();
        Date dd1 = dates.get(0);
        Date dd2 = dates.get(1);
        LocalDate startDateCY = dd1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDateCY = dd2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LOGGER.info(String.format("Getting CurrentMonthSaleQuantity for customerCode :: %s startDate :: endDate :: %s ", customerCode, startDateCY, endDateCY));
        double currentMonthSales = eydmsUserDao.getSalesQuantityForSalesPerformance(customerCode, getDate(startDateCY), getDate(endDateCY),  doList, subAreaList);
        //double currentMonthSales = eydmsUserDao.getSalesQuantity(customerCode, getDate(startDate), getDate(endDate), site);
        return currentMonthSales;
    }

    private String getDate(LocalDate localDate) {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime dateTime = localDate.atStartOfDay(zone);
        String date = formatter.format(Date.from(dateTime.toInstant()));
        return date;
    }

    private Double getYearToYearGrowth(String customerCode, BaseSiteModel site) {

        LocalDate lastYearCurrentDate = LocalDate.now().minusYears(1);
        LocalDate currentYearCurrentDate = LocalDate.now();
        LocalDate currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear(), Month.APRIL, 1);
        LocalDate lastFinancialYearDate = LocalDate.of(lastYearCurrentDate.getYear(), Month.APRIL, 1);

        LOGGER.info(String.format("Getting yoy growth for customerNo :: %s currentYearCurrentDate :: %s currentFinancialYearDate :: %s lastFinancialYearDate :: %s", customerCode, currentYearCurrentDate, currentFinancialYearDate, lastFinancialYearDate));
        double lastSale = eydmsUserDao.getSalesQuantity(customerCode, getDate(lastYearCurrentDate), getDate(lastFinancialYearDate));
        double currentSale = eydmsUserDao.getSalesQuantity(customerCode, getDate(currentYearCurrentDate), getDate(currentFinancialYearDate));
        LOGGER.info(String.format("For CustomerNo :: %s lastSale :: %s currentSale :: %s", customerCode, lastSale, currentSale));
        double growthValue = (float) (((lastSale - currentSale) / lastSale) * 100);
        return growthValue;
    }

    private Double getYearToYearGrowthForMonth(String customerCode, BaseSiteModel site) {

        LocalDate lastMonthCurrentDate = LocalDate.now().minusMonths(1);
        LocalDate currentMonthCurrentDate = LocalDate.now();
        LocalDate currentFinancialMonthDate = LocalDate.of(currentMonthCurrentDate.getYear(), currentMonthCurrentDate.getMonth(), 1);
        LocalDate lastFinancialMonthDate = LocalDate.of(lastMonthCurrentDate.getYear(), lastMonthCurrentDate.getMonth(), 1);

        LOGGER.info(String.format("Getting yoy growth for customerNo :: %s currentYearCurrentDate :: %s currentFinancialYearDate :: %s lastFinancialYearDate :: %s", customerCode, currentMonthCurrentDate, currentFinancialMonthDate, lastMonthCurrentDate));
        double lastSale = eydmsUserDao.getSalesQuantity(customerCode, getDate(lastMonthCurrentDate), getDate(currentFinancialMonthDate));
        double currentSale = eydmsUserDao.getSalesQuantity(customerCode, getDate(currentMonthCurrentDate), getDate(lastFinancialMonthDate));
        LOGGER.info(String.format("For CustomerNo :: %s lastSale :: %s currentSale :: %s", customerCode, lastSale, currentSale));
        double growthValue = (float) (((lastSale - currentSale) / lastSale) * 100);
        return growthValue;
    }

    @Override
    public Double getSalesByDeliveryDate(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite, Date startDate, Date endDate) {
        return getSalesPerformanceDao().getActualTargetForSalesYTD(subArea, eydmsUser, currentBaseSite, startDate, endDate);
    }

    @Override
    public Double getSalesByDeliveryDate(EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite, Date startDate, Date endDate, List<String> doList, List<String> subAreaList) {
        return getSalesPerformanceDao().getActualTargetForSalesYTD(eydmsUser, currentBaseSite, startDate, endDate, doList, subAreaList);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Double getSalesLeaderByDeliveryDate(EyDmsCustomerModel eydmsUser, BaseSiteModel currentBaseSite, Date startDate, Date endDate) {
        return (Double) sessionService.executeInLocalView(new SessionExecutionBody()
        {
            @Override
            public Double execute()
            {
                try {
                    searchRestrictionService.disableSearchRestrictions();
                    return getSalesPerformanceDao().getActualTargetForSalesLeaderYTD(eydmsUser, currentBaseSite, startDate, endDate);
                }
                finally {
                    searchRestrictionService.enableSearchRestrictions();
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<List<Object>> getSalesLeaderByDate(DistrictMasterModel district,BaseSiteModel currentBaseSite, Date startDate, Date endDate) {
        return (List<List<Object>>) sessionService.executeInLocalView(new SessionExecutionBody()
        {
            @Override
            public List<List<Object>> execute()
            {
                try {
                    searchRestrictionService.disableSearchRestrictions();
                    return getSalesPerformanceDao().getActualTargetForSalesLeader(district,currentBaseSite, startDate, endDate);
                }
                finally {
                    searchRestrictionService.enableSearchRestrictions();
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<List<Object>> getSalesLeaderByDateRetailer(SubAreaMasterModel district, BaseSiteModel currentBaseSite, Date startDate, Date endDate) {
        return (List<List<Object>>) sessionService.executeInLocalView(new SessionExecutionBody()
        {
            @Override
            public List<List<Object>> execute()
            {
                try {
                    searchRestrictionService.disableSearchRestrictions();
                    LOGGER.info("Disable search restriction");
                    return getSalesPerformanceDao().getActualTargetForSalesLeaderRetailer(district,currentBaseSite, startDate, endDate);
                }
                finally {
                    LOGGER.info("Enable search restriction");
                    searchRestrictionService.enableSearchRestrictions();
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<List<Object>> getPremiumSalesLeaderByDate(DistrictMasterModel district, BaseSiteModel currentBaseSite, Date startDate, Date endDate) {
        return (List<List<Object>>) sessionService.executeInLocalView(new SessionExecutionBody()
        {
            @Override
            public List<List<Object>> execute()
            {
                try {
                    searchRestrictionService.disableSearchRestrictions();
                    LOGGER.info("Disable search restriction");
                    return getSalesPerformanceDao().getPremiumForSalesLeader(district, currentBaseSite, startDate, endDate);
                }
                finally {
                    LOGGER.info("Enable search restriction");
                    searchRestrictionService.enableSearchRestrictions();
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<List<Object>> getPremiumSalesLeaderByDateRetailer(SubAreaMasterModel district, BaseSiteModel currentBaseSite, Date startDate, Date endDate) {
        return (List<List<Object>>) sessionService.executeInLocalView(new SessionExecutionBody()
        {
            @Override
            public List<List<Object>> execute()
            {
                try {
                    searchRestrictionService.disableSearchRestrictions();
                    return getSalesPerformanceDao().getPremiumForSalesLeaderRetailer(district, currentBaseSite, startDate, endDate);
                }
                finally {
                    searchRestrictionService.enableSearchRestrictions();
                }
            }
        });
    }

    @Override
    public List<List<Object>> getSalesHistoryModelList(String state, Date startDate, Date endDate, BaseSiteModel baseSiteForUID) {
        List<List<Object>> salesHistoryModelList = salesPerformanceDao.getSalesHistoryModelList(state, startDate, endDate, baseSiteForUID);
        return salesHistoryModelList;
    }

    @Override
    public List<List<Object>> getPartnerDetailsForSales(String searchKeyWord) {
        List<List<Object>> partnerDetailsForSales = salesPerformanceDao.getPartnerDetailsForSales(searchKeyWord);
        return partnerDetailsForSales;
    }

    @Override
    public double getSecondaryLeadDistanceForMonth(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, Integer year1, Integer month1,List<String> doList, List<String> subAreaList) {
        final UserModel user = getUserService().getCurrentUser();
        double distance = 0.0;
        List<List<Object>> list = salesPerformanceDao.getSecondaryLeadDistanceForMonth(eydmsUser,WarehouseType.DEPOT, baseSite, year1, month1,doList,subAreaList);
        int count = 0 ;
        double secdistance=0.0;
        if (list != null && !list.isEmpty()) {
            for (List<Object> objects : list) {
                if(objects.get(0)!=null) {
                    count = (Integer) objects.get(0);
                }
                if(objects.get(1)!=null ) {
                    distance = (Double) objects.get(1);
                }
            }
        }
        if(distance!=0 && count!=0) {
            secdistance = distance / count;
        }
        if(secdistance!=0.0)
        {
            return secdistance;
        }
        else
            return 0.0;

    }

    @Override
    public double getSecondaryLeadDistance(EyDmsUserModel eydmsUser, BaseSiteModel baseSite,List<String> doList, List<String> subAreaList) {
        final UserModel user = getUserService().getCurrentUser();
        double distance = 0.0;
        double sum = 0.0;
        List<List<Object>> list = salesPerformanceDao.getSecondaryLeadDistance(eydmsUser,WarehouseType.DEPOT, baseSite,doList,subAreaList);
        int count = 0 ;
        double secdistance=0.0;
        if (list != null && !list.isEmpty()) {
            for (List<Object> objects : list) {
                if(objects.get(0)!=null) {
                    count = (Integer) objects.get(0);
                }
                if(objects.get(1)!=null ) {
                    distance = (Double) objects.get(1);
                }
            }
        }
        if(distance!=0 && count!=0) {
            secdistance = distance / count;
        }
        if(secdistance!=0.0)
        {
            return secdistance;
        }
        else
            return 0.0;
    }

    @Override
    public double getSecondaryLeadDistanceMTD(EyDmsUserModel eydmsUser, BaseSiteModel baseSite,List<String> doList, List<String> subAreaList) {
        final UserModel user = getUserService().getCurrentUser();
        double distance = 0.0;
        double sum = 0.0;
        List<List<Object>> list = salesPerformanceDao.getSecondaryLeadDistanceMTD(eydmsUser,WarehouseType.DEPOT, baseSite,doList,subAreaList);
        int count = 0 ;
        double secdistance=0.0;
        if (list != null && !list.isEmpty()) {
            for (List<Object> objects : list) {
                if(objects.get(0)!=null) {
                    count = (Integer) objects.get(0);
                }
                if(objects.get(1)!=null ) {
                    distance = (Double) objects.get(1);
                }
            }
        }
        if(distance!=0 && count!=0) {
            secdistance = distance / count;
        }
        if(secdistance!=0.0)
        {
            return secdistance;
        }
        else
            return 0.0;
    }

    @Override
    public double getSecondaryLeadDistanceYTD(EyDmsUserModel eydmsUser, BaseSiteModel baseSite,List<String> doList, List<String> subAreaList) {
        final UserModel user = getUserService().getCurrentUser();
        double distance = 0.0;
        double sum = 0.0;
        List<Date> dates = getCurrentFinancialYear();
        Date startDate = null;
        Date endDate = null;
        startDate = dates.get(0);
        endDate = dates.get(1);
        List<List<Object>> list = salesPerformanceDao.getSecondaryLeadDistanceYTD(eydmsUser, WarehouseType.DEPOT,baseSite, startDate, endDate,doList,subAreaList);
        int count = 0 ;
        double secdistance=0.0;
        if (list != null && !list.isEmpty()) {
            for (List<Object> objects : list) {
               if(objects.get(0)!=null) {
                   count = (Integer) objects.get(0);
               }
                   if(objects.get(1)!=null ) {
                       distance = (Double) objects.get(1);
                   }
             }
         }
        if(distance!=0 && count!=0) {
            secdistance = distance / count;
        }
        if(secdistance!=0.0)
        {
            return secdistance;
        }
        else
            return 0.0;
    }

    @Override
    public DealerRevisedMonthlySalesModel getMonthlySaleTargetGraphForDealer(String customer, String month, String year) {
        return salesPerformanceDao.getMonthlySaleTargetGraphForDealer(customer, month, year);
    }

    @Override
    public List<DealerRevisedMonthlySalesModel> getMonthlySaleTargetGraphForSP(List<EyDmsCustomerModel> eydmsCustomer, String month, String year) {
        return salesPerformanceDao.getMonthlySaleTargetGraphForSP(eydmsCustomer, month, year);
    }

    @Override
    public DealerRevisedMonthlySalesModel getMonthlySaleTargetGraph(EyDmsUserModel user, String monthName, String yearName, List<String> doList, List<String> subAreaList) {
        return salesPerformanceDao.getMonthlySaleTargetGraph(user, monthName, yearName, doList, subAreaList);
    }

    @Override
    public double getActualSaleForDealerGraphYTD(EyDmsCustomerModel customer, Date startDate, Date endDate, BaseSiteModel currentBaseSite, String bgpFilter) {
        return salesPerformanceDao.getActualSaleForDealerGraphYTD(customer, startDate, endDate, currentBaseSite, bgpFilter);
    }

    @Override
    public double getActualSaleForDealerGraphYTDSP(List<EyDmsCustomerModel> eydmsCustomer, Date startDate, Date endDate, BaseSiteModel currentBaseSite) {
        return salesPerformanceDao.getActualSaleForDealerGraphYTDSP(eydmsCustomer, startDate, endDate, currentBaseSite);
    }

    @Override
    public double getActualSaleForGraphYTD(EyDmsUserModel userModel, Date startDate, Date endDate, BaseSiteModel currentBaseSite, List<String> doList, List<String> subAreaList) {
        return salesPerformanceDao.getActualSaleForGraphYTD(userModel, startDate, endDate, currentBaseSite, doList, subAreaList);
    }

    @Override
    public double getActualTargetFor10DayBucketForDealer(EyDmsCustomerModel eydmsCustomer, String bgpFilter, Date startDate1, Date endDate1) {
        LOGGER.info("10DayBucket startDate1: "+startDate1 + "and" + "endDate1:" + endDate1);
        return salesPerformanceDao.getActualTargetFor10DayBucketForDealer(eydmsCustomer, bgpFilter, startDate1, endDate1);
    }

    @Override
    public double getActualTargetFor10DayBucketForSP(List<EyDmsCustomerModel> eydmsCustomer, Date startDate1, Date endDate1) {
        return salesPerformanceDao.getActualTargetFor10DayBucketForSP(eydmsCustomer, startDate1, endDate1);
    }

    @Override
    public double getActualTargetFor10DayBucket(EyDmsUserModel eydmsUser, Date startDate1, Date endDate1, List<String> doList, List<String> subAreaList) {
        return salesPerformanceDao.getActualTargetFor10DayBucket(eydmsUser, startDate1, endDate1, doList, subAreaList);
    }

    @Override
    public Map<String, Object> getDirectDispatchOrdersMTDPercentage(int month, int year, List<String> doList, List<String> subAreaList) {

        Map<String, Object> map = new HashMap<>();
        final UserModel currentUser = getUserService().getCurrentUser();
        Integer directDispatchOrdersMTD = salesPerformanceDao.findDirectDispatchOrdersMTDCount(currentUser, WarehouseType.PLANT, month, year, doList,subAreaList);
        Integer secondaryDispatchOrdersMTD = salesPerformanceDao.findDirectDispatchOrdersMTDCount(currentUser, WarehouseType.DEPOT, month, year, doList,subAreaList);
        if (directDispatchOrdersMTD <= 0 && secondaryDispatchOrdersMTD <= 0) {
            throw new IllegalArgumentException("directDispatchOrdersMTD and secondaryDispatchOrdersMTD must be a positive non-zero value");
        } else {
            map.put("directDispatch", ((double) directDispatchOrdersMTD / ((double) directDispatchOrdersMTD + (double) secondaryDispatchOrdersMTD)) * 100);
            map.put("secondaryDispatch", ((double) secondaryDispatchOrdersMTD / ((double) directDispatchOrdersMTD + (double) secondaryDispatchOrdersMTD)) * 100);
            return map;
        }
    }

    @Override
    public Double getSalesLeaderByDeliveryDateRetailer(EyDmsCustomerModel eydmsUser, BaseSiteModel currentBaseSite, Date startDate, Date endDate) {
        return getSalesPerformanceDao().getActualTargetForSalesLeaderYTDRetailer(eydmsUser, currentBaseSite, startDate, endDate);
    }

    public SalesPerformanceDao getSalesPerformanceDao() {
        return salesPerformanceDao;
    }

    public void setSalesPerformanceDao(SalesPerformanceDao salesPerformanceDao) {
        this.salesPerformanceDao = salesPerformanceDao;
    }

    @Override
    public List<Date> getCurrentFinancialYear() {
        List<Date> dates = new ArrayList<>();
        Date startDate1 = null, endDate1 = null;
        LocalDate financialYearStart=null;
        LocalDate currentDate = LocalDate.now();
        int currentYear = currentDate.getYear();
        if (currentDate.getMonthValue() < Month.APRIL.getValue()) {
            financialYearStart= LocalDate.of(currentYear - 1, Month.APRIL, 1);
        } else {
            financialYearStart = LocalDate.of(currentYear, Month.APRIL, 1);
        }
        LocalDate financialYearEnd = financialYearStart.plusYears(1);
        startDate1 =Date.from(financialYearStart.atStartOfDay(ZoneId.systemDefault()).toInstant());
        endDate1 = Date.from(financialYearEnd.atStartOfDay(ZoneId.systemDefault()).toInstant());
        dates.add(startDate1);
        dates.add(endDate1);
        return dates;
    }

    public List<Date> getCurrentFinancialYearSales() {

        List<Date> dates = new ArrayList<>();
        Date startDate1 = null, endDate1 = null;

       /* LocalDate currentDate = LocalDate.now();
        LocalDate startDate;
        startDate = LocalDate.of(currentDate.getYear(), Month.APRIL, 1);//2023,04,01
        //endDate = LocalDate.of(currentDate.getYear() + 1, Month.APRIL, 1);//2024-04-01

        LocalDate endDate = LocalDate.of(currentDate.getYear(), Month.APRIL, 1);
        if(currentDate.getMonth().compareTo(Month.APRIL)<0) {
            endDate = LocalDate.of(currentDate.getYear()-1, Month.APRIL, 1);
        }
        startDate1 = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        endDate1 = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());*/
        LocalDate currentYearCurrentDate= LocalDate.now();
        LocalDate currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear(), Month.APRIL, 1);
        if(currentYearCurrentDate.getMonth().compareTo(Month.APRIL)<0) {
            currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear()-1, Month.APRIL, 1);
        }
        startDate1 = Date.from(currentYearCurrentDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        endDate1 = Date.from(currentFinancialYearDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        dates.add(endDate1);
        dates.add(startDate1);
        return dates;
    }


    public List<Date> getPreviousFinancialYear() {
        List<Date> dates = new ArrayList<>();
        Date startDate, endDate;
        Year starYear, endYear;
        LocalDate currentDate = LocalDate.now();
        Month currentMonth = currentDate.getMonth();
        Year currentYear = Year.of(currentDate.getYear());
        Calendar c1 = Calendar.getInstance();
        if (currentMonth.getValue() <= 4) {
            starYear = Year.of(currentYear.getValue() - 2);
            c1.set(starYear.getValue(), 03, 01);
            startDate = c1.getTime();
            endYear = Year.of(currentYear.getValue() - 1);
            c1.set(endYear.getValue(), 02, 31);
            endDate = c1.getTime();
            dates.add(startDate);
            dates.add(endDate);
        } else {
            starYear = currentYear;
            c1.set(starYear.getValue() - 1, 03, 01);
            startDate = c1.getTime();
            endYear = Year.of(currentYear.getValue());
            c1.set(endYear.getValue(), 02, 31);
            endDate = c1.getTime();
            dates.add(startDate);
            dates.add(endDate);
        }
        return dates;
    }

    public List<Date> getPrevFinancialYear() {

        List<Date> dates = new ArrayList<>();
        Date startDate1 = null, endDate1 = null;

        LocalDate currentDate = LocalDate.now().minusYears(1);
        LocalDate startDate, endDate;
        startDate = LocalDate.of(currentDate.getYear(), Month.APRIL, 1);
        endDate = LocalDate.of(currentDate.getYear() + 1, Month.APRIL, 1);
        startDate1 = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        endDate1 = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        dates.add(startDate1);
        dates.add(endDate1);
        return dates;
    }

    String findNextFinancialYear() {
        LocalDate date = LocalDate.now();
        int currentYear = date.getYear();
        int fyYear = currentYear + 1;
        StringBuilder f = new StringBuilder();
        return String.valueOf(f.append(String.valueOf(fyYear)).append("-").append(String.valueOf(currentYear)));
    }

    String findNextFinancialYearForActualTargetSales() {
        LocalDate date = LocalDate.now();
        int currentYear = date.getYear();
        int fyYear = currentYear + 1;
        StringBuilder f = new StringBuilder();
        return String.valueOf(f.append(String.valueOf(currentYear)).append("-").append(String.valueOf(fyYear)));
    }

    String findPreviousFinancialYear() {
        LocalDate date = LocalDate.now();
        int currentYear = date.getYear();
        int fyYear = currentYear - 2;
        StringBuilder f = new StringBuilder();
        return String.valueOf(f.append(String.valueOf(fyYear)).append("-").append(String.valueOf(currentYear + 1)));
    }

    public static String getYear(Date date) {
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        return yearFormat.format(date);
    }

    public static String getMonth(Date date) {
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
        return monthFormat.format(date);
    }

    @Override
    public List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForCustomer(EyDmsCustomerModel customer,
                                                                                       BaseSiteModel baseSite, String customerType, List<String> doList, List<String> subAreaList) {
        return getSalesPerformanceDao().getProductwiseSalesPercentRatioAndVolumeRatioForCustomer(customer, baseSite, customerType, doList, subAreaList);
    }

    @Override
    public List<List<Object>> getBrandwiseSalesPercentRatioAndVolumeRatioForCustomer(EyDmsCustomerModel customer) {
        return getSalesPerformanceDao().getBrandwiseSalesPercentRatioAndVolumeRatioForCustomer(customer);
    }

    @Override
    public List<List<Object>> getProductMixPercentRatioAndVolumeRatioWithPoints(EyDmsCustomerModel customer,String filter) {
        List<Date> dates = getCurrentFinancialYear();
        Date startDate = dates.get(0);
        Date endDate = dates.get(1);
        return getSalesPerformanceDao().getProductMixPercentRatioAndVolumeRatioWithPoints(customer,filter,startDate,endDate);
    }


    @Override
    public Double getActualTargetSalesForSelectedMonthAndYear(String subArea, EyDmsUserModel eydmsUser, BaseSiteModel baseSite, int month, int year) {
        return getSalesPerformanceDao().getActualTargetSalesForSelectedMonthAndYear(subArea, eydmsUser, baseSite, month, year);
    }

    public static Date getFirstDateOfMonth(int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH,
                calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        return calendar.getTime();
    }

    public static Date getLastDateOfMonth(int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return calendar.getTime();
    }

    List<Date> findFiscalYearforYTDLY() {
        List<Date> dates = new ArrayList<>();
        Date startDate, endDate;
        Year starYear, endYear;
        LocalDate currentDate = LocalDate.now();
        Month currentMonth = currentDate.getMonth();
        Year currentYear = Year.of(currentDate.getYear() - 1);
        Calendar c1 = Calendar.getInstance();
        if (currentMonth.getValue() < 4) {
            starYear = Year.of(currentYear.getValue() - 2);
            c1.set(starYear.getValue(), 03, 01);
            startDate = c1.getTime();
            endYear = Year.of(currentYear.getValue());
            c1.set(endYear.getValue(), 02, 31);
            endDate = c1.getTime();
            dates.add(startDate);
            dates.add(endDate);
        } else {
            starYear = currentYear;
            c1.set(starYear.getValue(), 03, 01);
            startDate = c1.getTime();
            endYear = Year.of(currentYear.getValue() + 1);
            c1.set(endYear.getValue(), 02, 31);
            endDate = c1.getTime();
            dates.add(startDate);
            dates.add(endDate);
        }
        return dates;
    }

    List<Date> findFiscalYearforYTDCY() {
        List<Date> dates = new ArrayList<>();
        Date startDate, endDate;
        Year starYear, endYear;
        LocalDate currentDate = LocalDate.now();
        Month currentMonth = currentDate.getMonth();
        Year currentYear = Year.of(currentDate.getYear() - 1);
        Calendar c1 = Calendar.getInstance();
        if (currentMonth.getValue() < 4) {
            starYear = Year.of(currentYear.getValue() - 1);
            c1.set(starYear.getValue(), 03, 01);
            startDate = c1.getTime();
            endYear = Year.of(currentYear.getValue());
            c1.set(endYear.getValue(), 02, 31);
            endDate = c1.getTime();
            dates.add(startDate);
            dates.add(endDate);
        } else {
            starYear = currentYear;
            c1.set(starYear.getValue(), 03, 01);
            startDate = c1.getTime();
            endYear = Year.of(currentYear.getValue() + 1);
            c1.set(endYear.getValue(), 02, 31);
            endDate = c1.getTime();
            dates.add(startDate);
            dates.add(endDate);
        }
        return dates;
    }

    @Override
    public ZeroLiftingNetworkData getZeroLiftingSummaryData(int month, int year, String filter, BaseSiteModel site, String SOFilter, List<String> doList, List<String> subAreaList) {
        ZeroLiftingNetworkData data = new ZeroLiftingNetworkData();
        RequestCustomerData requestData = new RequestCustomerData();
        List<String> counterType = List.of(SOFilter);
        requestData.setCounterType(counterType);
        List<SubAreaMasterModel> soList=new ArrayList<>();
        List<DistrictMasterModel> districtList=new ArrayList<>();
        if(subAreaList!=null && !subAreaList.isEmpty()){
            for (String id : subAreaList) {
                soList.add(territoryManagementDao.getTerritoryById(id));
            }
            if(soList.get(0)!=null)
                requestData.setSubAreaMasterPk(soList.get(0).getPk().toString());
        }
        if (doList != null && !doList.isEmpty()) {
            for (String code : doList) {
                districtList.add(districtMasterDao.findByCode(code));
            }
            if(districtList.get(0)!=null)
                requestData.setDistrict(districtList.get(0).getName());
        }

        List<EyDmsCustomerModel> eydmsCustomerFilteredList = territoryManagementService.getCustomerforUser(requestData);
        LOGGER.info(String.format("Size before last lifting from eydms customer:%s",String.valueOf(eydmsCustomerFilteredList.size())));

        List<EyDmsCustomerModel> customerFilteredList=new ArrayList<>();
        if(eydmsCustomerFilteredList!=null && !eydmsCustomerFilteredList.isEmpty()) {
            customerFilteredList = salesPerformanceDao.getEyDmsCustomerZeroLiftingList(eydmsCustomerFilteredList);
        }
        LOGGER.info(String.format("Size After last lifting from eydms customer:%s",String.valueOf(customerFilteredList.size())));
        if (customerFilteredList != null && !customerFilteredList.isEmpty()) {
            data.setCount(customerFilteredList.size());
            data.setPotential(customerFilteredList.stream().filter(c->c.getCounterPotential()!=null).mapToDouble(EyDmsCustomerModel::getCounterPotential).sum());
        }
        else{
            data.setPotential(0.0);
            data.setCount(0);
        }
        return data;
}

    @Override
    public ZeroLiftingNetworkData getZeroLiftingSummaryDataDealer( String leadType) {
        ZeroLiftingNetworkData data = new ZeroLiftingNetworkData();
        List<EyDmsCustomerModel> eydmsCustomerFilteredList = new ArrayList<>();
        List<EyDmsCustomerModel> customerFilteredList = new ArrayList<>();

        EyDmsCustomerModel currentUser = (EyDmsCustomerModel) userService.getCurrentUser();
        if (currentUser != null) {
            if (currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                if (!currentUser.getCounterType().equals(CounterType.SP)) {
                    if (leadType.equalsIgnoreCase(RETAILER)) {
                        eydmsCustomerFilteredList = territoryManagementService.getRetailerListForDealer();
                    } else if (leadType.equalsIgnoreCase(INFLUENCER)) {
                        eydmsCustomerFilteredList = territoryManagementService.getInfluencerListForDealer();
                    }
                }
            }
        }

        LOGGER.info(String.format("Size before last lifting from eydms customer:%s", String.valueOf(eydmsCustomerFilteredList.size())));

        if (eydmsCustomerFilteredList != null && !eydmsCustomerFilteredList.isEmpty()) {
            customerFilteredList = salesPerformanceDao.getEyDmsCustomerZeroLiftingList(eydmsCustomerFilteredList);
        }
        LOGGER.info(String.format("Size After last lifting from eydms customer:%s", String.valueOf(customerFilteredList.size())));

        if (customerFilteredList != null && !customerFilteredList.isEmpty()) {
            data.setCount(customerFilteredList.size());
            data.setPotential(customerFilteredList.stream().filter(c -> c.getCounterPotential() != null).mapToDouble(EyDmsCustomerModel::getCounterPotential).sum());
        } else {
            data.setPotential(0.0);
            data.setCount(0);
        }
        return  data;
    }


    private Double getSalesQuantityFromPointReqMY(List<PointRequisitionModel> saleQty,int m,int y) {
        return saleQty.stream().mapToDouble(PointRequisitionModel::getQuantity).sum();
    }
    private Double getSalesQuantityFromPointReqYTD(List<PointRequisitionModel> saleQty,Date sd,Date ed) {
        return saleQty.stream().filter(o->o.getDeliveryDate().after(sd)).filter(o->o.getDeliveryDate().before(ed)).mapToDouble(PointRequisitionModel::getQuantity).sum();
    }
    private Double getSalesQuantityFromPointReq(List<EyDmsCustomerModel> saleQty) {
        Double saleQtyMonth=0.0;
        for (EyDmsCustomerModel customerModel : saleQty) {
            PointRequisitionModel eydmsCustomerFromPointReq = salesPerformanceDao.getEyDmsCustomerFromPointReq(customerModel);
            saleQtyMonth+=eydmsCustomerFromPointReq.getQuantity();
        }
        return saleQtyMonth;
    }
    private Double getSalesQuantityInfForDealer(EyDmsCustomerModel customerModel) {
        LocalDate today = LocalDate.now();
        LocalDate monthStart1 = LocalDate.of(today.getYear(), today.getMonth(), 1);
        Date monthStart = Date.from(monthStart1.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date todayDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        return salesPerformanceDao.getInfluencerFromOrderReq(customerModel,monthStart,todayDate);
    }

    private Double getSalesQuantityMTD(List<NirmanMitraSalesHistoryModel> salesHistry) {
        Date todaty = new Date();
        Calendar monthStart = Calendar.getInstance();
        monthStart.set(Calendar.DAY_OF_MONTH, 1);
        var bagLifted = salesHistry.stream().filter(sale -> sale.getTransactionDate().after(monthStart.getTime()) && sale.getTransactionDate().before(todaty)).mapToDouble(NirmanMitraSalesHistoryModel::getTransactionQuantity).sum();
        return bagLifted / 20;
    }

    private Double getSalesQuantityYTD(List<NirmanMitraSalesHistoryModel> salesHistry) {
        List<Date> dates = getCurrentFinancialYear();
        Date startDate = null;
        Date endDate = null;
        startDate = dates.get(0);
        endDate = dates.get(1);

        Date finalStartDate = startDate;
        Date finalEndDate = endDate;
        var bagLifted = salesHistry.stream().filter(sale -> sale.getTransactionDate().after(finalStartDate) && sale.getTransactionDate().before(finalEndDate)).mapToDouble(NirmanMitraSalesHistoryModel::getTransactionQuantity).sum();
        return bagLifted / 20;
    }

    private Double getSalesQuantityCurrentMonthOrderReq(List<EyDmsCustomerModel> saleQty) {
        Double saleQtyCurrentMonth = 0.0;
        if(saleQty!=null && !saleQty.isEmpty()) {
            for (EyDmsCustomerModel customerModel : saleQty) {
                OrderRequisitionModel eydmsCustomerFromOrderReq = salesPerformanceDao.getEyDmsCustomerFromOrderReq(customerModel);
                if (eydmsCustomerFromOrderReq != null) {
                    if (eydmsCustomerFromOrderReq.getQuantity() != null) {
                        saleQtyCurrentMonth += eydmsCustomerFromOrderReq.getQuantity();
                    } else {
                        saleQtyCurrentMonth = 0.0;
                    }
                }
            }
            return saleQtyCurrentMonth;
        }
        else{
            return 0.0;
        }
    }
    private Double getSalesQuantityCurrentMonthRetailer(EyDmsCustomerModel customerModel) {
          LocalDate today = LocalDate.now();
        LocalDate monthStart1 = LocalDate.of(today.getYear(), today.getMonth(), 1);
        Date monthStart = Date.from(monthStart1.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date todayDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Double saleQtyCurrentMonth = salesPerformanceDao.getRetailerFromOrderReq(customerModel,monthStart,todayDate);
        return saleQtyCurrentMonth/20;
    }





    private Double getSalesQuantityYTDOrderReq(List<OrderRequisitionModel> saleQty) {
        List<Date> dates = getCurrentFinancialYear();
        Date startDate = null;
        Date endDate = null;
        startDate = dates.get(0);
        endDate = dates.get(1);

        Date finalStartDate = startDate;
        Date finalEndDate = endDate;
        var bagLifted = saleQty.stream().filter(sale -> sale.getDeliveredDate().after(finalStartDate) && sale.getDeliveredDate().before(finalEndDate)).mapToDouble(OrderRequisitionModel::getQuantity).sum();
        return bagLifted;
    }

    private Double getSalesQuantityCurrentMonthOrderReqYTD(List<OrderRequisitionModel> saleQty) {
        LocalDate today = LocalDate.now();
        LocalDate monthStart1 = LocalDate.of(today.getYear(), today.getMonth(), 1);
        Date monthStart = Date.from(monthStart1.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date todayDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        var saleQtyCurrentMonth = saleQty.stream().filter(sale -> sale.getDeliveredDate().after(monthStart) && sale.getDeliveredDate().before(todayDate)).mapToDouble(OrderRequisitionModel::getQuantity).sum();
        return saleQtyCurrentMonth;
    }

    private Double getSalesQuantityLastYearCurrentMonthOrderReq(List<EyDmsCustomerModel> cust) {
        LocalDate today = LocalDate.now();
        Double saleQty=0.0;
        LocalDate monthStart1 = LocalDate.of(today.getYear() - 1, today.getMonth(), 1);
        LocalDate monthEnd1 = LocalDate.of(today.getYear() - 1, today.getMonth(), today.lengthOfMonth());
        Date finalStartDate = Date.from(monthStart1.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date finalEndDate = Date.from(monthEnd1.atStartOfDay(ZoneId.systemDefault()).toInstant());
        List<EyDmsCustomerModel> collect = cust.stream().filter(sale -> sale.getLastLiftingDate().after(finalStartDate) && sale.getLastLiftingDate().before(finalEndDate)).collect(Collectors.toList());
        for (EyDmsCustomerModel customerModel : collect) {
            OrderRequisitionModel eydmsCustomerFromOrderReq = salesPerformanceDao.getEyDmsCustomerFromOrderReq(customerModel);
            if(eydmsCustomerFromOrderReq.getQuantity()!=null)
                saleQty+=eydmsCustomerFromOrderReq.getQuantity();
            else
                saleQty=0.0;
        }
       return saleQty;
    }
    private Double getSalesQuantityLastYearCurrentMonthRetailer(EyDmsCustomerModel customerModel) {
        LocalDate today = LocalDate.now();
        Double saleQty=0.0;
        LocalDate monthStart1 = LocalDate.of(today.getYear() - 1, today.getMonth(), 1);
        LocalDate monthEnd1 = LocalDate.of(today.getYear() - 1, today.getMonth(), today.lengthOfMonth());
        Date finalStartDate = Date.from(monthStart1.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date finalEndDate = Date.from(monthEnd1.atStartOfDay(ZoneId.systemDefault()).toInstant());
        //List<DealerRetailerMapModel> collect = cust.stream().filter(sale -> sale.getLastLiftingDate().after(finalStartDate) && sale.getLastLiftingDate().before(finalEndDate)).collect(Collectors.toList());
        saleQty=salesPerformanceDao.getRetailerFromOrderReq(customerModel,finalStartDate,finalEndDate);
        LOGGER.info(saleQty);
        LOGGER.info(monthStart1);
        LOGGER.info(monthEnd1);
        LOGGER.info(finalStartDate);
        LOGGER.info(finalEndDate);
        return salesPerformanceDao.getRetailerFromOrderReq(customerModel,finalStartDate,finalEndDate)/20;
    }

    private Double getSalesQuantityLastYearCurrentMonthOrderReqYTD(List<OrderRequisitionModel> saleQty) {
        LocalDate today = LocalDate.now();
        LocalDate monthStart1 = LocalDate.of(today.getYear() - 1, today.getMonth(), 1);
        LocalDate monthEnd1 = LocalDate.of(today.getYear() - 1, today.getMonth(), today.lengthOfMonth());
        Date finalStartDate = Date.from(monthStart1.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date finalEndDate = Date.from(monthEnd1.atStartOfDay(ZoneId.systemDefault()).toInstant());
        var salesQtyLastYear = saleQty.stream().filter(sale -> sale.getDeliveredDate().after(finalStartDate) && sale.getDeliveredDate().before(finalEndDate)).mapToDouble(OrderRequisitionModel::getQuantity).sum();
        return salesQtyLastYear;
    }

    private Double getSalesQuantityLastYearCurrentMonthOrderReqMonthYear(List<OrderRequisitionModel> saleQty, int month, int year) {
        LocalDate monthStart1 = LocalDate.of(year - 1, month, 1);
        LocalDate monthEnd1 = LocalDate.of(monthStart1.getYear() - 1, monthStart1.getMonth(), monthStart1.lengthOfMonth());
        Date finalStartDate = Date.from(monthStart1.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date finalEndDate = Date.from(monthEnd1.atStartOfDay(ZoneId.systemDefault()).toInstant());
        var salesQtyLastYear = saleQty.stream().filter(sale -> sale.getDeliveredDate().after(finalStartDate) && sale.getDeliveredDate().before(finalEndDate)).mapToDouble(OrderRequisitionModel::getQuantity).sum();
        return salesQtyLastYear;
    }

    private Double getSalesQuantityCurrentMonthOrderReqMonthYear(List<OrderRequisitionModel> saleQty, int month, int year) {
        LocalDate monthStart1 = LocalDate.of(year, month, 1);
        LocalDate monthEnd1 = LocalDate.of(monthStart1.getYear(), monthStart1.getMonth(), monthStart1.lengthOfMonth());
        Date finalStartDate = Date.from(monthStart1.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date finalEndDate = Date.from(monthEnd1.atStartOfDay(ZoneId.systemDefault()).toInstant());
        var salesQtyLastYear = saleQty.stream().filter(sale -> sale.getDeliveredDate().after(finalStartDate) && sale.getDeliveredDate().before(finalEndDate)).mapToDouble(OrderRequisitionModel::getQuantity).sum();
        return salesQtyLastYear;
    }

    @Override
    public LowPerformingNetworkData getLowPerformingSummaryData(int month, int year, String filter, BaseSiteModel site, String customerType, List<String> doList, List<String> subAreaList) {
        LowPerformingNetworkData data = new LowPerformingNetworkData();
        List<SalesPerformNetworkDetailsData> currentNetworkWsDataList = new ArrayList<>();

        RequestCustomerData requestData = new RequestCustomerData();
        List<String> counterType = List.of(customerType);
        requestData.setCounterType(counterType);
        List<SubAreaMasterModel> soList=new ArrayList<>();
        List<DistrictMasterModel> districtList=new ArrayList<>();
        if(subAreaList!=null && !subAreaList.isEmpty()){
            for (String id : subAreaList) {
                soList.add(territoryManagementDao.getTerritoryById(id));
            }
            if(soList.get(0)!=null)
            requestData.setSubAreaMasterPk(soList.get(0).getPk().toString());
        }
        if (doList != null && !doList.isEmpty()) {
            for (String code : doList) {
                districtList.add(districtMasterDao.findByCode(code));
            }
            if(doList.get(0)!=null)
            requestData.setDistrict(districtList.get(0).getName());
        }

        List<EyDmsCustomerModel> eydmsCustomerFilteredList = territoryManagementService.getCustomerforUser(requestData);
        LOGGER.info(String.format("Size before last lifting from eydms customer:%s",String.valueOf(eydmsCustomerFilteredList.size())));

        List<EyDmsCustomerModel> customerFilteredList=new ArrayList<>();
        if(eydmsCustomerFilteredList!=null && !eydmsCustomerFilteredList.isEmpty()) {
            customerFilteredList = salesPerformanceDao.getEyDmsCustomerLastLiftingList(eydmsCustomerFilteredList);
        }
        LOGGER.info(String.format("Size before last lifting from eydms customer:%s",String.valueOf(customerFilteredList.size())));
        if (customerFilteredList != null && !customerFilteredList.isEmpty()) {

            for (EyDmsCustomerModel customerModel : customerFilteredList) {

                SalesPerformNetworkDetailsData dealerCurrentNetworkData = new SalesPerformNetworkDetailsData();

                if (customerType.equalsIgnoreCase(DEALER)) {
                    dealerCurrentNetworkData.setCounterSharePercentage(getCounterShareForDealer(customerModel,  doList, subAreaList));
                    dealerCurrentNetworkData.setSalesQuantity(setSalesQuantityForCustomer(customerModel, customerType,  doList, subAreaList));
                    dealerCurrentNetworkData.setCode(customerModel.getUid());
                    dealerCurrentNetworkData.setName(customerModel.getName());
                    dealerCurrentNetworkData.setPotential(Objects.nonNull(customerModel.getCounterPotential()) ? String.valueOf(customerModel.getCounterPotential()) : ZERO);
                }
                currentNetworkWsDataList.add(dealerCurrentNetworkData);
            }
        }

        if(currentNetworkWsDataList!=null && !currentNetworkWsDataList.isEmpty()) {
            if (customerType.equalsIgnoreCase(DEALER)) {
                LOGGER.info((String.format("Size for Dealer:%s",currentNetworkWsDataList.size())));
                List<SalesPerformNetworkDetailsData> salesPerformNetworkDetailsData1 = currentNetworkWsDataList.stream().filter(nw->Objects.nonNull(nw.getSalesQuantity()) && Objects.nonNull(nw.getSalesQuantity().getActual())).filter(nw -> nw.getSalesQuantity().getActual() != 0.0).collect(Collectors.toList());
                List<SalesPerformNetworkDetailsData> salesPerformNetworkDetailsData2 = salesPerformNetworkDetailsData1.stream().sorted(Comparator.comparing(nw -> nw.getSalesQuantity().getActual())).collect(Collectors.toList());
                List<SalesPerformNetworkDetailsData> collect = salesPerformNetworkDetailsData2.stream().limit((long) salesPerformNetworkDetailsData2.size() * LOWPERCENTAGE / 100).collect(Collectors.toList());
                data.setCount(collect.size());

                Double avgOfCounterShare = 0.0;
                if (collect.size() != 0) {
                    avgOfCounterShare = collect.stream().filter(obj -> Objects.nonNull(obj.getCounterSharePercentage())).mapToDouble(SalesPerformNetworkDetailsData::getCounterSharePercentage).sum();
                    data.setCounterSharePercentage(avgOfCounterShare);
                } else {
                    avgOfCounterShare = 0.0;
                    data.setCounterSharePercentage(avgOfCounterShare);
                }

                Double totalMonthlyPotential = collect.stream().filter(obj -> Objects.nonNull(obj.getPotential())).mapToDouble(objects -> Double.parseDouble(objects.getPotential())).sum();
                data.setTotalMonthlyPotential(totalMonthlyPotential);

                Double avgMonthlyOrders = 0.0;
                if (collect.size() != 0) {
                    avgMonthlyOrders = collect.stream().filter(nw->Objects.nonNull(nw.getSalesQuantity().getActual())).filter(nw -> nw.getSalesQuantity().getActual() != 0).mapToDouble(objects -> objects.getSalesQuantity().getActual()).sum() / collect.size();
                    data.setCountOfAvgMonthlyOrders(avgMonthlyOrders);
                } else {
                    avgMonthlyOrders = 0.0;
                    data.setCountOfAvgMonthlyOrders(avgMonthlyOrders);
                }
            }
        }
        else{
            data.setCount(0);
            data.setCounterSharePercentage(0.0);
            data.setTotalMonthlyPotential(0.0);
            data.setCountOfAvgMonthlyOrders(0.0);
        }
        return data;
    }

    @Override
    public LowPerformingNetworkData getLowPerformingSummaryDataDealer(String customerType) {
        LowPerformingNetworkData data = new LowPerformingNetworkData();
        List<SalesPerformNetworkDetailsData> currentNetworkWsDataList = new ArrayList<>();
        List<EyDmsCustomerModel> eydmsCustomerFilteredList = new ArrayList<>();
        List<EyDmsCustomerModel> customerFilteredList = new ArrayList<>();

        EyDmsCustomerModel currentUser = (EyDmsCustomerModel) userService.getCurrentUser();
        if (currentUser != null) {
            if (currentUser.getGroups().contains(userService.getUserGroupForUID(EyDmsCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                if (!currentUser.getCounterType().equals(CounterType.SP)) {
                    if (customerType.equalsIgnoreCase(RETAILER)) {
                        eydmsCustomerFilteredList = territoryManagementService.getRetailerListForDealer();
                    } else if (customerType.equalsIgnoreCase(INFLUENCER)) {
                        eydmsCustomerFilteredList = territoryManagementService.getInfluencerListForDealer();
                    }
                }
            }
        }
        LOGGER.info(String.format("Size before last lifting from eydms customer:%s",String.valueOf(eydmsCustomerFilteredList.size())));

        if(eydmsCustomerFilteredList!=null && !eydmsCustomerFilteredList.isEmpty()) {
            customerFilteredList = salesPerformanceDao.getEyDmsCustomerLastLiftingList(eydmsCustomerFilteredList);
        }
        LOGGER.info(String.format("Size before last lifting from eydms customer:%s",String.valueOf(customerFilteredList.size()))); if (customerFilteredList != null && !customerFilteredList.isEmpty()) {

            for (EyDmsCustomerModel customerModel : customerFilteredList) {

                SalesPerformNetworkDetailsData dealerCurrentNetworkData = new SalesPerformNetworkDetailsData();

                if (customerType.equalsIgnoreCase(RETAILER)) {
                    EyDmsCustomerModel orderRequisitionSalesDataForRetailer = salesPerformanceDao.getRetailerSalesForDealerLowPerform(customerModel, baseSiteService.getCurrentBaseSite());
                    if (orderRequisitionSalesDataForRetailer != null) {
                        dealerCurrentNetworkData.setCode(customerModel.getUid());
                        dealerCurrentNetworkData.setName(customerModel.getName());
                        if(Objects.nonNull(customerModel.getCounterPotential())) {
                            dealerCurrentNetworkData.setPotential(String.valueOf(customerModel.getCounterPotential()/20));
                        }else{
                            dealerCurrentNetworkData.setPotential(ZERO);
                        }
                        SalesQuantityData sales = new SalesQuantityData();
                        dealerCurrentNetworkData.setCounterSharePercentage(getCounterShareRetailerForDealer(orderRequisitionSalesDataForRetailer));
                        sales.setCurrent(getSalesQuantityCurrentMonthRetailer(orderRequisitionSalesDataForRetailer));
                        dealerCurrentNetworkData.setSalesQuantity(sales);
                    }
                } else if (customerType.equalsIgnoreCase(INFLUENCER)) {

                    List<DealerInfluencerMapModel> pointRequisitionSalesDataForRetailer = salesPerformanceDao.getInfluencerSalesForDealerLowPerform(customerModel, baseSiteService.getCurrentBaseSite());
                    double salesQty=0.0;
                    if (pointRequisitionSalesDataForRetailer != null && !pointRequisitionSalesDataForRetailer.isEmpty()) {
                        dealerCurrentNetworkData.setCode(customerModel.getUid());
                        dealerCurrentNetworkData.setName(customerModel.getName());
                        dealerCurrentNetworkData.setPotential(Objects.nonNull(customerModel.getCounterPotential()) ? String.valueOf(customerModel.getCounterPotential()) : ZERO);
                        for (DealerInfluencerMapModel dealerInfluencerMapModel : pointRequisitionSalesDataForRetailer) {
                          salesQty+=getSalesQuantityInfForDealer(dealerInfluencerMapModel.getInfluencer());
                        }
                        dealerCurrentNetworkData.setBagLifted(salesQty);
                    }
                }
                currentNetworkWsDataList.add(dealerCurrentNetworkData);
            }
        }

        if(currentNetworkWsDataList!=null && !currentNetworkWsDataList.isEmpty()) {
            if (customerType.equalsIgnoreCase(RETAILER)) {
                LOGGER.info((String.format("Size for Retailer:%s",currentNetworkWsDataList.size())));
                List<SalesPerformNetworkDetailsData> salesPerformNetworkDetailsData1 = currentNetworkWsDataList.stream().filter(nw->Objects.nonNull(nw.getSalesQuantity()) && Objects.nonNull(nw.getSalesQuantity().getCurrent())).filter(nw -> nw.getSalesQuantity().getCurrent() != 0.0).collect(Collectors.toList());
                List<SalesPerformNetworkDetailsData> salesPerformNetworkDetailsData2 = salesPerformNetworkDetailsData1.stream().sorted(Comparator.comparing(nw -> nw.getSalesQuantity().getCurrent())).collect(Collectors.toList());
                List<SalesPerformNetworkDetailsData> collect = salesPerformNetworkDetailsData2.stream().limit((long) salesPerformNetworkDetailsData2.size() * LOWPERCENTAGE / 100).collect(Collectors.toList());
                data.setCount(collect.size());

                Double avgOfCounterShare = 0.0;
                if (collect.size() != 0) {
                    avgOfCounterShare = collect.stream().filter(obj -> Objects.nonNull(obj.getCounterSharePercentage())).mapToDouble(SalesPerformNetworkDetailsData::getCounterSharePercentage).sum();
                    data.setCounterSharePercentage(avgOfCounterShare);
                } else {
                    avgOfCounterShare = 0.0;
                    data.setCounterSharePercentage(avgOfCounterShare);
                }

                Double totalMonthlyPotential = collect.stream().filter(obj -> Objects.nonNull(obj.getPotential())).mapToDouble(objects -> Double.parseDouble(objects.getPotential())).sum();
                data.setTotalMonthlyPotential(totalMonthlyPotential);

                Double avgMonthlyOrders = 0.0;
                if (collect.size() != 0) {
                    avgMonthlyOrders = collect.stream().filter(nw ->  nw.getSalesQuantity().getCurrent()!=0).mapToDouble(objects -> objects.getSalesQuantity().getCurrent()).sum() / collect.size();
                    data.setCountOfAvgMonthlyOrders(avgMonthlyOrders);
                } else {
                    avgMonthlyOrders = 0.0;
                    data.setCountOfAvgMonthlyOrders(avgMonthlyOrders);
                }
            }
            if (customerType.equalsIgnoreCase(INFLUENCER)) {
                LOGGER.info((String.format("Size for INFLUENCER:%s",currentNetworkWsDataList.size())));
                List<SalesPerformNetworkDetailsData> collect2 = currentNetworkWsDataList.stream().filter(nw->Objects.nonNull(nw.getBagLifted())).filter(nw -> nw.getBagLifted() != 0).collect(Collectors.toList());
                List<SalesPerformNetworkDetailsData> collect1 = collect2.stream().filter(nw -> nw.getBagLifted() != 0.0).collect(Collectors.toList());
                List<SalesPerformNetworkDetailsData> collect = collect1.stream().limit((long) collect1.size() * LOWPERCENTAGE / 100).collect(Collectors.toList());
                data.setCount(collect.size());
                data.setTotalMonthlyPotential(0.0);
                data.setCounterSharePercentage(0.0);

                double avgMonthlyOrders = 0.0;
                if (collect.size() != 0) {
                    avgMonthlyOrders = collect.stream().mapToDouble(SalesPerformNetworkDetailsData::getBagLifted).sum() / collect.size();
                    data.setCountOfAvgMonthlyOrders(avgMonthlyOrders);
                } else {
                    avgMonthlyOrders = 0.0;
                    data.setCountOfAvgMonthlyOrders(avgMonthlyOrders);
                }
            }
        }
        else{
            data.setCount(0);
            data.setCounterSharePercentage(0.0);
            data.setTotalMonthlyPotential(0.0);
            data.setCountOfAvgMonthlyOrders(0.0);
        }
        return data;
    }

    @Override
    public Double getSalesTargetForMonth(EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite,
                                         Integer year, Integer month) {

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.YEAR, year);
        Date date = cal.getTime();

        String monthName = getMonth(date);
        String yearName = getYear(date);
        String formattedMonth = monthName.concat("-").concat(String.valueOf(yearName));
        return getSalesPerformanceDao().getMonthlySalesTarget(eydmsUser, currentBaseSite, formattedMonth, yearName);
    }

    @Override
    public Double getSalesTargetForMonth(EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite, Integer year, Integer month, List<String> doList, List<String> subAreaList) {

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.YEAR, year);
        Date date = cal.getTime();

        String monthName = getMonth(date);
        String yearName = getYear(date);

        return getSalesPerformanceDao().getMonthlySalesTarget(eydmsUser, currentBaseSite, monthName, yearName,doList,subAreaList);
    }

    @Override
    public Double getSalesTargetForMonthDealer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel currentBaseSite, Integer year, Integer month, String bgpFilter) {
        List<DealerRevisedMonthlySalesModel> monthlySalesTargetForDealerWithBGP = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.YEAR, year);
        Date date = cal.getTime();

        String monthName = getMonth(date);
        String yearName = getYear(date);

        String formattedMonth = monthName.concat("-").concat(String.valueOf(yearName));

        CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
        if (StringUtils.isNotBlank(bgpFilter) && !bgpFilter.equalsIgnoreCase("ALL")) {
        ProductModel product = productService.getProductForCode(catalogVersion, bgpFilter);

            double revisedTarget = 0.0, sumRevisedTarget = 0.0;
            if(bgpFilter.equalsIgnoreCase(product.getCode())) {
                monthlySalesTargetForDealerWithBGP = salesPerformanceDao.getMonthlySalesTargetForDealerWithBGP(eydmsCustomer, currentBaseSite, formattedMonth, yearName, bgpFilter);

                for (DealerRevisedMonthlySalesModel dealerRevisedMonthlySalesModel : monthlySalesTargetForDealerWithBGP) {
                    if (dealerRevisedMonthlySalesModel != null) {
                        for (ProductModel listOfSkus : dealerRevisedMonthlySalesModel.getListOfSkus()) {
                            if (listOfSkus.getCode().equalsIgnoreCase(product.getCode())) {
                                ProductSaleModel productSaleModel = salesPerformanceDao.getTotalTargetForProductBGPFilterMTD(dealerRevisedMonthlySalesModel.getCustomerCode(), listOfSkus.getCode(), formattedMonth, yearName);
                                if (productSaleModel != null) {
                                    revisedTarget = productSaleModel.getRevisedTarget();
                                    sumRevisedTarget += revisedTarget;
                                }
                            }
                        }
                    }
                }
            }
            return sumRevisedTarget;
        } else {
           // return salesPerformanceDao.getMonthlySalesTargetForDealer(eydmsCustomer, currentBaseSite, monthName, yearName, bgpFilter);
            return (Double) sessionService.executeInLocalView(new SessionExecutionBody()
            {
                @Override
                public Double execute()
                {
                    try {
                        searchRestrictionService.disableSearchRestrictions();
                        return salesPerformanceDao.getMonthlySalesTargetForDealer(eydmsCustomer, currentBaseSite, formattedMonth, yearName, bgpFilter);
                    }
                    finally {
                        searchRestrictionService.enableSearchRestrictions();
                    }
                }
            });
        }
    }

    @Override
    public Double getSalesTargetForMtdSP(List<EyDmsCustomerModel> eydmsUser, BaseSiteModel currentBaseSite, Integer year, Integer month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.YEAR, year);
        Date date = cal.getTime();

        String monthName = getMonth(date);
        String yearName = getYear(date);

        String formattedMonth = monthName.concat("-").concat(String.valueOf(yearName));

        return salesPerformanceDao.getMonthlySalesTargetForMtdSp(eydmsUser, currentBaseSite, monthName, yearName);
    }

    @Override
    public Double getSalesTargetForMonthSP(List<EyDmsCustomerModel> eydmsCustomerModel, Integer year, Integer month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.YEAR, year);
        Date date = cal.getTime();

        String monthName = getMonth(date);
        String yearName = getYear(date);

        String formattedMonth = monthName.concat("-").concat(String.valueOf(yearName));
        return salesPerformanceDao.getMonthlySalesTargetForSP(eydmsCustomerModel, monthName, yearName);
    }

    @Override
    public Double getSalesTargetForMonthRetailer(String retailerCode, BaseSiteModel currentBaseSite, Integer year, Integer month, String bgpFilter) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.YEAR, year);
        Date date = cal.getTime();

        String monthName = getMonth(date);
        String yearName = getYear(date);
        String formattedMonth = monthName.concat("-").concat(String.valueOf(yearName));
        if (StringUtils.isNotBlank(bgpFilter) && !bgpFilter.equalsIgnoreCase("ALL")) {
            return 0.0;
        } else {
            return getSalesPerformanceDao().getMonthlySalesTargetForRetailer(retailerCode, currentBaseSite, formattedMonth, bgpFilter);
        }
    }

    @Override
    public Double getSalesTargetForPartnerMonth(String code, EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite, Integer year, Integer month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.YEAR, year);
        Date date = cal.getTime();

        String monthName = getMonth(date);
        String yearName = getYear(date);

        return getSalesPerformanceDao().getMonthlySalesTarget(eydmsUser, currentBaseSite, monthName, yearName);
    }

    @Override
    public Double getActualTargetForSalesMTD(EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite, List<String> doList, List<String> subAreaList) {
        return salesPerformanceDao.getActualTargetForSalesMTD(eydmsUser, currentBaseSite, doList, subAreaList);
    }

    @Override
    public Double getActualTargetForSalesMTDDealer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel currentBaseSite, String bgpFilter) {
        return salesPerformanceDao.getActualTargetForSalesMTDDealer(eydmsCustomer, currentBaseSite, bgpFilter);
    }

    @Override
    public Double getActualTargetForSalesMTDRetailer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel currentBaseSite, String bgpFilter) {
        return salesPerformanceDao.getActualTargetForSalesMTDRetailer(eydmsCustomer, currentBaseSite, bgpFilter);
    }

    @Override
    public Double getActualTargetForSalesDealerMTD(EyDmsCustomerModel eydmsCustomer, BaseSiteModel currentBaseSite, String bgpFilter) {
       return salesPerformanceDao.getActualTargetForSalesDealerMTD(eydmsCustomer, currentBaseSite, bgpFilter);
    }

    @Override
    public Double getActualTargetForSalesSPMTD(List<EyDmsCustomerModel> eydmsCustomer) {
        return salesPerformanceDao.getActualTargetForSalesSPMTD(eydmsCustomer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Double getActualTargetForSalesSPMTDSearch(List<EyDmsCustomerModel> eydmsCustomer) {
       return (Double) sessionService.executeInLocalView(new SessionExecutionBody()
        {
            @Override
            public Double execute()
            {
                try {
                    searchRestrictionService.disableSearchRestrictions();
                    return salesPerformanceDao.getActualTargetForSalesSPMTDSearch(eydmsCustomer);
                }
                finally {
                    searchRestrictionService.enableSearchRestrictions();
                }
            }
        });
    }

    @Override
    public Double getActualTargetForSalesRetailerMTD(EyDmsCustomerModel eydmsCustomer, BaseSiteModel currentBaseSite, String bgpFilter) {
        return salesPerformanceDao.getActualTargetForSalesRetailerMTD(eydmsCustomer, currentBaseSite, bgpFilter);
    }

    @Override
    public Double getActualTargetForSalesLastMonth(EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite,int year, int month, List<String> doList, List<String> subAreaList) {
        return salesPerformanceDao.getActualTargetForSalesLastMonth(eydmsUser, currentBaseSite,year,month, doList, subAreaList);
    }

    @Override
    public Double getActualTargetForSalesYTD(EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite, List<String> doList, List<String> subAreaList) {
        List<Date> dates = getCurrentFinancialYear();
        Date startDate = null;
        Date endDate = null;
        startDate = dates.get(0);
        endDate = dates.get(1);
        return salesPerformanceDao.getActualTargetForSalesYTD(eydmsUser, currentBaseSite, startDate, endDate, doList, subAreaList);
    }

    @Override
    public Double getActualTargetForSalesYTDDealer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel currentBaseSite, String bgpFilter) {
        List<Date> dates = getCurrentFinancialYear();
        Date startDate = null;
        Date endDate = null;
        startDate = dates.get(0);
        endDate = dates.get(1);
        return salesPerformanceDao.getActualTargetForSalesYTDDealer(eydmsCustomer, currentBaseSite, startDate, endDate, bgpFilter);
    }

/*    @Override
    public Double getActualTargetForSalesYTDSP(List<EyDmsCustomerModel> eydmsCustomer, BaseSiteModel currentBaseSite) {
        List<Date> dates = getCurrentFinancialYear();
        Date startDate = null;
        Date endDate = null;
        startDate = dates.get(0);
        endDate = dates.get(1);
        return salesPerformanceDao.getActualTargetForSalesYTDSP(eydmsCustomer, currentBaseSite, startDate, endDate);
    }*/

    @SuppressWarnings("unchecked")
    @Override
    public Double getActualTargetForSalesYTDSP(List<EyDmsCustomerModel> eydmsCustomer, BaseSiteModel currentBaseSite) {
        return (Double) sessionService.executeInLocalView(new SessionExecutionBody()
        {
            @Override
            public Double execute()
            {
                try {
                    List<Date> dates = getCurrentFinancialYear();
                    Date startDate = null;
                    Date endDate = null;
                    startDate = dates.get(0);
                    endDate = dates.get(1);
                    searchRestrictionService.disableSearchRestrictions();
                    return salesPerformanceDao.getActualTargetForSalesYTDSP(eydmsCustomer, currentBaseSite, startDate, endDate);
                }
                finally {
                    searchRestrictionService.enableSearchRestrictions();
                }
            }
        });
    }

    @Override
    public Double getActualTargetForSalesYTDRetailer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel currentBaseSite, String bgpFilter) {
       List<Date> dates = getCurrentFinancialYear();
        Date startDate = null;
        Date endDate = null;
        startDate = dates.get(0);
        endDate = dates.get(1);
        return salesPerformanceDao.getActualTargetForSalesYTDRetailer(eydmsCustomer, currentBaseSite,startDate, endDate, bgpFilter);
    }


    @Override
    public Double getActualTargetForSalesDealerYTD(EyDmsCustomerModel eydmsCustomer, BaseSiteModel currentBaseSite, String bgpFilter) {
        List<Date> dates = getCurrentFinancialYear();
        Date startDate = null;
        Date endDate = null;
        startDate = dates.get(0);
        endDate = dates.get(1);
        return salesPerformanceDao.getActualTargetForSalesDealerYTD(eydmsCustomer, currentBaseSite, startDate, endDate, bgpFilter);
    }

    @Override
    public Double getActualTargetForSalesRetailerYTD(EyDmsCustomerModel eydmsCustomer, BaseSiteModel currentBaseSite, String bgpFilter) {
        List<Date> dates = getCurrentFinancialYear();
        Date startDate = null;
        Date endDate = null;
        startDate = dates.get(0);
        endDate = dates.get(1);
        return salesPerformanceDao.getActualTargetForSalesRetailerYTD(eydmsCustomer, currentBaseSite, startDate, endDate, bgpFilter);
    }

    @Override
    public Double getActualTargetForSalesLastYear(EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite, List<String> doList, List<String> subAreaList) {

        List<Date> dates = getPrevFinancialYear();
        Date startDate = null;
        Date endDate = null;
        startDate = dates.get(0);
        endDate = dates.get(1);

        return salesPerformanceDao.getActualTargetForSalesYTD(eydmsUser, currentBaseSite, startDate, endDate, doList, subAreaList);

    }

    @Override
    public Double getActualTargetForPartnerSalesMTD(String code, EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite, List<String> doList, List<String> subAreaList) {
        return salesPerformanceDao.getActualTargetForPartnerSalesMTD(code,eydmsUser, currentBaseSite, doList, subAreaList);
    }

    @Override
    public Double getActualTargetForPartnerSalesYTD(String code, EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite, List<String> doList, List<String> subAreaList) {
        List<Date> dates = getCurrentFinancialYear();
        Date startDate = null;
        Date endDate = null;
        startDate = dates.get(0);
        endDate = dates.get(1);
        return salesPerformanceDao.getActualTargetForPartnerSalesYTD(code,eydmsUser, currentBaseSite, startDate, endDate, doList, subAreaList);
    }

    @Override
    public Double getActualTargetForSalesForMonth(EyDmsUserModel eydmsUser,
                                                  BaseSiteModel currentBaseSite, int year, int month, List<String> doList, List<String> subAreaList) {
        return salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYear(eydmsUser, currentBaseSite, month, year, doList, subAreaList);
    }

    @Override
    public Double getActualTargetForSalesForMonthDealer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel currentBaseSite, int year, int month, String bgpFilter) {
        return salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYearForDealer(eydmsCustomer, currentBaseSite, month, year, bgpFilter);
    }

   /* @Override
    public Double getActualTargetForSalesForMonthDealer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel currentBaseSite, int year, int month, String bgpFilter) {
        return (Double) sessionService.executeInLocalView(new SessionExecutionBody()
        {
            @Override
            public Double execute()
            {
                try {
                    searchRestrictionService.disableSearchRestrictions();
                    return salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYearForDealer(eydmsCustomer, currentBaseSite, month, year, bgpFilter);
                }
                finally {
                    searchRestrictionService.enableSearchRestrictions();
                }
            }
        });
    }*/
    @Override
    public Double getActualTargetForSalesForMtdSp(List<EyDmsCustomerModel> eydmsCustomer, BaseSiteModel currentBaseSite, int year, int month) {
        return salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYearForMTDSP(eydmsCustomer, currentBaseSite, month, year);
    }

   /* @Override
    public Double getActualTargetForSalesForMonthSP(List<EyDmsCustomerModel> eydmsCustomerModels, int year, int month) {
        return salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYearForSP(eydmsCustomerModels, month, year);
    }
*/
    @SuppressWarnings("unchecked")
    @Override
    public Double getActualTargetForSalesForMonthSP(List<EyDmsCustomerModel> eydmsCustomerModels, int year, int month) {
        return (Double) sessionService.executeInLocalView(new SessionExecutionBody()
        {
            @Override
            public Double execute()
            {
                try {
                    searchRestrictionService.disableSearchRestrictions();
                    return salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYearForSP(eydmsCustomerModels, month, year);
                }
                finally {
                    searchRestrictionService.enableSearchRestrictions();
                }
            }
        });
    }


    @Override
    public Double getActualTargetForSalesForMonthRetailer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel currentBaseSite, int year, int month, String bgpFilter) {
        return salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYearForRetailer(eydmsCustomer, currentBaseSite, month, year, bgpFilter);
    }

    @Override
    public Double getActualTargetForPartnerSalesForMonth(String code, EyDmsUserModel eydmsUser, BaseSiteModel currentBaseSite, int year, int month, List<String> doList, List<String> subAreaList) {
        return salesPerformanceDao.getActualTargetPartnerSalesForSelectedMonthAndYear(code,eydmsUser, currentBaseSite, year, month, doList, subAreaList);
    }

    @Override
    public Double getActualTargetForSalesForMonth(String subArea, EyDmsUserModel eydmsUser,
                                                  BaseSiteModel currentBaseSite, int year, int month) {
        return salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYear(eydmsUser, currentBaseSite, month, year, null, null);
    }

    @Override
    public Double getActualTargetSalesForSelectedMonthAndYear(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, int month, int year, List<String> doList, List<String> subAreaList) {
        return getSalesPerformanceDao().getActualTargetSalesForSelectedMonthAndYear(eydmsUser, baseSite, month, year, doList, subAreaList);
    }

    public List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForYTD(EyDmsUserModel eydmsUser, BaseSiteModel baseSite, List<String> doList, List<String> subAreaList) {
        List<Date> dates = getCurrentFinancialYear();
        Date startDate = null;
        Date endDate = null;
        startDate = dates.get(0);
        endDate = dates.get(1);
        return salesPerformanceDao.getProductwiseSalesPercentRatioAndVolumeRatioForYTD(eydmsUser, baseSite, startDate, endDate, doList, subAreaList);
    }

    public static String format(double value) {
        if (value < 1000) {
            return format("###", value);
        } else {
            double hundreds = value % 1000;
            int other = (int) (value / 1000);
            return format(",##", other) + ',' + format("000", hundreds);
        }
    }

    private static String format(String pattern, Object value) {
        return new DecimalFormat(pattern).format(value);
    }


    public CatalogVersionService getCatalogVersionService() {
        return catalogVersionService;
    }

    public void setCatalogVersionService(CatalogVersionService catalogVersionService) {
        this.catalogVersionService = catalogVersionService;
    }
}
