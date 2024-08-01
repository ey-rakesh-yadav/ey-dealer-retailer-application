package com.scl.facades.impl;

import com.scl.core.notifications.service.SclNotificationService;
import com.scl.facades.SclNotificationFacade;
import org.springframework.beans.factory.annotation.Autowired;

public class SclNotificationFacadeImpl implements SclNotificationFacade {

    @Autowired
    SclNotificationService sclNotificationService;

    @Override
    public boolean updateNotificationStatus(String siteMessageId) {
        return sclNotificationService.updateNotificationStatus(siteMessageId);
    }
}
