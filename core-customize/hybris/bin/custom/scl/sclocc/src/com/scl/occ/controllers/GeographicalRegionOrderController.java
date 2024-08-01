package com.scl.occ.controllers;

import com.scl.facades.data.PincodeData;
import com.scl.facades.region.GeographicalRegionFacade;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "{baseSiteId}/users/{userId}/geographicalRegion/")
@ApiVersion("v2")
@Tag(name = "State District Taluka ErpCity Management")
@PermitAll
public class GeographicalRegionOrderController {

    @Resource
    GeographicalRegionFacade geographicalRegionFacade;

    @RequestMapping(value="/lpSourceErpCities", method = RequestMethod.POST)
    @ResponseBody
    @Operation(operationId = "getLpSourceErpCities", summary = "LPSource ErpCity List", description = "Get list of erpCity")
    @ApiBaseSiteIdAndUserIdParam
    public ResponseEntity<List<String>> findAllLpSourceErpCity(@RequestParam(name = "dealer id") final String dealerId, @RequestParam(required = false) String retailerUid,@Parameter(description = "state") @RequestParam final String state, @Parameter(description = "district") @RequestParam final String district, @Parameter(description = "taluka") @RequestParam final String taluka)
    {
        List<String> erpCities = new ArrayList<String>(geographicalRegionFacade.findAllLpSourceErpCity(dealerId,retailerUid,state, district, taluka));
        Collections.sort(erpCities);
        return ResponseEntity.status(HttpStatus.OK).body(erpCities);
    }

    @RequestMapping(value="/lpSourceTalukas", method = RequestMethod.POST)
    @ResponseBody
    @Operation(operationId = "getLpSourceTaluka", summary = "LPSource Taluka List", description = "Get list of Taluka")
    @ApiBaseSiteIdAndUserIdParam
    public ResponseEntity<List<String>> findAllLpSourceTaluka(@Parameter(description = "state") @RequestParam final String state,@Parameter(description = "district") @RequestParam final String district,@RequestParam(name = "dealer id") final String dealerId,@RequestParam(required = false) String retailerUid)
    {
        List<String> erpCities = new ArrayList<String>(geographicalRegionFacade.findAllLpSourceTaluka(state, district,dealerId,retailerUid));
        Collections.sort(erpCities);
        return ResponseEntity.status(HttpStatus.OK).body(erpCities);
    }

    @RequestMapping(value="/lpSourceDistricts", method = RequestMethod.POST)
    @ResponseBody
    @Operation(operationId = "getLpSourceDistrict", summary = "LPSource District List", description = "Get list of District")
    @ApiBaseSiteIdAndUserIdParam
    public ResponseEntity<List<String>> findAllLpSourceDistrict(@RequestParam(name = "dealer id") final String dealerId,@RequestParam(required = false) String retailerUid, @Parameter(description = "state") @RequestParam final String state)
    {
        List<String> erpCities = new ArrayList<String>(geographicalRegionFacade.findAllLpSourceDistrict(dealerId,retailerUid, state));
        Collections.sort(erpCities);
        return ResponseEntity.status(HttpStatus.OK).body(erpCities);
    }


    @RequestMapping(value="/lpSourceState", method = RequestMethod.GET)
    @ResponseBody
    @Operation(operationId = "getLpSourceState", summary = "LPSource State List", description = "Get list of State")
    @ApiBaseSiteIdAndUserIdParam
    public ResponseEntity<List<String>> findAllLpSourceState(@RequestParam(name = "dealer id") final String dealerId, @RequestParam(name = "retailer id",required = false) final String retailerId)
    {
        List<String> stateList = new ArrayList<>(geographicalRegionFacade.findAllLpSourceState(dealerId,retailerId));
        Collections.sort(stateList);
        return ResponseEntity.status(HttpStatus.OK).body(stateList);
    }


    @RequestMapping(value="/lpSourcePincode", method = RequestMethod.POST)
    @ResponseBody
    @Operation(operationId = "getLpSourcePincode", summary = "LPSource Pincode List", description = "Get list of pincode")
    @ApiBaseSiteIdAndUserIdParam
    public ResponseEntity<List<PincodeData>> findAllLpSourcePincode(@RequestParam(name = "dealer id") final String dealerId,@RequestParam(required = false) String retailerUid, @Parameter(description = "state") @RequestParam final String state,
                                                                    @Parameter(description = "district") @RequestParam final String district,
                                                                    @Parameter(description = "taluka") @RequestParam final String taluka,
                                                                    @Parameter(description = "city") @RequestParam final String city)
    {
        List<PincodeData> pincodes = geographicalRegionFacade.findAllLpSourcePincode(dealerId,retailerUid,state, district, taluka, city);
        List<PincodeData> sortedPincodes = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(pincodes)) {
            sortedPincodes = pincodes.stream().sorted(Comparator.comparing(PincodeData::getPincode)).collect(Collectors.toList());
        }
        return ResponseEntity.status(HttpStatus.OK).body(sortedPincodes);
    }

}
