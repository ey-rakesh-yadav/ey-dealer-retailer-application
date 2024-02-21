    package com.eydms.occ.controllers;

    import com.eydms.facades.prosdealer.ProsDealerFacade;
    import com.eydms.facades.prosdealer.data.*;
    import com.eydms.occ.security.EyDmsSecuredAccessConstants;
    import de.hybris.platform.commerceservices.customer.DuplicateUidException;
    import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
    import de.hybris.platform.servicelayer.config.ConfigurationService;
    import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
    import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdParam;
    import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
    import io.swagger.v3.oas.annotations.tags.Tag;
    import io.swagger.v3.oas.annotations.Operation;
    import io.swagger.v3.oas.annotations.Parameter;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.access.annotation.Secured;
    import org.springframework.stereotype.Controller;
    import org.springframework.validation.Validator;
    import org.springframework.web.bind.annotation.*;

    import javax.annotation.Resource;
    import javax.annotation.security.PermitAll;
import javax.ws.rs.core.MediaType;

    @Controller
    @RequestMapping(value = "/{baseSiteId}/eydmsProsDealers")
    @ApiVersion("v2")
    @Tag(name = "EyDms Prospective Dealer")
    public class ProspectiveDealerController extends EyDmsBaseController {

        @Autowired
        ConfigurationService configurationService;

        @Autowired
        ProsDealerFacade prosDealerFacade;

        @Resource(name = "dealerBasicDetailsValidator")
        private Validator dealerBasicDetailsValidator;

        @PermitAll
        @RequestMapping(method = RequestMethod.POST, value="/saveBasicDetails")
        @ResponseBody
        @Operation(operationId = "saveBasicDetails", summary = "Save Pros Dealer Basic Details", description = "Save Pros Dealer Basic Details")
        @ApiBaseSiteIdParam
        public ResponseEntity<Boolean> saveBasicDetails(@Parameter(description = "Data object that contains information necessary for pros dealer creation", required = true)
                                                    @RequestBody BasicProsDealerData basicProsDealerData) throws DuplicateUidException {
            validate(basicProsDealerData, "basicProsDealerData", dealerBasicDetailsValidator);
            boolean response =  prosDealerFacade.saveBasicDetails(basicProsDealerData);
           return ResponseEntity.status(HttpStatus.CREATED).body(true);
        }

        @Secured({EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT, EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP })
        @RequestMapping(method = RequestMethod.POST, value="/saveDealerApplicantDetails")
        @ResponseBody
        @Operation(operationId = "saveApplicantDetails", summary = "Save Dealer Applicants Details", description = "Save Dealer Applicant Details")
        @ApiBaseSiteIdAndUserIdParam
        public ResponseEntity<Boolean> saveApplicantDetails(@Parameter(description = "Data object that contains information necessary for pros dealer creation", required = true)
                                                    @RequestBody ApplicantProsDealerData applicantProsDealerData) {
            boolean response=   prosDealerFacade.saveApplicantDetails(applicantProsDealerData);
            return ResponseEntity.status(HttpStatus.CREATED).body(true);
        }

        @Secured({EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT, EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP })
        @RequestMapping(method = RequestMethod.POST, value="/saveDealerFinancialDetails")
        @ResponseBody
        @Operation(operationId = "saveDealerFinancialDetails", summary = "Save Pros Dealer Financial Details", description = "Save Pros Dealer Financial Details")
        @ApiBaseSiteIdAndUserIdParam
        public ResponseEntity<Boolean> saveDealerFinancialDetails(@Parameter(description = "Data object that contains information necessary for pros dealer creation", required = true)
                                                            @RequestBody FinancialDetailsData financialDetailsData) {
            boolean response=  prosDealerFacade.saveDealerFinancialDetails(financialDetailsData);
            return ResponseEntity.status(HttpStatus.CREATED).body(true);
        }

        @Secured({EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT, EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP })
        @RequestMapping(method = RequestMethod.POST, value="/saveDealerBusinessDetails")
        @ResponseBody
        @Operation(operationId = "saveDealerBusinessDetails", summary = "Save Pros Dealer Business Details", description = "Save Pros Dealer Business Details")
        @ApiBaseSiteIdAndUserIdParam
        public ResponseEntity<Boolean> saveDealerBusinessDetails(@Parameter(description = "Data object that contains information necessary for pros dealer creation", required = true)
                                                               @RequestBody DealerBusinessDetailsData dealerBusinessDetailsData) {
            boolean response=  prosDealerFacade.saveDealerBusinessDetails(dealerBusinessDetailsData);
            return ResponseEntity.status(HttpStatus.CREATED).body(true);
        }


        @RequestMapping(method = RequestMethod.POST, value="/sendSmsForDealerBasicDetails")
        @ResponseBody
        @Operation(operationId = "sendSmsForDealerBasicDetails", summary = "Send SMS to Pros Dealer for Basic Details submission", description = "Send SMS to Pros Dealer for Basic Details submission")
        @ApiBaseSiteIdParam
        public ResponseEntity<Boolean> sendSmsForDealerBasicDetails(@Parameter(description = "Data object that contains information necessary for sending sms to pros dealer for basic details", required = true)
                                                        @RequestParam String username) {
            boolean response =  prosDealerFacade.sendSmsForDealerBasicDetails(username);
            return ResponseEntity.status(HttpStatus.CREATED).body(true);
        }

        @RequestMapping(method = RequestMethod.POST, value="/sendSmsForDealerFinancialDetails")
        @ResponseBody
        @Operation(operationId = "sendSmsForDealerFinancialDetails", summary = "Send SMS to Pros Dealer for final form submission", description = "Send SMS to Pros Dealer for final form submission")
        @ApiBaseSiteIdParam
        public ResponseEntity<Boolean> sendSmsForDealerFinancialDetails(@Parameter(description = "Data object that contains information necessary for sending sms to pros dealer for final form submission", required = true)
                                                              @RequestParam String username) {
            boolean response =  prosDealerFacade.sendSmsForDealerFinancialDetails(username);
            return ResponseEntity.status(HttpStatus.CREATED).body(true);
        }


        public Validator getDealerBasicDetailsValidator() {
            return dealerBasicDetailsValidator;
        }

        public void setDealerBasicDetailsValidator(Validator dealerBasicDetailsValidator) {
            this.dealerBasicDetailsValidator = dealerBasicDetailsValidator;
        }
    }