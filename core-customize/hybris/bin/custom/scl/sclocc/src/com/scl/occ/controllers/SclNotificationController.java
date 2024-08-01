package com.scl.occ.controllers;

import com.scl.facades.SclNotificationFacade;
import com.scl.facades.data.SiteVisitFormData;
import com.scl.occ.security.SclSecuredAccessConstants;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller
@RequestMapping(value = "/{baseSiteId}/users/{userId}/notifications")
@Tag(name = "Scl Notifications")
public class SclNotificationController extends SclBaseController{

    @Autowired
    SclNotificationFacade sclNotificationFacade;

    @Secured({ SclSecuredAccessConstants.ROLE_B2BADMINGROUP, SclSecuredAccessConstants.ROLE_TRUSTED_CLIENT,SclSecuredAccessConstants.ROLE_CUSTOMERGROUP,SclSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/updateNotificationStatus/{siteMessageId}", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public boolean updateNotificationStatus(@PathVariable String siteMessageId)
    {
        return sclNotificationFacade.updateNotificationStatus(siteMessageId);
    }
}
