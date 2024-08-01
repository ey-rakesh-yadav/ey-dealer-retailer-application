package com.scl.occ.controllers;

import com.scl.occ.dto.DropdownListWsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.scl.facades.SSOLoginFacade;
import com.scl.facades.data.SSOLoginData;

import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;


@Controller
@RequestMapping(value = "/{baseSiteId}/singleSignOnAuth")
@ApiVersion("v2")
@Tag(name = "SingleSignOn Auth Controller")
public class SSOLoginController extends SclBaseController {

	@Autowired
	SSOLoginFacade sSOLoginFacade;
	
    @RequestMapping(value="/verifyUser", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdParam
    public ResponseEntity<SSOLoginData> verifyUserAndGetBrandId(@RequestParam final String uid) throws UnknownIdentifierException
    {
        return ResponseEntity.status(HttpStatus.OK).body(sSOLoginFacade.verifyUserAndGetBrand(uid));
    }

    @RequestMapping(value="/verifyCustomer", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdParam
    public ResponseEntity<SSOLoginData> verifyCustomerAndGetBrandId(@RequestParam final String customerNo) throws UnknownIdentifierException
    {
        return ResponseEntity.status(HttpStatus.OK).body(sSOLoginFacade.verifyCustomerAndGetBrand(customerNo));
    }
    
    @RequestMapping(value="/getApplicationVersion",method = RequestMethod.GET)
    @ResponseBody
    @Operation(operationId = "getApplicationVersionByName", summary = "Get Application Version", description = " ")
    public String getApplicationVersionByName(@RequestParam String appName){
        return  sSOLoginFacade.getApplicationVersionByName(appName);

    }
    
    @RequestMapping(value="/statusByBuildAndVersionNumber",method = RequestMethod.GET)
    @ResponseBody
    @ApiBaseSiteIdParam
    public Boolean getStatusByBuildAndVersionNumber(@RequestParam String buildNumber, @RequestParam String versionNumber){
        return  sSOLoginFacade.getStatusByBuildAndVersionNumber(buildNumber, versionNumber);

    }

    @RequestMapping(value="/getApplicationVersionByNumber",method = RequestMethod.GET)
    @ResponseBody
    @ApiBaseSiteIdParam
    public Boolean getApplicationVersionByNumber(@RequestParam int buildNumber){
        return  sSOLoginFacade.getApplicationVersionByNumber(buildNumber);
    }

    @RequestMapping(value="/statusByBuildAndVersionNo",method = RequestMethod.GET)
    @ResponseBody
    @ApiBaseSiteIdParam
    public Boolean getStatusByBuildAndVersionNo(@RequestParam String buildNumber, @RequestParam String versionNumber){
        return  sSOLoginFacade.getStatusByBuildAndVersionNo(buildNumber, versionNumber);

    }

    @RequestMapping(value="/getApplicationVersionByNo",method = RequestMethod.GET)
    @ResponseBody
    @ApiBaseSiteIdParam
    public Boolean getApplicationVersionByNo(@RequestParam int buildNumber){
        return  sSOLoginFacade.getApplicationVersionByNo(buildNumber);
    }

    @RequestMapping(value="/getAppSettings",method = RequestMethod.GET)
    @ResponseBody
    @ApiBaseSiteIdParam
    public DropdownListWsDTO getAppSettings(){
        return getDataMapper().map(sSOLoginFacade.getAppSettings(),DropdownListWsDTO.class,BASIC_FIELD_SET);
    }
}
