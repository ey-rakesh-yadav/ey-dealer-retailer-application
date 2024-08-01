package com.scl.facades.populators.prosdealer;

import com.scl.core.model.NominationModel;
import com.scl.core.model.PartnershipModel;
import com.scl.core.model.ProsDealerCompanyModel;
import com.scl.core.model.ProspectiveDealerModel;
import com.scl.facades.prosdealer.data.*;
import de.hybris.platform.converters.Populator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ApplicantDetailsPopulator implements Populator<ProspectiveDealerModel, ProsDealerData> {

    @Override
    public void populate(ProspectiveDealerModel source, ProsDealerData target) {

        List<ProsDealerCompanyModel> companies = new ArrayList<>(source.getCompanies());
        List<ProsDealerCompanyData> companyDataList = new ArrayList<>();

        List<PartnershipModel> partners = new ArrayList<>(source.getPartners());
        List<PartnershipData> partnershipDataList = new ArrayList<>();

        if(CollectionUtils.isNotEmpty(companies)){

            for(ProsDealerCompanyModel company: companies){
                ProsDealerCompanyData companyData = new ProsDealerCompanyData();
                companyData.setBanker(company.getBanker());
                //TODO://UPDATE CODE TO SET ADDRESS
                companyData.setAddress(company.getAddress());
                companyData.setFathersName(company.getFatherName());
                companyData.setPanNo(company.getPanNo());
                companyData.setDinNo(company.getDinNo());
                companyData.setNameOfDirector(company.getNameOfDirector());
                companyData.setCompanyID(company.getCompanyID());
                companyData.setBanker(company.getBanker());
                companyDataList.add(companyData);
            }
        }

        if(CollectionUtils.isNotEmpty(partners)){

            for(PartnershipModel partner : partners){

                PartnershipData partnershipData = new PartnershipData();

                partnershipData.setNameOfPartner(partner.getName());
                partnershipData.setRelation(partner.getRelation());
                partnershipData.setPartnerID(partner.getPartnerID());
                //partnershipData.setAddress(partner.getAddress());
                partnershipDataList.add(partnershipData);
            }
        }

        ApplicantProsDealerData applicantProsDealerData = new ApplicantProsDealerData();
        //TODO:: UPDATE CODE TO SET PROPER CODE
        applicantProsDealerData.setCode(source.getUid());
        applicantProsDealerData.setStatusOfApplicant(null != source.getStatusOfApplicant() ? source.getStatusOfApplicant().getCode() : StringUtils.EMPTY);
        applicantProsDealerData.setCompanies(companyDataList);
        applicantProsDealerData.setPartnerships(partnershipDataList);
        target.setApplicantDetails(applicantProsDealerData);
    }
}