package com.eydms.facades.impl;

import com.eydms.core.notifications.service.EyDmsNotificationService;
import com.eydms.facades.EyDmsNotificationFacade;
import org.springframework.beans.factory.annotation.Autowired;

public class EyDmsNotificationFacadeImpl implements EyDmsNotificationFacade {

    @Autowired
    EyDmsNotificationService eydmsNotificationService;

    @Override
    public boolean updateNotificationStatus(String siteMessageId) {
        return eydmsNotificationService.updateNotificationStatus(siteMessageId);
    }
}
