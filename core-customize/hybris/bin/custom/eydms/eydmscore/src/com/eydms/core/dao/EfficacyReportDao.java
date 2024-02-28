package com.eydms.core.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.eydms.core.jalo.EyDmsUser;
import com.eydms.core.model.CounterVisitMasterModel;
import com.eydms.core.model.CronjobsDateLogModel;
import com.eydms.core.model.EfficacyReportMasterModel;

import com.eydms.core.model.EyDmsCustomerModel;
import com.eydms.core.model.EyDmsUserModel;
import com.eydms.core.model.SubAreaMasterModel;
import com.eydms.core.model.VisitMasterModel;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.UserModel;

public interface EfficacyReportDao {

	public List<CounterVisitMasterModel> findCounterVisitForMonthYear(int month, int year, SubAreaMasterModel subArea);
	
	public Double findAfterSaleForDealer(Date visitDate, EyDmsCustomerModel customer);
	public Double findBeforeSaleForDealer(Date visitDate, EyDmsCustomerModel customer);

	public Double findAfterSaleForRetailer(Date visitDate, EyDmsCustomerModel customer);
	public Double findBeforeSaleForRetailer(Date visitDate, EyDmsCustomerModel customer);

	public EfficacyReportMasterModel getEfficacyReportForMonth(Integer month, Integer year, SubAreaMasterModel subarea, UserModel userModel);

	List<List<Double>> getOutstandingAmountAndDailyAverageSalesWithinDate(String customerCode, Date date);
	
	List<ProductModel> getAllNewProducts(Date startDate, Date endDate);
	
    List<VisitMasterModel> getAllVisitMasterForSubAreaAndSO(Date startDate, Date endDate, SubAreaMasterModel subarea, EyDmsUserModel so);
	
	List<List<Object>> getSalesForNewProducts(List<ProductModel> productList, EyDmsCustomerModel eydmsCustomer, Date startDate, Date endDate);

	Double getMonthlySalesForNewProduct(ProductModel product, List<EyDmsCustomerModel> eydmsCustomerList, Date startDate, Date endDate);

	Double getMonthlySalesForDealer(EyDmsCustomerModel dealer, Date startDate, Date endDate);
	
	Double getMonthlySalesForRetailer(EyDmsCustomerModel retailer, Date startDate, Date endDate);

	List<EyDmsCustomerModel> getObsoleteCountersList(EyDmsUserModel eydmsUser, Date startDate, Date endDate);
	
	List<EyDmsCustomerModel> getRevivedCountersList(List<EyDmsCustomerModel> obsoleteCounters);

	EfficacyReportMasterModel getEfficacyReportsMaster(String efficacyId);
	
	CronjobsDateLogModel getCronjobsDateLog();
	
	Double getSalesForCustomerList(List<EyDmsCustomerModel> customerList, Date startDate, Date endDate);

	Double getActualSalesForDealer(EyDmsCustomerModel eydmsCustomer, BaseSiteModel baseSite, Date startDate, Date endDate);
}
