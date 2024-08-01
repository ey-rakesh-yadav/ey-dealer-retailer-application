package com.scl.facades.process.email.context;

import com.scl.core.model.OTPEmailProcessModel;
import com.scl.core.model.SclCustomerModel;
import de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageModel;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.commerceservices.model.process.StoreFrontCustomerProcessModel;

public class sendOTPEmailContext extends CustomerEmailContext{

    private String otpCode;


    private String uid;

    @Override
    public void init(final StoreFrontCustomerProcessModel storeFrontCustomerProcessModel, final EmailPageModel emailPageModel)
    {
        super.init(storeFrontCustomerProcessModel, emailPageModel);
        if(storeFrontCustomerProcessModel instanceof OTPEmailProcessModel)
        {
            setOtpCode(((OTPEmailProcessModel) storeFrontCustomerProcessModel).getOtpCode());
            uid=((OTPEmailProcessModel) storeFrontCustomerProcessModel).getCustomer().getUid();
        }
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }



    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}
