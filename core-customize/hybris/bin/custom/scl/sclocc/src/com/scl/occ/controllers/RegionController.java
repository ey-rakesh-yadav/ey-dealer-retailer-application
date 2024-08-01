package com.scl.occ.controllers;

import java.util.List;

import javax.annotation.security.PermitAll;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.scl.facades.data.CityData;
import com.scl.facades.data.DistrictData;
import com.scl.facades.data.StateData;
import com.scl.facades.data.TalukaData;
import com.scl.facades.region.RegionFacade;

import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;


@RestController
@RequestMapping(value = "{baseSiteId}/region/")
@ApiVersion("v2")
@Tag(name = "State District Taluka City Management")
@PermitAll
public class RegionController {

	@Autowired
	RegionFacade regionFacade;
	
    @RequestMapping(value="/states", method = RequestMethod.POST)
    @ResponseBody
    @Operation(operationId = "getAllState", summary = " State List", description = "Get list of States")
    @ApiBaseSiteIdParam
    public ResponseEntity<List<StateData>> getAllStateForCountry()
    {
        return ResponseEntity.status(HttpStatus.OK).body(regionFacade.findAllState());
    }
    
    @RequestMapping(value="/districts", method = RequestMethod.POST)
    @ResponseBody
    @Operation(operationId = "getAllDistrict", summary = " District List", description = "Get list of district")
    @ApiBaseSiteIdParam
    public ResponseEntity<List<DistrictData>> getAllDistrictForState(@RequestParam final String stateCode)
    {
        return ResponseEntity.status(HttpStatus.OK).body(regionFacade.findDistrictByState(stateCode));
    }
    
    @RequestMapping(value="/talukas", method = RequestMethod.POST)
    @ResponseBody
    @Operation(operationId = "getAllTaluka", summary = " Taluka List", description = "Get list of taluka")
    @ApiBaseSiteIdParam
    public ResponseEntity<List<TalukaData>> getAllTalukaForState(@RequestParam final String districtCode)
    {
        return ResponseEntity.status(HttpStatus.OK).body(regionFacade.findTalukaByDistrict(districtCode));
    }
    
    
    @RequestMapping(value="/cities", method = RequestMethod.POST)
    @ResponseBody
    @Operation(operationId = "getAllCity", summary = " City List", description = "Get list of city")
    @ApiBaseSiteIdParam
    public ResponseEntity<List<CityData>> getAllCityForTaluka(@RequestParam final String talukaCode)
    {
        return ResponseEntity.status(HttpStatus.OK).body(regionFacade.findCityByTaluka(talukaCode));
    }
}