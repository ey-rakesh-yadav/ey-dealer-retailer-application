package com.scl.facades.populators;

import com.scl.core.dao.DataConstraintDao;
import com.scl.core.model.PartnerCustomerModel;
import com.scl.core.model.SclCustomerModel;
import com.scl.core.services.DealerService;
import com.scl.facades.data.PartnerCustomerData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.*;
import java.util.Date;

public class PartnerCustomerReversePopulator implements Populator<PartnerCustomerData, PartnerCustomerModel> {

    @Autowired
    UserService userService;

    @Autowired
    ModelService modelService;

    @Autowired
    DataConstraintDao dataConstraintDao;

    @Autowired
    DealerService dealerService;

    public void populate(PartnerCustomerData partnerCustomerData, PartnerCustomerModel partnerCustomerModel) {
        LocalDate currentDate = LocalDate.now();
        partnerCustomerModel.setName(partnerCustomerData.getName());
        partnerCustomerModel.setMobileNumber(partnerCustomerData.getMobileNumber());
        partnerCustomerModel.setRole(partnerCustomerData.getRole());
        partnerCustomerModel.setValidity(partnerCustomerData.getValidityInMonths());

        Date validityExpiredDate = dealerService.getValidityExpiredDate(partnerCustomerData.getValidityInMonths(), currentDate);
        partnerCustomerModel.setValidityExpired(validityExpiredDate);

        partnerCustomerModel.setActive(Boolean.TRUE);
    }

}
