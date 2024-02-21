package com.eydms.occ.controllers;

import com.eydms.dto.lead.LeadWsDTO;
import com.eydms.facades.lead.data.LeadData;
import com.eydms.facades.lead.EyDmsLeadFacade;
import com.eydms.occ.security.EyDmsSecuredAccessConstants;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.ws.rs.core.MediaType;

@Controller
@RequestMapping(value = "/{baseSiteId}/users/{userId}/eydmsLeads")
@ApiVersion("v2")
@Tag(name = "EyDms Lead Management")
public class EyDmsLeadsController extends EyDmsBaseController {

    @Resource
    private EyDmsLeadFacade eydmsLeadFacade;

    /**
     * Method to create lead
     * @param leadCreation
     * @param fields
     * @return
     */
    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP })
    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @Operation(operationId = "createLead", summary = " Create a Lead", description = "Creates a new lead Entry.")
    @ApiBaseSiteIdAndUserIdParam
    public LeadWsDTO createLead(
            @Parameter(description = "Data object that contains information necessary for Lead creation", required = true) @RequestBody final LeadWsDTO leadCreation,
            @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
    {


        final LeadData leadData = getDataMapper().map(leadCreation, LeadData.class);

        String leadId = eydmsLeadFacade.updateLead(leadData);
        LeadData  updatedLeadData = eydmsLeadFacade.getLeadForLeadId(leadId);
        return getDataMapper().map(updatedLeadData, LeadWsDTO.class, fields);
    }
}
