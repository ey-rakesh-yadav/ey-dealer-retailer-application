package com.eydms.facades.lead;

import com.eydms.facades.lead.data.LeadData;

public interface EyDmsLeadFacade {

    /**
     * create or update lead model with lead data
     * @param leadData
     * @return
     */
    String updateLead(final LeadData leadData);

    /**
     * fetches lead with given leadID
     * @param leadId
     * @return
     */
    LeadData getLeadForLeadId(final String leadId);
}
