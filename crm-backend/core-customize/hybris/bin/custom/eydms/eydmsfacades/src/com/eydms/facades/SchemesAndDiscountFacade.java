package com.eydms.facades;

import com.eydms.facades.data.*;

import de.hybris.platform.b2bacceleratorfacades.checkout.data.PlaceOrderData;
import de.hybris.platform.cmsfacades.data.CategoryData;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.commercefacades.product.ProductOption;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.List;
import java.util.Set;

public interface SchemesAndDiscountFacade {

//	CategoryData getCurrentSchemesByGeography(String geography, String counterType, String influencerType);

	CurrentYearSchemeListData getCurrentYearSchemes(String geography, String counterType, String influencerType, String influencerCode);

	AbstractOrderData claimGift(PlaceOrderData placeOrderData);

	Double getTotalAvailablePoints(String customerCode);

	void updateCart(String schemeCode, String influencerCode);
	
	GiftOrderListData getGiftRedeemList(String userId, String status, String searchKey, String district,  String taluka);

	Boolean rejectRedeemRequest(String requestId, String rejectionReason);
	
	Boolean approveRedeemRequest(String requestId);

	SearchPageData<DisburseHistoryData> getDisbursementHistory(String customerCode, String schemeCode, String startDate,
			String endDate, Boolean attachment, SearchPageData searchPageData, String fields, String orderCode);

	SearchPageData<ProductData> getProductsForScheme(String schemeCode, SearchPageData searchPageData, String giftType,
			Set<ProductOption> opts);

	Boolean updateGiftOrderStatus(String orderCode, String status, String reason);

	Boolean updateInfluencerPoint(String orderCode);
	
	Boolean sendRedeemRequestSmsOtp(String uid, String reqValue, String reqType,
			List<String> giftItems);
	
	Boolean sendRedeemRequestPlacedSms(String uid, String reqValue, String reqType,
			List<String> giftItems);

	Boolean saveFileRequest(SchemesFileRequestData schemesFileRequestData);

	SchemesListDetailData schemesList(List<String> applicableFor, SearchPageData searchPageData, String fields);

	SchemesDetailData getSchemeDetail(String schemeCode);

    SearchPageData<GiftOrderData> getPaginatedGiftOrderList(SearchPageData searchPageData, String scheme, String searchFilter, List<String> status);

	SearchPageData<GiftShopMessageData> getPaginatedGiftShopSchemeList(SearchPageData searchPageData, String scheme, String searchFilter, List<String> status);
}
