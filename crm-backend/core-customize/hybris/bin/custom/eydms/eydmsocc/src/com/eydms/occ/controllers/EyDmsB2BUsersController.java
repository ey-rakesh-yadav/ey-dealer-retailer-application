package com.eydms.occ.controllers;

import com.eydms.occ.dto.EyDmsOrgUserWsDto;
import com.eydms.occ.security.EyDmsSecuredAccessConstants;
import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.commerceservices.request.mapping.annotation.RequestMappingOverride;
import de.hybris.platform.commercewebservicescommons.annotation.SiteChannelRestriction;
import de.hybris.platform.commercewebservicescommons.dto.user.UserWsDTO;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import de.hybris.platform.webservicescommons.mapping.FieldSetLevelHelper;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

import static com.eydms.occ.constants.EyDmsoccConstants.OCC_REWRITE_OVERLAPPING_BASE_SITE_USER_PATH;

@Controller
@RequestMapping(value = OCC_REWRITE_OVERLAPPING_BASE_SITE_USER_PATH)
@ApiVersion("v2")
@Tag(name = "EYDMS B2B Users")
public class EyDmsB2BUsersController extends EyDmsBaseController {

    protected static final String API_COMPATIBILITY_B2B_CHANNELS = "api.compatibility.b2b.channels";
    private static final Logger LOG = LoggerFactory.getLogger(EyDmsB2BUsersController.class);
    @Resource(name = "b2bCustomerFacade")
    private CustomerFacade customerFacade;
    @Resource(name = "dataMapper")
    protected DataMapper dataMapper;

    @Secured({ EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,
            EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(method = RequestMethod.GET)
    @RequestMappingOverride(priorityProperty = "eydmsocc.EyDmsB2BUsersController.getUser.priority")
    @SiteChannelRestriction(allowedSiteChannelsProperty = API_COMPATIBILITY_B2B_CHANNELS)
    @ResponseBody
    @Operation(operationId = "getOrgUser", summary = "Get a EyDms B2B user profile", description = "Returns a EyDms B2B user profile.")
    @ApiBaseSiteIdAndUserIdParam
    public EyDmsOrgUserWsDto getOrgUser(
            @ApiFieldsParam @RequestParam(required = false, defaultValue = FieldSetLevelHelper.DEFAULT_LEVEL) final String fields)
    {
        try {
            CustomerData customerData = customerFacade.getCurrentCustomer();
            final EyDmsOrgUserWsDto dto = dataMapper.map(customerData, EyDmsOrgUserWsDto.class, fields);
            return dto;
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}
