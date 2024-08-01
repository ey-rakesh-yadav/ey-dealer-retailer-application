package com.scl.core.services.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import java.time.LocalDate;
import java.util.*;

import com.scl.core.enums.HoldsAndDisbursements;
import com.scl.core.enums.NotificationCategory;
import com.scl.core.jalo.SchemesFileRequest;
import com.scl.core.model.*;
import com.scl.core.notifications.service.SclNotificationService;
import com.scl.facades.data.FilterTalukaData;
import com.scl.facades.data.SchemesFileRequestData;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.scl.core.dao.SchemesAndDiscountDao;
import com.scl.core.enums.GiftType;
import com.scl.core.enums.TransactionType;
import com.scl.core.services.SchemesAndDiscountService;
import com.scl.core.services.TerritoryManagementService;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.services.B2BOrderService;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.impl.CatalogUtils;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.CategoryService;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.order.CalculationService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.time.TimeService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.store.services.BaseStoreService;

public class SchemesAndDiscountServiceImpl implements SchemesAndDiscountService{

    private static final Logger LOG = Logger.getLogger(SchemesAndDiscountServiceImpl.class);

	@Autowired
	SchemesAndDiscountDao schemesAndDiscountDao;

	@Autowired
	private CatalogVersionService catalogVersionService;
	
	@Autowired
	private CalculationService calculationService;
	
	@Autowired
	private CommonI18NService commonI18NService;
	
	@Autowired
	private BaseSiteService baseSiteService;

	@Autowired
	SclNotificationService sclNotificationService;
	
	@Autowired
	private BaseStoreService baseStoreService;
	
	@Autowired
	private ConfigurationService configurationService;
	
	@Autowired
	private TimeService timeService;
	
	@Autowired
	private ModelService modelService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	TerritoryManagementService territoryManagementService;
	
	@Autowired
	B2BOrderService b2bOrderService;

	@Autowired
	CategoryService categoryService;

	@Autowired
	private KeyGenerator fileRequestNoGenerator;
	
	public CalculationService getCalculationService() {
		return calculationService;
	}

	public void setCalculationService(CalculationService calculationService) {
		this.calculationService = calculationService;
	}

	@Override
	public GiftSchemeModel getCurrentSchemesByGeography(String geography, String influencerType) {
		validateParameterNotNull(geography, "Parameter 'geography' was null.");
		final Collection<GiftSchemeModel> categories = this.schemesAndDiscountDao.findGiftSchemeByStateAndPeriod(geography, influencerType, LocalDate.now());

		if (categories.isEmpty())
		{
			throw new UnknownIdentifierException(
					"Schemes with geography '" + geography + "' not found! (Active session catalogversions: " + getCatalogVersionsString() + ")");
		}
		else if (categories.size() > 1)
		{
			throw new AmbiguousIdentifierException("Schemes with geography '" + geography + "' in CatalogVersion '" + "' is not unique. " + categories.size()
							+ " categories found! (Active session catalogversions: " + getCatalogVersionsString() + ")");
		}
		return categories.iterator().next();
	}

	private String getCatalogVersionsString()
	{
		return CatalogUtils.getCatalogVersionsString(catalogVersionService.getSessionCatalogVersions());
	}

	@Override
	public List<List<Object>> getCurrentYearSchemes(CatalogVersionModel catalogVersion, String geography,
			String counterType, String influencerType) {
		validateParameterNotNull(catalogVersion, "Parameter 'catalogVersion' was null.");
		validateParameterNotNull(geography, "Parameter 'geography' was null.");
		
		int currentYear = LocalDate.now().getYear();
		int currentMonth = LocalDate.now().getMonthValue();
		String financiyalYearFrom="";
		String financiyalYearTo="";
		if (currentMonth<4) {
		    financiyalYearFrom=(currentYear-1)+"-04-01";
		    financiyalYearTo=(currentYear)+"-04-01";
		} else {
		    financiyalYearFrom=(currentYear)+"-04-01";
		    financiyalYearTo=(currentYear+1)+"-04-01";
		}
		final List<List<Object>> list = this.schemesAndDiscountDao.findCurrentYearSchemes(catalogVersion, geography, counterType, influencerType, financiyalYearFrom, financiyalYearTo);
		return list;
	}

	@Override
	public GiftOrderModel claimGift(CartModel cartModel) {
//		validateParameterNotNull(cartModel, "Cart model cannot be null");
//		final CommerceOrderResult result = new CommerceOrderResult();
//		try
//		{
//			if (calculationService.requiresCalculation(cartModel))
//			{
//				// does not make sense to fail here especially since we don't fail below when we calculate order.
//				// throw new IllegalArgumentException(String.format("Cart [%s] must be calculated", cartModel.getCode()));
//				LOG.error(String.format("CartModel's [%s] calculated flag was false", cartModel.getCode()));
//			}
//
//			final CustomerModel customer = (CustomerModel) cartModel.getUser();
//			validateParameterNotNull(customer, "Customer model cannot be null");
//			//cartModel.setLocale(getCommonI18NService().getCurrentLocale().toString());
//
//			final GiftOrderModel orderModel = null;//getOrderService().createOrderFromCart(cartModel);
//			if (orderModel != null)
//			{
//				// Reset the Date attribute for use in determining when the order was placed
//				orderModel.setDate(timeService.getCurrentTime());
//
//
//
//				// Store the current site and store on the order
//				orderModel.setSite(getBaseSiteService().getCurrentBaseSite());
//				orderModel.setStore(getBaseStoreService().getCurrentBaseStore());
//				orderModel.setLanguage(getCommonI18NService().getCurrentLanguage());
//
//				getModelService().saveAll(customer, orderModel);
//
//				getModelService().save(orderModel);
//
//				// Calculate the order now that it has been copied
//				try
//				{
//					getCalculationService().calculateTotals(orderModel, false);
//				}
//				catch (final CalculationException ex)
//				{
//					LOG.error("Failed to calculate order [" + orderModel + "]", ex);
//				}
//
//				getModelService().refresh(orderModel);
//				getModelService().refresh(customer);
//
//				//getOrderService().submitOrder(orderModel);
//			}
//			else
//			{
//				throw new IllegalArgumentException(String.format("Order was not properly created from cart %s", cartModel.getCode()));
//			}
//		}
//		finally
//		{
//			
//		}

		return null;
	}

	public CommonI18NService getCommonI18NService() {
		return commonI18NService;
	}

	public void setCommonI18NService(CommonI18NService commonI18NService) {
		this.commonI18NService = commonI18NService;
	}

	public BaseSiteService getBaseSiteService() {
		return baseSiteService;
	}

	public void setBaseSiteService(BaseSiteService baseSiteService) {
		this.baseSiteService = baseSiteService;
	}

	public BaseStoreService getBaseStoreService() {
		return baseStoreService;
	}

	public void setBaseStoreService(BaseStoreService baseStoreService) {
		this.baseStoreService = baseStoreService;
	}

	public ConfigurationService getConfigurationService() {
		return configurationService;
	}

	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	public TimeService getTimeService() {
		return timeService;
	}

	public void setTimeService(TimeService timeService) {
		this.timeService = timeService;
	}

	public ModelService getModelService() {
		return modelService;
	}

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	@Override
	public Double getTotalAvailablePoints(String customerCode) {

		B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();

		if(currentUser!=null && currentUser instanceof SclCustomerModel)
		{
			if((null == customerCode) || (null == currentUser.getUid()) || !currentUser.getUid().equalsIgnoreCase(customerCode))
			{
				throw new UnsupportedOperationException("Given uid" + customerCode + " " + "is not matching with logged in user " + currentUser.getUid());
			}
		}

		Double totalAvailablePoints = 0.0;
		UserModel user = userService.getUserForUID(customerCode);
		if(user!=null && user instanceof SclCustomerModel && ((SclCustomerModel)user).getAvailablePoints()!=null)
		{
			totalAvailablePoints = ((SclCustomerModel)user).getAvailablePoints();
		}
		return totalAvailablePoints;
	}

	@Override
	public synchronized Boolean updateInfluencerPoint(SclCustomerModel customer, double redeemPoint, TransactionType transactionType, GiftSchemeModel sheme, String orderCode) {
		customer = (SclCustomerModel)userService.getUserForUID(customer.getUid());
		Double availablePoints = 0.0;
		if(customer.getAvailablePoints()!=null) {
			availablePoints = customer.getAvailablePoints();
		}
		Double currentPoint = 0.0;
		if(TransactionType.DEBIT.equals(transactionType)) {
			currentPoint = availablePoints - redeemPoint;
		}
		else if(TransactionType.CREDIT.equals(transactionType)) {
			currentPoint = availablePoints + redeemPoint;
		}
		customer.setAvailablePoints(currentPoint);
		PointsTransactionMasterModel transaction = modelService.create(PointsTransactionMasterModel.class);
		transaction.setBalance(currentPoint);
		transaction.setTransactionType(transactionType);
		transaction.setPoints(redeemPoint);
		transaction.setScheme(sheme);
		transaction.setDate(new Date());
		transaction.setCustomer(customer);
		transaction.setOrderCode(orderCode);
		transaction.setActive(Boolean.TRUE);
		
		modelService.saveAll(customer, transaction);
		try {
			StringBuilder builder = new StringBuilder();
			Map<String,String> suggestion = new HashMap<>();
			OrderModel order = b2bOrderService.getOrderForCode(orderCode);
			suggestion.put("RequestId",orderCode);

			builder.append("You have a gift redemption request from Influencer " + customer.getName());
			builder.append(" with gift type  "+order.getGiftType().getCode() + " for quantity " +order.getTotalQuantity());
			builder.append(" , points  "+transaction.getPoints());
			String body = builder.toString();
			String sub ="Gift Redemption request raised";
			FilterTalukaData filterTalukaData = new FilterTalukaData();
			List<SubAreaMasterModel> subAreaList = territoryManagementService.getTaulkaForUser(filterTalukaData);
			List<SclUserModel> tsoList =  territoryManagementService.getTSOforSubAreas(subAreaList);
			for (SclUserModel tsoUser : tsoList) {
				sclNotificationService.submitDealerNotification((B2BCustomerModel) tsoUser, body, sub, NotificationCategory.REDEEM_REQUEST, suggestion);
			}

		}
		catch(Exception e) {
			LOG.error("Error while sending Redeem Request Notification");
		}

		return true;
	}


	@Override
	public synchronized Boolean updateInfluencerPoint(String orderCode) {
		OrderModel order = b2bOrderService.getOrderForCode(orderCode);
		double points = order.getTotalPrice();
		if(order.getGiftType()!=null && order.getGiftType().equals(GiftType.CASH)) {
			points = order.getEntries().get(0).getQuantity();
		}
		return updateInfluencerPoint((SclCustomerModel)order.getUser(), points, TransactionType.DEBIT, null, order.getCode());
	}


	//To be Checked
	@Override
	public List<OrderModel> getGiftRedeemList(String userId, String status, String searchKey, String district,
			String taluka) {
	
		List<String> districtTalukaList = new ArrayList<>();
		
		if(taluka==null)
		{
			List<List<Object>> stateDistrictTalukaList = territoryManagementService.getAllTerritoryForSO();
			
			for(List<Object> list : stateDistrictTalukaList)
			{
				
				String districtTaluka = ((String)list.get(1)).concat("_").concat((String)list.get(1));
				
				districtTalukaList.add(districtTaluka);
			}
		}
		
		else
		{
			districtTalukaList.add(district.concat("_").concat(taluka));
		}
		
		List<OrderModel> orderList = schemesAndDiscountDao.getGiftOrdersForDistrictAndTaluka(districtTalukaList, status, searchKey);
		
		return orderList;
	}

	@Override
	public Boolean rejectRedeemRequest(String requestId, String rejectionReason) {
		OrderModel order = b2bOrderService.getOrderForCode(requestId);
		order.setRejectionReason(rejectionReason);
		order.setStatus(OrderStatus.REJECTED);
		PointsTransactionMasterModel transactionModel  = schemesAndDiscountDao.findPointsTransactionMasterByOrderCode(requestId);
		if(transactionModel!=null) {
			transactionModel.setActive(Boolean.FALSE);
			SclCustomerModel influencer =  (SclCustomerModel) order.getUser();
			Double points = 0.0;
			Double addPoints = 0.0;
			if(influencer.getAvailablePoints()!=null) {
				points  = influencer.getAvailablePoints();
			}
			if(transactionModel.getPoints()!=null) {
				addPoints = transactionModel.getPoints();
			}
			points += addPoints; 
			influencer.setAvailablePoints(points);
			modelService.saveAll(transactionModel,influencer);
		}
		modelService.save(order);
		return Boolean.TRUE;
	}

	@Override
	public Boolean approveRedeemRequest(String requestId) {
		if(requestId!=null){
			String[] splitReqId = requestId.split(",");
			for (String reqId : splitReqId) {
				OrderModel order = b2bOrderService.getOrderForCode(reqId);
				if(order!=null) {
					order.setStatus(OrderStatus.APPROVED);
					order.setOrderSentForApprovalDate(new Date());
					order.setApprovedBy((B2BCustomerModel) userService.getCurrentUser());
					modelService.save(order);
				}
			}
		}
		return Boolean.TRUE;
	}
	
	@Override
	public SearchPageData<OrderEntryModel> getDisbursementHistory(CatalogVersionModel catalogVersion, String customerCode,
			String schemeCode, String startDate, String endDate, SearchPageData searchPageData, String orderCode) {
		UserModel user= null;
		GiftShopModel giftShop= null;
		if(customerCode!=null) {
			user = userService.getUserForUID(customerCode);
		}
		if(schemeCode!=null) {
			giftShop = (GiftShopModel)categoryService.getCategoryForCode(catalogVersion, schemeCode);
		}
		return schemesAndDiscountDao.getDisbursementHistory(user, giftShop, startDate, endDate, baseSiteService.getCurrentBaseSite(), searchPageData, orderCode);
	}

	@Override
	public SearchPageData<ProductModel> getProductsForScheme(CatalogVersionModel catalogVersion, String schemeCode, SearchPageData searchPageData, String giftType) {
		CategoryModel giftShop= null;
		if(schemeCode!=null) {
			giftShop = categoryService.getCategoryForCode(catalogVersion, schemeCode);
		}
		CategoryModel giftTypeCategory = null;
		if(giftType!=null) {
			giftTypeCategory = categoryService.getCategoryForCode(catalogVersion, giftType);
		}
		return schemesAndDiscountDao.getProductsForScheme(giftShop, searchPageData, giftTypeCategory);
	}

	@Override
	public String findRecentGiftOrder(String influencerCode, GiftType giftType, GiftShopModel giftShop) {
		return schemesAndDiscountDao.findRecentGiftOrder(userService.getUserForUID(influencerCode), giftType, giftShop);
	}

	@Override
	public Boolean updateGiftOrderStatus(String orderCode, String status, String reason) {
		OrderModel order = b2bOrderService.getOrderForCode(orderCode);
		if(status!=null && status.equals(OrderStatus.CANCELLED.getCode())) {
			order.setStatus(OrderStatus.CANCELLED);
			order.setCancelReason(reason);
			order.setCancelledBy((B2BCustomerModel) userService.getCurrentUser());
			order.setCancelledDate(new Date());
			
			PointsTransactionMasterModel transactionModel  = schemesAndDiscountDao.findPointsTransactionMasterByOrderCode(orderCode);
			if(transactionModel!=null) {
				transactionModel.setActive(Boolean.FALSE);
				SclCustomerModel influencer =  (SclCustomerModel) order.getUser();
				Double points = 0.0;
				Double addPoints = 0.0;
				if(influencer.getAvailablePoints()!=null) {
					points  = influencer.getAvailablePoints();
				}
				if(transactionModel.getPoints()!=null) {
					addPoints = transactionModel.getPoints();
				}
				points += addPoints; 
				influencer.setAvailablePoints(points);
				modelService.save(influencer);
				modelService.save(transactionModel);
			}
			
//			double points = order.getTotalPrice();
//			if(order.getGiftType()!=null && order.getGiftType().equals(GiftType.CASH)) {
//				points = order.getEntries().get(0).getQuantity();
//			}			
//			updateInfluencerPoint((SclCustomerModel) userService.getCurrentUser(), points, TransactionType.CREDIT, order.getGiftShop(), order.getCode());
			modelService.save(order);
		}
		return Boolean.TRUE;
	}

	@Override
	public Boolean saveFileRequest(SchemesFileRequestData schemesFileRequestData) {
		SchemesFileRequestModel schemesFileRequestModel = modelService.create(SchemesFileRequestModel.class);

		schemesFileRequestModel.setRequestNo(String.valueOf(fileRequestNoGenerator.generate()));
		schemesFileRequestModel.setSchemeCode(schemesFileRequestData.getSchemeCode());
		schemesFileRequestModel.setSchemeName(schemesFileRequestData.getSchemeCode());
		schemesFileRequestModel.setCustomerCode(schemesFileRequestData.getCustomerCode());
		schemesFileRequestModel.setCustomerName(schemesFileRequestData.getCustomerCode());
		schemesFileRequestModel.setReason(schemesFileRequestData.getReason());
		schemesFileRequestModel.setRequestType(HoldsAndDisbursements.valueOf(schemesFileRequestData.getRequestType()));

		B2BCustomerModel customer = (B2BCustomerModel) userService.getUserForUID(schemesFileRequestData.getCustomerCode());
		schemesFileRequestModel.setCustomer(customer);

		B2BCustomerModel currentUser = (B2BCustomerModel) userService.getCurrentUser();

		schemesFileRequestModel.setRequestBy(currentUser);
		schemesFileRequestModel.setRequestDate(new Date());

		modelService.save(schemesFileRequestModel);
		return Boolean.TRUE;
	}

    @Override
    public SearchPageData<OrderModel> getPaginatedGiftOrderList(SearchPageData searchPageData, String scheme, String searchFilter, List<String> status) {
		return schemesAndDiscountDao.getPaginatedGiftOrderList(searchPageData, scheme, searchFilter, status);

	}

	@Override
	public SearchPageData<GiftShopModel> getPaginatedGiftShopSchemeList(SearchPageData searchPageData, String scheme, String searchFilter, List<String> status) {
		return schemesAndDiscountDao.getPaginatedGiftShopSchemeList(searchPageData, scheme, searchFilter, status);
	}

}
