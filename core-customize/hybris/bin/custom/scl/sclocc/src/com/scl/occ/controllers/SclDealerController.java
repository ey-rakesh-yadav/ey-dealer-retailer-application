package com.scl.occ.controllers;

import java.util.List;
import java.util.Objects;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scl.facades.CreditLimitData;
import com.scl.facades.data.*;
import com.scl.occ.dto.LiftingBlockWsDTO;
import com.scl.occ.dto.OrderBlockWsDTO;
import com.scl.occ.dto.PartnerCustomerListWsDTO;
import com.scl.occ.dto.PartnerCustomerWsDTO;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.scl.facades.DealerFacade;
import com.scl.occ.annotation.ApiBaseSiteIdAndTerritoryParam;
import com.scl.occ.annotation.ApiBaseSiteIdAndUserIdAndTerritoryParam;
import com.scl.occ.security.SclSecuredAccessConstants;

import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@Controller
@RequestMapping(value = "/{baseSiteId}/sclDealerCont")
@ApiVersion("v2")
@Tag(name = "SCL Dealer Controller")
public class SclDealerController extends SclBaseController{

    private static final Logger LOG = LogManager.getLogger(SclDealerController.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    DealerFacade dealerFacade;

    @Resource(name = "b2bCustomerFacade")
    private CustomerFacade customerFacade;

    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/getCustomerProfile", method = RequestMethod.GET)
    @Operation(operationId = "GetCustomerProfile", summary = "Get Customer Profile")
    @ResponseBody
    @ApiBaseSiteIdParam
    public SclCustomerData getCustomerProfile(@Parameter(description = "uid") @RequestParam String uid) throws Exception {
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
    public SCLDealerSalesAllocationData getStockAllocationForDealer(@Parameter(description = "productCode", required=false) @RequestParam String productCode) {
        String dealerCode = null;
        return dealerFacade.getStockAllocationForDealer(productCode);
    }

    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
    @RequestMapping(value = "/getRetailerStockAllocation", method = RequestMethod.GET)
    @Operation(operationId = "getRetailerStockAllocation", summary = "Get stock allocation of retailer for a specified product")
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public SCLDealerSalesAllocationData getStockAllocationForRetailer(@Parameter(description = "productCode", required=false) @RequestParam String productCode) {
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

    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/checkOrderAllowed", method = RequestMethod.GET)
    @Operation(operationId = "Check Order Allowed", summary = "Check Order Allowed", description = "Check Order Allowed or not.")
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    @ApiBaseSiteIdAndTerritoryParam
    public OrderBlockWsDTO checkOrderAllowed(@RequestParam (required = false) String dealerUid,@RequestParam (required = false) String retailerUid,
                                             @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields, final HttpServletResponse response)
    {
        OrderBlockWsDTO orderBlockWsDTO = dealerFacade.getDealerOrderBlock(dealerUid,retailerUid);
        return orderBlockWsDTO;
    }

    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/checkLiftingAllowed", method = RequestMethod.GET)
    @Operation(operationId = "Check Lifting Allowed", summary = "Check Lifting Allowed", description = "Check Lifting Allowed or not.")
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    @ApiBaseSiteIdAndTerritoryParam
    public LiftingBlockWsDTO checkLiftingAllowed(@RequestParam (required = false) String dealerUid,@RequestParam (required = false) String retailerUid,
                                                 @RequestParam (required = false) String influencerUid,
                                                 @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields, final HttpServletResponse response)
    {
        LiftingBlockWsDTO liftingBlockWsDTO = dealerFacade.getCustomerLiftingBlock(dealerUid,retailerUid,influencerUid);
        return liftingBlockWsDTO;
    }

    @RequestMapping(value = "/getPartnerCustomers", method = RequestMethod.GET)
    @Operation(operationId = "GetPartnerCustomer", summary = "Get List of Partner Customers for Dealer")
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdParam
    public PartnerCustomerListWsDTO getPartnerCustomer(@RequestParam (required = false) String dealerUid,@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,final HttpServletResponse response,  @Parameter(description = "Current Page is Manage Widget or not")  @RequestParam (required = true) final boolean isManagePartnerWidget)
    {
        if(StringUtils.isEmpty(dealerUid)){
            throw new IllegalArgumentException(String.format("Please provide Dealer uid %s",dealerUid));
        }
        PartnerCustomerListData partnerCustomerListData = dealerFacade.getPartnerCustomers(dealerUid, isManagePartnerWidget);
        return getDataMapper().map(partnerCustomerListData, PartnerCustomerListWsDTO.class, fields);

    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/saveExtendedPartnerInfo", method = RequestMethod.POST)
    @Operation(operationId = "SaveExtendedPartnerInfo", summary = "Save Extended Partner for Dealer")
    @ResponseBody
    @ApiBaseSiteIdParam
    public PartnerCustomerWsDTO saveExtendedPartnerInfo(@RequestBody PartnerCustomerData partnerCustomerData, @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields, final HttpServletResponse response) {
        try {
            String json = objectMapper.writeValueAsString(partnerCustomerData);
            LOG.info("Partner Customer Data for SaveExtendedPartnerInfo API : " + json);
        } catch (Exception e) {
            LOG.error("Error occurred while logging Partner Customer Data object in saveExtendedPartnerInfo  API", e);
        }
        PartnerCustomerData extendPartnerInfoData = dealerFacade.saveExtendedPartnerInfo(partnerCustomerData);

        if(Objects.nonNull(extendPartnerInfoData.getIsSuccessful()) && extendPartnerInfoData.getIsSuccessful()) {
            response.setStatus(HttpServletResponse.SC_CREATED);
        }
        else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        return getDataMapper().map(extendPartnerInfoData, PartnerCustomerWsDTO.class, fields);
    }

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/updatePartnerCustomerInfo", method = RequestMethod.POST)
    @Operation(operationId = "UpdatePartnerCustomerInfo", summary = "Edit or Delete Partner Customer Info")
    @ResponseBody
    @ApiBaseSiteIdAndUserIdAndTerritoryParam
    public PartnerCustomerWsDTO updatePartnerCustomerInfo(@RequestBody PartnerCustomerData partnerCustomerData, @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields, @RequestParam (required = true) String operationType,  final HttpServletResponse response) {
        if(StringUtils.isBlank(partnerCustomerData.getId())) {
            throw new IllegalArgumentException(String.format("Please provide Partner Customer ID"));
        }
        try {
            String json = objectMapper.writeValueAsString(partnerCustomerData);
            LOG.info("Partner Customer Data for updatePartnerCustomerInfo API  : " + json);
        } catch (Exception e) {
            LOG.error("Error occurred while logging Partner Customer Data object in updatePartnerCustomerInfo API", e);
        }
        PartnerCustomerData updatedPartnerCustomerData = dealerFacade.updatePartnerCustomerInfo(partnerCustomerData,operationType);

        if(Objects.nonNull(updatedPartnerCustomerData.getIsSuccessful()) && updatedPartnerCustomerData.getIsSuccessful()) {
            response.setStatus(HttpServletResponse.SC_CREATED);
        }
        else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        return getDataMapper().map(updatedPartnerCustomerData, PartnerCustomerWsDTO.class, fields);
    }

}
