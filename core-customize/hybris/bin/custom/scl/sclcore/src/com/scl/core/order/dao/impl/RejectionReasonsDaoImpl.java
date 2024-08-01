package com.scl.core.order.dao.impl;

import com.scl.core.model.RejectionReasonModel;
import com.scl.core.order.dao.RejectionReasonsDao;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.List;

public class RejectionReasonsDaoImpl extends DefaultGenericDao<RejectionReasonModel> implements RejectionReasonsDao {

    public RejectionReasonsDaoImpl() {
        super(RejectionReasonModel._TYPECODE);
    }


    /**
     * @return
     */
    @Override
    public List<RejectionReasonModel> getRejectionReasons() {
          List<RejectionReasonModel> rejectionReasonModels=this.find();
          return CollectionUtils.isNotEmpty(rejectionReasonModels)?rejectionReasonModels: Collections.EMPTY_LIST;
    }
}
