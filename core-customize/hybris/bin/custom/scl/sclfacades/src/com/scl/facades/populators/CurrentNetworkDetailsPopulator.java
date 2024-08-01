package com.scl.facades.populators;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.DJPVisitDao;
import com.scl.core.dao.SclSalesSummaryDao;
import com.scl.core.model.SalesSummaryModel;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.model.SclUserModel;
import com.scl.core.services.NetworkService;
import com.scl.core.services.SalesPerformanceService;
import com.scl.core.services.SclSalesSummaryService;
import com.scl.core.services.TerritoryMasterService;
import com.scl.facades.SalesPerformanceFacade;
import com.scl.facades.data.CounterShareData;
import com.scl.facades.data.CounterShareResponseData;
import com.scl.facades.data.DealerCurrentNetworkData;
import com.scl.facades.data.SalesQuantityData;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

public class CurrentNetworkDetailsPopulator  implements Populator<SclCustomerModel, DealerCurrentNetworkData> {
    private static final Logger LOG = Logger.getLogger(CurrentNetworkDetailsPopulator.class);
    DecimalFormat df = new DecimalFormat("#0.00");

    @Autowired
    SclSalesSummaryService sclSalesSummaryService;
    @Autowired
    SclSalesSummaryDao salesSummaryDao;
    @Autowired
    SalesPerformanceService salesPerformanceService;
    @Autowired
    SalesPerformanceFacade salesPerformanceFacade;
    @Autowired
    BaseSiteService baseSiteService;
    @Autowired
    DJPVisitDao djpVisitDao;
    @Autowired
    TerritoryMasterService territoryMasterService;
    @Autowired
    NetworkService networkService;
    @Autowired
    UserService userService;
    @Override
    public void populate(SclCustomerModel customerModel, DealerCurrentNetworkData dealerCurrentNetworkData) throws ConversionException {
        dealerCurrentNetworkData.setCode(customerModel.getUid());
        dealerCurrentNetworkData.setName(customerModel.getName());
        dealerCurrentNetworkData.setCustomerNo(customerModel.getCustomerNo());
        dealerCurrentNetworkData.setContactNumber(customerModel.getMobileNumber());
        dealerCurrentNetworkData.setCounterShare(String.valueOf(salesPerformanceService.getCounterShareForDealer(customerModel, LocalDate.now().getMonthValue(), LocalDate.now().getYear())));
        List<List<Object>> districtAndTaluka = territoryMasterService.getDistrictAndTalukaForCustomer(customerModel);
        dealerCurrentNetworkData.setDaySinceLastOrder(salesPerformanceService.getDaysFromLastOrder(customerModel));
        if (districtAndTaluka != null && CollectionUtils.isNotEmpty(districtAndTaluka)) {
            for (List<Object> list : districtAndTaluka) {
                if (list.get(0) != null) {
                    dealerCurrentNetworkData.setTaluka(StringUtils.isNotBlank(String.valueOf(list.get(0))) ? String.valueOf(list.get(0)) : "");
                }
                if (list.get(1) != null) {
                    dealerCurrentNetworkData.setDistrict(StringUtils.isNotBlank(String.valueOf(list.get(1))) ? String.valueOf(list.get(1)) : "");
                }
            }
        }

        if (customerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
            dealerCurrentNetworkData.setPotential(Objects.nonNull(customerModel.getCounterPotential()) ? String.valueOf(customerModel.getCounterPotential()) : " 0");
            double target = salesPerformanceService.getMonthlySalesTargetForDealer(customerModel, baseSiteService.getCurrentBaseSite(), null);
            dealerCurrentNetworkData.setTarget(df.format(target));
            dealerCurrentNetworkData.setSalesQuantity(salesPerformanceService.setSalesQuantityForCustomer(customerModel, "DEALER", null));
            double totalOutstanding = djpVisitDao.getDealerOutstandingAmount(customerModel.getCustomerNo());
            dealerCurrentNetworkData.setOutstandingAmount(df.format(totalOutstanding));
            dealerCurrentNetworkData.setGrowthRate(df.format(salesPerformanceService.getYearToYearGrowthForDealer(customerModel)));
        } else if (customerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
            double salesMtd=0.0,salesQuantity=0.0,salesQuantityLastMonth=0.0,salesMtdLastMonth=0.0,salesLastYearQty=0.0,salesLastYear=0.0,salesCurrentYearQty=0.0,salesCurrentYear=0.0;
            LocalDate currentYearCurrentDate = LocalDate.now();
            LocalDate currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear(), Month.APRIL, 1);
            if (currentYearCurrentDate.getMonth().compareTo(Month.APRIL) < 0) {
                currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear() - 1, Month.APRIL, 1);
            }
            LocalDate lastYearCurrentDate = currentYearCurrentDate.minusYears(1);//2022-04-02

            LocalDate lastFinancialYearDate = currentFinancialYearDate.minusYears(1);//2022-04-01

            LocalDate startDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
            LocalDate finalCurrentFinancialYearDate = currentFinancialYearDate;
            Map<String, Double> salesQuantityForRetailerByMTD = networkService.getSalesQuantityForRetailerByMTD(customerModel, null, null);
            Map<String, Double> salesQuantityForRetailerLastMonth = networkService.getSalesQuantityForRetailerByMonthYear(customerModel, startDate.getMonthValue() - 1, startDate.getYear(), null, null);
            Map<String, Double> salesQuantityForRetailerByYTD = networkService.getSalesQuantityForRetailerByDate(customerModel, finalCurrentFinancialYearDate.toString(), currentYearCurrentDate.toString(), null, null);
            Map<String, Double> salesQuantityForRetailerByLastYTD = networkService.getSalesQuantityForRetailerByDate(customerModel, lastFinancialYearDate.toString(), lastYearCurrentDate.toString(), null, null);

            if (userService.getCurrentUser() instanceof SclUserModel) {
                dealerCurrentNetworkData.setPotential(String.valueOf(customerModel.getCounterPotential() != null ? customerModel.getCounterPotential() : null));
                if (salesQuantityForRetailerByMTD.containsKey("quantityInMT")) {
                    salesMtd = salesQuantityForRetailerByMTD.get("quantityInMT");
                }
                if (salesQuantityForRetailerLastMonth.containsKey("quantityInMT")) {
                    salesMtdLastMonth = salesQuantityForRetailerLastMonth.get("quantityInMT");
                }
                if (salesQuantityForRetailerByYTD.containsKey("quantityInMT")) {
                    salesCurrentYear = salesQuantityForRetailerByYTD.get("quantityInMT");
                }
                if (salesQuantityForRetailerByLastYTD.containsKey("quantityInMT")) {
                    salesLastYear = salesQuantityForRetailerByLastYTD.get("quantityInMT");
                }
            } else {
                dealerCurrentNetworkData.setPotential(String.valueOf(customerModel.getCounterPotential() != null ? customerModel.getCounterPotential() * 20 : null));

                if (salesQuantityForRetailerByMTD.containsKey("quantityInBags")) {
                    salesMtd = salesQuantityForRetailerByMTD.get("quantityInBags");
                }

                if (salesQuantityForRetailerLastMonth.containsKey("quantityInBags")) {
                    if (salesQuantityForRetailerLastMonth.get("quantityInBags") != null) {
                        salesMtdLastMonth = salesQuantityForRetailerLastMonth.get("quantityInBags");
                    }
                }
                if (salesQuantityForRetailerByYTD.containsKey("quantityInBags")) {
                    salesCurrentYear = salesQuantityForRetailerByYTD.get("quantityInBags");
                }
                if (salesQuantityForRetailerByLastYTD.containsKey("quantityInBags")) {
                    salesLastYear = salesQuantityForRetailerByLastYTD.get("quantityInBags");
                }
            }

            SalesQuantityData sales = new SalesQuantityData();
            sales.setRetailerSaleQuantity(salesMtd);
            sales.setCurrent(salesMtd);
            sales.setLastMonth(salesMtdLastMonth);

            dealerCurrentNetworkData.setSalesQuantity(sales);

            dealerCurrentNetworkData.setSalesYtd(df.format(salesCurrentYear));
            dealerCurrentNetworkData.setGrowthRate(df.format(getYearToYearGrowthRetailer(salesCurrentYear, salesLastYear)));

            String monthName = Month.of(LocalDate.now().getMonthValue()).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            String formattedMonth = monthName.concat("-").concat(String.valueOf(LocalDate.now().getYear()));
            dealerCurrentNetworkData.setTarget(String.valueOf(salesPerformanceService.getMonthlySalesTargetForRetailer(customerModel.getUid(), baseSiteService.getCurrentBaseSite(), formattedMonth)));
        }
    }

   private Double getYearToYearGrowthRetailer(double salesCurrentYearQty, double salesLastYearQty){
       if(salesLastYearQty>0) {
           return   (((salesCurrentYearQty- salesLastYearQty) / salesLastYearQty) * 100);
       }
       return 0.0;
   }
    public String getCounterShareForDealer(SclCustomerModel dealer) {
        CounterShareData counterShareData=new CounterShareData();
        counterShareData.setDealerCode(dealer.getUid());
        counterShareData.setMonth(LocalDate.now().getMonthValue());
        counterShareData.setYear(LocalDate.now().getYear());
        CounterShareResponseData responseData = salesPerformanceFacade.getCounterShareData(counterShareData);
        LOG.info(" Counter Share"+responseData.getCounterShare() + " Potential:"+responseData.getPotential()+ " Numerator:"+responseData.getNumeratorSales()+ " SelfBrandSale"+responseData.getSelfBrandSale()+ " TotalSales "+responseData.getTotalSales());
        return String.valueOf(responseData.getCounterShare());
    }
}
