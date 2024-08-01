package com.scl.facades.populators.lead;

import com.scl.core.model.LeadModel;
import com.scl.facades.lead.data.LeadData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class SclLeadPopulator implements Populator<LeadModel, LeadData> {

    @Override
    public void populate(LeadModel source, LeadData target) throws ConversionException {

       // target.setLeadId(source.getLeadId());
        target.setFirstName(source.getFirstName());
        target.setLastName(source.getLastName());
        target.setAnnualRevenue(source.getAnnualRevenue());
        target.setCampaignSource(source.getCampaignSource());
        //target.setCity(source.getCity());
        target.setEmail(source.getEmail());
        target.setDescription(source.getDescription());
        target.setCompany(source.getCompany());
        target.setFax(source.getFax());
        target.setMobile(source.getMobile());
        //target.setCountry(source.getCountry());
        target.setIndustryType(source.getIndustryType());
        target.setLeadSource(source.getLeadSource());
        target.setLeadStatus(source.getLeadStatus().getCode());
        target.setNoOfEmployees(source.getNoOfEmployees());
        target.setPhone(source.getPhone());
        target.setLeadQualificationLevel(source.getLeadQualificationLevel().getCode());
        //target.setState(source.getState());
        //target.setStreet(source.getStreet());
        target.setTitle(source.getTitle());
        target.setWebsite(source.getWebsite());
        //target.setZipcode(source.getZipCode());
        target.setAccountId(source.getAccountId());
        target.setAccountName(source.getAccountName());
    }
}
