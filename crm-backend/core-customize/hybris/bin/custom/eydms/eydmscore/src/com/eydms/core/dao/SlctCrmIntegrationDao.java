package com.eydms.core.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.eydms.core.enums.CustomerCategory;
import com.eydms.core.enums.OrderType;
import com.eydms.core.jalo.EyDmsSiteMaster;
import com.eydms.core.model.*;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.core.model.c2l.BrandModel;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.europe1.model.PriceRowModel;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.ordersplitting.model.VendorModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;

public interface SlctCrmIntegrationDao {

	List<MarketMappingDetailsModel> getAllMarketMappingDetails();

	List<VisitMasterModel> getAllVisit();

	List<CounterVisitMasterModel> getAllCounterVisit();

	List<EyDmsCustomerModel> getAllInfluencerDetails();

	List<List<Object>> getAllRetailerSalesInfo();

	public List<DestinationSourceMasterModel> getLpSourceMasterList(BaseSiteModel brand,
																	DeliveryModeModel deliveryMode, String grade, String packaging, String destCityId);

	public CreditAndOutstandingModel getCrmOutstandingDetails(String customerCode);

	public DJPCounterScoreMasterModel getCounterScoreDetails(String id);

	public DJPRouteScoreMasterModel getRouteScoreDetails(String id);

	public ObjectiveModel getObjectiveDetails(String objectiveId);

	public EyDmsCustomerModel getCustomerDetails(String uid);
	public DJPRunMasterModel getRunMasterDetails(String id);
	public RouteMasterModel getRouteMasterDetails(String routeId);
	public EyDmsCustomerModel getMitraMasterDetails(String customerID);
	public List<OrderEntryModel> getOrderLineScheduleDetails();

	public EyDmsCustomerModel getCustomerByCustNo(String customerNo);

	public CreditAndOutstandingModel getCreditByCustCode(String custCode);

	public List<List<Object>> getSlctCrmCustomerDetails();

	public List<ProspectiveNetworkModel> deleteAllProspectiveNetworkDetails();

	List<LedgerDetailsModel> deleteLedgerDetails(Date transactionDate);

	public List<ISOMasterModel> getIsoMasterDetails();

	public OutstandingHistoryModel getOutstandingHistory(String custCode);

	OrderEntryModel getOrderEntryByDiNumber(String diNumber);

	List<ISOMasterModel> getIsoMasterDetailsByDeliveryId(String deliveryId);

	AddressModel getAddressByErpAddressId(String erpAddressId);

	CustomerSubAreaMappingModel getCustomerSubAreaMapByCustomer(EyDmsCustomerModel eydmsCustomerModel);

	SubAreaMasterModel getSubAreaByTalukaAndDistrict(String taluka, String district);

	PrincipalGroupModel getPrincipalGroupByUid(String uid);

	CountryModel getCountryByIsoCode(String isoCode);

	List<MonthlySalesModel> getMonthlySalesModel(String monthYear);

	List<DealerPlannedMonthlySalesModel> getDealerPannedMonthlySalesDetails(List<SubAreaMasterModel> subAreaMasterList, EyDmsUserModel salesOfficer, String monthName, String monthYear);

	GeographicalMasterModel getGeographicalMasterModel(String taluka, String district, String city, String state);

	FreightAndIncoTermsMasterModel getFreightAndIncoTermsMasterModel(String brand, String district, String state, String orgType);

	SalesOrderDeliverySLAModel getDeliverySlaModel(String route);

	SalesHistoryModel getSalesHistoryModel(String customerTransactionId, String customerTransactionLineId, CMSSiteModel brand);

	WarehouseModel findWarehouseByCode(String code);

	VendorModel findVendorByCode(String code);

	EtaTrackerModel getEtaByDiNumber(String deliveryId);

	LedgerDetailsModel getLedgerDetails(String transactionLineGlRcId, Date transactionLineLastUpdateDate);

	List<OrderEntryModel> getOrderLinesForEpod();

	OrderEntryModel findOrderEntryByErpLineItemId(String erpLineItemId);

	List<DestinationSourceMasterModel> getAllDestinationSourceRecords();

	DeliveryModeModel getDeliveryMode(String deliveryMode);

	DepotSubAreaMappingModel getDepotSubAreaMappingModel(String taluka, String district, String state, String depotCode, String brand);

	RouteMasterModel getRouteMaster(String taluka, String district, String state, String brand);
	CounterRouteMappingModel getCounterRouteMapping(String customerUid);

	EyDmsUserModel getDoByUidAndEmpCode(String uid);

	UserSubAreaMappingModel getDoSubAreaMappingUid(String soUid, String brand, SubAreaMasterModel subAreaMaster, String state);

	CurrencyModel getCurrencyModelByISOCode(String isoCode);

	Date getInvoiceDateFromNcr(String erpLineItemId);

	List<List<Object>> getCountofLeadsGenerated(Integer year, Integer month);

	List<List<Object>> getProductSaleDetails(EyDmsUserModel so, String monthName, String monthYear, List<SubAreaMasterModel> subAreaMasterModelList, DistrictMasterModel districtMaster, CMSSiteModel brand);

	ProductModel getProductModelByGradeAndBagType(String grade, String bagType, CatalogVersionModel catalogVersion);

	List<MonthlySalesModel> getTopDownSpMonthlySales(String monthName, String monthYear, String district, String state, String taluka, BaseSiteModel brand);

	List<AnnualSalesModel> getAnnualSalesModel(String financialYear);

	List<List<Object>> getProductDetailsFromAnnualSales(DistrictMasterModel districtMasterModel, BaseSiteModel brand);

	List<List<Object>> getMonthWiseTargets(DistrictMasterModel districtMasterModel, BaseSiteModel brand, String productCode);

	BrandModel getBrandByIsoCode(String isoCode);

	CompetitorProductModel getCompetitorProductByCodeAndState(String code, String state, String brand);

	List<OrderModel> getModifiedOrdersOfYesterday(String date);

	List<ProductSaleModel> getProductSaleModels();

	List<OrderEntryModel> getModifiedOrderEntries(String date);

	String getStateCode(String stateName);

    IntegrationSettingModel getIntegrationSettingModelByName(String integrationName);

	String appendIntegrationSetting(String integrationName, Map<String, Object> params);

	List<PointRequisitionModel> getPointRequisitionDetails();

	List<SiteVisitMasterModel> getSiteVisitForms();

	List<EyDmsSiteMasterModel> getSiteMasterDetailsForSLCT();


    List<EndCustomerComplaintModel> getEndCustomerComplaintsForSLCT(String startDate, String endDate);

	List<InfluencerVisitMasterModel> getInfluencerVisitDetails();

    ProductPointMasterModel getProductPointMasterModel(String schemeId, ProductModel product);

	PriceRowModel getPriceRowByProduct(String productCode);

	GiftSchemeModel getSchemesDefinitionById(String schemeId, String state, String influencerType);

	GiftModel getGiftModelByCode(String code, CatalogVersionModel catalogVersion);

	StockLevelModel getStockLevelByProductAndWarehouse(String productCode, WarehouseModel warehouseModel);

	List<List<Object>> getGiftRedemptionForSLCT();
	List<OrderEntryModel> getGiftRedemptionsForSLCT();

	TsoTalukaMappingModel getTsoTalukaMappingByEmp(String soUid, String brand, SubAreaMasterModel subAreaMaster);
}