package com.scl.core.prosdealer.service;

import com.scl.core.enums.OnboardingStatus;
import com.scl.core.model.DistrictModel;
import com.scl.core.model.ProspectiveDealerModel;
import com.scl.core.model.SclUserModel;
import com.scl.facades.prosdealer.data.ApplicantProsDealerData;
import com.scl.facades.prosdealer.data.BasicProsDealerData;
import com.scl.facades.prosdealer.data.DealerBusinessDetailsData;
import com.scl.facades.prosdealer.data.FinancialDetailsData;
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

    List<ProspectiveDealerModel> fetchProsDealerPendingForSOAssignment(final SclUserModel districtInCharge);

    boolean sendSmsForDealerBasicDetails(String username);

    boolean sendSmsForDealerFinancialDetails(String username);
}
