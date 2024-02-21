package com.eydms.core.services.impl;

import static com.google.common.base.Preconditions.checkArgument;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.ServletException;

import com.eydms.core.dao.*;
import com.eydms.core.enums.*;
import com.eydms.core.model.*;
import com.eydms.core.notifications.service.EyDmsNotificationService;
import com.eydms.core.order.EYDMSB2BOrderService;
import com.eydms.facades.data.*;

import de.hybris.platform.basecommerce.enums.InStockStatus;
import de.hybris.platform.catalog.enums.ArticleApprovalStatus;
import de.hybris.platform.category.CategoryService;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.commerceservices.setup.SetupSyncJobService;
import de.hybris.platform.core.model.c2l.BrandModel;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.europe1.model.PriceRowModel;
import de.hybris.platform.hac.data.dto.SqlSearchResultData;
import de.hybris.platform.hac.facade.HacFlexibleSearchFacade;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.ordersplitting.model.VendorModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.apache.log4j.Logger;
import com.eydms.core.cart.dao.EyDmsWarehouseDao;
import com.eydms.core.constants.EyDmsCoreConstants;
import com.eydms.core.services.SlctCrmIntegrationService;
import com.eydms.core.services.TerritoryManagementService;
import com.eydms.core.source.dao.DestinationSourceMasterDao;
import com.eydms.facades.order.data.IntegrationOrderData;
import com.eydms.facades.order.data.IntegrationOrderEntryData;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.services.B2BOrderService;
import de.hybris.platform.b2b.services.impl.DefaultB2BUnitService;
import de.hybris.platform.b2b.enums.CheckoutPaymentType;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.customer.CustomerAccountService;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.order.DeliveryModeService;
import de.hybris.platform.ordersplitting.WarehouseService;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.outboundservices.facade.OutboundServiceFacade;
import de.hybris.platform.product.UnitService;
import de.hybris.platform.search.restriction.SearchRestrictionService;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;

public class SlctCrmIntegrationServiceImpl implements SlctCrmIntegrationService {

	private static final Logger LOG = Logger.getLogger(SlctCrmIntegrationServiceImpl.class);

	@Autowired
	SlctCrmIntegrationDao slctCrmIntegrationDao;

@Autowired
private SetupSyncJobService setupSyncJobService;
	@Autowired
	TechnicalAssistanceDao technicalAssistanceDao;
	@Autowired
	CustomerAccountService customerAccountService;

	@Resource
	FlexibleSearchService flexibleSearchService;

	@Autowired
	UserService userService;

	@Autowired
	ModelService modelService;

	@Resource
	BaseSiteService baseSiteService;

	@Resource
	EnumerationService enumerationService;

	@Resource
	WarehouseService warehouseService;

	@Resource
	DefaultB2BUnitService defaultB2BUnitService;

	@Autowired
	DestinationSourceMasterDao destinationSourceMasterDao;

	@Autowired
	KeyGenerator customCodeGenerator;

	@Autowired
	B2BOrderService b2bOrderService;

	private KeyGenerator keyGenerator;

	@Autowired
	private CommonI18NService commonI18NService;

	@Autowired
	private CatalogVersionService catalogVersionService;

	@Autowired
	private UnitService unitService;

	@Autowired
	private EyDmsWarehouseDao eydmsWarehouseDao;

	@Autowired
	DeliveryModeService deliveryModeService;

	@Autowired
	TerritoryManagementService territoryService;

	@Autowired
	TerritoryManagementDao territoryManagementDao;

	@Autowired
	private OutboundServiceFacade outboundServiceFacade;

	@Autowired
	private EYDMSB2BOrderService eydmsB2BOrderService;

	@Autowired
	ProductService productService;

	@Autowired
	private SessionService sessionService;

	@Autowired
	private SearchRestrictionService searchRestrictionService;

	@Autowired
	EyDmsNotificationService eydmsNotificationService;

	@Autowired
	TerritoryManagementService territoryManagementService;


	@Resource
	DealerDao dealerDao;

	@Autowired
	DataConstraintDao dataConstraintDao;

	@Autowired
	InfluencerDao influencerDao;

	private static final String OUTBOUND_PRICE_OBJECT = "EyDmsOutboundPrice";
	private static final String OUTBOUND_PRICE_DESTINATION = "eydmsScpiPriceDestination";


	@Autowired
	private Populator<AddressData, AddressModel> addressReversePopulator;

	@Autowired
	CategoryService categoryService;

	public List<MarketMappingDetailsModel> getAllMarketMappingDetails() {

		return slctCrmIntegrationDao.getAllMarketMappingDetails();

	}

	@Override
	public List<VisitMasterModel> getAllVisit() {

		return slctCrmIntegrationDao.getAllVisit();
	}

	@Override
	public List<CounterVisitMasterModel> getAllCounterVisit() {

		return slctCrmIntegrationDao.getAllCounterVisit();
	}

	@Override
	public List<EyDmsCustomerModel> getAllInfluencerDetails() {
		return slctCrmIntegrationDao.getAllInfluencerDetails();
	}

	@Override
	public List<List<Object>> getAllRetailerSalesInfo() {
		return slctCrmIntegrationDao.getAllRetailerSalesInfo();
	}

	@Override
	public List<DestinationSourceMasterModel> getLpSourceMasterList(BaseSiteModel brand, DeliveryModeModel deliveryMode, String grade, String packaging, String destCityId) {
		return slctCrmIntegrationDao.getLpSourceMasterList(brand, deliveryMode, grade, packaging, destCityId);
	}

	@Override
	public OrderType getTheOrderType(String order) {
		if (order != null && !order.isEmpty()) {
			final Map<String, Object> params = new HashMap<String, Object>();
			final StringBuilder builder = new StringBuilder("select {pk} from {OrderType} where {code}=?code");

			params.put("code", order);

			final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
			query.setResultClassList(Collections.singletonList(OrderType.class));
			query.addQueryParameters(params);
			final SearchResult<OrderType> searchResult = flexibleSearchService.search(query);
			if (searchResult.getResult() != null && !searchResult.getResult().isEmpty()) {
				return searchResult.getResult().get(0);
			}
		}
		return null;

	}

	@Override
	public CustomerCategory getTheCustType(String customer) {
		if (customer != null && !customer.isEmpty()) {
			final Map<String, Object> params = new HashMap<String, Object>();
			final StringBuilder builder = new StringBuilder("select {pk} from {CustomerCategory} where {code}=?code");

			params.put("code", customer);

			final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
			query.setResultClassList(Collections.singletonList(CustomerCategory.class));
			query.addQueryParameters(params);
			final SearchResult<CustomerCategory> searchResult = flexibleSearchService.search(query);
			if (searchResult.getResult() != null && !searchResult.getResult().isEmpty()) {
				return searchResult.getResult().get(0);
			}
		}

		return null;

	}

	@Override
	public BaseSiteModel getTheBrand(String brand) {
		if (brand != null && !brand.isEmpty()) {
			final Map<String, Object> params = new HashMap<String, Object>();
			final StringBuilder builder = new StringBuilder("select {pk} from {BaseSite} where {uid}=?uid");

			params.put("uid", brand);

			final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
			query.setResultClassList(Collections.singletonList(BaseSiteModel.class));
			query.addQueryParameters(params);
			final SearchResult<BaseSiteModel> searchResult = flexibleSearchService.search(query);
			if (searchResult.getResult() != null && !searchResult.getResult().isEmpty()) {
				return searchResult.getResult().get(0);
			}
		}
		return null;
	}

	@Override
	public DeliveryModeModel getTheDeliveryMode(String delivery) {
		if (delivery != null && !delivery.isEmpty()) {
			final Map<String, Object> params = new HashMap<String, Object>();
			final StringBuilder builder = new StringBuilder("select {pk} from {DeliveryMode} where {code}=?code");

			params.put("code", delivery);

			final FlexibleSearchQuery query = new FlexibleSearchQuery(builder.toString());
			query.setResultClassList(Collections.singletonList(DeliveryModeModel.class));
			query.addQueryParameters(params);
			final SearchResult<DeliveryModeModel> searchResult = flexibleSearchService.search(query);
			if (searchResult.getResult() != null && !searchResult.getResult().isEmpty()) {
				return searchResult.getResult().get(0);
			}
		}
		return null;
	}

	public CreditAndOutstandingModel getCrmOutstandingDetails(String custCode) {
		CreditAndOutstandingModel outstandingModel = slctCrmIntegrationDao.getCrmOutstandingDetails(custCode);
		return outstandingModel;
	}

	@Override
	public boolean updateRunMasterDetails(SlctRunMasterListData slctRunMasterListData) {
		List<SlctRunMasterData> runMasterDetails = slctRunMasterListData.getRunMasterDetails();
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		List<DJPRunMasterModel> modelList = new ArrayList<DJPRunMasterModel>();
		for (SlctRunMasterData runMasterData : runMasterDetails) {
			DJPRunMasterModel runMasterModel = modelService.create(DJPRunMasterModel.class);
			runMasterModel.setId(runMasterData.getId());
			runMasterModel.setBrand(runMasterData.getBrand());
			runMasterModel.setTaluka(runMasterData.getTaluka());
			runMasterModel.setDistrict(runMasterData.getDistrict());
			runMasterModel.setState(runMasterData.getState());
			if (runMasterData.getPlanDate() != null) {
				try {
					runMasterModel.setPlanDate(dateFormat.parse(runMasterData.getPlanDate()));
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
			}

			runMasterModel.setRecommendedRoute1(slctCrmIntegrationDao.getRouteMasterDetails(runMasterData.getRecommenededRoute1().getRouteId()));
			runMasterModel.setRecommendedRoute2(slctCrmIntegrationDao.getRouteMasterDetails(runMasterData.getRecommendedRoute2().getRouteId()));
			modelList.add(runMasterModel);
		}
		modelService.saveAll(modelList);
		return true;
	}

	@Override
	public boolean updateCounterScoreDetails(SlctCounterScoreListData slctCounterScoreListData) {
		List<SlctCounterScoreData> counterScoreDetails = slctCounterScoreListData.getCounterScoreDetails();
		List<DJPCounterScoreMasterModel> modelList = new ArrayList<DJPCounterScoreMasterModel>();
		for (SlctCounterScoreData counterScoreData : counterScoreDetails) {
			DJPCounterScoreMasterModel counterScoreMasterModel = modelService.create(DJPCounterScoreMasterModel.class);
			counterScoreMasterModel.setId(counterScoreData.getId());
			counterScoreMasterModel.setCounterScore(counterScoreData.getCounterScore());
			counterScoreMasterModel.setVisitSequence(counterScoreData.getVisitSequence());
			counterScoreMasterModel.setRouteScore(slctCrmIntegrationDao.getRouteScoreDetails(counterScoreData.getRouteScore().getId()));
			counterScoreMasterModel.setObjective(slctCrmIntegrationDao.getObjectiveDetails(counterScoreData.getObjective().getObjectiveId()));
			counterScoreMasterModel.setCustomer(slctCrmIntegrationDao.getCustomerDetails(counterScoreData.getCustomer().getUid()));
			modelList.add(counterScoreMasterModel);

		}
		modelService.saveAll(modelList);
		return true;
	}

	@Override
	public boolean updateRouteScoreDetails(SlctRouteScoreListData slctRouteScoreListData) {
		List<SlctRouteScoreData> routeScoreDetails = slctRouteScoreListData.getRouteScoreDetails();
		List<DJPRouteScoreMasterModel> modelList = new ArrayList<DJPRouteScoreMasterModel>();
		for (SlctRouteScoreData routeScoreData : routeScoreDetails) {
			DJPRouteScoreMasterModel routeScoreMasterModel = modelService.create(DJPRouteScoreMasterModel.class);
			routeScoreMasterModel.setId(routeScoreData.getId());
			routeScoreMasterModel.setRoutesScore(routeScoreData.getRouteScore());
			routeScoreMasterModel.setRoute(slctCrmIntegrationDao.getRouteMasterDetails(routeScoreData.getRoute().getRouteId()));
			routeScoreMasterModel.setRun(slctCrmIntegrationDao.getRunMasterDetails(routeScoreData.getRun().getId()));
			routeScoreMasterModel.setRecommendedObj1(routeScoreData.getRecommendedObj1());
			routeScoreMasterModel.setRecommendedObj2(routeScoreData.getRecommendedObj2());
			modelList.add(routeScoreMasterModel);
		}
		modelService.saveAll(modelList);
		return true;
	}

	@Override
	public boolean insertNcrDetails(SlctNcrListData slctNcrListData) {
		List<SlctNcrData> ncrDetails = slctNcrListData.getNcrDetails();
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		List<SalesHistoryModel> modelList = new ArrayList<SalesHistoryModel>();
		for (SlctNcrData ncrData : ncrDetails) {
			if (ncrData.getCustomerTransactionId() != null && !ncrData.getCustomerTransactionId().isEmpty() && ncrData.getCustomerTransactionLineId() != null && !ncrData.getCustomerTransactionLineId().isEmpty() && ncrData.getBrand() != null) {
				CMSSiteModel brand = (CMSSiteModel) baseSiteService.getBaseSiteForUID(ncrData.getBrand().getUid());
				SalesHistoryModel salesHistoryModel = slctCrmIntegrationDao.getSalesHistoryModel(ncrData.getCustomerTransactionId(), ncrData.getCustomerTransactionLineId(), brand);
				if (Objects.isNull(salesHistoryModel)) {
					salesHistoryModel = modelService.create(SalesHistoryModel.class);
					salesHistoryModel.setCustomerTransactionId(ncrData.getCustomerTransactionId());
					salesHistoryModel.setCustomerTransactionLineId(ncrData.getCustomerTransactionLineId());
					if ((CMSSiteModel) baseSiteService.getBaseSiteForUID(ncrData.getBrand().getUid()) != null) {
						salesHistoryModel.setBrand((CMSSiteModel) baseSiteService.getBaseSiteForUID(ncrData.getBrand().getUid()));
					}
					salesHistoryModel.setIsCreatedFromSlctIntegration(true);
				}

				salesHistoryModel.setInvoiceNo(ncrData.getInvoiceNo());
				if (ncrData.getInvoiceDate() != null && !ncrData.getInvoiceDate().isEmpty()) {
					try {
						salesHistoryModel.setInvoiceDate(dateFormat.parse(ncrData.getInvoiceDate()));
					} catch (ParseException e) {
						throw new RuntimeException(e);
					}
				}
				if (ncrData.getSalesOrderDate() != null && !ncrData.getSalesOrderDate().isEmpty()) {
					try {
						salesHistoryModel.setSalesOrderDate(dateFormat.parse(ncrData.getSalesOrderDate()));
					} catch (ParseException e) {
						throw new RuntimeException(e);
					}
				}
				salesHistoryModel.setCustomerNo(ncrData.getCustomerNo());
				salesHistoryModel.setSalesOrderNo(ncrData.getSalesOrderNo());
				salesHistoryModel.setLineId(ncrData.getLineId());
				salesHistoryModel.setInventoryItemId(ncrData.getInventoryItemId());
				salesHistoryModel.setProduct(ncrData.getProduct());
				salesHistoryModel.setQuantity(ncrData.getQuantityInvoiced());
				salesHistoryModel.setInvoicedUOMCode(ncrData.getInvoicedUOMCode());
				salesHistoryModel.setPackagingCondition(ncrData.getPackagingCondition());
				salesHistoryModel.setMode(ncrData.getMode());
				salesHistoryModel.setDeliveryId(ncrData.getDeliveryId());
				salesHistoryModel.setCity(ncrData.getCity());
				salesHistoryModel.setDistrict(ncrData.getDistrict());
				salesHistoryModel.setState(ncrData.getState());
				salesHistoryModel.setNcr(ncrData.getNcr());
				salesHistoryModel.setTaluka(ncrData.getTaluka());

				salesHistoryModel.setCustomerCategory(CustomerCategory.valueOf(ncrData.getCustomerCategory().getCode()));
				salesHistoryModel.setWarehouseType(WarehouseType.valueOf(ncrData.getWarehouseType().getCode()));
				if (ncrData.getWarehouse() != null && ncrData.getWarehouse().getCode() != null && !ncrData.getWarehouse().getCode().isEmpty()) {
					WarehouseModel warehouseModel = slctCrmIntegrationDao.findWarehouseByCode(ncrData.getWarehouse().getCode());
					if (!Objects.isNull(warehouseModel)) {
						salesHistoryModel.setWarehouse(warehouseModel);
					} else {
						warehouseModel = modelService.create(WarehouseModel.class);
						warehouseModel.setCode(ncrData.getWarehouse().getCode());
						warehouseModel.setName(ncrData.getWarehouse().getCode());
						VendorModel vendorModel = slctCrmIntegrationDao.findVendorByCode("default");
						warehouseModel.setVendor(vendorModel);
						if (ncrData.getWarehouseType().getCode().startsWith("F")) {
							warehouseModel.setType(WarehouseType.PLANT);
						} else {
							warehouseModel.setType(WarehouseType.DEPOT);
						}
						warehouseModel.setDefault(true);
						warehouseModel.setActive(true);
						warehouseModel.setIsCreatedFromSlctIntegration(true);
						modelService.save(warehouseModel);
						salesHistoryModel.setWarehouse(warehouseModel);
					}

				}
//				if(warehouseService.getWarehouseForCode(ncrData.getWarehouse().getCode()) != null) {
//					salesHistoryModel.setWarehouse(warehouseService.getWarehouseForCode(ncrData.getWarehouse().getCode()));
//				}
				if (ncrData.getBillingPrice() != null) {
					salesHistoryModel.setBillingPrice(ncrData.getBillingPrice());
				}
				if (ncrData.getUnitSellingPrice() != null) {
					salesHistoryModel.setUnitSellingPrice(ncrData.getUnitSellingPrice());
				}
				salesHistoryModel.setOrderType(ncrData.getOrderType());
				salesHistoryModel.setFobCode(ncrData.getFobCode());
				salesHistoryModel.setFreightTerms(ncrData.getFreightTerm());
				salesHistoryModel.setIsModifiedFromSlctIntegration(true);

				modelList.add(salesHistoryModel);
			}

		}
		modelService.saveAll(modelList);
		return true;
	}

	@Override
	public boolean insertMitraTransactionDetails(SlctMitraTransactionListData slctMitraTransactionListData) {
		List<SlctMitraTransactionData> mitraTransactionDetails = slctMitraTransactionListData.getMitraTransactions();
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		List<NirmanMitraSalesHistoryModel> modelList = new ArrayList<NirmanMitraSalesHistoryModel>();
		for (SlctMitraTransactionData mitraTransactionData : mitraTransactionDetails) {
			NirmanMitraSalesHistoryModel mitraSalesHistoryModel = modelService.create(NirmanMitraSalesHistoryModel.class);

			mitraSalesHistoryModel.setTransactionId(mitraTransactionData.getTransactionId());
			mitraSalesHistoryModel.setFromPartyId(mitraTransactionData.getFromPartyId());
			mitraSalesHistoryModel.setFromCustAccNumber(mitraTransactionData.getFromCustAccNumber());
			mitraSalesHistoryModel.setFromPartyName(mitraTransactionData.getFromPartyName());
			mitraSalesHistoryModel.setToPartyId(mitraTransactionData.getToPartyId());
			mitraSalesHistoryModel.setToCustAccNumber(mitraTransactionData.getToCustAccNumber());
			mitraSalesHistoryModel.setToPartyName(mitraTransactionData.getToPartyName());
			mitraSalesHistoryModel.setTransactionType(mitraTransactionData.getTransactionType());
			mitraSalesHistoryModel.setTransactionTypeDisp(mitraTransactionData.getTransactionTypeDisp());
			if (mitraTransactionData.getTransactionDate() != null) {
				try {
					mitraSalesHistoryModel.setTransactionDate(dateFormat.parse(mitraTransactionData.getTransactionDate()));
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
			}
			mitraSalesHistoryModel.setTransactionQuantity(mitraTransactionData.getTransactionQuantity());
			mitraSalesHistoryModel.setInventoryItemId(mitraTransactionData.getInventoryItemId());
			mitraSalesHistoryModel.setInventoryItemName(mitraTransactionData.getInventoryItemName());
			mitraSalesHistoryModel.setPackingType(mitraTransactionData.getPackingType());
			mitraSalesHistoryModel.setBrand(baseSiteService.getBaseSiteForUID(mitraTransactionData.getBrand().getUid()));
			if (mitraTransactionData.getCreationDate() != null && !mitraTransactionData.getCreationDate().isEmpty()) {
				try {
					mitraSalesHistoryModel.setCreationDate(dateFormat.parse(mitraTransactionData.getCreationDate()));
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
			}
			mitraSalesHistoryModel.setState(mitraTransactionData.getState());
			mitraSalesHistoryModel.setDistrict(mitraTransactionData.getDistrict());
			mitraSalesHistoryModel.setTaluka(mitraTransactionData.getTaluka());
			mitraSalesHistoryModel.setCity(mitraTransactionData.getCity());
			mitraSalesHistoryModel.setCityId(mitraTransactionData.getCityId());
			modelList.add(mitraSalesHistoryModel);
		}
		modelService.saveAll(modelList);
		return true;
	}

	@Override
	public boolean insertUpdateMitraMasterDetails(SlctMitraMasterListData slctMitraMasterListData) {
		List<SlctMitraMasterData> mitraMasterDetails = slctMitraMasterListData.getMitraMasterDetails();
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		for (SlctMitraMasterData mitraMasterData : mitraMasterDetails) {
			EyDmsCustomerModel mitraMasterModel = slctCrmIntegrationDao.getMitraMasterDetails(mitraMasterData.getCustomerID());
			if (Objects.isNull(mitraMasterModel)) {
				mitraMasterModel = modelService.create(EyDmsCustomerModel.class);
				//Generate CRM code (uid) and populate it
				mitraMasterModel.setUid(customCodeGenerator.generate().toString());
				mitraMasterModel.setIsCreatedFromSlctIntegration(true);
			}
			mitraMasterModel.setCustomerID(mitraMasterData.getCustomerID());
			mitraMasterModel.setNirmanMitraCode(mitraMasterData.getNirmanMitraCode());
			mitraMasterModel.setName(mitraMasterData.getName());
			if (mitraMasterData.getDateOfJoining() != null && !mitraMasterData.getDateOfJoining().isEmpty()) {
				try {
					mitraMasterModel.setDateOfJoining(dateFormat.parse(mitraMasterData.getDateOfJoining()));
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
			}
			mitraMasterModel.setInfluencerType(InfluencerType.valueOf(mitraMasterData.getInfluencerType()));
			mitraMasterModel.setMobileNumber(mitraMasterData.getMobileNumber());
			mitraMasterModel.setEmail(mitraMasterData.getEmail());
			mitraMasterModel.setIsModifiedFromSlctIntegration(true);
			mitraMasterModel.setDefaultB2BUnit(defaultB2BUnitService.getUnitForUid(mitraMasterData.getOrg_id()));

			AddressModel addressModel = modelService.create(AddressModel.class);
			addressModel.setStreetname(mitraMasterData.getStreetName());
			addressModel.setStreetnumber(mitraMasterData.getStreetNumber());
			addressModel.setErpCity(mitraMasterData.getErpCity());
			addressModel.setTaluka(mitraMasterData.getTaluka());
			addressModel.setDistrict(mitraMasterData.getDistrict());
			addressModel.setState(mitraMasterData.getState());
			addressModel.setPostalcode(mitraMasterData.getPostalCode());
			addressModel.setOwner(mitraMasterModel);
			modelService.save(addressModel);
			modelService.save(mitraMasterModel);
			customerAccountService.saveAddressEntry(mitraMasterModel, addressModel);
			//			mitraMasterModel.getAddresses().add(addressModel);
			//			mitraMasterModel.setAddresses(mitraMasterModel.getAddresses());
			//			modelService.save(mitraMasterModel);
		}
		return true;
	}

	@Override
	public List<OrderEntryModel> getOrderLineScheduleDetails() {
		return slctCrmIntegrationDao.getOrderLineScheduleDetails();
	}

	@Override
	public boolean updateOrderFromSlct(IntegrationOrderData orderData) {
		try {
			int updatedEntry = 0;
			StringBuilder errorMessage = new StringBuilder();
			if (orderData.getOrderEntries() != null && !orderData.getOrderEntries().isEmpty()) {
				List<IntegrationOrderEntryData> entryList = orderData.getOrderEntries();

				entryList.sort(Comparator.comparing(IntegrationOrderEntryData::getEventDate));

				Map<String, IntegrationOrderEntryData> map = new HashMap<String, IntegrationOrderEntryData>();
				entryList.forEach(data -> {
					map.put(data.getErpLineItemId(), data);
				});
				Collection<IntegrationOrderEntryData> entrySet = map.values();
				/*entrySet.forEach(data->{
					if(data.getParentId()!=null && !data.getParentId().isBlank()) {
						data.setParentLineId(Integer.valueOf(data.getErpLineItemId()));
					}
					else {
						data.setParentLineId(0);
					}
				});*/
				entrySet = entrySet.stream().sorted(Comparator.comparing(IntegrationOrderEntryData::getEntryNumber)).collect(Collectors.toList());

				OrderModel order = b2bOrderService.getOrderForCode(orderData.getCode());
				if (order == null) {
					errorMessage.append("Order not found in CRM " + orderData.getCode() + ", ");
				}
				Map<String, WarehouseModel> mapWarehouse = new HashMap<>();
				for (IntegrationOrderEntryData data : entrySet) {
					if (data.getWarehouseName() != null) {
						WarehouseModel source = warehouseService.getWarehouseForCode(data.getWarehouseName().split(":")[0]);
						if (source == null) {
							errorMessage.append(String.format("Source not found in crm for line item %s, ", data.getErpLineItemId()));
						} else {
							if (source.getType() != null) {
								mapWarehouse.put(data.getErpLineItemId(), source);
							} else {
								errorMessage.append(String.format("Source Type not found in crm for line item %s, ", data.getErpLineItemId()));
							}
						}
					} else {
						errorMessage.append(String.format("Source name missing for line item %s, ", data.getErpLineItemId()));
					}
				}

				String errorMsg = errorMessage.toString();
				if (StringUtils.isNotBlank(errorMsg)) {
					throw new UnknownIdentifierException(errorMsg);
				}
				for (IntegrationOrderEntryData data : entrySet) {

					Optional<AbstractOrderEntryModel> entryOptional = order.getEntries().stream().filter(entry -> entry.getErpLineItemId() != null && entry.getErpLineItemId().equals(data.getErpLineItemId())).findAny();
					OrderEntryModel entryModel = null;
					Boolean isOrderDelivered = false;
					if (entryOptional.isPresent()) {

						entryModel = (OrderEntryModel) entryOptional.get();
						double basePrice = entryModel.getBasePrice() != null ? entryModel.getBasePrice() : 0.0;
						populateOrderEntryModel(entryModel, data, basePrice, mapWarehouse);

					} else {

						Optional<AbstractOrderEntryModel> parentOptional = order.getEntries().stream().filter(entry -> data.getParentId() != null && data.getParentId().equals(entry.getErpLineItemId())).findAny();
						if (parentOptional.isPresent()) {

							AbstractOrderEntryModel parent = parentOptional.get();

							entryModel = modelService.create(OrderEntryModel.class);
							entryModel.setOrder(order);
							entryModel.setProduct(parent.getProduct());
							entryModel.setUnit(parent.getUnit());
							entryModel.setTruckNo(parent.getTruckNo());
							entryModel.setDriverContactNo(parent.getDriverContactNo());
							entryModel.setExpectedDeliveryslot(parent.getExpectedDeliveryslot());
							entryModel.setExpectedDeliveryDate(parent.getExpectedDeliveryDate());
							entryModel.setCalculatedDeliveryDate(parent.getCalculatedDeliveryDate());
							entryModel.setCalculatedDeliveryslot(parent.getCalculatedDeliveryslot());
							entryModel.setRemarks(parent.getRemarks());
							entryModel.setEntryNumber(data.getEntryNumber());
							entryModel.setSequence(data.getEntryNumber() + 1);
							entryModel.setErpLineItemId(data.getErpLineItemId());
							double basePrice = parent.getBasePrice() != null ? parent.getBasePrice() : 0.0;
							entryModel.setBasePrice(basePrice);
							isOrderDelivered = populateOrderEntryModel(entryModel, data, basePrice, mapWarehouse);
						}
					}
					if (entryModel != null) {
						modelService.save(entryModel);
						modelService.refresh(order);
						updatedEntry++;

						if (isOrderDelivered) {
							eydmsB2BOrderService.saveOrderRequisitionEntryDetails(entryModel.getOrder(), entryModel, "EPOD");
						}
					}
				}

				order.setTotalPrice(order.getEntries().stream().collect(Collectors.summingDouble(AbstractOrderEntryModel::getTotalPrice)));
				modelService.save(order);
				if (updatedEntry != entrySet.size()) {
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getName() + " Occurred";
			throw new UnknownIdentifierException(errorMsg);
		}
	}

	private Boolean populateOrderEntryModel(OrderEntryModel entryModel, IntegrationOrderEntryData data, Double basePrice, Map<String, WarehouseModel> mapWarehouse) {
		boolean isDelivered = false;
		boolean isInvoiced = false;
		boolean isTruckDispatched = false;
		boolean isTruckAllocated = false;
		if (data.getInvoiceCreationDate() != null) {
			if (data.getInvoiceCreationDate() != null && entryModel.getInvoiceCreationDateAndTime() == null) {
				isInvoiced = true;
			}
//			entryModel.setInvoiceCreationDateAndTime(data.getInvoiceCreationDate());
		}
		if (data.getTruckDispatchDate() != null && entryModel.getTruckDispatcheddate() == null) {
			isTruckDispatched = true;
		}
		if (data.getTruckAllocationDate() != null && entryModel.getTruckAllocatedDate() == null) {
			isTruckAllocated = true;
		}
		entryModel.setQuantityInMT(data.getQuantityMT());
		entryModel.setQuantity((long) (data.getQuantityMT() * 1000));
		entryModel.setTotalPrice(entryModel.getQuantityInMT() * basePrice);
		entryModel.setTruckAllocatedDate(data.getTruckAllocationDate());
		entryModel.setDiCreationDateAndTime(data.getDiCreationDate());
//		entryModel.setInvoiceCreationDateAndTime(data.getInvoiceCreationDate());
		entryModel.setTruckDispatcheddate(data.getTruckDispatchDate());
		entryModel.setTransporterName(data.getTransporterName());
		entryModel.setTransporterPhoneNumber(data.getTransporterNumber());
		entryModel.setErpTruckNumber(data.getErpTruckNumber());
		entryModel.setErpDriverNumber(data.getErpDriverNumber());
		entryModel.setInvoiceNumber(data.getInvoiceNumber());
		entryModel.setDiNumber(data.getDeliveryId());
		entryModel.setTokenNumber(data.getTokenNumber());
		entryModel.setEventDate(data.getEventDate());
		entryModel.setParentId(data.getParentId());
		entryModel.setConsigneeId(data.getConsigneeId());
		entryModel.setCarrierId(data.getCarrierId());
		entryModel.setCancelledDate(data.getCancelledDate());
		entryModel.setInvoiceCancelDate(data.getInvoiceCancelDate());
		entryModel.setInvoiceCancelQuantity(data.getInvoiceCancelQuantity());
		entryModel.setInvoiceQuantity(data.getInvoiceQuantity());
		entryModel.setTruckAllocatedQty(data.getTruckAllocatedQty());
		entryModel.setDeliveryQty(data.getDeliveryQty());
		entryModel.setRouteId(data.getRouteId());
		if (data.getWarehouseCode() != null) {
			WarehouseModel oldWarehouse = entryModel.getSource();
			WarehouseModel newWarehouse = mapWarehouse.get(data.getErpLineItemId());//warehouseService.getWarehouseForCode(data.getWarehouseName().split(":")[0]);
			entryModel.setSource(newWarehouse);

			if (entryModel.getProduct() != null && entryModel.getOrder().getDeliveryMode() != null && entryModel.getOrder().getDeliveryAddress() != null && entryModel.getOrder().getSite() != null) {
				DestinationSourceMasterModel destinationSource = destinationSourceMasterDao.getDestinationSourceBySource(OrderType.SO, CustomerCategory.TR, entryModel.getSource(), entryModel.getOrder().getDeliveryMode(), entryModel.getOrder().getDeliveryAddress().getErpCity(), entryModel.getOrder().getDeliveryAddress().getDistrict(), entryModel.getOrder().getDeliveryAddress().getState(), entryModel.getOrder().getSite(),
						entryModel.getProduct().getGrade(), entryModel.getProduct().getBagType(), entryModel.getOrder().getDeliveryAddress().getTaluka());
				if (destinationSource != null && destinationSource.getDistance() != null) {
					entryModel.setDistance(destinationSource.getDistance().doubleValue());
				}
			}

			if (oldWarehouse != null && newWarehouse != null && !oldWarehouse.getCode().equals(newWarehouse.getCode())) {
				OrderModel order = entryModel.getOrder();
				List<FreightAndIncoTermsMasterModel> freightAndIncoTerms = findFreightAndIncoTerms(order.getDeliveryAddress().getState(), order.getDeliveryAddress().getDistrict(),
						order.getSite(), entryModel.getSource().getType().getCode());
				if (freightAndIncoTerms != null && !freightAndIncoTerms.isEmpty()) {
					for (FreightAndIncoTermsMasterModel f : freightAndIncoTerms) {
						entryModel.setFob(IncoTerms.valueOf(f.getIncoTerms()));
						entryModel.setFreightTerms(FreightTerms.valueOf(f.getFrieghtTerms()));
						if (entryModel.getFob() != null && entryModel.getFob().equals(IncoTerms.EX))
							entryModel.setEpodCompleted(true);
						break;
					}
				}
			}
		}
		EyDmsCustomerModel customer = (EyDmsCustomerModel) entryModel.getOrder().getUser();
		entryModel.setStatus(OrderStatus.ORDER_ACCEPTED);
		if (data.getCancelledDate() != null) {
			entryModel.setStatus(OrderStatus.CANCELLED);
			eydmsB2BOrderService.saveOrderRequisitionEntryDetails(entryModel.getOrder(), entryModel, "LINE_CANCELLED");
			eydmsB2BOrderService.getRequisitionStatusByOrderLines(entryModel.getOrder());

			try {

				StringBuilder builder = new StringBuilder();
				Double amount = entryModel.getTotalPrice();
				String formattedAmount = formatIndianNumber(amount);

				builder.append("Order no. " + entryModel.getOrder().getCode() + "/" + entryModel.getEntryNumber() + " of product " + entryModel.getProduct().getName() + " of quantity " + entryModel.getQuantityInMT() + " MT of ₹" + formattedAmount);
				builder.append(" has been successfully cancelled for " + entryModel.getOrder().getUser().getUid());

				String body = builder.toString();

				StringBuilder builder1 = new StringBuilder("Order Line is Cancelled");

				String subject = builder1.toString();

				NotificationCategory category = NotificationCategory.ORDER_CANCELLED_ERP;
				eydmsNotificationService.submitOrderNotification(entryModel.getOrder(), (B2BCustomerModel) entryModel.getOrder().getUser(), body, subject, category);

				StringBuilder builder2 = new StringBuilder();
				EyDmsUserModel so = territoryService.getSOforCustomer((EyDmsCustomerModel) entryModel.getOrder().getUser());
				builder2.append("Order no. " + entryModel.getOrder().getCode() + "/" + entryModel.getEntryNumber() + " of product " + entryModel.getProduct().getName() + " of quantity " + entryModel.getQuantityInMT() + " MT of ₹" + formattedAmount);
				builder2.append(" has been successfully cancelled for " + so.getUid());

				String body2 = builder2.toString();
				eydmsNotificationService.submitOrderNotification(entryModel.getOrder(), so, body2, subject, category);

				StringBuilder builder3 = new StringBuilder();
				EyDmsCustomerModel sp = territoryService.getSpForCustomerAndBrand((EyDmsCustomerModel) entryModel.getOrder().getUser(), entryModel.getOrder().getSite());
				builder3.append("Order no. " + entryModel.getOrder().getCode() + "/" + entryModel.getEntryNumber() + " of product " + entryModel.getProduct().getName() + " of quantity " + entryModel.getQuantityInMT() + " MT of ₹" + formattedAmount);
				builder3.append(" has been successfully cancelled for " + sp.getUid());

				String body3 = builder3.toString();
				eydmsNotificationService.submitOrderNotification(entryModel.getOrder(), sp, body3, subject, category);
			} catch (Exception e) {
				LOG.error("Error while sending cancel order entry erp notification");
			}
		} else if (data.getInvoiceCancelDate() != null) {
			entryModel.setStatus(OrderStatus.INVOICE_CANCELLED);
			//when order is cancelled then receipt allocation will be reduced
			Double invoicedCancelledQuantity = 0.0;
			if (null != entryModel.getInvoiceCancelQuantity()) {
				invoicedCancelledQuantity = entryModel.getInvoiceCancelQuantity() * EyDmsCoreConstants.QUANTITY_INMT_TO_BAGS;
			}
			updateCancelledReceipts(entryModel.getProduct(), customer, invoicedCancelledQuantity);
		} else if (data.getTruckDispatchDate() != null && data.getInvoiceCancelDate() == null && data.getCancelledDate() == null) {
			if (entryModel.getSource() != null && entryModel.getSource().getType().equals(WarehouseType.PLANT) && entryModel.getFob() != null && entryModel.getFob().equals(IncoTerms.EX)) {
				entryModel.setStatus(OrderStatus.DELIVERED);
				isDelivered = true;
				entryModel.setDeliveredDate(data.getTruckDispatchDate());

			} else {
				if (entryModel.getTruckReachedDate() != null) {
					entryModel.setStatus(OrderStatus.TRUCK_REACHED_DESTINATION);
					try {
						StringBuilder builder = new StringBuilder();
						builder.append("Order no. " + entryModel.getOrder().getCode() + " with product " + entryModel.getProduct().getName() + " and quantity " + entryModel.getQuantityInMT());
						builder.append(" has reached the delivery location with Truck No. " + entryModel.getTruckNo() + ". Please proceed with unloading of the vehicle.");

						String body = builder.toString();
						StringBuilder builder1 = new StringBuilder();
						builder1.append("Vehicle Arrival Confirmation");

						String subject = builder1.toString();

						NotificationCategory category = NotificationCategory.VEHICLE_ARRIVAL_CONFIRMATION;
						eydmsNotificationService.submitOrderEntryNotification(entryModel.getOrder(), (B2BCustomerModel) entryModel.getOrder().getUser(), body, subject, category, entryModel.getEntryNumber());

						EyDmsUserModel so = territoryService.getSOforCustomer((EyDmsCustomerModel) entryModel.getOrder().getUser());
						eydmsNotificationService.submitOrderEntryNotification(entryModel.getOrder(), so, body, subject, category, entryModel.getEntryNumber());

						EyDmsCustomerModel sp = territoryService.getSpForCustomerAndBrand((EyDmsCustomerModel) entryModel.getOrder().getUser(), entryModel.getOrder().getSite());
						eydmsNotificationService.submitOrderEntryNotification(entryModel.getOrder(), sp, body, subject, category, entryModel.getEntryNumber());

					} catch (Exception e) {
						LOG.error("Error while sending Vehicle Arrival Confirmation Notification");
					}
				} else {
					entryModel.setStatus(OrderStatus.TRUCK_DISPATCHED);
				}
			}

		} else if (data.getInvoiceQuantity() != null && data.getInvoiceQuantity() > 0 && data.getTruckDispatchDate() == null && data.getInvoiceCancelDate() == null && data.getCancelledDate() == null) {
			if (entryModel.getSource() != null && entryModel.getSource().getType().equals(WarehouseType.DEPOT)) {
				entryModel.setTruckAllocatedDate(data.getInvoiceCreationDate());
				entryModel.setTruckDispatcheddate(data.getInvoiceCreationDate());
				entryModel.setStatus(OrderStatus.TRUCK_DISPATCHED);
				if (entryModel.getFob() != null && entryModel.getFob().equals(IncoTerms.EX)) {
					entryModel.setDeliveredDate(data.getInvoiceCreationDate());
					entryModel.setStatus(OrderStatus.DELIVERED);
					isDelivered = true;
				}
			} else {
				entryModel.setStatus(OrderStatus.INVOICED);
			}

		} else if (data.getTruckAllocatedQty() != null && data.getTruckAllocatedQty() > 0 && (data.getInvoiceQuantity() == null || data.getInvoiceQuantity() == 0) && data.getTruckDispatchDate() == null && data.getInvoiceCancelDate() == null && data.getCancelledDate() == null) {
			entryModel.setStatus(OrderStatus.TRUCK_ALLOCATED);

		} else if (data.getDeliveryQty() != null && data.getDeliveryQty() > 0 && (data.getInvoiceQuantity() == null || data.getInvoiceQuantity() == 0) && (data.getTruckAllocatedQty() == null || data.getTruckAllocatedQty() == 0) && data.getTruckDispatchDate() == null && data.getInvoiceCancelDate() == null && data.getCancelledDate() == null) {
			entryModel.setStatus(OrderStatus.DI_CREATED);
		}

		if (isInvoiced) {
			EyDmsCustomerModel cus = (EyDmsCustomerModel) entryModel.getOrder().getUser();
			Date existingLastLiftingDate = cus.getLastLiftingDate();
			if (existingLastLiftingDate != null) {
				if (existingLastLiftingDate.compareTo(data.getInvoiceCreationDate()) < 0) {
					cus.setLastLiftingDate(data.getInvoiceCreationDate());  // for networkType calculation
					cus.setNetworkType(NetworkType.ACTIVE.getCode());
					modelService.save(cus);
					modelService.refresh(cus);
				}
			} else {
				cus.setLastLiftingDate(data.getInvoiceCreationDate());  // for networkType calculation
				cus.setNetworkType(NetworkType.ACTIVE.getCode());
				modelService.save(cus);
				modelService.refresh(cus);
			}

			/*Double receiptQty = 0.0;
			if (null != entryModel.getInvoiceQuantity()) {
				receiptQty = entryModel.getInvoiceQuantity() * EyDmsCoreConstants.QUANTITY_INMT_TO_BAGS;
			}
			OrderModel orderRequistionCheck = entryModel.getOrder();
			if (null != orderRequistionCheck && (null == orderRequistionCheck.getRequisitions()
					|| orderRequistionCheck.getRequisitions().size() != 1)) {
				LOG.info("going inside update receipt");
				updateReceipts(entryModel.getProduct(), customer, receiptQty);
			}*/
		}
		if (isTruckDispatched) {
			try {
				EyDmsUserModel so = territoryService.getSOforCustomer((EyDmsCustomerModel) entryModel.getOrder().getUser());
				EyDmsCustomerModel sp = territoryService.getSpForCustomer((EyDmsCustomerModel) entryModel.getOrder().getUser());
				StringBuilder builder = new StringBuilder();
				builder.append("Source : " + entryModel.getSource() + " and " + entryModel.getSource().getCode());
				builder.append("Delivery address : " + entryModel.getDeliveryAddress());
				builder.append("Truck dispatched on : " + entryModel.getTruckDispatcheddate());
				builder.append("Order number : " + entryModel.getOrder() + " / " + entryModel.getEntryNumber());
				builder.append("Delivery quantity : " + entryModel.getQuantityInMT());
				builder.append("For Dealer : " + entryModel.getOrder().getUser() + " and " + entryModel.getOrder().getCode());
				builder.append("Truck no : " + entryModel.getTruckNo());
				builder.append("Driver contact no : " + entryModel.getDriverContactNo());
				String body = builder.toString();
				String sub = "Order has been dispatched from the plant";
//				eydmsNotificationService.submitOrderEntryNotification(entryModel.getOrder(),(B2BCustomerModel) entryModel.getOrder().getUser(),body,sub,NotificationCategory.TRUCK_DISPATCHED_CONFIRMATION,entryModel.getEntryNumber());
//				eydmsNotificationService.submitOrderEntryNotification(entryModel.getOrder(),so,body,sub,NotificationCategory.TRUCK_DISPATCHED_CONFIRMATION,entryModel.getEntryNumber());
//				eydmsNotificationService.submitOrderEntryNotification(entryModel.getOrder(),sp,body,sub,NotificationCategory.TRUCK_DISPATCHED_CONFIRMATION,entryModel.getEntryNumber());
			} catch (Exception e) {
				LOG.error("Error while sending Order dispatched notification");
			}
		}
		if (isTruckAllocated) {
			try {
				EyDmsUserModel so = territoryService.getSOforCustomer((EyDmsCustomerModel) entryModel.getOrder().getUser());
				StringBuilder builder = new StringBuilder();
				builder.append("Truck number: " + entryModel.getTruckNo());
				builder.append("Order number : " + entryModel.getOrder() + " / " + entryModel.getEntryNumber());
				builder.append("Product : " + entryModel.getProduct().getCode());
				builder.append("Quantity : " + entryModel.getQuantityInMT() + " MT");
				builder.append("Truck allocation date and time : " + entryModel.getTruckAllocatedDate() + " and " + entryModel.getTruckAllocatedDate().getTime());
				String body = builder.toString();

				String sub = "Truck has been assigned to order";
//				eydmsNotificationService.submitOrderEntryNotification(entryModel.getOrder(),(B2BCustomerModel) entryModel.getOrder().getUser(),body,sub,NotificationCategory.TRUCK_DISPATCHED_CONFIRMATION,entryModel.getEntryNumber());
//				eydmsNotificationService.submitOrderEntryNotification(entryModel.getOrder(),so,body,sub,NotificationCategory.TRUCK_DISPATCHED_CONFIRMATION,entryModel.getEntryNumber());

			} catch (Exception e) {
				LOG.error("Error while sending truck allocation notification");
			}
		}

		return isDelivered;
	}

	//To update the quantity as receipts for allocation calculation
	private void updateReceipts(ProductModel productCode, EyDmsCustomerModel dealerCode, Double invoicedQuantity) {
		LOG.info("inside update receipt");
		ReceiptAllocaltionModel receiptAllocate = dealerDao.getDealerAllocation(productCode, dealerCode);
		try {
		if (null != receiptAllocate) {
			Double updatedQty = receiptAllocate.getReceipt() + invoicedQuantity;
			LOG.info("updatedQty " + updatedQty);
			receiptAllocate.setReceipt((null != updatedQty) ? updatedQty.intValue() : 0);
			int receipt = receiptAllocate.getReceipt() != null ? receiptAllocate.getReceipt() : 0;
			int salesToRetailer = receiptAllocate.getSalesToRetailer() != null ? receiptAllocate.getSalesToRetailer() : 0;
			int salesToInfluencer = receiptAllocate.getSalesToInfluencer() != null ? receiptAllocate.getSalesToInfluencer() : 0;
			int stockRetailer = Math.abs(receipt - salesToRetailer - salesToInfluencer);
			receiptAllocate.setStockAvlForRetailer(stockRetailer);
			int stockInfluencer = Math.abs((int) ((0.7 * (receipt - salesToRetailer)) - salesToInfluencer));
			receiptAllocate.setStockAvlForInfluencer(stockInfluencer);
			modelService.save(receiptAllocate);
		} else {
			//If product and dealer is not found in the ReceiptAllocation
			//then it means new entry has to be made as order is placed with this combination
			ReceiptAllocaltionModel receiptAllocateNew = modelService.create(ReceiptAllocaltionModel.class);
			receiptAllocateNew.setProduct(productCode.getPk().toString());
			receiptAllocateNew.setDealerCode(dealerCode.getPk().toString());
			Double updatedQty = invoicedQuantity;
			receiptAllocateNew.setReceipt((null != updatedQty) ? updatedQty.intValue() : 0);
			receiptAllocateNew.setSalesToRetailer(0);
			receiptAllocateNew.setSalesToInfluencer(0);
			int stockRetailer = Math.abs(receiptAllocateNew.getReceipt() - receiptAllocateNew.getSalesToRetailer()
					- receiptAllocateNew.getSalesToInfluencer());
			receiptAllocateNew.setStockAvlForRetailer(stockRetailer);
			int stockInfluencer = Math.abs((int) ((0.7 * (receiptAllocateNew.getReceipt() - receiptAllocateNew.getSalesToRetailer()))
					- receiptAllocateNew.getSalesToInfluencer()));
			receiptAllocateNew.setStockAvlForInfluencer(stockInfluencer);
			modelService.save(receiptAllocateNew);
			modelService.refresh(receiptAllocateNew);
		}
	}
		catch (Exception e) {
			String errorMsg = e.getMessage()!=null?e.getMessage():e.getClass().getName() + "Update receipt method";
			LOG.info(errorMsg);
		}
	}

	//When order is cancelled then reduce the receipts and thereby, stocks calculation also
	private void updateCancelledReceipts(ProductModel productCode, EyDmsCustomerModel dealerCode, Double invoicedQuantity) {
		LOG.info("Order Invoiced CANCELLED... reduces the receipt and stocks for Dealer...by " + invoicedQuantity);
		ReceiptAllocaltionModel receiptAllocate = dealerDao.getDealerAllocation(productCode, dealerCode);
		if (null != receiptAllocate) {
			Double updatedQty = receiptAllocate.getReceipt() - invoicedQuantity;
			LOG.info("Order Invoiced CANCELLED... reduces the receipt to -->> " + updatedQty);
			receiptAllocate.setReceipt((null != updatedQty && updatedQty > 0.0) ? updatedQty.intValue() : 0);
			int receipt=receiptAllocate.getReceipt()!=null?receiptAllocate.getReceipt():0;
			int salesToRetailer=receiptAllocate.getSalesToRetailer()!=null?receiptAllocate.getSalesToRetailer():0;
			int salesToInfluencer= receiptAllocate.getSalesToInfluencer()!=null?receiptAllocate.getSalesToInfluencer():0;
			int stockRetailer = Math.abs(receipt - salesToRetailer - salesToInfluencer);
			receiptAllocate.setStockAvlForRetailer(stockRetailer);
			int stockInfluencer = Math.abs((int) ((0.7 * (receipt - salesToRetailer)) - salesToInfluencer));
			receiptAllocate.setStockAvlForInfluencer(stockInfluencer);
			LOG.info("Order Invoiced CANCELLED...  receipt  -->> " + updatedQty
					+ "---setStockAvlForRetailer---" + stockRetailer
					+ "---stockInfluencer---" + stockInfluencer);
			modelService.save(receiptAllocate);
		}
	}

	@Override
	public boolean updateDealerCategorization(DealerCategorizationListData dealerCategorizationListData) {
		List<DealerCategorizationData> dealerCategorizationDetails = dealerCategorizationListData.getDealerCategorizationData();
		List<EyDmsCustomerModel> customerModelList = new ArrayList<>();
//		List<CreditAndOutstandingModel> creditModelList = new ArrayList<>();

		for (DealerCategorizationData dealerData : dealerCategorizationDetails) {
			EyDmsCustomerModel dealerCustomerModel = slctCrmIntegrationDao.getCustomerByCustNo(dealerData.getCustomerCode());

			if (!Objects.isNull(dealerCustomerModel)) {
				dealerCustomerModel.setDealerCategory(DealerCategory.valueOf(dealerData.getDealerCategory()));
				dealerCustomerModel.setCreditMultiplier(dealerData.getCreditMultiplier());
//				dealerCustomerModel.setCreditLimit(dealerData.getCreditLimit());
//				Double toDouble = new Double(dealerData.getSecurityDeposit());
//				dealerCustomerModel.setSecurityDepositAmount(toDouble.intValue());
				customerModelList.add(dealerCustomerModel);

//				CreditAndOutstandingModel dealerCreditModel = slctCrmIntegrationDao.getCreditByCustCode(dealerData.getCustomerCode());
//				if(Objects.isNull(dealerCreditModel)) {
//					dealerCreditModel = modelService.create(CreditAndOutstandingModel.class);
//				}
//				dealerCreditModel.setCustomerCode(dealerData.getCustomerCode());
//				dealerCreditModel.setCreditLimit(dealerData.getCreditLimit());
//				dealerCreditModel.setSecurityDeposit(dealerData.getSecurityDeposit());
//				creditModelList.add(dealerCreditModel);
			}

		}
		modelService.saveAll(customerModelList);
//		modelService.saveAll(creditModelList);

		return true;
	}

	@Override
	public List<List<Object>> getSlctCrmCustomerDetails() {
		return slctCrmIntegrationDao.getSlctCrmCustomerDetails();
	}

	@Override
	public boolean insertProspectiveNetworksList(SlctProspectiveNetworksListData prospectiveNetworksListData) {
		List<ProspectiveNetworkModel> oldProspectiveNetworkModelsList = slctCrmIntegrationDao.deleteAllProspectiveNetworkDetails();
		modelService.removeAll(oldProspectiveNetworkModelsList);

		List<SlctProspectiveNetworksData> newProspectiveNetworksList = prospectiveNetworksListData.getSlctProspectiveNetworkDetails();
		List<ProspectiveNetworkModel> modelList = new ArrayList<>();

		for (SlctProspectiveNetworksData prospectiveNetworksData : newProspectiveNetworksList) {
			ProspectiveNetworkModel prospectiveNetworkModel = modelService.create(ProspectiveNetworkModel.class);
			String brand = prospectiveNetworksData.getBrand();
			if (brand.equals("shree")) {
				brand = "102";
			}
			if (brand.equals("bangur")) {
				brand = "103";
			}
			if (brand.equals("rockstrong")) {
				brand = "104";
			}
			prospectiveNetworkModel.setBrand(baseSiteService.getBaseSiteForUID(brand));
			prospectiveNetworkModel.setCounterShareAction(CounterShareAction.valueOf(prospectiveNetworksData.getCounterShareAction()));
			prospectiveNetworkModel.setCrmAccountID(prospectiveNetworksData.getCrmAccountId());
			modelList.add(prospectiveNetworkModel);
		}
		modelService.saveAll(modelList);
		return true;
	}

	@Override
	public List<FreightAndIncoTermsMasterModel> findFreightAndIncoTerms(String state, String district, BaseSiteModel brand, String orgType) {
		final Map<String, Object> attr = new HashMap<String, Object>();
		attr.put(FreightAndIncoTermsMasterModel.STATE, state.toUpperCase());
		attr.put(FreightAndIncoTermsMasterModel.DISTRICT, district.toUpperCase());
		attr.put(FreightAndIncoTermsMasterModel.BRAND, brand.getUid());
		attr.put(FreightAndIncoTermsMasterModel.ORGTYPE, orgType);
		String queryResult = "SELECT {f:pk} from {FreightAndIncoTermsMaster as f} where UPPER({f:state})=?state and UPPER({f:district})=?district and {f:brand}=?brand and {f:orgType}=?orgType";

		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryResult);
		query.getQueryParameters().putAll(attr);
		final SearchResult<FreightAndIncoTermsMasterModel> result = flexibleSearchService.search(query);
		if (result.getResult() != null && !result.getResult().isEmpty()) {
			return result.getResult();
		} else {
			return Collections.EMPTY_LIST;
		}
	}

	@Override
	public boolean insertDealerLedgerDetails(DealerLedgerListData dealerLedgerListData) throws ParseException {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//		DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		List<DealerLedgerData> dealerLedgerDetails = dealerLedgerListData.getDealerLedgerDetails();

		List<LedgerDetailsModel> modelList = new ArrayList<>();
		for (DealerLedgerData dealerLedgerData : dealerLedgerDetails) {

			LedgerDetailsModel dealerLedgerModel = modelService.create(LedgerDetailsModel.class);
			dealerLedgerModel.setTransactionLineGlRcId(dealerLedgerData.getTransactionLineGlRcId());
			if (dealerLedgerData.getTransactionLineLastUpdateDate() != null) {
				dealerLedgerModel.setTransactionLineLastUpdateDate(dealerLedgerData.getTransactionLineLastUpdateDate());
			}

			dealerLedgerModel.setBrand(dealerLedgerData.getBrand());
			dealerLedgerModel.setCustomerNo(dealerLedgerData.getCustomerNo());
			if (dealerLedgerData.getTransactionDate() != null && !dealerLedgerData.getTransactionDate().isEmpty()) {
				try {
					dealerLedgerModel.setDate(dateFormat.parse(dealerLedgerData.getTransactionDate()));
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
			}
			dealerLedgerModel.setTransactionType(dealerLedgerData.getTransactionType());
			dealerLedgerModel.setChqNo(dealerLedgerData.getChequeNo());
			dealerLedgerModel.setDocNo(dealerLedgerData.getDocNo());
			dealerLedgerModel.setCreditAmount(dealerLedgerData.getCreditAmount());
			dealerLedgerModel.setDebitAmount(dealerLedgerData.getDebitAmount());
			dealerLedgerModel.setInvoiceQty(dealerLedgerData.getInvoiceQuantity());
			dealerLedgerModel.setActive(dealerLedgerData.getActive());
			modelService.save(dealerLedgerModel);
		}
		return true;
	}

	@Override
	public boolean insertUpdateDealerLedgerDetails(DealerLedgerListData dealerLedgerListData) throws ParseException {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//		DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		List<DealerLedgerData> dealerLedgerDetails = dealerLedgerListData.getDealerLedgerDetails();

		List<LedgerDetailsModel> modelList = new ArrayList<>();
		for (DealerLedgerData dealerLedgerData : dealerLedgerDetails) {
			LedgerDetailsModel dealerLedgerModel = slctCrmIntegrationDao.getLedgerDetails(dealerLedgerData.getTransactionLineGlRcId(),
					dealerLedgerData.getTransactionLineLastUpdateDate());
			if (Objects.isNull(dealerLedgerModel)) {
				dealerLedgerModel = modelService.create(LedgerDetailsModel.class);
				dealerLedgerModel.setTransactionLineGlRcId(dealerLedgerData.getTransactionLineGlRcId());
				if (dealerLedgerData.getTransactionLineLastUpdateDate() != null) {
					dealerLedgerModel.setTransactionLineLastUpdateDate(dealerLedgerData.getTransactionLineLastUpdateDate());
				}
			}

			dealerLedgerModel.setBrand(dealerLedgerData.getBrand());
			dealerLedgerModel.setCustomerNo(dealerLedgerData.getCustomerNo());
			if (dealerLedgerData.getTransactionDate() != null && !dealerLedgerData.getTransactionDate().isEmpty()) {
				try {
					dealerLedgerModel.setDate(dateFormat.parse(dealerLedgerData.getTransactionDate()));
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
			}
			dealerLedgerModel.setTransactionType(dealerLedgerData.getTransactionType());
			dealerLedgerModel.setChqNo(dealerLedgerData.getChequeNo());
			dealerLedgerModel.setDocNo(dealerLedgerData.getDocNo());
			dealerLedgerModel.setCreditAmount(dealerLedgerData.getCreditAmount());
			dealerLedgerModel.setDebitAmount(dealerLedgerData.getDebitAmount());
			dealerLedgerModel.setInvoiceQty(dealerLedgerData.getInvoiceQuantity());
			dealerLedgerModel.setActive(dealerLedgerData.getActive());
			modelService.save(dealerLedgerModel);

//			try{
//				//EyDmsCustomerModel customerModel = (EyDmsCustomerModel) userService.getUserForUID(dealerLedgerData.getCustomerNo());
//				EyDmsCustomerModel customerModel = slctCrmIntegrationDao.getCustomerByCustNo(dealerLedgerData.getCustomerNo());
//				if(customerModel!=null){
//					StringBuilder builder = new StringBuilder();
//					builder.append(" Credit note has been issued, Dealer name: " + customerModel.getName() + " "+customerModel.getCustomerNo()+", Cheque no: "+dealerLedgerModel.getChqNo());
//					builder.append(" ,Document no: " + dealerLedgerModel.getDocNo() + ", Transaction type: " + dealerLedgerModel.getTransactionType() );
//					builder.append(", Credit amount: ₹ "+dealerLedgerModel.getCreditAmount());
//					String body = builder.toString();
//
//					StringBuilder builder1 = new StringBuilder();
//					builder1.append("Credit note added ");
//					String subject = builder1.toString();
//
//					NotificationCategory category = NotificationCategory.CREDIT_NOTE_ISSUE;
//
//					eydmsNotificationService.submitLimitNotification(customerModel,customerModel,body,subject,category);
//				}
//
//
//			}catch (Exception e){
//				LOG.error("Error while sending credit note/ledger details notification");
//			}


		}
		return true;
	}

	@Override
	public boolean deleteOldIsoMasterData() {
//		List<ISOMasterModel> previousIsoMasterDetails = slctCrmIntegrationDao.getIsoMasterDetails();
//		if(!previousIsoMasterDetails.isEmpty()) {
//			modelService.removeAll(previousIsoMasterDetails);
//			return true;
//		}
//		return false;

		SqlSearchResultData searchResult;
		LOG.info("Deletion of all ISO Master Data");
		Calendar calendar = Calendar.getInstance();
		//Returns current time in millis
		long timeMilli1 = calendar.getTimeInMillis();
		LOG.info("Start time in milliseconds in ISO Master Data:" + timeMilli1);
		try {
			String query = "delete from isomaster";
			HacFlexibleSearchFacade flexibleSearchFacade = new HacFlexibleSearchFacade();
			searchResult = flexibleSearchFacade.executeRawSql(query, 2000000, true);

		} catch (Exception e) {
			LOG.info("Exception Message in ISO Master Data" + e.getMessage());
			throw new RuntimeException(e);
		}
		long timeMilli2 = calendar.getTimeInMillis();
		LOG.info("End time in milliseconds in ISO Master Data:" + timeMilli2);
		LOG.info("Time taken in milliseconds in ISO Master Data:" + (timeMilli2 - timeMilli1));
		return true;
	}

	@Override
	public boolean insertIsoMasterDetails(SlctISOMasterListData slctISOMasterListData) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		List<SlctISOMasterData> newIsoMasterDetails = slctISOMasterListData.getIsoMasterDetails();
		List<ISOMasterModel> modelList = new ArrayList<>();
//		List<String> deliveryDetailIdsList =  new ArrayList<>();

		for (SlctISOMasterData isoMasterData : newIsoMasterDetails) {
			ISOMasterModel isoMasterModel = modelService.create(ISOMasterModel.class);

//			if(deliveryDetailIdsList.contains(isoMasterData.getDeliveryDetailId())) {
//				continue;
//			}
//			else {
//				deliveryDetailIdsList.add(isoMasterData.getDeliveryDetailId());
//			}

			if (isoMasterData.getDeliveryId() != null) {
				isoMasterModel.setDeliveryId(isoMasterData.getDeliveryId());
			}
			if (isoMasterData.getDeliveryDetailId() != null) {
				isoMasterModel.setDeliveryDetailId(isoMasterData.getDeliveryDetailId());
			}
			if (isoMasterData.getCustomer() != null) {
				isoMasterModel.setCustomer(isoMasterData.getCustomer());
			}
			isoMasterModel.setCustomerCategory(isoMasterData.getCustomerCategory());
			isoMasterModel.setConsignee(isoMasterData.getConsignee());
			isoMasterModel.setShipmentState(isoMasterData.getShipmentState());
			isoMasterModel.setPackagingType(isoMasterData.getPackagingType());
			isoMasterModel.setPackagingMaterial(isoMasterData.getPackagingMaterial());
			isoMasterModel.setTransporterName(isoMasterData.getTransporterName());
			isoMasterModel.setTransporterMobile(isoMasterData.getTransporterMobile());
			isoMasterModel.setDriverName(isoMasterData.getDriverName());
			isoMasterModel.setDriverMobile(isoMasterData.getDriverMobile());
			isoMasterModel.setDeliveryMode(isoMasterData.getDeliveryMode());
			isoMasterModel.setVehicleType(isoMasterData.getVechicleType());
			isoMasterModel.setVehicleNo(isoMasterData.getVehicleNo());
			isoMasterModel.setGrade(isoMasterData.getGrade());
			if (isoMasterData.getActualDepartureDate() != null && !isoMasterData.getActualDepartureDate().isEmpty()) {
				try {
					isoMasterModel.setActualDepartureDate(dateFormat.parse(isoMasterData.getActualDepartureDate()));
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
			}
			isoMasterModel.setFromOrgName(isoMasterData.getFromOrgName());
			if (isoMasterData.getQtyShipped() != null) {
				isoMasterModel.setQtyShipped(isoMasterData.getQtyShipped());
			}
			if (isoMasterData.getQtyDelivered() != null) {
				isoMasterModel.setQtyDelivered(isoMasterData.getQtyDelivered());
			}
			if (isoMasterData.getReceiptDate() != null && !isoMasterData.getReceiptDate().isEmpty()) {
				try {
					isoMasterModel.setReceiptDate(dateFormat.parse(isoMasterData.getReceiptDate()));
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
			}
			isoMasterModel.setBrand(isoMasterData.getBrand());
			isoMasterModel.setPlantCode(isoMasterData.getPlantCode());
			isoMasterModel.setDepotCode(isoMasterData.getDepotCode());

			modelList.add(isoMasterModel);
		}
		modelService.saveAll(modelList);
		return true;
	}

	private ProductModel findProductByStateAndPckCndtAndInventoryId(String state, String inventoryId, String brand, String packagingCondition) {
		return (ProductModel) sessionService.executeInLocalView(new SessionExecutionBody() {
			@Override
			public ProductModel execute() {
				try {
					searchRestrictionService.disableSearchRestrictions();
					catalogVersionService.setSessionCatalogVersion(brand + "ProductCatalog", "Online");
					CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(brand + "ProductCatalog", "Online");

					final Map<String, Object> attr = new HashMap<String, Object>();
					attr.put("state", state.toUpperCase());
					attr.put("inventoryId", inventoryId);
					attr.put("packagingCondition", packagingCondition);
					attr.put("custCategory", "TR");
					attr.put("catalogVersion", catalogVersion);

					String queryResult = "SELECT {p:pk} from {Product as p} where UPPER({p:state})=?state and {p.inventoryId}=?inventoryId and {p.packagingCondition}=?packagingCondition and {p.custCategory}=?custCategory and {p.catalogVersion}=?catalogVersion ";

					final FlexibleSearchQuery query = new FlexibleSearchQuery(queryResult);
					query.getQueryParameters().putAll(attr);
					final SearchResult<ProductModel> result = flexibleSearchService.search(query);
					if (result.getResult() != null && !result.getResult().isEmpty()) {
						return result.getResult().get(0);
					} else {
						return null;
					}
				} finally {
					searchRestrictionService.enableSearchRestrictions();
				}
			}
		});

	}

	private OrderModel findOrderbyErpOrderNoAndOrderType(String erpOrderNumber, String erpOrderType) {

		final Map<String, Object> attr = new HashMap<String, Object>();
		attr.put("erpOrderNumber", erpOrderNumber);
		attr.put("erpOrderType", erpOrderType);

		String queryResult = "SELECT {o:pk} from {Order as o} where {o.erpOrderNumber}=?erpOrderNumber and {o.erpOrderType}=?erpOrderType ";

		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryResult);
		query.getQueryParameters().putAll(attr);
		final SearchResult<OrderModel> result = flexibleSearchService.search(query);
		if (result.getResult() != null && !result.getResult().isEmpty()) {
			return result.getResult().get(0);
		} else {
			return null;
		}
	}

	public EyDmsCustomerModel findCustomerByCustomerNo(String customerNo) {

		final Map<String, Object> attr = new HashMap<String, Object>();
		attr.put("customerNo", customerNo);

		String queryResult = "SELECT {c:pk} from {EyDmsCustomer as c} where {c.customerNo}=?customerNo ";

		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryResult);
		query.getQueryParameters().putAll(attr);
		final SearchResult<EyDmsCustomerModel> result = flexibleSearchService.search(query);
		if (result.getResult() != null && !result.getResult().isEmpty()) {
			return result.getResult().get(0);
		} else {
			return null;
		}
	}

	@Override
	public List<String> updateOrderEntryFromErp(List<IntegrationOrderEntryData> entryList) {
		List<String> errorMessages = new ArrayList<>();
		try {

			int updatedEntry = 0;
			boolean isDelivered = false;
			CMSSiteModel brand = null;

			StringBuilder errorMessage = new StringBuilder();

			if (entryList != null && !entryList.isEmpty()) {
				for (IntegrationOrderEntryData data : entryList) {
					OrderModel order = b2bOrderService.getOrderForCode(data.getCrmOrderCode());

					if (order == null) {
						errorMessage.append("Order not found in CRM " + data.getCrmOrderCode() + ", ");
					}

					Map<String, WarehouseModel> mapWarehouse = new HashMap<>();
					if (data.getWarehouseName() != null) {

						WarehouseModel source = warehouseService.getWarehouseForCode(data.getWarehouseName().split(":")[0]);

						if (source == null) {
							errorMessage.append(String.format("Source not found in crm for line item %s, ", data.getErpLineItemId()));
						} else {
							if (source.getType() != null) {
								mapWarehouse.put(data.getErpLineItemId(), source);
							} else {
								errorMessage.append(String.format("Source Type not found in crm for line item %s, ", data.getErpLineItemId()));
							}
						}
					} else {
						errorMessage.append(String.format("Source name missing for line item %s, ", data.getErpLineItemId()));
					}

					String errorMsg = errorMessage.toString();

					if (StringUtils.isNotBlank(errorMsg)) {
						errorMessages.add(errorMsg);
						continue;
					}

					AbstractOrderEntryModel entryOptional = slctCrmIntegrationDao.findOrderEntryByErpLineItemId(data.getErpLineItemId());
					OrderEntryModel entryModel = null;

					Boolean isOrderDelivered = false;

					if (entryOptional == null) {
						LOG.info("when entry optional is null");
						AbstractOrderEntryModel baseEntryModel = order.getEntries().get(0);

						if (baseEntryModel != null) {
							entryModel = modelService.create(OrderEntryModel.class);
							entryModel.setOrder(order);
							entryModel.setProduct(baseEntryModel.getProduct());
							entryModel.setUnit(baseEntryModel.getUnit());
							entryModel.setEntryNumber(data.getEntryNumber());
							entryModel.setSequence(data.getEntryNumber() + 1);
							entryModel.setErpLineItemId(data.getErpLineItemId());
							double basePrice = baseEntryModel.getBasePrice() != null ? baseEntryModel.getBasePrice() : 0.0;
							entryModel.setBasePrice(basePrice);
							isOrderDelivered = populateOrderEntryModel(entryModel, data, basePrice, mapWarehouse);
							entryModel.setInvoiceCreationDateAndTime(data.getInvoiceCreationDate());
						}
					} else {
						LOG.info("when entry optional is not null");
						//update same info as in cron job for case1
						//quantity and invoice date
						entryModel.setQuantityInMT(data.getQuantityMT());
						entryModel.setQuantity((long) (data.getQuantityMT() * 1000));
						entryModel.setTotalPrice(entryModel.getQuantityInMT() * entryModel.getBasePrice());

						//status
						if (data.getInvoiceCreationDate() != null && data.getInvoiceQuantity() != null && data.getInvoiceQuantity() > 0 && entryModel.getTruckDispatcheddate() == null && entryModel.getInvoiceCancelDate() == null && entryModel.getCancelledDate() == null) {
							if (entryModel.getSource() != null && entryModel.getSource().getType().equals(WarehouseType.DEPOT)) {
								entryModel.setTruckAllocatedDate(entryModel.getInvoiceCreationDateAndTime());
								entryModel.setTruckDispatcheddate(entryModel.getInvoiceCreationDateAndTime());
								entryModel.setStatus(OrderStatus.TRUCK_DISPATCHED);
								if (entryModel.getFob() != null && entryModel.getFob().equals(IncoTerms.EX)) {
									entryModel.setDeliveredDate(entryModel.getInvoiceCreationDateAndTime());
									entryModel.setStatus(OrderStatus.DELIVERED);
									isDelivered = true;
								}
							} else {
								entryModel.setStatus(OrderStatus.INVOICED);
							}
						}

						//update last lifting date and network type
						EyDmsCustomerModel cus = (EyDmsCustomerModel) entryModel.getOrder().getUser();
						Date existingLastLiftingDate = cus.getLastLiftingDate();
						if (existingLastLiftingDate != null) {
							if (existingLastLiftingDate.compareTo(entryModel.getInvoiceCreationDateAndTime()) < 0) {
								cus.setLastLiftingDate(entryModel.getInvoiceCreationDateAndTime());  // for networkType calculation
								cus.setNetworkType(NetworkType.ACTIVE.getCode());
								modelService.save(cus);
								modelService.refresh(cus);
							}
						} else {
							cus.setLastLiftingDate(entryModel.getInvoiceCreationDateAndTime());  // for networkType calculation
							cus.setNetworkType(NetworkType.ACTIVE.getCode());
							modelService.save(cus);
							modelService.refresh(cus);
						}


						//stock quantity
						Double receiptQty = 0.0;
						if (null != entryModel.getInvoiceQuantity()) {
							receiptQty = entryModel.getInvoiceQuantity() * EyDmsCoreConstants.QUANTITY_INMT_TO_BAGS;
						}
						OrderModel orderRequistionCheck = entryModel.getOrder();
						if (null != orderRequistionCheck && (null == orderRequistionCheck.getRequisitions()
								|| orderRequistionCheck.getRequisitions().size() != 1)) {
							LOG.info("going inside update receipt");
							updateReceipts(entryModel.getProduct(), cus, receiptQty);
						}

						if (data.getCustomerTransactionId() != null && data.getCustomerTransactionLineId() != null && brand != null) {
							entryModel.setCustomerTransactionId(data.getCustomerTransactionId());
							entryModel.setCustomerTransactionLineId(data.getCustomerTransactionLineId());
							entryModel.setBrand(data.getBrand());
						}
					}

					if (entryModel != null) {
						modelService.save(entryModel);
						modelService.refresh(order);
						updatedEntry++;

						if (isOrderDelivered) {
							eydmsB2BOrderService.saveOrderRequisitionEntryDetails(entryModel.getOrder(), entryModel, "EPOD");
						}

						if (data.getBrand() != null) {
							brand = (CMSSiteModel) baseSiteService.getBaseSiteForUID(data.getBrand());
						}
						if (data.getCustomerTransactionId() != null && data.getCustomerTransactionLineId() != null && brand != null) {
							SalesHistoryModel salesHistory = slctCrmIntegrationDao.getSalesHistoryModel(data.getCustomerTransactionId(), data.getCustomerTransactionLineId(), brand);
							if (salesHistory != null) {
								salesHistory.setSynced(true);
								modelService.save(salesHistory);
								modelService.refresh(salesHistory);
							}
						}
					}

					order.setTotalPrice(order.getEntries().stream().collect(Collectors.summingDouble(AbstractOrderEntryModel::getTotalPrice)));
					LOG.info("saving into order model" + order.getCode() != null ? order.getCode() : "");
					modelService.save(order);
				}
			}
			return errorMessages;
		} catch (Exception e) {

			String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getName() + " Occurred";
			errorMessages.add(errorMsg);
			throw new UnknownIdentifierException(errorMsg);

		}
	}

	private void setCurrentBaseSite(final String baseSite) throws ServletException {
		final BaseSiteModel requestedBaseSite = baseSiteService.getBaseSiteForUID(baseSite);
		if (requestedBaseSite == null) {
			throw new ServletException("Requested BaseSite: " + baseSite + " cannot be null");
		}
		baseSiteService.setCurrentBaseSite(requestedBaseSite, true);
	}

	@Override
	public OrderModel createOrderFromErp(IntegrationOrderData orderData) {
		try {
			setCurrentBaseSite(orderData.getSite());

			EyDmsCustomerModel dealer = null;
			SubAreaMasterModel subAreaMaster = null;
			ProductModel productModel = null;
			StringBuilder errorMessage = new StringBuilder();
			if (orderData.getErpOrderNo() == null) {
				errorMessage.append("Erp Order No is missing, ");
			}
			if (orderData.getErpOrderType() == null) {
				errorMessage.append("Erp Order Type is missing, ");
			}
			String errorMsg1 = errorMessage.toString();
			if (StringUtils.isNotBlank(errorMsg1)) {
				throw new UnknownIdentifierException(errorMsg1);
			}
			OrderModel existingOrder = findOrderbyErpOrderNoAndOrderType(orderData.getErpOrderNo(), orderData.getErpOrderType());
			if (existingOrder == null) {
				if (orderData.getUser() != null && orderData.getUser().getUid() != null) {
					dealer = findCustomerByCustomerNo(orderData.getUser().getUid());
				} else {
					errorMessage.append("Customer No is missing, ");
				}
				if (dealer == null) {
					errorMessage.append(String.format("Dealer %s not found in crm, ", orderData.getUser().getUid()));
				} else {
					List<SubAreaMasterModel> listSubAreaMasterModel = territoryManagementDao.getTerritoriesForCustomerIncludingNonActive(dealer);
					if (listSubAreaMasterModel != null && !listSubAreaMasterModel.isEmpty()) {
						subAreaMaster = listSubAreaMasterModel.get(0);
					} else {
						errorMessage.append("SubArea not found for customer, ");
					}

				}
				if (orderData.getDeliveryAddress() == null) {
					errorMessage.append("Delivery address missing, ");
				}
				if (orderData.getDeliveryAddress() != null && (orderData.getDeliveryAddress().getState() == null || orderData.getDeliveryAddress().getDistrict() == null || orderData.getDeliveryAddress().getTaluka() == null || orderData.getDeliveryAddress().getErpId() == null)) {
					errorMessage.append("Delivery address state, district, taluka or erpAddressId missing, ");
				}
				Map<String, WarehouseModel> mapWarehouse = new HashMap<>();
				if (orderData.getOrderEntries() != null && !orderData.getOrderEntries().isEmpty()) {
					IntegrationOrderEntryData entryData = orderData.getOrderEntries().get(0);
					if (entryData.getBagType() == null || entryData.getInventoryItemId() == null) {
						errorMessage.append("Bag type or Inventory id missing, ");
					} else if (orderData.getDeliveryAddress() != null && orderData.getDeliveryAddress().getState() != null) {
						productModel = findProductByStateAndPckCndtAndInventoryId(orderData.getDeliveryAddress().getState(), entryData.getInventoryItemId(), orderData.getSite(), entryData.getBagType());
					}

					for (IntegrationOrderEntryData data : orderData.getOrderEntries()) {
						if (data.getWarehouseCode() != null) {
							WarehouseModel source = eydmsWarehouseDao.findWarehouseByOrgCode(data.getWarehouseCode());
							if (source == null) {
								errorMessage.append(String.format("Source not found in crm for line item %s, ", data.getErpLineItemId()));
							} else {
								if (source.getType() != null) {
									mapWarehouse.put(data.getErpLineItemId(), source);
								} else {
									errorMessage.append(String.format("Source Type not found in crm for line item %s, ", data.getErpLineItemId()));
								}
							}
						} else {
							errorMessage.append(String.format("Source code missing for line item %s, ", data.getErpLineItemId()));
						}
					}

				} else {
					errorMessage.append("Line items missing, ");
				}
				if (productModel == null) {
					errorMessage.append("Product not found, ");
				}
				String errorMsg = errorMessage.toString();
				if (StringUtils.isNotBlank(errorMsg)) {
					throw new UnknownIdentifierException(errorMsg);
				}

				List<IntegrationOrderEntryData> entryList = orderData.getOrderEntries();
				OrderModel order = modelService.create(OrderModel.class);
				order.setCode(String.valueOf(keyGenerator.generate()));
				order.setUser(dealer);
				order.setSite(baseSiteService.getCurrentBaseSite());
				order.setStore(order.getSite().getStores().get(0));
				order.setTotalPrice(0.0);
				order.setStatus(OrderStatus.ORDER_ACCEPTED);
				order.setIsDealerProvideOwnTransport(orderData.getIsDealerProvideOwnTransport());
				if (order.getUser() != null) {
					order.setPlacedBy(territoryService.getSOforCustomer((EyDmsCustomerModel) order.getUser()));
				}
				order.setSubAreaMaster(subAreaMaster);
				if (subAreaMaster != null) {
					order.setDistrictMaster(subAreaMaster.getDistrictMaster());
					if (subAreaMaster.getDistrictMaster() != null) {
						order.setRegionMaster(subAreaMaster.getDistrictMaster().getRegion());
						if (subAreaMaster.getDistrictMaster().getRegion() != null) {
							order.setStateMaster(subAreaMaster.getDistrictMaster().getRegion().getState());
						}
					}
				}
				CustDepotMasterModel custDepotForCustomer = territoryService.getCustDepotForCustomer((EyDmsCustomerModel) order.getUser());
				order.setCustDepot(custDepotForCustomer);

				order.setDate(orderData.getCreated());
				order.setOrderAcceptedDate(orderData.getCreated());
				order.setErpOrderNumber(orderData.getErpOrderNo());
				order.setPaymentType(CheckoutPaymentType.ACCOUNT);
				order.setErpOrderType(orderData.getErpOrderType());
				order.setOrderType(OrderType.valueOf(orderData.getOrderType()));
				order.setLanguage(commonI18NService.getCurrentLanguage());
				order.setCurrency(order.getStore().getDefaultCurrency());
				if (orderData.getDeliveryMode() != null && orderData.getDeliveryMode().getCode() != null) {
					order.setDeliveryMode(deliveryModeService.getDeliveryModeForCode(orderData.getDeliveryMode().getCode()));
				}

				AddressModel address = null;
				if (((EyDmsCustomerModel) order.getUser()).getAddresses() != null) {
					Optional<AddressModel> addOpiotnal = ((EyDmsCustomerModel) order.getUser()).getAddresses().stream().filter(each -> each.getErpAddressId() != null && each.getErpAddressId().equals(orderData.getDeliveryAddress().getErpId())).findAny();
					if (addOpiotnal.isPresent()) {
						address = addOpiotnal.get();
					} else {
						AddressData addressData = orderData.getDeliveryAddress();
						addressData.setShippingAddress(true);
						addressData.setVisibleInAddressBook(true);

						address = modelService.create(AddressModel.class);
						addressReversePopulator.populate(addressData, address);
						customerAccountService.saveAddressEntry((CustomerModel) order.getUser(), address);

					}
				}
				//address last used date change
				address.setLastUsedDate(new Date());
				modelService.save(address);
				modelService.refresh(address);
				AddressModel deliveryAddress = modelService.clone(address);
				deliveryAddress.setOwner(order);
				deliveryAddress.setShippingAddress(true);
				order.setDeliveryAddress(deliveryAddress);
				order.setIsErpOrder(Boolean.TRUE);
				modelService.save(order);
				entryList.forEach(data -> {
					OrderEntryModel entryModel = modelService.create(OrderEntryModel.class);
					entryModel.setOrder(order);
					entryModel.setProduct(findProductByStateAndPckCndtAndInventoryId(orderData.getDeliveryAddress().getState(), data.getInventoryItemId(), orderData.getSite(), data.getBagType()));
					entryModel.setUnit(unitService.getUnitForCode("pieces"));
					entryModel.setEntryNumber(data.getEntryNumber());
					entryModel.setSequence(data.getEntryNumber() + 1);
					entryModel.setErpLineItemId(data.getErpLineItemId());
					entryModel.setTotalPrice(0.0);
					entryModel.setQuantityInMT(data.getQuantityMT());
					entryModel.setQuantity((long) (data.getQuantityMT() * 1000));
					if (data.getIncoTerm() != null) {
						entryModel.setFob(IncoTerms.valueOf(data.getIncoTerm()));
						if (entryModel.getFob().equals(IncoTerms.EX))
							entryModel.setEpodCompleted(true);
					}
					if (data.getFreightTerm() != null) {
						entryModel.setFreightTerms(FreightTerms.valueOf(data.getFreightTerm()));
					}
					entryModel.setEventDate(data.getEventDate());
					entryModel.setParentId(data.getParentId());
					entryModel.setRouteId(data.getRouteId());
					//WarehouseModel source = eydmsWarehouseDao.findWarehouseByOrgCode(data.getWarehouseCode());
					if (mapWarehouse.containsKey(data.getErpLineItemId())) {
						entryModel.setSource(mapWarehouse.get(data.getErpLineItemId()));
					}
					entryModel.setStatus(OrderStatus.ORDER_ACCEPTED);

					if (entryModel.getProduct() != null && entryModel.getOrder().getDeliveryMode() != null && entryModel.getOrder().getDeliveryAddress() != null && entryModel.getOrder().getSite() != null) {
						DestinationSourceMasterModel destinationSource = destinationSourceMasterDao.getDestinationSourceBySource(OrderType.SO, CustomerCategory.TR, entryModel.getSource(), entryModel.getOrder().getDeliveryMode(), entryModel.getOrder().getDeliveryAddress().getErpCity(), entryModel.getOrder().getDeliveryAddress().getDistrict(), entryModel.getOrder().getDeliveryAddress().getState(), entryModel.getOrder().getSite(),
								entryModel.getProduct().getGrade(), entryModel.getProduct().getBagType(), entryModel.getOrder().getDeliveryAddress().getTaluka());
						if (destinationSource != null && destinationSource.getDistance() != null) {
							entryModel.setDistance(destinationSource.getDistance().doubleValue());
						}
					}

					LocalDateTime time = order.getDate().toInstant()
							.atZone(ZoneId.systemDefault())
							.toLocalDateTime();

					if (entryModel.getSource() != null && entryModel.getRouteId() != null) {
						DeliveryDateAndSlotListData listData = eydmsB2BOrderService.getOptimalDeliveryWindow(entryModel.getQuantityInMT(), data.getRouteId(), (B2BCustomerModel) order.getUser(), time, entryModel.getSource().getCode(), "false");
						if (listData != null && listData.getDeliveryDateAndSlots() != null && !listData.getDeliveryDateAndSlots().isEmpty()) {
							DeliveryDateAndSlotData deliveryData = listData.getDeliveryDateAndSlots().get(0);

							SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
							try {
								entryModel.setCalculatedDeliveryDate(formatter.parse(deliveryData.getDeliveryDate()));
								entryModel.setExpectedDeliveryDate(entryModel.getCalculatedDeliveryDate());
							} catch (ParseException e) {

							}

							entryModel.setCalculatedDeliveryslot(DeliverySlots.valueOf(deliveryData.getDeliverySlot()));
							entryModel.setExpectedDeliveryslot(entryModel.getCalculatedDeliveryslot());
						}
					}
					modelService.save(entryModel);
				});
				if (order.getEntries() != null) {
					order.setTotalQuantity(order.getEntries().stream().collect(Collectors.summingDouble(AbstractOrderEntryModel::getQuantityInMT)));
				}
				modelService.save(order);
				modelService.refresh(order);


				EyDmsOutboundPriceModel outboundPrice = new EyDmsOutboundPriceModel();
				outboundPrice.setProduct(order.getEntries().get(0).getProduct().getGrade());
				outboundPrice.setPacking_type(order.getEntries().get(0).getProduct().getBagType());
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				outboundPrice.setBill_date(dateFormat.format(order.getDate()));
				outboundPrice.setOrg_id(order.getSite().getUid());
				outboundPrice.setOrganization_id(order.getEntries().get(0).getSource().getOrganisationId());
				outboundPrice.setCity(order.getDeliveryAddress().getErpCity());
				outboundPrice.setTaluka(order.getDeliveryAddress().getTaluka());
				outboundPrice.setDistrict(order.getDeliveryAddress().getDistrict());
				outboundPrice.setState(order.getDeliveryAddress().getState());

				outboundServiceFacade.send(outboundPrice, OUTBOUND_PRICE_OBJECT, OUTBOUND_PRICE_DESTINATION).subscribe(

						// onNext
						responseEntityMap -> {

							String response = getPropertyValue(responseEntityMap, "GET_EFFECTIVE_ORDER_VALUE");
							if (StringUtils.isNotBlank(response)) {

								String[] arr = response.split(",");

								Double priceValue = 0.0;
								String status = null;
								if (arr.length > 0 && arr[0] != null && !arr[0].isBlank()) {
									priceValue = Double.valueOf(arr[0]);
								}
								if (arr.length > 1 && arr[1] != null && !arr[1].isBlank()) {
									status = arr[1];
								}

								Double basePrice = priceValue;

								order.getEntries().forEach(entry -> {
									entry.setBasePrice(basePrice);
									entry.setTotalPrice(basePrice * entry.getQuantityInMT());
									modelService.save(entry);
								});
								order.setTotalPrice(order.getEntries().stream().collect(Collectors.summingDouble(AbstractOrderEntryModel::getTotalPrice)));
								order.setPriceSlctStatus(status);
								modelService.save(order);
								modelService.refresh(order);
							}


						}

						// onError
						, error -> {
							order.getEntries().forEach(entry -> {
								entry.setBasePrice(0.0);
								entry.setTotalPrice(0.0);
								modelService.save(entry);
							});
							order.setTotalPrice(0.0);
							modelService.save(order);
							modelService.refresh(order);
						});

				return order;
			}
			return existingOrder;
		} catch (Exception e) {
			String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getName() + " Occurred";
			throw new UnknownIdentifierException(errorMsg);
		}
	}

	public KeyGenerator getKeyGenerator() {
		return keyGenerator;
	}

	public void setKeyGenerator(KeyGenerator keyGenerator) {
		this.keyGenerator = keyGenerator;
	}

	static String getPropertyValue(ResponseEntity<Map> responseEntityMap, String property) {
		if (responseEntityMap.getBody() != null) {
			Object next = responseEntityMap.getBody().keySet().iterator().next();
			checkArgument(next != null,
					String.format("SCPI response entity key set cannot be null for property [%s]!", property));

			String responseKey = next.toString();
			checkArgument(responseKey != null && !responseKey.isEmpty(),
					String.format("SCPI response property can neither be null nor empty for property [%s]!", property));

			Object propertyValue = responseEntityMap.getBody().get(responseKey);
			//checkArgument(propertyValue != null, String.format("SCPI response property [%s] value cannot be null!", property));

			return propertyValue.toString();
		} else {
			return null;
		}
	}

	@Override
	public boolean insertEtaDetails(SlctEtaListData slctEtaListData) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		List<SlctEtaData> slctEtaDataList = slctEtaListData.getEtaDetailsList();

		for (SlctEtaData slctEtaData : slctEtaDataList) {
			if (slctEtaData.getDeliveryId() != null && !slctEtaData.getDeliveryId().isEmpty()) {
				EtaTrackerModel etaTrackerModel = slctCrmIntegrationDao.getEtaByDiNumber(slctEtaData.getDeliveryId());
				if (Objects.isNull(etaTrackerModel)) {
					etaTrackerModel = modelService.create(EtaTrackerModel.class);
					etaTrackerModel.setDeliveryId(slctEtaData.getDeliveryId());
				}
				etaTrackerModel.setOrderType(slctEtaData.getDiOrISO());
				if (slctEtaData.getEtaDate() != null && !slctEtaData.getEtaDate().isEmpty()) {
					try {
						etaTrackerModel.setEtaDate(dateFormat.parse(slctEtaData.getEtaDate()));
					} catch (ParseException e) {
						throw new RuntimeException(e);
					}
				}

				modelService.save(etaTrackerModel);

			}
		}

		return true;
	}

	@Override
	public boolean insertUpdateCustomerMasterDetails(SlctCrmIntegrationCustomerMasterListData slctCrmIntegrationCustomerMasterListData) {
		List<SlctCrmIntegrationCustomerMasterData> customerMasterDetails = slctCrmIntegrationCustomerMasterListData.getCustomerMasters();
		DateFormat dateFormat1 = new SimpleDateFormat("dd-MM-yyyy");
		DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		for (SlctCrmIntegrationCustomerMasterData customerMasterData : customerMasterDetails) {
			if (customerMasterData.getCustomerNo() != null && !customerMasterData.getCustomerNo().isEmpty()) {
				Set<PrincipalGroupModel> groups = new HashSet<>();
				PrincipalGroupModel principalGroupModel;
				EyDmsCustomerModel customerMasterModel = slctCrmIntegrationDao.getCustomerByCustNo(customerMasterData.getCustomerNo());
				if (Objects.isNull(customerMasterModel)) {
					PrincipalGroupModel b2bCustomerGroupModel;
					customerMasterModel = modelService.create(EyDmsCustomerModel.class);
					//TODO comment once confirmed
					customerMasterModel.setUid(customCodeGenerator.generate().toString());
					customerMasterModel.setCrmRefCode(customerMasterModel.getUid());
					//TODO uncomment once confirmed
//					customerMasterModel.setUid(customerMasterData.getCustomerNo());
//					customerMasterModel.setCrmRefCode(customCodeGenerator.generate().toString());
					customerMasterModel.setCustomerNo(customerMasterData.getCustomerNo());
					b2bCustomerGroupModel = slctCrmIntegrationDao.getPrincipalGroupByUid("b2bcustomergroup");
					groups.add(b2bCustomerGroupModel);
					customerMasterModel.setIsCreatedFromSlctIntegration(true);
					customerMasterModel.setLoginDisabled(true);
				}
				customerMasterModel.setCustomerID(customerMasterData.getCustomerId());
				customerMasterModel.setName(customerMasterData.getCustomerName());
				if (customerMasterData.getCustomerCategory() != null && !customerMasterData.getCustomerCategory().isEmpty() && CustomerCategory.valueOf(customerMasterData.getCustomerCategory()) != null) {
					customerMasterModel.setCustomerCategory(CustomerCategory.valueOf(customerMasterData.getCustomerCategory()));
				}
				customerMasterModel.setState(customerMasterData.getState());
				customerMasterModel.setMobileNumber(customerMasterData.getMobileNumber());
				if(customerMasterData.getEmail()!=null && !customerMasterData.getEmail().isEmpty()) {
					if(isValidEmail(customerMasterData.getEmail())) {
						customerMasterModel.setEmail(customerMasterData.getEmail());
					}
					else {
						customerMasterModel.setEmail("dummy@test.com");
					}
				}
				else {
					customerMasterModel.setEmail("dummy@test.com");
				}
				if (customerMasterData.getDateOfJoining() != null && !customerMasterData.getDateOfJoining().isEmpty()) {
					try {
						customerMasterModel.setDateOfJoining(dateFormat1.parse(customerMasterData.getDateOfJoining()));
					} catch (ParseException e) {
						throw new RuntimeException(e);
					}
				}
				if (customerMasterData.getBrand() != null && !customerMasterData.getBrand().isEmpty()) {
					if (customerMasterData.getBrand().equals("102")) {
						customerMasterModel.setDefaultB2BUnit(defaultB2BUnitService.getUnitForUid("EyDmsShreeUnit"));
					} else if (customerMasterData.getBrand().equals("103")) {
						customerMasterModel.setDefaultB2BUnit(defaultB2BUnitService.getUnitForUid("EyDmsBangurUnit"));
					} else if (customerMasterData.getBrand().equals("104")) {
						customerMasterModel.setDefaultB2BUnit(defaultB2BUnitService.getUnitForUid("EyDmsRockstrongUnit"));
					}
				}

				if (customerMasterData.getCustomerType() != null && !customerMasterData.getCustomerType().isEmpty()) {

					if (customerMasterData.getCustomerType().toLowerCase().equals("dealer")) {
						customerMasterModel.setCounterType(CounterType.DEALER);
						principalGroupModel = slctCrmIntegrationDao.getPrincipalGroupByUid("EyDmsDealerGroup");
						groups.add(principalGroupModel);
						customerMasterModel.setGroups(groups);
					} else if (customerMasterData.getCustomerType().toLowerCase().equals("retailer")) {
						customerMasterModel.setCounterType(CounterType.RETAILER);
						principalGroupModel = slctCrmIntegrationDao.getPrincipalGroupByUid("EyDmsRetailerGroup");
						groups.add(principalGroupModel);
						customerMasterModel.setGroups(groups);
					}
				}

				customerMasterModel.setBankAccountNo(customerMasterData.getBankAccountNo());
				customerMasterModel.setBankName(customerMasterData.getBankName());
				customerMasterModel.setIfscCode(customerMasterData.getIfscCode());
				customerMasterModel.setGstIN(customerMasterData.getGstIn());
				customerMasterModel.setPanCard(customerMasterData.getPanCard());
				customerMasterModel.setIsModifiedFromSlctIntegration(true);

				if (customerMasterData.getInactiveDate() != null && !customerMasterData.getInactiveDate().isEmpty()) {
					try {
						customerMasterModel.setInactiveDate(dateFormat2.parse(customerMasterData.getInactiveDate()));
					} catch (ParseException e) {
						throw new RuntimeException(e);
					}
				}

				customerMasterModel.setSynced(true);
				customerMasterModel.setActive(customerMasterData.getActive());

				modelService.save(customerMasterModel);

				if (customerMasterData.getBillToErpAddressId() != null && !customerMasterData.getBillToErpAddressId().isEmpty()) {
					boolean isExistingBillingAddress = true;
					AddressModel billingAddressModel = slctCrmIntegrationDao.getAddressByErpAddressId(customerMasterData.getBillToErpAddressId());
					if (Objects.isNull(billingAddressModel)) {
						billingAddressModel = modelService.create(AddressModel.class);
						billingAddressModel.setErpAddressId(customerMasterData.getBillToErpAddressId());
						billingAddressModel.setOwner(customerMasterModel);
						isExistingBillingAddress = false;
					}
					billingAddressModel.setAccountName(customerMasterData.getBillToAccountName());
					billingAddressModel.setFirstname(customerMasterData.getBillToFirstName());
					billingAddressModel.setStreetname(customerMasterData.getBillToStreetName());
					billingAddressModel.setStreetnumber(customerMasterData.getBillToStreetNumber());
					billingAddressModel.setState(customerMasterData.getBillToState());
					billingAddressModel.setDistrict(customerMasterData.getBillToDistrict());
					billingAddressModel.setTaluka(customerMasterData.getBillToTaluka());
					billingAddressModel.setErpCity(customerMasterData.getBillToErpCity());
					billingAddressModel.setPostalcode(customerMasterData.getBillToPostalCard());
					billingAddressModel.setCellphone(customerMasterData.getBillToCellPhone());
					billingAddressModel.setEmail(customerMasterData.getBillToEmail());
					billingAddressModel.setIsPrimaryAddress(customerMasterData.getBillTOIsPrimaryAddress());
					billingAddressModel.setBillingAddress(customerMasterData.getBillToIsBillingAddress());
					billingAddressModel.setShippingAddress(customerMasterData.getBillToIsShippingAddress());
					billingAddressModel.setDuplicate(customerMasterData.getBillToIsDuplicate());
					if (customerMasterData.getBillToCountry() != null && !customerMasterData.getBillToCountry().isEmpty()) {
						CountryModel countryModel = slctCrmIntegrationDao.getCountryByIsoCode(customerMasterData.getBillToCountry());
						if (countryModel != null) {
							billingAddressModel.setCountry(countryModel);
						}
					}
					billingAddressModel.setVisibleInAddressBook(customerMasterData.getBillToIsVisibleInAddressBook());

					modelService.save(billingAddressModel);
					if (!isExistingBillingAddress) {
						customerAccountService.saveAddressEntry(customerMasterModel, billingAddressModel);
					}
				}

				if (customerMasterData.getShipToErpAddressId() != null && !customerMasterData.getShipToErpAddressId().isEmpty()) {
					boolean isExistingShippingAddress = true;
					AddressModel shippingAddressModel = slctCrmIntegrationDao.getAddressByErpAddressId(customerMasterData.getShipToErpAddressId());
					if (Objects.isNull(shippingAddressModel)) {
						shippingAddressModel = modelService.create(AddressModel.class);
						shippingAddressModel.setErpAddressId(customerMasterData.getShipToErpAddressId());
						shippingAddressModel.setOwner(customerMasterModel);
						isExistingShippingAddress = false;
					}
					shippingAddressModel.setAccountName(customerMasterData.getShipToAccountName());
					shippingAddressModel.setFirstname(customerMasterData.getShipToFirstName());
					shippingAddressModel.setStreetname(customerMasterData.getShipToStreetName());
					shippingAddressModel.setStreetnumber(customerMasterData.getShipToStreetNumber());
					shippingAddressModel.setState(customerMasterData.getShipToState());
					shippingAddressModel.setDistrict(customerMasterData.getShipToDistrict());
					shippingAddressModel.setTaluka(customerMasterData.getShipToTaluka());
					shippingAddressModel.setErpCity(customerMasterData.getShipToErpCity());
					shippingAddressModel.setPostalcode(customerMasterData.getShipToPostalCard());
					shippingAddressModel.setCellphone(customerMasterData.getShipToCellPhone());
					shippingAddressModel.setEmail(customerMasterData.getShipToEmail());
					shippingAddressModel.setIsPrimaryAddress(customerMasterData.getShipTOIsPrimaryAddress());
					shippingAddressModel.setBillingAddress(customerMasterData.getShipToIsBillingAddress());
					shippingAddressModel.setShippingAddress(customerMasterData.getShipToIsShippingAddress());
					shippingAddressModel.setDuplicate(customerMasterData.getShipToIsDuplicate());
					if (customerMasterData.getShipToCountry() != null && !customerMasterData.getShipToCountry().isEmpty()) {
						CountryModel countryModel = slctCrmIntegrationDao.getCountryByIsoCode(customerMasterData.getShipToCountry());
						if (countryModel != null) {
							shippingAddressModel.setCountry(countryModel);
						}
					}
					shippingAddressModel.setVisibleInAddressBook(customerMasterData.getShipToIsVisibleInAddressBook());
					modelService.save(shippingAddressModel);
					if (!isExistingShippingAddress) {
						customerAccountService.saveAddressEntry(customerMasterModel, shippingAddressModel);
					}
				}

				if (customerMasterData.getTaluka() != null && customerMasterData.getDistrict() != null && !customerMasterData.getTaluka().isEmpty() && !customerMasterData.getDistrict().isEmpty()) {
					SubAreaMasterModel subAreaMasterModel = slctCrmIntegrationDao.getSubAreaByTalukaAndDistrict(customerMasterData.getTaluka(), customerMasterData.getDistrict());
					if (Objects.isNull(subAreaMasterModel)) {
						subAreaMasterModel = modelService.create(SubAreaMasterModel.class);
						subAreaMasterModel.setTaluka(customerMasterData.getTaluka());
						subAreaMasterModel.setDistrict(customerMasterData.getDistrict());
						modelService.save(subAreaMasterModel);
					}

					CustomerSubAreaMappingModel customerSubAreaMappingModel = slctCrmIntegrationDao.getCustomerSubAreaMapByCustomer(customerMasterModel);
					if (Objects.isNull(customerSubAreaMappingModel)) {
						customerSubAreaMappingModel = modelService.create(CustomerSubAreaMappingModel.class);
						customerSubAreaMappingModel.setEyDmsCustomer(customerMasterModel);
					}
					customerSubAreaMappingModel.setSubArea(customerMasterData.getTaluka());
					customerSubAreaMappingModel.setDistrict(customerMasterData.getDistrict());
					customerSubAreaMappingModel.setState(customerMasterData.getState());
					customerSubAreaMappingModel.setBrand((CMSSiteModel) baseSiteService.getBaseSiteForUID(customerMasterData.getBrand()));
					customerSubAreaMappingModel.setSubAreaMaster(subAreaMasterModel);
					customerSubAreaMappingModel.setCounterType(customerMasterData.getCustomerType());
					customerSubAreaMappingModel.setIsOtherBrand(false);
					customerSubAreaMappingModel.setIsActive(customerMasterData.getActive());
					modelService.save(customerSubAreaMappingModel);

					RouteMasterModel routeMasterModel = slctCrmIntegrationDao.getRouteMaster(customerMasterData.getTaluka(), customerMasterData.getDistrict(), customerMasterData.getState(), customerMasterData.getBrand());
					String stateCode = slctCrmIntegrationDao.getStateCode(customerMasterData.getState());
					if (stateCode != null && !stateCode.isEmpty() && Objects.isNull(routeMasterModel)) {
						routeMasterModel = modelService.create(RouteMasterModel.class);
						String routeId = customerMasterData.getTaluka() + "_ALL_" + customerMasterData.getBrand() + "_" + stateCode + "_" + customerMasterData.getDistrict();
						routeMasterModel.setRouteId(routeId);
						routeMasterModel.setRouteName(routeId);
						routeMasterModel.setSubArea(customerMasterData.getTaluka());
						routeMasterModel.setDistrict(customerMasterData.getDistrict());
						routeMasterModel.setState(customerMasterData.getState());
						routeMasterModel.setBrand(customerMasterData.getBrand());
						routeMasterModel.setSubAreaMaster(subAreaMasterModel);
						routeMasterModel.setIsDefaultRoute(true);
						modelService.save(routeMasterModel);
					}

					CounterRouteMappingModel counterRouteMappingModel = slctCrmIntegrationDao.getCounterRouteMapping(customerMasterModel.getUid());
					if (Objects.isNull(counterRouteMappingModel)) {
						counterRouteMappingModel = modelService.create(CounterRouteMappingModel.class);
						counterRouteMappingModel.setCounterCode(customerMasterModel.getUid());
					}
					counterRouteMappingModel.setBrand(baseSiteService.getBaseSiteForUID(customerMasterData.getBrand()));
					counterRouteMappingModel.setRoute(routeMasterModel.getRouteId());
					counterRouteMappingModel.setRouteName(routeMasterModel.getRouteName());
					counterRouteMappingModel.setTaluka(customerMasterData.getTaluka());
					counterRouteMappingModel.setDistrict(customerMasterData.getDistrict());
					counterRouteMappingModel.setState(customerMasterData.getState());
					counterRouteMappingModel.setIsOtherBrand(false);
					if (customerMasterData.getCustomerCategory().equals("DEALER")) {
						counterRouteMappingModel.setCounterType("Dealer");
					} else if (customerMasterData.getCustomerCategory().equals("RETAILER")) {
						counterRouteMappingModel.setCounterType("Retailer");
					}
					counterRouteMappingModel.setIsCreatedFromSlctIntegration(true);
					modelService.save(counterRouteMappingModel);

				}
			}
		}
		return true;
	}

	@Override
	public List<MonthlySalesModel> getMonthlySaleModel(String monthYear) {
		return slctCrmIntegrationDao.getMonthlySalesModel(monthYear);
	}

	@Override
	public List<DealerPlannedMonthlySalesModel> getDealerPlannedMonthlySalesDetails(List<SubAreaMasterModel> subAreaMasterList, EyDmsUserModel salesOfficer, String monthName, String monthYear) {
		return slctCrmIntegrationDao.getDealerPannedMonthlySalesDetails(subAreaMasterList, salesOfficer, monthName, monthYear);
	}

	@Override
	public boolean insertUpdateGeographicalMasterDetails(SlctGeographicalMasterListData slctGeographicalMasterListData) {
		List<SlctGeographicalMasterData> geographicalMasterDataList = slctGeographicalMasterListData.getGeographicalMasterData();
		List<SubAreaMasterModel> subAreaMasterModelList = new ArrayList<>();
		List<GeographicalMasterModel> geographicalMasterModelList = new ArrayList<>();
		for (SlctGeographicalMasterData geographicalMasterData : geographicalMasterDataList) {
			if (geographicalMasterData.getTaluka() != null && !geographicalMasterData.getTaluka().isEmpty() && geographicalMasterData.getDistrict() != null && !geographicalMasterData.getDistrict().isEmpty()) {
				SubAreaMasterModel subAreaMasterModel = slctCrmIntegrationDao.getSubAreaByTalukaAndDistrict(geographicalMasterData.getTaluka(), geographicalMasterData.getDistrict());
				if (Objects.isNull(subAreaMasterModel)) {
					subAreaMasterModel = modelService.create(SubAreaMasterModel.class);
					subAreaMasterModel.setTaluka(geographicalMasterData.getTaluka());
					subAreaMasterModel.setDistrict(geographicalMasterData.getDistrict());
					modelService.save(subAreaMasterModel);
				}
				if (geographicalMasterData.getCity() != null && !geographicalMasterData.getCity().isEmpty() && geographicalMasterData.getState() != null && !geographicalMasterData.getState().isEmpty()) {
					GeographicalMasterModel geographicalMasterModel = slctCrmIntegrationDao.getGeographicalMasterModel(geographicalMasterData.getTaluka(), geographicalMasterData.getDistrict(), geographicalMasterData.getCity(), geographicalMasterData.getState());
					if (Objects.isNull(geographicalMasterModel)) {
						geographicalMasterModel = modelService.create(GeographicalMasterModel.class);
						geographicalMasterModel.setTaluka(geographicalMasterData.getTaluka());
						geographicalMasterModel.setDistrict(geographicalMasterData.getDistrict());
						geographicalMasterModel.setErpCity(geographicalMasterData.getCity());
						geographicalMasterModel.setState(geographicalMasterData.getState());
						geographicalMasterModel.setGeographicalState(geographicalMasterData.getState());
						modelService.save(geographicalMasterModel);
					}
				}
			}
		}

		return true;
	}

	@Override
	public boolean insertUpdateFreightAndIncoTermsMasterDetails(SlctFreightAndIncoTermsListData slctFreightAndIncoTermsListData) {
		List<SlctFreightAndIncoTermsData> freightAndIncoTermsDataList = slctFreightAndIncoTermsListData.getFreightAndIncoTermsData();
		List<FreightAndIncoTermsMasterModel> freightAndIncoTermsMasterModelList = new ArrayList<>();
		for (SlctFreightAndIncoTermsData freightAndIncoTermsData : freightAndIncoTermsDataList) {
			if (freightAndIncoTermsData.getBrand() != null && !freightAndIncoTermsData.getBrand().isEmpty() && freightAndIncoTermsData.getState() != null && !freightAndIncoTermsData.getState().isEmpty() && freightAndIncoTermsData.getDistrict() != null && !freightAndIncoTermsData.getDistrict().isEmpty() && freightAndIncoTermsData.getOrgType() != null && !freightAndIncoTermsData.getOrgType().isEmpty()) {
				FreightAndIncoTermsMasterModel freightAndIncoTermsMasterModel = slctCrmIntegrationDao.getFreightAndIncoTermsMasterModel(freightAndIncoTermsData.getBrand(), freightAndIncoTermsData.getDistrict(), freightAndIncoTermsData.getState(), freightAndIncoTermsData.getOrgType());
				if (Objects.isNull(freightAndIncoTermsMasterModel)) {
					freightAndIncoTermsMasterModel = modelService.create(FreightAndIncoTermsMasterModel.class);
					freightAndIncoTermsMasterModel.setBrand(freightAndIncoTermsData.getBrand());
					freightAndIncoTermsMasterModel.setDistrict(freightAndIncoTermsData.getDistrict());
					freightAndIncoTermsMasterModel.setState(freightAndIncoTermsData.getState());
					freightAndIncoTermsMasterModel.setOrgType(freightAndIncoTermsData.getOrgType());
				}
				freightAndIncoTermsMasterModel.setIncoTerms(freightAndIncoTermsData.getIncoTerms());
				freightAndIncoTermsMasterModel.setFrieghtTerms(freightAndIncoTermsData.getFreightTerms());
				modelService.save(freightAndIncoTermsMasterModel);
			}
		}

		return true;
	}

	@Override
	public boolean insertUpdateProductMasterDetails(SlctProductMasterListData slctProductMasterListData) {
		List<SlctProductMasterData> productMasterDataList = slctProductMasterListData.getProductMasterData();
		for (SlctProductMasterData productMasterData : productMasterDataList) {
			if (productMasterData.getBrand() != null && !productMasterData.getBrand().isEmpty() && productMasterData.getCode() != null && !productMasterData.getCode().isEmpty()) {
				CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(productMasterData.getBrand() + "ProductCatalog", "Staged");
				ProductModel productModel = productService.getProductForCode(catalogVersion, productMasterData.getCode());
				if (Objects.isNull(productModel)) {
					catalogVersion = catalogVersionService.getCatalogVersion(productMasterData.getBrand() + "ProductCatalog", "Staged");
					productModel = modelService.create(ProductModel.class);
					productModel.setCatalogVersion(catalogVersion);
					productModel.setCode(productMasterData.getCode());
				}
				if (productMasterData.getState() != null && !productMasterData.getState().isEmpty()) {
					productModel.setState(productMasterData.getState());
				}
				if (productMasterData.getCustCategory() != null && !productMasterData.getCustCategory().isEmpty()) {
					productModel.setCustCategory(productMasterData.getCustCategory());
				}
				if (productMasterData.getInventoryId() != null && !productMasterData.getInventoryId().isEmpty()) {
					productModel.setInventoryId(productMasterData.getInventoryId());
				}
				if (productMasterData.getGrade() != null && !productMasterData.getGrade().isEmpty()) {
					productModel.setGrade(productMasterData.getGrade());
				}
				if (productMasterData.getPackagingCondition() != null && !productMasterData.getPackagingCondition().isEmpty()) {
					productModel.setPackagingCondition(productMasterData.getPackagingCondition());
				}
				if (productMasterData.getBagType() != null && !productMasterData.getBagType().isEmpty()) {
					productModel.setBagType(productMasterData.getBagType());
				}
				if (productMasterData.getProductName() != null && !productMasterData.getProductName().isEmpty()) {
					productModel.setName(productMasterData.getProductName());
				}
				if (productMasterData.getPremium() != null && !productMasterData.getPremium().isEmpty()) {
					productModel.setPremium(productMasterData.getPremium());
				}
				modelService.save(productModel);

			}
		}
		return true;
	}

	@Override
	public boolean insertUpdateSalesOrderDeliverySlaDetails(SlctOrderDeliverySlaListData slctOrderDeliverySlaListData) {
		List<SlctOrderDeliverySlaData> slctOrderDeliverySlaDataList = slctOrderDeliverySlaListData.getSalesOrderDeliverySLADetails();
		List<SalesOrderDeliverySLAModel> salesOrderDeliverySLAModelList = new ArrayList<>();
		for (SlctOrderDeliverySlaData slaData : slctOrderDeliverySlaDataList) {
			if (slaData.getRoute() != null && !slaData.getRoute().isEmpty()) {
				SalesOrderDeliverySLAModel slaModel = slctCrmIntegrationDao.getDeliverySlaModel(slaData.getRoute());
				if (Objects.isNull(slaModel)) {
					slaModel = modelService.create(SalesOrderDeliverySLAModel.class);
					slaModel.setRoute(slaData.getRoute());
				}
				if (slaData.getSourceCity() != null && !slaData.getSourceCity().isEmpty()) {
					slaModel.setSourceCity(slaData.getSourceCity());
				}
				if (slaData.getDestinationCity() != null && !slaData.getDestinationCity().isEmpty()) {
					slaModel.setDestinationCity(slaData.getDestinationCity());
				}
				if (slaData.getDestinationDistrict() != null && !slaData.getDestinationDistrict().isEmpty()) {
					slaModel.setDestinationDistrict(slaData.getDestinationDistrict());
				}
				if (slaData.getDestinationPinCode() != null && !slaData.getDestinationPinCode().isEmpty()) {
					slaModel.setDestinationPincode(slaData.getDestinationPinCode());
				}
				if (slaData.getPackType() != null && !slaData.getPackType().isEmpty()) {
					slaModel.setPackType(slaData.getPackType());
				}
				if (slaData.getGrade() != null && !slaData.getGrade().isEmpty()) {
					slaModel.setGrade(slaData.getGrade());
				}
				if (slaData.getCustCategory() != null && !slaData.getCustCategory().isEmpty() && CustomerCategory.valueOf(slaData.getCustCategory()) != null) {
					slaModel.setCustomerCategory(CustomerCategory.valueOf(slaData.getCustCategory()));
				}
				if (slaData.getCommonTruckCapacity() != null) {
					slaModel.setCommonTruckCapacity(slaData.getCommonTruckCapacity());
				}
				if (slaData.getDeliverySlaHour() != null) {
					slaModel.setDeliverySlaHour(slaData.getDeliverySlaHour());
				}
				if (slaData.getDispatchSlaHour() != null) {
					slaModel.setDispatchSlaHour(slaData.getDispatchSlaHour());
				}
				slaModel.setNoEntryRestrictionStart1(slaData.getNoEntryRestrictionStart1());
				slaModel.setNoEntryRestrictionEnd1(slaData.getNoEntryRestrictionEnd1());
				slaModel.setNoEntryRestrictionStart2(slaData.getNoEntryRestrictionStart2());
				slaModel.setNoEntryRestrictionEnd2(slaData.getNoEntryRestrictionEnd2());
				slaModel.setNoEntryRestrictionStart3(slaData.getNoEntryRestrictionStart3());
				slaModel.setNoEntryRestrictionEnd3(slaData.getNoEntryRestrictionEnd3());
				slaModel.setNoEntryRestrictionStart4(slaData.getNoEntryRestrictionStart4());
				slaModel.setNoEntryRestrictionEnd4(slaData.getNoEntryRestrictionEnd4());
				slaModel.setIsModifiedFromSlctIntegration(true);
				modelService.save(slaModel);
			}
		}

		return true;
	}

	@Override
	public boolean insertWarehouseDetails(SlctWarehouseListData slctWarehouseListData) {
		List<SlctWarehouseData> warehouseDataList = slctWarehouseListData.getWarehouseCodes();
		for (SlctWarehouseData warehouseData : warehouseDataList) {
			if (warehouseData != null && warehouseData.getCode() != null && !warehouseData.getCode().isEmpty()) {
				WarehouseModel warehouseModel = slctCrmIntegrationDao.findWarehouseByCode(warehouseData.getCode());
				if (Objects.isNull(warehouseModel)) {
					warehouseModel = modelService.create(WarehouseModel.class);
					warehouseModel.setCode(warehouseData.getCode());
					warehouseModel.setName(warehouseData.getName());
					VendorModel vendorModel = slctCrmIntegrationDao.findVendorByCode("default");
					warehouseModel.setVendor(vendorModel);

					if (warehouseData.getCode().startsWith("F")) {
						warehouseModel.setType(WarehouseType.PLANT);
						warehouseModel.setWorkingHourStartTime("00:00");
						warehouseModel.setWorkingHourEndTime("23:59");
					} else {
						warehouseModel.setType(WarehouseType.DEPOT);
						warehouseModel.setWorkingHourStartTime("08:00");
						warehouseModel.setWorkingHourEndTime("20:00");
					}
					warehouseModel.setOrganisationId(warehouseData.getOrganisationId());
					warehouseModel.setLocationCode(warehouseData.getLocationCode());
					warehouseModel.setDefault(true);
					warehouseModel.setActive(true);
					warehouseModel.setIsCreatedFromSlctIntegration(true);
					modelService.save(warehouseModel);
				}
			}
		}
		return true;
	}

	@Override
	public List<OrderEntryModel> getOrderLinesForEpod() {
		return slctCrmIntegrationDao.getOrderLinesForEpod();
	}

	@Override
	public List<SLCTruckReachedDateData> updateTruckReachData(SLCTruckReachedDateListData slctTruckReachedDateListData) {
		List<SLCTruckReachedDateData> truckReachedDateList = slctTruckReachedDateListData.getTruckReached();
		List<SLCTruckReachedDateData> truckReachedDateListNew = new ArrayList<SLCTruckReachedDateData>();
		DateFormat reachDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		for (SLCTruckReachedDateData truckReachedDateData : truckReachedDateList) {
			SLCTruckReachedDateData truckReachedDateDataNew = new SLCTruckReachedDateData();
			if (truckReachedDateData != null && truckReachedDateData.getErpLineItemId() != null && !truckReachedDateData.getErpLineItemId().isEmpty()) {
				OrderEntryModel orderEntryModel = slctCrmIntegrationDao.findOrderEntryByErpLineItemId(truckReachedDateData.getErpLineItemId());
				if (null != orderEntryModel) {
					try {
						orderEntryModel.setTruckReachedDate(reachDateFormat.parse(truckReachedDateData.getTruckReachedDate()));
					} catch (ParseException e) {
						throw new RuntimeException(e);
					}

					if (orderEntryModel.getTruckDispatcheddate() != null) {
						orderEntryModel.setStatus(OrderStatus.TRUCK_REACHED_DESTINATION);
						try {
							StringBuilder builder = new StringBuilder();
							builder.append("Order no. " + orderEntryModel.getOrder().getCode() + " with product " + orderEntryModel.getProduct().getName() + " and quantity " + orderEntryModel.getQuantityInMT());
							builder.append(" has reached the delivery location with Truck No. " + orderEntryModel.getTruckNo() + ". Please proceed with unloading of the vehicle.");

							String body = builder.toString();
							StringBuilder builder1 = new StringBuilder();
							builder1.append("Vehicle Arrival Confirmation");

							String subject = builder1.toString();

							NotificationCategory category = NotificationCategory.VEHICLE_ARRIVAL_CONFIRMATION;
							eydmsNotificationService.submitOrderEntryNotification(orderEntryModel.getOrder(), (B2BCustomerModel) orderEntryModel.getOrder().getUser(), body, subject, category, orderEntryModel.getEntryNumber());

							EyDmsUserModel so = territoryService.getSOforCustomer((EyDmsCustomerModel) orderEntryModel.getOrder().getUser());
							eydmsNotificationService.submitOrderEntryNotification(orderEntryModel.getOrder(), so, body, subject, category, orderEntryModel.getEntryNumber());

							EyDmsCustomerModel sp = territoryService.getSpForCustomerAndBrand((EyDmsCustomerModel) orderEntryModel.getOrder().getUser(), orderEntryModel.getOrder().getSite());
							eydmsNotificationService.submitOrderEntryNotification(orderEntryModel.getOrder(), sp, body, subject, category, orderEntryModel.getEntryNumber());

						} catch (Exception e) {
							LOG.error("Error while sending Vehicle Arrival Confirmation Notification");
						}
					}

					modelService.save(orderEntryModel);
					truckReachedDateDataNew.setErpLineItemId(truckReachedDateData.getErpLineItemId());
					truckReachedDateDataNew.setTruckReachedDate(truckReachedDateData.getTruckReachedDate());
					truckReachedDateDataNew.setRecordUpdatedStatus(true);
					truckReachedDateListNew.add(truckReachedDateDataNew);
				}
			} else {
				truckReachedDateDataNew.setErpLineItemId(truckReachedDateData.getErpLineItemId());
				truckReachedDateDataNew.setTruckReachedDate(truckReachedDateData.getTruckReachedDate());
				truckReachedDateDataNew.setRecordUpdatedStatus(false);
				truckReachedDateListNew.add(truckReachedDateDataNew);
			}
		}
		return truckReachedDateListNew;
	}

	@Override
	public boolean removeExistingDestinationSourceMasterData() {
		List<DestinationSourceMasterModel> existingDestinationSourceMasterModels = slctCrmIntegrationDao.getAllDestinationSourceRecords();
		if (existingDestinationSourceMasterModels.isEmpty()) {
			return false;
		}
		modelService.removeAll(existingDestinationSourceMasterModels);
		return true;
	}

	@Override
	public boolean insertDestinationSourceData(LPSourceMasterListData lpSourceMasterListData) {
		List<LPSourceMasterData> lpSourceMasterDataList = lpSourceMasterListData.getLpSourceMaster();
		for (LPSourceMasterData lpSourceMasterData : lpSourceMasterDataList) {
			//Insertion into DestinationSourceMaster
			DestinationSourceMasterModel destinationSourceMasterModel = modelService.create(DestinationSourceMasterModel.class);

			destinationSourceMasterModel.setOrderType(OrderType.valueOf(lpSourceMasterData.getOrderType()));
			if (baseSiteService.getBaseSiteForUID(lpSourceMasterData.getBrand()) != null) {
				destinationSourceMasterModel.setBrand(baseSiteService.getBaseSiteForUID(lpSourceMasterData.getBrand()));
			}
			destinationSourceMasterModel.setCustomerCategory(CustomerCategory.valueOf(lpSourceMasterData.getCustCategory()));
			if (slctCrmIntegrationDao.getDeliveryMode(lpSourceMasterData.getDeliveryMode()) != null) {
				destinationSourceMasterModel.setDeliveryMode(slctCrmIntegrationDao.getDeliveryMode(lpSourceMasterData.getDeliveryMode()));
			}
			destinationSourceMasterModel.setGrade(lpSourceMasterData.getGrade());
			destinationSourceMasterModel.setPackaging(lpSourceMasterData.getPackaging());
			destinationSourceMasterModel.setDestinationTaluka(lpSourceMasterData.getDestTaluka());
			destinationSourceMasterModel.setDestinationDistrict(lpSourceMasterData.getDestDistrict());
			destinationSourceMasterModel.setDestinationCity(lpSourceMasterData.getDestCity());
			destinationSourceMasterModel.setDestinationCityId(lpSourceMasterData.getDestCityId());
			destinationSourceMasterModel.setDestinationState(lpSourceMasterData.getDestState());
			destinationSourceMasterModel.setSourceTaluka(lpSourceMasterData.getSourceTaluka());
			destinationSourceMasterModel.setSourceDistrict(lpSourceMasterData.getSourceDistrict());
			destinationSourceMasterModel.setSourceCity(lpSourceMasterData.getSourceCity());
			destinationSourceMasterModel.setSourceState(lpSourceMasterData.getSourceState());
			destinationSourceMasterModel.setSource(slctCrmIntegrationDao.findWarehouseByCode(lpSourceMasterData.getSource()));
			destinationSourceMasterModel.setNcrCost(lpSourceMasterData.getNcrCost());
			destinationSourceMasterModel.setSourcePriority(lpSourceMasterData.getSourcePriority());
			destinationSourceMasterModel.setRoute(lpSourceMasterData.getRoute());
			destinationSourceMasterModel.setTlcPerMT(lpSourceMasterData.getTlcPerMT());
			destinationSourceMasterModel.setContributionPerMT(lpSourceMasterData.getContributionPerMT());
			destinationSourceMasterModel.setType(WarehouseType.valueOf(lpSourceMasterData.getType()));
			destinationSourceMasterModel.setSecondaryRoute(lpSourceMasterData.getSecondaryRoute());
			destinationSourceMasterModel.setDistance(lpSourceMasterData.getDistance());
			modelService.save(destinationSourceMasterModel);

		}
		return true;
	}

	@Override
	public boolean removeExistingLpSourceMasterDataThroughSqlQuery() {
		SqlSearchResultData searchResult;
		LOG.info("Deletion of all LP Source Data");
		Calendar calendar = Calendar.getInstance();
		//Returns current time in millis
		long timeMilli1 = calendar.getTimeInMillis();
		LOG.info("Start time in milliseconds in LP Source Data:" + timeMilli1);
		try {
			String query = "delete from destinationsource";
			HacFlexibleSearchFacade flexibleSearchFacade = new HacFlexibleSearchFacade();
			searchResult = flexibleSearchFacade.executeRawSql(query, 2000000, true);

		} catch (Exception e) {
			LOG.info("Exception Message in LP Source Data" + e.getMessage());
			throw new RuntimeException(e);
		}
		long timeMilli2 = calendar.getTimeInMillis();
		LOG.info("End time in milliseconds in LP Source Data:" + timeMilli2);
		LOG.info("Time taken in milliseconds in LP Source Data:" + (timeMilli2 - timeMilli1));
		return true;
	}

	@Override
	public boolean removeExistingDepotSubAreaMappingData() {
		SqlSearchResultData searchResult1;
		LOG.info("Deletion of all Depot Sub Area Mapping Data");
		Calendar calendar = Calendar.getInstance();
		//Returns current time in millis
		long timeMilli1 = calendar.getTimeInMillis();
		LOG.info("Start time in milliseconds in Depot Sub Area Mapping Data:" + timeMilli1);
		try {
			String query1 = "delete from depotsubareamapping";
			HacFlexibleSearchFacade flexibleSearchFacade1 = new HacFlexibleSearchFacade();
			searchResult1 = flexibleSearchFacade1.executeRawSql(query1, 2000000, true);

		} catch (Exception e) {
			LOG.info("Exception Message in Depot Sub Area Mapping Data" + e.getMessage());
			throw new RuntimeException(e);
		}
		long timeMilli2 = calendar.getTimeInMillis();
		LOG.info("End time in milliseconds in Depot Sub Area Mapping Data:" + timeMilli2);
		LOG.info("Time taken in milliseconds in Depot SubArea Mapping Data:" + (timeMilli2 - timeMilli1));
		return true;
	}

	public static String formatIndianNumber(double number) {
		if (number < 100000) {
			return String.format("%.0f", number);
		} else {
			int quotient = (int) (number / 100000);
			int remainder = (int) (number % 100000);
			return String.format("%d,%02d,%03d", quotient, (remainder / 1000), (remainder % 1000));
		}
	}


	@Override
	public boolean insertUpdateDepotSubAreaMappingDetails(SlctDepotSubAreaMappingListData slctDepotSubAreaMappingListData) {

		List<SlctDepotSubAreaMappingData> depotSubAreaMappingList = slctDepotSubAreaMappingListData.getDepotSubAreaMappingList();
		for (SlctDepotSubAreaMappingData slctDepotSubAreaMappingData : depotSubAreaMappingList) {
			if (slctDepotSubAreaMappingData.getDepotType().equals("DEPOT") || slctDepotSubAreaMappingData.getDepotType().equals("depot") || slctDepotSubAreaMappingData.getDepotType().equals("DEPO")) {
				DepotSubAreaMappingModel depotSubAreaMappingModel = modelService.create(DepotSubAreaMappingModel.class);

				depotSubAreaMappingModel.setDepot(slctCrmIntegrationDao.findWarehouseByCode(slctDepotSubAreaMappingData.getDepot()));
				depotSubAreaMappingModel.setState(slctDepotSubAreaMappingData.getDestState());
				depotSubAreaMappingModel.setDistrict(slctDepotSubAreaMappingData.getDestDistrict());
				depotSubAreaMappingModel.setSubArea(slctDepotSubAreaMappingData.getDestTaluka());

				if (slctDepotSubAreaMappingData.getDestTaluka() != null && !slctDepotSubAreaMappingData.getDestTaluka().isEmpty() && slctDepotSubAreaMappingData.getDestDistrict() != null && !slctDepotSubAreaMappingData.getDestDistrict().isEmpty()) {
					SubAreaMasterModel subAreaMasterModel = slctCrmIntegrationDao.getSubAreaByTalukaAndDistrict(slctDepotSubAreaMappingData.getDestTaluka(), slctDepotSubAreaMappingData.getDestDistrict());
					if (subAreaMasterModel != null) {
						depotSubAreaMappingModel.setSubAreaMaster(subAreaMasterModel);
					} else {
						subAreaMasterModel = modelService.create(SubAreaMasterModel.class);
						subAreaMasterModel.setDistrict(slctDepotSubAreaMappingData.getDestDistrict());
						subAreaMasterModel.setTaluka(slctDepotSubAreaMappingData.getDestTaluka());
						modelService.save(subAreaMasterModel);
						depotSubAreaMappingModel.setSubAreaMaster(subAreaMasterModel);
					}
				}

				if (baseSiteService.getBaseSiteForUID(slctDepotSubAreaMappingData.getBrand()) != null) {
					depotSubAreaMappingModel.setBrand(baseSiteService.getBaseSiteForUID(slctDepotSubAreaMappingData.getBrand()));
				}
				modelService.save(depotSubAreaMappingModel);
			}
		}
		return true;
	}

	@Override
	public SlctCrmIntegrationDOMasterListData insertUpdateDoMasterDetails(SlctCrmIntegrationDOMasterListData slctCrmIntegrationDOMasterListData) {
		SlctCrmIntegrationDOMasterListData doMasterResponseOutput = new SlctCrmIntegrationDOMasterListData();
		List<SlctCrmIntegrationDOMasterData> doMasterDataList = slctCrmIntegrationDOMasterListData.getDoMasters();
		List<SlctCrmIntegrationDOMasterData> doMastersReponseList = new ArrayList<SlctCrmIntegrationDOMasterData>();
		for (SlctCrmIntegrationDOMasterData doMasterData : doMasterDataList) {
			SlctCrmIntegrationDOMasterData doMasterRepsonse = new SlctCrmIntegrationDOMasterData();

			Set<PrincipalGroupModel> groups = new HashSet<>();
			EyDmsUserModel doMasterModel = slctCrmIntegrationDao.getDoByUidAndEmpCode(doMasterData.getUid().toLowerCase());
			if (Objects.isNull(doMasterModel)) {
				PrincipalGroupModel b2bCustomerGroupModel, salesOfficerGroupModel;
				doMasterModel = modelService.create(EyDmsUserModel.class);
				doMasterModel.setUid(doMasterData.getUid().toLowerCase());
				doMasterModel.setEmail(doMasterData.getEmail().toLowerCase());
				doMasterModel.setEmployeeCode(doMasterData.getEmployeeCode());
				b2bCustomerGroupModel = slctCrmIntegrationDao.getPrincipalGroupByUid("b2bcustomergroup");
				salesOfficerGroupModel = slctCrmIntegrationDao.getPrincipalGroupByUid("salesofficergroup");
				groups.add(b2bCustomerGroupModel);
				groups.add(salesOfficerGroupModel);
				doMasterModel.setGroups(groups);
				doMasterModel.setIsCreatedFromSlctIntegration(true);
			}
			if (doMasterData.getBrand() != null && !doMasterData.getBrand().isEmpty()) {
				if (doMasterData.getBrand().equals("102")) {
					doMasterModel.setDefaultB2BUnit(defaultB2BUnitService.getUnitForUid("EyDmsShreeUnit"));
				} else if (doMasterData.getBrand().equals("103")) {
					doMasterModel.setDefaultB2BUnit(defaultB2BUnitService.getUnitForUid("EyDmsBangurUnit"));
				} else if (doMasterData.getBrand().equals("104")) {
					doMasterModel.setDefaultB2BUnit(defaultB2BUnitService.getUnitForUid("EyDmsRockstrongUnit"));
				}
			}
			doMasterModel.setUserType(EyDmsUserType.SO);
			doMasterModel.setName(doMasterData.getName());
			doMasterModel.setMobileNumber(doMasterData.getMobileNumber());
			doMasterModel.setState(doMasterData.getState());
			doMasterModel.setSessionCurrency(slctCrmIntegrationDao.getCurrencyModelByISOCode("INR"));
			modelService.save(doMasterModel);
			if (doMasterData.getActive().equals("N")) {
				doMasterModel.setLoginDisabled(true);
				doMasterModel.setActive(false);
			} else if (doMasterData.getActive().equals("Y")) {
				String password = "Qaz1@3ws%x";
				doMasterModel.setPassword(password);
				doMasterModel.setLoginDisabled(false);
				doMasterModel.setActive(true);
			}
			doMasterModel.setIsModifiedFromSlctIntegration(true);
			modelService.save(doMasterModel);
			doMasterRepsonse.setUid(doMasterModel.getUid());
			doMasterRepsonse.setCrmUpdated("Y");
			doMastersReponseList.add(doMasterRepsonse);

		}
		doMasterResponseOutput.setDoMasters(doMastersReponseList);
		return doMasterResponseOutput;
	}

	@Override
	public SlctDOSubAreaMappingListData insertUpdateDoSubAreaMapping(SlctDOSubAreaMappingListData slctDOSubAreaMappingListData) {
		SlctDOSubAreaMappingListData doSubAreaMappingResponseOutput = new SlctDOSubAreaMappingListData();
		List<SlctDOSubAreaMappingData> doSubAreaMappingDataList = slctDOSubAreaMappingListData.getDoSubAreaMappingList();
		List<SlctDOSubAreaMappingData> doSubAreaMappingResponseList = new ArrayList<>();
		for (SlctDOSubAreaMappingData doSubAreaMappingData : doSubAreaMappingDataList) {
			SlctDOSubAreaMappingData doSubAreaMappingResponse = new SlctDOSubAreaMappingData();

			SubAreaMasterModel subAreaMasterModel = slctCrmIntegrationDao.getSubAreaByTalukaAndDistrict(doSubAreaMappingData.getSubArea(), doSubAreaMappingData.getDistrict());
			if (Objects.isNull(subAreaMasterModel)) {
				subAreaMasterModel = modelService.create(SubAreaMasterModel.class);
				subAreaMasterModel.setTaluka(doSubAreaMappingData.getSubArea());
				subAreaMasterModel.setDistrict(doSubAreaMappingData.getDistrict());
				modelService.save(subAreaMasterModel);
			}

			UserSubAreaMappingModel doSubAreaMappingModel = slctCrmIntegrationDao.getDoSubAreaMappingUid(doSubAreaMappingData.getSoUid(), doSubAreaMappingData.getBrand(), subAreaMasterModel, doSubAreaMappingData.getState());
			if (Objects.isNull(doSubAreaMappingModel)) {
				doSubAreaMappingModel = modelService.create(UserSubAreaMappingModel.class);

				EyDmsUserModel doMaster = slctCrmIntegrationDao.getDoByUidAndEmpCode(doSubAreaMappingData.getSoUid().toLowerCase());
				doSubAreaMappingModel.setEyDmsUser(doMaster);
				doSubAreaMappingModel.setSubAreaMaster(subAreaMasterModel);
				doSubAreaMappingModel.setBrand((CMSSiteModel) baseSiteService.getBaseSiteForUID(doSubAreaMappingData.getBrand()));
				doSubAreaMappingModel.setState(doSubAreaMappingData.getState());
				doSubAreaMappingModel.setSubArea(doSubAreaMappingData.getSubArea());
				doSubAreaMappingModel.setDistrict(doSubAreaMappingData.getDistrict());
				doSubAreaMappingModel.setIsCreatedFromSlctIntegration(true);
			}

			if (doSubAreaMappingData.getIsActive().equals("Y")) {
				doSubAreaMappingModel.setIsActive(true);
			} else if (doSubAreaMappingData.getIsActive().equals("N")) {
				doSubAreaMappingModel.setIsActive(false);
			}
			doSubAreaMappingModel.setIsModifiedFromSlctIntegration(true);
			modelService.save(doSubAreaMappingModel);
			doSubAreaMappingResponse.setSoUid(doSubAreaMappingModel.getEyDmsUser().getUid());
			doSubAreaMappingResponse.setSoEmployeeCode(doSubAreaMappingModel.getEyDmsUser().getEmployeeCode());
			doSubAreaMappingResponse.setSubArea(doSubAreaMappingModel.getSubAreaMaster().getTaluka());
			doSubAreaMappingResponse.setDistrict(doSubAreaMappingModel.getSubAreaMaster().getDistrict());
			doSubAreaMappingResponse.setState(doSubAreaMappingModel.getState());
			doSubAreaMappingResponse.setCrmUpdated("Y");
			doSubAreaMappingResponseList.add(doSubAreaMappingResponse);
		}
		doSubAreaMappingResponseOutput.setDoSubAreaMappingList(doSubAreaMappingResponseList);
		return doSubAreaMappingResponseOutput;
	}

	@Override
	public List<List<Object>> getLeadCount(Integer year, Integer month) {
		return slctCrmIntegrationDao.getCountofLeadsGenerated(year, month);

	}

	@Override
	public boolean insertRoutesForSoDeliverySla(SlctRouteSLAListData slctRouteSLAListData) {
		List<SlctRouteSLAData> routesforSlaList = slctRouteSLAListData.getSlctRoutesForSLA();
		for (SlctRouteSLAData routeSlaData : routesforSlaList) {
			if (routeSlaData.getRoute() != null && !routeSlaData.getRoute().isEmpty()) {
				SalesOrderDeliverySLAModel slaModel = slctCrmIntegrationDao.getDeliverySlaModel(routeSlaData.getRoute());
				if (Objects.isNull(slaModel)) {
					slaModel = modelService.create(SalesOrderDeliverySLAModel.class);
					slaModel.setRoute(routeSlaData.getRoute());
				}
				if (routeSlaData.getCustCategory() != null && !routeSlaData.getCustCategory().isEmpty() && CustomerCategory.valueOf(routeSlaData.getCustCategory()) != null) {
					slaModel.setCustomerCategory(CustomerCategory.valueOf(routeSlaData.getCustCategory()));
				}
				slaModel.setCommonTruckCapacity(routeSlaData.getCommonTruckCapacity());
				slaModel.setDeliverySlaHour(routeSlaData.getDeliverySlaHour());
				slaModel.setDispatchSlaHour(routeSlaData.getDispatchSlaHour());
				slaModel.setIsModifiedFromSlctIntegration(true);
				modelService.save(slaModel);
			}
		}

		return true;
	}

	@Override
	public List<List<Object>> getProductSaleDetails(EyDmsUserModel so, String monthName, String monthYear, List<SubAreaMasterModel> subAreaMasterModelList, DistrictMasterModel districtMaster, CMSSiteModel brand) {
		return slctCrmIntegrationDao.getProductSaleDetails(so, monthName, monthYear, subAreaMasterModelList, districtMaster, brand);
	}

	@Override
	public SalesPlanningTopDownIntegrationListData getTopDownIntegrationDetails(SalesPlanningTopDownIntegrationListData salesPlanningTopDownIntegrationListData) {
		SalesPlanningTopDownIntegrationListData topDownResultOutput = new SalesPlanningTopDownIntegrationListData();
		List<SalesPlanningTopDownIntegrationData> topDownSpDataList = salesPlanningTopDownIntegrationListData.getSalesPlanningTopDownList();
		List<SalesPlanningTopDownIntegrationData> topDownSpResponseList = new ArrayList<>();
		for (SalesPlanningTopDownIntegrationData topDownIntegrationData : topDownSpDataList) {
			SubAreaMasterModel subAreaMasterModel=null;
			List<SubAreaMasterModel> subAreaMasterModelList = new ArrayList<>();
			SalesPlanningTopDownIntegrationData topDownSpResponse = new SalesPlanningTopDownIntegrationData();
			String monthName = topDownIntegrationData.getMonth() + "-" + topDownIntegrationData.getYear();
			if(topDownIntegrationData.getTaluka()!=null && topDownIntegrationData.getDistrict()!=null) {
				subAreaMasterModel = slctCrmIntegrationDao.getSubAreaByTalukaAndDistrict(topDownIntegrationData.getTaluka(),topDownIntegrationData.getDistrict());
				if(subAreaMasterModel!=null) {
					subAreaMasterModelList.add(subAreaMasterModel);
				}
			}

			List<MonthlySalesModel> monthlySalesModelsList = slctCrmIntegrationDao.getTopDownSpMonthlySales(monthName, topDownIntegrationData.getYear(), topDownIntegrationData.getDistrict(), topDownIntegrationData.getState(), topDownIntegrationData.getTaluka(), baseSiteService.getBaseSiteForUID(topDownIntegrationData.getBrand()));
//			EyDmsUserModel eydmsUserModel = slctCrmIntegrationDao.getDoByUidAndEmpCode(topDownIntegrationData.getRevisedBy());
			if (monthlySalesModelsList != null && !monthlySalesModelsList.isEmpty()) {
				for (MonthlySalesModel monthlySalesModel : monthlySalesModelsList) {
					if(monthlySalesModel.getSubAreaMasterList()!=null && !monthlySalesModel.getSubAreaMasterList().isEmpty()) {
						if(!monthlySalesModel.getSubAreaMasterList().contains(subAreaMasterModel)) {
							monthlySalesModel.setSubAreaMasterList(subAreaMasterModelList);
						}
					}
					else {
						monthlySalesModel.setSubAreaMasterList(subAreaMasterModelList);
					}

					monthlySalesModel.setRevisedByShEmpCode(topDownIntegrationData.getRevisedBy());
					monthlySalesModel.setRevisedTargetBySH(topDownIntegrationData.getRevisedTarget());
					monthlySalesModel.setCommentsBySH(topDownIntegrationData.getRemarks());
					monthlySalesModel.setIsTopDownIndicatorOn(true);
					monthlySalesModel.setPremiumTargetBySH(topDownIntegrationData.getPremiumTarget());
					monthlySalesModel.setNonPremiumTargetBySH(topDownIntegrationData.getNonPremiumTarget());
					modelService.save(monthlySalesModel);
					topDownSpResponse.setTaluka(topDownIntegrationData.getTaluka());
					topDownSpResponse.setDistrict(topDownIntegrationData.getDistrict());
					topDownSpResponse.setState(topDownIntegrationData.getState());
					topDownSpResponse.setMonth(topDownIntegrationData.getMonth());
					topDownSpResponse.setYear(topDownIntegrationData.getYear());
					topDownSpResponse.setRevisedTarget(topDownIntegrationData.getRevisedTarget());
					topDownSpResponse.setRevisedBy(topDownIntegrationData.getRevisedBy());
					topDownSpResponse.setBrand(topDownIntegrationData.getBrand());
					topDownSpResponse.setPremiumOrNonPremium(monthlySalesModel.getPremium());
					topDownSpResponse.setCrmUpdatedFlag(true);
					topDownSpResponseList.add(topDownSpResponse);
				}

			}
		}
		topDownResultOutput.setSalesPlanningTopDownList(topDownSpResponseList);
		return topDownResultOutput;
	}

	@Override
	public List<AnnualSalesModel> getAnnualSalesBottomUpData(String financialYear) {
		return slctCrmIntegrationDao.getAnnualSalesModel(financialYear);
	}

	@Override
	public SlctBrandMasterListData insertUpdateBrandMaster(SlctBrandMasterListData slctBrandMasterListData) {
		SlctBrandMasterListData brandMasterResponseOutput = new SlctBrandMasterListData();
		List<SlctBrandMasterData> brandMasterResponseList = new ArrayList<>();
		List<SlctBrandMasterData> brandMasterDataList = slctBrandMasterListData.getBrandList();
		for (SlctBrandMasterData brandMasterData : brandMasterDataList) {
			if (brandMasterData.getIsocode() != null && !brandMasterData.getIsocode().isEmpty()) {
				SlctBrandMasterData brandMasterResponseData = new SlctBrandMasterData();
				BrandModel brandModel = slctCrmIntegrationDao.getBrandByIsoCode(brandMasterData.getIsocode());
				if (Objects.isNull(brandModel)) {
					brandModel = modelService.create(BrandModel.class);
					brandModel.setIsocode(brandMasterData.getIsocode());
				}
				brandModel.setName(brandMasterData.getName());
				if (brandMasterData.getEyDmsBrand() != null && !brandMasterData.getEyDmsBrand().isEmpty()) {
					BaseSiteModel brand = baseSiteService.getBaseSiteForUID(brandMasterData.getEyDmsBrand());
					if (brand != null) {
						brandModel.setEyDmsBrand((CMSSiteModel) brand);
					}
				}
//				brandModel.setActive(true);
				modelService.save(brandModel);
				brandMasterResponseData.setIsocode(brandModel.getIsocode());
				brandMasterResponseData.setCrmUpdatedFlag("Y");
				brandMasterResponseList.add(brandMasterResponseData);
			}
		}
		brandMasterResponseOutput.setBrandList(brandMasterResponseList);
		return brandMasterResponseOutput;
	}

	@Override
	public SlctCompetitorProductMasterListData insertUpdateCompetitorProductMaster(SlctCompetitorProductMasterListData slctCompetitorProductMasterListData) {
		SlctCompetitorProductMasterListData competitorProductMasterResponseOutput = new SlctCompetitorProductMasterListData();
		List<SlctCompetitorProductMasterData> competitorProductMasterResponseList = new ArrayList<>();
		List<SlctCompetitorProductMasterData> competitorProductMasterDataList = slctCompetitorProductMasterListData.getCompetitorProductList();
		for (SlctCompetitorProductMasterData competitorProductData : competitorProductMasterDataList) {
			if (competitorProductData.getCode() != null && !competitorProductData.getCode().isEmpty() && competitorProductData.getState() != null && !competitorProductData.getState().isEmpty()) {
				SlctCompetitorProductMasterData competitorProductResponseData = new SlctCompetitorProductMasterData();
				CompetitorProductModel competitorProductModel = slctCrmIntegrationDao.getCompetitorProductByCodeAndState(competitorProductData.getCode(), competitorProductData.getState(), competitorProductData.getBrandIsoCode());
				if (Objects.isNull(competitorProductModel)) {
					competitorProductModel = modelService.create(CompetitorProductModel.class);
					competitorProductModel.setCode(competitorProductData.getCode());
					competitorProductModel.setState(competitorProductData.getState());
					if (competitorProductData.getBrandIsoCode() != null && !competitorProductData.getBrandIsoCode().isEmpty()) {
						competitorProductModel.setBrand(slctCrmIntegrationDao.getBrandByIsoCode(competitorProductData.getBrandIsoCode()));
					}
				}
				competitorProductModel.setName(competitorProductData.getName());
				competitorProductModel.setGrade(competitorProductData.getGrade());
				competitorProductModel.setPackaging(competitorProductData.getPackaging());
				competitorProductModel.setProduct(competitorProductData.getProduct());
				competitorProductModel.setActive(competitorProductData.getActive());
				modelService.save(competitorProductModel);

				competitorProductResponseData.setCode(competitorProductModel.getCode());
				competitorProductResponseData.setState(competitorProductModel.getState());
				competitorProductResponseData.setCrmUpdatedFlag("Y");
				competitorProductMasterResponseList.add(competitorProductResponseData);
			}
		}
		competitorProductMasterResponseOutput.setCompetitorProductList(competitorProductMasterResponseList);
		return competitorProductMasterResponseOutput;
	}

	@Override
	public List<OrderModel> getModifiedOrders() {
		Integer lastXDays = dataConstraintDao.findDaysByConstraintName("ORDER_HEADER_SLCT_MODIFIED_TIME");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -lastXDays);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String date = dateFormat.format(cal.getTime()) + "%";
		LOG.info("Yesterday date: " + date);
		return slctCrmIntegrationDao.getModifiedOrdersOfYesterday(date);
	}

	@Override
	public boolean insertUpdateCustomerAddress(ERPCustomerAddressListData erpCustomerAddressListData) {
		List<ERPCustomerAddressData> erpCustomerAddressDataList = erpCustomerAddressListData.getCustomerAddresses();
		for (ERPCustomerAddressData addressData : erpCustomerAddressDataList) {
			if (addressData.getCustomerNo() != null && !addressData.getCustomerNo().isEmpty()) {
				EyDmsCustomerModel customerModel = slctCrmIntegrationDao.getCustomerByCustNo(addressData.getCustomerNo());
				if (!Objects.isNull(customerModel)) {
					if (addressData.getErpAddressId() != null && !addressData.getErpAddressId().isEmpty()) {
						boolean isExistingAddress = true;
						AddressModel addressModel = slctCrmIntegrationDao.getAddressByErpAddressId(addressData.getErpAddressId());
						if (Objects.isNull(addressModel)) {
							addressModel = modelService.create(AddressModel.class);
							addressModel.setErpAddressId(addressData.getErpAddressId());
							addressModel.setOwner(customerModel);
							addressModel.setIsCreatedFromSlctIntegration(true);
							isExistingAddress = false;
						}
						addressModel.setAccountName(addressData.getAccountName());
						addressModel.setFirstname(addressData.getFirstName());
						addressModel.setStreetname(addressData.getStreetName());
						addressModel.setStreetnumber(addressData.getStreetNumber());
						addressModel.setState(addressData.getState());
						addressModel.setDistrict(addressData.getDistrict());
						addressModel.setTaluka(addressData.getTaluka());
						addressModel.setErpCity(addressData.getErpCity());
						addressModel.setPostalcode(addressData.getPostalCard());
						addressModel.setCellphone(addressData.getCellPhone());
						addressModel.setEmail(addressData.getEmail());
						addressModel.setIsPrimaryAddress(addressData.getPrimaryAddress());
						addressModel.setBillingAddress(addressData.getIsBillingAddress());
						addressModel.setShippingAddress(addressData.getIsShippingAddress());
						addressModel.setDuplicate(addressData.getIsDuplicate());
						addressModel.setVisibleInAddressBook(addressData.getVisibleInAddressBook());
						if (addressData.getCountry() != null && !addressData.getCountry().isEmpty()) {
							CountryModel countryModel = slctCrmIntegrationDao.getCountryByIsoCode(addressData.getCountry());
							if (countryModel != null) {
								addressModel.setCountry(countryModel);
							}
						}
						addressModel.setIsModifiedFromSlctIntegration(true);
						modelService.save(addressModel);
						if (!isExistingAddress) {
							customerAccountService.saveAddressEntry(customerModel, addressModel);
						}
					}
				}
			}
		}
		return true;
	}

	@Override
	public List<OrderEntryModel> getModifiedOrderEntries() {
		Integer lastXDays = dataConstraintDao.findDaysByConstraintName("ORDER_LINE_SLCT_MODIFIED_TIME");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -lastXDays);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String date = dateFormat.format(cal.getTime()) + "%";
		LOG.info("Yesterday date: " + date);
		return slctCrmIntegrationDao.getModifiedOrderEntries(date);
	}


	@Override
	public List<InfluencerVisitMasterModel> getInfluencerVisitDetails() {
		//return influencerDao.getInfluencerVisitDetails();
		return slctCrmIntegrationDao.getInfluencerVisitDetails();
	}

	@Override
	public List<EndCustomerComplaintModel> getEndCustomerComplaintsForSLCT(String startDate, String endDate) {
		//return technicalAssistanceDao.getEndCustomerComplaintsForSLCT(startDate, endDate);
		return slctCrmIntegrationDao.getEndCustomerComplaintsForSLCT(startDate, endDate);
	}

	@Override
	public List<EndCustomerComplaintModel> getEndCustomerComplaintClosureRequest() {
		return technicalAssistanceDao.getEndCustomerComplaintClosureRequest();

	}

    @Override
    public List<EyDmsSiteMasterModel> getSiteMasterDetailsForSLCT() {
		return slctCrmIntegrationDao.getSiteMasterDetailsForSLCT();
    }

    @Override
	public List<MeetingScheduleModel> getinfluencerMeetCompletion() {
		return influencerDao.getInfluencerMeetCompleted();
	}

	@Override
	public List<PointRequisitionModel> getPointRequisitonDetails() {
		return slctCrmIntegrationDao.getPointRequisitionDetails();
	}

	@Override
	public List<SiteVisitMasterModel> getSiteVisitForms() {
		return slctCrmIntegrationDao.getSiteVisitForms();
	}

	@Override
	public boolean insertUpdateProductPointMaster(SlctProductPointMasterListData slctProductPointMasterListData) {
		userService.setCurrentUser(userService.getUserForUID("admin"));
		List<SlctProductPointMasterData> productPointMasterDataList = slctProductPointMasterListData.getProductPointMasterData();
		for(SlctProductPointMasterData productPointMasterData : productPointMasterDataList) {
			baseSiteService.setCurrentBaseSite(baseSiteService.getBaseSiteForUID(productPointMasterData.getBrand()),true);
			CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid()+"ProductCatalog", "Online");
			ProductModel productModel = productService.getProductForCode(catalogVersion,productPointMasterData.getProductCode());
			if(!Objects.isNull(productModel)) {
				ProductPointMasterModel productPointMasterModel = slctCrmIntegrationDao.getProductPointMasterModel(productPointMasterData.getSchemeId(),productModel);
				if(Objects.isNull(productPointMasterModel)) {
					productPointMasterModel = modelService.create(ProductPointMasterModel.class);
					productPointMasterModel.setSchemeId(productPointMasterData.getSchemeId());
					productPointMasterModel.setProduct(productModel);
				}
				productPointMasterModel.setPoints(productPointMasterData.getPoints());
				modelService.save(productPointMasterModel);
			}
		}
		return true;
	}

	@Override
	public boolean insertUpdateGiftData(SlctGiftSchemesListData slctGiftSchemesListData) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		List<SlctGiftSchemesData> giftsDataList = slctGiftSchemesListData.getGiftSchemesData();
		List<String> brands = new ArrayList<>();
		brands.add("102");
		brands.add("103");
		brands.add("104");

		userService.setCurrentUser(userService.getUserForUID("admin"));

		for(SlctGiftSchemesData giftData : giftsDataList) {
			for(String brand : brands) {
				catalogVersionService.setSessionCatalogVersion(brand + "ProductCatalog", "Staged");
				LOG.info("brand"+brand);
				CategoryModel categoryModel = null;
				Collection<CategoryModel> categoryModels = new ArrayList<>();
				baseSiteService.setCurrentBaseSite(baseSiteService.getBaseSiteForUID(brand),true);
				CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid()+"ProductCatalog", "Staged");
				GiftModel giftModel = slctCrmIntegrationDao.getGiftModelByCode(giftData.getUniqueGiftId(),catalogVersion);

				if(Objects.isNull(giftModel)) {
					LOG.info("Creating a new gift model");
					giftModel = modelService.create(GiftModel.class);
					giftModel.setCode(giftData.getUniqueGiftId());
					giftModel.setCatalogVersion(catalogVersion);
					if(giftData.getGiftName().equalsIgnoreCase("cash")) {
						categoryModel = categoryService.getCategoryForCode(catalogVersion,"cash");
					}
					else {
						categoryModel = categoryService.getCategoryForCode(catalogVersion,"gift");
					}
					categoryModels.add(categoryModel);
					giftModel.setSupercategories(categoryModels);
					giftModel.setApprovalStatus(ArticleApprovalStatus.APPROVED);
				}
				giftModel.setName(giftData.getGiftName());
				giftModel.setGiftSchemeId(giftData.getGiftSchemeId());
				giftModel.setInfluencerType(giftData.getInfluencerType());
				giftModel.setGiftState(giftData.getState());
				giftModel.setGiftCode(giftData.getGiftCode());

				if(giftData.getStartDate()!=null && !giftData.getStartDate().isEmpty()) {
					try {
						giftModel.setOnlineDate(dateFormat.parse(giftData.getStartDate()));
					} catch (ParseException e) {
						throw new RuntimeException(e);
					}
				}

				if(giftData.getEndDate()!=null && !giftData.getEndDate().isEmpty()) {
					try {
						giftModel.setOfflineDate(dateFormat.parse(giftData.getEndDate()));
					} catch (ParseException e) {
						throw new RuntimeException(e);
					}
				}
				modelService.save(giftModel);

				StockLevelModel stockLevelModel = slctCrmIntegrationDao.getStockLevelByProductAndWarehouse(giftData.getUniqueGiftId(),warehouseService.getWarehouseForCode(brand+"Warehouse"));
				if(Objects.isNull(stockLevelModel)) {
					stockLevelModel = modelService.create(StockLevelModel.class);
					stockLevelModel.setProductCode(giftData.getUniqueGiftId());
					stockLevelModel.setWarehouse(warehouseService.getWarehouseForCode(brand+"Warehouse"));
				}
				stockLevelModel.setAvailable(1);
				stockLevelModel.setInStockStatus(InStockStatus.FORCEINSTOCK);
				stockLevelModel.setMaxPreOrder(1);
				stockLevelModel.setMaxStockLevelHistoryCount(-1);
				stockLevelModel.setOverSelling(0);
				stockLevelModel.setPreOrder(0);
				stockLevelModel.setReserved(0);
				modelService.save(stockLevelModel);

				PriceRowModel priceRowModel = slctCrmIntegrationDao.getPriceRowByProduct(giftData.getUniqueGiftId());
				if(Objects.isNull(priceRowModel)) {
					priceRowModel = modelService.create(PriceRowModel.class);
					priceRowModel.setProductId(giftData.getUniqueGiftId());
					priceRowModel.setUnit(unitService.getUnitForCode("pieces"));
					priceRowModel.setCurrency(slctCrmIntegrationDao.getCurrencyModelByISOCode("INR"));
				}
				priceRowModel.setPrice(giftData.getPoints());
				priceRowModel.setMinqtd(1L);
				priceRowModel.setUnitFactor(1);
				priceRowModel.setNet(false);
				modelService.save(priceRowModel);
			}
		}

		setupSyncJobService.executeCatalogSyncJob("102ProductCatalog");
		setupSyncJobService.executeCatalogSyncJob("103ProductCatalog");
		setupSyncJobService.executeCatalogSyncJob("104ProductCatalog");
		return true;
	}

	@Override
	public boolean schemesDefinitionFromSlct(SlctSchemesListData slctSchemesListData) {
		if(slctSchemesListData!=null)
		{
			if(slctSchemesListData.getSchemesData()!=null && !slctSchemesListData.getSchemesData().isEmpty()){
				for (SlctSchemesData schemesDatum : slctSchemesListData.getSchemesData()) {
						if(schemesDatum.getSchemeId()!=null && schemesDatum.getState()!=null && schemesDatum.getInfluencerType()!=null){
							GiftSchemeModel giftSchemeModel = slctCrmIntegrationDao.getSchemesDefinitionById(schemesDatum.getSchemeId(), schemesDatum.getState(), schemesDatum.getInfluencerType());

							if(Objects.isNull(giftSchemeModel)) {
								giftSchemeModel = modelService.create(GiftSchemeModel.class);
								giftSchemeModel.setSchemeId(schemesDatum.getSchemeId());
								giftSchemeModel.setInfluencerType(schemesDatum.getInfluencerType());
								giftSchemeModel.setState(schemesDatum.getState());
							}
							
							giftSchemeModel.setSchemeName(schemesDatum.getSchemeName());

							DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
							if (schemesDatum.getStartDate() != null) {
								try {
									giftSchemeModel.setStartDate(dateFormat.parse(schemesDatum.getStartDate()));
								} catch (ParseException e) {
									throw new RuntimeException(e);
								}
							}
							if (schemesDatum.getEndDate() != null) {
								try {
									giftSchemeModel.setEndDate(dateFormat.parse(schemesDatum.getEndDate()));
								} catch (ParseException e) {
									throw new RuntimeException(e);
								}
							}
							/*BaseSiteModel currentBaseSite = baseSiteService.getCurrentBaseSite();
							giftSchemeModel.setBrand(currentBaseSite);*/

							modelService.save(giftSchemeModel);
						}
				}
			}
		}
		return true;
	}

	@Override
	public List<List<Object>> getGiftRedemptionForSLCT() {
		return slctCrmIntegrationDao.getGiftRedemptionForSLCT();
	}

	@Override
	public List<OrderEntryModel> getGiftRedemptionsForSLCT() {
		return slctCrmIntegrationDao.getGiftRedemptionsForSLCT();
	}

	@Override
	public SlctCrmTSOMasterListData insertUpdateTSOMasterDetails(SlctCrmTSOMasterListData slctCrmTSOMasterListData) {
		SlctCrmTSOMasterListData tsoMasterResponseOutput = new SlctCrmTSOMasterListData();
		List<SlctCrmTSOMasterData> tsoMasterDataList = slctCrmTSOMasterListData.getTsoMaster();
		List<SlctCrmTSOMasterData> tsoMastersReponseList = new ArrayList<SlctCrmTSOMasterData>();
		for (SlctCrmTSOMasterData tsoMasterData : tsoMasterDataList) {
			SlctCrmTSOMasterData tsoMasterRepsonse = new SlctCrmTSOMasterData();

			Set<PrincipalGroupModel> groups = new HashSet<>();
			EyDmsUserModel tsoMasterModel = slctCrmIntegrationDao.getDoByUidAndEmpCode(tsoMasterData.getUid().toLowerCase());
			if (Objects.isNull(tsoMasterModel)) {
				PrincipalGroupModel b2bCustomerGroupModel, tsoGroupModel;
				tsoMasterModel = modelService.create(EyDmsUserModel.class);
				tsoMasterModel.setUid(tsoMasterData.getUid().toLowerCase());
				tsoMasterModel.setEmail(tsoMasterData.getEmail().toLowerCase());
				tsoMasterModel.setEmployeeCode(tsoMasterData.getEmployeeCode());
				b2bCustomerGroupModel = slctCrmIntegrationDao.getPrincipalGroupByUid("b2bcustomergroup");
				tsoGroupModel = slctCrmIntegrationDao.getPrincipalGroupByUid("tsogroup");
				groups.add(b2bCustomerGroupModel);
				groups.add(tsoGroupModel);
				tsoMasterModel.setGroups(groups);
				tsoMasterModel.setIsCreatedFromSlctIntegration(true);
			}
			tsoMasterModel.setDefaultB2BUnit(defaultB2BUnitService.getUnitForUid("EyDmsShreeUnit"));
			tsoMasterModel.setUserType(EyDmsUserType.TSO);
			tsoMasterModel.setName(tsoMasterData.getName());
			tsoMasterModel.setMobileNumber(tsoMasterData.getMobileNumber());
			tsoMasterModel.setState(tsoMasterData.getState());
			tsoMasterModel.setSessionCurrency(slctCrmIntegrationDao.getCurrencyModelByISOCode("INR"));
			modelService.save(tsoMasterModel);
			if (tsoMasterData.getActive().equals("N")) {
				tsoMasterModel.setLoginDisabled(true);
				tsoMasterModel.setActive(false);
			} else if (tsoMasterData.getActive().equals("Y")) {
				String password = "Qaz1@3ws%x";
				tsoMasterModel.setPassword(password);
				tsoMasterModel.setLoginDisabled(false);
				tsoMasterModel.setActive(true);
			}
			tsoMasterModel.setIsModifiedFromSlctIntegration(true);
			modelService.save(tsoMasterModel);
			tsoMasterRepsonse.setUid(tsoMasterModel.getUid());
			tsoMasterRepsonse.setCrmUpdated("Y");
			tsoMastersReponseList.add(tsoMasterRepsonse);

		}
		tsoMasterResponseOutput.setTsoMaster(tsoMastersReponseList);
		return tsoMasterResponseOutput;
	}

	@Override
	public SlctTSOSubAreaMappingListData insertUpdateTSOSubAreaMapping(SlctTSOSubAreaMappingListData slctTSOSubAreaMappingListData) {
		SlctTSOSubAreaMappingListData tsoSubAreaMappingResponseOutput = new SlctTSOSubAreaMappingListData();
		List<SlctTSOSubAreaMappingData> tsoSubAreaMappingDataList = slctTSOSubAreaMappingListData.getTsoSubAreaMappingList();
		List<SlctTSOSubAreaMappingData> tsoSubAreaMappingResponseList = new ArrayList<>();
		for (SlctTSOSubAreaMappingData tsoSubAreaMappingData : tsoSubAreaMappingDataList) {
			SlctTSOSubAreaMappingData tsoSubAreaMappingResponse = new SlctTSOSubAreaMappingData();

			SubAreaMasterModel subAreaMasterModel = slctCrmIntegrationDao.getSubAreaByTalukaAndDistrict(tsoSubAreaMappingData.getSubArea(), tsoSubAreaMappingData.getDistrict());
			if (Objects.isNull(subAreaMasterModel)) {
				subAreaMasterModel = modelService.create(SubAreaMasterModel.class);
				subAreaMasterModel.setTaluka(tsoSubAreaMappingData.getSubArea());
				subAreaMasterModel.setDistrict(tsoSubAreaMappingData.getDistrict());
				modelService.save(subAreaMasterModel);
			}

			TsoTalukaMappingModel tsoSubAreaMappingModel = slctCrmIntegrationDao.getTsoTalukaMappingByEmp(tsoSubAreaMappingData.getSoUid(), "102", subAreaMasterModel);
			if (Objects.isNull(tsoSubAreaMappingModel)) {
				tsoSubAreaMappingModel = modelService.create(TsoTalukaMappingModel.class);
				EyDmsUserModel tsoMaster = slctCrmIntegrationDao.getDoByUidAndEmpCode(tsoSubAreaMappingData.getSoUid().toLowerCase());
				tsoSubAreaMappingModel.setTsoUser(tsoMaster);
				tsoSubAreaMappingModel.setSubAreaMaster(subAreaMasterModel);
				tsoSubAreaMappingModel.setState(tsoSubAreaMappingData.getState());
				tsoSubAreaMappingModel.setBrand((CMSSiteModel) baseSiteService.getBaseSiteForUID("102"));
				tsoSubAreaMappingModel.setIsCreatedFromSlctIntegration(true);
			}

			if (tsoSubAreaMappingData.getIsActive().equals("Y")) {
				tsoSubAreaMappingModel.setIsActive(true);
			} else if (tsoSubAreaMappingData.getIsActive().equals("N")) {
				tsoSubAreaMappingModel.setIsActive(false);
			}
			tsoSubAreaMappingModel.setIsModifiedFromSlctIntegration(true);
			modelService.save(tsoSubAreaMappingModel);
			tsoSubAreaMappingResponse.setSoUid(tsoSubAreaMappingModel.getTsoUser().getUid());
			tsoSubAreaMappingResponse.setTsoEmployeeCode(tsoSubAreaMappingModel.getTsoUser().getEmployeeCode());
			tsoSubAreaMappingResponse.setSubArea(tsoSubAreaMappingModel.getSubAreaMaster().getTaluka());
			tsoSubAreaMappingResponse.setDistrict(tsoSubAreaMappingModel.getSubAreaMaster().getDistrict());
			tsoSubAreaMappingResponse.setState(tsoSubAreaMappingModel.getState());
			tsoSubAreaMappingResponse.setBrand(tsoSubAreaMappingModel.getBrand().getUid());
			tsoSubAreaMappingResponse.setCrmUpdated("Y");
			tsoSubAreaMappingResponseList.add(tsoSubAreaMappingResponse);
		}
		tsoSubAreaMappingResponseOutput.setTsoSubAreaMappingList(tsoSubAreaMappingResponseList);
		return tsoSubAreaMappingResponseOutput;
	}

	private static final String EMAIL_PATTERN =
			"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" +
					"[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

	public static boolean isValidEmail(String email) {
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}

}
