package com.eydms.occ.controllers;

import javax.annotation.Resource;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.eydms.facades.MarketMappingFacade;
import com.eydms.facades.djp.data.CounterMappingData;
import com.eydms.occ.dto.djp.CounterMappingWsDTO;
import com.eydms.occ.security.EyDmsSecuredAccessConstants;

import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller
@RequestMapping(value = "/{baseSiteId}/users/{userId}/marketMapping")
@ApiVersion("v2")
@Tag(name = "Market Mapping Form Controller")
public class MarketMappingController extends EyDmsBaseController {

	@Resource
	MarketMappingFacade marketMappingFacade;
	
	@Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP })
	@RequestMapping(value="/counterMapping", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	@ApiBaseSiteIdAndUserIdParam

	public CounterMappingWsDTO addCounter(@Parameter(description = "Address object.", required = true) @RequestBody final CounterMappingWsDTO counter,
			@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,@Parameter(description = "routeId") @RequestParam(required = false) final String routeId,@RequestParam(required = false) String leadId)
	{
		final CounterMappingData counterData = getDataMapper().map(counter, CounterMappingData.class);
		return getDataMapper().map(marketMappingFacade.addCounter(counterData,routeId,leadId), CounterMappingWsDTO.class,fields);
	}
}
