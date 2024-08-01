package com.scl.occ.controllers;

import com.scl.core.model.SclCustomerModel;
import com.scl.core.model.SclUserModel;
import com.scl.facades.customer.SclCustomerFacade;
import com.scl.occ.dto.SclOrgUserWsDto;
import com.scl.occ.security.SclSecuredAccessConstants;
import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.commerceservices.request.mapping.annotation.RequestMappingOverride;
import de.hybris.platform.commercewebservicescommons.annotation.SiteChannelRestriction;
import de.hybris.platform.commercewebservicescommons.dto.user.UserWsDTO;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import de.hybris.platform.webservicescommons.mapping.FieldSetLevelHelper;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

import static com.scl.occ.constants.ScloccConstants.OCC_REWRITE_OVERLAPPING_BASE_SITE_USER_PATH;

@Controller
@RequestMapping(value = OCC_REWRITE_OVERLAPPING_BASE_SITE_USER_PATH)
@ApiVersion("v2")
@Tag(name = "SCL B2B Users")
public class SclB2BUsersController extends SclBaseController{

    protected static final String API_COMPATIBILITY_B2B_CHANNELS = "api.compatibility.b2b.channels";
    private static final Logger LOG = LoggerFactory.getLogger(SclB2BUsersController.class);
    @Resource(name = "b2bCustomerFacade")
    private CustomerFacade customerFacade;
    @Resource(name = "dataMapper")
    protected DataMapper dataMapper;

    @Resource
    private UserService userService;

    @Resource
    private SclCustomerFacade sclCustomerFacade;

    @Secured({ SclSecuredAccessConstants.ROLE_CUSTOMERGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,
            SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(method = RequestMethod.GET)
    @RequestMappingOverride(priorityProperty = "sclocc.SclB2BUsersController.getUser.priority")
    @SiteChannelRestriction(allowedSiteChannelsProperty = API_COMPATIBILITY_B2B_CHANNELS)
    @ResponseBody
    @Operation(operationId = "getOrgUser", summary = "Get a Scl B2B user profile", description = "Returns a Scl B2B user profile.")
    @ApiBaseSiteIdAndUserIdParam
    public SclOrgUserWsDto getOrgUser(@ApiFieldsParam @RequestParam(required = false, defaultValue = FieldSetLevelHelper.DEFAULT_LEVEL) final String fields,@RequestParam(required = false) final String customerId)
    {
        CustomerData customerData;
        try {
            if(StringUtils.isNotBlank(customerId)){
                UserModel  userModel  =userService.getUserForUID(customerId);
                customerData =sclCustomerFacade.getCustomerData(userModel);
            }else{
                 customerData = customerFacade.getCurrentCustomer();
            }
            final SclOrgUserWsDto dto = dataMapper.map(customerData, SclOrgUserWsDto.class, fields);
            return dto;
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}
