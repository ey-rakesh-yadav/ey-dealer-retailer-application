package com.scl.occ.controllers;

import java.util.List;

import javax.annotation.security.PermitAll;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.scl.core.enums.OrderType;
import com.scl.core.enums.TradeOrderType;
import com.scl.facades.tradeOrder.TradeOrderFacade;

import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;


@RestController
@RequestMapping(value = "{baseSiteId}/tradeOrder/")
@ApiVersion("v2")
@Tag(name = "Trade Order Management")
@PermitAll
public class TradeOrderController 
{
	@Autowired  
	TradeOrderFacade tradeOrderFacade;
	
	@RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    @Operation(operationId = "getAllTradeOrderType", summary = " Trade Order Type List", description = "Get list of Trade Order Types")
    @ApiBaseSiteIdParam
    public ResponseEntity<List<OrderType>> getAllTradeOrderTypes()
    {
        return ResponseEntity.status(HttpStatus.OK).body(tradeOrderFacade.listAllTradeOrderType());
    }
}
