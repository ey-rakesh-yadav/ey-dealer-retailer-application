package com.scl.occ.controllers;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.scl.facades.data.*;
import com.scl.occ.annotation.ApiBaseSiteIdAndUserIdAndTerritoryParam;
import com.scl.occ.dto.CollectionDealerOutstandingDetailsListWsDTO;
import com.scl.occ.dto.NetworkAssitanceListWsDTO;
import com.scl.occ.dto.SPInvoiceListWsDto;
import com.scl.occ.security.SclSecuredAccessConstants;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.search.paginated.util.PaginatedSearchUtils;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.scl.facades.CollectionFacade;

import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.search.paginated.util.PaginatedSearchUtils;
import de.hybris.platform.webservicescommons.mapping.FieldSetLevelHelper;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(value = "/{baseSiteId}/users/{userId}/collection")
@ApiVersion("v2")
@Tag(name = "Collection")
public class CollectionController extends SclBaseController{
	
	@Resource
	CollectionFacade collectionFacade;


	@Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/outstanding", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public CollectionOutstandingData getOutstandingData()
	{
		return collectionFacade.getOutstandingData();
	}

	@Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/dealerOutstanding", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public CollectionDealerOutstandingDetailsListData getDealerOutstandingDetails()
	{
		return collectionFacade.getDealerOutstandingDetails();
	}

	@Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/topDealerOutstanding", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public TopDealerOutstandingListData topDealerOutstanding()
	{
		return collectionFacade.topDealerOutstanding();
	}
	
	@RequestMapping(value = "/dealerDetails", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public CollectionDealerDetailsData getDealerDetails(@Parameter(description = "dealerCode") @RequestParam(required = true) String dealerCode)
	{
		return collectionFacade.getDealerDetails(dealerCode);
	}
	
	@RequestMapping(value = "/dealerCreditDetails", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public CollectionCreditDetailsListData getDealerCreditDetails(@Parameter(description = "dealerCode") @RequestParam(required = true) String dealerCode)
	{
		return collectionFacade.getDealerCreditDetails(dealerCode);
	}
	
	@RequestMapping(value = "/ledgerDetails", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public CollectionLedgerListData getLedgerDetails(@Parameter(description = "dealerCode") @RequestParam(required = true) String dealerCode, @Parameter(description = "isDebit") @RequestParam(required = false) Boolean isDebit,
			@Parameter(description = "isCredit") @RequestParam(required = false) Boolean isCredit, @RequestParam(required = false) final String startDate, @RequestParam(required = false) final String endDate)
	{
		return collectionFacade.getLedgerDetails(dealerCode, isDebit, isCredit, startDate, endDate);
	}

	@Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/cashDiscountDetails", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public CollectionCDDetailsListData getCashDiscountDetails( @Parameter(description = "isMTD") @RequestParam(required = false) Boolean isMTD,
			@Parameter(description = "isYTD") @RequestParam(required = false) Boolean isYTD)
	{
		return collectionFacade.getCashDiscountDetails(isMTD, isYTD);
	}
	
	@RequestMapping(value = "/{userId}/sendLedgerReportMail", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public boolean sendLedgerReportMail(@Parameter(description = "userId") @PathVariable(required = true) String userId, @Parameter(description = "dealerCode") @RequestParam(required = true) String dealerCode, @Parameter(description = "isDebit") @RequestParam(required = false) Boolean isDebit,
			@Parameter(description = "isCredit") @RequestParam(required = false) Boolean isCredit, @RequestParam(required = false) final String startDate, @RequestParam(required = false) final String endDate)
	{
		return Boolean.TRUE;
	}

	@Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/OutstandingForSP", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public CollectionOutstandingData getOutstandingDataForSP()
	{
		return collectionFacade.getOutstandingDataForSP();
	}
	
	@RequestMapping(value = "/uploadSPInvoice", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public Boolean uploadSPInvoice(@Parameter(description = "Dealer Details") @RequestBody final SPInvoiceData data)
	{
		return collectionFacade.uploadSPInvoice(data);
	}

	@Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/dealerOutstandingForTSMRH", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public CollectionDealerOutstandingDetailsListWsDTO getDealerOutstandingDetailsForTSMRH(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields
			, @Parameter(description = "Optional pagination parameter. Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage
			, @Parameter(description = "Optional pagination parameter. Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize, final HttpServletResponse response, @RequestParam(required = false) List<String> so, @RequestParam(required = false) List<String> tsm)
	{

		final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);
		//SearchPageData<CollectionDealerOutstandingDetailsData> data =  collectionFacade.getDealerOutstandingDetailsForTSMRH(searchPageData,so,tsm);
		//listData.setCollectionDealerOutstandingDetailsData(data.getResults());

		CollectionDealerOutstandingDetailsListData data = collectionFacade.getDealerOutstandingDetailsForTSMRH(searchPageData,so,tsm);
//		if (data.getPagination() != null)
//		{
//			response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(data.getPagination().getTotalNumberOfResults()));
//		}

		return getDataMapper().map(data,CollectionDealerOutstandingDetailsListWsDTO.class,fields);
	}
	
	@RequestMapping(value = "/getSPInvoiceList", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public SPInvoiceListWsDto getSPInvoiceList(@Parameter(description = "startDate") @RequestParam(required = false) final String startDate,@Parameter(description = "endDate") @RequestParam(required = false) String endDate,@Parameter(description = "userId") @PathVariable(required = true) String userId
			, @RequestParam(required = false) final String sortKey, @RequestParam(required = false) final String sort
			, @ApiFieldsParam @RequestParam(required = false, defaultValue = FieldSetLevelHelper.DEFAULT_LEVEL) final String fields
			, @Parameter(description = "Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage
			, @Parameter(description = "Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize
			,final HttpServletResponse response)
	{
		final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);
		SPInvoiceListData listData = new SPInvoiceListData();
		SearchPageData<SPInvoiceData> respone = collectionFacade.getSPInvoiceList(searchPageData,startDate,endDate,userId,sortKey,sort);
		listData.setInvoices(respone.getResults());

		if (respone.getPagination() != null)
		{
			response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(respone.getPagination().getTotalNumberOfResults()));
		}
		return getDataMapper().map(listData, SPInvoiceListWsDto.class, fields);
	}

	@Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value = "/outstandingForTSMRH", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public CollectionOutstandingData getOutstandingDataForTSMRH()
	{
		return collectionFacade.getOutstandingDataForTSMRH();
	}
}
