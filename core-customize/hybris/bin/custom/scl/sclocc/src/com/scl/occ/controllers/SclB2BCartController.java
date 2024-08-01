package com.scl.occ.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.scl.facades.cart.SclB2BCartFacade;
import com.scl.facades.order.data.SclOrderHistoryData;
import com.scl.facades.order.data.SclOrderHistoryListData;
import com.scl.occ.annotation.ApiBaseSiteIdAndUserIdAndTerritoryParam;
import com.scl.occ.dto.order.SclOrderHistoryListWsDTO;

import de.hybris.platform.b2bacceleratorfacades.api.cart.CartFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.CartModificationData;
import de.hybris.platform.commercefacades.order.data.CartModificationDataList;
import de.hybris.platform.commercefacades.order.data.DeliveryModeData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.commerceservices.request.mapping.annotation.RequestMappingOverride;
import de.hybris.platform.commercewebservices.core.order.data.CartDataList;
import de.hybris.platform.commercewebservicescommons.annotation.SiteChannelRestriction;
import de.hybris.platform.commercewebservicescommons.dto.order.CartListWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.order.CartModificationListWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.order.CartWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.order.OrderEntryListWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.order.OrderEntryWsDTO;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.search.paginated.util.PaginatedSearchUtils;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import de.hybris.platform.webservicescommons.mapping.FieldSetLevelHelper;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdUserIdAndCartIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import static com.scl.occ.constants.ScloccConstants.OCC_REWRITE_OVERLAPPING_BASE_SITE_USER_PATH;

@Controller
@ApiVersion("v2")
@Tag(name = "SCL B2B Carts")
public class SclB2BCartController extends SclBaseController {
	protected static final String API_COMPATIBILITY_B2B_CHANNELS = "api.compatibility.b2b.channels";

	@Resource(name = "b2bCartFacade")
	private CartFacade cartFacade;

	@Resource(name = "userFacade")
	protected UserFacade userFacade;

	@Resource(name = "dataMapper")
	protected DataMapper dataMapper;

	@Resource(name = "b2BCartAddressValidator")
	private Validator b2BCartAddressValidator;

	@Resource(name = "b2BOrderEntriesCreateValidator")
	private Validator b2BOrderEntriesCreateValidator;

	@Resource
	private SclB2BCartFacade sclB2BCartFacade;

	@Operation(operationId = "doAddOrgCartEntries", summary = "Adds more quantity to the cart of specific products", description = "Updates the details of specified products in the cart, based either on the product code or the entryNumber.")
	@RequestMappingOverride(priorityProperty = "sclocc.CartResource.addCartEntry.priority")
	@SiteChannelRestriction(allowedSiteChannelsProperty = API_COMPATIBILITY_B2B_CHANNELS)
	@RequestMapping(value = OCC_REWRITE_OVERLAPPING_BASE_SITE_USER_PATH
			+ "/carts/{cartId}/entries/", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	@ApiBaseSiteIdUserIdAndCartIdParam
	public CartModificationListWsDTO addCartEntries(
			@Parameter(description = "Base site identifier.") @PathVariable final String baseSiteId,
			@ApiFieldsParam @RequestParam(required = false, defaultValue = FieldSetLevelHelper.DEFAULT_LEVEL) final String fields,
			@Parameter(description = "List of entries containing the amount to add and the product code or the entryNumber.") @RequestBody(required = true) final OrderEntryListWsDTO entries) {
		entries.getOrderEntries().forEach(entry->{
			if(entry.getQuantityMT() != null)
				entry.setQuantity((long) (entry.getQuantityMT()*1000));
		});
		validate(entries, "orderEntries", b2BOrderEntriesCreateValidator);

		final List<OrderEntryData> cartEntriesData = convertToData(entries);
		final List<CartModificationData> resultList = cartFacade.addOrderEntryList(cartEntriesData);

		return dataMapper.map(getCartModificationDataList(resultList), CartModificationListWsDTO.class, fields);
	}

	protected CartModificationDataList getCartModificationDataList(final List<CartModificationData> result) {
		final CartModificationDataList cartModificationDataList = new CartModificationDataList();
		cartModificationDataList.setCartModificationList(result);
		return cartModificationDataList;
	}

	protected List<OrderEntryData> convertToData(final OrderEntryListWsDTO entriesWS) {
		final List<OrderEntryData> entriesData = new ArrayList<>();
		for (final OrderEntryWsDTO entryDto : entriesWS.getOrderEntries()) {
			final OrderEntryData entryData = new OrderEntryData();
			entryData.setQuantity(entryDto.getQuantity());
			entryData.setProduct(new ProductData());
			entryData.getProduct().setCode(entryDto.getProduct().getCode());
			entryData.getProduct().setName(entryDto.getProduct().getName());
			entryData.setEntryNumber(entryDto.getEntryNumber());
			entryData.setSelectedDeliveryDate(entryDto.getSelectedDeliveryDate());
			entryData.setSelectedDeliverySlot(entryDto.getSelectedDeliverySlot());
			entryData.setCalculatedDeliveryDate(entryDto.getCalculatedDeliveryDate());
			entryData.setCalculatedDeliverySlot(entryDto.getCalculatedDeliverySlot());	
			entryData.setSequence(entryDto.getSequence());
			entryData.setTruckNo(entryDto.getTruckNo());
			entryData.setDriverContactNo(entryDto.getDriverContactNo());
			entryData.setQuantityMT(entryDto.getQuantityMT());
			entryData.setWarehouseCode(entryDto.getWarehouseCode());
			entryData.setRouteId(entryDto.getRouteId());
			entryData.setRemarks(entryDto.getRemarks());
			entryData.setAddressPk(entryDto.getAddressPk());
			entryData.setRetailerUid(entryDto.getRetailerUid());
			entryData.setIncoTerm(entryDto.getIncoTerm());
			entryData.setOrderFor(entryDto.getOrderFor());
			entryData.setIsDealerProvidingOwnTransport(entryDto.getIsDealerProvidingOwnTransport());
			entryData.setDeliveryMode(new DeliveryModeData());
			entryData.getDeliveryMode().setCode(entryDto.getDeliveryMode().getCode());
			entryData.setOrderRequisitionId(entryDto.getOrderRequisitionId());
			entryData.setIsPartnerCustomer(entryDto.getIsPartnerCustomer());
			entryData.setPlacedByCustomer(entryDto.getPlacedByCustomer());
			entriesData.add(entryData);
		}
		return entriesData;
	}


	@ResponseBody
	@Operation(operationId = "getCarts", summary = "Get all carts.", description = "Lists all carts saved by current user.")
	@RequestMapping(value = OCC_REWRITE_OVERLAPPING_BASE_SITE_USER_PATH + "/getcarts/", method = RequestMethod.POST)
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public SclOrderHistoryListWsDTO getCarts(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields, 
			@Parameter(description = "Optional pagination parameter. Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,
			@Parameter(description = "Product Name field") @RequestParam(required = false) final String productName,
			@Parameter(description = "Order Type  field") @RequestParam(required = false) final String orderType,
			@Parameter(description = "Optional {@link PaginationData} parameter in case of savedCartsOnly == true. Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize, final HttpServletResponse response, 
			@Parameter(description = "Filters by Month") @RequestParam(required = false) Integer month, @Parameter(description = "Filters by Year") @RequestParam(required = false) Integer year, @RequestParam(required = false) final String filter)
	{
		if (userFacade.isAnonymousUser())
		{
			throw new AccessDeniedException("Access is denied");
		}
		
		int month1 = 0, year1 = 0;
		if(month!=null)
			month1 = month.intValue();
		if(year!=null)
			year1 = year.intValue();	
		
		SclOrderHistoryListData sclOrderHistoryListData = new SclOrderHistoryListData();
		final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);
		final SearchPageData<SclOrderHistoryData> sclOrderHistoryData = sclB2BCartFacade.getSavedCartsBySavedBy(searchPageData, filter, month1, year1,productName,orderType);
		sclOrderHistoryListData.setOrdersList(sclOrderHistoryData.getResults());
		
		if (sclOrderHistoryData.getPagination() != null)
		{
			response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(sclOrderHistoryData.getPagination().getTotalNumberOfResults()));
		}
		
		return getDataMapper().map(sclOrderHistoryListData, SclOrderHistoryListWsDTO.class, fields);
	}


	
	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = OCC_REWRITE_OVERLAPPING_BASE_SITE_USER_PATH + "/carts/{cartId}/details/", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public CartWsDTO getCartForCode(@PathVariable final String cartId,
			@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		CartData cartData = cartFacade.getCurrentCart();
        return getDataMapper().map(cartData, CartWsDTO.class, fields);
	}
	
}
