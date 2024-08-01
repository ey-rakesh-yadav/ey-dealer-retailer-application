package com.scl.core.dao.impl;

import com.scl.core.dao.SalesSummaryDao;
import com.scl.core.enums.RequisitionStatus;
import com.scl.core.model.*;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.apache.commons.collections.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;

public class SalesSummaryDaoImpl implements SalesSummaryDao {
    @Resource
    FlexibleSearchService flexibleSearchService;

    @Override
    public List<List<Object>> getCustomerFromOrderRequisitionForUpload(String salesSummaryJobStatus, SclCustomerModel sclCustomer, Date startDate,Date endDate) {
        try {
            //Remove trasId and add custom code
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select MONTH({o:deliveredDate}),YEAR({o:deliveredDate}),{o:fromCustomer},sum({o:quantity}),{o:deliveredDate}  from {OrderRequisition as o} where {o:toCustomer}=?sclCustomer and {o:status}=?requisitionStatus and {o:deliveredDate} is not null and {o:saleSummaryJobStatus} =?salesSummaryJobStatus group by MONTH({o:deliveredDate}),YEAR({o:deliveredDate}),{o:fromCustomer},{o:deliveredDate}");
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
            params.put("startDate",startDate);
            params.put("endDate",endDate);
            params.put("sclCustomer",sclCustomer);
            params.put("requisitionStatus",requisitionStatus);
            params.put("salesSummaryJobStatus",salesSummaryJobStatus);
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class, String.class,SclCustomerModel.class,Double.class, Date.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }
    @Override
    public List<List<Object>> getCustomerFromOrderRequisitionForUploadTest(String salesSummaryJobStatus, SclCustomerModel sclCustomer) {
        try {
            //Remove trasId and add custom code
            final Map<String, Object> params = new HashMap<String, Object>();
            final StringBuilder builder = new StringBuilder("select MONTH({o:deliveredDate}),YEAR({o:deliveredDate}),{o:fromCustomer},sum({o:quantity}),{o:deliveredDate}  from {OrderRequisition as o} where {o:toCustomer}=?sclCustomer and {o:status}=?requisitionStatus and {o:deliveredDate} is not null and {o:saleSummaryJobStatus} =?salesSummaryJobStatus group by MONTH({o:deliveredDate}),YEAR({o:deliveredDate}),{o:fromCustomer},{o:deliveredDate}");
            final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
            RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
            params.put("sclCustomer",sclCustomer);
            params.put("requisitionStatus",requisitionStatus);
            params.put("salesSummaryJobStatus",salesSummaryJobStatus);
            query.addQueryParameters(params);
            query.setResultClassList(Arrays.asList(String.class, String.class,SclCustomerModel.class,Double.class, Date.class));
            final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
            List<List<Object>> result = searchResult.getResult();
            return (result != null && !result.isEmpty()) ? result : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(String.valueOf(e));
        }
    }

    @Override
    public OrderRequisitionModel updateJobStatusForOrderRequisition(SclCustomerModel sclCustomer, String salesSummaryJobStatus) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {o:pk} from {OrderRequisition as o} where {o:fromCustomer}=?sclCustomer and {o:status}=?requisitionStatus and {o:saleSummaryJobStatus}=?salesSummaryJobStatus");
        RequisitionStatus requisitionStatus = RequisitionStatus.DELIVERED;
        params.put("sclCustomer",sclCustomer);
        params.put("salesSummaryJobStatus",salesSummaryJobStatus);
        params.put("requisitionStatus",requisitionStatus);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(OrderRequisitionModel.class));
        final SearchResult<OrderRequisitionModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    @Override
    public RetailerSalesSummaryModel validateRecordFromSalesSummary(String month, String year, String dealerNo, String retailerNo, Date startDate, Date endDate) {
        final Map<String, Object> params = new HashMap<String, Object>();
        final StringBuilder builder = new StringBuilder("select {s:pk} from {RetailerSalesSummary as s} where {s:month}=?month and {s:year}=?year and {s:dealerErpCustomerNo}=?dealerNo and {s:retailerErpCustomerNo}=?retailerNo and {s:startDate}=?startDate and {s:endDate}=?endDate ");
        params.put("month",month);
        params.put("year",year);
        params.put("dealerNo",dealerNo);
        params.put("retailerNo",retailerNo);
        params.put("startDate",startDate);
        params.put("endDate",endDate);
        final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
        query.addQueryParameters(params);
        query.setResultClassList(Arrays.asList(RetailerSalesSummaryModel.class));
        final SearchResult<RetailerSalesSummaryModel> searchResult = flexibleSearchService.search(query);
        return CollectionUtils.isNotEmpty(searchResult.getResult()) && Objects.nonNull(searchResult.getResult().get(0)) ? searchResult.getResult().get(0) : null;
    }

    public FlexibleSearchService getFlexibleSearchService() {
        return flexibleSearchService;
    }

    public void setFlexibleSearchService(FlexibleSearchService flexibleSearchService) {
        this.flexibleSearchService = flexibleSearchService;
    }
}
