package com.scl.facades.populators;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.scl.core.constants.SclCoreConstants;
import com.scl.core.model.LedgerDetailsModel;
import com.scl.facades.data.CollectionLedgerData;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

public class LedgerPopulator implements Populator<LedgerDetailsModel,CollectionLedgerData>{

	@Override
	public void populate(LedgerDetailsModel source, CollectionLedgerData target) throws ConversionException {
		
		target.setCustomerNo(source.getCustomerNo());
		target.setNarration(source.getNarration());
		
		DateFormat dateFormat = new SimpleDateFormat(SclCoreConstants.CUSTOMER_ACCOUNT.DATE_FORMAT_1);
		String date = dateFormat.format(source.getDate());
		
		target.setDate(date);
		target.setOrgId(source.getBrand());
		target.setCreditAmount(source.getCreditAmount());
		target.setDebitAmount(source.getDebitAmount());
		target.setOpeningBalance(source.getOpeningBalance());
		target.setClosingBalance(source.getClosingBalance());
		target.setChqNo(source.getChqNo());
		target.setInvoiceQty(source.getInvoiceQty());
		target.setDocNo(source.getDocNo());
		target.setTransactionType(source.getTransactionType());
		
	}

}
