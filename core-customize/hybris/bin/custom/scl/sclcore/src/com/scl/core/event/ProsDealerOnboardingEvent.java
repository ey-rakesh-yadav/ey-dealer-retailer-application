package com.scl.core.event;

import com.scl.core.model.ProspectiveDealerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commerceservices.event.AbstractCommerceUserEvent;

public class ProsDealerOnboardingEvent extends AbstractCommerceUserEvent<BaseSiteModel> {


    private final ProspectiveDealerModel prospectiveDealer;

    public ProsDealerOnboardingEvent(ProspectiveDealerModel prospectiveDealer){
        super();
        this.prospectiveDealer = prospectiveDealer;
    }

    public ProspectiveDealerModel getProspectiveDealer() {
        return prospectiveDealer;
    }

}
