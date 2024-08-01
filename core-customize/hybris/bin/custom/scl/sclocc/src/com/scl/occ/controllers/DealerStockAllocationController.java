package com.scl.occ.controllers;

import com.scl.facades.DealerFacade;
import com.scl.facades.data.InvoiceListData;
import com.scl.facades.data.LiftingDateRangeData;
import com.scl.facades.data.ProductStockAllocationListData;
import com.scl.facades.exception.SclException;
import com.scl.occ.dto.source.InvoiceListWsDTO;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.webservicescommons.cache.CacheControl;
import de.hybris.platform.webservicescommons.cache.CacheControlDirective;
import de.hybris.platform.webservicescommons.pagination.WebPaginationUtils;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;


@RestController
@RequestMapping(value = "/{baseSiteId}/users/{userId}/dealerStockAllocation")
@ApiVersion("v2")
@CacheControl(directive = CacheControlDirective.PRIVATE)
@Tag(name = "Dealer Stock Allocation")
@PermitAll
public class DealerStockAllocationController extends SclBaseController
{
	private static final Logger LOG = LoggerFactory.getLogger(DealerStockAllocationController.class);


	@Autowired
	DealerFacade dealerFacade;

	@Resource(name = "webPaginationUtils")
	private WebPaginationUtils webPaginationUtils;


	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/getLiftingDateRange",method = RequestMethod.GET)
	@ResponseBody
	@Operation(operationId = "getLiftingDateRange", summary = "Get Lifting DateRange For Dealer", description = "Return Lifting DateRange.")
	@ApiBaseSiteIdAndUserIdParam
	@ApiResponse(responseCode = "200",description = "Return Lifting Date Range")
	public LiftingDateRangeData getLiftingDateRange(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
													@PathVariable final String userId,
													@Parameter(description = "Retailer/Influencer Uid") @RequestParam(required = false) final String customerId) throws SclException {
	    LOG.info(String.format("Get LiftingDateRange API called for User:- %s and Retailer/Influencer Uid:- %s",userId,customerId));
        try {
            return dealerFacade.getLiftingDateRange(customerId);
        } catch (Exception e) {
            throw new SclException(e.getMessage());
        }
    }


	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value="/getProductListForStockAllocation", method = RequestMethod.GET)
	@ResponseBody
	@Operation(operationId = "getProductListForStockAllocation", summary = "Product List For Stock Allocation.", description = "Return Product List For Stock Allocation.")
	@ApiBaseSiteIdAndUserIdParam
	@ApiResponse(responseCode = "200",description = "List of Product For Stock Allocation")
	public ProductStockAllocationListData getProductListForStockAllocation(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
																		   @Parameter(description = "dealer uid") @RequestParam final String dealerUid,
																		   @Parameter(description = "Retailer/Influencer Uid") @RequestParam(required = false) final String customerId,
																		   @Parameter(description ="Lifting Date (yyyy-mm-dd)") @RequestParam final String selectedLiftingDate) throws SclException {
		LOG.info(String.format("Get ProductListForStockAllocation API called for dealer :- %s or Retailer/Influencer Uid :- %s, with selected LiftingDate:- %s",dealerUid, customerId,selectedLiftingDate));

        try {
            if(dealerFacade.isValidSelectedDate(selectedLiftingDate,customerId)){
                return dealerFacade.getProductListForStockAllocation(dealerUid,customerId,selectedLiftingDate);
            }else{
                LOG.info(String.format("Selected Lifting Date %s is not valid for dealer :- %s or Retailer/Influencer Uid :- %s",selectedLiftingDate,dealerUid, customerId));
                throw new SclException("Selected Lifting Date is not valid..");
            }
        } catch (Exception e) {
            throw new SclException(e.getMessage());
        }

    }

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value="/getInvoiceListForProduct", method = RequestMethod.GET)
	@ResponseBody
	@Operation(operationId = "getInvoiceListForProduct", summary = "Invoice List For Product.", description = "Return Invoice List For Product.")
	@ApiBaseSiteIdAndUserIdParam
	@ApiResponse(responseCode = "200",description = "List of Invoice For Product")
	public InvoiceListWsDTO getInvoiceListForProduct(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
													@Parameter(description = "dealer uid") @RequestParam final String dealerUid,
													@Parameter(description = "product Code") @RequestParam final String productCode,
													 @Parameter(description = "product Alias") @RequestParam final String productAlias,
													@Parameter(description = "quantity") @RequestParam final Double quantity,
													 @Parameter(description = "Retailer/Influencer Uid") @RequestParam(required = false) final String customerId,
													@Parameter(description=  "Search term") @RequestParam(required = false) final String filter,
													@Parameter(description = "Lifting Date (yyyy-mm-dd)") @RequestParam final String selectedLiftingDate,
													@RequestParam(name = "sort", required = false,defaultValue = "invoiceddate:asc,invoicenumber:asc") final String sort,
													@RequestParam(name = "needsTotal", required = false, defaultValue = "true") final boolean needsTotal,
													@RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,
													@RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize) throws SclException {
		LOG.info(String.format("Get InvoiceListForProduct API called for dealer :- %s or Retailer/Influencer Uid :- %s with selected LiftingDate:- %s",dealerUid, customerId,selectedLiftingDate));
		final SearchPageData searchPageData = getWebPaginationUtils().buildSearchPageData(sort, currentPage, pageSize, needsTotal);
		recalculatePageSize(searchPageData);
        try {
            if(dealerFacade.isValidSelectedDate(selectedLiftingDate, customerId)){

                final InvoiceListData invoiceListData= dealerFacade.getInvoiceListForProduct(searchPageData,dealerUid,customerId,selectedLiftingDate,productCode,productAlias,quantity,filter);
				return getDataMapper().map(invoiceListData, InvoiceListWsDTO.class, fields);

			}else{
                LOG.info(String.format("Selected Lifting Date %s is not valid for dealer :- %s or Retailer/Influencer Uid :- %s",selectedLiftingDate,dealerUid, customerId));
                throw new SclException("Selected Lifting Date is not valid..");
            }
        } catch (Exception e) {
            throw new SclException(e.getMessage());
        }

    }
	protected WebPaginationUtils getWebPaginationUtils() {
		return webPaginationUtils;
	}



}
