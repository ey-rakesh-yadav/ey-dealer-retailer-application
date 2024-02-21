package com.eydms.occ.controllers;

import com.eydms.facades.data.GeographicalMasterData;
import com.eydms.facades.region.GeographicalRegionFacade;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Parameter;
import javax.annotation.Resource;
import javax.annotation.security.PermitAll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping(value = "{baseSiteId}/geographicalRegion/")
@ApiVersion("v2")
@Tag(name = "State District Taluka ErpCity Management")
@PermitAll
public class GeographicalRegionController {

	@Resource
	GeographicalRegionFacade geographicalRegionFacade;
	
	@RequestMapping(value="/states", method = RequestMethod.POST)
    @ResponseBody
    @Operation(operationId = "getAllState", summary = " State List", description = "Get list of States")
    @ApiBaseSiteIdParam
    public ResponseEntity<List<String>> getAllState()
    {
        List<String> states = new ArrayList<String>(geographicalRegionFacade.findAllState());
        Collections.sort(states);
        return ResponseEntity.status(HttpStatus.OK).body(states);
    }
	
	@RequestMapping(value="/districts", method = RequestMethod.POST)
    @ResponseBody
    @Operation(operationId = "getAllDistrict", summary = " District List", description = "Get list of district")
    @ApiBaseSiteIdParam
    public ResponseEntity<List<String>> getAllDistrict(@Parameter(description = "state") @RequestParam final String state)
    {
        List<String> districts = new ArrayList<String>(geographicalRegionFacade.findAllDistrict(state));
        Collections.sort(districts);
        return ResponseEntity.status(HttpStatus.OK).body(districts);
    }
    
    @RequestMapping(value="/talukas", method = RequestMethod.POST)
    @ResponseBody
    @Operation(operationId = "getAllTaluka", summary = " Taluka List", description = "Get list of taluka")
    @ApiBaseSiteIdParam
    public ResponseEntity<List<String>> getAllTaluka(@Parameter(description = "state") @RequestParam final String state,@Parameter(description = "district") @RequestParam final String district)
    {
        List<String> talukas = new ArrayList<String>(geographicalRegionFacade.findAllTaluka(state, district));
        Collections.sort(talukas);
        return ResponseEntity.status(HttpStatus.OK).body(talukas);
    }
    
    @RequestMapping(value="/erpCities", method = RequestMethod.POST)
    @ResponseBody
    @Operation(operationId = "getAllErpCities", summary = " ErpCity List", description = "Get list of erpCity")
    @ApiBaseSiteIdParam
    public ResponseEntity<List<String>> getAllErpCity(@Parameter(description = "state") @RequestParam final String state,@Parameter(description = "district") @RequestParam final String district,@Parameter(description = "taluka") @RequestParam final String taluka)
    {
        List<String> erpCities = new ArrayList<String>(geographicalRegionFacade.findAllErpCity(state, district, taluka));
        Collections.sort(erpCities);
        return ResponseEntity.status(HttpStatus.OK).body(erpCities);
    }
    
    @RequestMapping(value="/getAllErpCitiesForStateDistrict", method = RequestMethod.POST)
    @ResponseBody
    @Operation(operationId = "getAllErpCitiesForStateDistrict", summary = " ErpCity List", description = "Get list of erpCity")
    @ApiBaseSiteIdParam
    public ResponseEntity<List<String>> getAllErpCitiesForStateDistrict(@RequestParam final String state, @RequestParam final String district)
    {
        List<String> erpCities = new ArrayList<String>(geographicalRegionFacade.findAllErpCity(state, district));
        Collections.sort(erpCities);
        return ResponseEntity.status(HttpStatus.OK).body(erpCities);
    }
    
    @RequestMapping(value="/getGeographyByPincode", method = RequestMethod.POST)
    @ResponseBody
    @ApiBaseSiteIdParam
    public List<GeographicalMasterData> getGeographyByPincode(@Parameter(description = "pincode") @RequestParam final String pincode)
    {
        return geographicalRegionFacade.getGeographyByPincode(pincode);
    }    

    @RequestMapping(value="/getBusinessState", method = RequestMethod.POST)
    @ResponseBody
    @ApiBaseSiteIdParam
    public List<String> getBusinessState(@Parameter(description = "geographyState") @RequestParam final String geographyState
    		,@Parameter(description = "district") @RequestParam final String district,@Parameter(description = "taluka") @RequestParam final String taluka,@Parameter(description = "erpCity") @RequestParam final String erpCity)
    {
        return geographicalRegionFacade.getBusinessState(geographyState, district, taluka, erpCity);
    }
    
    @RequestMapping(value="/getGeographyState", method = RequestMethod.POST)
    @ResponseBody
    @ApiBaseSiteIdParam
    public List<String> getGeographyState(@Parameter(description = "googleMapState") @RequestParam final String googleMapState)
    {
        return geographicalRegionFacade.getGeographicalStateByGoogleMapState(googleMapState);
    }

   @RequestMapping(value="/getERPState", method = RequestMethod.POST)
   @ResponseBody
   @ApiBaseSiteIdParam
   public String getERPState(@Parameter(description = "gstState") @RequestParam final String gstState)
   {
       return geographicalRegionFacade.getErpStateForGstState(gstState);
   }
   
   @RequestMapping(value="/lpSourceErpCities", method = RequestMethod.POST)
   @ResponseBody
   @Operation(operationId = "getLpSourceErpCities", summary = "LPSource ErpCity List", description = "Get list of erpCity")
   @ApiBaseSiteIdParam
   public ResponseEntity<List<String>> findAllLpSourceErpCity(@Parameter(description = "state") @RequestParam final String state,@Parameter(description = "district") @RequestParam final String district,@Parameter(description = "taluka") @RequestParam final String taluka)
   {
       List<String> erpCities = new ArrayList<String>(geographicalRegionFacade.findAllLpSourceErpCity(state, district, taluka));
       Collections.sort(erpCities);
       return ResponseEntity.status(HttpStatus.OK).body(erpCities);
   }

   @RequestMapping(value="/lpSourceTalukas", method = RequestMethod.POST)
   @ResponseBody
   @Operation(operationId = "getLpSourceTaluka", summary = "LPSource Taluka List", description = "Get list of Taluka")
   @ApiBaseSiteIdParam
   public ResponseEntity<List<String>> findAllLpSourceTaluka(@Parameter(description = "state") @RequestParam final String state,@Parameter(description = "district") @RequestParam final String district)
   {
       List<String> erpCities = new ArrayList<String>(geographicalRegionFacade.findAllLpSourceTaluka(state, district));
       Collections.sort(erpCities);
       return ResponseEntity.status(HttpStatus.OK).body(erpCities);
   }
   
   @RequestMapping(value="/lpSourceDistricts", method = RequestMethod.POST)
   @ResponseBody
   @Operation(operationId = "getLpSourceDistrict", summary = "LPSource District List", description = "Get list of District")
   @ApiBaseSiteIdParam
   public ResponseEntity<List<String>> findAllLpSourceDistrict(@Parameter(description = "state") @RequestParam final String state)
   {
       List<String> erpCities = new ArrayList<String>(geographicalRegionFacade.findAllLpSourceDistrict(state));
       Collections.sort(erpCities);
       return ResponseEntity.status(HttpStatus.OK).body(erpCities);
   }
   
   @RequestMapping(value="/userStates", method = RequestMethod.POST)
   @ResponseBody
   @Operation(operationId = "userStates", summary = "User State List", description = "Get User State")
   @ApiBaseSiteIdParam
   public ResponseEntity<List<String>> findUserState(@RequestParam(required = false) String customerCode)
   {
       List<String> states = new ArrayList<String>(geographicalRegionFacade.findUserState(customerCode));
       Collections.sort(states);
       return ResponseEntity.status(HttpStatus.OK).body(states);
   }
}
