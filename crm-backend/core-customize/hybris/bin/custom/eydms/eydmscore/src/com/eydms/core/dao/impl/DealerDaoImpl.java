package com.eydms.core.dao.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.eydms.core.enums.PointRequisitionStatus;
import com.eydms.core.enums.RequisitionStatus;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.eydms.core.dao.DealerDao;
import com.eydms.core.model.ReceiptAllocaltionModel;
import com.eydms.core.model.RetailerRecAllocateModel;
import com.eydms.core.model.EyDmsCustomerModel;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.user.UserService;

public class DealerDaoImpl implements DealerDao{

	private static final Logger LOGGER = Logger.getLogger(EyDmsUserDaoImpl.class);

	@Autowired
	FlexibleSearchService flexibleSearchService;
	
	@Resource
    UserService userService;

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }
    
	@Override
	public List<List<Object>> getMonthWiseForRetailerMTD(List<EyDmsCustomerModel> cusUids, Date startDate, Date endDate) {
		/*for (EyDmsCustomerModel cusUid : cusUids) {
			LOGGER.info(String.format("Getting Sale QTY for customerNo :: %s startDate :: %s endDate :: %s",cusUid,startDate,endDate));
		}*/

		final Map<String, Object> params = new HashMap<String, Object>();
		if(cusUids.isEmpty()) {
			final StringBuilder builder = new StringBuilder("select year({o.deliveredDate}), month({o.deliveredDate}), sum({o.quantity}) from {OrderRequisition AS o} where {o:status}=?status and {o.deliveredDate}>=?startDate and {o.deliveredDate}<?endDate  group by year( {o.deliveredDate}), month({o.deliveredDate}) order by year({o.deliveredDate}) desc, month({o.deliveredDate}) desc");
			params.put("startDate", startDate);
			params.put("endDate", endDate);


			params.put("status", RequisitionStatus.DELIVERED);
			final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
			query.addQueryParameters(params);
			query.setResultClassList(Arrays.asList(Integer.class,Integer.class,Double.class));
			final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
			return searchResult.getResult();
		}
		else
		{
			final StringBuilder builder = new StringBuilder("select year({o.deliveredDate}), month({o.deliveredDate}), sum({o.quantity}) from {OrderRequisition AS o} where {o:status}=?status  and {o.toCustomer} IN (?cusUids) and {o.deliveredDate}>=?startDate and {o.deliveredDate}<?endDate  group by year( {o.deliveredDate}), month({o.deliveredDate}) order by year({o.deliveredDate}) desc, month({o.deliveredDate}) desc");
			params.put("startDate", startDate);
			params.put("endDate", endDate);
			params.put("cusUids", cusUids);

			params.put("status", RequisitionStatus.DELIVERED);
			final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
			query.addQueryParameters(params);
			query.setResultClassList(Arrays.asList(Integer.class,Integer.class,Double.class));
			final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
			return searchResult.getResult();
		}

	}

	@Override
	public List<List<Object>> getMonthWiseForInfluencerMTD(List<EyDmsCustomerModel> cusUids, Date startDate, Date endDate) {
		final Map<String, Object> params = new HashMap<String, Object>();
		if(cusUids.isEmpty()) {
			final StringBuilder builder = new StringBuilder("select year({p.deliveryDate}), month({p.deliveryDate}), sum({p.quantity}) from {PointRequisition AS p } where {p:status}=?status and {p.deliveryDate}>=?startDate and {p.deliveryDate}<?endDate group by year({p.deliveryDate}), month({p.deliveryDate}) order by year({p.deliveryDate}) desc, month({p.deliveryDate}) desc");
			PointRequisitionStatus status = PointRequisitionStatus.APPROVED;
			params.put("status", status);
			params.put("startDate", startDate);
			params.put("endDate", endDate);

			final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
			query.addQueryParameters(params);
			query.setResultClassList(Arrays.asList(Integer.class, Integer.class, Double.class));
			final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
			return searchResult.getResult();
		}
		else{
			final StringBuilder builder = new StringBuilder("select year({p.deliveryDate}), month({p.deliveryDate}), sum({p.quantity}) from {PointRequisition AS p } where {p:requestRaisedFor} IN (?cusUids) and {p:status}=?status and {p.deliveryDate}>=?startDate and {p.deliveryDate}<?endDate group by year({p.deliveryDate}), month({p.deliveryDate}) order by year({p.deliveryDate}) desc, month({p.deliveryDate}) desc");
			PointRequisitionStatus status = PointRequisitionStatus.APPROVED;
			params.put("status", status);
			params.put("startDate", startDate);
			params.put("endDate", endDate);
			params.put("cusUids", cusUids);

			final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
			query.addQueryParameters(params);
			query.setResultClassList(Arrays.asList(Integer.class, Integer.class, Double.class));
			final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
			return searchResult.getResult();
		}
	}

	@Override
	public Double getMonthWiseForRetailerYTD(List<EyDmsCustomerModel> cusUids, Date startDate, Date endDate) {
		final Map<String, Object> params = new HashMap<String, Object>();
		if(cusUids.isEmpty()) {
			final StringBuilder builder = new StringBuilder("select sum({o.quantity}) from {OrderRequisition AS o } where {o:status}=?status and {o.deliveredDate}>=?startDate and {o.deliveredDate}<?endDate ");
			params.put("startDate", startDate);
			params.put("endDate", endDate);

			params.put("status", RequisitionStatus.DELIVERED);
			final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
			query.setResultClassList(Arrays.asList(Double.class));
			query.addQueryParameters(params);
			final SearchResult<Double> searchResult = flexibleSearchService.search(query);
			if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
				return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
			else
				return 0.0;

		}
		else {
			final StringBuilder builder = new StringBuilder("select sum({o.quantity}) from {OrderRequisition AS o } where {o:status}=?status  and  {o.toCustomer} IN (?cusUids) and {o.deliveredDate}>=?startDate and {o.deliveredDate}<?endDate ");
			params.put("startDate", startDate);
			params.put("endDate", endDate);
			params.put("cusUids", cusUids);

			params.put("status", RequisitionStatus.DELIVERED);
			final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
			query.setResultClassList(Arrays.asList(Double.class));
			query.addQueryParameters(params);
			final SearchResult<Double> searchResult = flexibleSearchService.search(query);
			if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
				return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
			else
				return 0.0;
		}
	}
	
	@Override
	public Double getMonthWiseForInfluencerYTD(List<EyDmsCustomerModel> cusUids, Date startDate, Date endDate) {
		if(cusUids.isEmpty()) {
			final Map<String, Object> params = new HashMap<String, Object>();
			final StringBuilder builder = new StringBuilder("select sum({p.quantity}) from {PointRequisition AS p } where  {p:status}=?status and {p.deliveryDate}>=?startDate and {p.deliveryDate}<?endDate ");
			PointRequisitionStatus status = PointRequisitionStatus.APPROVED;

			params.put("status", status);
			params.put("startDate", startDate);
			params.put("endDate", endDate);

			final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
			query.setResultClassList(Arrays.asList(Double.class));
			query.addQueryParameters(params);
			final SearchResult<Double> searchResult = flexibleSearchService.search(query);
			if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
				return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
			else
				return 0.0;
		}
		else {
			final Map<String, Object> params = new HashMap<String, Object>();
			final StringBuilder builder = new StringBuilder("select sum({p.quantity}) from {PointRequisition AS p } where  {p:status}=?status and {p.requestRaisedFor} IN (?cusUids) and {p.deliveryDate}>=?startDate and {p.deliveryDate}<?endDate ");
			PointRequisitionStatus status = PointRequisitionStatus.APPROVED;

			params.put("status", status);
			params.put("startDate", startDate);
			params.put("endDate", endDate);
			params.put("cusUids", cusUids);

			final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
			query.setResultClassList(Arrays.asList(Double.class));
			query.addQueryParameters(params);
			final SearchResult<Double> searchResult = flexibleSearchService.search(query);
			if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
				return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
			else
				return 0.0;
		}
	}

	@Override
	public ReceiptAllocaltionModel getDealerAllocation(ProductModel productCode, EyDmsCustomerModel dealerCode) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {ReceiptAllocaltion} WHERE {dealerCode}=?dealerCode AND {product}=?product");
		
		params.put("dealerCode", dealerCode);
		params.put("product", productCode);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(ReceiptAllocaltionModel.class));
		query.addQueryParameters(params);
		final SearchResult<ReceiptAllocaltionModel> searchResult = flexibleSearchService.search(query);
		LOGGER.info("In DealerDao:getDealerAllocation method--> query:::" + builder.toString() + ":::Product Code:::" + productCode.getPk().toString() + ":::Dealer Code:::" + dealerCode.getPk().toString());
		LOGGER.info("In DealerDao:getDealerAllocation method--> Show the result of the query:::" + searchResult.getResult());
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : null;	
		else
			return null;
	}
	
	@Override
	public List<List<Integer>>  getDealerTotalAllocation(EyDmsCustomerModel dealerCode) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT SUM({stockAvlForInfluencer}), SUM({stockAvlForRetailer}) FROM {ReceiptAllocaltion} WHERE {dealerCode}=?dealerCode");
		params.put("dealerCode", dealerCode);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Integer.class,Integer.class));
		query.addQueryParameters(params);
		final SearchResult<List<Integer>> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult()!=null?searchResult.getResult():Collections.emptyList();
	}
	
	@Override
	public RetailerRecAllocateModel getRetailerAllocation(ProductModel productCode, EyDmsCustomerModel dealerCode) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {RetailerRecAllocate} WHERE {dealerCode}=?dealerCode AND {product}=?product");
		
		params.put("dealerCode", dealerCode);
		params.put("product", productCode);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(RetailerRecAllocateModel.class));
		query.addQueryParameters(params);
		final SearchResult<RetailerRecAllocateModel> searchResult = flexibleSearchService.search(query);
		if(searchResult.getResult()!=null&&!(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0)!=null ? searchResult.getResult().get(0) : null;	
		else
			return null;
	}
	
	@Override
	public List<List<Integer>>  getRetailerTotalAllocation(EyDmsCustomerModel dealerCode) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT SUM({stockAvlForInfluencer}) FROM {RetailerRecAllocate} WHERE {dealerCode}=?dealerCode");
		params.put("dealerCode", dealerCode);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Integer.class,Integer.class));
		query.addQueryParameters(params);
		final SearchResult<List<Integer>> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult()!=null?searchResult.getResult():Collections.emptyList();
	}
	
}
