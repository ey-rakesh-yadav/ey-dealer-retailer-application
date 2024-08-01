/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.scl.occ.constants;

@SuppressWarnings({"deprecation","squid:CallToDeprecatedMethod"})
public class ScloccConstants extends GeneratedScloccConstants
{
	public static final String EXTENSIONNAME = "sclocc";

	public static final String OCC_REWRITE_OVERLAPPING_BASE_SITE_USER_PATH = "#{ ${occ.rewrite.overlapping.paths.enabled:false} ? '/{baseSiteId}/orgUsers/{userId}' : '/{baseSiteId}/users/{userId}'}";
	private ScloccConstants()
	{
		//empty
	}
}
