package com.scl.occ.controllers;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.ws.rs.core.MediaType;

import com.scl.facades.data.SCLIncoTermMasterListData;
import com.scl.facades.data.order.vehicle.DealerDriverDetailsData;
import com.scl.facades.data.order.vehicle.DealerDriverDetailsListData;
import com.scl.facades.data.order.vehicle.DealerVehicleDetailsData;
import com.scl.facades.data.order.vehicle.DealerVehicleDetailsListData;
import com.scl.facades.order.DealerTransitFacade;
import com.scl.occ.annotation.ApiBaseSiteIdAndUserIdAndTerritoryParam;
import com.scl.occ.dto.order.vehicle.*;
import com.scl.occ.dto.source.SclIncoTermMasterListWsDTO;
import com.scl.occ.dto.source.SclIncoTermMasterWsDTO;
import de.hybris.platform.webservicescommons.dto.error.ErrorListWsDTO;
import de.hybris.platform.webservicescommons.dto.error.ErrorWsDTO;
import org.apache.commons.collections.CollectionUtils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import com.scl.facades.cart.SclCartFacade;
import com.scl.facades.data.DestinationSourceListData;
import com.scl.facades.order.SclOrderFacade;
import com.scl.occ.dto.source.DestinationSourceListWsDTO;
import com.scl.occ.security.SclSecuredAccessConstants;
import com.scl.occ.dto.source.DeliveryModeListWsDto;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import com.scl.facades.data.DeliveryModeListData;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.*;

@RestController
//@RequestMapping(value = "{baseSiteId}/users/{userId}/territories/{territories}/sclOrder/")
@RequestMapping(value = "{baseSiteId}/users/{userId}/sclOrder/")
@ApiVersion("v2")
@Tag(name = "Scl Order Management")
@PermitAll
public class SclOrderController extends SclBaseController{

	@Resource(name="sclOrderFacade")
	SclOrderFacade sclOrderFacade;
	
	@Resource(name="sclCartFacade")
	SclCartFacade sclCartFacade;

	@Resource
	private DealerTransitFacade dealerTransitFacade;
	
	/**
	 * Get Order count for current month for SO
	 * @return
	 */
	@RequestMapping(value = "/orderDeliveredCountAndQty", method = RequestMethod.GET)
	@ResponseBody
	@Operation(operationId = "getOrderDeliveredCountAndQty", summary = "Orders Delivered MTD", description = "Get count of Orders delivered")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public ResponseEntity<Map<String, Long>> getOrderDeliveredCountAndQty() {
		return ResponseEntity.status(HttpStatus.OK).body(sclOrderFacade.getOrderDeliveredCountAndQty());
	}	

	/**
	 * Get Order Count by Status for SO
	 * @param orderStatus
	 * @return
	 */
	@RequestMapping(value = "/orderCountByStatus", method = RequestMethod.GET)
	@ResponseBody
	@Operation(operationId = "getOrderCountByStatus", summary = "Order Count by Status", description = "Get count of Order as per the Order status")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public ResponseEntity<Integer> getOrdersCountByStatus(@RequestParam String orderStatus, @RequestParam(required = false, defaultValue = "false") Boolean approvalPending) {
		return ResponseEntity.status(HttpStatus.OK).body(sclOrderFacade.getOrderCountByStatus(orderStatus, approvalPending));
	}
	
	@RequestMapping(value = "/orderEntryCountByStatus", method = RequestMethod.GET)
	@ResponseBody
	@Operation(operationId = "getOrderEntryCountByStatus", summary = "Order Entry Count by Status", description = "Get count of Order Entry as per the Order status")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public ResponseEntity<Integer> getOrderEntriesCountByStatus(@RequestParam String orderStatus) {
		return ResponseEntity.status(HttpStatus.OK).body(sclOrderFacade.getOrderEntryCountByStatus(orderStatus));
	}
	
	@RequestMapping(value = "/deliveryItemCountByStatus", method = RequestMethod.GET)
	@ResponseBody
	@Operation(operationId = "getDeliveryItemCountByStatus", summary = "Delivery Item Count by Status", description = "Get count of Delivery Item as per the Order status")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public ResponseEntity<Integer> getDeliveryItemCountByStatus(@RequestParam String orderStatus) {
		return ResponseEntity.status(HttpStatus.OK).body(sclOrderFacade.getDeliveryItemCountByStatus(orderStatus));
	}
	
	@RequestMapping(value = "/directDispatchOrdersMTDByMonthAndYear", method = RequestMethod.GET)
	@ResponseBody
	@Operation(operationId = "getDirectDispatchOrdersMTDPercentage", summary = "Direct Dispatch Orders MTD Percentage", description = "Direct Dispatch Orders MTD Percentage as per month and year")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public ResponseEntity<Map<String,Object>> getDirectDispatchOrdersMTDPercentage(
			@Parameter(description = "Filters by Month") @RequestParam(required = false) Integer month, @Parameter(description = "Filters by Year") @RequestParam(required = false) Integer year) {
		int month1 = 0, year1 = 0;
		if(month!=null)
			month1 = month.intValue();
		if(year!=null)
			year1 = year.intValue();		
		return ResponseEntity.status(HttpStatus.OK).body(sclOrderFacade.getDirectDispatchOrdersMTDPercentage(month1, year1));
	}
	
	@Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,
			SclSecuredAccessConstants.ROLE_CUSTOMERGROUP })
	@RequestMapping(method = RequestMethod.GET, value = "/countOfDI")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@Operation(operationId = "getCountOfDI", summary = "Returns Count of DI ", description = "Returns Count of DI line items.")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public Integer getCountOfDI(
			@Parameter(description = "Order quantity.", required = true) @RequestParam final Integer orderQty,
			@Parameter(description = "City Uid.", required = true) @RequestParam final String cityUid,
			@Parameter(description = "Warehouse Code.", required = true) @RequestParam final String warehouseCode) {
		return sclCartFacade.getCountOfDI(orderQty, cityUid, warehouseCode);
	}

	@Secured({ SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
	@RequestMapping(value="/fetchDestinationSource",method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@Operation(operationId = "fetchDestinationSource", summary = "select source as per order type", description = "select source as per order type")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public DestinationSourceListWsDTO fetchDestinationSource(
            @RequestParam final String orderType, @RequestParam final String deliveryMode, @RequestParam final String productCode,
            @RequestParam final String transportationZone, @RequestParam final String incoTerm, @RequestParam final double orderQty,
            @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {

		final DestinationSourceListData destinationSourceListData = sclCartFacade.fetchDestinationSource(orderType,deliveryMode,productCode,transportationZone,incoTerm,orderQty);
		return getDataMapper().map(destinationSourceListData, DestinationSourceListWsDTO.class, fields);
	}


	@Secured({ SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
	@RequestMapping(value="/fetchIncoTerms",method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@Operation(operationId = "fetchIncoTerms", summary = "get incoTerms", description = "select incoTerms as per order type")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public SclIncoTermMasterListWsDTO fetchIncoTerms(
			@RequestParam final String orderType, @RequestParam final String deliveryMode, @RequestParam final String productCode,
			@RequestParam final String transportationZone,@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {

		final SCLIncoTermMasterListData incoTermsListData = sclCartFacade.fetchIncoTerms(orderType,deliveryMode,productCode,transportationZone);
		return getDataMapper().map(incoTermsListData, SclIncoTermMasterListWsDTO.class, fields);
	}



	@Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERGROUP })
	@RequestMapping(method = RequestMethod.GET, value = "/checkOrderQuantityForSO")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@Operation(operationId = "checkOrderQuantityForSO", summary = "Checks entered Order Quantity For SO", description = "Checks if entered Order Quantity is is more than max. order quantity for that dealer in last 6 months.")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public Boolean checkOrderQuantityForSO(
			@Parameter(description = "Order quantity.", required = true) @RequestParam final Integer orderQty,
			@Parameter(description = "district.", required = true) @RequestParam final String districtCode) {
		return sclOrderFacade.checkOrderQuantityForSO(orderQty, districtCode);
	}

	@RequestMapping(value = "/cancelOrderCountByStatus", method = RequestMethod.GET)
	@ResponseBody
	@Operation(operationId = "getCancelOrderCountByStatus", summary = "Cancel Order Count by Status", description = "Get count of Order as per the Order status")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public ResponseEntity<Integer> getCancelOrdersCountByStatus(@RequestParam String orderStatus) {
		return ResponseEntity.status(HttpStatus.OK).body(sclOrderFacade.getCancelOrderCountByStatus(orderStatus));
	}

	@RequestMapping(value = "/cancelOrderEntryCountByStatus", method = RequestMethod.GET)
	@ResponseBody
	@Operation(operationId = "getCancelOrderEntryCountByStatus", summary = "Cancel Order Entry Count by Status", description = "Get count of Order Entry as per the Order status")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public ResponseEntity<Integer> getCancelOrderEntriesCountByStatus(@RequestParam String orderStatus) {
		return ResponseEntity.status(HttpStatus.OK).body(sclOrderFacade.getCancelOrderEntryCountByStatus(orderStatus));
	}

	@RequestMapping(value = "/vehicle-details", method = RequestMethod.GET)
	@ResponseBody
	@Operation(operationId = "getVehicleDetailsForDealer", summary = "Gets List of Vehicles for the Dealer", description = "Gets List of Vehicles for the Dealer")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public DealerVehicleDetailsListWsDTO getVehicleDetailsForDealer(
			@Parameter(description = "User ID identifier")@PathVariable final String userId) {
		final DealerVehicleDetailsListData dealerVehicleDetailsData = dealerTransitFacade.getDealerVehicleDetails(userId);
		return getDataMapper().map(dealerVehicleDetailsData, DealerVehicleDetailsListWsDTO.class,DEFAULT_FIELD_SET);
	}

	@RequestMapping(value = "/vehicle-details", method = RequestMethod.POST)
	@ResponseBody
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(operationId = "createAndSaveVehicleDetailsForDealer", summary = "Creates and persists List of Vehicles for the Dealer", description = "Creates and persists List of Vehicles for the Dealer")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public ErrorListWsDTO createAndSaveVehicleDetailsForDealer(
			@Parameter(description = "User ID identifier")@PathVariable final String userId,
			@Parameter(description = "Request body parameter that contains attributes for creating the Vehicle details", required = true)
			@RequestBody final DealerVehicleDetailsListWsDTO dealerVehicleDetailsListWsDTO) {
		DealerVehicleDetailsListData dealerVehicleDetailsListData = getDataMapper().map(dealerVehicleDetailsListWsDTO,DealerVehicleDetailsListData.class,DEFAULT_FIELD_SET);
		if(null != dealerVehicleDetailsListData && CollectionUtils.isNotEmpty(dealerVehicleDetailsListData.getVehicleDetails())){
			return dealerTransitFacade.createDealerVehicleDetails(dealerVehicleDetailsListData,userId);
		}
		else{
			return getEmptyRequestErrorMessage();
		}

	}


	@RequestMapping(value = "/driver-details", method = RequestMethod.GET)
	@ResponseBody
	@Operation(operationId = "getDriverDetailsForDealer", summary = "Gets List of Drivers for the Dealer", description = "Gets List of Drivers for the Dealer")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public DealerDriverDetailsListWsDTO DriverDetailsForDealer(
			@Parameter(description = "User ID identifier")@PathVariable final String userId) {
		final DealerDriverDetailsListData dealerDriverDetailsData = dealerTransitFacade.getDealerDriverDetails(userId);
		return getDataMapper().map(dealerDriverDetailsData, DealerDriverDetailsListWsDTO.class,DEFAULT_FIELD_SET);
	}

	@RequestMapping(value = "/driver-details", method = RequestMethod.POST)
	@ResponseBody
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(operationId = "createAndSaveDriverDetailsForDealer", summary = "Creates and persists List of Drivers for the Dealer", description = "Creates and persists List of Drivers for the Dealer")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public ErrorListWsDTO createAndSaveDriverDetailsForDealer(
			@Parameter(description = "User ID identifier")@PathVariable final String userId,
			@Parameter(description = "Request body parameter that contains attributes for creating the Driver details", required = true)
			@RequestBody final DealerDriverDetailsListWsDTO dealerDriverDetailsListWsDTO) {

		  DealerDriverDetailsListData dealerDriverDetailsListData = getDataMapper().map(dealerDriverDetailsListWsDTO,DealerDriverDetailsListData.class,DEFAULT_FIELD_SET);
		  if(null != dealerDriverDetailsListData && CollectionUtils.isNotEmpty(dealerDriverDetailsListData.getDriverDetails())){
			  return dealerTransitFacade.createDealerDriverDetails(dealerDriverDetailsListData,userId);
		  }
		  else{
			  return getEmptyRequestErrorMessage();
		  }
	}

	/**
	 * Fetch Delivery Mode
	 * @param orderType
	 * @param productCode
	 * @param transportationZone
	 * @param fields
	 * @return
	 */
	@Secured({ SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP})
	@RequestMapping(value="/fetchDeliveryMode",method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@Operation(operationId = "fetchDeliveryMode", summary = "Delivery Mode Data", description = "Fetch Code and Name From Delivery Mode Data")
	@ApiBaseSiteIdAndUserIdAndTerritoryParam
	public DeliveryModeListWsDto fetchDeliveryMode(
			@RequestParam final String orderType, @RequestParam final String productCode,
			@RequestParam final String transportationZone,
			@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
		if(StringUtils.isEmpty(orderType)){
			throw new IllegalArgumentException("Please provide the list Order Type");
		}
		if(StringUtils.isEmpty(productCode)){
			throw new IllegalArgumentException("Please provide product code");
		}
		if(StringUtils.isEmpty(transportationZone)){
			throw new IllegalArgumentException("Please provide transportation zone");
		}
		final DeliveryModeListData deliveryModeListData = sclCartFacade.fetchDeliveryMode(orderType,productCode,transportationZone);
		return getDataMapper().map(deliveryModeListData, DeliveryModeListWsDto.class, fields);
	}

}
