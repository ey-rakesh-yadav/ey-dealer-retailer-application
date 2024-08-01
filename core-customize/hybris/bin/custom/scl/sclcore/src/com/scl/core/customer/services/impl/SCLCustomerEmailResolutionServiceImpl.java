package com.scl.core.customer.services.impl;

import com.scl.core.customer.services.SCLCustomerEmailResolutionService;
import com.scl.core.model.SclCustomerModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commerceservices.customer.impl.DefaultCustomerEmailResolutionService;
import de.hybris.platform.commerceservices.enums.CustomerType;
import de.hybris.platform.commerceservices.multisite.MultiSiteUidDecorationService;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.util.mail.MailUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.log4j.Logger;

import java.util.Locale;
import java.util.Objects;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

public class SCLCustomerEmailResolutionServiceImpl  extends DefaultCustomerEmailResolutionService implements SCLCustomerEmailResolutionService {

    private static final Logger LOG = Logger.getLogger(SCLCustomerEmailResolutionServiceImpl.class);

    /**
     * @param customerModel the customer
     * @return
     */

    @Override
    public String getEmailForCustomer(final CustomerModel customerModel)
    {
        final String emailAfterProcessing = validateAndProcessEmailForCustomer(customerModel);
        if (StringUtils.isNotEmpty(emailAfterProcessing))
        {
            return emailAfterProcessing;
        }

        return getConfigurationService().getConfiguration().getString(DEFAULT_CUSTOMER_KEY, DEFAULT_CUSTOMER_EMAIL);
    }

    protected String validateAndProcessEmailForCustomer(final CustomerModel customerModel)
    {
        validateParameterNotNullStandardMessage("customerModel", customerModel);
        String email = null;
        final String originalUid = customerModel.getOriginalUid();

        if(customerModel instanceof SclCustomerModel) {
            SclCustomerModel sclCustomer = (SclCustomerModel) customerModel;

            if (Objects.nonNull(originalUid)) {
                email = CustomerType.GUEST.equals(sclCustomer.getType())
                        ? StringUtils.substringAfter(sclCustomer.getOriginalUid(), "|").toLowerCase(Locale.getDefault())
                        : sclCustomer.getEmail();

            }

            if (StringUtils.isEmpty(email)) {
                LOG.info(String.format("Contact Email Address missing for Dealer %s", customerModel.getUid()));
            } else {
                try {
                    MailUtils.validateEmailAddress(email, "customer email");
                    return email;
                } catch (final EmailException e) {
                    LOG.info("Given uid is not appropriate email. Customer UID: " + String.valueOf(customerModel.getUid()) + " Exception: "
                            + e.getClass().getName());
                }
            }
        }else{
            LOG.info(String.format("Customer is not type of SclCustomer %s", customerModel.getUid()));
        }
        return null;
    }
}
