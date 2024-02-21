package com.eydms.occ.controllers;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.ws.rs.core.MediaType;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.eydms.core.enums.DeliverySlots;
import com.eydms.facades.data.DeliverySlotMasterData;
import com.eydms.facades.delivery.DeliveryModesFacade;
import com.eydms.facades.delivery.DeliverySlotsFacade;
import com.eydms.facades.order.EYDMSB2BOrderFacade;

import de.hybris.platform.commercefacades.order.data.DeliveryModeData;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.commercewebservicescommons.dto.user.AddressWsDTO;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(value = "/{baseSiteId}/delivery")
@ApiVersion("v2")
@Tag(name = "Delivery Management")
@PermitAll
public class DeliveryManagementController 
{
	
	@Resource
	DeliverySlotsFacade deliverySlotsFacade;

	@Resource
	DeliveryModesFacade deliveryModesFacade;
	

	@Resource(name = "orderFacade")
	private EYDMSB2BOrderFacade orderFacade;

	@PostMapping("deliveryModes")
	@ResponseBody
	@Operation(operationId = "getAllDeliveryModes", summary = " Delivery Modes List", description = "Get list of All Delivery Modes")
	@ApiBaseSiteIdParam
	public ResponseEntity<Collection<DeliveryModeData>> getDeliverySlots()
	{
		return ResponseEntity.status(HttpStatus.OK).body(deliveryModesFacade.getAllDeliveryModes());
	}

	@PostMapping("deliverySlots")
	@ResponseBody
	@Operation(operationId = "getAllDeliverySlots", summary = "Delivery Slots List", description = "Get list of All Delivery Slots")
	@ApiBaseSiteIdParam
	public ResponseEntity<Map<DeliverySlots,String>> getDeliveryModes()
	{
		return ResponseEntity.status(HttpStatus.OK).body(deliverySlotsFacade.getAllDeliverySlots());
	}
	
	
	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@RequestMapping(value = "/getDeliverySlots", method = RequestMethod.GET)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam
	public List<DeliverySlotMasterData> getDeliverySlotList()
	{
		return orderFacade.getDeliverySlotList();
	}		
}
