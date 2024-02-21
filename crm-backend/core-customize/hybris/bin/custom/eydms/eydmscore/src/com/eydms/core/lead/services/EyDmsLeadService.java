package com.eydms.core.lead.services;

import com.eydms.core.model.LeadModel;
import com.eydms.facades.lead.data.LeadData;
import de.hybris.platform.b2b.model.B2BCustomerModel;

import java.util.List;

public interface EyDmsLeadService {


    /**
     * Update lead model with some data
     * @param b2BCustomer
     * @param leadModel
     * @return
     */
    Boolean updateLeadEntry(final B2BCustomerModel b2BCustomer,  final LeadModel leadModel);

    /**
     * Service method to get lead by lead ID
     * @param leadID
     * @return
     */
    LeadModel findLeadByLeadId(final String leadID);

    /**
     * get all the leads
     * @return
     */
    List<LeadModel> getAllLeads();
}
