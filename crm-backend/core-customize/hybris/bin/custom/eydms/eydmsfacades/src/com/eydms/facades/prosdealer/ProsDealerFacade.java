package com.eydms.facades.prosdealer;

import com.eydms.facades.prosdealer.data.ApplicantProsDealerData;
import com.eydms.facades.prosdealer.data.BasicProsDealerData;
import com.eydms.facades.prosdealer.data.DealerBusinessDetailsData;
import com.eydms.facades.prosdealer.data.FinancialDetailsData;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import org.springframework.web.multipart.MultipartFile;

public interface ProsDealerFacade {

    void uploadDealerDocument(final String uid , final String documentType , final MultipartFile file);

    Boolean saveBasicDetails(BasicProsDealerData basicProsDealerData) throws DuplicateUidException;

    Boolean saveApplicantDetails(ApplicantProsDealerData applicantProsDealerData);

    Boolean saveDealerFinancialDetails(FinancialDetailsData financialDetailsData);

    Boolean saveDealerBusinessDetails(DealerBusinessDetailsData dealerBusinessDetailsData);

    boolean sendSmsForDealerBasicDetails(String username);

    boolean sendSmsForDealerFinancialDetails(String username);
}
