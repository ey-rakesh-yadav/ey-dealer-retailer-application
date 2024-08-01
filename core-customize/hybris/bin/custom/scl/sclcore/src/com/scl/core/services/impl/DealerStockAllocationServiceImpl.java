package com.scl.core.services.impl;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.dao.DataConstraintDao;
import com.scl.core.dao.impl.DealerStockAllocationDaoImpl;
import com.scl.core.model.MasterStockAllocationModel;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.services.DealerStockAllocationService;
import com.scl.core.utility.SclDateUtility;
import com.scl.facades.data.*;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class DealerStockAllocationServiceImpl implements DealerStockAllocationService {

    private static final Logger LOG = Logger.getLogger(DealerStockAllocationServiceImpl.class);
    public static final String MONTH_CUTOVER_DATE = "MONTH_CUTOVER_DATE";
    public static final String ROLLING_PERIOD_DAYS = "ROLLING_PERIOD_DAYS";
    private static final String DEFAULTDATEFORMAT = "yyyy-MM-dd";
    private static final String INVOICEDATEFORMAT = "yyyy-MM-dd";
    public static final String RET = "RET";
    public static final String INF = "INF";
    private static final String LOOSE_PRODUCT_FILTER_KEYWORD = "LOOSE_PRODUCT_FILTER_KEYWORD";
    private static final String LOOSE_PRODUCT_EXCLUDE = "LOOSE_PRODUCT_EXCLUDE";


    private DataConstraintDao dataConstraintDao;
    private DealerStockAllocationDaoImpl  dealerStockAllocationDao;

    @Autowired
    UserService userService;


    /**
     *
     * @param sclCustomer
     * @return
     * @throws Exception
     */
    @Override
    public LiftingDateRangeData getLiftingDateRange(SclCustomerModel sclCustomer) throws Exception {

        LiftingDateRangeData rangeData= new LiftingDateRangeData();
        Integer cutover_date_value = null;
        Integer rolling_period_days = null;
        if(Objects.nonNull(sclCustomer)){
            UserGroupModel influencerGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.INFLUENCER_USER_GROUP_UID);
            UserGroupModel retailerGroup = userService.getUserGroupForUID(SclCoreConstants.CUSTOMER.RETAILER_USER_GROUP_UID);
            if(sclCustomer.getGroups() !=null && sclCustomer.getGroups().contains(influencerGroup)) {
                cutover_date_value= dataConstraintDao.findDaysByConstraintName(INF+"_"+MONTH_CUTOVER_DATE);
                rolling_period_days= dataConstraintDao.findDaysByConstraintName(INF+"_"+ROLLING_PERIOD_DAYS);
                LOG.info(String.format("CUTOVER_DATE_VALUE:- %s ROLLING_PERIOD_DAYS :- %s FOR INFLUENCER :- %s",cutover_date_value,rolling_period_days,sclCustomer.getUid()));
                if(null==rolling_period_days ||null==cutover_date_value)
                    throw new Exception("INF_MONTH_CUTOVER_DATE or INF_ROLLING_PERIOD_DAYS is not Found");
            } else if(sclCustomer.getGroups()!=null && sclCustomer.getGroups().contains(retailerGroup)) {
                cutover_date_value= dataConstraintDao.findDaysByConstraintName(RET+"_"+MONTH_CUTOVER_DATE);
                rolling_period_days= dataConstraintDao.findDaysByConstraintName(RET+"_"+ROLLING_PERIOD_DAYS);
                LOG.info(String.format("CUTOVER_DATE_VALUE:- %s ROLLING_PERIOD_DAYS :- %s FOR RETAILER :- %s",cutover_date_value,rolling_period_days,sclCustomer.getUid()));
                if(null==rolling_period_days ||null==cutover_date_value)
                    throw new Exception("RET_MONTH_CUTOVER_DATE or RET_ROLLING_PERIOD_DAYS is not Found");
            }
        }
        if(null==rolling_period_days ||null==cutover_date_value) {
            cutover_date_value = dataConstraintDao.findDaysByConstraintName(MONTH_CUTOVER_DATE);
            rolling_period_days = dataConstraintDao.findDaysByConstraintName(ROLLING_PERIOD_DAYS);
            LOG.info(String.format("CUTOVER_DATE_VALUE:- %s ROLLING_PERIOD_DAYS :- %s FOR DEALER",cutover_date_value,rolling_period_days));
            if (null == rolling_period_days || null == cutover_date_value)
                throw new Exception("MONTH_CUTOVER_DATE or ROLLING_PERIOD_DAYS is not Found");
        }
        LocalDate currentDate = LocalDate.now();
        LocalDate cutover_date= SclDateUtility.getSpecificDayOfMonth(cutover_date_value);
        LocalDate rolling_period_date=SclDateUtility.getDateMinusXDays(rolling_period_days);

        LOG.info(String.format("CURRENT_DATE:- %s CUTOVER_DATE:- %s ROLLING_PERIOD_DATE:- %s" ,currentDate,cutover_date,rolling_period_date));

        if(currentDate.isBefore(cutover_date)){
            rangeData.setStartDate(rolling_period_date.toString());
            rangeData.setEndDate(SclDateUtility.getDateMinusXDays(1).toString());
        }else {
            if (rolling_period_date.getMonth().equals(currentDate.getMonth())) {
                rangeData.setStartDate(rolling_period_date.toString());
            } else {
                rangeData.setStartDate(SclDateUtility.getSpecificDayOfMonth(1).toString());
            }
        }
        rangeData.setEndDate(SclDateUtility.getDateMinusXDays(1).toString());

        LOG.info(String.format("LiftingDateRange Starting :- %s Ending:- %s" ,rangeData.getStartDate(), rangeData.getEndDate()));
        return rangeData;
    }

    /**
     *
     * @param dealer
     * @param customer
     * @param selectedLiftingDate
     * @return
     * @throws Exception
     */
    @Override
    public ProductStockAllocationListData getProductListForStockAllocation(SclCustomerModel dealer, SclCustomerModel customer, String selectedLiftingDate) throws Exception {
        LiftingDateRangeData rangeData=getLiftingDateRange(customer);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DEFAULTDATEFORMAT);
        LocalDate selectedDate= LocalDate.parse(selectedLiftingDate,formatter);
        LocalDate startDate= LocalDate.parse(rangeData.getStartDate(),formatter);
        LocalDate endDate= LocalDate.parse(rangeData.getEndDate(),formatter);
        List<MasterStockAllocationModel> stockAllocationModelList;
        if(!selectedDate.getMonth().equals(startDate.getMonth())) {
            stockAllocationModelList =  dealerStockAllocationDao.getMasterStockAllocationForDateRange(dealer, customer, selectedDate.withDayOfMonth(1).toString(), selectedDate.toString());
        }else{
            stockAllocationModelList =  dealerStockAllocationDao.getMasterStockAllocationForDateRange(dealer,customer,startDate.toString(),selectedDate.toString());
        }

        return convertProductStockAllocationListResponse(stockAllocationModelList);

    }

    /**
     * @param searchPageData
     * @param dealer
     * @param customer
     * @param selectedLiftingDate
     * @param productCode
     * @param productAlias
     * @param quantity
     * @param filter
     * @return InvoiceListData
     * @throws Exception
     */
    @Override
    public InvoiceListData getInvoiceListForProduct(SearchPageData searchPageData, SclCustomerModel dealer, SclCustomerModel customer, String selectedLiftingDate, String productCode, String productAlias, Double quantity, String filter) throws Exception {
        LiftingDateRangeData rangeData=getLiftingDateRange(customer);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DEFAULTDATEFORMAT);
        LocalDate selectedDate= LocalDate.parse(selectedLiftingDate,formatter);
        LocalDate startDate= LocalDate.parse(rangeData.getStartDate(),formatter);
        LocalDate endDate= LocalDate.parse(rangeData.getEndDate(),formatter);
        List<MasterStockAllocationModel> stockAllocationModelList;
        if(!selectedDate.getMonth().equals(startDate.getMonth())) {
            stockAllocationModelList =  dealerStockAllocationDao.getMasterStockAllocationForProductInvoice(searchPageData,dealer, customer, selectedDate.withDayOfMonth(1).toString(), selectedDate.toString(),quantity,productCode,productAlias,filter);
        }else{
            stockAllocationModelList =  dealerStockAllocationDao.getMasterStockAllocationForProductInvoice(searchPageData,dealer,customer,startDate.toString(),selectedDate.toString(),quantity,productCode,productAlias,filter);
        }

        return convertProductInvoiceListResponse(stockAllocationModelList);

    }

    /**
     *
     * @param stockAllocationModelList
     * @return InvoiceListData
     */
    private InvoiceListData convertProductInvoiceListResponse(List<MasterStockAllocationModel> stockAllocationModelList) {
        InvoiceListData listData = new InvoiceListData();
        if (CollectionUtils.isNotEmpty(stockAllocationModelList)) {
            List<InvoiceData> invoices = new ArrayList<>();
            stockAllocationModelList.forEach(item -> {
                InvoiceData invoiceData = new InvoiceData();
                invoiceData.setInvoiceDate(getFormattedDate(item.getInvoicedDate(), INVOICEDATEFORMAT));
                invoiceData.setInvoiceId(item.getId());
                invoiceData.setTaxInvoiceNumber(item.getTaxInvoiceNumber());
                invoiceData.setTruckNumber(item.getTruckNumber());
                invoiceData.setBalQtyInMT(item.getBalanceQtyInMt());
                invoiceData.setBalQtyInBags(item.getBalanceQtyInBags());
                invoiceData.setProductCode(item.getProductCode());
                invoiceData.setProductAliasName(item.getAliasCode());
                invoiceData.setEquivalenceProductCode(item.getProduct().getEquivalenceProductCode());
                invoices.add(invoiceData);
            });
            listData.setInvoices(invoices);
        }
        return listData;

    }

    /**
     *
     * @param stockAllocationModelList
     * @return ProductStockAllocationListData
     */
    private ProductStockAllocationListData convertProductStockAllocationListResponse(List<MasterStockAllocationModel> stockAllocationModelList ){
        ProductStockAllocationListData listData = new ProductStockAllocationListData();
        if (CollectionUtils.isNotEmpty(stockAllocationModelList)) {
            List<ProductStockAllocationData> productStockAllocationDataList = new ArrayList<>();
            List<MasterStockAllocationModel> filteredlList;
            if(dataConstraintDao.findVersionByConstraintName(LOOSE_PRODUCT_EXCLUDE).equalsIgnoreCase("true")) {
                String looseProduct = dataConstraintDao.findVersionByConstraintName(LOOSE_PRODUCT_FILTER_KEYWORD);
                filteredlList=stockAllocationModelList.stream().filter(item -> !item.getAliasCode().toUpperCase().contains(looseProduct)).collect(Collectors.toList());
            }else{
                filteredlList=stockAllocationModelList;
            }

            HashMap<String,String> map=new HashMap<String,String>();
            filteredlList.forEach(item -> {
               map.put(item.getAliasCode(),item.getProduct().getEquivalenceProductCode());
            });

            map.forEach((k, v) -> {
                ProductStockAllocationData data = new ProductStockAllocationData();
                data.setProductCode(v);
                data.setAlias(k);
                productStockAllocationDataList.add(data);
            });

            listData.setProductList(productStockAllocationDataList.stream().sorted(Comparator.comparing(ProductStockAllocationData::getAlias)).collect(Collectors.toList()));
        }
        return listData;
    }

    /**
     *
     * @param date
     * @param format
     * @return FormattedDate
     */
    private String getFormattedDate(Date date, String format) {
        SimpleDateFormat dateFormat=new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

    public DataConstraintDao getDataConstraintDao() {
        return dataConstraintDao;
    }

    public void setDataConstraintDao(DataConstraintDao dataConstraintDao) {
        this.dataConstraintDao = dataConstraintDao;
    }

    public DealerStockAllocationDaoImpl getDealerStockAllocationDao() {
        return dealerStockAllocationDao;
    }

    public void setDealerStockAllocationDao(DealerStockAllocationDaoImpl dealerStockAllocationDao) {
        this.dealerStockAllocationDao = dealerStockAllocationDao;
    }
}
