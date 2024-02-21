package com.eydms.occ.controllers;

import java.util.List;

import javax.annotation.Resource;

import com.eydms.facades.CreditLimitData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.eydms.facades.DealerFacade;
import com.eydms.facades.data.MonthlySalesData;
import com.eydms.facades.data.OnboardingPartnerData;
import com.eydms.facades.data.EYDMSDealerSalesAllocationData;
import com.eydms.facades.data.EyDmsCustomerData;
import com.eydms.occ.annotation.ApiBaseSiteIdAndTerritoryParam;
import com.eydms.occ.annotation.ApiBaseSiteIdAndUserIdAndTerritoryParam;

import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@Controller
@RequestMapping(value = "/{baseSiteId}/eydmsDealerCont")
@ApiVersion("v2")
@Tag(name = "EYDMS Dealer Controller")
public class EyDmsDealerController extends EyDmsBaseController {

	@Autowired
	DealerFacade dealerFacade;
	
	@Resource(name = "b2bCustomerFacade")
    private CustomerFacade customerFacade;
	
	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/getCustomerProfile", method = RequestMethod.GET)
    @Operation(operationId = "GetCustomerProfile", summary = "Get Customer Profile")
	@ResponseBody
    @ApiBaseSiteIdParam
    public EyDmsCustomerData getCustomerProfile(@Parameter(description = "uid") @RequestParam String uid) throws Exception {
		return dealerFacade.getCustomerProfile(uid);
	}
	
	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value="/customerMonthWiseSales", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdParam
    public List<MonthlySalesData> getcustomerMonthWiseSales(@Parameter(description = "userId") @RequestParam String userId, @Parameter(description = "filter") @RequestParam String filter, @Parameter(description = "customerType") @RequestParam String customerType,
                                                            @RequestParam(required = false) final String customerUid)
    {
        return dealerFacade.getLastSixMonthSalesForDealer(userId, filter, customerType,customerUid);
    }

    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/getHighPriorityActions", method = RequestMethod.GET)
    @Operation(operationId = "GetHighPriorityActions", summary = "Get High Priority Actions")
    @ResponseBody
    @ApiBaseSiteIdParam
    public CreditLimitData getHighPriorityActions(@Parameter(description = "dealerCode") @RequestParam String uid){
        return dealerFacade.getHighPriorityActions(uid);
    }
    
    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/getHighPriorityActionsForDealer", method = RequestMethod.GET)
    @Operation(operationId = "GetHighPriorityActionsForDealer", summary = "Get High Priority Actions for dealer")
    @ResponseBody
    @ApiBaseSiteIdParam
    public List<CreditLimitData> getHighPriorityActionsForDealer(@Parameter(description = "dealerCode") @RequestParam String uid){
        return dealerFacade.getHighPriorityActionsForDealer(uid);
    }
    
    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @RequestMapping(value = "/getDealerStockAllocation", method = RequestMethod.GET)
    @Operation(operationId = "getDealerStockAllocation", summary = "Get stock allocation for dealer and for a specified product")
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
	public EYDMSDealerSalesAllocationData getStockAllocationForDealer(@Parameter(description = "productCode", required=false) @RequestParam String productCode) {
		String dealerCode = null;
        return dealerFacade.getStockAllocationForDealer(productCode);
    }
    
    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @RequestMapping(value = "/getRetailerStockAllocation", method = RequestMethod.GET)
    @Operation(operationId = "getRetailerStockAllocation", summary = "Get stock allocation of retailer for a specified product")
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
	public EYDMSDealerSalesAllocationData getStockAllocationForRetailer(@Parameter(description = "productCode", required=false) @RequestParam String productCode) {
        return dealerFacade.getStockAllocationForRetailer(productCode);
    }
    
    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})   
    @RequestMapping(value = "/uidByCustomerNo", method = RequestMethod.GET)
	@Operation(operationId = "uidByCustomerNo", summary = "Uid by Customer NO")
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    @ApiBaseSiteIdAndTerritoryParam
	public String getUidByCustomerNo()
	{
    	CustomerData customerData = customerFacade.getCurrentCustomer();
    	return customerData.getUid();
	}
	 
}
