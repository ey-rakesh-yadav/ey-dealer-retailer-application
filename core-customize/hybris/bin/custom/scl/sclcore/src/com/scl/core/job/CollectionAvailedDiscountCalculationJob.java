package com.scl.core.job;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Resource;

import com.scl.core.dao.CollectionDao;
import com.scl.core.model.CashDiscountAvailedModel;
import com.scl.core.model.InvoiceMasterModel;

import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.model.ModelService;

public class CollectionAvailedDiscountCalculationJob extends AbstractJobPerformable<CronJobModel>{

	@Resource
	CollectionDao collectionDao;
	
	@Resource
	ModelService modelService;
	
	@Override
	public PerformResult perform(CronJobModel arg0) {
		
		List<InvoiceMasterModel> invoiceList = collectionDao.getReconciledInvoices();
		
		List<CashDiscountAvailedModel> cdModelList = new ArrayList<>();
		
		for(InvoiceMasterModel invoice : invoiceList)
		{
			String invoiceNo = invoice.getInvoiceNo();
			String customerNo = invoice.getCustomerNo();
			CashDiscountAvailedModel cdAvailedModel = collectionDao.getCashDiscountAvailedModel(customerNo, invoiceNo);
			
			if(Objects.isNull(cdAvailedModel))
			{
				cdAvailedModel = modelService.create(CashDiscountAvailedModel.class);
				cdAvailedModel.setCustomerNo(customerNo);
				cdAvailedModel.setInvoiceNo(invoiceNo);
				
			}
			
			cdAvailedModel.setAvailedDiscount(invoice.getInvoiceAmount()-invoice.getFinalReconciledInvoiceAmount());
			cdAvailedModel.setDiscountAvailedDate(invoice.getReconcilationDate());
			
			cdModelList.add(cdAvailedModel);

		}
		
		modelService.saveAll(cdModelList);
		
		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}

}
