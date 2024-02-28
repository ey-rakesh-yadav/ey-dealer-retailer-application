package com.eydms.occ.controllers;


import com.eydms.facades.customer.EyDmsCustomerFacade;
import com.eydms.facades.prosdealer.ProsDealerFacade;
import com.eydms.facades.prosdealer.data.ProsDealerData;
import com.eydms.occ.dto.prosdealer.ProsDealerWsDTO;
import com.eydms.occ.security.EyDmsSecuredAccessConstants;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * Controller class Prospective  Dealer Management
 */
@Controller
@RequestMapping(value = "/{baseSiteId}/dealer/{uid}")
@ApiVersion("v2")
@Tag(name = "Prospective Dealer Management")
public class ProsDealersController extends EyDmsBaseController {

    @Resource
    private EyDmsCustomerFacade eydmsCustomerFacade;

    @Resource
    private ProsDealerFacade prosDealerFacade;

    /**
     * upload documents of dealer with dealer code
     * @param
     *
     * @return
     */
    @Secured({ EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CLIENT })
    @RequestMapping(value = "/upload/{documentType}", method = RequestMethod.POST,consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(operationId = "uploadDealerDocument", summary = "Uploads the document of the dealer", description = "Uploads the document of the dealer")
    @ApiBaseSiteIdParam
    public ResponseEntity<Boolean> uploadDocument(
            @Parameter(description = "Prospective dealer Uid .", required = true) @PathVariable final String uid,
            @Parameter(description = "document type .", required = true) @PathVariable final String documentType,
            @Parameter(description = "Object contains document file",required = true) @RequestParam(value = "file") final MultipartFile file) {

        validateDocument(file);
        prosDealerFacade.uploadDealerDocument(uid,documentType,file);
        return ResponseEntity.status(HttpStatus.CREATED).body(true);
    }

    @Secured({ EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CLIENT })
    @RequestMapping(method = RequestMethod.GET,value = "/prospdealerdetails")
    @ResponseBody
    @Operation(operationId = "getProspectiveDealerDetails", summary = "Returns Prospective dealer details", description = "Returns details of  prospective dealer with the uid.")
    @ApiBaseSiteIdParam
    public ResponseEntity<ProsDealerWsDTO> getProspectiveDealerDetail(
            @Parameter(description = "Pros Dealer Code identifier", required = true) @PathVariable final String uid){

        ProsDealerData prosDealerDetails = eydmsCustomerFacade.getProsDealerDetailsByUid(uid);
        ProsDealerWsDTO prosDealerWsDTO= getDataMapper().map(prosDealerDetails,ProsDealerWsDTO.class, FULL_FIELD_SET);
        return ResponseEntity.status(HttpStatus.OK).body(prosDealerWsDTO);

    }
}
