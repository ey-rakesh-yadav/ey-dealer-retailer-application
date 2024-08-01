/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.scl.integration.controller;

import static com.scl.integration.constants.SclintegrationConstants.PLATFORM_LOGO_CODE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.scl.integration.service.SclintegrationService;


@Controller
public class SclintegrationHelloController
{
	@Autowired
	private SclintegrationService sclintegrationService;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String printWelcome(final ModelMap model)
	{
		model.addAttribute("logoUrl", sclintegrationService.getHybrisLogoUrl(PLATFORM_LOGO_CODE));
		return "welcome";
	}
}
