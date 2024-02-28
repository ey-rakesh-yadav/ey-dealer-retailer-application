package com.eydms.occ.controllers;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletResponse;

import com.eydms.facades.data.*;
import com.eydms.occ.annotation.ApiBaseSiteIdAndTerritoryParam;
import com.eydms.occ.dto.GiftOrderListWsDTO;
import com.eydms.occ.dto.GiftShopListWsDTO;
import de.hybris.platform.webservicescommons.pagination.WebPaginationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import com.eydms.facades.SchemesAndDiscountFacade;
import com.eydms.facades.order.impl.DefaultEYDMSB2BCheckoutFacade;
import com.eydms.occ.dto.DisburseHistoryListWsDTO;
import com.eydms.occ.security.EyDmsSecuredAccessConstants;

import de.hybris.platform.b2bacceleratorfacades.api.cart.CartFacade;
import de.hybris.platform.b2bacceleratorfacades.checkout.data.PlaceOrderData;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cmswebservices.dto.CategoryWsDTO;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.product.ProductOption;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.commercewebservicescommons.dto.order.OrderWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.product.ProductListWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.product.ProductWsDTO;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.PaymentAuthorizationException;
import de.hybris.platform.commercewebservicescommons.strategies.CartLoaderStrategy;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.search.paginated.util.PaginatedSearchUtils;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import de.hybris.platform.webservicescommons.mapping.FieldSetLevelHelper;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(value = "{baseSiteId}/users/{userId}/schemes/")
@ApiVersion("v2")
@Tag(name = "Schemes And Discount Management")
@PermitAll
public class SchemesAndDiscountsController extends EYDMSB2BOrdersController{

	@Autowired
	SchemesAndDiscountFacade schemesFacade;

	@Resource(name = "cartLoaderStrategy")
	private CartLoaderStrategy cartLoaderStrategy;

	@Resource(name = "b2bCartFacade")
	private CartFacade cartFacade;

	@Resource(name = "dataMapper")
	private DataMapper dataMapper;
	
	@Resource(name = "defaultEYDMSB2BCheckoutFacade")
	private DefaultEYDMSB2BCheckoutFacade b2bCheckoutFacade;

	@Autowired
	WebPaginationUtils webPaginationUtils;
	
	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/current", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public CategoryWsDTO getCategoryByCode(@RequestParam final String geography, @RequestParam final String counterType, @RequestParam(required = false) final String influencerType, @RequestParam final String giftType) throws CMSItemNotFoundException, ConversionException
	{
		return null;
//		return getDataMapper().map(schemesFacade.getCurrentSchemesByGeography(geography, counterType, influencerType), CategoryWsDTO.class);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/currentYearSchemes", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public CurrentYearSchemeListData getCurrentYearSchemes(@RequestParam(required = false) final String influencerCode, @RequestParam final String geography, @RequestParam final String counterType, @RequestParam(required = false) final String influencerType) throws CMSItemNotFoundException, ConversionException
	{
		return schemesFacade.getCurrentYearSchemes(geography, counterType, influencerType, influencerCode);
	}


	
	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/{customerCode}/totalAvailablePoints", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public Double getTotalAvailablePoints(@PathVariable final String customerCode)
	{
		return schemesFacade.getTotalAvailablePoints(customerCode);
	}
	
	@Secured(
			{ EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_GUEST,
				EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT })
	@RequestMapping(value = "/{customerCode}/claimGift", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public Boolean placeOrgOrder(
			@PathVariable final String customerCode,
			@RequestParam(required = true) final String cartId,
			@ApiFieldsParam @RequestParam(required = false, defaultValue = FieldSetLevelHelper.DEFAULT_LEVEL) final String fields)
					throws InvalidCartException, PaymentAuthorizationException
	{
//		cartLoaderStrategy.loadCart(cartId);
//		final CartData cartData = cartFacade.getCurrentCart();
//
//		PlaceOrderData placeOrderData = new PlaceOrderData();
//
//		return schemesFacade.claimGift(placeOrderData);
		return true;
	}

	@Secured({"ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP","ROLE_CUSTOMERGROUP" })
	@GetMapping(value="/giftOrderList")
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public GiftOrderListWsDTO getGiftOrderList(@RequestParam(name = "schemeId", required = false) final String scheme,
												  @RequestParam(name = "search", required = false) final String searchFilter,
												  @RequestParam(name = "status", required = false) final List<String> status,
												  @RequestParam(name = "currentPage", required = false, defaultValue = "0") final int currentPage,
												  @RequestParam(name = "pageSize", required = false, defaultValue = "10") final int pageSize,
												  @RequestParam(name = "sort", defaultValue = "modifiedtime:desc") final String sort,
												  @RequestParam(name = "needsTotal", required = false, defaultValue = "true") final boolean needsTotal,
												  @ApiFieldsParam @RequestParam(defaultValue =
														  DEFAULT_FIELD_SET) final String fields
	) {
		final SearchPageData searchPageData = webPaginationUtils.buildSearchPageData(sort, currentPage, pageSize, needsTotal);
		recalculatePageSize(searchPageData);
		GiftOrderListData giftOrderListData = new GiftOrderListData();
		SearchPageData<GiftOrderData> paginatedList = schemesFacade.getPaginatedGiftOrderList(searchPageData,scheme,searchFilter,status);
		giftOrderListData.setGiftOrders(paginatedList.getResults());
		return getDataMapper().map(giftOrderListData, GiftOrderListWsDTO.class, fields);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/schemesFilter", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public GiftShopListWsDTO getSchemesFilter(@RequestParam(name = "schemeId", required = false) final String scheme,
											  @RequestParam(name = "search", required = false) final String searchFilter,
											  @RequestParam(name = "status", required = false) final List<String> status,
											  @RequestParam(name = "currentPage", required = false, defaultValue = "0") final int currentPage,
											  @RequestParam(name = "pageSize", required = false, defaultValue = "10") final int pageSize,
											  @RequestParam(name = "needsTotal", required = false, defaultValue = "true") final boolean needsTotal,
											  @ApiFieldsParam @RequestParam(defaultValue =
															DEFAULT_FIELD_SET) final String fields
	) {/*
		final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);
		GiftShopMessageListData giftShopMessageListData = new GiftShopMessageListData();
		SearchPageData<GiftShopMessageData> paginatedList = schemesFacade.getPaginatedGiftShopSchemeList(searchPageData,scheme,searchFilter,status);
		giftShopMessageListData.setGiftList(paginatedList.getResults());
		return getDataMapper().map(giftShopMessageListData, GiftShopListWsDTO.class, fields);*/
		return null;
	}
	

	@Secured(
	{ EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_GUEST,
			EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT })
	@RequestMapping(value = "/claimGifts", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public OrderWsDTO placeOrgOrder(
			@Parameter(description = "Cart identifier: cart code for logged in user, cart guid for anonymous user, 'current' for the last modified cart", required = true) @RequestParam(required = true) final String cartId,
			@Parameter(description = "Whether terms were accepted or not.", required = true) @RequestParam(required = true) final boolean termsChecked, @RequestParam(required = false) final String ncrGapAcceptanceReason,
			@ApiFieldsParam @RequestParam(required = false, defaultValue = FieldSetLevelHelper.DEFAULT_LEVEL) final String fields, @RequestParam(required = false) String schemeCode,  @RequestParam String influencerCode)
			throws InvalidCartException, PaymentAuthorizationException
	{
		cartLoaderStrategy.loadCart(cartId);
		schemesFacade.updateCart(schemeCode, influencerCode);

		validateUser();
		cartLoaderStrategy.loadCart(cartId);
		final CartData cartData = cartFacade.getCurrentCart();

		validateCart(cartData);
		PlaceOrderData placeOrderData = new PlaceOrderData();
		OrderData order = b2bCheckoutFacade.placeOrder(placeOrderData);
		schemesFacade.updateInfluencerPoint(order.getCode());
		OrderWsDTO orderWsDto = dataMapper.map(order, OrderWsDTO.class, fields);
		return orderWsDto;
	}
	
	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/giftRedeemList", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public GiftOrderListData getGiftRedeemList(@PathVariable final String userId, @RequestParam(required = false) final String status, @RequestParam(required = false) final String searchKey, @RequestParam(required = false) final String district, @RequestParam(required = false) final String taluka) 
	{
		return schemesFacade.getGiftRedeemList(userId, status, searchKey, district, taluka);
	}
	
	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/rejectRedeemRequest", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public Boolean rejectRedeemRequest(@RequestParam final String requestId, @RequestParam final String rejectionReason) 
	{
		return schemesFacade.rejectRedeemRequest(requestId, rejectionReason);
	}
	
	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/approveRedeemRequest", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public Boolean approveRedeemRequest(@RequestParam final String requestId) 
	{
		return schemesFacade.approveRedeemRequest(requestId);
	}
	
	
	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/disbursementHistory", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public DisburseHistoryListWsDTO getDisbursementHistory(@RequestParam(required = false) final String customerCode, @RequestParam(required = false) String schemeCode, @RequestParam(required = false) String orderCode
			,@RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate, @RequestParam Boolean attachment
			,@ApiFieldsParam @RequestParam(required = false, defaultValue = FieldSetLevelHelper.DEFAULT_LEVEL) final String fields
			,@Parameter(description = "Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage
			,@Parameter(description = "Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize
			,final HttpServletResponse response)
	{
		final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);
		DisburseHistoryListData listData = new DisburseHistoryListData();
		SearchPageData<DisburseHistoryData> respone = schemesFacade.getDisbursementHistory(customerCode, schemeCode, startDate, endDate, attachment, searchPageData, fields, orderCode);
		listData.setDisburseHistory(respone.getResults());

		if (respone.getPagination() != null)
		{
			response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(respone.getPagination().getTotalNumberOfResults()));
		}
		return getDataMapper().map(listData, DisburseHistoryListWsDTO.class, fields);

	}


	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/productsForScheme", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public ProductListWsDTO getProductForScheme(@RequestParam String schemeCode, @RequestParam String giftType
			,@ApiFieldsParam @RequestParam(required = false, defaultValue = FieldSetLevelHelper.DEFAULT_LEVEL) final String fields
			,@Parameter(description = "Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage
			,@Parameter(description = "Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize
			,final HttpServletResponse response)
	{
		/*final Set<ProductOption> opts = EnumSet.of(ProductOption.BASIC, ProductOption.PRICE, ProductOption.IMAGES);
		final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);
		SearchPageData<ProductData> productSearchResult = schemesFacade.getProductsForScheme(schemeCode, searchPageData, giftType, opts);

		if (productSearchResult.getPagination() != null)
		{
			response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(productSearchResult.getPagination().getTotalNumberOfResults()));
		}
		
		final ProductListWsDTO productList = new ProductListWsDTO();
		productList.setProducts(productSearchResult //
				.getResults() //
				.stream() //
				.map(productData -> getDataMapper().map(productData, ProductWsDTO.class)) //
				.collect(Collectors.toList()));
		//productList.setPagination(getWebPaginationUtils().buildPagination(productSearchResult));
		return productList;*/
		return null;
	}
	
	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/updateGiftOrderStatus", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public Boolean updateGiftOrderStatus(@RequestParam final String orderCode, @RequestParam final String status, @RequestParam(required = false) final String reason)
	{
		Boolean result = schemesFacade.updateGiftOrderStatus(orderCode, status, reason);
		return result;
	}


	@RequestMapping(value = "/fileRequest", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public Boolean fileRequest(@RequestBody SchemesFileRequestData schemesFileRequestData)
	{
		return schemesFacade.saveFileRequest(schemesFileRequestData);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/schemesList", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public SchemesListDetailWsDTO getSchemesList(@RequestParam(required = false) List<String> applicableFor
			,@ApiFieldsParam @RequestParam(required = false, defaultValue = FieldSetLevelHelper.DEFAULT_LEVEL) final String fields
			,@Parameter(description = "Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage
			,@Parameter(description = "Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize
			,final HttpServletResponse response)
	{
		final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);

		SchemesListDetailData listData = schemesFacade.schemesList(applicableFor, searchPageData, fields);

		return getDataMapper().map(listData, SchemesListDetailWsDTO.class, fields);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/schemeDetail", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public SchemesDetailData getSchemeDetail(@RequestParam String schemeCode){
		return schemesFacade.getSchemeDetail(schemeCode);
	}
	
}
