package com.scl.core.dao.impl;

import java.time.LocalDate;
import java.util.*;

import javax.annotation.Resource;

import com.scl.core.enums.CounterType;
import com.scl.core.enums.PointRequisitionStatus;
import com.scl.core.enums.RequisitionStatus;
import com.scl.core.model.*;
import com.scl.core.region.dao.impl.DistrictMasterDaoImpl;
import org.apache.commons.collections.CollectionUtils;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.dao.DealerDao;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.user.UserService;

public class DealerDaoImpl implements DealerDao{

	private static final Logger LOGGER = Logger.getLogger(SclUserDaoImpl.class);

	@Autowired
	FlexibleSearchService flexibleSearchService;
	@Resource
	DistrictMasterDaoImpl districtMasterDao;
	@Resource
	TerritoryManagementDaoImpl territoryManagementDao;
	
	@Resource
    UserService userService;

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

	private static final Logger LOG = Logger.getLogger(DealerDaoImpl.class);
    
	@Override
	public List<List<Object>> getMonthWiseForRetailerMTD(List<SclCustomerModel> cusUids, List<String> districtList, List<String> subAreaList, Date startDate, Date endDate) {
		/*for (SclCustomerModel cusUid : cusUids) {
			LOGGER.info(String.format("Getting Sale QTY for customerNo :: %s startDate :: %s endDate :: %s",cusUid,startDate,endDate));
		}*/

		List<DistrictMasterModel> tempDistricelist = new ArrayList<>();
		List<SubAreaMasterModel> tempSubAreaList = new ArrayList<>();

		final Map<String, Object> params = new HashMap<String, Object>();
		if(CollectionUtils.isNotEmpty(cusUids)) {
			final StringBuilder builder = new StringBuilder("select year({o.deliveredDate}), month({o.deliveredDate}), sum({o.quantity}) from {OrderRequisition AS o} where {o:status}=?status  and {o.toCustomer} IN (?cusUids) and {o.deliveredDate}>=?startDate and {o.deliveredDate}<?endDate  group by year( {o.deliveredDate}), month({o.deliveredDate}) order by year({o.deliveredDate}) desc, month({o.deliveredDate}) desc");
			params.put("startDate", startDate);
			params.put("endDate", endDate);
			params.put("cusUids", cusUids);
			/*if (CollectionUtils.isNotEmpty(districtList)) {
				for (String code : districtList) {
					tempDistricelist.add(districtMasterDao.findByCode(code));
				}
				params.put("doList", tempDistricelist);
				builder.append(" and {o.districtMaster} in (?doList) ");
			}
			if (CollectionUtils.isNotEmpty(subAreaList)) {
				for (String id : subAreaList) {
					tempSubAreaList.add(territoryManagementDao.getTerritoryById(id));
				}
				params.put("subAreaList", tempSubAreaList);
				builder.append(" and {o.subAreaMaster} in (?subAreaList) ");
			}*/
			params.put("status", RequisitionStatus.DELIVERED);
			builder.append("group by year( {o.deliveredDate}), month({o.deliveredDate}) order by year({o.deliveredDate}) desc, month({o.deliveredDate}) desc");
			LOGGER.info(String.format("Query For getMonthWiseForRetailerMTD :: %s", builder.toString()));
			final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
			query.addQueryParameters(params);
			query.setResultClassList(Arrays.asList(Integer.class,Integer.class,Double.class));
			final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
			return searchResult.getResult();
		}
		else
		{
			final StringBuilder builder = new StringBuilder("select year({o.deliveredDate}), month({o.deliveredDate}), sum({o.quantity}) from {OrderRequisition AS o} where {o:status}=?status and {o.deliveredDate}>=?startDate and {o.deliveredDate}<?endDate ");
			params.put("startDate", startDate);
			params.put("endDate", endDate);
			params.put("cusUids", cusUids);

			params.put("status", RequisitionStatus.DELIVERED);
			LOGGER.info(String.format("Query For getMonthWiseForRetailerMTD :: %s", builder.toString()));
			final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
			query.addQueryParameters(params);
			query.setResultClassList(Arrays.asList(Integer.class,Integer.class,Double.class));
			final SearchResult<List<Object>> searchResult = flexibleSearchService.search(query);
			return searchResult.getResult();
		}

	}

	@Override
	public List<List<Object>> getMonthWiseForInfluencerMTD(List<SclCustomerModel> cusUids, Date startDate, Date endDate) {
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
	public Double getMonthWiseForRetailerYTD(List<SclCustomerModel> cusUids, List<String> districtList, List<String> subAreaList, Date startDate, Date endDate) {
		final Map<String, Object> params = new HashMap<String, Object>();

		List<DistrictMasterModel> tempDistricelist = new ArrayList<>();
		List<SubAreaMasterModel> tempSubAreaList = new ArrayList<>();

		if(CollectionUtils.isNotEmpty(cusUids)) {
			final StringBuilder builder = new StringBuilder("select sum({o.quantity}) from {OrderRequisition AS o } where {o:status}=?status and {o.deliveredDate}>=?startDate and {o.deliveredDate}<?endDate and {o.toCustomer} in (cusUids) ");
			params.put("startDate", startDate);
			params.put("endDate", endDate);
			params.put("status", RequisitionStatus.DELIVERED);
			params.put("cusUids",cusUids);

			if (CollectionUtils.isNotEmpty(districtList)) {
				for (String code : districtList) {
					tempDistricelist.add(districtMasterDao.findByCode(code));
				}
				params.put("doList", tempDistricelist);
				builder.append(" and {o.districtMaster} in (?doList) ");
			}
			if (CollectionUtils.isNotEmpty(subAreaList)) {
				for (String id : subAreaList) {
					tempSubAreaList.add(territoryManagementDao.getTerritoryById(id));
				}
				params.put("subAreaList", tempSubAreaList);
				builder.append(" and {o.subAreaMaster} in (?subAreaList) ");
			}
			LOGGER.info(String.format("Query for getMonthWiseForRetailerYTD :: %s",builder.toString()));
			final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
			query.setResultClassList(Arrays.asList(Double.class));
			query.addQueryParameters(params);
			final SearchResult<Double> searchResult = flexibleSearchService.search(query);
			if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
				return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : 0.0;
			else
				return 0.0;

		}else{
			return 0.0;
		}
	}
	
	@Override
	public Double getMonthWiseForInfluencerYTD(List<SclCustomerModel> cusUids, Date startDate, Date endDate) {
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
	public ReceiptAllocaltionModel getDealerAllocation(ProductModel productCode, SclCustomerModel dealerCode) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {ReceiptAllocaltion} WHERE {dealerCode}=?dealerCode AND {product}=?product AND {year}=?year AND {month}=?month");
		LocalDate currentDate = LocalDate.now();
		Integer month=currentDate.getMonth().getValue();
		Integer year=currentDate.getYear();
		params.put("dealerCode", dealerCode);
		params.put("product", productCode);
		params.put("year",year);
		params.put("month",month);
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
	public List<List<Integer>>  getDealerTotalAllocation(SclCustomerModel dealerCode) {
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
	public RetailerRecAllocateModel getRetailerAllocation(ProductModel productCode, SclCustomerModel dealerCode) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {pk} FROM {RetailerRecAllocate} WHERE {dealerCode}=?dealerCode AND {product}=?product AND {year}=?year AND {month}=?month" );
		LocalDate currentDate = LocalDate.now();
		Integer month=currentDate.getMonth().getValue();
		Integer year=currentDate.getYear();
		params.put("dealerCode", dealerCode);
		params.put("product", productCode);
		params.put("year",year);
		params.put("month",month);
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
	public List<List<Integer>>  getRetailerTotalAllocation(SclCustomerModel dealerCode) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT SUM({stockAvlForInfluencer}) FROM {RetailerRecAllocate} WHERE {dealerCode}=?dealerCode");
		params.put("dealerCode", dealerCode);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Arrays.asList(Integer.class,Integer.class));
		query.addQueryParameters(params);
		final SearchResult<List<Integer>> searchResult = flexibleSearchService.search(query);
		return searchResult.getResult()!=null?searchResult.getResult():Collections.emptyList();
	}

	@Override
	public List<SclCustomerModel> getDealerFromTerritoryCode(TerritoryMasterModel territoryMasterModel) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("SELECT {sc.pk} FROM {SclCustomer as sc JOIN EnumerationValue as e on {e.pk}={sc.counterType}} where {e.code}=?counterType  AND {sc:territoryCode}=?territoryCode");

		params.put("territoryCode", territoryMasterModel);
		params.put("counterType", String.valueOf(CounterType.DEALER));
		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(SclCustomerModel.class));
		query.addQueryParameters(params);
		final SearchResult<SclCustomerModel> searchResult = flexibleSearchService.search(query);
		if (searchResult == null)
		{
			LOGGER.debug("DealerRetailerMapping search: No Results found ! ");
			return Collections.emptyList();
		}
		return searchResult.getResult();
	}


	@Override
	public List<SclUserModel> getSclUserUsingTerritoryCode(String trriId){
		final Map<String, Object> params = new HashMap<String, Object>();

		String queryBuild = "SELECT {pk} FROM {scluser as su join territorymaster as tm on {tm.scluser}={su.pk}}  WHERE {tm.territoryCode} =?territoryCode";
		params.put("territoryCode",trriId);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryBuild);
		query.setResultClassList(Collections.singletonList(SclUserModel.class));
		query.addQueryParameters(params);
		final SearchResult<SclUserModel> searchResult = flexibleSearchService.search(query);

		return CollectionUtils.isNotEmpty(searchResult.getResult())? searchResult.getResult():null;
	}


	@Override
	public List<SclUserModel> getSclUserUsingTerritoryusermap(TerritoryMasterModel trriId){
		final Map<String, Object> params = new HashMap<String, Object>();

		String queryBuild = "SELECT {su.pk} FROM {scluser as su join territoryusermapping as tum on {tum.scluser}={su.pk}}  WHERE {tum.territoryMaster} =?territoryCode and {tum.validFrom}<=CURRENT_TIMESTAMP and {tum.VALIDTO}>=CURRENT_TIMESTAMP";
		params.put("territoryCode",trriId);

		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryBuild);
		query.setResultClassList(Collections.singletonList(SclUserModel.class));
		query.addQueryParameters(params);
		final SearchResult<SclUserModel> searchResult = flexibleSearchService.search(query);

		return CollectionUtils.isNotEmpty(searchResult.getResult())? searchResult.getResult():null;
	}

	@Override
	public PartnerCustomerModel getPartnerCustomerById(String id) {
		final Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder builder = new StringBuilder("select {pk} from {PartnerCustomer} where {id} = ?id and {active} = ?active and {validityExpired} >= ?currentDate");
		params.put("id", id);
		params.put("active", Boolean.TRUE);
		params.put("currentDate", new Date());

		final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
		query.setResultClassList(Collections.singletonList(PartnerCustomerModel.class));
		query.addQueryParameters(params);
		LOG.info(String.format("getPartnerCustomerQuery for Partner Customer ID %s :%s",id,query));
		final SearchResult<PartnerCustomerModel> searchResult = flexibleSearchService.search(query);
		if (searchResult.getResult() != null && !(searchResult.getResult().isEmpty()))
			return searchResult.getResult().get(0) != null ? searchResult.getResult().get(0) : null;
		else
			return null;
	}


	/**
	 * @param active
	 * @param mobileNumber
	 * @return
	 */
	@Override
	public boolean isPartnerActiveMobilePresent(Boolean active, String mobileNumber, String partnerId, SclCustomerModel dealer){
		final Map<String, Object> attr = new HashMap<String, Object>();
		final StringBuilder sql = new StringBuilder("select {pk} from {PartnerCustomer} where {sclCustomer} = ?dealer and {active}=?active and {mobileNumber}=?mobileNumber and {id}!=?partnerId");
		attr.put("active", Boolean.TRUE);
		attr.put("mobileNumber", mobileNumber);
		attr.put("partnerId", partnerId);
		attr.put("dealer", dealer);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
		query.setResultClassList(Arrays.asList(PartnerCustomerModel.class));
		query.getQueryParameters().putAll(attr);
		LOG.info(String.format("isPartnerActiveMobilePresent query :: %s",query));
		final SearchResult<PartnerCustomerModel> result = flexibleSearchService.search(query);
		return CollectionUtils.isNotEmpty(result.getResult());
	}

	/**
	 * @param mobileNumber
	 * @return
	 */
	@Override
	public boolean isCustomerMobileNumberPresent(String mobileNumber) {
		final Map<String, Object> attr = new HashMap<String, Object>();
		final StringBuilder sql = new StringBuilder("select {pk} from {Sclcustomer} where {mobileNumber}=?mobileNumber");
		attr.put("mobileNumber", mobileNumber);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(sql.toString());
		query.setResultClassList(Arrays.asList(SclCustomerModel.class));
		query.getQueryParameters().putAll(attr);
		LOG.info(String.format("isCustomerMobileNumberPresent query :: %s",query));
		final SearchResult<SclCustomerModel> result = flexibleSearchService.search(query);
		return CollectionUtils.isNotEmpty(result.getResult());
	}

}
