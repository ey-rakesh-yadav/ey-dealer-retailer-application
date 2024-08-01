package com.scl.occ.controllers;

import java.util.Objects;

import javax.annotation.Resource;


import com.scl.facades.data.*;
import com.scl.occ.dto.*;

import org.apache.commons.lang.StringUtils;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.GetMapping;

import com.scl.facades.customer.SclCustomerFacade;
import com.scl.facades.customer.SclEndCustomerFacade;
import com.scl.facades.otp.SmsOtpFacade;
import com.scl.occ.annotation.ApiBaseSiteIdAndTerritoryParam;
import com.scl.occ.annotation.ApiBaseSiteIdAndUserIdAndTerritoryParam;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.scl.occ.dto.EndCustomerWsDto;
import com.scl.occ.dto.EndCustomerListWsDTO;
import com.scl.occ.dto.EndCustomerDealerWsDto;
import com.scl.occ.dto.EndCustomerDealerListWsDTO;
import com.scl.occ.dto.ProductsListWsDTO;

import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.servicelayer.search.paginated.util.PaginatedSearchUtils;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import de.hybris.platform.webservicescommons.mapping.FieldSetLevelHelper;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.commercewebservicescommons.dto.user.UserWsDTO;


import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping(value = "/{baseSiteId}/endCustomer")
@ApiVersion("v2")
@Tag(name = "End Customer Controller")
public class EndCustomerController extends SclBaseController {

	@Autowired
	SclEndCustomerFacade sclEndCustomerFacade;

	@Resource
    private SclCustomerFacade sclCustomerFacade;
	
	@Resource(name = "dataMapper")
    private DataMapper dataMapper;
	
	@Resource
	public SmsOtpFacade smsOtpFacade;
	
	private static final String DEFAULT_FIELD_SET = FieldSetLevelHelper.DEFAULT_LEVEL;
	
	private static final Logger LOGGER = Logger.getLogger(EndCustomerController.class);
	
	protected DataMapper getDataMapper() {
        return dataMapper;
    }
	
	@ResponseStatus(value = HttpStatus.CREATED)
	@PostMapping(value = "/saveEndCustomerDetails", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(operationId = "saveEndCustomerDetails", summary = "Save End Customer Details")
	@ApiBaseSiteIdAndTerritoryParam
	@ResponseBody
	public ResponseEntity<String> saveEndCustomerDetails(@Parameter(description = "End Customer Details") @RequestBody final EndCustomerWsDto endCustomerWsDto, @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
		var endCustomerData = dataMapper.map(endCustomerWsDto, SclEndCustomerData.class, fields);
		LOGGER.info("End Customer save called in EndCustomerController is having value of endCustomerData received is ==>>" + endCustomerData);
        String customerUid = sclEndCustomerFacade.saveEndCustomerData(endCustomerData);
        LOGGER.info("End Customer Data created and customreUid is==>>" + customerUid);
        if (Objects.nonNull(customerUid)) {
            return ResponseEntity.status(HttpStatus.CREATED).body(customerUid);
        }
        LOGGER.info("End Customer Data not created successfully. Please verify.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error Occured in creating End Customer");
	}

    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/getEndCustomerDetails", method = RequestMethod.GET)
    @Operation(operationId = "getEndCustomerDetails", summary = "Get Registered End Customer Details")
    @ResponseBody
    @ApiBaseSiteIdParam
    public EndCustomerWsDto getEndCustomerDetails(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
        var endCustomerRegisteredData = sclEndCustomerFacade.getRegisteredEndCustomer();
        return dataMapper.map(endCustomerRegisteredData, EndCustomerWsDto.class, fields);
    }

    @Secured({"ROLE_B2BADMINGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERGROUP"})
    @RequestMapping(value = "/getPaginatedPartnerList", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.CREATED)
    @Operation(operationId = "getPaginatedDealersList", summary = "Get Paginated Dealers List")
    @ApiBaseSiteIdParam
    @ResponseBody
    public EndCustomerDealerListWsDTO getPaginatedCustomersList(@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields,
                                                                @RequestParam(required = false) final String brand,
                                                                @RequestParam(required = false) final String state,
                                                                @RequestParam(required = false) final String district,
                                                                @RequestParam(required = false) final String city,
                                                                @RequestParam(required = false) final String pincode,
                                                                @RequestParam(required = false) final String influencerType,
                                                                @RequestParam(required = false) final String counterType,
                                                                @RequestParam(defaultValue = DEFAULT_CURRENT_PAGE) final int currentPage,
                                                                @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int pageSize,
                                                                final HttpServletResponse response)
    {
        final SearchPageData searchPageData = PaginatedSearchUtils.createSearchPageDataWithPagination(pageSize, currentPage, true);
        SclEndCustomerDealerListData listData = new SclEndCustomerDealerListData();
        SearchPageData<SclEndCustomerDealerData> searchList = sclEndCustomerFacade.getDealersList(searchPageData,brand,state,district,city,pincode,influencerType,counterType);
        listData.setEndCustomerDealers(searchList.getResults());

        if (searchList.getPagination() != null)
        {
            response.setHeader(HEADER_TOTAL_COUNT, String.valueOf(searchList.getPagination().getTotalNumberOfResults()));
        }
        return dataMapper.map(listData, EndCustomerDealerListWsDTO.class, fields);
    }

    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping(value = "/sendLoginSmsOtp")
    @Operation(operationId = "sendLoginSmsOtp", summary = "Send Login Sms Otp")
    @ResponseBody
    @ApiBaseSiteIdParam
    public ResponseEntity<Boolean> sendLoginSmsOtp(@RequestParam final String mobileNo) {
        Boolean smsSent = sclCustomerFacade.sendOnboardingSmsOtp("",mobileNo);

        if (smsSent.equals(Boolean.TRUE)) {
            return ResponseEntity.status(HttpStatus.CREATED).body(Boolean.TRUE);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Boolean.FALSE);
    }
}
