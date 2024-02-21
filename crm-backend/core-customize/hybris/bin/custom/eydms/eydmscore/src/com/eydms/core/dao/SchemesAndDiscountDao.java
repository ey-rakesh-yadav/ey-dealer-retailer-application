package com.eydms.core.dao;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.eydms.core.enums.GiftType;
import com.eydms.core.model.*;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

public interface SchemesAndDiscountDao {

	Collection<GiftShopModel> findCategoriesByGeographyAndPeroid(CatalogVersionModel catalogVersion, String geography, String counterType, String influencerType,
			LocalDate now);

	List<List<Object>> findCurrentYearSchemes(CatalogVersionModel catalogVersion, String geography,
			String counterType, String influencerType, String financiyalYearFrom, String financiyalYearTo);

	List<OrderModel> getGiftOrdersForDistrictAndTaluka(List<String> districtSubarea, String status, String key);

	SearchPageData<ProductModel> getProductsForScheme(CategoryModel giftShop, SearchPageData searchPageData,
			CategoryModel giftType);

	SearchPageData<OrderEntryModel> getDisbursementHistory(UserModel user, GiftShopModel giftShop, String startDate,
			String endDate, BaseSiteModel currentBaseSite, SearchPageData searchPageData, String orderCode);

	String findRecentGiftOrder(UserModel customer, GiftType giftType, GiftShopModel giftShop);

	PointsTransactionMasterModel findPointsTransactionMasterByOrderCode(String orderCode);

    SearchPageData<OrderModel> getPaginatedGiftOrderList(SearchPageData searchPageData, String scheme, String searchFilter, List<String> status);


	SearchPageData<GiftShopModel> getPaginatedGiftShopSchemeList(SearchPageData searchPageData, String scheme, String searchFilter, List<String> status);


	Collection<GiftSchemeModel> findGiftSchemeByStateAndPeriod(String state, String influencerType, LocalDate now);
}
