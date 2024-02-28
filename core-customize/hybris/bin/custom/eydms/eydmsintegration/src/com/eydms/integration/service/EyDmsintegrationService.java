/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.eydms.integration.service;

public interface EyDmsintegrationService
{
	String getHybrisLogoUrl(String logoCode);

	void createLogo(String logoCode);
}
