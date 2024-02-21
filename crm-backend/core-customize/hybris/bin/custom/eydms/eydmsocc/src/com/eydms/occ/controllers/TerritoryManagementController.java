package com.eydms.occ.controllers;

import java.util.List;


import com.eydms.core.model.DistrictMasterModel;
import com.eydms.facades.data.*;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.search.paginated.util.PaginatedSearchUtils;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.eydms.facades.TerritoryManagementFacade;
import com.eydms.facades.prosdealer.data.CustomerListData;
import com.eydms.facades.prosdealer.data.DealerListData;
import com.eydms.occ.annotation.ApiBaseSiteIdAndUserIdAndTerritoryParam;
import com.eydms.occ.dto.CustomerListWsDTO;
import com.eydms.occ.dto.TerritoryListWsDTO;
import com.eydms.occ.dto.dealer.DealerListWsDTO;
import com.eydms.occ.security.EyDmsSecuredAccessConstants;

import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import io.swagger.v3.oas.annotations.Operation;


@Controller
@RequestMapping(value = "/{baseSiteId}/users/{userId}/territoryManagement")
@ApiVersion("v2")
@Tag(name = "Territory Management Controller")
public class TerritoryManagementController extends EyDmsBaseController {

	   @Autowired
	   TerritoryManagementFacade territoryManagementFacade;
	   
	   @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	   @RequestMapping(value="/subAreasForSO", method = RequestMethod.GET)
	   @ResponseStatus(value = HttpStatus.CREATED)
	   @ResponseBody
	   @ApiBaseSiteIdAndUserIdParam
	   public List<String> getAllSubAreaForSO(@PathVariable final String userId)
	   {   	
		   return territoryManagementFacade.getAllSubAreaForSO(userId);
	   }
	   
	   @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	   @RequestMapping(value="/subAreasForCustomer", method = RequestMethod.GET)
	   @ResponseStatus(value = HttpStatus.CREATED)
	   @ResponseBody
	   @ApiBaseSiteIdAndUserIdAndTerritoryParam
	   public List<String> getAllSubAreaForCustomer(@RequestParam final String customerId)
	   {   	
		   return territoryManagementFacade.getAllSubAreaForCustomer(customerId);
	   }

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/getDistrictForSP", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public DistrictMasterListData getDistrictForSP(@RequestBody(required = false) FilterDistrictData filterDistrictData)
	{
		return territoryManagementFacade.getDistrictForSP(filterDistrictData);
	}
	   
	   //New Territory Change
	   @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	   @RequestMapping(value="/customersForSubArea", method = RequestMethod.GET)
	   @ResponseStatus(value = HttpStatus.CREATED)
	   @ResponseBody
	   @ApiBaseSiteIdAndUserIdAndTerritoryParam
	   public DealerListWsDTO getAllCustomersForSubArea()
	   {   	
		   DealerListData dataList =  territoryManagementFacade.getAllCustomerForSubArea();
	        return getDataMapper().map(dataList,DealerListWsDTO.class, BASIC_FIELD_SET);
	   }
	   
	   //New Territory Change
	   @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	   @RequestMapping(value="/dealersForSubArea", method = RequestMethod.GET)
	   @ResponseStatus(value = HttpStatus.CREATED)
	   @ResponseBody
	   @ApiBaseSiteIdAndUserIdAndTerritoryParam
	   public DealerListWsDTO getAllDealersForSubArea()
	   {   	
		   DealerListData dataList =  territoryManagementFacade.getAllDealersForSubArea();
	        return getDataMapper().map(dataList,DealerListWsDTO.class, BASIC_FIELD_SET);
	   }
	   
	   //New Territory Change
	   @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	   @RequestMapping(value="/retailersForSubArea", method = RequestMethod.GET)
	   @ResponseStatus(value = HttpStatus.CREATED)
	   @ResponseBody
	   @ApiBaseSiteIdAndUserIdAndTerritoryParam
	   public DealerListWsDTO getAllRetailersForSubArea()
	   {   	
		   DealerListData dataList =  territoryManagementFacade.getAllRetailersForSubArea();
	        return getDataMapper().map(dataList,DealerListWsDTO.class, BASIC_FIELD_SET);
	   }
	   
	   //New Territory Change
	   @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	   @RequestMapping(value="/influencersForSubArea", method = RequestMethod.GET)
	   @ResponseStatus(value = HttpStatus.CREATED)
	   @ResponseBody
	   @ApiBaseSiteIdAndUserIdAndTerritoryParam
	   public DealerListWsDTO getAllInfluencersForSubArea()
	   {   	
		   DealerListData dataList =  territoryManagementFacade.getAllInfluencersForSubArea();
	        return getDataMapper().map(dataList,DealerListWsDTO.class, BASIC_FIELD_SET);
	   }
	   
	   //New Territory Change
	   @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	   @RequestMapping(value="/sitesForSubArea", method = RequestMethod.GET)
	   @ResponseStatus(value = HttpStatus.CREATED)
	   @ResponseBody
	   @ApiBaseSiteIdAndUserIdAndTerritoryParam
	   public DealerListWsDTO getAllSitesForSubArea()
	   {   	
		   DealerListData dataList =  territoryManagementFacade.getAllSitesForSubArea();
	        return getDataMapper().map(dataList,DealerListWsDTO.class, BASIC_FIELD_SET);
	   }

	   @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	   @RequestMapping(value="/retailersForSubAreaTOP", method = RequestMethod.GET)
	   @ResponseStatus(value = HttpStatus.CREATED)
	   @ResponseBody
	   @ApiBaseSiteIdAndUserIdAndTerritoryParam
	   public DealerListWsDTO getAllRetailersForSubAreaTOP(@RequestParam(required = false) final String subArea,@RequestParam final String dealerCode)
	   {   	
		   DealerListData dataList = territoryManagementFacade.getAllRetailersForSubAreaTOP(subArea,dealerCode);
	        return getDataMapper().map(dataList,DealerListWsDTO.class, BASIC_FIELD_SET);

	   }
	   
	   @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	   @RequestMapping(value="/getAllTerritoryForSO", method = RequestMethod.GET)
	   @ResponseStatus(value = HttpStatus.CREATED)
	   @ResponseBody
	   @ApiBaseSiteIdAndUserIdParam
	   public TerritoryListWsDTO getAllTerritoryForSO(@RequestParam(required = false) final String subArea)
	   {   	
		   TerritoryListData dataList = territoryManagementFacade.getAllTerritoryForSO(subArea);
		   return getDataMapper().map(dataList,TerritoryListWsDTO.class, BASIC_FIELD_SET);
	   }

	//Sales Performance Req. = get sales officer list by state for leaderboard page
	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/getAllSalesOfficersByState", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public DealerListWsDTO getAllSalesOfficersByState(@RequestParam(required = true) final String state)
	{
		DealerListData dataList = territoryManagementFacade.getAllSalesOfficersByState(state);
		return getDataMapper().map(dataList,DealerListWsDTO.class, BASIC_FIELD_SET);

	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/getAllStatesForSO", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public List<String> getAllStatesForSO()
	{
		 return territoryManagementFacade.getAllStatesForSO();
	}
	
	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/territories", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public TerritoryListData getTerritoriesForSO()
	{   	
		return territoryManagementFacade.getTerritoriesForSO();
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/getTerritoriesForCustomer", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public TerritoryListWsDTO getTerritoriesForCustomer()
	{
		TerritoryListData dataList = territoryManagementFacade.getTerritoriesForCustomer();
		return getDataMapper().map(dataList,TerritoryListWsDTO.class, BASIC_FIELD_SET);
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/getRetailerListForDealerPagination", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public DealerListWsDTO getRetailerListForDealerPagination( @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields
			, @Parameter(description="Search term") @RequestParam(required = false) final String searchKey
            ,@Parameter(description="Network Type") @RequestParam(required = false) final String networkType
            ,@RequestParam(required = false, defaultValue = "false") final Boolean isNew
			,@Parameter(description = "Optional pagination parameter. Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage
			,@Parameter(description = "Optional pagination parameter. Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize,@RequestParam(required = false) Boolean isTOP)
	{
		final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);
		DealerListData dealerListData=new DealerListData();
		SearchPageData<CustomerData> retailerListForDealerPagination = territoryManagementFacade.getRetailerListForDealerPagination(searchPageData, networkType, isNew, searchKey, isTOP);
		dealerListData.setDealers(retailerListForDealerPagination.getResults());
		return getDataMapper().map(dealerListData,DealerListWsDTO.class, BASIC_FIELD_SET);
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/getInfluencerListForDealerPagination", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public DealerListWsDTO getInfluencerListForDealerPagination( @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields
            ,@Parameter(description="Search term") @RequestParam(required = false) final String searchKey
            ,@Parameter(description="Network Type") @RequestParam(required = false) final String networkType
            ,@RequestParam(required = false) final String influencerType
            ,@RequestParam(required = false) final String influencerCategory
            ,@RequestParam(required = false, defaultValue = "false") final Boolean isNew
			,@Parameter(description = "Optional pagination parameter. Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage
			,@Parameter(description = "Optional pagination parameter. Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize)
	{
		final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);
		DealerListData dealerListData=new DealerListData();
		SearchPageData<CustomerData> retailerListForDealerPagination = territoryManagementFacade.getInfluencerListForDealerPagination(searchPageData, networkType, isNew, searchKey, influencerType, influencerCategory);
		dealerListData.setDealers(retailerListForDealerPagination.getResults());
		return getDataMapper().map(dealerListData,DealerListWsDTO.class, BASIC_FIELD_SET);
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/getInfluencerListForRetailerPagination", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public DealerListWsDTO getInfluencerListForRetailerPagination( @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields
            , @Parameter(description="Search term") @RequestParam(required = false) final String searchKey
            ,@Parameter(description="Network Type") @RequestParam(required = false) final String networkType
            ,@RequestParam(required = false) final String influencerType
            ,@RequestParam(required = false) final String influencerCategory
            ,@RequestParam(required = false, defaultValue = "false") final Boolean isNew
            ,@Parameter(description = "Optional pagination parameter. Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage
			,@Parameter(description = "Optional pagination parameter. Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize)
	{
		final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);
		DealerListData dealerListData=new DealerListData();
		SearchPageData<CustomerData> retailerListForDealerPagination = territoryManagementFacade.getInfluencerListForRetailerPagination(searchPageData, networkType, isNew, searchKey, influencerType, influencerCategory);
		dealerListData.setDealers(retailerListForDealerPagination.getResults());
		return getDataMapper().map(dealerListData,DealerListWsDTO.class, BASIC_FIELD_SET);
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/getRetailerListForDealer", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public DealerListWsDTO getRetailerListForDealer()
	{

		DealerListData dataList = territoryManagementFacade.getRetailerListForDealer();
		return getDataMapper().map(dataList,DealerListWsDTO.class, BASIC_FIELD_SET);
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/getInfluencerListForDealer", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public DealerListWsDTO getInfluencerListForDealer()
	{
		DealerListData dataList = territoryManagementFacade.getInfluencerListForDealer();
		return getDataMapper().map(dataList,DealerListWsDTO.class, BASIC_FIELD_SET);
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/getInfluencerListForRetailer", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public DealerListWsDTO getInfluencerListForRetailer()
	{

		DealerListData dataList = territoryManagementFacade.getInfluencerListForRetailer();
		return getDataMapper().map(dataList,DealerListWsDTO.class, BASIC_FIELD_SET);
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/getDealerCountForRetailer", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public Integer getDealerCountForRetailer()
	{
		return territoryManagementFacade.getDealerCountForRetailer();
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/getRetailerCountForDealer", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public Integer getRetailerCountForDealer()
	{
		return territoryManagementFacade.getRetailerCountForDealer();
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/getInfluencerCountForDealer", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public Integer getInfluencerCountForDealer()
	{
		return territoryManagementFacade.getInfluencerCountForDealer();
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/getDealerListForRetailerPagination", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public DealerListWsDTO getDealerListForRetailerPagination( @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields
			, @Parameter(description="Search term") @RequestParam(required = false) final String filter
			,@Parameter(description = "Optional pagination parameter. Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage
			,@Parameter(description = "Optional pagination parameter. Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize)
	{
		final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);
		DealerListData dealerListData=new DealerListData();
		SearchPageData<CustomerData> retailerListForDealerPagination = territoryManagementFacade.getDealerListForRetailerPagination(searchPageData,filter);
		dealerListData.setDealers(retailerListForDealerPagination.getResults());
		return getDataMapper().map(dealerListData,DealerListWsDTO.class, BASIC_FIELD_SET);
	}
	
	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/getInfluencerCountForRetailer", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public Integer getInfluencerCountForRetailer()
	{
		return territoryManagementFacade.getInfluencerCountForRetailer();
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/getTalukaForUser", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public TerritoryListData getTalukaForUser(@RequestBody(required = false) FilterTalukaData filterTalukaData) {
		return territoryManagementFacade.getTalukaForUser(filterTalukaData);
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/getDistrictForUser", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public DistrictMasterListData getDistrictForUser(@RequestBody(required = false) FilterDistrictData filterDistrictData) {
		return territoryManagementFacade.getDistrictForUser(filterDistrictData);
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/getRegionsForUser", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public RegionListData getRegionsForUser(@RequestBody final FilterRegionData filterRegionData)
	{
		return territoryManagementFacade.getRegionsForUser(filterRegionData);
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/getSOSubAreaMappingForUser", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public SOSubAreaMappingListData getSOSubAreaMappingForUser(@RequestBody(required = false) FilterTalukaData filterTalukaData) {
		return territoryManagementFacade.getSOSubAreaMappingForUser(filterTalukaData);
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/getTSMDistrictMappingForUser", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public TSMDistrictMappingListData getTSMDistrictMappingForUser(@RequestBody(required = false) FilterDistrictData filterDistrictData) {
		return territoryManagementFacade.getTSMDistrcitMappingForUser(filterDistrictData);
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/getSOForUser", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public EyDmsUserListData getSOForUser(@RequestBody(required = false) FilterTalukaData filterTalukaData) {
		return territoryManagementFacade.getSOForUser(filterTalukaData);
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/getTSMForUser", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public EyDmsUserListData getTSMForUser(@RequestBody(required = false) FilterDistrictData filterDistrictData) {
		return territoryManagementFacade.getTSMForUser(filterDistrictData);
	}

	@RequestMapping(value="/getCustomerForUser", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public CustomerListWsDTO getCustomerForUser(@RequestBody final RequestCustomerData customerData)
	{   	
		CustomerListData dataList =  territoryManagementFacade.getCustomerForUser(customerData);
        return getDataMapper().map(dataList,CustomerListWsDTO.class, BASIC_FIELD_SET);
	}
	
	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/getCustomerForUserPagination", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public CustomerListWsDTO getCustomerForUserPagination( @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields
			,@Parameter(description = "Optional pagination parameter. Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage
			,@Parameter(description = "Optional pagination parameter. Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize
			,@RequestBody final RequestCustomerData customerData)
	{
		final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);
		CustomerListData customerListData=new CustomerListData();
		SearchPageData<CustomerData> result = territoryManagementFacade.getCustomerForUserPagination(searchPageData,customerData);
		customerListData.setCustomers(result.getResults());
		customerListData.setTotalCount(result.getPagination().getTotalNumberOfResults());
		return getDataMapper().map(customerListData,CustomerListWsDTO.class, BASIC_FIELD_SET);
	}

	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
	@RequestMapping(value="/getSPForCustomer", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.OK)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public EyDmsCustomerData getSPForCustomer(@RequestParam String uid) {
		return territoryManagementFacade.getSPForCustomer(uid);
	}
}
