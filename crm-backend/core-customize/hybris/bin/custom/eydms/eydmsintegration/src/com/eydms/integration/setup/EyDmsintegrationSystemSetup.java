/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.eydms.integration.setup;

import static com.eydms.integration.constants.EyDmsintegrationConstants.PLATFORM_LOGO_CODE;

import de.hybris.platform.core.initialization.SystemSetup;

import java.io.InputStream;

import com.eydms.integration.constants.EyDmsintegrationConstants;
import com.eydms.integration.service.EyDmsintegrationService;


@SystemSetup(extension = EyDmsintegrationConstants.EXTENSIONNAME)
public class EyDmsintegrationSystemSetup
{
	private final EyDmsintegrationService eydmsintegrationService;

	public EyDmsintegrationSystemSetup(final EyDmsintegrationService eydmsintegrationService)
	{
		this.eydmsintegrationService = eydmsintegrationService;
	}

	@SystemSetup(process = SystemSetup.Process.INIT, type = SystemSetup.Type.ESSENTIAL)
	public void createEssentialData()
	{
		eydmsintegrationService.createLogo(PLATFORM_LOGO_CODE);
	}

	private InputStream getImageStream()
	{
		return EyDmsintegrationSystemSetup.class.getResourceAsStream("/eydmsintegration/sap-hybris-platform.png");
	}
}
