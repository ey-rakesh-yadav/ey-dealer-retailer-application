package com.scl.facades.lead.impl;

import com.scl.core.lead.services.SclLeadService;
import com.scl.core.model.LeadModel;
import com.scl.facades.lead.data.LeadData;
import com.scl.facades.lead.SclLeadFacade;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Resource;

import java.util.UUID;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

public class DefaultSclLeadFacade implements SclLeadFacade {

    private ModelService modelService;
    private SclLeadService sclLeadService;
    private Converter<LeadData, LeadModel> sclLeadReverseConverter;
    private Converter<LeadModel , LeadData> sclLeadConverter;

    /**
     * create or update lead model with lead data
     * @param leadData
     * @return
     */
    @Override
    public String updateLead(LeadData leadData) {
        validateParameterNotNullStandardMessage("leadData", leadData);
        LeadModel leadModel;
        String leadId = UUID.randomUUID().toString();
        if(StringUtils.isBlank(leadData.getLeadId())){
            leadModel = modelService.create(LeadModel.class);
            leadModel.setLeadId(leadId);
        }
        else {
            leadModel = sclLeadService.findLeadByLeadId(leadData.getLeadId());
            leadId = leadData.getLeadId();
        }
        modelService.save(sclLeadReverseConverter.convert(leadData,leadModel));
        return leadId;
    }

    /**
     * fetches lead with given leadID
     * @param leadId
     * @return
     */
    @Override
    public LeadData getLeadForLeadId(String leadId) {

         LeadData leadData = new LeadData();
         final LeadModel leadModel = sclLeadService.findLeadByLeadId(leadId);
         return sclLeadConverter.convert(leadModel,leadData);
    }

    public ModelService getModelService() {
        return modelService;
    }

    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

    public SclLeadService getSclLeadService() {
        return sclLeadService;
    }

    public void setSclLeadService(SclLeadService sclLeadService) {
        this.sclLeadService = sclLeadService;
    }

    public Converter<LeadData, LeadModel> getSclLeadReverseConverter() {
        return sclLeadReverseConverter;
    }

    public void setSclLeadReverseConverter(Converter<LeadData, LeadModel> sclLeadReverseConverter) {
        this.sclLeadReverseConverter = sclLeadReverseConverter;
    }

    public Converter<LeadModel, LeadData> getSclLeadConverter() {
        return sclLeadConverter;
    }

    public void setSclLeadConverter(Converter<LeadModel, LeadData> sclLeadConverter) {
        this.sclLeadConverter = sclLeadConverter;
    }

}
