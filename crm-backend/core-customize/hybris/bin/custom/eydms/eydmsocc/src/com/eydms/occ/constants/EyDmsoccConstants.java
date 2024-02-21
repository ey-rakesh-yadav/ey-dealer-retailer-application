/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.eydms.occ.constants;

@SuppressWarnings({"deprecation","squid:CallToDeprecatedMethod"})
public class EyDmsoccConstants extends GeneratedEyDmsoccConstants
{
	public static final String EXTENSIONNAME = "eydmsocc";

	public static final String OCC_REWRITE_OVERLAPPING_BASE_SITE_USER_PATH = "#{ ${occ.rewrite.overlapping.paths.enabled:false} ? '/{baseSiteId}/orgUsers/{userId}' : '/{baseSiteId}/users/{userId}'}";
	private EyDmsoccConstants()
	{
		//empty
	}
}
