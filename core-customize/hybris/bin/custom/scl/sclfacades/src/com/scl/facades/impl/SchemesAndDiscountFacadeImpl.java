package com.scl.facades.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.scl.facades.data.*;
import org.apache.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.enums.CRMOrderType;
import com.scl.core.enums.GiftType;
import com.scl.core.enums.IncentiveType;
import com.scl.core.enums.TransactionType;
import com.scl.core.model.GiftShopModel;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.services.SchemesAndDiscountService;
import com.scl.core.services.SmsOtpService;
import com.scl.facades.SchemesAndDiscountFacade;

import de.hybris.platform.b2bacceleratorfacades.checkout.data.PlaceOrderData;
import de.hybris.platform.b2b.enums.CheckoutPaymentType;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.CategoryService;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.cmsfacades.data.CategoryData;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.commercefacades.order.impl.DefaultCheckoutFacade;
import de.hybris.platform.commercefacades.product.ProductOption;
import de.hybris.platform.commercefacades.product.data.ImageData;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.service.data.CommerceCheckoutParameter;
import de.hybris.platform.converters.ConfigurablePopulator;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.order.daos.DeliveryModeDao;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;


public class SchemesAndDiscountFacadeImpl extends DefaultCheckoutFacade implements SchemesAndDiscountFacade{

	private static final Logger LOGGER = Logger.getLogger(SchemesAndDiscountFacadeImpl.class);
	
	@Autowired
	SchemesAndDiscountService schemesAndDiscountService;
	
	private Converter<CategoryModel, CategoryData> categoryDataConverter;
	
	@Autowired
	private CatalogVersionService catalogVersionService;

	@Autowired
	BaseSiteService baseSiteService;
	
	@Autowired
	Populator<AddressModel, AddressData> addressPopulator;
	
	@Autowired
	EnumerationService enumerationService;
	
	@Autowired
	I18NService i18NService;

	@Autowired
	CategoryService categoryService;
	
	@Autowired
	SmsOtpService smsOtpService;
	
	private Converter<MediaModel, ImageData> imageConverter;
	
	private Converter<ProductModel, ProductData> productConverter;
	
	private ConfigurablePopulator<ProductModel, ProductData, ProductOption> productConfiguredPopulator;

//	@Override
//	public CategoryData getCurrentSchemesByGeography(String geography, String counterType, String influencerType) {
//        CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
//
//		return getCategoryDataConverter().convert(schemesAndDiscountService.getCurrentSchemesByGeography(geography, counterType, influencerType));
//	}

	public Converter<CategoryModel, CategoryData> getCategoryDataConverter() {
		return categoryDataConverter;
	}

	public void setCategoryDataConverter(Converter<CategoryModel, CategoryData> categoryDataConverter) {
		this.categoryDataConverter = categoryDataConverter;
	}

	public CatalogVersionService getCatalogVersionService() {
		return catalogVersionService;
	}

	public void setCatalogVersionService(CatalogVersionService catalogVersionService) {
		this.catalogVersionService = catalogVersionService;
	}

	@Override
	public CurrentYearSchemeListData getCurrentYearSchemes(String geography, String counterType,
			String influencerType, String influencerCode) {
//        CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");

		CurrentYearSchemeListData listData = new CurrentYearSchemeListData();
//		List<CurrentYearSchemeData> list = new ArrayList<CurrentYearSchemeData>();
//		List<List<Object>> currentYearSchemes = schemesAndDiscountService.getCurrentYearSchemes(catalogVersion, geography, counterType, influencerType);
//		Map<Object, List<List<Object>>> map = currentYearSchemes.stream().collect(Collectors.groupingBy(each->{
//			return each.get(0);
//		}));
//	    for (Entry<Object, List<List<Object>>> entry : map.entrySet()) {
//	    	String schemeCode = (String) entry.getKey();
//	    	List<List<Object>> model = entry.getValue();
//	    	String schemeName = model!=null&&!model.isEmpty()&&model.get(0).size()>1?(String) model.get(0).get(1):"";
//	    	Double redeemedPoints = 0.0;
//	    	Double earnedPoints = 0.0;
//	    	if(model!=null) {
//	    		for(List<Object> objList : model) {
//	    			if(objList.size()>2 && objList.get(2)!=null && ((String)objList.get(2)).equals(TransactionType.CREDIT.getCode())) {
//	    				if(objList.size()>3 && objList.get(3)!=null) {
//	    					earnedPoints = (Double) objList.get(3);
//	    				}
//	    			}
//	    			else if(objList.size()>2 && objList.get(2)!=null && ((String)objList.get(2)).equals(TransactionType.DEBIT.getCode())) {
//	    				if(objList.size()>3 && objList.get(3)!=null) {
//	    					redeemedPoints = (Double) objList.get(3);
//	    				}
//	    			}
//	    		}
//	    	}
//			CurrentYearSchemeData data = new CurrentYearSchemeData();
//			data.setSchemeCode(schemeCode);
//			data.setSchemeName(schemeName);
//			data.setEarnedPoints(earnedPoints);
//			data.setRedeemedPoints(redeemedPoints);
//			list.add(data);
//	    }
//		listData.setSchemeList(list);
		if(influencerCode!=null) {
//			GiftShopModel giftShopModel = schemesAndDiscountService.getCurrentSchemesByGeography(catalogVersion, geography, counterType, influencerType);
			listData.setCashOrderCode(schemesAndDiscountService.findRecentGiftOrder(influencerCode, GiftType.CASH, null));
			listData.setKindOrderCode(schemesAndDiscountService.findRecentGiftOrder(influencerCode, GiftType.KIND, null));
		}
		return listData;
	}

	@Override
	public AbstractOrderData claimGift(PlaceOrderData placeOrderData) {
//		final CartModel cartModel = getCart();
//		if (cartModel != null)
//		{
//			if (cartModel.getUser().equals(getCurrentUserForCheckout()) || getCheckoutCustomerStrategy().isAnonymousCheckout())
//			{
//				final GiftOrderModel orderModel = schemesAndDiscountService.claimGift(cartModel);
//				if (orderModel != null)
//				{
//					//return getOrderConverter().convert(orderModel);
//				}
//			}
//		}
		return null;}

	@Autowired
	private UserService userService;
	
    @Autowired
    DeliveryModeDao deliveryModeDao;
    
    @Autowired
    ModelService modelService;
		
	@Override
	public Double getTotalAvailablePoints(String customerCode) {
		return schemesAndDiscountService.getTotalAvailablePoints(customerCode);
	}

	@Override
	public void updateCart(String schemeCode, String influencerCode) {
		/*CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
		GiftShopModel giftShopModel = (GiftShopModel)categoryService.getCategoryForCode(catalogVersion, schemeCode);*/
		Date currentDate = new Date();
		/*if((giftShopModel.getRedeemStartDate().before(currentDate) || giftShopModel.getRedeemStartDate().equals(currentDate))
				&& (giftShopModel.getRedeemEndDate().after(currentDate) || giftShopModel.getRedeemEndDate().equals(currentDate))) {*/

			CartModel cart = getCart();
			GiftType giftType = GiftType.KIND;
			if(cart.getEntries()!=null && !cart.getEntries().isEmpty() && cart.getEntries().get(0).getProduct()!=null) {
				if(cart.getEntries().get(0).getProduct().getName().equals("Cash")) {
					giftType = GiftType.CASH;
				}
			}
			/*LOGGER.error(String.format("Recent order input , %s , %s , %s ", influencerCode, giftType.getCode(), giftShopModel.getCode()));*/
			/*String orderCode = schemesAndDiscountService.findRecentGiftOrder(influencerCode, giftType, giftShopModel);
			LOGGER.error("Order found " +  orderCode);
			if(orderCode==null || orderCode.isEmpty() || orderCode.isBlank()) {*/
				if(userService.getCurrentUser().getAddresses()!=null) {
					Optional<AddressModel> address = userService.getCurrentUser().getAddresses().stream().filter(each->each.getShippingAddress()).findAny();
					if(address.isPresent()) {
						final CommerceCheckoutParameter parameter = createCommerceCheckoutParameter(cart, true);
						parameter.setAddress(address.get());
						parameter.setIsDeliveryAddress(false);
						getCommerceCheckoutService().setDeliveryAddress(parameter);
					}
				}
				String deliveryMode= "ROAD";
				List<DeliveryModeModel> deliveryModeList = deliveryModeDao.findDeliveryModesByCode(deliveryMode);
				cart.setDeliveryMode(deliveryModeList.get(0));
				cart.setPaymentType(CheckoutPaymentType.ACCOUNT);
				cart.setCrmOrderType(CRMOrderType.GIFT);
				cart.setGiftType(giftType);
				/*cart.setGiftShop(giftShopModel);*/


				modelService.save(cart);
			/*}
			else {
				LOGGER.error("Already redeemed once");
				throw new UnsupportedOperationException("Already redeemed once");
			}*/
		/*}
		else {
			LOGGER.error("Redeem Window is closed");
			throw new UnsupportedOperationException("Redeem Window is closed");
		}*/
	}

	@Override
	public GiftOrderListData getGiftRedeemList(String userId, String status, String searchKey, String district,
			String taluka) {
		
		List<OrderModel> orderList = schemesAndDiscountService.getGiftRedeemList(userId, status, searchKey, district, taluka);
		
		List<GiftOrderData> dataList = new ArrayList<>();
		
		for(OrderModel order : orderList)
		{
			GiftOrderData data = new GiftOrderData();
			data.setName(order.getUser()!=null ? order.getUser().getName() : null);	
			data.setCode(order.getUser()!=null ? ((SclCustomerModel)order.getUser()).getCustomerNo() : null);
			data.setRequestId(order.getCode());
			
			IncentiveType i  = order.getIncentiveType();
			
			if(i!=null)
			{
				data.setRequestType(null != enumerationService.getEnumerationName(i, i18NService.getCurrentLocale()) ? enumerationService.getEnumerationName(i, i18NService.getCurrentLocale()) : i.getCode());
			}
			
			data.setScheme(order.getGiftShop()!=null ? order.getGiftShop().getName() : null);
			
			SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss");  
			
		    if(order.getDate()!=null)
		    {
		    	data.setDate(formatter.format(order.getDate()));
		    }
		    
			data.setRejectionReason(order.getRejectionReason());
			data.setPointsRedeemed(order.getTotalPrice());
			
			data.setStatus(null != enumerationService.getEnumerationName(order.getStatus(), i18NService.getCurrentLocale()) ? enumerationService.getEnumerationName(order.getStatus(), i18NService.getCurrentLocale()) : order.getStatus().getCode());
			data.setPhoto(order.getUser()!=null ? order.getUser().getProfilePicture()!=null ? order.getUser().getProfilePicture().getURL() : null : null);
			AddressData address = new AddressData();
			addressPopulator.populate(order.getDeliveryAddress(), address);
			data.setAddress(address);
			
			dataList.add(data);
		}
		
		GiftOrderListData data = new GiftOrderListData();
		data.setGiftOrders(dataList);
		
		return data;
	}

	@Override
	public Boolean rejectRedeemRequest(String requestId, String rejectionReason) {
		return schemesAndDiscountService.rejectRedeemRequest(requestId, rejectionReason);
	}

	@Override
	public Boolean approveRedeemRequest(String requestId) {
		return schemesAndDiscountService.approveRedeemRequest(requestId);
	}

	@Override
	public SearchPageData<DisburseHistoryData> getDisbursementHistory(String customerCode, String schemeCode, String startDate, String endDate,
			Boolean attachment, SearchPageData searchPageData, String fields, String orderCode) {
		List<DisburseHistoryData> dataList = new ArrayList<DisburseHistoryData>();
		CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");

		SearchPageData<OrderEntryModel> searchResult = schemesAndDiscountService.getDisbursementHistory(catalogVersion, customerCode, schemeCode, startDate, endDate, searchPageData, orderCode);
		if(searchResult!=null && searchResult.getResults()!=null) {
			List<OrderEntryModel> itemWonList = searchResult.getResults();
			itemWonList.forEach(entry -> {
				DisburseHistoryData history = new DisburseHistoryData();
				history.setGiftCode(entry.getOrder().getCode());
				history.setEntryNumber(entry.getEntryNumber().toString());
				history.setProductName(entry.getProduct().getName());
				history.setSchemeCode(entry.getOrder().getGiftShop().getCode());
				history.setSchemeName(entry.getOrder().getGiftShop().getName());
				if(entry.getDateOfReceiving()!=null) {
					DateFormat dateFormat = new SimpleDateFormat(SclCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
					history.setDateOfReceiving(dateFormat.format(entry.getDateOfReceiving()));
				}
				if(fields!=null && fields.equals("FULL")) {
					history.setReceiverName(entry.getDisbursement().getRecieverName());
					history.setIsSelfRecipient(true);
					history.setPoint(entry.getTotalPrice());
					
					if(entry.getDisbursement().getDisbursementReciept()!=null) {
						List<ImageData> receipts = new ArrayList<ImageData>();
						entry.getDisbursement().getDisbursementReciept().forEach(each->{
							final ImageData img = getImageConverter().convert(each);
							receipts.add(img);
						});
						history.setReceipts(receipts);
					}

					if(entry.getDisbursement().getDisbursementPhoto()!=null) {
						List<ImageData> receipts = new ArrayList<ImageData>();
						entry.getDisbursement().getDisbursementPhoto().forEach(each->{
							final ImageData img = getImageConverter().convert(each);
							receipts.add(img);
						});
						history.setPhotos(receipts);
					}
				}
				dataList.add(history);
			});
		}		
		final SearchPageData<DisburseHistoryData> result = new SearchPageData<>();
		result.setPagination(searchResult.getPagination());
		result.setSorts(searchResult.getSorts());
		result.setResults(dataList);
		return result;
	}

	@Override
	public SearchPageData<ProductData> getProductsForScheme(String schemeCode, SearchPageData searchPageData, String giftType, Set<ProductOption> options) {
		CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion(baseSiteService.getCurrentBaseSite().getUid() + "ProductCatalog", "Online");
		SearchPageData<ProductModel> searchResult = schemesAndDiscountService.getProductsForScheme(catalogVersion, schemeCode, searchPageData, giftType);
		List<ProductData> list =  new ArrayList<ProductData>();
		if(searchResult!=null && searchResult.getResults()!=null) {
			list = getProductConverter().convertAll(searchResult.getResults());
			for(ProductData productData : list) {
			if (options != null)
			{
				ProductModel productModel = searchResult.getResults().stream().filter(product->product.getCode().equals(productData.getCode())).findAny().get();
				getProductConfiguredPopulator().populate(productModel, productData, options);
			}

			}
		}
		final SearchPageData<ProductData> result = new SearchPageData<>();
		result.setPagination(searchResult.getPagination());
		result.setSorts(searchResult.getSorts());
		result.setResults(list);
		return result;
	}	

	
	protected ConfigurablePopulator<ProductModel, ProductData, ProductOption> getProductConfiguredPopulator()
	{
		return productConfiguredPopulator;
	}

	@Required
	public void setProductConfiguredPopulator(
			final ConfigurablePopulator<ProductModel, ProductData, ProductOption> productConfiguredPopulator)
	{
		this.productConfiguredPopulator = productConfiguredPopulator;
	}
	
	public Converter<MediaModel, ImageData> getImageConverter() {
		return imageConverter;
	}

	public void setImageConverter(Converter<MediaModel, ImageData> imageConverter) {
		this.imageConverter = imageConverter;
	}
		
	public Converter<ProductModel, ProductData> getProductConverter() {
		return productConverter;
	}

	public void setProductConverter(Converter<ProductModel, ProductData> productConverter) {
		this.productConverter = productConverter;
	}

	@Override
	public Boolean updateGiftOrderStatus(String orderCode, String status, String reason) {
		return schemesAndDiscountService.updateGiftOrderStatus(orderCode, status, reason);
	}
	
	@Override
	public Boolean updateInfluencerPoint(String orderCode) {
		return schemesAndDiscountService.updateInfluencerPoint(orderCode);
	}

	@Override
	public Boolean sendRedeemRequestSmsOtp(String uid, String reqValue, String reqType,
			List<String> giftItems) {
		return smsOtpService.sendRedeemRequestSmsOtp(uid, reqValue, reqType, giftItems);
	}

	@Override
	public Boolean sendRedeemRequestPlacedSms(String uid, String reqValue, String reqType, List<String> giftItems) {
		return smsOtpService.sendRedeemRequestPlacedSms(uid, reqValue, reqType, giftItems);
	}

	@Override
	public Boolean saveFileRequest(SchemesFileRequestData schemesFileRequestData) {
		return schemesAndDiscountService.saveFileRequest(schemesFileRequestData);
	}

	@Override
	public SchemesListDetailData schemesList(List<String> applicableFor, SearchPageData searchPageData, String fields) {
		SchemesListDetailData list = new SchemesListDetailData();
		List<SchemesListData> dataList = new ArrayList<>();

		for(int i=0;i<10; i++){
			SchemesListData data = new SchemesListData();
			data.setName("Name "+i);
			data.setType("Type "+i);
			data.setGeography("Geography "+i);

			Date date = new Date();

			SimpleDateFormat formatter = new SimpleDateFormat("dd-mm-yyyy");
			String startDate = formatter.format(date);
			String endDate = formatter.format(date);
			data.setStartDate(startDate);
			data.setEndDate(endDate);

			dataList.add(data);
		}
		list.setSchemes(dataList);
		list.setTotalCount(String.valueOf(10));


		return list;
	}

	@Override
	public SchemesDetailData getSchemeDetail(String schemeCode) {
		SchemesDetailData data = new SchemesDetailData();

		data.setCode(schemeCode);
		data.setName("name");
		data.setType("schemeType");
		data.setGeography("geography");
		data.setTimeLeft("timeLeft");
		data.setDiscountActual("discountActual");
		data.setDiscountTarget("discountTarget");
		data.setVolumneActual("volumeActual");
		data.setVolumneTarget("VolumeTarget");

		Date date = new Date();

		SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
		String startDate = formatter.format(date);
		String endDate = formatter.format(date);

		data.setStartDate(startDate);
		data.setEndDate(endDate);

		SchemesPerformanceData performanceData = new SchemesPerformanceData();
		performanceData.setDisbursementDate(String.valueOf(new Date()));
		performanceData.setPartnerCode("partnerCode");
		performanceData.setPartnerName("partnerName");
		performanceData.setSlabAchieved("slabsAchieved");
		performanceData.setPartnerSale("partnerSale");

		List<SchemesSlabData> schemesSlabDataList = new ArrayList<>();
		for(int i=0;i<3;i++){
			SchemesSlabData slabData = new SchemesSlabData();
			slabData.setSlabNumber("slabNumber");
			slabData.setSlabAchievementQty("slabQty");

			schemesSlabDataList.add(slabData);
		}

		performanceData.setSlabs(schemesSlabDataList);
		data.setPerformance(performanceData);

		return data;
	}

    @Override
    public SearchPageData<GiftOrderData> getPaginatedGiftOrderList(SearchPageData searchPageData, String scheme, String searchFilter, List<String> status) {
		SearchPageData<OrderModel> searchResult = schemesAndDiscountService.getPaginatedGiftOrderList(searchPageData, scheme, searchFilter, status);


		List<GiftOrderData> dataList = new ArrayList<>();

		if (searchResult != null && searchResult.getResults() != null) {
			LOGGER.info(String.format("Size is  :: %s ", String.valueOf(searchResult.getResults().size())));
			List<OrderModel> list = searchResult.getResults();
			list.forEach(order -> {
				LOGGER.info(String.format("entered loop"));
				GiftOrderData data = new GiftOrderData();
				data.setName(order.getUser()!=null ? order.getUser().getName() : null);
				data.setCode(order.getUser()!=null ? ((SclCustomerModel)order.getUser()).getUid() : null);
				data.setRequestId(order.getCode());
				data.setRequestType(order.getGiftType().getCode());
				/*data.setScheme(order.getGiftShop()!=null ? order.getGiftShop().getName() : null);*/
				SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss");
				if(order.getDate()!=null)
				{
					data.setDate(formatter.format(order.getDate()));
				}
				data.setRejectionReason(order.getRejectionReason());
				data.setPointsRedeemed(order.getTotalPrice());
				if(order.getStatus()!=null){
					data.setStatus(null != enumerationService.getEnumerationName(order.getStatus(), i18NService.getCurrentLocale()) ? enumerationService.getEnumerationName(order.getStatus(), i18NService.getCurrentLocale()) : order.getStatus().getCode());
				}
				AddressData address = new AddressData();
				addressPopulator.populate(order.getDeliveryAddress(), address);
				data.setAddress(address);

				dataList.add(data);
				LOGGER.info(String.format("exit loop ::"));
			});
		}

		final SearchPageData<GiftOrderData> result = new SearchPageData<>();
		result.setPagination(searchResult.getPagination());
		result.setSorts(searchResult.getSorts());
		result.setResults(dataList);
		LOGGER.info(String.format("set results ::"));
		return result;
    }

	@Override
	public SearchPageData<GiftShopMessageData> getPaginatedGiftShopSchemeList(SearchPageData searchPageData, String scheme, String searchFilter, List<String> status) {
		SearchPageData<GiftShopModel> searchResult = schemesAndDiscountService.getPaginatedGiftShopSchemeList(searchPageData, scheme, searchFilter, status);


		List<GiftShopMessageData> dataList = new ArrayList<>();

		if (searchResult != null && searchResult.getResults() != null) {
			LOGGER.info(String.format("Size is  :: %s ", String.valueOf(searchResult.getResults().size())));
			List<GiftShopModel> list = searchResult.getResults();
			list.forEach(schemes -> {
				LOGGER.info(String.format("entered loop"));
				GiftShopMessageData data = new GiftShopMessageData();
				data.setSchemeName(schemes.getName()!=null ? schemes.getName() : null);
				data.setSchemeCode(schemes.getCode()!=null ? schemes.getCode() : null);
				dataList.add(data);
				LOGGER.info(String.format("exit loop ::"));
			});
		}

		final SearchPageData<GiftShopMessageData> result = new SearchPageData<>();
		result.setPagination(searchResult.getPagination());
		/*result.setSorts(searchResult.getSorts());*/
		result.setResults(dataList);
		LOGGER.info(String.format("set results ::"));
		return result;
	}
}
