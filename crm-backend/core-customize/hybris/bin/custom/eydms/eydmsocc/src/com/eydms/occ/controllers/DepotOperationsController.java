package com.eydms.occ.controllers;

import java.util.*;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import com.eydms.facades.data.warehousing.WarehouseListData;
import com.eydms.occ.annotation.ApiBaseSiteIdAndUserIdAndTerritoryParam;
import com.eydms.occ.dto.DropdownListWsDTO;
import com.eydms.occ.dto.ISOVisibilityListWsDTO;
import com.eydms.occ.dto.djp.VisitMasterListWsDTO;
import com.eydms.occ.dto.warehousing.WarehouseListWsDto;
import com.eydms.occ.security.EyDmsSecuredAccessConstants;

import de.hybris.platform.warehousingfacades.storelocator.data.WarehouseData;
import de.hybris.platform.warehousingwebservices.dto.store.WarehouseWsDto;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import com.eydms.facades.data.DropdownData;
import com.eydms.facades.data.DropdownListData;
import com.eydms.facades.data.ISOVisibilityData;
import com.eydms.facades.data.ISOVisibilityListData;
import com.eydms.facades.data.VisitMasterData;
import com.eydms.facades.data.VisitMasterListData;
import com.eydms.facades.depot.operations.DepotOperationsFacade;
import com.eydms.facades.depot.operations.data.DailyCapacityUtilizationData;
import com.eydms.facades.depot.operations.data.DepotStockData;

import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.commercewebservices.core.user.data.AddressDataList;
import de.hybris.platform.commercewebservicescommons.dto.user.AddressListWsDTO;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.search.paginated.util.PaginatedSearchUtils;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(value = "{baseSiteId}/depotOperations/")
@ApiVersion("v2")
@Tag(name = "Depot Operations Management")
@PermitAll
public class DepotOperationsController extends EyDmsBaseController
{
	@Resource
	DepotOperationsFacade depotOperationsFacade ;

    private static final Logger LOG = Logger.getLogger(DepotOperationsController.class);
	
	@RequestMapping(value="/stockavailability", method = RequestMethod.POST)
    @ResponseBody
    @Operation(operationId = "getStockAvailability", summary = "Stock Availability", description = "Get Stock Availability for Depots")
    @ApiBaseSiteIdAndUserIdParam
    public ResponseEntity<List<DepotStockData>> getStockAvailability(
    		@Parameter(description = "List of Product Grade") @RequestParam(required = false) final List<String> productGrade,@Parameter(description = "depotCode") @RequestParam(required = false) final List<String> depotCode)
    {
		List<DepotStockData> stock = depotOperationsFacade.getStockAvailability(productGrade, depotCode);
        return ResponseEntity.status(HttpStatus.OK).body(stock);
    }
	
	@RequestMapping(value="/dailyCapacityUtilization", method = RequestMethod.POST)
    @ResponseBody
    @Operation(operationId = "getDailyCapacityUtilization", summary = "Daily Capacity Utilization", description = "Get Daily Capacity Utilization for products and depots")
    @ApiBaseSiteIdAndUserIdParam
    public ResponseEntity<DailyCapacityUtilizationData> getDailyCapacityUtilization(
    		@Parameter(description = "List of Product Grade") @RequestParam(required = false) final List<String> productGrade,
    		@Parameter(description = "depotCode") @RequestParam(required = false) final List<String> depotCode)
    {
        return ResponseEntity.status(HttpStatus.OK).body(depotOperationsFacade.getDailyCapacityUtilization(productGrade, depotCode));
    }
	
	@RequestMapping(value="/dispatchTATAndDeliveryTime", method = RequestMethod.POST)
    @ResponseBody
    @Operation(operationId = "getDispatchTATAndDeliveryTime", summary = "Dispatch TAT and Service Level Delivery Time", description = "Get Dispatch TAT and Service Level Delivery Time")
    @ApiBaseSiteIdParam
    public ResponseEntity<Map<String,Map<String,Integer>>> getDispatchTATAndDeliveryTime()
    {
		Map<String,Map<String,Integer>> time = depotOperationsFacade.getDispatchTATAndDeliveryTime();
        return ResponseEntity.status(HttpStatus.OK).body(time);
    }
	
	@RequestMapping(value="/stockAvailabilityCard", method = RequestMethod.GET)
    @ResponseBody
    @Operation(operationId = "getStockAvailabilityTotal", summary = "Stock Availability Total", description = "Get Stock Availability Total count for Depots")
    @ApiBaseSiteIdParam
    public Long getStockAvailabilityTotal()
    {
		return depotOperationsFacade.getStockAvailabilityTotal();
    }
	
	@RequestMapping(value="/depotListForUser", method = RequestMethod.GET)
    @ResponseBody
    @ApiBaseSiteIdParam
    public List<DropdownData> getDepotListForUser()
    {
		return depotOperationsFacade.getWareHouseDataFromDepotSubAreaMapping();
    }

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value="/getDepotAddress", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public AddressListWsDTO getDepotAddresses(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
			@Parameter(description = "depotCode") @RequestParam final String depotCode)
	{
		final List<AddressData> addressList = depotOperationsFacade.getDepotAddresses(depotCode);
		final AddressDataList addressDataList = new AddressDataList();
		addressDataList.setAddresses(addressList);
		return getDataMapper().map(addressDataList, AddressListWsDTO.class, fields);
	}

    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value="/warehousedata", method = RequestMethod.GET)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public DropdownListWsDTO getDepotList()
    {

        List<DropdownData> warehouseDataList = depotOperationsFacade.getWareHouseDataFromDepotSubAreaMapping();
        DropdownListData warehouseListData = new DropdownListData();
        warehouseListData.setDropdown(warehouseDataList);

        return getDataMapper().map(warehouseListData,DropdownListWsDTO.class,DEFAULT_FIELD_SET);

    }

    @RequestMapping(value="/depotGrades", method = RequestMethod.GET)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public List<String> getDepotListOfGrades()
    {
		return depotOperationsFacade.getDepotListOfGrades();
    }
    
    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP })
	@RequestMapping(value="/getISOVisibility", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public ISOVisibilityListWsDTO getISOVisibilityDetails(@Parameter(description = "Optional pagination parameter. Default value 0.") @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,
			@Parameter(description = "Optional pagination parameter. Default value 20.") @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize, final HttpServletResponse response,
			@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields, @Parameter(description="DELIVERED or PENDING") @RequestParam final String mrnStatus,
			@Parameter(description="ROAD or RAIL", required = false) @RequestParam(required = false) final List<String> deliveryMode, @Parameter(description = "products",required = false) @RequestParam(required = false) final List<String> products,
			@Parameter(description="depots", required = false) @RequestParam(required = false) final List<String> depots)
	{
		ISOVisibilityListData result = new ISOVisibilityListData();
		final SearchPageData<ISOVisibilityData> searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);
		SearchPageData<ISOVisibilityData> isoVisibilityDetails = depotOperationsFacade.getISOVisibilityDetails(searchPageData, mrnStatus, depots, deliveryMode, products);
		result.setIsoVisibilityList(isoVisibilityDetails.getResults());
		
		if (isoVisibilityDetails.getPagination() != null)
		{
			response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(isoVisibilityDetails.getPagination().getTotalNumberOfResults()));
		}

		return getDataMapper().map(result, ISOVisibilityListWsDTO.class,fields);
	}
    
    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value="/mrnPendingCount", method = RequestMethod.GET)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public ResponseEntity<Integer> getMRNPendingCount()
    {
    	return ResponseEntity.status(HttpStatus.OK).body(depotOperationsFacade.getMRNPendingCount());
    }
 
}
