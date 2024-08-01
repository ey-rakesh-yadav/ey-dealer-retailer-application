/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.scl.core.constants;

/**
 * Global class for all SclCore constants. You can add global constants for your extension into this class.
 */
public final class SclCoreConstants extends GeneratedSclCoreConstants {
	public static final String EXTENSIONNAME = "sclcore";
    public static final String CMS_SITEID = "001000";
	public static final String INVALID_MOBILE_NUMBER = "0000000000";
    public static final String SHIPTOTYPE = "WE";
	public static final String SCL_SITE = "scl";

	public static final String ALL_SCL_ = "_ALL_SCL_";

    private SclCoreConstants() {
		//empty
	}

	// implement here constants used by this extension
	public static final String QUOTE_BUYER_PROCESS = "quote-buyer-process";
	public static final String QUOTE_SALES_REP_PROCESS = "quote-salesrep-process";
	public static final String QUOTE_USER_TYPE = "QUOTE_USER_TYPE";
	public static final String QUOTE_SELLER_APPROVER_PROCESS = "quote-seller-approval-process";
	public static final String QUOTE_TO_EXPIRE_SOON_EMAIL_PROCESS = "quote-to-expire-soon-email-process";
	public static final String QUOTE_EXPIRED_EMAIL_PROCESS = "quote-expired-email-process";
	public static final String QUOTE_POST_CANCELLATION_PROCESS = "quote-post-cancellation-process";
	public static final String IMAGE_MEDIA_FOLDER_NAME = "images";

	public static final Integer CANCELLED_ORDER_VISIBLITY_DEFAULT = 30;
	public static final Integer ORDER_LISTING_VISIBLITY_DEFAULT = 90;
	public static final Integer QUANTITY_INMT_TO_BAGS = 20;
	
	public static final String DOCUMENT_PAN_CARD = "pan";
	public static final String DOCUMENT_TYPE_GST_DETAILS = "gst";
	public static final String DOCUMENT_TYPE_DD_DETAILS = "dd";
	public static final String DOCUMENT_TYPE_GODOWN_DETAILS = "godown";
	public static final String DOCUMENT_TYPE_BANK_STATEMENT = "bankstatement";
	public static final String DOCUMENT_TYPE_BLANK_CHEQUE = "blankcheque";
	public static final String DOCUMENT_TYPE_LETTER_HEAD_COPY = "letterhead";
	public static final String DOCUMENT_PAN_CARD_NAME = "Pan Card";
	public static final String DOCUMENT_TYPE_GST_DETAILS_NAME = "GST Details";
	public static final String DOCUMENT_TYPE_DD_DETAILS_NAME = "Demand Draft";
	public static final String DOCUMENT_TYPE_GODOWN_DETAILS_NAME = "Godown Details";
	public static final String DOCUMENT_TYPE_BANK_STATEMENT_NAME = "Bank Statement";
	public static final String DOCUMENT_TYPE_BLANK_CHEQUE_NAME = "Blank Cheque";
	public static final String DOCUMENT_TYPE_LETTER_HEAD_COPY_NAME = "LetterHead";
	public static final String UNDERSCORE_CHARACTER = "_";

	public static final String CREATED_SUCCESSFULLY = "201 CREATED";

	public static final String ERROR = "ERROR";

	public static final String SUCCESSFULLY = "SUCCESSFULLY";
	public static final String SHIPTOID = "ShipToId";

	public static final String ISSOREJECT = "ISSOREJECT";
	public static final String SHIPTOIDMSG = "Msg";


	public static class PROCESSING_CONSTANT {
		public static final String EVENT_AFTER_SO_APPROVAL_PARAM_NAME = "AFTER_SO_APPROVAL_EVENT";
		public static final String EVENT_AFTER_SH_APPROVAL_PARAM_NAME = "AFTER_SH_APPROVAL_EVENT";
		public static final String EVENT_AFTER_PM_APPROVAL_PARAM_NAME = "AFTER_PM_APPROVAL_EVENT";
		public static final String EVENT_AFTER_SA_DOCUMENT_VERIFICATION_PARAM_NAME = "AFTER_SA_DOCUMENT_VERIFIED_EVENT";
	}

	public static class APPROVAL_CONSTANT {
		public static final String ORDER_REVIEW_DECISION_EVENT_NAME = "ORDER_REVIEW_DECISION";
		public static final String SO_APPROVAL_WORKFLOW_COMPLETE_EVENT = "SO_APPROVAL_WORKFLOW_COMPLETE_EVENT";
		public static final String SH_APPROVAL_WORKFLOW_COMPLETE_EVENT = "SH_APPROVAL_WORKFLOW_COMPLETE_EVENT";
		public static final String PM_APPROVAL_WORKFLOW_COMPLETE_EVENT = "PM_APPROVAL_WORKFLOW_COMPLETE_EVENT";
		public static final String SA_DOCUMENT_VERIFICATION_WORKFLOW_COMPLETE_EVENT = "SA_DOCUMENT_VERIFICATION_WORKFLOW_COMPLETE_EVENT";
	}

	public static final String CUSTOMER_QUERY_ALERT_TITLE = "Customer Query Acknowledge";
	public static final String CUSTOMER_QUERY_ALERT_CONTENT = "Acknowledge is pending for prospective dealer";
	public static final String NOTIFICATION_EXPIRY = "notification.expiry.hours";

	public static final String TSMUSERSXDAYSOLD = "tsmxdaysold.xdays.old";
	public static final String CUSTOMER_QUERY_LINK = "http://sclerp.com/Account/Login";
	public static final String SCL_PROS_DEALER_DUMMY_UNIT = "DummySclUnit";
	public static final String SCL_PROS_DEALER_DUMMY_USER_GROUP = "scldealerusergroup";

	public static class CUSTOMER {
		public static final String DEALER_USER_GROUP_UID = "SclDealerGroup";
		public static final String SHIPTO_USER_GROUP_UID = "SclShipToGroup";
		public static final String INFLUENCER_USER_GROUP_UID = "SclInfluencerGroup";
		public static final String SITE_USER_GROUP_UID = "SclSiteGroup";
		public static final String RETAILER_USER_GROUP_UID = "SclRetailerGroup";
		public static final String SALES_PROMOTER_USER_GROUP_UID = "salespromotergroup";

		public static final String DEALER_USER_GROUP_TYPE = "Dealer";
		public static final String INFLUENCER_USER_GROUP_TYPE = "Influencer";
		public static final String SITE_USER_GROUP_TYPE = "Site";
		public static final String RETAILER_USER_GROUP_TYPE = "Retailer";
		public static final String DEFAULT_SCL_CUSTOMER_UNIT = "SclCustomerUnit";
		
		public static final String SALES_OFFICER_GROUP_ID = "salesofficergroup";
		public static final String TSM_GROUP_ID = "tsmgroup";
		public static final String RH_GROUP_ID = "rhgroup";
		public static final String SP_GROUP_ID = "spgroup";
		public static final String STATEHEAD_GROUP_ID = "stateheadgroup";
		public static final String CLUSTERHEAD_GROUP_ID = "clusterheadgroup";
		public static final String ZONALHEAD_GROUP_ID = "zonalheadgroup";
		public static final String NATINALHEAD_GROUP_ID = "nationalheadgroup";

		public static final String TSO_GROUP_ID = "tsogroup";
		
		public static final String DEALER_ONBOARDING_USER_GROUP_UID = "SclDealerOnboardingGroup";
		public static final String INFLUENCER_ONBOARDING_USER_GROUP_UID = "SclInfluencerOnboardingGroup";
		public static final String RETAILER_ONBOARDING_USER_GROUP_UID = "SclRetailerOnboardingGroup";

		public static final String SALES_PROMOTER_ONBOARDING_USER_GROUP_UID = "SclSalesPromoterOnboardingGroup";

		//public static final String OTP_EXPIRATION_TIME = "otp.expiration.time";
		public static final String EXCLUDE_INFLUENCER_SITE = "EXCLUDE_INFLUENCER_SITE";
		public static final String CUSTOMER_LOGIN_BLOCKED = "CUSTOMER_LOGIN_BLOCKED";
		public static final String ENABLE_CUSTOMER_LOGIN_BLOCKED_CHECK = "ENABLE_CUSTOMER_LOGIN_BLOCKED_CHECK";

	}

	public static class CUSTOMER_ACCOUNT {
		public static final String LAST_PASSWORD_CHECK_LIMIT = "last.password.check.limit";
		public static final String DATE_FORMAT_1 = "dd/MM/yyyy";
	}

	public static class ORDER {
		public static final String PENDING_FOR_APPROVAL_STATUS = "PENDING_FOR_APPROVAL";
		public static final String WAITING_FOR_DISPATCH_STATUS = "WAITING_FOR_DISPATCH";
		public static final String TO_BE_DELIVERED_BY_TODAY_STATUS = "TO_BE_DELIVERED_BY_TODAY";
		public static final String VEHICLE_ARRIVAL_CONFIRMATION_STATUS = "VEHICLE_ARRIVAL_CONFIRMATION";

		public static final String PENDING_FOR_APPROVAL_STATUS_MAPPING = "mapping.statuses.pending.for.approval";
		public static final String WAITING_FOR_DISPATCH_STATUS_MAPPING = "mapping.statuses.waiting.for.dispatch";
		public static final String TO_BE_DELIVERED_BY_TODAY_STATUS_MAPPING = "mapping.statuses.to.be.delivered.today";
		public static final String VEHICLE_ARRIVAL_CONFIRMATION_STATUS_MAPPING = "mapping.statuses.vehicle.arrival.confirmation";

		public static final String NO_MATCHING_STATUS_ERROR = "Not a valid order status";
		public static final String ENUM_VALUES_SEPARATOR = ",";

		public static final String ORDER_CANCELLATION_STATUS = "CANCELLATION";
		public static final String ORDER_CANCELLATION_STATUS_MAPPING = "mapping.statuses.order.cancel";

		public static final String ORDER_LINE_CANCELLATION_STATUS = "LINE_CANCELLATION";
		public static final String ORDER_LINE_CANCELLATION_STATUS_MAPPING = "mapping.statuses.order.line.cancel";

		public static final String ORDER_LINE_CANCELLATION_REASON_CODE = "CANCEL_REASON_REJECTION_CODE";


		public static final String SAP_CODE_BLOCKED = "SAP_CODE_BLOCKED";
		public static final String SAP_CODE_BLOCKED_CUSTOMER = "SAP_CODE_BLOCKED_CUSTOMER";
		public static final String SAP_ORDER_PLACEMENT_BLOCKED= "SAP_ORDER_PLACEMENT_BLOCKED";
		public static final String PLACE_ORDER_ENABLED="PLACE_ORDER_ENABLED";
		public static final String PENDING_ORDER_REQUISITION = "PENDING";
		public static final String EXPIRED_ORDER_REQUISITION = "EXPIRED";
		public static final String APPROVED_ORDER_REQUISITION = "APPROVED";
		public static final String REJECTED_ORDER_REQUISITION = "REJECTED";

		public static final String PENDING_FOR_OR_STATUS_MAPPING = "mapping.statuses.pending.or";
		public static final String APPROVED_FOR_OR_STATUS_MAPPING = "mapping.statuses.approved.or";
		public static final String REJECTED_FOR_OR_STATUS_MAPPING = "mapping.statuses.rejected.or";
		public static final String EXPIRED_FOR_OR_STATUS_MAPPING = "mapping.statuses.expired.or";

		public static final String UDAAN_CONNECT_PLACE_ORDER_ENABLED = "UDAAN_CONNECT_PLACE_ORDER_ENABLED";
		public static final String UDAAN_PRO_PLACE_ORDER_ENABLED = "UDAAN_PRO_PLACE_ORDER_ENABLED";
		public static final String UDAAN_CONNECT_LIFTING_ENABLED = "UDAAN_CONNECT_LIFTING_ENABLED";
		public static final String CUSTOMER_LIFTING_BLOCKED = "CUSTOMER_LIFTING_BLOCKED";
		public static final String UDAAN_CONNECT_LIFTING_BLOCKED = "UDAAN_CONNECT_LIFTING_BLOCKED";
	}

	public static class DJP {
		public static final String ADD_UI_ACTION = "ADD";
		public static final String UPDATE_UI_ACTION = "UPDATE";
		public static final String DELETE_UI_ACTION = "DELETE";
		public static final String STATISTICS_MONTH_PATTERN = "MMM,YY";
		public static final String STATISTICS_DATE_PATTERN = "dd/MM";
		public static final String DASH_CHARACTER = "-";
		public static final String RETAILER_TRANSACTION_TYPE = "Retailer";
		public static final String INFLEUNCER_TRANSACTION_TYPE = "Mitra";
	}

	public static class B2B_UNIT {
		public static final String SCL_SHREE_UNIT_UID = "SclShreeUnit";
		public static final String SCL_BANGUR_UNIT_UID = "SclBangurUnit";
		public static final String SCL_ROCKSTRONG_UNIT_UID = "SclRockstrongUnit";
		public static final String SCL_CUSTOMER_UNIT_UID = "SclCustomerUnit";
		public static final String SCL_OTHER_UNIT_UID = "SclOtherUnit";
	}

	public static class SITE {
		public static final String SHREE_SITE = "102";
		public static final String BANGUR_SITE = "103";
		public static final String ROCKSTRONG_SITE = "104";
		public static final String SCL_SITE = "scl";
	}

	public static class COUNTER_TYPE {
		public static final String RETAILER = "Retailer";
		public static final String DEALER = "Dealer";
		public static final String INFLUENCER = "Influencer";
		public static final String SITE = "Site";
	}

	public static class PRODUCT_CLASSIFICATION {
		public static final String CUSTOMER_CATEGORY = "102ClassificationCatalog/1.0/10000.customerCategory, 10002";
		public static final String PACKAGING_CONDITION = "102ClassificationCatalog/1.0/10000.packagingCond, 10004";
		public static final String STATE = "102ClassificationCatalog/1.0/10000.state, 10001";
	}

	public static class SCHEME {
		public static final String SCHME_ATTR = "SCHEME";
	}

	public static class ORDER_NOTIFICATION {
		public static final String ORDER_PLACED_APPROVED_NOTIFICATION = "Placed Order has been Approved";
		public static final String ORDER_CANCELLED_NOTIFICATION = "Placed Order has been Cancelled";
		public static final String ORDER_CREDIT_LIMIT_BREACH_NOTIFICATION="Placed Order Credit Limit has been Breached";
	}
	
	public static class STOCK_TYPE {
		public static final String GOODS_IN = "goodsIn";
		public static final String GOODS_OUT = "goodsOut";
		public static final String DISBURSED = "disbursed";
	}

	public static class ENUM_TYPE {
		public static final String REASON_FOR_SITE_LOSS = "ReasonForSiteLoss";

	}

}
