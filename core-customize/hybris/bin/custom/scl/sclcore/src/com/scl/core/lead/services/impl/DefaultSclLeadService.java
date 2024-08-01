package com.scl.core.lead.services.impl;


import com.scl.core.lead.dao.SclLeadDao;
import com.scl.core.lead.services.SclLeadService;
import com.scl.core.model.LeadModel;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

public class DefaultSclLeadService implements SclLeadService {

    private ModelService modelService;
    private SclLeadDao sclLeadDao;

    private static final Logger LOG = Logger.getLogger(DefaultSclLeadService.class);

    /**
     * Update lead model with some data
     * @param b2BCustomer
     * @param leadModel
     * @return
     */

    @Override
    public Boolean updateLeadEntry(final B2BCustomerModel b2BCustomer,  final LeadModel leadModel){

        leadModel.setModifiedBy(b2BCustomer);

        try{
            getModelService().save(leadModel);
        }
        catch (ModelSavingException mse){
            LOG.error("Error occured while updating Lead "+leadModel.getLeadId()+"\n");
            LOG.error("Exception is: "+mse.getMessage());
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * Service method to get lead by lead ID
     * @param leadID
     * @return
     */
    @Override
    public LeadModel findLeadByLeadId(final String leadID){
        return getSclLeadDao().getLeadByLeadID(leadID);
    }

    /**
     * get all the leads
     * @return
     */
    @Override
    public List<LeadModel> getAllLeads(){
        return getSclLeadDao().getAllLeadsData();
    }


    public ModelService getModelService() {
        return modelService;
    }

    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

    public SclLeadDao getSclLeadDao() {
        return sclLeadDao;
    }

    public void setSclLeadDao(SclLeadDao sclLeadDao) {
        this.sclLeadDao = sclLeadDao;
    }
}
