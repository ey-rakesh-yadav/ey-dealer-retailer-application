package com.scl.core.event;

import de.hybris.platform.core.model.user.PhoneContactInfoModel;
import de.hybris.platform.servicelayer.event.events.AbstractEvent;

public class UpdateContactNumberEvent extends AbstractEvent {

    private final PhoneContactInfoModel phoneContactInfoModel;

    public UpdateContactNumberEvent(PhoneContactInfoModel phoneContactInfoModel) {
        this.phoneContactInfoModel = phoneContactInfoModel;
    }

    public PhoneContactInfoModel getPhoneContactInfoModel(){
        return phoneContactInfoModel;
    }
}
