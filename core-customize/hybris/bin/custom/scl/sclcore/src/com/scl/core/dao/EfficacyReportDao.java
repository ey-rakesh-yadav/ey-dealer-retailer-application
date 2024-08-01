package com.scl.core.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.scl.core.jalo.SclUser;
import com.scl.core.model.CounterVisitMasterModel;
import com.scl.core.model.CronjobsDateLogModel;
import com.scl.core.model.EfficacyReportMasterModel;

import com.scl.core.model.SclCustomerModel;
import com.scl.core.model.SclUserModel;
import com.scl.core.model.SubAreaMasterModel;
import com.scl.core.model.VisitMasterModel;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.UserModel;

public interface EfficacyReportDao {

	public List<CounterVisitMasterModel> findCounterVisitForMonthYear(int month, int year, SubAreaMasterModel subArea);
	
	public Double findAfterSaleForDealer(Date visitDate, SclCustomerModel customer);
	public Double findBeforeSaleForDealer(Date visitDate, SclCustomerModel customer);

	public Double findAfterSaleForRetailer(Date visitDate, SclCustomerModel customer);
	public Double findBeforeSaleForRetailer(Date visitDate, SclCustomerModel customer);

	public EfficacyReportMasterModel getEfficacyReportForMonth(Integer month, Integer year, SubAreaMasterModel subarea, UserModel userModel);

	List<List<Double>> getOutstandingAmountAndDailyAverageSalesWithinDate(String customerCode, Date date);
	
	List<ProductModel> getAllNewProducts(Date startDate, Date endDate);
	
    List<VisitMasterModel> getAllVisitMasterForSubAreaAndSO(Date startDate, Date endDate, SubAreaMasterModel subarea, SclUserModel so);
	
	List<List<Object>> getSalesForNewProducts(List<ProductModel> productList, SclCustomerModel sclCustomer, Date startDate, Date endDate);

	Double getMonthlySalesForNewProduct(ProductModel product, List<SclCustomerModel> sclCustomerList, Date startDate, Date endDate);

	Double getMonthlySalesForDealer(SclCustomerModel dealer, Date startDate, Date endDate);
	
	Double getMonthlySalesForRetailer(SclCustomerModel retailer, Date startDate, Date endDate);

	List<SclCustomerModel> getObsoleteCountersList(SclUserModel sclUser, Date startDate, Date endDate);
	
	List<SclCustomerModel> getRevivedCountersList(List<SclCustomerModel> obsoleteCounters);

	EfficacyReportMasterModel getEfficacyReportsMaster(String efficacyId);
	
	CronjobsDateLogModel getCronjobsDateLog();
	
	Double getSalesForCustomerList(List<SclCustomerModel> customerList, Date startDate, Date endDate);

	Double getActualSalesForDealer(SclCustomerModel sclCustomer, BaseSiteModel baseSite, Date startDate, Date endDate);
}
