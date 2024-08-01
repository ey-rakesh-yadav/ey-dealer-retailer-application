/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.scl.integration.setup;

import static com.scl.integration.constants.SclintegrationConstants.PLATFORM_LOGO_CODE;

import de.hybris.platform.core.initialization.SystemSetup;

import java.io.InputStream;

import com.scl.integration.constants.SclintegrationConstants;
import com.scl.integration.service.SclintegrationService;


@SystemSetup(extension = SclintegrationConstants.EXTENSIONNAME)
public class SclintegrationSystemSetup
{
	private final SclintegrationService sclintegrationService;

	public SclintegrationSystemSetup(final SclintegrationService sclintegrationService)
	{
		this.sclintegrationService = sclintegrationService;
	}

	@SystemSetup(process = SystemSetup.Process.INIT, type = SystemSetup.Type.ESSENTIAL)
	public void createEssentialData()
	{
		sclintegrationService.createLogo(PLATFORM_LOGO_CODE);
	}

	private InputStream getImageStream()
	{
		return SclintegrationSystemSetup.class.getResourceAsStream("/sclintegration/sap-hybris-platform.png");
	}
}
