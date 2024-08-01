package com.scl.core.notifications.dao;

import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.notificationservices.dao.SiteMessageDao;
import de.hybris.platform.notificationservices.model.SiteMessageForCustomerModel;
import de.hybris.platform.notificationservices.model.SiteMessageModel;

public interface SclSiteMessageDao{
    SearchPageData<SiteMessageForCustomerModel> findPaginatedMessages(CustomerModel customer, SearchPageData searchPageData);

    SiteMessageModel findSiteMessageById(String siteMessageId);
}
