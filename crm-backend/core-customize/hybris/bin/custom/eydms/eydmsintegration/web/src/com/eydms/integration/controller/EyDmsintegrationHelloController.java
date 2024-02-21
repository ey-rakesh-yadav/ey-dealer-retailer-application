/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.eydms.integration.controller;

import static com.eydms.integration.constants.EyDmsintegrationConstants.PLATFORM_LOGO_CODE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.eydms.integration.service.EyDmsintegrationService;


@Controller
public class EyDmsintegrationHelloController
{
	@Autowired
	private EyDmsintegrationService eydmsintegrationService;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String printWelcome(final ModelMap model)
	{
		model.addAttribute("logoUrl", eydmsintegrationService.getHybrisLogoUrl(PLATFORM_LOGO_CODE));
		return "welcome";
	}
}
