package com.eydms.occ.controllers;

import com.eydms.facades.EyDmsNotificationFacade;
import com.eydms.facades.data.SiteVisitFormData;
import com.eydms.occ.security.EyDmsSecuredAccessConstants;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller
@RequestMapping(value = "/{baseSiteId}/users/{userId}/notifications")
@Tag(name = "EyDms Notifications")
public class EyDmsNotificationController extends EyDmsBaseController {

    @Autowired
    EyDmsNotificationFacade eydmsNotificationFacade;

    @Secured({ EyDmsSecuredAccessConstants.ROLE_B2BADMINGROUP, EyDmsSecuredAccessConstants.ROLE_TRUSTED_CLIENT,EyDmsSecuredAccessConstants.ROLE_CUSTOMERGROUP,EyDmsSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP })
    @RequestMapping(value="/updateNotificationStatus/{siteMessageId}", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    @ResponseBody
    @ApiBaseSiteIdAndUserIdParam
    public boolean updateNotificationStatus(@PathVariable String siteMessageId)
    {
        return eydmsNotificationFacade.updateNotificationStatus(siteMessageId);
    }
}
