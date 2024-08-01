package com.scl.integration.cpi.stagegate.impl;

import com.scl.core.dao.DataConstraintDao;
import com.scl.core.model.DeliveryItemModel;
import com.scl.core.model.SclOutboundStageGateModel;
import com.scl.integration.cpi.stagegate.SclSapCpiOutboundStageGateConversionService;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;

public class SclSapCpiOutboundStageGateConversionServiceimpl implements SclSapCpiOutboundStageGateConversionService {

    private static final Logger LOG = Logger.getLogger(SclSapCpiOutboundStageGateConversionServiceimpl.class);

    @Autowired
    BaseSiteService baseSiteService;

    @Autowired
    UserService userService;

    @Resource
    DataConstraintDao dataConstraintDao;

    @Override
    public SclOutboundStageGateModel convertDIToStageGateOutBound(DeliveryItemModel di, OrderEntryModel oe) {
        LOG.info("convertDIToStageGateOutBound getting called");

        SclOutboundStageGateModel sclOutboundStageGateModel = new SclOutboundStageGateModel();

        sclOutboundStageGateModel.setErpOrderId(oe.getOrder().getErpOrderNumber());
        sclOutboundStageGateModel.setDiNumber(di.getDiNumber());
        sclOutboundStageGateModel.setCrmOrdrCode(oe.getOrder().getCode());
        sclOutboundStageGateModel.setDeliveryLineNumber(di.getDeliveryLineNumber());
        sclOutboundStageGateModel.setEntryNumber(oe.getEntryNumber());
        sclOutboundStageGateModel.setErpLineItemId(oe.getErpLineItemId());

        return sclOutboundStageGateModel;
    }
}