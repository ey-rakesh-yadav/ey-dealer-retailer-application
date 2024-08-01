package com.scl.core.services;

import java.util.List;
import java.util.Map;

import com.scl.core.enums.GiftType;
import com.scl.core.enums.TransactionType;
import com.scl.core.model.*;
import com.scl.facades.data.GiftOrderListData;

import com.scl.facades.data.SchemesFileRequestData;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

public interface SchemesAndDiscountService {

	GiftSchemeModel getCurrentSchemesByGeography(String geography,
												 String influencerType);

	List<List<Object>> getCurrentYearSchemes(CatalogVersionModel catalogVersion, String geography,
			String counterType, String influencerType);

	GiftOrderModel claimGift(CartModel cartModel);

	Double getTotalAvailablePoints(String customerCode);

	Boolean updateInfluencerPoint(SclCustomerModel customer, double redeemPoint, TransactionType transactionType,
			GiftSchemeModel sheme, String orderCode);

	List<OrderModel> getGiftRedeemList(String userId, String status, String searchKey, String district,  String taluka);

	Boolean rejectRedeemRequest(String requestId, String rejectionReason);
	
	Boolean approveRedeemRequest(String requestId);

	SearchPageData<ProductModel> getProductsForScheme(CatalogVersionModel catalogVersion, String schemeCode,
			SearchPageData searchPageData, String giftType);

	SearchPageData<OrderEntryModel> getDisbursementHistory(CatalogVersionModel catalogVersion, String customerCode,
			String schemeCode, String startDate, String endDate, SearchPageData searchPageData, String orderCode);

	String findRecentGiftOrder(String influencerCode, GiftType cash, GiftShopModel giftShop);

	Boolean updateGiftOrderStatus(String orderCode, String status, String reason);

	Boolean updateInfluencerPoint(String orderCode);

    Boolean saveFileRequest(SchemesFileRequestData schemesFileRequestData);

    SearchPageData<OrderModel> getPaginatedGiftOrderList(SearchPageData searchPageData, String scheme, String searchFilter, List<String> status);

	SearchPageData<GiftShopModel> getPaginatedGiftShopSchemeList(SearchPageData searchPageData, String scheme, String searchFilter, List<String> status);
}
