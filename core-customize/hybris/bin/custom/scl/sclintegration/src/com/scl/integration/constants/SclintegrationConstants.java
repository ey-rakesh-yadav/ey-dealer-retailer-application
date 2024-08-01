/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.scl.integration.constants;

/**
 * Global class for all Sclintegration constants. You can add global constants for your extension into this class.
 */
public final class SclintegrationConstants extends GeneratedSclintegrationConstants
{
	public static final String EXTENSIONNAME = "sclintegration";

	private SclintegrationConstants()
	{
		//empty to avoid instantiating this constant class
	}

	public static final String PLATFORM_LOGO_CODE = "sclintegrationPlatformLogo";


	public static final String DEFAULT_VALUE_X ="X";
	public static final String SCL_SALESORDER_ORDERTYPE ="scl.salesOrder.OrderType";
	public static final String LINE_ITEM_OPERATION ="CREATE";
	public static final String PACKING_TYPE ="PACKED";
	public static final String UNIT_OF_MEASURE ="MT";
	public static final String FOB ="FOR";

	public static final String ORDER_PLACED_APPROVED_NOTIFICATION = "Placed Order has been Approved";
	public static final String ORDER_LINE_CANCELLED_NOTIFICATION = "Order Line has been Cancelled";

	public static class  CUSTOMER_ACCOUNT {
		public static final String DATE_FORMAT_1 = "dd/MM/yyyy";
	}
}
