package com.scl.core.dao.impl;

import com.scl.core.dao.DealerStockAllocationDao;
import com.scl.core.model.MasterStockAllocationModel;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.utility.SclDateUtility;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchParameter;
import de.hybris.platform.servicelayer.search.paginated.PaginatedFlexibleSearchService;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DealerStockAllocationDaoImpl implements DealerStockAllocationDao {

    private static final Logger LOG = Logger.getLogger(DealerStockAllocationDaoImpl.class);

    private FlexibleSearchService flexibleSearchService;

    private PaginatedFlexibleSearchService paginatedFlexibleSearchService;
    private Map<String, String> sclStockAllocationSortCodeToQueryAlias;
    private ConfigurationService configurationService;

    @Autowired
    UserService userService;

    /**
     *
     * @param dealer
     * @param customer
     * @param startDate
     * @param endDate
     * @return
     */
    @Override
    public List<MasterStockAllocationModel> getMasterStockAllocationForDateRange(SclCustomerModel dealer, SclCustomerModel customer, String startDate, String endDate) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("Select {pk} from {MasterStockAllocation as ms} where {ms.dealer}=?dealer and {ms.balanceQtyInMt} > 0 and {ms.retailer} IS NULL");
        params.put("dealer", dealer);


        if(startDate!=null && endDate!=null){
            builder.append(" and ");
            builder.append(SclDateUtility.getDateRangeClauseQuery("ms.invoicedDate",startDate, endDate,params));
        }

        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Collections.singletonList(MasterStockAllocationModel.class));

        LOG.info(String.format("Get MasterStockAllocation Query used: %s",query));
        final SearchResult<MasterStockAllocationModel> searchResult = flexibleSearchService.search(query);
        List<MasterStockAllocationModel> result = searchResult.getResult();
        if(result != null && !result.isEmpty()){
            LOG.info(String.format("%s Record found for MasterStockAllocation for invoicedDate range btw Starting :- %s Ending:- %s  for Dealer:- %s"  ,searchResult.getResult().size(),startDate, endDate,dealer.getUid()));
            return result;
        }else{
            LOG.info(String.format("No Record found for MasterStockAllocation for invoicedDate range btw Starting :- %s Ending:- %s  for Dealer:- %s"  ,startDate, endDate,dealer.getUid()));
           return Collections.emptyList();
        }
    }

    /**
     * @param searchPageData
     * @param dealer
     * @param customer
     * @param startDate
     * @param endDate
     * @param quantity
     * @param productCode
     * @param filter
     * @return
     */
    @Override
    public List<MasterStockAllocationModel> getMasterStockAllocationForProductInvoice(SearchPageData searchPageData, SclCustomerModel dealer, SclCustomerModel customer, String startDate, String endDate, Double quantity, String productCode,String productAlias, String filter) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("Select {pk} from {MasterStockAllocation as ms JOIN Product as p on {p.pk}={ms.product}} where {ms.dealer}=?dealer  and {ms.retailer} IS NULL and {p.equivalenceProductCode}=?productCode and {ms.aliasCode}=?productAlias");
        params.put("dealer", dealer);
        params.put("productCode", productCode);
        params.put("productAlias", productAlias);

        if(quantity!=null){
            builder.append(" and {ms.balanceQtyInBags} >= ?qty");
            params.put("qty", quantity);
        }

        if(startDate!=null && endDate!=null){
            builder.append(" and ");
            builder.append(SclDateUtility.getDateRangeClauseQuery("ms.invoicedDate",startDate, endDate,params));
        }

        if (StringUtils.isNotBlank(filter)) {
            builder.append(" AND (UPPER({ms.taxInvoiceNumber}) like (?filter))");
            params.put("filter", "%" + filter.toUpperCase() + "%");
        }

        final FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery(builder.toString());
        flexibleSearchQuery.addQueryParameters(params);

        LOG.info(String.format("Get MasterStockAllocation Invoice Query used: %s",flexibleSearchQuery));

        return getPaginatedFlexibleSearchForStockAllocationInvoice(searchPageData,flexibleSearchQuery,startDate,endDate,dealer,customer,productCode);

    }

    /**
     * @param searchPageData
     * @param searchQuery
     * @param startDate
     * @param endDate
     * @param dealer
     * @param customer
     * @param productCode
     * @return
     */
    private List<MasterStockAllocationModel> getPaginatedFlexibleSearchForStockAllocationInvoice(SearchPageData searchPageData, FlexibleSearchQuery searchQuery, String startDate, String endDate, SclCustomerModel dealer, SclCustomerModel customer,String productCode){
        String customerUid=null!=customer?customer.getUid():null;
        final PaginatedFlexibleSearchParameter parameter = new PaginatedFlexibleSearchParameter();
        parameter.setSearchPageData(searchPageData);
        parameter.setFlexibleSearchQuery(searchQuery);
        parameter.setSortCodeToQueryAlias(getSclStockAllocationSortCodeToQueryAlias());
        final SearchPageData<MasterStockAllocationModel> searchResult = this.getPaginatedFlexibleSearchService().search(parameter);
        if(searchResult!=null && !CollectionUtils.isEmpty(searchResult.getResults())) {
            List<MasterStockAllocationModel> result = searchResult.getResults();
            if(result != null && !result.isEmpty()){
                LOG.info(String.format("%s Invoice Record found for equivalenceProductCode :- %s  btw invoicedDate range Starting :- %s Ending:- %s  for Dealer:- %s and Retailer/Influencer Uid:- %s"  ,searchResult.getResults().size(),productCode,startDate, endDate,dealer.getUid(),customerUid));
                return result;
            }else{
                LOG.info(String.format("No Invoice Record found for equivalenceProductCode :- %s  btw invoicedDate range Starting :- %s Ending:- %s  for Dealer:- %s and Retailer/Influencer Uid:- %s"  ,productCode,startDate, endDate,dealer.getUid(),customerUid));
                return Collections.emptyList();
            }
        }else{
            LOG.info(String.format("No Invoice Record found for equivalenceProductCode :- %s  btw invoicedDate range Starting :- %s Ending:- %s  for Dealer:- %s and Retailer/Influencer Uid:- %s"  ,productCode,startDate, endDate,dealer.getUid(),customerUid));
            return Collections.emptyList();
        }
    }


    public FlexibleSearchService getFlexibleSearchService() {
        return flexibleSearchService;
    }

    public void setFlexibleSearchService(FlexibleSearchService flexibleSearchService) {
        this.flexibleSearchService = flexibleSearchService;
    }

    public PaginatedFlexibleSearchService getPaginatedFlexibleSearchService() {
        return paginatedFlexibleSearchService;
    }

    public void setPaginatedFlexibleSearchService(PaginatedFlexibleSearchService paginatedFlexibleSearchService) {
        this.paginatedFlexibleSearchService = paginatedFlexibleSearchService;
    }


    public Map<String, String> getSclStockAllocationSortCodeToQueryAlias() {
        return sclStockAllocationSortCodeToQueryAlias;
    }

    public void setSclStockAllocationSortCodeToQueryAlias(Map<String, String> sclStockAllocationSortCodeToQueryAlias) {
        this.sclStockAllocationSortCodeToQueryAlias = sclStockAllocationSortCodeToQueryAlias;
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }
}
