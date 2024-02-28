/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.eydms.core.constants;

/**
 * Global class for all EyDmsCore constants. You can add global constants for your extension into this class.
 */
public final class EyDmsCoreConstants extends GeneratedEyDmsCoreConstants {
	public static final String EXTENSIONNAME = "eydmscore";


    private EyDmsCoreConstants() {
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
	public static final String CUSTOMER_QUERY_LINK = "http://eydmserp.com/Account/Login";
	public static final String EYDMS_PROS_DEALER_DUMMY_UNIT = "DummyEyDmsUnit";
	public static final String EYDMS_PROS_DEALER_DUMMY_USER_GROUP = "eydmsdealerusergroup";

	public static class CUSTOMER {
		public static final String DEALER_USER_GROUP_UID = "EyDmsDealerGroup";
		public static final String INFLUENCER_USER_GROUP_UID = "EyDmsInfluencerGroup";
		public static final String SITE_USER_GROUP_UID = "EyDmsSiteGroup";
		public static final String RETAILER_USER_GROUP_UID = "EyDmsRetailerGroup";
		public static final String SALES_PROMOTER_USER_GROUP_UID = "salespromotergroup";

		public static final String DEALER_USER_GROUP_TYPE = "Dealer";
		public static final String INFLUENCER_USER_GROUP_TYPE = "Influencer";
		public static final String SITE_USER_GROUP_TYPE = "Site";
		public static final String RETAILER_USER_GROUP_TYPE = "Retailer";
		public static final String DEFAULT_EYDMS_CUSTOMER_UNIT = "EyDmsCustomerUnit";
		
		public static final String SALES_OFFICER_GROUP_ID = "salesofficergroup";
		public static final String TSM_GROUP_ID = "tsmgroup";
		public static final String RH_GROUP_ID = "rhgroup";
		public static final String SP_GROUP_ID = "spgroup";

		public static final String TSO_GROUP_ID = "tsogroup";
		
		public static final String DEALER_ONBOARDING_USER_GROUP_UID = "EyDmsDealerOnboardingGroup";
		public static final String INFLUENCER_ONBOARDING_USER_GROUP_UID = "EyDmsInfluencerOnboardingGroup";
		public static final String RETAILER_ONBOARDING_USER_GROUP_UID = "EyDmsRetailerOnboardingGroup";

		public static final String SALES_PROMOTER_ONBOARDING_USER_GROUP_UID = "EyDmsSalesPromoterOnboardingGroup";

		//public static final String OTP_EXPIRATION_TIME = "otp.expiration.time";
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
		public static final String EYDMS_SHREE_UNIT_UID = "EyDmsShreeUnit";
		public static final String EYDMS_BANGUR_UNIT_UID = "EyDmsBangurUnit";
		public static final String EYDMS_ROCKSTRONG_UNIT_UID = "EyDmsRockstrongUnit";
		public static final String EYDMS_CUSTOMER_UNIT_UID = "EyDmsCustomerUnit";
		public static final String EYDMS_OTHER_UNIT_UID = "EyDmsOtherUnit";
	}

	public static class SITE {
		public static final String SHREE_SITE = "102";
		public static final String BANGUR_SITE = "103";
		public static final String ROCKSTRONG_SITE = "104";
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
}
