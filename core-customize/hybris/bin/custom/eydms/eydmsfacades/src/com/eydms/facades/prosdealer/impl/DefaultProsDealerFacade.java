package com.eydms.facades.prosdealer.impl;

import com.eydms.core.model.ProspectiveDealerModel;
import com.eydms.core.prosdealer.service.ProsDealerService;
import com.eydms.facades.prosdealer.ProsDealerFacade;
import com.eydms.facades.prosdealer.data.ApplicantProsDealerData;
import com.eydms.facades.prosdealer.data.BasicProsDealerData;
import com.eydms.facades.prosdealer.data.DealerBusinessDetailsData;
import com.eydms.facades.prosdealer.data.FinancialDetailsData;
import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

public class DefaultProsDealerFacade implements ProsDealerFacade {

    private ProsDealerService prosDealerService;

    @Autowired
    UserFacade userFacade;

    @Override
    public void uploadDealerDocument(String uid, String documentType, MultipartFile file) {
        ProspectiveDealerModel prospectiveDealer = getProsDealerService().getProsDealerByUid(uid);
        getProsDealerService().uploadDealerDocument(prospectiveDealer,documentType,file);
    }

    @Override
    public Boolean saveBasicDetails(BasicProsDealerData basicProsDealerData) throws DuplicateUidException {
        if (userFacade.isUserExisting(basicProsDealerData.getMobileNo()))
        {
            throw new DuplicateUidException("Duplicate Prospective Dealer Found " + basicProsDealerData.getMobileNo());
        }
        return prosDealerService.saveBasicDetails(basicProsDealerData);
    }

    @Override
    public Boolean saveApplicantDetails(ApplicantProsDealerData applicantProsDealerData) {
        return prosDealerService.saveApplicantDetails(applicantProsDealerData);
    }

    @Override
    public Boolean saveDealerFinancialDetails(FinancialDetailsData financialDetailsData) {
        return prosDealerService.saveDealerFinancialDetails(financialDetailsData);
    }

    @Override
    public Boolean saveDealerBusinessDetails(DealerBusinessDetailsData dealerBusinessDetailsData) {
        return prosDealerService.saveDealerBusinessDetails(dealerBusinessDetailsData);
    }

    @Override
    public boolean sendSmsForDealerBasicDetails(String username) {
        return prosDealerService.sendSmsForDealerBasicDetails(username);
    }

    @Override
    public boolean sendSmsForDealerFinancialDetails(String username) {
        return prosDealerService.sendSmsForDealerFinancialDetails(username);
    }

    public ProsDealerService getProsDealerService() {
        return prosDealerService;
    }

    public void setProsDealerService(ProsDealerService prosDealerService) {
        this.prosDealerService = prosDealerService;
    }

}
