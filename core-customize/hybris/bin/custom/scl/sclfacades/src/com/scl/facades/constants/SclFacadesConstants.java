/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.scl.facades.constants;

/**
 * Global class for all SclFacades constants.
 */
public class SclFacadesConstants extends GeneratedSclFacadesConstants
{
	public static final String EXTENSIONNAME = "sclfacades";

	private SclFacadesConstants()
	{
		//empty
	}

	public static class SITE_STATUS {
		public static final String SITE_CONVERTED = "SITE_CONVERTED";
		public static final String SITE_LOST = "SITE_LOST";
		public static final String NOT_CONVERTED = "NOT_CONVERTED";
		public static final String NEED_MORE_FOLLOW_UP = "NEED_MORE_FOLLOW_UP";
		public static final String BANGUR_RUNNING_SITE = "BANGUR_RUNNING_SITE";
		public static final String CLOSED = "CLOSED";
	}

	public static class SITE_CATEGORY {
		public static final String COMPETITOR_SITE = "CS";
		public static final String BANGUR_SITE = "SS";
		public static final String FRESH_SITE = "FS";
	}
}
