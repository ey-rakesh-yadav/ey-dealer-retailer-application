package com.scl.integration.cpi.hook;

import com.scl.core.enums.NotificationCategory;
import com.scl.core.model.CreditAndOutstandingModel;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.services.SlctCrmIntegrationService;
import com.scl.integration.constants.SclintegrationConstants;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.notificationservices.enums.NotificationType;
import de.hybris.platform.notificationservices.enums.SiteMessageType;
import de.hybris.platform.notificationservices.model.SiteMessageForCustomerModel;
import de.hybris.platform.notificationservices.model.SiteMessageModel;
import de.hybris.platform.odata2services.odata.persistence.hook.PostPersistHook;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

public class SclCreditLimitPostPersistHook implements PostPersistHook {

    @Autowired
    UserService userService;

    @Resource
    ModelService modelService;

    @Autowired
    SlctCrmIntegrationService slctCrmIntegrationService;

    private static final Logger LOG = Logger.getLogger(SclOrderPostPersistHook.class);
    @Override
    public void execute(ItemModel item) {
        if (item instanceof SclCustomerModel) {
            LOG.info("The persistence hook SclCreditLimitPostPersistHook is called!");
            SclCustomerModel sclCust= (SclCustomerModel) item;
            try {
                if (null!= sclCust.getCreditLimit() && null!= sclCust.getSecurityDepositAmount()) {
                    CreditAndOutstandingModel co=slctCrmIntegrationService.getCrmOutstandingDetails(sclCust.getUid());
                    if (Objects.isNull(co)){
                        co=new CreditAndOutstandingModel();
                        co.setCreditLimit(sclCust.getCreditLimit());
                        co.setSecurityDeposit(Double.valueOf(sclCust.getSecurityDepositAmount()));
                        co.setCustomerCode(sclCust.getUid());
                        modelService.save(co);
                    }
                    else {
                        co.setCreditLimit(sclCust.getCreditLimit());
                        co.setSecurityDeposit(Double.valueOf(sclCust.getSecurityDepositAmount()));
                        modelService.save(co);
                    }
                }
            }catch (RuntimeException e){
                LOG.info("SclCreditLimitPostPersistHook for cust id: "+ sclCust.getUid() + " Exception: "+ e.getMessage() );
                e.printStackTrace();
            }
        }
    }

}
