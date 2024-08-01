package com.scl.core.interceptor;

import com.scl.core.model.MasterStockAllocationModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class MasterStockAllocationPrepareInterceptor implements PrepareInterceptor<MasterStockAllocationModel> {
    Logger log = LoggerFactory.getLogger(MasterStockAllocationPrepareInterceptor.class);


    @Autowired
    KeyGenerator masterStockAllocationIdGenerator;

    @Autowired
    ModelService modelService;



    /**
     * @param masterStockAllocationModel
     * @param interceptorContext
     * @throws InterceptorException
     */
    @Override
    public void onPrepare(MasterStockAllocationModel masterStockAllocationModel, InterceptorContext interceptorContext) throws InterceptorException {
        log.info("masterStockAllocationPrepareInterceptor called....");

        if(modelService.isNew(masterStockAllocationModel)) {
            masterStockAllocationModel.setId(masterStockAllocationIdGenerator.generate().toString());
        }

    }
}
