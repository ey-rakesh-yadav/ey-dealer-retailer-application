package com.scl.occ.controllers;

import com.scl.facades.TerritoryMasterFacade;
import com.scl.facades.data.RequestCustomerData;
import com.scl.facades.data.TerritoryData;
import com.scl.facades.data.TerritoryListData;
import com.scl.facades.prosdealer.data.CustomerListData;
import com.scl.facades.prosdealer.data.DealerListData;
import com.scl.occ.annotation.ApiBaseSiteIdAndUserIdAndTerritoryParam;
import com.scl.occ.dto.CustomerListWsDTO;
import com.scl.occ.dto.dealer.DealerListWsDTO;
import com.scl.occ.security.SclSecuredAccessConstants;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(value = "/{baseSiteId}/users/{userId}/territoryMaster")
@ApiVersion("v2")
@Tag(name = "Territory Master Controller")
public class TerritoryMasterController extends SclBaseController{

    @Autowired
    TerritoryMasterFacade territoryMasterFacade;

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getTerritoryById", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public TerritoryData getTerritoryById(@RequestParam (required = false) String territoryId)
    {
        return territoryMasterFacade.getTerritoryById(territoryId);
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getTerritoriesForCustomer", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public TerritoryListData getTerritoriesForCustomer()
    {
       return territoryMasterFacade.getTerritoriesForCustomer();
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getTerritoriesForSO", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public TerritoryListData getTerritoriesForSO()
    {
        return territoryMasterFacade.getTerritoriesForSO();
    }

    @RequestMapping(value="/getCustomerForUser", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public CustomerListWsDTO getCustomerForUser(@RequestBody final RequestCustomerData customerData)
    {
        CustomerListData dataList =  territoryMasterFacade.getCustomerForUser(customerData);
        return getDataMapper().map(dataList,CustomerListWsDTO.class, BASIC_FIELD_SET);
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getTerritoryForUser", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public TerritoryListData getTerritoryForUser(@RequestParam (required = false) String territoryId)
    {
        return territoryMasterFacade.getTerritoryForUser(territoryId);
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/getAllSalesOfficersByState", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public DealerListWsDTO getAllSalesOfficersByState(@RequestParam(required = true) final String state)
    {
        DealerListData dataList = territoryMasterFacade.getAllSalesOfficersByState(state);
        return getDataMapper().map(dataList,DealerListWsDTO.class, BASIC_FIELD_SET);

    }

}
