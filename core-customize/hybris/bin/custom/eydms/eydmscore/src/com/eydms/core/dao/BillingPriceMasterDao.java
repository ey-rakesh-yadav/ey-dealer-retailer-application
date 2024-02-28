package com.eydms.core.dao;

import java.util.Date;

import com.eydms.core.enums.CustomerCategory;
import com.eydms.core.model.BillingPriceMasterModel;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;

public interface BillingPriceMasterDao {

	public BillingPriceMasterModel getBillingPriceMasterForProduct(BaseSiteModel brand, String inventoryItemId, String erpCity, CustomerCategory customerCategory, String packagingCondition, String state, Date currentDate);
}
