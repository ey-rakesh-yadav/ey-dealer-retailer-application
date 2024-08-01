package com.scl.facades.populators;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.DJPVisitDao;
import com.scl.core.dao.PointRequisitionDao;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.model.SclUserModel;
import com.scl.core.services.NetworkService;
import com.scl.core.services.SalesPerformanceService;
import com.scl.core.services.SclSalesSummaryService;
import com.scl.core.services.TerritoryMasterService;
import com.scl.facades.SalesPerformanceFacade;
import com.scl.facades.data.*;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

public class SalesPerformnceCustomerDetailsPopulator  implements Populator<SclCustomerModel, SalesPerformNetworkDetailsData>

    {
        private static final Logger LOG = Logger.getLogger(SalesPerformnceCustomerDetailsPopulator.class);
        DecimalFormat df = new DecimalFormat("#0.00");

        @Autowired
        SalesPerformanceService salesPerformanceService;
        @Autowired
        PointRequisitionDao pointRequisitionDao;
        @Autowired
        EnumerationService enumerationService;
        @Autowired
        SalesPerformanceFacade salesPerformanceFacade;
        @Autowired
        BaseSiteService baseSiteService;
        @Autowired
        DJPVisitDao djpVisitDao;
        @Autowired
        UserService userService;
        @Autowired
        TerritoryMasterService territoryMasterService;
        @Autowired
        NetworkService networkService;


        @Override
        public void populate(SclCustomerModel customerModel, SalesPerformNetworkDetailsData dealerCurrentNetworkData) throws
        ConversionException {

            dealerCurrentNetworkData.setCode(customerModel.getUid());
            dealerCurrentNetworkData.setName(customerModel.getName());
            dealerCurrentNetworkData.setContactNumber(customerModel.getMobileNumber());
            dealerCurrentNetworkData.setPotential(Objects.nonNull(customerModel.getCounterPotential()) ? String.valueOf(customerModel.getCounterPotential()) : " 0");
            dealerCurrentNetworkData.setType(customerModel.getNetworkType());

            if(customerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.DEALER_USER_GROUP_UID))) {
                dealerCurrentNetworkData.setCounterSharePercentage(salesPerformanceService.getCounterShareForDealer(customerModel, LocalDate.now().getMonthValue(), LocalDate.now().getYear()));
                double target = salesPerformanceService.getMonthlySalesTargetForDealer(customerModel, baseSiteService.getCurrentBaseSite(), null);
                dealerCurrentNetworkData.setTarget(df.format(target));
                dealerCurrentNetworkData.setSalesQuantity(salesPerformanceService.setSalesQuantityForCustomer(customerModel, "DEALER", null));
                double totalOutstanding = djpVisitDao.getDealerOutstandingAmount(customerModel.getCustomerNo());
                dealerCurrentNetworkData.setOutstandingAmount(df.format(totalOutstanding));
                dealerCurrentNetworkData.setGrowthRateYoYPercentage(df.format(salesPerformanceService.getYearToYearGrowthForDealer(customerModel)));

            }
           else if(customerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID))) {
                double salesMtd=0.0,salesQuantity=0.0,salesQuantityLastMonth=0.0,salesMtdLastMonth=0.0,salesLastYearQty=0.0,salesLastYear=0.0,salesCurrentYearQty=0.0,salesCurrentYear=0.0;
                LocalDate currentYearCurrentDate= LocalDate.now();
                LocalDate currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear(), Month.APRIL, 1);
                if(currentYearCurrentDate.getMonth().compareTo(Month.APRIL)<0) {
                    currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear()-1, Month.APRIL, 1);
                }
                LocalDate lastYearCurrentDate= currentYearCurrentDate.minusYears(1);//2022-04-02

                LocalDate lastFinancialYearDate = currentFinancialYearDate.minusYears(1);//2022-04-01

                LocalDate startDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
                LocalDate finalCurrentFinancialYearDate = currentFinancialYearDate;
                    Map<String, Double> salesQuantityForRetailerByMTD = networkService.getSalesQuantityForRetailerByMTD(customerModel, null, null);
                    Map<String, Double> salesQuantityForRetailerLastMonth = networkService.getSalesQuantityForRetailerByMonthYear(customerModel,startDate.getMonthValue()-1 , startDate.getYear(),null,null);
                    Map<String, Double> salesQuantityForRetailerByYTD = networkService.getSalesQuantityForRetailerByDate(customerModel, finalCurrentFinancialYearDate.toString(), currentYearCurrentDate.toString(),null,null);
                    Map<String, Double> salesQuantityForRetailerByLastYTD = networkService.getSalesQuantityForRetailerByDate(customerModel, lastFinancialYearDate.toString(), lastYearCurrentDate.toString(),null,null);

                    if(userService.getCurrentUser() instanceof SclUserModel) {
                        dealerCurrentNetworkData.setPotential(String.valueOf(customerModel.getCounterPotential()!=null?customerModel.getCounterPotential():null));
                        if(salesQuantityForRetailerByMTD.containsKey("quantityInMT")) {
                            salesMtd = salesQuantityForRetailerByMTD.get("quantityInMT");
                        }
                        if(salesQuantityForRetailerLastMonth.containsKey("quantityInMT")) {
                            salesMtdLastMonth = salesQuantityForRetailerLastMonth.get("quantityInMT");
                        }
                        if(salesQuantityForRetailerByYTD.containsKey("quantityInMT")) {
                            salesCurrentYear = salesQuantityForRetailerByYTD.get("quantityInMT");
                        }
                        if(salesQuantityForRetailerByLastYTD.containsKey("quantityInMT")) {
                            salesLastYear = salesQuantityForRetailerByLastYTD.get("quantityInMT");
                        }
                    }else{
                        dealerCurrentNetworkData.setPotential(String.valueOf(customerModel.getCounterPotential()!=null?customerModel.getCounterPotential()*20:null));

                        if(salesQuantityForRetailerByMTD.containsKey("quantityInBags")) {
                            salesMtd = salesQuantityForRetailerByMTD.get("quantityInBags");
                        }

                        if(salesQuantityForRetailerLastMonth.containsKey("quantityInBags")) {
                            if(salesQuantityForRetailerLastMonth.get("quantityInBags")!=null){
                                salesMtdLastMonth = salesQuantityForRetailerLastMonth.get("quantityInBags");
                            }
                        }
                        if(salesQuantityForRetailerByYTD.containsKey("quantityInBags")) {
                            salesCurrentYear = salesQuantityForRetailerByYTD.get("quantityInBags");
                        }
                        if(salesQuantityForRetailerByLastYTD.containsKey("quantityInBags")) {
                            salesLastYear = salesQuantityForRetailerByLastYTD.get("quantityInBags");
                        }
                    }

                    SalesQuantityData sales = new SalesQuantityData();
                    sales.setRetailerSaleQuantity(salesMtd);
                    sales.setCurrent(salesMtd);
                    sales.setLastMonth(salesMtdLastMonth);

                    dealerCurrentNetworkData.setSalesQuantity(sales);

                    dealerCurrentNetworkData.setSalesYtd(df.format(salesCurrentYear));
                    dealerCurrentNetworkData.setGrowthRateYoYPercentage(df.format(getYearToYearGrowthRetailer(salesCurrentYear,salesLastYear)));

                    String monthName = Month.of(LocalDate.now().getMonthValue()).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                    String formattedMonth = monthName.concat("-").concat(String.valueOf(LocalDate.now().getYear()));
                    dealerCurrentNetworkData.setTarget(String.valueOf(salesPerformanceService.getMonthlySalesTargetForRetailer(customerModel.getUid(),baseSiteService.getCurrentBaseSite(),formattedMonth)));
            }
            else if(customerModel.getGroups().contains(userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID))) {
                {
                    String startDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).toString();
                    String endDate = LocalDate.now().plusDays(1).toString();
                    List<SclCustomerModel> influencerList=new ArrayList<>();
                    influencerList.add(customerModel);
                    List<List<Object>> list = pointRequisitionDao.getBagLiftedMTDforInfluencers(influencerList, startDate, endDate,null,null);
                    Map<String, Double>  map = list.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                            .collect(Collectors.toMap(each->((SclCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));

                    LocalDate currentYearCurrentDate= LocalDate.now();
                    LocalDate currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear(), Month.APRIL, 1);
                    if(currentYearCurrentDate.getMonth().compareTo(Month.APRIL)<0) {
                        currentFinancialYearDate = LocalDate.of(currentYearCurrentDate.getYear()-1, Month.APRIL, 1);
                    }
                    LocalDate lastYearCurrentDate= currentYearCurrentDate.minusYears(1);

                    LocalDate lastFinancialYearDate = currentFinancialYearDate.minusYears(1);

                    List<List<Object>> currentYTD = pointRequisitionDao.getBagLiftedMTDforInfluencers(influencerList, currentFinancialYearDate.toString(), currentYearCurrentDate.toString(),null,null);
                    Map<String, Double>  mapCurrentYTD = currentYTD.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                            .collect(Collectors.toMap(each->((SclCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));

                    List<List<Object>> lastYTD = pointRequisitionDao.getBagLiftedMTDforInfluencers(influencerList, lastFinancialYearDate.toString(), lastYearCurrentDate.toString(),null,null);

                    Map<String, Double>  mapLastYTD = lastYTD.stream().filter(each->each!=null && each.size()>1 && each.get(0)!=null && each.get(1)!=null)
                            .collect(Collectors.toMap(each->((SclCustomerModel)each.get(0)).getUid(), each->(Double)each.get(1)));

                    dealerCurrentNetworkData.setCategory(customerModel.getInfluencerType() != null ?
                            enumerationService.getEnumerationName(customerModel.getInfluencerType()) : "-");
                    dealerCurrentNetworkData.setTimesContacted(customerModel.getTimesContacted());
                    dealerCurrentNetworkData.setPoints(Objects.nonNull(customerModel.getAvailablePoints()) ? customerModel.getAvailablePoints() : 0.0);

                    var bagLifted = 0.0;
                    if(map.containsKey(customerModel.getUid())) {
                        bagLifted = map.get(customerModel.getUid());
                    }
                    var salesQuantity = (bagLifted / 20);
                    dealerCurrentNetworkData.setBagLifted(bagLifted);
                    dealerCurrentNetworkData.setBagLiftedNo(bagLifted);
                    dealerCurrentNetworkData.setBagLiftedQty(String.valueOf(salesQuantity));

                    double salesCurrentYear = 0.0;
                    if(mapCurrentYTD.containsKey(customerModel.getUid())) {
                        salesCurrentYear = mapCurrentYTD.get(customerModel.getUid());
                    }
                    double salesCurrentYearQty = (salesCurrentYear / 20);

                    double salesLastYear = 0.0;
                    if(mapLastYTD.containsKey(customerModel.getUid())) {
                        salesLastYear = mapLastYTD.get(customerModel.getUid());
                    }
                    double salesLastYearQty = (salesLastYear / 20);
                    dealerCurrentNetworkData.setSalesYtd(df.format(salesCurrentYearQty));
                    dealerCurrentNetworkData.setGrowthRateYoYPercentage(df.format(getYearToYearGrowthRetailer(salesCurrentYearQty,salesLastYearQty)));                }
            }
            dealerCurrentNetworkData.setCounterSharePercentage(salesPerformanceService.getCounterShareForDealer(customerModel, LocalDate.now().getMonthValue(), LocalDate.now().getYear()));
            dealerCurrentNetworkData.setDaySinceLastLifting(salesPerformanceService.getDaysFromLastOrder(customerModel));
            dealerCurrentNetworkData.setDaysSinceLastOrder(salesPerformanceService.getDaysFromLastOrder(customerModel));
            List<List<Object>> districtAndTaluka = territoryMasterService.getDistrictAndTalukaForCustomer(customerModel);
            if (CollectionUtils.isNotEmpty(districtAndTaluka)) {
                for (List<Object> list : districtAndTaluka) {
                    if(list.get(0)!=null){
                        dealerCurrentNetworkData.setTaluka(StringUtils.isNotBlank(String.valueOf(list.get(0))) ? String.valueOf(list.get(0)) : "");
                    }
                    if(list.get(1)!=null) {
                        dealerCurrentNetworkData.setDistrict(StringUtils.isNotBlank(String.valueOf(list.get(1))) ? String.valueOf(list.get(1)) : "");
                    }
                }
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

