package com.eydms.core.prosdealer.service;

import com.eydms.core.enums.OnboardingStatus;
import com.eydms.core.model.DistrictModel;
import com.eydms.core.model.ProspectiveDealerModel;
import com.eydms.core.model.EyDmsUserModel;
import com.eydms.facades.prosdealer.data.ApplicantProsDealerData;
import com.eydms.facades.prosdealer.data.BasicProsDealerData;
import com.eydms.facades.prosdealer.data.DealerBusinessDetailsData;
import com.eydms.facades.prosdealer.data.FinancialDetailsData;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
public interface ProsDealerService {

    ProspectiveDealerModel getProsDealerByCode(final String dealerCode);

    ProspectiveDealerModel getProsDealerByUid(final String uid);

    void uploadDealerDocument(final ProspectiveDealerModel prosDealer , final String documentType , final MultipartFile file);

    List<ProspectiveDealerModel> getProsDealerForSOCustomerQueryAlert();

    List<ProspectiveDealerModel> getProsDealerForSHCustomerQueryAlert();

    Boolean saveBasicDetails(BasicProsDealerData basicProsDealerData);

    Boolean saveApplicantDetails(ApplicantProsDealerData applicantProsDealerData);

    Boolean saveDealerFinancialDetails(FinancialDetailsData financialDetailsData);

    Boolean saveDealerBusinessDetails(DealerBusinessDetailsData dealerBusinessDetailsData);

    List<ProspectiveDealerModel> fetchProsDealerByDistrictAndOnboardingStatus(final OnboardingStatus onboardingStatus, final DistrictModel district);

    List<ProspectiveDealerModel> fetchProsDealerPendingForSOAssignment(final EyDmsUserModel districtInCharge);

    boolean sendSmsForDealerBasicDetails(String username);

    boolean sendSmsForDealerFinancialDetails(String username);
}
