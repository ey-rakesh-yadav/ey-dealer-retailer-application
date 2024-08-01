package com.scl.core.job;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.scl.core.dao.CollectionDao;
import com.scl.core.model.CashDiscountLostModel;
import com.scl.core.model.CashDiscountSlabsModel;
import com.scl.core.model.InvoiceMasterModel;

import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.model.ModelService;

public class CollectionLostDiscountCalculationJob extends AbstractJobPerformable<CronJobModel>{

	@Resource
	CollectionDao collectionDao;
	
	@Resource
	ModelService modelService;
	
	
	@Override
	public PerformResult perform(CronJobModel arg0) {
		
		List<CashDiscountSlabsModel> cdSlabList = collectionDao.getAllCashDiscountSlabs();
		
		Calendar cal = Calendar.getInstance();
		
		Date currentTime = cal.getTime();
		
		cal.add(Calendar.DATE, -1);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		Date startDate = cal.getTime();
		
		cal.set(Calendar.HOUR, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 999);
		
		Date endDate = cal.getTime();
		
		
		List<CashDiscountLostModel>  cdLostModelList = new ArrayList<>();
		
		for(CashDiscountSlabsModel cdSlab : cdSlabList)
		{
			int endDay = cdSlab.getEndDay();
			
			cal.setTime(startDate);
			cal.add(Calendar.DATE, -endDay);
			
			startDate = cal.getTime();
			
			cal.setTime(endDate);
			cal.add(Calendar.DATE, -endDay);
			
			endDate = cal.getTime();
			
			String customerNo = cdSlab.getCustomerNo();
			
			List<InvoiceMasterModel> invoiceList = collectionDao.getNonReconciledInvoices(customerNo, startDate, endDate);
			
			for(InvoiceMasterModel invoice : invoiceList)
			{
				double invoiceAmount = invoice.getInvoiceAmount();
				double currentDiscount = cdSlab.getDiscount();
				double nextDiscount = collectionDao.getNextSlabDiscount(customerNo, currentDiscount);
				
				double lostDiscount;
				
				if(nextDiscount!=0.0)
				{
					CashDiscountLostModel cdLostModel = modelService.create(CashDiscountLostModel.class);
					
					cdLostModel.setCustomerNo(customerNo);
					cdLostModel.setInvoiceNo(invoice.getInvoiceNo());
					
					if(currentDiscount!=0.0)
					{
						lostDiscount = (invoiceAmount/currentDiscount) - (invoiceAmount/nextDiscount);
						cdLostModel.setLostDiscount(lostDiscount);
						cdLostModel.setDiscountLostDate(currentTime);
					}
					
					cdLostModelList.add(cdLostModel);
				}
			}
		}
		
		modelService.saveAll(cdLostModelList);
		
		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}

}
