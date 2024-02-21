package com.eydms.occ.controllers;

import com.eydms.facades.SlctCrmIntegrationFacade;
import com.eydms.facades.data.SalesPlanningBottomUpIntegrationListData;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/slctCrmSpIntegration")
public class SlctCrmSalesPlanningIntegrationController extends EyDmsBaseController {

    @Autowired
    SlctCrmIntegrationFacade slctCrmIntegrationFacade;

    @Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/BottomUpSalesPlanning", method = RequestMethod.GET)
    @ResponseBody
    public SalesPlanningBottomUpIntegrationListData getBottomUpSalesPlanningData() throws CMSItemNotFoundException, ConversionException
    {
        return slctCrmIntegrationFacade.getBottomUpSalesPlanningData();
    }
}
