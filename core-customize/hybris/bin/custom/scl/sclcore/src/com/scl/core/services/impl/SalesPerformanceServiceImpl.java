package com.scl.core.services.impl;

import com.oracle.wls.shaded.org.apache.regexp.RE;
import com.scl.core.brand.dao.BrandDao;
import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.*;
import com.scl.core.enums.CounterType;
import com.scl.core.enums.CustomerCategory;
import com.scl.core.enums.NetworkType;
import com.scl.core.enums.WarehouseType;
import com.scl.core.model.*;
import com.scl.core.region.dao.DistrictMasterDao;
import com.scl.core.services.*;
import de.hybris.platform.core.model.c2l.BrandModel;
import de.hybris.platform.search.restriction.SearchRestrictionService;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import com.scl.core.utility.SclDateUtility;
import com.scl.facades.data.*;
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
import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class SalesPerformanceServiceImpl implements SalesPerformanceService {

    private static final Logger LOGGER = Logger.getLogger(SalesPerformanceServiceImpl.class);
    DecimalFormat df = new DecimalFormat("#.##");
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
    private String START_MONTH="startMonth";
    private String START_YEAR="startYear";
    private String END_MONTH="endMonth";
    private String END_YEAR="endYear";

    @Resource
    private SalesPerformanceDao salesPerformanceDao;
    @Resource
    private TerritoryMasterService territoryMasterService;
    @Resource
    private NetworkService networkService;
    @Autowired
    SclSalesSummaryDao sclSalesSummaryDao;
    @Autowired
    SalesPerformanceService salesPerformanceService;
    @Autowired
    PointRequisitionDao pointRequisitionDao;

    @Resource
    private TerritoryManagementService territoryManagementService;
    @Resource
    private SclUserDao sclUserDao;
    @Autowired
    TerritoryManagementDao territoryManagementDao;
    @Autowired
    Converter<SclCustomerModel, SalesPerformNetworkDetailsData> salesPerformnceCustomerDetailsConverter;
    @Autowired
    Converter<SclCustomerModel, DealerCurrentNetworkData> currentNetworkDetailsConverter;
    @Autowired
    DistrictMasterDao districtMasterDao;
    @Resource
    DJPVisitDao djpVisitDao;
    @Autowired
    DJPVisitService djpVisitService;
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
    SclSalesSummaryService sclSalesSummaryService;

    @Resource
    ModelService modelService;

    @Autowired
    private DataConstraintDao dataConstraintDao;
    @Autowired
    BrandDao brandDao;
    @Autowired
    private SearchRestrictionService searchRestrictionService;

    @Autowired
    private DealerDao dealerDao;

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

    public SclUserDao getSclUserDao() {
        return sclUserDao;
    }

    public void setSclUserDao(SclUserDao sclUserDao) {
        this.sclUserDao = sclUserDao;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Resource
    private Converter<SclCustomerModel, SalesPerformNetworkDetailsData> influencerSummarySalesPerformanceConverter;

    public Converter<SclCustomerModel, SalesPerformNetworkDetailsData> getInfluencerSummarySalesPerformanceConverter() {
        return influencerSummarySalesPerformanceConverter;
    }

    public void setInfluencerSummarySalesPerformanceConverter(Converter<SclCustomerModel, SalesPerformNetworkDetailsData> influencerSummarySalesPerformanceConverter) {
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
    public Double getActualTargetForSalesMTD(String subArea, SclUserModel sclUser, BaseSiteModel currentBaseSite) {
        return salesPerformanceDao.getActualTargetForSalesMTD(subArea, sclUser, currentBaseSite);
    }

    @Override
    public Double getActualTargetForSalesYTD(String subArea, SclUserModel sclUser, BaseSiteModel currentBaseSite) {
        List<Date> dates = getCurrentFinancialYear();
        Date startDate = null;
        Date endDate = null;
        startDate = dates.get(0);
        endDate = dates.get(1);
        return salesPerformanceDao.getActualTargetForSalesYTD(subArea, sclUser, currentBaseSite, startDate, endDate);
    }

    @Override
    public Double getMonthlySalesTarget(SclUserModel sclUser, BaseSiteModel currentBaseSite,List<String> territoryList) {
        //String month = getMonth(new Date());
        // String year = getYear(new Date());
        LocalDate localDate = LocalDate.now();
        int currentYear = localDate.getYear();
        String month = getMonth(new Date());
        String formattedMonth = month.concat("-").concat(String.valueOf(currentYear));
        return salesPerformanceDao.getMonthlySalesTarget(sclUser, currentBaseSite, formattedMonth, String.valueOf(currentYear),territoryList);
    }
    @Override
    public Double getMonthlySalesTarget(SclUserModel sclUser, BaseSiteModel currentBaseSite,int month,int year,List<String> territoryList) {
        String monthName = Month.of(month).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        String formattedMonth = monthName.concat("-").concat(String.valueOf(year));
        return salesPerformanceDao.getMonthlySalesTarget(sclUser, currentBaseSite, formattedMonth, String.valueOf(year),territoryList);
    }

    @Override
    public Double getMonthlySalesTargetForDealer(SclCustomerModel sclCustomerModel, BaseSiteModel currentBaseSite, String bgpFilter) {
        List<DealerRevisedMonthlySalesModel> monthlySalesTargetForDealerWithBGP = new ArrayList<>();
        LocalDate localDate = LocalDate.now();
        int currentYear = localDate.getYear();
        String month = getMonth(new Date());
        double revisedTarget = 0.0, sumRevisedTarget = 0.0;
        String formattedMonth = month.concat("-").concat(String.valueOf(currentYear));
        if (StringUtils.isNotBlank(bgpFilter) && !bgpFilter.equalsIgnoreCase("ALL")) {
            ProductModel productModel = sclSalesSummaryService.getProductAliasNameByCode(bgpFilter);
            if (productModel != null) {
                CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
                ProductModel product = productService.getProductForCode(catalogVersion, productModel.getCode());
                if (product != null) {
                  //  if (bgpFilter.equalsIgnoreCase(product.getName())) {

                        monthlySalesTargetForDealerWithBGP = salesPerformanceDao.getMonthlySalesTargetForDealerWithBGP(sclCustomerModel, currentBaseSite, formattedMonth, String.valueOf(currentYear), bgpFilter);
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
                    //}
                }

            }
            return sumRevisedTarget;
        }else {
           // return salesPerformanceDao.getMonthlySalesTargetForDealer(sclCustomerModel, currentBaseSite, formattedMonth, String.valueOf(currentYear), bgpFilter);
            return (Double) sessionService.executeInLocalView(new SessionExecutionBody()
            {
                @Override
                public Double execute()
                {
                    try {
                        searchRestrictionService.disableSearchRestrictions();
                        return salesPerformanceDao.getMonthlySalesTargetForDealer(sclCustomerModel, currentBaseSite, formattedMonth, String.valueOf(currentYear), bgpFilter);
                    }
                    finally {
                        searchRestrictionService.enableSearchRestrictions();
                    }
                }
            });
        }
    }

    @Override
    public Double getMonthlySalesTargetForDealerList(List<B2BCustomerModel> b2BCustomerModels, BaseSiteModel currentBaseSite,int month,int year) {
        String monthName = Month.of(month).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        String formattedMonth = monthName.concat("-").concat(String.valueOf(year));
       return salesPerformanceDao.getMonthlySalesTargetForDealerList(b2BCustomerModels, currentBaseSite, formattedMonth, String.valueOf(year), null);
    }
    @Override
    public Double getMonthlySalesTargetForDealer(SclCustomerModel sclCustomerModel, BaseSiteModel currentBaseSite,int month,int year,String bgpFilter) {
        String monthName = Month.of(month).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        String formattedMonth = monthName.concat("-").concat(String.valueOf(year));
        return salesPerformanceDao.getMonthlySalesTargetForDealer(sclCustomerModel, currentBaseSite, formattedMonth, String.valueOf(year), bgpFilter);
    }

    @Override
    public Double getMonthlySalesTargetForDealerLastMonth(List<B2BCustomerModel> b2BCustomerModel, BaseSiteModel currentBaseSite,int month,int year) {
        String monthName = Month.of(month).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        String formattedMonth = monthName.concat("-").concat(String.valueOf(year));
        return salesPerformanceDao.getMonthlySalesTargetForDealerList(b2BCustomerModel, currentBaseSite, formattedMonth, String.valueOf(year), null);
    }


    @Override
    public Double getMonthlySalesTargetForSP(List<SclCustomerModel> sclCustomerModel) {
        LocalDate localDate = LocalDate.now();
        int currentYear = localDate.getYear();
        String month = getMonth(new Date());
        double revisedTarget = 0.0, sumRevisedTarget = 0.0;
        String formattedMonth = month.concat("-").concat(String.valueOf(currentYear));
        return salesPerformanceDao.getMonthlySalesTargetForSP(sclCustomerModel,formattedMonth, String.valueOf(currentYear));
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
    public Double getMonthlySalesTargetForRetailer(List<SclCustomerModel> retailerCode, BaseSiteModel currentBaseSite, String bgpFilter, String monthYear) {
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
    public List<List<Object>> getMonthlySalesTargetForRetailer(List<SclCustomerModel> sclCustomerModels) {
        LocalDate localDate = LocalDate.now();
        int currentYear = localDate.getYear();
        String month = getMonth(new Date());
        String formattedMonth = month.concat("-").concat(String.valueOf(currentYear));
        return salesPerformanceDao.getMonthlySalesTargetForRetailer(sclCustomerModels, formattedMonth);
    }

    @Override
    public Double getActualTargetForSalesYTDRetailerList(SclCustomerModel currentUser, BaseSiteModel currentBaseSite) {

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
    public Double getActualTargetForSalesMTDRetailerList(SclCustomerModel currentUser, BaseSiteModel currentBaseSite) {
        return salesPerformanceDao.getActualTargetForSalesRetailerMTDList(currentUser, currentBaseSite);
    }

    @Override
    public Double getActualTargetForSalesYTRetailer(SclCustomerModel currentUser, BaseSiteModel currentBaseSite, String bgpFilter) {
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
    public List<SclCustomerModel> getSPForDistrict(DistrictMasterModel districtForSP) {
        return salesPerformanceDao.getSPForDistrict(districtForSP);
    }

    @Override
    public Double getAnnualSalesTargetForDealerFY(SclCustomerModel sclCustomerModel, String bgpFilter, String financialYear) {

        List<List<Object>> list = new ArrayList<>();
        List<DealerRevisedAnnualSalesModel> dealerRevisedAnnualSalesModels = new ArrayList<>();
        Double totalTarget = 0.0, sumTotalTarget = 0.0;
        if (StringUtils.isNotBlank(bgpFilter) && !bgpFilter.equalsIgnoreCase("ALL")) {
            ProductModel productModel = sclSalesSummaryService.getProductAliasNameByCode(bgpFilter);
            if(productModel!=null) {
                CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
                ProductModel product = productService.getProductForCode(catalogVersion, productModel.getCode());
                if (product != null) {
                  //  if (bgpFilter.equalsIgnoreCase(product.getName())) {
                        dealerRevisedAnnualSalesModels = salesPerformanceDao.getAnnualSalesTargetForDealerWithBGP(sclCustomerModel, financialYear, bgpFilter);
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
                    //}
                }
            }
            return sumTotalTarget;
        } else {
            Double annualSalesTarget = salesPerformanceDao.getAnnualSalesTargetForDealer(sclCustomerModel, financialYear, bgpFilter);
            return annualSalesTarget;
        }
    }

    @Override
    public Double getAnnualSalesTargetForRetailerFY(SclCustomerModel sclCustomerModel, String bgpFilter, String financialYear) {
        double annualSalesTarget = 0.0, totalSale = 0.0;
        List<List<Object>> list = new ArrayList<>();
        if (StringUtils.isNotBlank(bgpFilter) && !bgpFilter.equalsIgnoreCase("ALL")) {
            return annualSalesTarget;
        } else {
            annualSalesTarget = salesPerformanceDao.getAnnualSalesTargetForRetailer(sclCustomerModel, financialYear, bgpFilter);
            return annualSalesTarget;
        }

    }

    @Override
    public List<SclUserModel> getTsmByRegion(String district) {

        return salesPerformanceDao.getTsmByRegion(district);
    }

    @Override
    public List<SclUserModel> getRHByState(String district) {

        return salesPerformanceDao.getRHByState(district);
    }

    @Override
    public List<SclCustomerModel> getCustomerForTsm(String district, SclUserModel sclUserModel) {
        return salesPerformanceDao.getCustomerForTsm(district,sclUserModel);
    }

    @Override
    public List<SclCustomerModel> getCustomerForRH(String district, SclUserModel sclUserModel) {
        return salesPerformanceDao.getCustomerForRH(district,sclUserModel);
    }

    @Override
    public List<List<Object>> getSalesDealerByDate(String district, SclUserModel sclUserModel,Date startDate, Date endDate) {
        return salesPerformanceDao.getSalesDealerByDate(district,sclUserModel,startDate,endDate);

    }


    @SuppressWarnings("unchecked")
    @Override
    public List<List<Object>> getRHSalesDealerByDate(SclUserModel sclUserModel,Date startDate, Date endDate)  {
        return (List<List<Object>>) sessionService.executeInLocalView(new SessionExecutionBody()
        {
            @Override
            public List<List<Object>> execute()
            {
                try {
                    searchRestrictionService.disableSearchRestrictions();
                    return salesPerformanceDao.getRHSalesDealerByDate(sclUserModel,startDate,endDate);
                }
                finally {
                    searchRestrictionService.enableSearchRestrictions();
                }
            }
        });
    }

    @Override
    public List<SclCustomerModel> getCustomerForSp(SclCustomerModel sclCustomer) {

        return salesPerformanceDao.getCustomerForSp(sclCustomer);
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
    public Double getLastMonthSalesTarget(SclUserModel sclUser, BaseSiteModel currentBaseSite,List<String> territoryList) {

        LocalDate date1 = LocalDate.now();
        LocalDate month1 = date1.minusMonths(1);
        Date date = Date.from(month1.atStartOfDay(ZoneId.systemDefault()).toInstant());
        String month = getMonth(date);
        String year = getYear(new Date());
        String formattedMonth = month.concat("-").concat(year);
        return salesPerformanceDao.getMonthlySalesTarget(sclUser, currentBaseSite, formattedMonth, year,territoryList);
    }

    @Override
    public Double getMonthlySalesForPartnerTarget(String code, SclUserModel sclUser, BaseSiteModel currentBaseSite) {
        String month = getMonth(new Date());
        String year = getYear(new Date());
        return salesPerformanceDao.getMonthlySalesTarget(sclUser, currentBaseSite, month, year);
    }

    @Override
    public Double getAnnualSalesTarget(SclUserModel sclUser,List<String> territoryList) {
        String financialYear = findNextFinancialYear();
        double annualSalesTarget = 0.0;
        annualSalesTarget  = salesPerformanceDao.getAnnualSalesTarget(sclUser, financialYear,territoryList);
        return annualSalesTarget;
    }

    @Override
    public Double getAnnualSalesTargetForDealer(SclCustomerModel sclCustomerModel, String bgpFilter) {
        String financialYear = findNextFinancialYear();
        List<List<Object>> list = new ArrayList<>();
        List<DealerRevisedAnnualSalesModel> dealerRevisedAnnualSalesModels = new ArrayList<>();
        Double totalTarget = 0.0, sumTotalTarget = 0.0;
        if (StringUtils.isNotBlank(bgpFilter) && !bgpFilter.equalsIgnoreCase("ALL")) {
            ProductModel productModel = sclSalesSummaryService.getProductAliasNameByCode(bgpFilter);
            if(productModel!=null) {
                CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
                ProductModel product = productService.getProductForCode(catalogVersion, productModel.getCode());
                if (product != null) {
                    //if (bgpFilter.equalsIgnoreCase(product.getName())) {
                        dealerRevisedAnnualSalesModels = salesPerformanceDao.getAnnualSalesTargetForDealerWithBGP(sclCustomerModel, financialYear, bgpFilter);
                        if (dealerRevisedAnnualSalesModels != null && !dealerRevisedAnnualSalesModels.isEmpty()) {
                            for (DealerRevisedAnnualSalesModel dealerRevisedAnnualSalesModel : dealerRevisedAnnualSalesModels) {
                                if (dealerRevisedAnnualSalesModel != null) {
                                    for (ProductModel listOfSkus : dealerRevisedAnnualSalesModel.getListOfSkus()) {
                                        if (listOfSkus != null) {
                                            if (listOfSkus.getCode().equalsIgnoreCase(product.getCode())) {
                                                ProductSaleModel productSaleModel = salesPerformanceDao.getTotalTargetForProductBGPFilter(dealerRevisedAnnualSalesModel.getCustomerCode(), listOfSkus.getCode());
                                                if (productSaleModel != null) {
                                                    totalTarget = productSaleModel.getRevisedTarget();
                                                    if (totalTarget != null)
                                                        sumTotalTarget += totalTarget;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    //}
                }
            }
            return sumTotalTarget;
        } else {
            Double annualSalesTarget = salesPerformanceDao.getAnnualSalesTargetForDealer(sclCustomerModel, financialYear, bgpFilter);
            return annualSalesTarget;
        }
    }

    @Override
    public Double getAnnualSalesTargetForSP(List<SclCustomerModel> sclCustomerModel) {
        String financialYear = findNextFinancialYear();
        Double annualSalesTarget = salesPerformanceDao.getAnnualSalesTargetForSP(sclCustomerModel, financialYear);
        return annualSalesTarget;
    }

    @Override
    public Double getAnnualSalesTargetForRetailer(SclCustomerModel sclCustomerModel, String bgpFilter) {
        String financialYear = findNextFinancialYear();
        double annualSalesTarget = 0.0, totalSale = 0.0;
        List<List<Object>> list = new ArrayList<>();
        if (StringUtils.isNotBlank(bgpFilter) && !bgpFilter.equalsIgnoreCase("ALL")) {
            return annualSalesTarget;
        } else {
            annualSalesTarget = salesPerformanceDao.getAnnualSalesTargetForRetailer(sclCustomerModel, financialYear, bgpFilter);
            return annualSalesTarget;
        }

    }

    @Override
    public Double getLastYearSalesTarget(SclUserModel sclUser, BaseSiteModel currentBaseSite,List<String> territoryList) {
        String financialYear = findPreviousFinancialYear();
        double annualSalesTarget = 0.0;
        annualSalesTarget = salesPerformanceDao.getAnnualSalesTarget(sclUser, financialYear,territoryList);
        return annualSalesTarget;
    }

    @Override
    public Double getAnnualSalesForPartnerTarget(String code, SclUserModel sclUser, BaseSiteModel currentBaseSite) {
        String financialYear = findNextFinancialYear();
        double annualSalesTarget = 0.0;
        annualSalesTarget = salesPerformanceDao.getAnnualSalesTarget(sclUser, financialYear,null);
        return annualSalesTarget;
    }

    @Override
    public List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatio(SclUserModel sclUser, BaseSiteModel baseSite, SclCustomerModel customer, List<String> doList, List<String> subAreaList) {
        return salesPerformanceDao.getProductwiseSalesPercentRatioAndVolumeRatio(sclUser, baseSite, customer, doList, subAreaList);
    }


    public List<SalesPerformNetworkDetailsData> getAllLowPerformingDetails(String subArea, BaseSiteModel site, String leadType, int month, int year, String filter) {
/*
        Double yoyGrowth;
        Double zero = 0.0;
        List<SalesPerformNetworkDetailsData> currentNetworkWsDataList = new ArrayList<>();
        List<SclCustomerModel> customerFilteredList = new ArrayList<>();
        if (leadType.equalsIgnoreCase(RETAILER)) {
            customerFilteredList = networkService.getCustomerListFromSubArea(subArea, site).stream().filter(sclCustomerModel -> sclCustomerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))
                    .filter(sclCustomerModel -> sclCustomerModel.getCustomerNo() != null).collect(Collectors.toList());
        } else if (leadType.equalsIgnoreCase(DEALER)) {
            customerFilteredList = networkService.getCustomerListFromSubArea(subArea, site).stream().filter(sclCustomerModel -> sclCustomerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))
                    .filter(sclCustomerModel -> sclCustomerModel.getCustomerNo() != null).collect(Collectors.toList());
        }

        for (SclCustomerModel customerModel : customerFilteredList) {
            double sum=0.0;
            if (month != 0 && year != 0) {
                SalesPerformNetworkDetailsData dealerCurrentNetworkData = new SalesPerformNetworkDetailsData();
                Date startDate = getFirstDateOfMonth(month, year);
                Date endDate = getLastDateOfMonth(month, year);
               // SclCustomerModel customerModelAndOrdersForSpecificDate = sclUserDao.getCustomerModelAndOrdersForSpecificDate(customerModel.getUid(), subArea, site, startDate, endDate);
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
                double target = sclUserDao.getCustomerTarget(customerModel.getCustomerNo(), subArea, SclDateUtility.getMonth(new Date()), SclDateUtility.getYear(new Date()));
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
    public SalesPerformNetworkDetailsListData getListOfAllDealerRetailerInfluencers(String fields, BaseSiteModel currentBaseSite, String leadType, String searchKey,List<String> territoryList,List<String> subAreaList,List<String> districtList) {
        SalesPerformNetworkDetailsListData dealerCurrentNetworkListData = new SalesPerformNetworkDetailsListData();
        List<SalesPerformNetworkDetailsData> currentNetworkWsDataList = new ArrayList<>();
        List<TerritoryMasterModel> territoryMasterModels= (List<TerritoryMasterModel>) territoryMasterService.getCurrentTerritory();
        LOGGER.info(String.format("territoryMasterModels:: %s",territoryMasterModels));

        List<SclCustomerModel> customerFilteredList = salesPerformanceDao.getCurrentNetworkCustomers(leadType, null, territoryMasterModels, searchKey, true,false,false);
        if(CollectionUtils.isNotEmpty(customerFilteredList)) {
            LOGGER.info(String.format("Dealer-customer List size:%s", customerFilteredList.size()));
        }else{
            LOGGER.info(String.format("Dealer-customer List size is empty"));
        }
        if(CollectionUtils.isNotEmpty(customerFilteredList)){
            List<SalesPerformNetworkDetailsData> convertedList = salesPerformnceCustomerDetailsConverter.convertAll(customerFilteredList);
            /*List<DealerCurrentNetworkData> convertedList = currentNetworkDetailsConverter.convertAll(currentNetworkCustomers);
            currentNetworkData.addAll(convertedList.stream().sorted(Comparator.comparing(DealerCurrentNetworkData::getName)).collect(Collectors.toList()));
            listData.setDealerCurrentNetworkList(currentNetworkData);*/

            currentNetworkWsDataList.addAll(convertedList);
        }
        currentNetworkWsDataList.sort(Comparator.comparing(SalesPerformNetworkDetailsData::getName));
        dealerCurrentNetworkListData.setNetworkDetails(currentNetworkWsDataList);
        return dealerCurrentNetworkListData;
        /* List<SclCustomerModel> customerFilteredList =getCustomersByLeadType(leadType,territoryList,subAreaList,districtList);
        customerFilteredList=getCustomersByTerritoryCode(customerFilteredList,territoryList);
        if (StringUtils.isNotBlank(searchKey)) {
            customerFilteredList = filterSclCustomersWithSearchTerm(customerFilteredList, searchKey);
        }*/
       /* if (customerFilteredList != null && !customerFilteredList.isEmpty()) {
            for (SclCustomerModel customerModel : customerFilteredList) {
                if (Objects.nonNull(customerModel.getCustomerNo())) {
                    SalesPerformNetworkDetailsData dealerCurrentNetworkData = new SalesPerformNetworkDetailsData();
                    if (customerModel.getUid() != null) {
                        dealerCurrentNetworkData.setCode(customerModel.getUid());
                    }
                    dealerCurrentNetworkData.setName(customerModel.getName());
                    dealerCurrentNetworkData.setPotential(Objects.nonNull(customerModel.getCounterPotential()) ? String.valueOf(customerModel.getCounterPotential()) : ZERO);

                        dealerCurrentNetworkData.setCounterSharePercentage(getCounterShareForDealer(customerModel,LocalDate.now().getMonthValue(), LocalDate.now().getYear()));
                    double target =getMonthlySalesTargetForDealer(customerModel,baseSiteService.getCurrentBaseSite(),null);
                       // double target = sclUserDao.getCustomerTarget(customerModel.getUid(), SclDateUtility.getMonth(new Date()), SclDateUtility.getYear(new Date()));
                        LOGGER.info(String.format("Target ::%s", target));
                        dealerCurrentNetworkData.setTarget(String.valueOf(target));
                        dealerCurrentNetworkData.setSalesQuantity(setSalesQuantityForCustomer(customerModel, leadType, territoryList));
                        if (customerModel.getCustomerNo() != null) {
                            double totalOutstanding = djpVisitDao.getDealerOutstandingAmount(customerModel.getCustomerNo());
                            //dealerCurrentNetworkData.setOutstandingAmount(String.valueOf(totalOutstanding));
                            dealerCurrentNetworkData.setOutstandingAmount(df.format(totalOutstanding));
                        }

                        dealerCurrentNetworkData.setGrowthRateYoYPercentage(df.format(getYearToYearGrowthForDealer(customerModel)));
                        //dealerCurrentNetworkData.setDaysSinceLastOrder(String.valueOf(getDaysFromLastOrder(customerModel.getOrders())));
                        dealerCurrentNetworkData.setDaysSinceLastOrder(getDaysSinceLastLiftingInfForDealer(customerModel));

                    List<List<Object>> obj = territoryMasterService.getDistrictAndTalukaForCustomer(customerModel);
                    if (obj != null && !obj.isEmpty()) {
                        for (List<Object> list : obj) {
                            if (list.get(0) != null) {
                                dealerCurrentNetworkData.setTaluka(StringUtils.isNotBlank(String.valueOf(list.get(0))) ? String.valueOf(list.get(0)) : "");
                            }
                            if (list.get(1) != null) {
                                dealerCurrentNetworkData.setDistrict(StringUtils.isNotBlank(String.valueOf(list.get(1))) ? String.valueOf(list.get(1)) : "");
                            }
                        }
                    }
                    currentNetworkWsDataList.add(dealerCurrentNetworkData);
                }
            }
        }
       /* if (leadType.equalsIgnoreCase(RETAILER)) {
            computeRankForRetailer(currentNetworkWsDataList);
        } else if (leadType.equalsIgnoreCase(DEALER)) {
            computeRankForDealer(currentNetworkWsDataList);
        }
       currentNetworkWsDataList= currentNetworkWsDataList.stream().sorted(Comparator.comparing(SalesPerformNetworkDetailsData::getRank)).collect(Collectors.toList());*/
    }

    @Override
    public SalesPerformNetworkDetailsListData getListOfAllRetailerInfluencersForDealer(String fields, BaseSiteModel currentBaseSite, String leadType, String searchKey, List<String> doList, List<String> subAreaList) {
        {
            SalesPerformNetworkDetailsListData dealerCurrentNetworkListData = new SalesPerformNetworkDetailsListData();
            List<SalesPerformNetworkDetailsData> currentNetworkWsDataList = new ArrayList<>();
            List<SclCustomerModel> customerFilteredList = new ArrayList<>();
            SclCustomerModel currentUser = (SclCustomerModel) userService.getCurrentUser();
            if(currentUser != null) {
                if (currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
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
            for (SclCustomerModel customerModel : customerFilteredList) {
                LOGGER.info(String.format("Customer List uid:%s",customerModel.getUid()));
            }

            if (StringUtils.isNotBlank(searchKey)) {
                customerFilteredList = filterSclCustomersWithSearchTerm(customerFilteredList, searchKey);
            }

            List<SalesPerformNetworkDetailsData> detailedSummaryListData=new ArrayList<>();
            if(leadType.equalsIgnoreCase(RETAILER)) {
                detailedSummaryListData = getRetailerDetailedSummaryListDataForSP(customerFilteredList);
            }else if(leadType.equalsIgnoreCase(INFLUENCER)){
                detailedSummaryListData = getInfluencerDetailedSummaryListData(customerFilteredList,null,null);
            }
            detailedSummaryListData.sort(Comparator.comparing(SalesPerformNetworkDetailsData::getName));
            dealerCurrentNetworkListData.setNetworkDetails(detailedSummaryListData);
            /*if (customerFilteredList != null && !customerFilteredList.isEmpty()) {
                for (SclCustomerModel customerModel : customerFilteredList) {
                    SalesPerformNetworkDetailsData dealerCurrentNetworkData = new SalesPerformNetworkDetailsData();
                    dealerCurrentNetworkData.setCode(customerModel.getUid());
                    dealerCurrentNetworkData.setName(customerModel.getName());
                    if(customerModel.getCounterPotential()!=null) {
                        double potential = (customerModel.getCounterPotential() * 1000) / 50;
                        dealerCurrentNetworkData.setPotential(String.valueOf(potential));
                    }
                    else{
                        dealerCurrentNetworkData.setPotential(ZERO);
                    }

                    double counterShareRetailerForDealer = 0.0, current = 0.0, lastYear = 0.0;
                    if (leadType.equalsIgnoreCase(RETAILER)) {
                        SclCustomerModel orderRequisitionSalesDataForRetailer = salesPerformanceDao.getRetailerSalesForDealer(customerModel, baseSiteService.getCurrentBaseSite());
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

                  *//*  List<SubAreaMasterModel> subAreaMasterModelList = territoryManagementService.getTerritoriesForCustomer(customerModel);
                    if (CollectionUtils.isNotEmpty(subAreaMasterModelList) && subAreaMasterModelList.get(0) != null) {
                        if (subAreaMasterModelList.get(0).getDistrict() != null && subAreaMasterModelList.get(0).getTaluka() != null) {
                            dealerCurrentNetworkData.setDistrict(subAreaMasterModelList.get(0).getDistrict());
                            dealerCurrentNetworkData.setTaluka(subAreaMasterModelList.get(0).getTaluka());
                        }
                    }*//*
                    List<List<Object>> obj = territoryMasterService.getDistrictAndTalukaForCustomer(customerModel);
                    if (obj!=null && !obj.isEmpty()) {
                        for (List<Object> list : obj) {
                            if(list.get(0)!=null){
                                dealerCurrentNetworkData.setTaluka(StringUtils.isNotBlank(String.valueOf(list.get(0))) ? String.valueOf(list.get(0)) : "");
                            }
                            if(list.get(1)!=null) {
                                dealerCurrentNetworkData.setDistrict(StringUtils.isNotBlank(String.valueOf(list.get(1))) ? String.valueOf(list.get(1)) : "");
                            }
                        }
                    }
                    currentNetworkWsDataList.add(dealerCurrentNetworkData);
                }
            }
            LOGGER.info(String.format("Customer List before set into list:%s",currentNetworkWsDataList.size()));*/
            /*if (leadType.equalsIgnoreCase(RETAILER)) {
                computeRankForRetailer(currentNetworkWsDataList);
            } else if (leadType.equalsIgnoreCase(DEALER)) {
                computeRankForDealer(currentNetworkWsDataList);
            }*/

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
    public List<SclCustomerModel> filterSclCustomersWithSearchTerm(List<SclCustomerModel> customers, String searchTerm) {
        return customers.stream().filter(user -> containsIgnoreCase(user.getName(), searchTerm) || containsIgnoreCase(user.getUid() , searchTerm) || (user.getInfluencerType()!=null && containsIgnoreCase(String.valueOf(user.getInfluencerType()),searchTerm))).collect(Collectors.toList());
    }

    @Override
    public Double getActualDealerCounterShareMarket(SclCustomerModel dealer, BaseSiteModel site, List<String> doList, List<String> subAreaList) {
        var totalSales = getCurrentMonthSaleQty(dealer.getCustomerNo(),  doList);
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

   /* private Double getCounterShareForDealer(SclCustomerModel dealer, List<String> territoryList) {
        double totalSales = getCurrentMonthSaleQty(dealer.getUid(),  territoryList);
        LOGGER.info(String.format("Total Sales %s",String.valueOf(totalSales)));
        if (Objects.nonNull(dealer.getCounterPotential()) && dealer.getCounterPotential() > 0.0) {
            LOGGER.info(String.format("Dealer Counter Potential %s",String.valueOf(dealer.getCounterPotential())));
            LOGGER.info(String.format("Dealer Counter Share %s",String.valueOf(totalSales * 100 / dealer.getCounterPotential())));
            return (totalSales * 100 / dealer.getCounterPotential());
        }
        return 0.0;
    }*/

    public Double getCounterShareForDealer(SclCustomerModel dealer,int month,int year) {
        CounterShareData counterShareData=new CounterShareData();
        counterShareData.setDealerCode(dealer.getUid());
        counterShareData.setMonth(month);
        counterShareData.setYear(year);
        CounterShareResponseData responseData = getCounterShareData(counterShareData);
        LOGGER.info(" Counter Share"+responseData.getCounterShare() + " Potential:"+responseData.getPotential()+ " Numerator:"+responseData.getNumeratorSales()+ " SelfBrandSale"+responseData.getSelfBrandSale()+ " TotalSales "+responseData.getTotalSales());
        return responseData.getCounterShare();
    }

    @Override
    public List<Map<String, Object>> getLastLiftingDateAndQtyForCustomers(String sclCustomerCode,String leadType) {
        List<Map<String,Object>> list=new ArrayList<>();
        try {
            SclCustomerModel customerModel = null;
            if(sclCustomerCode!=null) {
                customerModel = (SclCustomerModel) userService.getUserForUID(sclCustomerCode);
            }
            if(customerModel!=null) {
                if ((customerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID)))) {
                    Map<String, Object> maxInvoicedDateAndQunatityDeliveryItem = salesPerformanceDao.findMaxInvoicedDateAndQunatityDeliveryItem(customerModel);
                    list.add(maxInvoicedDateAndQunatityDeliveryItem);
                } else if ((customerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))) {
                    Map<String, Object> maxInvoicedDateAndQuantityForRetailer=new HashMap<>();
                    if(customerModel.getGroups().contains(getUserService().getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                        maxInvoicedDateAndQuantityForRetailer = salesPerformanceDao.findMaxInvoicedDateAndQuantityForRetailer(customerModel, (SclCustomerModel) userService.getCurrentUser());
                    }else{
                        maxInvoicedDateAndQuantityForRetailer = salesPerformanceDao.findMaxInvoicedDateAndQuantityForRetailer(customerModel, null);
                    }
                    list.add(maxInvoicedDateAndQuantityForRetailer);
                } else
                    return null;
            }
        }catch (Exception e){
            LOGGER.info("Exception to get last lifting date and qty for retatiler and dealer:"+e.getMessage());
            return null;
        }
        return list;
    }

    @Override
    public String getDaysFromLastOrder(SclCustomerModel sclCustomerModel) {
            Date lastOrderDate = null;
            if(sclCustomerModel.getLastLiftingDate()!=null){
                lastOrderDate=sclCustomerModel.getLastLiftingDate();
            }
            String numberOfDays=null;
            if(lastOrderDate!=null) {
                Date currentDate = new Date(System.currentTimeMillis());
                LocalDateTime current = LocalDateTime.ofInstant(currentDate.toInstant(), ZoneId.systemDefault());
                LocalDateTime last = LocalDateTime.ofInstant(lastOrderDate.toInstant(), ZoneId.systemDefault());
                LOGGER.info(String.format("Getting Number Of Days last :: %s current :: %s ", last, current));
                numberOfDays = String.valueOf((int) ChronoUnit.DAYS.between(last, current));
            }else{
                numberOfDays="0";
            }
            return numberOfDays;
    }

    @Override
    public List<DealerCurrentNetworkData> getDealerDetailedSummaryListData(List<SclCustomerModel> dealerList) {
        List<List<Object>> outstandingAmount = djpVisitDao.getDealerOutstandingAmount(dealerList);
        Map<String, Double>  mapOutstanding = outstandingAmount.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                .collect(Collectors.toMap(each->((String)each.get(0)), each->(Double)each.get(1)));

        String monthName = Month.of(LocalDate.now().getMonthValue()).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        String formattedMonth = monthName.concat("-").concat(String.valueOf(LocalDate.now().getYear()));
        List<List<Object>> targetList = salesPerformanceDao.getMonthlySalesTargetForDealerList(dealerList, formattedMonth,String.valueOf(LocalDate.now().getYear()));
        Map<String, Double>  mapTarget = targetList.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                .collect(Collectors.toMap(each->((String)each.get(0)), each->(Double)each.get(1)));

        List<List<Object>> list = sclSalesSummaryDao.getSalesMTDforDealer(dealerList, LocalDate.now().getMonthValue(), LocalDate.now().getYear());
        Map<String, Double>  map = list.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                .collect(Collectors.toMap(each->((String)each.get(0)), each->(Double)each.get(1)));

        List<List<Object>> listLastMonth = sclSalesSummaryDao.getSalesMTDforDealer(dealerList, LocalDate.now().minusMonths(1).getMonthValue(), LocalDate.now().getYear());
        Map<String, Double>  mapLastMonth = listLastMonth.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                .collect(Collectors.toMap(each->(String)each.get(0), each->(Double)each.get(1)));

        LocalDate currentYearCurrentDate= LocalDate.now();
        LocalDate currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear(), Month.APRIL, 1);
        if(currentYearCurrentDate.getMonth().compareTo(Month.APRIL)<0) {
            currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear()-1, Month.APRIL, 1);
        }
        LocalDate lastYearCurrentDate= currentYearCurrentDate.minusYears(1);//2022-04-02

        LocalDate lastFinancialYearDate = currentFinancialYearDate.minusYears(1);//2022-04-01

        Map<String, Integer> mapYear = sclSalesSummaryService.currentFySalesDates();
        List<List<Object>> currentYTD = sclSalesSummaryDao.getSalesYTDforDealer(dealerList, mapYear.get(START_MONTH), mapYear.get(START_YEAR),mapYear.get(END_MONTH),mapYear.get(END_YEAR));
        Map<String, Double>  mapCurrentYTD = currentYTD.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                .collect(Collectors.toMap(each->((String)each.get(0)),each->(Double)each.get(1)));

        Map<String, Integer> mapLastYear = sclSalesSummaryService.currentFySalesDates(lastYearCurrentDate.getYear(),lastFinancialYearDate.getYear());
        List<List<Object>> lastYTD = sclSalesSummaryDao.getSalesYTDforDealer(dealerList,  mapLastYear.get(START_MONTH), mapLastYear.get(START_YEAR),mapLastYear.get(END_MONTH),mapLastYear.get(END_YEAR));

        Map<String, Double>  mapLastYTD = lastYTD.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                .collect(Collectors.toMap(each->((String)each.get(0)), each->(Double)each.get(1)));

        List<DealerCurrentNetworkData> summaryDataList = new ArrayList<>();
        dealerList.forEach(dealer -> {
            DealerCurrentNetworkData dealerCurrentNetworkData = new DealerCurrentNetworkData();
            var subAraMappinglist = territoryManagementService.getTerritoriesForCustomer(dealer);
            dealerCurrentNetworkData.setCode(dealer.getUid());
            dealerCurrentNetworkData.setCustomerNo(dealer.getCustomerNo()!=null?dealer.getCustomerNo():"-");
            if(dealer.getContactNumber()!=null){
                dealerCurrentNetworkData.setContactNumber(dealer.getMobileNumber());
            }
            dealerCurrentNetworkData.setName(dealer.getName());
            B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
            double salesMtd=0.0,salesQuantity=0.0,salesQuantityLastMonth=0.0,salesMtdLastMonth=0.0,salesLastYearQty=0.0,salesLastYear=0.0,salesCurrentYearQty=0.0,salesCurrentYear=0.0,totalOutstanding=0.0,targetCurrentMonth=0.0;

            dealerCurrentNetworkData.setPotential(String.valueOf(dealer.getCounterPotential()!=null?dealer.getCounterPotential():"0.0"));
            if(map.containsKey(dealer.getUid())) {
                salesMtd = map.get(dealer.getUid());
            }

            if(mapLastMonth.containsKey(dealer.getUid())) {
                if(mapLastMonth.get(dealer.getUid())!=null){
                    salesMtdLastMonth = mapLastMonth.get(dealer.getUid());
                }
            }
            if(mapTarget.containsKey(dealer.getUid())) {
                if(mapTarget.get(dealer.getUid())!=null){
                    targetCurrentMonth = mapTarget.get(dealer.getUid());
                }
            }

            if(mapOutstanding.containsKey(dealer.getUid())) {
                if(mapOutstanding.get(dealer.getUid())!=null){
                    totalOutstanding = mapOutstanding.get(dealer.getUid());
                }
            }

            salesQuantity = salesMtd;
            salesQuantityLastMonth = salesMtdLastMonth ;

            SalesQuantityData sales = new SalesQuantityData();
            sales.setActual(salesQuantity);
            sales.setCurrent(salesQuantity);
            sales.setLastMonth(salesQuantityLastMonth);
            dealerCurrentNetworkData.setSalesQuantity(sales);

            if (dealer.getLastLiftingDate() != null) {
               dealerCurrentNetworkData.setDaySinceLastOrder(getDaysFromLastOrder(dealer));
            } else {
                dealerCurrentNetworkData.setDaySinceLastOrder(String.valueOf("0"));
            }

            if(mapCurrentYTD.containsKey(dealer.getUid())) {
                salesCurrentYear = mapCurrentYTD.get(dealer.getUid());
            }

            if(mapLastYTD.containsKey(dealer.getUid())) {
                salesLastYear = mapLastYTD.get(dealer.getUid());
            }
            salesCurrentYearQty = (salesCurrentYear);
            salesLastYearQty = (salesLastYear);

            dealerCurrentNetworkData.setSalesYtd(df.format(salesCurrentYearQty));
            dealerCurrentNetworkData.setGrowthRate(df.format(getYearToYearGrowth(salesCurrentYearQty,salesLastYearQty)));
            if(CollectionUtils.isNotEmpty(subAraMappinglist)) {
                var subareaMaster=subAraMappinglist.get(0);
                dealerCurrentNetworkData.setDistrict(subareaMaster.getDistrict());
                dealerCurrentNetworkData.setTaluka(subareaMaster.getTaluka());
            }

            dealerCurrentNetworkData.setTarget(df.format(targetCurrentMonth));

            dealerCurrentNetworkData.setOutstandingAmount(df.format(totalOutstanding));

            dealerCurrentNetworkData.setCounterShare(String.valueOf(salesPerformanceService.getCounterShareForDealer(dealer,LocalDate.now().getMonthValue(), LocalDate.now().getYear())));

            summaryDataList.add(dealerCurrentNetworkData);
        });
        //AtomicInteger rank=new AtomicInteger(1);
        if(CollectionUtils.isNotEmpty(summaryDataList)) {
            return summaryDataList.stream().filter(obj->Objects.nonNull(obj.getName())).sorted(Comparator.comparing(DealerCurrentNetworkData::getName)).collect(Collectors.toList());
        }else{
            return  summaryDataList;
        }
    }

    @Override
    public List<DealerCurrentNetworkData> getRetailerDetailedSummaryListData(List<SclCustomerModel> retailerList) { List<List<Object>> list= new ArrayList<>();
        List<List<Object>> listLastMonth= new ArrayList<>();
        List<List<Object>> lastYTD=new ArrayList<>();
        List<List<Object>> currentYTD=new ArrayList<>();

        Map<String, Double>  map=new HashMap<>();
        Map<String, Double>  mapLastMonth=new HashMap<>();
        Map<String, Double>  mapCurrentYTD=new HashMap<>();
        Map<String, Double>  mapLastYTD=new HashMap<>();

        String startDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).toString();
        String endDate = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).plusDays(1).toString();

        String startDateLastMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).minusMonths(1).toString();
        String endDateLastMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).minusMonths(1).plusDays(1).toString();

        LocalDate currentYearCurrentDate= LocalDate.now().plusDays(1);
        LocalDate currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear(), Month.APRIL, 1);
        if(currentYearCurrentDate.getMonth().compareTo(Month.APRIL)<0) {
            currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear()-1, Month.APRIL, 1);
        }
        LocalDate lastYearCurrentDate= currentYearCurrentDate.minusYears(1).plusDays(1);//2022-04-02

        LocalDate lastFinancialYearDate = currentFinancialYearDate.minusYears(1);//2022-04-01

        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();

        if(currentUser instanceof SclUserModel){
            list = networkService.getSalesForRetailerList(retailerList, null,startDate,endDate);

            listLastMonth = networkService.getSalesForRetailerList(retailerList, null,startDateLastMonth,endDateLastMonth);


            map = list.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                    .collect(Collectors.toMap(each->((SclCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));
            mapLastMonth = listLastMonth.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                    .collect(Collectors.toMap(each->((SclCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));


            currentYTD = networkService.getSalesForRetailerList(retailerList, null,currentFinancialYearDate.toString(), currentYearCurrentDate.toString());
            mapCurrentYTD = currentYTD.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                    .collect(Collectors.toMap(each->((SclCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));

            lastYTD = networkService.getSalesForRetailerList(retailerList, null,lastFinancialYearDate.toString(), lastYearCurrentDate.toString());

            mapLastYTD = lastYTD.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                    .collect(Collectors.toMap(each->((SclCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));

        }else if((currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))){
            list = networkService.getSalesForRetailerList(retailerList, null,startDate,endDate);
            listLastMonth = networkService.getSalesForRetailerList(retailerList, null,startDateLastMonth,endDateLastMonth);
            map = list.stream().filter(each->each!=null && each.size()>2 && each.get(0)!=null && each.get(2)!=null)
                    .collect(Collectors.toMap(each->((SclCustomerModel)each.get(0)).getUid(), each->(Double)each.get(2)));
            mapLastMonth = listLastMonth.stream().filter(each->each!=null && each.size()>2 && each.get(0)!=null && each.get(2)!=null)
                    .collect(Collectors.toMap(each->((SclCustomerModel)each.get(0)).getUid(), each->(Double)each.get(2)));

            currentYTD = networkService.getSalesForRetailerList(retailerList, null,currentFinancialYearDate.toString(), currentYearCurrentDate.toString());
            mapCurrentYTD = currentYTD.stream().filter(each->each!=null && each.size()>2 && each.get(0)!=null && each.get(2)!=null)
                    .collect(Collectors.toMap(each->((SclCustomerModel)each.get(0)).getUid(), each->(Double)each.get(2)));

            lastYTD = networkService.getSalesForRetailerList(retailerList, null,lastFinancialYearDate.toString(), lastYearCurrentDate.toString());

            mapLastYTD = lastYTD.stream().filter(each->each!=null && each.size()>2 && each.get(0)!=null && each.get(2)!=null)
                    .collect(Collectors.toMap(each->((SclCustomerModel)each.get(0)).getUid(), each->(Double)each.get(2)));

        }
        else if(currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))){
            list = networkService.getSalesForRetailerList(retailerList, (SclCustomerModel) currentUser,startDate,endDate);
            listLastMonth = networkService.getSalesForRetailerList(retailerList, (SclCustomerModel) currentUser,startDateLastMonth,endDateLastMonth);

            map = list.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                    .collect(Collectors.toMap(each->((SclCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));
            mapLastMonth = listLastMonth.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                    .collect(Collectors.toMap(each->((SclCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));

            currentYTD = networkService.getSalesForRetailerList(retailerList, (SclCustomerModel) currentUser,currentFinancialYearDate.toString(), currentYearCurrentDate.toString());
            mapCurrentYTD = currentYTD.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                    .collect(Collectors.toMap(each->((SclCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));

            lastYTD = networkService.getSalesForRetailerList(retailerList, (SclCustomerModel) currentUser,lastFinancialYearDate.toString(), lastYearCurrentDate.toString());

            mapLastYTD = lastYTD.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                    .collect(Collectors.toMap(each->((SclCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));

        }

        List<List<Object>> monthlySalesTargetForRetailer = getMonthlySalesTargetForRetailer(retailerList);
        Map<String,Double> mapTarget = monthlySalesTargetForRetailer.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                .collect(Collectors.toMap(each->((String)each.get(0)), each->(Double)each.get(1)));


        Map<String, Double> finalMap = map;
        Map<String, Double> finalMapLastMonth = mapLastMonth;
        Map<String, Double> finalMapCurrentYTD = mapCurrentYTD;
        Map<String, Double> finalMapLastYTD = mapLastYTD;

        List<DealerCurrentNetworkData> summaryDataList = new ArrayList<>();
        retailerList.forEach(retailer -> {
            DealerCurrentNetworkData dealerCurrentNetworkData = new DealerCurrentNetworkData();
            var subAraMappinglist = territoryManagementService.getTerritoriesForCustomer(retailer);
            dealerCurrentNetworkData.setCode(retailer.getUid());
            if(retailer.getContactNumber()!=null){
                dealerCurrentNetworkData.setContactNumber(retailer.getContactNumber());
            }
            dealerCurrentNetworkData.setName(retailer.getName());
            dealerCurrentNetworkData.setCustomerNo(retailer.getCustomerNo());

            double salesMtd=0.0,salesMtdLastMonth=0.0,salesLastYear=0.0,salesCurrentYear=0.0,target=0.0;

            if(!(currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))) {
                if (retailer.getCounterPotential() != null) {
                    dealerCurrentNetworkData.setPotential(String.valueOf(retailer.getCounterPotential()));
                } else {
                    dealerCurrentNetworkData.setPotential("0");
                }
            }else {
                if (retailer.getCounterPotential() != null) {
                    dealerCurrentNetworkData.setPotential(String.valueOf((retailer.getCounterPotential() * 1000) / 50));
                } else {
                    dealerCurrentNetworkData.setPotential("0");
                }
            }

            if(finalMap.containsKey(retailer.getUid())){
                salesMtd+=finalMap.get(retailer.getUid());
            }
            if(finalMapLastMonth.containsKey(retailer.getUid())){
                salesMtdLastMonth+=finalMapLastMonth.get(retailer.getUid());
            }
            if(finalMapCurrentYTD.containsKey(retailer.getUid())){
                salesCurrentYear+=finalMapCurrentYTD.get(retailer.getUid());
            }
            if(finalMapLastYTD.containsKey(retailer.getUid())){
                salesLastYear+=finalMapLastYTD.get(retailer.getUid());
            }
            if(mapTarget.containsKey(retailer.getUid())){
                target=mapTarget.get(retailer.getUid());
            }

            SalesQuantityData sales = new SalesQuantityData();
            sales.setRetailerSaleQuantity(salesMtd);
            sales.setCurrent(salesMtd);
            sales.setLastMonth(salesMtdLastMonth);
            sales.setLastYear(salesLastYear);
            sales.setActual(salesMtd);

            dealerCurrentNetworkData.setSalesQuantity(sales);

            dealerCurrentNetworkData.setSalesYtd(df.format(salesCurrentYear));
            dealerCurrentNetworkData.setGrowthRate(df.format(getYearToYearGrowth(salesCurrentYear,salesLastYear)));
            dealerCurrentNetworkData.setYoyGrowth(df.format(getYearToYearGrowth(salesCurrentYear,salesLastYear)));
            if(retailer.getLastLiftingDate()!=null) {
                dealerCurrentNetworkData.setDaySinceLastOrder(salesPerformanceService.getDaysFromLastOrder(retailer));
            }else{
                dealerCurrentNetworkData.setDaySinceLastOrder("-");
            }
            if(CollectionUtils.isNotEmpty(subAraMappinglist)) {
                var subareaMaster=subAraMappinglist.get(0);
                dealerCurrentNetworkData.setDistrict(subareaMaster.getDistrict());
                dealerCurrentNetworkData.setTaluka(subareaMaster.getTaluka());
            }
            //       String monthName = Month.of(LocalDate.now().getMonthValue()).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            dealerCurrentNetworkData.setTarget(String.valueOf(target));
            dealerCurrentNetworkData.setCounterShare(String.valueOf(salesPerformanceService.getCounterShareForDealer(retailer,LocalDate.now().getMonthValue(), LocalDate.now().getYear())));
            summaryDataList.add(dealerCurrentNetworkData);
        });
       /* AtomicInteger rank=new AtomicInteger(1);
        summaryDataList.stream().sorted(Comparator.comparing(nw -> nw.getSalesQuantity().getRetailerSaleQuantity())).forEach(data->data.setRank(String.valueOf(rank.getAndIncrement())));*/
        return summaryDataList;
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

    private Double getYearToYearGrowthForDealer1(String customerCode) {
        LocalDate currentYearCurrentDate = LocalDate.now();//2023-11-02
        LocalDate currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear(), Month.APRIL, 1);//2023-04-01
        if (currentYearCurrentDate.getMonth().compareTo(Month.APRIL) < 0) {
            currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear() - 1, Month.APRIL, 1);
        }
        LocalDate lastYearCurrentDate = LocalDate.now().minusYears(1);//2022-11-02

        LocalDate lastFinancialYearDate = currentFinancialYearDate.minusYears(1);//2022-04-01

        LOGGER.info(String.format("Getting yoy growth for customerNo :: %s currentYearCurrentDate :: %s currentFinancialYearDate :: %s lastFinancialYearDate :: %s", customerCode, currentYearCurrentDate, currentFinancialYearDate, lastFinancialYearDate));
        double lastSale = sclUserDao.getSalesQuantity(customerCode, getStringDate(lastFinancialYearDate), getStringDate(lastYearCurrentDate));
        double currentSale = sclUserDao.getSalesQuantity(customerCode, getStringDate(currentFinancialYearDate), getStringDate(currentYearCurrentDate));
        LOGGER.info(String.format("For CustomerNo :: %s lastSale :: %s currentSale :: %s", customerCode, lastSale, currentSale));
       /* if (lastSale > 0) {
            return (((lastSale - currentSale) / lastSale) * 100);
        }*/
        if (currentSale > 0 && lastSale > 0) {
            return (((currentSale - lastSale) / lastSale) * 100);
        }
        return 0.0;
    }

    public Double getYearToYearGrowthForDealer(SclCustomerModel customerModel) {
        double currentFySales = sclSalesSummaryService.getCurrentFySales(customerModel, Collections.emptyList());
        double lastFYSales = sclSalesSummaryService.getLastFYSales(customerModel, null);
        LOGGER.info(String.format("For CustomerNo :: %s lastSale :: %s currentSale :: %s", customerModel.getUid(), lastFYSales, currentFySales));
        if (currentFySales > 0 && lastFYSales > 0) {
            return (((currentFySales - lastFYSales) / lastFYSales) * 100);
        }
        return 0.0;
    }


    private Double getYearToYearGrowthRetailerForDealer(SclCustomerModel customerModel) {

        LocalDate currentYearCurrentDate = LocalDate.now();
        LocalDate currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear(), Month.APRIL, 1);
        if (currentYearCurrentDate.getMonth().compareTo(Month.APRIL) < 0) {
            currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear() - 1, Month.APRIL, 1);
        }
        LocalDate lastYearCurrentDate = LocalDate.now().minusYears(1);
        LocalDate lastFinancialYearDate = currentFinancialYearDate.minusYears(1);
        double lastSale=0.0,currentSale=0.0;
        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
        List<SclCustomerModel> dealer=new ArrayList<>();
        dealer.add((SclCustomerModel) currentUser);
        if(currentUser != null && currentUser instanceof  SclCustomerModel) {
            Map<String, Double> salesQtyLast = networkService.getSalesQuantityForRetailerByDate(customerModel, lastFinancialYearDate.toString(), lastYearCurrentDate.toString(), dealer, null);
            if(salesQtyLast!=null)
                lastSale = salesQtyLast.get("quantityInBags");
            //lastSale = salesPerformanceDao.getRetailerFromOrderReq(customerModel, getDateToDate(lastFinancialYearDate), getDateToDate(lastYearCurrentDate));
            LOGGER.info("Last Sale:::" + lastSale);

            Map<String, Double> salesQtyCurrent = networkService.getSalesQuantityForRetailerByDate(customerModel, currentFinancialYearDate.toString(), currentYearCurrentDate.toString(), dealer, null);
            if(salesQtyCurrent!=null)
                currentSale = salesQtyCurrent.get("quantityInBags");
            //currentSale = salesPerformanceDao.getRetailerFromOrderReq(customerModel, getDateToDate(currentFinancialYearDate), getDateToDate(currentYearCurrentDate));
            LOGGER.info("Current Sale:::" + currentSale);
        }
        if ((lastSale) > 0) {
            return ((((currentSale) - (lastSale)) / (lastSale)) * 100);
       }
       return 0.0;
    }

    private double getCounterShareForRetailer(SclCustomerModel customerModel, List<NirmanMitraSalesHistoryModel> salesHistry) {
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

    private double getCounterShareForRetailerFromOR(SclCustomerModel customerModel, List<SclCustomerModel> saleQty) {
        Double saleQuantity=0.0;
        LocalDate currentDate = LocalDate.now();
        LocalDate of = LocalDate.of(currentDate.getYear(), currentDate.getMonth(), 1);
        LocalDate of1 = LocalDate.of(currentDate.getYear(), currentDate.getMonth(), currentDate.lengthOfMonth());
        Date startDate = Date.from(of.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(of1.atStartOfDay(ZoneId.systemDefault()).toInstant());
        for (SclCustomerModel sclCustomerModel : saleQty) {
            OrderRequisitionModel sclCustomerFromOrderReq = salesPerformanceDao.getSclCustomerFromOrderReq(sclCustomerModel);
            saleQuantity += sclCustomerFromOrderReq.getQuantity();
        }
            var counterPotential = customerModel.getCounterPotential();
            if (Objects.nonNull(counterPotential)) {
                return (saleQuantity * 100) / counterPotential;
            }
            else {
                return 0.0;
            }
    }
  private double getCounterShareRetailerForDealer(SclCustomerModel customerModel) {
        Double saleQuantity=0.0;

        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
        List<SclCustomerModel> dealer=new ArrayList<>();
        dealer.add((SclCustomerModel) currentUser);
        if(currentUser instanceof SclCustomerModel) {
            LOGGER.info(String.format("Customer id:%s", String.valueOf(customerModel.getUid())));
            Map<String, Double> salesQuantityForRetailerByMTD = networkService.getSalesQuantityForRetailerByMTD(customerModel,dealer, null);
            if(salesQuantityForRetailerByMTD!=null){
                 saleQuantity = salesQuantityForRetailerByMTD.get("quantityInBags");
            }
            //saleQuantity = salesPerformanceDao.getRetailerFromOrderReq(customerModel, startDate, endDate);
        }
        LOGGER.info(String.format("Customer id:%s,%s",customerModel.getUid(),String.valueOf(saleQuantity)));
        var counterPotential = (customerModel.getCounterPotential()!=null?customerModel.getCounterPotential():0.0)*20;
        LOGGER.info(String.format("Customer potential:%s",String.valueOf(customerModel.getCounterPotential()!=null?customerModel.getCounterPotential():0.0)));
        if(counterPotential!=0.0)
            //return    ((saleQuantity/20) * 100) / (counterPotential/20);
        return    (saleQuantity / counterPotential);
        else
            return 0.0;
    }

    private double getCounterShareForRetailerFromORMonthYear(SclCustomerModel customerModel, List<OrderRequisitionModel> saleQty, int month, int year) {
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

    private double getCounterShareForRetailerYTDFromOR(SclCustomerModel customerModel, List<OrderRequisitionModel> saleQty) {
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
    public SalesPerformNetworkDetailsListData getListOfAllZeroLifting(String fields, BaseSiteModel site, String customerType, String searchKey, List<String> territoryList,List<String> subAreaList,List<String> districtList) {
            SalesPerformNetworkDetailsListData dealerCurrentNetworkListData = new SalesPerformNetworkDetailsListData();
            List<SalesPerformNetworkDetailsData> currentNetworkWsDataList = new ArrayList<>();

            List<SalesPerformNetworkDetailsData>  collect;
            List<SalesPerformNetworkDetailsData> data = new ArrayList<>();

			//List<SclCustomerModel> sclCustomerFilteredList = getCustomersByLeadType(customerType,territoryList,subAreaList,districtList);
           //sclCustomerFilteredList=getCustomersByTerritoryCode(sclCustomerFilteredList,territoryList);
        List<TerritoryMasterModel> territoryMasterModels= (List<TerritoryMasterModel>) territoryMasterService.getCurrentTerritory();
        LOGGER.info(String.format("territoryMasterModels:: %s",territoryMasterModels));

        List<SclCustomerModel> customerFilteredList = salesPerformanceDao.getCurrentNetworkCustomers(customerType, null, territoryMasterModels, searchKey, true,true,false);
        if(CollectionUtils.isNotEmpty(customerFilteredList)) {
            LOGGER.info(String.format("Dealer-customer List size for zero lifting:%s", customerFilteredList.size()));
        }else{
            LOGGER.info(String.format("Dealer-customer List size  for zero lifting is empty"));
        }
           /* LOGGER.info(String.format("size of Cust before:%s",customerFilteredList.size()));
            List<SclCustomerModel> customerFilteredList=new ArrayList<>();
            if(sclCustomerFilteredList!=null && !sclCustomerFilteredList.isEmpty()) {
                customerFilteredList = salesPerformanceDao.getSclCustomerZeroLiftingList(sclCustomerFilteredList);
            }
            LOGGER.info(String.format("size of Cust after:%s",customerFilteredList.size()));

            if (StringUtils.isNotBlank(searchKey)) {
                customerFilteredList = filterSclCustomersWithSearchTerm(customerFilteredList, searchKey);
            }

                for (SclCustomerModel customerModel : customerFilteredList) {
                    if (Objects.nonNull(customerModel.getCustomerNo())){

                        SalesPerformNetworkDetailsData dealerCurrentNetworkData = new SalesPerformNetworkDetailsData();

                    dealerCurrentNetworkData.setCode(customerModel.getUid());
                    dealerCurrentNetworkData.setName(customerModel.getName());
                    dealerCurrentNetworkData.setPotential(Objects.nonNull(customerModel.getCounterPotential()) ? String.valueOf(customerModel.getCounterPotential()) : ZERO);

                    if (customerType.equalsIgnoreCase(DEALER)) {
                        dealerCurrentNetworkData.setCounterSharePercentage(getCounterShareForDealer(customerModel,LocalDate.now().getMonthValue(), LocalDate.now().getYear()));
                        //HAVE TO APPLY TARGER tERRITORY LIST
                        double target =getMonthlySalesTargetForDealer(customerModel,baseSiteService.getCurrentBaseSite(),null);
                        //double target = sclUserDao.getCustomerTarget(customerModel.getUid(), SclDateUtility.getMonth(new Date()), SclDateUtility.getYear(new Date()));
                        dealerCurrentNetworkData.setTarget(Objects.nonNull(target) ? String.valueOf(target) : ZERO);

                        dealerCurrentNetworkData.setSalesQuantity(setSalesQuantityForCustomer(customerModel, customerType, territoryList));
                        if (customerModel.getCustomerNo() != null) {
                            double totalOutstanding = djpVisitDao.getDealerOutstandingAmount(customerModel.getCustomerNo());
                            dealerCurrentNetworkData.setOutstandingAmount(df.format(totalOutstanding));
                            //dealerCurrentNetworkData.setOutstandingAmount(amountFormatService.getFormattedValue(totalOutstanding));
                        }

                        dealerCurrentNetworkData.setGrowthRateYoYPercentage(df.format(getYearToYearGrowthForDealer(customerModel)));
                        dealerCurrentNetworkData.setDaysSinceLastOrder(String.valueOf(getDaysFromLastOrder(customerModel.getOrders())));
                    }

                   /* List<List<Object>> list = territoryMasterService.getDistrictAndTalukaForCustomer(customerModel);
                    if (CollectionUtils.isNotEmpty(list)) {
                        dealerCurrentNetworkData.setTaluka(StringUtils.isNotBlank(String.valueOf(list.get(0)))? String.valueOf(list.get(0)) :"");
                        dealerCurrentNetworkData.setDistrict(StringUtils.isNotBlank(String.valueOf(list.get(1)))? String.valueOf(list.get(1)) :"");
                    }
                    List<List<Object>> obj = territoryMasterService.getDistrictAndTalukaForCustomer(customerModel);
                    if (obj != null && !obj.isEmpty()) {
                        for (List<Object> list : obj) {
                            if (list.get(0) != null) {
                                dealerCurrentNetworkData.setTaluka(StringUtils.isNotBlank(String.valueOf(list.get(0))) ? String.valueOf(list.get(0)) : "");
                            }
                            if (list.get(1) != null) {
                                dealerCurrentNetworkData.setDistrict(StringUtils.isNotBlank(String.valueOf(list.get(1))) ? String.valueOf(list.get(1)) : "");
                            }
                        }
                    }
                    currentNetworkWsDataList.add(dealerCurrentNetworkData);
                }
              }*/

            if(CollectionUtils.isNotEmpty(customerFilteredList)){
                List<SalesPerformNetworkDetailsData> convertedList = salesPerformnceCustomerDetailsConverter.convertAll(customerFilteredList);
                currentNetworkWsDataList.addAll(convertedList);
            }

        if (customerType.equalsIgnoreCase(DEALER)) {
            collect = currentNetworkWsDataList.stream().filter(nw -> Objects.nonNull(nw.getSalesQuantity())).filter(nw -> Objects.nonNull(nw.getSalesQuantity().getActual())).filter(nw -> nw.getSalesQuantity().getActual() == 0).collect(Collectors.toList());
            data = collect.stream().sorted(Comparator.comparing(SalesPerformNetworkDetailsData::getName)).collect(Collectors.toList());
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

        List<SclCustomerModel> sclCustomerFilteredList = new ArrayList<>();
        List<SclCustomerModel> customerFilteredList = new ArrayList<>();

        SclCustomerModel currentUser = (SclCustomerModel) userService.getCurrentUser();
        if(currentUser != null) {
            if (currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                if(!currentUser.getCounterType().equals(CounterType.SP)) {
                    if (customerType.equalsIgnoreCase(RETAILER)) {
                        sclCustomerFilteredList = territoryManagementService.getRetailerListForDealer();
                    } else if (customerType.equalsIgnoreCase(INFLUENCER)) {
                        sclCustomerFilteredList = territoryManagementService.getInfluencerListForDealer();
                    }
                }
            }
        }
        if(sclCustomerFilteredList!=null && !sclCustomerFilteredList.isEmpty()) {
            customerFilteredList = salesPerformanceDao.getSclCustomerZeroLiftingList(sclCustomerFilteredList);
        }

        if (StringUtils.isNotBlank(searchKey)) {
            customerFilteredList = filterSclCustomersWithSearchTerm(customerFilteredList, searchKey);
        }

        if (customerFilteredList != null && !customerFilteredList.isEmpty()) {

            for (SclCustomerModel customerModel : customerFilteredList) {

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
                        dealerCurrentNetworkData.setDaysSinceLastOrder("0");
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
                    dealerCurrentNetworkData.setDaySinceLastLifting("0");
                }

               /* List<SubAreaMasterModel> subAreaMasterModelList = territoryManagementService.getTerritoriesForCustomer(customerModel);
                if (CollectionUtils.isNotEmpty(subAreaMasterModelList) && subAreaMasterModelList.get(0) != null) {
                    if (subAreaMasterModelList.get(0).getDistrict() != null && subAreaMasterModelList.get(0).getTaluka() != null) {
                        dealerCurrentNetworkData.setDistrict(subAreaMasterModelList.get(0).getDistrict());
                        dealerCurrentNetworkData.setTaluka(subAreaMasterModelList.get(0).getTaluka());
                    }
                }*/
                List<List<Object>> obj = territoryMasterService.getDistrictAndTalukaForCustomer(customerModel);
                if (obj!=null && !obj.isEmpty()) {
                    for (List<Object> list : obj) {
                        if(list.get(0)!=null){
                            dealerCurrentNetworkData.setTaluka(StringUtils.isNotBlank(String.valueOf(list.get(0))) ? String.valueOf(list.get(0)) : "");
                        }
                        if(list.get(1)!=null) {
                            dealerCurrentNetworkData.setDistrict(StringUtils.isNotBlank(String.valueOf(list.get(1))) ? String.valueOf(list.get(1)) : "");
                        }
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
    public SalesPerformNetworkDetailsListData getListOfAllLowPerforming(String fields, String customerType, String searchKey,  List<String> territoryList,List<String> subAreaList,List<String> districtList) {
       SalesPerformNetworkDetailsListData dealerCurrentNetworkListData = new SalesPerformNetworkDetailsListData();
            List<SalesPerformNetworkDetailsData> currentNetworkWsDataList = new ArrayList<>();
            List<SalesPerformNetworkDetailsData> collect1, collect;
            List<SalesPerformNetworkDetailsData> data = new ArrayList<>();

            /*RequestCustomerData requestData = new RequestCustomerData();
            List<String> counterType = List.of(customerType);
            requestData.setCounterType(counterType);
             List<SclCustomerModel> sclCustomerFilteredList = territoryManagementService.getCustomerforUser(requestData);*/
       /* List<SclCustomerModel> sclCustomerFilteredList = getCustomersByLeadType(customerType, territoryList,subAreaList,districtList);
            LOGGER.info(String.format("Size before last lifting from SCL customer:%s",String.valueOf(sclCustomerFilteredList.size())));
            List<SclCustomerModel> customerFilteredList=getCustomersByTerritoryCode(sclCustomerFilteredList,territoryList);
            if(!customerFilteredList.isEmpty()) {
                customerFilteredList = salesPerformanceDao.getSclCustomerLastLiftingList(customerFilteredList);
            }
            LOGGER.info(String.format("Size after last lifting from SCL customer:%s",String.valueOf(customerFilteredList.size())));*/

             /*if (StringUtils.isNotBlank(searchKey)) {
                customerFilteredList = filterSclCustomersWithSearchTerm(customerFilteredList, searchKey);
            }*/


        List<TerritoryMasterModel> territoryMasterModels= (List<TerritoryMasterModel>) territoryMasterService.getCurrentTerritory();
        LOGGER.info(String.format("territoryMasterModels:: %s",territoryMasterModels));

        List<SclCustomerModel> customerFilteredList = salesPerformanceDao.getCurrentNetworkCustomers(customerType, null, territoryMasterModels, searchKey, true,false,true);
        if(CollectionUtils.isNotEmpty(customerFilteredList)) {
            LOGGER.info(String.format("Dealer-customer List size for zero lifting:%s", customerFilteredList.size()));
        }else{
            LOGGER.info(String.format("Dealer-customer List size  for zero lifting is empty"));
        }
        if (customerFilteredList != null && !customerFilteredList.isEmpty()) {
            if(CollectionUtils.isNotEmpty(customerFilteredList)){
                List<SalesPerformNetworkDetailsData> convertedList = salesPerformnceCustomerDetailsConverter.convertAll(customerFilteredList);
                currentNetworkWsDataList.addAll(convertedList);
            }
        }

          /*      for (SclCustomerModel customerModel : customerFilteredList) {
                    if (Objects.nonNull(customerModel.getCustomerNo())) {
                        SalesPerformNetworkDetailsData dealerCurrentNetworkData = new SalesPerformNetworkDetailsData();

                        dealerCurrentNetworkData.setCode(customerModel.getUid());
                        dealerCurrentNetworkData.setName(customerModel.getName());
                        dealerCurrentNetworkData.setPotential(Objects.nonNull(customerModel.getCounterPotential()) ? String.valueOf(customerModel.getCounterPotential()) : ZERO);

                        if (customerType.equalsIgnoreCase(DEALER)) {
                            dealerCurrentNetworkData.setCounterSharePercentage(getCounterShareForDealer(customerModel,LocalDate.now().getMonthValue(), LocalDate.now().getYear()));
                            double target =getMonthlySalesTargetForDealer(customerModel,baseSiteService.getCurrentBaseSite(),null);
                            //double target = sclUserDao.getCustomerTarget(customerModel.getUid(), SclDateUtility.getMonth(new Date()), SclDateUtility.getYear(new Date()));
                            dealerCurrentNetworkData.setTarget(Objects.nonNull(target) ? String.valueOf(target) : ZERO);

                            dealerCurrentNetworkData.setSalesQuantity(setSalesQuantityForCustomer(customerModel, customerType, territoryList));
                            if (customerModel.getCustomerNo() != null) {
                                double totalOutstanding = djpVisitDao.getDealerOutstandingAmount(customerModel.getCustomerNo());
                                dealerCurrentNetworkData.setOutstandingAmount(df.format(totalOutstanding));
                               // dealerCurrentNetworkData.setOutstandingAmount(amountFormatService.getFormattedValue(totalOutstanding));
                            }

                            dealerCurrentNetworkData.setGrowthRateYoYPercentage(df.format(getYearToYearGrowthForDealer(customerModel)));
                            dealerCurrentNetworkData.setDaysSinceLastOrder(getDaysSinceLastLiftingInfForDealer(customerModel));
                        }

                   *//* List<List<Object>> list = territoryMasterService.getDistrictAndTalukaForCustomer(customerModel);
                    if (CollectionUtils.isNotEmpty(list)) {
                        dealerCurrentNetworkData.setTaluka(StringUtils.isNotBlank(String.valueOf(list.get(0)))? String.valueOf(list.get(0)) :"");
                        dealerCurrentNetworkData.setDistrict(StringUtils.isNotBlank(String.valueOf(list.get(1)))? String.valueOf(list.get(1)) :"");
                    }*//*
                        List<List<Object>> obj = territoryMasterService.getDistrictAndTalukaForCustomer(customerModel);
                        if (obj != null && !obj.isEmpty()) {
                            for (List<Object> list : obj) {
                                if (list.get(0) != null) {
                                    dealerCurrentNetworkData.setTaluka(StringUtils.isNotBlank(String.valueOf(list.get(0))) ? String.valueOf(list.get(0)) : "");
                                }
                                if (list.get(1) != null) {
                                    dealerCurrentNetworkData.setDistrict(StringUtils.isNotBlank(String.valueOf(list.get(1))) ? String.valueOf(list.get(1)) : "");
                                }
                            }
                        }
                        currentNetworkWsDataList.add(dealerCurrentNetworkData);
                    }
                }*/

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
                List<SalesPerformNetworkDetailsData> collect2 = data.stream().limit(((long) data.size() * LOWPERCENTAGE) / 100).collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(collect2)) {
                    dealerCurrentNetworkListData.setNetworkDetails(collect2.stream().sorted(Comparator.comparing(SalesPerformNetworkDetailsData::getName)).collect(Collectors.toList()));
                }
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
        List<SclCustomerModel> sclCustomerFilteredList = new ArrayList<>();
        List<SclCustomerModel> customerFilteredList = new ArrayList<>();

        SclCustomerModel currentUser = (SclCustomerModel) userService.getCurrentUser();
        if(currentUser != null) {
            if (currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                if(!currentUser.getCounterType().equals(CounterType.SP)) {
                    if (customerType.equalsIgnoreCase(RETAILER)) {
                        sclCustomerFilteredList = territoryManagementService.getRetailerListForDealer();
                    } else if (customerType.equalsIgnoreCase(INFLUENCER)) {
                        sclCustomerFilteredList = territoryManagementService.getInfluencerListForDealer();
                    }
                }
            }
        }
        LOGGER.info(String.format("Size before last lifting from SCL customer:%s",String.valueOf(sclCustomerFilteredList.size())));

        if(sclCustomerFilteredList!=null && !sclCustomerFilteredList.isEmpty()) {
            customerFilteredList = salesPerformanceDao.getSclCustomerLastLiftingList(sclCustomerFilteredList);
        }
        LOGGER.info(String.format("Size before last lifting from SCL customer:%s",String.valueOf(customerFilteredList.size())));


        if (StringUtils.isNotBlank(searchKey)) {
            customerFilteredList = filterSclCustomersWithSearchTerm(customerFilteredList, searchKey);
        }

        if (customerFilteredList != null && !customerFilteredList.isEmpty()) {

            for (SclCustomerModel customerModel : customerFilteredList) {

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
                    SclCustomerModel orderRequisitionSalesDataForRetailer = salesPerformanceDao.getRetailerSalesForDealerLowPerform(customerModel, baseSiteService.getCurrentBaseSite());
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
                        dealerCurrentNetworkData.setDaySinceLastLifting("0");
                    }
                }
/*
                List<SubAreaMasterModel> subAreaMasterModelList = territoryManagementService.getTerritoriesForCustomer(customerModel);
                if (CollectionUtils.isNotEmpty(subAreaMasterModelList) && subAreaMasterModelList.get(0) != null) {
                    if (subAreaMasterModelList.get(0).getDistrict() != null && subAreaMasterModelList.get(0).getTaluka() != null) {
                        dealerCurrentNetworkData.setDistrict(subAreaMasterModelList.get(0).getDistrict());
                        dealerCurrentNetworkData.setTaluka(subAreaMasterModelList.get(0).getTaluka());
                    }
                }*/
                List<List<Object>> obj = territoryMasterService.getDistrictAndTalukaForCustomer(customerModel);
                if (obj!=null && !obj.isEmpty()) {
                    for (List<Object> list : obj) {
                        if(list.get(0)!=null){
                            dealerCurrentNetworkData.setTaluka(StringUtils.isNotBlank(String.valueOf(list.get(0))) ? String.valueOf(list.get(0)) : "");
                        }
                        if(list.get(1)!=null) {
                            dealerCurrentNetworkData.setDistrict(StringUtils.isNotBlank(String.valueOf(list.get(1))) ? String.valueOf(list.get(1)) : "");
                        }
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
    public Double getPremiumSalesLeaderByDeliveryDate(SclCustomerModel so, BaseSiteModel currentBaseSite, Date startDate, Date endDate) {
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
    public Double getPremiumSalesLeaderByDeliveryDateRetailer(SclCustomerModel sclUser, BaseSiteModel currentBaseSite, Date startDate, Date endDate) {
        return getSalesPerformanceDao().getActualTargetForPremiumSalesLeaderYTDRetailer(sclUser, currentBaseSite, startDate, endDate);
    }

    @Override
    public Double getCashDiscountAvailedPercentage(SclCustomerModel so, BaseSiteModel currentBaseSite, Date startDate, Date endDate) {
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
    public String getDaysSinceLastLiftingForRetailer(List<SclCustomerModel> saleQty) {
        Optional<SclCustomerModel> salesModel = saleQty.stream().max(Comparator.comparing(SclCustomerModel::getLastLiftingDate));
        if (salesModel.isPresent()) {
            LocalDate today = LocalDate.now();
            LocalDate deliveredDate = salesModel.get().getLastLiftingDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            return String.valueOf(Math.toIntExact(ChronoUnit.DAYS.between(deliveredDate, today)));
        }
        return "-";
    }

    public String getDaysSinceLastLiftingRetailerForDealer(SclCustomerModel cust) {
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
    public String getDaysSinceLastLiftingForInfluencer(List<SclCustomerModel> saleQty) {

        Optional<SclCustomerModel> salesModel = saleQty.stream().max(Comparator.comparing(SclCustomerModel::getLastLiftingDate));
        if (salesModel.isPresent()) {
            LocalDate today = LocalDate.now();
            LocalDate deliveredDate = salesModel.get().getLastLiftingDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            return String.valueOf(Math.toIntExact(ChronoUnit.DAYS.between(deliveredDate, today)));
        }
        return "-";
    }
    public String getDaysSinceLastLiftingInfForDealer(SclCustomerModel saleQty) {
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




    public SalesQuantityData setSalesQuantityForCustomer(SclCustomerModel customerModel, String customerType, List<String> territoryList) {
        SalesQuantityData salesQuantityData = new SalesQuantityData();
        LOGGER.info(String.format("getting Sale Quantity for leadType :: %s customerUID :: %s", customerType, customerModel.getUid()));
        if (customerType.equalsIgnoreCase(DEALER)) {
            double currentMonthSales = sclSalesSummaryService.getCurrentMonthSales(customerModel, territoryList);
            double lastMonthSales = sclSalesSummaryService.getLastMonthSales(customerModel, territoryList);
           /* double lastMonth = getlastMonthSaleQty(customerModel.getUid(),  doList, subAreaList);
            double actual = getCurrentMonthSaleQty(customerModel.getUid(),  doList, subAreaList);*/
            salesQuantityData.setLastMonth(Objects.nonNull(lastMonthSales) ? lastMonthSales : 0.0);
            salesQuantityData.setActual(Objects.nonNull(currentMonthSales) ? currentMonthSales : 0.0);
            LOGGER.info(String.format("Sale Quantity for leadType :: %s customerUID :: %s lastMonth :: %s actual :: %s", customerType, customerModel.getUid(), lastMonthSales, currentMonthSales));
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
    public Integer getCountForAllDealerRetailerInfluencers(String leadType, B2BCustomerModel user, BaseSiteModel site,List<String> territoryList,List<String> subAreaList,List<String> districtList) {
       /* List<SclCustomerModel> customerFilteredList = getCustomersByLeadType(leadType,territoryList,subAreaList,districtList);
        //if territoryList is not empty then filter customerFilteredList based upon territory list
        customerFilteredList=getCustomersByTerritoryCode(customerFilteredList,territoryList);*/
        List<TerritoryMasterModel> territoryMasterModels= (List<TerritoryMasterModel>) territoryMasterService.getCurrentTerritory();
        LOGGER.info(String.format("territoryMasterModels:: %s",territoryMasterModels));
        List<SclCustomerModel> customerFilteredList = salesPerformanceDao.getCurrentNetworkCustomers(leadType,null,territoryMasterModels,null,true,false,false);
        return CollectionUtils.isNotEmpty(customerFilteredList)?customerFilteredList.size():0;
    }

    @Override
    public Integer getCountOfAllRetailersInfluencers(String leadType) {
        List<SclCustomerModel> customerFilteredList = new ArrayList<>();
        SclCustomerModel currentUser = (SclCustomerModel) userService.getCurrentUser();
        if(currentUser != null) {
            if (currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
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
    public SalesPerformNetworkDetailsListData getBottomLaggingCounters(List<String> territoryList) {
        String leadType = DEALER;
        SalesPerformNetworkDetailsListData dealerCurrentNetworkListData = new SalesPerformNetworkDetailsListData();
        List<SalesPerformNetworkDetailsData> currentNetworkWsDataList = new ArrayList<>();
        List<TerritoryMasterModel> territoryMasterModels= (List<TerritoryMasterModel>) territoryMasterService.getCurrentTerritory();
        LOGGER.info(String.format("territoryMasterModels:: %s",territoryMasterModels));
        List<SclCustomerModel> customerFilteredList = salesPerformanceDao.getCurrentNetworkCustomers(leadType,null,territoryMasterModels,null,true,false,false);
        if(CollectionUtils.isNotEmpty(customerFilteredList)){
            List<SalesPerformNetworkDetailsData> convertedList = salesPerformnceCustomerDetailsConverter.convertAll(customerFilteredList);
            currentNetworkWsDataList.addAll(convertedList.stream().sorted(Comparator.comparing(SalesPerformNetworkDetailsData::getName)).collect(Collectors.toList()));
            dealerCurrentNetworkListData.setNetworkDetails(currentNetworkWsDataList);
        }
        if(currentNetworkWsDataList!=null && !currentNetworkWsDataList.isEmpty()){
            LOGGER.info(String.format("Size of List Before Limit:%s", currentNetworkWsDataList.size()));
            for (SalesPerformNetworkDetailsData detailsData : currentNetworkWsDataList) {
                LOGGER.info(String.format("Sales Qty:%s",detailsData.getSalesQuantity().getActual()));
            }
        }
        List<SalesPerformNetworkDetailsData> collect = currentNetworkWsDataList.stream().filter(nw -> nw.getSalesQuantity().getActual() != 0 || nw.getSalesQuantity().getActual() != 0.0).collect(Collectors.toList());
        if(collect.size()>0){
            LOGGER.info(String.format("Size of List Before Limit:%s", collect.size()));
        }
        List<SalesPerformNetworkDetailsData> data = collect.stream().sorted(Comparator.comparing(nw -> nw.getSalesQuantity().getActual())).collect(Collectors.toList());
        if(userService.getCurrentUser().getGroups().contains((userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.SP_GROUP_ID)))) {
            dealerCurrentNetworkListData.setNetworkDetails(data.stream().limit(5).sorted(Comparator.comparing(SalesPerformNetworkDetailsData::getName)).collect(Collectors.toList()));
        }else{
            dealerCurrentNetworkListData.setNetworkDetails(data.stream().limit(3).sorted(Comparator.comparing(SalesPerformNetworkDetailsData::getName)).collect(Collectors.toList()));
        }
        if(dealerCurrentNetworkListData.getNetworkDetails() !=null && !dealerCurrentNetworkListData.getNetworkDetails().isEmpty()){
            LOGGER.info(String.format("Size of Bottom List:%s", dealerCurrentNetworkListData.getNetworkDetails().size()));
        }
        return dealerCurrentNetworkListData;
    }

    @Override
    public SalesPerformamceCockpitSaleData getSalesHistoryForDealers(String subArea, SclUserModel sclUser, BaseSiteModel currentBaseSite) {

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

    public List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForYTD(SclUserModel sclUser, BaseSiteModel baseSite, SclCustomerModel customer, List<String> doList, List<String> subAreaList) {
        List<Date> dates = getCurrentFinancialYear();
        Date startDate = dates.get(0);
        Date endDate = dates.get(1);
        return salesPerformanceDao.getProductwiseSalesPercentRatioAndVolumeRatioForYTD(null, baseSite, startDate, endDate, customer, doList, subAreaList);
    }

    @Override
    public List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForMonthPicker(SclUserModel sclUser, BaseSiteModel baseSite, int month, int year, List<String> doList, List<String> subAreaList) {
        return salesPerformanceDao.getProductwiseSalesPercentRatioAndVolumeRatioForMonthPicker(sclUser, baseSite, month, year, doList, subAreaList);
    }

    @Override
    public List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForCustomerMonthPicker(SclCustomerModel sclCustomerModel, BaseSiteModel baseSite, int month, int year,List<String> subAreaList,List<String> districtList) {
        return salesPerformanceDao.getProductwiseSalesPercentRatioAndVolumeRatioForCustomerMonthPicker(sclCustomerModel, baseSite, month, year,subAreaList,districtList);
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
        double lastMonthSales = sclUserDao.getSalesQuantityForSalesPerformance(customerCode, getDate(startDate), getDate(endDate),  doList, subAreaList);
        return lastMonthSales;
    }

    /*  private double getCurrentMonthSaleQty(String customerCode, BaseSiteModel site){
          LocalDate date=LocalDate.now();
          LocalDate startDate = LocalDate.of(date.getYear(),date.getMonth(),1);
          LocalDate endDate = date;
          LOGGER.info(String.format("Getting CurrentMonthSaleQuantity for customerCode :: %s startDate :: endDate :: %s ",customerCode,startDate,endDate));
          double currentMonthSales = sclUserDao.getSalesQuantityForSalesPerformance(customerCode, getDate(startDate), getDate(endDate), site);
          return currentMonthSales;
      }*/
   /* private double getlastMonthSaleQty(String customerCode, BaseSiteModel site){
        LocalDate currentMonthdate=LocalDate.now();
        LocalDate lastMonthdate=LocalDate.now().minusMonths(1);
        LocalDate startDate = LocalDate.of(lastMonthdate.getYear(),lastMonthdate.getMonth(),1);
        LocalDate endDate = LocalDate.of(currentMonthdate.getYear(),currentMonthdate.getMonth(),1).minusDays(1);
        LOGGER.info(String.format("Getting LastMonthSaleQuantity for customerCode :: %s startDate :: endDate :: %s ",customerCode,startDate,endDate));
        double lastMonthSales = sclUserDao.getSalesQuantityForBottomLogging(customerCode, getDate(startDate), getDate(endDate), site);
        // double lastMonthSales = sclUserDao.getSalesQuantity(customerCode, getDate(startDate), getDate(endDate), site);
        return lastMonthSales;
    }
*/
    private double getCurrentMonthSaleQty(String customerCode, List<String> subAreaList) {
        LocalDate date = LocalDate.now();
        SclCustomerModel customerModel = (SclCustomerModel) userService.getUserForUID(customerCode);
        LocalDate startDate = LocalDate.of(date.getYear(), date.getMonth(), 1);
        LocalDate endDate = LocalDate.of(date.getYear(), date.getMonth(), date.lengthOfMonth()).plusDays(1);
        LOGGER.info(String.format("Getting CurrentMonthSaleQuantity for customerCode :: %s startDate :: endDate :: %s ", customerCode, startDate, endDate));
        //double currentMonthSales = sclUserDao.getSalesQuantityForBottomLogging(customerCode, getDate(startDate), getDate(endDate),  null, subAreaList);
        double currentMonthSales = sclSalesSummaryService.getCurrentMonthSales(customerModel,Collections.EMPTY_LIST);
        return currentMonthSales;
    }

    private double getlastMonthSaleQty(String customerCode,  int month, int year, List<String> doList, List<String> subAreaList) {
        LocalDate startDate = LocalDate.of(year, month, 1).minusMonths(1);
        LocalDate endDate = LocalDate.of(year, month, 1).minusDays(1);
        LOGGER.info(String.format("Getting LastMonthSaleQuantity for customerCode :: %s startDate :: endDate :: %s ", customerCode, startDate, endDate));
        double lastMonthSales = sclUserDao.getSalesQuantityForSalesPerformance(customerCode, getDate(startDate), getDate(endDate),  doList, subAreaList);
        return lastMonthSales;
    }

    private double getCurrentMonthSaleQty(String customerCode,  int month, int year, List<String> doList, List<String> subAreaList) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1);
        LOGGER.info(String.format("Getting CurrentMonthSaleQuantity for customerCode :: %s startDate :: endDate :: %s ", customerCode, startDate, endDate));
        double currentMonthSales = sclUserDao.getSalesQuantityForSalesPerformance(customerCode, getDate(startDate), getDate(endDate),  doList, subAreaList);
        //double currentMonthSales = sclUserDao.getSalesQuantity(customerCode, getDate(startDate), getDate(endDate), site);
        return currentMonthSales;
    }

    private double getlastYearSaleQty(String customerCode, BaseSiteModel site, List<String> doList, List<String> subAreaList) {
        List<Date> date = findFiscalYearforYTDLY();
        Date d1 = date.get(0);
        Date d2 = date.get(1);
        LocalDate startDateLY = d1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDateLY = d2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        LOGGER.info(String.format("Getting LastMonthSaleQuantity for customerCode :: %s startDate :: endDate :: %s ", customerCode, startDateLY, endDateLY));
        double lastMonthSales = sclUserDao.getSalesQuantityForSalesPerformance(customerCode, getDate(startDateLY), getDate(endDateLY),  doList, subAreaList);
        return lastMonthSales;
    }

    private double getCurrentYearSaleQty(String customerCode, BaseSiteModel site, List<String> doList, List<String> subAreaList) {
        List<Date> dates = findFiscalYearforYTDCY();
        Date dd1 = dates.get(0);
        Date dd2 = dates.get(1);
        LocalDate startDateCY = dd1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDateCY = dd2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LOGGER.info(String.format("Getting CurrentMonthSaleQuantity for customerCode :: %s startDate :: endDate :: %s ", customerCode, startDateCY, endDateCY));
        double currentMonthSales = sclUserDao.getSalesQuantityForSalesPerformance(customerCode, getDate(startDateCY), getDate(endDateCY),  doList, subAreaList);
        //double currentMonthSales = sclUserDao.getSalesQuantity(customerCode, getDate(startDate), getDate(endDate), site);
        return currentMonthSales;
    }

    private String getDate(LocalDate localDate) {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime dateTime = localDate.atStartOfDay(zone);
        String date = formatter.format(Date.from(dateTime.toInstant()));
        return date;
    }

    private Double getYearToYearGrowth(double salesCurrentYearQty,double salesLastYearQty) {
            if(salesLastYearQty>0) {
                return   (((salesCurrentYearQty- salesLastYearQty) / salesLastYearQty) * 100);
            }
            return 0.0;

       /* LocalDate lastYearCurrentDate = LocalDate.now().minusYears(1);
        LocalDate currentYearCurrentDate = LocalDate.now();
        LocalDate currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear(), Month.APRIL, 1);
        LocalDate lastFinancialYearDate = LocalDate.of(lastYearCurrentDate.getYear(), Month.APRIL, 1);

        LOGGER.info(String.format("Getting yoy growth for customerNo :: %s currentYearCurrentDate :: %s currentFinancialYearDate :: %s lastFinancialYearDate :: %s", customerCode, currentYearCurrentDate, currentFinancialYearDate, lastFinancialYearDate));
        double lastSale = sclUserDao.getSalesQuantity(customerCode, getDate(lastYearCurrentDate), getDate(lastFinancialYearDate));
        double currentSale = sclUserDao.getSalesQuantity(customerCode, getDate(currentYearCurrentDate), getDate(currentFinancialYearDate));
        LOGGER.info(String.format("For CustomerNo :: %s lastSale :: %s currentSale :: %s", customerCode, lastSale, currentSale));
        double growthValue = (float) (((lastSale - currentSale) / lastSale) * 100);
        return growthValue;*/
    }

    private Double getYearToYearGrowthForMonth(String customerCode, BaseSiteModel site) {

        LocalDate lastMonthCurrentDate = LocalDate.now().minusMonths(1);
        LocalDate currentMonthCurrentDate = LocalDate.now();
        LocalDate currentFinancialMonthDate = LocalDate.of(currentMonthCurrentDate.getYear(), currentMonthCurrentDate.getMonth(), 1);
        LocalDate lastFinancialMonthDate = LocalDate.of(lastMonthCurrentDate.getYear(), lastMonthCurrentDate.getMonth(), 1);

        LOGGER.info(String.format("Getting yoy growth for customerNo :: %s currentYearCurrentDate :: %s currentFinancialYearDate :: %s lastFinancialYearDate :: %s", customerCode, currentMonthCurrentDate, currentFinancialMonthDate, lastMonthCurrentDate));
        double lastSale = sclUserDao.getSalesQuantity(customerCode, getDate(lastMonthCurrentDate), getDate(currentFinancialMonthDate));
        double currentSale = sclUserDao.getSalesQuantity(customerCode, getDate(currentMonthCurrentDate), getDate(lastFinancialMonthDate));
        LOGGER.info(String.format("For CustomerNo :: %s lastSale :: %s currentSale :: %s", customerCode, lastSale, currentSale));
        double growthValue = (float) (((lastSale - currentSale) / lastSale) * 100);
        return growthValue;
    }

    @Override
    public Double getSalesByDeliveryDate(String subArea, SclUserModel sclUser, BaseSiteModel currentBaseSite, Date startDate, Date endDate) {
        return getSalesPerformanceDao().getActualTargetForSalesYTD(subArea, sclUser, currentBaseSite, startDate, endDate);
    }

    @Override
    public Double getSalesByDeliveryDate(SclUserModel sclUser, BaseSiteModel currentBaseSite, Date startDate, Date endDate, List<String> doList, List<String> subAreaList) {
        return getSalesPerformanceDao().getActualTargetForSalesYTD(sclUser, currentBaseSite, startDate, endDate, doList, subAreaList);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Double getSalesLeaderByDeliveryDate(SclCustomerModel sclUser, BaseSiteModel currentBaseSite, Date startDate, Date endDate) {
        return (Double) sessionService.executeInLocalView(new SessionExecutionBody()
        {
            @Override
            public Double execute()
            {
                try {
                    searchRestrictionService.disableSearchRestrictions();
                    return getSalesPerformanceDao().getActualTargetForSalesLeaderYTD(sclUser, currentBaseSite, startDate, endDate);
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
    public double getSecondaryLeadDistanceForMonth(SclUserModel sclUser, BaseSiteModel baseSite, Integer year1, Integer month1, List<String> territoryList) {
        final UserModel user = getUserService().getCurrentUser();
        double distance = 0.0;
        List<List<Object>> list = salesPerformanceDao.getSecondaryLeadDistanceForMonth(sclUser,WarehouseType.DEPOT, baseSite, year1, month1,territoryList);
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
    public double getSecondaryLeadDistance(SclUserModel sclUser, BaseSiteModel baseSite, List<String> territoryList) {
        final UserModel user = getUserService().getCurrentUser();
        double distance = 0.0;
        double sum = 0.0;
        List<List<Object>> list = salesPerformanceDao.getSecondaryLeadDistance(sclUser,WarehouseType.DEPOT, baseSite,territoryList);
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
    public double getSecondaryLeadDistanceMTD(SclUserModel sclUser, BaseSiteModel baseSite,List<String> territoryList) {
        final UserModel user = getUserService().getCurrentUser();
        double distance = 0.0;
        double sum = 0.0;
        List<List<Object>> list = salesPerformanceDao.getSecondaryLeadDistanceMTD(sclUser,WarehouseType.DEPOT, baseSite,territoryList);
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
    public double getSecondaryLeadDistanceYTD(SclUserModel sclUser, BaseSiteModel baseSite, List<String> teritoryList) {
        final UserModel user = getUserService().getCurrentUser();
        double distance = 0.0;
        double sum = 0.0;
        List<Date> dates = getCurrentFinancialYear();
        Date startDate = null;
        Date endDate = null;
        startDate = dates.get(0);
        endDate = dates.get(1);
        List<List<Object>> list = salesPerformanceDao.getSecondaryLeadDistanceYTD(sclUser, WarehouseType.DEPOT,baseSite, startDate, endDate,teritoryList);
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
    public List<DealerRevisedMonthlySalesModel> getMonthlySaleTargetGraphForSP(List<SclCustomerModel> sclCustomer, String month, String year) {
        return salesPerformanceDao.getMonthlySaleTargetGraphForSP(sclCustomer, month, year);
    }

    @Override
    public DealerRevisedMonthlySalesModel getMonthlySaleTargetGraph(SclUserModel user, String monthName, String yearName, List<String> doList, List<String> subAreaList) {
        return salesPerformanceDao.getMonthlySaleTargetGraph(user, monthName, yearName, doList, subAreaList);
    }

    @Override
    public double getActualSaleForDealerGraphYTD(SclCustomerModel customer, Date startDate, Date endDate, BaseSiteModel currentBaseSite, String bgpFilter) {
        return salesPerformanceDao.getActualSaleForDealerGraphYTD(customer, startDate, endDate, currentBaseSite, bgpFilter);
    }

    @Override
    public double getActualSaleForDealerGraphYTDSP(List<SclCustomerModel> sclCustomer, Date startDate, Date endDate, BaseSiteModel currentBaseSite) {
        return salesPerformanceDao.getActualSaleForDealerGraphYTDSP(sclCustomer, startDate, endDate, currentBaseSite);
    }

    @Override
    public double getActualSaleForGraphYTD(SclUserModel userModel, Date startDate, Date endDate, BaseSiteModel currentBaseSite, List<String> doList, List<String> subAreaList) {
        return salesPerformanceDao.getActualSaleForGraphYTD(userModel, startDate, endDate, currentBaseSite, doList, subAreaList);
    }

    @Override
    public double getActualTargetFor10DayBucketForDealer(SclCustomerModel sclCustomer, String bgpFilter, Date startDate1, Date endDate1) {
        LOGGER.info("10DayBucket startDate1: "+startDate1 + "and" + "endDate1:" + endDate1);
        return salesPerformanceDao.getActualTargetFor10DayBucketForDealer(sclCustomer, bgpFilter, startDate1, endDate1);
    }

    @Override
    public double getActualTargetFor10DayBucketForSP(List<SclCustomerModel> sclCustomer, Date startDate1, Date endDate1) {
        return salesPerformanceDao.getActualTargetFor10DayBucketForSP(sclCustomer, startDate1, endDate1);
    }

    @Override
    public double getActualTargetFor10DayBucket(SclUserModel sclUser, Date startDate1, Date endDate1, List<String> doList, List<String> subAreaList) {
        return salesPerformanceDao.getActualTargetFor10DayBucket(sclUser, startDate1, endDate1, doList, subAreaList);
    }

    @Override
    public Map<String, Object> getDirectDispatchOrdersMTDPercentage(int month, int year, List<String> doList, List<String> territoryList) {

        Map<String, Object> map = new HashMap<>();
        final UserModel currentUser = getUserService().getCurrentUser();
        Integer directDispatchOrdersMTD = salesPerformanceDao.findDirectDispatchOrdersMTDCount(currentUser, WarehouseType.PLANT, month, year, doList,territoryList);
        Integer secondaryDispatchOrdersMTD = salesPerformanceDao.findDirectDispatchOrdersMTDCount(currentUser, WarehouseType.DEPOT, month, year, doList,territoryList);
        if (directDispatchOrdersMTD <= 0 && secondaryDispatchOrdersMTD <= 0) {
            throw new IllegalArgumentException("directDispatchOrdersMTD and secondaryDispatchOrdersMTD must be a positive non-zero value");
        } else {
            map.put("directDispatch", ((double) directDispatchOrdersMTD / ((double) directDispatchOrdersMTD + (double) secondaryDispatchOrdersMTD)) * 100);
            map.put("secondaryDispatch", ((double) secondaryDispatchOrdersMTD / ((double) directDispatchOrdersMTD + (double) secondaryDispatchOrdersMTD)) * 100);
            return map;
        }
    }

    @Override
    public Double getSalesLeaderByDeliveryDateRetailer(SclCustomerModel sclUser, BaseSiteModel currentBaseSite, Date startDate, Date endDate) {
        return getSalesPerformanceDao().getActualTargetForSalesLeaderYTDRetailer(sclUser, currentBaseSite, startDate, endDate);
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
        int startYear=0;
        if(date.getMonth().getValue()<4){
            startYear= currentYear - 1;
        }else {
            startYear = currentYear;
        }
        StringBuilder f = new StringBuilder();
        return String.valueOf(f.append(String.valueOf(startYear)).append("-").append(String.valueOf(startYear+1)));
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
        int fyYear = currentYear - 1;
        StringBuilder f = new StringBuilder();
        return String.valueOf(f.append(String.valueOf(fyYear)).append("-").append(String.valueOf(currentYear)));
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
    public List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForCustomer(SclCustomerModel customer,
                                                                                       BaseSiteModel baseSite, String customerType, List<String> doList, List<String> subAreaList) {
        return getSalesPerformanceDao().getProductwiseSalesPercentRatioAndVolumeRatioForCustomer(customer, baseSite, customerType, doList, subAreaList);
    }

    @Override
    public List<List<Object>> getBrandwiseSalesPercentRatioAndVolumeRatioForCustomer(SclCustomerModel customer) {
        return getSalesPerformanceDao().getBrandwiseSalesPercentRatioAndVolumeRatioForCustomer(customer);
    }

    @Override
    public List<List<Object>> getProductMixPercentRatioAndVolumeRatioWithPoints(SclCustomerModel customer,String filter) {
        List<Date> dates = getCurrentFinancialYear();
        Date startDate = dates.get(0);
        Date endDate = dates.get(1);
        return getSalesPerformanceDao().getProductMixPercentRatioAndVolumeRatioWithPoints(customer,filter,startDate,endDate);
    }


    @Override
    public Double getActualTargetSalesForSelectedMonthAndYear(String subArea, SclUserModel sclUser, BaseSiteModel baseSite, int month, int year) {
        return getSalesPerformanceDao().getActualTargetSalesForSelectedMonthAndYear(subArea, sclUser, baseSite, month, year);
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
    public ZeroLiftingNetworkData getZeroLiftingSummaryData(int month, int year, String filter, BaseSiteModel site, String SOFilter, List<String> territoryList,List<String> subAreaList,List<String> districtList) {
        ZeroLiftingNetworkData data = new ZeroLiftingNetworkData();


       /* List<SclCustomerModel> sclCustomerFilteredList = getCustomersByLeadType(SOFilter,territoryList,subAreaList,districtList);
        LOGGER.info(String.format("Size before last lifting from SCL customer:%s",String.valueOf(sclCustomerFilteredList.size())));
        sclCustomerFilteredList=getCustomersByTerritoryCode(sclCustomerFilteredList,territoryList);
        //if territoryList is not empty then filter sclCustomerFilteredList based upon territory list
        List<SclCustomerModel> customerFilteredList=new ArrayList<>();
        if(sclCustomerFilteredList!=null && !sclCustomerFilteredList.isEmpty()) {
            customerFilteredList = salesPerformanceDao.getSclCustomerZeroLiftingList(sclCustomerFilteredList);
        }*/
        List<TerritoryMasterModel> territoryMasterModels= (List<TerritoryMasterModel>) territoryMasterService.getCurrentTerritory();
        LOGGER.info(String.format("territoryMasterModels:: %s",territoryMasterModels));
        List<SclCustomerModel> customerFilteredList = salesPerformanceDao.getCurrentNetworkCustomers(SOFilter,null,territoryMasterModels,filter,true,true,false);
        LOGGER.info(String.format("Size After last lifting from SCL customer:%s",String.valueOf(customerFilteredList.size())));
        if (CollectionUtils.isNotEmpty(customerFilteredList)) {
            data.setCount(customerFilteredList.size());
            data.setPotential(customerFilteredList.stream().filter(c->c.getCounterPotential()!=null).mapToDouble(SclCustomerModel::getCounterPotential).sum());
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
        List<SclCustomerModel> sclCustomerFilteredList = new ArrayList<>();
        List<SclCustomerModel> customerFilteredList = new ArrayList<>();

        SclCustomerModel currentUser = (SclCustomerModel) userService.getCurrentUser();
        if (currentUser != null) {
            if (currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                if (!currentUser.getCounterType().equals(CounterType.SP)) {
                    if (leadType.equalsIgnoreCase(RETAILER)) {
                        sclCustomerFilteredList = territoryManagementService.getRetailerListForDealer();
                    } else if (leadType.equalsIgnoreCase(INFLUENCER)) {
                        sclCustomerFilteredList = territoryManagementService.getInfluencerListForDealer();
                    }
                }
            }
        }

        LOGGER.info(String.format("Size before last lifting from SCL customer:%s", String.valueOf(sclCustomerFilteredList.size())));

        if (sclCustomerFilteredList != null && !sclCustomerFilteredList.isEmpty()) {
            customerFilteredList = salesPerformanceDao.getSclCustomerZeroLiftingList(sclCustomerFilteredList);
        }
        LOGGER.info(String.format("Size After last lifting from SCL customer:%s", String.valueOf(customerFilteredList.size())));

        if (customerFilteredList != null && !customerFilteredList.isEmpty()) {
            data.setCount(customerFilteredList.size());
            data.setPotential(customerFilteredList.stream().filter(c -> c.getCounterPotential() != null).mapToDouble(SclCustomerModel::getCounterPotential).sum());
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
    private Double getSalesQuantityFromPointReq(List<SclCustomerModel> saleQty) {
        Double saleQtyMonth=0.0;
        for (SclCustomerModel customerModel : saleQty) {
            PointRequisitionModel sclCustomerFromPointReq = salesPerformanceDao.getSclCustomerFromPointReq(customerModel);
            saleQtyMonth+=sclCustomerFromPointReq.getQuantity();
        }
        return saleQtyMonth;
    }
    private Double getSalesQuantityInfForDealer(SclCustomerModel customerModel) {
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

    private Double getSalesQuantityCurrentMonthOrderReq(List<SclCustomerModel> saleQty) {
        Double saleQtyCurrentMonth = 0.0;
        if(saleQty!=null && !saleQty.isEmpty()) {
            for (SclCustomerModel customerModel : saleQty) {
                OrderRequisitionModel sclCustomerFromOrderReq = salesPerformanceDao.getSclCustomerFromOrderReq(customerModel);
                if (sclCustomerFromOrderReq != null) {
                    if (sclCustomerFromOrderReq.getQuantity() != null) {
                        saleQtyCurrentMonth += sclCustomerFromOrderReq.getQuantity();
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
    private Double getSalesQuantityCurrentMonthRetailer(SclCustomerModel customerModel) {
        Double saleQtyCurrentMonth=0.0;
        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
        List<SclCustomerModel> dealer=new ArrayList<>();
        dealer.add((SclCustomerModel) currentUser);
        if(currentUser instanceof SclCustomerModel) {
            LOGGER.info(String.format("Customer id:%s", String.valueOf(customerModel.getUid())));
            Map<String, Double> salesQuantityForRetailerByMTD = networkService.getSalesQuantityForRetailerByMTD(customerModel, dealer, null);
            if(salesQuantityForRetailerByMTD!=null){
                saleQtyCurrentMonth = salesQuantityForRetailerByMTD.get("quantityInBags");
            }
            //saleQuantity = salesPerformanceDao.getRetailerFromOrderReq(customerModel, startDate, endDate);
        }

       // Double saleQtyCurrentMonth = salesPerformanceDao.getRetailerFromOrderReq(customerModel,monthStart,todayDate);
        return saleQtyCurrentMonth;
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

    private Double getSalesQuantityLastYearCurrentMonthOrderReq(List<SclCustomerModel> cust) {
        LocalDate today = LocalDate.now();
        Double saleQty=0.0;
        LocalDate monthStart1 = LocalDate.of(today.getYear() - 1, today.getMonth(), 1);
        LocalDate monthEnd1 = LocalDate.of(today.getYear() - 1, today.getMonth(), today.lengthOfMonth());
        Date finalStartDate = Date.from(monthStart1.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date finalEndDate = Date.from(monthEnd1.atStartOfDay(ZoneId.systemDefault()).toInstant());
        List<SclCustomerModel> collect = cust.stream().filter(sale -> sale.getLastLiftingDate().after(finalStartDate) && sale.getLastLiftingDate().before(finalEndDate)).collect(Collectors.toList());
        for (SclCustomerModel customerModel : collect) {
            OrderRequisitionModel sclCustomerFromOrderReq = salesPerformanceDao.getSclCustomerFromOrderReq(customerModel);
            if(sclCustomerFromOrderReq.getQuantity()!=null)
                saleQty+=sclCustomerFromOrderReq.getQuantity();
            else
                saleQty=0.0;
        }
       return saleQty;
    }
    private Double getSalesQuantityLastYearCurrentMonthRetailer(SclCustomerModel customerModel) {
       LocalDate today=LocalDate.now();
        Double saleQtyLastMonth=0.0;
        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
        List<SclCustomerModel> dealer=new ArrayList<>();
        dealer.add((SclCustomerModel) currentUser);
        if(currentUser instanceof SclCustomerModel) {
            LOGGER.info(String.format("Customer id:%s", String.valueOf(customerModel.getUid())));
            Map<String, Double> salesQuantityForRetailerByMTD = networkService.getSalesQuantityForRetailerByMonthYear(customerModel, today.getMonthValue(),today.getYear()-1, dealer, null);
            if(salesQuantityForRetailerByMTD!=null){
                saleQtyLastMonth = salesQuantityForRetailerByMTD.get("quantityInBags");
            }
        }
        LOGGER.info(String.format("Last year current month:%s,%s,%s,%s,%s",customerModel,currentUser,saleQtyLastMonth,today.getMonthValue(),today.getYear()));
        return saleQtyLastMonth;
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
    public LowPerformingNetworkData getLowPerformingSummaryData(int month, int year, String filter, BaseSiteModel site, String customerType, List<String> territoryList,List<String> subAreaList,List<String> districtList) {
        /*List<SclCustomerModel> sclCustomerFilteredList = getCustomersByLeadType(customerType,territoryList,subAreaList,districtList);
        LOGGER.info(String.format("Size before last lifting from SCL customer:%s",String.valueOf(sclCustomerFilteredList.size())));
       //add territory code filter logic
        //if territoryList is not empty then filter customerFilteredList based upon territory list

        List<SclCustomerModel> customerFilteredList=getCustomersByTerritoryCode(sclCustomerFilteredList,territoryList);
        if(customerFilteredList!=null && !customerFilteredList.isEmpty()) {
            customerFilteredList = salesPerformanceDao.getSclCustomerLastLiftingList(customerFilteredList);
        }
        LOGGER.info(String.format("Size before last lifting from SCL customer:%s",String.valueOf(customerFilteredList.size())));
        if (customerFilteredList != null && !customerFilteredList.isEmpty()) {

            for (SclCustomerModel customerModel : customerFilteredList) {

                SalesPerformNetworkDetailsData dealerCurrentNetworkData = new SalesPerformNetworkDetailsData();

                if (customerType.equalsIgnoreCase(DEALER)) {
                    dealerCurrentNetworkData.setCounterSharePercentage(getCounterShareForDealer(customerModel,LocalDate.now().getMonthValue(), LocalDate.now().getYear()));
                    dealerCurrentNetworkData.setSalesQuantity(setSalesQuantityForCustomer(customerModel, customerType, territoryList));
                    dealerCurrentNetworkData.setCode(customerModel.getUid());
                    dealerCurrentNetworkData.setName(customerModel.getName());
                    dealerCurrentNetworkData.setPotential(Objects.nonNull(customerModel.getCounterPotential()) ? String.valueOf(customerModel.getCounterPotential()) : ZERO);
                }
                currentNetworkWsDataList.add(dealerCurrentNetworkData);
            }
        }
*/
        LowPerformingNetworkData data = new LowPerformingNetworkData();
        List<SalesPerformNetworkDetailsData> currentNetworkWsDataList = new ArrayList<>();
        if(currentNetworkWsDataList!=null && !currentNetworkWsDataList.isEmpty()) {
            if (customerType.equalsIgnoreCase(DEALER)) {
                LOGGER.info((String.format("Size for Dealer:%s",currentNetworkWsDataList.size())));
                List<SalesPerformNetworkDetailsData> salesPerformNetworkDetailsData1 = currentNetworkWsDataList.stream().filter(nw->Objects.nonNull(nw.getSalesQuantity()) && Objects.nonNull(nw.getSalesQuantity().getActual())).filter(nw -> nw.getSalesQuantity().getActual() != 0.0).collect(Collectors.toList());
                List<SalesPerformNetworkDetailsData> salesPerformNetworkDetailsData2 = salesPerformNetworkDetailsData1.stream().sorted(Comparator.comparing(nw -> nw.getSalesQuantity().getActual())).collect(Collectors.toList());
                List<SalesPerformNetworkDetailsData> collect = salesPerformNetworkDetailsData2.stream().limit((long) salesPerformNetworkDetailsData2.size() * LOWPERCENTAGE / 100).collect(Collectors.toList());
                data.setCount(collect.size());

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

                Double avgOfCounterShare = 0.0;
                CounterShareResponseData responseData=null;
                double sumOfNumeratorSales=0.0,sumOfPotential=0.0;
                if (CollectionUtils.isNotEmpty(collect)) {
                    {
                        for (SalesPerformNetworkDetailsData detailsData : collect) {
                            CounterShareData counterShareData=new CounterShareData();
                            counterShareData.setDealerCode(detailsData.getCode());
                            counterShareData.setMonth(LocalDate.now().getMonthValue());
                            counterShareData.setYear(LocalDate.now().getYear());
                            responseData = getCounterShareData(counterShareData);
                            if(Objects.nonNull(responseData) && responseData.getPotential()>0){
                                sumOfNumeratorSales+=responseData.getNumeratorSales();
                                sumOfPotential+=responseData.getPotential();
                            }
                            LOGGER.info(" Counter Share"+responseData.getCounterShare() + " Potential:"+responseData.getPotential()+ " Numerator:"+responseData.getNumeratorSales()+ " SelfBrandSale"+responseData.getSelfBrandSale()+ " TotalSales "+responseData.getTotalSales());
                        }
                    }
                    if(sumOfPotential!=0.0 && sumOfNumeratorSales!=0.0) {
                        avgOfCounterShare = (sumOfNumeratorSales/sumOfPotential) * 100;
                    }else{
                        avgOfCounterShare=0.0;
                    }
                  /*  double avgMonthlySales = collect.stream().filter(nw->Objects.nonNull(nw.getSalesQuantity().getActual())).filter(nw -> nw.getSalesQuantity().getActual() != 0).mapToDouble(objects -> objects.getSalesQuantity().getActual()).sum();
                    totalMonthlyPotential = collect.stream().filter(obj -> Objects.nonNull(obj.getPotential())).mapToDouble(objects -> Double.parseDouble(objects.getPotential())).sum();
                    avgOfCounterShare = (avgMonthlySales/totalMonthlyPotential) * 100;*/
                    data.setCounterSharePercentage(avgOfCounterShare);
                } else {
                    avgOfCounterShare = 0.0;
                    data.setCounterSharePercentage(avgOfCounterShare);
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
        List<SclCustomerModel> sclCustomerFilteredList = new ArrayList<>();
        List<SclCustomerModel> customerFilteredList = new ArrayList<>();

        SclCustomerModel currentUser = (SclCustomerModel) userService.getCurrentUser();
        if (currentUser != null) {
            if (currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                if (!currentUser.getCounterType().equals(CounterType.SP)) {
                    if (customerType.equalsIgnoreCase(RETAILER)) {
                        sclCustomerFilteredList = territoryManagementService.getRetailerListForDealer();
                    } else if (customerType.equalsIgnoreCase(INFLUENCER)) {
                        sclCustomerFilteredList = territoryManagementService.getInfluencerListForDealer();
                    }
                }
            }
        }
        LOGGER.info(String.format("Size before last lifting from SCL customer:%s",String.valueOf(sclCustomerFilteredList.size())));

        if(sclCustomerFilteredList!=null && !sclCustomerFilteredList.isEmpty()) {
            customerFilteredList = salesPerformanceDao.getSclCustomerLastLiftingList(sclCustomerFilteredList);
        }
        LOGGER.info(String.format("Size before last lifting from SCL customer:%s",String.valueOf(customerFilteredList.size()))); if (customerFilteredList != null && !customerFilteredList.isEmpty()) {

            for (SclCustomerModel customerModel : customerFilteredList) {

                SalesPerformNetworkDetailsData dealerCurrentNetworkData = new SalesPerformNetworkDetailsData();

                if (customerType.equalsIgnoreCase(RETAILER)) {
                    SclCustomerModel orderRequisitionSalesDataForRetailer = salesPerformanceDao.getRetailerSalesForDealerLowPerform(customerModel, baseSiteService.getCurrentBaseSite());
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

              /*  Double avgOfCounterShare = 0.0;
                if (collect.size() != 0) {
                    avgOfCounterShare = collect.stream().filter(obj -> Objects.nonNull(obj.getCounterSharePercentage())).mapToDouble(SalesPerformNetworkDetailsData::getCounterSharePercentage).sum();
                    data.setCounterSharePercentage(avgOfCounterShare);
                } else {
                    avgOfCounterShare = 0.0;
                    data.setCounterSharePercentage(avgOfCounterShare);
                }*/

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

                Double avgOfCounterShare = 0.0;
                if (collect.size() != 0) {
                    double avgMonthlySales = collect.stream().filter(nw->Objects.nonNull(nw.getSalesQuantity().getActual())).filter(nw -> nw.getSalesQuantity().getActual() != 0).mapToDouble(objects -> objects.getSalesQuantity().getActual()).sum();
                    totalMonthlyPotential = collect.stream().filter(obj -> Objects.nonNull(obj.getPotential())).mapToDouble(objects -> Double.parseDouble(objects.getPotential())).sum();
                    avgOfCounterShare = (avgMonthlySales/totalMonthlyPotential) * 100;
                    data.setCounterSharePercentage(avgOfCounterShare);
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
    public Double getSalesTargetForMonth(SclUserModel sclUser, BaseSiteModel currentBaseSite,
                                         Integer year, Integer month) {

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.YEAR, year);
        Date date = cal.getTime();

        String monthName = getMonth(date);
        String yearName = getYear(date);
        String formattedMonth = monthName.concat("-").concat(String.valueOf(yearName));
        return getSalesPerformanceDao().getMonthlySalesTarget(sclUser, currentBaseSite, formattedMonth, yearName);
    }

    @Override
    public Double getSalesTargetForMonth(SclUserModel sclUser, BaseSiteModel currentBaseSite, Integer year, Integer month, List<String> territoryList) {

      /*  Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.YEAR, year);
        Date date = cal.getTime();

        String monthName = getMonth(date);
        String yearName = getYear(date);*/
        String monthName = Month.of(month).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        String formattedMonth = monthName.concat("-").concat(String.valueOf(year));

        return getSalesPerformanceDao().getMonthlySalesTarget(sclUser, currentBaseSite, formattedMonth, String.valueOf(year),territoryList);
    }

    @Override
    public Double getSalesTargetForMonthDealer(SclCustomerModel sclCustomer, BaseSiteModel currentBaseSite, Integer year, Integer month, String bgpFilter) {
        List<DealerRevisedMonthlySalesModel> monthlySalesTargetForDealerWithBGP = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.YEAR, year);
        Date date = cal.getTime();

        String monthName = getMonth(date);
        String yearName = getYear(date);

        String formattedMonth = monthName.concat("-").concat(String.valueOf(yearName));


        if (StringUtils.isNotBlank(bgpFilter) && !bgpFilter.equalsIgnoreCase("ALL")) {
            ProductModel productModel = sclSalesSummaryService.getProductAliasNameByCode(bgpFilter);
            CatalogVersionModel catalogVersion = null;
            ProductModel product = null;
            if (productModel != null) {
                catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
                product = productService.getProductForCode(catalogVersion, productModel.getCode());
            }

            double revisedTarget = 0.0, sumRevisedTarget = 0.0;
            if (product != null){
               // if (bgpFilter.equalsIgnoreCase(product.getName())) {
                    monthlySalesTargetForDealerWithBGP = salesPerformanceDao.getMonthlySalesTargetForDealerWithBGP(sclCustomer, currentBaseSite, formattedMonth, yearName, bgpFilter);

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
//                }
           }
            return sumRevisedTarget;
        } else {
           // return salesPerformanceDao.getMonthlySalesTargetForDealer(sclCustomer, currentBaseSite, monthName, yearName, bgpFilter);
            return (Double) sessionService.executeInLocalView(new SessionExecutionBody()
            {
                @Override
                public Double execute()
                {
                    try {
                        searchRestrictionService.disableSearchRestrictions();
                        return salesPerformanceDao.getMonthlySalesTargetForDealer(sclCustomer, currentBaseSite, formattedMonth, yearName, bgpFilter);
                    }
                    finally {
                        searchRestrictionService.enableSearchRestrictions();
                    }
                }
            });
        }
    }

    @Override
    public Double getSalesTargetForMtdSP(List<SclCustomerModel> sclUser, BaseSiteModel currentBaseSite, Integer year, Integer month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.YEAR, year);
        Date date = cal.getTime();

        String monthName = getMonth(date);
        String yearName = getYear(date);

        String formattedMonth = monthName.concat("-").concat(String.valueOf(yearName));

        return salesPerformanceDao.getMonthlySalesTargetForMtdSp(sclUser, currentBaseSite, monthName, yearName);
    }

    @Override
    public Double getSalesTargetForMonthSP(List<SclCustomerModel> sclCustomerModel, Integer year, Integer month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.YEAR, year);
        Date date = cal.getTime();

        String monthName = getMonth(date);
        String yearName = getYear(date);

        String formattedMonth = monthName.concat("-").concat(String.valueOf(yearName));
        return salesPerformanceDao.getMonthlySalesTargetForSP(sclCustomerModel, monthName, yearName);
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
    public Double getSalesTargetForPartnerMonth(String code, SclUserModel sclUser, BaseSiteModel currentBaseSite, Integer year, Integer month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.YEAR, year);
        Date date = cal.getTime();

        String monthName = getMonth(date);
        String yearName = getYear(date);

        return getSalesPerformanceDao().getMonthlySalesTarget(sclUser, currentBaseSite, monthName, yearName);
    }

    @Override
    public Double getActualTargetForSalesMTD(SclUserModel sclUser, BaseSiteModel currentBaseSite, List<String> doList, List<String> subAreaList) {
        return salesPerformanceDao.getActualTargetForSalesMTD(sclUser, currentBaseSite, doList, subAreaList);
    }

    @Override
    public Double getActualTargetForSalesMTDDealer(SclCustomerModel sclCustomer, BaseSiteModel currentBaseSite, String bgpFilter) {
        return salesPerformanceDao.getActualTargetForSalesMTDDealer(sclCustomer, currentBaseSite, bgpFilter);
    }

    @Override
    public Double getActualTargetForSalesMTDRetailer(SclCustomerModel sclCustomer, BaseSiteModel currentBaseSite, String bgpFilter) {
        return salesPerformanceDao.getActualTargetForSalesMTDRetailer(sclCustomer, currentBaseSite, bgpFilter);
    }

    @Override
    public Double getActualTargetForSalesDealerMTD(SclCustomerModel sclCustomer, BaseSiteModel currentBaseSite, String bgpFilter) {
       return salesPerformanceDao.getActualTargetForSalesDealerMTD(sclCustomer, currentBaseSite, bgpFilter);
    }

    @Override
    public Double getActualTargetForSalesSPMTD(List<SclCustomerModel> sclCustomer) {
        return salesPerformanceDao.getActualTargetForSalesSPMTD(sclCustomer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Double getActualTargetForSalesSPMTDSearch(List<SclCustomerModel> sclCustomer) {
       return (Double) sessionService.executeInLocalView(new SessionExecutionBody()
        {
            @Override
            public Double execute()
            {
                try {
                    searchRestrictionService.disableSearchRestrictions();
                    return salesPerformanceDao.getActualTargetForSalesSPMTDSearch(sclCustomer);
                }
                finally {
                    searchRestrictionService.enableSearchRestrictions();
                }
            }
        });
    }

    @Override
    public Double getActualTargetForSalesRetailerMTD(SclCustomerModel sclCustomer, BaseSiteModel currentBaseSite, String bgpFilter) {
        return salesPerformanceDao.getActualTargetForSalesRetailerMTD(sclCustomer, currentBaseSite, bgpFilter);
    }

    @Override
    public Double getActualTargetForSalesLastMonth(SclUserModel sclUser, BaseSiteModel currentBaseSite,int year, int month, List<String> doList, List<String> subAreaList) {
        return salesPerformanceDao.getActualTargetForSalesLastMonth(sclUser, currentBaseSite,year,month, doList, subAreaList);
    }

    @Override
    public Double getActualTargetForSalesYTD(SclUserModel sclUser, BaseSiteModel currentBaseSite, List<String> doList, List<String> subAreaList) {
        List<Date> dates = getCurrentFinancialYear();
        Date startDate = null;
        Date endDate = null;
        startDate = dates.get(0);
        endDate = dates.get(1);
        return salesPerformanceDao.getActualTargetForSalesYTD(sclUser, currentBaseSite, startDate, endDate, doList, subAreaList);
    }

    @Override
    public Double getActualTargetForSalesYTDDealer(SclCustomerModel sclCustomer, BaseSiteModel currentBaseSite, String bgpFilter) {
        List<Date> dates = getCurrentFinancialYear();
        Date startDate = null;
        Date endDate = null;
        startDate = dates.get(0);
        endDate = dates.get(1);
        return salesPerformanceDao.getActualTargetForSalesYTDDealer(sclCustomer, currentBaseSite, startDate, endDate, bgpFilter);
    }

/*    @Override
    public Double getActualTargetForSalesYTDSP(List<SclCustomerModel> sclCustomer, BaseSiteModel currentBaseSite) {
        List<Date> dates = getCurrentFinancialYear();
        Date startDate = null;
        Date endDate = null;
        startDate = dates.get(0);
        endDate = dates.get(1);
        return salesPerformanceDao.getActualTargetForSalesYTDSP(sclCustomer, currentBaseSite, startDate, endDate);
    }*/

    @SuppressWarnings("unchecked")
    @Override
    public Double getActualTargetForSalesYTDSP(List<SclCustomerModel> sclCustomer, BaseSiteModel currentBaseSite) {
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
                    return salesPerformanceDao.getActualTargetForSalesYTDSP(sclCustomer, currentBaseSite, startDate, endDate);
                }
                finally {
                    searchRestrictionService.enableSearchRestrictions();
                }
            }
        });
    }

    @Override
    public Double getActualTargetForSalesYTDRetailer(SclCustomerModel sclCustomer, BaseSiteModel currentBaseSite, String bgpFilter) {
       List<Date> dates = getCurrentFinancialYear();
        Date startDate = null;
        Date endDate = null;
        startDate = dates.get(0);
        endDate = dates.get(1);
        return salesPerformanceDao.getActualTargetForSalesYTDRetailer(sclCustomer, currentBaseSite,startDate, endDate, bgpFilter);
    }


    @Override
    public Double getActualTargetForSalesDealerYTD(SclCustomerModel sclCustomer, BaseSiteModel currentBaseSite, String bgpFilter) {
        List<Date> dates = getCurrentFinancialYear();
        Date startDate = null;
        Date endDate = null;
        startDate = dates.get(0);
        endDate = dates.get(1);
        return salesPerformanceDao.getActualTargetForSalesDealerYTD(sclCustomer, currentBaseSite, startDate, endDate, bgpFilter);
    }

    @Override
    public Double getActualTargetForSalesRetailerYTD(SclCustomerModel sclCustomer, BaseSiteModel currentBaseSite, String bgpFilter) {
        List<Date> dates = getCurrentFinancialYear();
        Date startDate = null;
        Date endDate = null;
        startDate = dates.get(0);
        endDate = dates.get(1);
        return salesPerformanceDao.getActualTargetForSalesRetailerYTD(sclCustomer, currentBaseSite, startDate, endDate, bgpFilter);
    }

    @Override
    public Double getActualTargetForSalesLastYear(SclUserModel sclUser, BaseSiteModel currentBaseSite, List<String> doList, List<String> subAreaList) {

        List<Date> dates = getPrevFinancialYear();
        Date startDate = null;
        Date endDate = null;
        startDate = dates.get(0);
        endDate = dates.get(1);

        return salesPerformanceDao.getActualTargetForSalesYTD(sclUser, currentBaseSite, startDate, endDate, doList, subAreaList);

    }

    @Override
    public Double getActualTargetForPartnerSalesMTD(String code, SclUserModel sclUser, BaseSiteModel currentBaseSite, List<String> doList, List<String> subAreaList) {
        return salesPerformanceDao.getActualTargetForPartnerSalesMTD(code,sclUser, currentBaseSite, doList, subAreaList);
    }

    @Override
    public Double getActualTargetForPartnerSalesYTD(String code, SclUserModel sclUser, BaseSiteModel currentBaseSite, List<String> doList, List<String> subAreaList) {
        List<Date> dates = getCurrentFinancialYear();
        Date startDate = null;
        Date endDate = null;
        startDate = dates.get(0);
        endDate = dates.get(1);
        return salesPerformanceDao.getActualTargetForPartnerSalesYTD(code,sclUser, currentBaseSite, startDate, endDate, doList, subAreaList);
    }

    @Override
    public Double getActualTargetForSalesForMonth(SclUserModel sclUser,
                                                  BaseSiteModel currentBaseSite, int year, int month, List<String> doList, List<String> subAreaList) {
        return salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYear(sclUser, currentBaseSite, month, year, doList, subAreaList);
    }

    @Override
    public Double getActualTargetForSalesForMonthDealer(SclCustomerModel sclCustomer, BaseSiteModel currentBaseSite, int year, int month, String bgpFilter) {
        return salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYearForDealer(sclCustomer, currentBaseSite, month, year, bgpFilter);
    }

   /* @Override
    public Double getActualTargetForSalesForMonthDealer(SclCustomerModel sclCustomer, BaseSiteModel currentBaseSite, int year, int month, String bgpFilter) {
        return (Double) sessionService.executeInLocalView(new SessionExecutionBody()
        {
            @Override
            public Double execute()
            {
                try {
                    searchRestrictionService.disableSearchRestrictions();
                    return salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYearForDealer(sclCustomer, currentBaseSite, month, year, bgpFilter);
                }
                finally {
                    searchRestrictionService.enableSearchRestrictions();
                }
            }
        });
    }*/
    @Override
    public Double getActualTargetForSalesForMtdSp(List<SclCustomerModel> sclCustomer, BaseSiteModel currentBaseSite, int year, int month) {
        return salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYearForMTDSP(sclCustomer, currentBaseSite, month, year);
    }

   /* @Override
    public Double getActualTargetForSalesForMonthSP(List<SclCustomerModel> sclCustomerModels, int year, int month) {
        return salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYearForSP(sclCustomerModels, month, year);
    }
*/
    @SuppressWarnings("unchecked")
    @Override
    public Double getActualTargetForSalesForMonthSP(List<SclCustomerModel> sclCustomerModels, int year, int month) {
        return (Double) sessionService.executeInLocalView(new SessionExecutionBody()
        {
            @Override
            public Double execute()
            {
                try {
                    searchRestrictionService.disableSearchRestrictions();
                    return salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYearForSP(sclCustomerModels, month, year);
                }
                finally {
                    searchRestrictionService.enableSearchRestrictions();
                }
            }
        });
    }


    @Override
    public Double getActualTargetForSalesForMonthRetailer(SclCustomerModel sclCustomer, BaseSiteModel currentBaseSite, int year, int month, String bgpFilter) {
        return salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYearForRetailer(sclCustomer, currentBaseSite, month, year, bgpFilter);
    }

    @Override
    public Double getActualTargetForPartnerSalesForMonth(String code, SclUserModel sclUser, BaseSiteModel currentBaseSite, int year, int month, List<String> doList, List<String> subAreaList) {
        return salesPerformanceDao.getActualTargetPartnerSalesForSelectedMonthAndYear(code,sclUser, currentBaseSite, year, month, doList, subAreaList);
    }

    @Override
    public Double getActualTargetForSalesForMonth(String subArea, SclUserModel sclUser,
                                                  BaseSiteModel currentBaseSite, int year, int month) {
        return salesPerformanceDao.getActualTargetSalesForSelectedMonthAndYear(sclUser, currentBaseSite, month, year, null, null);
    }

    @Override
    public Double getActualTargetSalesForSelectedMonthAndYear(SclUserModel sclUser, BaseSiteModel baseSite, int month, int year, List<String> doList, List<String> subAreaList) {
        return getSalesPerformanceDao().getActualTargetSalesForSelectedMonthAndYear(sclUser, baseSite, month, year, doList, subAreaList);
    }

    public List<List<Object>> getProductwiseSalesPercentRatioAndVolumeRatioForYTD(SclUserModel sclUser, BaseSiteModel baseSite, List<String> doList, List<String> subAreaList) {
        List<Date> dates = getCurrentFinancialYear();
        Date startDate = null;
        Date endDate = null;
        startDate = dates.get(0);
        endDate = dates.get(1);
        return salesPerformanceDao.getProductwiseSalesPercentRatioAndVolumeRatioForYTD(sclUser, baseSite, startDate, endDate, doList, subAreaList);
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

    public TerritoryMasterService getTerritoryMasterService() {
        return territoryMasterService;
    }

    public void setTerritoryMasterService(TerritoryMasterService territoryMasterService) {
        this.territoryMasterService = territoryMasterService;
    }

    @Override
    public List<SclCustomerModel> getCustomersByLeadType(String leadType,List<String> territoryList,List<String> subAreaList,List<String> districtList) {
        Collection<SclCustomerModel> counterList =new ArrayList<>();
        List<SclCustomerModel> customerFilteredList = new ArrayList<>();
        List<SubAreaMasterModel> subAreaListModel=new ArrayList<>();
        RequestCustomerData data=new RequestCustomerData();
        if(CollectionUtils.isNotEmpty(subAreaList)){
            for (String id : subAreaList) {
                subAreaListModel.add(territoryManagementService.getTerritoryById(id));
            }
            if(!subAreaListModel.isEmpty() && subAreaListModel.get(0) != null)
                data.setSubAreaMasterPk(subAreaListModel.get(0).getPk().toString());
        }
        List<DistrictMasterModel> districtListModel=new ArrayList<>();
        if(CollectionUtils.isNotEmpty(districtList)){
            for (String code : districtList) {
                districtListModel.add(districtMasterDao.findByCode(code));
            }
            if(!districtListModel.isEmpty() && districtListModel.get(0) != null)
                data.setDistrict(districtListModel.get(0).getName());
        }

        if(CollectionUtils.isNotEmpty(subAreaListModel) && CollectionUtils.isNotEmpty(districtListModel)) {
            counterList = territoryManagementService.getSCLAndNonSCLDealersRetailersForSubArea(subAreaListModel.get(0), districtListModel.get(0));

        }else if(CollectionUtils.isNotEmpty(subAreaListModel) && CollectionUtils.isEmpty(districtListModel)) {
            counterList = territoryManagementService.getSCLAndNonSCLDealersRetailersForSubArea(subAreaListModel.get(0),null);
        }
        else if(CollectionUtils.isNotEmpty(districtListModel) && CollectionUtils.isEmpty(subAreaListModel)) {
                counterList = territoryManagementService.getSCLAndNonSCLDealersRetailersForSubArea(null,districtListModel.get(0));
        }else{
            counterList = territoryManagementService.getSCLAndNonSCLDealersRetailersForSubArea(null,null);
        }
         LOGGER.info(String.format("counterList---::%s",counterList));
//dealer/retailer change - source from territory master and dealerretailer mapping
        if(CollectionUtils.isNotEmpty(counterList)) {
            List<SclCustomerModel> filterCounterList = djpVisitService.filterCustomerByDOTerritoryCode(List.copyOf(counterList));
            if (CollectionUtils.isNotEmpty(filterCounterList)) {
                if (leadType.equalsIgnoreCase(RETAILER)) {
                    customerFilteredList = filterCounterList.stream().filter(sclCustomerModel -> sclCustomerModel.getGroups().contains(getUserService().getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))).filter(sclCustomerModel -> sclCustomerModel.getCustomerNo()!=null).collect(Collectors.toList());
                } else if (leadType.equalsIgnoreCase(DEALER)) {
                    customerFilteredList = filterCounterList.stream().filter(sclCustomerModel -> sclCustomerModel.getGroups().contains(getUserService().getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))).filter(sclCustomerModel -> sclCustomerModel.getCustomerNo()!=null).collect(Collectors.toList());
                }
            }
        }
//no change
            if (leadType.equalsIgnoreCase(INFLUENCER)) {
                data.setCounterType(List.of(INFLUENCER));
                    customerFilteredList = territoryManagementService.getCustomerforUser(data);
            } else if (leadType.equalsIgnoreCase(SITE)) {
                data.setCounterType(List.of(SITE));
                customerFilteredList = territoryManagementService.getCustomerforUser(data);
            }
                LOGGER.info("customer size inside customers By lead Type Dealer/Retailer/Inf/site:" + customerFilteredList.size());
        return customerFilteredList;
    }

    /**
     * @param productCode
     * @param dealerCode
     * @param invoicedQuantity
     */
    @Override
    public void updateReceipts(ProductModel productCode, SclCustomerModel dealerCode, Double invoicedQuantity) {

        String stockInfluencerMultiplyValue=dataConstraintDao.findVersionByConstraintName("STOCK_INFLUENCER_MULTIPLY_VALUE");
        try {
            ReceiptAllocaltionModel receiptAllocate = dealerDao.getDealerAllocation(productCode, dealerCode);
            if (null != receiptAllocate) {
                Double updatedQty = receiptAllocate.getReceipt() + invoicedQuantity;
                receiptAllocate.setReceipt((null != updatedQty)?updatedQty.intValue():0);
                Integer salesToRetailer = receiptAllocate.getSalesToRetailer()!=null ? receiptAllocate.getSalesToRetailer() : 0;
                Integer salesToInfluencer = receiptAllocate.getSalesToInfluencer()!=null ? receiptAllocate.getSalesToInfluencer() :0;
                int stockRetailer = Math.abs(receiptAllocate.getReceipt() - salesToRetailer
                        - salesToInfluencer);
                receiptAllocate.setStockAvlForRetailer(stockRetailer);
                   if(StringUtils.isNotBlank(stockInfluencerMultiplyValue)) {
                       int stockInfluencer = Math.abs((int) ((Double.valueOf(stockInfluencerMultiplyValue) * (receiptAllocate.getReceipt() - salesToRetailer))
                               - salesToInfluencer));
                       receiptAllocate.setStockAvlForInfluencer(stockInfluencer);
                   }
                receiptAllocate.setYear(LocalDate.now().getYear());
                receiptAllocate.setMonth(LocalDate.now().getMonthValue());
                modelService.save(receiptAllocate);
            } else {
                //If product and dealer is not found in the ReceiptAllocation
                //then it means new entry has to be made as order is placed with this combination
                ReceiptAllocaltionModel receiptAllocateNew = modelService.create(ReceiptAllocaltionModel.class);
                receiptAllocateNew.setProduct(productCode.getPk().toString());
                receiptAllocateNew.setDealerCode(dealerCode.getPk().toString());
                Double updatedQty = invoicedQuantity;
                receiptAllocateNew.setReceipt((null != updatedQty)?updatedQty.intValue():0);
                receiptAllocateNew.setSalesToRetailer(0);
                receiptAllocateNew.setSalesToInfluencer(0);
                int stockRetailer = Math.abs(receiptAllocateNew.getReceipt() - receiptAllocateNew.getSalesToRetailer()
                        - receiptAllocateNew.getSalesToInfluencer());
                receiptAllocateNew.setStockAvlForRetailer(stockRetailer);
                if(StringUtils.isNotBlank(stockInfluencerMultiplyValue)) {
                    int stockInfluencer = Math.abs((int) ((Double.valueOf(stockInfluencerMultiplyValue) * (receiptAllocateNew.getReceipt() - receiptAllocateNew.getSalesToRetailer()))
                            - receiptAllocateNew.getSalesToInfluencer()));
                    receiptAllocateNew.setStockAvlForInfluencer(stockInfluencer);
                }
                receiptAllocateNew.setYear(LocalDate.now().getYear());
                receiptAllocateNew.setMonth(LocalDate.now().getMonthValue());
                modelService.save(receiptAllocateNew);
                modelService.refresh(receiptAllocateNew);
            }
        } catch (Exception e) {
            String errorMsg = e.getMessage()!=null?e.getMessage():e.getClass().getName() + " Order Replication Job updateReceipts Occurred";
            throw new UnknownIdentifierException(errorMsg);
        }

    }

    @Override
    public List<SclCustomerModel> getCustomersByTerritoryCode(List<SclCustomerModel> customerList, List<String> territoryList) {
            List<TerritoryMasterModel> territoryMasterModelList = new ArrayList<>();
            List<SclCustomerModel> filterdDealerList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(customerList) && CollectionUtils.isNotEmpty(territoryList)) {
                for (String s : territoryList) {
                    TerritoryMasterModel territoryById = territoryMasterService.getTerritoryById(s);
                    territoryMasterModelList.add(territoryById);
                }
                if (CollectionUtils.isNotEmpty(territoryMasterModelList) && CollectionUtils.isNotEmpty(customerList)) {
                    for (SclCustomerModel sclCustomerModel : customerList) {
                        if (territoryMasterModelList.contains(sclCustomerModel.getTerritoryCode())) {
                            LOGGER.info(String.format("territoryModels dealer check inside target ::%s ", territoryMasterModelList.contains(sclCustomerModel.getTerritoryCode())));
                            filterdDealerList.add(sclCustomerModel);
                        }
                        LOGGER.info(String.format("Filter Dealer list inside target ::%s ", filterdDealerList.size()));
                    }
                }
            } else {
                filterdDealerList.addAll(customerList);
            }
            return filterdDealerList;
        }

    @Override
    public Map<String, Object> getPotentialForCustomer(SclCustomerModel sclCustomer,String startDate,String endDate) {
        return salesPerformanceDao.getPotentialForCustomer(sclCustomer,startDate,endDate);
    }

    @Override
    public Map<String, Object> getSelfBrandSaleforCustomer(SclCustomerModel sclCustomer, String countervistId, BrandModel brand) {
        return salesPerformanceDao.getSelfBrandSaleforCustomer(sclCustomer,countervistId,brand);
    }

    public DealerCurrentNetworkListData getCurrentNetworkCustomers(String leadType,String networkType,String searchKeyFilter, boolean sclExclusiveCustomer){
        DealerCurrentNetworkListData listData=new DealerCurrentNetworkListData();
        List<DealerCurrentNetworkData> currentNetworkData=new ArrayList<>();
        List<SclCustomerModel> currentNetworkCustomers=new ArrayList<>();
        List<TerritoryMasterModel> territoryMasterModels= (List<TerritoryMasterModel>) territoryMasterService.getCurrentTerritory();
        LOGGER.info(String.format("territoryMasterModels:: %s",territoryMasterModels));
        B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();
        if(currentUser instanceof SclUserModel){
            currentNetworkCustomers = salesPerformanceDao.getCurrentNetworkCustomers(leadType, networkType, territoryMasterModels, searchKeyFilter,true,false,false);
        }else if(currentUser instanceof  SclCustomerModel){
            if(currentUser.getGroups().contains(getUserService().getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                currentNetworkCustomers = territoryManagementService.getRetailerListForDealer();
            }else  if(currentUser.getGroups().contains(getUserService().getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                currentNetworkCustomers = territoryManagementService.getInfluencerListForDealer();
            }
        }
        if(CollectionUtils.isNotEmpty(currentNetworkCustomers)){
            if(leadType.equalsIgnoreCase(DEALER)){
                List<DealerCurrentNetworkData> dealerDetailedSummaryListData = getDealerDetailedSummaryListData(currentNetworkCustomers);
                listData.setDealerCurrentNetworkList(dealerDetailedSummaryListData);
            }else if(leadType.equalsIgnoreCase(RETAILER)) {
                List<DealerCurrentNetworkData> retailerDetailedSummaryListData = getRetailerDetailedSummaryListData(currentNetworkCustomers);
                listData.setDealerCurrentNetworkList(retailerDetailedSummaryListData);
            }
            /*List<DealerCurrentNetworkData> convertedList = currentNetworkDetailsConverter.convertAll(currentNetworkCustomers);
            currentNetworkData.addAll(convertedList.stream().sorted(Comparator.comparing(DealerCurrentNetworkData::getName)).collect(Collectors.toList()));
            listData.setDealerCurrentNetworkList(currentNetworkData);*/
        }
        return listData;
    }
    @Override
    public CounterShareResponseData getCounterShareData(CounterShareData counterShareData) {
        CounterShareResponseData data=new CounterShareResponseData();
        Double lastMonthsales=0.0,lastToLastMonthsales=0.0;
        LocalDate firstDayOfMonth=null,lastDayOfMonth=null;
        String id=null;
        LocalDate lastMonth=null;
        BrandModel brand=null;
        Map<String, Double> salesQty=new HashMap<>();
        Map<String, Object> potentailforCustomer=new HashMap<>();
        Map<String, Object> selfBrandSaleMap=new HashMap<>();
        double selfBrandSale=0.0,potentail=0.0,numeratorCounterPotentail=0.0,counterShare=0.0,lastTwoCompletedMonthSales=0.0;
        if(Objects.nonNull(counterShareData)) {
            SclCustomerModel customerModel = (SclCustomerModel) userService.getUserForUID(counterShareData.getDealerCode());
            if((counterShareData.getMonth()==0 && counterShareData.getYear()==0) || (counterShareData.getMonth()==null && counterShareData.getYear()==null) ||
                    counterShareData.getMonth().equals(LocalDate.now().getMonth().getValue()) && counterShareData.getYear().equals(LocalDate.now().getYear())) {
                firstDayOfMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
                lastDayOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).plusDays(1);
                if(customerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                    lastMonthsales = sclSalesSummaryService.getLastMonthSales((B2BCustomerModel) userService.getUserForUID(counterShareData.getDealerCode()), Collections.EMPTY_LIST);
                    lastToLastMonthsales = sclSalesSummaryService.getLastToLastMonthSales((B2BCustomerModel) userService.getUserForUID(counterShareData.getDealerCode()), Collections.EMPTY_LIST);
                }else if(customerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                    lastMonth = LocalDate.now().minusMonths(1);
                    salesQty = networkService.getSalesQuantityForRetailerByMonthYear(customerModel, lastMonth.getMonthValue(),lastMonth.getYear(),  null, null);
                    lastMonthsales = salesQty.get("quantityInMT");
                    salesQty = networkService.getSalesQuantityForRetailerByMonthYear(customerModel, lastMonth.getMonthValue()-1,lastMonth.getYear(),  null, null);
                    lastToLastMonthsales=salesQty.get("quantityInMT");
                }
                lastTwoCompletedMonthSales = (lastMonthsales + lastToLastMonthsales) / 2;
            }
            else if(counterShareData.getMonth()!=0 && counterShareData.getYear()!=0) {
                YearMonth of = YearMonth.of(counterShareData.getYear(), Month.of(counterShareData.getMonth()));
                LocalDate date = LocalDate.of(counterShareData.getYear(), Month.of(counterShareData.getMonth()), of.lengthOfMonth());
                firstDayOfMonth = date.with(TemporalAdjusters.firstDayOfMonth());
                lastDayOfMonth = date.with(TemporalAdjusters.lastDayOfMonth()).plusDays(1);

                if (customerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                    lastMonthsales = sclSalesSummaryService.getSalesByMonth(customerModel, counterShareData.getMonth(), counterShareData.getYear(), Collections.EMPTY_LIST);
                    lastToLastMonthsales = sclSalesSummaryService.getSalesByMonth(customerModel, counterShareData.getMonth() - 1, counterShareData.getYear(), Collections.EMPTY_LIST);
                } else if (customerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                    salesQty = networkService.getSalesQuantityForRetailerByMonthYear(customerModel, counterShareData.getMonth(), counterShareData.getYear(), null, null);
                    lastMonthsales = salesQty.get("quantityInMT");
                    salesQty = networkService.getSalesQuantityForRetailerByMonthYear(customerModel, counterShareData.getMonth() - 1, counterShareData.getYear(), null, null);
                    lastToLastMonthsales = salesQty.get("quantityInMT");
                }
                lastTwoCompletedMonthSales = (lastMonthsales + lastToLastMonthsales) / 2;
            }
            potentailforCustomer = getPotentialForCustomer((SclCustomerModel) userService.getUserForUID(counterShareData.getDealerCode()), firstDayOfMonth.toString(), lastDayOfMonth.toString());
            if(Objects.nonNull(potentailforCustomer)) {
                if (potentailforCustomer.get("id") != null) {
                    id = (String) potentailforCustomer.get("id");
                    //potentail from counterVisitMaster
                    if(potentailforCustomer.get("counterPotential")!=null) {
                        potentail = (double) potentailforCustomer.get("counterPotential");
                    }
                    brand = brandDao.findBrandById("BANGUR");
                    if (brand != null) {
                        if(potentailforCustomer.get("uid")!=null) {
                            selfBrandSaleMap = getSelfBrandSaleforCustomer((SclCustomerModel) userService.getUserForUID((String) potentailforCustomer.get("uid")), id, brand);

                            if (selfBrandSaleMap.get("retailSales") != null && selfBrandSaleMap.get("wholeSales") != null)
                                //self Brand Sales from Market Mapping
                                selfBrandSale = (double) selfBrandSaleMap.get("retailSales") + (double) selfBrandSaleMap.get("wholeSales");
                        }
                    }
                }
            }

                  /*   //Total Sales
                     lastMonthsales=sclSalesSummaryService.getLastMonthSales((B2BCustomerModel) userService.getUserForUID(counterShareData.getDealerCode()),Collections.EMPTY_LIST);
                     lastToLastMonthsales=sclSalesSummaryService.getLastToLastMonthSales((B2BCustomerModel) userService.getUserForUID(counterShareData.getDealerCode()),Collections.EMPTY_LIST);
                     lastTwoCompletedMonthSales=(lastMonthsales+lastToLastMonthsales)/2;*/

            //To set Numerator Sales
            if(selfBrandSale>0 && lastTwoCompletedMonthSales>0){
                numeratorCounterPotentail=Math.min(selfBrandSale,lastTwoCompletedMonthSales);
            }else if(selfBrandSale==0.0){
                numeratorCounterPotentail=lastTwoCompletedMonthSales;
            }
            else if(lastTwoCompletedMonthSales==0.0){
                numeratorCounterPotentail=selfBrandSale;
            }
            else{
                numeratorCounterPotentail=0.0;
            }

            //To set counterShare
            if(potentail>0){
                if(numeratorCounterPotentail==0.0)
                    counterShare=0.0;
                else {
                    if(numeratorCounterPotentail>potentail){
                        counterShare=100;
                    }else{
                        counterShare=numeratorCounterPotentail*100/potentail;
                    }
                }
            }
        }
        data.setCounterShare(counterShare);
        data.setSelfBrandSale(selfBrandSale);
        data.setPotential(potentail);
        data.setNumeratorSales(numeratorCounterPotentail);
        data.setTotalSales(lastTwoCompletedMonthSales);
        if (potentailforCustomer.get("id") != null)
            data.setCounterVisitId(id);
        if (potentailforCustomer.get("endVisitTime") != null) {
            String pattern = "yyyy-MM-dd HH:mm:ss";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String date = simpleDateFormat.format(potentailforCustomer.get("endVisitTime"));
            data.setEndVisitTime(date);
        }

        return data;
    }
   @Override
    public List<SalesPerformNetworkDetailsData> getRetailerDetailedSummaryListDataForSP(List<SclCustomerModel> retailerList) {

       List<List<Object>> list= new ArrayList<>();
       List<List<Object>> listLastMonth= new ArrayList<>();
       List<List<Object>> lastYTD=new ArrayList<>();
       List<List<Object>> currentYTD=new ArrayList<>();

       Map<String, Double>  map=new HashMap<>();
       Map<String, Double>  mapLastMonth=new HashMap<>();
       Map<String, Double>  mapCurrentYTD=new HashMap<>();
       Map<String, Double>  mapLastYTD=new HashMap<>();

       String startDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).toString();
       String endDate = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).plusDays(1).toString();

       String startDateLastMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).minusMonths(1).toString();
       String endDateLastMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).minusMonths(1).toString();

       LocalDate currentYearCurrentDate= LocalDate.now().plusDays(1);
       LocalDate currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear(), Month.APRIL, 1);
       if(currentYearCurrentDate.getMonth().compareTo(Month.APRIL)<0) {
           currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear()-1, Month.APRIL, 1);
       }
       LocalDate lastYearCurrentDate= currentYearCurrentDate.minusYears(1).plusDays(1);//2022-04-02

       LocalDate lastFinancialYearDate = currentFinancialYearDate.minusYears(1);//2022-04-01

       B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();

       if(currentUser instanceof SclUserModel){
           list = networkService.getSalesForRetailerList(retailerList, null,startDate,endDate);
           listLastMonth = networkService.getSalesForRetailerList(retailerList, null,startDateLastMonth,endDateLastMonth);

           map = list.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                   .collect(Collectors.toMap(each->((SclCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));
           mapLastMonth = listLastMonth.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                   .collect(Collectors.toMap(each->((SclCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));

           currentYTD = networkService.getSalesForRetailerList(retailerList, null,currentFinancialYearDate.toString(), currentYearCurrentDate.toString());
           mapCurrentYTD = currentYTD.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                   .collect(Collectors.toMap(each->((SclCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));

           lastYTD = networkService.getSalesForRetailerList(retailerList, null,lastFinancialYearDate.toString(), lastYearCurrentDate.toString());

           mapLastYTD = lastYTD.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                   .collect(Collectors.toMap(each->((SclCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));


       }else if((currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))){
           list = networkService.getSalesForRetailerList(retailerList, null,startDate,endDate);
           listLastMonth = networkService.getSalesForRetailerList(retailerList, null,startDateLastMonth,endDateLastMonth);

           map = list.stream().filter(each->each!=null && each.size()>2 && each.get(0)!=null && each.get(2)!=null)
                   .collect(Collectors.toMap(each->((SclCustomerModel)each.get(0)).getUid(), each->(Double)each.get(2)));
           mapLastMonth = listLastMonth.stream().filter(each->each!=null && each.size()>2 && each.get(0)!=null && each.get(2)!=null)
                   .collect(Collectors.toMap(each->((SclCustomerModel)each.get(0)).getUid(), each->(Double)each.get(2)));


           currentYTD = networkService.getSalesForRetailerList(retailerList, null,currentFinancialYearDate.toString(), currentYearCurrentDate.toString());
           mapCurrentYTD = currentYTD.stream().filter(each->each!=null && each.size()>2 && each.get(0)!=null && each.get(2)!=null)
                   .collect(Collectors.toMap(each->((SclCustomerModel)each.get(0)).getUid(), each->(Double)each.get(2)));

           lastYTD = networkService.getSalesForRetailerList(retailerList, null,lastFinancialYearDate.toString(), lastYearCurrentDate.toString());

           mapLastYTD = lastYTD.stream().filter(each->each!=null && each.size()>2 && each.get(0)!=null && each.get(2)!=null)
                   .collect(Collectors.toMap(each->((SclCustomerModel)each.get(0)).getUid(), each->(Double)each.get(2)));

       }
       else if(currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))){
           list = networkService.getSalesForRetailerList(retailerList, (SclCustomerModel) currentUser,startDate,endDate);

           listLastMonth = networkService.getSalesForRetailerList(retailerList, (SclCustomerModel) currentUser,startDateLastMonth,endDateLastMonth);

           map = list.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                   .collect(Collectors.toMap(each->((SclCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));
           mapLastMonth = listLastMonth.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                   .collect(Collectors.toMap(each->((SclCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));

           currentYTD = networkService.getSalesForRetailerList(retailerList, (SclCustomerModel) currentUser,currentFinancialYearDate.toString(), currentYearCurrentDate.toString());
           mapCurrentYTD = currentYTD.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                   .collect(Collectors.toMap(each->((SclCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));

           lastYTD = networkService.getSalesForRetailerList(retailerList, (SclCustomerModel) currentUser,lastFinancialYearDate.toString(), lastYearCurrentDate.toString());

           mapLastYTD = lastYTD.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                   .collect(Collectors.toMap(each->((SclCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));

       }

       List<List<Object>> monthlySalesTargetForRetailer = getMonthlySalesTargetForRetailer(retailerList);
       Map<String,Double> mapTarget = monthlySalesTargetForRetailer.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
               .collect(Collectors.toMap(each->((String)each.get(0)), each->(Double)each.get(1)));


       Map<String, Double> finalMap = map;
       Map<String, Double> finalMapLastMonth = mapLastMonth;
       Map<String, Double> finalMapCurrentYTD = mapCurrentYTD;
       Map<String, Double> finalMapLastYTD1 = mapLastYTD;

       List<SalesPerformNetworkDetailsData> summaryDataList = new ArrayList<>();
       retailerList.forEach(retailer -> {
           SalesPerformNetworkDetailsData dealerCurrentNetworkData = new SalesPerformNetworkDetailsData();
           var subAraMappinglist = territoryManagementService.getTerritoriesForCustomer(retailer);
           dealerCurrentNetworkData.setCode(retailer.getUid());
           if(retailer.getContactNumber()!=null){
               dealerCurrentNetworkData.setContactNumber(retailer.getContactNumber());
           }
           dealerCurrentNetworkData.setName(retailer.getName());
           dealerCurrentNetworkData.setCustomerNo(retailer.getCustomerNo());

           double salesMtd=0.0,salesMtdLastMonth=0.0,salesLastYear=0.0,salesCurrentYear=0.0,target=0.0;

           if(!(currentUser.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID)))) {
               if (retailer.getCounterPotential() != null) {
                   dealerCurrentNetworkData.setPotential(String.valueOf(retailer.getCounterPotential()));
               } else {
                   dealerCurrentNetworkData.setPotential("0");
               }
           }else {
               if (retailer.getCounterPotential() != null) {
                   dealerCurrentNetworkData.setPotential(String.valueOf((retailer.getCounterPotential() * 1000) / 50));
               } else {
                   dealerCurrentNetworkData.setPotential("0");
               }
           }
           if(finalMap.containsKey(retailer.getUid())){
               salesMtd+=finalMap.get(retailer.getUid());
           }

           if(finalMapLastMonth.containsKey(retailer.getUid())){
               salesMtdLastMonth+=finalMapLastMonth.get(retailer.getUid());
           }

           if(finalMapCurrentYTD.containsKey(retailer.getUid())){
               salesCurrentYear+=finalMapCurrentYTD.get(retailer.getUid());
           }

           if(finalMapLastYTD1.containsKey(retailer.getUid())){
               salesLastYear+=finalMapLastYTD1.get(retailer.getUid());
           }

           if(mapTarget.containsKey(retailer.getUid())){
               target=mapTarget.get(retailer.getUid());
           }

           SalesQuantityData sales = new SalesQuantityData();
           sales.setRetailerSaleQuantity(salesMtd);
           sales.setCurrent(salesMtd);
           sales.setLastMonth(salesMtdLastMonth);
           sales.setLastYear(salesLastYear);
           sales.setActual(salesMtd);

           dealerCurrentNetworkData.setSalesQuantity(sales);

           dealerCurrentNetworkData.setSalesYtd(df.format(salesCurrentYear));
           dealerCurrentNetworkData.setGrowthRateYoYPercentage(df.format(getYearToYearGrowth(salesCurrentYear,salesLastYear)));
           if(retailer.getLastLiftingDate()!=null) {
               dealerCurrentNetworkData.setDaySinceLastLifting(salesPerformanceService.getDaysFromLastOrder(retailer));
           }else{
               dealerCurrentNetworkData.setDaySinceLastLifting("-");
           }
           if(CollectionUtils.isNotEmpty(subAraMappinglist)) {
               var subareaMaster=subAraMappinglist.get(0);
               dealerCurrentNetworkData.setDistrict(subareaMaster.getDistrict());
               dealerCurrentNetworkData.setTaluka(subareaMaster.getTaluka());
           }
           //       String monthName = Month.of(LocalDate.now().getMonthValue()).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
           dealerCurrentNetworkData.setTarget(String.valueOf(target));
           dealerCurrentNetworkData.setCounterSharePercentage(salesPerformanceService.getCounterShareForDealer(retailer,LocalDate.now().getMonthValue(), LocalDate.now().getYear()));
           summaryDataList.add(dealerCurrentNetworkData);
       });
       /* AtomicInteger rank=new AtomicInteger(1);
        summaryDataList.stream().sorted(Comparator.comparing(nw -> nw.getSalesQuantity().getRetailerSaleQuantity())).forEach(data->data.setRank(String.valueOf(rank.getAndIncrement())));*/
       return summaryDataList;
   }

    @Override
    public List<SalesPerformNetworkDetailsData> getInfluencerDetailedSummaryListData(List<SclCustomerModel> influencerList, List<String> doList, List<String> territoryList) {

        List<SalesPerformNetworkDetailsData> summaryDataList = new ArrayList<>();
        String startDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).toString();
        String endDate = LocalDate.now().plusDays(1).toString();
        List<List<Object>> list = pointRequisitionDao.getBagLiftedMTDforInfluencers(influencerList, startDate, endDate,null,territoryList);
        Map<String, Double>  map = list.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                .collect(Collectors.toMap(each->((SclCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));

        LocalDate currentYearCurrentDate= LocalDate.now();
        LocalDate currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear(), Month.APRIL, 1);
        if(currentYearCurrentDate.getMonth().compareTo(Month.APRIL)<0) {
            currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear()-1, Month.APRIL, 1);
        }
        LocalDate lastYearCurrentDate= currentYearCurrentDate.minusYears(1);

        LocalDate lastFinancialYearDate = currentFinancialYearDate.minusYears(1);

        List<List<Object>> currentYTD = pointRequisitionDao.getBagLiftedMTDforInfluencers(influencerList, currentFinancialYearDate.toString(), currentYearCurrentDate.toString(),null,territoryList);
        Map<String, Double>  mapCurrentYTD = currentYTD.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                .collect(Collectors.toMap(each->((SclCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));

        List<List<Object>> lastYTD = pointRequisitionDao.getBagLiftedMTDforInfluencers(influencerList, lastFinancialYearDate.toString(), lastYearCurrentDate.toString(),null,territoryList);

        Map<String, Double>  mapLastYTD = lastYTD.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                .collect(Collectors.toMap(each->((SclCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));

        influencerList.forEach(influencer -> {
            SalesPerformNetworkDetailsData dealerCurrentNetworkData=new SalesPerformNetworkDetailsData();
            dealerCurrentNetworkData.setCode(influencer.getUid());
            dealerCurrentNetworkData.setName(influencer.getName());
            dealerCurrentNetworkData.setCategory(influencer.getInfluencerType() != null ?
                    enumerationService.getEnumerationName(influencer.getInfluencerType()) : "-");
            dealerCurrentNetworkData.setContactNumber(influencer.getContactNumber());
            dealerCurrentNetworkData.setTimesContacted(influencer.getTimesContacted());
            dealerCurrentNetworkData.setType(influencer.getNetworkType());
            dealerCurrentNetworkData.setPoints(Objects.nonNull(influencer.getAvailablePoints()) ? influencer.getAvailablePoints() : 0.0);

            var subAraMappinglist = territoryManagementService.getTerritoriesForCustomer(influencer);
            var bagLifted = 0.0;
            if(map.containsKey(influencer.getUid())) {
                bagLifted = map.get(influencer.getUid());
            }
            var salesQuantity = (bagLifted / 20);
            dealerCurrentNetworkData.setBagLifted(bagLifted);
            dealerCurrentNetworkData.setBagLiftedNo(bagLifted);
            dealerCurrentNetworkData.setBagLiftedQty(String.valueOf(salesQuantity));
            if(influencer.getLastLiftingDate()!=null) {
                LocalDate today = LocalDate.now();
                LocalDate transactionDate = influencer.getLastLiftingDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                dealerCurrentNetworkData.setDaySinceLastLifting(String.valueOf(Math.toIntExact(ChronoUnit.DAYS.between(transactionDate,today))));
            }
            if(CollectionUtils.isNotEmpty(subAraMappinglist)) {
                var subareaMaster=subAraMappinglist.get(0);
                dealerCurrentNetworkData.setDistrict(subareaMaster.getDistrict());
                dealerCurrentNetworkData.setTaluka(subareaMaster.getTaluka());
            }
            dealerCurrentNetworkData.setPotential(Objects.nonNull(influencer.getCounterPotential()) ? String.valueOf(influencer.getCounterPotential()) : "0");

            double salesCurrentYear = 0.0;
            if(mapCurrentYTD.containsKey(influencer.getUid())) {
                salesCurrentYear = mapCurrentYTD.get(influencer.getUid());
            }
            double salesCurrentYearQty = (salesCurrentYear / 20);

            double salesLastYear = 0.0;
            if(mapLastYTD.containsKey(influencer.getUid())) {
                salesLastYear = mapLastYTD.get(influencer.getUid());
            }
            double salesLastYearQty = (salesLastYear / 20);
            dealerCurrentNetworkData.setSalesYtd(df.format(salesCurrentYearQty));
            dealerCurrentNetworkData.setGrowthRateYoYPercentage(df.format(getYearToYearGrowth(salesCurrentYearQty,salesLastYearQty)));

            summaryDataList.add(dealerCurrentNetworkData);
        });
        /*AtomicInteger rank=new AtomicInteger(1);
        summaryDataList.stream().sorted(Comparator.comparing(SalesPerformNetworkDetailsData::getBagLifted).reversed()).forEach(infdata-> infdata.setRank(String.valueOf(rank.getAndIncrement())));*/
        return summaryDataList;
    }

    @Override
    public List<SclCustomerModel> getCurrentNetworkCustomersForTSMRH(RequestCustomerData requestCustomerData) {
        return salesPerformanceDao.getCurrentNetworkCustomersForTSMRH(requestCustomerData);
    }
}
