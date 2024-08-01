package com.scl.core.dao.impl;

import com.scl.core.dao.SclSalesSummaryDao;
import com.scl.core.dao.TerritoryManagementDao;
import com.scl.core.dao.TerritoryMasterDao;
import com.scl.core.model.*;
import com.scl.core.services.TerritoryManagementService;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.*;

public class SclSalesSummaryDaoImpl implements SclSalesSummaryDao {

    private static final Logger LOG = Logger.getLogger(SclSalesSummaryDaoImpl.class);

    @Resource
    private FlexibleSearchService flexibleSearchService;
    @Resource
    TerritoryManagementDao territoryManagementDao;
    @Autowired
    TerritoryMasterDao territoryMasterDao;

    @Resource
    private TerritoryManagementService territoryManagementService;

    private static final String SALES_QUERY = "SELECT SUM({s:sale}) FROM {SalesSummary as s} where {s:month}=?month and {s:year}=?year ";
    private static final String SALES_QUERY_MTD = "SELECT {customerNo},sum({sale}) FROM {SalesSummary as s} where {s:month}=?month and {s:year}=?year and {customerNo} in (?customerNos) group by {customerNo} ";
    private static final String SALES_QUERY_ProductMIX ="SELECT {s:alias},SUM({s:sale}) from {SalesSummary as s} where {s:month}=?month and {s:year}=?year ";
    private static final String SALES_QUERY_FY = "SELECT SUM({s:sale})  FROM {SalesSummary as s} where (({s:month} >=?startMonth and {s:year} =?startYear) or ({s:month} <?endMonth and {s:year} =?endYear)) ";
    private static final String SALES_QUERY_YTD = "SELECT {customerNo},sum({sale}) FROM {SalesSummary as s} where (({s:month} >=?startMonth and {s:year} =?startYear) or ({s:month} <?endMonth and {s:year} =?endYear)) and {customerNo} in (?customerNos) group by {customerNo}  ";
    private static final String SALES_QUERY_ProductMIX_FY ="SELECT {s:alias},SUM({s:sale}) from {SalesSummary as s} where (({s:month} >=?startMonth and {s:year} =?startYear) or ({s:month} <=?endMonth and {s:year} =?endYear)) ";
    private static final String SALES_QUERY_10DAY_BUCKET="SELECT {s:pk} FROM {SalesSummary as s} where {s:month}=?month and {s:year}=?year";
    private static final String PRODUCT_ALIAS_BY_CODE="SELECT distinct({pp:pk}) from {ProductAlias as p join Product as pp on {pp:pk}={p:product}} where {pp:code}=?bgpFilter OR {p:aliasName}=?bgpFilter ";

    @Override
    public double getSalesDetails(B2BCustomerModel b2bCustomer, int month, int year, List<String> territoryList) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(SALES_QUERY);
        params.put("month", month);
        params.put("year", year);

        getAdditionalParams(b2bCustomer, params, builder,territoryList);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }

    @Override
    public double getSalesDetails(B2BCustomerModel b2bCustomer, int startMonth, int startYear, int endMonth, int endYear, List<String> territoryList) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(SALES_QUERY_FY);
        params.put("startMonth", startMonth);
        params.put("endMonth", endMonth);
        params.put("startYear", startYear);
        params.put("endYear", endYear);
      
        getAdditionalParams(b2bCustomer, params, builder,territoryList);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }

    @Override
    public double getSalesDetails(List<B2BCustomerModel> b2BCustomer, int startMonth, int startYear, int endMonth, int endYear, List<String> territoryList) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(SALES_QUERY_FY);
        params.put("startMonth", startMonth);
        params.put("endMonth", endMonth);
        params.put("startYear", startYear);
        params.put("endYear", endYear);

        getAddParams(b2BCustomer, params, builder,territoryList);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }

    @Override
    public double getSalesDetails(B2BCustomerModel b2bCustomer, int startMonth, int startYear, int endMonth, int endYear, String productAlias) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(SALES_QUERY_FY);
        params.put("startMonth", startMonth);
        params.put("endMonth", endMonth);
        params.put("startYear", startYear);
        params.put("endYear", endYear);

        if(StringUtils.isNotEmpty(productAlias) && !productAlias.equalsIgnoreCase("ALL")) {
            builder.append(" and {s:alias}=?productAlias");
            params.put("productAlias", productAlias);
        }

        getAdditionalParams(b2bCustomer,params,builder, null);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }

    @Override
    public double getSalesDetails(List<B2BCustomerModel> b2BCustomer, int month, int year) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(SALES_QUERY);
        params.put("month", month);
        params.put("year", year);

        getAddParams(b2BCustomer,params,builder,null);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }

    @Override
    public List<List<Object>> getSalesYTDforDealer(List<SclCustomerModel> b2BCustomerModels,int startMonth, int startYear, int endMonth, int endYear) {
        try{
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(SALES_QUERY_YTD);
        params.put("startMonth", startMonth);
        params.put("endMonth", endMonth);
        params.put("startYear", startYear);
        params.put("endYear", endYear);
            List<String> customerNos=new ArrayList<>();
            if(CollectionUtils.isNotEmpty(b2BCustomerModels)){
                for (B2BCustomerModel b2BCustomerModel : b2BCustomerModels) {
                    customerNos.add(b2BCustomerModel.getUid());
                }
                params.put("customerNos",customerNos);
            }
        //getAddParams(b2BCustomer,params,builder,null);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
            LOG.info("Query for MTD Sales:"+query);
        query.setResultClassList(Arrays.asList(String.class, Double.class));
        final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
        List<List<Object>> result = searchResult.getResult();
        return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
    } catch (IndexOutOfBoundsException e) {
        throw new IndexOutOfBoundsException(String.valueOf(e));
    }
    }

    @Override
    public List<List<Object>> getSalesMTDforDealer(List<SclCustomerModel> b2BCustomerModels,int month, int year) {
        try{
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(SALES_QUERY_MTD);
        params.put("month", month);
        params.put("year", year);
        List<String> customerNos=new ArrayList<>();
        if(CollectionUtils.isNotEmpty(b2BCustomerModels)){
            for (B2BCustomerModel b2BCustomerModel : b2BCustomerModels) {
                customerNos.add(b2BCustomerModel.getUid());
            }
            params.put("customerNos",customerNos);
        }


        //getAddParams(b2BCustomer,params,builder,null);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
            LOG.info("Query for YTD Sales:"+query);
        query.setResultClassList(Arrays.asList(String.class, Double.class));
        final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
        List<List<Object>> result = searchResult.getResult();
        return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
    } catch (IndexOutOfBoundsException e) {
        throw new IndexOutOfBoundsException(String.valueOf(e));
    }
    }

    private void getAdditionalParams(B2BCustomerModel b2bCustomer, Map<String, Object> params, StringBuilder builder, List<String> territoryList) {
        String customerNo="";
        if (b2bCustomer != null && b2bCustomer instanceof SclCustomerModel) {
            if (StringUtils.isNotEmpty(((SclCustomerModel) b2bCustomer).getCustomerNo()))
                customerNo = ((SclCustomerModel) b2bCustomer).getCustomerNo();
            builder.append(" and {s:customerNo}=?customerNo ");
            params.put("customerNo", customerNo);
        }
        else if(b2bCustomer != null && b2bCustomer instanceof SclUserModel)
        {
           // Collection<TerritoryMasterModel> territories = territoryMasterDao.getCurrentTerritory();
            List<TerritoryMasterModel> territoryMasterModelList=new ArrayList<>();
            if(CollectionUtils.isNotEmpty(territoryList))
            {
                for (String s : territoryList) {
                    TerritoryMasterModel territoryById = territoryMasterDao.getTerritoryById(s);
                    territoryMasterModelList.add(territoryById);
                }
                if(CollectionUtils.isNotEmpty(territoryMasterModelList)) {
                    builder.append(" and {s:territoryMaster} in (?territoryMasterModelList) ");
                    params.put("territoryMasterModelList", territoryMasterModelList);
                }
            }
            else {
                Collection<TerritoryMasterModel> territories = territoryMasterDao.getTerritoryForUser(null);
                if (territories != null && !territories.isEmpty()) {
                    builder.append(" and {s:territoryMaster} in (?territories) ");
                    params.put("territories", territories);
                }else{
                    builder.append(" and {s:territoryMaster} in (?territories) and {s:territoryMaster} is not null ");
                    params.put("territories", Collections.emptyList());
                }
            }
        }
    }

    private void getAddParams(List<B2BCustomerModel> b2bCustomer, Map<String, Object> params, StringBuilder builder, List<String> territoryList) {
        String customerNo="";
        List<String> customerNos=new ArrayList<>();
        if (CollectionUtils.isNotEmpty(b2bCustomer) ) {
            for (B2BCustomerModel customerModel : b2bCustomer) {
                if (customerModel instanceof SclCustomerModel) {
                    if (StringUtils.isNotEmpty(((SclCustomerModel) customerModel).getCustomerNo()))
                        customerNo = ((SclCustomerModel) customerModel).getCustomerNo();
                    customerNos.add(customerNo);
                }
            }
            builder.append(" and {s:customerNo} in (?customerNos) ");
            params.put("customerNos", customerNos);
        }
    }

    @Override
    public double getSalesDetails(int month, int year) {
        return 0;
    }

    @Override
    public double getSalesDetails(B2BCustomerModel b2BCustomer, int month, int year, String productAlias) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(SALES_QUERY);
        params.put("month", month);
        params.put("year", year);
        if(StringUtils.isNotEmpty(productAlias) && !productAlias.equalsIgnoreCase("ALL"))
        {
            builder.append(" and {s:alias}=?productAlias");
            params.put("productAlias",productAlias);
        }
        getAdditionalParams(b2BCustomer, params, builder, null);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }

    @Override
    public List<List<Object>> getProductMixSalesDetailsMTD(B2BCustomerModel b2bCustomer, int month, int year,List<String> territoryList) {
        try{
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(SALES_QUERY_ProductMIX);
        params.put("month", month);
        params.put("year", year);
        getAdditionalParams(b2bCustomer,params,builder, territoryList);
        builder.append(" group by {s:alias}");
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(String.class, Double.class));
        final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
        List<List<Object>> result = searchResult.getResult();
        return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
    } catch (IndexOutOfBoundsException e) {
        throw new IndexOutOfBoundsException(String.valueOf(e));
    }
    }

    @Override
    public List<List<Object>> getProductMixSalesDetailsYTD(B2BCustomerModel b2BCustomer, int startMonth, int startYear, int endMonth, int endYear,List<String> territoryList) {
        try{
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(SALES_QUERY_ProductMIX_FY);
        params.put("startMonth", startMonth);
        params.put("endMonth", endMonth);
        params.put("startYear", startYear);
        params.put("endYear", endYear);
        getAdditionalParams(b2BCustomer,params,builder, territoryList);
        builder.append(" group by {s:alias}");
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(String.class, Double.class));
        final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
        List<List<Object>> result = searchResult.getResult();
        return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
        throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public List<SalesSummaryModel> getSalesDetailsFor10DayBucket(B2BCustomerModel b2bCustomer, int month, int year){
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(SALES_QUERY_10DAY_BUCKET);
        params.put("month", month);
        params.put("year", year);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Collections.singletonList(SalesSummaryModel.class));
        query.addQueryParameters(params);
        final SearchResult<SalesSummaryModel> searchResult = flexibleSearchService.search(query);
        return searchResult.getResult();
    }

    @Override
    public double getSalesDetails(String customerNo, int month, int year, int startMonth, int startYear, int endMonth, int endYear) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("SELECT SUM({s:sale}) FROM {SalesSummary as s} where {s:customerNo}=?customerNo");

        if(month!=0 && year!=0)
        {
            builder.append(" and {s:month}=?month and {s:year}=?year ");
            params.put("month", month);
            params.put("year", year);
            params.put("customerNo",customerNo);
        }

        if (startMonth!=0 && endMonth!=0 && startYear!=0 && endYear!=0)
        {
            builder.append(" and (({s:month} >=?startMonth and {s:year} =?startYear) or ({s:month} <=?endMonth and {s:year} =?endYear))");
            params.put("startMonth", startMonth);
            params.put("endMonth", endMonth);
            params.put("startYear", startYear);
            params.put("endYear", endYear);
            params.put("customerNo",customerNo);
        }
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(Double.class));
        query.addQueryParameters(params);
        final SearchResult<Double> searchResult = flexibleSearchService.search(query);
        if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : 0.0;
        else
            return 0.0;
    }

    @Override
    public ProductModel getProductAliasNameByCode(String bgpFilter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder(PRODUCT_ALIAS_BY_CODE);
        params.put("bgpFilter", bgpFilter);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.setResultClassList(Arrays.asList(ProductModel.class));
        query.addQueryParameters(params);
        final SearchResult<ProductModel> searchResult = flexibleSearchService.search(query);
        if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
            return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) :null;
        else
            return null;
    }

    public FlexibleSearchService getFlexibleSearchService() {
        return flexibleSearchService;
    }

    public void setFlexibleSearchService(FlexibleSearchService flexibleSearchService) {
        this.flexibleSearchService = flexibleSearchService;
    }

    public TerritoryManagementService getTerritoryManagementService() {
        return territoryManagementService;
    }

    public void setTerritoryManagementService(TerritoryManagementService territoryManagementService) {
        this.territoryManagementService = territoryManagementService;
    }

    public TerritoryManagementDao getTerritoryManagementDao() {
        return territoryManagementDao;
    }

    public void setTerritoryManagementDao(TerritoryManagementDao territoryManagementDao) {
        this.territoryManagementDao = territoryManagementDao;
    }
}
